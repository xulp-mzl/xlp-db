package org.xlp.db.tableoption.handlers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.xlp.db.tableoption.handlers.result.DefaultResult;
import org.xlp.db.tableoption.handlers.result.Result;

/**
 * 结果集处理成javabeanList对象
 * 
 * @author 徐龙平
 *         <p>
 *         2017-5-23
 *         </p>
 * @version 2.0
 * 
 */
public class BeanListHandle<T> implements ResultSetHandle<List<T>> {
	// bean对象类型
	private final Class<T> cs;
	//结果集转换器
	private Result resultConverter;


	public BeanListHandle(Class<T> cs) {
		this(cs, new DefaultResult());
	}

	/**
	 * @param cs
	 * @param resultConverter
	 *            结果集转换器
	 */
	public BeanListHandle(Class<T> cs, Result resultConverter) {
		this.cs = cs;
		this.resultConverter = resultConverter;
	}

	@Override
	public List<T> handle(ResultSet rs) throws SQLException {
		return resultConverter.toJavaBeanList(rs, cs);
	}

}
