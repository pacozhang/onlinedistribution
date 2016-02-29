package nds.weixinpublicparty.ext;

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.publicweixin.ext.tools.RestUtils;
import nds.query.QueryEngine;
import nds.util.NDSException;

public class BigWheelRecordCommand extends Command {
	
	private static Logger logger= LoggerManager.getInstance().getLogger(BigWheelRecordCommand.class.getName());
	private String serverUrl;
	private String SKEY;
	private String Sign;
	private String ts;
	@Override
	public ValueHolder execute(DefaultWebEvent event) throws NDSException,
			RemoteException {
		ValueHolder vh=new ValueHolder();
		JSONObject messagejo = (JSONObject) event.getParameterValue("jsonObject");
		String wx_bigwheelrecord_id="";
		String username ="";
		String phonenum = "";
		String ticketno = "";
		boolean isErp=false;
		
		try {
			messagejo=new JSONObject(messagejo.optString("params"));
			wx_bigwheelrecord_id = messagejo.getString("wxBigwheelrecordId"); 
			username = messagejo.getString("NAME");
			phonenum = messagejo.getString("PHONENUM");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String resql = "select bwr.WX_BIGWHEELAWARD_ID,bwr.RECORD_STATE,bwr.WX_VIP_ID,bwr.AD_CLIENT_ID from WX_BIGWHEELRECORD bwr where bwr.id = ?";
		JSONObject rejson = QueryEngine.getInstance().doQueryObject(resql.toString(),new Object[] {wx_bigwheelrecord_id});
		String wx_bigwheelaward_id = rejson.optString("WX_BIGWHEELAWARD_ID");
		int record_state = rejson.optInt("RECORD_STATE");
		int wx_vip_id = rejson.optInt("WX_VIP_ID");
		String ad_client_id = rejson.optString("AD_CLIENT_ID");
		
		String vipsql = "select v.VIPCARDNO from wx_vip v where v.id=?";
		JSONObject vipjson= QueryEngine.getInstance().doQueryObject(vipsql.toString(),new Object[]{wx_vip_id});
		String vipcardno = vipjson.optString("VIPCARDNO");
		
		String awsql = "select bwa.REWARDTYPE,bwa.NAME,bwa.WX_COUPON_ID,bwa.INTEGRAL from WX_BIGWHEELAWARD bwa where bwa.id=?";
		JSONObject awjson = QueryEngine.getInstance().doQueryObject(awsql.toString(),new Object[]{wx_bigwheelaward_id});
		String reward_type = awjson.optString("REWARDTYPE");
		String name = awjson.optString("NAME");
		String wx_coupon_id = awjson.optString("WX_COUPON_ID");
		int integral = awjson.optInt("INTEGRAL");
		
		if(rejson==null) {
			logger.debug("WX_BIGWHEELRECORD not found");
			vh.put("code", "-1");
			vh.put("message", "�н���¼������");
			return vh;
		}
		
		List all=QueryEngine.getInstance().doQueryList("select ifs.erpurl,ifs.username,ifs.iserp,wc.wxparam from WX_INTERFACESET ifs join web_client wc on ifs.ad_client_id=wc.ad_client_id WHERE ifs.ad_client_id="+ad_client_id);
		if(all!=null&&all.size()>0) {
			logger.debug("WX_INTERFACESET size->"+all.size());
			serverUrl=(String)((List)all.get(0)).get(0);
			isErp="Y".equalsIgnoreCase((String)((List)all.get(0)).get(2));
			SKEY=(String)((List)all.get(0)).get(3);
			if(isErp&&(nds.util.Validator.isNull(serverUrl)||nds.util.Validator.isNull(SKEY))) {
				logger.debug("SERVERuRL OR SKEY IS NULL");
			}
		}else {
			System.out.println("not find WX_INTERFACESET");
		}
		
		
		if(reward_type.equals("INT"))//��ƷΪ����
		{
			if(isErp) {//��ͨ����
				HashMap<String, String> params =new HashMap<String, String>();
				String ts=String.valueOf(System.currentTimeMillis());
				logger.debug("ts->"+ts);
				params.put("args[cardid]",ad_client_id);
				params.put("args[cardno]",vipcardno);
				params.put("args[docno]",ts);
				params.put("args[description]","�ι��ֽ�Ʒ�ͻ���");
				params.put("args[integral]",String.valueOf(integral));				
				params.put("format","JSON");
				
				params.put("client","");
				params.put("ver","1.0");
				params.put("ts",ts);
				try {
					params.put("sig",nds.util.MD5Sum.toCheckSumStr(String.valueOf(ad_client_id) + ts+ SKEY));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				params.put("method","adjustIntegral");
				try{
					vh=RestUtils.sendRequest(serverUrl,params,"POST");
					logger.debug("vh->"+vh.get("message"));
				} catch (Throwable tx) {
					logger.debug("ERP����ͨ���ϰ�!");
					try {
						throw new Exception("ERP����ͨ���ϰ�!->"+tx.getMessage());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				String result=(String) vh.get("message");
				JSONObject jo=null;
				try {
					jo = new JSONObject(result);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if(jo.optInt("errCode",-1)==0) {//���³ɹ�
					 record_state=2;//�����»��ָ��³ɹ�����Ʒ״̬�ĳ�����ȡ
					 vh.put("code", "0");
					 vh.put("message", "���²����ɹ�");
				}else{
					vh.put("code", "-1");
					vh.put("message", "���²���ʧ��");
				}	
			}
			//���ܽӲ���ͨ����
				 JSONObject consumejo=new JSONObject();
				 JSONObject jo2=new JSONObject();
				   try {
					consumejo.put("vipid", wx_vip_id);
					consumejo.put("getCredits", integral);
					consumejo.put("description", "�ι��ֽ�Ʒ�ͻ���");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				   String resultStr=null;
				   ArrayList params2=new ArrayList();
				   params2.add(ad_client_id);
				   params2.add(consumejo.toString());
				   logger.debug("user.updateTrade call oracle params->"+consumejo.toString());
				   logger.debug("params2->"+params2);
				   ArrayList para=new ArrayList();
				   para.add( java.sql.Clob.class);
				   Collection list=QueryEngine.getInstance().executeFunction("wx_coupon_$r_adjust", params2, para);
				   resultStr=(String)list.iterator().next();
				   logger.debug("resultStr------>"+resultStr);
				   try {
					jo2=new JSONObject(resultStr);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if(jo2.optInt("errCode",-1)==0) {
					record_state=3;//�����ϻ��ָ��³ɹ�����Ʒ״̬�ĳ��ѷ���
					 vh.put("code", "0");
					 vh.put("message", "���ϲ����ɹ�");
				}else{
					vh.put("code", "-1");
					vh.put("message", "���ϲ���ʧ��");
					return vh;
				}
			
			
		}else if(reward_type.equals("COU"))//��ƷΪ�Ż�ȯ
		{
			List couponinfo = QueryEngine.getInstance().doQueryList("select c.USETYPE1,c.NUM from wx_coupon c where c.id = "+wx_coupon_id+"");
			int usertype = Integer.parseInt(String.valueOf(((List)couponinfo.get(0)).get(0)));
			String 	wx_cp_num = String.valueOf(((List)couponinfo.get(0)).get(1));
			if(isErp) {//���½�ͨ�ɹ�
				List cinfo=QueryEngine.getInstance().doQueryList("select vp.vipcardno,vp.wechatno,nvl(cp.value,'0'),"+
						"to_char(decode(nvl(cp.validay,0),0,nvl(cp.starttime,sysdate), sysdate), 'YYYYMMDD'),"+
						"to_char(decode(nvl(cp.validay,0),0, nvl(cp.endtime, add_months(cp.starttime, 1)),sysdate+cp.validay), 'YYYYMMDD')"+
						"from wx_coupon cp,wx_vip vp "+
						"where cp.num='"+wx_cp_num+"' and vp.id='"+wx_vip_id+"' and cp.ad_client_id=vp.ad_client_id");
					logger.debug("cinfo->"+cinfo);
					String openid = String.valueOf(((List)cinfo.get(0)).get(1));
					String couponvalue = String.valueOf(((List)cinfo.get(0)).get(2));
					String starttime = String.valueOf(((List)cinfo.get(0)).get(3));
					String endtime = String.valueOf(((List)cinfo.get(0)).get(4));
				if(usertype == 2||usertype == 3)
				{
					HashMap<String, String> params =new HashMap<String, String>();
					ts=String.valueOf(System.currentTimeMillis());
					logger.debug("ts->"+ts);
					params.put("args[cardid]",ad_client_id);
					params.put("args[openid]",openid);
					params.put("args[vipno]",vipcardno);
					params.put("args[couponno]",wx_cp_num);
					params.put("args[couponvalue]",couponvalue);				
					params.put("args[begintime]",starttime);
					params.put("args[endtime]",endtime);
					
					params.put("format","JSON");
					params.put("client","");
					params.put("ver","1.0");
					params.put("ts",ts);
					try {
						params.put("sig",nds.util.MD5Sum.toCheckSumStr(String.valueOf(ad_client_id) + ts+ SKEY));
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					params.put("method","sendcoupon");
					logger.debug("params->"+params);
					logger.debug("aaaaaaaaaaaaaaaaaaaaaaaaa");
					try{
						logger.debug("welcome!!!");
						vh=RestUtils.sendRequest(serverUrl,params,"POST");
						logger.debug("vh->"+vh);
					   } catch (Throwable tx) {
						logger.debug("ERP����ͨ���ϰ�!");
						try {
							throw new Exception("ERP����ͨ���ϰ�!->"+tx.getMessage());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					String result=(String) vh.get("message");
					logger.debug("coupon offline code result->"+result);
					JSONObject jo=null;
					try {
						jo = new JSONObject(result);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(jo.optInt("errCode",-1)==0) {//���·����ɹ�
						 record_state=2;//�����·����ɹ�����Ʒ״̬�ĳ�����ȡ
						ticketno = jo.optJSONObject("result").optJSONObject("data").optString("code");
						vh.put("code", "0");
						vh.put("message", "���²����ɹ�");
					}else{
						vh.put("code", "-1");
						vh.put("message", "���²���ʧ��");
					}
				}
			}
			//���½�ͨ���ɹ�
			 JSONObject consumejo=new JSONObject();
			 JSONObject jo2=new JSONObject();
			   try {
				consumejo.put("vipid", wx_vip_id);
				consumejo.put("couponcode",wx_cp_num);
				consumejo.put("tickno",ticketno);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			   String resultStr=null;
			   ArrayList params2=new ArrayList();
			   params2.add(ad_client_id);
			   params2.add(consumejo.toString());
			   logger.debug("user.updateTrade call oracle params->"+consumejo.toString());
			   logger.debug("params2->"+params2);
			   ArrayList para=new ArrayList();
			   para.add( java.sql.Clob.class);
			   Collection list=QueryEngine.getInstance().executeFunction("wx_coupon_$r_send", params2, para);
			   resultStr=(String)list.iterator().next();
			   try {
				   logger.debug("resultStr->"+resultStr);
				   jo2=new JSONObject(resultStr);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if(jo2.optInt("code",-1)==0) {
				record_state=3;//�����ϻ��ָ��³ɹ�����Ʒ״̬�ĳ��ѷ���
				vh.put("code", "0");
				vh.put("message", "���ϲ����ɹ�");
				logger.debug("11111111111111111111");
			}else{
				vh.put("code", "-1");
				vh.put("message", "���²���ʧ��");
				logger.debug("222222222222222222222");
				return vh;
			}
			
		}
		//�����н���¼
		String sql = "update wx_bigwheelrecord b set b.name ='"+username+"' ,b.phonenum='"+phonenum+"' ,b.record_state="+record_state+" where b.id="+wx_bigwheelrecord_id+"";
		logger.debug("!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		try {
			QueryEngine.getInstance().executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return vh;
	}

}
