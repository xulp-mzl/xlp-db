package org.xlp.db.xml.ele;

import org.xlp.db.sql.SQL;


/**
 * 参数所需常量
 * 
 * @author 徐龙平
 * 
 * @version 1.0	
 */
public class ParamConstants {
	/**
	 * 参数标记
	 */
	public enum Flag{
		map, obj
	}
	
	/**
	 * 值类型
	 */
	public enum Type{
		number, string, date, stream, bool
	}
	
	/**
	 * 操作符
	 */
	public enum Op{
		in(SQL.IN), not_in(SQL.NOT_IN), like(SQL.LIKE),
		eq(SQL.EQ), not_eq(SQL.NOT_EQ), lt(SQL.LT),
		gt(SQL.GT), le(SQL.LE), ge(SQL.GE);
		private String op;
		private Op(String op){
			this.op = op;
		}
		
		public String getOp(){
			return op;
		}
	}
	
	/**
	 * 连接符
	 */
	public enum Connector{
		and(SQL.AND), or(SQL.OR), blank("");
		private String connector;
		private Connector(String connector){
			this.connector = connector;
		}
		
		public String getConnector(){
			return connector;
		}
	}
}
