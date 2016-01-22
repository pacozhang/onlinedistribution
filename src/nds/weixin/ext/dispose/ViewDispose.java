package nds.weixin.ext.dispose;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.publicweixin.ext.common.WxPublicControl;

import org.json.JSONObject;

public class ViewDispose implements IMessageDispose {
	private static Logger logger= LoggerManager.getInstance().getLogger(ViewDispose.class.getName());
	
	@Override
	public void dispose(HttpServletRequest request,HttpServletResponse response, WxPublicControl wpc, JSONObject jo) {
		logger.debug("viewDispose->");
	}

}
