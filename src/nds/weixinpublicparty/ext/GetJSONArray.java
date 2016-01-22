package nds.weixinpublicparty.ext;

import java.rmi.RemoteException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.control.web.WebUtils;
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
import nds.util.StringUtils;
import nds.util.Tools;
import nds.web.config.ListDataConfig;
import nds.web.config.ListUIConfig;
import nds.web.config.PortletConfigManager;

public class GetJSONArray extends Command{

	@Override
	public ValueHolder execute(DefaultWebEvent event) throws NDSException,
			RemoteException {
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
		String adListDataConfName=jo.optString("adListDataConfName");
		String adListUIConfName=jo.optString("adListUIConfName","WX_SETAD");
		String fixedcol=jo.optString("fixedcol");
		String parm=jo.optString("parm");
		int pos=jo.optInt("pos",0);
		int adClientId=jo.optInt("companyid",0);
		
		
		if(nds.util.Validator.isNull(adListDataConfName)){
			logger.debug("params error->adListDataConfName and adListUIConfName must not null");
			vh.put("code", "-1");
			vh.put("message", "参数错误");
			return vh;
		}
		
		PortletConfigManager pcManager=(PortletConfigManager)WebUtils.getServletContextManager().getActor(nds.util.WebKeys.PORTLETCONFIG_MANAGER);
		ListDataConfig dataConfig= (ListDataConfig)pcManager.getPortletConfig(adListDataConfName,nds.web.config.PortletConfig.TYPE_LIST_DATA);
		ListUIConfig uiConfig= (ListUIConfig)pcManager.getPortletConfig(adListUIConfName,nds.web.config.PortletConfig.TYPE_LIST_UI);
		
		TableManager manager=TableManager.getInstance();
		QueryEngine engine=QueryEngine.getInstance();
		int tableId=dataConfig.getTableId();
		Table table;
		table= manager.getTable(tableId);
		
		if(table.isAdClientIsolated()&&adClientId<=0){
			logger.debug("params error->companyid must not null");
			vh.put("code", "-1");
			vh.put("message", "参数错误");
			return vh;
		}

		QueryRequestImpl query=QueryEngine.getInstance().createRequest(null);

	    query.setMainTable(tableId,true, dataConfig.getFilter());
	    		
	    query.addSelection(table.getPrimaryKey().getId());
	    query.addColumnsToSelection(dataConfig.getColumnMasks(),true, uiConfig.getColumnCount() );
	    
	    //根据表配置设置排序
	    JSONObject od=null;
	    boolean isorderby=false;
	    JSONObject tjo=table.getJSONProps();
	    if(tjo!=null) {
		    JSONArray orderby=tjo.optJSONArray("orderby");
			if(orderby!=null){
				if(orderby.length()>0) {isorderby=true;}
	    		for(int i=0;i<orderby.length();i++){
	    			try{
	    				od= orderby.getJSONObject(i);
	    				ColumnLink cl= new ColumnLink(table.getName()+"."+ od.getString("column"));
	    				query.addOrderBy( cl.getColumnIDs(), od.optBoolean("asc",true));
	    			}catch(Throwable t){
	    				logger.error("fail to parse column link:"+ table.getName()+"."+ od.optString("column"), t);
	    			}	
	    		}
			}
	    }
	    
		if( dataConfig.getOrderbyColumnId()!=-1){
			Column orderColumn= manager.getColumn(dataConfig.getOrderbyColumnId());
			if(orderColumn!=null && orderColumn.getTable().getId()== tableId)query.setOrderBy(new int[]{dataConfig.getOrderbyColumnId()}, dataConfig.isAscending());
		}else if(!isorderby){
	    	query.setOrderBy(new int[]{ table.getPrimaryKey().getId()}, false);
	    }
		
		//实现分页
		int currentpage=jo.optInt("currentpage",0);
		int pagesize=jo.optInt("pagesize",uiConfig.getPageSize());
		if (pagesize<=0){pagesize=100;}
		int startindex=currentpage*pagesize;
		if (startindex<0){startindex=0;}
		query.setRange(startindex, pagesize);
		logger.debug("currentpage:"+currentpage+",pagesize:"+pagesize+",startindex:"+startindex+",parm:"+parm+",pos:"+pos+",adClientId:"+adClientId);

	    
	    Expression expr=null;
		if(table.isAcitveFilterEnabled()){
			expr=new Expression(new ColumnLink(new int[]{table.getColumn("ISACTIVE").getId()}), "=Y", null);
		}
		if(table.isAdClientIsolated()){
			if(expr!=null)expr=expr.combine(new Expression(new ColumnLink(new int[]{table.getColumn("AD_CLIENT_ID").getId()}), "="+adClientId, null), SQLCombination.SQL_AND, null);
			else expr=new Expression(new ColumnLink(new int[]{table.getColumn("AD_CLIENT_ID").getId()}), "="+adClientId, null);
		}
		
		PairTable fixedColumns=null;
		Expression fixedExpr=null;
		try{
			fixedColumns= PairTable.parse(fixedcol,null );
	 		fixedExpr=Expression.parsePairTable(fixedColumns);
		}catch(NumberFormatException  e){
			logger.debug("params error->fixedcol format error");
			vh.put("code", "-1");
			vh.put("message", "参数错误");
			return vh;
		}
		
		if(fixedExpr!=null){
			if(expr==null){expr=fixedExpr;}
			else{expr=expr.combine(fixedExpr, SQLCombination.SQL_AND, null);}
		}

		query.addParam(expr);
		logger.debug("expr->"+(expr==null?"is null":expr.toString()));
		
		QueryResult result= QueryEngine.getInstance().doQuery(query);
		
	    String mainTablePath=(dataConfig.getMainURL().startsWith("/")?"":"/")+
	    				dataConfig.getMainURL(); // like "/news.vml", this is used in preview page
	    if(mainTablePath.indexOf("@ID@")==-1&&parm==null){
	    	mainTablePath=mainTablePath+"?id=@ID@";
	    }

	    int pkValue;
	    String url=null;
        
        //paco add begin 2015.01.08
        JSONArray dataja=new JSONArray();
        JSONObject currentjo=null;
        JSONObject returnjo=new JSONObject();
        //paco add end 2015.01.08
        
        Column column=null;
        ArrayList columns=null;
        int[] columnMasks = new int[] { Column.MASK_PRINT_SUBLIST };
		while(result.next()){
	        pkValue= Tools.getInt(result.getObject(1),-1);
	        url=StringUtils.replace(mainTablePath, "@ID@", String.valueOf(pkValue));  //mainTablePath+"?id="+pkValue;
	        if(parm!=null&&pos>0){
	        	url=StringUtils.replace(mainTablePath, parm, String.valueOf(result.getObject(pos)));  //mainTablePath+"?id="+pkValue;
	        }
	        
	        currentjo=new JSONObject();
	        try{
		        currentjo.put("MASTERHERFURL", url);
		        currentjo.put("id", pkValue);
		        columns= table.getColumns(columnMasks, true, 0);
		        for (int i = 0; i < columns.size(); i++) {
					column = (Column) columns.get(i);
					if (column.getDisplaySetting().getObjectType() == DisplaySetting.OBJ_CLOB) {
						currentjo.put(column.getName().toLowerCase(),result.getObject(i + 2));
					} else {
						currentjo.put(column.getName().toLowerCase(),result.getString(i + 2, true, false));
					}
				}
	        }catch(Exception e){
	        	
	        }
			
	       
	        dataja.put(currentjo);
		}
		
		vh.put("code", "0");
		vh.put("message", "操作成功");
		
		int allcount=0;
		if(result!=null){allcount=result.getTotalRowCount();}
		try {
			returnjo.put("allcount", allcount);
			returnjo.put("data", dataja);
		} catch (Exception e) {
			e.printStackTrace();
		}
		

		vh.put("data", returnjo);
		return vh;
	}
}
