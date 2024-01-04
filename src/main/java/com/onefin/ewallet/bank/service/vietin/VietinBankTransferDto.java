package com.onefin.ewallet.bank.service.vietin;

import com.onefin.ewallet.bank.common.VietinConstants;
import com.onefin.ewallet.bank.config.BankHelper;
import com.onefin.ewallet.bank.dto.vietin.*;
import com.onefin.ewallet.bank.repository.jpa.*;
import com.onefin.ewallet.bank.service.bvb.BVBTransferRequestUtil;
import com.onefin.ewallet.bank.service.common.ConfigLoader;
import com.onefin.ewallet.bank.service.common.NumberSequenceService;
import com.onefin.ewallet.common.base.constants.OneFinConstants;
import com.onefin.ewallet.common.base.errorhandler.RuntimeBadRequestException;
import com.onefin.ewallet.common.base.service.BaseService;
import com.onefin.ewallet.common.domain.bank.common.BankList;
import com.onefin.ewallet.common.domain.bank.common.BankListDetails;
import com.onefin.ewallet.common.domain.bank.transfer.BankTransferChildRecords;
import com.onefin.ewallet.common.domain.bank.transfer.BankTransferTransaction;
import com.onefin.ewallet.common.domain.errorCode.PartnerErrorCode;
import com.onefin.ewallet.common.utility.date.DateTimeHelper;
import com.onefin.ewallet.common.utility.string.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VietinBankTransferDto extends BaseService<BankTransferTransaction> {

	private static final Logger LOGGER = LoggerFactory.getLogger(VietinBankTransferDto.class);

	@Value("${bvb.IBFT.channel}")
	private String bvbTransferChannel;

	@Value("${bvb.IBFT.merchantId}")
	private String bvbTransferMerchantId;

	@Value("${bvb.IBFT.onefinClientCode}")
	private String onefinClientCode;

	@Autowired
	private ConfigLoader configLoader;

	@Autowired
	private VietinMessageUtil iMessageUtil;

	@Autowired
	private VietinEncryptUtil encryptUtil;

	@Autowired
	private PartnerErrorCodeRepo partnerErrorCodeRepo;

	@Autowired
	private NumberSequenceService numberSequenceService;

	@Autowired
	private DateTimeHelper dateTimeHelper;

	@Autowired
	private BankTransferRepo bankTransferRepo;

	@Autowired
	private ChildBankTransferRecordsRepo childBankTransferRecordsRepo;

	@Autowired
	private VietinMessageUtil vietinMessageUtil;

	@Autowired
	private BankListRepository bankListRepository;
	@Autowired
	private BankListDetailsRepository bankListDetailsRepository;

	@Autowired
	private VietinRequestUtil vietinRequestUtil;

	@Autowired
	private BVBTransferRequestUtil bvbTransferRequestUtil;

	@Autowired
	private BankHelper bankHelper;

	@Autowired
	@Qualifier("bankTransferRepo")
	public void setBankTransferRepo(BankTransferRepo<?> ewalletTransactionRepository) {
		this.setTransBaseRepository(ewalletTransactionRepository);
	}

	public BankTransferRequest buildBankTransfer(BankTransferRequest data) throws Exception {
		data.getRecords().stream().forEach(e -> {
			e.setSenderBankId(configLoader.getVietinPaymentBankTransferOnefinBankId().get(e.getSenderAccountOrder()));
			e.setSenderAcctId(configLoader.getVietinPaymentBankTransferOnefinAcctId().get(e.getSenderAccountOrder()));
		});
		data.setRequestId(numberSequenceService.nextVTBBankTransferTransId());
		data.setProviderId(configLoader.getVietinPaymentBankTransferProviderId());
		data.setMerchantId(configLoader.getVietinBankTransferMerchantId());
		data.setTransTime(dateTimeHelper.currentDateString(VietinConstants.HO_CHI_MINH_TIME_ZONE, VietinConstants.DATE_FORMAT_yyyyMMDDHHmmss));
		data.setVerifyByBank(VietinConstants.VerifyByBank.NO.getValue());

		for (int i = 0; i < data.getRecords().size(); i++) {
			data.getRecords().get(i).setTransId(numberSequenceService.nextVTBBankTransferChildTransId());
			data.getRecords().get(i).setCurrencyCode(VietinConstants.CurrencyCode.VND.getValue());
		}

		data.setVersion(configLoader.getVietinPaymentBankTransferApiVersion());

		String dataSign = String.format("%s%s%s%s", data.getRequestId(), data.getProviderId(),
				data.getMerchantId(), data.getSendRecord());
		LOGGER.info("== Before Sign Data - " + dataSign);
		String signData = viettinSign(dataSign);
		data.setSignature(signData);
		LOGGER.info("== After Sign Data - " + signData);
		return data;
	}

	public BankTransferInquiryRequest buildBankTransferInquiry(BankTransferInquiryRequest data, BankTransferTransaction queryTrans) throws Exception {
		data.setRequestId(numberSequenceService.nextVTBBankTransferTransId());
		data.setProviderId(configLoader.getVietinPaymentBankTransferProviderId());
		data.setMerchantId(configLoader.getVietinBankTransferMerchantId());
		data.setOriginalRequestId(queryTrans.getRequestId());
		data.setTransTime(dateTimeHelper.currentDateString(VietinConstants.HO_CHI_MINH_TIME_ZONE, VietinConstants.DATE_FORMAT_yyyyMMDDHHmmss));
		data.setVersion(configLoader.getVietinPaymentBankTransferApiVersion());

		String dataSign = String.format("%s%s%s%s", data.getRequestId(), data.getProviderId(),
				data.getMerchantId(), data.getOriginalRequestId());
		LOGGER.info("== Before Sign Data - " + dataSign);
		String signData = viettinSign(dataSign);
		data.setSignature(signData);
		LOGGER.info("== After Sign Data - " + signData);
		return data;
	}

	public BankTransferAccountInquiryRequest buildBankTransferAccountInquiry(BankTransferAccountInquiryRequest data) throws Exception {
		data.setRequestId(numberSequenceService.nextVTBBankTransferTransId());
		data.setProviderId(configLoader.getVietinPaymentBankTransferProviderId());
		data.setMerchantId(configLoader.getVietinBankTransferMerchantId());
		data.setTransTime(dateTimeHelper.currentDateString(VietinConstants.HO_CHI_MINH_TIME_ZONE, VietinConstants.DATE_FORMAT_yyyyMMDDHHmmss));
		data.setVersion(configLoader.getVietinPaymentBankTransferApiVersion());

		String dataSign = String.format("%s%s%s%s%s%s%s%s%s", data.getRequestId(), data.getProviderId(),
				data.getMerchantId(), data.getAccountId(), data.getBankId(), data.getTransTime(), data.getChannel(), data.getVersion(), data.getLanguage());
		LOGGER.info("== Before Sign Data - " + dataSign);
		String signData = viettinSign(dataSign);
		data.setSignature(signData);
		LOGGER.info("== After Sign Data - " + signData);
		return data;
	}

	public BankTransferProviderInquiryRequest buildBankTransferProviderInquiry(BankTransferProviderInquiryRequest data) throws Exception {
		data.setAccountId(configLoader.getVietinPaymentBankTransferOnefinAcctId().get(data.getInquiryAccountOrder()));
		data.setRequestId(numberSequenceService.nextVTBBankTransferTransId());
		data.setProviderId(configLoader.getVietinPaymentBankTransferProviderId());
		data.setMerchantId(configLoader.getVietinBankTransferMerchantId());
		data.setTransTime(dateTimeHelper.currentDateString(VietinConstants.HO_CHI_MINH_TIME_ZONE, VietinConstants.DATE_FORMAT_yyyyMMDDHHmmss));
		data.setVersion(configLoader.getVietinPaymentBankTransferApiVersion());

		String dataSign = String.format("%s%s%s%s%s%s%s%s", data.getRequestId(), data.getProviderId(),
				data.getMerchantId(), data.getAccountId(), data.getTransTime(), data.getChannel(), data.getVersion(), data.getLanguage());
		LOGGER.info("== Before Sign Data - " + dataSign);
		String signData = viettinSign(dataSign);
		data.setSignature(signData);
		LOGGER.info("== After Sign Data - " + signData);
		return data;
	}

	private String viettinSign(String input) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		PrivateKey privateKeyOneFin = encryptUtil.readPrivateKey(configLoader.getOnefinBankTransferPrivateKey());
		String signedData = encryptUtil.sign(input, privateKeyOneFin);
		return signedData;
	}

	/**
	 * @param data
	 * @return
	 */
	public boolean validateResponse(BankTransferResponse data) {
		try {
			String code = null;
			if (data.getStatus() != null) {
				code = Objects.toString(data.getStatus().getCode(), "");
			}
			if (!isValidMessage(data.getRequestId(), data.getProviderId(), data.getMerchantId(), data.getSignature())) {
				LOGGER.error("== Invalid response from Vietin!!!");
				return false;
			}

			// validate signature
			if (!verifySignature(data.getRequestId() + data.getProviderId() + data.getMerchantId() + code, data.getSignature())) {
				LOGGER.error("== Verify signature fail!!!");
				return false;
			}

			LOGGER.info("== Validation success!");
			return true;

		} catch (Exception e) {
			LOGGER.error("== Validate response from Vietin error!!!", e);
			return false;
		}
	}

	private boolean isValidMessage(String requestId, String providerId, String merchantId, String signature) {
		if (providerId == null || providerId.trim().isEmpty() || requestId == null || requestId.trim().isEmpty() || signature == null || signature.trim().isEmpty() || merchantId == null || merchantId.trim().isEmpty()) {

			return false;
		}
		if (!configLoader.getVietinPaymentBankTransferProviderId().equals(providerId)) {
			LOGGER.error("== ProviderId not support: {}", providerId);
			return false;
		}
		if (!configLoader.getVietinBankTransferMerchantId().equals(merchantId)) {
			LOGGER.error("== MerchantId not support: {}", merchantId);
			return false;
		}
		return true;
	}

	private boolean verifySignature(String data, String signature) throws CertificateException, IOException {
		PublicKey publicKeyVietin = encryptUtil.readPublicKey2(configLoader.getVtbBankTransferPublicKey());
		return encryptUtil.verifySignature(data, signature, publicKeyVietin);
	}

	public void transformErrorCode(BankTransferResponse data, String code, String lang) {
		PartnerErrorCode partnerCode = partnerErrorCodeRepo.findAllByPartnerAndDomainAndCode(OneFinConstants.PARTNER_VIETINBANK, OneFinConstants.BANK_TRANSFER, code);
		if (partnerCode == null) {
			LOGGER.warn("No error code found, please check the config file: {}", code);
			return;
		}
		data.getStatus().setCode(partnerCode.getBaseErrorCode().getCode());
		if (lang.equals(OneFinConstants.LANGUAGE.VIETNAMESE.getValue())) {
			data.getStatus().setMessage(partnerCode.getBaseErrorCode().getMessageVi());
		} else if (lang.equals(OneFinConstants.LANGUAGE.ENGLISH.getValue())) {
			data.getStatus().setMessage(partnerCode.getBaseErrorCode().getMessageEn());
		} else {
			data.getStatus().setMessage(partnerCode.getBaseErrorCode().getMessageEn());
		}
		try {
			data.getRecords().stream().forEach(e -> {
				PartnerErrorCode partnerCodeRecord = partnerErrorCodeRepo.findAllByPartnerAndDomainAndCode(OneFinConstants.PARTNER_VIETINBANK, OneFinConstants.BANK_TRANSFER, e.getStatus());
				if (partnerCodeRecord == null) {
					LOGGER.warn("No error code found, please check the config file: {}", code);
				}
				e.setStatus(partnerCodeRecord.getBaseErrorCode().getCode());
				if (lang.equals(OneFinConstants.LANGUAGE.VIETNAMESE.getValue())) {
					e.setDescription(partnerCodeRecord.getBaseErrorCode().getMessageVi());
				} else if (lang.equals(OneFinConstants.LANGUAGE.ENGLISH.getValue())) {
					e.setDescription(partnerCodeRecord.getBaseErrorCode().getMessageEn());
				} else {
					e.setDescription(partnerCodeRecord.getBaseErrorCode().getMessageEn());
				}
			});
		} catch (Exception e) {
			LOGGER.error("Exeption", e);
		}
	}

	public BankTransferTransaction createBankTransferTrans(BankTransferTransaction data) {
		Date currentDate = dateTimeHelper.currentDate(OneFinConstants.HO_CHI_MINH_TIME_ZONE);
		data.setCreatedDate(currentDate);
		data.setUpdatedDate(currentDate);
		return (BankTransferTransaction) bankTransferRepo.saveAndFlush(data);
	}

	public BankTransferTransaction updateBankTransferTrans(BankTransferTransaction data) {
		Date currentDate = dateTimeHelper.currentDate(OneFinConstants.HO_CHI_MINH_TIME_ZONE);
		data.setUpdatedDate(currentDate);
		return (BankTransferTransaction) bankTransferRepo.saveAndFlush(data);
	}

	public BankTransferChildRecords createChildBankTransferRecords(BankTransferChildRecords data) {
		Date currentDate = dateTimeHelper.currentDate(OneFinConstants.HO_CHI_MINH_TIME_ZONE);
		data.setCreatedDate(currentDate);
		data.setUpdatedDate(currentDate);
		return childBankTransferRecordsRepo.save(data);
	}

	public BankTransferChildRecords updateChildBankTransferRecords(BankTransferChildRecords data) {
		Date currentDate = dateTimeHelper.currentDate(OneFinConstants.HO_CHI_MINH_TIME_ZONE);
		data.setUpdatedDate(currentDate);
		return childBankTransferRecordsRepo.save(data);
	}

	public ResponseEntity<?> bankTransfer(String walletId, BankTransferRequest requestBody) throws Exception {
		// Validate if client request id exist
		List<BankTransferTransaction> existTrans = bankTransferRepo.findByClientRequestId(requestBody.getClientRequestId(),
				OneFinConstants.BankListQrService.CTG.getBankCode());
		if (existTrans.size() > 0) {
			throw new RuntimeBadRequestException(String.format("RequestId %s already exists", requestBody.getClientRequestId()));
		}

		// Build vietin bank transfer request
		BankTransferRequest data = buildBankTransfer(requestBody);

		// Init data in db
		BankTransferTransaction trans = new BankTransferTransaction();
		trans.setRequestId(data.getRequestId());
		trans.setApiOperation(OneFinConstants.BANK_TRANSFER);
		trans.setTranStatus(OneFinConstants.TRANS_PROCESSING);
		trans.setClientRequestId(requestBody.getClientRequestId());
		trans.setWalletId(walletId);
		trans.setProviderId(data.getProviderId());
		trans.setMerchantId(data.getMerchantId());
		trans.setProcessedRecord(data.getSendRecord());
		// Assume that only one transaction in process
		if (requestBody.getRecords().get(0).getRecvBankId().equals(
				OneFinConstants.BankListQrService.CTG.getCitad()
		)) {
			trans.setRemittanceType(VietinConstants.RemittanceType.VIETIN_INTERNAL.getValue());
		} else {
			trans.setRemittanceType(requestBody.getRemittanceType());
		}

		trans.setFeeType(requestBody.getFeeType());
		trans.setVerifyByBank(data.getVerifyByBank());
		trans.setChannel(data.getChannel());
		trans.setLanguage(data.getLanguage());
		trans.setBankTransTime(dateTimeHelper.parseDate2(data.getTransTime(), VietinConstants.DATE_FORMAT_yyyyMMDDHHmmss));
		trans.setProviderBankCode(OneFinConstants.BankListQrService.CTG.getBankCode());
		//trans = bankService.createBankTransferTrans(trans);

		for (int i = 0; i < data.getSendRecord(); i++) {
			BankTransferChildRecords childRecord = new BankTransferChildRecords();
			BankList senderBank = bankListRepository.findByCode(data.getRecords().get(i).getSenderBankId());
			if (senderBank == null) {
				throw new RuntimeBadRequestException(String.format("Sender bank not support, please check with admin: %s", data.getRecords().get(i).getSenderBankId()));
			}
			BankList recvBank = bankListRepository.findByCode(data.getRecords().get(i).getRecvBankId());
			if (recvBank == null) {
				throw new RuntimeBadRequestException(String.format("Receive bank not support, please check with admin: %s", data.getRecords().get(i).getRecvBankId()));
			}
			// Force to Vietin channel if both send and recv are Vietin account/card
			if (senderBank.getBankCode().equals(VietinConstants.VTB_BANK_CODE) && recvBank.getBankCode().equals(VietinConstants.VTB_BANK_CODE) && configLoader.isVietinBankTransferForceVietinChannel()) {
				LOGGER.info("Force to Vietin channel");
				data.setRemittanceType(VietinConstants.RemittanceType.VIETIN_INTERNAL.getValue());
				trans.setRemittanceType(data.getRemittanceType());
			}
			BankListDetails bankListDetails = null;
			// Transfer with CITAD_TTSP channel => validate recv bank branch
			if (requestBody.getRemittanceType().equals(VietinConstants.RemittanceType.CITAD_TTSP.getValue())) {
				bankListDetails = bankListDetailsRepository.findByBranchCode(data.getRecords().get(i).getRecvBranchId());
				if (bankListDetails == null) {
					throw new RuntimeBadRequestException(String.format("Receive bank branch not support, please check with admin: %s", data.getRecords().get(i).getRecvBranchId()));
				}
			}
			childRecord.setTransId(data.getRecords().get(i).getTransId());
			childRecord.setPriority(data.getRecords().get(i).getPriority());
			childRecord.setSenderBank(senderBank);
			childRecord.setSenderAcctId(data.getRecords().get(i).getSenderAcctId());
			childRecord.setSenderAcctName(data.getRecords().get(i).getSenderAcctName());
			childRecord.setRecvBank(recvBank);
			childRecord.setRecvAcctId(data.getRecords().get(i).getRecvAcctId());
			childRecord.setRecvBranchId(bankListDetails);
			childRecord.setRecvAcctName(data.getRecords().get(i).getRecvAcctName());
			childRecord.setAmount(data.getRecords().get(i).getAmount());
			childRecord.setPayRefNo(data.getRecords().get(i).getPayRefNo());
			childRecord.setPayRefInfor(data.getRecords().get(i).getPayRefInfor());
			childRecord.setRemark(data.getRecords().get(i).getRemark());
			childRecord.setBankTransferTransaction(trans);
			childRecord.setCurrency(data.getRecords().get(i).getCurrencyCode());
			//childRecord = bankService.createChildBankTransferRecords(childRecord);
			if (trans.getRecords() == null) {
				trans.setRecords(new HashSet<>());
			}
			trans.getRecords().add(childRecord);
		}

		trans = createBankTransferTrans(trans);

		// Send request to VietinBank
		BankTransferResponse response = vietinRequestUtil.sendBankTransfer(data);

		// Validate response from VTB
		boolean validateData = validateResponse(response);
		BankTransferTransaction finalTrans = trans;
		finalTrans.getRecords().forEach(e -> {
			finalTrans.setConnResult(validateData ? VietinConstants.CONN_SUCCESS : VietinConstants.CONN_PARTNER_INVALID_RESPONSE);
			finalTrans.setBankStatusCode(response.getStatus().getCode());
			// Update parent transaction
			if (response.getStatus().getCode().equals(VietinConstants.VTB_BT_SUCCESS_CODE) && validateData) {
				finalTrans.setTranStatus(OneFinConstants.TRANS_SUCCESS);
			} else if (response.getStatus().getCode().equals(VietinConstants.VTB_BT_PENDING_CODE) && validateData) {
				finalTrans.setTranStatus(OneFinConstants.TRANS_PENDING);
			} else {
				finalTrans.setTranStatus(OneFinConstants.TRANS_ERROR);
			}
			// Update child transaction
			List<BankTransferRecordResponse> childRecordResponse = response.getRecords().stream().filter(c -> c.getTransId().equals(e.getTransId())).collect(Collectors.toList());
			if (childRecordResponse.size() > 0) {
				LOGGER.info("Child record match: {}", childRecordResponse);
				e.setBankStatusCode(childRecordResponse.get(0).getStatus());
				e.setMessage(childRecordResponse.get(0).getDescription());
				e.setFeeAmount(childRecordResponse.get(0).getFeeAmount() != null ? new BigDecimal(childRecordResponse.get(0).getFeeAmount()) : null);
				e.setVatAmount(childRecordResponse.get(0).getVatAmount() != null ? new BigDecimal(childRecordResponse.get(0).getVatAmount()) : null);
				e.setBankTransactionId(childRecordResponse.get(0).getBankTransactionId());
				//e.setCurrency(childRecordResponse.get(0).getCurrencyCode());
				//bankService.updateChildBankTransferRecords(e);
			} else {
				LOGGER.error("Child record not match: {}", response);
			}
		});
		updateBankTransferTrans(finalTrans);

		transformErrorCode(response, response.getStatus().getCode(), requestBody.getLanguage());
		return new ResponseEntity<>(vietinMessageUtil.buildVietinBankTransferConnectorResponse(validateData ? VietinConstants.CONN_SUCCESS : VietinConstants.CONN_PARTNER_INVALID_RESPONSE, response, null), HttpStatus.OK);
	}

	public ResponseEntity<?> bankTransferTransInquiry(BankTransferInquiryRequest requestBody)
			throws Exception {
		List<BankTransferTransaction> listTrans = (List<BankTransferTransaction>) bankTransferRepo.findByClientRequestId(
				requestBody.getQueryRequestId(), OneFinConstants.BankListQrService.CTG.getBankCode());
		if (listTrans.size() == 0) {
			throw new RuntimeBadRequestException(String.format("Query request %s not found", requestBody.getQueryRequestId()));
		}

		BankTransferTransaction trans = listTrans.get(0);

		BankTransferInquiryRequest data = buildBankTransferInquiry(requestBody, trans);

		// Send request to VietinBank
		BankTransferResponse response = vietinRequestUtil.sendBankTransferInquiry(data);

		// Validate response from VTB
		boolean validateData = validateResponse(response);
		BankTransferResponse finalResponse = response;
		trans.getRecords().forEach(e -> {
			trans.setConnResult(validateData ? VietinConstants.CONN_SUCCESS : VietinConstants.CONN_PARTNER_INVALID_RESPONSE);
			trans.setBankStatusCode(finalResponse.getStatus().getCode());
			List<BankTransferRecordResponse> childRecordResponse = new ArrayList<>();
			try {
				childRecordResponse = finalResponse.getRecords().stream().filter(c -> c.getTransId().equals(e.getTransId())).collect(Collectors.toList());
			} catch (Exception e1) {
				LOGGER.error(e1.getMessage(), e);
			}
			// Update parent transaction
			if (finalResponse.getStatus().getCode().equals(VietinConstants.VTB_BT_SUCCESS_CODE) && validateData) {
				trans.setTranStatus(OneFinConstants.TRANS_SUCCESS);
			} else if (finalResponse.getStatus().getCode().equals(VietinConstants.VTB_BT_PENDING_CODE) && validateData) {
				trans.setTranStatus(OneFinConstants.TRANS_PENDING);
			} else {
				trans.setTranStatus(OneFinConstants.TRANS_ERROR);
			}
			if (childRecordResponse.size() > 0) {
				LOGGER.info("Child record match: {}", childRecordResponse);
				e.setBankStatusCode(childRecordResponse.get(0).getStatus());
				e.setMessage(childRecordResponse.get(0).getDescription());
				e.setFeeAmount(new BigDecimal(childRecordResponse.get(0).getFeeAmount()));
				e.setVatAmount(new BigDecimal(childRecordResponse.get(0).getVatAmount()));
				e.setBankTransactionId(childRecordResponse.get(0).getBankTransactionId());
				//e.setCurrency(childRecordResponse.get(0).getCurrencyCode());
				updateChildBankTransferRecords(e);
			} else {
				LOGGER.error("Child record not match: {}", finalResponse);
			}
		});
		updateBankTransferTrans(trans);
		transformErrorCode(response, response.getStatus().getCode(), requestBody.getLanguage());
		return new ResponseEntity<>(vietinMessageUtil.buildVietinBankTransferConnectorResponse(validateData ? VietinConstants.CONN_SUCCESS : VietinConstants.CONN_PARTNER_INVALID_RESPONSE, response, null), HttpStatus.OK);
	}

	public ResponseEntity<?> bankTransferAccountInquiry(BankTransferAccountInquiryRequest requestBody)
			throws Exception {
		if (requestBody.getRemittanceType().equals(OneFinConstants.BANK_TRANSFER_TYPE.CITAD_TTSP.getValue()) && StringHelper.checkNullEmptyBlankStatic(requestBody.getBankId())) {
			throw new RuntimeBadRequestException("Not empty bankId");
		}
		BankTransferResponse response;

		// Find citad by Napas BIN
		if (requestBody.getRemittanceType().equals(OneFinConstants.BANK_TRANSFER_TYPE.NAPAS_247.getValue()) && requestBody.getAccountId().substring(0, 4).equals("9704")) {
			BankList bankDetails = bankListRepository.findByNapasBin(requestBody.getAccountId().substring(0, 6)); // split 6 first digits
			LOGGER.info("Bank details napas: {}", bankDetails);
			if (bankDetails == null) {
				response = (BankTransferResponse) bankHelper.createModelStructure(new BankTransferResponse());
				transformErrorCode(response, "101", requestBody.getLanguage()); // 101 code: invalid account/card number
				return new ResponseEntity(vietinMessageUtil.buildVietinBankTransferConnectorResponse(VietinConstants.CONN_SUCCESS, response, null), HttpStatus.OK);
			}
			requestBody.setBankId(bankDetails.getCode());
		}

		BankTransferAccountInquiryRequest data = buildBankTransferAccountInquiry(requestBody);

		// Send request to VietinBank
		response = vietinRequestUtil.sendBankTransferAccountInquiry(data);

		// Validate response from VTB
		boolean validateData = validateResponse(response);
		return new ResponseEntity<>(vietinMessageUtil.buildVietinBankTransferConnectorResponse(validateData ? VietinConstants.CONN_SUCCESS : VietinConstants.CONN_PARTNER_INVALID_RESPONSE, response, null), HttpStatus.OK);

	}

	public ResponseEntity<?> providerInquiry(BankTransferProviderInquiryRequest requestBody) throws Exception {
		BankTransferResponse response;
		boolean validateData;

		BankTransferProviderInquiryRequest data = buildBankTransferProviderInquiry(requestBody);

		// Send request to VietinBank
		response = vietinRequestUtil.sendBankTransferProviderInquiry(data);

		// Validate response from VTB
		validateData = validateResponse(response);
		return new ResponseEntity<>(vietinMessageUtil.buildVietinBankTransferConnectorResponse(validateData ? VietinConstants.CONN_SUCCESS : VietinConstants.CONN_PARTNER_INVALID_RESPONSE, response, null), HttpStatus.OK);
	}

}
