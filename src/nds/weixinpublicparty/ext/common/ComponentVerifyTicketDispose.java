package nds.weixinpublicparty.ext.common;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import nds.weixin.ext.WePublicparty;

public class ComponentVerifyTicketDispose implements IAuthorizedEventDispose{

	@Override
	public void dispose(HttpServletRequest req, HttpServletResponse rep,WePublicparty wpp, JSONObject jo) {
		wpp.setComponent_verify_ticket(jo);
	}

}
