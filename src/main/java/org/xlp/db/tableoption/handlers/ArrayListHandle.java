package org.xlp.db.tableoption.handlers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.xlp.db.tableoption.handlers.result.DefaultResult;
import org.xlp.db.tableoption.handlers.result.Result;

/**
 * 结果集处理成<code>Object[]</code> List集合（List<Object[]>）
 * 
 * @author 徐龙平
 *         <p>
 *         2017-5-23
 *         </p>
 * @version 2.0
 * 
 */
public class ArrayListHandle implements ResultSetHandle<List<Object[]>> {
	//结果集转换器
	private Result resultConverter;
	
	public ArrayListHandle() {
		this(new DefaultResult());
	}

	/**
	 * 结果集转换器
	 * 
	 * @param resultConverter
	 */
	public ArrayListHandle(Result resultConverter) {
		this.resultConverter = resultConverter;
	}
	
	@Override
	public List<Object[]> handle(ResultSet rs) throws SQLException {
		return resultConverter.toArrayList(rs);
	}
}
