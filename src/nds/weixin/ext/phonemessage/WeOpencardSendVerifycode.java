package nds.weixin.ext.phonemessage;

import java.util.ArrayList;
import java.util.Random;

import nds.control.util.ValueHolder;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.QueryEngine;

import org.json.JSONObject;

import antlr.collections.List;

public class WeOpencardSendVerifycode implements ISendSMSMessage{
	private static Logger logger= LoggerManager.getInstance().getLogger(WeOpencardSendVerifycode.class.getName());
	
	@Override
	public ValueHolder sendmessage(JSONObject jo) {
		ValueHolder holder = null;
		
		if(jo==null) {
			logger.debug("open card send phone message error->params is null");
	    	holder=new ValueHolder();
	    	holder.put("code", "-1");
	    	holder.put("message", "验证码发送失败！");
	    	return holder;
		}
		
		int vipid=jo.optInt("vipid",0);
		if(vipid<=0) {
			logger.debug("open card send phone message error->vipid isNaN");
	    	holder=new ValueHolder();
	    	holder.put("code", "-1");
	    	holder.put("message", "验证码发送失败！");
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
		int getNum;
		String dn=null;
		Random rd = new Random();
		do {
			getNum = Math.abs(rd.nextInt())%10 + 48;//产生数字0-9的随机数
			//getNum = Math.abs(rd.nextInt())%26 + 97;//产生字母a-z的随机数
			char num1 = (char)getNum;
			dn= Character.toString(num1);
			verifycode += dn;
		} while (verifycode.length()<6);
		
		try {
			logger.debug("opencard send verifycode is->"+verifycode);
			jo.put("verifycode", verifycode);
			jo.put("content", "您正在进行会员开卡操作,验证码:"+verifycode+"，请务将验证码泄漏!");
		} catch (Exception e) {
			
		}
		logger.debug("opencard sendphone info->"+jo.toString());
		
		holder=WeSendSMS.sendsms(jo);
		if(holder==null||!holder.get("code").equals("0")) {
	    	logger.debug("bind card send phone message error->result is null or error");
	    	holder.put("code", "-1");
	    	holder.put("message", "验证码发送失败！");
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
	    	logger.debug("open card send phone message error->result is null");
	    	holder=new ValueHolder();
	    	holder.put("code", "-1");
	    	holder.put("message", "开卡发验证码失败！");
	    	return holder;
	    }
	    JSONObject resultjo=(JSONObject)holder.get("restResult");
	    if(resultjo==null) {
	    	logger.debug("open card send phone message error->result restResult is not JSONObject");
	    	holder.put("code", "-1");
	    	holder.put("message", "开卡发验证码失败！");
	    	return holder;
	    }
	    
	    int sendcount=resultjo.optInt("count",0);
	    if(sendcount<=0) {
	    	logger.debug("open card send phone message error->send count is 0");
	    	holder.put("code", "-1");
	    	holder.put("message", "开卡发验证码失败！");
	    	return holder;
	    }
	    */
	    
	    
		return holder;
	}

}
