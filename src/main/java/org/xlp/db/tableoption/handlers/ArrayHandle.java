package org.xlp.db.tableoption.handlers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.xlp.db.tableoption.handlers.result.DefaultResult;
import org.xlp.db.tableoption.handlers.result.Result;

/**
 * 结果集处理成<code>Object[]</code>
 * 
 * @author 徐龙平
 *         <p>
 *         2017-5-23
 *         </p>
 * @version 2.0
 * 
 */
public class ArrayHandle implements ResultSetHandle<Object[]> {
	//结果集转换器
	private Result resultConverter;
	
	public ArrayHandle() {
		this(new DefaultResult());
	}

	/**
	 * 结果集转换器
	 * 
	 * @param result
	 */
	public ArrayHandle(Result resultConverter) {
		this.resultConverter = resultConverter;
	}
	
	@Override
	public Object[] handle(ResultSet rs) throws SQLException {
		return resultConverter.toArray(rs);
	}
}
