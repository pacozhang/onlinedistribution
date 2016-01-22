package nds.weixin.ext.dispose;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.publicweixin.ext.common.WxPublicControl;
import nds.query.QueryEngine;
import nds.query.QueryException;
import nds.weixin.ext.WeUtils;

import org.json.JSONObject;

public class UnSubscribeDispose implements IMessageDispose {
	private static Logger logger= LoggerManager.getInstance().getLogger(UnSubscribeDispose.class.getName());
	
	@Override
	public void dispose(HttpServletRequest request,HttpServletResponse response, WxPublicControl wpc, JSONObject jo) {
		String sql="update wx_vip_inqury vi set vi.dateout=sysdate,vi.issubscribe='N' where vi.wechatno=? and vi.ad_client_id=?";
		String sqlo="update wx_vip v set v.IFCANCLEATTENTION='N',v.modifieddate=sysdate where v.wechatno=? and v.ad_client_id=?";
		WeUtils wu=wpc.getWxPublic();
		if(wu==null) {return;}
		
		try {
			QueryEngine.getInstance().executeUpdate(sql, new Object[] {jo.optString("FromUserName"),wu.getAd_client_id()});
			QueryEngine.getInstance().executeUpdate(sqlo, new Object[] {jo.optString("FromUserName"),wu.getAd_client_id()});
		} catch (QueryException e) {
			logger.debug("update vip_inqury error->"+e.getMessage());
			e.printStackTrace();
		}
	}

}
