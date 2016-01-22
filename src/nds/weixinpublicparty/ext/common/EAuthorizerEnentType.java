package nds.weixinpublicparty.ext.common;

import java.util.Hashtable;


public enum EAuthorizerEnentType {
	unauthorized("unauthorized","nds.weixinpublicparty.ext.common.UnAuthorizedDispose"),
	componentverifyticket("component_verify_ticket","nds.weixinpublicparty.ext.common.ComponentVerifyTicketDispose");
	
	
	private String ekey;
	private String evalue;
	
	EAuthorizerEnentType(String key,String value) {
		this.ekey=key;
		this.evalue=value;
	}
	
	public String getKey() {
		return this.ekey;
	}
	
	public String getValue() {
		return this.evalue;
	}
	
	private static Hashtable<String,EAuthorizerEnentType> eAets;
	public static EAuthorizerEnentType getEAet(String ekey) {
		EAuthorizerEnentType eaet=null;
		if(eAets==null) {
			eAets=new Hashtable<String,EAuthorizerEnentType>();
			eAets.put("unauthorized", unauthorized);
			eAets.put("component_verify_ticket", componentverifyticket);
		}
		
		eaet=eAets.get(ekey);
		return eaet;
	}
}
