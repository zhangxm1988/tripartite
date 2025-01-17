package work.gaigeshen.triparttite.pay.wechat.config;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Objects;

/**
 * 默认的微信商户私钥
 *
 * @author gaigeshen
 */
public class DefaultWechatPrivateKey implements WechatPrivateKey {

  private final PrivateKey privateKey;

  private final String certSerialNumber;

  public DefaultWechatPrivateKey(PrivateKey privateKey, String certSerialNumber) {
    if (Objects.isNull(privateKey)) {
      throw new IllegalArgumentException("private key cannot be null");
    }
    if (Objects.isNull(certSerialNumber)) {
      throw new IllegalArgumentException("certificate serial number cannot be null");
    }
    this.privateKey = privateKey;
    this.certSerialNumber = certSerialNumber;
  }

  public static DefaultWechatPrivateKey load(String privateKeyContent, String certSerialNumber) throws WechatPrivateKeyException {
    if (Objects.isNull(privateKeyContent)) {
      throw new IllegalArgumentException("private key content cannot be null");
    }
    if (Objects.isNull(certSerialNumber)) {
      throw new IllegalArgumentException("certificate serial number cannot be null");
    }
    try {
      KeyFactory factory = KeyFactory.getInstance("RSA");
      KeySpec ks = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyContent));
      PrivateKey privateKey = factory.generatePrivate(ks);
      return new DefaultWechatPrivateKey(privateKey, certSerialNumber);
    }
    catch (NoSuchAlgorithmException e) {
      throw new WechatPrivateKeyException("'RSA' is not supported", e);
    }
    catch (InvalidKeySpecException e) {
      throw new WechatPrivateKeyException("could not generate private key", e);
    }
  }

  public static DefaultWechatPrivateKey load(InputStream inputStream, String certSerialNumber) throws WechatPrivateKeyException {
    if (Objects.isNull(inputStream)) {
      throw new IllegalArgumentException("private key input stream cannot be null");
    }
    if (Objects.isNull(certSerialNumber)) {
      throw new IllegalArgumentException("certificate serial number cannot be null");
    }
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      byte[] buffer = new byte[4096];
      int len;
      while ((len = inputStream.read(buffer)) != -1) {
        out.write(buffer, 0, len);
      }
      return load(out.toString("utf-8"), certSerialNumber);
    } catch (IOException e) {
      throw new WechatPrivateKeyException("could not read private key", e);
    }
  }

  public static DefaultWechatPrivateKey loadClasspath(String classpath, String certSerialNumber) throws WechatPrivateKeyException {
    if (Objects.isNull(classpath)) {
      throw new IllegalArgumentException("classpath cannot be null");
    }
    if (Objects.isNull(certSerialNumber)) {
      throw new IllegalArgumentException("certificate serial number cannot be null");
    }
    try (InputStream in = DefaultWechatPrivateKey.class.getClassLoader().getResourceAsStream(classpath)) {
      if (Objects.isNull(in)) {
        throw new WechatPrivateKeyException("could not read resource: " + classpath);
      }
      return load(in, certSerialNumber);
    } catch (IOException e) {
      throw new WechatPrivateKeyException("could not load from classpath: " + classpath, e);
    }
  }

  public static DefaultWechatPrivateKey loadFile(String filename, String certSerialNumber) throws WechatPrivateKeyException {
    if (Objects.isNull(filename)) {
      throw new IllegalArgumentException("filename cannot be null");
    }
    if (Objects.isNull(certSerialNumber)) {
      throw new IllegalArgumentException("certificate serial number cannot be null");
    }
    Path path = Paths.get(filename);
    if (!Files.isReadable(path)) {
      throw new IllegalArgumentException("file not readable: " + filename);
    }
    try (InputStream inputStream = Files.newInputStream(path)) {
      return load(inputStream, certSerialNumber);
    } catch (IOException e) {
      throw new WechatPrivateKeyException("could not load from file: " + filename, e);
    }
  }

  @Override
  public String sign(byte[] content) throws WechatPrivateKeyException {
    if (Objects.isNull(content)) {
      throw new IllegalArgumentException("content cannot be null");
    }
    try {
      Signature signature = Signature.getInstance("SHA256withRSA");
      signature.initSign(privateKey);
      signature.update(content);
      byte[] signResult = signature.sign();
      return Base64.getEncoder().encodeToString(signResult);
    }
    catch (InvalidKeyException e) {
      throw new WechatPrivateKeyException("private key is invalid", e);
    }
    catch (GeneralSecurityException e) {
      throw new WechatPrivateKeySigningException("failed to calculate signature", e);
    }
  }

  @Override
  public byte[] decrypt(String cipherText) throws WechatPrivateKeyException {
    if (Objects.isNull(cipherText)) {
      throw new IllegalArgumentException("cipher text cannot be null");
    }
    try {
      Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
      cipher.init(Cipher.DECRYPT_MODE, privateKey);
      return cipher.doFinal(Base64.getDecoder().decode(cipherText));
    }
    catch (InvalidKeyException e) {
      throw new WechatPrivateKeyException("private key is invalid", e);
    }
    catch (GeneralSecurityException e) {
      throw new WechatPrivateKeyDecryptionException("could not decrypt", e);
    }
  }

  @Override
  public String getCertSerialNumber() {
    return certSerialNumber;
  }
}
