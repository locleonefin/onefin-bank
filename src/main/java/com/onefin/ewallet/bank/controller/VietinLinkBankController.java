package com.onefin.ewallet.bank.controller;

import com.onefin.ewallet.bank.common.VietinConstants;
import com.onefin.ewallet.bank.dto.vietin.*;
import com.onefin.ewallet.bank.repository.jpa.LinkBankTransRepo;
import com.onefin.ewallet.bank.service.common.ConfigLoader;
import com.onefin.ewallet.bank.service.vietin.*;
import com.onefin.ewallet.common.base.constants.OneFinConstants;
import com.onefin.ewallet.common.base.constants.OneFinConstants.LinkType;
import com.onefin.ewallet.common.base.controller.AbstractBaseController;
import com.onefin.ewallet.common.base.errorhandler.RuntimeBadRequestException;
import com.onefin.ewallet.common.base.errorhandler.RuntimeInternalServerException;
import com.onefin.ewallet.common.domain.bank.common.LinkBankTransaction;
import com.onefin.ewallet.common.utility.date.DateTimeHelper;
import com.onefin.ewallet.common.utility.json.JSONHelper;
import com.onefin.ewallet.common.utility.sercurity.SercurityHelper;
import com.onefin.ewallet.common.utility.string.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

@RestController
@RequestMapping("/vietin/ewallet")
public class VietinLinkBankController extends AbstractBaseController {

	@Autowired
	public VietinLinkBankDto vietinLinkBankService;

	@Autowired
	private VietinMessageUtil imsgUtil;

	@Autowired
	private VietinRequestUtil IHTTPRequestUtil;

	@Autowired
	private ConfigLoader configLoader;

	@Autowired
	@Qualifier("jsonHelper")
	private JSONHelper JsonHelper;

	@Autowired
	private LinkBankTransRepo<LinkBankTransaction> transRepository;

	@Autowired
	private StringHelper stringHelper;

	@Autowired
	private DateTimeHelper dateTimeHelper;

	@Autowired
	private SercurityHelper sercurityHelper;

	private static final Logger LOGGER = LoggerFactory.getLogger(VietinLinkBankController.class);

	private static final List<String> listLinkedApi = Arrays.asList(VietinConstants.TOKEN_ISSUER, VietinConstants.TOKEN_ISSUER_TOPUP, VietinConstants.TOKEN_REISSUE);

	@PostMapping("/tokenIssue/type/{type}")
	public ResponseEntity<?> getTokenIssue(@PathVariable(required = true) LinkType type,
										   @Valid @RequestBody(required = true) TokenIssue requestBody, HttpServletRequest request) throws Exception {
		LOGGER.info("RequestID {} - Start TokenIssue", requestBody.getRequestId());
		LinkBankTransaction trx = new LinkBankTransaction();
		trx.setBank(OneFinConstants.PARTNER_VIETINBANK);

		ResponseEntity<LinkBankBaseResponse> responseBaseEntity = null;
		LinkBankBaseResponse responseBase = null;
		ConnResponse responseEntity = null;
		try {
			// Bind data
			trx.setTranStatus(VietinConstants.TRANS_PROCESSING);
			trx.setApiOperation(VietinConstants.TOKEN_ISSUER);
			trx.setWalletId(requestBody.getProviderCustId());
			trx.setRequestId(requestBody.getRequestId());
			trx.setSsRequestId(requestBody.getRequestId());
			trx.setLinkType(type.toString());
			trx.setTransDate(handleDateFormat(requestBody.getTransTime()));
			trx.setCardAccountNumber(sercurityHelper.truncateCard(requestBody.getCardNumber()));

			if (type.toString().equals(VietinConstants.LinkType.CARD.toString())) {
				trx.setMerchantId(configLoader.getVietinMerchantIdCard());
			}
			if (type.toString().equals(VietinConstants.LinkType.ACCOUNT.toString())) {
				trx.setMerchantId(configLoader.getVietinMerchantIdAccount());
			}
			vietinLinkBankService.backUpRequestResponse(requestBody.getRequestId(), requestBody, null);
			trx = vietinLinkBankService.create(trx);

			TokenIssue requestMap = vietinLinkBankService.buildVietinTokenIssuer(requestBody, type.toString());
			responseBaseEntity = IHTTPRequestUtil.sendTokenIssue(requestMap);
			responseBase = responseBaseEntity.getBody();
			vietinLinkBankService.backUpRequestResponse(requestBody.getRequestId(), null, responseBase);

			// Validate response from VTB
			responseEntity = vietinLinkBankService.validateResponse(responseBase, trx, responseBaseEntity.getStatusCode(), type.toString(), requestBody.getLanguage());

			// Bind data
			trx.setConnResult(responseEntity != null ? responseEntity.getConnectorCode() : "");
			LOGGER.info("Transaction {}", trx);
			trx
					.setBankStatusCode(responseBase != null ? Objects.toString(responseBase.getStatus().getCode(), "") : "");
			if (trx.getConnResult().equals(VietinConstants.CONN_SUCCESS)
					&& trx.getBankStatusCode().equals(VietinConstants.VTB_SUCCESS_CODE)) {
				trx.setTranStatus(VietinConstants.TRANS_PENDING);
			} else {
				trx.setTranStatus(VietinConstants.TRANS_ERROR);
			}

			vietinLinkBankService.update(trx);
			return new ResponseEntity<>(responseEntity, HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.error("Fail to process TokenIssue", e);
			throw e;
		}
	}

	@PostMapping("/verifyPin/type/{type}")
	public ResponseEntity<?> verifyPin(@PathVariable(required = true) LinkType type,
									   @Valid @RequestBody(required = true) VerifyPin requestBody, HttpServletRequest request) throws Exception {
		LOGGER.info("RequestID {} - Start VerifyPin {}", requestBody.getRequestId(), requestBody);
		LinkBankTransaction trx = transRepository
				.findByRequestId(requestBody.getVerifyTransactionId());
		if (trx == null) {
			LOGGER.error("RequestID {} - Previous transaction not found", requestBody.getRequestId());
			throw new RuntimeInternalServerException();
		}
		ResponseEntity<LinkBankBaseResponse> responseBaseEntity = null;
		LinkBankBaseResponse responseBase = null;
		ConnResponse responseEntity = null;

		try {
			// Bind data
			trx.setTranStatus(VietinConstants.TRANS_PROCESSING);
			vietinLinkBankService.backUpRequestResponse(requestBody.getRequestId(), requestBody, null);
			trx = vietinLinkBankService.update(trx);
			VerifyPin requestMap = vietinLinkBankService.buildVietinVerifyPin(requestBody, type.toString());

			responseBaseEntity = IHTTPRequestUtil.sendVerifyPin(requestMap);
			responseBase = responseBaseEntity.getBody();
			vietinLinkBankService.backUpRequestResponse(requestBody.getRequestId(), null, responseBase);
			// Validate response from VTB
			responseEntity = vietinLinkBankService.validateResponse(responseBase, null, responseBaseEntity.getStatusCode(), type.toString(), requestBody.getLanguage());
			// Bind data
			trx.setConnResult(responseEntity != null ? responseEntity.getConnectorCode() : "");
			trx
					.setBankStatusCode(responseBase != null ? Objects.toString(responseBase.getStatus().getCode(), "") : "");
			if (trx.getConnResult().equals(VietinConstants.CONN_SUCCESS)
					&& trx.getBankStatusCode().equals(VietinConstants.VTB_SUCCESS_CODE)) {
				trx.setTranStatus(VietinConstants.TRANS_SUCCESS);
			}
			trx.setBankTransactionId(responseBase != null ?
					StringUtils.isEmpty(trx.getBankTransactionId()) ? responseBase.getBankTransactionId()
							: trx.getBankTransactionId() : trx.getBankTransactionId());
			trx.setTokenNumber(responseBase != null ?
					StringUtils.isEmpty(trx.getTokenNumber()) ? responseBase.getToken() : trx.getTokenNumber() : trx.getTokenNumber());
			vietinLinkBankService.update(trx);
			LOGGER.info("RequestID {} - End VerifyPin {}", requestBody.getRequestId(), responseBase);
			return new ResponseEntity<>(responseEntity, HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.error("Fail to process VerifyPin", e);
			throw e;
		}
	}

	@PostMapping("/registerOnlinePay/type/{type}")
	public ResponseEntity<?> registerOnlinePay(@PathVariable(required = true) LinkType type,
											   @Valid @RequestBody(required = true) RegisterOnlinePay requestBody, HttpServletRequest request)
			throws Exception {
		LOGGER.info("RequestID {} - Start RegisterOnlinePay", requestBody.getRequestId());
		LinkBankTransaction trx = new LinkBankTransaction();
		trx.setBank(OneFinConstants.PARTNER_VIETINBANK);

		ResponseEntity<LinkBankBaseResponse> responseBaseEntity = null;
		LinkBankBaseResponse responseBase = null;
		ConnResponse responseEntity = null;
		if (!requestBody.isAcceptRegistered()) {
			LOGGER.error("User not accept register online payment");
			throw new RuntimeBadRequestException();
		}
		try {
			// Bind data
			trx.setTranStatus(VietinConstants.TRANS_PROCESSING);
			trx.setApiOperation(VietinConstants.REGISTER_ONLINE_PAYMENT);
			trx.setWalletId(requestBody.getProviderCustId());
			trx.setRequestId(requestBody.getRequestId());
			trx.setSsRequestId(requestBody.getRequestId());
			trx.setLinkType(type.toString());
			trx.setTransDate(handleDateFormat(requestBody.getTransTime()));
			if (type.toString().equals(VietinConstants.LinkType.CARD.toString())) {
				trx.setMerchantId(configLoader.getVietinMerchantIdCard());
			}
			if (type.toString().equals(VietinConstants.LinkType.ACCOUNT.toString())) {
				trx.setMerchantId(configLoader.getVietinMerchantIdAccount());
			}
			vietinLinkBankService.backUpRequestResponse(requestBody.getRequestId(), requestBody, null);
			trx = vietinLinkBankService.create(trx);

			RegisterOnlinePay requestMap = vietinLinkBankService.buildVietinRegisterOnlinePay(requestBody, type.toString());

			responseBaseEntity = IHTTPRequestUtil.sendRegisterOnlinePay(requestMap);
			responseBase = responseBaseEntity.getBody();
			vietinLinkBankService.backUpRequestResponse(requestBody.getRequestId(), null, responseBase);
			// Validate response from VTB
			responseEntity = vietinLinkBankService.validateResponse(responseBase, trx, responseBaseEntity.getStatusCode(), type.toString(), requestBody.getLanguage());
			// Bind data
			trx.setConnResult(responseEntity != null ? responseEntity.getConnectorCode() : "");
			trx.setBankStatusCode(responseBase != null ? Objects.toString(responseBase.getStatus().getCode(), "") : "");
			if (trx.getConnResult().equals(VietinConstants.CONN_SUCCESS)
					&& trx.getBankStatusCode().equals(VietinConstants.VTB_SUCCESS_CODE)) {
				trx.setTranStatus(VietinConstants.TRANS_PENDING);
			} else {
				trx.setTranStatus(VietinConstants.TRANS_ERROR);
			}

			vietinLinkBankService.update(trx);
			LOGGER.info("RequestID {} - End RegisterOnlinePay", requestBody.getRequestId());
			return new ResponseEntity<>(responseEntity, HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.error("Fail to process RegisterOnlinePay", e);
			throw e;
		}
	}

	@PostMapping("/tokenRevoke/type/{type}")
	public ResponseEntity<?> tokenRevoke(@PathVariable(required = true) LinkType type,
										 @Valid @RequestBody(required = true) TokenRevokeReIssue requestBody, HttpServletRequest request)
			throws Exception {
		LOGGER.info("RequestID {} - Start TokenRevoke", requestBody.getRequestId());
		LinkBankTransaction trx = new LinkBankTransaction();
		trx.setBank(OneFinConstants.PARTNER_VIETINBANK);

		ResponseEntity<LinkBankBaseResponse> responseBaseEntity = null;
		LinkBankBaseResponse responseBase = null;
		ConnResponse responseEntity = null;
		try {
			// Bind data
			trx.setTranStatus(VietinConstants.TRANS_PROCESSING);
			trx.setApiOperation(VietinConstants.TOKEN_REVOKE);
			try {
				List<LinkBankTransaction> results = transRepository.findByLinkActionAndToken(OneFinConstants.PARTNER_VIETINBANK, listLinkedApi, requestBody.getToken());
				trx.setWalletId(results.get(0).getWalletId());
			} catch (Exception e) {
				LOGGER.error("Not found token");
			}
			trx.setRequestId(requestBody.getRequestId());
			trx.setSsRequestId(requestBody.getRequestId());
			trx.setLinkType(type.toString());

			trx.setTransDate(handleDateFormat(requestBody.getTransTime()));
			if (type.toString().equals(VietinConstants.LinkType.CARD.toString())) {
				trx.setMerchantId(configLoader.getVietinMerchantIdCard());
			}
			if (type.toString().equals(VietinConstants.LinkType.ACCOUNT.toString())) {
				trx.setMerchantId(configLoader.getVietinMerchantIdAccount());
			}
			trx.setTokenNumber(requestBody.getToken());
			vietinLinkBankService.backUpRequestResponse(requestBody.getRequestId(), requestBody, null);
			trx = vietinLinkBankService.create(trx);
			TokenRevokeReIssue requestMap = vietinLinkBankService.buildVietinTokenRevoke(requestBody, type.toString());
			responseBaseEntity = IHTTPRequestUtil.sendTokenRevoke(requestMap);
			responseBase = responseBaseEntity.getBody();
			vietinLinkBankService.backUpRequestResponse(requestBody.getRequestId(), null, responseBase);
			// Validate response from VTB
			responseEntity = vietinLinkBankService.validateResponse(responseBase, trx, responseBaseEntity.getStatusCode(), type.toString(), requestBody.getLanguage());
			// Bind data
			trx.setConnResult(responseEntity != null ? responseEntity.getConnectorCode() : "");
			trx
					.setBankStatusCode(responseBase != null ? Objects.toString(responseBase.getStatus().getCode(), "") : "");
			if (trx.getConnResult().equals(VietinConstants.CONN_SUCCESS)
					&& trx.getBankStatusCode().equals(VietinConstants.VTB_SUCCESS_CODE)) {
				trx.setTranStatus(VietinConstants.TRANS_SUCCESS);
			} else {
				trx.setTranStatus(VietinConstants.TRANS_ERROR);
			}

			vietinLinkBankService.update(trx);
			LOGGER.info("RequestID {} - End TokenRevoke", requestBody.getRequestId());
			return new ResponseEntity<>(responseEntity, HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.error("Fail to process TokenRevoke", e);
			throw e;
		}
	}

	@PostMapping("/tokenReIssue/type/{type}")
	public ResponseEntity<?> tokenReIssue(@PathVariable(required = true) LinkType type,
										  @Valid @RequestBody(required = true) TokenRevokeReIssue requestBody, HttpServletRequest request)
			throws Exception {
		LOGGER.info("RequestID {} - Start TokenReIssue", requestBody.getRequestId());
		LinkBankTransaction trx = new LinkBankTransaction();
		trx.setBank(OneFinConstants.PARTNER_VIETINBANK);

		ResponseEntity<LinkBankBaseResponse> responseBaseEntity = null;
		LinkBankBaseResponse responseBase = null;
		ConnResponse responseEntity = null;

		try {
			// Bind data
			trx.setTranStatus(VietinConstants.TRANS_PROCESSING);
			trx.setApiOperation(VietinConstants.TOKEN_REISSUE);
			try {
				List<LinkBankTransaction> results = transRepository.findByLinkActionAndToken(OneFinConstants.PARTNER_VIETINBANK, listLinkedApi, requestBody.getToken());
				trx.setWalletId(results.get(0).getWalletId());
			} catch (Exception e) {
				LOGGER.error("Not found token");
			}
			trx.setRequestId(requestBody.getRequestId());
			trx.setSsRequestId(requestBody.getRequestId());
			trx.setLinkType(type.toString());
			trx.setTransDate(handleDateFormat(requestBody.getTransTime()));
			if (type.toString().equals(VietinConstants.LinkType.CARD.toString())) {
				trx.setMerchantId(configLoader.getVietinMerchantIdCard());
			}
			if (type.toString().equals(VietinConstants.LinkType.ACCOUNT.toString())) {
				trx.setMerchantId(configLoader.getVietinMerchantIdAccount());
			}

			vietinLinkBankService.backUpRequestResponse(requestBody.getRequestId(), requestBody, null);
			trx = vietinLinkBankService.create(trx);

			TokenRevokeReIssue requestMap = vietinLinkBankService.buildVietinTokenReIssue(requestBody, type.toString());

			responseBaseEntity = IHTTPRequestUtil.sendTokenReIssue(requestMap);
			responseBase = responseBaseEntity.getBody();
			vietinLinkBankService.backUpRequestResponse(requestBody.getRequestId(), null, responseBase);
			// Validate response from VTB
			responseEntity = vietinLinkBankService.validateResponse(responseBase, trx, responseBaseEntity.getStatusCode(), type.toString(), requestBody.getLanguage());
			// Bind data
			trx.setTokenNumber(responseBase != null ? responseBase.getToken() : null);
			trx.setConnResult(responseEntity != null ? responseEntity.getConnectorCode() : "");
			trx
					.setBankStatusCode(responseBase != null ? Objects.toString(responseBase.getStatus().getCode(), "") : "");
			if (trx.getConnResult().equals(VietinConstants.CONN_SUCCESS)
					&& trx.getBankStatusCode().equals(VietinConstants.VTB_SUCCESS_CODE)) {
				trx.setTranStatus(VietinConstants.TRANS_SUCCESS);
			} else {
				trx.setTranStatus(VietinConstants.TRANS_ERROR);
			}

			vietinLinkBankService.update(trx);
			LOGGER.info("RequestID {} - End TokenReIssue", requestBody.getRequestId());
			return new ResponseEntity<>(responseEntity, HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.error("Fail to process TokenReissue", e);
			throw e;
		}
	}

	@PostMapping("/paymentByToken/type/{type}")
	public ResponseEntity<?> payment(@PathVariable(required = true) LinkType type,
									 @Valid @RequestBody(required = true) Map<String, Object> requestBody, HttpServletRequest request)
			throws Exception {
		ResponseEntity<?> response = null;
		if (configLoader.isAlwaysTopupOTP()) {
			PaymentByOTP data = (PaymentByOTP) JsonHelper.convertObject2Map(requestBody, PaymentByOTP.class);
			response = paymentByOTP(type, data, request);
		} else {
			PaymentByToken data = (PaymentByToken) JsonHelper.convertObject2Map(requestBody, PaymentByToken.class);
			response = paymentByToken(type, data, request);
		}
		return response;
	}

	public ResponseEntity<?> paymentByToken(LinkType type, PaymentByToken requestBody, HttpServletRequest request)
			throws Exception {
		LOGGER.info("RequestID {} - Start PaymentByToken", requestBody.getRequestId());
		LinkBankTransaction trx = new LinkBankTransaction();
		trx.setBank(OneFinConstants.PARTNER_VIETINBANK);
		ResponseEntity<LinkBankBaseResponse> responseBaseEntity = null;
		LinkBankBaseResponse responseBase = null;
		ConnResponse responseEntity = null;

		try {
			// Bind data
			trx.setTranStatus(VietinConstants.TRANS_PROCESSING);
			trx.setApiOperation(VietinConstants.TOPUP_TOKEN);
			try {
				List<LinkBankTransaction> results = transRepository.findByLinkActionAndToken(OneFinConstants.PARTNER_VIETINBANK, listLinkedApi, requestBody.getToken());
				trx.setWalletId(results.get(0).getWalletId());
			} catch (Exception e) {
				LOGGER.error("Not found token");
			}
			trx.setRequestId(requestBody.getRequestId());
			trx.setSsRequestId(requestBody.getRequestId());
			trx.setLinkType(type.toString());
			trx.setCurrency(requestBody.getCurrencyCode());
			trx.setAmount(new BigDecimal(requestBody.getAmount()));
			trx.setTransDate(handleDateFormat(requestBody.getTransTime()));

			if (type.toString().equals(VietinConstants.LinkType.CARD.toString())) {
				trx.setMerchantId(configLoader.getVietinMerchantIdCard());
			}
			if (type.toString().equals(VietinConstants.LinkType.ACCOUNT.toString())) {
				trx.setMerchantId(configLoader.getVietinMerchantIdAccount());
			}
			trx.setTokenNumber(requestBody.getToken());
			vietinLinkBankService.backUpRequestResponse(requestBody.getRequestId(), requestBody, null);
			trx = vietinLinkBankService.create(trx);

			PaymentByToken requestMap = vietinLinkBankService.buildVietinPaymentByToken(requestBody, type.toString());

			responseBaseEntity = IHTTPRequestUtil.sendPaymentByToken(requestMap);
			responseBase = responseBaseEntity.getBody();
			vietinLinkBankService.backUpRequestResponse(requestBody.getRequestId(), null, responseBase);
			// Validate response from VTB
			responseEntity = vietinLinkBankService.validateResponse(responseBase, trx, responseBaseEntity.getStatusCode(), type.toString(), requestBody.getLanguage());
			// Bind data
			trx.setBankTransactionId(responseBase != null ? responseBase.getBankTransactionId() : trx.getBankTransactionId());
			trx.setConnResult(responseEntity != null ? responseEntity.getConnectorCode() : "");
			trx
					.setBankStatusCode(responseBase != null ? Objects.toString(responseBase.getStatus().getCode(), "") : "");
			if (trx.getConnResult().equals(VietinConstants.CONN_SUCCESS)
					&& trx.getBankStatusCode().equals(VietinConstants.VTB_SUCCESS_CODE)) {
				trx.setTranStatus(VietinConstants.TRANS_SUCCESS);
			} else if (trx.getConnResult().equals(VietinConstants.CONN_SUCCESS)
					&& trx.getBankStatusCode().equals(VietinConstants.VTB_PAY_BY_OTP_CODE)) {
				trx.setTranStatus(VietinConstants.TRANS_PENDING);
			} else {
				trx.setTranStatus(VietinConstants.TRANS_ERROR);
			}

			vietinLinkBankService.update(trx);
			LOGGER.info("RequestID {} - End PaymentByToken", requestBody.getRequestId());
			return new ResponseEntity<>(responseEntity, HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.error("Fail to process PaymentByToken", e);
			throw e;
		}
	}

	public ResponseEntity<?> paymentByOTP(LinkType type, PaymentByOTP requestBody, HttpServletRequest request)
			throws Exception {
		LOGGER.info("RequestID {} - Start PaymentByOTP", requestBody.getRequestId());
		LinkBankTransaction trx = new LinkBankTransaction();
		trx.setBank(OneFinConstants.PARTNER_VIETINBANK);

		ResponseEntity<LinkBankBaseResponse> responseBaseEntity = null;
		LinkBankBaseResponse responseBase = null;
		ConnResponse responseEntity = null;

		try {
			// Bind data
			trx.setTranStatus(VietinConstants.TRANS_PROCESSING);
			trx.setApiOperation(VietinConstants.TOPUP_TOKEN_OTP);

			try {
				List<LinkBankTransaction> results = transRepository.findByLinkActionAndToken(OneFinConstants.PARTNER_VIETINBANK, listLinkedApi, requestBody.getToken());
				trx.setWalletId(results.get(0).getWalletId());
			} catch (Exception e) {
				LOGGER.error("Not found token");
			}
			trx.setRequestId(requestBody.getRequestId());
			trx.setSsRequestId(requestBody.getRequestId());
			trx.setLinkType(type.toString());
			trx.setCurrency(requestBody.getCurrencyCode());
			trx.setAmount(new BigDecimal(requestBody.getAmount()));
			trx.setTransDate(handleDateFormat(requestBody.getTransTime()));

			if (type.toString().equals(VietinConstants.LinkType.CARD.toString())) {
				trx.setMerchantId(configLoader.getVietinMerchantIdCard());
			}
			if (type.toString().equals(VietinConstants.LinkType.ACCOUNT.toString())) {
				trx.setMerchantId(configLoader.getVietinMerchantIdAccount());
			}
			trx.setTokenNumber(requestBody.getToken());
			vietinLinkBankService.backUpRequestResponse(requestBody.getRequestId(), requestBody, null);
			trx = vietinLinkBankService.create(trx);

			PaymentByOTP requestMap = vietinLinkBankService.buildVietinPaymentByOTP(requestBody, type.toString());

			responseBaseEntity = IHTTPRequestUtil.sendPaymentByOTP(requestMap);
			responseBase = responseBaseEntity.getBody();
			vietinLinkBankService.backUpRequestResponse(requestBody.getRequestId(), null, responseBase);
			// Validate response from VTB
			responseEntity = vietinLinkBankService.validateResponse(responseBase, trx, responseBaseEntity.getStatusCode(), type.toString(), requestBody.getLanguage());
			// Change response code for SS navigate to OTP screen: 20
			if (configLoader.isAlwaysTopupOTP()
					&& responseBase.getStatus().getCode().equals(VietinConstants.VTB_SUCCESS_CODE)
					&& responseEntity.getConnectorCode().equals(VietinConstants.CONN_SUCCESS)) {
				responseBase.getStatus().setCode(VietinConstants.VTB_PAY_BY_OTP_CODE);
				responseEntity = imsgUtil.buildVietinConnectorResponse(VietinConstants.CONN_SUCCESS,
						responseBase, type.toString());
			}
			// Bind data
			trx.setConnResult(responseEntity != null ? responseEntity.getConnectorCode() : "");
			// Change response for OTP screen
			trx.setBankStatusCode(
					responseBase != null ? Objects.toString(responseBase.getStatus().getCode(), null) : null);
			if (trx.getConnResult().equals(VietinConstants.CONN_SUCCESS)
					&& trx.getBankStatusCode().equals(VietinConstants.VTB_PAY_BY_OTP_CODE)) {
				trx.setTranStatus(VietinConstants.TRANS_PENDING);
			} else {
				trx.setTranStatus(VietinConstants.TRANS_ERROR);
			}

			vietinLinkBankService.update(trx);
			LOGGER.info("RequestID {} - End PaymentByOTP", requestBody.getRequestId());
			return new ResponseEntity<>(responseEntity, HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.error("Fail to process PaymentByOTP", e);
			throw e;
		}
	}

	@PostMapping("/withdraw/type/{type}")
	public ResponseEntity<?> withdraw(@PathVariable(required = true) LinkType type,
									  @Valid @RequestBody(required = true) Withdraw requestBody, HttpServletRequest request) throws Exception {
		LOGGER.info("RequestID {} - Start Withdraw", requestBody.getRequestId());
		LinkBankTransaction trx = new LinkBankTransaction();
		trx.setBank(OneFinConstants.PARTNER_VIETINBANK);
		ResponseEntity<LinkBankBaseResponse> responseBaseEntity = null;
		LinkBankBaseResponse responseBase = null;
		ConnResponse responseEntity = null;

		try {
			// Bind data
			trx.setTranStatus(VietinConstants.TRANS_PROCESSING);
			trx.setApiOperation(VietinConstants.WITHDRAW);

			try {
				List<LinkBankTransaction> results = transRepository.findByLinkActionAndToken(OneFinConstants.PARTNER_VIETINBANK, listLinkedApi, requestBody.getToken());
				trx.setWalletId(results.get(0).getWalletId());
			} catch (Exception e) {
				LOGGER.error("Not found token");
			}
			trx.setRequestId(requestBody.getRequestId());
			trx.setSsRequestId(requestBody.getRequestId());
			trx.setLinkType(type.toString());
			trx.setCurrency(requestBody.getCurrencyCode());
			trx.setAmount(new BigDecimal(requestBody.getAmount()));
			trx.setTransDate(handleDateFormat(requestBody.getTransTime()));

			if (type.toString().equals(VietinConstants.LinkType.CARD.toString())) {
				trx.setMerchantId(configLoader.getVietinMerchantIdCard());
			}
			if (type.toString().equals(VietinConstants.LinkType.ACCOUNT.toString())) {
				trx.setMerchantId(configLoader.getVietinMerchantIdAccount());
			}
			trx.setTokenNumber(requestBody.getToken());
			vietinLinkBankService.backUpRequestResponse(requestBody.getRequestId(), requestBody, null);
			trx = vietinLinkBankService.create(trx);

			Withdraw requestMap = vietinLinkBankService.buildVietinWithdraw(requestBody, type.toString());

			responseBaseEntity = IHTTPRequestUtil.sendWithdraw(requestMap);
			responseBase = responseBaseEntity.getBody();
			vietinLinkBankService.backUpRequestResponse(requestBody.getRequestId(), requestBody, responseBase);
			// Validate response from VTB
			responseEntity = vietinLinkBankService.validateResponse(responseBase, trx, responseBaseEntity.getStatusCode(), type.toString(), requestBody.getLanguage());
			// Bind data
			trx.setConnResult(responseEntity != null ? responseEntity.getConnectorCode() : "");
			trx
					.setBankStatusCode(responseBase != null ? Objects.toString(responseBase.getStatus().getCode(), "") : "");
			if (trx.getConnResult().equals(VietinConstants.CONN_SUCCESS)
					&& trx.getBankStatusCode().equals(VietinConstants.VTB_SUCCESS_CODE)) {
				trx.setTranStatus(VietinConstants.TRANS_SUCCESS);
			} else {
				trx.setTranStatus(VietinConstants.TRANS_ERROR);
			}

			trx.setBankTransactionId(responseBase != null ? responseBase.getBankTransactionId() : trx.getBankTransactionId());

			vietinLinkBankService.update(trx);
			LOGGER.info("RequestID {} - End Withdraw", requestBody.getRequestId());
			return new ResponseEntity<>(responseEntity, HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.error("Fail to process Withdraw", e);
			throw e;
		}
	}

	@PostMapping("/transactionInquiry/type/{type}")
	public ResponseEntity<?> transactionInquiry(@PathVariable(required = true) LinkType type,
												@Valid @RequestBody(required = true) TransactionInquiry requestBody, HttpServletRequest request)
			throws Exception {
		LOGGER.info("RequestID {} - Start TransactionInquiry {}", requestBody.getRequestId(), requestBody.getQueryTransactionId());
		LinkBankTransaction vietinTrans = transRepository
				.findByRequestId(requestBody.getQueryTransactionId());
		if (vietinTrans == null) {
			LOGGER.error("RequestID {} - Previous transaction not found {}", requestBody.getRequestId(), requestBody.getQueryTransactionId());
			throw new RuntimeInternalServerException();
		}
		ResponseEntity<LinkBankBaseResponse> responseBaseEntity = null;
		LinkBankBaseResponse responseBase = null;

		try {
			vietinLinkBankService.backUpRequestResponse(requestBody.getRequestId(), requestBody, null);
			TransactionInquiry requestMap = vietinLinkBankService.buildVietinTransactionInquiry(requestBody, type.toString());

			responseBaseEntity = IHTTPRequestUtil.sendTransactionInquiry(requestMap);
			responseBase = responseBaseEntity.getBody();
			vietinLinkBankService.backUpRequestResponse(requestBody.getRequestId(), null, responseBase);
			// Validate response from VTB
			ConnResponse responseEntity = vietinLinkBankService.validateResponse(responseBase, vietinTrans, responseBaseEntity.getStatusCode(), type.toString(), requestBody.getLanguage());
			// Bind data
			//vietinTrans.setConnResult(responseEntity != null ? responseEntity.getConnectorCode() : "");
			if (responseBase != null && Objects.toString(responseBase.getStatus().getCode(), "").equals(VietinConstants.VTB_SUCCESS_CODE)) {
				vietinTrans.setBankStatusCode(VietinConstants.VTB_SUCCESS_CODE);
			}
			if (vietinTrans.getConnResult().equals(VietinConstants.CONN_SUCCESS)
					&& vietinTrans.getBankStatusCode().equals(VietinConstants.VTB_SUCCESS_CODE)) {
				vietinTrans.setTranStatus(VietinConstants.TRANS_SUCCESS);
			}
//			else {
//				vietinTrans.setTranStatus(VietinConstants.TRANS_ERROR);
//			}
			//vietinTrans.setRequestId(requestBody.getRequestId());
			//vietinTrans.setLinkType(type.toString());
			//vietinTrans.setCurrency(requestBody.getCurrencyCode());
			//vietinTrans.setAmount(new BigDecimal(requestBody.getAmount()));
			//vietinTrans.setTransDate(requestBody.getTransTime());
//			if (type.toString().equals(VietinConstants.LinkType.CARD.toString())) {
//				vietinTrans.setMerchantId(configLoader.getVietinMerchantIdCard());
//			}
//			if (type.toString().equals(VietinConstants.LinkType.ACCOUNT.toString())) {
//				vietinTrans.setMerchantId(configLoader.getVietinMerchantIdAccount());
//			}
			vietinTrans.setBankTransactionId(responseBase != null ? !stringHelper.checkNullEmptyBlank(responseBase.getBankTransactionId()) ? responseBase.getBankTransactionId() : vietinTrans.getBankTransactionId() : vietinTrans.getBankTransactionId());
			vietinTrans.setTokenNumber(responseBase != null ? !stringHelper.checkNullEmptyBlank(responseBase.getToken()) ? responseBase.getToken() : vietinTrans.getTokenNumber() : vietinTrans.getTokenNumber());
			vietinLinkBankService.update(vietinTrans);
			LOGGER.info("RequestID {} - End TransactionInquiry {}", requestBody.getRequestId(), requestBody.getQueryTransactionId());
			return new ResponseEntity<>(responseEntity, HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.error("Fail to process TransactionInquiry", e);
			throw e;
		}
	}

	@PostMapping("/providerInquiry")
	public ResponseEntity<?> providerInquiry(
			@Valid @RequestBody(required = true) ProviderInquiry requestBody, HttpServletRequest request)
			throws Exception {
		LOGGER.info("RequestID {} - Start ProviderInquiry", requestBody.getRequestId());
		Map<String, Object> response = null;
		try {
			response = providerInquiry(requestBody);
			LOGGER.info("RequestID {} - End ProviderInquiry", requestBody.getRequestId());
			return new ResponseEntity<>(imsgUtil.buildVietinConnectorResponse(VietinConstants.CONN_SUCCESS, response), HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.warn("Fail to process ProviderInquiry", e);
			return new ResponseEntity<>(imsgUtil.buildVietinConnectorResponse(VietinConstants.CONN_PARTNER_INVALID_RESPONSE, response), HttpStatus.OK);
		}
	}

	public Map<String, Object> providerInquiry(ProviderInquiry requestBody) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		ProviderInquiry requestMap = vietinLinkBankService.buildVietinProviderInquiry(requestBody);
		return IHTTPRequestUtil.sendProviderInquiry(requestMap);
	}

	@PostMapping("/tokenIssuer-payment/type/{type}")
	public ResponseEntity<?> getTokenIssuerPayment(@PathVariable(required = true) LinkType type,
												   @Valid @RequestBody(required = true) TokenIssuePayment requestBody, HttpServletRequest request)
			throws Exception {
		LOGGER.info("RequestID {} - Start TokenIssuePayment", requestBody.getRequestId());
		LinkBankTransaction trx = new LinkBankTransaction();
		trx.setBank(OneFinConstants.PARTNER_VIETINBANK);

		ResponseEntity<LinkBankBaseResponse> responseBaseEntity = null;
		LinkBankBaseResponse responseBase = null;
		ConnResponse responseEntity = null;
		try {
			// Bind data
			trx.setTranStatus(VietinConstants.TRANS_PROCESSING);
			trx.setApiOperation(VietinConstants.TOKEN_ISSUER_TOPUP);
			trx.setWalletId(requestBody.getProviderCustId());
			trx.setRequestId(requestBody.getRequestId());
			trx.setSsRequestId(requestBody.getRequestId());
			trx.setLinkType(type.toString());
			trx.setCurrency(requestBody.getCurrencyCode());
			trx.setAmount(new BigDecimal(requestBody.getAmount()));
			trx.setTransDate(handleDateFormat(requestBody.getTransTime()));
			trx.setCardAccountNumber(sercurityHelper.truncateCard(requestBody.getCardNumber()));

			if (type.toString().equals(VietinConstants.LinkType.CARD.toString())) {
				trx.setMerchantId(configLoader.getVietinMerchantIdCard());
			}
			if (type.toString().equals(VietinConstants.LinkType.ACCOUNT.toString())) {
				trx.setMerchantId(configLoader.getVietinMerchantIdAccount());
			}
			vietinLinkBankService.backUpRequestResponse(requestBody.getRequestId(), requestBody, null);
			trx = vietinLinkBankService.create(trx);
			TokenIssuePayment requestMap = vietinLinkBankService.buildVietinTokenIssuerPayment(requestBody, type.toString());

			responseBaseEntity = IHTTPRequestUtil.sendTokenIssuePayment(requestMap);
			responseBase = responseBaseEntity.getBody();
			vietinLinkBankService.backUpRequestResponse(requestBody.getRequestId(), null, responseBase);
			// Validate response from VTB
			responseEntity = vietinLinkBankService.validateResponse(responseBase, trx, responseBaseEntity.getStatusCode(), type.toString(), requestBody.getLanguage());
			// Bind data
			trx.setConnResult(responseEntity != null ? responseEntity.getConnectorCode() : "");
			trx
					.setBankStatusCode(responseBase != null ? Objects.toString(responseBase.getStatus().getCode(), "") : "");
			if (trx.getConnResult().equals(VietinConstants.CONN_SUCCESS)
					&& trx.getBankStatusCode().equals(VietinConstants.VTB_SUCCESS_CODE)) {
				trx.setTranStatus(VietinConstants.TRANS_PENDING);
			} else {
				trx.setTranStatus(VietinConstants.TRANS_ERROR);
			}

			vietinLinkBankService.update(trx);
			LOGGER.info("RequestID {} - End TokenIssuePayment", requestBody.getRequestId());
			return new ResponseEntity<>(responseEntity, HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.error("Fail to process TokenIssuePayment", e);
			throw e;
		}
	}

	@PostMapping("/refund/type/{type}")
	public ResponseEntity<?> refund(@PathVariable(required = true) LinkType type,
									@Valid @RequestBody(required = true) Refund requestBody, HttpServletRequest request) throws Exception {
		LOGGER.info("RequestID {} - Start Refund - refund Id {}", requestBody.getRequestId(), requestBody.getRefundTransactionId());
		LinkBankTransaction vietinTransRefund = transRepository
				.findByBankAndRequestIdAndTranStatus(OneFinConstants.PARTNER_VIETINBANK, requestBody.getRefundTransactionId(), VietinConstants.TRANS_SUCCESS);
		if (vietinTransRefund == null) {
			LOGGER.error("RequestID {} - Previous transaction not found", requestBody.getRequestId());
			throw new RuntimeInternalServerException();
		}
		LinkBankTransaction trx = new LinkBankTransaction();
		trx.setBank(OneFinConstants.PARTNER_VIETINBANK);
		ResponseEntity<LinkBankBaseResponse> responseBaseEntity = null;
		LinkBankBaseResponse responseBase = null;
		ConnResponse responseEntity = null;

		try {
			// Bind data
			trx.setTranStatus(VietinConstants.TRANS_PROCESSING);
			trx.setApiOperation(VietinConstants.REFUND);
			try {
				List<LinkBankTransaction> results = transRepository.findByLinkActionAndToken(OneFinConstants.PARTNER_VIETINBANK, listLinkedApi, vietinTransRefund.getTokenNumber());
				trx.setWalletId(results.get(0).getWalletId());
			} catch (Exception e) {
				LOGGER.error("Not found token");
			}
			trx.setRequestId(requestBody.getRequestId());
			trx.setSsRequestId(requestBody.getRequestId());
			trx.setLinkType(type.toString());
			trx.setTransDate(handleDateFormat(requestBody.getTransTime()));

			if (type.toString().equals(VietinConstants.LinkType.CARD.toString())) {
				trx.setMerchantId(configLoader.getVietinMerchantIdCard());
			}
			if (type.toString().equals(VietinConstants.LinkType.ACCOUNT.toString())) {
				trx.setMerchantId(configLoader.getVietinMerchantIdAccount());
			}
			trx.setAmount(new BigDecimal(requestBody.getAmount()));
			trx.setCurrency(requestBody.getCurrencyCode());
			trx.setRefundId(requestBody.getRefundTransactionId());
			vietinLinkBankService.backUpRequestResponse(requestBody.getRequestId(), requestBody, null);
			trx = vietinLinkBankService.create(trx);

			Refund requestMap = vietinLinkBankService.buildVietinRefund(requestBody, type.toString());
			responseBaseEntity = IHTTPRequestUtil.sendRefund(requestMap);
			responseBase = responseBaseEntity.getBody();
			vietinLinkBankService.backUpRequestResponse(requestBody.getRequestId(), null, responseBase);
			// Validate response from VTB

			responseEntity = vietinLinkBankService.validateResponse(responseBase, trx, responseBaseEntity.getStatusCode(), type.toString(), requestBody.getLanguage());
			// Bind data
			trx.setConnResult(responseEntity != null ? responseEntity.getConnectorCode() : "");
			trx
					.setBankStatusCode(responseBase != null ? Objects.toString(responseBase.getStatus().getCode(), "") : "");
			if (trx.getConnResult().equals(VietinConstants.CONN_SUCCESS)
					&& trx.getBankStatusCode().equals(VietinConstants.VTB_SUCCESS_CODE)) {
				trx.setTranStatus(VietinConstants.TRANS_SUCCESS);
			} else {
				trx.setTranStatus(VietinConstants.TRANS_ERROR);
			}

			trx.setBankTransactionId(responseBase != null ? responseBase.getBankTransactionId() : null);

			vietinLinkBankService.update(trx);
			LOGGER.info("RequestID {} - End Refund", requestBody.getRequestId());
			return new ResponseEntity<>(responseEntity, HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.error("Fail to process Refund", e);
			throw e;
		}
	}

	private Date handleDateFormat(String dateString) {
		try {
			Date date = dateTimeHelper.parseDate2(dateString, OneFinConstants.DATE_FORMAT_yyyyMMDDHHmmss);
			return date;
		} catch (Exception ex) {
			LOGGER.error("Can't parse vietin date: {}, {}", dateString, ex.getMessage());
			return null;
		}

	}
}
