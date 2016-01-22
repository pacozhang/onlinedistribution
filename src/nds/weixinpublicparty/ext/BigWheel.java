package nds.weixinpublicparty.ext;

import java.util.Hashtable;

import nds.query.QueryEngine;
import nds.query.QueryException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BigWheel {
	private static Hashtable<String, BigWheel> factorys;
	private String adClientId;

	private BigWheel() {
	};

	public static synchronized BigWheel getInstance(String adClientId) {
		if (adClientId == null)
			return null;

		BigWheel instance = null;
		if (factorys == null) {
			factorys = new Hashtable<String, BigWheel>();
			instance = new BigWheel();
			factorys.put(adClientId, instance);
		} else if (factorys.containsKey(adClientId)) {
			instance = factorys.get(adClientId);
		} else {
			instance = new BigWheel();
			factorys.put(adClientId, instance);
		}
		instance.adClientId = adClientId;
		return instance;
	}

	/**
	 * 
	 * @param adClientId
	 *            公司ID
	 * @param wxBigWheelId
	 *            活动ID
	 * @param wxVipId
	 *            会员ID
	 * @return code 是否中奖（0为中奖，其他为失败） prizetype几等奖 wx_bigwheelrecord_id中奖纪录id
	 *         name奖项名称 rewardtype奖项类型
	 * @throws NumberFormatException
	 * @throws JSONException
	 * @throws QueryException
	 */
	public synchronized String bigwheel(String adClientId, String wxBigWheelId,
			String wxVipId) throws NumberFormatException, JSONException,
			QueryException {
		// 查询每人最多允许抽奖次数和中奖率
		String queryMAXTIMES = "select t.MAXTIMES,t.REWARDRATE from WX_BIGWHEEL t where t.ad_client_id = ? and t.id = ? ";
		JSONObject bigwheelJson = QueryEngine.getInstance().doQueryObject(
				queryMAXTIMES.toString(),
				new Object[] { adClientId, wxBigWheelId });

		int maxtimes = bigwheelJson.optInt("MAXTIMES");
		double rewardrate = bigwheelJson.optDouble("REWARDRATE");

		// 查询vip已经抽奖次数
		String queryUse = "select count(*) from WX_BIGWHEELRECORD t where t.ad_client_id = ? and t.wx_bigwheel_id = ? and t.wx_vip_id = ? ";
		int useCount = Integer.parseInt(QueryEngine
				.getInstance()
				.doQueryOne(queryUse.toString(),
						new Object[] { adClientId, wxBigWheelId, wxVipId })
				.toString());

		// 查询vip是否中奖
		String recordCountStr = "select count(*) from WX_BIGWHEELRECORD t where t.ad_client_id = ? and t.wx_bigwheel_id = ? and t.wx_vip_id = ? and t.wx_bigwheelaward_id is not null";
		int recordCount = Integer.parseInt(QueryEngine
				.getInstance()
				.doQueryOne(recordCountStr.toString(),
						new Object[] { adClientId, wxBigWheelId, wxVipId })
				.toString());
		if(recordCount >= 1){
			return "{\"code\":\"-1\",\"message\":\"您本次活动已经抽中奖品，则不可再转！\"}";
		}

		int remainCount = maxtimes - useCount;
		if (remainCount <= 0) {
			return "{\"code\":\"-1\",\"message\":\"抽奖次数已经用完\"}";
		}

		/*
		 * 查询奖项设置信息 rewarddegree:奖项等级 wx_bigwheelaward_id id： 奖品ID
		 * rewardtype：奖品类型 name：奖品名称 wx_coupon_id：优惠券ID integral：积分数
		 */
		/*StringBuffer queryAward = new StringBuffer();

		queryAward
				.append("select t.rewarddegree,t.wx_bigwheelaward_id,b.id,b.rewardtype,b.name,b.wx_coupon_id,b.integral from wx_bigwheelitem t, wx_bigwheelaward b");
		queryAward
				.append(" where  t.ad_client_id = ? and t.wx_bigwheel_id = ? and ");
		queryAward
				.append(" (select count(1) from wx_bigwheelrecord a where a.ad_client_id = ? and a.wx_bigwheel_id = ? and a.wx_bigwheelaward_id = t.wx_bigwheelaward_id) < t.rewardcount and t.wx_bigwheelaward_id = b.id");
		JSONArray array = QueryEngine.getInstance().doQueryObjectArray(
				queryAward.toString(),
				new Object[] { adClientId, wxBigWheelId, adClientId,
						wxBigWheelId });
		*/
		
		int recordId = QueryEngine.getInstance().getSequence("WX_BIGWHEELRECORD");

		StringBuffer insertSQL = new StringBuffer();

		insertSQL
				.append("insert into wx_bigwheelrecord(id,ad_client_id,ad_org_id,wx_bigwheel_id,wx_bigwheelaward_id,wx_vip_id,record_state,receivetime,ownerid,modifierid,creationdate,modifieddate,isactive) ");
		insertSQL
				.append("select ?,t.id,t.ad_org_id,?,?,?,1,sysdate,t.ownerid,t.modifierid,sysdate,sysdate,'Y' from ad_client t where t.id = ?");

		int temp = (int) Math.round(Math.random() * 10000);// 随见产生一个数
		int max = (int) Math.round(rewardrate / 100.0 * 10000);// 通过概率算区间
		
		String joa = "SELECT i.WX_BIGWHEELAWARD_ID,i.rewardcount,s.name,i.REWARDDEGREE,s.rewardtype,s.wx_coupon_id,s.integral,nvl(sc. \"wincount\", 0) \"wi count\",i.rewardcount - nvl(sc. \"wincount\", 0) \"surpluscount\" FROM WX_BIGWHEELITEM i LEFT JOIN (SELECT s.WX_BIGWHEEL_ID, s.WX_BIGWHEELAWARD_ID, COUNT(1) \"wincount\" FROM WX_BIGWHEELRECORD s WHERE s.WX_BIGWHEELAWARD_ID IS NOT NULL AND s.WX_BIGWHEEL_ID = ? GROUP BY s.WX_BIGWHEEL_ID,s.WX_BIGWHEELAWARD_ID) sc ON i.WX_BIGWHEELAWARD_ID = sc.WX_BIGWHEELAWARD_ID LEFT JOIN WX_BIGWHEELAWARD s ON s.id = i.WX_BIGWHEELAWARD_ID WHERE i.WX_BIGWHEEL_ID = ? and nvl(i.rewardcount,0) > nvl(sc. \"wincount\", 0) order by i.rewarddegree asc";
		JSONArray joas = QueryEngine.getInstance().doQueryObjectArray(joa, new Object[]{wxBigWheelId,wxBigWheelId});
		
		if (joas == null || joas.length() <= 0 || temp > max) {
			// 插入中奖纪录
			QueryEngine.getInstance().executeUpdate(
					insertSQL.toString(),
					new Object[] { recordId, wxBigWheelId, "", wxVipId,
							adClientId });
			return "{\"code\":\"-1\",\"message\":\"未抽中奖品\"}";
		}
		
		// 如果temp<=max则为中奖
		String sql = "select sum(nvl(s.REWARDCOUNT, 0)), sum(nvl(sn.\"count\",  0)) from WX_BIGWHEELITEM s left join (select count(1) \"count\",wbe.WX_BIGWHEELAWARD_ID from WX_BIGWHEELRECORD wbe where wbe.wx_bigwheel_id =? and wbe.wx_bigwheelaward_id is not null group by wbe.wx_bigwheelaward_id) sn on s.WX_BIGWHEELAWARD_ID = sn.WX_BIGWHEELAWARD_ID where s.wx_bigwheel_id = ?";
		JSONArray ja = QueryEngine.getInstance().doQueryJSONArray(sql, new Object[]{wxBigWheelId,wxBigWheelId});
		
		long sumprize=ja.optJSONArray(0).optLong(0);
		long winprize=ja.optJSONArray(0).optLong(1);
		long surplusprize=sumprize-winprize;

		long start=0;
		int index = 0;
		long boundary=0;
		long sboundary=0;
		int startindex=0;
		long randowvalue=-1;
		int length=joas.length();
		System.out.println("nds.weixin.ext.BigWheel length->"+length+",sumprize->"+sumprize+",winprize->"+winprize+",surplusprize->"+surplusprize);
		
		if(length>1) {
			/*
			if((sumprize / 2) < surplusprize) {
				startindex=1;
				start=joas.optJSONObject(0).optLong("SURPLUSCOUNT");
			}
			*/
			System.out.println("nds.weixin.ext.BigWheel startindex->"+startindex+",start->"+start);
			randowvalue=(long)(Math.random() * (surplusprize-start));
			System.out.println("nds.weixin.ext.BigWheel randowvalue->"+randowvalue);
			
			for(int i=startindex;i<length;i++) {
				boundary=joas.optJSONObject(i).optLong("SURPLUSCOUNT");
				System.out.println("nds.weixin.ext.Scratch sboundary->"+sboundary+",boundary->"+boundary);
				if(randowvalue>=sboundary&&randowvalue<(sboundary+boundary)) {
					index=i;
					break;
				}
				sboundary+=boundary;
			}
		}
		System.out.println("nds.weixin.ext.BigWheel index->"+index);
		
		// 如果中奖了,从奖品中任意取一件
		// int index = (int) Math.random() * array.length();
		QueryEngine.getInstance().executeUpdate(
				insertSQL.toString(),
				new Object[] {
						recordId,
						wxBigWheelId,
						joas.getJSONObject(index).getString(
								"WX_BIGWHEELAWARD_ID"), wxVipId, adClientId });

		return "{\"code\":\"0\",\"prizetype\":"
				+ joas.getJSONObject(index).getString("REWARDDEGREE")
				+ ",\"wx_bigwheelrecord_id\":" + recordId + ",\"name\":\""
				+ joas.getJSONObject(index).getString("NAME")
				+ "\",\"rewardtype\":\""
				+ joas.getJSONObject(index).getString("REWARDTYPE")
				+ "\",\"message\":\"恭喜你中奖了\"}";
	}
}
