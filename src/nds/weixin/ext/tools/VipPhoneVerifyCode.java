package nds.weixin.ext.tools;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import nds.control.util.ValueHolder;
import nds.query.QueryEngine;
import nds.weixin.ext.SipStatus;

public class VipPhoneVerifyCode {
	public static ValueHolder verifyphonecode(int vipid,String phone,String verifycode) {
		ValueHolder vh=new ValueHolder();
		List verifyc=null;
		
		try {
			verifyc=QueryEngine.getInstance().doQueryList("select vvc.verifycode,vvc.phone,to_number(to_char(vvc.senddate,'yyyyMMddhh24miss')),vvc.isverify from wx_vipverifycode vvc where vvc.wx_vip_id=?",new Object[] {vipid});
		} catch (Exception e) {
			System.out.println("VipPhoneVerifyCode find vipverifycode error:"+e.getLocalizedMessage());
			e.printStackTrace();
			vh.put("code", "-1");
			vh.put("message", "��֤����Ϣ����������");
			return vh;
		}
		
		if (verifyc==null||verifyc.size()<=0) {
			System.out.println("VipPhoneVerifyCode find vipverifycode error: not find data");
			vh.put("code", "-1");
			vh.put("message", "��֤����Ϣ����������");
			return vh;
		}
		verifyc=(List)verifyc.get(0);
		
		//�ж���֤���Ƿ�ʹ�ù�
		boolean isverify="N".equalsIgnoreCase(String.valueOf(verifyc.get(3)));
		if(!isverify) {
			System.out.println("VipPhoneVerifyCode vipverifycode isverify->"+isverify);
			vh.put("code", "-1");
			vh.put("message", "��֤���ѹ���");
			return vh;
		}
		
		
		//�жϷ�����֤���ֻ��뵱ǰ�ֻ��Ƿ�һ��
		String dphone=String.valueOf(verifyc.get(1));
		if(nds.util.Validator.isNull(dphone)||!dphone.equals(phone)) {
			System.out.println("VipPhoneVerifyCode put vipverifycode error:dphoe->"+dphone+",phone->"+phone);
			vh.put("code", "-1");
			vh.put("message", "������֤���ֻ��뵱ǰ�ֻ���һ��");
			return vh;
		}
		
		//�ж���֤���Ƿ�һ��
		String dverifycode=String.valueOf(verifyc.get(0));
		if(nds.util.Validator.isNull(verifycode)||!dverifycode.equals(verifycode)) {
			System.out.println("VipPhoneVerifyCode put vipverifycode error:dverifycode->"+dverifycode+",verifycode->"+verifycode);
			vh.put("code", "-1");
			vh.put("message", "��֤���������������");
			return vh;
		}
		
		//�ж���֤���Ƿ���ڣ���Ч��10���ӣ�
		long dsenddate=Long.valueOf(String.valueOf(verifyc.get(2)));
		Date nowTime=new Date(); 
		SimpleDateFormat time=new SimpleDateFormat("yyyyMMddHHmmss");
		String strnowtime=time.format(nowTime);
		long lnowtime=Long.valueOf(strnowtime);
		if(dsenddate+10*60<lnowtime) {
			System.out.println("VipPhoneVerifyCode vipverifycode time is outtime:sendtime->"+dsenddate+",nowtime->"+strnowtime);
			vh.put("code", "-1");
			vh.put("message", "��֤�볬��10���ӣ������·���");
			return vh;
		}
		
		vh.put("code", "0");
		vh.put("message", "��֤����ȷ");
		return vh;
	}

}
