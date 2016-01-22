package nds.weixin.ext.phonemessage;

import nds.control.util.ValueHolder;

import org.json.JSONObject;

public interface ISendSMSMessage {
	public ValueHolder sendmessage(JSONObject jo);
}
