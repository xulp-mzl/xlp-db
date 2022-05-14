package org.xlp.db.sql.item;
/**
 * <p>创建时间：2022年5月13日 下午11:09:39</p>
 * @author xlp
 * @version 1.0 
 * @Description sql连接符枚举
*/
public enum ConnectorEnum {
	AND("and"),
	OR("or");
	
	/**
	 * 连接符
	 */
	private String connector;
	
	private ConnectorEnum(String connector){
		this.connector = connector;
	}
	
	public String getConnector(){
		return connector;
	}
}
