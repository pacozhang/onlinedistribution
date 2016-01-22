package nds.weixinpublicparty.ext;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.QueryEngine;
import nds.rest.RestUtils;
import nds.util.NDSException;
import nds.util.Tools;
import nds.weixin.ext.WeUtils;
import nds.weixin.ext.WeUtilsManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SignIntegralCommand extends Command {
	private static Logger logger= LoggerManager.getInstance().getLogger(SignIntegralCommand.class.getName());
	private String serverUrl;
	private String SKEY;
	private String Sign;
	private String ts;
	@Override
	public ValueHolder execute(DefaultWebEvent event) throws NDSException,
			RemoteException {
		ValueHolder vh=new ValueHolder();
		
		/*SimpleDateFormat df = new SimpleDateFormat();
		List signList = null;
		List signNoteList = null;
		int count =1;
		int todayintegral =0;
		Date date = new Date();
		List all=null;
		boolean isErp=false;
		String vipcardno="";
		boolean isEnabl=false;
		String nextgigndate = "";*/
		
		
		SimpleDateFormat df = new SimpleDateFormat();
		String nextgigndate = "";
		List all=null;
		boolean isErp=false;
		String vipcardno="";
		List signList = null;
		List signOne=null;
		int signtype=-1;
		int baseintegral=0;
		int signintegral=0;
		int signcount=0;
		int cumulativeintegral=0;
		int maxintegral=0;
		int cumulativetype=-1;
		StringBuffer lastSignCondition=null;
		StringBuffer thisSignCondition=null;
		List lastsign=null;
		List thissign=null;
		List vipList=null;
		QueryEngine qe=null;
		JSONObject messagejo = (JSONObject) event.getParameterValue("jsonObject");
		
		try {
			messagejo=new JSONObject(messagejo.optString("params"));
		} catch (JSONException e) {
			logger.debug("params error->"+e.getLocalizedMessage());
			e.printStackTrace();
			vh.put("code", "-1");
			vh.put("message", "ǩ���쳣��������");
			return vh;
		}
		int ad_client_id = messagejo.optInt("AD_CLIENT_ID",-1);
		int wx_vip_id = messagejo.optInt("WX_VIP_ID",-1);
		
		if(ad_client_id==-1||wx_vip_id==-1) {
			logger.debug("param error->ad_client_id:"+ad_client_id+",wx_vip_id:"+wx_vip_id);
			vh.put("code", "-1");
			vh.put("message", "ǩ���쳣��������");
			return vh;
		}
		String vipsql = "select wv.integral,wv.opencard_status,wv.vipcardno from wx_vip wv where wv.id="+wx_vip_id;
		
		try {
			qe=QueryEngine.getInstance();
			vipList = qe.doQueryList(vipsql);
		}catch(Exception e) {
			logger.debug("search data error->"+e.getLocalizedMessage());
			e.printStackTrace();
			vh.put("code", "-1");
			vh.put("message", "ǩ���쳣��������");
			return vh;
		}
		
		if(vipList==null||vipList.size()<=0) {
			logger.debug("not find vip");
			vh.put("code", "-1");
			vh.put("message", "ǩ���쳣��������");
			return vh;
		}
		vipList=(List)vipList.get(0);
		if(Tools.getInt(vipList.get(1), 1)==1){
			logger.debug("vip not opencared");
			vh.put("code", "-1");
			vh.put("message", "�������ǻ�ԱŶ��������ȡ��Ա��~");
			return vh;
		}
		
		//��ѯǩ���
		String signsql = "select w.id,w.signtype,w.integralway,nvl(w.integralbase,0),nvl(w.stackintegral,0),nvl(w.integralmax,0), w.isenabl from wx_sign w where w.ad_client_id="+ad_client_id+" and w.isenabl='Y'";
		signList = qe.doQueryList(signsql);
		
		if(signList==null ||signList.size()<=0){
			logger.debug("not find sign");
			vh.put("code", "-1");
			vh.put("message", "����û��ǩ���");
			return vh;
		}
		signOne=(List)signList.get(0);
		signtype=Tools.getInt(signOne.get(1), -1);
		if(signtype==-1 ||(signtype!=1 && signtype!=2)) {
			logger.debug("sign type is error->"+signtype);
			vh.put("code", "-1");
			vh.put("message", "ǩ���쳣��������");
			return vh;
		}
		
		int wx_sign_id =Tools.getInt(signOne.get(0), -1);
		lastSignCondition=new StringBuffer();
		thisSignCondition=new StringBuffer();
		thisSignCondition.append("select sn.consign from wx_sign_note sn where sn.wx_sign_id=").append(wx_sign_id).append(" and sn.wx_vip_id=").append(wx_vip_id).append(" and ");
		lastSignCondition.append("select sn.consign from wx_sign_note sn where sn.wx_sign_id=").append(wx_sign_id).append(" and sn.wx_vip_id=").append(wx_vip_id).append(" and ");
		if(signtype==1) {	//ÿ��ǩ��
			nextgigndate = "to_number(to_char(sysdate+1,'yyyyMMdd'))";
			lastSignCondition.append("to_number(to_char(sn.creationdate,'yyyyMMdd'))=to_number(to_char(sysdate-1,'yyyyMMdd'))");
			thisSignCondition.append("to_number(to_char(sn.creationdate,'yyyyMMdd'))=to_number(to_char(sysdate,'yyyyMMdd'))");
		}else{	//ÿ��ǩ��
			nextgigndate = "to_char(trunc(sysdate,'IW')+7,'yyyyMMDD')";
			lastSignCondition.append("to_number(to_char(sn.creationdate,'yyyyMMdd')) >= to_number(to_char(trunc(sysdate,'IW')-7,'yyyyMMDD')) and to_number(to_char(sn.creationdate,'yyyyMMdd')) <= to_number(to_char(trunc(sysdate,'IW')-1,'yyyyMMDD'))");
			thisSignCondition.append("to_number(to_char(sn.creationdate,'yyyyMMdd')) >= to_number(to_char(trunc(sysdate,'IW'),'yyyyMMDD')) and to_number(to_char(sn.creationdate,'yyyyMMdd')) <= to_number(to_char(trunc(sysdate,'IW')+6,'yyyyMMDD'))");
		}
		
		try {
			thissign=qe.doQueryList(thisSignCondition.toString());
		}catch(Exception e) {
			logger.debug("search thissign error->"+e.getLocalizedMessage());
			e.printStackTrace();
			vh.put("code", "-1");
			vh.put("message", "ǩ���쳣��������");
			return vh;
		}

		if(thissign!=null&&thissign.size()>0) {
			logger.debug("already sign ");
			vh.put("code", "-1");
			if(signtype==1) {vh.put("message", "��������ǩ������������Ŷ~");}
			else {vh.put("message", "��������ǩ������������Ŷ~");}
			
			return vh;
		}
		
		try {
			lastsign=qe.doQueryList(lastSignCondition.toString());
		}catch(Exception e) {
			logger.debug("search lastsign error->"+e.getLocalizedMessage());
			e.printStackTrace();
			vh.put("code", "-1");
			vh.put("message", "ǩ���쳣��������");
			return vh;
		}
		cumulativetype=Tools.getInt(signOne.get(2), -1);
		baseintegral=Tools.getInt(signOne.get(3), -1);
		if(lastsign!=null&&lastsign.size()>0) {signcount=Tools.getInt(((List)lastsign).get(0),0);}
		logger.debug("signcount->"+signcount);
		if(cumulativetype!=2) {
			cumulativeintegral=Tools.getInt(signOne.get(4), -1);
			maxintegral=Tools.getInt(signOne.get(5), -1);
			signintegral=baseintegral+signcount*cumulativeintegral;
			if(maxintegral>0&&signintegral>maxintegral) {signintegral=maxintegral;}
		}else {
			signintegral=baseintegral;
		}
		signcount+=1;
		
		all=QueryEngine.getInstance().doQueryList("select ifs.erpurl,ifs.username,ifs.iserp,ifs.wxparam from WX_INTERFACESET ifs WHERE ifs.ad_client_id="+ad_client_id);
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
		if(isErp) {//����
			vipcardno=String.valueOf(vipList.get(2));
			HashMap<String, String> params =new HashMap<String, String>();
			ts=String.valueOf(System.currentTimeMillis());
			logger.debug("ts->"+ts);
			params.put("args[cardno]",vipcardno);
			params.put("args[docno]",ts);
			params.put("args[description]","ǩ���ͻ���");
			params.put("args[integral]",String.valueOf(signintegral));				
			
			params.put("format","JSON");
			params.put("client","");
			params.put("ver","1.0");
			params.put("ts",ts);
			try {
				params.put("sig",nds.util.MD5Sum.toCheckSumStr(String.valueOf(ad_client_id) + ts+ SKEY));
				logger.error("sig->"+params.get("sig"));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			params.put("method","adjustIntegral");
			
			try{
				//ValueHolder vh=new ValueHolder();
				vh=RestUtils.sendRequest(serverUrl,params,"POST");
				logger.debug("vh->"+vh);
			} catch (Throwable tx) {
				logger.debug("ERP����ͨ���ϰ�!");
				tx.printStackTrace();
				vh.put("code", "-1");
				vh.put("message", "ǩ���쳣��������");
				return vh;
			}
			
			//int vvid=QueryEngine.getInstance().getSequence("WX_COUPONEMPLOY", conn);
			String result=(String) vh.get("message");
			logger.debug("sign adjust integral offline result->"+result);
			JSONObject jo=null;
			try {
				jo = new JSONObject(result);
			} catch (JSONException e) {
				logger.debug("sign adjust integral offline error->"+e.getLocalizedMessage());
				e.printStackTrace();
				vh.put("code", "-1");
				vh.put("message", "ǩ���쳣��������");
				return vh;
			}

			//{"result":{"data":{"code":"26f5lb99fr0-0","couponId":"6F5Lb99Fr"},"card":{"balance":0,"level":215,"no":"WX140515000000002","credit":0},"openid":"owAZBuEBBLn-LQ_5ebcbkSh_wFDk","cardid":"37"},"errMessage":"΢�����Ա�����ɹ���","errCode":0}
			
			//int insertcount=1;
			if(jo.optInt("errCode",-1)!=0) {
				vh.put("code", "-1");
				vh.put("message", "ǩ��ʧ��");
				return vh;
			}
		}
		
		
		//����
	    JSONObject consumejo=new JSONObject();
	    JSONObject jo=new JSONObject();
	   
	    try {
		   consumejo.put("vipid", wx_vip_id);
		   consumejo.put("getCredits", signintegral);
		   consumejo.put("description", "ǩ���ͻ���");
		} catch (JSONException e1) {
			
		}
	    
	    WeUtils wu=WeUtilsManager.getByAdClientId(ad_client_id);
	    String urlString = "";
	    try {
	    	urlString = URLEncoder.encode("http://"+wu.getDoMain()+"/html/nds/oto/webapp/cardscore/index.vml", "GBK");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	    
		   String resultStr=null;
		   ArrayList params=new ArrayList();
		   params.add(ad_client_id);
		   params.add(consumejo.toString());
		   
		   logger.debug("user.updateTrade call oracle params->"+consumejo.toString());
		   ArrayList para=new ArrayList();
		   para.add( java.sql.Clob.class);
		   try {
			   Collection list=QueryEngine.getInstance().executeFunction("wx_coupon_$r_adjust", params, para);
			   resultStr=(String)list.iterator().next();
			   logger.debug("sign adjust vip integral call oracle result->"+resultStr);
			   jo=new JSONObject(resultStr);
			   if(jo.optInt("errCode",-1)==0) {
					vh.put("code", "0");
					int count = Tools.getInt(vipList.get(0), 0)+signintegral;
					if(messagejo.has("FromUserName") == true && nds.util.Validator.isNotNull(urlString)){
						if(signtype==1)//ÿ��ǩ������
						{
							vh.put("message", "ǩ���ɹ�\r\n��ǰ"+count+"������\r\n��������ǩ��"+signcount+"��\r\n����ǩ�����"+signintegral+"������\r\n"+"���<a href=\"https://open.weixin.qq.com/connect/oauth2/authorize?appid="+wu.getAppId()+"&redirect_uri="+urlString+"&response_type=code&scope=snsapi_base&state=123#wechat_redirect\">�鿴���ּ�¼</a>");
						}else{
							vh.put("message", "ǩ���ɹ�\r\n��ǰ"+count+"������\r\n��������ǩ��"+signcount+"��\r\n����ǩ�����"+signintegral+"������\r\n"+"���<a href=\"https://open.weixin.qq.com/connect/oauth2/authorize?appid="+wu.getAppId()+"&redirect_uri="+urlString+"&response_type=code&scope=snsapi_base&state=123#wechat_redirect\">�鿴���ּ�¼</a>");
						}
					}else{
						if(signtype==1)//ÿ��ǩ������
						{
							vh.put("message", "ǩ���ɹ�\r\n��ǰ"+count+"������\r\n��������ǩ��"+signcount+"��\r\n����ǩ�����"+signintegral+"������");
						}else{
							vh.put("message", "ǩ���ɹ�\r\n��ǰ"+count+"������\r\n��������ǩ��"+signcount+"��\r\n����ǩ�����"+signintegral+"������");
						}
					}
					vh.put("todayintegral", signintegral);
				}else{
					vh.put("code", "-1");
					vh.put("message", "ǩ��ʧ��");
					return vh;
				}
		   }catch(Exception e) {
			   vh.put("errCode", "-1");
			   vh.put("errMessage", "sign adjust vip integral call oracle error->"+e.getMessage());
			   logger.debug("sign adjust vip integral call oracle error->"+e.getMessage());
			  return vh;
		   }
			
		String insert = "insert into wx_sign_note(id,ad_client_id,ad_org_id,wx_sign_id,wx_vip_id,ownerid,modifierid,creationdate,modifieddate,isactive,consign,todayintegral,nextgigndate) " +
										" select get_sequences('wx_sign_note'),"+ad_client_id+", wc.ad_org_id ,"+wx_sign_id+","+wx_vip_id+",wc.ownerid,"
										+"wc.modifierid,sysdate,sysdate,'Y',"+signcount+","+signintegral+","+nextgigndate+"  from web_client wc where ad_client_id="+ad_client_id+"";
		try {
			qe.executeUpdate(insert);
		} catch (SQLException e) {
			logger.debug("add sign record error->"+e.getLocalizedMessage());
			e.printStackTrace();
		}	
		
		
		/*
		try {
			QueryEngine qe=QueryEngine.getInstance();
			messagejo=new JSONObject(messagejo.optString("params"));
			int ad_client_id = messagejo.getInt("AD_CLIENT_ID");
			int wx_vip_id = messagejo.getInt("WX_VIP_ID");
			String vipsql = "select wv.integral,wv.opencard_status,wv.vipcardno from wx_vip wv where wv.id="+wx_vip_id+"";
			List vipList = qe.doQueryList(vipsql);
			int integral = 0;//Integer.parseInt(String.valueOf(((List)vipList.get(0)).get(0)));
			
			if(vipList==null||vipList.size()<=0) {
				logger.debug("vip not found");
				vh.put("code", "-1");
				vh.put("message", "�����쳣��������");
				return vh;
			}
			vipcardno=String.valueOf(((List)vipList.get(0)).get(2));
			if(Integer.parseInt(String.valueOf(((List)vipList.get(0)).get(1)))==1)
			{
				logger.debug("�������ǻ�ԱŶ��������ȡ��Ա��~");
				vh.put("code", "-1");
				vh.put("message", "�������ǻ�ԱŶ��������ȡ��Ա��~");
				return vh;
			}
			
			
			int id=qe.getSequence("wx_sign_note");
			//��ѯ�
			//String signsql = "select s.id,s.signtype,s.signtime,s.signintegral from wx_sign w where w.ad-client_id="+ad_client_id+"and s.starttime"+"<="+df.format(date)+"<="+"s.endtime";
			//String signsql = "select s.id,s.signtype,s.integraltype,s.integralbase,s.spintegral,s.highintegral from wx_sign w where w.ad-client_id="+ad_client_id+"and nvl(s.starttime,sysdate)<=sysdate and sysdate<=nvl(s.endtime,sysdate)";
			String signsql = "select w.id,w.signtype,w.integralway,nvl(w.integralbase,0),nvl(w.stackintegral,0),nvl(w.integralmax,0), w.isenabl from wx_sign w where w.ad_client_id="+ad_client_id+" and w.isenabl='Y'";
			signList = qe.doQueryList(signsql);
			
			if(signList==null ||signList.size()<=0){
				logger.debug("����û��ǩ���");
				vh.put("code", "-1");
				vh.put("message", "����û��ǩ���");
				return vh;
			}
//			isEnabl = "Y".equalsIgnoreCase.((String)(((List)signList.get(0)).get(6));
//			isEnabl = "Y".equalsIgnoreCase((String)((List)signList.get(0)).get(6));			
//			if(isEnabl)
//			{
//				logger.debug("�û�����ã�");
//				vh.put("code", "-1");
//				vh.put("message", "�û�����ã�");
//				return vh;			
//			}
			int wx_sign_id = Integer.parseInt(String.valueOf(((List)signList.get(0)).get(0)));
			
			signOne=(List)signList.get(0);
			signtype=Tools.getInt(signOne.get(1), -1);
			if(signtype==-1 ||(signtype!=1 && signtype!=2)) {
				logger.debug("sign type is error->"+signtype);
				vh.put("code", "-1");
				vh.put("message", "ǩ���쳣��������");
				return vh;
			}
			
			if(signtype==1) {	//ÿ��ǩ��
				lastSignCondition="to_number(to_char(sn.creationdate,'yyyyMMdd'))=to_number(to_char(sysdate-1,'yyyyMMdd'))";
				thisSignCondition="to_number(to_char(sn.creationdate,'yyyyMMdd'))=to_number(to_char(sysdate,'yyyyMMdd'))";
			}else{	//ÿ��ǩ��
				lastSignCondition="to_number(to_char(sn.creationdate,'yyyyMMdd')) >= to_number(to_char(trunc(sysdate,'IW')-7,'yyyyMMDD')) and to_number(to_char(sn.creationdate,'yyyyMMdd')) <= to_number(to_char(trunc(sysdate,'IW')-1,'yyyyMMDD'))";
				thisSignCondition="to_number(to_char(sn.creationdate,'yyyyMMdd')) >= to_number(to_char(trunc(sysdate,'IW'),'yyyyMMDD')) and to_number(to_char(sn.creationdate,'yyyyMMdd')) <= to_number(to_char(trunc(sysdate,'IW')+6,'yyyyMMDD'))";
			}
			
			
			
			
			//
			if(Integer.parseInt(String.valueOf(((List)signList.get(0)).get(1)))==1)//ÿ��ǩ������
					{
						nextgigndate = "to_number(to_char(sysdate+1,'yyyyMMdd'))";
				
						String signnotesql = "select sn.consign,sn.modifierid,sn.ad_org_id,sn.ownerid from wx_sign_note sn where sn.wx_sign_id="+wx_sign_id+" and sn.wx_vip_id="+wx_vip_id+" and to_number(to_char(sn.creationdate,'yyyyMMdd'))=to_number(to_char(sysdate-1,'yyyyMMdd'))";
						signNoteList = qe.doQueryList(signnotesql);//��ѯ����ǩ�����
						
						String signnotesql2 = "select sn.consign,sn.modifierid,sn.ad_org_id,sn.ownerid from wx_sign_note sn where sn.wx_sign_id="+wx_sign_id+" and sn.wx_vip_id="+wx_vip_id+" and to_number(to_char(sn.creationdate,'yyyyMMdd'))=to_number(to_char(sysdate,'yyyyMMdd'))";
						List signNoteList2 = qe.doQueryList(signnotesql2);//��ѯ����ǩ�����
						
						if(signNoteList2!=null&&signNoteList2.size()>0){
								logger.debug("��������ǩ������������Ŷ~");
								vh.put("code", "-1");
								vh.put("message", "��������ǩ������������Ŷ~");
								return vh;
							}
						
						//����û��ǩ���Ļ�
						if(signNoteList!=null&&signNoteList.size()>0){//����ǩ������������ǩ��
							count = Integer.parseInt(String.valueOf(((List)signNoteList.get(0)).get(0)));
							count++;
							
							if(Integer.parseInt(String.valueOf(((List)signList.get(0)).get(2)))==2)//�̶��ͻ���
							{
								todayintegral = Integer.parseInt(String.valueOf(((List)signList.get(0)).get(3)));
								integral+=todayintegral;
								
							}else//�����ͻ���
							{
								if(Integer.parseInt(String.valueOf(((List)signList.get(0)).get(5)))==0)//�ж���߻���
								{
									todayintegral = Integer.parseInt(String.valueOf(((List)signList.get(0)).get(3)))+Integer.parseInt(String.valueOf(((List)signList.get(0)).get(4)))*(count-1);
									integral+=todayintegral;
								}else
								{
									todayintegral = Integer.parseInt(String.valueOf(((List)signList.get(0)).get(3)))+Integer.parseInt(String.valueOf(((List)signList.get(0)).get(4)))*(count-1);
									if(todayintegral >= Integer.parseInt(String.valueOf(((List)signList.get(0)).get(5))))
									{
										todayintegral = Integer.parseInt(String.valueOf(((List)signList.get(0)).get(5)));
										integral+=todayintegral;
									}
								}
							}
							
							
						}else//�м��ж�
						{
							//String signbefsql2 = "select sn.consign from wx_sign_note sn where sn.wx_sign_id="+wx_sign_id+" and sn.wx_vip_id="+wx_vip_id+" ";
							count = 1;
							if(Integer.parseInt(String.valueOf(((List)signList.get(0)).get(2)))==2)//�̶��ͻ���
							{
								todayintegral=Integer.parseInt(String.valueOf(((List)signList.get(0)).get(3)));
								integral+=todayintegral;
							}else//�����ͻ���
							{
								todayintegral = Integer.parseInt(String.valueOf(((List)signList.get(0)).get(3)));
								integral+=todayintegral;
							}
						}
						
							
					}else if(Integer.parseInt(String.valueOf(((List)signList.get(0)).get(1)))==2)//ÿ��ǩ������
						{
						nextgigndate = "to_char(trunc(sysdate,'IW')+7,'yyyyMMDD')";
						
						String signnotesql3 = "select sn.consign,sn.modifierid,sn.ad_org_id,sn.ownerid from wx_sign_note sn where sn.wx_sign_id="+wx_sign_id+" and sn.wx_vip_id="+wx_vip_id+" and to_number(to_char(sn.creationdate,'yyyyMMdd')) > to_number(to_char(trunc(sysdate,'IW')-1,'yyyyMMDD')) and to_number(to_char(sn.creationdate,'yyyyMMdd')) < to_number(to_char(trunc(sysdate,'IW')+7,'yyyyMMDD'))";
						List signNoteList3 = qe.doQueryList(signnotesql3);//��ѯ����ǩ�����
						
						String signnotesql4 = "select sn.consign,sn.modifierid,sn.ad_org_id,sn.ownerid from wx_sign_note sn where sn.wx_sign_id="+wx_sign_id+" and sn.wx_vip_id="+wx_vip_id+" and to_number(to_char(sn.creationdate,'yyyyMMdd')) > to_number(to_char(trunc(sysdate,'IW')-8,'yyyyMMDD')) and to_number(to_char(sn.creationdate,'yyyyMMdd')) < to_number(to_char(trunc(sysdate,'IW'),'yyyyMMDD'))";
						List signNoteList4 = qe.doQueryList(signnotesql4);//��ѯ����ǩ�����
						
						
						if(signNoteList3!=null&&signNoteList3.size()>0){//������ǩ���Ļ�
							logger.debug("��������ǩ������������Ŷ~");
							vh.put("code", "-1");
							vh.put("message", "��������ǩ������������Ŷ~");
							return vh;
						}
							
						if(signNoteList4!=null&&signNoteList4.size()>0){//����ǩ������������ǩ��
							count = Integer.parseInt(String.valueOf(((List)signNoteList4.get(0)).get(0)));
							count++;
							if(Integer.parseInt(String.valueOf(((List)signList.get(0)).get(2)))==2)//�̶��ͻ���
							{
							todayintegral = Integer.parseInt(String.valueOf(((List)signList.get(0)).get(3)));
							integral+=todayintegral;
							}else//�����ͻ���
							{
								if(Integer.parseInt(String.valueOf(((List)signList.get(0)).get(5)))==0)//�ж���߻���
								{
									todayintegral = Integer.parseInt(String.valueOf(((List)signList.get(0)).get(3)))+Integer.parseInt(String.valueOf(((List)signList.get(0)).get(4)))*(count-1);
									integral+=todayintegral;
								}else
								{
									todayintegral = Integer.parseInt(String.valueOf(((List)signList.get(0)).get(3)))+Integer.parseInt(String.valueOf(((List)signList.get(0)).get(4)))*(count-1);
									if(todayintegral >= Integer.parseInt(String.valueOf(((List)signList.get(0)).get(5))))
								    { 
										todayintegral = Integer.parseInt(String.valueOf(((List)signList.get(0)).get(5)));
									    integral+=todayintegral;
								    }
											
								}
							}
						}else//�м��ж�
						{
							//String signbefsql2 = "select sn.consign from wx_sign_note sn where sn.wx_sign_id="+wx_sign_id+" and sn.wx_vip_id="+wx_vip_id+" ";
							count = 1;
							if(Integer.parseInt(String.valueOf(((List)signList.get(0)).get(2)))==2)//�̶��ͻ���
							{
								todayintegral=Integer.parseInt(String.valueOf(((List)signList.get(0)).get(3)));
								integral+=todayintegral;
							}else//�����ͻ���
							{
								todayintegral=Integer.parseInt(String.valueOf(((List)signList.get(0)).get(3)));
								integral+=todayintegral;
							}
						}

				
					}
			
			
			
			all=QueryEngine.getInstance().doQueryList("select ifs.erpurl,ifs.username,ifs.iserp,ifs.wxparam from WX_INTERFACESET ifs WHERE ifs.ad_client_id="+ad_client_id);
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
			if(isErp) {//����
				HashMap<String, String> params =new HashMap<String, String>();
				ts=String.valueOf(System.currentTimeMillis());
				logger.debug("ts->"+ts);
				params.put("args[cardno]",vipcardno);
				params.put("args[docno]",ts);
				params.put("args[description]","ǩ���ͻ���");
				params.put("args[integral]",String.valueOf(integral));				
				
				params.put("format","JSON");
				params.put("client","");
				params.put("ver","1.0");
				params.put("ts",ts);
				try {
					params.put("sig",nds.util.MD5Sum.toCheckSumStr(String.valueOf(ad_client_id) + ts+ SKEY));
					logger.error("sig->"+params.get("sig"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				params.put("method","adjustIntegral");
				
				try{
//					ValueHolder vh=new ValueHolder();
					vh=RestUtils.sendRequest(serverUrl,params,"POST");
					logger.debug("vh->"+vh);
				} catch (Throwable tx) {
					logger.debug("ERP����ͨ���ϰ�!");
					try {
						throw new Exception("ERP����ͨ���ϰ�!->"+tx.getMessage());
					} catch (Exception e) {
						e.printStackTrace();
					}
					//return false;
				}
				
				//int vvid=QueryEngine.getInstance().getSequence("WX_COUPONEMPLOY", conn);
				String result=(String) vh.get("message");
				logger.debug("adjust integral offline code result->"+result);
				JSONObject jo= new JSONObject(result);

				//{"result":{"data":{"code":"26f5lb99fr0-0","couponId":"6F5Lb99Fr"},"card":{"balance":0,"level":215,"no":"WX140515000000002","credit":0},"openid":"owAZBuEBBLn-LQ_5ebcbkSh_wFDk","cardid":"37"},"errMessage":"΢�����Ա�����ɹ���","errCode":0}
				
//				int insertcount=1;
				if(jo.optInt("errCode",-1)==0) {
					vh.put("code", "0");
					if(Integer.parseInt(String.valueOf(((List)signList.get(0)).get(1)))==1)//ÿ��ǩ������
					{
						vh.put("message", "ǩ���ɹ���\r\n ����ǩ�����"+todayintegral+"�����֣�\r\n ��������ǩ��"+count+"�졣");
					}else{
						vh.put("message", "ǩ���ɹ���\r\n ����ǩ�����"+todayintegral+"�����֣�\r\n ��������ǩ��"+count+"�ܡ�");
					}
					vh.put("todayintegral", todayintegral);
				}else{
					vh.put("code", "-1");
					vh.put("message", "ǩ��ʧ��");
				}
			}
			//����
			   JSONObject consumejo=new JSONObject();
			   JSONObject jo=new JSONObject();
			   consumejo.put("vipid", wx_vip_id);
			   consumejo.put("getCredits", integral);
			   consumejo.put("description", "ǩ���ͻ���");
			   
			   String resultStr=null;
			   ArrayList params=new ArrayList();
			   params.add(ad_client_id);
			   params.add(consumejo.toString());
			   
			   logger.debug("user.updateTrade call oracle params->"+consumejo.toString());
			   ArrayList para=new ArrayList();
			   para.add( java.sql.Clob.class);
			   try {
				   Collection list=QueryEngine.getInstance().executeFunction("wx_coupon_$r_adjust", params, para);
				   resultStr=(String)list.iterator().next();
				   logger.debug("user.updateTrade call oracle result->"+resultStr);
				   jo=new JSONObject(resultStr);
				   if(jo.optInt("errCode",-1)==0) {
						vh.put("code", "0");
						if(Integer.parseInt(String.valueOf(((List)signList.get(0)).get(1)))==1)//ÿ��ǩ������
						{
							vh.put("message", "ǩ���ɹ���\r\n ����ǩ�����"+todayintegral+"�����֣�\r\n ��������ǩ��"+count+"�졣");
						}else{
							vh.put("message", "ǩ���ɹ���\r\n ����ǩ�����"+todayintegral+"�����֣�\r\n ��������ǩ��"+count+"�ܡ�");
						}
						vh.put("todayintegral", todayintegral);
					}else{
						vh.put("code", "-1");
						vh.put("message", "ǩ��ʧ��");
						return vh;
					}
			   }catch(Exception e) {
				   jo.put("errCode", "-1");
				   jo.putOpt("errMessage", "user.updateTrade call oracle error->"+e.getMessage());
				   logger.debug("user.updateTrade call oracle error->"+e.getMessage());
			   }
				
			String insert = "insert into wx_sign_note(id,ad_client_id,ad_org_id,wx_sign_id,wx_vip_id,ownerid,modifierid,creationdate,modifieddate,isactive,consign,todayintegral,nextgigndate) " +
											" select "+id+","+ad_client_id+", wc.ad_org_id ,"+wx_sign_id+","+wx_vip_id+",wc.ownerid,"
											+"wc.modifierid,sysdate,sysdate,'Y',"+count+","+todayintegral+","+nextgigndate+"  from web_client wc where ad_client_id="+ad_client_id+"";
			try {
				qe.executeUpdate(insert);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		return vh;
	}
}