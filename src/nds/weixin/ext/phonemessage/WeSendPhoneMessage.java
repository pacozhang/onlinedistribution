package nds.weixin.ext.phonemessage;

import org.json.JSONObject;

import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.control.web.ClientControllerWebImpl;
import nds.control.web.WebUtils;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.NDSException;

public class WeSendPhoneMessage {
	private static Logger logger= LoggerManager.getInstance().getLogger(WeSendPhoneMessage.class.getName());
	
	public ValueHolder sendPhoneMessage(JSONObject jo) {
		ClientControllerWebImpl controller = (ClientControllerWebImpl)WebUtils.getServletContextManager().getActor(nds.util.WebKeys.WEB_CONTROLLER);
		DefaultWebEvent event = new DefaultWebEvent("CommandEvent");
		event.setParameter("operatorid", jo.optString("userid","893"));
		event.setParameter("command", "nds.monitor.ext.SendSMS");
		event.setParameter("nds.control.ejb.UserTransaction", "N");
		event.put("jsonObject", jo);

	    ValueHolder holder=null;
		try {
			holder = controller.handleEvent(event);
		} catch (NDSException e) {
			e.printStackTrace();
		}
		return holder;
	}
}
