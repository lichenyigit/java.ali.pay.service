package ali.pay.service.servlet;

import ali.pay.service.exception.ParameterConvertException;
import ali.pay.service.service.ALIPay.AliPayService;
import ali.pay.service.servlet.base.BaseReturnJsonServlet;
import ali.pay.service.util.ALIPay.ALIPayConstants;
import ali.pay.service.util.CommonUtils;
import ali.pay.service.util.ConvertUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@SuppressWarnings("all")
@WebServlet(urlPatterns={"/getQRCode"}, description = "生成预支付交易并返回交易链接（qr_code）,扫码支付")
public class GetQRCodeServlet extends BaseReturnJsonServlet<Map<String, Object> > {
	private static final Logger logger = LogManager.getLogger(GetQRCodeServlet.class);
	
	private static final long serialVersionUID = 1L;

	protected Map<String, Object>  processPost(HttpServletRequest request, HttpServletResponse response) throws ParameterConvertException {
		String attach = ConvertUtil.getNonEmptyStringFromRequestParam(request, "attach");//交易记录中的自定义参数
		String subject = ConvertUtil.getNonEmptyStringFromRequestParam(request, "subject");//订单标题
		String total_fee = ConvertUtil.getNonEmptyStringFromRequestParam(request, "total_fee");//单位为元,小数点后最多为两位
		String order_id = ConvertUtil.getNonEmptyStringFromRequestParam(request, "order_id");//订单id

        Map<String, Object> parametersMap = CommonUtils.createMap("out_trade_no", order_id);//商户订单号,64个字符以内、只能包含字母、数字、下划线
        CommonUtils.generateMap(parametersMap, "total_amount", total_fee);//订单总金额，单位为元，精确到小数点后两位
        CommonUtils.generateMap(parametersMap, "subject", subject);//订单标题
        CommonUtils.generateMap(parametersMap, "timeout_express", ALIPayConstants.TIMEOUT_EXPRESS);
        CommonUtils.generateMap(parametersMap, "body", attach);//商品描述
        Map<String, Object> result = AliPayService.precreate(parametersMap);
		return result;
	}
		
}
