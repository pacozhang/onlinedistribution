package nds.weixinpublicparty.ext;

import java.rmi.RemoteException;
import java.util.Random;

import org.json.JSONObject;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.control.web.ClientControllerWebImpl;
import nds.control.web.WebUtils;
import nds.query.QueryEngine;
import nds.security.User;
import nds.util.NDSException;
import nds.util.Tools;
import nds.weixin.ext.dispose.IMessageDispose;
import nds.weixin.ext.phonemessage.ESendPhoneType;
import nds.weixin.ext.phonemessage.ISendSMSMessage;

public class SendPhoneMessageCommand extends Command{

	@Override
	public ValueHolder execute(DefaultWebEvent event) throws NDSException,
			RemoteException {
		ValueHolder vh =null;
		//获取用户
		
		JSONObject jo = (JSONObject) event.getParameterValue("jsonObject");
		try {
			jo=new JSONObject(jo.optString("params"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		//判断客户金额是否足够
		int canusesms=0;
		canusesms=QueryEngine.getInstance().doQueryInt("select sms.canusenumber-GETSENDSMSCOUNT(sms.ad_client_id) from wx_smsinfo sms where sms.ad_client_id=?", new Object[] {jo.optString("companyid")});
		logger.debug("send phone message canusenumber is->"+canusesms);
		if(canusesms<=0) {
			logger.debug("send phone message error->company smsnumber is "+canusesms+",ad_client_id:"+jo.optString("companyid"));
			vh=new ValueHolder();
			vh.put("code","-1");
			vh.put("message","公司短信功能异常，请联系商家。");
			return vh;
		}
		
		//获取发送短信对象
		String method=jo.optString("method");
		ESendPhoneType sendtype=ESendPhoneType.getEventType(method);
		if(sendtype==null) {
			logger.debug("send phone message error->send method is not find");
			vh=new ValueHolder();
			vh.put("code", "-1");
			vh.put("message", "发送验证码失败");
			return vh;
		}
		ISendSMSMessage sendmessage=null;
		try {
			sendmessage=(ISendSMSMessage)Class.forName(sendtype.getValue()).newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(sendmessage==null) {
			logger.debug("send phone message error->not find sendmessage program");
			vh=new ValueHolder();
			vh.put("code", "-1");
			vh.put("message", "发送验证码失败");
			return vh;
		}
		
		logger.debug("sendphone info->"+jo.toString());
		vh=sendmessage.sendmessage(jo);
		return vh;
	}

}
