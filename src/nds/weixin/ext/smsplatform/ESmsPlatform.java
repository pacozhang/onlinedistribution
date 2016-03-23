package nds.weixin.ext.smsplatform;

import java.util.HashMap;
import java.util.Map;

public enum ESmsPlatform {
	smsdj("ssdj",""),
	zhutong("zhutong","nds.weixin.ext.smsplatform.ZhutongPhoneMessage");
	
	private String platform;
	private String implpath;
	ESmsPlatform(String platform,String implpath){
		this.platform=platform;
		this.implpath=implpath;
	}
	
	public String getKey() {
		return this.platform;
	}
	
	public String getValue() {
		return this.implpath;
	}
	
	private static Map<String,ESmsPlatform> smsTypes ;
    public static ESmsPlatform getEventType(String eKey) {
        if(smsTypes == null) {
        	smsTypes = new HashMap<String,ESmsPlatform>();
        	smsTypes.put("ssdj", smsdj);
        	smsTypes.put("bindcard", zhutong);
        }
        
        return smsTypes.get(eKey);
    }
}
