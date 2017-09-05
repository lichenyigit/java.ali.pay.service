package ali.pay.service.service.ALIPay;

import ali.pay.service.exception.DatabaseException;
import ali.pay.service.exception.HttpRequestFailedException;
import ali.pay.service.util.ALIPay.ALIPayConstants;
import ali.pay.service.util.ALIPay.ALIPayUtil;
import ali.pay.service.util.CommonUtils;
import ali.pay.service.util.DBUtil;
import ali.pay.service.util.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lichenyi
 * @date 2017-9-3-0003.
 */
public class AliPayService {
    private static Logger logger = LogManager.getLogger(AliPayService.class);

    public static Map<String, Object> precreate(Map<String, Object> data){
        AlipayClient alipayClient = new DefaultAlipayClient(ALIPayConstants.URL, ALIPayConstants.APP_ID, ALIPayConstants.APP_PRIVATE_KEY, ALIPayConstants.FORMAT, ALIPayConstants.CHARSET, ALIPayConstants.APP_PUBLIC_KEY, ALIPayConstants.SIGN_TYPE); //获得初始化的AlipayClient
        AlipayTradePrecreateRequest requestAliPay = new AlipayTradePrecreateRequest();//创建API对应的request类
        requestAliPay.setNotifyUrl(ALIPayConstants.NOTIFY_URL);
        requestAliPay.setBizContent(JSON.toJSONString(data));//设置业务参数
        AlipayTradePrecreateResponse responseAliPay = null;
        try {
            responseAliPay = alipayClient.execute(requestAliPay);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        String result = responseAliPay.getBody();
        return JSON.parseObject(result, Map.class);
    }

    /**
     *
     * @param
     * @return
     * @author lichenyi
     * @date 2017-8-31 0031 13:57
     */
    public static void callback(Map<String, Object> data) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> attach = JSON.parseObject(data.get("body").toString(), Map.class);//回掉url
                String url = attach.get("url").toString();
                String orderid = data.get("out_trade_no").toString();
                Map<String, String> resultData = new HashMap<String, String>();
                resultData.put("trade_no", data.get("trade_no").toString());
                resultData.put("out_trade_no", orderid);
                resultData.put("buyer_pay_amount", data.get("buyer_pay_amount").toString());
                resultData.put("receipt_amount", data.get("receipt_amount").toString());
                resultData.put("trade_status", data.get("trade_status").toString());
                //TODO 添加去重判断
                try {
                    if(queryByOrderId(orderid)){
                        return;
                    }
                } catch (DatabaseException e) {
                    logger.error("根据订单id查询异常");
                    e.printStackTrace();
                }
                try {
                    String sign = AlipaySignature.getSignContent(resultData);
                    resultData.put("sign", sign);
                } catch (Exception e) {
                    logger.error("callback 生成签名 异常");
                    e.printStackTrace();
                }
                String result = null;
                try {
                    result = HttpUtil.postString(url, mapToListParameter(resultData));
                } catch (HttpRequestFailedException e) {
                    e.printStackTrace();
                }
                logger.trace("callback url --> 【{}】, parameter --> 【{}】, result --> 【{}】", url, JSON.toJSONString(resultData), result);
            }
        }, "callback 线程");
        thread.start();
    }

    public static void insertLog(Map<String, Object> data){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> resultData = CommonUtils.createMap("third_order_id", data.get("trade_no").toString());
                CommonUtils.generateMap(resultData, "third_platform", "");
                CommonUtils.generateMap(resultData, "description", "");
                CommonUtils.generateMap(resultData, "order_id", data.get("out_trade_no").toString());
                CommonUtils.generateMap(resultData, "total_fee", data.get("buyer_pay_amount").toString());
                CommonUtils.generateMap(resultData, "openid", data.get("buyer_id").toString());
                CommonUtils.generateMap(resultData, "third_pre_pay_data", JSON.toJSONString(data));
                if(data.get("body") != null){
                    Map<String, Object> attachMap = JSON.parseObject(data.get("body").toString(), Map.class);
                    CommonUtils.generateMap(resultData, "third_platform", attachMap.get("platform"));
                    CommonUtils.generateMap(resultData, "description", attachMap.get("descrition"));
                }else{
                    logger.trace("attach is null.");
                }
                try {
                    boolean result = addServerLog(resultData);
                    logger.trace("支付日志写入结果 --> {}.", result);
                } catch (DatabaseException e) {
                    logger.trace("支付日志写入失败.");
                    e.printStackTrace();
                }
            }
        }, "insertLog 线程");
        thread.start();
    }

    private static List<HttpUtil.Parameter> mapToListParameter(Map<String, String> data){
        List<HttpUtil.Parameter> result = new ArrayList<>();
        for(Map.Entry<String, String> entry : data.entrySet()){
            result.add(new HttpUtil.Parameter(entry.getKey(), entry.getValue().toString()));
        }
        return result;
    }

    //*************************************************************************************************************************************************************
    //*******************************************************   数据库操作
    //*************************************************************************************************************************************************************

    /**
     * 添加日志
     * @param
     * @return
     * @author lichenyi
     * @date 2017-8-31 0031 14:40
     */
    private static boolean addServerLog(Map<String, Object> data) throws DatabaseException {
        try(Connection conn = DBUtil.getConnection()){
            PreparedStatement pstmt = conn.prepareStatement("INSERT INTO pay_server_log (id, order_id, third_order_id, third_platform, total_fee, description, openid, third_pre_pay_data) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            int idx = 1;
            pstmt.setString(idx++, ALIPayUtil.generateOrderId());
            pstmt.setString(idx++, data.get("order_id").toString());
            pstmt.setString(idx++, data.get("third_order_id").toString());
            pstmt.setString(idx++, data.get("third_platform").toString());
            pstmt.setString(idx++, data.get("total_fee").toString());
            pstmt.setString(idx++, data.get("description").toString());
            pstmt.setString(idx++, data.get("openid").toString());
            pstmt.setString(idx++, data.get("third_pre_pay_data").toString());
            int result = pstmt.executeUpdate();
            conn.commit();
            return result > 0;
        }catch(SQLException e){
            throw new DatabaseException(e);
        }

    }

    /**
     * 异步添加请求日志
     * @param
     * @return
     * @author lichenyi
     * @date 2017-9-1 0001 15:43
     */
    public static boolean sysncAddReqeustLog(Map<String, Object> data){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean result = addReqeustLog(data);
                    logger.trace("添加请求日志结果 -- {}", result);
                } catch (DatabaseException e) {
                    logger.error("添加请求日志异常");
                    e.printStackTrace();
                }
            }
        }, "添加请求日志线程");
        thread.start();
        return false;
    }

    /**
     * 添加请求日志
     * @param
     * @return
     * @author lichenyi
     * @date 2017-9-1 0001 15:43
     */
    public static boolean addReqeustLog(Map<String, Object> data) throws DatabaseException {
        try(Connection conn = DBUtil.getConnection()){
            PreparedStatement pstmt = conn.prepareStatement("INSERT INTO request_log (client_ip, request_type, request_url, request_parameters) VALUES (?, ?, ?, ?)");
            int idx = 1;
            pstmt.setString(idx++, data.get("client_ip").toString());
            pstmt.setString(idx++, data.get("request_type").toString());
            pstmt.setString(idx++, data.get("request_url").toString());
            pstmt.setString(idx++, data.get("request_parameters").toString());
            int result = pstmt.executeUpdate();
            conn.commit();
            return result > 0;
        }catch(SQLException e){
            throw new DatabaseException(e);
        }
    }

    /**
     *根据订单id查询该订单是否已经通知过
     *@return
     *@param
     *@auther Lichenyi
     *@date 2017-9-3-0003 08:19
     */
    public static boolean queryByOrderId(String orderId) throws DatabaseException {
        try(Connection conn = DBUtil.getConnection()){
            PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM pay_server_log WHERE order_id = ? FOR UPDATE ");
            int idx = 1;
            pstmt.setString(idx++, orderId);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                return rs.getLong(1) > 0;
            }
            return false;
        }catch(SQLException e){
            throw new DatabaseException(e);
        }
    }

}
