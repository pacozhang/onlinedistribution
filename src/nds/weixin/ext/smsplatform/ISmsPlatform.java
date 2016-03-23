package nds.weixin.ext.smsplatform;

import nds.control.util.ValueHolder;

import org.json.JSONObject;

public interface ISmsPlatform {
	public ValueHolder sendMessage(JSONObject jo);
}
