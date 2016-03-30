package nds.weixin.ext.tools;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.publicplatform.api.GetUserAuthorization;
import nds.publicplatform.api.WeUser;
import nds.query.QueryEngine;
import nds.query.QueryException;
import nds.util.Tools;
import nds.weixin.ext.WeUtils;

public class WeCreateOrSearchVip {
	private static Logger logger= LoggerManager.getInstance().getLogger(WeCreateOrSearchVip.class.getName());
	private final static String searchVipid="select wvi.wx_vip_id from wx_vip_inqury wvi where wvi.WECHATNO=? and wvi.AD_CLIENT_ID=? and ROWNUM=1";
	private final static String seachVip="select wvi.wx_vip_id,wvi.issubscribe,wvi.id from wx_vip_inqury wvi where wvi.WECHATNO=? and wvi.AD_CLIENT_ID=? and ROWNUM=1";
	private final static String createVip="insert into wx_vip_inqury (ID,AD_CLIENT_ID,PHOTO,WECHATNO,DATEIN,NAME,UNIONID,COUNTRY,PROVINCE,CITY,GENDER,CONTACTADDRESS,ISSUBSCRIBE,DESCRIPTION,ownerid,creationdate,modifierid,modifieddate,isactive)"
			  					+" select ?,?,?,?,?,?,?,?,?,?,?,?,?,?,c.ownerid,sysdate,c.ownerid,sysdate,'Y' from web_client c where c.ad_client_id=?";
	private final static String createViptemp ="insert into wx_vip_inqury (id,ad_client_id,wechatno,issubscribe,description,ownerid,creationdate,modifierid,modifieddate,isactive)"
								+" select ?,?,?,'N',?,c.ownerid,sysdate,c.ownerid,sysdate,'Y' from web_client c where c.ad_client_id=?";
	private final static String updatetvip="update wx_vip_inqury vi set vi.PHOTO=?,vi.NAME=?,vi.UNIONID=?,vi.COUNTRY=?,vi.PROVINCE=?,vi.CITY=?,vi.GENDER=?,vi.CONTACTADDRESS=?,vi.ISSUBSCRIBE=?,vi.updatedate=to_char(sysdate,'yyyyMMdd')"+
			  " where vi.id=?";
	
	public JSONObject vipDispose(WeUtils wu, JSONObject jo) {
		int vipid=0;
		int viptempid=0;
		String event=jo.optString("Event");
		boolean issubscribe=false;
		JSONObject returnjo=new JSONObject();
		ArrayList paramsvip=new ArrayList();
		StringBuffer address=new StringBuffer();
		String openid=jo.optString("FromUserName");
		logger.debug("params is:"+jo.toString());
		logger.debug("event:"+event);
		Connection con=null;
		
		List vipinfo=null;
		int count=0;
		JSONObject userjo=null;
		try{
			con=QueryEngine.getInstance().getConnection();
			con.setAutoCommit(false);
			
			
			vipinfo=QueryEngine.getInstance().doQueryList(seachVip, new Object[] {openid,wu.getAd_client_id()}, con);
			if(vipinfo!=null&&vipinfo.size()>0) {
				vipinfo=(List)vipinfo.get(0);
				vipid=Tools.getInt(vipinfo.get(0),-1);
				viptempid=Tools.getInt(vipinfo.get(2),-1);
				issubscribe="Y".equalsIgnoreCase(String.valueOf(vipinfo.get(1)));
			}
			logger.debug("seach vipid=>"+vipid);
			if(vipid>0) {
				if("scan".equalsIgnoreCase(event)||"subscribe".equalsIgnoreCase(event)) {
					if(!issubscribe) {
						WeUser gua=WeUser.getInstance(wu.getAppId());
						userjo=gua.getUser(openid);
						
						if(userjo==null||!userjo.has("openid")) {
							logger.debug("userjo is null");
						}
						if(userjo!=null&&userjo.has("openid")) {
							logger.debug("create tempvip=>");
							address.append(userjo.optString("country"));
							address.append(userjo.optString("province"));
							address.append(userjo.optString("city"));
						
							try {
								QueryEngine.getInstance().executeUpdate(updatetvip, new Object[] {userjo.optString("headimgurl",""),userjo.optString("nickname",""),userjo.optString("unionid",""),userjo.optString("country",""),userjo.optString("province",""),userjo.optString("city",""),userjo.optString("sex"),(address.length()<=0?"":address.toString()),(0==userjo.optInt("subscribe",0)?"N":"Y"),viptempid},con);
								QueryEngine.getInstance().executeUpdate("update wx_vip v set v.photo=?,v.name=?,v.ifcancleattention='N' where v.id=?",new Object[] {userjo.optString("headimgurl",""),userjo.optString("nickname",""),vipid},con);
							}catch(Exception e) {
								logger.debug("update tempvip error->"+e.getLocalizedMessage());
							}
						}
					}else {
						String sql="update wx_vip_inqury vi set vi.dateout=sysdate,vi.issubscribe='Y' where vi.wechatno=? and vi.ad_client_id=?";
						String sqlo="update wx_vip v set v.IFCANCLEATTENTION='N',v.modifieddate=sysdate where v.wechatno=? and v.ad_client_id=?";
						
						try {
							QueryEngine.getInstance().executeUpdate(sql, new Object[] {openid,wu.getAd_client_id()},con);
							QueryEngine.getInstance().executeUpdate(sqlo, new Object[] {openid,wu.getAd_client_id()},con);
						} catch (QueryException e) {
							logger.debug("update vip_inqury error->"+e.getMessage());
							//e.printStackTrace(); 
						}
					}
				}
				
				con.commit();
				returnjo.put("code", 0);
				returnjo.put("vipid", vipid);
				returnjo.put("type", "search");
				return returnjo;
			}else {
				String usertype="";
				if("scan".equalsIgnoreCase(event)||"subscribe".equalsIgnoreCase(event)) {
					WeUser gua=WeUser.getInstance(wu.getAppId());
					userjo=gua.getUser(openid);
					usertype="用户关注时新增用户："+userjo.optInt("subscribe",0);
				}else if(event.contains("snsapi_userinfo")) {	
					JSONObject authinfo=jo.optJSONObject("authorizainfo");
					if(authinfo!=null&&authinfo.has("access_token")) {
						GetUserAuthorization gua=GetUserAuthorization.getInstance(wu.getAppId());
						userjo=gua.getAuthorizerUserinfo(authinfo);
						if(userjo!=null&&userjo.has("data")) {
							userjo=userjo.optJSONObject("data");
							usertype="用户授权时新增用户："+userjo.optInt("subscribe",0);
						}
					}
				}
				if(userjo!=null&&userjo.has("openid")) {
					logger.debug("create tempvip=>");
					address.append(userjo.optString("country"));
					address.append(userjo.optString("province"));
					address.append(userjo.optString("city"));
					
					viptempid=QueryEngine.getInstance().getSequence("wx_vip_inqury", con);
					
					logger.debug("viptempid-?"+viptempid);
					paramsvip.add(viptempid);
					QueryEngine.getInstance().executeUpdate(createVip, new Object[] {viptempid,wu.getAd_client_id(),userjo.optString("headimgurl",""),openid,userjo.optString("subscribe_time"),userjo.optString("nickname",""),userjo.optString("unionid"),userjo.optString("country"),userjo.optString("province"),userjo.optString("city"),userjo.optString("sex"),(address.length()<=0?"":address.toString()),(0==userjo.optInt("subscribe",0)?"N":"Y"),usertype,wu.getAd_client_id()}, con);
					QueryEngine.getInstance().executeStoredProcedure("WX_VIP_INQURY_AC", paramsvip, false, con);
					vipid=QueryEngine.getInstance().doQueryInt(searchVipid, new Object[] {openid,wu.getAd_client_id()}, con);
					logger.debug("execute create vip=>");
					
					con.commit();
					returnjo.put("code", 0);
					returnjo.put("vipid", vipid);
					returnjo.put("type", "create");
					return returnjo;
				}
				
				viptempid=QueryEngine.getInstance().getSequence("wx_vip_inqury", con);
				
				logger.debug("viptempid-?"+viptempid);
				paramsvip.add(viptempid);
				QueryEngine.getInstance().executeUpdate(createViptemp, new Object[] {viptempid,wu.getAd_client_id(),openid,"用户授权时新增用户：N",wu.getAd_client_id()}, con);
				QueryEngine.getInstance().executeStoredProcedure("WX_VIP_INQURY_AC", paramsvip, false, con);
				vipid=QueryEngine.getInstance().doQueryInt(searchVipid, new Object[] {openid,wu.getAd_client_id()}, con);
				logger.debug("execute create vip=>");
				
				returnjo.put("code", 0);
				returnjo.put("vipid", vipid);
				returnjo.put("type", "create");
				
				con.commit();
				return returnjo;
				
				/*
				if("scan".equalsIgnoreCase(event)||"subscribe".equalsIgnoreCase(event)) {
					WeUser gua=WeUser.getInstance(wu.getAppId());
					userjo=gua.getUser(openid);
					
					if(userjo!=null&&userjo.has("openid")) {
						logger.debug("create tempvip=>");
						address.append(userjo.optString("country"));
						address.append(userjo.optString("province"));
						address.append(userjo.optString("city"));
						
						viptempid=QueryEngine.getInstance().getSequence("wx_vip_inqury", con);
						
						logger.debug("viptempid-?"+viptempid);
						paramsvip.add(viptempid);
						QueryEngine.getInstance().executeUpdate(createVip, new Object[] {viptempid,wu.getAd_client_id(),userjo.optString("headimgurl",""),openid,userjo.optString("subscribe_time"),userjo.optString("nickname",""),userjo.optString("unionid"),userjo.optString("country"),userjo.optString("province"),userjo.optString("city"),userjo.optString("sex"),(address.length()<=0?"":address.toString()),(0==userjo.optInt("subscribe",0)?"N":"Y"),"用户关注时新增用户："+userjo.optInt("subscribe",0),wu.getAd_client_id()}, con);
						QueryEngine.getInstance().executeStoredProcedure("WX_VIP_INQURY_AC", paramsvip, false, con);
						vipid=QueryEngine.getInstance().doQueryInt(searchVipid, new Object[] {openid,wu.getAd_client_id()}, con);
						logger.debug("execute create vip=>");
						
						returnjo.put("code", 0);
						returnjo.put("vipid", vipid);
						returnjo.put("type", "create");
						return returnjo;
					}
				}else {
					if(event.contains("snsapi_userinfo")) {
						JSONObject authinfo=jo.optJSONObject("authorizainfo");
						if(authinfo!=null&&authinfo.has("access_token")) {
							GetUserAuthorization gua=GetUserAuthorization.getInstance(wu.getAppId());
							userjo=gua.getAuthorizerUserinfo(authinfo);
							if(userjo!=null&&userjo.has("data")) {
								userjo=userjo.optJSONObject("data");
								if(userjo!=null&&userjo.has("openid")) {
									logger.debug("create tempvip=>");
									address.append(userjo.optString("country"));
									address.append(userjo.optString("province"));
									address.append(userjo.optString("city"));
									
									viptempid=QueryEngine.getInstance().getSequence("wx_vip_inqury", con);
									
									logger.debug("viptempid-?"+viptempid);
									paramsvip.add(viptempid);
									QueryEngine.getInstance().executeUpdate(createVip, new Object[] {viptempid,wu.getAd_client_id(),userjo.optString("headimgurl",""),openid,userjo.optString("subscribe_time"),userjo.optString("nickname",""),userjo.optString("unionid"),userjo.optString("country"),userjo.optString("province"),userjo.optString("city"),userjo.optString("sex"),(address.length()<=0?"":address.toString()),(0==userjo.optInt("subscribe",0)?"N":"Y"),"用户授权时新增用户："+userjo.optInt("subscribe",0),wu.getAd_client_id()}, con);
									QueryEngine.getInstance().executeStoredProcedure("WX_VIP_INQURY_AC", paramsvip, false, con);
									vipid=QueryEngine.getInstance().doQueryInt(searchVipid, new Object[] {openid,wu.getAd_client_id()}, con);
									logger.debug("execute create vip=>");
									
									returnjo.put("code", 0);
									returnjo.put("vipid", vipid);
									returnjo.put("type", "create");
									return returnjo;
								}
							}
						}
					}
					
					viptempid=QueryEngine.getInstance().getSequence("wx_vip_inqury", con);
						
					logger.debug("viptempid-?"+viptempid);
					paramsvip.add(viptempid);
					QueryEngine.getInstance().executeUpdate(createViptemp, new Object[] {viptempid,wu.getAd_client_id(),openid,"用户授权时新增用户：N",wu.getAd_client_id()}, con);
					QueryEngine.getInstance().executeStoredProcedure("WX_VIP_INQURY_AC", paramsvip, false, con);
					vipid=QueryEngine.getInstance().doQueryInt(searchVipid, new Object[] {openid,wu.getAd_client_id()}, con);
					logger.debug("execute create vip=>");
					
					returnjo.put("code", 0);
					returnjo.put("vipid", vipid);
					returnjo.put("type", "create");
				}
				*/
			}
			
		}catch(Throwable t) {
			vipid=0;
			try {
				if(con!=null) {con.rollback();}
				returnjo.put("code", -1);
				returnjo.put("vipid", vipid);
				returnjo.put("type", "search");
			}
			catch(Throwable t1) {
				logger.debug("rollback create vip error->"+t1.getMessage());
				t1.printStackTrace();
			}
			
			logger.debug("create vip error->"+t.getMessage());
			t.printStackTrace();
		}finally{
			if(con!=null) {
				try {
					con.setAutoCommit(true);
					con.close();
				}
				catch(Throwable t2) {
					logger.debug("create vip error close con error->"+t2.getMessage());
				}
			}
		}
		
		return returnjo;
	}

}
