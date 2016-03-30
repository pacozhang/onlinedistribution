package nds.weixin.ext.phonemessage;

import java.util.ArrayList;

import nds.control.util.ValueHolder;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.QueryEngine;

import org.apache.commons.lang.RandomStringUtils;
import org.json.JSONObject;

public class WeUpdatecardSendVerifycode implements ISendSMSMessage{
	private static Logger logger= LoggerManager.getInstance().getLogger(WeUpdatecardSendVerifycode.class.getName());

	@Override
	public ValueHolder sendmessage(JSONObject jo) {
		ValueHolder holder = new ValueHolder();
		if(jo==null) {
			logger.debug("bind card send phone message error->params is null");
	    	holder.put("code", "-1");
	    	holder.put("message", "完善资料发验证码失败！");
	    	return holder;
		}
		
		int vipid=jo.optInt("vipid",0);
		if(vipid<=0) {
			logger.debug("bind card send phone message error->vipid isNaN");
	    	holder.put("code", "-1");
	    	holder.put("message", "完善资料证码失败！");
	    	return holder;
		}
		
		//生成随机六数字
		String verifycode="";
		//生成随机数字字符串
		verifycode=RandomStringUtils.randomNumeric(6);
		
		
		try {
			logger.debug("bindcard send verifycode->"+verifycode);
			jo.put("verifycode", verifycode);
			jo.put("content", "您正在完善资料,验证码:"+verifycode+"，请务将验证码泄漏!");
		} catch (Exception e) {
			
		}
		logger.debug("bindcard sendphone info->"+jo.toString());
		
		holder=WeSendSMS.sendsms(jo);
		if(holder==null||!holder.get("code").equals("0")) {
	    	logger.debug("bind card send phone message error->result is null or error");
	    	holder.put("code", "-1");
	    	holder.put("message", "完善资料发验证码失败！");
	    	return holder;
	    }
		
		//修改会员表中的验证码字段
	    try {
	    	JSONObject verifycodeinfo=new JSONObject();
	    	verifycodeinfo.put("vipid", jo.optInt("vipid"));
	    	verifycodeinfo.put("phone", jo.optString("phone"));
	    	verifycodeinfo.put("verifycode",jo.optString("verifycode"));
	    	verifycodeinfo.put("verifymessage", jo.optString("content"));
	    	ArrayList params=new ArrayList();
	    	params.add(verifycodeinfo.toString());
	    	ArrayList returns=new ArrayList();
	    	returns.add(java.sql.Clob.class);
	    	
	    	QueryEngine.getInstance().executeFunction("wx_vip_disposeverifycode", params, returns);
	    }catch(Exception e) {
	    	logger.debug("send phone message update vip("+jo.optInt("vipid")+") error->"+e.getLocalizedMessage());
	    	e.printStackTrace();
	    	holder.put("code", "-1");
	    	holder.put("message", "验证码发送失败！");
	    	return holder;
	    }
		
		
		return holder;
	}

}
