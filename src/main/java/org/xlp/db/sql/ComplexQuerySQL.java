package org.xlp.db.sql;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xlp.assertion.AssertUtils;
import org.xlp.db.exception.EntityException;
import org.xlp.db.sql.item.ComplexQueryFieldItem;
import org.xlp.db.sql.item.ComplexQueryFieldItem.ValueType;
import org.xlp.db.sql.item.ConnectorEnum;
import org.xlp.db.sql.item.OperatorEnum;
import org.xlp.db.sql.limit.Limit;
import org.xlp.db.sql.statisticsfun.Avg;
import org.xlp.db.sql.statisticsfun.DistinctCount;
import org.xlp.db.sql.statisticsfun.Max;
import org.xlp.db.sql.statisticsfun.Min;
import org.xlp.db.sql.statisticsfun.SQLStatisticsType;
import org.xlp.db.sql.statisticsfun.Sum;
import org.xlp.db.sql.table.Table;
import org.xlp.db.tableoption.annotation.XLPForeign;
import org.xlp.utils.XLPArrayUtil;
import org.xlp.utils.XLPSplitUtils;
import org.xlp.utils.XLPStringUtil;

/**
 * <p>创建时间：2022年5月15日 下午7:52:43</p>
 * @author xlp
 * @version 1.0 
 * @Description 多表联合查询SQL对象
*/
public class ComplexQuerySQL implements SQL{
	/**
	 * 日志记录
	 */
	protected final static Logger LOGGER = LoggerFactory.getLogger(ComplexQuerySQL.class);
	
	/**
	 * 标对象
	 */
	private Table<?> table;
	
	/**
	 * 前一个ComplexQuerySQL查询对象
	 */
	private ComplexQuerySQL preComplexQuerySQL;
	
	/**
	 * 最顶层ComplexQuerySQL查询对象
	 */
	private ComplexQuerySQL topComplexQuerySQL;
	
	/**
	 * 子ComplexQuerySQL查询对象
	 */
	private List<ComplexQuerySQL> childrenComplexQuerySQL = new ArrayList<ComplexQuerySQL>();
	
	/**
	 * 统计数据
	 */
	private List<SQLStatisticsType> sqlStatisticsType = new ArrayList<SQLStatisticsType>();
	
	/**
	 * 查询出的字段key名称
	 */
	public static final String QUERY_FIELD_NAME_KEY = "name";
	
	/**
	 * 查询出的字段别名key名称
	 */
	public static final String QUERY_FIELD_ALIAS_KEY = "alias";
	
	/**
	 * 需要查询出的字段名称[{name:xxx, alias:xxx}]
	 */
	private List<Map<String, String>> queryFields = new ArrayList<Map<String, String>>();
	
	/**
	 * 表连接方式
	 */
	private JoinType joinType;
	
	/**
	 * sql条件对象集合
	 */
	protected List<ComplexQueryFieldItem> fieldItems = new ArrayList<ComplexQueryFieldItem>();
	
	/**
	 * 存储主键对应的字段
	 */
	private List<Field> primaryFields = new ArrayList<Field>(0);
	
	/**
	 * 存储外键对应的字段
	 */
	private List<Field> foreignFields = new ArrayList<Field>(0);
	
	/**
	 * 标记是否去除重复的数据
	 */
	private boolean distinct;
	
	/**
	 * limit对象暂时只支持mysql数据库
	 */
	private Limit limit;
	
	/**
	 * 存储排序字段
	 */
	private Map<String, String> sortFields = new LinkedHashMap<String, String>();
	
	/**
	 * 存储分组字段
	 */
	private Set<String> groupFields = new LinkedHashSet<String>();
	
	/**
	 * 标记 having 条件开始
	 */
	private boolean having = false;
	
	/**
	 * sql having 条件对象集合
	 */
	protected List<ComplexQueryFieldItem> havingFieldItems = new ArrayList<ComplexQueryFieldItem>();
	
	/**
	 * 标记是否是执行count语句
	 */
	private boolean exeCount = false;
	
	/**
	 * 私有构造函数
	 */
	private ComplexQuerySQL(){}
	
	/**
	 * 用实体类构造该对象
	 * 
	 * @param entityClass
	 * @return
	 * @throws NullPointerException 假如第参数为null，则抛出该异常
	 * @throws EntityException 
	 */
	public static <T> ComplexQuerySQL of(Class<T> beanClass){
		return of(beanClass, null);
	}
	
	/**
	 * 用实体类构造该对象
	 * 
	 * @param entityClass
	 * @param alias 实体类别名
	 * @return
	 * @throws NullPointerException 假如第一个参数为null，则抛出该异常
	 * @throws EntityException 
	 */
	public static <T> ComplexQuerySQL of(Class<T> beanClass, String alias){
		AssertUtils.isNotNull(beanClass, "beanClass paramter is null!");
		ComplexQuerySQL complexQuerySQL = new ComplexQuerySQL();
		complexQuerySQL.table = new Table<>(beanClass);
		complexQuerySQL.table.setAlias(alias);
		complexQuerySQL.foreignFields = complexQuerySQL.table.getForeignFields();
		complexQuerySQL.primaryFields = complexQuerySQL.table.getPrimaryFields();
		return complexQuerySQL;
	}
	
	/**
	 * 类似两表inner join操作
	 * 
	 * @param beanClass 要操作表对应的实体
	 * @param alias 对应的别名
	 * @param joinType
	 * @param usePrimaryAndForeignKey 是否用外键和组件作为默认连接
	 * @return
	 * @throws NullPointerException 假如第一个参数为null，则抛出该异常
	 * @throws EntityException 
	 */
	private <T> ComplexQuerySQL join(Class<T> beanClass, String alias, JoinType joinType,
			boolean usePrimaryAndForeignKey){
		AssertUtils.isNotNull(beanClass, "beanClass paramter is null!");
		ComplexQuerySQL complexQuerySQL = of(beanClass, alias);
		complexQuerySQL.joinType = joinType;
		this.childrenComplexQuerySQL.add(complexQuerySQL);
		complexQuerySQL.preComplexQuerySQL = this;
		
		if (usePrimaryAndForeignKey) {
			addDefaultCondition(complexQuerySQL, this);
		}
		
		return complexQuerySQL;
	}
	
	/**
	 * 添加两表默认的条件
	 * 
	 * @param complexQuerySQL1
	 * @param complexQuerySQL2
	 */
	private void addDefaultCondition(ComplexQuerySQL complexQuerySQL1, ComplexQuerySQL complexQuerySQL2) {
		List<Field> primaryFields1 = complexQuerySQL1.primaryFields;
		List<Field> foreignFields1 = complexQuerySQL1.foreignFields;
		
		List<Field> primaryFields2 = complexQuerySQL2.primaryFields;
		List<Field> foreignFields2 = complexQuerySQL2.foreignFields;
		
		String tableAlias1 = SQLUtil.getTableAlias(complexQuerySQL1.table);
		String tableAlias2 = SQLUtil.getTableAlias(complexQuerySQL2.table);
		
		XLPForeign foreign;
		String[] foreignFieldNames;
		String primaryFieldName;
		for (Field field2 : foreignFields2) {
			foreign = field2.getAnnotation(XLPForeign.class);
			foreignFieldNames = foreign.to();
			foreignFieldNames = foreignFieldNames.length == 0 
					? new String[]{field2.getName()} : foreignFieldNames;
			if (foreign.value() == complexQuerySQL1.getEntityClass()) {
				out: for (Field field : primaryFields1) {
					primaryFieldName = field.getName();
					for (String foreignFieldName : foreignFieldNames) { 
						if (primaryFieldName.equals(foreignFieldName)) {
							complexQuerySQL1.andEq(tableAlias2 + foreignFieldName , tableAlias1 + primaryFieldName, true);
							break out;
						}
					}
				}
			}
		}
		
		for (Field field2 : foreignFields1) {
			foreign = field2.getAnnotation(XLPForeign.class);
			foreignFieldNames = foreign.to();
			foreignFieldNames = foreignFieldNames.length == 0 
					? new String[]{field2.getName()} : foreignFieldNames;
			if (foreign.value() == complexQuerySQL1.getEntityClass()) {
				out: for (Field field : primaryFields2) {
					primaryFieldName = field.getName();
					for (String foreignFieldName : foreignFieldNames) { 
						if (primaryFieldName.equals(foreignFieldName)) {
							complexQuerySQL1.andEq(tableAlias1 + foreignFieldName , tableAlias2 + primaryFieldName, true);
							break out;
						}
					}
				}
			}
		}
	}

	/**
	 * 类似两表inner join操作
	 * 
	 * @param beanClass 要操作表对应的实体
	 * @param alias 对应的别名
	 * @param usePrimaryAndForeignKey 是否用外键和组件作为默认连接
	 * @return
	 * @throws NullPointerException 假如第一个参数为null，则抛出该异常
	 * @throws EntityException 
	 */
	public <T> ComplexQuerySQL innerJoin(Class<T> beanClass, String alias,
			boolean usePrimaryAndForeignKey){
		return join(beanClass, alias, JoinType.INNER, usePrimaryAndForeignKey);
	}
	
	/**
	 * 类似两表inner join操作
	 * 
	 * @param beanClass 要操作表对应的实体
	 * @param alias 对应的别名
	 * @return
	 * @throws NullPointerException 假如第一个参数为null，则抛出该异常
	 * @throws EntityException 
	 */
	public <T> ComplexQuerySQL innerJoin(Class<T> beanClass, String alias){
		return innerJoin(beanClass, alias, true);
	}
	
	/**
	 * 类似两表inner join操作
	 * 
	 * @param beanClass 要操作表对应的实体
	 * @param usePrimaryAndForeignKey 是否用外键和组件作为默认连接
	 * @return
	 * @throws NullPointerException 假如第一个参数为null，则抛出该异常
	 * @throws EntityException 
	 */
	public <T> ComplexQuerySQL innerJoin(Class<T> beanClass,
			boolean usePrimaryAndForeignKey){
		return innerJoin(beanClass, null, usePrimaryAndForeignKey);
	}
	
	/**
	 * 类似两表inner join操作
	 * 
	 * @param beanClass 要操作表对应的实体
	 * @return
	 * @throws NullPointerException 假如第参数为null，则抛出该异常
	 * @throws EntityException 
	 */
	public <T> ComplexQuerySQL innerJoin(Class<T> beanClass){
		return innerJoin(beanClass, true);
	}
	
	/**
	 * 类似两表left join操作
	 * 
	 * @param beanClass 要操作表对应的实体
	 * @param alias 对应的别名
	 * @param usePrimaryAndForeignKey 是否用外键和组件作为默认连接
	 * @return
	 * @throws NullPointerException 假如第一个参数为null，则抛出该异常
	 * @throws EntityException 
	 */
	public <T> ComplexQuerySQL leftJoin(Class<T> beanClass, String alias,
			boolean usePrimaryAndForeignKey){
		return join(beanClass, alias, JoinType.LEFT, usePrimaryAndForeignKey);
	}
	
	/**
	 * 类似两表left join操作
	 * 
	 * @param beanClass 要操作表对应的实体
	 * @param alias 对应的别名
	 * @return
	 * @throws NullPointerException 假如第一个参数为null，则抛出该异常
	 * @throws EntityException 
	 */
	public <T> ComplexQuerySQL leftJoin(Class<T> beanClass, String alias){
		return leftJoin(beanClass, alias, true);
	}
	
	/**
	 * 类似两表left join操作
	 * 
	 * @param beanClass 要操作表对应的实体
	 * @param usePrimaryAndForeignKey 是否用外键和组件作为默认连接
	 * @return
	 * @throws NullPointerException 假如第一个参数为null，则抛出该异常
	 * @throws EntityException 
	 */
	public <T> ComplexQuerySQL leftJoin(Class<T> beanClass,
			boolean usePrimaryAndForeignKey){
		return leftJoin(beanClass, null, usePrimaryAndForeignKey);
	}
	
	/**
	 * 类似两表inner join操作
	 * 
	 * @param beanClass 要操作表对应的实体
	 * @return
	 * @throws NullPointerException 假如第参数为null，则抛出该异常
	 * @throws EntityException 
	 */
	public <T> ComplexQuerySQL leftJoin(Class<T> beanClass){
		return leftJoin(beanClass, true);
	}
	
	/**
	 * 类似两表right join操作
	 * 
	 * @param beanClass 要操作表对应的实体
	 * @param alias 对应的别名
	 * @param usePrimaryAndForeignKey 是否用外键和组件作为默认连接
	 * @return
	 * @throws NullPointerException 假如第一个参数为null，则抛出该异常
	 * @throws EntityException 
	 */
	public <T> ComplexQuerySQL rightJoin(Class<T> beanClass, String alias,
			boolean usePrimaryAndForeignKey){
		return join(beanClass, alias, JoinType.RIGHT, usePrimaryAndForeignKey);
	}
	
	/**
	 * 类似两表right join操作
	 * 
	 * @param beanClass 要操作表对应的实体
	 * @param alias 对应的别名
	 * @return
	 * @throws NullPointerException 假如第一个参数为null，则抛出该异常
	 * @throws EntityException 
	 */
	public <T> ComplexQuerySQL rightJoin(Class<T> beanClass, String alias){
		return rightJoin(beanClass, alias, true);
	}
	
	/**
	 * 类似两表right join操作
	 * 
	 * @param beanClass 要操作表对应的实体
	 * @param usePrimaryAndForeignKey 是否用外键和组件作为默认连接
	 * @return
	 * @throws NullPointerException 假如第一个参数为null，则抛出该异常
	 * @throws EntityException 
	 */
	public <T> ComplexQuerySQL rightJoin(Class<T> beanClass,
			boolean usePrimaryAndForeignKey){
		return rightJoin(beanClass, null, usePrimaryAndForeignKey);
	}
	
	/**
	 * 类似两表inner join操作
	 * 
	 * @param beanClass 要操作表对应的实体
	 * @return
	 * @throws NullPointerException 假如第参数为null，则抛出该异常
	 * @throws EntityException 
	 */
	public <T> ComplexQuerySQL rightJoin(Class<T> beanClass){
		return rightJoin(beanClass, true);
	}
	
	public ComplexQuerySQL getPreComplexQuerySQL() {
		return preComplexQuerySQL;
	}

	public List<ComplexQuerySQL> getChildrenComplexQuerySQL() {
		return childrenComplexQuerySQL;
	}

	public List<SQLStatisticsType> getSqlStatisticsType() {
		return getTopComplexQuerySQL().sqlStatisticsType;
	}

	public List<Map<String, String>> getQueryFields() {
		return getTopComplexQuerySQL().queryFields;
	}

	public JoinType getJoinType() {
		return joinType;
	}

	public Table<?> getTable() {
		return table;
	}

	@Override
	public String getSql() {
		StringBuilder sb = new StringBuilder(getParamSql());
		return SQLUtil.fillWithParams(sb, getParams());
	}

	@Override
	public String getParamSql() {
		return SQLPartUtil.formatTablePartSql(this);
	}

	@Override
	public Object[] getParams() {
		return SQLParamUtil.getSqlParams(this);
	}

	@Override
	public Class<?> getEntityClass() {
		ComplexQuerySQL complexQuerySQL = getTopComplexQuerySQL();
		//获取最顶层实体类
		return complexQuerySQL.getEntityClass();
	}
	
	/**
	 * 获取最顶层查询对象
	 * @return
	 */
	public ComplexQuerySQL getTopComplexQuerySQL(){
		if (topComplexQuerySQL == null) {
			ComplexQuerySQL preComplexQuerySQL = this.getPreComplexQuerySQL();
			ComplexQuerySQL complexQuerySQL = this;
			while (preComplexQuerySQL != null) {
				complexQuerySQL = preComplexQuerySQL;
				preComplexQuerySQL = preComplexQuerySQL.getPreComplexQuerySQL();
			}
			topComplexQuerySQL = complexQuerySQL;
		}
		return topComplexQuerySQL;
	}

	/**
	 * 开始分组条件查询
	 * 
	 * @return
	 */
	public ComplexQuerySQL group(){
		fieldItems.add(new ComplexQueryFieldItem(OperatorEnum.LEFT_BRACKET, null));
		return this;
	}
	
	/**
	 * 结束分组条件查询
	 * 
	 * @return
	 */
	public ComplexQuerySQL endGroup(){
		fieldItems.add(new ComplexQueryFieldItem(OperatorEnum.RIGHT_BRACKET, null));
		return this;
	}
	
	/**
	 * 条件拼装
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value 其对应值
	 * @param connector 条件（and | or）
	 * @param op 操作符（=，>, < ...）
	 * @param flagValue 标记value是否是字段名称
	 * @return
	 */
	private ComplexQuerySQL M(String fieldName, Object value
			, ConnectorEnum connector, OperatorEnum op, boolean flagValue){
		if(fieldName == null)
			return this;
		
		if (getTopComplexQuerySQL().having) {
			String colName = SQLUtil.getColumnName(fieldName, table, false);
			if (flagValue) {
				value = SQLUtil.getColumnName(fieldName, table, false);
			}
			getTopComplexQuerySQL().havingFieldItems
				.add(new ComplexQueryFieldItem(connector, op, colName, value, null,
						flagValue ? ValueType.FIELD : ValueType.VALUE));
		} else {
			String colName = SQLUtil.getColumnName(fieldName, table);
			if (flagValue) {
				value = SQLUtil.getColumnName(fieldName, table);
			}
			fieldItems.add(new ComplexQueryFieldItem(connector, op, colName, value, null, 
					flagValue ? ValueType.FIELD : ValueType.VALUE));
		}
		return this;
	}
	
	/**
	 * null
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param connector 条件（and | or）
	 * @param op 操作符（is null，is not null）
	 * @return
	 */
	private ComplexQuerySQL NULL(String fieldName
			, ConnectorEnum connector, OperatorEnum op){
		if(fieldName == null)
			return this;
		String colName = SQLUtil.getColumnName(fieldName, table);
		
		fieldItems.add(new ComplexQueryFieldItem(connector, op, colName, null, null));
		
		return this;
	}
	
	/**
	 * 条件or=
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public ComplexQuerySQL orEq(String fieldName, Object value){
		return orEq(fieldName, value, false);
	}
	
	/**
	 * 条件or=
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value 对应的值，或bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param flagValue 标记value是否是字段名称
	 * @return SQL对象
	 */
	public ComplexQuerySQL orEq(String fieldName, Object value, boolean flagValue){
		if(value == null && !flagValue){
			return orIsNull(fieldName);
		}		
		return M(fieldName, value, ConnectorEnum.OR, OperatorEnum.EQ, flagValue);
	}
	
	/**
	 * 条件or is null
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @return SQL对象
	 */
	public ComplexQuerySQL orIsNull(String fieldName){
		return NULL(fieldName, ConnectorEnum.OR, OperatorEnum.IS_NULL);
	}
	
	/**
	 * 条件and=
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public ComplexQuerySQL andEq(String fieldName, Object value){
		return andEq(fieldName, value, false);
	}
	
	/**
	 * 条件and=
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value 对应的值，或bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param flagValue 标记value是否是字段名称
	 * @return SQL对象
	 */
	public ComplexQuerySQL andEq(String fieldName, Object value, boolean flagValue){
		if(value == null && !flagValue){
			return andIsNull(fieldName);
		}		
		return M(fieldName, value, ConnectorEnum.AND, OperatorEnum.EQ, flagValue);
	}
	
	/**
	 * 条件and is null
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @return SQL对象
	 */
	public ComplexQuerySQL andIsNull(String fieldName){
		return NULL(fieldName, ConnectorEnum.AND, OperatorEnum.IS_NULL);
	}

	/**
	 * 条件or !=
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value 对应的值，或bean字段名，也可以是数据库中表的列名，但最好是bean字段名
     * @param flagValue 标记value是否是字段名称
	 * @return SQL对象
	 */
	public ComplexQuerySQL orNotEq(String fieldName, Object value, boolean flagValue){
		if(value == null && !flagValue)
			return orNotNull(fieldName);

		return M(fieldName, value, ConnectorEnum.OR, OperatorEnum.NEQ, flagValue);
	}
	
	/**
	 * 条件or !=
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public ComplexQuerySQL orNotEq(String fieldName, Object value){
		return orNotEq(fieldName, value, false);
	}
	
	/**
	 * 条件or is not null
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @return SQL对象
	 */
	public ComplexQuerySQL orNotNull(String fieldName){
		return NULL(fieldName, ConnectorEnum.OR, OperatorEnum.IS_NOT_NULL);
	}
	
	/**
	 * 条件and !=
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value 对应的值，或bean字段名，也可以是数据库中表的列名，但最好是bean字段名
     * @param flagValue 标记value是否是字段名称
	 * @return SQL对象
	 */
	public ComplexQuerySQL andNotEq(String fieldName, Object value, boolean flagValue){
		if(value == null && !flagValue)
			return andNotNull(fieldName);

		return M(fieldName, value, ConnectorEnum.AND, OperatorEnum.NEQ, flagValue);
	}
	
	/**
	 * 条件and !=
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public ComplexQuerySQL andNotEq(String fieldName, Object value){
		return andNotEq(fieldName, value, false);
	}
	
	/**
	 * 条件and is not null
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @return SQL对象
	 */
	public ComplexQuerySQL andNotNull(String fieldName){
		return NULL(fieldName, ConnectorEnum.AND, OperatorEnum.IS_NOT_NULL);
	}
	
	/**
	 * 条件and >
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public ComplexQuerySQL andGt(String fieldName, Object value){
		return M(fieldName, value, ConnectorEnum.AND, OperatorEnum.GT, false);
	}
	
	/**
	 * 条件or >
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public ComplexQuerySQL orGt(String fieldName, Object value){
		return M(fieldName, value, ConnectorEnum.OR, OperatorEnum.GT, false);
	}
	
	/**
	 * 条件and <
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public ComplexQuerySQL andLt(String fieldName, Object value){
		return M(fieldName, value, ConnectorEnum.AND, OperatorEnum.LT, false);
	}
	
	/**
	 * 条件or <
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public ComplexQuerySQL orLt(String fieldName, Object value){
		return M(fieldName, value, ConnectorEnum.OR, OperatorEnum.LT, false);
	}
	
	/**
	 * 条件and <=
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public ComplexQuerySQL andLe(String fieldName, Object value){
		return M(fieldName, value, ConnectorEnum.AND, OperatorEnum.LE, false);
	}
	
	/**
	 * 条件or <=
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public ComplexQuerySQL orLe(String fieldName, Object value){
		return M(fieldName, value, ConnectorEnum.OR, OperatorEnum.LE, false);
	}
	
	/**
	 * 条件and >=
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public ComplexQuerySQL andGe(String fieldName, Object value){
		return M(fieldName, value, ConnectorEnum.AND, OperatorEnum.GE, false);
	}
	
	/**
	 * 条件or >=
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public ComplexQuerySQL orGe(String fieldName, Object value){
		return M(fieldName, value, ConnectorEnum.OR, OperatorEnum.GE, false);
	}
	
	/**
	 * 条件拼装
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value 其对应值
	 * @param connector 条件（and | or）
	 * @param op 操作符（like not like）
	 * @return
	 */
	private ComplexQuerySQL _like(String fieldName, Object value
			, ConnectorEnum connector, OperatorEnum op){
		if(fieldName == null)
			return this;
		
		String colName = SQLUtil.getColumnName(fieldName, table);
		
		value = (value == null ? "" :  value.toString()
				.replace("%", "\\%").replace("_", "\\_"));
	
		fieldItems.add(new ComplexQueryFieldItem(connector, op, colName, "%" + value + "%", null));
		
		return this;
	}
	
	/**
	 * 条件or like
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public ComplexQuerySQL orLike(String fieldName, Object value){
		return _like(fieldName, value, ConnectorEnum.OR, OperatorEnum.LIKE);
	}
	
	/**
	 * 条件and like
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public ComplexQuerySQL andLike(String fieldName, Object value){
		return _like(fieldName, value, ConnectorEnum.AND, OperatorEnum.LIKE);
	}
	
	/**
	 * 条件or not like
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public ComplexQuerySQL orNotLike(String fieldName, Object value){
		return _like(fieldName, value, ConnectorEnum.OR, OperatorEnum.NLIKE);
	}
	
	/**
	 * 条件and not like
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public ComplexQuerySQL andNotLike(String fieldName, Object value){
		return _like(fieldName, value, ConnectorEnum.AND, OperatorEnum.NLIKE);
	}
	
	/**
	 * in
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param values 对应的值
	 * @param op 操作符（in | not in）
	 * @param connector (or | and)
	 * @return
	 */
	private ComplexQuerySQL _in(String fieldName, OperatorEnum op, 
			ConnectorEnum connector,  Object... values){
		if(fieldName == null || values == null || values.length == 0)
			return this;
		String colName = SQLUtil.getColumnName(fieldName, table);
		
		_deepIn(colName, op, connector, values);
		
		return this;
	}

	/**
	 * 当in的条件长度大于998时，对其进行分组
	 * 
	 * @param fieldName
	 * @param op
	 * @param connector
	 * @param values
	 */
	private void _deepIn(String fieldName, OperatorEnum op, ConnectorEnum connector, Object... values) {
		if (values.length <= 998) {
			fieldItems.add(new ComplexQueryFieldItem(connector, op, fieldName, values, null));
		} else {
			group();
			List<Object[]> splitValues = XLPSplitUtils.split(values, 998);
			boolean first = true;
			for (Object[] splitValue : splitValues) {
				if (first) {
					fieldItems.add(new ComplexQueryFieldItem(connector, op, fieldName, splitValue, null));
				} else {
					fieldItems.add(new ComplexQueryFieldItem(ConnectorEnum.OR, op, fieldName, splitValue, null));
				}
				first = false;
			}
			endGroup();
		}
	}
	
	/**
	 * 条件and in
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public ComplexQuerySQL andIn(String fieldName, Object... values){
		return _in(fieldName, OperatorEnum.IN, ConnectorEnum.AND, values);
	}

	/**
	 * 条件or in
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public ComplexQuerySQL orIn(String fieldName, Object... values){
		return _in(fieldName, OperatorEnum.IN, ConnectorEnum.OR, values);
	}
	
	/**
	 * 条件or not in
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param values 对应的值
	 * @return SQL对象
	 */
	public ComplexQuerySQL orNotIn(String fieldName, Object... values){
		return _in(fieldName, OperatorEnum.NIN, ConnectorEnum.OR, values);
	}
	
	/**
	 * 条件and not in
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param values 对应的值
	 * @return SQL对象
	 */
	public ComplexQuerySQL andNotIn(String fieldName, Object... values){
		return _in(fieldName, OperatorEnum.NIN, ConnectorEnum.AND, values);
	}
	
	/**
	 * 条件or between
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value1 对应的值1
	 * @param value2 对应的值2
	 * @return SQL对象
	 */
	public ComplexQuerySQL orBetween(String fieldName, Object value1, Object value2){
		return between(fieldName, ConnectorEnum.OR, OperatorEnum.BETWEEN, value1, value2);
	}
	
	/**
	 * 条件and between
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value1 对应的值1
	 * @param value2 对应的值2
	 * @return SQL对象
	 */
	public ComplexQuerySQL andBetween(String fieldName, Object value1, Object value2){
		return between(fieldName, ConnectorEnum.AND, OperatorEnum.BETWEEN, value1, value2);
	}

	/**
	 * between
	 * 
	 * @param fieldName
	 * @param condition
	 * @param operator
	 * @param value1
	 * @param value2
	 * @return
	 */
	private ComplexQuerySQL between(String fieldName, ConnectorEnum condition,
			OperatorEnum operator, Object value1, Object value2) {
		if(fieldName == null)
			return this;
		String colName = SQLUtil.getColumnName(fieldName, table);
		
		fieldItems.add(new ComplexQueryFieldItem(condition, operator, colName, 
				new Object[]{value1, value2}, null));
		
		return this;
	}
	
	/**
	 * 条件or not between
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value1 对应的值1
	 * @param value2 对应的值2
	 * @return SQL对象
	 */
	public ComplexQuerySQL orNotBetween(String fieldName, Object value1, Object value2){
		return between(fieldName, ConnectorEnum.OR, OperatorEnum.NBETWEEN, value1, value2);
	}
	
	/**
	 * 条件and not between
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value1 对应的值1
	 * @param value2 对应的值2
	 * @return SQL对象
	 */
	public ComplexQuerySQL andNotBetween(String fieldName, Object value1, Object value2){
		return between(fieldName, ConnectorEnum.AND, OperatorEnum.NBETWEEN, value1, value2);
	}

	public List<ComplexQueryFieldItem> getFieldItems() {
		return fieldItems;
	}

	protected List<Field> getPrimaryFields() {
		return primaryFields;
	}

	protected List<Field> getForeignFields() {
		return foreignFields;
	}

	/**
	 * @return the limit
	 */
	public Limit getLimit() {
		return getTopComplexQuerySQL().limit;
	}

	/**
	 * @param limit the limit to set
	 */
	public ComplexQuerySQL limit(Limit limit) {
		ComplexQuerySQL topComplexQuerySQL = getTopComplexQuerySQL();
		Limit tempLimit = topComplexQuerySQL.getLimit();
		if (tempLimit != null && LOGGER.isWarnEnabled()) {
			LOGGER.warn("不能重复执行limit函数！");
		}
		topComplexQuerySQL.limit = limit;
		return this;
	}

	/**
	 * @return the distinct
	 */
	public boolean isDistinct() {
		return getTopComplexQuerySQL().distinct;
	}
	
	/**
	 * @param distinct the distinct to set
	 */
	public ComplexQuerySQL distinct(boolean distinct) {
		getTopComplexQuerySQL().distinct = distinct;
		return this;
	}
	
	/**
	 * order by
	 * 
	 * @param fieldNames bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param orderType 排序方式（asc | desc）
	 * @return
	 */
	private ComplexQuerySQL orderBy(String orderType, String... fieldNames){
		if(!XLPArrayUtil.isEmpty(fieldNames)){
			String colName;
			for (String name : fieldNames) {
				colName = SQLUtil.getColumnName(name, table, false); 
				getTopComplexQuerySQL().sortFields.put(colName, orderType);
			}
		}
		return this;
	}
	
	/**
	 * 排序升序
	 * 
	 * @param fieldNames bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @return SQL对象
	 */
	public ComplexQuerySQL asc(String... fieldNames){
		return orderBy(ASC, fieldNames);
	}
	
	/**
	 * 排序降序
	 * 
	 * @param fieldNames bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @return SQL对象
	 */
	public ComplexQuerySQL desc(String... fieldNames){
		return orderBy(DESC, fieldNames);
	}

	/**
	 * group by
	 * 
	 * @param fieldNames bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @return SQL对象
	 */
	public ComplexQuerySQL groupBy(String... fieldNames){
		if(!XLPArrayUtil.isEmpty(fieldNames)){
			String colName;
			for (String name : fieldNames) {
				colName = SQLUtil.getColumnName(name, table); 
				getTopComplexQuerySQL().groupFields.add(colName);
			}
		}
		return this;
	}
	
	/**
	 * max
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param alias 别名
	 * @return SQL对象
	 */
	public ComplexQuerySQL max(String fieldName, String alias){
		getTopComplexQuerySQL().sqlStatisticsType.add(new Max(table, fieldName, alias));
		return this;
	}
	
	/**
	 * max
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @return SQL对象
	 */
	public ComplexQuerySQL max(String fieldName){
		return max(fieldName, null);
	}
	
	/**
	 * min
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param alias 别名
	 * @return SQL对象
	 */
	public ComplexQuerySQL min(String fieldName, String alias){
		getTopComplexQuerySQL().sqlStatisticsType.add(new Min(table, fieldName, alias));
		return this;
	}
	
	/**
	 * min
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @return SQL对象
	 */
	public ComplexQuerySQL min(String fieldName){
		return min(fieldName, null);
	}
	
	/**
	 * sum
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param alias 别名
	 * @return SQL对象
	 */
	public ComplexQuerySQL sum(String fieldName, String alias){
		getTopComplexQuerySQL().sqlStatisticsType.add(new Sum(table, fieldName, alias));
		return this;
	}
	
	/**
	 * sum
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @return SQL对象
	 */
	public ComplexQuerySQL sum(String fieldName){
		return sum(fieldName, null);
	}
	
	/**
	 * avg
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param alias 别名
	 * @return SQL对象
	 */
	public ComplexQuerySQL avg(String fieldName, String alias){
		getTopComplexQuerySQL().sqlStatisticsType.add(new Avg(table, fieldName, alias));
		return this;
	}
	
	/**
	 * avg
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @return SQL对象
	 */
	public ComplexQuerySQL avg(String fieldName){
		return avg(fieldName, null);
	}
	
	/**
	 * count(fieldName)
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param alias 别名
	 * @return SQL对象
	 */
	public ComplexQuerySQL count(String fieldName, String alias){
		getTopComplexQuerySQL().sqlStatisticsType.add(new DistinctCount(table, fieldName, alias));
		return this;
	}
	
	/**
	 * count(fieldName)
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @return SQL对象
	 */
	public ComplexQuerySQL count(String fieldName){
		return count(fieldName, null);
	}
	
	/**
	 * count(*)
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param alias 别名
	 * @return SQL对象
	 */
	public ComplexQuerySQL countAll(String alias){
		DistinctCount distinctCount = new DistinctCount();
		distinctCount.setTable(table);
		distinctCount.setAlias(alias);
		getTopComplexQuerySQL().sqlStatisticsType.add(distinctCount);
		return this;
	}
	
	/**
	 * count(*)
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @return SQL对象
	 */
	public ComplexQuerySQL count(){
		return countAll(null);
	}
	
	/**
	 * count(distinctCount xx, yyy)
	 * 
	 * @param fieldNames bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param alias 别名
	 * @return SQL对象
	 */
	public ComplexQuerySQL distinctCount(String alias, String... fieldNames){
		DistinctCount distinctCount = new DistinctCount(table, fieldNames, alias);
		getTopComplexQuerySQL().sqlStatisticsType.add(distinctCount);
		return this;
	}
	
	/**
	 * count(distinctCount xx, yyy)
	 * 
	 * @param fieldNames bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @return SQL对象
	 */
	public ComplexQuerySQL distinctCount(String... fieldNames){
		return distinctCount(null, fieldNames);
	}
	
	/**
	 * 开始分组条件查询
	 * 
	 * @return
	 */
	public ComplexQuerySQL having(){
		getTopComplexQuerySQL().having = true;
		return this;
	}
	
	/**
	 * 结束分组条件查询
	 * 
	 * @return
	 */
	public ComplexQuerySQL endHaving(){
		getTopComplexQuerySQL().having = false;
		return this;
	}
	
	/**
	 * 设置要查询出的字段名称，多个字段可连续调用
	 * 
	 * @param fieldName 字段名称 也可以是数据库中表的列名，但最好是bean字段名, 也可以是固定值
	 * @param alias 字段对应的别名
	 * @return
	 */
	public ComplexQuerySQL properties(String fieldName, String alias){
		if (!XLPStringUtil.isEmpty(fieldName)) {
			String colName = SQLUtil.getColumnName(fieldName, table, false);
			Map<String, String> map = new HashMap<String, String>(2);
			map.put(QUERY_FIELD_ALIAS_KEY, alias);
			map.put(QUERY_FIELD_NAME_KEY, colName);
			getTopComplexQuerySQL().queryFields.add(map);
		}
		return this;
	}
	
	/**
	 * 设置要查询出的字段名称，多个字段可连续调用
	 * 
	 * @param fieldName 字段名称 也可以是数据库中表的列名，但最好是bean字段名, 也可以是固定值
	 * @return
	 */
	public ComplexQuerySQL properties(String fieldName){
		return properties(fieldName, null);
	}
	
	/**
	 * exists语句
	 * 
	 * @param sql
	 * @return
	 */
	private ComplexQuerySQL exists(SQL sql, ConnectorEnum connector,
			OperatorEnum operator){
		fieldItems.add(new ComplexQueryFieldItem(connector, operator, ValueType.SQL, sql, null));
		return this;
	}
	
	/**
	 * and exists
	 * 
	 * @param sql
	 * @throws NullPointerException 假如第参数为null，则抛出该异常
	 * @return
	 */
	public ComplexQuerySQL andExists(SQL sql){
		AssertUtils.isNotNull(sql, "sql parameter is null!");
		return exists(sql, ConnectorEnum.AND, OperatorEnum.EXISTS);
	}
	
	/**
	 * or exists
	 * 
	 * @param sql
	 * @throws NullPointerException 假如第参数为null，则抛出该异常
	 * @return
	 */
	public ComplexQuerySQL orExists(SQL sql){
		AssertUtils.isNotNull(sql, "sql parameter is null!");
		return exists(sql, ConnectorEnum.OR, OperatorEnum.EXISTS);
	}
	
	/**
	 * and not exists
	 * 
	 * @param sql
	 * @throws NullPointerException 假如第参数为null，则抛出该异常
	 * @return
	 */
	public ComplexQuerySQL andNotExists(SQL sql){
		AssertUtils.isNotNull(sql, "sql parameter is null!");
		return exists(sql, ConnectorEnum.AND, OperatorEnum.NOT_EXISTS);
	}
	
	/**
	 * or not exists语句
	 * 
	 * @param sql
	 * @throws NullPointerException 假如第参数为null，则抛出该异常
	 * @return
	 */
	public ComplexQuerySQL orNotExists(SQL sql){
		AssertUtils.isNotNull(sql, "sql parameter is null!");
		return exists(sql, ConnectorEnum.OR, OperatorEnum.NOT_EXISTS);
	}

	public List<ComplexQueryFieldItem> getHavingFieldItems() {
		return havingFieldItems;
	}

	/**
	 * @return 是否统计数据条数
	 */
	public boolean isExeCount() {
		return exeCount;
	}
	
	public Map<String, String> getSortFields() {
		return sortFields;
	}

	public Set<String> getGroupFields() {
		return groupFields;
	}

	/**
	 * 获取统计SQL对象
	 * 
	 * @return
	 */
	public SQL countSql(){
		ComplexQuerySQL topComplexQuerySQL = getTopComplexQuerySQL();
		SQL sql = new SQL() {
			private ComplexQuerySQL sql = topComplexQuerySQL;
			
			@Override
			public String getSql() {
				sql.exeCount = true;
				String sqlStr = sql.getSql();
				sql.exeCount = false;
				return sqlStr;
			}
			
			@Override
			public Object[] getParams() {
				sql.exeCount = true;
				Object[] params = sql.getParams();
				sql.exeCount = false;
				return params;
			}
			
			@Override
			public String getParamSql() {
				sql.exeCount = true;
				String sqlStr = sql.getParamSql();
				sql.exeCount = false;
				return sqlStr;
			}
			
			@Override
			public Class<?> getEntityClass() {
				return sql.getEntityClass();
			}
		};
		topComplexQuerySQL.exeCount = false;
		return sql;
	}
}
