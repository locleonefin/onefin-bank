package com.onefin.ewallet.bank.service.vcb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onefin.ewallet.bank.common.VcbConstants;
import com.onefin.ewallet.bank.dto.vcb.*;
import com.onefin.ewallet.bank.repository.jpa.LinkBankTransRepo;
import com.onefin.ewallet.bank.service.common.ConfigLoader;
import com.onefin.ewallet.bank.common.VcbConstants.VCBEwalletApiOperation;
import com.onefin.ewallet.common.base.constants.OneFinConstants;
import com.onefin.ewallet.common.base.errorhandler.RuntimeInternalServerException;
import com.onefin.ewallet.common.base.service.BaseService;
import com.onefin.ewallet.common.domain.bank.common.LinkBankTransaction;
import com.onefin.ewallet.common.utility.json.JSONHelper;
import com.onefin.ewallet.common.utility.string.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class LinkBankDto extends BaseService<LinkBankTransaction> {

	private static final Logger LOGGER = LoggerFactory.getLogger(LinkBankDto.class);

	@Autowired
	private ConfigLoader configLoader;

	@Autowired
	private LinkBankMessageUtil linkBankMessageUtil;

	@Autowired
	private LinkBankAccountEncryptUtil linkBankAccountEncryptUtil;

	@Autowired
	private StringHelper stringHelper;

	@Autowired
	private JSONHelper jSONHelper;

	@Autowired
	private LinkBankTransRepo linkBankTransRepo;

	@Autowired
	public void setEwalletTransactionRepository(LinkBankTransRepo<?> ewalletTransactionRepository) {
		this.setTransBaseRepository(ewalletTransactionRepository);
	}

	/**
	 * Decode data object receive from VCB and parse into VcbRequest
	 *
	 * @param encryptedStr
	 * @return
	 */
	public Ewallet2VcbDataDecryptResponse decodeVcbResponse(String encryptedStr) {
		Ewallet2VcbDataDecryptResponse responseData = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			String dataDecrypted = linkBankAccountEncryptUtil.vcbDecrypt(encryptedStr);
			LOGGER.info("Ewallet2VcbDataDetailResponse String: {}", dataDecrypted);
			responseData = mapper.readValue(dataDecrypted, Ewallet2VcbDataDecryptResponse.class);
			LOGGER.info("Ewallet2VcbDataDetailResponse: {}", responseData);
			return responseData;
		} catch (Exception e) {
			LOGGER.error("Cannot parse Ewallet2VcbDataDetailResponse!", e);
			throw new RuntimeInternalServerException();
		}
	}


	public Map<String, Object> buildOneFin2VcbRequest(String onefinTransId, String messageType, EWallet2VcbRequest requestBody) {
		requestBody.setPartnerId(configLoader.getVcbPartnerId());
		requestBody.setTxnId(onefinTransId);
		return linkBankMessageUtil.buildVcbBaseMessage(configLoader.getVcbPartnerId(), onefinTransId, messageType, requestBody);
	}


	public Map<String, Object> validateBaseMessage(Vcb2WalletAccountRequest data) throws JsonProcessingException {

		// check if partnerId is correct
		if (!data.getPartnerId().equals(configLoader.getVcbPartnerId())) {
			LOGGER.error("Invalid VCB partnerId");
			return linkBankMessageUtil.buildFailResponse(data, "partnerId is not correct, expected:" + configLoader.getVcbPartnerId(), null);
		}

		// check if message type is support
		if (!configLoader.getVcbActionMap().contains(data.getMessageType())) {
			LOGGER.error("Unsupported message type");
			return linkBankMessageUtil.buildFailResponse(data, "MessageType: " + data.getMessageType() + " is not supported!", null);
		}

		// validate signature
		boolean validateSign = linkBankAccountEncryptUtil.verifySignature(data.getPartnerId() + data.getRequestId() + data.getMessageType() + data.getData(), data.getSignature());
		if (!validateSign) {
			LOGGER.error("Invalid signature");
			return linkBankMessageUtil.buildFailResponse(data, "Signature is invalid!", null);
		}
		LOGGER.info("Validated successfully");
		return null;
	}

	public VcbAccountDataRequest processDataInBaseMessage(Vcb2WalletAccountRequest requestBody, String messageType, AtomicReference<LinkBankTransaction> trans) {
		String dataStr = requestBody.getData();
		String requestId = requestBody.getRequestId();
		Optional<VcbAccountDataRequest> decodeData = Optional.ofNullable(linkBankAccountEncryptUtil.decodeDataRequest(dataStr));
		return decodeData.map(e -> {
			if (VCBEwalletApiOperation.TOPUP.getVcbAction().equals(messageType)) {
				trans.getAndSet((LinkBankTransaction) linkBankTransRepo.findByBankAndPhoneNumAndTranStatus(OneFinConstants.PARTNER_VCB, e.getCustomerId(), VcbConstants.TRANS_PENDING).get(0));
				trans.get().setApiOperation(VCBEwalletApiOperation.TOPUP.getCoreWalletAction());
			} else if (VCBEwalletApiOperation.ACTIVE.getVcbAction().equals(messageType) || VCBEwalletApiOperation.DEACTIVE.getVcbAction().equals(messageType)) {
				if (VCBEwalletApiOperation.ACTIVE.getVcbAction().equals(messageType)) {
					trans.getAndSet((LinkBankTransaction) linkBankTransRepo.findByBankAndPhoneNumAndTranStatus(OneFinConstants.PARTNER_VCB, e.getCustomerId(), VcbConstants.TRANS_PENDING).get(0));
					trans.get().setApiOperation(VCBEwalletApiOperation.ACTIVE.getCoreWalletAction());
				} else {
					trans.getAndSet(new LinkBankTransaction());
					trans.get().setApiOperation(VCBEwalletApiOperation.DEACTIVE.getCoreWalletAction());
				}
			} else if (VCBEwalletApiOperation.CHECK_ACTIVE.getVcbAction().equals(messageType) || VCBEwalletApiOperation.GET_INFO.getVcbAction().equals(messageType)) {
				if (VCBEwalletApiOperation.CHECK_ACTIVE.getVcbAction().equals(messageType)) {
					trans.getAndSet(new LinkBankTransaction());
					trans.get().setApiOperation(VCBEwalletApiOperation.CHECK_ACTIVE.getCoreWalletAction());
				} else {
					trans.getAndSet(new LinkBankTransaction());
					trans.get().setApiOperation(VCBEwalletApiOperation.GET_INFO.getCoreWalletAction());
				}
			} else {
				LOGGER.error("Invalid request from VCB {} {}", requestId, messageType);
				return null;
			}
			e.setMessType(messageType);
			if ((e.getMoney() == null || e.getMoney().compareTo(BigDecimal.ZERO) <= 0) && VCBEwalletApiOperation.TOPUP.getVcbAction().equals(messageType)) {
				LOGGER.error("Invalid amount from VCB {} {}", requestId, messageType);
				return null;
			}
			if (e.getVcbId() == null && (VCBEwalletApiOperation.ACTIVE.getVcbAction().equals(messageType) /* || VCBEwalletApiOperation.DEACTIVE.getVcbAction().equals(messageType)*/)) {
				LOGGER.error("Invalid vcbId from VCB {} {}", requestId, messageType);
				return null;
			}
			if (e.getVcbTrans() == null || e.getCustomerId() == null || e.getTransDatetime() == null) {
				return null;
			}
			trans.get().setBankRequestTrans(e.getVcbTrans());
			trans.get().setPhoneNum(e.getCustomerId());
			trans.get().setTransDate(e.getTransDatetime());
			trans.get().setAmount(e.getMoney());
			trans.get().setBankTransactionId(e.getVcbId());
			return e;
		}).orElseGet(() -> {
			LOGGER.error("Invalid request from VCB {} {}", requestId, messageType);
			return null;
		});
	}

}
