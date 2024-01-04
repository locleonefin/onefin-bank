package com.onefin.ewallet.bank.service.bvb;


import com.onefin.ewallet.bank.repository.redis.VirtualAcctTransWatchingRepo;
import com.onefin.ewallet.common.base.constants.BankConstants;
import com.onefin.ewallet.bank.dto.bvb.*;
import com.onefin.ewallet.bank.dto.vietin.*;
import com.onefin.ewallet.bank.repository.jpa.VietinNotifyTransTableRepo;
import com.onefin.ewallet.bank.repository.jpa.VirtualAcctRepo;
import com.onefin.ewallet.bank.repository.jpa.VirtualAcctStatusHistoryRepo;
import com.onefin.ewallet.bank.repository.jpa.VirtualAcctTransHistoryRepo;
import com.onefin.ewallet.bank.service.common.ConfigLoader;
import com.onefin.ewallet.bank.service.common.NumberSequenceService;
import com.onefin.ewallet.bank.service.common.VirtualAcctAbstract;
import com.onefin.ewallet.common.base.anotation.MeasureExcutionTime;
import com.onefin.ewallet.common.base.constants.OneFinConstants;
import com.onefin.ewallet.common.base.errorhandler.RuntimeInternalServerException;
import com.onefin.ewallet.common.domain.bank.vietin.VietinNotifyTransTable;
import com.onefin.ewallet.common.domain.bank.vietin.VietinVirtualAcctStatusHistory;
import com.onefin.ewallet.common.domain.bank.vietin.VietinVirtualAcctTable;
import com.onefin.ewallet.common.domain.bank.vietin.VietinVirtualAcctTransHistory;
import com.onefin.ewallet.common.utility.date.DateTimeHelper;
import org.apache.commons.lang3.RandomStringUtils;
import org.joda.time.DateTime;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.security.PrivateKey;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Pattern;

import static com.onefin.ewallet.bank.common.VietinConstants.VIR_ACCT_ORDER_EXIST;

@Service
public class BVBVirtualAcct extends VirtualAcctAbstract {

	private static final Logger LOGGER = LoggerFactory.getLogger(BVBVirtualAcct.class);

	private static final int MAX_WAIT_TIME = 60_000; //60 seconds

	private static final String DEFAULT_SCHOOL_POOL_VIRTUAL_ACCT = "1";

	private static final int WAIT_INTERVAL = 5_000; // 1 second

	private final int TIME_OUT_CONSTANT = 24; // ho

	@Autowired
	private NumberSequenceService numberSequenceService;

	@Autowired
	private BVBRequestUtil bvbRequestUtil;

	@Autowired
	private BVBEncryptUtil bvbEncryptUtil;

	@Autowired
	private ConfigLoader configLoader;

	@Autowired
	private VirtualAcctRepo virtualAcctRepo;

	@Autowired
	private VirtualAcctStatusHistoryRepo virtualAcctStatusHistoryRepo;

	@Autowired
	private DateTimeHelper dateTimeHelper;

	@Autowired
	private VirtualAcctTransHistoryRepo virtualAcctTransHistoryRepo;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private Environment env;

	@Autowired
	private VietinNotifyTransTableRepo vietinNotifyTransTableRepo;

	@Autowired
	private VirtualAcctTransWatchingRepo virtualAcctTransWatchingRepo;

	public BVBVirtualAcctCreateRequest buildBVBVirtualAcctCreateRequest(VirtualAcctCreateRequest data) throws Exception {

		BVBVirtualAcctCreateRequest request = new BVBVirtualAcctCreateRequest();
		request.setRequestId(data.getRequestId());

		request.setFrom(configLoader.getBvbVirtualAcctPartnerCode());
		BVBVirtualAcctCreateRequestData partnerRequest = new BVBVirtualAcctCreateRequestData();
		partnerRequest.setPartnerCode(configLoader.getBvbVirtualAcctPartnerCode());
		LOGGER.info("Virtual acct length: " + data.getVirtualAcctVar().length());
		assert data.getVirtualAcctVar().length() < 7 : "virtual acct var must be smaller than 7";
		partnerRequest.setAccNameSuffix(data.getVirtualAcctName());
		partnerRequest.setAccType(configLoader.getBvbVirtualAcctDefaultAccType());
		partnerRequest.setClientUserId("");
		partnerRequest.setFixedAmount(BigDecimal.valueOf(1000));
		partnerRequest.setAccNoSuffix(data.getVirtualAcctVar());
		request.setData(partnerRequest);
		return request;
	}

	@Override
	public ConnResponse buildCreateResponseEntity(VirtualAcctCreateRequest requestBody) throws Exception {

		// build BVB Virtual acct Create request
		BVBVirtualAcctCreateRequest bvbVirtualAcctCreateRequest = buildBVBVirtualAcctCreateRequest(requestBody);

		// Request create api
		ResponseEntity<?> responseFromBVBCreateVirtualAcct = bvbRequestUtil.requestBVB(bvbVirtualAcctCreateRequest, configLoader.getBvbVirtualAcctCreateVirtualAcctUrl(), bvbVirtualAcctCreateRequest.getRequestId(), BankConstants.BVB_BACKUP_CREATE_PREFIX);

		// validate signature
		ConnResponse responseSignature = bvbRequestUtil.checkSignature(responseFromBVBCreateVirtualAcct);
		if (responseSignature != null) {
			LOGGER.error("Invalid response from BVB !!!");
			ConnResponse response = new ConnResponse();
			bvbRequestUtil.transformErrorCode(response, BankConstants.BVB_REQUEST_STATUS_WRONG_SIGNATURE, requestBody.getLanguage());
			return response;
		}

		// construct BVB Create virtual code response body
		BVBVirtualAcctCreateResponse bvbVirtualAcctCreateResponse = modelMapper.map(responseFromBVBCreateVirtualAcct.getBody(), BVBVirtualAcctCreateResponse.class);

		ConnResponse responseEntity = null;

//		// validate response
		responseEntity = buildConnResponse(bvbVirtualAcctCreateResponse, requestBody.getLanguage());

		createApiUpdateTable(bvbVirtualAcctCreateResponse, bvbVirtualAcctCreateRequest, requestBody.getPoolName());
		return responseEntity;
	}

	public <T extends BVBVirtualAcctCommonResponse> ConnResponse buildConnResponse(T data, String lang) {
		try {
			String code = data.getRCode() != null ? Objects.toString(data.getRCode()) : null;

			// Checking rCode
			assert data.getRCode() != null;
			String errorCode = data.getRCode();
			if (!errorCode.equals(BankConstants.BVB_REQUEST_STATUS_SUCCESS)) {
				LOGGER.warn("Warning: RequestId {} - {}", data.getRequestId(), data);
//				errorCode = data.getData().getError();
			}

			ConnResponse result = new ConnResponse();
			bvbRequestUtil.transformErrorCode(result, errorCode, lang);
			return result;
		} catch (Exception e) {
			LOGGER.error("Error from BVB server !!!", e);
			throw new RuntimeInternalServerException();
		}
	}

	@Override
	public ConnResponse buildActUpdateResponseEntity(VirtualAcctUpdateStatusRequest requestBody) throws Exception {
		return null;
	}

	@Override
	public void buildSeedVirtualAcctPoolResponseEntity(VirtualAcctSeedRequest requestBody) throws Exception {

		int totalVirtualAccount = virtualAcctRepo.countVirtualAcct(requestBody.getPool(), OneFinConstants.BankListQrService.VCCB.getBankCode());
		LOGGER.info("Total virtual account: {}", totalVirtualAccount);
		int diff = requestBody.getVirtualAccPoolInitSize() - totalVirtualAccount;
		if (diff > 0) {
			for (int i = 0; i < diff; i++) {
				VirtualAcctCreateRequest data = new VirtualAcctCreateRequest();
				data.setRequestId(RandomStringUtils.random(12, true, true));
				data.setVirtualAcctName(requestBody.getVirtualAccPoolAccountName());

				if (requestBody.getPrefix().equals(NumberSequenceService.prefixBvbPoolVirtualAcctSchoolMerchantNumber)) {
					data.setVirtualAcctVar(numberSequenceService.nextBVBSchoolMerchantVirtualAcctNumber());
				} else if (requestBody.getPrefix().equals(NumberSequenceService.prefixBvbPoolVirtualAcctCommonMerchantNumber)) {
					data.setVirtualAcctVar(numberSequenceService.nextBVBCommonMerchantVirtualAcctNumber());
				} else {
					throw new RuntimeInternalServerException("Prefix not support");
				}
				data.setLanguage(OneFinConstants.LANGUAGE.VIETNAMESE.getValue());
				data.setPoolName(requestBody.getPool());
				buildCreateResponseEntity(data);
			}
		}
	}

	public VietinVirtualAcctDto buildVirtualAcctInPool(VirtualAcctGetRequest requestBody) throws Exception {

		// get current Datetime
		DateTime currDate = dateTimeHelper.currentDateTime(OneFinConstants.HO_CHI_MINH_TIME_ZONE);
		// Check if order (transUniqueKey, merchantCode) already exists (not expired)
		// exist: Not borrow new virtual acct => return the current virtual acct binding with the order
		// Not exist: Borrow the new one, create new order

		// check trans record
		VietinVirtualAcctTransHistory transRecord = virtualAcctTransHistoryRepo.findByTransUniqueKeyAndMerchantCodeAndNotExpireAndTranStatus(requestBody.getTransUniqueKey(), requestBody.getMerchantCode(), currDate.toDate(), OneFinConstants.TRANS_PENDING);
		if (transRecord != null) {
			Optional<VietinVirtualAcctTable> item = virtualAcctRepo.findFirstByVirtualAcctIdAndInUse(transRecord.getVirtualAcctId(), true, OneFinConstants.BankListQrService.VCCB.getBankCode());
			if (item.isPresent()) {
				VietinVirtualAcctDto data = modelMapper.map(item.get(), VietinVirtualAcctDto.class);

//				String qrUrl = String.format(configLoader.getVirtualAccPool1VietQrIoTemplateId(), item.get().getVirtualAcctId(), grantAccount.getAmount().toString(), URLEncoder.encode(item.get().getVirtualAcctName(), "UTF-8").replace("+", "%20"));
//				data.setQrURL(qrUrl);
				data.setPeriod(configLoader.getVirtualAccPoolExpire() * 60);
				data.setBank(OneFinConstants.BankListQrService.VCCB.getName());

//				VietinVirtualAcctDto data2 = new VietinVirtualAcctDto();
				LOGGER.info("Virtual account still valid return: {}", data);
				return data;
			} else {
				LOGGER.error("Something went wrong with grant virtual account, Please check: {}, {}", requestBody, transRecord);
				throw new RuntimeInternalServerException("Something went wrong with grant virtual account");
			}
		} else {
			VietinVirtualAcctDto item = getVirtualAcct(requestBody);
			item.setBank(OneFinConstants.BankListQrService.VCCB.getName());
			LOGGER.info("New virtual account return: {}", item);
			return item;
		}
	}

	public synchronized VietinVirtualAcctDto getVirtualAcct(VirtualAcctGetRequest requestBody) throws Exception {
		// get start time
		long startTime = System.currentTimeMillis();
		while (true) {
			Optional<VietinVirtualAcctTable> item = virtualAcctRepo.findFirstByInUseAndPoolName(false, requestBody.getPool(), OneFinConstants.BankListQrService.VCCB.getBankCode());

			if (item.isPresent()) {
				// check order existence
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

				// check acct status

				// update virtual account
				try {
					BVBVirtualAcctInfoDetailResponse updateResponse = checkVirtualAccountDetailAndUpdate(item.get().getVirtualAcctId());
					if (updateResponse == null) {
						throw new Exception("Checking virtual acct Failed");
					}
					if (updateResponse.getData().getAccStat().equals(BankConstants.BVBVirtualAcctStatus.CLOSE.getValue())) {
						LOGGER.info("Virtual acct has been closed, request BVB to reopen");
						BVBVirtualAcctReopenRequest bvbVirtualAcctReopenRequest = new BVBVirtualAcctReopenRequest();

						bvbVirtualAcctReopenRequest.setFrom(configLoader.getBvbVirtualAcctPartnerCode());
						bvbVirtualAcctReopenRequest.setRequestId(RandomStringUtils.random(12, true, true));

						BVBVirtualAcctCommonPartnerRequest bvbVirtualAcctCommonPartnerRequest = new BVBVirtualAcctCommonPartnerRequest();

						bvbVirtualAcctCommonPartnerRequest.setAccNo(item.get().getVirtualAcctId());
						bvbVirtualAcctCommonPartnerRequest.setClientUserId(requestBody.getMerchantCode());
						bvbVirtualAcctCommonPartnerRequest.setFixedAmount(requestBody.getAmount());
						bvbVirtualAcctCommonPartnerRequest.setAccNameSuffix(item.get().getVirtualAcctName());
						bvbVirtualAcctCommonPartnerRequest.setPartnerCode(configLoader.getBvbVirtualAcctPartnerCode());

						bvbVirtualAcctReopenRequest.setData(bvbVirtualAcctCommonPartnerRequest);

						// request BVB reopen virtual acc
						ResponseEntity<?> openResponseResponseEntity = bvbRequestUtil.requestBVB(bvbVirtualAcctReopenRequest, configLoader.getBvbVirtualAcctReopenVirtualAcctUrl(), bvbVirtualAcctReopenRequest.getRequestId(), BankConstants.BVB_BACKUP_REOPEN_PREFIX);

						// validate signature
						if (!bvbRequestUtil.validateBvbSignature(openResponseResponseEntity)) {
							LOGGER.error("Invalid response from BVB !!!");
							throw new RuntimeInternalServerException("Signature validate failed !!!!!");
						}

						BVBVirtualAcctReopenResponse bvbVirtualAcctReopenResponse = modelMapper.map(openResponseResponseEntity.getBody(), BVBVirtualAcctReopenResponse.class);
						LOGGER.info("Reopen response: " + bvbVirtualAcctReopenResponse);

						reOpenApiUpdateTable(bvbVirtualAcctReopenResponse, bvbVirtualAcctReopenRequest);

						if (!bvbVirtualAcctReopenResponse.getRCode().equals(BankConstants.BVB_REQUEST_STATUS_SUCCESS)) {
							LOGGER.error("Virtual Acct closed but reopen Request Failed!!!");
							throw new RuntimeInternalServerException("Virtual Acct closed but reopen Request Failed !!!!!");
						}


					} else {
						LOGGER.info("Virtual acct is opening, updating transaction info");

						BVBVirtualAcctUpdateRequest bvbVirtualAcctUpdateRequest = new BVBVirtualAcctUpdateRequest();

						bvbVirtualAcctUpdateRequest.setFrom(configLoader.getBvbVirtualAcctPartnerCode());
						bvbVirtualAcctUpdateRequest.setRequestId(RandomStringUtils.random(12, true, true));

						BVBVirtualAcctCommonPartnerRequest bvbVirtualAcctCommonPartnerRequest = new BVBVirtualAcctCommonPartnerRequest();

						bvbVirtualAcctCommonPartnerRequest.setAccNo(item.get().getVirtualAcctId());
						bvbVirtualAcctCommonPartnerRequest.setClientUserId(requestBody.getMerchantCode());
						bvbVirtualAcctCommonPartnerRequest.setFixedAmount(requestBody.getAmount());
						bvbVirtualAcctCommonPartnerRequest.setAccNameSuffix(item.get().getVirtualAcctName());
						bvbVirtualAcctCommonPartnerRequest.setPartnerCode(configLoader.getBvbVirtualAcctPartnerCode());

						bvbVirtualAcctUpdateRequest.setData(bvbVirtualAcctCommonPartnerRequest);

						// request BVB update virtual acc
						ResponseEntity<?> updateResponseEntity = bvbRequestUtil.requestBVB(bvbVirtualAcctUpdateRequest, configLoader.getBvbVirtualAcctUpdateVirtualAcctUrl(), bvbVirtualAcctUpdateRequest.getRequestId(), BankConstants.BVB_BACKUP_UPDATE_ACCOUNT_PREFIX);

						// validate signature
						if (!bvbRequestUtil.validateBvbSignature(updateResponseEntity)) {
							LOGGER.error("Invalid response from BVB !!!");
							throw new RuntimeInternalServerException("Signature validate failed !!!!!");
						}

						BVBVirtualAcctUpdateResponse bvbVirtualAcctUpdateResponse = modelMapper.map(updateResponseEntity.getBody(), BVBVirtualAcctUpdateResponse.class);
						LOGGER.info("Reopen response: " + bvbVirtualAcctUpdateResponse);

						updateApiUpdateTable(bvbVirtualAcctUpdateResponse, bvbVirtualAcctUpdateRequest);

						if (!bvbVirtualAcctUpdateResponse.getRCode().equals(BankConstants.BVB_REQUEST_STATUS_SUCCESS)) {
							LOGGER.error("Updated Request Failed!!!");
							throw new RuntimeInternalServerException("Virtual Acct closed but reopen Request Failed !!!!!");
						}
					}

				} catch (Exception e) {
					throw new RuntimeInternalServerException("Process check virtual account failed");
				}

				virtualAcctRepo.updateVirtualAcctInUse(item.get().getVirtualAcctId(), true, currDate.toDate(), releaseTime, OneFinConstants.BankListQrService.VCCB.getBankCode());

				VietinVirtualAcctDto data = modelMapper.map(item.get(), VietinVirtualAcctDto.class);
				data.setReleaseTime(releaseTime);
				LOGGER.info("Borrowed virtual account: {} --- {}", item.get(), data);
				//String qrUrl = String.format(configLoader.getVirtualAccPool1VietQrIoTemplateId(), data.getVirtualAcctId(), requestBody.getAmount().toString(), URLEncoder.encode(data.getVirtualAcctName(), "UTF-8").replace("+", "%20"));
				//data.setQrURL(qrUrl);
				data.setPeriod(configLoader.getVirtualAccPoolExpire() * 60);

				// Save trans history
				createVietinVirtualAcctTransHistory(currDate.toDate(), OneFinConstants.TRANS_PENDING, data.getVirtualAcctVar(), data.getVirtualAcctId(), requestBody.getTransUniqueKey(), requestBody.getMerchantCode(), requestBody.getAmount(), requestBody.getBackendUrl(), data.getReleaseTime(), data.getQrURL(), null, requestBody.getRemark());

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

	public void releaseVirtualAcctFromBatchJob() {
		List<VietinVirtualAcctTable> items = virtualAcctRepo.findByInUseAndBufferExpired(true, dateTimeHelper.currentDate(OneFinConstants.HO_CHI_MINH_TIME_ZONE), configLoader.getVirtualAccPoolExpireBuffer(), OneFinConstants.BankListQrService.VCCB.getBankCode());
		LOGGER.info("Number of virtual account need to release: {}", items.size());
		items.forEach(e -> {
			DateTime currDate = dateTimeHelper.currentDateTime(OneFinConstants.HO_CHI_MINH_TIME_ZONE);
			// Virtual acct borrowed, but after expired time there is no notify from bank => must release
			List<VietinVirtualAcctTransHistory> history = virtualAcctTransHistoryRepo.findByVirtualAcctVarIdAndTranStatus(e.getVirtualAcctId(), OneFinConstants.TRANS_PENDING, OneFinConstants.BankListQrService.VCCB.getBankCode());
			if (history.size() == 1) {
				LOGGER.info("Release account details: {}", e);
				try {
					// Disable virtual account
					try {
						BVBVirtualAcctInfoDetailResponse updateResponse = checkVirtualAccountDetailAndUpdate(e.getVirtualAcctId());
						if (updateResponse.getData().getAccStat().equals(BankConstants.BVBVirtualAcctStatus.OPEN.getValue())) {
							closeVirtualAcct(e.getVirtualAcctId());
						}
					} catch (Exception ex) {
						LOGGER.error("Process update virtual account fail", ex);
					}
					// TODO
					// Check if this virtual account hold any transaction from VietinBank. If hold update to SUCCESS and send Callback

					// Updated trans to Time out
					virtualAcctTransHistoryRepo.updateVietinVirtualAcctByStatus(OneFinConstants.TRANS_TIMEOUT, currDate.toDate(), null, e.getVirtualAcctId(), OneFinConstants.TRANS_PENDING, OneFinConstants.BankListQrService.VCCB.getBankCode(), history.get(0).getTransUniqueKey());
					// Release virtual acct
					virtualAcctRepo.updateVirtualAcctInUse(e.getVirtualAcctId(), false, currDate.toDate(), currDate.toDate(), OneFinConstants.BankListQrService.VCCB.getBankCode());
				} catch (Exception ex) {
					LOGGER.error("Error release virtual account: ", ex);
				}
			} else {
				LOGGER.error("Something went wrong with release virtual account, Please check: {}, trans history {}", e, history.size());
			}
		});
	}


	public void createVietinVirtualAcctTransHistory(Date currDate, String status, String virtualAcctVar, String virtualAcctId, String transUniqueKey, String merchantCode, BigDecimal amount, String backendUrl, Date releaseTime, String qrUrl, UUID vietinNotiUuid, String remark) {
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
		history.setBankCode(OneFinConstants.BankListQrService.VCCB.getBankCode());
		LOGGER.info("Stored virtual account history: {}", history);
		virtualAcctTransHistoryRepo.save(history);
	}

	boolean checkKey(String requestBody) throws Exception {
		PrivateKey privateKey = bvbEncryptUtil.readPrivateKeyBVB(env.getProperty("bvb.virtualAcct.onefinPrivateKey"));
		String digitalSignature = bvbEncryptUtil.sign(requestBody, privateKey);
		InputStream readKey = new FileInputStream(env.getProperty("bvb.virtualAcct.onefinPublicKey"));
		return BVBEncryptUtil.bvbVerify(readKey, requestBody, digitalSignature);
	}

	@MeasureExcutionTime
	public VietinVirtualAcctTransHistory releaseVirtualAcctFromNotify(VietinNotifyTransTable vietinNotifyTrans) throws Exception {

		// Check callback batch api
		if (vietinNotifyTrans.getMsgType().equals(BankConstants.BVBTransMsgType.BATCH.getValue())) {

			Optional<VietinNotifyTransTable> historyNotifyCheck = vietinNotifyTransTableRepo.findByTransIdAndAmountAndBankCodeAndMsgTypeAndLimit1(vietinNotifyTrans.getTransId(), vietinNotifyTrans.getAmount(), OneFinConstants.BankListQrService.VCCB.getBankCode(), vietinNotifyTrans.getRemark(), BankConstants.BVBTransMsgType.INDIVIDUAL.getValue());

			if (historyNotifyCheck.isPresent()) {
				LOGGER.info("receive callback batch api from bvbank, no need to reconciliation");
				return null;
			}
		}

		Optional<VietinVirtualAcctTable> item = virtualAcctRepo.findByVirtualAcctIdAndInUse(vietinNotifyTrans.getRecvVirtualAcctId(), true, OneFinConstants.BankListQrService.VCCB.getBankCode());
		List<VietinVirtualAcctTransHistory> historyDataPending = virtualAcctTransHistoryRepo.findByVirtualAcctIdAndTranStatusAndAmount(vietinNotifyTrans.getRecvVirtualAcctId(), OneFinConstants.TRANS_PENDING, new BigDecimal(vietinNotifyTrans.getAmount()), OneFinConstants.BankListQrService.VCCB.getBankCode());

		DateTime currDate = dateTimeHelper.currentDateTime(OneFinConstants.HO_CHI_MINH_TIME_ZONE);
		if (item.isPresent() && historyDataPending.size() == 1) {
			// update virtual account
			try {
//				BVBVirtualAcctInfoDetailResponse updateResponse = checkVirtualAccountDetailAndUpdate(vietinNotifyTrans.getRecvVirtualAcctId());
//				if (updateResponse.getData().getAccStat().equals(BVBConstants.BVBVirtualAcctStatus.OPEN.getValue())) {
//
//				}
				// close virtual account
				closeVirtualAcct(item.get().getVirtualAcctId());
			} catch (Exception e) {
				LOGGER.error("Process update virtual account failed", e);
			}
			// Update history
			virtualAcctTransHistoryRepo.updateVietinVirtualAcctByStatus(OneFinConstants.TRANS_SUCCESS, currDate.toDate(), vietinNotifyTrans.getId(), vietinNotifyTrans.getRecvVirtualAcctId(), OneFinConstants.TRANS_PENDING, OneFinConstants.BankListQrService.VCCB.getBankCode(), historyDataPending.get(0).getTransUniqueKey());

			//Update watching transactions
			virtualAcctTransWatchingRepo.findById(historyDataPending.get(0).getTransUniqueKey()).ifPresent(t -> {
				t.setNotified(true);
				virtualAcctTransWatchingRepo.save(t);
			});

			// Update virtual account
			virtualAcctRepo.updateVietinVirtualAcctIdInUse(vietinNotifyTrans.getRecvVirtualAcctId(), false, currDate.toDate(), currDate.toDate(), OneFinConstants.BankListQrService.VCCB.getBankCode());
			LOGGER.info("Released virtual account: {}", vietinNotifyTrans);
			return historyDataPending.get(0);
		} else {
			LOGGER.error("Virtual acct {} and trans {} history not found!", item.isPresent(), historyDataPending.size());
		}
		List<VietinVirtualAcctTransHistory> historyDataTimeout = virtualAcctTransHistoryRepo.findByVirtualAcctIdAndTranStatusAndAmountAndLimit1(vietinNotifyTrans.getRecvVirtualAcctId(), OneFinConstants.TRANS_TIMEOUT, new BigDecimal(vietinNotifyTrans.getAmount()), TIME_OUT_CONSTANT, OneFinConstants.BankListQrService.VCCB.getBankCode());

		if (historyDataTimeout.size() == 1 && vietinNotifyTrans.getRemark() != null && historyDataTimeout.get(0).getRemark() != null && Pattern.compile(Pattern.quote(historyDataTimeout.get(0).getRemark()), Pattern.CASE_INSENSITIVE).matcher(vietinNotifyTrans.getRemark()).find()) {
			// Update history

			virtualAcctTransHistoryRepo.updateVirtualAcctById(OneFinConstants.TRANS_SUCCESS, currDate.toDate(), vietinNotifyTrans.getId(), historyDataTimeout.get(0).getId(), OneFinConstants.BankListQrService.VCCB.getBankCode());
			LOGGER.info("Update timeout trans successfully: {}, {}", vietinNotifyTrans, historyDataTimeout.get(0));
			return historyDataTimeout.get(0);
		} else {

			LOGGER.error("Not found this virtual account to release => Waiting settle: {}, {}, {}", vietinNotifyTrans, item, historyDataPending.size());
			createVietinVirtualAcctTransHistory(currDate.toDate(), OneFinConstants.TRANS_DISPUTE, null, vietinNotifyTrans.getRecvVirtualAcctId(), null, null, new BigDecimal(vietinNotifyTrans.getAmount()), null, null, null, vietinNotifyTrans.getId(), vietinNotifyTrans.getRemark());
			return null;
		}
	}

	public void closeVirtualAcct(String virtualAcctId) throws Exception {
		LOGGER.info("Close virtual account");
		BVBVirtualAcctCloseRequest requestBody = new BVBVirtualAcctCloseRequest();
		requestBody.setRequestId(RandomStringUtils.random(12, true, true));
		requestBody.setFrom(configLoader.getBvbVirtualAcctPartnerCode());
		BVBVirtualAcctCommonPartnerRequest requestBodyData = new BVBVirtualAcctCommonPartnerRequest();
		requestBodyData.setAccNo(virtualAcctId);
		requestBodyData.setPartnerCode(configLoader.getBvbVirtualAcctPartnerCode());
		requestBody.setData(requestBodyData);

		LOGGER.info("URL: {}", configLoader.getBvbVirtualAcctCloseVirtualAcctUrl());

		// Request close api
		ResponseEntity<?> bvbResponse = bvbRequestUtil.requestBVB(requestBody, configLoader.getBvbVirtualAcctCloseVirtualAcctUrl(), requestBody.getRequestId(), BankConstants.BVB_BACKUP_CLOSE_PREFIX);

		// validate signature
		if (!bvbRequestUtil.validateBvbSignature(bvbResponse)) {
			LOGGER.error("Invalid signature from BVB, close failed!!!");
			return;
		}
		// construct BVB close virtual code response body
		BVBVirtualAcctCloseResponse bvbVirtualAcctCloseResponse = modelMapper.map(bvbResponse.getBody(), BVBVirtualAcctCloseResponse.class);

		closeApiUpdateTable(bvbVirtualAcctCloseResponse, requestBody);
		LOGGER.info("Close virtual account done!");
	}

	public BVBVirtualAcctInfoDetailResponse checkVirtualAccountDetailAndUpdate(String virtualAcctId) throws Exception {

		try {
			LOGGER.info("URL: {}", configLoader.getBvbVirtualAcctCheckDetailVirtualAcctUrl());

			BVBVirtualAcctInfoDetailRequest bvbVirtualAcctInfoDetailRequest = new BVBVirtualAcctInfoDetailRequest();

			bvbVirtualAcctInfoDetailRequest.setFrom(configLoader.getBvbVirtualAcctPartnerCode());
			bvbVirtualAcctInfoDetailRequest.setRequestId(RandomStringUtils.random(12, true, true));
			BVBVirtualAcctCommonPartnerRequest bvbVirtualAcctCommonPartnerRequest = new BVBVirtualAcctCommonPartnerRequest();

			bvbVirtualAcctCommonPartnerRequest.setAccNo(virtualAcctId);
			bvbVirtualAcctCommonPartnerRequest.setPartnerCode(configLoader.getBvbVirtualAcctPartnerCode());
			bvbVirtualAcctInfoDetailRequest.setData(bvbVirtualAcctCommonPartnerRequest);
			// Request view Detail api
			ResponseEntity<?> bvbResponse = bvbRequestUtil.requestBVB(bvbVirtualAcctInfoDetailRequest, configLoader.getBvbVirtualAcctCheckDetailVirtualAcctUrl(), bvbVirtualAcctInfoDetailRequest.getRequestId(), BankConstants.BVB_BACKUP_VIEW_DETAIL_PREFIX);

			// validate signature
			boolean responseSignature = bvbRequestUtil.validateBvbSignature(bvbResponse);
			if (!responseSignature) {
				return null;
			}

			// construct BVB virtual code response body
			BVBVirtualAcctInfoDetailResponse bvbVirtualAcctInfoDetailResponse = modelMapper.map(bvbResponse.getBody(), BVBVirtualAcctInfoDetailResponse.class);

			viewAccountDetailApiUpdateTable(bvbVirtualAcctInfoDetailResponse, bvbVirtualAcctInfoDetailRequest);
			return bvbVirtualAcctInfoDetailResponse;
		} catch (Exception e) {
			LOGGER.error("Update virtual acct info failed", e);
			return null;
		}
	}

	public BVBVirtualAcctInfoDetailResponse checkVirtualAccountDetailCollection(String virtualAcctId) throws Exception {

		try {
			LOGGER.info("URL: {}", configLoader.getBvbVirtualAcctCheckDetailVirtualAcctUrl());

			BVBVirtualAcctInfoDetailRequest bvbVirtualAcctInfoDetailRequest = new BVBVirtualAcctInfoDetailRequest();

			bvbVirtualAcctInfoDetailRequest.setFrom(configLoader.getBvbVirtualAcctPartnerCode());
			bvbVirtualAcctInfoDetailRequest.setRequestId(RandomStringUtils.random(12, true, true));
			BVBVirtualAcctCommonPartnerRequest bvbVirtualAcctCommonPartnerRequest = new BVBVirtualAcctCommonPartnerRequest();

			bvbVirtualAcctCommonPartnerRequest.setAccNo(virtualAcctId);
			bvbVirtualAcctCommonPartnerRequest.setPartnerCode(configLoader.getBvbVirtualAcctPartnerCode());
			bvbVirtualAcctInfoDetailRequest.setData(bvbVirtualAcctCommonPartnerRequest);
			// Request view Detail api
			ResponseEntity<?> bvbResponse = bvbRequestUtil.requestBVB(bvbVirtualAcctInfoDetailRequest, configLoader.getBvbVirtualAcctCheckDetailVirtualAcctUrl(), bvbVirtualAcctInfoDetailRequest.getRequestId(), BankConstants.BVB_BACKUP_VIEW_DETAIL_PREFIX);

			// validate signature
			boolean responseSignature = bvbRequestUtil.validateBvbSignature(bvbResponse);
			if (!responseSignature) {
				return null;
			}

			// construct BVB virtual code response body
			BVBVirtualAcctInfoDetailResponse bvbVirtualAcctInfoDetailResponse = modelMapper.map(bvbResponse.getBody(), BVBVirtualAcctInfoDetailResponse.class);

			// check request status
			if (!bvbVirtualAcctInfoDetailResponse.getRCode().equals(BankConstants.BVB_REQUEST_STATUS_SUCCESS)) {
				LOGGER.error("request not success !!!");
				throw new RuntimeInternalServerException("request not success !!!!!");
			}

			return bvbVirtualAcctInfoDetailResponse;
		} catch (Exception e) {
			LOGGER.error("Check virtual acct {} info failed", virtualAcctId, e);
			return null;
		}
	}

	public BVBVirtualAcctReopenResponse reopenVirtualAcctCollectionAndUpdate(String virtualAcctId, BigDecimal amount, String merchantCode, String virtualAcctName) throws Exception {

		try {
			LOGGER.info("URL reopen: {}", configLoader.getBvbVirtualAcctReopenVirtualAcctUrl());

			BVBVirtualAcctReopenRequest bvbVirtualAcctReopenRequest = new BVBVirtualAcctReopenRequest();

			bvbVirtualAcctReopenRequest.setFrom(configLoader.getBvbVirtualAcctPartnerCode());
			bvbVirtualAcctReopenRequest.setRequestId(RandomStringUtils.random(12, true, true));

			BVBVirtualAcctCommonPartnerRequest bvbVirtualAcctCommonPartnerRequest = new BVBVirtualAcctCommonPartnerRequest();

			bvbVirtualAcctCommonPartnerRequest.setAccNo(virtualAcctId);
			bvbVirtualAcctCommonPartnerRequest.setClientUserId(merchantCode);
			bvbVirtualAcctCommonPartnerRequest.setFixedAmount(amount);
			bvbVirtualAcctCommonPartnerRequest.setAccNameSuffix(virtualAcctName);
			bvbVirtualAcctCommonPartnerRequest.setPartnerCode(configLoader.getBvbVirtualAcctPartnerCode());
			bvbVirtualAcctReopenRequest.setData(bvbVirtualAcctCommonPartnerRequest);

			// request BVB reopen virtual acc
			ResponseEntity<?> openResponseResponseEntity = bvbRequestUtil.requestBVB(bvbVirtualAcctReopenRequest,
					configLoader.getBvbVirtualAcctReopenVirtualAcctUrl(),
					bvbVirtualAcctReopenRequest.getRequestId(),
					BankConstants.BVB_BACKUP_REOPEN_PREFIX);

			// validate signature
			if (!bvbRequestUtil.validateBvbSignature(openResponseResponseEntity)) {
				String errorString = String.format("Request VA with acc id %s open failed", virtualAcctId);
				LOGGER.error(errorString);
				throw new RuntimeInternalServerException(errorString);
			}

			BVBVirtualAcctReopenResponse bvbVirtualAcctReopenResponse = modelMapper.map(openResponseResponseEntity.getBody(), BVBVirtualAcctReopenResponse.class);
			LOGGER.info("Reopen response: " + bvbVirtualAcctReopenResponse);

			reOpenApiUpdateTable(bvbVirtualAcctReopenResponse, bvbVirtualAcctReopenRequest);

			// check request status
			if (!bvbVirtualAcctReopenResponse.getRCode().equals(BankConstants.BVB_REQUEST_STATUS_SUCCESS)) {
				LOGGER.error("request not success !!!");
				throw new RuntimeInternalServerException("request not success !!!!!");
			}

			return bvbVirtualAcctReopenResponse;
		} catch (Exception e) {
			LOGGER.error("reopen virtual acct {} failed", virtualAcctId, e);
			return null;
		}
	}

	public BVBVirtualAcctUpdateResponse updateVirtualAcctCollectionAndUpdate(String virtualAcctId, BigDecimal amount, String merchantCode, String virtualAcctName) throws Exception {

		try {
			LOGGER.info("URL update virtual acct: {}", configLoader.getBvbVirtualAcctUpdateVirtualAcctUrl());

			BVBVirtualAcctUpdateRequest bvbVirtualAcctUpdateRequest = new BVBVirtualAcctUpdateRequest();

			bvbVirtualAcctUpdateRequest.setFrom(configLoader.getBvbVirtualAcctPartnerCode());
			bvbVirtualAcctUpdateRequest.setRequestId(RandomStringUtils.random(12, true, true));

			BVBVirtualAcctCommonPartnerRequest bvbVirtualAcctCommonPartnerRequest = new BVBVirtualAcctCommonPartnerRequest();

			bvbVirtualAcctCommonPartnerRequest.setAccNo(virtualAcctId);
			bvbVirtualAcctCommonPartnerRequest.setClientUserId(merchantCode);
			bvbVirtualAcctCommonPartnerRequest.setFixedAmount(amount);
			bvbVirtualAcctCommonPartnerRequest.setAccNameSuffix(virtualAcctName);
			bvbVirtualAcctCommonPartnerRequest.setPartnerCode(configLoader.getBvbVirtualAcctPartnerCode());

			bvbVirtualAcctUpdateRequest.setData(bvbVirtualAcctCommonPartnerRequest);

			// request BVB update virtual acc
			ResponseEntity<?> updateResponseEntity = bvbRequestUtil.requestBVB(bvbVirtualAcctUpdateRequest, configLoader.getBvbVirtualAcctUpdateVirtualAcctUrl(), bvbVirtualAcctUpdateRequest.getRequestId(), BankConstants.BVB_BACKUP_UPDATE_ACCOUNT_PREFIX);

			// validate signature
			if (!bvbRequestUtil.validateBvbSignature(updateResponseEntity)) {
				LOGGER.error("Invalid response from BVB !!!");
				throw new RuntimeInternalServerException("Signature validate failed !!!!!");
			}

			BVBVirtualAcctUpdateResponse bvbVirtualAcctUpdateResponse = modelMapper.map(updateResponseEntity.getBody(), BVBVirtualAcctUpdateResponse.class);
			LOGGER.info("Reopen response: " + bvbVirtualAcctUpdateResponse);

			updateApiUpdateTable(bvbVirtualAcctUpdateResponse, bvbVirtualAcctUpdateRequest);

			// check request status
			if (!bvbVirtualAcctUpdateResponse.getRCode().equals(BankConstants.BVB_REQUEST_STATUS_SUCCESS)) {
				LOGGER.error("request not success !!!");
				throw new RuntimeInternalServerException("request not success !!!!!");
			}


			return bvbVirtualAcctUpdateResponse;
		} catch (Exception e) {
			LOGGER.error("Update virtual acct {} failed", virtualAcctId, e);
			return null;
		}
	}

	public BVBVirtualAcctCloseResponse closeVirtualAcctCollectionAndUpdate(String virtualAcctId) throws Exception {
		try {
			LOGGER.info("Close virtual account");
			BVBVirtualAcctCloseRequest requestBody = new BVBVirtualAcctCloseRequest();
			requestBody.setRequestId(RandomStringUtils.random(12, true, true));
			requestBody.setFrom(configLoader.getBvbVirtualAcctPartnerCode());
			BVBVirtualAcctCommonPartnerRequest requestBodyData = new BVBVirtualAcctCommonPartnerRequest();
			requestBodyData.setAccNo(virtualAcctId);
			requestBodyData.setPartnerCode(configLoader.getBvbVirtualAcctPartnerCode());
			requestBody.setData(requestBodyData);

			LOGGER.info("URL: {}", configLoader.getBvbVirtualAcctCloseVirtualAcctUrl());

			// Request close api
			ResponseEntity<?> bvbResponse = bvbRequestUtil.requestBVB(requestBody, configLoader.getBvbVirtualAcctCloseVirtualAcctUrl(), requestBody.getRequestId(), BankConstants.BVB_BACKUP_CLOSE_PREFIX);

			// validate signature
			if (!bvbRequestUtil.validateBvbSignature(bvbResponse)) {
				LOGGER.error("Invalid signature from BVB, close failed!!!");
				return null;
			}
			// construct BVB close virtual code response body
			BVBVirtualAcctCloseResponse bvbVirtualAcctCloseResponse = modelMapper.map(bvbResponse.getBody(), BVBVirtualAcctCloseResponse.class);

			closeApiUpdateTable(bvbVirtualAcctCloseResponse, requestBody);
			LOGGER.info("Close virtual account done!");

			// check request status
			if (!bvbVirtualAcctCloseResponse.getRCode().equals(BankConstants.BVB_REQUEST_STATUS_SUCCESS)) {
				LOGGER.error("request not success !!!");
				throw new RuntimeInternalServerException("request not success !!!!!");
			}

			return bvbVirtualAcctCloseResponse;
		} catch (Exception ex) {
			LOGGER.error("Close virtual acct {} failed", virtualAcctId, ex);
			return null;
		}
	}

	public void viewAccountDetailApiUpdateTable(BVBVirtualAcctInfoDetailResponse responseBody, BVBVirtualAcctInfoDetailRequest requestBody) throws ParseException {
		if (responseBody.getRCode().equals(BankConstants.BVB_REQUEST_STATUS_SUCCESS)) {
			Date currentDate = dateTimeHelper.currentDate(OneFinConstants.HO_CHI_MINH_TIME_ZONE);

			if (responseBody.getData().getAccStat().equals(BankConstants.BVBVirtualAcctStatus.OPEN.getValue())) {
				virtualAcctRepo.updateBVBVirtualAcctOpenNotifyApi(responseBody.getData().getAccNo(), responseBody.getData().getAccStat(), responseBody.getData().getFixedAmount(), responseBody.getData().getClientUserId(), extractVarName(responseBody.getData().getAccName()), responseBody.getData().getAccName(), currentDate, OneFinConstants.BankListQrService.VCCB.getBankCode());

			}
			if (responseBody.getData().getAccStat().equals(BankConstants.BVBVirtualAcctStatus.CLOSE.getValue())) {
				virtualAcctRepo.updateBVBVirtualAcctCloseNotifyApi(responseBody.getData().getAccNo(), bvbRequestUtil.readDateString(responseBody.getData().getCloseDate()), responseBody.getData().getAccStat(), responseBody.getData().getFixedAmount(), responseBody.getData().getClientUserId(), extractVarName(responseBody.getData().getAccName()), responseBody.getData().getAccName(), currentDate, OneFinConstants.BankListQrService.VCCB.getBankCode());

			}
		}
		updateVirtualAccStatusHistoryTableWithViewDetailApi(requestBody, responseBody);
	}

	public String extractVarName(String AcctName) {

		try {
			String[] arrOfStr = AcctName.split("_", 2);
			return arrOfStr[1];

		} catch (Exception e) {
			LOGGER.warn("Extract acct name failed return orgin String");
			if (AcctName != null) {
				return AcctName;
			} else {
				return "";
			}

		}
	}

	private void updateVirtualAccStatusHistoryTableWithViewDetailApi(BVBVirtualAcctInfoDetailRequest requestBody, BVBVirtualAcctInfoDetailResponse responseBody) {
		VietinVirtualAcctStatusHistory vietinVirtualAcctStatusHistory = new VietinVirtualAcctStatusHistory();
		updateVirtualAccStatusHistoryTableCommonApi(vietinVirtualAcctStatusHistory, responseBody.getData(), responseBody, requestBody.getData(), requestBody);
		vietinVirtualAcctStatusHistory.setClientUserId(responseBody.getData().getClientUserId());
		vietinVirtualAcctStatusHistory.setVirtualAcctName(extractVarName(responseBody.getData().getAccName()));

		virtualAcctStatusHistoryRepo.save(vietinVirtualAcctStatusHistory);
	}

	private <X extends BVBVirtualAcctCommonResponse, Y extends BVBVirtualAcctCommonRequest> void updateVirtualAccStatusHistoryTableCommonApi(VietinVirtualAcctStatusHistory vietinVirtualAcctStatusHistory, BVBVirtualAcctCommonPartnerResponse partnerResponse, X commonResponse, BVBVirtualAcctCommonPartnerRequest partnerRequest, Y commonRequest) {
		vietinVirtualAcctStatusHistory.setRequestId(commonRequest.getRequestId());
		vietinVirtualAcctStatusHistory.setVirtualAcctVar(extractVarNumber(partnerRequest.getAccNo()));
		vietinVirtualAcctStatusHistory.setVirtualAcctName(partnerRequest.getAccNameSuffix());
		if (commonResponse.getRCode().equals(BankConstants.BVB_REQUEST_STATUS_SUCCESS)) {
			if (partnerResponse.getFixedAmount() != null) {
				vietinVirtualAcctStatusHistory.setFixedAmount(partnerResponse.getFixedAmount());
			}
			vietinVirtualAcctStatusHistory.setNewStatus(partnerResponse.getAccStat());
		} else {
			try {
				vietinVirtualAcctStatusHistory.setNewStatus(partnerResponse.getError());
				vietinVirtualAcctStatusHistory.setDataErrorMessage(partnerResponse.getMessage());
			} catch (Exception e) {
				vietinVirtualAcctStatusHistory.setNewStatus(commonResponse.getRCode());
				LOGGER.warn("String error when reading error field, try reading rCode");
			}

		}

		// default in active status
		vietinVirtualAcctStatusHistory.setStatusCode(commonResponse.getRCode());
		vietinVirtualAcctStatusHistory.setBankCode(OneFinConstants.BankListQrService.VCCB.getBankCode());
		vietinVirtualAcctStatusHistory.setMsgId(commonResponse.getMsgId());
		vietinVirtualAcctStatusHistory.setBankTime(commonResponse.getBankTime());
		vietinVirtualAcctStatusHistory.setRequestErrorMessage(commonResponse.getRMsg());

	}

	private String extractVarNumber(String accNo) {
		String lastFourChars = "";
		try {
			if (accNo.length() > 7) {
				lastFourChars = accNo.substring(accNo.length() - 7);
			} else {
				lastFourChars = accNo;
			}
			return lastFourChars;
		} catch (Exception e) {
			LOGGER.warn("Extract acct name failed return orgin String");
			if (accNo != null) {
				return accNo;
			} else {
				return "";
			}
		}
	}

	public <Q, R> ResponseEntity<?> bvbVirtualRequest(Q requestBody, String url, String requestId, String prefix, Class<R> responseType) throws Exception {

		// Request create api
		ResponseEntity<?> bvbResponse = bvbRequestUtil.requestBVB(requestBody, url, requestId, prefix);

		// validate signature
		ConnResponse responseSignature = bvbRequestUtil.checkSignature(bvbResponse);
		if (responseSignature != null) {
			return new ResponseEntity<>(responseSignature, HttpStatus.BAD_REQUEST);
		}

		// Construct response
		R returnResponse = modelMapper.map(bvbResponse.getBody(), responseType);

		return new ResponseEntity<>(returnResponse, HttpStatus.OK);
	}

	public BVBVirtualAcctCreateResponse checkAcctVarFromAccountList(String acctVar) throws Exception {
		BVBAccountListRequest bvbAccountListRequest = new BVBAccountListRequest();
		bvbAccountListRequest.setFrom(configLoader.getBvbVirtualAcctPartnerCode());
		bvbAccountListRequest.setRequestId(RandomStringUtils.random(12, true, true));
		BVBAccountListRequestData bvbAccountListRequestData = new BVBAccountListRequestData();
		bvbAccountListRequestData.setPartnerCode(configLoader.getBvbVirtualAcctPartnerCode());
		bvbAccountListRequest.setData(bvbAccountListRequestData);
		LOGGER.info("Find Virtual acct list requestMap {}", bvbAccountListRequest);
		ResponseEntity<?> bvbResponse = bvbRequestUtil.requestBVB(
				bvbAccountListRequest,
				configLoader.getBvbFindVirtualAcctListUrl(),
				bvbAccountListRequest.getRequestId(),
				"findAccounts");

		// validate signature
		if (!bvbRequestUtil.validateBvbSignature(bvbResponse)) {
			LOGGER.error("Invalid signature from response of BVB: {}", bvbResponse.getBody());
			return null;
		}

		BVBAccountListResponse bvbAccountListResponse = modelMapper.map(bvbResponse.getBody(), BVBAccountListResponse.class);

		if (!bvbAccountListResponse.getRCode().equals(BankConstants.BVB_REQUEST_STATUS_SUCCESS)) {
			LOGGER.error("Request account list failed: {}", bvbResponse.getBody());
			return null;
		}

		if (bvbAccountListResponse.getData().getContent().size() > 0) {
			for (BVBVirtualAcctCommonPartnerResponse e : bvbAccountListResponse.getData().getContent()) {
//				LOGGER.info("Compare {} vs {} result {}",
//						e.getAccNo(), acctVar, e.getAccNo().contains(acctVar));
				if (e.getAccNo().contains(acctVar)) {
					BVBVirtualAcctCreateResponse bvbVirtualAcctCreateResponse = new BVBVirtualAcctCreateResponse();
					bvbVirtualAcctCreateResponse.setData(e);
					bvbVirtualAcctCreateResponse.setBankTime(bvbAccountListResponse.getBankTime());
					bvbVirtualAcctCreateResponse.setMsgId(bvbAccountListResponse.getMsgId());
					bvbVirtualAcctCreateResponse.setRequestId(bvbAccountListResponse.getRequestId());
					bvbVirtualAcctCreateResponse.setRCode(bvbAccountListResponse.getRCode());
					return bvbVirtualAcctCreateResponse;
				}
			}
		}
		return null;
	}


	// ******************************************** UPDATE TABLE FUNCTIONS ******************************************* //
	private void updateVirtualAccStatusHistoryTableWithCreateApi(BVBVirtualAcctCreateResponse responseBody, BVBVirtualAcctCreateRequest requestBody) {
		VietinVirtualAcctStatusHistory vietinVirtualAcctStatusHistory = new VietinVirtualAcctStatusHistory();

		LOGGER.info("BVBVirtualAcctCreateResponse: {}\n" + "BVBVirtualAcctCreateRequest: {}", responseBody, requestBody);
		updateVirtualAccStatusHistoryTableCommonApi(vietinVirtualAcctStatusHistory, responseBody.getData(), responseBody, requestBody.getData(), requestBody);
		vietinVirtualAcctStatusHistory.setClientUserId(responseBody.getData().getClientUserId());
		vietinVirtualAcctStatusHistory.setVirtualAcctVar(extractVarNumber(responseBody.getData().getAccNo()));
		vietinVirtualAcctStatusHistory.setVirtualAcctName(requestBody.getData().getAccNameSuffix());

		virtualAcctStatusHistoryRepo.save(vietinVirtualAcctStatusHistory);
	}

	public void updateApiUpdateTable(BVBVirtualAcctUpdateResponse responseBody, BVBVirtualAcctUpdateRequest requestBody) {
		if (responseBody.getRCode().equals(BankConstants.BVB_REQUEST_STATUS_SUCCESS)) {
			Date currentDate = dateTimeHelper.currentDate(OneFinConstants.HO_CHI_MINH_TIME_ZONE);
			virtualAcctRepo.updateBVBVirtualAcctUpdateApi(responseBody.getData().getAccNo(), requestBody.getData().getAccNameSuffix(), responseBody.getData().getFixedAmount(), responseBody.getData().getClientUserId(), responseBody.getData().getAccName(), currentDate, OneFinConstants.BankListQrService.VCCB.getBankCode());
		}
		updateVirtualAccStatusHistoryTableWithUpdateApi(responseBody, requestBody);
	}

	private void updateVirtualAccStatusHistoryTableWithUpdateApi(BVBVirtualAcctUpdateResponse responseBody, BVBVirtualAcctUpdateRequest requestBody) {

		VietinVirtualAcctStatusHistory vietinVirtualAcctStatusHistory = new VietinVirtualAcctStatusHistory();
		updateVirtualAccStatusHistoryTableCommonApi(vietinVirtualAcctStatusHistory, responseBody.getData(), responseBody, requestBody.getData(), requestBody);
		String clientUserId = responseBody.getData().getClientUserId();
		clientUserId = (clientUserId != null) ? clientUserId : "";
		vietinVirtualAcctStatusHistory.setClientUserId(clientUserId);
		vietinVirtualAcctStatusHistory.setVirtualAcctVar(extractVarNumber(responseBody.getData().getAccNo()));
		vietinVirtualAcctStatusHistory.setVirtualAcctName(requestBody.getData().getAccNameSuffix());
		virtualAcctStatusHistoryRepo.save(vietinVirtualAcctStatusHistory);
	}

	private void updateVirtualAccTableWithCreateApi(BVBVirtualAcctCreateResponse bvbVirtualAcctCreateResponse, BVBVirtualAcctCreateRequest requestBody, String poolName) {
		VietinVirtualAcctTable vietinVirtualAcctTable;
		Optional<VietinVirtualAcctTable> checkAccount = virtualAcctRepo.findFirstByVirtualAcctVarId(bvbVirtualAcctCreateResponse.getData().getAccNo(), OneFinConstants.BankListQrService.VCCB.getBankCode());
		// insert to Virtual acct Table
		vietinVirtualAcctTable = checkAccount.orElseGet(VietinVirtualAcctTable::new);
		vietinVirtualAcctTable.setInUse(false);
		vietinVirtualAcctTable.setPoolName(poolName);
		vietinVirtualAcctTable.setBankCode(OneFinConstants.BankListQrService.VCCB.getBankCode());
		vietinVirtualAcctTable.setQrURL("");
		vietinVirtualAcctTable.setLanguage(OneFinConstants.LANGUAGE.VIETNAMESE.getValue());
		vietinVirtualAcctTable.setVirtualAcctName(requestBody.getData().getAccNameSuffix());
		vietinVirtualAcctTable.setVirtualAcctVar(extractVarNumber(bvbVirtualAcctCreateResponse.getData().getAccNo()));
		vietinVirtualAcctTable.setCustomerName(bvbVirtualAcctCreateResponse.getData().getClientUserId());
		vietinVirtualAcctTable.setPartnerAccNo(bvbVirtualAcctCreateResponse.getData().getPartnerAccNo());
		vietinVirtualAcctTable.setAccType(bvbVirtualAcctCreateResponse.getData().getAccType());
		vietinVirtualAcctTable.setBankTime(bvbVirtualAcctCreateResponse.getBankTime());
		vietinVirtualAcctTable.setPartnerCode(bvbVirtualAcctCreateResponse.getData().getPartnerCode());
		vietinVirtualAcctTable.setMakerId(bvbVirtualAcctCreateResponse.getData().getMakerId());
		vietinVirtualAcctTable.setAccName(bvbVirtualAcctCreateResponse.getData().getAccName());
		vietinVirtualAcctTable.setCheckerId(bvbVirtualAcctCreateResponse.getData().getCheckerId());
		vietinVirtualAcctTable.setMsgId(bvbVirtualAcctCreateResponse.getMsgId());
		vietinVirtualAcctTable.setCcy(bvbVirtualAcctCreateResponse.getData().getCcy());
		vietinVirtualAcctTable.setVirtualAcctStatus(bvbVirtualAcctCreateResponse.getData().getAccStat());
		vietinVirtualAcctTable.setVirtualAcctId(bvbVirtualAcctCreateResponse.getData().getAccNo());
		vietinVirtualAcctTable.setEffectiveDate(bvbVirtualAcctCreateResponse.getData().getOpenDate());
		vietinVirtualAcctTable.setFixedAmount(bvbVirtualAcctCreateResponse.getData().getFixedAmount());
		vietinVirtualAcctTable.setReOpenDate(null);
		vietinVirtualAcctTable.setCloseDate(null);
		virtualAcctRepo.save(vietinVirtualAcctTable);
	}

	public void reOpenApiUpdateTable(BVBVirtualAcctReopenResponse responseBody, BVBVirtualAcctReopenRequest requestBody) throws ParseException {
		if (responseBody.getRCode().equals(BankConstants.BVB_REQUEST_STATUS_SUCCESS)) {
			Date currentDate = dateTimeHelper.currentDate(OneFinConstants.HO_CHI_MINH_TIME_ZONE);

			virtualAcctRepo.updateBVBVirtualAcctReopenApi(responseBody.getData().getAccNo(), bvbRequestUtil.readDateString(responseBody.getData().getReOpenDate()), responseBody.getData().getAccStat(), responseBody.getData().getFixedAmount(), responseBody.getData().getClientUserId(), requestBody.getData().getAccNameSuffix(), responseBody.getData().getAccName(), currentDate, OneFinConstants.BankListQrService.VCCB.getBankCode());
		}

		updateVirtualAccStatusHistoryTableWithReopenApi(requestBody, responseBody);
	}

	private void updateVirtualAccStatusHistoryTableWithReopenApi(BVBVirtualAcctReopenRequest requestBody, BVBVirtualAcctReopenResponse responseBody) {

		VietinVirtualAcctStatusHistory vietinVirtualAcctStatusHistory = new VietinVirtualAcctStatusHistory();
		updateVirtualAccStatusHistoryTableCommonApi(vietinVirtualAcctStatusHistory, responseBody.getData(), responseBody, requestBody.getData(), requestBody);
		vietinVirtualAcctStatusHistory.setClientUserId(responseBody.getData().getClientUserId());
		vietinVirtualAcctStatusHistory.setVirtualAcctVar(extractVarNumber(responseBody.getData().getAccNo()));
		vietinVirtualAcctStatusHistory.setVirtualAcctName(requestBody.getData().getAccNameSuffix());
		virtualAcctStatusHistoryRepo.save(vietinVirtualAcctStatusHistory);
	}

	public void closeApiUpdateTable(BVBVirtualAcctCloseResponse responseBody, BVBVirtualAcctCloseRequest requestBody) throws ParseException {
		if (responseBody.getRCode().equals(BankConstants.BVB_REQUEST_STATUS_SUCCESS)) {
			Date currentDate = dateTimeHelper.currentDate(OneFinConstants.HO_CHI_MINH_TIME_ZONE);

			virtualAcctRepo.updateBVBVirtualAcctCloseDate(responseBody.getData().getAccNo(), bvbRequestUtil.readDateString(responseBody.getData().getCloseDate()), responseBody.getData().getAccStat(), currentDate, OneFinConstants.BankListQrService.VCCB.getBankCode());
		}

		updateVirtualAccStatusHistoryTableWithCloseApi(requestBody, responseBody);
	}

	private void updateVirtualAccStatusHistoryTableWithCloseApi(BVBVirtualAcctCloseRequest requestBody, BVBVirtualAcctCloseResponse responseBody) {
		VietinVirtualAcctStatusHistory vietinVirtualAcctStatusHistory = new VietinVirtualAcctStatusHistory();
		updateVirtualAccStatusHistoryTableCommonApi(vietinVirtualAcctStatusHistory, responseBody.getData(), responseBody, requestBody.getData(), requestBody);

		vietinVirtualAcctStatusHistory.setClientUserId(responseBody.getData().getClientUserId());
		vietinVirtualAcctStatusHistory.setVirtualAcctName(extractVarName(responseBody.getData().getAccName()));

		virtualAcctStatusHistoryRepo.save(vietinVirtualAcctStatusHistory);
	}

	public void createApiUpdateTable(BVBVirtualAcctCreateResponse bvbVirtualAcctCreateResponse, BVBVirtualAcctCreateRequest requestBody, String poolName) throws Exception {
		if (bvbVirtualAcctCreateResponse.getRCode().equals(BankConstants.BVB_REQUEST_STATUS_SUCCESS)) {
			// insert to virtual acct table
			updateVirtualAccTableWithCreateApi(bvbVirtualAcctCreateResponse, requestBody, poolName);
			// insert to virtual acct status history table
			updateVirtualAccStatusHistoryTableWithCreateApi(bvbVirtualAcctCreateResponse, requestBody);
		} else if (bvbVirtualAcctCreateResponse.getRCode().equals(BankConstants.BVB_REQUEST_STATUS_FAILED)) {

			if (bvbVirtualAcctCreateResponse.getData().getError().equals(BankConstants.BVB_ACCOUNT_ALREADY_EXIST_ERROR_CODE)) {
				LOGGER.warn("Virtual acct already existed, try to find in account list");

				BVBVirtualAcctCreateResponse commonResponse = checkAcctVarFromAccountList(requestBody.getData().getAccNoSuffix());

				if (commonResponse != null) {

					Optional<VietinVirtualAcctTable> checkAccount = virtualAcctRepo.findFirstByVirtualAcctId(commonResponse.getData().getAccNo(), OneFinConstants.BankListQrService.VCCB.getBankCode());

					commonResponse.setRCode(bvbVirtualAcctCreateResponse.getRCode());
					commonResponse.setRequestId(bvbVirtualAcctCreateResponse.getRequestId());
					commonResponse.setMsgId(bvbVirtualAcctCreateResponse.getMsgId());
					commonResponse.setBankTime(bvbVirtualAcctCreateResponse.getBankTime());
					commonResponse.getData().setError(bvbVirtualAcctCreateResponse.getData().getError());
					commonResponse.getData().setMessage(bvbVirtualAcctCreateResponse.getData().getMessage());
					if (!checkAccount.isPresent()) {
						// insert to virtual acct table
						updateVirtualAccTableWithCreateApi(commonResponse, requestBody, poolName);
					} else {
						LOGGER.info("Virtual acct already in database");
					}
					// insert to virtual acct status history table
					updateVirtualAccStatusHistoryTableWithCreateApi(commonResponse, requestBody);
				} else {
					LOGGER.warn("Virtual acct not found in account list");

				}
			}
		}
	}

}
