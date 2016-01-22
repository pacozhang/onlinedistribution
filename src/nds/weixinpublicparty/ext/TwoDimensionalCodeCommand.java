package nds.weixinpublicparty.ext;

import java.io.File;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.control.web.WebUtils;
import nds.publicplatform.api.WePopularizeSupport;
import nds.publicweixin.ext.common.WxPublicControl;
import nds.security.User;
import nds.util.Configurations;
import nds.util.FileUtils;
import nds.util.NDSException;
import nds.util.WebKeys;
import nds.util.qrcode;
import nds.weixin.ext.WeUtils;
import nds.weixin.ext.WeUtilsManager;

public class TwoDimensionalCodeCommand extends Command {

	@Override
	public ValueHolder execute(DefaultWebEvent event) throws NDSException,RemoteException {
		//获取用户
		User user = this.helper.getOperator(event);
		ValueHolder vh =new ValueHolder();
		if(user==null) {
			logger.debug("publish menu error->user logout");
			vh.put("code","-1");
			vh.put("message","用户不存在，请重新登陆->");
			return vh;
		}
		JSONObject jo = (JSONObject) event.getParameterValue("jsonObject");
		try {
			jo=new JSONObject(jo.optString("params"));
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		logger.debug("jo->"+jo.toString());
		
		//获取与接口相关的信息对象
		WeUtils wu=WeUtilsManager.getByAdClientId(user.adClientId);
		if(wu==null) {
			logger.debug("publish menu error->not find WeUtils WeUtilsManager.getByAdClientId("+user.adClientId+")");
			vh.put("code","-1");
			vh.put("message","请到菜单【微信】的【微信接口配置】中设置APPID与APPSECRET并点击【刷新APP】按钮");
			return vh;
		}
		//判断APPID与APPSECRET是否为空
		if(nds.util.Validator.isNull(wu.getAppId())) {
			logger.debug("publish menu error->appid or appsecret is null[appid:"+wu.getAppId()+"][appsecret:"+wu.getAppSecret()+"]");
			vh.put("code","-1");
			vh.put("message","请到菜单【微信】的【微信接口配置】中设置APPID与APPSECRET并点击【刷新APP】按钮");
			return vh;
		}
		
		WxPublicControl wc=WxPublicControl.getInstance(wu.getAppId());
		if(wc==null) {
			logger.debug("publish menu error->not find WeControl WeControl.getInstance("+wu.getCustomId()+")");
			vh.put("code","-1");
			vh.put("message","请到菜单【微信】的【微信接口配置】中设置APPID与APPSECRET并点击【刷新APP】按钮");
			return vh;
		}

				
		JSONObject pp=null;
		WePopularizeSupport wpz=new WePopularizeSupport();
		String rqcode=jo.optString("rqcodetype","temporary");
		
		if(nds.util.Validator.isNull(rqcode)) {
			vh.put("code", "-1");
			vh.put("message", "请选择二维码类型");
			return vh;
		}
		/*
		int coment=jo.optInt("scene_id",0);
		if(coment<=0) {
			vh.put("code", "-1");
			vh.put("message", "二维码参数必须是1-100000之间的整数");
			return vh;
		}
		*/
		if("temporary".equalsIgnoreCase(rqcode)) {pp=wpz.createTemporaryQuickmark(wc, jo);}
		else if("permanence".equalsIgnoreCase(rqcode)) {pp=wpz.createPermanenceQuickmark(wc, jo);}
		else {
			vh.put("code", -1);
			vh.put("message", "类型只能是临时或永久性二维码。");
			return vh;
		}
		
		if(pp==null) {
			vh.put("code", -1);
			vh.put("message", "系统异常，请重试。");
			return vh;
		}
		String logopath=jo.optString("logopath");
		if(nds.util.Validator.isNotNull(logopath)) {
			 Configurations conf=(Configurations)nds.control.web.WebUtils.getServletContextManager().getActor(nds.util.WebKeys.CONFIGURATIONS);	    
			 String clientWebRoot=conf.getProperty("webclient.upload","/act.net/webhome");
			 logopath=logopath.replace("/servlets/userfolder", "");
			 String path= clientWebRoot+"/"+wu.getDoMain()+logopath;
			 try {
				 jo.put("logopath", path);
			 }catch(Exception e) {
				 
			 }
		}
		
		if(pp.has("url")&&nds.util.Validator.isNotNull(pp.optString("url"))) {
			Configurations conf=(Configurations)WebUtils.getServletContextManager().getActor(WebKeys.CONFIGURATIONS);
		    String m_storageDir = conf.getProperty("webclient.upload", "/act.net/webhome");
		    String svrPath = m_storageDir+ File.separator  + wu.getDoMain()+File.separator+"TwoDimensionalCode";
		    String fileName=new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())+".jpg";
    	    svrPath += File.separator + fileName;
		     
			wpz.encoderQRCode(pp.optString("url"), svrPath, "jpg", 18, jo.optString("logopath"));
			vh.put("code", 0);
			vh.put("message", "创建成功");
			try {
				JSONObject tjos=new JSONObject();
				tjos.put("imgpath", "/servlets/userfolder/TwoDimensionalCode/"+fileName);
				tjos.put("ticket", pp.optString("ticket"));
				tjos.put("url", pp.optString("url"));
				vh.put("data", tjos);
			}catch(Exception e) {
				
			}
			logger.debug(pp.optString("url"));
		}else {
			vh.put("code", pp.optInt("code"));
			vh.put("message", pp.optString("message"));
		}
		return vh;
	}

}
