package org.xlp.db.tableoption.handlers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.xlp.db.tableoption.annotation.XLPEntity;
import org.xlp.db.tableoption.handlers.result.DefaultResult;
import org.xlp.db.tableoption.handlers.result.Result;
import org.xlp.db.tableoption.handlers.result.ResultWithAnnotation;

/**
 * 结果集处理成javabean对象，自动选择适应的处理器
 * 
 * @author 徐龙平
 *         <p>
 *         2017-12-9
 *         </p>
 * @version 1.0
 * 
 */
public class DefaultBeanHandle<T> implements ResultSetHandle<T> {
	// bean对象类型
	private final Class<T> cs;
	// 结果集转换器
	private Result resultConverter;

	/***
	 * @param cs
	 * @throws NullPointerException
	 *             假如参数为空抛出该异常
	 */
	public DefaultBeanHandle(Class<T> cs) {
		if(cs == null)
			throw new NullPointerException("cs参数不能为null");
		this.cs = cs;
		this.resultConverter = cs.getAnnotation(XLPEntity.class)
				== null ? new DefaultResult() : new ResultWithAnnotation();
	}

	/**
	 * @param cs
	 * @param resultConverter
	 *            结果集转换器
	 */
	public DefaultBeanHandle(Class<T> cs, Result resultConverter) {
		this.cs = cs;
		this.resultConverter = resultConverter;
	}

	@Override
	public T handle(ResultSet rs) throws SQLException {
		return resultConverter.toJavaBean(rs, cs);
	}

}
