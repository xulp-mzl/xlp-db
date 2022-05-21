package org.xlp.db.sql;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.xlp.db.exception.EntityException;
import org.xlp.db.sql.limit.Limit;
import org.xlp.db.sql.table.Table;
import org.xlp.db.tableoption.key.CompoundPrimaryKey;
import org.xlp.db.utils.BeanUtil;
import org.xlp.utils.XLPStringUtil;

/**
 * 可含条件单表数据查询SQL信息类
 * 
 * @author 徐龙平
 *         <p>
 *         2017-5-27
 *         </p>
 * @version 1.0
 */
public class QuerySQL<T> extends QuerySQLAbstract<T> {
	/**
	 * 是否去重 
	 */
	private boolean distinct;
	
	// limit对象
	private Limit limit;
	// 要查询出列的数据
	private List<String> columnNames = new LinkedList<String>();

	/**
	 * 用bean对象构建此对象
	 * 
	 * @param bean
	 * @throws EntityException
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	public QuerySQL(T bean) throws EntityException {
		super(bean);
		columnNames.addAll(Arrays.asList(getTable().getAllColumnNames()));
	}

	/**
	 * 用beanClass对象构建此对象
	 * 
	 * @param beanClass
	 * @throws EntityException
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	public QuerySQL(Class<T> beanClass) throws EntityException {
		super(beanClass);
		columnNames.addAll(Arrays.asList(getTable().getAllColumnNames()));
	}

	private QuerySQL() {
		super();
	}

	/**
	 * 获取QuerySQL对象
	 * 
	 * @param bean
	 *            实体对象
	 * @return
	 * @throws EntityException
	 *             假如不是实体，则抛出该异常
	 * @throws NullPointerException
	 *             假如参数为null，则抛出该异常
	 */
	public static <T> QuerySQL<T> getInstance(T bean)
			throws EntityException {
		QuerySQL<T> querySQL = new QuerySQL<T>();
		@SuppressWarnings("unchecked")
		Class<T> beanClass = (Class<T>) bean.getClass();
		querySQL.setTable(new Table<T>(beanClass));
		querySQL.beanClass = beanClass;
		querySQL.primaryKey = new CompoundPrimaryKey(bean, false);
		return querySQL;
	}

	/**
	 * 添加要查询的列
	 * 
	 * @param fieldName
	 *            可以是实体的字段名，也可以数据库中的列名称
	 * @return
	 */
	public QuerySQL<T> queryName(String fieldName) {
		if (!XLPStringUtil.isEmpty(fieldName)) {
			String colName = BeanUtil.getFieldAlias(getTable(), fieldName);
			columnNames.add(colName);
		}
		return this;
	}

	/**
	 * 清除所有要查询的列
	 * 
	 * @return
	 */
	public QuerySQL<T> clearQuery() {
		columnNames.clear();
		return this;
	}

	/**
	 * 清除指定的查询列
	 * @param fieldName
	 *            可以是实体的字段名，也可以数据库中的列名称
	 * @return
	 */
	public QuerySQL<T> removeQuery(String fieldName) {
		if (!XLPStringUtil.isEmpty(fieldName)) {
			String colName = BeanUtil.getFieldAlias(getTable(), fieldName);
			columnNames.remove(colName);
		}
		return this;
	}

	/**
	 * 在构造时要初始化的逻辑，可以放在这个函数体中
	 * 
	 * @param bean
	 */
	@Override
	protected void init(T bean) throws EntityException {
		int count = primaryKey.getCount();

		String[] keyNames = primaryKey.getNames();
		Object[] keyValues = primaryKey.getValues();
		for (int i = 0; i < count; i++) {
			if (keyValues[i] != null
					&& !(primaryKey.isPrimitives()[i] && "0"
							.equals(keyValues[i].toString()))) {
				andEq(keyNames[i], keyValues[i]);
			}
		}

	}

	@Override
	public String getParamSql() {
		StringBuilder sb = new StringBuilder();
		sb.append(preSql());
		//拼接条件
		String condition = formatterConditionSql();
		if (!condition.isEmpty()) {
			sb.append("where ").append(condition);
		}
		//拼接分组排序
		sb.append(formatterGroupByAndOrderBySql());
		
		if (limit != null)
			sb.append(" limit ?,?");

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("形成的查询SQL语句是：" + sb);
		}
		return sb.toString();
	}

	@Override
	public String getSql() {
		StringBuilder sb = new StringBuilder();
		sb.append(preSql());
		//拼接条件
		String condition = formatterConditionSourceSql();
		if (!condition.isEmpty()) {
			sb.append("where ").append(condition);
		}
		//拼接分组排序
		sb.append(formatterGroupByAndOrderBySql());
		
		if (limit != null){
			sb.append(" limit ").append(limit.getStartPos())
				.append(COMMA).append(limit.getResultCount());
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("形成的查询SQL语句是：" + sb);
		}
		return sb.toString();
	}

	/**
	 * 获取查询语句的前缀
	 * 
	 * @return
	 */
	private StringBuilder preSql() {
		String tableAlias = SQLUtil.getTableAlias(getTable());
		
		StringBuilder pre = new StringBuilder("select ");
		if (distinct){
			pre.append(" distinct ");
		}
		// 假如要查的列长度0，则查询全部的列
		if (columnNames.size() == 0)
			columnNames = Arrays.asList(getTable().getAllColumnNames());

		int len = columnNames.size();
		if (len == 0) {
			pre.append(tableAlias).append("*");
		} else {
			for (int i = 0; i < len; i++) {
				if (i != 0)
					pre.append(COMMA);
				pre.append(tableAlias).append(columnNames.get(i));
			}
		}
		pre.append(" from ").append(getTable().getTableName()).append(" ")
			.append(tableAlias.isEmpty() ? tableAlias : tableAlias.substring(0, tableAlias.length() - 1))
			.append(" ");
		return pre;
	}

	/**
	 * 去除重复的数据
	 */
	public QuerySQL<T> distinct() {
		distinct = true;
		return this;
	}
	
	/**
	 * 分页查询信息，暂时只支持mysql数据库,其他数据库调用了该函数肯能会出错
	 * 
	 * @param limit
	 * @return
	 * @throws EntityException
	 */
	public QuerySQL<T> limit(Limit limit) throws EntityException {
		this.limit = limit;
		return this;
	}

	@Override
	public Object[] getParams() {
		Object[] values = super.getParams();
		if (limit != null) {
			int length = values.length;
			values = Arrays.copyOf(values, length + 2);
			values[length] = limit.getStartPos();
			values[length +1] = limit.getResultCount();
		}
		return values;
	}
}
