package org.xlp.db.tableoption.handlers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.xlp.db.tableoption.handlers.result.DefaultResult;
import org.xlp.db.tableoption.handlers.result.Result;

/**
 * 结果集处理成Map List集合（List<Map<>>）
 * 
 * @author 徐龙平
 *         <p>
 *         2017-5-23
 *         </p>
 * @version 2.0
 * 
 */
public class MapListHandle implements ResultSetHandle<List<Map<String, Object>>> {
	//结果集转换器
	private Result resultConverter;
	
	public MapListHandle() {
		this(new DefaultResult());
	}

	/**
	 * 结果集转换器
	 * @param resultConverter
	 */
	public MapListHandle(Result resultConverter){
		this.resultConverter = resultConverter;
	}
	
	@Override
	public List<Map<String, Object>> handle(ResultSet rs) throws SQLException {
		return resultConverter.toMapList(rs);
	}
}
