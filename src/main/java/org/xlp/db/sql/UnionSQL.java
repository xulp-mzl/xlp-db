package org.xlp.db.sql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xlp.assertion.AssertUtils;
import org.xlp.db.sql.limit.Limit;
import org.xlp.db.sql.table.Table;
import org.xlp.utils.XLPMapUtil;
import org.xlp.utils.XLPStringUtil;

/**
 * <p>创建时间：2022年5月21日 下午10:50:08</p>
 * @author xlp
 * @version 1.0 
 * @Description Union sql对象
*/
public class UnionSQL implements Query {
	/**
	 * 日志记录
	 */
	protected final static Logger LOGGER = LoggerFactory.getLogger(UnionSQL.class);
	
	/**
	 * 起始Query对象
	 */
	private Query query;
	
	/**
	 * limit
	 */
	private Limit limit;
	
	/**
	 * 存储排序字段
	 */
	private Map<String, String> sortFields = new LinkedHashMap<String, String>();
	
	/**
	 * 查询出数据转换成bean是的类名，如果不设置，取主查询对象对应的实体类
	 */
	private Class<?> entityClass;
	
	/**
	 * 整个SQL对象查询形成临时表的别名，默认值是：a
	 */
	private String alias = "a";
	
	/**
	 * 子Query对象，key：union [all] 后的Query对象，value：UnionType枚举对象
	 */
	private Map<Query, UnionType> childrenQueryMap = new LinkedHashMap<Query, UnionType>();

	/**
	 * 私有构造函数
	 */
	private UnionSQL() {
	}
	
	/**
	 * 构造UnionSQL对象
	 * 
	 * @param query 可查询SQL对象
	 * @return
	 * @throws NullPointerException 假如参数为null，则抛出该异常
	 */
	public static UnionSQL of(Query query){
		UnionSQL unionSQL = new UnionSQL();
		unionSQL.query = query;
		return unionSQL;
	}
	
	@Override
	public String getSql() {
		StringBuilder sb = new StringBuilder(getParamSql());
		String sql = SQLUtil.fillWithParams(sb, getParams());
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("执行SQL语句是：" + sql);
		}
		return sql;
	}

	@Override
	public String getParamSql() {
		boolean wrapper = limit != null || !XLPMapUtil.isEmpty(sortFields);
		StringBuilder sb = new StringBuilder();
		if (wrapper) {
			sb.append("select * from (");
		}
		if (!XLPMapUtil.isEmpty(childrenQueryMap)) {
			sb.append(LEFT_BRACKET);
		}
		sb.append(query.getParamSql());
		if (!XLPMapUtil.isEmpty(childrenQueryMap)) {
			sb.append(RIGHT_BRACKET);
		}
		//拼接子SQL对象
		for (Entry<Query, UnionType> entry : childrenQueryMap.entrySet()) {
			sb.append(" ").append(entry.getValue().getValue())
				.append(" ").append(LEFT_BRACKET).append(entry.getKey().getParamSql())
				.append(RIGHT_BRACKET);
		}
		if (wrapper) {
			sb.append(RIGHT_BRACKET).append(" ").append(getAlias());
		}
		
		//拼接排序SQL片段
		if (!XLPMapUtil.isEmpty(sortFields)) {
			sb.append(" order by ");
			boolean start = true;
			for (Entry<String, String> sort : sortFields.entrySet()) {
				if (!start) {
					sb.append(COMMA).append(" ");
				}
				sb.append(sort.getKey()).append(" ").append(sort.getValue());
				start = false;
			}
		}
		
		//拼接limt SQL片段
		if (limit != null) {
			sb.append(" limit ?,?");
		}
		String sql = sb.toString();
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("执行SQL语句是：" + sql);
		}
		return sql;
	}

	@Override
	public Object[] getParams() {
		List<Object> params = new ArrayList<Object>();
		params.addAll(Arrays.asList(query.getParams()));
		for (Entry<Query, UnionType> entry : childrenQueryMap.entrySet()) {
			params.addAll(Arrays.asList(entry.getKey().getParams()));
		}
		if (limit != null) {
			params.add(limit.getStartPos());
			params.add(limit.getResultCount());
		}
		return params.toArray();
	}

	@Override
	public Class<?> getEntityClass() {
		return entityClass == null && query != null ? query.getEntityClass() : entityClass;
	}

	/**
	 * 返回主查询SQL对象对应的Table对象
	 */
	@Override
	public Table<?> getTable() {
		return query != null ? query.getTable() : null;
	}

	/**
	 * @param entityClass 设置的查询出数据转换成bean是的类名
	 */
	public UnionSQL setEntityClass(Class<?> entityClass) {
		this.entityClass = entityClass;
		return this;
	}

	/**
	 * @return the limit
	 */
	public Limit getLimit() {
		return limit;
	}

	/**
	 * @param limit the limit to set
	 */
	public UnionSQL limit(Limit limit) {
		this.limit = limit;
		return this;
	}

	/**
	 * @return the sortFields
	 */
	public Map<String, String> getSortFields() {
		return sortFields;
	}

	/**
	 * 整个SQL对象查询形成临时表的别名，默认值是：a
	 * 
	 * @return the alias
	 */
	public String getAlias() {
		return alias;
	}

	/**
	 * @param alias 整个SQL对象查询形成临时表的别名
	 */
	public UnionSQL setAlias(String alias) {
		if (!XLPStringUtil.isEmpty(alias)) {
			this.alias = alias;
		}
		return this;
	}

	/**
	 * @return 子Query对象，key：union [all] 后的Query对象，value：UnionType枚举对象
	 */
	public Map<Query, UnionType> getChildrenQueryMap() {
		return childrenQueryMap;
	}
	
	/**
	 * union 操作
	 * 
	 * @param query
	 * @return
	 * @throws NullPointerException 假如参数为null，则抛出该异常
	 */
	public UnionSQL union(Query query){
		AssertUtils.isNotNull(query, "query parameter is null!");
		childrenQueryMap.put(query, UnionType.UNION);
		return this;
	}
	
	/**
	 * union all 操作
	 * 
	 * @param query
	 * @return
	 * @throws NullPointerException 假如参数为null，则抛出该异常
	 */
	public UnionSQL unionAll(Query query){
		AssertUtils.isNotNull(query, "query parameter is null!");
		childrenQueryMap.put(query, UnionType.UNION_ALL);
		return this;
	}
	
	/**
	 * order by
	 * 
	 * @param fieldName 字段名称
	 * @param orderType 排序方式（asc | desc）
	 * @return
	 */
	private UnionSQL orderBy(String fieldName, String orderType){
		if(fieldName == null)
			return this;
		String colName = SQLUtil.getColumnName(fieldName);
		sortFields.put(colName, orderType);
		return this;
	}
	
	/**
	 * 排序升序
	 * 
	 * @param fieldName 字段名
	 * @return SQL对象
	 */
	public UnionSQL asc(String fieldName){
		return orderBy(fieldName, ASC);
	}
	
	/**
	 * 排序降序
	 * 
	 * @param fieldName 字段名
	 * @return SQL对象
	 */
	public UnionSQL desc(String fieldName){
		return orderBy(fieldName, DESC);
	}

	/**
	 * Union 查询类型
	 */
	public static enum UnionType{
		UNION("union"),
		UNION_ALL("union all");
		
		private String value;
		
		private UnionType(String value){
			this.value = value;
		}
		
		public String getValue(){
			return value;
		}
	}
}
