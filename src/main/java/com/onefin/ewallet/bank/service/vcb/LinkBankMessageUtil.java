package com.onefin.ewallet.bank.service.vcb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onefin.ewallet.bank.common.VcbConstants;
import com.onefin.ewallet.bank.common.VcbConstants.VCBEwalletApiOperation;
import com.onefin.ewallet.bank.common.VietinConstants;
import com.onefin.ewallet.bank.dto.vcb.ConnResponse;
import com.onefin.ewallet.bank.dto.vcb.Vcb2EWalletResponse;
import com.onefin.ewallet.bank.dto.vcb.Vcb2WalletAccountRequest;
import com.onefin.ewallet.bank.repository.jpa.PartnerErrorCodeRepo;
import com.onefin.ewallet.common.base.constants.OneFinConstants;
import com.onefin.ewallet.common.base.errorhandler.RuntimeInternalServerException;
import com.onefin.ewallet.common.domain.bank.common.LinkBankTransaction;
import com.onefin.ewallet.common.domain.errorCode.PartnerErrorCode;
import com.onefin.ewallet.common.utility.string.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class LinkBankMessageUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(LinkBankMessageUtil.class.getName());

	@Autowired
	private LinkBankAccountEncryptUtil linkBankAccountEncryptUtil;

	@Autowired
	private PartnerErrorCodeRepo partnerErrorCodeRepo;

	@Autowired
	private StringHelper stringHelper;

	public Pair<String, String> findMessageByErrorCode(String code, String lang, String domain) {
		PartnerErrorCode partnerCode = partnerErrorCodeRepo.findAllByPartnerAndDomainAndCode(OneFinConstants.PARTNER_VCB, domain, String.valueOf(code));
		if (partnerCode == null) {
			LOGGER.warn("No error code found, please check the config file: {}", code);
			// Unknow error
			partnerCode = partnerErrorCodeRepo.findAllByPartnerAndDomainAndCode(OneFinConstants.PARTNER_VCB, domain, "99");
		}
		if (stringHelper.checkNullEmptyBlank(lang)) {
			return Pair.of(partnerCode.getBaseErrorCode().getCode(), partnerCode.getBaseErrorCode().getMessageEn());
		} else if (lang.equals(OneFinConstants.LANGUAGE.VIETNAMESE.getValue())) {
			return Pair.of(partnerCode.getBaseErrorCode().getCode(), partnerCode.getBaseErrorCode().getMessageVi());
		} else if (lang.equals(OneFinConstants.LANGUAGE.ENGLISH.getValue())) {
			return Pair.of(partnerCode.getBaseErrorCode().getCode(), partnerCode.getBaseErrorCode().getMessageEn());
		}
		return Pair.of(partnerCode.getBaseErrorCode().getCode(), partnerCode.getBaseErrorCode().getMessageEn());
	}

	public Map<String, Object> buildFailResponse(Vcb2WalletAccountRequest baseRequest, String message, Map<String, Object> data) throws JsonProcessingException {
		int code = VCBEwalletApiOperation.CHECK_ACTIVE.getFailCode();
		String desc = VcbConstants.VCB_CHECK_FAIL;
		if (VCBEwalletApiOperation.CHECK_ACTIVE.getVcbAction().equals(baseRequest.getMessageType()) || VCBEwalletApiOperation.GET_INFO.getVcbAction().equals(baseRequest.getMessageType())) {
			code = VCBEwalletApiOperation.CHECK_ACTIVE.getFailCode();
			desc = VcbConstants.VCB_CHECK_FAIL;
		} else if (VCBEwalletApiOperation.ACTIVE.getVcbAction().equals(baseRequest.getMessageType())) {
			code = VCBEwalletApiOperation.ACTIVE.getFailCode();
			desc = VcbConstants.VCB_ACTIVE_FAIL;
		} else if (VCBEwalletApiOperation.DEACTIVE.getVcbAction().equals(baseRequest.getMessageType())) {
			code = VCBEwalletApiOperation.DEACTIVE.getFailCode();
			desc = VcbConstants.VCB_DEACTIVE_FAIL;
		} else if (VCBEwalletApiOperation.TOPUP.getVcbAction().equals(baseRequest.getMessageType())) {
			code = VCBEwalletApiOperation.TOPUP.getFailCode();
			desc = VcbConstants.VCB_TOPUP_FAIL;
		}

		Map<String, Object> dataMap = buildVcbResponseData(code, desc, message, Optional.ofNullable(data));
		return buildVcbBaseResponse(baseRequest, dataMap);
	}

	public Map<String, Object> buildVcbResponseFromEwalletCore(Vcb2WalletAccountRequest requestBody, Vcb2EWalletResponse eCoreData, LinkBankTransaction trx) throws JsonProcessingException {
		if (VCBEwalletApiOperation.CHECK_ACTIVE.getVcbAction().equals(requestBody.getMessageType())) {
			String desc = null;
			String message = null;
			if (eCoreData.getCode() == VCBEwalletApiOperation.CHECK_ACTIVE.getSuccessCode()) {
				desc = VcbConstants.VCB_CHECK_SUCCESS;
				message = "Check account successfully";
				trx.setTranStatus(VietinConstants.TRANS_PENDING);
				trx.setSendOtp(true);
				trx.setBankStatusCode(Integer.toString(eCoreData.getCode()));
			}
			if (eCoreData.getCode() == VCBEwalletApiOperation.CHECK_ACTIVE.getFailCode()) {
				desc = VcbConstants.VCB_CHECK_FAIL;
				message = "Check account error";
				trx.setTranStatus(VietinConstants.TRANS_ERROR);
				trx.setBankStatusCode(Integer.toString(eCoreData.getCode()));
			}
			if (eCoreData.getCode() == VCBEwalletApiOperation.CHECK_ACTIVE.getExistedCode()) {
				desc = VcbConstants.VCB_ALREADY_EXISTED;
				message = "Previously registered account";
				trx.setTranStatus(VietinConstants.TRANS_ERROR);
				trx.setBankStatusCode(Integer.toString(eCoreData.getCode()));
			}
			if (eCoreData.getCode() == VCBEwalletApiOperation.CHECK_ACTIVE.getInvalidNotfoundCode()) {
				desc = VcbConstants.VCB_NOT_FOUND_OR_INVALID;
				message = "The account is invalid or does not exist";
				trx.setTranStatus(VietinConstants.TRANS_ERROR);
				trx.setBankStatusCode(Integer.toString(eCoreData.getCode()));
			}
			Map<String, Object> dataMap = buildVcbResponseData(eCoreData.getCode(), desc, message, Optional.ofNullable(eCoreData.getData()));
			return buildVcbBaseResponse(requestBody, dataMap);

		} else if (VCBEwalletApiOperation.ACTIVE.getVcbAction().equals(requestBody.getMessageType())) {
			String desc = null;
			String message = null;
			if (eCoreData.getCode() == VCBEwalletApiOperation.ACTIVE.getSuccessCode()) {
				desc = VcbConstants.VCB_ACTIVE_SUCCESS;
				message = "Registered successfully";
				trx.setTranStatus(VietinConstants.TRANS_SUCCESS);
				trx.setBankStatusCode(Integer.toString(eCoreData.getCode()));
			}
			if (eCoreData.getCode() == VCBEwalletApiOperation.ACTIVE.getFailCode()) {
				desc = VcbConstants.VCB_ACTIVE_FAIL;
				message = "Registered unsuccessfully";
				trx.setTranStatus(VietinConstants.TRANS_ERROR);
				trx.setBankStatusCode(Integer.toString(eCoreData.getCode()));
			}
			if (eCoreData.getCode() == VCBEwalletApiOperation.ACTIVE.getExistedCode()) {
				desc = VcbConstants.VCB_ALREADY_EXISTED;
				message = "Previously registered account";
				trx.setTranStatus(VietinConstants.TRANS_ERROR);
				trx.setBankStatusCode(Integer.toString(eCoreData.getCode()));
			}
			if (eCoreData.getCode() == VCBEwalletApiOperation.ACTIVE.getInvalidNotfoundCode()) {
				desc = VcbConstants.VCB_NOT_FOUND_OR_INVALID;
				message = "The account is invalid or does not exist";
				trx.setTranStatus(VietinConstants.TRANS_ERROR);
				trx.setBankStatusCode(Integer.toString(eCoreData.getCode()));
			}
			Map<String, Object> dataMap = buildVcbResponseData(eCoreData.getCode(), desc, message, Optional.ofNullable(eCoreData.getData()));
			return buildVcbBaseResponse(requestBody, dataMap);

		} else if (VCBEwalletApiOperation.DEACTIVE.getVcbAction().equals(requestBody.getMessageType())) {
			String desc = null;
			String message = null;
			if (eCoreData.getCode() == VCBEwalletApiOperation.DEACTIVE.getSuccessCode()) {
				desc = VcbConstants.VCB_DEACTIVE_SUCCESS;
				message = "Unlink successfully";
				trx.setTranStatus(VietinConstants.TRANS_SUCCESS);
				trx.setBankStatusCode(Integer.toString(eCoreData.getCode()));
			}
			if (eCoreData.getCode() == VCBEwalletApiOperation.DEACTIVE.getFailCode()) {
				desc = VcbConstants.VCB_DEACTIVE_FAIL;
				message = "Unlink unsuccessfully";
				trx.setTranStatus(VietinConstants.TRANS_ERROR);
				trx.setBankStatusCode(Integer.toString(eCoreData.getCode()));
			}
			if (eCoreData.getCode() == VCBEwalletApiOperation.DEACTIVE.getInvalidNotfoundCode()) {
				desc = VcbConstants.VCB_NOT_FOUND_OR_INVALID;
				message = "The account is invalid or does not exist";
				trx.setTranStatus(VietinConstants.TRANS_ERROR);
				trx.setBankStatusCode(Integer.toString(eCoreData.getCode()));
			}
			Map<String, Object> dataMap = buildVcbResponseData(eCoreData.getCode(), desc, message, Optional.ofNullable(eCoreData.getData()));
			return buildVcbBaseResponse(requestBody, dataMap);

		} else if (VCBEwalletApiOperation.GET_INFO.getVcbAction().equals(requestBody.getMessageType())) {
			String desc = null;
			String message = null;
			if (eCoreData.getCode() == VCBEwalletApiOperation.GET_INFO.getSuccessCode()) {
				desc = VcbConstants.VCB_CHECK_SUCCESS;
				message = "Check account successfully";
				trx.setTranStatus(VietinConstants.TRANS_PENDING);
				trx.setBankStatusCode(Integer.toString(eCoreData.getCode()));
			}
			if (eCoreData.getCode() == VCBEwalletApiOperation.GET_INFO.getFailCode()) {
				desc = VcbConstants.VCB_CHECK_FAIL;
				message = "Check account error";
				trx.setTranStatus(VietinConstants.TRANS_ERROR);
				trx.setBankStatusCode(Integer.toString(eCoreData.getCode()));
			}
			Map<String, Object> dataMap = buildVcbResponseData(eCoreData.getCode(), desc, message, Optional.ofNullable(eCoreData.getData()));
			return buildVcbBaseResponse(requestBody, dataMap);

		} else if (VCBEwalletApiOperation.TOPUP.getVcbAction().equals(requestBody.getMessageType())) {
			String desc = null;
			String message = null;
			if (eCoreData.getCode() == VCBEwalletApiOperation.TOPUP.getSuccessCode()) {
				desc = VcbConstants.VCB_TOPUP_SUCCESS;
				message = "Topup successfully";
				trx.setTranStatus(VietinConstants.TRANS_SUCCESS);
				trx.setBankStatusCode(Integer.toString(eCoreData.getCode()));
			}
			if (eCoreData.getCode() == VCBEwalletApiOperation.TOPUP.getFailCode()) {
				desc = VcbConstants.VCB_TOPUP_FAIL;
				message = "Topup error";
				trx.setTranStatus(VietinConstants.TRANS_ERROR);
				trx.setBankStatusCode(Integer.toString(eCoreData.getCode()));
			}
			if (eCoreData.getCode() == VCBEwalletApiOperation.TOPUP.getTimeoutCode()) {
				desc = VcbConstants.VCB_TOPUP_TIMEOUT;
				message = "Topup timeout";
				trx.setTranStatus(VietinConstants.TRANS_ERROR);
				trx.setBankStatusCode(Integer.toString(eCoreData.getCode()));
			}
			if (eCoreData.getCode() == VCBEwalletApiOperation.TOPUP.getInvalidNotfoundCode()) {
				desc = VcbConstants.VCB_NOT_FOUND_OR_INVALID;
				message = "The account is invalid or does not exist";
				trx.setTranStatus(VietinConstants.TRANS_ERROR);
				trx.setBankStatusCode(Integer.toString(eCoreData.getCode()));
			}
			Map<String, Object> dataMap = buildVcbResponseData(eCoreData.getCode(), desc, message, Optional.ofNullable(eCoreData.getData()));
			return buildVcbBaseResponse(requestBody, dataMap);
		}

		trx.setTranStatus(VietinConstants.TRANS_ERROR);
		trx.setBankStatusCode(Integer.toString(eCoreData.getCode()));
		trx.setWalletId(eCoreData.getData() != null ? eCoreData.getData().getWalletId() : null);
		return buildFailResponse(requestBody, "System error!", null);
	}

	private Map<String, Object> buildVcbBaseResponse(Vcb2WalletAccountRequest baseRequest, Object data) {
		return buildVcbBaseMessage(baseRequest.getPartnerId(), baseRequest.getRequestId(), baseRequest.getMessageType(), data);
	}

	private Map<String, Object> buildVcbResponseData(Integer code, String desc, String message, Optional<Object> data) {
		Map<String, Object> result = new HashMap<>();
		result.put(VcbConstants.VCB_RESP_CODE, code);
		result.put(VcbConstants.VCB_RESP_DESC, desc);
		result.put(VcbConstants.VCB_RESP_MESSAGE, message);
		data.ifPresent((e) -> {
			ObjectMapper mapper = new ObjectMapper();
			try {
				result.put(VcbConstants.VCB_RESP_DATA, mapper.writeValueAsString(e));
			} catch (JsonProcessingException ex) {
				ex.printStackTrace();
			}
		});
		return result;
	}

	/**
	 * Build final Json object send / response to VCB
	 *
	 * @param partnerId
	 * @param requestId
	 * @param messageType
	 * @param data
	 * @return
	 */
	public Map<String, Object> buildVcbBaseMessage(String partnerId, String requestId, String messageType, Object data) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			Map<String, Object> map = new HashMap<>();
			map.put(VcbConstants.VCB_PARTNER_ID, partnerId);
			map.put(VcbConstants.VCB_REQUEST_ID, requestId);
			map.put(VcbConstants.VCB_MESSAGE_TYPE, messageType);

			String dataStr = mapper.writeValueAsString(data);
			LOGGER.info("Data Vcb Base Message {}: {}", messageType, dataStr);

			String encrypedData = linkBankAccountEncryptUtil.vcbEncrypt(dataStr);
			String signedStr = partnerId + requestId + messageType + encrypedData;
			String signedData = linkBankAccountEncryptUtil.sign(signedStr);

			map.put(VcbConstants.VCB_DATA, encrypedData);
			map.put(VcbConstants.VCB_SIGNATURE, signedData);

			return map;
		} catch (Exception e) {
			LOGGER.error("Cannot build VCB base object", e);
			throw new RuntimeInternalServerException();
		}
	}

	public ConnResponse buildEcoreConnectorResponse(String code, Object data, String... args) {
		ConnResponse response = new ConnResponse();
		response.setConnectorCode(code);
		response.setResponse(data);
//		response.setVersion("");
//		response.setType("");
		return response;
	}
}
