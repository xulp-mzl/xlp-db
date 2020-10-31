package org.xlp.db.tableoption.handlers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

import org.xlp.db.tableoption.handlers.result.Result;
import org.xlp.db.tableoption.handlers.result.ResultWithAnnotation;

/**
 * 用注解结果集处理成javabean  Set集合，与相关注解一起使用
 * 
 * @author 徐龙平
 *         <p>
 *         2017-5-23
 *         </p>
 * @version 2.0
 * 
 */
public class BeanSetHandleWithAnnotation<T> implements ResultSetHandle<Set<T>> {
	// bean对象类型
	private final Class<T> cs;
	//结果集转换器
	private Result resultConverter;


	public BeanSetHandleWithAnnotation(Class<T> cs) {
		this(cs, new ResultWithAnnotation());
	}

	/**
	 * @param cs
	 * @param resultConverter
	 *            结果集转换器
	 */
	public BeanSetHandleWithAnnotation(Class<T> cs, Result resultConverter) {
		this.cs = cs;
		this.resultConverter = resultConverter;
	}

	@Override
	public Set<T> handle(ResultSet rs) throws SQLException {
		return resultConverter.toJavaBeanSet(rs, cs);
	}

}
