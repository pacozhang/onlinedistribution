package nds.weixin.ext.ssdjsms;

import java.rmi.RemoteException;

import javax.xml.namespace.QName;
import javax.xml.rpc.Call;

import org.json.JSONObject;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.control.web.ClientControllerWebImpl;
import nds.control.web.WebUtils;
import nds.util.NDSException;

public class SSDJSendSMSCommand extends Command{

	@Override
	public ValueHolder execute(DefaultWebEvent event) throws NDSException,
			RemoteException {
		 ValueHolder holder=null;
		 JSONObject ssmsjo=null;
		JSONObject jo = (JSONObject) event.getParameterValue("jsonObject");
		try {
			jo=new JSONObject(jo.optString("params"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String url=WebUtils.getProperty("sms.url","http://ws.iems.net.cn/GeneralSMS/ws/SmsInterface?wsdl");
		SmsInterfaceServiceLocator service = new SmsInterfaceServiceLocator();
		service.setSmsInterfaceEndpointAddress(url);

		Call call= null;
		try {
			call = service.createCall();
			
			call.setTargetEndpointAddress(url);
			call.setOperationName(new QName(url,"clusterSend"));
			
			String result = (String)call.invoke(new Object[]{WebUtils.getProperty("sms.account"),WebUtils.getProperty("sms.password"),"",jo.optString("phone"),jo.optString("content"),"",""});
			
			logger.debug("send sms return->"+result);
			if (nds.util.Validator.isNotNull(result)) {
				 try{
					ssmsjo=org.json.XML.toJSONObject(result);
					//String sx=org.json.XML.toString(jo.toString());
					ssmsjo=jo.optJSONObject("xml");
					ssmsjo=ssmsjo.optJSONObject("resp");
					
					SsdjSMSSipStatus sss=SsdjSMSSipStatus.getStatus(ssmsjo.optString("code"));
					if (sss==SsdjSMSSipStatus.SUCCESS) {
						holder.put("code", "0");
						holder.put("message", "发送成功");
					}else {
						holder.put("code", "-1");
						if(sss==null) {holder.put("message", "发送失败");}
						else {holder.put("message", sss.toString());}
							
					}
			        logger.debug("request json->"+jo.toString());
		        }catch(Exception e){
		        	e.printStackTrace();
		        	holder.put("code", "-1");
					holder.put("message", "发送异常");
		        }
			}else {
				holder.put("code", "-1");
				holder.put("message", "发送异常");
			}
			
		} catch (Exception e) {
			holder=new ValueHolder();
			holder.put("code", "-1");
			holder.put("code", "发送异常");
		}
		return holder;
	}

}
