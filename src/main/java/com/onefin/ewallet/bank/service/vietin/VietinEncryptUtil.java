package com.onefin.ewallet.bank.service.vietin;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class VietinEncryptUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(VietinEncryptUtil.class);

	private static final String RSA = "RSA";
	private static final String UTF8 = "UTF8";
	private static final String SHA1RSA = "SHA1withRSA";

	/**
	 * This method takes the file path for private key and returns the instance of
	 * private key.
	 * 
	 * @param filename : Filename along with the path for private key.
	 */
	public PrivateKey readPrivateKey(String filename)
			throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(readFileBytes(filename));
		KeyFactory keyFactory = KeyFactory.getInstance(RSA);
		return keyFactory.generatePrivate(keySpec);
	}

	public String sign(String data, PrivateKey privateKey) {
		return this.sign(data, privateKey, SHA1RSA);
	}

  public String sign(String data, PrivateKey privateKey, String algorithm) {
    try {

      Signature signature = Signature.getInstance(algorithm);
      signature.initSign(privateKey);
      signature.update(data.getBytes(UTF8));
      byte[] signedByteData = signature.sign();

      return Base64.encodeBase64String(signedByteData);

    } catch (Exception e) {
      LOGGER.error("Cannot sign {}", algorithm, e);
    }

    return null;
  }

	/**
	 * This method reads the bytes from the file available at the path provided in
	 * the filename and returns it.
	 * 
	 * @param filename : Filename along with the path.
	 */
	public byte[] readFileBytes(String filename) throws IOException {
		Path path = Paths.get(filename);
		return Files.readAllBytes(path);
	}

	/**
	 * This method takes the file path for public key and returns the instance of
	 * public key.
	 * 
	 * @param filename : Filename along with the path for public key. Sample
	 *                 filename. E.g. "/home/key/MerchantXpublic.der"
	 * @throws CertificateException
	 */
	public PublicKey readPublicKey2(String filename) throws IOException, CertificateException {
		FileInputStream fin = new FileInputStream(filename);
		CertificateFactory f = CertificateFactory.getInstance("X.509");
		X509Certificate certificate = (X509Certificate) f.generateCertificate(fin);
		return certificate.getPublicKey();
	}

	/**
	 * @param data
	 * @param signedData
	 * @param publicKey
	 * @return
	 */
	public boolean verifySignature(String data, String signedData, PublicKey publicKey) {
    return this.verifySignature(data, signedData, publicKey, SHA1RSA);
	}

  public boolean verifySignature(String data, String signedData, PublicKey publicKey, String algorithm) {
    try {

      Signature signature = Signature.getInstance(algorithm);
      signature.initVerify(publicKey);

      signature.update(data.getBytes(UTF8));

      byte[] signatureBytes = Base64.decodeBase64(signedData.getBytes(UTF8));
      return signature.verify(signatureBytes);

    } catch (Exception e) {
      LOGGER.error("Cannot verify Signature", e);
    }

    return false;
  }
}
