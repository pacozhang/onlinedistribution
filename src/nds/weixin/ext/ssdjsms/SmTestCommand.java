package nds.weixin.ext.ssdjsms;

import java.rmi.RemoteException;

import javax.xml.namespace.QName;
import javax.xml.rpc.Call;

import org.json.JSONException;
import org.json.JSONObject;


import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.control.web.WebUtils;
import nds.util.NDSException;
/**
 * 发送短信消息
 * @author kunlun
 *
 */
public class SmTestCommand extends Command {

	@Override
	public ValueHolder execute(DefaultWebEvent event) throws NDSException,
			RemoteException {
		// TODO Auto-generated method stub
		ValueHolder vh =new ValueHolder();
		JSONObject jo = (JSONObject) event.getParameterValue("jsonObject");
		
		try {
			jo=new JSONObject(jo.optString("params"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.debug("params--"+jo.toString());
		
		String url=WebUtils.getProperty("sms.url","http://ws.iems.net.cn/GeneralSMS/ws/SmsInterface?wsdl");
		SmsInterfaceServiceLocator service = new SmsInterfaceServiceLocator();
		service.setSmsInterfaceEndpointAddress(url);
		
		Call call= null;
		try {
			call = service.createCall();
			
			call.setTargetEndpointAddress(url);
			call.setOperationName(new QName(url,"clusterSend"));
			String result = (String)call.invoke(new Object[]{"68847:admin","49413779","","13092368360","尊敬的微平台用户,您正在进行网页注册,验证码:434864请务将验证码泄漏!","",""});
			logger.debug("send sms return->"+result);
			if (nds.util.Validator.isNotNull(result)) {
				vh.put("code", "0");
				vh.put("message", "发送短信成功");
			}else {
				vh.put("code", "-1");
				vh.put("message", "发送短信异常");
				return vh;
			}
		} catch (Exception e1) {
			// TODO: handle exception
			e1.printStackTrace();
			vh.put("code", "-1");
			vh.put("message", "发送短信异常");
			return vh;
		}
		
		return vh;
	}
}
