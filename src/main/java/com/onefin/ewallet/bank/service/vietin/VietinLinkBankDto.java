package com.onefin.ewallet.bank.service.vietin;

import com.onefin.ewallet.bank.common.VietinConstants;
import com.onefin.ewallet.bank.dto.vietin.*;
import com.onefin.ewallet.bank.repository.jpa.LinkBankTransRepo;
import com.onefin.ewallet.bank.repository.jpa.PartnerErrorCodeRepo;
import com.onefin.ewallet.bank.service.common.ConfigLoader;
import com.onefin.ewallet.common.base.constants.OneFinConstants;
import com.onefin.ewallet.common.base.errorhandler.RuntimeInternalServerException;
import com.onefin.ewallet.common.base.service.BackupService;
import com.onefin.ewallet.common.base.service.BaseService;
import com.onefin.ewallet.common.domain.bank.common.LinkBankTransaction;
import com.onefin.ewallet.common.domain.errorCode.PartnerErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Objects;

@Service
public class VietinLinkBankDto extends BaseService<LinkBankTransaction> {

	private static final Logger LOGGER = LoggerFactory.getLogger(VietinLinkBankDto.class);

	@Autowired
	private ConfigLoader configLoader;

	@Autowired
	private VietinMessageUtil iMessageUtil;

	@Autowired
	private VietinEncryptUtil encryptUtil;

	@Autowired
	private BackupService backupService;

	@Autowired
	private PartnerErrorCodeRepo partnerErrorCodeRepo;

	@Autowired
	private Environment env;

	@Autowired
	public void setEwalletTransactionRepository(LinkBankTransRepo<?> ewalletTransactionRepository) {
		this.setTransBaseRepository(ewalletTransactionRepository);
	}

	public TokenIssue buildVietinTokenIssuer(TokenIssue data, String linkType)
			throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		data.setProviderId(configLoader.getVietinProviderId());
		if (linkType.equals(VietinConstants.LinkType.CARD.toString())) {
			data.setMerchantId(configLoader.getVietinMerchantIdCard());
		}
		if (linkType.equals(VietinConstants.LinkType.ACCOUNT.toString())) {
			data.setMerchantId(configLoader.getVietinMerchantIdAccount());
		}
		data.setVersion(configLoader.getVietinVersion());

		String dataSign = String.format("%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s", data.getCardNumber(), data.getCardIssueDate(),
				data.getCardHolderName(), data.getProviderCustId(), data.getCustPhoneNo(), data.getCustIDNo(),
				data.getClientIP(), data.getTransTime(), data.getRequestId(), data.getProviderId(),
				data.getMerchantId(), data.getChannel(), data.getVersion(), data.getLanguage(), data.getMac());

		LOGGER.debug("Before Sign Data - " + dataSign);
		String signData = viettinSign(dataSign);
		data.setSignature(signData);
		LOGGER.debug("After Sign Data - " + signData);
		return data;
	}

	public VerifyPin buildVietinVerifyPin(VerifyPin data, String linkType)
			throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		data.setProviderId(configLoader.getVietinProviderId());
		if (linkType.equals(VietinConstants.LinkType.CARD.toString())) {
			data.setMerchantId(configLoader.getVietinMerchantIdCard());
		}
		if (linkType.equals(VietinConstants.LinkType.ACCOUNT.toString())) {
			data.setMerchantId(configLoader.getVietinMerchantIdAccount());
		}
		data.setVersion(configLoader.getVietinVersion());

		String dataSign = String.format("%s%s%s%s%s%s%s%s%s%s%s%s", data.getOtp(), data.getVerifyTransactionId(),
				data.getVerifyBy(), data.getTransTime(), data.getClientIP(), data.getRequestId(), data.getProviderId(),
				data.getMerchantId(), data.getChannel(), data.getVersion(), data.getLanguage(), data.getMac());

		LOGGER.debug("Before Sign Data - " + dataSign);
		String signData = viettinSign(dataSign);
		data.setSignature(signData);
		LOGGER.debug("After Sign Data - " + signData);
		return data;
	}

	public RegisterOnlinePay buildVietinRegisterOnlinePay(RegisterOnlinePay data, String linkType)
			throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		data.setProviderId(configLoader.getVietinProviderId());
		if (linkType.equals(VietinConstants.LinkType.CARD.toString())) {
			data.setMerchantId(configLoader.getVietinMerchantIdCard());
		}
		if (linkType.equals(VietinConstants.LinkType.ACCOUNT.toString())) {
			data.setMerchantId(configLoader.getVietinMerchantIdAccount());
		}
		data.setVersion(configLoader.getVietinVersion());

		String dataSign = String.format("%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s", data.getCardNumber(),
				data.getCardIssueDate(), data.getCardHolderName(), data.getProviderCustId(), data.getCustIDNo(),
				data.getCustIDIssueDate(), data.getCustIDIssueBy(), data.getCustPhoneNo(), data.getCustGender(),
				data.getCustBirthday(), data.getClientIP(), data.getTransTime(), data.getProviderId(),
				data.getMerchantId(), data.getChannel(), data.getVersion(), data.getLanguage(), data.getMac());

		LOGGER.debug("Before Sign Data - " + dataSign);
		String signData = viettinSign(dataSign);
		data.setSignature(signData);
		LOGGER.debug("After Sign Data - " + signData);
		return data;
	}

	public TokenRevokeReIssue buildVietinTokenRevoke(TokenRevokeReIssue data, String linkType)
			throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		data.setProviderId(configLoader.getVietinProviderId());
		if (linkType.equals(VietinConstants.LinkType.CARD.toString())) {
			data.setMerchantId(configLoader.getVietinMerchantIdCard());
		}
		if (linkType.equals(VietinConstants.LinkType.ACCOUNT.toString())) {
			data.setMerchantId(configLoader.getVietinMerchantIdAccount());
		}
		data.setVersion(configLoader.getVietinVersion());

		String dataSign = String.format("%s%s%s%s%s%s%s%s%s%s", data.getToken(), data.getTokenIssueDate(),
				data.getTransTime(), data.getClientIP(), data.getProviderId(), data.getMerchantId(), data.getChannel(),
				data.getVersion(), data.getLanguage(), data.getMac());

		LOGGER.debug("Before Sign Data - " + dataSign);
		String signData = viettinSign(dataSign);
		data.setSignature(signData);
		LOGGER.debug("After Sign Data - " + signData);
		return data;
	}


	public TokenRevokeReIssue buildVietinTokenReIssue(TokenRevokeReIssue data, String linkType)
			throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		data.setProviderId(configLoader.getVietinProviderId());
		if (linkType.equals(VietinConstants.LinkType.CARD.toString())) {
			data.setMerchantId(configLoader.getVietinMerchantIdCard());
		}
		if (linkType.equals(VietinConstants.LinkType.ACCOUNT.toString())) {
			data.setMerchantId(configLoader.getVietinMerchantIdAccount());
		}
		data.setVersion(configLoader.getVietinVersion());

		String dataSign = String.format("%s%s%s%s%s%s%s%s%s%s", data.getToken(), data.getTokenIssueDate(),
				data.getTransTime(), data.getClientIP(), data.getProviderId(), data.getMerchantId(), data.getChannel(),
				data.getVersion(), data.getLanguage(), data.getMac());

		LOGGER.debug("Before Sign Data - " + dataSign);
		String signData = viettinSign(dataSign);
		data.setSignature(signData);
		LOGGER.debug("After Sign Data - " + signData);
		return data;
	}


	public PaymentByToken buildVietinPaymentByToken(PaymentByToken data, String linkType)
			throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		data.setCurrencyCode(VietinConstants.CURRENCY_VND);
		data.setProviderId(configLoader.getVietinProviderId());
		if (linkType.equals(VietinConstants.LinkType.CARD.toString())) {
			data.setMerchantId(configLoader.getVietinMerchantIdCard());
		}
		if (linkType.equals(VietinConstants.LinkType.ACCOUNT.toString())) {
			data.setMerchantId(configLoader.getVietinMerchantIdAccount());
		}
		data.setVersion(configLoader.getVietinVersion());

		String dataSign = String.format("%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s", data.getToken(), data.getTokenIssueDate(),
				data.getAmount(), data.getCurrencyCode(), data.getTransTime(), data.getClientIP(), data.getPayMethod(),
				data.getGoodsType(), data.getBillNo(), data.getRemark(), data.getProviderId(), data.getMerchantId(),
				data.getChannel(), data.getVersion(), data.getLanguage(), data.getMac());

		LOGGER.debug("Before Sign Data - " + dataSign);
		String signData = viettinSign(dataSign);
		data.setSignature(signData);
		LOGGER.debug("After Sign Data - " + signData);
		return data;
	}


	public PaymentByOTP buildVietinPaymentByOTP(PaymentByOTP data, String linkType)
			throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		data.setCurrencyCode(VietinConstants.CURRENCY_VND);
		data.setProviderId(configLoader.getVietinProviderId());
		if (linkType.equals(VietinConstants.LinkType.CARD.toString())) {
			data.setMerchantId(configLoader.getVietinMerchantIdCard());
		}
		if (linkType.equals(VietinConstants.LinkType.ACCOUNT.toString())) {
			data.setMerchantId(configLoader.getVietinMerchantIdAccount());
		}
		data.setVersion(configLoader.getVietinVersion());

		String dataSign = String.format("%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s", data.getToken(), data.getTokenIssueDate(),
				data.getAmount(), data.getCurrencyCode(), data.getTransTime(), data.getClientIP(), data.getPayMethod(),
				data.getGoodsType(), data.getBillNo(), data.getRemark(), data.getProviderId(), data.getMerchantId(),
				data.getChannel(), data.getVersion(), data.getLanguage(), data.getMac());

		LOGGER.debug("Before Sign Data - " + dataSign);
		String signData = viettinSign(dataSign);
		data.setSignature(signData);
		LOGGER.debug("After Sign Data - " + signData);
		return data;
	}


	public Withdraw buildVietinWithdraw(Withdraw data, String linkType)
			throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		data.setCurrencyCode(VietinConstants.CURRENCY_VND);
		data.setProviderId(configLoader.getVietinProviderId());
		if (linkType.equals(VietinConstants.LinkType.CARD.toString())) {
			data.setMerchantId(configLoader.getVietinMerchantIdCard());
		}
		if (linkType.equals(VietinConstants.LinkType.ACCOUNT.toString())) {
			data.setMerchantId(configLoader.getVietinMerchantIdAccount());
		}
		data.setVersion(configLoader.getVietinVersion());

		String dataSign = String.format("%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s", data.getToken(), data.getTokenIssueDate(),
				data.getAmount(), data.getCurrencyCode(), data.getTransTime(), data.getClientIP(), data.getBenName(),
				data.getBenAcctNo(), data.getBenIDNo(), data.getBenAddInfo(), data.getRemark(), data.getProviderId(),
				data.getMerchantId(), data.getChannel(), data.getVersion(), data.getLanguage(), data.getMac());

		LOGGER.debug("Before Sign Data - " + dataSign);
		String signData = viettinSign(dataSign);
		data.setSignature(signData);
		LOGGER.debug("After Sign Data - " + signData);
		return data;
	}


	public TransactionInquiry buildVietinTransactionInquiry(TransactionInquiry data, String linkType)
			throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		data.setProviderId(configLoader.getVietinProviderId());
		if (linkType.equals(VietinConstants.LinkType.CARD.toString())) {
			data.setMerchantId(configLoader.getVietinMerchantIdCard());
		}
		if (linkType.equals(VietinConstants.LinkType.ACCOUNT.toString())) {
			data.setMerchantId(configLoader.getVietinMerchantIdAccount());
		}
		data.setVersion(configLoader.getVietinVersion());

		String dataSign = String.format("%s%s%s%s%s%s%s%s", data.getQueryTransactionId(), data.getQueryType(),
				data.getProviderId(), data.getMerchantId(), data.getChannel(), data.getVersion(), data.getLanguage(),
				data.getMac());

		LOGGER.debug("Before Sign Data - " + dataSign);
		String signData = viettinSign(dataSign);
		data.setSignature(signData);
		LOGGER.debug("After Sign Data - " + signData);
		return data;
	}


	public ProviderInquiry buildVietinProviderInquiry(ProviderInquiry data)
			throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		data.setProviderId(configLoader.getVietinProviderId());
		data.setMerchantId(configLoader.getVietinMerchantIdCard());
		data.setVersion(configLoader.getVietinVersion());

		String dataSign = String.format("%s%s%s%s%s%s", data.getProviderId(), data.getMerchantId(), data.getChannel(),
				data.getVersion(), data.getLanguage(), data.getMac());

		LOGGER.debug("Before Sign Data - " + dataSign);
		String signData = viettinSign(dataSign);
		data.setSignature(signData);
		LOGGER.debug("After Sign Data - " + signData);
		return data;
	}


	public TokenIssuePayment buildVietinTokenIssuerPayment(TokenIssuePayment data, String linkType)
			throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		data.setCurrencyCode(VietinConstants.CURRENCY_VND);
		data.setProviderId(configLoader.getVietinProviderId());
		if (linkType.equals(VietinConstants.LinkType.CARD.toString())) {
			data.setMerchantId(configLoader.getVietinMerchantIdCard());
		}
		if (linkType.equals(VietinConstants.LinkType.ACCOUNT.toString())) {
			data.setMerchantId(configLoader.getVietinMerchantIdAccount());
		}
		data.setVersion(configLoader.getVietinVersion());

		String dataSign = String.format("%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s", data.getCardNumber(),
				data.getCardIssueDate(), data.getCardHolderName(), data.getAmount(), data.getCurrencyCode(),
				data.getProviderCustId(), data.getCustPhoneNo(), data.getCustIDNo(), data.getClientIP(),
				data.getTransTime(), data.getRequestId(), data.getProviderId(), data.getMerchantId(), data.getChannel(),
				data.getVersion(), data.getLanguage(), data.getMac());

		LOGGER.debug("Before Sign Data - " + dataSign);
		String signData = viettinSign(dataSign);
		data.setSignature(signData);
		LOGGER.debug("After Sign Data - " + signData);
		return data;
	}


	public Refund buildVietinRefund(Refund data, String linkType)
			throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		data.setCurrencyCode(VietinConstants.CURRENCY_VND);
		data.setProviderId(configLoader.getVietinProviderId());
		if (linkType.equals(VietinConstants.LinkType.CARD.toString())) {
			data.setMerchantId(configLoader.getVietinMerchantIdCard());
		}
		if (linkType.equals(VietinConstants.LinkType.ACCOUNT.toString())) {
			data.setMerchantId(configLoader.getVietinMerchantIdAccount());
		}
		data.setVersion(configLoader.getVietinVersion());

		String dataSign = String.format("%s%s%s%s%s%s%s%s%s%s%s", data.getAmount(), data.getCurrencyCode(),
				data.getRefundTransactionId(), data.getTransTime(), data.getClientIP(), data.getProviderId(),
				data.getMerchantId(), data.getChannel(), data.getVersion(), data.getLanguage(), data.getMac());

		LOGGER.debug("Before Sign Data - " + dataSign);
		String signData = viettinSign(dataSign);
		data.setSignature(signData);
		LOGGER.debug("After Sign Data - " + signData);
		return data;
	}

	private String viettinSign(String input) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		PrivateKey privateKeyOneFin = encryptUtil.readPrivateKey(configLoader.getOnefinPrivateKey());
		String signedData = encryptUtil.sign(input, privateKeyOneFin);
		return signedData;
	}

	/**
	 * Validate response from VTB
	 *
	 * @param data
	 * @param trans
	 * @return
	 */

	public ConnResponse validateResponse(LinkBankBaseResponse data, LinkBankTransaction trans, HttpStatus httpStatus, String type, String lang) {
		try {
			String code = data.getStatus() != null ? Objects.toString(data.getStatus().getCode()) : null;
			if (!code.equals(VietinConstants.VTB_SUCCESS_CODE) && !code.equals(VietinConstants.VTB_PAY_BY_OTP_CODE)) {
				LOGGER.warn("Warning: RequestId {} - {}", data.getRequestId(), data);
			}
			// Check sent OTP
			if (trans != null && (code.equals(VietinConstants.VTB_PAY_BY_OTP_CODE) || (code.equals(VietinConstants.VTB_SUCCESS_CODE) &&
					(trans.getApiOperation().equals(VietinConstants.TOKEN_ISSUER) || trans.getApiOperation().equals(VietinConstants.REGISTER_ONLINE_PAYMENT) ||
							trans.getApiOperation().equals(VietinConstants.TOPUP_TOKEN_OTP) || trans.getApiOperation().equals(VietinConstants.TOKEN_ISSUER_TOPUP))))) {
				LOGGER.info("Need verify OTP: RequestId {} - {}", data.getRequestId(), data);
				trans.setSendOtp(true);
			}
			// Temporary
			if (code.equals(VietinConstants.VTB_INSUFFICIENT_MASTER_ACCOUNT_BALANCE)) {
				return iMessageUtil.buildVietinConnectorResponse(VietinConstants.CONN_SERVICE_UNDER_MAINTENANCE, null,
						type);
			}
			if (!isValidMessage(data.getRequestId(), data.getProviderId(), data.getMerchantId(), data.getSignature())) {
				LOGGER.error("Invalid response from Vietin!!!");
				return iMessageUtil.buildVietinConnectorResponse(VietinConstants.CONN_PARTNER_INVALID_RESPONSE, null,
						type);
			}

			// validate signature
			if (!verifySignature(data.getRequestId() + data.getProviderId() + data.getMerchantId() + code,
					data.getSignature())) {
				LOGGER.error("Verify signature fail!!!");
				return iMessageUtil.buildVietinConnectorResponse(VietinConstants.CONN_PARTNER_INVALID_SIGNATURE, null,
						type);
			}

			LOGGER.info("Validation success!");
			transformErrorCode(data, data.getStatus().getCode(), lang);
			return iMessageUtil.buildVietinConnectorResponse(VietinConstants.CONN_SUCCESS, data, type);

		} catch (Exception e) {
			LOGGER.error("Validate response from Vietin error!!!", e);
			throw new RuntimeInternalServerException();
		}
	}


	public void backUpRequestResponse(String requestId, Object request, Object response) throws Exception {
		if (request != null) {
			backupService.backup(configLoader.getBackupVietinLinkBank(), requestId, request,
					VietinConstants.BACKUP_REQUEST);
		}
		if (response != null) {
			backupService.backup(configLoader.getBackupVietinLinkBank(), requestId, response,
					VietinConstants.BACKUP_RESPONSE);
		}
	}

	private boolean isValidMessage(String requestId, String providerId, String merchantId, String signature) {
		if (providerId == null || providerId.trim().isEmpty() || requestId == null || requestId.trim().isEmpty()
				|| signature == null || signature.trim().isEmpty() || merchantId == null
				|| merchantId.trim().isEmpty()) {

			return false;
		}
		if (!configLoader.getVietinProviderId().equals(providerId)) {
			LOGGER.error("ProviderId not support: {}", providerId);
			return false;
		}
		if (!configLoader.getVietinMerchantIdCard().equals(merchantId)
				&& !configLoader.getVietinMerchantIdAccount().equals(merchantId)) {
			LOGGER.error("MerchantId not support: {}", merchantId);
			return false;
		}
		return true;
	}

	private boolean verifySignature(String data, String signature) throws CertificateException, IOException {
		PublicKey publicKeyVietin = encryptUtil.readPublicKey2(configLoader.getVtbPublicKey());
		return encryptUtil.verifySignature(data, signature, publicKeyVietin);
	}

	private void transformErrorCode(LinkBankBaseResponse data, String code, String lang) {
		PartnerErrorCode partnerCode = partnerErrorCodeRepo.findAllByPartnerAndDomainAndCode(OneFinConstants.PARTNER_VIETINBANK, OneFinConstants.LINK_BANK, code);
		if (partnerCode == null) {
			LOGGER.warn("No error code found, please check the config file: {}", code);
			return;
		}
		//data.getStatus().setCode(partnerCode.getBaseErrorCode().getCode());
		if (lang.equals(OneFinConstants.LANGUAGE.VIETNAMESE.getValue())) {
			data.getStatus().setMessage(partnerCode.getBaseErrorCode().getMessageVi());
		} else if (lang.equals(OneFinConstants.LANGUAGE.ENGLISH.getValue())) {
			data.getStatus().setMessage(partnerCode.getBaseErrorCode().getMessageEn());
		} else {
			data.getStatus().setMessage(partnerCode.getBaseErrorCode().getMessageEn());
		}
	}

}
