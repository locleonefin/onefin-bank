package com.onefin.ewallet.bank.service.vietin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onefin.ewallet.bank.dto.vietin.*;
import com.onefin.ewallet.bank.repository.jpa.BankListDetailsRepository;
import com.onefin.ewallet.bank.service.common.ConfigLoader;
import com.onefin.ewallet.common.base.service.RestTemplateHelper;
import com.onefin.ewallet.common.utility.string.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class VietinRequestUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(VietinRequestUtil.class);

	private static final String ibmClientId = "x-ibm-client-id";

	private static final String xIbmClientSecret = "x-ibm-client-secret";

	@Autowired
	private ConfigLoader configLoader;

	@Autowired
	protected RestTemplateHelper restTemplateHelper;

	@Autowired
	private Environment env;

	@Autowired
	private BankListDetailsRepository bankListDetailsRepository;

	@Autowired
	private StringHelper stringHelper;

	@Lazy
	@Autowired
	public VietinVirtualAcct vietinVirtualAcct;

	public ResponseEntity<LinkBankBaseResponse> sendTokenIssue(TokenIssue data) {
		String url = configLoader.getTokenIssue();
		LOGGER.info("== Send TokenIssue request to Vietin {} - url: {}", data, url);
		ResponseEntity<LinkBankBaseResponse> responseEntity = restTemplateHelper.post(url, MediaType.APPLICATION_JSON_VALUE, genVietinLinkBankHeaderMap(), new ArrayList<String>(), new HashMap<>(), configLoader.getProxyConfig(), data, new ParameterizedTypeReference<LinkBankBaseResponse>() {
		});
		LOGGER.info("== Success receive response from Vietin {}", responseEntity.getBody());
		return responseEntity;
	}

	public ResponseEntity<LinkBankBaseResponse> sendRegisterOnlinePay(RegisterOnlinePay data) {
		String url = configLoader.getRegisterOnlinePay();
		LOGGER.info("== Send RegisterOnlinePay request to Vietin {} - url: {}", data, url);
		ResponseEntity<LinkBankBaseResponse> responseEntity = restTemplateHelper.post(url, MediaType.APPLICATION_JSON_VALUE, genVietinLinkBankHeaderMap(), new ArrayList<String>(), new HashMap<>(), configLoader.getProxyConfig(), data, new ParameterizedTypeReference<LinkBankBaseResponse>() {
		});
		LOGGER.info("== Success receive response from Vietin {}", responseEntity.getBody());
		return responseEntity;
	}

	public ResponseEntity<LinkBankBaseResponse> sendVerifyPin(VerifyPin data) {
		String url = configLoader.getVerifyPin();
		LOGGER.info("== Send VerifyPin request to Vietin {} - url: {}", data, url);
		ResponseEntity<LinkBankBaseResponse> responseEntity = restTemplateHelper.post(url, MediaType.APPLICATION_JSON_VALUE, genVietinLinkBankHeaderMap(), new ArrayList<String>(), new HashMap<>(), configLoader.getProxyConfig(), data, new ParameterizedTypeReference<LinkBankBaseResponse>() {
		});
		LOGGER.info("== Success receive response from Vietin {}", responseEntity.getBody());
		return responseEntity;
	}

	public ResponseEntity<LinkBankBaseResponse> sendTokenRevoke(TokenRevokeReIssue data) {
		String url = configLoader.getTokenRevoke();
		LOGGER.info("== Send TokenRevoke request to Vietin {} - url: {}", data, url);
		ResponseEntity<LinkBankBaseResponse> responseEntity = restTemplateHelper.post(url, MediaType.APPLICATION_JSON_VALUE, genVietinLinkBankHeaderMap(), new ArrayList<String>(), new HashMap<>(), configLoader.getProxyConfig(), data, new ParameterizedTypeReference<LinkBankBaseResponse>() {
		});
		LOGGER.info("== Success receive response from Vietin {}", responseEntity.getBody());
		return responseEntity;
	}

	public ResponseEntity<LinkBankBaseResponse> sendTokenReIssue(TokenRevokeReIssue data) {
		String url = configLoader.getTokenReissue();
		LOGGER.info("== Send TokenReIssue request to Vietin {} - url: {}", data, url);
		ResponseEntity<LinkBankBaseResponse> responseEntity = restTemplateHelper.post(url, MediaType.APPLICATION_JSON_VALUE, genVietinLinkBankHeaderMap(), new ArrayList<String>(), new HashMap<>(), configLoader.getProxyConfig(), data, new ParameterizedTypeReference<LinkBankBaseResponse>() {
		});
		LOGGER.info("== Success receive response from Vietin {}", responseEntity.getBody());
		return responseEntity;
	}

	public ResponseEntity<LinkBankBaseResponse> sendPaymentByToken(PaymentByToken data) {
		String url = configLoader.getPaymentByToken();
		LOGGER.info("== Send PaymentByToken request to Vietin {} - url: {}", data, url);
		ResponseEntity<LinkBankBaseResponse> responseEntity = restTemplateHelper.post(url, MediaType.APPLICATION_JSON_VALUE, genVietinLinkBankHeaderMap(), new ArrayList<String>(), new HashMap<>(), configLoader.getProxyConfig(), data, new ParameterizedTypeReference<LinkBankBaseResponse>() {
		});
		LOGGER.info("== Success receive response from Vietin {}", responseEntity.getBody());
		return responseEntity;
	}

	public ResponseEntity<LinkBankBaseResponse> sendPaymentByOTP(PaymentByOTP data) {
		String url = configLoader.getPaymentByOTP();
		LOGGER.info("== Send PaymentByOTP request to Vietin {} - url: {}", data, url);
		ResponseEntity<LinkBankBaseResponse> responseEntity = restTemplateHelper.post(url, MediaType.APPLICATION_JSON_VALUE, genVietinLinkBankHeaderMap(), new ArrayList<String>(), new HashMap<>(), configLoader.getProxyConfig(), data, new ParameterizedTypeReference<LinkBankBaseResponse>() {
		});
		LOGGER.info("== Success receive response from Vietin {}", responseEntity.getBody());
		return responseEntity;
	}

	public ResponseEntity<LinkBankBaseResponse> sendWithdraw(Withdraw data) {
		String url = configLoader.getWidthdraw();
		LOGGER.info("== Send Withdraw request to Vietin {} - url: {}", data, url);
		ResponseEntity<LinkBankBaseResponse> responseEntity = restTemplateHelper.post(url, MediaType.APPLICATION_JSON_VALUE, genVietinLinkBankHeaderMap(), new ArrayList<String>(), new HashMap<>(), configLoader.getProxyConfig(), data, new ParameterizedTypeReference<LinkBankBaseResponse>() {
		});
		LOGGER.info("== Success receive response from Vietin {}", responseEntity.getBody());
		return responseEntity;
	}

	public ResponseEntity<LinkBankBaseResponse> sendTransactionInquiry(TransactionInquiry data) {
		String url = configLoader.getTransactionInquiry();
		LOGGER.info("== Send TransactionInquiry request to Vietin {} - url: {}", data, url);
		ResponseEntity<LinkBankBaseResponse> responseEntity = restTemplateHelper.post(url, MediaType.APPLICATION_JSON_VALUE, genVietinLinkBankHeaderMap(), new ArrayList<String>(), new HashMap<>(), configLoader.getProxyConfig(), data, new ParameterizedTypeReference<LinkBankBaseResponse>() {
		});
		LOGGER.info("== Success receive response from Vietin {}", responseEntity.getBody());
		return responseEntity;
	}

	public Map<String, Object> sendProviderInquiry(ProviderInquiry data) {
		String url = configLoader.getProviderInquiry();
		LOGGER.info("== Send ProviderInquiry request to Vietin {} - url: {}", data, url);
		ResponseEntity<LinkBankBaseResponse> responseEntity = restTemplateHelper.post(url, MediaType.APPLICATION_JSON_VALUE, genVietinLinkBankHeaderMap(), new ArrayList<String>(), new HashMap<>(), configLoader.getProxyConfig(), data, new ParameterizedTypeReference<LinkBankBaseResponse>() {
		});
		LOGGER.info("== Success receive response from Vietin {}", responseEntity.getBody());
		LinkBankBaseResponse tmp = responseEntity.getBody();
		try {
			Map<String, Object> response = new HashMap<String, Object>();
			response.put("signature", tmp.getSignature());
			response.put("providerId", tmp.getProviderId());
			response.put("merchantId", tmp.getMerchantId());
			response.put("requestId", tmp.getRequestId());
			response.put("status", tmp.getStatus());
			response.put("balances", tmp.getBalances().get(0));
			return response;
		} catch (Exception e) {
			LOGGER.warn("== Can't parse result from Vietin!!! {}", e);
			return null;
		}
	}

	public ResponseEntity<LinkBankBaseResponse> sendTokenIssuePayment(TokenIssuePayment data) {
		String url = configLoader.getTokenIssuePayment();
		LOGGER.info("== Send TokenIssuePayment request to Vietin {} - url: {}", data, url);
		ResponseEntity<LinkBankBaseResponse> responseEntity = restTemplateHelper.post(url, MediaType.APPLICATION_JSON_VALUE, genVietinLinkBankHeaderMap(), new ArrayList<String>(), new HashMap<>(), configLoader.getProxyConfig(), data, new ParameterizedTypeReference<LinkBankBaseResponse>() {
		});
		LOGGER.info("== Success receive response from Vietin {}", responseEntity.getBody());
		return responseEntity;
	}

	public ResponseEntity<LinkBankBaseResponse> sendRefund(Refund data) {
		String url = configLoader.getRefund();
		LOGGER.info("== Send Refund request to Vietin {} - url: {}", data, url);
		ResponseEntity<LinkBankBaseResponse> responseEntity = restTemplateHelper.post(url, MediaType.APPLICATION_JSON_VALUE, genVietinLinkBankHeaderMap(), new ArrayList<String>(), new HashMap<>(), configLoader.getProxyConfig(), data, new ParameterizedTypeReference<LinkBankBaseResponse>() {
		});
		LOGGER.info("== Success receive response from Vietin {}", responseEntity.getBody());
		return responseEntity;
	}

	public BankTransferResponse sendBankTransfer(BankTransferRequest data) {
		String url = configLoader.getVietinBankTransferUrl();
		LOGGER.info("== Send Bank Transfer request to Vietin {} - url: {}", data, url);
		ResponseEntity<BankTransferResponse> responseEntity = restTemplateHelper.post(url, MediaType.APPLICATION_JSON_VALUE, genVietinBankTransferHeaderMap(), new ArrayList<String>(), new HashMap<>(), configLoader.getProxyConfig(), data, new ParameterizedTypeReference<BankTransferResponse>() {
		});
		LOGGER.info("== Success receive response from Vietin {}", responseEntity.getBody());
		return responseEntity.getBody();
	}

	public BankTransferResponse sendBankTransferInquiry(BankTransferInquiryRequest data) {
		String url = configLoader.getVietinBankTransferInquiryUrl();
		LOGGER.info("== Send Bank Transfer Inquiry request to Vietin {} - url: {}", data, url);
		ResponseEntity<BankTransferResponse> responseEntity = restTemplateHelper.post(url, MediaType.APPLICATION_JSON_VALUE, genVietinBankTransferHeaderMap(), new ArrayList<String>(), new HashMap<>(), configLoader.getProxyConfig(), data, new ParameterizedTypeReference<BankTransferResponse>() {
		});
		LOGGER.info("== Success receive response from Vietin {}", responseEntity.getBody());
		return responseEntity.getBody();
	}

	public BankTransferResponse sendBankTransferAccountInquiry(BankTransferAccountInquiryRequest data) {
		String url = configLoader.getVietinBankTransferAccInquiryUrl();
		LOGGER.info("== Send Bank Transfer Account Inquiry request to Vietin {} - url: {}", data, url);
		ResponseEntity<BankTransferResponse> responseEntity = restTemplateHelper.post(url, MediaType.APPLICATION_JSON_VALUE, genVietinBankTransferHeaderMap(), new ArrayList<String>(), new HashMap<>(), configLoader.getProxyConfig(), data, new ParameterizedTypeReference<BankTransferResponse>() {
		});
		LOGGER.info("== Success receive response from Vietin {}", responseEntity.getBody());
//		if (!stringHelper.checkNullEmptyBlank(responseEntity.getBody().getBankId())) {
//			List<BankListDetails> bankListDetails = bankListDetailsRepository.findByCitadId(responseEntity.getBody().getBankId());
//			if (bankListDetails.size() > 0) {
//				responseEntity.getBody().setBankName(bankListDetails.get(0).getBankList().getName());
//			} else {
//				LOGGER.error("Bank name not found with citad code {}, please check with Vietin", responseEntity.getBody().getBankId());
//			}
//		}

		return responseEntity.getBody();
	}

	public BankTransferResponse sendBankTransferProviderInquiry(BankTransferProviderInquiryRequest data) {
		String url = configLoader.getVietinBankTransferProviderInquiryUrl();
		LOGGER.info("== Send Bank Transfer Provider Inquiry request to Vietin {} - url: {}", data, url);
		ResponseEntity<BankTransferResponse> responseEntity = restTemplateHelper.post(url, MediaType.APPLICATION_JSON_VALUE, genVietinBankTransferHeaderMap(), new ArrayList<String>(), new HashMap<>(), configLoader.getProxyConfig(), data, new ParameterizedTypeReference<BankTransferResponse>() {
		});
		LOGGER.info("== Success receive response from Vietin {}", responseEntity.getBody());
		return responseEntity.getBody();
	}

	public ResponseEntity<VietinVirtualAcctCreateBaseResponse> sendVirtualAcctCreate(VietinVirtualAcctCreate data) throws Exception {
		String url = configLoader.getVietinVirtualAcctCreateVirtualAcctUrl();
		vietinVirtualAcct.backUpRequestResponse(data.getRequestId(), data, null);

		systemPrintObjectJSON("sendVirtualAcctCreate request", data);
		ResponseEntity<VietinVirtualAcctCreateBaseResponse> responseEntity = restTemplateHelper.post(url, MediaType.APPLICATION_JSON_VALUE, genVietinVirtualAcctHeaderMap(), new ArrayList<String>(), new HashMap<>(), configLoader.getProxyConfig(), data, new ParameterizedTypeReference<VietinVirtualAcctCreateBaseResponse>() {
		});

		vietinVirtualAcct.backUpRequestResponse(Objects.requireNonNull(responseEntity.getBody()).getRequestId(), null, responseEntity.getBody());

		systemPrintObjectJSON("sendVirtualAcctCreate response", responseEntity.getBody());
		LOGGER.info("== Success receive response from Vietin {}", responseEntity.getBody());
		return responseEntity;
	}

	public ResponseEntity<VietinVirtualAcctUpdateStatusBaseResponse> sendVirtualAcctUpdateStatus(VietinVirtualAcctUpdateStatus data) throws Exception {
		String url = configLoader.getVietinVirtualAcctUpdateVirtualAcctUrl();
		LOGGER.info("== Send VirtualAcctUpdateStatus request to Vietin {} - url: {}", data, url);
		vietinVirtualAcct.backUpRequestResponse(data.getRequestId(), data, null);

		systemPrintObjectJSON("sendVirtualAcctUpdateStatus request", data);
		ResponseEntity<VietinVirtualAcctUpdateStatusBaseResponse> responseEntity = restTemplateHelper.post(url, MediaType.APPLICATION_JSON_VALUE, genVietinVirtualAcctHeaderMap(), new ArrayList<String>(), new HashMap<>(), configLoader.getProxyConfig(), data, new ParameterizedTypeReference<VietinVirtualAcctUpdateStatusBaseResponse>() {
		});

		vietinVirtualAcct.backUpRequestResponse(Objects.requireNonNull(responseEntity.getBody()).getRequestId(), null, responseEntity.getBody());

		systemPrintObjectJSON("sendVirtualAcctUpdateStatus response", responseEntity.getBody());
		LOGGER.info("== Success receive response from Vietin {}", responseEntity.getBody());
		return responseEntity;
	}

	private HashMap<String, String> genVietinVirtualAcctHeaderMap() {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.ALL));
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(ibmClientId, env.getProperty("vietin.virtualAcct.ibmClientId"));
		headers.add(xIbmClientSecret, env.getProperty("vietin.virtualAcct.xIbmClientSecret"));
		Map<String, String> headersMap = new HashMap<>();
		headers.keySet().forEach(header -> {
			headersMap.put(header, headers.getFirst(header));
		});
		return (HashMap<String, String>) headersMap;
	}


	private HashMap<String, String> genVietinLinkBankHeaderMap() {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.ALL));
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(ibmClientId, env.getProperty("vietin.linkbank.ibmClientId"));
		headers.add(xIbmClientSecret, env.getProperty("vietin.linkbank.xIbmClientSecret"));
		Map<String, String> headersMap = new HashMap<>();
		headers.keySet().forEach(header -> {
			headersMap.put(header, headers.getFirst(header));
		});
		return (HashMap<String, String>) headersMap;
	}

	private HashMap<String, String> genVietinBankTransferHeaderMap() {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.ALL));
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(ibmClientId, env.getProperty("vietin.bankTransfer.ibmClientId"));
		headers.add(xIbmClientSecret, env.getProperty("vietin.bankTransfer.xIbmClientSecret"));
		Map<String, String> headersMap = new HashMap<>();
		headers.keySet().forEach(header -> {
			headersMap.put(header, headers.getFirst(header));
		});
		return (HashMap<String, String>) headersMap;
	}

	public void systemPrintObjectJSON(String description, Object o) throws JsonProcessingException {
		// just for debug
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(o);
		// System.out.printf("%s JSON: %s%n", description, data);
		LOGGER.info("{}: {}", description, data);
	}
}
