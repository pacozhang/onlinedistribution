package nds.weixinpublicparty.ext;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.velocity.runtime.directive.Foreach;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.query.ColumnLink;
import nds.query.Expression;
import nds.query.QueryEngine;
import nds.query.QueryRequestImpl;
import nds.query.QueryResult;
import nds.query.SQLCombination;
import nds.schema.Column;
import nds.schema.DisplaySetting;
import nds.schema.Table;
import nds.schema.TableManager;
import nds.util.NDSException;
import nds.util.PairTable;

public class GetJSONObject extends Command{

	@Override
	public ValueHolder execute(DefaultWebEvent event) throws NDSException,RemoteException {
		ValueHolder vh=new ValueHolder();
		
		JSONObject jo = (JSONObject)event.getParameterValue("jsonObject");
		try {
			jo=new JSONObject(jo.optString("params"));
		} catch (Exception e) {
			logger.debug("get params error->"+e.getLocalizedMessage());
			e.printStackTrace();
			vh.put("code", "-1");
			vh.put("message", "参数错误");
			return vh;
		}
		
		String tableName=jo.optString("tablename");
		if(nds.util.Validator.isNull(tableName)) {
			vh.put("code", "-1");
			vh.put("message", "必须传入表名");
			return vh;
		}
		
		QueryEngine engine= QueryEngine.getInstance();
		TableManager manager= TableManager.getInstance();
		Table table=manager.getTable(tableName);
		QueryRequestImpl query= engine.createRequest(null);
		query.setMainTable(table.getId());
		
		int[] columnMasks=null;
		if(jo.has("columnMasks")) {
			JSONArray cmja=jo.optJSONArray("columnMasks");
			if(cmja==null||cmja.length()<=0) {columnMasks=new int[] {Column.MASK_PRINT_SUBLIST};}
			else {
				int l=cmja.length();
				columnMasks=new int[l];
				for (int i = 0; i < l; i++) {
					columnMasks[i]=cmja.optInt(i);
				}
			}
		}else {
			columnMasks=new int[] {Column.MASK_PRINT_SUBLIST};
		}
		
		logger.debug("getObject columnMasks->"+columnMasks.toString());
		query.addSelection(table.getPrimaryKey().getId());
		query.addColumnsToSelection(columnMasks, true);
		int objId=jo.optInt("objectid",-1);
		int adClientId=jo.optInt("companyid",0);
		String fixedcol=jo.optString("fixedcol");
		
		Expression expr=null;
		if(objId>0){
			expr=new Expression(new ColumnLink(new int[]{table.getPrimaryKey().getId()}), "="+objId, null);
			if(table.isAcitveFilterEnabled()){
				expr=expr.combine(new Expression(new ColumnLink(new int[]{table.getColumn("ISACTIVE").getId()}), "=Y", null), SQLCombination.SQL_AND, null);
			}
			if(table.isAdClientIsolated()){
				expr=expr.combine(new Expression(new ColumnLink(new int[]{table.getColumn("AD_CLIENT_ID").getId()}), "="+adClientId, null), SQLCombination.SQL_AND, null);
			}
		}else if(fixedcol!=null){
			PairTable fixedColumns=null;
			//Expression fixedExpr=null;
			try{
				fixedColumns= PairTable.parse(fixedcol,null );
		 		expr=Expression.parsePairTable(fixedColumns);	
			}catch(NumberFormatException  e){
				logger.debug("params error->fixedcol format error");
				vh.put("code", "-1");
				vh.put("message", "参数错误");
				return vh;
			}
			if(table.isAcitveFilterEnabled()){
				expr=expr.combine(new Expression(new ColumnLink(new int[]{table.getColumn("ISACTIVE").getId()}), "=Y", null), SQLCombination.SQL_AND, null);
			}
			if(table.isAdClientIsolated()){
				expr=expr.combine(new Expression(new ColumnLink(new int[]{table.getColumn("AD_CLIENT_ID").getId()}), "="+adClientId, null), SQLCombination.SQL_AND, null);
			}
		}
		if(expr!=null)query.addParam(expr);
		
		QueryResult result= engine.doQuery(query);
		//QueryResultMetaData meta=result.getMetaData();
		JSONObject resultdata=new JSONObject();
		
		if(result.getRowCount()>0){
			result.next();
			Column column=null;
			try {
				resultdata.put("id", result.getObject(1));
			} catch (JSONException e) {
			}
			ArrayList columns= table.getColumns(columnMasks,true,0);
			for(int i=0;i<columns.size();i++){		
				column=(Column) columns.get(i);

				try {
					//columnData=result.getString(i+1, true, true);
					if(column.getDisplaySetting().getObjectType()==DisplaySetting.OBJ_CLOB){
						resultdata.put(column.getName().toLowerCase(), result.getObject(i+2));
					}else{
						resultdata.put(column.getName().toLowerCase(), result.getString(i+2,false,false));
					}
				} catch (Exception e) {
				}
				
			}
		}
		
		vh.put("code", "0");
		vh.put("message", "操作成功");
		vh.put("data", resultdata);
		
		return vh;
	}

}
