package nds.weixinpublicparty.ext;

import java.rmi.RemoteException;

import org.json.JSONException;
import org.json.JSONObject;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.query.QueryEngine;
import nds.util.NDSException;

public class ScanSotreRqcodeCommand extends Command{

	@Override
	public ValueHolder execute(DefaultWebEvent event) throws NDSException,
			RemoteException {
		ValueHolder vh=new ValueHolder();
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
		int storeid=messagejo.optInt("CommandValue",-1);
		int ad_client_id =messagejo.optInt("AD_CLIENT_ID",-1);
		if(storeid<=0||ad_client_id<=0) {
			logger.debug("vipid or adclientid error->"+messagejo.toString());
			vh.put("code", "-1");
			vh.put("message", "vipid error");
			return vh;
		}
		
		try {
			QueryEngine.getInstance().executeUpdate("update wx_vip v set v.store_id=?,v.modifieddate=sysdate where v.wechatno=? and v.ad_client_id=? and v.store_id is null", new Object[] {storeid,messagejo.optString("ToUserName"),ad_client_id});
			vh.put("code", "0");
			vh.put("message", "update vip store_id success");
		} catch (Exception e) {
			logger.debug("update vip store_id error->"+e.getLocalizedMessage());
			vh.put("code", "-1");
			vh.put("message", "update vip store_id error->"+e.getLocalizedMessage());
			return vh;
		}
		return vh;
	}

}
