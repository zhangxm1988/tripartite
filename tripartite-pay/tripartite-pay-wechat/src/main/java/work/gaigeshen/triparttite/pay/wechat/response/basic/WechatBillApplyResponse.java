package work.gaigeshen.triparttite.pay.wechat.response.basic;

import work.gaigeshen.triparttite.pay.wechat.response.WechatResponse;

/**
 * @author gaigeshen
 */
public class WechatBillApplyResponse implements WechatResponse {

  public String hash_type;

  public String hash_value;

  public String download_url;
}
