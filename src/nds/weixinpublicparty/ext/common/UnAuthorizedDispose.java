package nds.weixinpublicparty.ext.common;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nds.weixin.ext.WePublicparty;
import nds.weixin.ext.WeUtils;
import nds.weixin.ext.WeUtilsManager;

import org.json.JSONObject;

public class UnAuthorizedDispose implements IAuthorizedEventDispose{

	@Override
	public void dispose(HttpServletRequest req, HttpServletResponse rep,WePublicparty wpp,JSONObject jo) {
		String pappid=jo.optString("AuthorizerAppid");
		WeUtilsManager wum=WeUtilsManager.getInstance();
		WeUtils wu=wum.getByAppid(pappid);
		
		if(wu!=null) {wu.unAuthorized();}
	}
}
