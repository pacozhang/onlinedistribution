package nds.weixinpublicparty.ext;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import nds.control.util.ValueHolder;
import nds.process.SvrProcess;
import nds.query.QueryEngine;
import nds.rest.RestUtils;

public class SendCouponsvr extends SvrProcess {
	private Hashtable<String, List> interfacesets = new Hashtable<String, List>();

	@Override
	protected void prepare() {

	}

	@Override
	protected String doIt() {
		String serverUrl = "";
		String SKEY = "";
		String ad_client_id = "";
		List<List> all = null;
		String ifsql = "select ifs.ad_client_id, ifs.erpurl,ifs.iserp,wc.wxparam,ifs.username from WX_INTERFACESET ifs join web_client wc on ifs.ad_client_id=wc.ad_client_id where ifs.iserp='Y' and ifs.erpurl is not null and wc.wxparam is not null";

		Connection con = null;
		try {
			QueryEngine qe = QueryEngine.getInstance();
			con = qe.getConnection();
			try {
				all = qe.doQueryList(ifsql, con);
			} catch (Exception e) {
				log.error("find WX_INTERFACESET error:" + e.getLocalizedMessage());
				e.printStackTrace();
				return null;
			}
			if (all == null || all.size() <= 0) {
				log.error("not find WX_INTERFACESET");
				return null;
			}

			log.debug("offline WX_INTERFACESET size->" + all.size());
			for (int i = 0; i < all.size(); i++) {
				ad_client_id = String.valueOf((all.get(i)).get(0));
				interfacesets.put(ad_client_id, all.get(i));
			}

			String ts;
			String couponvalue;
			String minimumcharge;
			String starttime;
			String endtime;
			int usertype;
			String wx_cp_num;
			HashMap<String, String> params;
			
			String ticketno = "";
			String openid;
			String vipcardno;
			int wvipid;
			int scdtlid;

			String selectsendsql = "select wsc.id,nvl(cp.value,'0'),to_char(decode(nvl(cp.validay,0),0,nvl(cp.starttime,sysdate), sysdate), 'YYYYMMDD'),"
					+ "to_char(decode(nvl(cp.validay,0),0, nvl(cp.endtime, add_months(cp.starttime, 1)),sysdate+cp.validay), 'YYYYMMDD'),"
					+ "cp.USETYPE1,cp.NUM,wsc.ad_client_id,vp.wechatno,vp.vipcardno,vp.id,cp.minimumcharge"
					+ " from wx_sendcoupondetail wsc,wx_coupon cp,wx_vip vp where wsc.wx_coupon_id=cp.id and wsc.wx_vip_id=vp.id and nvl(wsc.sync_count,0)<=5 and nvl(wsc.sync_state,1)=1 "
					+ "and exists(select 1 from wx_sendcoupon sc where sc.id=wsc.wx_sendcoupon_id and sc.status=2) and rownum<=500 order  by wsc.wx_sendcoupon_id desc,wsc.creationdate desc, wsc.modifieddate asc";

			List<List> sendcouponinfo = qe.doQueryList(selectsendsql, con);
			log.debug("本次发券条数为： " + sendcouponinfo.size());
			for (int i = 0; i < sendcouponinfo.size(); i++) {
				ValueHolder vh;
				try {

					scdtlid = Integer.parseInt(String.valueOf(sendcouponinfo.get(i).get(0)));
					couponvalue = String.valueOf(sendcouponinfo.get(i).get(1));
					starttime = String.valueOf(sendcouponinfo.get(i).get(2));
					endtime = String.valueOf(sendcouponinfo.get(i).get(3));
					usertype = Integer.parseInt(String.valueOf(sendcouponinfo.get(i).get(4)));
					wx_cp_num = String.valueOf(sendcouponinfo.get(i).get(5));
					ad_client_id = String.valueOf(sendcouponinfo.get(i).get(6));
					
					openid = String.valueOf(sendcouponinfo.get(i).get(7));
					vipcardno = String.valueOf(sendcouponinfo.get(i).get(8));
					wvipid = Integer.parseInt(String.valueOf(sendcouponinfo.get(i).get(9)));
					minimumcharge = String.valueOf(sendcouponinfo.get(i).get(10));
					params = new HashMap<String, String>();
					ts = String.valueOf(System.currentTimeMillis());
					List ins = interfacesets.get(ad_client_id);
					ticketno = "";

					if (usertype == 2||usertype == 3) {
						if (ins == null) {
							log.debug("not find WX_INTERFACESET :ad_client_id-->" + ad_client_id);
							continue;
						} else {
							serverUrl = String.valueOf(ins.get(1));
							boolean isErp = "Y".equalsIgnoreCase(String.valueOf(ins.get(2)));
							SKEY = (String) ins.get(3);
							if ((nds.util.Validator.isNull(serverUrl) || nds.util.Validator.isNull(SKEY)) || !isErp) {
								log.debug("serverUrl OR SKEY IS NULL or isErp is 'N' of ad_client_id:" + ad_client_id);
								continue;
							}
						}

						params.put("args[cardid]", ad_client_id);
						params.put("args[openid]", openid);
						params.put("args[vipno]", vipcardno);
						params.put("args[couponno]", wx_cp_num);
						params.put("args[couponvalue]", couponvalue);
						params.put("args[minimumcharge]", minimumcharge);
						params.put("args[begintime]", starttime);
						params.put("args[endtime]", endtime);

						params.put("format", "JSON");
						params.put("client", "");
						params.put("ver", "1.0");
						params.put("ts", ts);
						try {
							params.put("sig", nds.util.MD5Sum.toCheckSumStr(String.valueOf(ad_client_id) + ts + SKEY));
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						params.put("method", "sendcoupon");
						log.debug("params->" + params);
						try {
							log.debug("welcome!!!");
							vh = RestUtils.sendRequest(serverUrl, params, "POST");
							log.debug("vh->" + vh);
						} catch (Throwable tx) {
							log.debug("ERP网络通信障碍!" + tx.getLocalizedMessage());
							tx.printStackTrace();
							continue;
						}
						String result = (String) vh.get("message");
						log.debug("coupon offline code result->" + result);
						JSONObject jo = null;
						try {
							jo = new JSONObject(result);
						} catch (JSONException e) {
							e.printStackTrace();
						}

						if (jo != null && jo.optInt("errCode", -1) == 0) {// 线下发劵成功
							String updateState = " update wx_sendcoupondetail scdtl set scdtl.sync_log='线下发劵成功',scdtl.sync_state=2  where scdtl.id="
									+ scdtlid;
							qe.executeUpdate(updateState);
							ticketno = jo.optJSONObject("result").optJSONObject("data").optString("code");
							log.debug("线下操作成功");
						} else {
							String updatelog = " update wx_sendcoupondetail scdtl set scdtl.sync_log='线下发劵失败',scdtl.sync_count=nvl(scdtl.sync_count,0)+1 where scdtl.id="
									+ scdtlid;
							qe.executeUpdate(updatelog);
							log.debug("线下操作失败");
							continue;
						}

					}
					log.debug("connection isClosed-->" + con.isClosed() + "  con-->" + con);
					if (con == null || con.isClosed()) {
						con = qe.getConnection();
					}
					JSONObject consumejo = new JSONObject();
					JSONObject jo2 = new JSONObject();
					try {
						consumejo.put("vipid", wvipid);
						consumejo.put("couponcode", wx_cp_num);
						consumejo.put("tickno", ticketno);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					String resultStr = null;
					ArrayList<String> params2 = new ArrayList<String>();

					params2.add(ad_client_id);
					params2.add(consumejo.toString());

					log.debug("user.SendCouponsvr call oracle params->" + consumejo.toString());
					log.debug("params2->" + params2);

					ArrayList para = new ArrayList();

					para.add(java.sql.Clob.class);
					Collection list = qe.executeFunction("wx_coupon_$r_send", params2, para, con);
					resultStr = (String) list.iterator().next();
					try {
						log.debug("resultStr->" + resultStr);
						jo2 = new JSONObject(resultStr);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					if (jo2.optInt("code", -1) == 0) {
						log.debug("线上操作成功11111111111111111111");
						String updateState = " update wx_sendcoupondetail scdtl set scdtl.sync_log='发送成功',scdtl.sync_state=2 where scdtl.id="
								+ scdtlid;
						qe.executeUpdate(updateState);
					} else {
						log.debug("线上操作失败222222222222222222222");
						String updatelog = " update wx_sendcoupondetail scdtl set scdtl.sync_log='线上操作失败',scdtl.sync_count=nvl(scdtl.sync_count,0)+1 where scdtl.id="
								+ scdtlid;
						qe.executeUpdate(updatelog);
					}

				} catch (Exception e) {
					log.debug("SendCouponsvr inner error: " + e.getMessage());
					e.printStackTrace();
					continue;
				}
			}

		} catch (Throwable tx) {
			log.debug("SendCouponsvr inner error: " + tx.getMessage());
			tx.printStackTrace();
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (Exception e) {
					log.debug("SendCouponsvr close Connection error!");
				}
			}
		}

		return null;
	}

	public boolean internalTransaction() {
		return true;
	}

}
