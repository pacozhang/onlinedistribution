package nds.weixinpublicparty.ext;

import java.io.Reader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.publicplatform.api.WeSendMessage;
import nds.publicweixin.ext.common.WxPublicControl;
import nds.query.QueryEngine;
import nds.query.QueryException;
import nds.security.User;
import nds.util.NDSException;
import nds.weixin.ext.WeUtils;
import nds.weixin.ext.WeUtilsManager;

public class MassReplyCommand extends Command {

	@Override
	public ValueHolder execute(DefaultWebEvent event) throws NDSException,RemoteException {
		ValueHolder vh =new ValueHolder();
		User user = this.helper.getOperator(event);
		//获取用户
		if(user==null) {
			logger.debug("publish menu error->user logout");
			vh.put("code","-1");
			vh.put("message","用户不存在，请重新登陆->");
			return vh;
		}
		
		
		JSONObject jo = (JSONObject) event.getParameterValue("jsonObject");
		try {
			jo=new JSONObject(jo.optString("params"));
		} catch (JSONException e) {
			logger.debug("parent is->"+jo.optString("params")+"not typeof JSONObject");
			vh.put("code", "-1");
			vh.put("message", "参数异常");
			return vh;
		}
		logger.debug("jo->"+jo.toString());
		
		//获取与接口相关的信息对象
		WeUtils wu=WeUtilsManager.getByAdClientId(user.adClientId);
		if(wu==null) {
			logger.debug("massreply error->not find WeUtils WeUtilsManager.getByAdClientId("+user.adClientId+")");
			vh.put("code","-1");
			vh.put("message","请到菜单【微信】的【微信接口配置】中设置APPID与APPSECRET并点击【刷新APP】按钮");
			return vh;
		}
		//判断APPID与APPSECRET是否为空
		if(nds.util.Validator.isNull(wu.getAppId())) {
			logger.debug("massreply error->appid or appsecret is null[appid:"+wu.getAppId()+"][appsecret:"+wu.getAppSecret()+"]");
			vh.put("code","-1");
			vh.put("message","请到菜单【微信】的【微信接口配置】中设置APPID与APPSECRET并点击【刷新APP】按钮");
			return vh;
		}
		
		WxPublicControl wc=WxPublicControl.getInstance(wu.getAppId());
		if(wc==null) {
			logger.debug("massreply error->not find WeControl WeControl.getInstance("+wu.getCustomId()+")");
			vh.put("code","-1");
			vh.put("message","请到菜单【微信】的【微信接口配置】中设置APPID与APPSECRET并点击【刷新APP】按钮");
			return vh;
		}
		
		
		String str1="";
		String str2="";
		String reply=null;
		String openid=null;
		JSONObject replyjo=null;
		List notifyrecode=null;
		
		java.sql.Clob clob=null;
		Reader inStream=null;
		char[] c=null;
		
		try {
			notifyrecode=QueryEngine.getInstance().doQueryList("select tm.contentjson,nr.massopenid from wx_notifyrecode nr join wx_towxmessage tm on nr.wx_towxmessage_id=tm.id  where nr.id="+jo.optInt("niid",-1));
		}catch(Exception e) {
			logger.debug("massreply error->search notifyrecode");
			e.printStackTrace();
		}
		if(notifyrecode==null||notifyrecode.size()<=0) {
			logger.debug("massreply error->reply is null");
			vh.put("code","-1");
			vh.put("message","数据异常，请重试。");
			return vh;
		}
		clob=(java.sql.Clob)((List)notifyrecode.get(0)).get(0);
		if(clob==null){reply="";}
		else{
			try {
				inStream = clob.getCharacterStream();
				c = new char[(int) clob.length()];
				inStream.read(c);
				reply=new String(c);
				inStream.close();
			}catch(Exception e) {
				
			}
		}
		
		clob=(java.sql.Clob)((List)notifyrecode.get(0)).get(1);
		if(clob==null){openid="";}
		else{
			try {
				inStream = clob.getCharacterStream();
				c = new char[(int) clob.length()];
				inStream.read(c);
				openid=new String(c);
				inStream.close();
			}catch(Exception e) {
				
			}
		}


		if(nds.util.Validator.isNull(reply)) {
			logger.debug("massreply error->reply is null");
			vh.put("code","-1");
			vh.put("message","数据异常，请重试。");
			return vh;
		}

		if(nds.util.Validator.isNull(openid)) {
			vh.put("code", "-1");
			vh.put("message", "找不到相应记录,请重试。");
			logger.debug("not find notifymember id is->"+jo.optInt("niid"));
			return vh;
		}
		
		if("4".equals(wu.getWXType())) {
			str1="https://open.weixin.qq.com/connect/oauth2/authorize?appid="+wu.getAppId()+"&redirect_uri=http://"+wu.getDoMain();
			str2="&response_type=code&scope=snsapi_base&state=1&component_appid="+wu.getPublicpartyappid()+"#wechat_redirect";
			//reply=reply.replaceAll("(?<=(url\":\").*(?=(\")",URLEncoder.encode("$1", "UTF-8"));
		}else {
			str1="";
			str2="";
		}
		reply=reply.replace("@str1@", str1);
		reply=reply.replace("@str2@", str2);
		//notifyjo=replyjo.optJSONObject("reply");
		logger.debug("massreply->"+reply);
		try {
			replyjo=new JSONObject(reply);
			replyjo.put("touser", openid);
		}catch(Exception e) {
			logger.debug("massreply error->reply is not JSONObject");
			vh.put("code","-1");
			vh.put("message","数据异常，请重试。");
			e.printStackTrace();
			return vh;
		}

		WeSendMessage sm=new WeSendMessage();
		JSONObject tempjo=sm.sendMessage(wc,replyjo.toString());
		if(tempjo!=null) {
			vh.put("code",tempjo.optInt("code",-1));
			vh.put("message",tempjo.optString("message"));
			vh.put("data", tempjo);
			try {
				if(tempjo.optInt("code",-1)==0) {
					QueryEngine.getInstance().executeUpdate("update wx_notifyrecode nr set nr.state='Y',nr.modifieddate=sysdate where nr.id=?", new Object[] {jo.optInt("niid",-1)});
				}/*else {
					QueryEngine.getInstance().executeUpdate("update wx_notifymember nm set nm.modifieddate=sysdate where nm.id=?", new Object[] {jo.optInt("niid",-1)});
				}*/
			}catch(Exception e) {
				
			}
		}else {
			vh.put("code","-1");
			vh.put("message","回复失败！");
		}
		
		return vh;
	}

}
