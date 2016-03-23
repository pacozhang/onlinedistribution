package nds.weixin.ext.phonemessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import nds.control.util.ValueHolder;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.QueryEngine;
import nds.rest.RestUtils;
import nds.weixin.ext.SipStatus;

import org.apache.commons.lang.RandomStringUtils;
import org.json.JSONObject;

public class WeBindcardSendVerifycode implements ISendSMSMessage{
	private static Logger logger= LoggerManager.getInstance().getLogger(WeBindcardSendVerifycode.class.getName());
	
	@Override
	public ValueHolder sendmessage(JSONObject jo) {
		ValueHolder holder = new ValueHolder();
		if(jo==null) {
			logger.debug("bind card send phone message error->params is null");
	    	holder.put("code", "-1");
	    	holder.put("message", "绑卡发验证码失败！");
	    	return holder;
		}
		
		int vipid=jo.optInt("vipid",0);
		if(vipid<=0) {
			logger.debug("bind card send phone message error->vipid isNaN");
	    	holder.put("code", "-1");
	    	holder.put("message", "绑卡发验证码失败！");
	    	return holder;
		}
		int userid=893;
		String sql="select t.id from users t join wx_vip v on t.ad_client_id=v.ad_client_id where v.id=? and rownum=1";
	
		try {
			userid=QueryEngine.getInstance().doQueryInt(sql, new Object[] {vipid});
			jo.put("userid", userid);
		}catch(Exception e) {
			
		}
		//生成随机六数字
		String verifycode="";
		//生成随机数字字符串
		verifycode=RandomStringUtils.randomNumeric(6);
		
		
		try {
			logger.debug("bindcard send verifycode->"+verifycode);
			jo.put("verifycode", verifycode);
			jo.put("content", "您正在进行会员绑卡操作,验证码:"+verifycode+"，请务将验证码泄漏!");
		} catch (Exception e) {
			
		}
		logger.debug("bindcard sendphone info->"+jo.toString());
		
		holder=WeSendSMS.sendsms(jo);
		if(holder==null||!holder.get("code").equals("0")) {
	    	logger.debug("bind card send phone message error->result is null or error");
	    	holder.put("code", "-1");
	    	holder.put("message", "绑卡发验证码失败！");
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
		
		/*
	    WeSendPhoneMessage wspm=new WeSendPhoneMessage();
	    holder=wspm.sendPhoneMessage(jo);
	    if(holder==null) {
	    	logger.debug("bind card send phone message error->result is null");
	    	holder.put("code", "-1");
	    	holder.put("message", "绑卡发验证码失败！");
	    	return holder;
	    }
	    JSONObject resultjo=(JSONObject)holder.get("restResult");
	    if(resultjo==null) {
	    	logger.debug("bind card send phone message error->result restResult is not JSONObject");
	    	holder.put("code", "-1");
	    	holder.put("message", "绑卡发验证码失败！");
	    	return holder;
	    }
	    
	    int sendcount=resultjo.optInt("count",0);
	    if(sendcount<=0) {
	    	logger.debug("bind card send phone message error->send count is 0");
	    	holder.put("code", "-1");
	    	holder.put("message", "绑卡发验证码失败！");
	    	return holder;
	    }
	    
	    //修改会员表中的验证码字段
	    try {
	    	JSONObject verifycodeinfo=new JSONObject();
	    	verifycodeinfo.put("vipid", vipid);
	    	verifycodeinfo.put("phone", jo.optString("phone"));
	    	verifycodeinfo.put("verifycode",verifycode);
	    	verifycodeinfo.put("verifymessage", "您正在进行会员绑卡操作,验证码:"+verifycode+"，请务将验证码泄漏!");
	    	ArrayList params=new ArrayList();
	    	params.add(verifycodeinfo.toString());
	    	ArrayList returns=new ArrayList();
	    	returns.add(java.sql.Clob.class);
	    	
	    	QueryEngine.getInstance().executeFunction("wx_vip_disposeverifycode", params, returns);
	    }catch(Exception e) {
	    	logger.debug("bind card send phone message update vip("+vipid+") error->"+e.getLocalizedMessage());
	    	holder.put("code", "-1");
	    	holder.put("message", "绑卡发验证码失败！");
	    	return holder;
	    }
	    */
		return holder;
	}
	
}
