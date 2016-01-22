package nds.weixinpublicparty.ext;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nds.control.web.WebUtils;
import nds.control.web.binhandler.BinaryHandler;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.Tools;
import nds.util.Validator;
import nds.weixin.ext.WePublicparty;
import nds.weixin.ext.WePublicpartyManger;
import nds.weixin.ext.tools.WXBizMsgCrypt;
import nds.weixin.ext.tools.XMLParse;
import nds.weixinpublicparty.ext.common.EAuthorizerEnentType;
import nds.weixinpublicparty.ext.common.IAuthorizedEventDispose;
import org.json.JSONObject;
import org.json.XML;

public class RestWeixin
  implements BinaryHandler
{
  private Logger logger = LoggerManager.getInstance().getLogger(RestWeixin.class.getName());
  private static long NETWORK_DELAY_SECONDS = 600000L;

  public void init(ServletContext context)
  {
    NETWORK_DELAY_SECONDS = Tools.getInt(WebUtils.getProperty("rest.timewindow", "10"), 10) * 1000 * 60;
  }

  public void process(HttpServletRequest request, HttpServletResponse response)
    throws Exception
  {
    String charset = request.getCharacterEncoding();
    charset = Validator.isNull(charset) ? "iso8859-1" : charset;
    InputStream inputStream = request.getInputStream();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    BufferedInputStream bis = null;
    byte[] buf = new byte[1024];
    bis = new BufferedInputStream(inputStream);
    for (int len = 0; (len = bis.read(buf)) != -1; ) {
      baos.write(buf, 0, len);
    }
    inputStream.close();
    String result = baos.toString(charset);

    this.logger.debug("request string->" + result);
    JSONObject jo = null;
    try
    {
      jo = XML.toJSONObject(result);
      jo = jo.optJSONObject("xml");
      this.logger.debug("request json->" + jo.toString());
    } catch (Exception e) {
      this.logger.debug("publicparty error->" + e.getLocalizedMessage());
      e.printStackTrace();
      PrintWriter pw = response.getWriter();
      pw.print("success");
      pw.flush();
      pw.close();
      return;
    }

    String[] tmp = null;
    String params = null;

    this.logger.debug("get pathinfo->" + request.getPathInfo() + ",get servletpath->" + request.getServletPath());
    this.logger.debug("from online get param->" + request.getQueryString());
    this.logger.debug("from online post param->");
    for (Iterator iter = request.getParameterMap().entrySet().iterator(); iter.hasNext(); ) {
      Map.Entry element = (Map.Entry)iter.next();
      tmp = (String[])element.getValue();
      params = String.valueOf(element.getKey() + "::");
      if ((tmp != null) && (tmp.length > 0)) {
        for (int i = 0; i < tmp.length; i++) {
          params = params + tmp[i];
        }
      }
      this.logger.debug(params);
    }

    //String appid = jo.optString("AppId", "wx73b758959e1ef0f2");
    WePublicparty wpp = WePublicpartyManger.getInstance().getWpc();//.getByAppid(appid);
    String appid=wpp.getAppid();
    if (wpp == null) { 
    	logger.error("not find WePublicparty by appid:"+appid);
    	return;
    }

    String msg_signature = request.getParameter("msg_signature");
    String timestamp = request.getParameter("timestamp");
    String nonce = request.getParameter("nonce");
    String signature = request.getParameter("signature");
    String echostr = request.getParameter("echostr");
    boolean issuccess = false;

    result = XMLParse.extract(result);
    this.logger.debug("extract->" + result);
    WXBizMsgCrypt pc = new WXBizMsgCrypt(wpp.getToken(), wpp.getNewencodingaeskey(), appid);
    issuccess = pc.verifyMsg(msg_signature, timestamp, nonce, result);

    if (!issuccess) {
      this.logger.debug("use newaes verifymsg error");
      pc = new WXBizMsgCrypt(wpp.getToken(), wpp.getOldencodingaeskey(), appid);
      issuccess = pc.verifyMsg(msg_signature, timestamp, nonce, result);
      if (issuccess) wpp.setCurrentencodingaeskey(wpp.getOldencodingaeskey());
    }

    if (Validator.isNotNull(echostr)) {
      issuccess = pc.verifyUrl(signature, timestamp, nonce, echostr);
      this.logger.debug("use newaes verifyurl result->" + issuccess);
    }

    if (!issuccess) {
    	logger.error("verify error");
    	return;
    }
    String eventinfo = pc.decryptMsg(result);
    this.logger.debug("eventinfo->" + eventinfo);

    jo = XML.toJSONObject(eventinfo);

    jo = jo.optJSONObject("xml");
    EAuthorizerEnentType eaet = EAuthorizerEnentType.getEAet(jo.optString("InfoType"));

    String classname = eaet.getValue();
    IAuthorizedEventDispose iaed = (IAuthorizedEventDispose)Class.forName(classname).newInstance();
    if (iaed != null) {
      iaed.dispose(request, response, wpp, jo);
    }
    
    PrintWriter pw = response.getWriter();
    pw.print("success");
    pw.flush();
    pw.close();
  }
}