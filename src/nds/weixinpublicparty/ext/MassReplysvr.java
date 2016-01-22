package nds.weixinpublicparty.ext;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import nds.process.SvrProcess;
import nds.publicplatform.api.WeSendMessage;
import nds.publicweixin.ext.common.WxPublicControl;
import nds.query.QueryEngine;
import nds.query.QueryException;
import nds.weixin.ext.WeUtils;
import nds.weixin.ext.WeUtilsManager;

public class MassReplysvr extends SvrProcess {

	@Override
	protected void prepare() {
		// TODO 自动生成的方法存根

	}

	@Override
	protected String doIt() throws Exception {
		//int ad_client_id=this.getAD_Client_ID();
		//int ad_client_id=getProcessInfo().getAD_Client_ID();
		
		ArrayList params=new ArrayList();
		params.add(0);
		ArrayList para=new ArrayList();
		para.add( java.sql.Clob.class);
		
		String resultStr="";
		try {
			Collection list=QueryEngine.getInstance().executeFunction("wx_notifyrecode_massreply",params,para);
			resultStr=(String)list.iterator().next();
			log.debug("FSendReply get oracle result->"+resultStr);
		}catch (QueryException e) {
			log.debug("get FSendReply erroe->"+e.getMessage());
			e.printStackTrace();
			//vh.put("code", "-1");
			//vh.put("message", "获取回复数据异常->"+e.getMessage());
			return null;
		}
		JSONObject tempjo=null;
		JSONArray replyja=null;
		try{
			tempjo=org.json.XML.toJSONObject(resultStr);
			tempjo=tempjo.optJSONObject("xml");
			replyja=tempjo.optJSONArray("replys");
			if(replyja==null||replyja.length()==0) {
				replyja=new JSONArray();
				replyja.put(tempjo.optJSONObject("replys"));
			}
		}catch(Exception e) {
			log.debug("get FSendReply is not a JSONObject");
			e.printStackTrace();
			return null;
		}
		
		
		//URLEncoder.encode("", "UTF-8");
		/*try {
			replyja=new JSONArray(resultStr);
		} catch (JSONException e) {
			log.debug("FSendReply get oracle result is not a JSONArray");
			//vh.put("code", "-1");
			//vh.put("message", "获取回复数据异常->"+e.getMessage());
			return null;
		}*/
		
		String str1="";
		String str2="";
		String reply=null;
		int ad_client_id=0;
		String openid=null;
		int notifyrecordid=0;
		JSONObject replyjo=null;
		JSONObject notifyjo=null;
		boolean isSucces=false;
		
		String sql=null;
		List members=null;
		String membersql=null;
		
		WeUtils wu=null;
		WxPublicControl wc=null;
		WeSendMessage sm=new WeSendMessage();
		for(int i=0;i<replyja.length();i++) {
			replyjo=replyja.optJSONObject(i);
			if(replyjo==null||!replyjo.has("id")) {continue;}
			notifyrecordid=replyjo.optInt("id",-1);
			if(notifyrecordid<0) {
				log.debug("notifyrecordid is null->"+notifyrecordid);
				continue;
			}
			if(!(replyjo.has("reply")&&replyjo.has("members")&&replyjo.has("ad_client_id"))){
				log.debug("reply is error->"+(replyjo==null?"":replyjo.toString()));
				try{
					QueryEngine.getInstance().executeUpdate("update wx_notifyrecode nr set nr.modifieddate=sysdate,nr.errcount=nvl(nr.errcount,0)+1,nr.errorlog='replyjo is error' where nr.id=?", new Object[] {notifyrecordid});
				}catch(Exception e) {
					
				}
				continue;
			}
			
			ad_client_id=replyjo.optInt("ad_client_id",-1);
			if(ad_client_id<=0) {
				try{
					QueryEngine.getInstance().executeUpdate("update wx_notifyrecode nr set nr.modifieddate=sysdate,nr.errcount=nvl(nr.errcount,0)+1,nr.errorlog='ad_client_id error' where nr.id=?", new Object[] {notifyrecordid});
				}catch(Exception e) {
					
				}
				continue;
			}
			
			//获取与接口相关的信息对象
			wu=WeUtilsManager.getByAdClientId(ad_client_id);
			if(wu==null) {
				log.debug("FSendReply error->not find WeUtils WeUtilsManager.getByAdClientId("+ad_client_id+")");
				try{
					QueryEngine.getInstance().executeUpdate("update wx_notifyrecode nr set nr.modifieddate=sysdate,nr.errcount=nvl(nr.errcount,0)+1,nr.errorlog='not find WeUtils by ad_client_id' where nr.id=?", new Object[] {notifyrecordid});
				}catch(Exception e) {
					
				}
				//vh.put("code","-1");
				//vh.put("message","请到菜单【微信】的【微信接口配置】中设置APPID与APPSECRET并点击【刷新APP】按钮");
				continue;
			}

			//判断APPID与APPSECRET是否为空
			if(nds.util.Validator.isNull(wu.getAppId())) {
				log.debug("FSendReply error->appid or appsecret is null[appid:"+wu.getAppId()+"][appsecret:"+wu.getAppSecret()+"]");
				try{
					QueryEngine.getInstance().executeUpdate("update wx_notifyrecode nr set nr.modifieddate=sysdate,nr.errcount=nvl(nr.errcount,0)+1,nr.errorlog='appid or appsecret is null' where nr.id=?", new Object[] {notifyrecordid});
				}catch(Exception e) {
					
				}
				//vh.put("code","-1");
				//vh.put("message","请到菜单【微信】的【微信接口配置】中设置APPID与APPSECRET并点击【刷新APP】按钮");
				continue;
			}
			
			wc=WxPublicControl.getInstance(wu.getAppId());
			if(wc==null) {
				log.debug("FSendReply error->not find WeControl WeControl.getInstance("+wu.getCustomId()+")");
				try{
					QueryEngine.getInstance().executeUpdate("update wx_notifyrecode nr set nr.modifieddate=sysdate,nr.errcount=nvl(nr.errcount,0)+1,nr.errorlog='not find WeControl by customid' where nr.id=?", new Object[] {notifyrecordid});
				}catch(Exception e) {
					
				}
				//vh.put("code","-1");
				//vh.put("message","请到菜单【微信】的【微信接口配置】中设置APPID与APPSECRET并点击【刷新APP】按钮");
				continue;
			}
			
			reply=replyjo.optString("reply");
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
			try {
				notifyjo=new JSONObject(reply);
			}catch(Exception e) {
				e.printStackTrace();
			}
			if(notifyjo==null||!notifyjo.has("msgtype")){
				log.debug("notifyjo is error->"+(notifyjo==null?"":notifyjo.toString()));
				try{
					QueryEngine.getInstance().executeUpdate("update wx_notifyrecode nr set nr.modifieddate=sysdate,nr.errcount=nvl(nr.errcount,0)+1,nr.errorlog='notifyjo is error' where nr.id=?", new Object[] {notifyrecordid});
				}catch(Exception e) {
					
				}
				continue;
			}
			
			//查询会员数据
			membersql=replyjo.optString("members",null);
			if(nds.util.Validator.isNull(membersql)) {
				log.debug("notifyrcord member is null");
				continue;
			}
			sql="select v.wechatno from wx_vip v where v.ad_client_id="+ad_client_id+" and v.id "+membersql;
			try {
				members=QueryEngine.getInstance().doQueryList(sql);
			}catch(Exception e) {
				log.debug("search members error->"+e.getLocalizedMessage());
				e.printStackTrace();
			}
			if(members==null ||members.size()<=0) {
				log.debug("nitifymemberja is error->"+sql);
				try{
					QueryEngine.getInstance().executeUpdate("update wx_notifyrecode nr set nr.modifieddate=sysdate,nr.errcount=nvl(nr.errcount,0)+1,nr.errorlog='nitifymemberja is error' where nr.id=?", new Object[] {notifyrecordid});
				}catch(Exception e) {
					
				}
				continue;
			}
			
			isSucces=false;
			for(int j=0;j<members.size();j++) {
				openid=String.valueOf(members.get(j));
				if(nds.util.Validator.isNull(openid)) {
					log.debug("openid is null->"+openid);
					continue;
				}
				
				notifyjo.put("touser", openid);
				try {
					log.debug("FSendReply send message->"+notifyjo.toString());
					tempjo=sm.sendMessage(wc,notifyjo.toString());
					log.debug("FSendReply send message result openid:"+openid+",notifyrecordid:"+notifyrecordid+",msg->"+(tempjo==null?"":tempjo.toString()));
					if(tempjo!=null) {if(tempjo.optInt("code",-1)==0) {isSucces=true;}}
				}catch(Exception e) {
					log.debug("FSendReply send message error openid:"+openid+",dataid:"+notifyrecordid+",errormsg->"+e.getMessage());
				}
			}
			if(isSucces) {
				try {
					QueryEngine.getInstance().executeUpdate("update wx_notifyrecode nr set nr.state='Y',nr.modifieddate=sysdate where nr.id=?", new Object[] {notifyrecordid});
				}catch(Exception e) {
					log.debug("FSendReply update wx_notifyrecord success error->"+notifyjo.toString());
				}
			}else {
				try {
					QueryEngine.getInstance().executeUpdate("update wx_notifyrecode nr set nr.modifieddate=sysdate,nr.errcount=nvl(nr.errcount,0)+1,nr.errorlog='发送失败' where nr.id=?", new Object[] {notifyrecordid});
				}catch(Exception e) {
					log.debug("FSendReply update wx_notifyrecord fail error->"+notifyjo.toString());
				}
			}
		}
		
		
		return null;
	}

}
