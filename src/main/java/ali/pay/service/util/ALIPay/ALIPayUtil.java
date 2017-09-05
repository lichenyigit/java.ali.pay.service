package ali.pay.service.util.ALIPay;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayConstants;
import com.alipay.api.internal.util.AlipaySignature;

import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

/**
 * @author lichenyi
 * @date 2017-9-3-0003.
 */
public class ALIPayUtil {

    /**
     * 获取随机字符串 Nonce Str
     *
     * @return String 随机字符串
     */
    public static String generateNonceStr() {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 32);
    }
    public static String generateNonceStr(int length) {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, length);
    }

    public static String generateOrderId(){
        StringBuffer result = new StringBuffer("ali");
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        result.append(String.format("%1$tY%1$tm%1$td%1$tH%1$tM%1$tS", calendar.getTime()));
        result.append(generateNonceStr(18));
        return result.toString();
    }

    /**
     * 获取支付宝签名
     * @param
     * @return
     * @author lichenyi
     * @date 2017-9-4 0004 13:32
     */
    public static String rsaSign(Map<String, String> data) throws AlipayApiException {
        String content = AlipaySignature.getSignContent(data);
        if (AlipayConstants.SIGN_TYPE_RSA.equals(ALIPayConstants.SIGN_TYPE)) {
            return AlipaySignature.rsaSign(content, ALIPayConstants.APP_PRIVATE_KEY, ALIPayConstants.CHARSET);
        } else if (AlipayConstants.SIGN_TYPE_RSA2.equals(ALIPayConstants.SIGN_TYPE)) {
            return AlipaySignature.rsa256Sign(content, ALIPayConstants.APP_PRIVATE_KEY, ALIPayConstants.CHARSET);
        } else {
            throw new AlipayApiException("Sign Type is Not Support : signType=" + ALIPayConstants.SIGN_TYPE);
        }
    }

}
