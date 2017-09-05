package ali.pay.service.servlet;

import ali.pay.service.service.ALIPay.AliPayService;
import ali.pay.service.util.ALIPay.ALIPayConstants;
import ali.pay.service.util.CommonUtils;
import com.alipay.api.internal.util.AlipaySignature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Map;

@SuppressWarnings("all")
@WebServlet(urlPatterns={"/notify"}, description = "支付宝异步回调通知")
public class NotifyServlet extends HttpServlet {
	private static final Logger logger = LogManager.getLogger(NotifyServlet.class);
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response){
		Writer writer = null;
		String result = null;
		Map<String, Object> notifyMap = null;
		//返回结果
		try {
			CommonUtils.getRequestInfo(request, logger);
		    notifyMap =  CommonUtils.resquestParameterToMap(request);
		    //TODO 进行支付宝验签
            //如果验签失败说明该请求不是来自支付宝,返回404
            if(AlipaySignature.rsaCheckV1(CommonUtils.resquestParameterToMap2(request), ALIPayConstants.APP_PUBLIC_KEY, ALIPayConstants.CHARSET, ALIPayConstants.SIGN_TYPE)){
                logger.trace("签名验证成功");
                if(!"TRADE_SUCCESS".equals(notifyMap.get("trade_status")) && !"TRADE_FINISHED".equals(notifyMap.get("trade_status"))){
                    response.setStatus(404);
                }
            }
			writer = response.getWriter();
			request.setCharacterEncoding("UTF-8");

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(notifyMap != null){
				//回掉通知(仅在成功的时候才会调用)
                AliPayService.callback(notifyMap);
				//向数据库插入日志数据
                AliPayService.insertLog(notifyMap);
			}
			//TODO 回调通知
			try {
				writer.write("success");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
