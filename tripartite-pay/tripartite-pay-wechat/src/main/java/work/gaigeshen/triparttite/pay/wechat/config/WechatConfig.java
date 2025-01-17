package work.gaigeshen.triparttite.pay.wechat.config;

import java.util.Objects;

/**
 *
 * @author gaigeshen
 */
public class WechatConfig {

  private final String serverHost;

  private final String notifyUrl;

  private final String appId;

  private final String merchantId;

  private final WechatSecretKey secretKey;

  private final WechatPrivateKey privateKey;

  private final WechatCertificates certificates;

  private WechatConfig(Builder builder) {
    this.serverHost = builder.serverHost;
    this.notifyUrl = builder.notifyUrl;
    this.appId = builder.appId;
    this.merchantId = builder.merchantId;
    this.secretKey = builder.secretKey;
    this.privateKey = builder.privateKey;
    this.certificates = builder.certificates;
  }

  public static Builder builder() {
    return new Builder();
  }

  public String getServerHost() {
    return serverHost;
  }

  public String getNotifyUrl() {
    return notifyUrl;
  }

  public String getAppId() {
    return appId;
  }

  public String getMerchantId() {
    return merchantId;
  }

  public WechatSecretKey getSecretKey() {
    return secretKey;
  }

  public WechatPrivateKey getPrivateKey() {
    return privateKey;
  }

  public WechatCertificates getCertificates() {
    return certificates;
  }

  @Override
  public int hashCode() {
    return Objects.hash(appId, merchantId);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (Objects.isNull(obj) || getClass() != obj.getClass()) {
      return false;
    }
    WechatConfig other = (WechatConfig) obj;
    return appId.equals(other.appId) && merchantId.equals(other.merchantId);
  }

  @Override
  public String toString() {
    return "WechatConfig: " + appId + "/" + merchantId;
  }

  /**
   *
   * @author gaigeshen
   */
  public static class Builder {

    private String serverHost;

    private String notifyUrl;

    private String appId;

    private String merchantId;

    private WechatSecretKey secretKey;

    private WechatPrivateKey privateKey;

    private WechatCertificates certificates;

    public void setServerHost(String serverHost) {
      this.serverHost = serverHost;
    }

    public void setNotifyUrl(String notifyUrl) {
      this.notifyUrl = notifyUrl;
    }

    public void setAppId(String appId) {
      this.appId = appId;
    }

    public void setMerchantId(String merchantId) {
      this.merchantId = merchantId;
    }

    public void setSecretKey(WechatSecretKey secretKey) {
      this.secretKey = secretKey;
    }

    public void setPrivateKey(WechatPrivateKey privateKey) {
      this.privateKey = privateKey;
    }

    public void setCertificates(WechatCertificates certificates) {
      this.certificates = certificates;
    }

    public void setSecretKeyContent(String secretKeyContent) throws WechatSecretKeyException {
      setSecretKey(DefaultWechatSecretKey.load(secretKeyContent));
    }

    public void setPrivateKeyContent(String privateKeyContent, String certSerialNumber) throws WechatPrivateKeyException {
      setPrivateKey(DefaultWechatPrivateKey.load(privateKeyContent, certSerialNumber));
    }

    public void setCertificateContent(String certificateContent) throws WechatCertificateException {
      setCertificates(DefaultWechatCertificates.load(certificateContent));
    }

    public void setSecretKeyClasspath(String secretKeyClasspath) throws WechatSecretKeyException {
      setSecretKey(DefaultWechatSecretKey.loadClasspath(secretKeyClasspath));
    }

    public void setPrivateKeyClasspath(String privateKeyClasspath, String certSerialNumber) throws WechatPrivateKeyException {
      setPrivateKey(DefaultWechatPrivateKey.loadClasspath(privateKeyClasspath, certSerialNumber));
    }

    public void setCertificateClasspath(String certificateClasspath) throws WechatCertificateException {
      setCertificates(DefaultWechatCertificates.loadClasspath(certificateClasspath));
    }

    public void setSecretKeyFile(String secretKeyFilename) throws WechatSecretKeyException {
      setSecretKey(DefaultWechatSecretKey.loadFile(secretKeyFilename));
    }

    public void setPrivateKeyFile(String privateKeyFilename, String certSerialNumber) throws WechatPrivateKeyException {
      setPrivateKey(DefaultWechatPrivateKey.loadFile(privateKeyFilename, certSerialNumber));
    }

    public void setCertificateFile(String certificateFilename) throws WechatCertificateException {
      setCertificates(DefaultWechatCertificates.loadFile(certificateFilename));
    }

    public WechatConfig build() throws WechatConfigException {
      if (Objects.isNull(serverHost)) {
        throw new WechatConfigException("'serverHost' not found");
      }
      if (Objects.isNull(notifyUrl)) {
        throw new WechatConfigException("'notifyUrl' not found");
      }
      if (Objects.isNull(appId)) {
        throw new WechatConfigException("'appId' not found");
      }
      if (Objects.isNull(merchantId)) {
        throw new WechatConfigException("'merchantId' not found");
      }
      if (Objects.isNull(secretKey)) {
        throw new WechatConfigException("'secretKey' not found");
      }
      if (Objects.isNull(privateKey)) {
        throw new WechatConfigException("'privateKey' not found");
      }
      if (Objects.isNull(certificates)) {
        throw new WechatConfigException("'certificates' not found");
      }
      return new WechatConfig(this);
    }
  }
}
