package org.xlp.db.xml;

import java.util.List;
import java.util.Map;

import org.dom4j.Element;
import org.xlp.db.xml.ele.SQLEle;

/**
 * @author 徐龙平
 * 
 * @version 1.0	
 * 
 * <p>主要功能是得到指定的SQLEle对象
 */
public final class SQLEleHelper {
	/**
	 * 用指定的sqls元素id和sql元素的id找出SQLEle对象
	 * 
	 * @param sqlsId
	 * @param sqlId
	 * @return SQLEle 假如未找到返回null
	 * @throws XMLSQLException 
	 */
	public static SQLEle getSqlEle(String sqlsId, String sqlId) 
		throws XMLSQLException{
		SQLEle sqlEle = getSqlEle(sqlsId, sqlId, SqlsContext.sqlsMap);
		if(sqlEle == null)
			throw new XMLSQLException("以指定的sqls元素id:[" + sqlsId + "]和sql元素的id:[" + sqlId + "]找不到SQLEle对象");
		return sqlEle;
	}
	
	/**
	 * 用SqlsContext对象和指定的sqls元素id和sql元素的id找出SQLEle对象
	 * 
	 * @param context
	 * @param sqlsId
	 * @param sqlId
	 * @return SQLEle 假如未找到返回null
	 * @throws XMLSQLException 
	 */
	public static SQLEle getSqlEle(SqlsContext context, String sqlsId, String sqlId)
		throws XMLSQLException{
		SQLEle sqlEle = getSqlEle(sqlsId, sqlId, context.getSqlsMap());
		if(sqlEle == null)
			throw new XMLSQLException("以指定的sqls元素id:[" + sqlsId + "]和sql元素的id:[" + sqlId + "]找不到SQLEle对象");
		return sqlEle;
	}
	
	/**
	 * 
	 * @param sqlsId
	 * @param sqlId
	 * @param map
	 * @return
	 */
	private static SQLEle getSqlEle(String sqlsId, String sqlId
			, Map<String, Element> map){
		List<SQLEle> sqlEles = new SqlsEle(sqlsId, map).getSqlEles();
		for (SQLEle sqlEle : sqlEles) {
			if(sqlEle.getId().equals(sqlId))
				return sqlEle;
		}
		return null;
	}
}
