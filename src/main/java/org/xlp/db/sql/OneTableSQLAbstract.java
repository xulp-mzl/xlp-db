package org.xlp.db.sql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xlp.assertion.AssertUtils;
import org.xlp.db.exception.EntityException;
import org.xlp.db.sql.item.ConnectorEnum;
import org.xlp.db.sql.item.FieldItem;
import org.xlp.db.sql.item.OperatorEnum;
import org.xlp.db.sql.table.Table;
import org.xlp.db.tableoption.key.CompoundPrimaryKey;
import org.xlp.db.utils.BeanUtil;
import org.xlp.utils.XLPSplitUtils;

/**
 * 含条件单表SQL信息抽象类
 * <p>注意：这个类的子类的构造方法中应该先调用父类的构造方法，在执行别的内容，否则可能会抛出异常
 * 
 * @author 徐龙平
 *         <p>
 *         2017-5-27
 *         </p>
 * @version 1.0
 */
public abstract class OneTableSQLAbstract<T> implements SQL{
	//日志记录
	protected final static Logger LOGGER = LoggerFactory.getLogger(OneTableSQLAbstract.class);
	//bean类型
	protected Class<T> beanClass;
	
	// 实体对应的table对象
	private Table<T> table;
	
	/**
	 * sql条件对象集合
	 */
	protected List<FieldItem> fieldItems = new ArrayList<FieldItem>();
	
	//主键信息
	protected CompoundPrimaryKey primaryKey;
	
	
	/**
	 * 用bean对象构建此对象
	 * @param bean
	 * @throws EntityException 
	 * @throws NullPointerException 假如参数为null，抛出该异常
	 */
	@SuppressWarnings("unchecked")
	protected OneTableSQLAbstract(T bean) throws EntityException {
		if(bean == null)
			throw new NullPointerException("bean对象必须不为空");
		this.beanClass = (Class<T>) bean.getClass();
		if(!BeanUtil.isEntity(beanClass)){
			LOGGER.error(beanClass.getName() + ": 没有XLPEntity实体注解");
			throw new EntityException("此对象不是实体");
		}
		this.primaryKey = new CompoundPrimaryKey(bean, false);
		table = new Table<T>(beanClass);
		init(bean);
	}
	
	protected OneTableSQLAbstract(){}
	
	/**
	 * 数据初始化
	 * 
	 * @throws EntityException
	 */
	protected abstract void init(T bean) throws EntityException;

	/**
	 * 用beanClass对象构建此对象
	 * @param beanClass
	 * @throws EntityException 
	 * @throws NullPointerException 假如参数为null，抛出该异常
	 */
	public OneTableSQLAbstract(Class<T> beanClass) throws EntityException {
		this(BeanUtil.newInstance(beanClass));
	}
	
	/**
	 * 得到表对象
	 * 
	 * @return
	 */
	public Table<T> getTable() {
		return table;
	}
	
	/**
	 * 设置表对象
	 * 
	 * @return
	 */
	protected void setTable(Table<T> table) {
		this.table = table;
	}
	
	/**
	 * 给表设置别名 
	 * 
	 * @param alias
	 */
	public void setTableAlias(String alias) {
		table.setAlias(alias);
	}
	
	/**
	 * 得到主键信息
	 * 
	 * @return
	 */
	public CompoundPrimaryKey getPrimaryKey() {
		return primaryKey;
	}
	
	@Override
	public Class<?> getEntityClass() {
		return beanClass;
	}
	
	/**
	 * 条件拼装
	 * 
	 * @param fieldName bean字段名，但最好是bean字段名
	 * @param value 其对应值
	 * @param connector 条件（and | or）
	 * @param op 操作符（=，>, < ...）
	 * @return
	 */
	private OneTableSQLAbstract<T>  M(String fieldName, Object value
			, ConnectorEnum connector, OperatorEnum op){
		if(fieldName == null)
			return this;
		
		String colName = BeanUtil.getFieldAlias(getTable(), fieldName);
	
		fieldItems.add(new FieldItem(connector, op, colName, value, table));
		
		return this;
	}
	
	/**
	 * null
	 * 
	 * @param fieldName bean字段名，但最好是bean字段名
	 * @param connector 条件（and | or）
	 * @param op 操作符（is null，is not null）
	 * @return
	 */
	private OneTableSQLAbstract<T> NULL(String fieldName
			, ConnectorEnum connector, OperatorEnum op){
		if(fieldName == null)
			return this;
		String colName = BeanUtil.getFieldAlias(getTable(), fieldName);
		
		fieldItems.add(new FieldItem(connector, op, colName, null, table));
		
		return this;
	}
	
	/**
	 * 条件or=
	 * 
	 * @param fieldName bean字段名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> orEq(String fieldName, Object value){
		if(value == null)
			return orIsNull(fieldName);
		
		return M(fieldName, value, ConnectorEnum.OR, OperatorEnum.EQ);
	}
	
	/**
	 * 条件or is null
	 * 
	 * @param fieldName bean字段名，但最好是bean字段名
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> orIsNull(String fieldName){
		return NULL(fieldName, ConnectorEnum.OR, OperatorEnum.IS_NULL);
	}
	
	/**
	 * 条件and=
	 * 
	 * @param fieldName bean字段名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> andEq(String fieldName, Object value){
		if(value == null)
			return andIsNull(fieldName);
		
		return M(fieldName, value, ConnectorEnum.AND, OperatorEnum.EQ);
	}
	
	/**
	 * 条件and is null
	 * 
	 * @param fieldName bean字段名，但最好是bean字段名
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> andIsNull(String fieldName){
		return NULL(fieldName, ConnectorEnum.AND, OperatorEnum.IS_NULL);
	}

	/**
	 * 条件or !=
	 * 
	 * @param fieldName bean字段名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> orNotEq(String fieldName, Object value){
		if(value == null)
			return orNotNull(fieldName);

		return M(fieldName, value, ConnectorEnum.OR, OperatorEnum.NEQ);
	}
	
	/**
	 * 条件or is not null
	 * 
	 * @param fieldName bean字段名，但最好是bean字段名
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> orNotNull(String fieldName){
		return NULL(fieldName, ConnectorEnum.OR, OperatorEnum.IS_NOT_NULL);
	}
	
	/**
	 * 条件and !=
	 * 
	 * @param fieldName bean字段名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> andNotEq(String fieldName, Object value){
		if(value == null)
			return andNotNull(fieldName);

		return M(fieldName, value, ConnectorEnum.AND, OperatorEnum.NEQ);
	}
	
	/**
	 * 条件and is not null
	 * 
	 * @param fieldName bean字段名，但最好是bean字段名
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> andNotNull(String fieldName){
		return NULL(fieldName, ConnectorEnum.AND, OperatorEnum.IS_NOT_NULL);
	}
	
	/**
	 * 条件and >
	 * 
	 * @param fieldName bean字段名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> andGt(String fieldName, Object value){
		return M(fieldName, value, ConnectorEnum.AND, OperatorEnum.GT);
	}
	
	/**
	 * 条件or >
	 * 
	 * @param fieldName bean字段名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> orGt(String fieldName, Object value){
		return M(fieldName, value, ConnectorEnum.OR, OperatorEnum.GT);
	}
	
	/**
	 * 条件and <
	 * 
	 * @param fieldName bean字段名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> andLt(String fieldName, Object value){
		return M(fieldName, value, ConnectorEnum.AND, OperatorEnum.LT);
	}
	
	/**
	 * 条件or <
	 * 
	 * @param fieldName bean字段名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> orLt(String fieldName, Object value){
		return M(fieldName, value, ConnectorEnum.OR, OperatorEnum.LT);
	}
	
	/**
	 * 条件and <=
	 * 
	 * @param fieldName bean字段名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> andLe(String fieldName, Object value){
		return M(fieldName, value, ConnectorEnum.AND, OperatorEnum.LE);
	}
	
	/**
	 * 条件or <=
	 * 
	 * @param fieldName bean字段名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> orLe(String fieldName, Object value){
		return M(fieldName, value, ConnectorEnum.OR, OperatorEnum.LE);
	}
	
	/**
	 * 条件and >=
	 * 
	 * @param fieldName bean字段名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> andGe(String fieldName, Object value){
		return M(fieldName, value, ConnectorEnum.AND, OperatorEnum.GE);
	}
	
	/**
	 * 条件or >=
	 * 
	 * @param fieldName bean字段名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> orGe(String fieldName, Object value){
		return M(fieldName, value, ConnectorEnum.OR, OperatorEnum.GE);
	}
	
	/**
	 * 条件拼装
	 * 
	 * @param fieldName bean字段名，但最好是bean字段名
	 * @param value 其对应值
	 * @param connector 条件（and | or）
	 * @param op 操作符（like not like）
	 * @return
	 */
	private OneTableSQLAbstract<T>  _like(String fieldName, Object value
			, ConnectorEnum connector, OperatorEnum op){
		if(fieldName == null)
			return this;
		
		String colName = BeanUtil.getFieldAlias(getTable(), fieldName);
		value = (value == null ? "" :  value.toString()
				.replace("%", "\\%").replace("_", "\\_"));
	
		fieldItems.add(new FieldItem(connector, op, colName, "%" + value + "%", table));
		
		return this;
	}
	
	/**
	 * 条件or like
	 * 
	 * @param fieldName bean字段名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> orLike(String fieldName, Object value){
		return _like(fieldName, value, ConnectorEnum.OR, OperatorEnum.LIKE);
	}
	
	/**
	 * 条件and like
	 * 
	 * @param fieldName bean字段名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> andLike(String fieldName, Object value){
		return _like(fieldName, value, ConnectorEnum.AND, OperatorEnum.LIKE);
	}
	
	/**
	 * 条件or not like
	 * 
	 * @param fieldName bean字段名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> orNotLike(String fieldName, Object value){
		return _like(fieldName, value, ConnectorEnum.OR, OperatorEnum.NLIKE);
	}
	
	/**
	 * 条件and not like
	 * 
	 * @param fieldName bean字段名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> andNotLike(String fieldName, Object value){
		return _like(fieldName, value, ConnectorEnum.AND, OperatorEnum.NLIKE);
	}
	
	/**
	 * in
	 * 
	 * @param fieldName bean字段名，但最好是bean字段名
	 * @param values 对应的值
	 * @param op 操作符（in | not in）
	 * @param connector (or | and)
	 * @return
	 */
	private OneTableSQLAbstract<T> _in(String fieldName, OperatorEnum op, 
			ConnectorEnum connector,  Object... values){
		if(fieldName == null || values == null || values.length == 0)
			return this;
		String colName = BeanUtil.getFieldAlias(getTable(), fieldName);
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
			fieldItems.add(new FieldItem(connector, op, fieldName, values, table));
		} else {
			group();
			List<Object[]> splitValues = XLPSplitUtils.split(values, 998);
			boolean first = true;
			for (Object[] splitValue : splitValues) {
				if (first) {
					fieldItems.add(new FieldItem(connector, op, fieldName, splitValue, table));
				} else {
					fieldItems.add(new FieldItem(ConnectorEnum.OR, op, fieldName, splitValue, table));
				}
				first = false;
			}
			endGroup();
		}
	}
	
	/**
	 * 条件and in
	 * 
	 * @param fieldName bean字段名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> andIn(String fieldName, Object... values){
		return _in(fieldName, OperatorEnum.IN, ConnectorEnum.AND, values);
	}

	/**
	 * 条件or in
	 * 
	 * @param fieldName bean字段名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> orIn(String fieldName, Object... values){
		return _in(fieldName, OperatorEnum.IN, ConnectorEnum.OR, values);
	}
	
	/**
	 * 条件or not in
	 * 
	 * @param fieldName bean字段名，但最好是bean字段名
	 * @param values 对应的值
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> orNotIn(String fieldName, Object... values){
		return _in(fieldName, OperatorEnum.NIN, ConnectorEnum.OR, values);
	}
	
	/**
	 * 条件and not in
	 * 
	 * @param fieldName bean字段名，但最好是bean字段名
	 * @param values 对应的值
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> andNotIn(String fieldName, Object... values){
		return _in(fieldName, OperatorEnum.NIN, ConnectorEnum.AND, values);
	}
	
	/**
	 * 条件or between
	 * 
	 * @param fieldName bean字段名，但最好是bean字段名
	 * @param value1 对应的值1
	 * @param value2 对应的值2
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> orBetween(String fieldName, Object value1, Object value2){
		return between(fieldName, ConnectorEnum.OR, OperatorEnum.BETWEEN, value1, value2);
	}
	
	/**
	 * 条件and between
	 * 
	 * @param fieldName bean字段名，但最好是bean字段名
	 * @param value1 对应的值1
	 * @param value2 对应的值2
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> andBetween(String fieldName, Object value1, Object value2){
		return between(fieldName, ConnectorEnum.AND, OperatorEnum.BETWEEN,value1, value2);
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
	private OneTableSQLAbstract<T> between(String fieldName, ConnectorEnum condition,
			OperatorEnum operator, Object value1, Object value2) {
		if(fieldName == null)
			return this;
		String colName = BeanUtil.getFieldAlias(getTable(), fieldName);
		
		fieldItems.add(new FieldItem(condition, operator, colName, 
				new Object[]{value1, value2}, table));
		
		return this;
	}
	
	/**
	 * 条件or not between
	 * 
	 * @param fieldName bean字段名，但最好是bean字段名
	 * @param value1 对应的值1
	 * @param value2 对应的值2
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> orNotBetween(String fieldName, Object value1, Object value2){
		return between(fieldName, ConnectorEnum.OR, OperatorEnum.NBETWEEN, value1, value2);
	}
	
	/**
	 * 条件and not between
	 * 
	 * @param fieldName bean字段名，但最好是bean字段名
	 * @param value1 对应的值1
	 * @param value2 对应的值2
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> andNotBetween(String fieldName, Object value1, Object value2){
		return between(fieldName, ConnectorEnum.AND, OperatorEnum.NBETWEEN, value1, value2);
	}
	
	/**
	 * 开始分组条件查询
	 * 
	 * @return
	 */
	public OneTableSQLAbstract<T> group(){
		fieldItems.add(new FieldItem(OperatorEnum.LEFT_BRACKET, table));
		return this;
	}
	
	/**
	 * 结束分组条件查询
	 * 
	 * @return
	 */
	public OneTableSQLAbstract<T> endGroup(){
		fieldItems.add(new FieldItem(OperatorEnum.RIGHT_BRACKET, table));
		return this;
	}
	
	/**
	 * exists语句
	 * 
	 * @param query
	 * @return
	 */
	private OneTableSQLAbstract<T> exists(Query query, ConnectorEnum connector,
			OperatorEnum operator){
		fieldItems.add(new FieldItem(connector, operator, null, query, query.getTable()));
		return this;
	}
	
	/**
	 * and exists
	 * 
	 * @param query
	 * @throws NullPointerException 假如第参数为null，则抛出该异常
	 * @return
	 */
	public OneTableSQLAbstract<T> andExists(Query query){
		AssertUtils.isNotNull(query, "query parameter is null!");
		return exists(query, ConnectorEnum.AND, OperatorEnum.EXISTS);
	}
	
	/**
	 * or exists
	 * 
	 * @param query
	 * @throws NullPointerException 假如第参数为null，则抛出该异常
	 * @return
	 */
	public OneTableSQLAbstract<T> orExists(Query query){
		AssertUtils.isNotNull(query, "query parameter is null!");
		return exists(query, ConnectorEnum.OR, OperatorEnum.EXISTS);
	}
	
	/**
	 * and not exists
	 * 
	 * @param query
	 * @throws NullPointerException 假如第参数为null，则抛出该异常
	 * @return
	 */
	public OneTableSQLAbstract<T> andNotExists(Query query){
		AssertUtils.isNotNull(query, "query parameter is null!");
		return exists(query, ConnectorEnum.AND, OperatorEnum.NOT_EXISTS);
	}
	
	/**
	 * or not exists语句
	 * 
	 * @param query
	 * @throws NullPointerException 假如第参数为null，则抛出该异常
	 * @return
	 */
	public OneTableSQLAbstract<T> orNotExists(Query query){
		AssertUtils.isNotNull(query, "query parameter is null!");
		return exists(query, ConnectorEnum.OR, OperatorEnum.NOT_EXISTS);
	}
	
	/**
	 * 得到预处理值数组
	 * 
	 * @return
	 */
	public Object[] getValues(){
		List<Object> valueList = new ArrayList<>();
		for (FieldItem fieldItem : fieldItems) {
			switch (fieldItem.getOperator()) {
			case EQ: 
			case NEQ:
			case LIKE:
			case NLIKE:
			case GT:
			case GE:
			case LT:
			case LE:
				 valueList.add(fieldItem.getValue());
				break;

			case IN:
			case NIN:
			case BETWEEN:
			case NBETWEEN:
				 valueList.addAll(Arrays.asList(fieldItem.getValues())); 
				break;
			case NOT_EXISTS:
			case EXISTS:
				valueList.addAll(Arrays.asList(((SQL)fieldItem.getValue()).getParams()));
				break;
			default:
				break;
			}
		}
		return valueList.toArray();
	}
	
	/**
	 * 形成条件部分带预处理参数的sql语句
	 * 
	 * @return
	 */
	protected String formatterConditionSql(){
		String tableAlias = SQLUtil.getTableAlias(table);
		
		StringBuilder sb = new StringBuilder();
		//标记是否是第一个条件
		boolean firstCondition = true;
		//存储左括号
		Stack<String> stack = new Stack<String>();
		for (FieldItem fieldItem : fieldItems) {
			switch (fieldItem.getOperator()) {
			case LEFT_BRACKET:
				 stack.push(fieldItem.getOperator().getOperator());
				break;
			case RIGHT_BRACKET:
				sb.append(fieldItem.getOperator().getOperator());
				firstCondition = false;
				break;
				
			case EQ: 
			case NEQ:
			case LIKE:
			case NLIKE:
			case GT:
			case GE:
			case LT:
			case LE:
				fillPartSql(firstCondition, sb, fieldItem, tableAlias, stack);
				sb.append(" ?");
				firstCondition = false;
				break;

			case IS_NULL:
			case IS_NOT_NULL:
			case BETWEEN:
			case NBETWEEN:
				fillPartSql(firstCondition, sb, fieldItem, tableAlias, stack);
				firstCondition = false;
				break;
				
			case IN:
			case NIN:
				fillPartSql(firstCondition, sb, fieldItem, tableAlias, stack);
				sb.append(SQLUtil.formatInSql(fieldItem.getValues().length)); 
				firstCondition = false;
				break;
			case NOT_EXISTS:
			case EXISTS:
				if (!firstCondition) {
					sb.append(" ").append(fieldItem.getConnector().getConnector()).append(" ");
				}
				sb.append(fieldItem.getOperator().getOperator()).append(SQL.LEFT_BRACKET)
					.append(((SQL)fieldItem.getValue()).getParamSql()).append(SQL.RIGHT_BRACKET);
				firstCondition = false;
				break;
			default:
				break;
			}
		}
		return sb.toString();
	}
	
	private void fillPartSql(boolean firstCondition, StringBuilder sb, FieldItem fieldItem,
			String tableAlias, Stack<String> stack){
		if (!firstCondition) {
			sb.append(" ").append(fieldItem.getConnector().getConnector()).append(" ");
		}
		while (!stack.isEmpty()) {
			sb.append(stack.pop());
		}
		sb.append(tableAlias).append(fieldItem.getFieldName()).append(" ")
			.append(fieldItem.getOperator().getOperator());
	}
	
	/**
	 * 形成条件部分不带预处理参数的sql语句
	 * 
	 * @return
	 */
	protected String formatterConditionSourceSql(){
		StringBuilder sb = new StringBuilder(formatterConditionSql());
		return SQLUtil.fillWithParams(sb, getValues());
	}
	
	@Override
	public String toString() {
		return getSql();
	}
	
	@Override
	public Object[] getParams() {
		return getValues();
	}
}
