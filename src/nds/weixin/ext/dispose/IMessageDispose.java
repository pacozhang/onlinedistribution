package nds.weixin.ext.dispose;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nds.publicweixin.ext.common.WxPublicControl;

import org.json.JSONObject;

public interface IMessageDispose {
	void dispose(HttpServletRequest request, HttpServletResponse response, WxPublicControl wpc,JSONObject jo);
}
