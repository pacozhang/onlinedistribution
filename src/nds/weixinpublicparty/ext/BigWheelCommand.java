package nds.weixinpublicparty.ext;

import java.rmi.RemoteException;

import org.json.JSONObject;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.util.NDSException;

public class BigWheelCommand extends Command {

	@Override
	public ValueHolder execute(DefaultWebEvent event) throws NDSException,
			RemoteException {
		ValueHolder vh = new ValueHolder();
		JSONObject jo = (JSONObject) event.getParameterValue("jsonObject");
		try {
			jo = jo.optJSONObject("params");
			logger.debug("WX_BIGWHEEL params adClientId->"+jo.optString("adClientId"));
			logger.debug("WX_BIGWHEEL params wxBigWheelId->"+jo.optString("wxBigWheelId"));
			logger.debug("WX_BIGWHEEL params wxVipId->"+jo.optString("wxVipId"));
		} catch (Exception e) {
			logger.debug("WX_BIGWHEEL error->params error");
			vh.put("code", "-1");
			vh.put("message", "params error");
			return vh;
		}

		BigWheel bigwheel = BigWheel.getInstance(jo.optString("adClientId"));
		try {
			String result = null;
			result = bigwheel.bigwheel(jo.optString("adClientId"),
					jo.optString("wxBigWheelId"), jo.optString("wxVipId"));
			vh.put("code", "0");
			vh.put("message", result);
		} catch (Exception e) {
			logger.debug("WX_BIGWHEEL error->exception error");
			vh.put("code", "-1");
			vh.put("message", "exception error->" + e.getMessage());
			return vh;
		}
		return vh;
	}

}
