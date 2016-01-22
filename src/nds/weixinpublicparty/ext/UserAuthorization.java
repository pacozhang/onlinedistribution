package nds.weixinpublicparty.ext;

import java.util.List;

import org.json.JSONObject;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.publicplatform.api.GetUserAuthorization;
import nds.query.QueryEngine;
import nds.weixin.ext.IAuthorization;
import nds.weixin.ext.WeUtils;
import nds.weixin.ext.tools.WeCreateOrSearchVip;

public class UserAuthorization implements IAuthorization{
	private static Logger logger= LoggerManager.getInstance().getLogger(UserAuthorization.class.getName());
	private final static String seachVip="select wv.ID,wv.OPENCARD_STATUS,vi.id,vi.issubscribe,vi.gender,vi.id from wx_vip wv LEFT JOIN wx_vip_inqury vi ON vi.wx_vip_id=wv.id where wv.id=? and wv.AD_CLIENT_ID=? and ROWNUM=1";
	
	
	@Override
	public List getVip(WeUtils wu, JSONObject jo) {
		int vipid=0;
		List vip =null;
		int viptmpid=0;
		String openid=null;
		JSONObject vipjo=null;
		String authorizedcode=jo.optString("code");
		
		GetUserAuthorization gua=GetUserAuthorization.getInstance(wu.getAppId());
		logger.debug("domain->"+wu.getDoMain()+",get access_token=>"+authorizedcode);
		
		//根据CODE获取ACCESS_TOKEN
		vipjo=gua.getAuthorizerUserOpenid(authorizedcode);
		if(vipjo==null||!"0".equals(vipjo.optString("code"))) {return vip;}
		vipjo=vipjo.optJSONObject("data");
		if(vipjo==null||!vipjo.has("openid")) {return vip;}
		
		logger.debug("authorization=>"+jo.toString());
		openid=vipjo.optString("openid");

		try {
			//jo.put("Event", "authorizetion");
			jo.put("Event", vipjo.optString("scope"));
			jo.put("FromUserName", openid);
			jo.put("authorizainfo", vipjo);
		}catch(Exception e) {
			
		}

		WeCreateOrSearchVip wsocvip= new WeCreateOrSearchVip();
		
		//synchronized(this) {
			vipjo=wsocvip.vipDispose(wu, jo);
		//}
		vipid=vipjo.optInt("vipid",-1);
		
		try {
			vip=QueryEngine.getInstance().doQueryList(seachVip, new Object[] {vipid,wu.getAd_client_id()});
		} catch (Exception e) {
			logger.debug("before create vip seach vipid error=>"+e.getMessage());
			e.printStackTrace();
		}
		return vip;
	}

}
