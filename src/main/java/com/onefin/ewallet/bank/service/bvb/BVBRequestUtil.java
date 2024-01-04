package com.onefin.ewallet.bank.service.bvb;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.onefin.ewallet.common.base.constants.BankConstants;
import com.onefin.ewallet.bank.dto.vietin.ConnResponse;
import com.onefin.ewallet.bank.repository.jpa.PartnerErrorCodeRepo;
import com.onefin.ewallet.bank.service.common.ConfigLoader;
import com.onefin.ewallet.common.base.constants.OneFinConstants;
import com.onefin.ewallet.common.base.service.BackupService;
import com.onefin.ewallet.common.base.service.RestTemplateHelper;
import com.onefin.ewallet.common.domain.errorCode.PartnerErrorCode;
import com.onefin.ewallet.common.utility.json.JSONHelper;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.PrivateKey;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class BVBRequestUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(BVBRequestUtil.class);

	@Autowired
	private Environment env;

	@Autowired
	private ConfigLoader configLoader;

	@Autowired
	protected RestTemplateHelper restTemplateHelper;

	@Autowired
	private JSONHelper jsonHelper;

	@Autowired
	private BVBEncryptUtil bvbEncryptUtil;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private PartnerErrorCodeRepo partnerErrorCodeRepo;

	@Autowired
	private BackupService backupService;

	public HashMap<String, String> buildHeader(String signature) throws Exception {

		PrivateKey privateKey =
				bvbEncryptUtil.readPrivateKeyBVB(env.getProperty("bvb.virtualAcct.onefinPrivateKey"));
		String digitalSignature = bvbEncryptUtil.sign(signature, privateKey);
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.set("signature", digitalSignature);
		httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + env.getProperty("bvb.virtualAcct.bearerToken"));
		HashMap<String, String> headersMap = new HashMap<>();
		httpHeaders.keySet().forEach(header -> {
			headersMap.put(header, httpHeaders.getFirst(header));
		});
		LOGGER.info("Header request: " + httpHeaders);
		LOGGER.info("Body request: " + signature);
		return headersMap;

	}

	public <T> ResponseEntity<?> requestBVB(
			T dataRequest,
			String url,
			String requestId,
			String prefix
	) throws Exception {

		return postRequest(url, dataRequest, requestId, prefix);

	}

	public <Q, R> ResponseEntity<R> postRequest(String url, Q dataRequest, String requestId) throws Exception {

		HashMap<String, String> header = buildHeader(jsonHelper.convertMap2JsonString(dataRequest));
		ResponseEntity<R> responseEntity = postRequest(url, dataRequest, header);
		// backup api
		try {
			backUpRequestResponse(requestId, dataRequest, responseEntity.getBody(), header);
		} catch (Exception e) {
			LOGGER.error("Api backup Failed: url - {}\n" +
					"requestId: {}\n" +
					"dataRequest: {}\n" +
					"response: {}", url, requestId, dataRequest, responseEntity.getBody());
		}

		return responseEntity;
	}

	public <Q, R> ResponseEntity<R> postRequest(String url, Q dataRequest, String requestId, String prefix) throws Exception {
		HashMap<String, String> header = buildHeader(jsonHelper.convertMap2JsonString(dataRequest));


		ResponseEntity<R> responseEntity = postRequest(url, dataRequest, header);

		// backup api
		try {
			backUpRequestResponse(prefix, requestId, dataRequest, responseEntity.getBody(), header);
		} catch (Exception e) {
			LOGGER.error("Api backup Failed: url - {}\n" +
					"requestId: {}\n" +
					"dataRequest: {}\n" +
					"response: {}", url, requestId, dataRequest, responseEntity.getBody());
		}

		return responseEntity;
	}

	public <Q, R> ResponseEntity<R> postRequest(String url, Q dataRequest) throws Exception {
		return restTemplateHelper.post(url,
				MediaType.APPLICATION_JSON_VALUE,
				buildHeader(jsonHelper.convertMap2JsonString(dataRequest)),
				new ArrayList<String>(), new HashMap<>(),
				configLoader.getProxyConfig(),
				dataRequest,
				new ParameterizedTypeReference<R>() {
				});
	}

	public <Q, R> ResponseEntity<R> postRequest(String url, Q dataRequest,
												Map<String, String> header) throws Exception {
		return restTemplateHelper.post(url,
				MediaType.APPLICATION_JSON_VALUE,
				header,
				new ArrayList<String>(), new HashMap<>(),
				configLoader.getProxyConfig(),
				dataRequest,
				new ParameterizedTypeReference<R>() {
				});
	}

	public boolean validateBvbSignature(ResponseEntity<?> responseEntity) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			HttpHeaders httpHeaders = responseEntity.getHeaders();

			String requestBodyString = mapper.writeValueAsString(responseEntity.getBody());

			LOGGER.info("header: " + httpHeaders);
			LOGGER.info("body: " + responseEntity.getBody());
			String signature = Objects.requireNonNull(httpHeaders.get("Signature")).get(0);
			InputStream readKey = new FileInputStream(env.getProperty("bvb.virtualAcct.bvbPublicKey"));
			boolean verifyResult = BVBEncryptUtil.bvbVerify(readKey, requestBodyString, signature);
			if (verifyResult) {
				LOGGER.info("Validate BVB signature successfully!");
			} else {
				LOGGER.error("Validate BVB signature failed!");
			}
			return verifyResult;
		} catch (Exception e) {
			LOGGER.error("Error when validate signature!");
			return false;
		}
	}

	public ConnResponse checkSignature(ResponseEntity<?> responseEntity) {
		if (!validateBvbSignature(responseEntity)) {
			LOGGER.error("Invalid signature from BVB !!!");
			ConnResponse response = new ConnResponse();
			transformErrorCode(
					response,
					BankConstants.BVBVirtualAcctErrorCode.WRONG_SIGNATURE.getPartnerCode(),
					BankConstants.BVBVirtualAcctErrorCode.WRONG_SIGNATURE.getDomainCode(),
					BankConstants.BVBVirtualAcctErrorCode.WRONG_SIGNATURE.getCode(),
					OneFinConstants.LANGUAGE.VIETNAMESE.getValue()
			);
			return response;
		} else {
			return null;
		}
	}

	public boolean validateBvbSignatureString(String requestBody, Map<String, String> httpHeaders) {
		try {

			LOGGER.info("header: " + httpHeaders);
			LOGGER.info("body: " + requestBody);
			String signature = Objects.requireNonNull(httpHeaders.get("signature"));
			InputStream readKey = new FileInputStream(env.getProperty("bvb.virtualAcct.bvbPublicKey"));
			boolean verifyResult = bvbEncryptUtil.bvbVerify(readKey, requestBody, signature);
			if (verifyResult) {
				LOGGER.info("Validate BVB signature successfully!");
			} else {
				LOGGER.error("Validate BVB signature failed!");
			}
			return verifyResult;
		} catch (Exception e) {
			LOGGER.error("Error when validate signature!", e);
			return false;
		}
	}


	public void transformErrorCode(ConnResponse data, String code, String lang) {
		PartnerErrorCode partnerCode = partnerErrorCodeRepo
				.findAllByPartnerAndDomainAndCode(
						OneFinConstants.PARTNER_BVBANK,
						OneFinConstants.VIRTUAL_ACCT, code);
		// LOGGER.info("transformErrorCode code: {} {} {}", OneFinConstants.PARTNER_VIETINBANK, OneFinConstants.VIRTUAL_ACCT, code);
		if (partnerCode == null) {
			LOGGER.warn("No error code found, please check the config file: {}", code);
		}
		data.setConnectorCode(partnerCode.getBaseErrorCode().getCode());
		if (lang.equals(OneFinConstants.LANGUAGE.VIETNAMESE.getValue())) {
			data.setMessage(partnerCode.getBaseErrorCode().getMessageVi());
		} else if (lang.equals(OneFinConstants.LANGUAGE.ENGLISH.getValue())) {
			data.setMessage(partnerCode.getBaseErrorCode().getMessageEn());
		} else {
			data.setMessage(partnerCode.getBaseErrorCode().getMessageEn());
		}
	}

	public void transformErrorCode(
			ConnResponse data,
			String domain,
			String code,
			String lang) {
		PartnerErrorCode partnerCode = partnerErrorCodeRepo
				.findAllByPartnerAndDomainAndCode(
						OneFinConstants.PARTNER_BVBANK,
						domain, code);
		// LOGGER.info("transformErrorCode code: {} {} {}", OneFinConstants.PARTNER_VIETINBANK, OneFinConstants.VIRTUAL_ACCT, code);
		if (partnerCode == null) {
			LOGGER.warn("No error code found, please check the config file: {}", code);
		}
		data.setConnectorCode(partnerCode.getBaseErrorCode().getCode());
		if (lang.equals(OneFinConstants.LANGUAGE.VIETNAMESE.getValue())) {
			data.setMessage(partnerCode.getBaseErrorCode().getMessageVi());
		} else if (lang.equals(OneFinConstants.LANGUAGE.ENGLISH.getValue())) {
			data.setMessage(partnerCode.getBaseErrorCode().getMessageEn());
		} else {
			data.setMessage(partnerCode.getBaseErrorCode().getMessageEn());
		}
	}

	public void transformErrorCode(
			ConnResponse data,
			String partner,
			String domain,
			String code,
			String lang) {
		PartnerErrorCode partnerCode = partnerErrorCodeRepo
				.findAllByPartnerAndDomainAndCode(
						partner, domain, code);
		// LOGGER.info("transformErrorCode code: {} {} {}", OneFinConstants.PARTNER_VIETINBANK, OneFinConstants.VIRTUAL_ACCT, code);
		if (partnerCode == null) {
			LOGGER.warn("No error code found, please check the config file: {}", code);
		}
		data.setConnectorCode(partnerCode.getBaseErrorCode().getCode());
		if (lang.equals(OneFinConstants.LANGUAGE.VIETNAMESE.getValue())) {
			data.setMessage(partnerCode.getBaseErrorCode().getMessageVi());
		} else if (lang.equals(OneFinConstants.LANGUAGE.ENGLISH.getValue())) {
			data.setMessage(partnerCode.getBaseErrorCode().getMessageEn());
		} else {
			data.setMessage(partnerCode.getBaseErrorCode().getMessageEn());
		}
	}

	public Date readDateString(String dateString) throws ParseException {
		SimpleDateFormat formatter
				= new SimpleDateFormat(BankConstants.BVB_DATE_FORMAT);
		return formatter.parse(dateString);
	}

	public Date readTransactionDateString(String dateString) throws ParseException {
		SimpleDateFormat formatter
				= new SimpleDateFormat(BankConstants.BVB_TRANSACTIONS_DATE_FORMAT);
		return formatter.parse(dateString);
	}

	public Date readBatchDateString(String dateString) throws ParseException {
		SimpleDateFormat formatter
				= new SimpleDateFormat(BankConstants.BVB_BATCH_DATE_FORMAT);
		return formatter.parse(dateString);
	}

	public void backUpRequestResponse(
			String requestId,
			Object request,
			Object response,
			Object header
	) throws Exception {
		if (request != null) {
			backupService.backup(configLoader.getBackupApiUriBvbVirtualAcct(), requestId, request,
					BankConstants.BACKUP_REQUEST);
		}
		if (response != null) {
			backupService.backup(configLoader.getBackupApiUriBvbVirtualAcct(), requestId, response,
					BankConstants.BACKUP_RESPONSE);
		}
		if (header != null) {
			backupService.backup(configLoader.getBackupApiUriBvbVirtualAcct(), requestId, header,
					BankConstants.BACKUP_HEADER);
		}
	}

	public void backUpRequestResponse(
			String prefix,
			String requestId,
			Object request,
			Object response,
			Object header
	) throws Exception {
		if (request != null) {
			backupService.backup(configLoader.getBackupApiUriBvbVirtualAcct(), requestId, request,
					prefix + "-" + BankConstants.BACKUP_REQUEST);
		}
		if (response != null) {
			backupService.backup(configLoader.getBackupApiUriBvbVirtualAcct(), requestId, response,
					prefix + "-" + BankConstants.BACKUP_RESPONSE);
		}
		if (header != null) {
			backupService.backup(configLoader.getBackupApiUriBvbVirtualAcct(), requestId, header,
					prefix + "-" + BankConstants.BACKUP_HEADER);
		}
	}


}
