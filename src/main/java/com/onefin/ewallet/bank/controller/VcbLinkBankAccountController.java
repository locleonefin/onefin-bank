package com.onefin.ewallet.bank.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.onefin.ewallet.bank.common.VcbConstants;
import com.onefin.ewallet.bank.common.VcbConstants.VCBEwalletApiOperation;
import com.onefin.ewallet.bank.common.VietinConstants;
import com.onefin.ewallet.bank.dto.vcb.*;
import com.onefin.ewallet.bank.repository.jpa.LinkBankTransRepo;
import com.onefin.ewallet.bank.service.common.ConfigLoader;
import com.onefin.ewallet.bank.service.common.NumberSequenceService;
import com.onefin.ewallet.bank.service.vcb.LinkBankDto;
import com.onefin.ewallet.bank.service.vcb.LinkBankMessageUtil;
import com.onefin.ewallet.bank.service.vcb.LinkBankRequestUtil;
import com.onefin.ewallet.common.base.constants.OneFinConstants;
import com.onefin.ewallet.common.base.errorhandler.RuntimeBadRequestException;
import com.onefin.ewallet.common.base.errorhandler.RuntimeInternalServerException;
import com.onefin.ewallet.common.domain.bank.common.LinkBankTransaction;
import com.onefin.ewallet.common.utility.json.JSONHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping("/bank/vcb/ewallet/account")
public class VcbLinkBankAccountController {

	private static final Logger LOGGER = LoggerFactory.getLogger(VcbLinkBankAccountController.class);

	@Autowired
	private LinkBankDto linkBankDto;

	@Autowired
	private LinkBankMessageUtil linkBankMessageUtil;

	@Autowired
	private LinkBankRequestUtil vcbLinkBankLinkBankRequestUtil;

	@Autowired
	private NumberSequenceService numberSequenceService;

	@Autowired
	private LinkBankTransRepo<?> transRepository;

	@Autowired
	private ConfigLoader configLoader;

	@Autowired
	private JSONHelper jsonHelper;

	/**
	 * Link account - Send VCB request to verify bank account details
	 *
	 * @param walletId
	 * @param requestBody
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/linkCheckActive")
	public @ResponseBody
	ResponseEntity<?> linkCheckActive(@RequestHeader(value = "walletId") String walletId, @Valid @RequestBody LinkEWallet2VcbRequest requestBody) throws Exception {
		LinkBankTransaction trx = new LinkBankTransaction();
		trx.setBank(OneFinConstants.PARTNER_VCB);

		String requestId = numberSequenceService.nextVietcomLinkBankAccountTransId();
		VCBEwalletApiOperation operation = VCBEwalletApiOperation.ACTIVE_CUSTOMER;
		// Bind data
		trx.setTranStatus(VietinConstants.TRANS_PROCESSING);
		trx.setRequestId(requestId);
		trx.setLinkType(OneFinConstants.LinkType.ACCOUNT.toString());
		trx.setPhoneNum(requestBody.getUserId());
		trx.setWalletId(walletId);
		trx.setApiOperation(operation.getCoreWalletAction());
		trx.setSsRequestId(requestBody.getRequestId());
		trx.setHolderName(requestBody.getFullname());
		trx.setCardAccountNumber(requestBody.getBankAccount());
		trx = linkBankDto.create(trx);
		requestBody.setId_pic_filename(requestBody.getId_number() + "_" + requestBody.getUserId() + ".png");
		requestBody.setTxnTime(trx.getCreatedDate());
		//Thread.sleep(61000);
		return oneFin2VcbProcess(operation.getVcbAction(), (EWallet2VcbRequest) jsonHelper.convertObject2Map(requestBody, EWallet2VcbRequest.class), trx, requestId);
	}

	/**
	 * Link account - Vcb account valid => verify OTP to complete link account process
	 *
	 * @param walletId
	 * @param requestBody
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/linkVerifyOtp")
	public @ResponseBody
	ResponseEntity<?> linkVerifyOtp(@RequestHeader(value = "walletId") String walletId, @Valid @RequestBody LinkOtpEWallet2VcbRequest requestBody) throws Exception {
		//backupService.backup(configLoader.getBackupVcbLinkBank(), null, requestBody, VcbConstants.BACKUP_REQUEST);
		LinkBankTransaction trx = transRepository.findByBankAndBankRequestTransAndPhoneNum(OneFinConstants.PARTNER_VCB, requestBody.getVcbtransID(), requestBody.getUserId());
		VCBEwalletApiOperation operation = VCBEwalletApiOperation.ACTIVE_CUSTOMER_OTP;
		if (trx == null) {
			throw new RuntimeBadRequestException();
		}
		String requestId = numberSequenceService.nextVietcomLinkBankAccountTransId();
		// Bind data
		trx.setTranStatus(VietinConstants.TRANS_PROCESSING);
		trx.setRequestId(requestId);
		trx.setLinkType(OneFinConstants.LinkType.ACCOUNT.toString());
		trx.setPhoneNum(requestBody.getUserId());
		trx.setApiOperation(operation.getCoreWalletAction());
		trx.setSsRequestId(requestBody.getRequestId());
		trx = linkBankDto.update(trx);

		requestBody.setId_pic_filename(requestBody.getId_number() + "_" + requestBody.getUserId() + ".png");
		requestBody.setTxnTime(trx.getUpdatedDate());
		return oneFin2VcbProcess(operation.getVcbAction(), (EWallet2VcbRequest) jsonHelper.convertObject2Map(requestBody, EWallet2VcbRequest.class), trx, requestId);
	}

	/**
	 * Unlink account - Unlink account with ewallet
	 *
	 * @param walletId
	 * @param requestBody
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/unlink")
	public @ResponseBody
	ResponseEntity<?> unlink(@RequestHeader(value = "walletId") String walletId, @Valid @RequestBody UnlinkEWallet2VcbRequest requestBody) throws Exception {
		//backupService.backup(configLoader.getBackupVcbLinkBank(), null, requestBody, VcbConstants.BACKUP_REQUEST);
		LinkBankTransaction trx = new LinkBankTransaction();
		trx.setBank(OneFinConstants.PARTNER_VCB);

		String requestId = numberSequenceService.nextVietcomLinkBankAccountTransId();
		VCBEwalletApiOperation operation = VCBEwalletApiOperation.DEACTIVE_CUSTOMER;
		// Bind data
		trx.setTranStatus(VietinConstants.TRANS_PROCESSING);
		trx.setRequestId(requestId);
		trx.setLinkType(OneFinConstants.LinkType.ACCOUNT.toString());
		trx.setPhoneNum(requestBody.getUserId());
		trx.setWalletId(walletId);
		trx.setSsRequestId(requestBody.getRequestId());
		trx.setApiOperation(operation.getCoreWalletAction());
		trx = linkBankDto.create(trx);
		requestBody.setTxnTime(trx.getCreatedDate());
		return oneFin2VcbProcess(operation.getVcbAction(), (EWallet2VcbRequest) jsonHelper.convertObject2Map(requestBody, EWallet2VcbRequest.class), trx, requestId);
	}

	/**
	 * Get OneFin master account details
	 *
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/masterAccount")
	public @ResponseBody
	ResponseEntity<?> masterAccountBalance() throws Exception {
		EWallet2VcbRequest requestBody = new EWallet2VcbRequest();
		LinkBankTransaction connTrans = new LinkBankTransaction();
		connTrans.setBank(OneFinConstants.PARTNER_VCB);

		String requestId = numberSequenceService.nextVietcomLinkBankAccountTransId();
		return oneFin2VcbProcess(VcbConstants.VCBEwalletApiOperation.GET_PARTNER_ACC_BALANCE.getVcbAction(), requestBody, connTrans, requestId);
	}

	/**
	 * Top-up x amount from link account to wallet account
	 *
	 * @param walletId
	 * @param requestBody
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/topup")
	public @ResponseBody
	ResponseEntity<?> cashin(@RequestHeader(value = "walletId") String walletId, @Valid @RequestBody TopupEWallet2VcbRequest requestBody) throws Exception {
		//backupService.backup(configLoader.getBackupVcbLinkBank(), null, requestBody, VcbConstants.BACKUP_REQUEST);
		LinkBankTransaction trx = new LinkBankTransaction();
		trx.setBank(OneFinConstants.PARTNER_VCB);

		String requestId = numberSequenceService.nextVietcomLinkBankAccountTransId();
		VCBEwalletApiOperation operation = VCBEwalletApiOperation.CASHIN;
		// Bind data
		trx.setTranStatus(VietinConstants.TRANS_PROCESSING);
		trx.setRequestId(requestId);
		trx.setLinkType(OneFinConstants.LinkType.ACCOUNT.toString());
		trx.setPhoneNum(requestBody.getUserId());
		trx.setWalletId(walletId);
		trx.setAmount(new BigDecimal(requestBody.getTxnAmount()));
		trx.setApiOperation(operation.getCoreWalletAction());
		trx.setCurrency(requestBody.getTxnCurrency());
		requestBody.setPartnerAcctID(configLoader.getVcbDefaultMasterAccount());
		//trx.setMasterAccountId(requestBody.getPartnerAcctID());
		trx.setSsRequestId(requestBody.getRequestId());
		trx = linkBankDto.create(trx);
		return oneFin2VcbProcess(operation.getVcbAction(), (EWallet2VcbRequest) jsonHelper.convertObject2Map(requestBody, EWallet2VcbRequest.class), trx, requestId);
	}

	/**
	 * If top-up account over limit top-up amount from VCB => need OTP verify step
	 *
	 * @param walletId
	 * @param requestBody
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/topupVerifyOtp")
	public @ResponseBody
	ResponseEntity<?> cashinOtp(@RequestHeader(value = "walletId") String walletId, @Valid @RequestBody TopupOtpEWallet2VcbRequest requestBody) throws Exception {
		//backupService.backup(configLoader.getBackupVcbLinkBank(), null, requestBody, VcbConstants.BACKUP_REQUEST);
		LinkBankTransaction trx = transRepository.findByBankAndBankRequestTransAndPhoneNum(OneFinConstants.PARTNER_VCB, requestBody.getVcbtransID(), requestBody.getUserId());
		if (trx == null) {
			throw new RuntimeBadRequestException();
		}
		String requestId = numberSequenceService.nextVietcomLinkBankAccountTransId();
		VCBEwalletApiOperation operation = VCBEwalletApiOperation.CASHIN_OTP;
		// Bind data
		trx.setTranStatus(VietinConstants.TRANS_PROCESSING);
		trx.setRequestId(requestId);
		requestBody.setPartnerAcctID(configLoader.getVcbDefaultMasterAccount());
		trx.setApiOperation(operation.getCoreWalletAction());
		trx.setSsRequestId(requestBody.getRequestId());
		trx = linkBankDto.update(trx);
		return oneFin2VcbProcess(operation.getVcbAction(), (EWallet2VcbRequest) jsonHelper.convertObject2Map(requestBody, EWallet2VcbRequest.class), trx, requestId);
	}

	/**
	 * Withdraw x amount from wallet account to vcb account
	 *
	 * @param walletId
	 * @param requestBody
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/withdraw")
	public @ResponseBody
	ResponseEntity<?> withdraw(@RequestHeader(value = "walletId") String walletId, @Valid @RequestBody WithdrawEWallet2VcbRequest requestBody) throws Exception {
		//backupService.backup(configLoader.getBackupVcbLinkBank(), null, requestBody, VcbConstants.BACKUP_REQUEST);
		LinkBankTransaction trx = new LinkBankTransaction();
		trx.setBank(OneFinConstants.PARTNER_VCB);

		String requestId = numberSequenceService.nextVietcomLinkBankAccountTransId();
		VCBEwalletApiOperation operation = VCBEwalletApiOperation.CASHOUT;
		// Bind data
		trx.setTranStatus(VietinConstants.TRANS_PROCESSING);
		trx.setRequestId(requestId);
		trx.setLinkType(OneFinConstants.LinkType.ACCOUNT.toString());
		trx.setPhoneNum(requestBody.getUserId());
		trx.setWalletId(walletId);
		trx.setAmount(new BigDecimal(requestBody.getTxnAmount()));
		trx.setApiOperation(operation.getCoreWalletAction());
		trx.setCurrency(requestBody.getTxnCurrency());
		requestBody.setPartnerAcctID(configLoader.getVcbDefaultMasterAccount());
		//trx.setMasterAccountId(requestBody.getPartnerAcctID());
		trx.setSsRequestId(requestBody.getRequestId());
		trx = linkBankDto.create(trx);
		return oneFin2VcbProcess(operation.getVcbAction(), (EWallet2VcbRequest) jsonHelper.convertObject2Map(requestBody, EWallet2VcbRequest.class), trx, requestId);
	}

	/**
	 * Check if user wallet account linked with VCB
	 *
	 * @param requestBody
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/checkActiveStatus")
	public @ResponseBody
	ResponseEntity<?> checkActiveStatus(@Valid @RequestBody CheckActiveEWallet2VcbRequest requestBody) throws Exception {
		//backupService.backup(configLoader.getBackupVcbLinkBank(), null, requestBody, VcbConstants.BACKUP_REQUEST);
		LinkBankTransaction connTrans = new LinkBankTransaction();
		connTrans.setBank(OneFinConstants.PARTNER_VCB);
		String requestId = numberSequenceService.nextVietcomLinkBankAccountTransId();
		return oneFin2VcbProcess(VcbConstants.VCBEwalletApiOperation.CHECK_ACTIVE_STATUS.getVcbAction(), (EWallet2VcbRequest) jsonHelper.convertObject2Map(requestBody, EWallet2VcbRequest.class), connTrans, requestId);
	}

	/**
	 * Check transaction status in case no response from VCB
	 *
	 * @param requestBody
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = VcbConstants.REQUEST_CHECK_TRX_STATUS)
	public @ResponseBody
	ResponseEntity<?> checkTransStatus(@Valid @RequestBody CheckTrxEWallet2VcbRequest requestBody) throws Exception {
		//backupService.backup(configLoader.getBackupVcbLinkBank(), null, requestBody, VcbConstants.BACKUP_REQUEST);
		LinkBankTransaction vcbTransPre = transRepository.findByBankAndSsRequestIdAndPhoneNum(OneFinConstants.PARTNER_VCB, requestBody.getRequestId(), requestBody.getUserId());
		if (vcbTransPre == null) {
			throw new RuntimeBadRequestException();
		}
		String requestId = vcbTransPre.getRequestId();
		requestBody.setVcbTransId(null);
		return oneFin2VcbProcess(VcbConstants.VCBEwalletApiOperation.CHECK_TRANS_STATUS.getVcbAction(), (EWallet2VcbRequest) jsonHelper.convertObject2Map(requestBody, EWallet2VcbRequest.class), vcbTransPre, requestId);
	}

	/**
	 * Common function to process request OneFin to VCB
	 *
	 * @param messageType
	 * @param requestBody
	 * @param trx
	 * @param requestId
	 * @return
	 * @throws Exception
	 */
	private ResponseEntity<?> oneFin2VcbProcess(String messageType, EWallet2VcbRequest requestBody, LinkBankTransaction trx, String requestId) throws Exception {
		LOGGER.info("RequestId {} - Start {}", requestId, messageType);
		LOGGER.info("Processing request from OneFin: requestId {} messageType {}", requestId, messageType);
		Optional<Ewallet2VcbDataDecryptResponse> ewallet2VcbDataDetailResponse;
		Optional<Vcb2WalletAccountRequest> baseMessage;
		try {
			// Step1: Send request to VCB
			Map<String, Object> requestMap = linkBankDto.buildOneFin2VcbRequest(requestId, messageType, requestBody);
			baseMessage = Optional.ofNullable(vcbLinkBankLinkBankRequestUtil.sendVcb(requestMap));
			if (!baseMessage.isPresent()) {
				throw new RuntimeInternalServerException();
			}
			//backupService.backup(configLoader.getBackupVcbLinkBank(), baseMessage.get().getRequestId(), baseMessage.get(), VcbConstants.BACKUP_RESPONSE);
			Optional<Map<String, Object>> validateResult = Optional.ofNullable(linkBankDto.validateBaseMessage(baseMessage.get()));
			if (validateResult.isPresent()) {
				throw new RuntimeInternalServerException();
			}

			// Step2: Decode and validate data field in base request
			ewallet2VcbDataDetailResponse = Optional.ofNullable(linkBankDto.decodeVcbResponse(baseMessage.get().getData()));
			ewallet2VcbDataDetailResponse.get().setConnRequestId(requestId);

			if ((messageType.equals(VCBEwalletApiOperation.CASHIN.getVcbAction()) && ewallet2VcbDataDetailResponse.get().getCode() == VcbConstants.VCB_PAY_BY_OTP_CODE && ewallet2VcbDataDetailResponse.get().getData().getTransStatus().equals(VcbConstants.VCB_PENDING_DETAIL_CODE)) || (messageType.equals(VCBEwalletApiOperation.ACTIVE_CUSTOMER.getVcbAction()) && ewallet2VcbDataDetailResponse.get().getCode() == VcbConstants.VCB_SUCCESS_CODE && ewallet2VcbDataDetailResponse.get().getData().getTransStatus().equals(VcbConstants.VCB_PENDING_DETAIL_CODE))) {
				trx.setTranStatus(VcbConstants.TRANS_PENDING);
				trx.setSendOtp(true);
			} else if (ewallet2VcbDataDetailResponse.get().getCode() == VcbConstants.VCB_TIMEOUT_CODE && ewallet2VcbDataDetailResponse.get().getData().getTransStatus().equals(VcbConstants.VCB_TIMEOUT_DETAIL_CODE)) {
				trx.setTranStatus(VcbConstants.TRANS_TIMEOUT);
			} else if (ewallet2VcbDataDetailResponse.get().getCode() == VcbConstants.VCB_SUCCESS_CODE /*&& ewallet2VcbDataDetailResponse.get().getData().getTransStatus().equals(VcbConstants.VCB_SUCCESS_DETAIL_CODE)*/) {
				trx.setTranStatus(VcbConstants.TRANS_SUCCESS);
			} else {
				trx.setTranStatus(VcbConstants.TRANS_ERROR);
			}

			trx.setBankRequestTrans(ewallet2VcbDataDetailResponse.get().getData() != null ? ewallet2VcbDataDetailResponse.get().getData().getTransID() : null);
			trx.setTransDate(ewallet2VcbDataDetailResponse.get().getData() != null ? ewallet2VcbDataDetailResponse.get().getData().getTransTime() : null);
			trx.setBankStatusCode(Integer.toString(ewallet2VcbDataDetailResponse.get().getCode()));
			//trx.setStatus(ewallet2VcbDataDetailResponse.get().getData() != null ? ewallet2VcbDataDetailResponse.get().getData().getTransStatus() : null);
			trx.setBankTransactionId(ewallet2VcbDataDetailResponse.get().getData() != null ? ewallet2VcbDataDetailResponse.get().getData().getVcbId() : null);
			if (!messageType.equals(VcbConstants.VCBEwalletApiOperation.GET_PARTNER_ACC_BALANCE.getVcbAction()) && !messageType.equals(VcbConstants.VCBEwalletApiOperation.CHECK_ACTIVE_STATUS.getVcbAction())) {
				linkBankDto.update(trx);
			}
			LOGGER.info("RequestID {} - End {}", requestId, messageType);
			ewallet2VcbDataDetailResponse.get().setMessage(linkBankMessageUtil.findMessageByErrorCode(String.valueOf(ewallet2VcbDataDetailResponse.get().getCode()), requestBody.getLang(), VcbConstants.LINK_BANK_ACCOUNT).getSecond());
			return new ResponseEntity<Object>(linkBankMessageUtil.buildEcoreConnectorResponse(VcbConstants.CONN_SUCCESS, ewallet2VcbDataDetailResponse.get()), HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.error("RequestId {} - Fail to process {}", requestId, messageType, e);
			throw e;
		}
	}

	/**
	 * Receive request from VCB, accept below actions: - check_active - active -
	 * deactive - get_info - topup
	 *
	 * @param requestBody
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/services")
	public @ResponseBody
	ResponseEntity<?> handleWalletServices(@RequestBody Vcb2WalletAccountRequest requestBody, HttpServletRequest request) throws JsonProcessingException {
		String partnerId = requestBody.getPartnerId();
		String vcbRequestId = requestBody.getRequestId();
		String messageType = requestBody.getMessageType();
		LOGGER.info("Processing request from VCB: {} {} {}", partnerId, vcbRequestId, messageType);

		Map<String, Object> validationResult = linkBankDto.validateBaseMessage(requestBody);
		if (validationResult != null) {
			return new ResponseEntity<>(validationResult, HttpStatus.OK);
		}

		AtomicReference<LinkBankTransaction> transaction = new AtomicReference<>(new LinkBankTransaction());
		transaction.get().setBank(OneFinConstants.PARTNER_VCB);
		// Decode and validate data field in base request
		VcbAccountDataRequest vcbData = linkBankDto.processDataInBaseMessage(requestBody, messageType, transaction);
		if (vcbData == null) {
			return new ResponseEntity<>(linkBankMessageUtil.buildFailResponse(requestBody, "Invalid request", null), HttpStatus.OK);
		}

		try {
			String requestId = numberSequenceService.nextVietcomLinkBankAccountTransId();
			transaction.get().setTranStatus(VietinConstants.TRANS_PROCESSING);
			transaction.get().setRequestId(requestId);
			transaction.get().setLinkType(OneFinConstants.LinkType.ACCOUNT.toString());
			transaction.get().setRequestFromBank(true);
			transaction.get().setPhoneNum(vcbData.getCustomerId());
			transaction.get().setVcbRequestId(vcbRequestId);
			transaction.get().setBankRequestTrans(vcbData.getVcbTrans());
			transaction.get().setTransDate(vcbData.getTransDatetime());

			VCBEwalletApiOperation.stream()
					.filter(e -> e.getVcbAction().equals(messageType))
					.findFirst()
					.ifPresent(e -> transaction.get().setApiOperation(e.getCoreWalletAction()));

			transaction.set(linkBankDto.create(transaction.get()));
			// Send request to core ewallet to check data
			Vcb2EWalletResponse eCoreData = vcbLinkBankLinkBankRequestUtil.sendVcb2EwalletCore(vcbData);
			if (eCoreData == null) {
				transaction.get().setTranStatus(VietinConstants.TRANS_ERROR);
				return new ResponseEntity<>(linkBankMessageUtil.buildFailResponse(requestBody, "Failed to process data", null), HttpStatus.OK);
			}

			Map<String, Object> response = linkBankMessageUtil.buildVcbResponseFromEwalletCore(requestBody, eCoreData, transaction.get());
			linkBankDto.update(transaction.get());
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.error("Failed to process data from VCB!", e);
			return new ResponseEntity<>(linkBankMessageUtil.buildFailResponse(requestBody, "Failed to process data", null), HttpStatus.OK);
		}
	}

}
