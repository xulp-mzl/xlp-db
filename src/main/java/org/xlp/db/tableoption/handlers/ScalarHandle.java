package org.xlp.db.tableoption.handlers;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 当行单列结果集处理成Object对象的处理器
 * 
 * @author 徐龙平
 *         <p>
 *         2017-1-12
 *         </p>
 * @version 1.0
 * 
 */
public class ScalarHandle implements ResultSetHandle<Object>{
	//列名称
	private final String columnName;
	//列号
	private final Integer index;
	
	public ScalarHandle(){
		this(null, 1);
	}
	
	private ScalarHandle(String columnName, Integer index){
		this.columnName = columnName;
		this.index = index;
	}
	
	public ScalarHandle(String columnName){
		this(columnName, null);
	}
	
	public ScalarHandle(int index){
		this(null, index);
	}
	
	@Override
	public Object handle(ResultSet rs) throws SQLException {
		if (!rs.next()) {
			return null;
		}
		
		if (columnName == null && index == null) {
			return null;
		}
		
		if (index != null) {
			return rs.getObject(index);
		}
		return rs.getObject(columnName);
	}

}
