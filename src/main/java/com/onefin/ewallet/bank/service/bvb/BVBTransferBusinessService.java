package com.onefin.ewallet.bank.service.bvb;

import com.onefin.ewallet.common.base.constants.BankConstants;
import com.onefin.ewallet.bank.common.VietinConstants;
import com.onefin.ewallet.bank.dto.bvb.bankTransfer.*;
import com.onefin.ewallet.bank.dto.vietin.*;
import com.onefin.ewallet.bank.repository.jpa.BankListRepository;
import com.onefin.ewallet.bank.repository.jpa.BankTransferRepo;
import com.onefin.ewallet.bank.repository.jpa.ChildBankTransferRecordsRepo;
import com.onefin.ewallet.bank.service.common.ConfigLoader;
import com.onefin.ewallet.bank.service.common.NumberSequenceService;
import com.onefin.ewallet.bank.service.vietin.VietinBankTransferDto;
import com.onefin.ewallet.bank.service.vietin.VietinRequestUtil;
import com.onefin.ewallet.common.base.constants.OneFinConstants;
import com.onefin.ewallet.common.base.constants.OneFinEnum;
import com.onefin.ewallet.common.base.errorhandler.RuntimeBadRequestException;
import com.onefin.ewallet.common.base.errorhandler.RuntimeInternalServerException;
import com.onefin.ewallet.common.domain.bank.common.BankList;
import com.onefin.ewallet.common.domain.bank.transfer.BankTransferChildRecords;
import com.onefin.ewallet.common.domain.bank.transfer.BankTransferTransaction;
import com.onefin.ewallet.common.utility.date.DateTimeHelper;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class BVBTransferBusinessService {

	private static final org.apache.logging.log4j.Logger LOGGER
			= LogManager.getLogger(BVBTransferBusinessService.class);

	@Autowired
	private DateTimeHelper dateTimeHelper;

	@Autowired
	private BVBEncryptUtil bvbEncryptUtil;

	@Autowired
	private BVBTransferRequestUtil bvbTransferRequestUtil;

	@Autowired
	private Environment env;

	@Autowired
	private ConfigLoader configLoader;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private BankTransferRepo bankTransferRepo;

	@Autowired
	private BankListRepository bankListRepository;

	@Value("${bvb.IBFT.channel}")
	private String bvbTransferChannel;

	@Value("${bvb.IBFT.merchantId}")
	private String bvbTransferMerchantId;

	@Value("${bvb.IBFT.onefinClientCode}")
	private String onefinClientCode;

	@Autowired
	private VietinBankTransferDto vietinBankTransferDto;

	@Autowired
	private VietinRequestUtil vietinRequestUtil;

	@Autowired
	private NumberSequenceService numberSequenceService;

	@Autowired
	private ChildBankTransferRecordsRepo childBankTransferRecordsRepo;


	public ResponseEntity<?> bankTransfer(String walletId, BankTransferRequest requestBody) throws Exception {
		// Validate if client request id exist
		if (!requestBody.getRemittanceType().equals(VietinConstants.RemittanceType.NAPAS.getValue())) {
			throw new RuntimeInternalServerException(String.format(
					"Service only support for Napas 24/7 - index number: %s",
					VietinConstants.RemittanceType.NAPAS.getValue()));
		}

		if (BankConstants.BVBFeeModel.stream().noneMatch(e -> requestBody.getFeeType().equals(e.getValue()))) {
			throw new RuntimeInternalServerException("Fee model is not supported");
		}
		List<BankTransferTransaction> existTrans = bankTransferRepo.findByClientRequestId(requestBody.getClientRequestId(),
				OneFinConstants.BankListQrService.VCCB.getBankCode());
		if (existTrans.size() > 0) {
			throw new RuntimeBadRequestException(String.format("RequestId %s already exists", requestBody.getClientRequestId()));
		}

		if (requestBody.getSendRecord() > 1) {
			throw new RuntimeBadRequestException(String.format("Service currently not support for multi transactions"));
		}
		if (requestBody.getRecords().isEmpty()) {
			throw new RuntimeBadRequestException(String.format("No records found"));
		}

		// Build bvb bank transfer request
		String requestId = numberSequenceService.nextBVBBankTransferTransId();
		requestBody.setRequestId(requestId);
		requestBody.setProviderId(onefinClientCode);
//		requestBody.setMerchantId(bvbTransferMerchantId);
		Date currentDate = dateTimeHelper.currentDate(OneFinConstants.HO_CHI_MINH_TIME_ZONE);
		requestBody.setTransTime(dateTimeHelper.parseDate2String(
				currentDate, VietinConstants.DATE_FORMAT_yyyyMMDDHHmmss));
		requestBody.setVerifyByBank(VietinConstants.VerifyByBank.NO.getValue());
		requestBody.getRecords().forEach(
				e -> {
					e.setCurrencyCode(OneFinEnum.CurrencyCode.VND.getValue());
					e.setTransId(bvbTransferRequestUtil.getRequestId());
					int accountOrder = e.getSenderAccountOrder();
					e.setSenderBankId(configLoader.getBvbBankTransferOnefinBankId().get(accountOrder));
					e.setSenderAcctId(configLoader.getBvbBankTransferOnefinAcctId().get(accountOrder));
				}
		);
		// Init data in db
		BankTransferTransaction trans = new BankTransferTransaction();
		trans.setRequestId(requestBody.getRequestId());
		trans.setApiOperation(OneFinConstants.BANK_TRANSFER);
		trans.setTranStatus(OneFinConstants.TRANS_PROCESSING);
		trans.setClientRequestId(requestBody.getClientRequestId());
		trans.setWalletId(walletId);
		trans.setProviderId(requestBody.getProviderId());
		trans.setMerchantId(requestBody.getMerchantId());
		trans.setProcessedRecord(requestBody.getSendRecord());
		trans.setRemittanceType(requestBody.getRemittanceType());
		trans.setFeeType(requestBody.getFeeType());
		trans.setVerifyByBank(requestBody.getVerifyByBank());
		trans.setChannel(bvbTransferChannel);
		trans.setLanguage(requestBody.getLanguage());
		trans.setBankTransTime(dateTimeHelper.parseDate2(requestBody.getTransTime(), VietinConstants.DATE_FORMAT_yyyyMMDDHHmmss));
		trans.setProviderBankCode(OneFinConstants.BankListQrService.VCCB.getBankCode());

		for (int i = 0; i < requestBody.getSendRecord(); i++) {
			// Persist child Record
			BankTransferChildRecords childRecord = new BankTransferChildRecords();
			BankList senderBank = bankListRepository.findByCode(requestBody.getRecords().get(i).getSenderBankId());
			if (senderBank == null) {
				throw new RuntimeBadRequestException(String.format("Sender bank not support, please check with admin: %s", requestBody.getRecords().get(i).getSenderBankId()));
			}
			BankList recvBank = bankListRepository.findByCode(requestBody.getRecords().get(i).getRecvBankId());
			if (recvBank == null) {
				throw new RuntimeBadRequestException(String.format("Receive bank not support, please check with admin: %s", requestBody.getRecords().get(i).getRecvBankId()));
			}

			if (recvBank.getVccbBankId() == null) {
				throw new RuntimeBadRequestException(String.format("Receive napas id not support, please check with admin: %s",
						requestBody.getRecords().get(i).getRecvBankId()));
			}

			// BVB info
			childRecord.setTransId(requestBody.getRecords().get(i).getTransId());
			childRecord.setAmount(requestBody.getRecords().get(i).getAmount());
			childRecord.setCurrency(requestBody.getRecords().get(i).getCurrencyCode());

			// other info from Vietin rule
			childRecord.setPriority(requestBody.getRecords().get(i).getPriority());
			childRecord.setSenderBank(senderBank);
			childRecord.setSenderAcctId(requestBody.getRecords().get(i).getSenderAcctId());
			childRecord.setSenderAcctName(requestBody.getRecords().get(i).getSenderAcctName());
			childRecord.setRecvBank(recvBank);
			if (!requestBody.getRecords().get(i).getIsCard()) {
				childRecord.setRecvAcctId(requestBody.getRecords().get(i).getRecvAcctId());
			} else {
				childRecord.setRecvAcctCard(requestBody.getRecords().get(i).getRecvAcctId());
			}
			childRecord.setPayRefNo(requestBody.getClientRequestId());
			childRecord.setRemark(requestBody.getRecords().get(i).getRemark());
			childRecord.setBankTransferTransaction(trans);
			trans.addChildRecord(childRecord);
		}

		trans.setCreatedDate(currentDate);
		trans.setUpdatedDate(currentDate);
		BankTransferTransaction finalTrans = (BankTransferTransaction) bankTransferRepo.saveAndFlush(trans);


		Status status = new Status();
		// request BVB transfer api
		BankTransferChildRecords childRecords
				= new ArrayList<>(finalTrans.getRecords()).get(0);

		List<BankTransferRecordResponse> bankTransferRecordResponses = new ArrayList<>();

		// Send request to BVBank
		BVBIBFTFundTransferRequest request;
		ResponseEntity<BVBIBFTFundTransferResponse> response;
		BVBIBFTFundTransferResponse responseBody;


		try {
			request = bvbTransferRequestUtil.buildIBFTFundTransferRequest(childRecords, finalTrans);

			// Request api
			response = bvbTransferRequestUtil.requestBVB(request,
					configLoader.getBvbIBFTFundTransferUrl(),
					request.getRequestId(),
					BankConstants.BVB_IBFT_BACKUP_FUND_TRANSFER_PREFIX,
					BVBIBFTFundTransferResponse.class
			);

			// construct BVB response body
			responseBody = response.getBody();
			LOGGER.log(Level.getLevel("INFOWT"), "IBFT Transfer responseBody: {}", responseBody);

			// validate dto
			ConnResponse isDtoValid = bvbTransferRequestUtil.checkDTOAndReturnIfNotValid(responseBody);
			if (isDtoValid != null) {
				finalTrans.setConnResult(isDtoValid.getConnectorCode());
				vietinBankTransferDto.updateBankTransferTrans(finalTrans);
				return new ResponseEntity<>(isDtoValid, HttpStatus.OK);
			}

			// validate signature
			ConnResponse isSignatureValid = bvbTransferRequestUtil.checkSignature(responseBody);
			if (isSignatureValid != null) {
				finalTrans.setConnResult(isSignatureValid.getConnectorCode());
				vietinBankTransferDto.updateBankTransferTrans(finalTrans);
				return new ResponseEntity<>(isSignatureValid, HttpStatus.OK);
			}

			// Update parent transaction
			if (BankConstants.BVBIBFTErrorCode.findByErrorCode(responseBody.getErrorCode())
					.getTransStatus().equals(BankConstants.BVBIBFTTransStatus.SUCCESS.getName())) {
				finalTrans.setTranStatus(OneFinConstants.TRANS_SUCCESS);
			} else if (BankConstants.BVBIBFTErrorCode.findByErrorCode(responseBody.getErrorCode())
					.getTransStatus().equals(BankConstants.BVBIBFTTransStatus.PENDING.getName())) {
				finalTrans.setTranStatus(OneFinConstants.TRANS_PENDING);
			} else if (BankConstants.BVBIBFTErrorCode.findByErrorCode(responseBody.getErrorCode())
					.getTransStatus().equals(BankConstants.BVBIBFTTransStatus.FAILED.getName())) {
				finalTrans.setTranStatus(BankConstants.BVBIBFTTransStatus.FAILED.getName());
			} else {
				finalTrans.setTranStatus(OneFinConstants.TRANS_ERROR);
			}


			Date updateDate = dateTimeHelper.currentDate(OneFinConstants.HO_CHI_MINH_TIME_ZONE);
			// Update child transaction
			childRecords.setBankStatusCode(responseBody.getStatus());
			childRecords.setVccbErrorCode(responseBody.getErrorCode());
			childRecords.setMessage(responseBody.getErrorMessage());//
			childRecords.setVccbOnus(request.getData().getOnus());
			childRecords.setBankTransactionId(responseBody.getResponseId());
			childRecords.setUpdatedDate(updateDate);
			status.setCode(responseBody.getErrorCode());
			status.setMessage(responseBody.getErrorMessage());
			BankTransferRecordResponse bankTransferRecordResponse
					= modelMapper.map(childRecords, BankTransferRecordResponse.class);
			bankTransferRecordResponse.setCurrencyCode(childRecords.getCurrency());
			bankTransferRecordResponse.setStatus(childRecords.getBankStatusCode());
			bankTransferRecordResponse.setDescription(childRecords.getMessage());
			bankTransferRecordResponse.setRemark(childRecords.getRemark());
			bankTransferRecordResponses.add(bankTransferRecordResponse);

			try {
				childRecords.setSettleDate(responseBody.getDataParsed().getSettleDate());
			} catch (Exception ex) {
				LOGGER.error("Error reading settle date", ex);
			}
		} catch (Exception ex) {
			LOGGER.error("Error occurred: ", ex);
			throw new RuntimeInternalServerException("Error while processing response from bank provider");
		}

		finalTrans.setConnResult(VietinConstants.CONN_SUCCESS);
		finalTrans = vietinBankTransferDto.updateBankTransferTrans(finalTrans);

		// Construct response
		BankTransferResponse finalResponse = new BankTransferResponse();
		finalResponse.setRequestId(finalTrans.getRequestId());
		finalResponse.setProviderId(finalTrans.getProviderId());
		finalResponse.setMerchantId(finalTrans.getMerchantId());
		finalResponse.setStatus(status);
		finalResponse.setProcessedRecord(String.valueOf(finalTrans.getRecords().size()));
		finalResponse.setRecords(bankTransferRecordResponses);

		bvbTransferRequestUtil.transformErrorCode(finalResponse,
				OneFinConstants.PARTNER_BVBANK,
				OneFinConstants.BVB_CODE_TRANSFER,
				finalResponse.getStatus().getCode(),
				requestBody.getLanguage());
		ConnResponse e = new ConnResponse();
		e.setResponse(finalResponse);
		e.setConnectorCode(finalResponse.getStatus().getCode());
		return new ResponseEntity<>(e, HttpStatus.OK);
	}

	public ResponseEntity<?> bankTransferTransInquiry(BankTransferInquiryRequest requestBody)
			throws Exception {

		List<BankTransferTransaction> listTrans = (List<BankTransferTransaction>) bankTransferRepo.findByClientRequestId(
				requestBody.getQueryRequestId(), OneFinConstants.BankListQrService.VCCB.getBankCode());

		if (listTrans.size() == 0) {
			throw new RuntimeBadRequestException(String.format("Query request %s not found", requestBody.getQueryRequestId()));
		}

		BankTransferTransaction trans = listTrans.get(0);

//		String requestId = numberSequenceService.nextBVBBankTransferTransId();
		requestBody.setRequestId(bvbTransferRequestUtil.getRequestId());
		requestBody.setProviderId(onefinClientCode);
//		requestBody.setMerchantId(bvbTransferMerchantId);

		Date currentDate = dateTimeHelper.currentDate(OneFinConstants.HO_CHI_MINH_TIME_ZONE);
		requestBody.setOriginalRequestId(trans.getRequestId());
		requestBody.setTransTime(dateTimeHelper.parseDate2String(currentDate,
				VietinConstants.DATE_FORMAT_yyyyMMDDHHmmss));

		BVBIBFTQueryStatusRequest request
				= bvbTransferRequestUtil.buildIBFTTransferInquiryRequest(requestBody, trans);

		ResponseEntity<BVBIBFTQueryStatusResponse> response;
		BVBIBFTQueryStatusResponse responseBody;

		// Request api
		response = bvbTransferRequestUtil.requestBVB(request,
				configLoader.getBvbIBFTQueryStatusUrl(),
				request.getRequestId(),
				BankConstants.BVB_IBFT_BACKUP_QUERY_STATUS_PREFIX,
				BVBIBFTQueryStatusResponse.class
		);

		// construct BVB response body
		responseBody = response.getBody();
		LOGGER.log(Level.getLevel("INFOWT"), "IBFT Transfer inquiry responseBody: {}", responseBody);
		// validate dto
		ConnResponse isDtoValid = bvbTransferRequestUtil.checkDTOAndReturnIfNotValid(responseBody);
		if (isDtoValid != null) {
			return new ResponseEntity<>(isDtoValid, HttpStatus.OK);
		}

		// validate signature
		ConnResponse isSignatureValid = bvbTransferRequestUtil.checkSignature(responseBody);
		if (isSignatureValid != null) {
			return new ResponseEntity<>(isSignatureValid, HttpStatus.OK);
		}

		// request BVB transfer api
		BankTransferChildRecords childRecords
				= new ArrayList<>(trans.getRecords()).get(0);


		trans.setConnResult(VietinConstants.CONN_SUCCESS);

		if (responseBody.getStatus().equals(BankConstants.BVBIBFTErrorCode.TRANSACTION_SUCCESS.getErrorCode())) {
			// Update parent transaction
			if (BankConstants.BVBIBFTErrorCode.findByErrorCode(responseBody.getErrorCode())
					.getTransStatus().equals(BankConstants.BVBIBFTTransStatus.SUCCESS.getName())) {
				trans.setTranStatus(OneFinConstants.TRANS_SUCCESS);
			} else if (BankConstants.BVBIBFTErrorCode.findByErrorCode(responseBody.getErrorCode())
					.getTransStatus().equals(BankConstants.BVBIBFTTransStatus.PENDING.getName())) {
				trans.setTranStatus(OneFinConstants.TRANS_PENDING);
			} else if (BankConstants.BVBIBFTErrorCode.findByErrorCode(responseBody.getErrorCode())
					.getTransStatus().equals(BankConstants.BVBIBFTTransStatus.FAILED.getName())) {
				trans.setTranStatus(BankConstants.BVBIBFTTransStatus.FAILED.getName());
			} else {
				trans.setTranStatus(OneFinConstants.TRANS_ERROR);
			}

			// Update child transaction
			childRecords.setBankStatusCode(responseBody.getStatus());
			childRecords.setVccbErrorCode(responseBody.getErrorCode());
			childRecords.setMessage(responseBody.getErrorMessage());
			childRecords.setBankTransactionId(responseBody.getResponseId());
			childRecords.setUpdatedDate(currentDate);
			childBankTransferRecordsRepo.save(childRecords);
			vietinBankTransferDto.updateBankTransferTrans(trans);
		}


		List<BankTransferRecordResponse> bankTransferRecordResponses = new ArrayList<>();
		BankTransferRecordResponse bankTransferRecordResponse
				= modelMapper.map(childRecords, BankTransferRecordResponse.class);
		bankTransferRecordResponse.setCurrencyCode(childRecords.getCurrency());
		bankTransferRecordResponse.setStatus(childRecords.getBankStatusCode());
		bankTransferRecordResponse.setDescription(childRecords.getMessage());
		bankTransferRecordResponses.add(bankTransferRecordResponse);

		// Construct response
		BankTransferResponse finalResponse = new BankTransferResponse();
		Status status = new Status();

		finalResponse.setRequestId(requestBody.getRequestId());
		finalResponse.setProviderId(requestBody.getProviderId());
		finalResponse.setMerchantId(requestBody.getMerchantId());
		finalResponse.setStatus(status);
		finalResponse.setProcessedRecord(String.valueOf(trans.getRecords().size()));
		finalResponse.addRecordList(bankTransferRecordResponses);

		bvbTransferRequestUtil.transformErrorCode(finalResponse,
				OneFinConstants.PARTNER_BVBANK,
				OneFinConstants.BVB_CODE_TRANSFER,
				childRecords.getVccbErrorCode(),
				requestBody.getLanguage());
		ConnResponse result = new ConnResponse();
		result.setResponse(finalResponse);
		result.setConnectorCode(finalResponse.getStatus().getCode());
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	public ResponseEntity<?> bankTransferAccountInquiry(BankTransferAccountInquiryRequest requestBody)
			throws Exception {

		if (!requestBody.getRemittanceType().equals(OneFinConstants.BANK_TRANSFER_TYPE.NAPAS_247.getValue())) {
			throw new RuntimeInternalServerException("Service only support for Napas 24/7");
		}

		Optional<BankList> bank = Optional.ofNullable(bankListRepository.findBankListByCode(requestBody.getBankId()));
		if (!bank.isPresent()) {
			throw new RuntimeInternalServerException(String.format("Bank with citad %s not found", requestBody.getBankId()));
		}

		if (bank.get().getVccbBankId() == null) {
			throw new RuntimeInternalServerException(String.format("Bank with citad %s not support by bvbank", requestBody.getBankId()));
		}

		BVBIBFTInquiryRequest bvbibftInquiryRequest
				= new BVBIBFTInquiryRequest();
		BVBIBFTInquiryRequestData bvbibftInquiryRequestData
				= new BVBIBFTInquiryRequestData();


//		String requestId = numberSequenceService.nextBVBBankTransferTransId();
		requestBody.setRequestId(
				Optional.ofNullable(requestBody.getRequestId())
						.orElseGet(() -> bvbTransferRequestUtil.getRequestId())
		);

		requestBody.setProviderId(onefinClientCode);
//		requestBody.setMerchantId(bvbTransferMerchantId);
		Date currentDate = dateTimeHelper.currentDate(OneFinConstants.HO_CHI_MINH_TIME_ZONE);
		requestBody.setTransTime(dateTimeHelper.parseDate2String(
				currentDate, VietinConstants.DATE_FORMAT_yyyyMMDDHHmmss));

		bvbibftInquiryRequest.setRequestId(requestBody.getRequestId());
		bvbibftInquiryRequest.setClientCode(requestBody.getProviderId());
		Optional.ofNullable(requestBody.getMerchantId())
				.ifPresent(bvbibftInquiryRequest::setClientUserId);
		bvbibftInquiryRequest.setTime(dateTimeHelper.parseDate2(requestBody.getTransTime(),
				OneFinConstants.DATE_FORMAT_yyyyMMDDHHmmss));
		bvbibftInquiryRequest.setData(bvbibftInquiryRequestData);

		if (requestBody.getIsCard()) {
			bvbibftInquiryRequest.getData().setCardNo(requestBody.getAccountId());
		} else {
			bvbibftInquiryRequest.getData().setAccountNo(requestBody.getAccountId());
		}

		if (requestBody.getBankId().equals(configLoader.getBvbBankTransferOnefinBankId().get(0))) {
			bvbibftInquiryRequest.getData().setOnus(BankConstants.BVBonus.INTERNAL.getValue());
		} else {
			bvbibftInquiryRequest.getData().setOnus(BankConstants.BVBonus.EXTERNAL.getValue());
		}
		bvbibftInquiryRequest.getData().setBankCode(bank.get().getVccbBankId());


		// Request api
		ResponseEntity<BVBIBFTInquiryResponse> responseEntity
				= bvbTransferRequestUtil.requestBVB(bvbibftInquiryRequest,
				configLoader.getBvbIBFTInquiryUrl(),
				bvbibftInquiryRequest.getRequestId(),
				BankConstants.BVB_IBFT_BACKUP_INQUIRY_PREFIX,
				BVBIBFTInquiryResponse.class
		);

		BVBIBFTInquiryResponse responseBody;

		// construct BVB response body
		responseBody = responseEntity.getBody();
		LOGGER.log(Level.getLevel("INFOWT"), "IBFT Account inquiry responseBody: {}", responseBody);
		// validate dto
		ConnResponse isDtoValid = bvbTransferRequestUtil.checkDTOAndReturnIfNotValid(responseBody);
		if (isDtoValid != null) {
			return new ResponseEntity<>(isDtoValid, HttpStatus.OK);
		}

		// validate signature
		ConnResponse isSignatureValid = bvbTransferRequestUtil.checkSignature(responseBody);
		if (isSignatureValid != null) {
			return new ResponseEntity<>(isSignatureValid, HttpStatus.OK);
		}

		BankTransferResponse response;

		// Construct response
		BankTransferResponse finalResponse = new BankTransferResponse();
		Status status = new Status();

		finalResponse.setRequestId(requestBody.getRequestId());
		finalResponse.setProviderId(requestBody.getProviderId());
		finalResponse.setMerchantId(requestBody.getMerchantId());
		finalResponse.setBankId(bank.get().getCode());
		finalResponse.setBankCode(bank.get().getBankCode());
		finalResponse.setBankName(bank.get().getName());
		finalResponse.setStatus(status);
		if (responseBody.getErrorCode().equals(BankConstants.BVBIBFTErrorCode.TRANSACTION_SUCCESS.getErrorCode())) {
			finalResponse.setAccountName(responseBody.getDataParsed().getFullName());
		}


		bvbTransferRequestUtil.transformErrorCode(finalResponse,
				OneFinConstants.PARTNER_BVBANK,
				OneFinConstants.BVB_CODE_TRANSFER,
				responseBody.getErrorCode(),
				requestBody.getLanguage());
		ConnResponse result = new ConnResponse();
		result.setResponse(finalResponse);
		result.setConnectorCode(finalResponse.getStatus().getCode());

		return new ResponseEntity<>(result, HttpStatus.OK);

	}
}
