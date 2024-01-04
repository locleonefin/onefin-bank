package com.onefin.ewallet.bank.service.vcb;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import com.onefin.ewallet.common.utility.json.JSONHelper;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onefin.ewallet.bank.dto.vcb.PasswordDeriveBytes;
import com.onefin.ewallet.bank.common.VcbConstants;
import com.onefin.ewallet.bank.dto.vcb.VcbAccountDataRequest;
import com.onefin.ewallet.bank.service.common.ConfigLoader;

@Service
public class LinkBankAccountEncryptUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(LinkBankAccountEncryptUtil.class);

	private static final String RSA = "RSA";
	private static final String UTF8 = "UTF8";
	private static final String RSAECBPKCS1PADDING = "RSA/ECB/PKCS1PADDING";
	private static final String SHA1RSA = "SHA1withRSA";

	@Autowired
	private ConfigLoader configLoader;

	@Autowired
	private JSONHelper jsonHelper;

	public String vcbEncrypt(String data) {
		try {

			PasswordDeriveBytes password = new PasswordDeriveBytes(configLoader.getVcbPassPhrase(),
					configLoader.getVcbSaltValue().getBytes(), configLoader.getVcbHashAlgorithm(),
					configLoader.getVcbPasswordIterations());
			byte[] keyBytes = password.GetBytes(configLoader.getVcbKeySize() / 8); // 32
			SecretKey secret = new SecretKeySpec(keyBytes, "AES");

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, secret,
					new IvParameterSpec(configLoader.getVcbInitVector().getBytes(UTF8)));
			byte[] encryptedText = cipher.doFinal(data.getBytes(UTF8));

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			outputStream.write(encryptedText);

			// properly encode the complete cipher text
			return DatatypeConverter.printBase64Binary(outputStream.toByteArray());
		} catch (Exception e) {
			LOGGER.error("VCB - Cannot encrypt data!", e);
		}

		return null;
	}

	/**
	 * Sign message before send to VCB
	 *
	 * @param data
	 * @return
	 */
	public String sign(String data) {
		try {
			return this.sign(data, readOneFinPrivateKey());
		} catch (Exception e) {
			LOGGER.error("Cannot sign data!", e);
		}
		return null;
	}

	/**
	 * Sign data
	 *
	 * @param data
	 * @param privateKey
	 * @return
	 */
	public String sign(String data, PrivateKey privateKey) {
		try {
			Signature signature = Signature.getInstance(SHA1RSA);
			signature.initSign(privateKey);
			signature.update(data.getBytes(VcbConstants.UTF8_1));
			byte[] signedByteData = signature.sign();

			return Base64.encodeBase64String(signedByteData);

		} catch (Exception e) {
			LOGGER.error("Cannot signRSASHA1", e);
		}

		return null;
	}

	public PrivateKey readOneFinPrivateKey() throws Exception {
		return readPrivateKey(configLoader.getOfVcbPublicKeyM(), configLoader.getOfVcbPrivateKeyD());
	}

	public PublicKey readVcbPublicKey() throws Exception {
		return readPublicKey(configLoader.getVcbPublicKeyM(), configLoader.getVcbKeyExponent());
	}

	public PrivateKey readPrivateKey(String privateKeyM, String privateKeyD) throws Exception {
		byte[] modBytes = Base64.decodeBase64(privateKeyM.trim());
		byte[] dBytes = Base64.decodeBase64(privateKeyD.trim());

		BigInteger modules = new BigInteger(1, modBytes);
		BigInteger d = new BigInteger(1, dBytes);

		KeyFactory factory = KeyFactory.getInstance(RSA);
		Cipher cipher = Cipher.getInstance(RSAECBPKCS1PADDING);

		RSAPrivateKeySpec privSpec = new RSAPrivateKeySpec(modules, d);
		return factory.generatePrivate(privSpec);
	}

	public PublicKey readPublicKey(String publicKeyM, String keyExponent) throws Exception {
		//LOGGER.info("=====publicKeyM:" + publicKeyM);
		byte[] modulusBytes = Base64.decodeBase64(publicKeyM);
		byte[] exponentBytes = Base64.decodeBase64(keyExponent);
		BigInteger modulus = new BigInteger(1, modulusBytes);
		BigInteger publicExponent = new BigInteger(1, exponentBytes);

		RSAPublicKeySpec rsaPubKey = new RSAPublicKeySpec(modulus, publicExponent);
		KeyFactory fact = KeyFactory.getInstance(RSA);
		return fact.generatePublic(rsaPubKey);
	}

	/**
	 * verify signature from VCB when receive signed data
	 *
	 * @param data
	 * @param signatureStr
	 * @return
	 */
	public boolean verifySignature(String data, String signatureStr) {
		try {
			return this.verifySignature(data.trim(), signatureStr.trim(), readVcbPublicKey());
		} catch (Exception e) {
			LOGGER.error("Cannot sign data!", e);
		}
		return false;
	}

	/**
	 * Verify signature
	 *
	 * @param data
	 * @param signatureStr
	 * @param publicKey
	 * @return
	 */
	public boolean verifySignature(String data, String signatureStr, PublicKey publicKey) {
		try {

			Signature signature = Signature.getInstance(SHA1RSA);
			signature.initVerify(publicKey);
			signature.update(data.getBytes(UTF8));
			return signature.verify(Base64.decodeBase64(signatureStr.getBytes(UTF8)));

		} catch (Exception e) {
			LOGGER.error("Cannot signRSASHA1", e);
		}

		return false;
	}

	/**
	 * Decode data object receive from VCB and parse into VcbTopupRequest
	 *
	 * @param encryptedStr
	 * @return
	 */
	public VcbAccountDataRequest decodeDataRequest(String encryptedStr) {
		VcbAccountDataRequest responseData = null;
		try {
			String dataDecrypted = this.vcbDecrypt(encryptedStr);
			LOGGER.info("Decode data from VCB: {}", dataDecrypted);
			responseData = (VcbAccountDataRequest) jsonHelper.convertString2Map(dataDecrypted, VcbAccountDataRequest.class);
		} catch (Exception e) {
			LOGGER.error("Cannot parse response data!", e);
		}
		return responseData;
	}

	public String vcbDecrypt(String data) {
		try {

			PasswordDeriveBytes password = new PasswordDeriveBytes(configLoader.getVcbPassPhrase(),
					configLoader.getVcbSaltValue().getBytes(), configLoader.getVcbHashAlgorithm(),
					configLoader.getVcbPasswordIterations());
			byte[] keyBytes = password.GetBytes(configLoader.getVcbKeySize() / 8); // 32

			Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
			SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
			cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(configLoader.getVcbInitVector().getBytes(UTF8)));

			byte[] ciphertext = DatatypeConverter.parseBase64Binary(data);
			byte[] plaintext = cipher.doFinal(ciphertext);
			return new String(plaintext, UTF8);
		} catch (Exception e) {
			LOGGER.error("VCB - Cannot decrypt data!", e);
		}
		return null;
	}

	/**
	 * Decode data object receive from VCB and parse into VcbRequest
	 *
	 * @param encryptedStr
	 * @return
	 */
	public VcbAccountDataRequest decodeVcbRequest(String encryptedStr) {

		VcbAccountDataRequest responseData = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			String dataDecrypted = vcbDecrypt(encryptedStr);
			responseData = mapper.readValue(dataDecrypted, VcbAccountDataRequest.class);
		} catch (Exception e) {
			LOGGER.error("Cannot parse response data!", e);
		}

		return responseData;
	}
}
