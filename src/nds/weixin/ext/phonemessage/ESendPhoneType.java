package nds.weixin.ext.phonemessage;

import java.util.HashMap;
import java.util.Map;

public enum ESendPhoneType {
	opencard("opencard","nds.weixin.ext.phonemessage.WeOpencardSendVerifycode"),
	bindcard("bindcard","nds.weixin.ext.phonemessage.WeBindcardSendVerifycode");
	
	private String eKey;
	private String eValue;
	
	ESendPhoneType(String key,String value) {
		this.eKey=key;
		this.eValue=value;
	}
	
	public String getKey() {
		return this.eKey;
	}
	
	public String getValue() {
		return this.eValue;
	}
	
	private static Map<String,ESendPhoneType> eSendType ;
    public static ESendPhoneType getEventType(String eKey) {
        if(eSendType == null) {
        	eSendType = new HashMap<String,ESendPhoneType>();
        	eSendType.put("opencard", opencard);
        	eSendType.put("bindcard", bindcard);
        }
        
        return eSendType.get(eKey);
    }
}
