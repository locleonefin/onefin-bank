package com.onefin.ewallet.bank.service.vcb;

import com.onefin.ewallet.bank.common.VcbConstants;
import com.onefin.ewallet.bank.common.VcbConstants.VCBEwalletApiOperation;
import com.onefin.ewallet.bank.config.BankHelper;
import com.onefin.ewallet.bank.dto.vcb.*;
import com.onefin.ewallet.bank.repository.jpa.LinkBankTransRepo;
import com.onefin.ewallet.bank.service.common.ConfigLoader;
import com.onefin.ewallet.bank.service.common.NumberSequenceService;
import com.onefin.ewallet.common.base.constants.OneFinConstants;
import com.onefin.ewallet.common.base.errorhandler.RuntimeBadRequestException;
import com.onefin.ewallet.common.base.errorhandler.RuntimeInternalServerException;
import com.onefin.ewallet.common.base.service.BackupService;
import com.onefin.ewallet.common.base.service.RestTemplateHelper;
import com.onefin.ewallet.common.domain.bank.common.LinkBankTransaction;
import com.onefin.ewallet.common.utility.date.DateTimeHelper;
import com.onefin.ewallet.common.utility.json.JSONHelper;
import com.onefin.ewallet.common.utility.sercurity.SercurityHelper;
import com.onefin.ewallet.common.utility.string.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.*;

@Service
public class LinkBankRequestUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(LinkBankRequestUtil.class);

	@Autowired
	private ConfigLoader configLoader;

	@Autowired
	protected RestTemplateHelper restTemplateHelper;

	@Autowired
	private DateTimeHelper dateTimeHelper;

	@Autowired
	private StringHelper stringHelper;

	@Autowired
	private JSONHelper jsonHelper;

	@Autowired
	private LinkBankMessageUtil linkBankMessageUtil;

	@Autowired
	private SercurityHelper sercurityHelper;

	@Autowired
	private LinkBankTransRepo transRepository;

	@Autowired
	private NumberSequenceService numberSequenceService;

	@Autowired
	private BankHelper bankHelper;

	@Autowired
	private LinkBankDto linkBankDto;

	@Autowired
	private BackupService backupService;

	/************************** Account *********************************/

	public Vcb2WalletAccountRequest sendVcb(Object data) {
		String url = configLoader.getVcbUrl();
		LOGGER.info("Send request to VCB {} - url: {}", data, url);
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.ALL));
		headers.setContentType(MediaType.APPLICATION_JSON);
		HashMap<String, String> headersMap = new HashMap<String, String>();
		for (String header : headers.keySet()) {
			headersMap.put(header, headers.getFirst(header));
		}
		HashMap<String, String> urlParameters = new HashMap<>();
		List<String> pathVariables = new ArrayList<String>();
		ResponseEntity<Vcb2WalletAccountRequest> responseEntity = restTemplateHelper.post(url, MediaType.APPLICATION_JSON_VALUE,
				headersMap, pathVariables, urlParameters, configLoader.getProxyConfig(), data,
				new ParameterizedTypeReference<Vcb2WalletAccountRequest>() {
				});
		LOGGER.info("Success receive response from VCB {}", responseEntity.getBody());
		return responseEntity.getBody();
	}

	public Vcb2EWalletResponse sendVcb2EwalletCore(VcbAccountDataRequest data) throws Exception {
		VCBEwalletApiOperation vcbEwalletApiOperation = VCBEwalletApiOperation.stream().filter(t ->
				t.getVcbAction().equals(data.getMessType())
		).findFirst().orElse(null);
		String url = vcbEwalletApiOperation != null ? configLoader.getVcb2OfBaseUrl() + vcbEwalletApiOperation.getVcb2OfUri() : null;
		LOGGER.info("Send {} request to core ewallet - url: {}", data, url);
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.ALL));
		headers.setContentType(MediaType.APPLICATION_JSON);
		HashMap<String, String> headersMap = new HashMap<String, String>();
		for (String header : headers.keySet()) {
			headersMap.put(header, headers.getFirst(header));
		}
		HashMap<String, String> urlParameters = new HashMap<>();
		List<String> pathVariables = new ArrayList<String>();
		ResponseEntity<Vcb2EWalletResponse> responseEntity = restTemplateHelper.post(url, MediaType.APPLICATION_JSON_VALUE,
				headersMap, pathVariables, urlParameters, null, data,
				new ParameterizedTypeReference<Vcb2EWalletResponse>() {
				});
		LOGGER.info("Success receive response from core ewallet - code {} - body {}", responseEntity.getBody().getCode(), responseEntity.getBody());
		return responseEntity.getBody();
	}

	/************************** Account *********************************/

	/************************** Card *********************************/

	public UserRegistrationCardResponse lookupUser(CardTokenIssue tokenIssue) throws Exception {
		LOGGER.info("Start send lookup user request: {}", tokenIssue);
		String url = configLoader.getVcbCardBaseUrl() + configLoader.getVcbCardLookupUser();

		List<String> pathVariables = new ArrayList<String>();
		pathVariables.add(tokenIssue.getWalletId());

		Map<String, String> signedHeaders = new LinkedHashMap<String, String>();
		signedHeaders.put("Content-Type", "application/json");
		signedHeaders.put("Accept", "application/json");
		signedHeaders.put("Accept-Language", "en");
		signedHeaders.put(VcbConstants.X_OP_DATE_CARD_HEADER, dateTimeHelper.currentDateString(OneFinConstants.HO_CHI_MINH_TIME_ZONE, OneFinConstants.DATE_FORMAT_yyyyMMddTHHmmssZ));
		signedHeaders.put(VcbConstants.X_OP_EXPIRES_CARD_HEADER, configLoader.getVcbCardRequestTimeout());

		String dataString = null;
		byte[] bd = null == dataString ? "".getBytes("UTF-8") : dataString.getBytes("UTF-8");

		LinkBankCardAuthorization auth = new LinkBankCardAuthorization(configLoader.getVcbCardAccessKeyId(), configLoader.getVcbCardSecretAccessKey(), configLoader.getVcbCardRegion(), configLoader.getVcbCardService(), "GET", stringHelper.fillPathVariable(configLoader.getVcbCardLookupUser(), pathVariables), new LinkedHashMap<String, String>(),
				signedHeaders, bd);

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.ALL));
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Accept-Language", "en");
		headers.add(VcbConstants.X_OP_AUTHORIZATION_CARD_HEADER, auth.toString());
		headers.add(VcbConstants.X_OP_DATE_CARD_HEADER, auth.getTimeStampString());
		headers.add(VcbConstants.X_OP_EXPIRES_CARD_HEADER, configLoader.getVcbCardRequestTimeout());
		LOGGER.info("X-OP-Authorization: {}", auth.toString());
		LOGGER.info("X-OP-Date: {}", auth.getTimeStampString());
		LOGGER.info("X-OP-Expires: {}", auth.getExpires());
		HashMap<String, String> headersMap = new HashMap<String, String>();
		for (String header : headers.keySet()) {
			headersMap.put(header, headers.getFirst(header));
		}
		HashMap<String, String> urlParameters = new HashMap<>();
		try {
			ResponseEntity<UserRegistrationCardResponse> responseEntity = restTemplateHelper.get(url,
					headersMap, pathVariables, urlParameters, configLoader.getProxyConfig(),
					new ParameterizedTypeReference<UserRegistrationCardResponse>() {
					});
			backupService.backup(configLoader.getBackupVcbLinkBank(), tokenIssue.getRequestId(), responseEntity.getBody(), VcbConstants.BACKUP_RESPONSE);
			LOGGER.info("Success receive response from VCB {}", responseEntity.getBody());
			Pair<String, String> ofErrorCode = linkBankMessageUtil.findMessageByErrorCode(responseEntity.getBody().getState(), tokenIssue.getLang(), VcbConstants.LINK_BANK_CARD);
			responseEntity.getBody().setCode(ofErrorCode.getFirst());
			responseEntity.getBody().setMessage(ofErrorCode.getSecond());
			responseEntity.getBody().setHttpCode(responseEntity.getStatusCode());
			LOGGER.info("End send create user request");
			return responseEntity.getBody();
		} catch (HttpStatusCodeException e) {
			LOGGER.info("Http status code exception: ", e);
			LOGGER.info("Http status code exception status: {}, body: {}", e.getStatusCode(), e.getResponseBodyAsString());
			UserRegistrationCardResponse response = (UserRegistrationCardResponse) jsonHelper.convertString2Map(e.getResponseBodyAsString(), UserRegistrationCardResponse.class);
			backupService.backup(configLoader.getBackupVcbLinkBank(), tokenIssue.getRequestId(), response, VcbConstants.BACKUP_RESPONSE);
			Pair<String, String> ofErrorCode = linkBankMessageUtil.findMessageByErrorCode(response.getName(), tokenIssue.getLang(), VcbConstants.LINK_BANK_CARD);
			response.setCode(ofErrorCode.getFirst());
			response.setMessage(ofErrorCode.getSecond());
			response.setHttpCode(e.getStatusCode());
			LOGGER.info("End send create user request");
			return response;
		}
	}

	public UserRegistrationCardResponse searchUser(CardTokenIssue tokenIssue, LinkBankTransaction trans) throws Exception {
		LOGGER.info("Start send search user request: {}", tokenIssue);
		String url = configLoader.getVcbCardBaseUrl() + configLoader.getVcbCardSearchUser();

		List<String> pathVariables = new ArrayList<String>();
		pathVariables.add(configLoader.getVcbCardUserGroupId());
		pathVariables.add(sercurityHelper.MD5Hashing(tokenIssue.getPhoneNo(), true));

		Map<String, String> signedHeaders = new LinkedHashMap<String, String>();
		signedHeaders.put("Content-Type", "application/json");
		signedHeaders.put("Accept", "application/json");
		signedHeaders.put("Accept-Language", "en");
		signedHeaders.put(VcbConstants.X_OP_DATE_CARD_HEADER, dateTimeHelper.currentDateString(OneFinConstants.HO_CHI_MINH_TIME_ZONE, OneFinConstants.DATE_FORMAT_yyyyMMddTHHmmssZ));
		signedHeaders.put(VcbConstants.X_OP_EXPIRES_CARD_HEADER, configLoader.getVcbCardRequestTimeout());

		String dataString = null;
		byte[] bd = null == dataString ? "".getBytes("UTF-8") : dataString.getBytes("UTF-8");

		LinkBankCardAuthorization auth = new LinkBankCardAuthorization(configLoader.getVcbCardAccessKeyId(), configLoader.getVcbCardSecretAccessKey(), configLoader.getVcbCardRegion(), configLoader.getVcbCardService(), "GET", stringHelper.fillPathVariable(configLoader.getVcbCardSearchUser(), pathVariables), new LinkedHashMap<String, String>(),
				signedHeaders, bd);

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.ALL));
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Accept-Language", "en");
		headers.add(VcbConstants.X_OP_AUTHORIZATION_CARD_HEADER, auth.toString());
		headers.add(VcbConstants.X_OP_DATE_CARD_HEADER, auth.getTimeStampString());
		headers.add(VcbConstants.X_OP_EXPIRES_CARD_HEADER, configLoader.getVcbCardRequestTimeout());
		LOGGER.info("X-OP-Authorization: {}", auth.toString());
		LOGGER.info("X-OP-Date: {}", auth.getTimeStampString());
		LOGGER.info("X-OP-Expires: {}", auth.getExpires());
		HashMap<String, String> headersMap = new HashMap<String, String>();
		for (String header : headers.keySet()) {
			headersMap.put(header, headers.getFirst(header));
		}
		HashMap<String, String> urlParameters = new HashMap<>();
		try {
			ResponseEntity<UserRegistrationCardResponse> responseEntity = restTemplateHelper.get(url,
					headersMap, pathVariables, urlParameters, configLoader.getProxyConfig(),
					new ParameterizedTypeReference<UserRegistrationCardResponse>() {
					});
			backupService.backup(configLoader.getBackupVcbLinkBank(), tokenIssue.getRequestId(), responseEntity.getBody(), VcbConstants.BACKUP_RESPONSE);
			LOGGER.info("Success receive response from VCB {}", responseEntity.getBody());
			Pair<String, String> ofErrorCode = linkBankMessageUtil.findMessageByErrorCode(responseEntity.getBody().getState(), tokenIssue.getLang(), VcbConstants.LINK_BANK_CARD);
			responseEntity.getBody().setCode(ofErrorCode.getFirst());
			responseEntity.getBody().setMessage(ofErrorCode.getSecond());
			responseEntity.getBody().setHttpCode(responseEntity.getStatusCode());
			trans.setCardUserId(responseEntity.getBody().getId());
			responseEntity.getBody().setRequestId(trans.getSsRequestId());
			LOGGER.info("End send search user request");
			return responseEntity.getBody();
		} catch (HttpStatusCodeException e) {
			LOGGER.info("Http status code exception: ", e);
			LOGGER.info("Http status code exception status: {}, body: {}", e.getStatusCode(), e.getResponseBodyAsString());
			UserRegistrationCardResponse response = (UserRegistrationCardResponse) jsonHelper.convertString2Map(e.getResponseBodyAsString(), UserRegistrationCardResponse.class);
			backupService.backup(configLoader.getBackupVcbLinkBank(), tokenIssue.getRequestId(), response, VcbConstants.BACKUP_RESPONSE);
			Pair<String, String> ofErrorCode = linkBankMessageUtil.findMessageByErrorCode(response.getName(), tokenIssue.getLang(), VcbConstants.LINK_BANK_CARD);
			response.setCode(ofErrorCode.getFirst());
			response.setMessage(ofErrorCode.getSecond());
			response.setHttpCode(e.getStatusCode());
			response.setRequestId(trans.getRequestId());
			LOGGER.info("End send search user request");
			return response;
		}
	}

	public UserRegistrationCardResponse createUser(CardTokenIssue tokenIssue, LinkBankTransaction trans) throws Exception {
		LOGGER.info("Start send create user request: {}", tokenIssue);
		String url = configLoader.getVcbCardBaseUrl() + configLoader.getVcbCardCreateUser();

		UserRegistrationCardRequest data = new UserRegistrationCardRequest();
		data.setGroup_id(configLoader.getVcbCardUserGroupId());
		data.setRef_id(sercurityHelper.MD5Hashing(tokenIssue.getPhoneNo(), true));
		String[] splitCardHolderName = tokenIssue.getCardHolderName().split("\\s+");
		data.setFirst_name(splitCardHolderName[splitCardHolderName.length - 1]);
		data.setLast_name(splitCardHolderName[0]);
		data.setMpin(configLoader.getVcbCardMpin());
		data.setMobile(tokenIssue.getPhoneNo());
		data.setEmail(configLoader.getVcbCardEmail());

		LOGGER.info("Request body {}", data);

		List<String> pathVariables = new ArrayList<String>();

		Map<String, String> signedHeaders = new LinkedHashMap<String, String>();
		signedHeaders.put("Content-Type", "application/json");
		signedHeaders.put("Accept", "application/json");
		signedHeaders.put("Accept-Language", "en");
		signedHeaders.put(VcbConstants.X_OP_DATE_CARD_HEADER, dateTimeHelper.currentDateString(OneFinConstants.HO_CHI_MINH_TIME_ZONE, OneFinConstants.DATE_FORMAT_yyyyMMddTHHmmssZ));
		signedHeaders.put(VcbConstants.X_OP_EXPIRES_CARD_HEADER, configLoader.getVcbCardRequestTimeout());

		String dataString = jsonHelper.convertMap2JsonString(data);
		byte[] bd = null == dataString ? "".getBytes("UTF-8") : dataString.getBytes("UTF-8");

		LinkBankCardAuthorization auth = new LinkBankCardAuthorization(configLoader.getVcbCardAccessKeyId(), configLoader.getVcbCardSecretAccessKey(), configLoader.getVcbCardRegion(), configLoader.getVcbCardService(), "POST", stringHelper.fillPathVariable(configLoader.getVcbCardCreateUser(), pathVariables), new LinkedHashMap<String, String>(),
				signedHeaders, bd);

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.ALL));
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Accept-Language", "en");
		headers.add(VcbConstants.X_OP_AUTHORIZATION_CARD_HEADER, auth.toString());
		headers.add(VcbConstants.X_OP_DATE_CARD_HEADER, auth.getTimeStampString());
		headers.add(VcbConstants.X_OP_EXPIRES_CARD_HEADER, configLoader.getVcbCardRequestTimeout());
		LOGGER.info("X-OP-Authorization: {}", auth.toString());
		LOGGER.info("X-OP-Date: {}", auth.getTimeStampString());
		LOGGER.info("X-OP-Expires: {}", auth.getExpires());
		HashMap<String, String> headersMap = new HashMap<String, String>();
		for (String header : headers.keySet()) {
			headersMap.put(header, headers.getFirst(header));
		}
		HashMap<String, String> urlParameters = new HashMap<>();
		try {
			ResponseEntity<UserRegistrationCardResponse> responseEntity = restTemplateHelper.post(url, MediaType.APPLICATION_JSON_VALUE,
					headersMap, pathVariables, urlParameters, configLoader.getProxyConfig(), data,
					new ParameterizedTypeReference<UserRegistrationCardResponse>() {
					});
			backupService.backup(configLoader.getBackupVcbLinkBank(), tokenIssue.getRequestId(), responseEntity.getBody(), VcbConstants.BACKUP_RESPONSE);
			LOGGER.info("Success receive response from VCB {}", responseEntity.getBody());
			Pair<String, String> ofErrorCode = linkBankMessageUtil.findMessageByErrorCode(responseEntity.getBody().getState(), tokenIssue.getLang(), VcbConstants.LINK_BANK_CARD);
			responseEntity.getBody().setCode(ofErrorCode.getFirst());
			responseEntity.getBody().setMessage(ofErrorCode.getSecond());
			responseEntity.getBody().setHttpCode(responseEntity.getStatusCode());
			trans.setCardUserId(responseEntity.getBody().getId());
			responseEntity.getBody().setRequestId(trans.getSsRequestId());
			LOGGER.info("End send create user request");
			return responseEntity.getBody();
		} catch (HttpStatusCodeException e) {
			LOGGER.info("Http status code exception: ", e);
			LOGGER.info("Http status code exception status: {}, body: {}", e.getStatusCode(), e.getResponseBodyAsString());
			UserRegistrationCardResponse response = (UserRegistrationCardResponse) jsonHelper.convertString2Map(e.getResponseBodyAsString(), UserRegistrationCardResponse.class);
			backupService.backup(configLoader.getBackupVcbLinkBank(), tokenIssue.getRequestId(), response, VcbConstants.BACKUP_RESPONSE);
			Pair<String, String> ofErrorCode = linkBankMessageUtil.findMessageByErrorCode(response.getName(), tokenIssue.getLang(), VcbConstants.LINK_BANK_CARD);
			response.setCode(ofErrorCode.getFirst());
			response.setMessage(ofErrorCode.getSecond());
			response.setHttpCode(e.getStatusCode());
			LOGGER.info("End send create user request");
			trans.setBankStatusCode(response.getName());
			trans.setTranStatus(VcbConstants.TRANS_ERROR);
			return response;
		}
	}

	public CreateInstrumentCardResponse createInstrument(CardTokenIssue tokenIssue, UserRegistrationCardResponse userRegistrationCardResponse, LinkBankTransaction trans) throws Exception {
		LOGGER.info("Start send create instrument request: {}, {}", tokenIssue, userRegistrationCardResponse);
		String url = configLoader.getVcbCardBaseUrl() + configLoader.getVcbCardCreateInstruments();

		CreateInstrumentCardRequest data = new CreateInstrumentCardRequest();
		data.setUser_id(userRegistrationCardResponse.getId());
		data.setType("card");
		data.setName(tokenIssue.getCardHolderName());
		data.setNumber(tokenIssue.getCardNumber());
		data.setMonth(tokenIssue.getCardIssueMonth());
		data.setYear(tokenIssue.getCardIssueYear());
		data.setBilling_address(userRegistrationCardResponse.getAddress());

		LOGGER.info("Request body {}", data);

		List<String> pathVariables = new ArrayList<String>();

		Map<String, String> signedHeaders = new LinkedHashMap<String, String>();
		signedHeaders.put("Content-Type", "application/json");
		signedHeaders.put("Accept", "application/json");
		signedHeaders.put("Accept-Language", "en");
		signedHeaders.put(VcbConstants.X_OP_DATE_CARD_HEADER, dateTimeHelper.currentDateString(OneFinConstants.HO_CHI_MINH_TIME_ZONE, OneFinConstants.DATE_FORMAT_yyyyMMddTHHmmssZ));
		signedHeaders.put(VcbConstants.X_OP_EXPIRES_CARD_HEADER, configLoader.getVcbCardRequestTimeout());

		String dataString = jsonHelper.convertMap2JsonString(data);
		byte[] bd = null == dataString ? "".getBytes("UTF-8") : dataString.getBytes("UTF-8");

		LinkBankCardAuthorization auth = new LinkBankCardAuthorization(configLoader.getVcbCardAccessKeyId(), configLoader.getVcbCardSecretAccessKey(), configLoader.getVcbCardRegion(), configLoader.getVcbCardService(), "POST", stringHelper.fillPathVariable(configLoader.getVcbCardCreateInstruments(), pathVariables), new LinkedHashMap<String, String>(),
				signedHeaders, bd);

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.ALL));
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Accept-Language", "en");
		headers.add(VcbConstants.X_OP_AUTHORIZATION_CARD_HEADER, auth.toString());
		headers.add(VcbConstants.X_OP_DATE_CARD_HEADER, auth.getTimeStampString());
		headers.add(VcbConstants.X_OP_EXPIRES_CARD_HEADER, configLoader.getVcbCardRequestTimeout());
		LOGGER.info("X-OP-Authorization: {}", auth.toString());
		LOGGER.info("X-OP-Date: {}", auth.getTimeStampString());
		LOGGER.info("X-OP-Expires: {}", auth.getExpires());
		HashMap<String, String> headersMap = new HashMap<String, String>();
		for (String header : headers.keySet()) {
			headersMap.put(header, headers.getFirst(header));
		}
		HashMap<String, String> urlParameters = new HashMap<>();
		try {
			ResponseEntity<CreateInstrumentCardResponse> responseEntity = restTemplateHelper.post(url, MediaType.APPLICATION_JSON_VALUE,
					headersMap, pathVariables, urlParameters, configLoader.getProxyConfig(), data,
					new ParameterizedTypeReference<CreateInstrumentCardResponse>() {
					});
			backupService.backup(configLoader.getBackupVcbLinkBank(), tokenIssue.getRequestId(), responseEntity.getBody(), VcbConstants.BACKUP_RESPONSE);
			LOGGER.info("Success receive response from VCB {}", responseEntity.getBody());
			Pair<String, String> ofErrorCode = linkBankMessageUtil.findMessageByErrorCode(responseEntity.getBody().getState(), tokenIssue.getLang(), VcbConstants.LINK_BANK_CARD);
			responseEntity.getBody().setCode(ofErrorCode.getFirst());
			responseEntity.getBody().setMessage(ofErrorCode.getSecond());
			responseEntity.getBody().setHttpCode(responseEntity.getStatusCode());
			trans.setCardInsId(responseEntity.getBody().getId());
			trans.setCardAuthorizationId(responseEntity.getBody().getAuthorization().getId());
			trans.setSendOtp(true);
			trans.setBankStatusCode(responseEntity.getBody().getState());
			trans.setTranStatus(VcbConstants.TRANS_PENDING);
			responseEntity.getBody().setRequestId(trans.getSsRequestId());
			LOGGER.info("End send create instrument request");
			return responseEntity.getBody();
		} catch (HttpStatusCodeException e) {
			LOGGER.info("Http status code exception: ", e);
			LOGGER.info("Http status code exception status: {}, body: {}", e.getStatusCode(), e.getResponseBodyAsString());
			CreateInstrumentCardResponse response = (CreateInstrumentCardResponse) jsonHelper.convertString2Map(e.getResponseBodyAsString(), CreateInstrumentCardResponse.class);
			backupService.backup(configLoader.getBackupVcbLinkBank(), tokenIssue.getRequestId(), response, VcbConstants.BACKUP_RESPONSE);
			Pair<String, String> ofErrorCode = linkBankMessageUtil.findMessageByErrorCode(response.getName(), tokenIssue.getLang(), VcbConstants.LINK_BANK_CARD);
			response.setCode(ofErrorCode.getFirst());
			response.setMessage(ofErrorCode.getSecond());
			response.setHttpCode(e.getStatusCode());
			trans.setBankStatusCode(response.getName());
			trans.setTranStatus(VcbConstants.TRANS_ERROR);
			LOGGER.info("End send create instrument request");
			return response;
		}
	}

	public CardInstrumentAuthorizationResponse instrumentAuthorization(String authorizationId, CardInstrumentPaymentAuthorizationRequest data, LinkBankTransaction trans) throws Exception {
		LOGGER.info("Start send instrument authorization request: {}, {}", authorizationId, data);
		String url = configLoader.getVcbCardBaseUrl() + configLoader.getVcbCardAuthorizeInstruments();
		LOGGER.info("Send request to VCB card: authorizationId {}, body {}, url: {}", authorizationId, data, url);

		List<String> pathVariables = new ArrayList<String>();
		pathVariables.add(authorizationId);

		Map<String, String> signedHeaders = new LinkedHashMap<String, String>();
		signedHeaders.put("Content-Type", "application/json");
		signedHeaders.put("Accept", "application/json");
		signedHeaders.put("Accept-Language", "en");
		signedHeaders.put(VcbConstants.X_OP_DATE_CARD_HEADER, dateTimeHelper.currentDateString(OneFinConstants.HO_CHI_MINH_TIME_ZONE, OneFinConstants.DATE_FORMAT_yyyyMMddTHHmmssZ));
		signedHeaders.put(VcbConstants.X_OP_EXPIRES_CARD_HEADER, configLoader.getVcbCardRequestTimeout());

		String dataString = jsonHelper.convertMap2JsonString(data);
		byte[] bd = null == dataString ? "".getBytes("UTF-8") : dataString.getBytes("UTF-8");

		LinkBankCardAuthorization auth = new LinkBankCardAuthorization(configLoader.getVcbCardAccessKeyId(), configLoader.getVcbCardSecretAccessKey(), configLoader.getVcbCardRegion(), configLoader.getVcbCardService(), "PATCH", stringHelper.fillPathVariable(configLoader.getVcbCardAuthorizeInstruments(), pathVariables), new LinkedHashMap<String, String>(),
				signedHeaders, bd);

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.ALL));
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Accept-Language", "en");
		headers.add(VcbConstants.X_OP_AUTHORIZATION_CARD_HEADER, auth.toString());
		headers.add(VcbConstants.X_OP_DATE_CARD_HEADER, auth.getTimeStampString());
		headers.add(VcbConstants.X_OP_EXPIRES_CARD_HEADER, configLoader.getVcbCardRequestTimeout());
		LOGGER.info("X-OP-Authorization: {}", auth.toString());
		LOGGER.info("X-OP-Date: {}", auth.getTimeStampString());
		LOGGER.info("X-OP-Expires: {}", auth.getExpires());
		HashMap<String, String> headersMap = new HashMap<String, String>();
		for (String header : headers.keySet()) {
			headersMap.put(header, headers.getFirst(header));
		}
		HashMap<String, String> urlParameters = new HashMap<>();
		try {
			ResponseEntity<CardInstrumentAuthorizationResponse> responseEntity = restTemplateHelper.patch(url, MediaType.APPLICATION_JSON_VALUE,
					headersMap, pathVariables, urlParameters, configLoader.getProxyConfig(), data,
					new ParameterizedTypeReference<CardInstrumentAuthorizationResponse>() {
					});
			backupService.backup(configLoader.getBackupVcbLinkBank(), null, responseEntity.getBody(), VcbConstants.BACKUP_RESPONSE);
			LOGGER.info("Success receive response from VCB {}", responseEntity.getBody());
			Pair<String, String> ofErrorCode = linkBankMessageUtil.findMessageByErrorCode(responseEntity.getBody().getState(), data.getLang(), VcbConstants.LINK_BANK_CARD);
			responseEntity.getBody().setCode(ofErrorCode.getFirst());
			responseEntity.getBody().setMessage(ofErrorCode.getSecond());
			responseEntity.getBody().setHttpCode(responseEntity.getStatusCode());
			trans.setTokenId(responseEntity.getBody().getInstrument().getToken().getId());
			trans.setCardInsId(responseEntity.getBody().getInstrument().getId());
			trans.setTokenNumber(responseEntity.getBody().getInstrument().getToken().getNumber());
			trans.setTokenIcvv(responseEntity.getBody().getInstrument().getToken().getIcvv());
			trans.setTokenExpireMonth(responseEntity.getBody().getInstrument().getToken().getExpire_month());
			trans.setTokenExpireYear(responseEntity.getBody().getInstrument().getToken().getExpire_year());
			trans.setBankStatusCode(responseEntity.getBody().getState());
			trans.setTokenState(OneFinConstants.TokenState.ACTIVE.getValue());
			trans.setTranStatus(VcbConstants.TRANS_SUCCESS);
			responseEntity.getBody().setRequestId(trans.getSsRequestId());
			LOGGER.info("End send instrument authorization request");
			return responseEntity.getBody();
		} catch (HttpStatusCodeException e) {
			LOGGER.info("Http status code exception: ", e);
			LOGGER.info("Http status code exception status: {}, body: {}", e.getStatusCode(), e.getResponseBodyAsString());
			CardInstrumentAuthorizationResponse response = (CardInstrumentAuthorizationResponse) jsonHelper.convertString2Map(e.getResponseBodyAsString(), CardInstrumentAuthorizationResponse.class);
			backupService.backup(configLoader.getBackupVcbLinkBank(), null, response, VcbConstants.BACKUP_RESPONSE);
			Pair<String, String> ofErrorCode = linkBankMessageUtil.findMessageByErrorCode(response.getName(), data.getLang(), VcbConstants.LINK_BANK_CARD);
			response.setCode(ofErrorCode.getFirst());
			response.setMessage(ofErrorCode.getSecond());
			response.setHttpCode(e.getStatusCode());
			trans.setBankStatusCode(response.getName());
			trans.setTranStatus(VcbConstants.TRANS_ERROR);
			LOGGER.info("End send instrument authorization request");
			return response;
		}
	}

	public ErrorResponse userDelete(String userId, String lang, LinkBankTransaction trans) throws Exception {
		LOGGER.info("Start send user delete request: {}", userId);
		String url = configLoader.getVcbCardBaseUrl() + configLoader.getVcbCardDeleteUser();

		List<String> pathVariables = new ArrayList<String>();
		pathVariables.add(userId);

		Map<String, String> signedHeaders = new LinkedHashMap<String, String>();
		signedHeaders.put("Content-Type", "application/json");
		signedHeaders.put("Accept", "application/json");
		signedHeaders.put("Accept-Language", "en");
		signedHeaders.put(VcbConstants.X_OP_DATE_CARD_HEADER, dateTimeHelper.currentDateString(OneFinConstants.HO_CHI_MINH_TIME_ZONE, OneFinConstants.DATE_FORMAT_yyyyMMddTHHmmssZ));
		signedHeaders.put(VcbConstants.X_OP_EXPIRES_CARD_HEADER, configLoader.getVcbCardRequestTimeout());

		String dataString = null;
		byte[] bd = null == dataString ? "".getBytes("UTF-8") : dataString.getBytes("UTF-8");

		LinkBankCardAuthorization auth = new LinkBankCardAuthorization(configLoader.getVcbCardAccessKeyId(), configLoader.getVcbCardSecretAccessKey(), configLoader.getVcbCardRegion(), configLoader.getVcbCardService(), "DELETE", stringHelper.fillPathVariable(configLoader.getVcbCardDeleteUser(), pathVariables), new LinkedHashMap<String, String>(),
				signedHeaders, bd);

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.ALL));
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Accept-Language", "en");
		headers.add(VcbConstants.X_OP_AUTHORIZATION_CARD_HEADER, auth.toString());
		headers.add(VcbConstants.X_OP_DATE_CARD_HEADER, auth.getTimeStampString());
		headers.add(VcbConstants.X_OP_EXPIRES_CARD_HEADER, configLoader.getVcbCardRequestTimeout());
		LOGGER.info("X-OP-Authorization: {}", auth.toString());
		LOGGER.info("X-OP-Date: {}", auth.getTimeStampString());
		LOGGER.info("X-OP-Expires: {}", auth.getExpires());
		HashMap<String, String> headersMap = new HashMap<String, String>();
		for (String header : headers.keySet()) {
			headersMap.put(header, headers.getFirst(header));
		}
		HashMap<String, String> urlParameters = new HashMap<>();
		try {
			ResponseEntity<ErrorResponse> responseEntity = restTemplateHelper.delete(url,
					headersMap, pathVariables, urlParameters, configLoader.getProxyConfig(),
					new ParameterizedTypeReference<ErrorResponse>() {
					});
			backupService.backup(configLoader.getBackupVcbLinkBank(), null, responseEntity.getBody(), VcbConstants.BACKUP_RESPONSE);
			LOGGER.info("Success receive response from VCB {}", responseEntity.getBody());
			Pair<String, String> ofErrorCode = linkBankMessageUtil.findMessageByErrorCode(VcbConstants.APPROVED_STATUS_CARD, lang, VcbConstants.LINK_BANK_CARD);
			responseEntity.getBody().setCode(ofErrorCode.getFirst());
			responseEntity.getBody().setMessage(ofErrorCode.getSecond());
			responseEntity.getBody().setHttpCode(responseEntity.getStatusCode());
			trans.setTokenState(OneFinConstants.TokenState.INACTIVE.getValue());
			LOGGER.info("End send user delete request");
			return responseEntity.getBody();
		} catch (HttpStatusCodeException e) {
			LOGGER.info("Http status code exception: ", e);
			LOGGER.info("Http status code exception status: {}, body: {}", e.getStatusCode(), e.getResponseBodyAsString());
			ErrorResponse response = (ErrorResponse) jsonHelper.convertString2Map(e.getResponseBodyAsString(), ErrorResponse.class);
			backupService.backup(configLoader.getBackupVcbLinkBank(), null, response, VcbConstants.BACKUP_RESPONSE);
			Pair<String, String> ofErrorCode = linkBankMessageUtil.findMessageByErrorCode(response.getName(), lang, VcbConstants.LINK_BANK_CARD);
			response.setCode(ofErrorCode.getFirst());
			response.setMessage(ofErrorCode.getSecond());
			response.setHttpCode(e.getStatusCode());
			trans.setBankStatusCode(response.getName());
			trans.setTranStatus(VcbConstants.TRANS_ERROR);
			LOGGER.info("End send user delete request");
			return response;
		}
	}

	public ErrorResponse instrumentDelete(String instrumentId, String lang, LinkBankTransaction trans) throws Exception {
		LOGGER.info("Start send instrument delete request: {}", instrumentId);
		String url = configLoader.getVcbCardBaseUrl() + configLoader.getVcbCardDeleteInstrument();

		List<String> pathVariables = new ArrayList<String>();
		pathVariables.add(instrumentId);

		Map<String, String> signedHeaders = new LinkedHashMap<String, String>();
		signedHeaders.put("Content-Type", "application/json");
		signedHeaders.put("Accept", "application/json");
		signedHeaders.put("Accept-Language", "en");
		signedHeaders.put(VcbConstants.X_OP_DATE_CARD_HEADER, dateTimeHelper.currentDateString(OneFinConstants.HO_CHI_MINH_TIME_ZONE, OneFinConstants.DATE_FORMAT_yyyyMMddTHHmmssZ));
		signedHeaders.put(VcbConstants.X_OP_EXPIRES_CARD_HEADER, configLoader.getVcbCardRequestTimeout());

		String dataString = null;
		byte[] bd = null == dataString ? "".getBytes("UTF-8") : dataString.getBytes("UTF-8");

		LinkBankCardAuthorization auth = new LinkBankCardAuthorization(configLoader.getVcbCardAccessKeyId(), configLoader.getVcbCardSecretAccessKey(), configLoader.getVcbCardRegion(), configLoader.getVcbCardService(), "DELETE", stringHelper.fillPathVariable(configLoader.getVcbCardDeleteInstrument(), pathVariables), new LinkedHashMap<String, String>(),
				signedHeaders, bd);

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.ALL));
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Accept-Language", "en");
		headers.add(VcbConstants.X_OP_AUTHORIZATION_CARD_HEADER, auth.toString());
		headers.add(VcbConstants.X_OP_DATE_CARD_HEADER, auth.getTimeStampString());
		headers.add(VcbConstants.X_OP_EXPIRES_CARD_HEADER, configLoader.getVcbCardRequestTimeout());
		LOGGER.info("X-OP-Authorization: {}", auth.toString());
		LOGGER.info("X-OP-Date: {}", auth.getTimeStampString());
		LOGGER.info("X-OP-Expires: {}", auth.getExpires());
		HashMap<String, String> headersMap = new HashMap<String, String>();
		for (String header : headers.keySet()) {
			headersMap.put(header, headers.getFirst(header));
		}
		HashMap<String, String> urlParameters = new HashMap<>();
		try {
			ResponseEntity<ErrorResponse> responseEntity = restTemplateHelper.delete(url,
					headersMap, pathVariables, urlParameters, configLoader.getProxyConfig(),
					new ParameterizedTypeReference<ErrorResponse>() {
					});
			backupService.backup(configLoader.getBackupVcbLinkBank(), null, responseEntity.getBody(), VcbConstants.BACKUP_RESPONSE);
			LOGGER.info("Success receive response from VCB {}", responseEntity.getBody());
			Pair<String, String> ofErrorCode = linkBankMessageUtil.findMessageByErrorCode(VcbConstants.APPROVED_STATUS_CARD, lang, VcbConstants.LINK_BANK_CARD);
			responseEntity.getBody().setCode(ofErrorCode.getFirst());
			responseEntity.getBody().setMessage(ofErrorCode.getSecond());
			responseEntity.getBody().setHttpCode(responseEntity.getStatusCode());
			trans.setTokenState(OneFinConstants.TokenState.INACTIVE.getValue());
			LOGGER.info("End send instrument delete request");
			return responseEntity.getBody();
		} catch (HttpStatusCodeException e) {
			LOGGER.info("Http status code exception: ", e);
			LOGGER.info("Http status code exception status: {}, body: {}", e.getStatusCode(), e.getResponseBodyAsString());
			ErrorResponse response = (ErrorResponse) jsonHelper.convertString2Map(e.getResponseBodyAsString(), ErrorResponse.class);
			backupService.backup(configLoader.getBackupVcbLinkBank(), null, response, VcbConstants.BACKUP_RESPONSE);
			Pair<String, String> ofErrorCode = linkBankMessageUtil.findMessageByErrorCode(response.getName(), lang, VcbConstants.LINK_BANK_CARD);
			response.setCode(ofErrorCode.getFirst());
			response.setMessage(ofErrorCode.getSecond());
			response.setHttpCode(e.getStatusCode());
			trans.setBankStatusCode(response.getName());
			trans.setTranStatus(VcbConstants.TRANS_ERROR);
			LOGGER.info("End send instrument delete request");
			return response;
		}
	}

	public CreatePaymentCardResponse createPayment(CreatePaymentCardWalletRequest data, LinkBankTransaction trans) throws Exception {
		LOGGER.info("Start send create payment request: {}", data);
		String url = configLoader.getVcbCardBaseUrl() + configLoader.getVcbCardCreatePayment();

		Optional<LinkBankTransaction> token = Optional.ofNullable(transRepository.findByBankAndTranStatusAndTokenStateAndTokenId(OneFinConstants.PARTNER_VCB, VcbConstants.TRANS_SUCCESS, OneFinConstants.TokenState.ACTIVE.getValue(), data.getTokenId()));
		return token.map(e -> {
			try {
				// TODO
				// Check token expire
				// 1. Expire => Unlink => user link again
				Date currDate = dateTimeHelper.currentDate(VcbConstants.HO_CHI_MINH_TIME_ZONE);
				Date tokenDate = dateTimeHelper.parseDate2(String.format("%s-%s", e.getTokenExpireYear(), e.getTokenExpireMonth()), VcbConstants.HO_CHI_MINH_TIME_ZONE, VcbConstants.DATE_FORMAT_yyyyMM);
				if (currDate.equals(tokenDate) || currDate.after(tokenDate)) {
					LOGGER.info("Token expired: {}", e);
					trans.setBankStatusCode(VcbConstants.TOKEN_EXPIRED);
					trans.setTranStatus(VcbConstants.TRANS_ERROR);
					ErrorResponse insDeleteRes = instrumentDelete(e.getCardInsId(), data.getLang(), e);
					linkBankDto.update(e);
					if (insDeleteRes.getHttpCode().is2xxSuccessful()) {
						CreatePaymentCardResponse response = (CreatePaymentCardResponse) bankHelper.createModelStructure(new CreatePaymentCardResponse());
						Pair<String, String> ofErrorCode = linkBankMessageUtil.findMessageByErrorCode(VcbConstants.TOKEN_EXPIRED, data.getLang(), VcbConstants.LINK_BANK_CARD);
						response.setCode(ofErrorCode.getFirst());
						response.setMessage(ofErrorCode.getSecond());
						response.setHttpCode(HttpStatus.NOT_FOUND);
						return response;
					} else {
						throw new RuntimeInternalServerException("Unlink unsuccessfully");
					}
				}

				String tokenNumber = e.getTokenNumber();
				int expireMonth = Integer.parseInt(e.getTokenExpireMonth());
				int expireYear = Integer.parseInt(e.getTokenExpireYear());
				String initCVV = e.getTokenIcvv();
				int sequenceNumber = Integer.parseInt(numberSequenceService.nextVietcomLinkBankCardTokenCvvTransId());
				String payTime = dateTimeHelper.currentDateString(VcbConstants.HO_CHI_MINH_TIME_ZONE, VcbConstants.DATE_FORMAT_yyyyMMddTHHmmssZ);

				LinkBankCardTokenCvvGenerator dcvv = new LinkBankCardTokenCvvGenerator();
				String tcvv = dcvv.generate(tokenNumber, expireMonth, expireYear, initCVV, sequenceNumber, payTime);
				int cvv = Integer.parseInt(tcvv);

				CreatePaymentCardRequest request = (CreatePaymentCardRequest) bankHelper.createModelStructure(new CreatePaymentCardRequest());
				request.getTerminal().setId(configLoader.getVcbCardTerminalId());
				request.setAmount(data.getAmount());
				request.setCurrency(VcbConstants.CURRENCY_VND);
				request.getToken().setNumber(tokenNumber);
				request.getToken().setExpire_month(expireMonth);
				request.getToken().setExpire_year(expireYear);
				request.getToken().setCvv(cvv);
				request.getToken().setPay_time(payTime);
				request.getToken().setSequence_number(sequenceNumber);
				String orderId = dateTimeHelper.currentDateString(VcbConstants.HO_CHI_MINH_TIME_ZONE, VcbConstants.DATE_FORMAT_yyyyMMDDHHmmss);
				request.getOrder().setId(String.format("%s%s", orderId, sequenceNumber));
				request.getOrder().setInformation(String.format("CashIn_%s%s", orderId, sequenceNumber));
				request.setMerchant_id(configLoader.getVcbCardMerchantId());
				request.setMerchant_txn_ref(trans.getRequestId());
				request.setUser_id(e.getCardUserId());
				request.setUser_mpin(configLoader.getVcbCardMpin());

				LOGGER.info("Request body {}", request);

				trans.setCurrency(request.getCurrency());

				List<String> pathVariables = new ArrayList<String>();

				Map<String, String> signedHeaders = new LinkedHashMap<String, String>();
				signedHeaders.put("Content-Type", "application/json");
				signedHeaders.put("Accept", "application/json");
				signedHeaders.put("Accept-Language", "en");
				signedHeaders.put(VcbConstants.X_OP_DATE_CARD_HEADER, dateTimeHelper.currentDateString(OneFinConstants.HO_CHI_MINH_TIME_ZONE, OneFinConstants.DATE_FORMAT_yyyyMMddTHHmmssZ));
				signedHeaders.put(VcbConstants.X_OP_EXPIRES_CARD_HEADER, configLoader.getVcbCardRequestTimeout());

				String dataString = jsonHelper.convertMap2JsonString(request);
				byte[] bd = null == dataString ? "".getBytes("UTF-8") : dataString.getBytes("UTF-8");

				LinkBankCardAuthorization auth = new LinkBankCardAuthorization(configLoader.getVcbCardAccessKeyId(), configLoader.getVcbCardSecretAccessKey(), configLoader.getVcbCardRegion(), configLoader.getVcbCardService(), "POST", stringHelper.fillPathVariable(configLoader.getVcbCardCreatePayment(), pathVariables), new LinkedHashMap<String, String>(),
						signedHeaders, bd);

				HttpHeaders headers = new HttpHeaders();
				headers.setAccept(Collections.singletonList(MediaType.ALL));
				headers.setContentType(MediaType.APPLICATION_JSON);
				headers.add("Accept-Language", "en");
				headers.add(VcbConstants.X_OP_AUTHORIZATION_CARD_HEADER, auth.toString());
				headers.add(VcbConstants.X_OP_DATE_CARD_HEADER, auth.getTimeStampString());
				headers.add(VcbConstants.X_OP_EXPIRES_CARD_HEADER, configLoader.getVcbCardRequestTimeout());
				LOGGER.info("X-OP-Authorization: {}", auth.toString());
				LOGGER.info("X-OP-Date: {}", auth.getTimeStampString());
				LOGGER.info("X-OP-Expires: {}", auth.getExpires());
				HashMap<String, String> headersMap = new HashMap<String, String>();
				for (String header : headers.keySet()) {
					headersMap.put(header, headers.getFirst(header));
				}
				HashMap<String, String> urlParameters = new HashMap<>();

				try {
					ResponseEntity<CreatePaymentCardResponse> responseEntity = restTemplateHelper.post(url, MediaType.APPLICATION_JSON_VALUE,
							headersMap, pathVariables, urlParameters, configLoader.getProxyConfig(), request,
							new ParameterizedTypeReference<CreatePaymentCardResponse>() {
							});
					backupService.backup(configLoader.getBackupVcbLinkBank(), data.getRequestId(), responseEntity.getBody(), VcbConstants.BACKUP_RESPONSE);
					LOGGER.info("Success receive response from VCB {}", responseEntity.getBody());
					Pair<String, String> ofErrorCode = linkBankMessageUtil.findMessageByErrorCode(responseEntity.getBody().getState(), data.getLang(), VcbConstants.LINK_BANK_CARD);
					responseEntity.getBody().setCode(ofErrorCode.getFirst());
					responseEntity.getBody().setMessage(ofErrorCode.getSecond());
					responseEntity.getBody().setHttpCode(responseEntity.getStatusCode());
					responseEntity.getBody().setRequestId(trans.getSsRequestId());
					responseEntity.getBody().setTokenId(e.getTokenId());
					if (responseEntity.getBody().getState().equals(VcbConstants.AUTHORIZATION_REQUIRED_STATUS_CARD)) {
						trans.setSendOtp(true);
						trans.setTranStatus(VcbConstants.TRANS_PENDING);
						trans.setCardAuthorizationId(responseEntity.getBody().getAuthorization().getId());
					} else if (responseEntity.getBody().getState().equals(VcbConstants.APPROVED_STATUS_CARD)) {
						trans.setTranStatus(VcbConstants.TRANS_SUCCESS);
					} else {
						trans.setTranStatus(VcbConstants.TRANS_ERROR);
					}
					trans.setBankStatusCode(responseEntity.getBody().getState());
					trans.setBankTransactionId(responseEntity.getBody().getId());
					LOGGER.info("End send create instrument request");
					return responseEntity.getBody();
				} catch (HttpStatusCodeException ex1) {
					LOGGER.info("Http status code exception: ", e);
					LOGGER.info("Http status code exception status: {}, body: {}", e.getBankStatusCode(), ex1.getResponseBodyAsString());
					CreatePaymentCardResponse response = (CreatePaymentCardResponse) jsonHelper.convertString2Map(ex1.getResponseBodyAsString(), CreatePaymentCardResponse.class);
					backupService.backup(configLoader.getBackupVcbLinkBank(), data.getRequestId(), response, VcbConstants.BACKUP_RESPONSE);
					Pair<String, String> ofErrorCode = linkBankMessageUtil.findMessageByErrorCode(response.getName(), data.getLang(), VcbConstants.LINK_BANK_CARD);
					response.setCode(ofErrorCode.getFirst());
					response.setMessage(ofErrorCode.getSecond());
					response.setHttpCode(ex1.getStatusCode());
					trans.setBankStatusCode(response.getName());
					trans.setTranStatus(VcbConstants.TRANS_ERROR);
					LOGGER.info("End send create instrument request");
					return response;
				}
			} catch (Exception ex) {
				LOGGER.error(ex.getMessage(), ex);
			}
			return null;
		}).orElseThrow(() -> new RuntimeBadRequestException("Token not found"));
	}

	public CardPaymentAuthorizationResponse paymentAuthorization(String authorizationId, CardInstrumentPaymentAuthorizationRequest data, LinkBankTransaction trans) throws Exception {
		LOGGER.info("Start send payment authorization request: {}, {}", authorizationId, data);
		String url = configLoader.getVcbCardBaseUrl() + configLoader.getVcbCardAuthorizePayment();
		LOGGER.info("Send request to VCB card: authorizationId {}, body {}, url: {}", authorizationId, data, url);

		List<String> pathVariables = new ArrayList<String>();
		pathVariables.add(authorizationId);

		Map<String, String> signedHeaders = new LinkedHashMap<String, String>();
		signedHeaders.put("Content-Type", "application/json");
		signedHeaders.put("Accept", "application/json");
		signedHeaders.put("Accept-Language", "en");
		signedHeaders.put(VcbConstants.X_OP_DATE_CARD_HEADER, dateTimeHelper.currentDateString(OneFinConstants.HO_CHI_MINH_TIME_ZONE, OneFinConstants.DATE_FORMAT_yyyyMMddTHHmmssZ));
		signedHeaders.put(VcbConstants.X_OP_EXPIRES_CARD_HEADER, configLoader.getVcbCardRequestTimeout());

		String dataString = jsonHelper.convertMap2JsonString(data);
		byte[] bd = null == dataString ? "".getBytes("UTF-8") : dataString.getBytes("UTF-8");

		LinkBankCardAuthorization auth = new LinkBankCardAuthorization(configLoader.getVcbCardAccessKeyId(), configLoader.getVcbCardSecretAccessKey(), configLoader.getVcbCardRegion(), configLoader.getVcbCardService(), "PATCH", stringHelper.fillPathVariable(configLoader.getVcbCardAuthorizePayment(), pathVariables), new LinkedHashMap<String, String>(),
				signedHeaders, bd);

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.ALL));
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Accept-Language", "en");
		headers.add(VcbConstants.X_OP_AUTHORIZATION_CARD_HEADER, auth.toString());
		headers.add(VcbConstants.X_OP_DATE_CARD_HEADER, auth.getTimeStampString());
		headers.add(VcbConstants.X_OP_EXPIRES_CARD_HEADER, configLoader.getVcbCardRequestTimeout());
		LOGGER.info("X-OP-Authorization: {}", auth.toString());
		LOGGER.info("X-OP-Date: {}", auth.getTimeStampString());
		LOGGER.info("X-OP-Expires: {}", auth.getExpires());
		HashMap<String, String> headersMap = new HashMap<String, String>();
		for (String header : headers.keySet()) {
			headersMap.put(header, headers.getFirst(header));
		}
		HashMap<String, String> urlParameters = new HashMap<>();
		try {
			ResponseEntity<CardPaymentAuthorizationResponse> responseEntity = restTemplateHelper.patch(url, MediaType.APPLICATION_JSON_VALUE,
					headersMap, pathVariables, urlParameters, configLoader.getProxyConfig(), data,
					new ParameterizedTypeReference<CardPaymentAuthorizationResponse>() {
					});
			backupService.backup(configLoader.getBackupVcbLinkBank(), null, responseEntity.getBody(), VcbConstants.BACKUP_RESPONSE);
			LOGGER.info("Success receive response from VCB {}", responseEntity.getBody());
			Pair<String, String> ofErrorCode = linkBankMessageUtil.findMessageByErrorCode(responseEntity.getBody().getState(), data.getLang(), VcbConstants.LINK_BANK_CARD);
			responseEntity.getBody().setCode(ofErrorCode.getFirst());
			responseEntity.getBody().setMessage(ofErrorCode.getSecond());
			responseEntity.getBody().setHttpCode(responseEntity.getStatusCode());
			responseEntity.getBody().setRequestId(trans.getSsRequestId());
			if (responseEntity.getBody().getState().equals(VcbConstants.APPROVED_STATUS_CARD)) {
				trans.setTranStatus(VcbConstants.TRANS_SUCCESS);
			} else {
				trans.setTranStatus(VcbConstants.TRANS_ERROR);
			}
			LOGGER.info("End send instrument authorization request");
			return responseEntity.getBody();
		} catch (HttpStatusCodeException e) {
			LOGGER.info("Http status code exception: ", e);
			LOGGER.info("Http status code exception status: {}, body: {}", e.getStatusCode(), e.getResponseBodyAsString());
			CardPaymentAuthorizationResponse response = (CardPaymentAuthorizationResponse) jsonHelper.convertString2Map(e.getResponseBodyAsString(), CardPaymentAuthorizationResponse.class);
			backupService.backup(configLoader.getBackupVcbLinkBank(), null, response, VcbConstants.BACKUP_RESPONSE);
			Pair<String, String> ofErrorCode = linkBankMessageUtil.findMessageByErrorCode(response.getName(), data.getLang(), VcbConstants.LINK_BANK_CARD);
			response.setCode(ofErrorCode.getFirst());
			response.setMessage(ofErrorCode.getSecond());
			response.setHttpCode(e.getStatusCode());
			trans.setBankStatusCode(response.getName());
			trans.setTranStatus(VcbConstants.TRANS_ERROR);
			LOGGER.info("End send instrument authorization request");
			return response;
		}
	}

	public CreatePaymentCardResponse paymentWithdrawSearch(CheckTransStatusWalletRequest data, LinkBankTransaction trans) throws Exception {
		LOGGER.info("Start send payment search request: {}, {}", data, trans);
		String url = configLoader.getVcbCardBaseUrl() + configLoader.getVcbCardSearchPayment();

		List<String> pathVariables = new ArrayList<String>();
		pathVariables.add(configLoader.getVcbCardUserGroupId());
		pathVariables.add(configLoader.getVcbCardMerchantId());
		pathVariables.add(trans.getMerchantRefId());

		Map<String, String> signedHeaders = new LinkedHashMap<String, String>();
		signedHeaders.put("Content-Type", "application/json");
		signedHeaders.put("Accept", "application/json");
		signedHeaders.put("Accept-Language", "en");
		signedHeaders.put(VcbConstants.X_OP_DATE_CARD_HEADER, dateTimeHelper.currentDateString(OneFinConstants.HO_CHI_MINH_TIME_ZONE, OneFinConstants.DATE_FORMAT_yyyyMMddTHHmmssZ));
		signedHeaders.put(VcbConstants.X_OP_EXPIRES_CARD_HEADER, configLoader.getVcbCardRequestTimeout());

		String dataString = null;
		byte[] bd = null == dataString ? "".getBytes("UTF-8") : dataString.getBytes("UTF-8");

		LinkBankCardAuthorization auth = new LinkBankCardAuthorization(configLoader.getVcbCardAccessKeyId(), configLoader.getVcbCardSecretAccessKey(), configLoader.getVcbCardRegion(), configLoader.getVcbCardService(), "GET", stringHelper.fillPathVariable(configLoader.getVcbCardSearchPayment(), pathVariables), new LinkedHashMap<String, String>(),
				signedHeaders, bd);

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.ALL));
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Accept-Language", "en");
		headers.add(VcbConstants.X_OP_AUTHORIZATION_CARD_HEADER, auth.toString());
		headers.add(VcbConstants.X_OP_DATE_CARD_HEADER, auth.getTimeStampString());
		headers.add(VcbConstants.X_OP_EXPIRES_CARD_HEADER, configLoader.getVcbCardRequestTimeout());
		LOGGER.info("X-OP-Authorization: {}", auth.toString());
		LOGGER.info("X-OP-Date: {}", auth.getTimeStampString());
		LOGGER.info("X-OP-Expires: {}", auth.getExpires());
		HashMap<String, String> headersMap = new HashMap<String, String>();
		for (String header : headers.keySet()) {
			headersMap.put(header, headers.getFirst(header));
		}
		HashMap<String, String> urlParameters = new HashMap<>();
		try {
			ResponseEntity<CreatePaymentCardResponse> responseEntity = restTemplateHelper.get(url,
					headersMap, pathVariables, urlParameters, configLoader.getProxyConfig(),
					new ParameterizedTypeReference<CreatePaymentCardResponse>() {
					});
			backupService.backup(configLoader.getBackupVcbLinkBank(), null, responseEntity.getBody(), VcbConstants.BACKUP_RESPONSE);
			LOGGER.info("Success receive response from VCB {}", responseEntity.getBody());
			Pair<String, String> ofErrorCode = linkBankMessageUtil.findMessageByErrorCode(responseEntity.getBody().getState(), data.getLang(), VcbConstants.LINK_BANK_CARD);
			responseEntity.getBody().setCode(ofErrorCode.getFirst());
			responseEntity.getBody().setMessage(ofErrorCode.getSecond());
			responseEntity.getBody().setHttpCode(responseEntity.getStatusCode());
			responseEntity.getBody().setRequestId(trans.getSsRequestId());
			responseEntity.getBody().setTokenId(trans.getTokenId());
			if (responseEntity.getBody().getState().equals(VcbConstants.APPROVED_STATUS_CARD)) {
				trans.setTranStatus(VcbConstants.TRANS_SUCCESS);
			} else {
				trans.setTranStatus(VcbConstants.TRANS_ERROR);
			}
			LOGGER.info("End send payment search request");
			return responseEntity.getBody();
		} catch (HttpStatusCodeException e) {
			LOGGER.info("Http status code exception: ", e);
			LOGGER.info("Http status code exception status: {}, body: {}", e.getStatusCode(), e.getResponseBodyAsString());
			CreatePaymentCardResponse response = (CreatePaymentCardResponse) jsonHelper.convertString2Map(e.getResponseBodyAsString(), CreatePaymentCardResponse.class);
			backupService.backup(configLoader.getBackupVcbLinkBank(), null, response, VcbConstants.BACKUP_RESPONSE);
			Pair<String, String> ofErrorCode = linkBankMessageUtil.findMessageByErrorCode(response.getName(), data.getLang(), VcbConstants.LINK_BANK_CARD);
			response.setCode(ofErrorCode.getFirst());
			response.setMessage(ofErrorCode.getSecond());
			response.setHttpCode(e.getStatusCode());
			trans.setBankStatusCode(response.getName());
			trans.setTranStatus(VcbConstants.TRANS_ERROR);
			LOGGER.info("End send payment search request");
			return response;
		}
	}

	public CreateWithdrawCardResponse withdraw(CreateWithdrawCardWalletRequest data, LinkBankTransaction trans) throws Exception {
		LOGGER.info("Start send create withdraw request: {}", data);
		String url = configLoader.getVcbCardBaseUrl() + configLoader.getVcbCardWithdraw();

		Optional<LinkBankTransaction> token = Optional.ofNullable(transRepository.findByBankAndTranStatusAndTokenStateAndTokenId(OneFinConstants.PARTNER_VCB, VcbConstants.TRANS_SUCCESS, OneFinConstants.TokenState.ACTIVE.getValue(), data.getTokenId()));
		return token.map(e -> {
			try {
				// TODO
				// Check token expire
				// 1. Expire => Unlink => user link again
				Date currDate = dateTimeHelper.currentDate(VcbConstants.HO_CHI_MINH_TIME_ZONE);
				Date tokenDate = dateTimeHelper.parseDate2(String.format("%s-%s", e.getTokenExpireYear(), e.getTokenExpireMonth()), VcbConstants.HO_CHI_MINH_TIME_ZONE, VcbConstants.DATE_FORMAT_yyyyMM);
				if (currDate.equals(tokenDate) || currDate.after(tokenDate)) {
					LOGGER.info("Token expired: {}", e);
					trans.setBankStatusCode(VcbConstants.TOKEN_EXPIRED);
					trans.setTranStatus(VcbConstants.TRANS_ERROR);
					ErrorResponse insDeleteRes = instrumentDelete(e.getCardInsId(), data.getLang(), e);
					linkBankDto.update(e);
					if (insDeleteRes.getHttpCode().is2xxSuccessful()) {
						CreateWithdrawCardResponse response = (CreateWithdrawCardResponse) bankHelper.createModelStructure(new CreateWithdrawCardResponse());
						Pair<String, String> ofErrorCode = linkBankMessageUtil.findMessageByErrorCode(VcbConstants.TOKEN_EXPIRED, data.getLang(), VcbConstants.LINK_BANK_CARD);
						response.setCode(ofErrorCode.getFirst());
						response.setMessage(ofErrorCode.getSecond());
						response.setHttpCode(HttpStatus.NOT_FOUND);
						return response;
					} else {
						throw new RuntimeInternalServerException("Unlink unsuccessfully");
					}
				}

				String tokenNumber = e.getTokenNumber();
				int expireMonth = Integer.parseInt(e.getTokenExpireMonth());
				int expireYear = Integer.parseInt(e.getTokenExpireYear());
				String initCVV = e.getTokenIcvv();
				int sequenceNumber = Integer.parseInt(numberSequenceService.nextVietcomLinkBankCardTokenCvvTransId());
				String payTime = dateTimeHelper.currentDateString(VcbConstants.HO_CHI_MINH_TIME_ZONE, VcbConstants.DATE_FORMAT_yyyyMMddTHHmmssZ);

				LinkBankCardTokenCvvGenerator dcvv = new LinkBankCardTokenCvvGenerator();
				String tcvv = dcvv.generate(tokenNumber, expireMonth, expireYear, initCVV, sequenceNumber, payTime);
				int cvv = Integer.parseInt(tcvv);

				CreateWithdrawCardRequest request = (CreateWithdrawCardRequest) bankHelper.createModelStructure(new CreateWithdrawCardRequest());
				request.getTerminal().setId(configLoader.getVcbCardTerminalId());
				request.setAmount(data.getAmount());
				request.setCurrency(VcbConstants.CURRENCY_VND);
				request.getToken().setNumber(tokenNumber);
				request.getToken().setExpire_month(expireMonth);
				request.getToken().setExpire_year(expireYear);
				request.getToken().setCvv(cvv);
				request.getToken().setPay_time(payTime);
				request.getToken().setSequence_number(sequenceNumber);
				String orderId = dateTimeHelper.currentDateString(VcbConstants.HO_CHI_MINH_TIME_ZONE, VcbConstants.DATE_FORMAT_yyyyMMDDHHmmss);
				request.setOrder_info(String.format("Cashout %s%s", orderId, sequenceNumber));
				request.setInvoice_ref(String.format("DEPOSIT_%s%s", orderId, sequenceNumber));
				request.setMerchant_id(configLoader.getVcbCardMerchantId());
				request.setMerchant_txn_ref(trans.getRequestId());
				request.setUser_id(e.getCardUserId());
				request.setUser_mpin(configLoader.getVcbCardMpin());

				LOGGER.info("Request body {}", request);

				trans.setCurrency(request.getCurrency());

				List<String> pathVariables = new ArrayList<String>();

				Map<String, String> signedHeaders = new LinkedHashMap<String, String>();
				signedHeaders.put("Content-Type", "application/json");
				signedHeaders.put("Accept", "application/json");
				signedHeaders.put("Accept-Language", "en");
				signedHeaders.put(VcbConstants.X_OP_DATE_CARD_HEADER, dateTimeHelper.currentDateString(OneFinConstants.HO_CHI_MINH_TIME_ZONE, OneFinConstants.DATE_FORMAT_yyyyMMddTHHmmssZ));
				signedHeaders.put(VcbConstants.X_OP_EXPIRES_CARD_HEADER, configLoader.getVcbCardRequestTimeout());

				String dataString = jsonHelper.convertMap2JsonString(request);
				byte[] bd = null == dataString ? "".getBytes("UTF-8") : dataString.getBytes("UTF-8");

				LinkBankCardAuthorization auth = new LinkBankCardAuthorization(configLoader.getVcbCardAccessKeyId(), configLoader.getVcbCardSecretAccessKey(), configLoader.getVcbCardRegion(), configLoader.getVcbCardService(), "POST", stringHelper.fillPathVariable(configLoader.getVcbCardWithdraw(), pathVariables), new LinkedHashMap<String, String>(),
						signedHeaders, bd);

				HttpHeaders headers = new HttpHeaders();
				headers.setAccept(Collections.singletonList(MediaType.ALL));
				headers.setContentType(MediaType.APPLICATION_JSON);
				headers.add("Accept-Language", "en");
				headers.add(VcbConstants.X_OP_AUTHORIZATION_CARD_HEADER, auth.toString());
				headers.add(VcbConstants.X_OP_DATE_CARD_HEADER, auth.getTimeStampString());
				headers.add(VcbConstants.X_OP_EXPIRES_CARD_HEADER, configLoader.getVcbCardRequestTimeout());
				LOGGER.info("X-OP-Authorization: {}", auth.toString());
				LOGGER.info("X-OP-Date: {}", auth.getTimeStampString());
				LOGGER.info("X-OP-Expires: {}", auth.getExpires());
				HashMap<String, String> headersMap = new HashMap<String, String>();
				for (String header : headers.keySet()) {
					headersMap.put(header, headers.getFirst(header));
				}
				HashMap<String, String> urlParameters = new HashMap<>();

				try {
					ResponseEntity<CreateWithdrawCardResponse> responseEntity = restTemplateHelper.post(url, MediaType.APPLICATION_JSON_VALUE,
							headersMap, pathVariables, urlParameters, configLoader.getProxyConfig(), request,
							new ParameterizedTypeReference<CreateWithdrawCardResponse>() {
							});
					backupService.backup(configLoader.getBackupVcbLinkBank(), data.getRequestId(), responseEntity.getBody(), VcbConstants.BACKUP_RESPONSE);
					LOGGER.info("Success receive response from VCB {}", responseEntity.getBody());
					Pair<String, String> ofErrorCode = linkBankMessageUtil.findMessageByErrorCode(responseEntity.getBody().getState(), data.getLang(), VcbConstants.LINK_BANK_CARD);
					responseEntity.getBody().setCode(ofErrorCode.getFirst());
					responseEntity.getBody().setMessage(ofErrorCode.getSecond());
					responseEntity.getBody().setHttpCode(responseEntity.getStatusCode());
					responseEntity.getBody().setRequestId(trans.getSsRequestId());
					responseEntity.getBody().setTokenId(e.getTokenId());
					if (responseEntity.getBody().getState().equals(VcbConstants.APPROVED_STATUS_CARD)) {
						trans.setTranStatus(VcbConstants.TRANS_SUCCESS);
					} else {
						trans.setTranStatus(VcbConstants.TRANS_ERROR);
					}
					trans.setBankStatusCode(responseEntity.getBody().getState());
					trans.setBankTransactionId(responseEntity.getBody().getId());
					LOGGER.info("End send create withdraw request");
					return responseEntity.getBody();
				} catch (HttpStatusCodeException ex1) {
					LOGGER.info("Http status code exception: ", e);
					LOGGER.info("Http status code exception status: {}, body: {}", e.getBankStatusCode(), ex1.getResponseBodyAsString());
					CreateWithdrawCardResponse response = (CreateWithdrawCardResponse) jsonHelper.convertString2Map(ex1.getResponseBodyAsString(), CreateWithdrawCardResponse.class);
					backupService.backup(configLoader.getBackupVcbLinkBank(), data.getRequestId(), response, VcbConstants.BACKUP_RESPONSE);
					Pair<String, String> ofErrorCode = linkBankMessageUtil.findMessageByErrorCode(response.getName(), data.getLang(), VcbConstants.LINK_BANK_CARD);
					response.setCode(ofErrorCode.getFirst());
					response.setMessage(ofErrorCode.getSecond());
					response.setHttpCode(ex1.getStatusCode());
					trans.setBankStatusCode(response.getName());
					trans.setTranStatus(VcbConstants.TRANS_ERROR);
					LOGGER.info("End send create withdraw request");
					return response;
				}
			} catch (Exception ex) {
				LOGGER.error(ex.getMessage(), ex);
			}
			return null;
		}).orElseThrow(RuntimeInternalServerException::new);
	}

	/************************** Card *********************************/

}
