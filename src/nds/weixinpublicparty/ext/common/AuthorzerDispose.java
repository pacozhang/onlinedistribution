package nds.weixinpublicparty.ext.common;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nds.query.QueryEngine;
import nds.weixin.ext.WePublicparty;

import org.json.JSONObject;

public class AuthorzerDispose implements IAuthorizedEventDispose{

	@Override
	public void dispose(HttpServletRequest req, HttpServletResponse rep,WePublicparty wpp, JSONObject jo) {
		
	}

}
