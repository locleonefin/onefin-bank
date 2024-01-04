package com.onefin.ewallet.bank.service.bvb;


import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.Level;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@Service
public class BVBEncryptUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(BVBEncryptUtil.class);

	private static final String SHA256RSA = "SHA256withRSA";

	private static final String RSA = "RSA";

	public PrivateKey readPrivateKey(String filename)
			throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		PKCS8EncodedKeySpec keySpec =
				new PKCS8EncodedKeySpec(readFileBytes(filename));
		KeyFactory keyFactory = KeyFactory.getInstance(RSA);
		return keyFactory.generatePrivate(keySpec);
	}

	public PrivateKey readPrivateKeyRSA(String filename) throws Exception {
		File file = new File(filename);
		String key = new String(Files.readAllBytes(file.toPath()), Charset.defaultCharset());

		String privateKeyPEM = key
				.replace("-----BEGIN PRIVATE KEY-----", "")
				.replaceAll(System.lineSeparator(), "")
				.replace("-----END PRIVATE KEY-----", "");

		byte[] encoded = Base64.decodeBase64(privateKeyPEM);

		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
		return keyFactory.generatePrivate(keySpec);
	}

	public byte[] readFileBytes(String filename) throws IOException {
		Path path = Paths.get(filename);
		return Files.readAllBytes(path);
	}

	public String sign(String data, PrivateKey privateKey, String algorithm) {
		try {
			Signature signature = Signature.getInstance(algorithm);
			signature.initSign(privateKey);
			signature.update(data.getBytes(StandardCharsets.UTF_8));
			byte[] signedByteData = signature.sign();

			return Base64.encodeBase64String(signedByteData);

		} catch (Exception e) {
			LOGGER.error("Cannot sign {}", algorithm, e);
		}

		return null;
	}

	private String hex(byte[] data) {
		StringBuilder sb = new StringBuilder();
		for (byte b : data) sb.append(String.format("%02x", b & 0xFF));
		return sb.toString();
		//return printHexBinary(data).toLowerCase();
	}

	private String hex(String data) throws UnsupportedEncodingException {
		return String.format("%040x", new BigInteger(1, data.getBytes("UTF-8")));
		//return printHexBinary(data).toLowerCase();
	}

	public String signHex(String data, PrivateKey privateKey, String algorithm) {
		try {
			Signature signature = Signature.getInstance(algorithm);
			signature.initSign(privateKey);
			signature.update(data.getBytes(StandardCharsets.UTF_8));
			byte[] signedByteData = signature.sign();
//			return hex(Base64.encodeBase64(signedByteData));
			return hex(signedByteData);

		} catch (Exception e) {
			LOGGER.error("Cannot sign {}", algorithm, e);
		}
		return null;
	}

	public String sign(String data, PrivateKey privateKey) {
		return sign(data, privateKey, SHA256RSA);
	}

	public String signHex(String data, PrivateKey privateKey) {
		return signHex(data, privateKey, SHA256RSA);
	}


	public boolean verifySignature(String data, String signatureStr, PublicKey publicKey) {
		try {

			Signature signature = Signature.getInstance(SHA256RSA);
			signature.initVerify(publicKey);
			signature.update(data.getBytes(StandardCharsets.UTF_8));
			return signature.verify(Base64.decodeBase64(signatureStr.getBytes(StandardCharsets.UTF_8)));

		} catch (Exception e) {
			LOGGER.error("Cannot SHA256RSA", e);
		}

		return false;
	}

	public PrivateKey readPrivateKeyBVB(String filename) throws Exception {
		InputStream targetStream = new FileInputStream(filename);
		String privateKeyPEM = getKey(targetStream);
		return getPrivateKeyFromString(privateKeyPEM);

	}

	public PublicKey readPublicKey(String filename) throws IOException,
			CertificateException, NoSuchAlgorithmException, InvalidKeySpecException {
		File file = new File(filename);
		String key = new String(Files.readAllBytes(file.toPath()), Charset.defaultCharset());

		String publicKeyPEM = key
				.replace("-----BEGIN PUBLIC KEY-----", "")
				.replaceAll(System.lineSeparator(), "")
				.replace("-----END PUBLIC KEY-----", "");

		byte[] encoded = Base64.decodeBase64(publicKeyPEM);

		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
		return (RSAPublicKey) keyFactory.generatePublic(keySpec);

	}

	public static RSAPrivateKey getPrivateKeyFromString(String key) throws IOException,
			GeneralSecurityException {
		String privateKeyPEM = key;
		privateKeyPEM =
				privateKeyPEM.replace("-----BEGIN PRIVATE KEY-----\n", "");
		privateKeyPEM = privateKeyPEM.replace("-----END PRIVATE KEY-----", "");
		byte[] encoded = org.apache.tomcat.util.codec.binary.Base64.decodeBase64(privateKeyPEM);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
		RSAPrivateKey privKey = (RSAPrivateKey) kf.generatePrivate(keySpec);
		return privKey;
	}

	public static RSAPrivateKey getPrivateKey(InputStream is) throws IOException,
			GeneralSecurityException {
		String privateKeyPEM = getKey(is);
		return getPrivateKeyFromString(privateKeyPEM);
	}

	public static String getKey(InputStream is) throws IOException {
		// Read key from file
		String strKeyPEM = "";
		BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		String line;
		while ((line = br.readLine()) != null) {
			strKeyPEM += line + "\n";
		}
		br.close();
		return strKeyPEM;
	}


	public static String getKeyPub(InputStream is) throws IOException {
		// Read key from file
		String strKeyPEM = "";
		BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		String line;
		while ((line = br.readLine()) != null) {
			strKeyPEM += line + "";
		}
		br.close();
		return strKeyPEM;
	}

	public PublicKey readPublicKeyInputStream(String filename)
			throws IOException, GeneralSecurityException {
		InputStream targetStream = new FileInputStream(filename);

		String key = getKey(targetStream);

		return getPublicKeyFromString(key);


	}

	public static RSAPublicKey getPublicKeyFromString(String key) throws IOException,
			GeneralSecurityException {
		String publicKeyPEM = key;
		publicKeyPEM =
				publicKeyPEM.replace("-----BEGIN PUBLIC KEY-----\n", "");
		publicKeyPEM = publicKeyPEM.replace("-----END PUBLIC KEY-----", "");
		byte[] encoded = org.apache.tomcat.util.codec.binary.Base64.decodeBase64(publicKeyPEM);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		X509EncodedKeySpec spec = new X509EncodedKeySpec(encoded);
		return (RSAPublicKey) kf.generatePublic(spec);
	}


	public static RSAPublicKey getPublicKey(InputStream is) throws IOException,
			GeneralSecurityException {
		String publicKeyPEM = getKey(is);
		return getPublicKeyFromString(publicKeyPEM);
	}

	public static boolean bvbVerify(InputStream is, String message,
									String signature) throws Exception {

		PublicKey publicKey = getPublicKey(is);

		Signature sign = Signature.getInstance("SHA256withRSA");
		sign.initVerify(publicKey);
		sign.update(message.getBytes("UTF-8"));
		return sign.verify(org.apache.tomcat.util.codec.binary.Base64.decodeBase64(signature.getBytes("UTF-8")));
	}

	public boolean bvbVerifyHex(InputStream is, String message,
								String signature) throws Exception {

		PublicKey publicKey = getPublicKey(is);
		Signature sign = Signature.getInstance("SHA256withRSA");
		sign.initVerify(publicKey);
		sign.update(message.getBytes("UTF-8"));

		byte[] bytes = Hex.decodeHex(signature);
		return sign.verify(bytes);
//		decodeBase64
	}

	public byte[] decodeUsingBigInteger(String hexString) {
		byte[] byteArray = new BigInteger(hexString, 16)
				.toByteArray();
		if (byteArray[0] == 0) {
			byte[] output = new byte[byteArray.length - 1];
			System.arraycopy(
					byteArray, 1, output,
					0, output.length);
			return output;
		}
		return byteArray;
	}

	public static byte[] hexStringToByteArray(String hex) {
		int l = hex.length();
		byte[] data = new byte[l / 2];
		for (int i = 0; i < l; i += 2) {
			data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
					+ Character.digit(hex.charAt(i + 1), 16));
		}
		return data;
	}

	public String MD5Hashing(String input) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			md.update(input.getBytes());
			byte[] digest = md.digest();
//			String myHash = uppercase == true ? DatatypeConverter.printHexBinary(digest).toUpperCase() : DatatypeConverter.printHexBinary(digest).toLowerCase();
//			return DatatypeConverter.printHexBinary(digest);
//			private String hex(byte[] data) {
			return hex(digest);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}


}
