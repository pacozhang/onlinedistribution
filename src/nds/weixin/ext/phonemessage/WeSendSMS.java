package nds.weixin.ext.phonemessage;

import java.util.ArrayList;
import java.util.List;

import nds.control.util.ValueHolder;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.QueryEngine;
import nds.weixin.ext.smsplatform.ESmsPlatform;
import nds.weixin.ext.smsplatform.ISmsPlatform;

import org.json.JSONObject;

public class WeSendSMS {
	private static Logger logger= LoggerManager.getInstance().getLogger(WeSendSMS.class.getName());
	
	
	public static ValueHolder sendsms(JSONObject jo) {
		ValueHolder vh=new ValueHolder();
		
		//获取客户短信设置
		List smsinfo=null;
		try {
			smsinfo=QueryEngine.getInstance().doQueryList("select s.smsplatform,s.smsurl,s.username,s.userpwd from wx_smsinfo s where s.ad_client_id=?",new Object[] {jo.optInt("companyid")});
		} catch (Exception e) {
			logger.warning("send sms find smsinfo error:"+e.getLocalizedMessage());
			e.printStackTrace();
		}
		
		if(smsinfo==null||smsinfo.isEmpty()) {
			logger.warning("not find smsinfo by companyid:"+jo.optInt("companyid"));
			vh.put("code", "-1");
			vh.put("message","短信发送失败");
			return vh;
		}
		smsinfo=(List)smsinfo.get(0);
		
		String platfrom=String.valueOf(smsinfo.get(0));
		ESmsPlatform epf=ESmsPlatform.getEventType(platfrom);
		if(epf==null) {
			logger.warning("not find ESmsPlatform by key:"+platfrom);
			vh.put("code", "-1");
			vh.put("message","短信发送失败");
			return vh;
		}
		
		ISmsPlatform sendsms=null;
		try {
			sendsms=(ISmsPlatform)Class.forName(epf.getValue()).newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(sendsms==null) {
			logger.warning("send phone message error->not find ISmsPlatform program");
			vh=new ValueHolder();
			vh.put("code", "-1");
			vh.put("message", "短信发送失败");
			return vh;
		}
		
		try {
			jo.put("url", String.valueOf(smsinfo.get(1)));
			jo.put("username", String.valueOf(smsinfo.get(2)));
			jo.put("userpwd", String.valueOf(smsinfo.get(3)));
		} catch (Exception e) {
			
		}
		
		//短信发送
		vh=sendsms.sendMessage(jo);
		return vh;
	}
}
