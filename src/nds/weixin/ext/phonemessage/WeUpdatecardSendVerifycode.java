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
	    	holder.put("message", "�������Ϸ���֤��ʧ�ܣ�");
	    	return holder;
		}
		
		int vipid=jo.optInt("vipid",0);
		if(vipid<=0) {
			logger.debug("bind card send phone message error->vipid isNaN");
	    	holder.put("code", "-1");
	    	holder.put("message", "��������֤��ʧ�ܣ�");
	    	return holder;
		}
		
		//�������������
		String verifycode="";
		//������������ַ���
		verifycode=RandomStringUtils.randomNumeric(6);
		
		
		try {
			logger.debug("bindcard send verifycode->"+verifycode);
			jo.put("verifycode", verifycode);
			jo.put("content", "��������������,��֤��:"+verifycode+"��������֤��й©!");
		} catch (Exception e) {
			
		}
		logger.debug("bindcard sendphone info->"+jo.toString());
		
		holder=WeSendSMS.sendsms(jo);
		if(holder==null||!holder.get("code").equals("0")) {
	    	logger.debug("bind card send phone message error->result is null or error");
	    	holder.put("code", "-1");
	    	holder.put("message", "�������Ϸ���֤��ʧ�ܣ�");
	    	return holder;
	    }
		
		//�޸Ļ�Ա���е���֤���ֶ�
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
	    	holder.put("message", "��֤�뷢��ʧ�ܣ�");
	    	return holder;
	    }
		
		
		return holder;
	}

}
