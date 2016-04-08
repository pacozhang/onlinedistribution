package nds.weixinpublicparty.ext;

import java.rmi.RemoteException;

import org.json.JSONException;
import org.json.JSONObject;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.query.QueryEngine;
import nds.util.NDSException;

public class ScanGuideRqcodeCommand extends Command{

	@Override
	public ValueHolder execute(DefaultWebEvent event) throws NDSException,
			RemoteException {
		ValueHolder vh =new ValueHolder();
		
		JSONObject messagejo = (JSONObject) event.getParameterValue("jsonObject");
		try {
			messagejo=new JSONObject(messagejo.optString("params"));
		} catch (JSONException e) {
			logger.debug("get params error->"+messagejo.toString());
			e.printStackTrace();
			vh.put("code", "-1");
			vh.put("message", "get params error->"+e.getLocalizedMessage());
			return vh;
		}
		
		if(messagejo==null||!messagejo.has("CommandValue")) {
			logger.debug("params not have CommandValue");
			vh.put("code", "-1");
			vh.put("message", "params not have CommandValue");
			return vh;
		}
		int guideid=messagejo.optInt("CommandValue",-1);
		int ad_client_id =messagejo.optInt("AD_CLIENT_ID",-1);
		if(guideid<=0||ad_client_id<=0) {
			logger.debug("guideid or adclientid error->"+messagejo.toString());
			vh.put("code", "-1");
			vh.put("message", "guideid error");
			return vh;
		}
		
		
		try{
			QueryEngine.getInstance().executeUpdate("update wx_vip v set (v.store_id,v.guide)=(select nvl(g.wx_store_id,v.store_id), nvl(g.id,v.guide) from wx_guide g where g.id=?) where v.wechatno=? and v.ad_client_id=? ",new Object[]{guideid,messagejo.optString("ToUserName"),ad_client_id});
			vh.put("code", "0");
			vh.put("message", "update vip guide success");
		}catch(Exception e){
			logger.debug("update vip guide error->"+e.getLocalizedMessage());
			vh.put("code", "-1");
			vh.put("message", "update vip guide error->"+e.getLocalizedMessage());
			return vh;
		}
		
		return vh;
	}

}
