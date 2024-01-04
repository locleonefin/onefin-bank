package com.onefin.ewallet.bank.controller;

import com.onefin.ewallet.bank.common.VcbConstants;
import com.onefin.ewallet.bank.common.VietinConstants;
import com.onefin.ewallet.bank.dto.vcb.*;
import com.onefin.ewallet.bank.repository.jpa.LinkBankTransRepo;
import com.onefin.ewallet.bank.service.common.NumberSequenceService;
import com.onefin.ewallet.bank.service.vcb.LinkBankDto;
import com.onefin.ewallet.bank.service.vcb.LinkBankMessageUtil;
import com.onefin.ewallet.bank.service.vcb.LinkBankRequestUtil;
import com.onefin.ewallet.common.base.constants.OneFinConstants;
import com.onefin.ewallet.common.base.errorhandler.RuntimeBadRequestException;
import com.onefin.ewallet.common.base.errorhandler.RuntimeInternalServerException;
import com.onefin.ewallet.common.domain.bank.common.LinkBankTransaction;

import com.onefin.ewallet.common.utility.sercurity.SercurityHelper;
import com.onefin.ewallet.common.utility.string.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/bank/vcb/ewallet/card")
public class VcbLinkBankCardController {

	private static final Logger LOGGER = LoggerFactory.getLogger(VcbLinkBankCardController.class);

	@Autowired
	private StringHelper stringHelper;

	@Autowired
	private LinkBankMessageUtil linkBankMessageUtil;

	@Autowired
	private LinkBankDto linkBankDto;

	@Autowired
	private LinkBankTransRepo transRepository;

	@Autowired
	private LinkBankRequestUtil linkBankRequestUtil;

	@Autowired
	private NumberSequenceService numberSequenceService;

	@Autowired
	private SercurityHelper sercurityHelper;

	/******************* Link card *************************/

	/**
	 * Link card - Send VCB request to create user and verify card details
	 *
	 * @param requestBody
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/tokenIssue")
	public @ResponseBody
	ResponseEntity<?> tokenIssue(@Valid @RequestBody CardTokenIssue requestBody, HttpServletRequest request) throws Exception {
		LinkBankTransaction trans = new LinkBankTransaction();
		trans.setBank(OneFinConstants.PARTNER_VCB);

		trans.setRequestId(numberSequenceService.nextVietcomLinkBankCardTransId());
		trans.setSsRequestId(requestBody.getRequestId());
		trans.setTranStatus(VietinConstants.TRANS_PROCESSING);
		trans.setApiOperation(VcbConstants.TOKEN_ISSUER);
		trans.setWalletId(requestBody.getWalletId());
		trans.setPhoneNum(requestBody.getPhoneNo());
		trans.setCardAccountNumber(sercurityHelper.truncateCard(requestBody.getCardNumber()));
		trans.setCardIssueDate(requestBody.getCardIssueYear() + requestBody.getCardIssueMonth());
		trans.setHolderName(requestBody.getCardHolderName());
		trans.setLinkType(VcbConstants.LinkType.CARD.toString());
		trans = linkBankDto.create(trans);
		LOGGER.info("Transaction data: {}", trans);
		// 1. Check if user exist
		UserRegistrationCardResponse userSearchCardResponse = linkBankRequestUtil.searchUser(requestBody, trans);
		if (userSearchCardResponse.getHttpCode().is2xxSuccessful() && userSearchCardResponse.getState().equals(VcbConstants.APPROVED_STATUS_CARD)) {
			// User exist
			// 2. Create instrument
			CreateInstrumentCardResponse createInstrument = linkBankRequestUtil.createInstrument(requestBody, userSearchCardResponse, trans);
			linkBankDto.update(trans);
			return new ResponseEntity<Object>(linkBankMessageUtil.buildEcoreConnectorResponse(VcbConstants.CONN_SUCCESS, createInstrument), HttpStatus.OK);
		} else {
			// User not exist
			// 2. Create user
			UserRegistrationCardResponse userRegistrationCardResponse = linkBankRequestUtil.createUser(requestBody, trans);
			if (userRegistrationCardResponse.getHttpCode().is2xxSuccessful() && userRegistrationCardResponse.getState().equals(VcbConstants.APPROVED_STATUS_CARD)) {
				// 3. Create instrument
				CreateInstrumentCardResponse createInstrument = linkBankRequestUtil.createInstrument(requestBody, userRegistrationCardResponse, trans);
				linkBankDto.update(trans);
				return new ResponseEntity<Object>(linkBankMessageUtil.buildEcoreConnectorResponse(VcbConstants.CONN_SUCCESS, createInstrument), HttpStatus.OK);
			}
			linkBankDto.update(trans);
			return new ResponseEntity<Object>(linkBankMessageUtil.buildEcoreConnectorResponse(VcbConstants.CONN_INTERNAL_SERVER_ERROR, null), HttpStatus.OK);
		}
	}

	/**
	 * Link card/Payment - Card/payment valid => continue to verify opt to complete link card/payment process
	 *
	 * @param requestBody
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/verifyPin")
	public @ResponseBody
	ResponseEntity<?> verifyPin(@Valid @RequestBody CardInstrumentPaymentAuthorizationRequest requestBody, HttpServletRequest request) {
		Optional<LinkBankTransaction> trans = Optional.ofNullable(transRepository.findByBankAndSsRequestIdAndTranStatus(OneFinConstants.PARTNER_VCB, requestBody.getVerifyTransactionId(), VcbConstants.TRANS_PENDING));
		return trans.map(e -> {
			e.setOtpCode(requestBody.getAuthorization_input().getOtp());
			CardInstrumentAuthorizationResponse instrumentAuthorizationResponse = null;
			CardPaymentAuthorizationResponse paymentAuthorizationResponse = null;
			Object response = null;
			try {
				if (e.getApiOperation().equals(VcbConstants.TOKEN_ISSUER)) {
					instrumentAuthorizationResponse = linkBankRequestUtil.instrumentAuthorization(e.getCardAuthorizationId(), requestBody, e);
					response = instrumentAuthorizationResponse;
				} else if (e.getApiOperation().equals(VcbConstants.TOPUP_TOKEN)) {
					paymentAuthorizationResponse = linkBankRequestUtil.paymentAuthorization(e.getCardAuthorizationId(), requestBody, e);
					response = paymentAuthorizationResponse;
				} else {
					throw new RuntimeInternalServerException(String.format("Vcb not found previous requestId %s", requestBody.getVerifyTransactionId()));
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			linkBankDto.update(e);
			return new ResponseEntity<>(linkBankMessageUtil.buildEcoreConnectorResponse(VcbConstants.CONN_SUCCESS, response), HttpStatus.OK);
		}).orElseThrow(() -> new RuntimeBadRequestException("Verify transaction with pending status not exists"));

	}

	/**
	 * Unlink card - Unlink card
	 *
	 * @param requestBody
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/tokenRevoke")
	public @ResponseBody
	ResponseEntity<?> tokenRevoke(@Valid @RequestBody TokenRevokeRequest requestBody, HttpServletRequest request) throws Exception {
		Optional<LinkBankTransaction> trans = Optional.ofNullable(transRepository.findByBankAndTranStatusAndTokenStateAndTokenId(OneFinConstants.PARTNER_VCB, VcbConstants.TRANS_SUCCESS, OneFinConstants.TokenState.ACTIVE.getValue(), requestBody.getTokenId()));
		return trans.map(e -> {
			ErrorResponse instrumentDeleteResponse = null;
			try {
				instrumentDeleteResponse = linkBankRequestUtil.instrumentDelete(e.getCardInsId(), requestBody.getLang(), e);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			linkBankDto.update(e);
			return new ResponseEntity<>(linkBankMessageUtil.buildEcoreConnectorResponse(VcbConstants.CONN_SUCCESS, instrumentDeleteResponse), HttpStatus.OK);
		}).orElseThrow(RuntimeInternalServerException::new);

	}

	/**
	 * Topup
	 *
	 * @param requestBody
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/paymentByToken")
	public @ResponseBody
	ResponseEntity<?> paymentByToken(@Valid @RequestBody CreatePaymentCardWalletRequest requestBody, HttpServletRequest request) throws Exception {
		LinkBankTransaction trans = new LinkBankTransaction();
		trans.setBank(OneFinConstants.PARTNER_VCB);

		trans.setWalletId(request.getHeader("walletId"));
		trans.setPhoneNum(request.getHeader("phone"));
		trans.setRequestId(numberSequenceService.nextVietcomLinkBankCardTransId());
		trans.setSsRequestId(requestBody.getRequestId());
		trans.setTokenId(requestBody.getTokenId());
		trans.setTranStatus(VietinConstants.TRANS_PROCESSING);
		trans.setApiOperation(VcbConstants.TOPUP_TOKEN);
		trans.setLinkType(VcbConstants.LinkType.CARD.toString());
		trans.setAmount(requestBody.getAmount());
		trans.setMerchantRefId(trans.getRequestId());
		trans = linkBankDto.create(trans);
		LOGGER.info("Transaction data: {}", trans);
		CreatePaymentCardResponse instrumentDeleteResponse = linkBankRequestUtil.createPayment(requestBody, trans);
		// Case payment fail
		// ss query again
		linkBankDto.update(trans);
		return new ResponseEntity<>(linkBankMessageUtil.buildEcoreConnectorResponse(VcbConstants.CONN_SUCCESS, instrumentDeleteResponse), HttpStatus.OK);
	}

	/**
	 * Withdraw
	 *
	 * @param requestBody
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/withdraw")
	public @ResponseBody
	ResponseEntity<?> withdraw(@Valid @RequestBody CreateWithdrawCardWalletRequest requestBody, HttpServletRequest request) throws Exception {
		LinkBankTransaction trans = new LinkBankTransaction();
		trans.setBank(OneFinConstants.PARTNER_VCB);

		trans.setWalletId(request.getHeader("walletId"));
		trans.setPhoneNum(request.getHeader("phone"));
		trans.setRequestId(numberSequenceService.nextVietcomLinkBankCardTransId());
		trans.setSsRequestId(requestBody.getRequestId());
		trans.setTokenId(requestBody.getTokenId());
		trans.setTranStatus(VietinConstants.TRANS_PROCESSING);
		trans.setApiOperation(VcbConstants.WITHDRAW);
		trans.setLinkType(VcbConstants.LinkType.CARD.toString());
		trans.setAmount(requestBody.getAmount());
		trans.setMerchantRefId(trans.getRequestId());
		trans = linkBankDto.create(trans);
		LOGGER.info("Transaction data: {}", trans);
		CreateWithdrawCardResponse withdrawResponse = linkBankRequestUtil.withdraw(requestBody, trans);
		linkBankDto.update(trans);
		return new ResponseEntity<>(linkBankMessageUtil.buildEcoreConnectorResponse(VcbConstants.CONN_SUCCESS, withdrawResponse), HttpStatus.OK);
	}

	/**
	 * Check trans status
	 *
	 * @param requestBody
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/checkTransStatus")
	public @ResponseBody
	ResponseEntity<?> checkTransStatus(@Valid @RequestBody CheckTransStatusWalletRequest requestBody, HttpServletRequest request) throws Exception {
		Optional<LinkBankTransaction> trans = null;
		if (requestBody.getQueryType().getValue().equals(VcbConstants.TransQueryType.TOKEN_ISSUE.getValue())) {
			trans = Optional.ofNullable(transRepository.findByBankAndSsRequestIdAndApiOperation(OneFinConstants.PARTNER_VCB, requestBody.getQueryRequestId(), VcbConstants.TOKEN_ISSUER));
			return trans.map(e -> {
				// Case 1. Trans SUCCESS in OneFin connector
				if (e.getTranStatus().equals(VcbConstants.TRANS_SUCCESS)) {
					CardInstrumentAuthorizationResponse response = new CardInstrumentAuthorizationResponse();
					response.setRequestId(e.getSsRequestId());
					response.setTokenId(e.getTokenId());
					Pair<String, String> ofErrorCode = linkBankMessageUtil.findMessageByErrorCode(e.getBankStatusCode(), requestBody.getLang(), VcbConstants.LINK_BANK_CARD);
					response.setCode(ofErrorCode.getFirst());
					response.setMessage(ofErrorCode.getSecond());
					return new ResponseEntity<>(linkBankMessageUtil.buildEcoreConnectorResponse(VcbConstants.CONN_SUCCESS, response), HttpStatus.OK);
				}
				// Case 2. Trans NOT SUCCESS in OneFin connector
				else {
					// Call unlink request to VCB (Vcb not have api to query token)
					if (stringHelper.checkNullEmptyBlank(e.getCardInsId())) {
						throw new RuntimeInternalServerException("Not exist instrument id");
					} else {
						ErrorResponse instrumentDeleteResponse = null;
						try {
							instrumentDeleteResponse = linkBankRequestUtil.instrumentDelete(e.getCardInsId(), requestBody.getLang(), e);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
						linkBankDto.update(e);
						return new ResponseEntity<>(linkBankMessageUtil.buildEcoreConnectorResponse(VcbConstants.CONN_SUCCESS, instrumentDeleteResponse), HttpStatus.OK);
					}
				}
			}).orElseThrow(() -> new RuntimeBadRequestException("QueryRequestId not found"));
		}
		if (requestBody.getQueryType().getValue().equals(VcbConstants.TransQueryType.TOPUP.getValue())) {
			trans = Optional.ofNullable(transRepository.findByBankAndSsRequestIdAndApiOperation(OneFinConstants.PARTNER_VCB, requestBody.getQueryRequestId(), VcbConstants.TOPUP_TOKEN));
		}
		if (requestBody.getQueryType().getValue().equals(VcbConstants.TransQueryType.WITHDRAW.getValue())) {
			trans = Optional.ofNullable(transRepository.findByBankAndSsRequestIdAndApiOperation(OneFinConstants.PARTNER_VCB, requestBody.getQueryRequestId(), VcbConstants.WITHDRAW));
		}
		return trans.map(e -> {
			CreatePaymentCardResponse queryResponse = null;
			try {
				queryResponse = linkBankRequestUtil.paymentWithdrawSearch(requestBody, e);
				linkBankDto.update(e);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return new ResponseEntity<>(linkBankMessageUtil.buildEcoreConnectorResponse(VcbConstants.CONN_SUCCESS, queryResponse), HttpStatus.OK);
		}).orElseThrow(() -> new RuntimeBadRequestException("QueryRequestId not found"));
	}

}
