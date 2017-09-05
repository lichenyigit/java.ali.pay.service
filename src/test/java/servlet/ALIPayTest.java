package servlet;

import ali.pay.service.exception.HttpRequestFailedException;
import ali.pay.service.util.ALIPay.ALIPayUtil;
import ali.pay.service.util.HttpUtil;
import com.alibaba.fastjson.JSON;
import org.junit.Test;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lichenyi
 * @date 2017-8-31 0031
 */
public class ALIPayTest {

    @Test
    public void getQRCodeServletTest(){
        Map<String, Object> map = new HashMap<>();
        map.put("url", "http://wechat.lichenyi.cn/index?");//回调url。请求方式post， 返回的参数样例（http://wechat.lichenyi.cn/index?】, parameter --> 【{"transaction_id":"4006892001201709019539268994","out_trade_no":"wx20170911sdfadfaadg","total_fee":"1","return_code":"SUCCESS"}）
        map.put("platform", "alipay");                      //调用平台
        map.put("descrition", "支付宝支付描述");                //支付描述测试

        List<HttpUtil.Parameter> list = new ArrayList<>();
        list.add(new HttpUtil.Parameter("total_fee", "0.01"));                     //支付金额
        list.add(new HttpUtil.Parameter("order_id", ALIPayUtil.generateOrderId()));      //订单id
        list.add(new HttpUtil.Parameter("subject", "支付宝扫码支付"));              //订单标题
        list.add(new HttpUtil.Parameter("attach", JSON.toJSONString(map)));             //自定义参数
        try {
            String result = HttpUtil.postString("http://pay.ali.lichenyi.cn/getQRCode", list);
            System.out.println(result);
        } catch (HttpRequestFailedException e) {
            e.printStackTrace();
        }
    }

}
