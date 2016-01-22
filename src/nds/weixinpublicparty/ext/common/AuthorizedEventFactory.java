package nds.weixinpublicparty.ext.common;

import java.util.Hashtable;

import nds.log.Logger;
import nds.log.LoggerManager;

public class AuthorizedEventFactory {
	private String appid;
	private Hashtable<String,IAuthorizedEventDispose> disposes;
	private static final String CONTENT_TYPE_TEXT = "text/html; charset=UTF-8";
	
	private static Logger logger= LoggerManager.getInstance().getLogger(AuthorizedEventFactory.class.getName());
	private static Hashtable<String,AuthorizedEventFactory> factorys;
	
}
