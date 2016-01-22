package nds.weixin.ext.tools;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.weixin.ext.WeUtils;

public class AuthorizeUrl {
	private static Logger logger= LoggerManager.getInstance().getLogger(AuthorizeUrl.class.getName());

	public static String getAuthorizeUrl(WeUtils wu,String url){
	    try {
	    	url = URLEncoder.encode("http://"+wu.getDoMain()+url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	    String red_url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+wu.getAppId()+"&redirect_uri="+url+"&response_type=code&scope=snsapi_base&state=123&component_appid="+wu.getPublicpartyappid()+"#wechat_redirect";
	    logger.debug("redirect_uri----->"+red_url);
		return red_url;
	}
}
