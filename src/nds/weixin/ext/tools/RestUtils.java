package nds.weixin.ext.tools;
import nds.util.*;
import nds.control.util.*;
import nds.control.web.WebUtils;
import nds.control.web.binhandler.Rest;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.security.*;

import java.util.*;
import java.net.*;
import java.io.*;

import javax.activation.MimetypesFileTypeMap;
import javax.net.ssl.HttpsURLConnection;

public class RestUtils {
	private static Logger logger= LoggerManager.getInstance().getLogger(RestUtils.class.getName());	 
	
	public static ValueHolder sendRequest(String apiURL, Map<String, String> params, String method,String filePath) throws Exception {
		ValueHolder vh=null;
		if(nds.util.Validator.isNotNull(filePath)){
			File file=new File(filePath);
			vh=sendRequest(apiURL,params,method,file);
		}else{
			vh=new ValueHolder();
			vh.put("code",String.valueOf(-1));
			vh.put("message", "filepath is null!");
			vh.put("queryString",(null == params) ? "" : delimit(params.entrySet(),true	) );
		}
		
		return vh;
	}
	
	public static ValueHolder sendRequest(String apiURL, Map<String, String> params, String method,File file) throws Exception {
		ValueHolder vh=new ValueHolder();
		String queryString = (null == params) ? "" : delimit(params.entrySet(),true	);
		String BOUNDARY = "----------" + System.currentTimeMillis();
		
		if(file==null||!file.exists()){
			vh.put("code",String.valueOf(-1));
			vh.put("message", "file is not exists!");
			vh.put("queryString",queryString );
			return vh;
		}

		logger.debug("url->"+apiURL);
		logger.debug("queryString :"+queryString);
        HttpURLConnection conn = (HttpURLConnection) new URL(apiURL).openConnection();
       
        
        URL U=new URL(apiURL);
        conn.setConnectTimeout(6* 1000);
		
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setRequestMethod(method);
		conn.setUseCaches(false);
		conn.setRequestProperty("Connection", "Keep-Alive");  
		conn.setRequestProperty("Charset", "UTF-8");
        //conn.setRequestProperty("User-Agent","Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)");  
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary="+BOUNDARY);  
        
        conn.getOutputStream().write(queryString.getBytes("utf-8"));	
        conn.connect();
		String contentType = new MimetypesFileTypeMap().getContentType(file); 

        StringBuffer strBuf = new StringBuffer();  
        strBuf.append("--").append(BOUNDARY).append("\r\n");  
        strBuf.append("Content-Disposition: form-data; name=\"media\"; filelength=\""+file.length()+"\"; filename=\"" + file.getName() + "\"\r\n");  
        strBuf.append("Content-Type:" + contentType + "\r\n\r\n");  

        OutputStream out = new DataOutputStream(conn.getOutputStream());  
        out.write(strBuf.toString().getBytes("utf-8")); 
        
		DataInputStream in = new DataInputStream(new FileInputStream(file));  
		int bytes = 0;
		byte[] bufferOut = new byte[1024];
		while ((bytes = in.read(bufferOut)) != -1) {
			out.write(bufferOut, 0, bytes);
		}
        in.close();  
        out.write(("\r\n--"+BOUNDARY+"--\r\n").getBytes("utf-8"));
        out.flush();
        out.close();
        conn.disconnect();
        
		String charset = getChareset(conn.getContentType());
		if (conn.getResponseCode() != 200) {
			logger.debug("server status->"+conn.getResponseCode());
			throw new NDSException("«Î«Ûurl ß∞‹");
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), charset));
		
		StringBuffer buffer = new StringBuffer();
		String line;
		while ((line = reader.readLine()) != null) {
			buffer.append(line);
		}
		logger.debug(Tools.toString( conn.getHeaderFields()));
        SipStatus status =SipStatus.getStatus( conn.getHeaderField("sip_status"));
		reader.close();
        conn.disconnect();
		String msg= buffer.toString();
		

		int code=0;
		if(status!=null){
			if(status.equals( SipStatus.SUCCESS)){
				code=0;
			}else{
				code=- nds.util.Tools.getInt(status.getCode(),1);
				if(code==0){
					// SipStatus.ERROR =0
					code=-1;
				}
			}
		}
		
		vh.put("code",String.valueOf(code));
		vh.put("message", msg);
		vh.put("queryString",queryString );
        //SipResult result = new SipResult(SipStatus.getStatus(code),buffer.toString());

        return vh;
	}
	
	public static ValueHolder sendRequest(String apiURL, Map<String, String> params, String method) throws Exception {
        String queryString = (null == params) ? "" : delimit(params.entrySet(),true	);
        logger.debug("url->"+apiURL);
		logger.debug("queryString :"+queryString);
		
        HttpURLConnection conn = (HttpURLConnection) new URL(apiURL).openConnection();
        //HttpsURLConnection conn=(HttpsURLConnection) new URL(apiURL).openConnection();
        
	    conn.setConnectTimeout(6* 1000);
		conn.setRequestMethod(method);
		conn.setDoOutput(true); 
		conn.connect();
		
		conn.getOutputStream().write(queryString.getBytes("utf-8"));
		if (conn.getResponseCode() != 200) {
			logger.debug("server status->"+conn.getResponseCode());
			throw new NDSException("«Î«Ûurl ß∞‹");
		}
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BufferedInputStream bis = null;
		byte[] buf = new byte[1024];
		bis = new BufferedInputStream(conn.getInputStream());
		for (int len = 0; (len = bis.read(buf)) != -1;){
			baos.write(buf,0,len);
		}
		
		String charset = getChareset(conn.getContentType());
		/*
		BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), charset));
		StringBuffer buffer = new StringBuffer();
		String line;
		while ((line = reader.readLine()) != null) {
			buffer.append(line);
		}
		reader.close();
		*/
		logger.debug(Tools.toString( conn.getHeaderFields()));
        SipStatus status =SipStatus.getStatus( conn.getHeaderField("sip_status"));
		
        conn.disconnect();
		//String msg= buffer.toString();
		ValueHolder vh=new ValueHolder();

		int code=0;
		if(status!=null){
			if(status.equals( SipStatus.SUCCESS)){
				code=0;
			}else{
				code=- nds.util.Tools.getInt(status.getCode(),1);
				if(code==0){
					// SipStatus.ERROR =0
					code=-1;
				}
			}
		}
		
		vh.put("code",String.valueOf(code));
		//vh.put("message", msg);
		vh.put("message", baos.toString(charset));
		vh.put("queryString",queryString );
        //SipResult result = new SipResult(SipStatus.getStatus(code),buffer.toString());

        return vh;
	}
	
	public static ValueHolder sendRequest_buffs(String apiURL,  Map<String, String> params, String method) throws Exception {
		ValueHolder vh=new ValueHolder();
		String queryString = (null == params||params.isEmpty()) ? "" : delimit(params.entrySet(),true	);
		
		logger.debug("url->"+apiURL);
		logger.debug("queryString :"+queryString);
        HttpURLConnection conn = (HttpURLConnection) new URL(apiURL).openConnection();
        conn.setRequestMethod(method);
		//conn.setRequestProperty("Content-Type", contentType);  
        if(nds.util.Validator.isNotNull(queryString)){
        	conn.setDoInput(true);
        	conn.setDoOutput(true);
        	conn.setUseCaches(false);
        	conn.setInstanceFollowRedirects(false); 
        	conn.getOutputStream().write(queryString.getBytes("utf-8"));
        }
		conn.connect();
		
		if(conn.getResponseCode()!=200) {
			logger.debug("server status->"+conn.getResponseCode());
			vh.put("code","-1");
			vh.put("queryString","«Î«Û ß∞‹" );
			return vh;
		}
		
		InputStream inputStream = conn.getInputStream();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BufferedInputStream bis = null;
		byte[] buf = new byte[1024];
		bis = new BufferedInputStream(conn.getInputStream());
		for (int len = 0; (len = bis.read(buf)) != -1;){
			baos.write(buf,0,len);
		}
	    inputStream.close(); 
	    vh.put("message", baos.toByteArray());

			
		logger.debug(Tools.toString( conn.getHeaderFields()));
        SipStatus status =SipStatus.getStatus( conn.getHeaderField("sip_status"));
		
        conn.disconnect();
		
		
		
		int code=0;
		if(status!=null){
			if(status.equals( SipStatus.SUCCESS)){
				code=0;
			}else{
				code=- nds.util.Tools.getInt(status.getCode(),1);
				if(code==0){
					// SipStatus.ERROR =0
					code=-1;
				}
			}
		}
		vh.put("code",String.valueOf(code));
		vh.put("queryString",queryString );
        //SipResult result = new SipResult(SipStatus.getStatus(code),buffer.toString());

        return vh;
	}
	
	public static ValueHolder sendRequest_buff(String apiURL, String params, String method) throws Exception {
        String queryString = (null == params) ? "" : params;
		
        logger.debug("url->"+apiURL);
        logger.debug("queryString :"+queryString);
        HttpURLConnection conn = (HttpURLConnection) new URL(apiURL).openConnection();
		conn.setRequestMethod(method);
		if(nds.util.Validator.isNotNull(queryString)){
        	conn.setDoInput(true);
        	conn.setDoOutput(true);
        	conn.setUseCaches(false);
        	conn.setInstanceFollowRedirects(false); 
        	conn.getOutputStream().write(queryString.getBytes("utf-8"));
        }
		//conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");  
		conn.connect();

		String charset = getChareset(conn.getContentType());
		BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), charset));
		StringBuffer buffer = new StringBuffer();
		String line;
		while ((line = reader.readLine()) != null) {
			buffer.append(line);
		}
		logger.debug(Tools.toString( conn.getHeaderFields()));
        SipStatus status =SipStatus.getStatus( conn.getHeaderField("sip_status"));
		reader.close();
        conn.disconnect();
		String msg= buffer.toString();
		ValueHolder vh=new ValueHolder();

		int code=0;
		if(status!=null){
			if(status.equals( SipStatus.SUCCESS)){
				code=0;
			}else{
				code=- nds.util.Tools.getInt(status.getCode(),1);
				if(code==0){
					// SipStatus.ERROR =0
					code=-1;
				}
			}
		}
		
		vh.put("code",String.valueOf(code));
		vh.put("message", msg);
		vh.put("queryString",queryString );
        //SipResult result = new SipResult(SipStatus.getStatus(code),buffer.toString());

        return vh;
	}

	public static String getChareset(String contentType) {
		int i = contentType == null ? -1 : contentType.indexOf("charset=");
		return i == -1 ? "UTF-8" : contentType.substring(i + 8);
	}

	// …˙≥…querystring
	public  static String delimit(Collection<Map.Entry<String, String>> entries,boolean doEncode) throws Exception {
		if (entries == null || entries.isEmpty()) {
			return null;
		}
		StringBuffer buffer = new StringBuffer();
		boolean notFirst = false;
		for (Map.Entry<String, ?> entry : entries) {
			if (notFirst) {
				buffer.append("&");
			} else {
				notFirst = true;
			}
			Object value = entry.getValue();
			if(value==null)value="";
			buffer.append(doEncode?URLEncoder.encode(entry.getKey(), "UTF8"):entry.getKey()).append("=").append(
					doEncode ? URLEncoder.encode(value.toString(), "UTF8") : value);
		}
		return buffer.toString();
	}
}
