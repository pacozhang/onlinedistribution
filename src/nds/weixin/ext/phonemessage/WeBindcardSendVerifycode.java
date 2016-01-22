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
	    	holder.put("message", "�󿨷���֤��ʧ�ܣ�");
	    	return holder;
		}
		
		int vipid=jo.optInt("vipid",0);
		if(vipid<=0) {
			logger.debug("bind card send phone message error->vipid isNaN");
	    	holder.put("code", "-1");
	    	holder.put("message", "�󿨷���֤��ʧ�ܣ�");
	    	return holder;
		}
		int userid=893;
		String sql="select t.id from users t join wx_vip v on t.ad_client_id=v.ad_client_id where v.id=? and rownum=1";
	
		try {
			userid=QueryEngine.getInstance().doQueryInt(sql, new Object[] {vipid});
			jo.put("userid", userid);
		}catch(Exception e) {
			
		}
		//�������������
		String verifycode="";
		//������������ַ���
		verifycode=RandomStringUtils.randomNumeric(6);
		
		/*
		int getNum;
		String dn=null;
		Random rd = new Random();
		do {
			getNum = Math.abs(rd.nextInt())%10 + 48;//��������0-9�������
			//getNum = Math.abs(rd.nextInt())%26 + 97;//������ĸa-z�������
			char num1 = (char)getNum;
			dn= Character.toString(num1);
			verifycode += dn;
		} while (verifycode.length()<6);
		*/
		
		
		try {
			logger.debug("bindcard send verifycode->"+verifycode);
			jo.put("content", "�����ڽ��л�Ա�󿨲���,��֤��:"+verifycode+"��������֤��й©!");
		} catch (Exception e) {
			
		}
		logger.debug("bindcard sendphone info->"+jo.toString());
	    WeSendPhoneMessage wspm=new WeSendPhoneMessage();
	    holder=wspm.sendPhoneMessage(jo);
	    if(holder==null) {
	    	logger.debug("bind card send phone message error->result is null");
	    	holder.put("code", "-1");
	    	holder.put("message", "�󿨷���֤��ʧ�ܣ�");
	    	return holder;
	    }
	    JSONObject resultjo=(JSONObject)holder.get("restResult");
	    if(resultjo==null) {
	    	logger.debug("bind card send phone message error->result restResult is not JSONObject");
	    	holder.put("code", "-1");
	    	holder.put("message", "�󿨷���֤��ʧ�ܣ�");
	    	return holder;
	    }
	    
	    int sendcount=resultjo.optInt("count",0);
	    if(sendcount<=0) {
	    	logger.debug("bind card send phone message error->send count is 0");
	    	holder.put("code", "-1");
	    	holder.put("message", "�󿨷���֤��ʧ�ܣ�");
	    	return holder;
	    }
	    
	    //�޸Ļ�Ա���е���֤���ֶ�
	    try {
	    	JSONObject verifycodeinfo=new JSONObject();
	    	verifycodeinfo.put("vipid", vipid);
	    	verifycodeinfo.put("phone", jo.optString("phone"));
	    	verifycodeinfo.put("verifycode",verifycode);
	    	verifycodeinfo.put("verifymessage", "�����ڽ��л�Ա�󿨲���,��֤��:"+verifycode+"��������֤��й©!");
	    	ArrayList params=new ArrayList();
	    	params.add(verifycodeinfo.toString());
	    	ArrayList returns=new ArrayList();
	    	returns.add(java.sql.Clob.class);
	    	
	    	QueryEngine.getInstance().executeFunction("wx_vip_disposeverifycode", params, returns);
	    }catch(Exception e) {
	    	logger.debug("bind card send phone message update vip("+vipid+") error->"+e.getLocalizedMessage());
	    	holder.put("code", "-1");
	    	holder.put("message", "�󿨷���֤��ʧ�ܣ�");
	    	return holder;
	    }
		return holder;
	}
	
}
