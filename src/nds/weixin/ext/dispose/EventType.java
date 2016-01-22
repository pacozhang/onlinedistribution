package nds.weixin.ext.dispose;

import java.util.HashMap;
import java.util.Map;

public enum EventType {
	image("image","nds.weixin.ext.dispose.ImageDispose"),
	link("link","nds.weixin.ext.dispose.linkDispose"),
	location("location","nds.weixin.ext.dispose.LocationDispose"),
	message("message",""),
	text("text","nds.weixin.ext.dispose.TextDispose"),
	video("video","nds.weixin.ext.dispose.VideoDispose"),
	voice("voice","nds.weixin.ext.dispose.VoiceDispose"),
	event("event","nds.weixin.ext.dispose.EventDispose"),
	subscribe("subscribe","nds.weixin.ext.dispose.SubscribeDispose"),
	unsubscribe("unsubscribe","nds.weixin.ext.dispose.UnSubscribeDispose"),
	scan("scan","nds.weixin.ext.dispose.ScanDispose"),
	//locationEvent("locatione",""),
	click("click","nds.weixin.ext.dispose.ClickDispose"),
	view("view","nds.weixin.ext.dispose.ViewDispose");
	
	private String eType;
    private String eDispose;
    
    EventType(String eventType,String dispose) {
    	this.eType=eventType;
    	this.eDispose=dispose;
    }
    
    public String getEtype() {
    	return this.eType;
    }
    
    public String getEDispose() {
    	return this.eDispose;
    }
    
    private static Map<String,EventType> eTypes ;
    public static EventType getEventType(String eType) {
    	//System.out.print("SipStatus code is"+code);
        if(eTypes == null) {
        	eTypes = new HashMap<String,EventType>();
        	eTypes.put("image", image);
        	eTypes.put("link", link);
        	eTypes.put("location", location);
        	eTypes.put("message", message);
        	eTypes.put("text",text);
        	eTypes.put("video", video);
        	eTypes.put("voice", voice);
        	eTypes.put("event", event);
        	eTypes.put("subscribe", subscribe);
        	eTypes.put("unsubscribe", unsubscribe);
        	eTypes.put("scan", scan);
        	eTypes.put("click", click);
        	eTypes.put("view", view);
        }
        
        return eTypes.get(eType);
    }
}
