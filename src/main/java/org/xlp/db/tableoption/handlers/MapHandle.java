package org.xlp.db.tableoption.handlers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.xlp.db.tableoption.handlers.result.DefaultResult;
import org.xlp.db.tableoption.handlers.result.Result;

/**
 * 结果集处理成Map对象
 * 
 * @author 徐龙平
 *         <p>
 *         2017-5-23
 *         </p>
 * @version 2.0
 * 
 */
public class MapHandle implements ResultSetHandle<Map<String, Object>> {
	//结果集转换器
	private Result resultConverter;
	
	public MapHandle() {
		this(new DefaultResult());
	}

	/**
	 * 结果集转换器
	 * @param resultConverter
	 */
	public MapHandle(Result resultConverter){
		this.resultConverter = resultConverter;
	}
	
	@Override
	public Map<String, Object> handle(ResultSet rs) throws SQLException {
		return resultConverter.toMap(rs);
	}
}
