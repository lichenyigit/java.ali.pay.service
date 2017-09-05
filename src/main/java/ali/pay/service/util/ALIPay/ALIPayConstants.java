package ali.pay.service.util.ALIPay;

/**
 * @author Lichenyi
 * @date 2017-9-4 0004
 */
public class ALIPayConstants {

    public static final String URL = "https://openapi.alipay.com/gateway.do";//支付宝网关（固定）
    public static final String APP_ID = "";//APPID即创建应用后生成
    public static final String APP_PRIVATE_KEY = "";//开发者应用私钥，由开发者自己生成
    public static final String APP_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmFoCIBq+8SUfyZMDPVorlPv0W6yaFeIaxBZnrIFtnl0yXSK7H5olqZtwkaLWP6HJofXfXdFXGxGzS1oHy3Yjzwob3kyHe+19K4fxXgcpQ6OqE3ux2+QXwobMa+clA9vggeZY7yRHa0Hl08caF2NYIjrQ0XEKbEIqvWChdoQ18hJ7o96horn3fFEwdkm2sTnCgXEy9v94i3uhXFvkWiH0qLqQBFIQDTEYZlGbR+fSgLx0N1peNGqCxosYUlGGJFCdBk/r5uZNuhx25o1/rez/ip7CjX5xVBh5NP+yU/77EADw8WCB9Sz/aqZ5X9JQNSEZ74ey366RsngV60gNo8vRUQIDAQAB";//开发者应用公钥，由开发者自己生成
    public static final String FORMAT = "json";//设备号 自定义参数，可以为终端设备号(门店号或收银设备ID)，PC网页或公众号内支付可以传"WEB"
    public static final String CHARSET = "UTF-8";
    public static final String SIGN_TYPE = "RSA2";//商户生成签名字符串所使用的签名算法类型，目前支持RSA2和RSA，推荐使用RSA2
    public static final String NOTIFY_URL = "http://项目部署的域名/notify";//回调url
    public static final String TIMEOUT_EXPRESS = "30m";//订单超时时间

}
