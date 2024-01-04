package com.onefin.ewallet.bank.service.vcb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by locdt on 4/17/16
 */
public class LinkBankCardAuthorization {

	private static final Logger LOGGER = LoggerFactory.getLogger(LinkBankCardAuthorization.class);

	public static final String EMPTY_BODY_SHA256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
	public static final String UNSIGNED_PAYLOAD = "UNSIGNED-PAYLOAD";
	public static final String X_OP_AUTHORIZATION_HEADER = "X-OP-Authorization";
	public static final String X_OP_DATE_HEADER = "X-OP-Date";
	public static final String X_OP_EXPIRES_HEADER = "X-OP-Expires";
	public static final String SCHEME = "OWS1";
	public static final String ALGORITHM = "OWS1-HMAC-SHA256";
	public static final String TERMINATOR = "ows1_request";
	public static final DateFormat yyyyMMdd = new SimpleDateFormat("yyyyMMdd");
	public static final DateFormat yyyyMMddTHHmmssZ = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");

	static {
		yyyyMMdd.setTimeZone(TimeZone.getTimeZone("UTC"));
		yyyyMMddTHHmmssZ.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	private String httpMethod;
	private String uri;
	private SortedMap<String, String> queryParameters = new TreeMap<String, String>(); //Sorted Map
	private String algorithm;
	private String credential;
	private String region;
	private String service;
	private String terminator;
	private SortedMap<String, String> signedHeaders = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER); //Case Insensitive Sorted Map
	private String signedHeaderNames;
	private String signature;
	private String accessKeyId;
	private String secretAccessKey;
	private int expires;
	private Date timeStamp;
	private byte[] payload;
	private Map<String, String> debugInfo = new LinkedHashMap<String, String>();

	public LinkBankCardAuthorization(String accessKeyId, String secretAccessKey, String region, String service,
	                                 String httpMethod, String uri, Map queryParameters, Map signedHeaders, byte[] payload) throws Exception {
		this(accessKeyId, secretAccessKey, region, service,
				httpMethod, uri, queryParameters, signedHeaders, payload,
				yyyyMMddTHHmmssZ.parse((String) signedHeaders.get(X_OP_DATE_HEADER)),
				Integer.parseInt((String) signedHeaders.get(X_OP_EXPIRES_HEADER))
		);
	}

	public LinkBankCardAuthorization(String accessKeyId, String secretAccessKey, String region, String service,
	                                 String httpMethod, String uri, Map queryParameters, Map signedHeaders, byte[] payload, Date dateTime, int expires) {
		this(ALGORITHM, accessKeyId, secretAccessKey, region, service, TERMINATOR,
				httpMethod, uri, queryParameters, signedHeaders, payload, dateTime, expires);
	}

	public LinkBankCardAuthorization(String algorithm, String accessKeyId, String secretAccessKey, String region, String service, String terminator,
	                                 String httpMethod, String uri, Map queryParameters, Map signedHeaders, byte[] payload, Date timeStamp, int expires) {
		this.algorithm = algorithm;
		this.accessKeyId = accessKeyId;
		this.secretAccessKey = secretAccessKey;
		this.timeStamp = timeStamp;
		this.region = region;
		this.service = service;
		this.terminator = terminator;
		this.credential = accessKeyId + "/" + yyyyMMdd.format(timeStamp) + "/" + region + "/" + service + "/" + terminator;
		this.httpMethod = httpMethod;
		this.uri = uri;
		if (queryParameters != null) this.queryParameters.putAll(queryParameters);
		if (signedHeaders != null) this.signedHeaders.putAll(signedHeaders);
		this.payload = payload;
		this.expires = expires;
		sign();
	}

	private static final Pattern pAuthorization = Pattern.compile("^([^ ]+) Credential=([^/]+/\\d{8}/[^/]+/[^/]+/[^,]+),SignedHeaders=([-_a-z0-9;]*),Signature=([a-f0-9]{64})$");
	private static final Pattern pCredential = Pattern.compile("^([^/]+)/(\\d{8})/([^/]+)/([^/]+)/([^,]+)$");
	private static final Pattern pSignedHeaders = Pattern.compile("([^;]+)");
	private static final Pattern pSignature = Pattern.compile("^[a-f0-9]{64}$");

	public LinkBankCardAuthorization(String authorizationHeader, String timeStamp, int expires) {
		try {
			this.timeStamp = yyyyMMddTHHmmssZ.parse(timeStamp);
			this.expires = expires;
			if (authorizationHeader != null) {
				Matcher mAuthorization = pAuthorization.matcher(authorizationHeader);
				if (mAuthorization.find()) {
					algorithm = mAuthorization.group(1);
					credential = mAuthorization.group(2);
					Matcher mCredential = pCredential.matcher(credential);
					if (mCredential.find()) {
						accessKeyId = mCredential.group(1);
						//timeStamp = yyyyMMdd.parse(mCredential.group(2));
						region = mCredential.group(3);
						service = mCredential.group(4);
						terminator = mCredential.group(5);
					}
					signedHeaderNames = mAuthorization.group(3);
					Matcher mHeaders = pSignedHeaders.matcher(signedHeaderNames);
					while (mHeaders.find()) {
						signedHeaders.put(mHeaders.group(1), "");
					}
					signature = mAuthorization.group(4);
				}
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	public LinkBankCardAuthorization(String algorithm, String credential, String signedHeaderNames, String signature, String timeStamp, int expires) {
		try {
			this.timeStamp = yyyyMMddTHHmmssZ.parse(timeStamp);
			this.expires = expires;
			this.algorithm = algorithm;
			if (credential != null && signedHeaderNames != null && signature != null) {
				Matcher mCredential = pCredential.matcher(credential);
				if (mCredential.find()) {
					this.credential = credential;
					accessKeyId = mCredential.group(1);
					//timeStamp = yyyyMMdd.parse(mCredential.group(2));
					region = mCredential.group(3);
					service = mCredential.group(4);
					terminator = mCredential.group(5);
				}
				this.signedHeaderNames = signedHeaderNames;
				Matcher mHeaders = pSignedHeaders.matcher(signedHeaderNames);
				while (mHeaders.find()) {
					this.signedHeaders.put(mHeaders.group(1), "");
				}

				Matcher mSignature = pSignature.matcher(signature);
				if (mSignature.find()) {
					this.signature = signature;
				}
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	public String getHttpMethod() {
		return httpMethod;
	}

	public String getUri() {
		return uri;
	}

	public Map getQueryParameters() {
		return queryParameters;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public String getCredential() {
		return credential;
	}

	public String getAccessKeyId() {
		return accessKeyId;
	}

	public String getRegion() {
		return region;
	}

	public String getService() {
		return service;
	}

	public String getTerminator() {
		return terminator;
	}

	public Map<String, String> getSignedHeaders() {
		return signedHeaders;
	}

	public String getSignature() {
		return signature;
	}

	public Date getTimeStamp() {
		return timeStamp;
	}

	public String getTimeStampString() {
		return yyyyMMddTHHmmssZ.format(timeStamp);
	}

	public int getExpires() {
		return expires;
	}

	public byte[] getPayload() {
		return payload;
	}

	public boolean isExpired() {
		return (System.currentTimeMillis() - timeStamp.getTime()) / 1000 > expires;
	}

	public String toString() {
		return algorithm + " Credential=" + credential + ",SignedHeaders=" + signedHeaderNames + ",Signature=" + signature;
	}

	public String toQueryString() {
		try {
			return "X-OP-Algorithm=" + algorithm +
					"&X-OP-Credential=" + uriEncode(credential, true) +
					"&X-OP-Date=" + yyyyMMddTHHmmssZ.format(timeStamp) +
					"&X-OP-Expires=" + expires +
					"&X-OP-SignedHeaders=" + uriEncode(signedHeaderNames, true) +
					"&X-OP-Signature=" + signature;
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

	public Map<String, String> getDebugInfo() {
		return debugInfo;
	}

	private void sign() {
		try {
			String canonicalUri = uriEncode(uri, false);

			StringBuilder canonicalQueryString = new StringBuilder();
			for (Map.Entry<String, String> entry : queryParameters.entrySet()) {
				if (canonicalQueryString.length() > 0) canonicalQueryString.append("&");
				canonicalQueryString.append(uriEncode(entry.getKey(), true)).append("=").append(uriEncode(entry.getValue(), true));
			}

			StringBuilder canonicalHeaders = new StringBuilder();

			StringBuilder buf = new StringBuilder();
			for (Map.Entry<String, String> entry : signedHeaders.entrySet()) {
				canonicalHeaders.append(entry.getKey().toLowerCase()).append(":").append(entry.getValue().trim()).append("\n");
				if (buf.length() > 0) buf.append(";");
				buf.append(entry.getKey().toLowerCase());
			}
			signedHeaderNames = buf.toString();

			//String hashedPayload = (payload != null && payload.length > 0) ? hex(sha256Hash(payload)) : "UNSIGNED-PAYLOAD";
			String hashedPayload = (payload != null) ? (payload.length > 0 ? hex(sha256Hash(payload)) : EMPTY_BODY_SHA256) : UNSIGNED_PAYLOAD;

			String canonicalRequest = httpMethod + "\n" +
					canonicalUri + "\n" +
					canonicalQueryString.toString() + "\n" +
					canonicalHeaders.toString() + "\n" +
					signedHeaderNames + "\n" +
					hashedPayload;

			String timeStamp = yyyyMMddTHHmmssZ.format(this.timeStamp);

			String scope = yyyyMMdd.format(this.timeStamp) + "/" + region + "/" + service + "/" + terminator;

			String stringToSign = algorithm + "\n" +
					timeStamp + "\n" +
					scope + "\n" +
					hex(sha256Hash(canonicalRequest));

			byte[] dateKey = hmacSha256(SCHEME + secretAccessKey, yyyyMMdd.format(this.timeStamp));
			byte[] dateRegionKey = hmacSha256(dateKey, region);
			byte[] dateRegionServiceKey = hmacSha256(dateRegionKey, service);
			byte[] signingKey = hmacSha256(dateRegionServiceKey, terminator);
			signature = hex(hmacSha256(signingKey, stringToSign));


			debugInfo.put("canonicalRequest", canonicalRequest);
			debugInfo.put("stringToSign", stringToSign);
			debugInfo.put("dateKey", hex(dateKey));
			debugInfo.put("dateRegionKey", hex(dateRegionKey));
			debugInfo.put("dateRegionServiceKey", hex(dateRegionServiceKey));
			debugInfo.put("signingKey", hex(signingKey));
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	private static String uriEncode(String data, boolean encodeSlash) throws Exception {
		StringBuilder sb = new StringBuilder();
		for (byte ch : data.getBytes("UTF-8"))
			if ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z')
					|| (ch >= '0' && ch <= '9')
					|| ch == '_' || ch == '-' || ch == '~' || ch == '.'
					|| (ch == '/' && !encodeSlash)
			) sb.append(Character.toChars(ch));
			else sb.append("%").append(String.format("%02X", ch & 0xFF));
		return sb.toString();
	}

	private static byte[] hmacSha256(String key, String data) throws Exception {
		return hmacSha256(key.getBytes("UTF-8"), data);
	}

	private static byte[] hmacSha256(byte[] key, String data) throws Exception {
		SecretKeySpec signingKey = new SecretKeySpec(key, "HMACSHA256");
		Mac mac = Mac.getInstance("HMACSHA256");
		mac.init(signingKey);
		return mac.doFinal(data.getBytes("UTF-8"));
	}

	private static byte[] sha256Hash(String data) throws Exception {
		return sha256Hash(data.getBytes("UTF-8"));
	}

	private static byte[] sha256Hash(byte[] data) throws Exception {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(data);
		return md.digest();
	}

	private static String hex(byte[] data) {
		StringBuilder sb = new StringBuilder();
		for (byte b : data) sb.append(String.format("%02x", b & 0xFF));
		return sb.toString();
		//return printHexBinary(data).toLowerCase();
	}
}
