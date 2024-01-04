package com.onefin.ewallet.bank.service.vietin;

import com.google.common.base.Strings;
import com.onefin.ewallet.bank.common.VietinConstants;
import com.onefin.ewallet.bank.dto.vietin.*;
import com.onefin.ewallet.bank.repository.jpa.PartnerErrorCodeRepo;
import com.onefin.ewallet.bank.repository.jpa.VirtualAcctRepo;
import com.onefin.ewallet.bank.repository.jpa.VirtualAcctStatusHistoryRepo;
import com.onefin.ewallet.bank.repository.jpa.VirtualAcctTransHistoryRepo;
import com.onefin.ewallet.bank.repository.redis.VirtualAcctTransWatchingRepo;
import com.onefin.ewallet.bank.service.common.ConfigLoader;
import com.onefin.ewallet.bank.service.common.NumberSequenceService;
import com.onefin.ewallet.common.base.constants.OneFinConstants;
import com.onefin.ewallet.common.base.errorhandler.RuntimeInternalServerException;
import com.onefin.ewallet.common.base.service.BackupService;
import com.onefin.ewallet.common.base.service.RestTemplateHelper;
import com.onefin.ewallet.common.domain.bank.vietin.VietinNotifyTransTable;
import com.onefin.ewallet.common.domain.bank.vietin.VietinVirtualAcctStatusHistory;
import com.onefin.ewallet.common.domain.bank.vietin.VietinVirtualAcctTable;
import com.onefin.ewallet.common.domain.bank.vietin.VietinVirtualAcctTransHistory;
import com.onefin.ewallet.common.domain.errorCode.PartnerErrorCode;
import com.onefin.ewallet.common.utility.date.DateTimeHelper;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.joda.time.DateTime;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import static com.onefin.ewallet.bank.common.VietinConstants.VIR_ACCT_ORDER_EXIST;

@Service
public class VietinVirtualAcct {

	private static final Logger LOGGER = LoggerFactory.getLogger(VietinVirtualAcct.class);

	private static final org.apache.logging.log4j.Logger LOGGER1 = LogManager.getLogger(VietinVirtualAcct.class);

	private static final int MAX_WAIT_TIME = 60_000; //60 seconds

	private static final String DEFAULT_SCHOOL_POOL_VIRTUAL_ACCT = "1";
	private static final int WAIT_INTERVAL = 5_000; // 1 second

	private final int TIME_OUT_CONSTANT = 24;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private ConfigLoader configLoader;

	@Autowired
	private VirtualAcctRepo virtualAcctRepo;

	@Autowired
	private VietinEncryptUtil encryptUtil;

	@Autowired
	private BackupService backupService;

	@Autowired
	private NumberSequenceService numberSequenceService;

	@Autowired
	private DateTimeHelper dateTimeHelper;

	@Autowired
	private PartnerErrorCodeRepo partnerErrorCodeRepo;

	@Autowired
	private RestTemplateHelper restTemplateHelper;

	@Autowired
	private VirtualAcctTransHistoryRepo virtualAcctTransHistoryRepo;

	@Autowired
	private VietinRequestUtil IHTTPRequestUtil;

	@Autowired
	private VirtualAcctStatusHistoryRepo virtualAcctStatusHistoryRepo;

	@Autowired
	private VirtualAcctTransHistoryRepo vietVirtualAcctTransHistoryRepo;

	@Autowired
	private VirtualAcctTransWatchingRepo virtualAcctTransWatchingRepo;

	private final static HashMap<String, NotifyErrorsResponse> errorCode = new HashMap<>();

	static {
		errorCode.put("00", new NotifyErrorsResponse("00", "Nhận giao dịch thành công"));
		errorCode.put("01", new NotifyErrorsResponse("01", "Không xác thực được chữ ký số"));
		errorCode.put("02", new NotifyErrorsResponse("02", "Mã KH/Tài khoản không tồn tại"));
		errorCode.put("03", new NotifyErrorsResponse("03", "Lỗi xảy ra khi gạch nợ/tăng sức mua"));
		errorCode.put("99", new NotifyErrorsResponse("99", "Lỗi không xác định"));
	}

	public String getTransTime() {
		return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
	}

	public ConnResponse buildCreateResponseEntity(VirtualAcctCreateRequest requestBody) throws Exception {

		try {// Build Vietin virtual acct create request
			VietinVirtualAcctCreate requestMap = buildVietinCreateVirtualAcct(requestBody);

			// Backup request
			backUpRequestResponse(requestBody.getRequestId(), requestBody, null);

			ResponseEntity<VietinVirtualAcctCreateBaseResponse> responseBaseEntity = null;
			VietinVirtualAcctCreateBaseResponse responseBase = null;
			ConnResponse responseEntity = null;

			// Request create api
			responseBaseEntity = IHTTPRequestUtil.sendVirtualAcctCreate(requestMap);

			// insert to Virtual acct table
			responseBase = responseBaseEntity.getBody();
			VietinVirtualAcctTable vietinVirtualAcctTable = modelMapper.map(requestMap, VietinVirtualAcctTable.class);

			vietinVirtualAcctTable.setQrURL(responseBase.getQrURL());
			vietinVirtualAcctTable.setVirtualAcctId(responseBase.getVirtualAcctId());
			vietinVirtualAcctTable.setPoolName(requestBody.getPoolName());
			vietinVirtualAcctTable.setInUse(false);
			vietinVirtualAcctTable.setBankCode(OneFinConstants.BankListQrService.CTG.getBankCode());

			//validate response

			responseEntity = validateAndBuildResponse(responseBase, responseBaseEntity.getStatusCode(), requestMap.getLanguage());

			if (responseBase.getStatus().getCode().equals("1")) {
				virtualAcctRepo.save(vietinVirtualAcctTable);
				VietinVirtualAcctStatusHistory vietinVirtualAcctStatusHistory = new VietinVirtualAcctStatusHistory();
				vietinVirtualAcctStatusHistory.setRequestId(requestMap.getRequestId());
				vietinVirtualAcctStatusHistory.setVirtualAcctCode(requestMap.getVirtualAcctCode());
				vietinVirtualAcctStatusHistory.setVirtualAcctVar(requestMap.getVirtualAcctVar());
				// default in active status
				vietinVirtualAcctStatusHistory.setNewStatus("0");
				vietinVirtualAcctStatusHistory.setStatusCode(responseBase.getStatus().getCode());
				virtualAcctStatusHistoryRepo.save(vietinVirtualAcctStatusHistory);
			}

			backUpRequestResponse(requestBody.getRequestId(), null, responseEntity);
			LOGGER.info("RequestID {} - End createVirtualAcct", requestBody.getRequestId());
			return responseEntity;
		} catch (Exception e) {
			LOGGER.error("Fail to process createVirtualAcct", e);
			throw e;
		}
	}

	public ConnResponse buildActUpdateResponseEntity(VirtualAcctUpdateStatusRequest requestBody) throws Exception {

		// Backup request
		backUpRequestResponse(requestBody.getRequestId(), requestBody, null);

		// Build request
		VietinVirtualAcctUpdateStatus requestMap = buildVietinVirtualAcctUpdateStatus(requestBody);

		LOGGER.info("requestMap {}", requestMap);
		ResponseEntity<VietinVirtualAcctUpdateStatusBaseResponse> responseBaseEntity = null;
		VietinVirtualAcctUpdateStatusBaseResponse responseBase = null;
		ConnResponse responseEntity = null;

		responseBaseEntity = IHTTPRequestUtil.sendVirtualAcctUpdateStatus(requestMap);
		responseBase = responseBaseEntity.getBody();

		VietinVirtualAcctStatusHistory vietinVirtualAcctStatusHistory = new VietinVirtualAcctStatusHistory();
		vietinVirtualAcctStatusHistory.setRequestId(requestMap.getRequestId());
		vietinVirtualAcctStatusHistory.setVirtualAcctCode(requestMap.getVirtualAcctCode());
		vietinVirtualAcctStatusHistory.setVirtualAcctVar(requestMap.getVirtualAcctVar());
		vietinVirtualAcctStatusHistory.setNewStatus(requestMap.getNewStatus());
		vietinVirtualAcctStatusHistory.setStatusCode(responseBase.getStatus().getCode());
		virtualAcctStatusHistoryRepo.save(vietinVirtualAcctStatusHistory);

		responseEntity = validateAndBuildResponseVirtualAcctUpdateStatus(responseBase, responseBaseEntity.getStatusCode(), requestMap.getLanguage());

		// check whether request success?
		if (responseBase.getStatus().getCode().equals("1")) {
			virtualAcctRepo.updateVietinVirtualAcctStatus(requestMap.getVirtualAcctVar(), requestMap.getNewStatus(), OneFinConstants.BankListQrService.CTG.getBankCode());
		}

		backUpRequestResponse(requestBody.getRequestId(), null, responseEntity);
		return responseEntity;

	}


	public void buildSeedVirtualAcctPoolResponseEntity(VirtualAcctSeedRequest requestBody) throws Exception {
		int totalVirtualAccount = virtualAcctRepo.countVirtualAcct(requestBody.getPool(), OneFinConstants.BankListQrService.CTG.getBankCode());
		LOGGER.info("Total virtual account: {}", totalVirtualAccount);
		int diff = requestBody.getVirtualAccPoolInitSize() - totalVirtualAccount;
		if (diff > 0) {
			for (int i = 0; i < diff; i++) {
				VirtualAcctCreateRequest data = new VirtualAcctCreateRequest();
				data.setRequestId(RandomStringUtils.random(12, true, true));
				data.setVirtualAcctName(requestBody.getVirtualAccPoolAccountName());

				if (requestBody.getPrefix().equals(NumberSequenceService.prefixVtbPoolVirtualAcctSchoolMerchantNumberName)) {
					data.setVirtualAcctVar(numberSequenceService.nextVTBSchoolMerchantVirtualAcctNumber());
				} else if (requestBody.getPrefix().equals(NumberSequenceService.prefixVtbPoolVirtualAcctCommonMerchantNumberName)) {
					data.setVirtualAcctVar(numberSequenceService.nextVTBCommonMerchantVirtualAcctNumber());
				} else {
					throw new RuntimeInternalServerException("Prefix not support");
				}
				data.setLanguage(OneFinConstants.LANGUAGE.VIETNAMESE.getValue());
				data.setPoolName(requestBody.getPool());
				buildCreateResponseEntity(data);
			}
		}
	}

	public VietinVirtualAcctCreate buildVietinCreateVirtualAcct(VirtualAcctCreateRequest data) throws Exception {
		VietinVirtualAcctCreate result = new VietinVirtualAcctCreate();

		result.setRequestId(numberSequenceService.nextVTBVirtualAcct());
		result.setChannel("WEB");
		result.setLanguage("vi");
		result.setMerchantId(configLoader.getVietinVirtualAcctMerchantId());
		result.setProviderId(configLoader.getVietinVirtualAcctProviderId());
		result.setVersion(configLoader.getVietinVirtualAcctApiVersion());
		result.setClientIP(configLoader.getVietinVirtualAcctClientIP());
		result.setVirtualAcctStatus("0");
		result.setIsManagedByVTB("1");
		result.setAcctId(configLoader.getVietinVirtualAcctOnefinAcctId());
		result.setVirtualAcctCode(configLoader.getVietinVirtualAcctVirtualAcctCode());

		result.setEffectiveDate(data.getEffectiveDate());
		result.setExpireDate(data.getExpireDate());
		result.setVirtualAcctVar(data.getVirtualAcctVar());
		result.setVirtualAcctName(data.getVirtualAcctName());
		result.setProductName(data.getProductName());
		result.setProductCode(data.getProductCode());
		result.setCustomerCode(data.getCustomerCode());
		result.setCustomerName(data.getCustomerName());
		result.setMaxCredit(data.getMaxCredit());
		result.setMinCredit(data.getMinCredit());
		result.setCreditExpireDate(data.getCreditExpireDate());
		result.setTransTime(getTransTime());
		result.setLanguage(data.getLanguage());

		String dataSign = String.format("%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s", result.getRequestId(), result.getProviderId(), result.getMerchantId(), result.getAcctId(), result.getVirtualAcctCode(), result.getVirtualAcctVar(), result.getEffectiveDate(), result.getExpireDate(), result.getCheckerId(), result.getMakerId(), result.getCustomerCode(), result.getProductCode(), result.getTransTime(), result.getChannel(), result.getVersion(), result.getClientIP(), result.getLanguage());

		LOGGER.debug("Before Sign Data - " + dataSign);
		String signData = vietinVirtualAcctSign(dataSign);
		result.setSignature(signData);
		LOGGER.debug("After Sign Data - " + signData);
		return result;
	}

	public VietinVirtualAcctUpdateStatus buildVietinVirtualAcctUpdateStatus(VirtualAcctUpdateStatusRequest data) throws Exception {
		VietinVirtualAcctUpdateStatus result = new VietinVirtualAcctUpdateStatus();

		result.setRequestId(numberSequenceService.nextVTBVirtualAcct());
		result.setChannel("WEB");
		result.setMerchantId(configLoader.getVietinVirtualAcctMerchantId());
		result.setProviderId(configLoader.getVietinVirtualAcctProviderId());
		result.setVersion("1.0");
		result.setClientIP(configLoader.getVietinVirtualAcctClientIP());
		result.setAcctId(configLoader.getVietinVirtualAcctOnefinAcctId());
		result.setVirtualAcctCode(configLoader.getVietinVirtualAcctVirtualAcctCode());
		result.setTransTime(this.getTransTime());

		result.setVirtualAcctVar(data.getVirtualAcctVar());
		result.setNewStatus(data.getNewStatus());
		result.setLanguage(data.getLanguage());

		String dataSign = String.format("%s%s%s%s%s%s%s%s%s%s%s%s", result.getRequestId(), result.getProviderId(), result.getMerchantId(), result.getAcctId(), result.getVirtualAcctCode(), result.getVirtualAcctVar(), result.getNewStatus(), result.getTransTime(), result.getClientIP(), result.getChannel(), result.getVersion(), result.getLanguage());

		LOGGER.debug("buildVietinVirtualAcctUpdateStatus Before Sign Data - " + dataSign);
		String signData = vietinVirtualAcctSign(dataSign);
		result.setSignature(signData);
		LOGGER.debug("buildVietinVirtualAcctUpdateStatusAfter Sign Data - " + signData);
		return result;
	}

	private String vietinVirtualAcctSign(String input) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		// System.out.println(String.format("SignStringRaw: %s", input));

		PrivateKey privateKeyOneFin = encryptUtil.readPrivateKey(configLoader.getVietinVirtualAcctVirtualAcctOnefinPrivateKey());
		String signedData = encryptUtil.sign(input, privateKeyOneFin);
		return signedData;
	}

	private String vietinNotifySign(String input) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		// System.out.println(String.format("SignStringRaw: %s", input));

		PrivateKey privateKeyOneFin = encryptUtil.readPrivateKey(configLoader.getVtbVirtualAcctNotifyOnefinPrivateKey());
		String signedData = encryptUtil.sign(input, privateKeyOneFin, "SHA256withRSA");
		return signedData;
	}

	public ConnResponse validateAndBuildResponse(VietinVirtualAcctCreateBaseResponse data, HttpStatus httpStatus, String lang) {
		try {
			String code = data.getStatus() != null ? Objects.toString(data.getStatus().getCode()) : null;
			if (!code.equals(VietinConstants.VTB_VIRTUAL_ACCT_SUCCESS_CODE)) {
				LOGGER.warn("Warning: RequestId {} - {}", data.getRequestId(), data);
			}

			if (!isValidMessage(data.getRequestId(), data.getProviderId(), data.getMerchantId())) {
				LOGGER.error("Invalid response from VietinVirtualAcctCreate!!!");
				return buildVietinVirtualAcctConnectorResponse(VietinConstants.CONN_PARTNER_INVALID_RESPONSE, null);
			}

			ConnResponse result = new ConnResponse();
			transformErrorCode(result, data.getStatus().getCode(), lang);

			VirtualAcctCreateResponse virtualAcctCreateResponse = new VirtualAcctCreateResponse();
			virtualAcctCreateResponse.setVirtualAcctId(data.getVirtualAcctId());
			virtualAcctCreateResponse.setQrURL(data.getQrURL());
			result.setResponse(virtualAcctCreateResponse);
			return result;
		} catch (Exception e) {
			LOGGER.error("Validate response from VietinVirtualAcct error!!!", e);
			throw new RuntimeInternalServerException();
		}
	}

	public ConnResponse validateAndBuildResponseVirtualAcctUpdateStatus(VietinVirtualAcctUpdateStatusBaseResponse data, HttpStatus httpStatus, String lang) {
		try {
			String code = data.getStatus() != null ? Objects.toString(data.getStatus().getCode()) : null;
			if (!code.equals(VietinConstants.VTB_VIRTUAL_ACCT_SUCCESS_CODE)) {
				LOGGER.warn("Warning: RequestId {} - {}", data.getRequestId(), data);
			}

			if (!isValidMessage(data.getRequestId(), data.getProviderId(), data.getMerchantId())) {
				LOGGER.error("Invalid response from VietinVirtualAcctUpdateStatus!!!");
				return buildVietinVirtualAcctConnectorResponse(VietinConstants.CONN_PARTNER_INVALID_RESPONSE, null);
			}

			ConnResponse result = new ConnResponse();
			transformErrorCode(result, data.getStatus().getCode(), lang);
			return result;
		} catch (Exception e) {
			LOGGER.error("Validate response from VietinVirtualAcct error!!!", e);
			throw new RuntimeInternalServerException();
		}
	}

	public NotifyTransResponse validateAndBuildNotifyTransResponse(NotifyTransRequest data) throws CertificateException, IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		NotifyTransResponse result = new NotifyTransResponse();
		StringBuilder signatureRawRequest = new StringBuilder(new String());

		// Populate header value
		result.setHeader(modelMapper.map(data.getHeader(), NotifyTransHeaderResponse.class));

		result.setData(new NotifyTransDataResponse());
		result.getData().setRecords(new ArrayList<>());

		if (data.getHeader().getMsgType().equals(VietinConstants.VirtualAcctMsgType.NOTIFY_TRANS.getRequest())) {
			result.getHeader().setMsgType(VietinConstants.VirtualAcctMsgType.NOTIFY_TRANS.getResponse());
			for (NotifyTransRecordsRequest notifyTransRecordsRequest : data.getData().getRecords()) {
				NotifyTransRecordsResponse notifyTransRecordsResponse = modelMapper.map(notifyTransRecordsRequest, NotifyTransRecordsResponse.class);
				result.getData().getRecords().add(notifyTransRecordsResponse);

				signatureRawRequest.append(String.format("%s%s%s%s%s%s%s%s%s%s%s", Strings.nullToEmpty(notifyTransRecordsRequest.getTransId()), Strings.nullToEmpty(notifyTransRecordsRequest.getOriginalId()), Strings.nullToEmpty(notifyTransRecordsRequest.getTransTime()), Strings.nullToEmpty(notifyTransRecordsRequest.getCustCode()), Strings.nullToEmpty(notifyTransRecordsRequest.getCustName()), Strings.nullToEmpty(notifyTransRecordsRequest.getCustAcct()), Strings.nullToEmpty(notifyTransRecordsRequest.getAmount()), Strings.nullToEmpty(notifyTransRecordsRequest.getBalance()), Strings.nullToEmpty(notifyTransRecordsRequest.getStatus().getCode()), Strings.nullToEmpty(notifyTransRecordsRequest.getBankTransId()), Strings.nullToEmpty(notifyTransRecordsRequest.getRemark())));
			}
		}

		if (data.getHeader().getMsgType().equals(VietinConstants.VirtualAcctMsgType.INQ_BILL.getRequest())) {
			result.getHeader().setMsgType(VietinConstants.VirtualAcctMsgType.INQ_BILL.getResponse());
			signatureRawRequest.append(String.format("%s%s%s", Strings.nullToEmpty(data.getData().getTransId()), Strings.nullToEmpty(data.getData().getTransTime()), Strings.nullToEmpty(data.getData().getCustCode())));
			result.getData().setDetails(new InquiryBillResponseDetail());
			result.getData().getDetails().setTransId(data.getData().getTransId());
			result.getData().getDetails().setTransTime(data.getData().getTransTime());
			result.getData().getDetails().setCustCode(data.getData().getCustCode());
		}

		if (!isValidMessageNotifyTrans(data.getHeader().getMsgId(), data.getHeader().getProviderId())) {
			LOGGER.error("Invalid NotifyTrans request!!!");
			return buildNotifyTransResponseWithErrorCode(result, "99", "Invalid NotifyTrans request!!!");
		}

		if (data.getHeader().getMsgType().equals(VietinConstants.VirtualAcctMsgType.NOTIFY_TRANS.getRequest()) && !verifySignature(String.valueOf(signatureRawRequest), data.getHeader().getSignature())) {
			// Không xác thực được chữ ký số
			LOGGER.error("Verify signature fail!!!");
			return buildNotifyTransResponseWithErrorCode(result, "01", null);
		}

		if (data.getHeader().getMsgType().equals(VietinConstants.VirtualAcctMsgType.INQ_BILL.getRequest()) && !verifySignatureBillInq(String.valueOf(signatureRawRequest), data.getHeader().getSignature())) {
			// Không xác thực được chữ ký số
			LOGGER.error("Verify signature fail!!!");
			return buildNotifyTransResponseWithErrorCode(result, "01", null);
		}

		// =======================SET STATUS==============================
		for (NotifyTransRecordsResponse notifyTransRecordsResponse : result.getData().getRecords()) {
			notifyTransRecordsResponse.setStatus(notifyTransRecordStatusSuccessful());
		}

		// ===============================================================
		return buildNotifyTransResponseWithErrorCode(result, "00", null);
	}

	public NotifyTransResponse buildNotifyTransResponseWithErrorCode(NotifyTransResponse notifyTransResponse, String statusErrorCode, String statusErrorDesc) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		// ============================SIGNATURE========================================
		StringBuilder signatureRawResponse = new StringBuilder(new String());

		if (notifyTransResponse.getHeader().getMsgType().equals(VietinConstants.VirtualAcctMsgType.NOTIFY_TRANS.getResponse())) {
			signatureRawResponse.append(statusErrorCode);
			for (NotifyTransRecordsResponse notifyTransRecordsResponse : notifyTransResponse.getData().getRecords()) {
				signatureRawResponse.append(String.format("%s%s%s%s%s%s%s%s", Strings.nullToEmpty(notifyTransRecordsResponse.getTransId()), Strings.nullToEmpty(notifyTransRecordsResponse.getOriginalId()), Strings.nullToEmpty(notifyTransRecordsResponse.getTransTime()), Strings.nullToEmpty(notifyTransRecordsResponse.getCustCode()), Strings.nullToEmpty(notifyTransRecordsResponse.getCustName()), Strings.nullToEmpty(notifyTransRecordsResponse.getCustAcct()), Strings.nullToEmpty(notifyTransRecordsResponse.getAmount()), Strings.nullToEmpty(notifyTransRecordsResponse.getStatus().getCode())));
			}
		}

		if (notifyTransResponse.getHeader().getMsgType().equals(VietinConstants.VirtualAcctMsgType.INQ_BILL.getResponse())) {
			signatureRawResponse.append(String.format("%s%s%s%s%s%s%s%s", Strings.nullToEmpty(notifyTransResponse.getData().getDetails().getTransId()), Strings.nullToEmpty(notifyTransResponse.getHeader().getChannelId()), Strings.nullToEmpty(notifyTransResponse.getData().getDetails().getTransTime()), Strings.nullToEmpty(notifyTransResponse.getData().getDetails().getCustCode()), Strings.nullToEmpty(notifyTransResponse.getData().getDetails().getCustName()), Strings.nullToEmpty(notifyTransResponse.getData().getDetails().getBillId()), Strings.nullToEmpty(notifyTransResponse.getData().getDetails().getAmount()), Strings.nullToEmpty(statusErrorCode)));
		}
		LOGGER1.log(Level.getLevel("INFOWT"), "Vietin virtual acct {} raw text sig: {}", notifyTransResponse.getHeader().getMsgType(), signatureRawResponse);
		notifyTransResponse.getHeader().setSignature(vietinNotifySign(String.valueOf(signatureRawResponse)));
		// ============================SET ERRORCODE====================================
		notifyTransResponse.getData().setErrors(errorCode.get(statusErrorCode));
		if (statusErrorDesc != null) {
			notifyTransResponse.getData().getErrors().setErrorDesc(statusErrorDesc);
		}

		return notifyTransResponse;
	}

	public NotifyTransResponse NotifyTransBuildExceptionResponse() {
		NotifyTransResponse result = new NotifyTransResponse();

		result.setData(new NotifyTransDataResponse());
		result.getData().setErrors(errorCode.get("99"));

		return result;
	}

	public NotifyTransResponse NotifyTransBuildCusBillNotExistResponse() {
		NotifyTransResponse result = new NotifyTransResponse();

		result.setData(new NotifyTransDataResponse());
		result.getData().setErrors(errorCode.get("02"));

		return result;
	}

	public ConnResponse buildVietinVirtualAcctConnectorResponse(String code, Object data, String... args) {
		ConnResponse response = new ConnResponse();
		response.setConnectorCode(code);
		response.setVtbResponse(data);
		response.setVersion(configLoader.getVietinVersion());
		response.setType(args.length > 0 ? args[0] : null);
		return response;
	}

	public void backUpRequestResponse(String requestId, Object request, Object response) throws Exception {
		if (request != null) {
			backupService.backup(configLoader.getBackupApiUriVietinVirtualAcct(), requestId, request, VietinConstants.BACKUP_REQUEST);
		}
		if (response != null) {
			backupService.backup(configLoader.getBackupApiUriVietinVirtualAcct(), requestId, response, VietinConstants.BACKUP_RESPONSE);
		}
	}

	private boolean isValidMessageNotifyTrans(String requestId, String providerId) {
		if (providerId == null || providerId.trim().isEmpty() || requestId == null || requestId.trim().isEmpty()) {
			return false;
		}
		if (!configLoader.getVietinVirtualAcctProviderId().equals(providerId)) {
			LOGGER.error("ProviderId not support: {}", providerId);
			return false;
		}
		return true;
	}

	// check Signature
	private boolean isValidMessage(String requestId, String providerId, String merchantId) {
		if (providerId == null || providerId.trim().isEmpty() || requestId == null || requestId.trim().isEmpty() || merchantId == null || merchantId.trim().isEmpty()) {
			return false;
		}
		if (!configLoader.getVietinVirtualAcctProviderId().equals(providerId)) {
			LOGGER.error("ProviderId not support: {}", providerId);
			return false;
		}
		if (!configLoader.getVietinVirtualAcctMerchantId().equals(merchantId)) {
			LOGGER.error("MerchantId not support: {}", merchantId);
			return false;
		}
		return true;
	}

	private boolean verifySignature(String data, String signature) throws CertificateException, IOException {
		PublicKey publicKeyVietin = encryptUtil.readPublicKey2(configLoader.getVtbVirtualAcctNotifyVtbPublicKey());
		return encryptUtil.verifySignature(data, signature, publicKeyVietin, "SHA256withRSA");
	}

	private boolean verifySignatureBillInq(String data, String signature) throws CertificateException, IOException {
		PublicKey publicKeyVietin = encryptUtil.readPublicKey2(configLoader.getVtbVirtualAcctBillInqVtbPublicKey());
		return encryptUtil.verifySignature(data, signature, publicKeyVietin, "SHA256withRSA");
	}

	private void transformErrorCode(ConnResponse data, String code, String lang) {
		PartnerErrorCode partnerCode = partnerErrorCodeRepo.findAllByPartnerAndDomainAndCode(OneFinConstants.PARTNER_VIETINBANK, OneFinConstants.VIRTUAL_ACCT, code);
		// LOGGER.info("transformErrorCode code: {} {} {}", OneFinConstants.PARTNER_VIETINBANK, OneFinConstants.VIRTUAL_ACCT, code);
		if (partnerCode == null) {
			LOGGER.warn("No error code found, please check the config file: {}", code);
		}
		data.setConnectorCode(partnerCode.getBaseErrorCode().getCode());
		if (lang.equals(OneFinConstants.LANGUAGE.VIETNAMESE.getValue())) {
			data.setMessage(partnerCode.getBaseErrorCode().getMessageVi());
		} else if (lang.equals(OneFinConstants.LANGUAGE.ENGLISH.getValue())) {
			data.setMessage(partnerCode.getBaseErrorCode().getMessageEn());
		} else {
			data.setMessage(partnerCode.getBaseErrorCode().getMessageEn());
		}
	}

	public NotifyTransRequest notifyTransGenSign(NotifyTransRequest data) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {

		if (data.getHeader().getMsgType().equals(VietinConstants.VirtualAcctMsgType.NOTIFY_TRANS.getRequest())) {
			// ============================SIGNATURE========================================
			StringBuilder signatureRaw = new StringBuilder(new String());

			for (NotifyTransRecordsRequest notifyTransRecordsRequest : data.getData().getRecords()) {
				signatureRaw.append(String.format("%s%s%s%s%s%s%s%s%s%s%s", Strings.nullToEmpty(notifyTransRecordsRequest.getTransId()), Strings.nullToEmpty(notifyTransRecordsRequest.getOriginalId()), Strings.nullToEmpty(notifyTransRecordsRequest.getTransTime()), Strings.nullToEmpty(notifyTransRecordsRequest.getCustCode()), Strings.nullToEmpty(notifyTransRecordsRequest.getCustName()), Strings.nullToEmpty(notifyTransRecordsRequest.getCustAcct()), Strings.nullToEmpty(notifyTransRecordsRequest.getAmount()), Strings.nullToEmpty(notifyTransRecordsRequest.getBalance()), Strings.nullToEmpty(notifyTransRecordsRequest.getStatus().getCode()), Strings.nullToEmpty(notifyTransRecordsRequest.getBankTransId()), Strings.nullToEmpty(notifyTransRecordsRequest.getRemark())));
			}

			data.getHeader().setSignature(vietinNotifySign(String.valueOf(signatureRaw)));
			return data;
		}
		if (data.getHeader().getMsgType().equals(VietinConstants.VirtualAcctMsgType.INQ_CUSTOMER.getRequest())) {
			// ============================SIGNATURE========================================
			StringBuilder signatureRaw = new StringBuilder(new String());

			for (NotifyTransRecordsRequest notifyTransRecordsRequest : data.getData().getRecords()) {
				signatureRaw.append(String.format("%s%s%s%s", Strings.nullToEmpty(notifyTransRecordsRequest.getTransId()), Strings.nullToEmpty(notifyTransRecordsRequest.getTransTime()), Strings.nullToEmpty(notifyTransRecordsRequest.getCustCode()), Strings.nullToEmpty(notifyTransRecordsRequest.getCustName())));
			}

			data.getHeader().setSignature(vietinNotifySign(String.valueOf(signatureRaw)));
			return data;
		}
		return null;

	}

	private Status notifyTransRecordStatusSuccessful() {
		Status status = new Status();
		status.setCode("00");
		status.setMessage("Receive Notify Transaction Successful");
		return status;
	}

	/************************** Pool ************************/

//	@Transactional(rollbackFor = {Exception.class, Throwable.class})
	public synchronized VietinVirtualAcctDto getVirtualAcct(VirtualAcctGetRequest requestBody) {
		long startTime = System.currentTimeMillis();
		while (true) {
			Optional<VietinVirtualAcctTable> item = virtualAcctRepo.findFirstByInUseAndPoolName(false, requestBody.getPool(), OneFinConstants.BankListQrService.CTG.getBankCode());
			// Check if virtual account for specific school not available => get default virtual account
			if (!item.isPresent()) {
				item = virtualAcctRepo.findFirstByInUseAndPoolName(false, DEFAULT_SCHOOL_POOL_VIRTUAL_ACCT, OneFinConstants.BankListQrService.CTG.getBankCode());
				LOGGER.warn("No virtual account pool {} left, get default virtual account pool {} ", requestBody.getPool(), DEFAULT_SCHOOL_POOL_VIRTUAL_ACCT);
			}
			if (item.isPresent()) {
				Optional<VietinVirtualAcctTransHistory> checkHistory = Optional.ofNullable(virtualAcctTransHistoryRepo.findByTransUniqueKeyAndMerchantCode(requestBody.getTransUniqueKey(), requestBody.getMerchantCode()));
				// The order must be unique
				if (checkHistory.isPresent()) {
					VietinVirtualAcctDto data = new VietinVirtualAcctDto();
					data.setConnectorCode(VIR_ACCT_ORDER_EXIST);
					data.setMessage(String.format("Virtual account - Order already exist: %s, %s", requestBody.getTransUniqueKey(), requestBody.getMerchantCode()));
					LOGGER.warn(data.getMessage());
					return data;
					//throw new RuntimeInternalServerException("Virtual account - order already exist");
				}
				DateTime currDate = dateTimeHelper.currentDateTime(OneFinConstants.HO_CHI_MINH_TIME_ZONE);
				Date releaseTime = currDate.plusMinutes(configLoader.getVirtualAccPoolExpire()).toDate();
				virtualAcctRepo.updateVietinVirtualAcctInUse(item.get().getVirtualAcctVar(), true, currDate.toDate(), releaseTime, OneFinConstants.BankListQrService.CTG.getBankCode());
				VietinVirtualAcctDto data = modelMapper.map(item.get(), VietinVirtualAcctDto.class);
				data.setReleaseTime(releaseTime);
				LOGGER.info("Borrowed virtual account: {} --- {}", item.get(), data);
				//String qrUrl = String.format(configLoader.getVirtualAccPool1VietQrIoTemplateId(), data.getVirtualAcctId(), requestBody.getAmount().toString(), URLEncoder.encode(data.getVirtualAcctName(), "UTF-8").replace("+", "%20"));
				//data.setQrURL(qrUrl);
				data.setPeriod(configLoader.getVirtualAccPoolExpire() * 60);

				// Save trans history
				createVietinVirtualAcctTransHistory(currDate.toDate(), OneFinConstants.TRANS_PENDING, data.getVirtualAcctVar(), data.getVirtualAcctId(), requestBody.getTransUniqueKey(), requestBody.getMerchantCode(), requestBody.getAmount(), requestBody.getBackendUrl(), data.getReleaseTime(), data.getQrURL(), null, requestBody.getRemark());
				try {
					updateVirtualAccount(data.getVirtualAcctVar(), VietinConstants.VirtualAcctOnOff.ON.getValue());
				} catch (Exception e) {
					LOGGER.error("Process update virtual account fail", e);
				}

				return data;
			}
			// Waiting over MAX_WAIT_TIME => return error
			if (System.currentTimeMillis() - startTime >= MAX_WAIT_TIME) {
				throw new RuntimeInternalServerException(String.format("No virtual account pool %s left !!!!!", requestBody.getPool()));
			}
			try {
				LOGGER.info("No virtual account available, waiting {} ...", WAIT_INTERVAL);
				Thread.sleep(WAIT_INTERVAL);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public VietinVirtualAcctTransHistory releaseVirtualAcctFromNotify(VietinNotifyTransTable vietinNotifyTrans) {
		Optional<VietinVirtualAcctTable> item = virtualAcctRepo.findByVirtualAcctIdAndInUse(vietinNotifyTrans.getRecvVirtualAcctId(), true, OneFinConstants.BankListQrService.CTG.getBankCode());
		List<VietinVirtualAcctTransHistory> historyDataPending = virtualAcctTransHistoryRepo.findByVirtualAcctIdAndTranStatusAndAmount(vietinNotifyTrans.getRecvVirtualAcctId(), OneFinConstants.TRANS_PENDING, new BigDecimal(vietinNotifyTrans.getAmount()), OneFinConstants.BankListQrService.CTG.getBankCode());
		DateTime currDate = dateTimeHelper.currentDateTime(OneFinConstants.HO_CHI_MINH_TIME_ZONE);
		if (item.isPresent() && historyDataPending.size() == 1) {
			// Disable virtual account
			try {
				updateVirtualAccount(item.get().getVirtualAcctVar(), VietinConstants.VirtualAcctOnOff.OFF.getValue());
			} catch (Exception e) {
				LOGGER.error("Process update virtual account fail", e);
			}
			// Update history
			virtualAcctTransHistoryRepo.updateVietinVirtualAcctByStatus(OneFinConstants.TRANS_SUCCESS, currDate.toDate(), vietinNotifyTrans.getId(), vietinNotifyTrans.getRecvVirtualAcctId(), OneFinConstants.TRANS_PENDING, OneFinConstants.BankListQrService.CTG.getBankCode(), historyDataPending.get(0).getTransUniqueKey());

			//Update watching transactions
			virtualAcctTransWatchingRepo.findById(historyDataPending.get(0).getTransUniqueKey()).ifPresent(t -> {
				t.setNotified(true);
				virtualAcctTransWatchingRepo.save(t);
			});

			// Update virtual account
			virtualAcctRepo.updateVietinVirtualAcctIdInUse(vietinNotifyTrans.getRecvVirtualAcctId(), false, currDate.toDate(), currDate.toDate(), OneFinConstants.BankListQrService.CTG.getBankCode());
			LOGGER.info("Released virtual account: {}", vietinNotifyTrans);
			return historyDataPending.get(0);
		}
		List<VietinVirtualAcctTransHistory> historyDataTimeout = virtualAcctTransHistoryRepo.findByVirtualAcctIdAndTranStatusAndAmountAndLimit1(vietinNotifyTrans.getRecvVirtualAcctId(), OneFinConstants.TRANS_TIMEOUT, new BigDecimal(vietinNotifyTrans.getAmount()), TIME_OUT_CONSTANT, OneFinConstants.BankListQrService.CTG.getBankCode());
		if (historyDataTimeout.size() == 1 && vietinNotifyTrans.getRemark() != null && historyDataTimeout.get(0).getRemark() != null && Pattern.compile(Pattern.quote(historyDataTimeout.get(0).getRemark()), Pattern.CASE_INSENSITIVE).matcher(vietinNotifyTrans.getRemark()).find()) {
			// Update history
			LOGGER.info("Update timeout trans successfully: {}, {}", vietinNotifyTrans, historyDataTimeout.get(0));
			virtualAcctTransHistoryRepo.updateVirtualAcctById(OneFinConstants.TRANS_SUCCESS, currDate.toDate(), vietinNotifyTrans.getId(), historyDataTimeout.get(0).getId(), OneFinConstants.BankListQrService.CTG.getBankCode());
			return historyDataTimeout.get(0);
		} else {
			LOGGER.warn("Not found this virtual account to release => Waiting settle: {}, {}, {}", vietinNotifyTrans, item, historyDataPending.size());
			createVietinVirtualAcctTransHistory(currDate.toDate(), OneFinConstants.TRANS_DISPUTE, null, vietinNotifyTrans.getRecvVirtualAcctId(), null, null, new BigDecimal(vietinNotifyTrans.getAmount()), null, null, null, vietinNotifyTrans.getId(), vietinNotifyTrans.getRemark());
			return null;
		}
	}

	public void releaseVirtualAcctFromBatchJob() {
		List<VietinVirtualAcctTable> items = virtualAcctRepo.findByInUseAndBufferExpired(true, dateTimeHelper.currentDate(OneFinConstants.HO_CHI_MINH_TIME_ZONE), configLoader.getVirtualAccPoolExpireBuffer(), OneFinConstants.BankListQrService.CTG.getBankCode());
		LOGGER.info("Number of virtual account need to release: {}", items.size());
		items.forEach(e -> {
			DateTime currDate = dateTimeHelper.currentDateTime(OneFinConstants.HO_CHI_MINH_TIME_ZONE);
			// Virtual acct borrowed, but after expired time there is no notify from bank => must release
			List<VietinVirtualAcctTransHistory> history = virtualAcctTransHistoryRepo.findByVirtualAcctVarAndTranStatus(e.getVirtualAcctVar(), OneFinConstants.TRANS_PENDING, OneFinConstants.BankListQrService.CTG.getBankCode());
			if (history.size() == 1) {
				LOGGER.info("Release account details: {}", e);
				try {
					// Disable virtual account
					try {
						updateVirtualAccount(e.getVirtualAcctVar(), VietinConstants.VirtualAcctOnOff.OFF.getValue());
					} catch (Exception ex) {
						LOGGER.error("Process update virtual account fail", ex);
					}
					// TODO
					// Check if this virtual account hold any transaction from VietinBank. If hold update to SUCCESS and send Callback

					// Updated trans to Time out
					virtualAcctTransHistoryRepo.updateVietinVirtualAcctByStatus(OneFinConstants.TRANS_TIMEOUT, currDate.toDate(), null, e.getVirtualAcctId(), OneFinConstants.TRANS_PENDING, OneFinConstants.BankListQrService.CTG.getBankCode(), history.get(0).getTransUniqueKey());
					// Release virtual acct
					virtualAcctRepo.updateVietinVirtualAcctInUse(e.getVirtualAcctVar(), false, currDate.toDate(), currDate.toDate(), OneFinConstants.BankListQrService.CTG.getBankCode());
				} catch (Exception ex) {
					LOGGER.error("Error release virtual account: ", ex);
				}
			} else {
				LOGGER.error("Something went wrong with release virtual account, Please check: {}, trans history {}", e, history.size());
			}
		});

	}

	public VietinVirtualAcctTransHistory createVietinVirtualAcctTransHistory(Date currDate, String status, String virtualAcctVar, String virtualAcctId, String transUniqueKey, String merchantCode, BigDecimal amount, String backendUrl, Date releaseTime, String qrUrl, UUID vietinNotiUuid, String remark) {
		VietinVirtualAcctTransHistory history = new VietinVirtualAcctTransHistory();
		history.setCreatedDate(currDate);
		history.setUpdatedDate(currDate);
		history.setTranStatus(status);
		history.setVirtualAcctVar(virtualAcctVar);
		history.setVirtualAcctId(virtualAcctId);
		history.setTransUniqueKey(transUniqueKey);
		history.setMerchantCode(merchantCode);
		history.setAmount(amount);
		history.setBackendUrl(backendUrl);
		history.setExpireTime(releaseTime != null ? releaseTime : currDate);
		//history.setQrUrl(qrUrl);
		history.setVietinNotiUuid(vietinNotiUuid);
		history.setRemark(remark);
		history.setBankCode(OneFinConstants.BankListQrService.CTG.getBankCode());
		LOGGER.info("Stored virtual account history: {}", history);
		return virtualAcctTransHistoryRepo.save(history);
	}

	public void updateVirtualAccount(String virtualAcctVar, String status) throws Exception {
		LOGGER.info("Start update virtual account");
		VirtualAcctUpdateStatusRequest requestBodyUpdateVirtualAcct = new VirtualAcctUpdateStatusRequest();
		requestBodyUpdateVirtualAcct.setRequestId(RandomStringUtils.random(12, true, true));
		requestBodyUpdateVirtualAcct.setVirtualAcctVar(virtualAcctVar);
		requestBodyUpdateVirtualAcct.setNewStatus(status);
		requestBodyUpdateVirtualAcct.setLanguage(OneFinConstants.LANGUAGE.VIETNAMESE.getValue());
		ConnResponse response = virtualAcctUpdateStatus(requestBodyUpdateVirtualAcct);
		LOGGER.info("End update virtual account: {}", response);
	}

	public ConnResponse virtualAcctUpdateStatus(VirtualAcctUpdateStatusRequest requestBody) throws Exception {
		try {
			backUpRequestResponse(requestBody.getRequestId(), requestBody, null);
			VietinVirtualAcctUpdateStatus requestMap = buildVietinVirtualAcctUpdateStatus(requestBody);

			LOGGER.info("requestMap {}", requestMap);
			ResponseEntity<VietinVirtualAcctUpdateStatusBaseResponse> responseBaseEntity = null;
			VietinVirtualAcctUpdateStatusBaseResponse responseBase = null;
			ConnResponse responseEntity = null;

			responseBaseEntity = IHTTPRequestUtil.sendVirtualAcctUpdateStatus(requestMap);
			responseBase = responseBaseEntity.getBody();

			VietinVirtualAcctStatusHistory vietinVirtualAcctStatusHistory = new VietinVirtualAcctStatusHistory();
			vietinVirtualAcctStatusHistory.setRequestId(requestMap.getRequestId());
			vietinVirtualAcctStatusHistory.setVirtualAcctCode(requestMap.getVirtualAcctCode());
			vietinVirtualAcctStatusHistory.setVirtualAcctVar(requestMap.getVirtualAcctVar());
			vietinVirtualAcctStatusHistory.setNewStatus(requestMap.getNewStatus());
			vietinVirtualAcctStatusHistory.setStatusCode(responseBase.getStatus().getCode());
			vietinVirtualAcctStatusHistory.setBankCode(OneFinConstants.BankListQrService.CTG.getBankCode());
			virtualAcctStatusHistoryRepo.save(vietinVirtualAcctStatusHistory);

			responseEntity = validateAndBuildResponseVirtualAcctUpdateStatus(responseBase, responseBaseEntity.getStatusCode(), requestMap.getLanguage());

			// check whether request success?
			if (responseBase.getStatus().getCode().equals("1")) {
				virtualAcctRepo.updateVietinVirtualAcctStatus(requestMap.getVirtualAcctVar(), requestMap.getNewStatus(), OneFinConstants.BankListQrService.CTG.getBankCode());
			}

			backUpRequestResponse(requestBody.getRequestId(), null, responseEntity);
			LOGGER.info("RequestID {} - End VirtualAcctUpdateStatus", requestBody.getRequestId());
			return responseEntity;
		} catch (Exception e) {
			LOGGER.error("Fail to process VirtualAcctUpdateStatus", e);
			throw e;
		}
	}

	/************************** Pool ************************/

	@Async("asyncExecutor")
	public ResponseEntity<?> callbackWithPoolVirtualAcct(String backendUrl, String virtualAcctTransHistoryId, String statusCode, String bankTransId, String from) {
		LOGGER.info("== Send callbackWithPoolVirtualAcct to url: {}", backendUrl);
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.ALL));
		headers.setContentType(MediaType.APPLICATION_JSON);
		Map<String, String> headersMap = new HashMap<>();
		headers.keySet().forEach(header -> headersMap.put(header, headers.getFirst(header)));
		Map<String, String> data = new HashMap<>();
		data.put("statusCode", statusCode);
		data.put("orderId", virtualAcctTransHistoryId);
		data.put("bankTransId", bankTransId);
		data.put("bankAccountName", from);
		LOGGER.info("CallbackWithPoolVirtualAcct request {}", data);
		ResponseEntity<Map> responseEntity = restTemplateHelper.post(backendUrl, MediaType.APPLICATION_JSON_VALUE, headersMap, new ArrayList<>(), new HashMap<>(), null, data, new ParameterizedTypeReference<Map>() {
		});
		LOGGER.info("CallbackWithPoolVirtualAcct response {}", responseEntity.getBody());
		return responseEntity;
	}


	public VietinVirtualAcctDto buildVirtualAcctInPool(VirtualAcctGetRequest requestBody) {
		DateTime currDate = dateTimeHelper.currentDateTime(OneFinConstants.HO_CHI_MINH_TIME_ZONE);
		// Check if order (transUniqueKey, merchantCode) already exists (not expired)
		// exist: Not borrow new virtual acct => return the current virtual acct binding with the order
		// Not exist: Borrow the new one, create new order

		VietinVirtualAcctTransHistory grantAccount = vietVirtualAcctTransHistoryRepo.findByTransUniqueKeyAndMerchantCodeAndNotExpireAndTranStatus(requestBody.getTransUniqueKey(), requestBody.getMerchantCode(), currDate.toDate(), OneFinConstants.TRANS_PENDING);
		if (grantAccount != null) {
			Optional<VietinVirtualAcctTable> item = virtualAcctRepo.findFirstByVirtualAcctIdAndInUse(grantAccount.getVirtualAcctId(), true, OneFinConstants.BankListQrService.CTG.getBankCode());
			if (item.isPresent()) {
				VietinVirtualAcctDto data = modelMapper.map(item.get(), VietinVirtualAcctDto.class);
//				String qrUrl = String.format(configLoader.getVirtualAccPool1VietQrIoTemplateId(), item.get().getVirtualAcctId(), grantAccount.getAmount().toString(), URLEncoder.encode(item.get().getVirtualAcctName(), "UTF-8").replace("+", "%20"));
//				data.setQrURL(qrUrl);
				data.setPeriod(configLoader.getVirtualAccPoolExpire() * 60);
				data.setBank(OneFinConstants.BankListQrService.CTG.getName());
				LOGGER.info("Virtual account still valid return: {}", data);
				return data;
			} else {
				LOGGER.error("Something went wrong with grant virtual account, Please check: {}, {}", requestBody, grantAccount);
				throw new RuntimeInternalServerException("Something went wrong with grant virtual account");
			}
		} else {
			VietinVirtualAcctDto item = getVirtualAcct(requestBody);
			item.setBank(OneFinConstants.BankListQrService.CTG.getName());
			LOGGER.info("New virtual account return: {}", item);
			return item;
		}
	}


}
