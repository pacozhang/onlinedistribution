package nds.weixinpublicparty.ext;

import java.rmi.RemoteException;
import java.util.List;

import org.json.JSONObject;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.query.QueryEngine;
import nds.util.NDSException;
import nds.weixin.ext.tools.VipPhoneVerifyCode;

public class ValidationVipVerifycodeCommand  extends Command{

	@Override
	public ValueHolder execute(DefaultWebEvent event) throws NDSException,
			RemoteException {
		JSONObject jo=null;
		ValueHolder holder = new ValueHolder();
		try {
			jo = (JSONObject) event.getParameterValue("jsonObject");
			jo=jo.optJSONObject("params");
		}catch(Exception e) {
			jo=null;
		}
		logger.debug("verify code params->"+jo.toString());
		if(jo==null||jo==JSONObject.NULL){
			logger.debug("verify code error->params is null");
	    	
	    	holder.put("code", "-1");
	    	holder.put("message", "参数异常");
	    	return holder;
		}
		
		int vipid=jo.optInt("vipid",0);
		String verifycode=jo.optString("verifycode");
		String phone=jo.optString("phone");
		if(vipid<=0) {
			logger.debug("verify code error->vipid isNaN");
	    	
	    	holder.put("code", "-1");
	    	holder.put("message", "VIP信息异常！");
	    	return holder;
		}
		if(nds.util.Validator.isNull(verifycode)) {
			logger.debug("verify code error->verifycode isnull");
	    	
	    	holder.put("code", "-1");
	    	holder.put("message", "验证码不能为空！");
	    	return holder;
		}
		holder=VipPhoneVerifyCode.verifyphonecode(vipid, phone, verifycode);
		
		/*
		List vipinfo=null;
		try {
			vipinfo=QueryEngine.getInstance().doQueryList("select v.verifycode from wx_vip v where v.id=? and v.verifycode=?",new Object[] {vipid,verifycode});
		}catch(Exception e) {
			logger.debug("verify code error->"+e.getLocalizedMessage());
			vipinfo=null;
		}
		
		if(vipinfo==null||vipinfo.size()<=0) {
			holder=new ValueHolder();
	    	holder.put("code", "-1");
	    	holder.put("message", "验证码错误！");
	    	return holder;
		}
		
		holder=new ValueHolder();
    	holder.put("code", "0");
    	holder.put("message", "验证成功！");*/
    	return holder;
	}

}
