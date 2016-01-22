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
		//��ȡ�û�
		User user = this.helper.getOperator(event);
		ValueHolder vh =new ValueHolder();
		if(user==null) {
			logger.debug("publish menu error->user logout");
			vh.put("code","-1");
			vh.put("message","�û������ڣ������µ�½->");
			return vh;
		}
		JSONObject jo = (JSONObject) event.getParameterValue("jsonObject");
		try {
			jo=new JSONObject(jo.optString("params"));
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		logger.debug("jo->"+jo.toString());
		
		//��ȡ��ӿ���ص���Ϣ����
		WeUtils wu=WeUtilsManager.getByAdClientId(user.adClientId);
		if(wu==null) {
			logger.debug("publish menu error->not find WeUtils WeUtilsManager.getByAdClientId("+user.adClientId+")");
			vh.put("code","-1");
			vh.put("message","�뵽�˵���΢�š��ġ�΢�Žӿ����á�������APPID��APPSECRET�������ˢ��APP����ť");
			return vh;
		}
		//�ж�APPID��APPSECRET�Ƿ�Ϊ��
		if(nds.util.Validator.isNull(wu.getAppId())) {
			logger.debug("publish menu error->appid or appsecret is null[appid:"+wu.getAppId()+"][appsecret:"+wu.getAppSecret()+"]");
			vh.put("code","-1");
			vh.put("message","�뵽�˵���΢�š��ġ�΢�Žӿ����á�������APPID��APPSECRET�������ˢ��APP����ť");
			return vh;
		}
		
		WxPublicControl wc=WxPublicControl.getInstance(wu.getAppId());
		if(wc==null) {
			logger.debug("publish menu error->not find WeControl WeControl.getInstance("+wu.getCustomId()+")");
			vh.put("code","-1");
			vh.put("message","�뵽�˵���΢�š��ġ�΢�Žӿ����á�������APPID��APPSECRET�������ˢ��APP����ť");
			return vh;
		}

				
		JSONObject pp=null;
		WePopularizeSupport wpz=new WePopularizeSupport();
		String rqcode=jo.optString("rqcodetype","temporary");
		
		if(nds.util.Validator.isNull(rqcode)) {
			vh.put("code", "-1");
			vh.put("message", "��ѡ���ά������");
			return vh;
		}
		/*
		int coment=jo.optInt("scene_id",0);
		if(coment<=0) {
			vh.put("code", "-1");
			vh.put("message", "��ά�����������1-100000֮�������");
			return vh;
		}
		*/
		if("temporary".equalsIgnoreCase(rqcode)) {pp=wpz.createTemporaryQuickmark(wc, jo);}
		else if("permanence".equalsIgnoreCase(rqcode)) {pp=wpz.createPermanenceQuickmark(wc, jo);}
		else {
			vh.put("code", -1);
			vh.put("message", "����ֻ������ʱ�������Զ�ά�롣");
			return vh;
		}
		
		if(pp==null) {
			vh.put("code", -1);
			vh.put("message", "ϵͳ�쳣�������ԡ�");
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
			vh.put("message", "�����ɹ�");
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
