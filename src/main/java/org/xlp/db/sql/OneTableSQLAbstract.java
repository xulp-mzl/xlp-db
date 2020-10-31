package org.xlp.db.sql;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.xlp.db.exception.EntityException;
import org.xlp.db.tableoption.key.CompoundPrimaryKey;
import org.xlp.db.utils.BeanUtil;

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
	protected final static Logger LOGGER = Logger.getLogger(OneTableSQLAbstract.class);
	//bean类型
	protected Class<T> beanClass;
	//表名
	private String tableName;
	//条件之后的预处理值集合
	protected List<Object> valueList = new ArrayList<Object>();
	//条件SQL
	protected StringBuilder partSql = new StringBuilder(WHERE); 
	//主键信息
	protected CompoundPrimaryKey primaryKey;
	protected static final String WHERE = " where 1=1 ";
	
	
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
		setTableName();
		init(bean);
	}
	
	protected OneTableSQLAbstract(){}
	
	/**
	 * 初始化表名
	 */
	protected void setTableName(){
		tableName = SQLUtil.getTableName(beanClass);
	}
	
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
	 * 得到表名
	 * 
	 * @return
	 */
	public String getTableName() {
		return tableName;
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
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value 其对应值
	 * @param condition 条件（and | or）
	 * @param op 操作符（=，>, < ...）
	 * @return
	 */
	private OneTableSQLAbstract<T>  M(String fieldName, Object value
			, String condition, String op){
		if(fieldName == null)
			return this;
		
		String colName = BeanUtil.getFieldAlias(beanClass, fieldName);
		colName = (colName == null ? fieldName : colName);
	
		partSql.append(" ").append(condition).append(" ").append(tableName).append(".")
			.append(colName).append(op).append(INTERROGATION);
		valueList.add(value);
		return this;
	}
	
	/**
	 * null
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param condition 条件（and | or）
	 * @param op 操作符（is null，is not null）
	 * @return
	 */
	private OneTableSQLAbstract<T> NULL(String fieldName
			, String condition, String op){
		if(fieldName == null)
			return this;
		String colName = BeanUtil.getFieldAlias(beanClass, fieldName);
		colName = (colName == null ? fieldName : colName);
		
		partSql.append(" ").append(condition).append(" ").append(tableName).append(".")
			.append(colName).append(" ").append(op);
		return this;
	}
	
	/**
	 * 条件or=
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> orEq(String fieldName, Object value){
		if(value == null)
			return orIsNull(fieldName);
		
		return M(fieldName, value, OR, EQ);
	}
	
	/**
	 * 条件or is null
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> orIsNull(String fieldName){
		return NULL(fieldName, OR, IS_NULL);
	}
	
	/**
	 * 条件and=
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> andEq(String fieldName, Object value){
		if(value == null)
			return andIsNull(fieldName);
		
		return M(fieldName, value, AND, EQ);
	}
	
	/**
	 * 条件and is null
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> andIsNull(String fieldName){
		return NULL(fieldName, AND, IS_NULL);
	}

	/**
	 * 条件or !=
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> orNotEq(String fieldName, Object value){
		if(value == null)
			return orNotNull(fieldName);

		return M(fieldName, value, OR, NOT_EQ);
	}
	
	/**
	 * 条件or is not null
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> orNotNull(String fieldName){
		return NULL(fieldName, OR, IS_NOT_NULL);
	}
	
	/**
	 * 条件and !=
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> andNotEq(String fieldName, Object value){
		if(value == null)
			return andNotNull(fieldName);

		return M(fieldName, value, AND, NOT_EQ);
	}
	
	/**
	 * 条件and is not null
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> andNotNull(String fieldName){
		return NULL(fieldName, AND, IS_NOT_NULL);
	}
	
	/**
	 * 条件and >
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> andGt(String fieldName, Object value){
		return M(fieldName, value, AND, GT);
	}
	
	/**
	 * 条件or >
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> orGt(String fieldName, Object value){
		return M(fieldName, value, OR, GT);
	}
	
	/**
	 * 条件and <
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> andLt(String fieldName, Object value){
		return M(fieldName, value, AND, LT);
	}
	
	/**
	 * 条件or <
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> orLt(String fieldName, Object value){
		return M(fieldName, value, OR, LT);
	}
	
	/**
	 * 条件and <=
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> andLe(String fieldName, Object value){
		return M(fieldName, value, AND, LE);
	}
	
	/**
	 * 条件or <=
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> orLe(String fieldName, Object value){
		return M(fieldName, value, OR, LE);
	}
	
	/**
	 * 条件and >=
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> andGe(String fieldName, Object value){
		return M(fieldName, value, AND, GE);
	}
	
	/**
	 * 条件or >=
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> orGe(String fieldName, Object value){
		return M(fieldName, value, OR, GE);
	}
	
	/**
	 * 条件or like
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> orLike(String fieldName, Object value){
		if(fieldName == null)
			return this;
		
		String colName = BeanUtil.getFieldAlias(beanClass, fieldName);
		colName = (colName == null ? fieldName : colName);
		
		value = (value == null ? "" :  value.toString()
				.replace("%", "\\%").replace("_", "\\_"));
		partSql.append("or ").append(tableName).append(".")
			.append(colName).append(" like ? ");
		valueList.add("%" + value + "%");
		return this;
	}
	
	/**
	 * 条件and like
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> andLike(String fieldName, Object value){
		if(fieldName == null)
			return this;
		String colName = BeanUtil.getFieldAlias(beanClass, fieldName);
		colName = (colName == null ? fieldName : colName);
		
		value = (value == null ? "" :  value.toString()
				.replace("%", "\\%").replace("_", "\\_"));
		partSql.append("and ").append(tableName).append(".")
			.append(colName).append(" like ? ");
		valueList.add("%" + value + "%");
		return this;
	}
	
	/**
	 * in
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param values 对应的值
	 * @param op 操作符（in | not in）
	 * @param condition (or | and)
	 * @return
	 */
	private OneTableSQLAbstract<T> _in(String fieldName, String op
			, String condition,  Object... values){
		if(fieldName == null || values == null || values.length == 0)
			return this;
		String colName = BeanUtil.getFieldAlias(beanClass, fieldName);
		colName = (colName == null ? fieldName : colName);
		
		partSql.append(" ").append(condition).append(" ")
			.append(tableName).append(".").append(colName)
			.append(" ").append(op);
		in(values); 
		return this;
	}
	
	/**
	 * 条件and in
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> andIn(String fieldName, Object... values){
		return _in(fieldName, IN, AND, values);
	}

	/**
	 * 条件or in
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> orIn(String fieldName, Object... values){
		return _in(fieldName, IN, OR, values);
	}
	
	/**
	 * 条件or not in
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param values 对应的值
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> orNotIn(String fieldName, Object... values){
		return _in(fieldName, NOT_IN, OR, values);
	}
	
	/**
	 * 条件and not in
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param values 对应的值
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> andNotIn(String fieldName, Object... values){
		return _in(fieldName, NOT_IN, AND, values);
	}
	
	/**
	 * 形成in部分SQL语句
	 * 
	 * @param values
	 */
	private void in(Object... values) {
		int len = values.length;
		partSql.append(LEFT_BRACKET);
		for (int i = 0; i < len; i++) {
			if(i != 0)
				partSql.append(COMMA);
			partSql.append(INTERROGATION);
			valueList.add(values[i]);
		}
		partSql.append(RIGHT_BRACKET).append(" ");
	}
	
	/**
	 * 条件or between
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value1 对应的值1
	 * @param value2 对应的值2
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> orBetween(String fieldName, Object value1, Object value2){
		return between(fieldName, OR, value1, value2);
	}
	
	/**
	 * 条件and between
	 * 
	 * @param fieldName bean字段名，也可以是数据库中表的列名，但最好是bean字段名
	 * @param value1 对应的值1
	 * @param value2 对应的值2
	 * @return SQL对象
	 */
	public OneTableSQLAbstract<T> andBetween(String fieldName, Object value1, Object value2){
		return between(fieldName, AND, value1, value2);
	}

	/**
	 * between
	 * 
	 * @param fieldName
	 * @param condition
	 * @param value1
	 * @param value2
	 * @return
	 */
	private OneTableSQLAbstract<T> between(String fieldName,String condition,
			Object value1, Object value2) {
		if(fieldName == null)
			return this;
		String colName = BeanUtil.getFieldAlias(beanClass, fieldName);
		colName = (colName == null ? fieldName : colName);
		
		partSql.append(condition).append(" ").append(tableName).append(".")
			.append(colName).append(" between ? and ?");
		valueList.add(value1);
		valueList.add(value2);
		return this;
	}
	
	/**
	 * 得到预处理值数组
	 * 
	 * @return
	 */
	public Object[] getValues(){
		return valueList.toArray();
	}
	
	/**
	 * 把条件好的partSql装换成不带预处理参数的sql语句
	 * 
	 * @return
	 */
	protected String partSqlToString(){
		StringBuilder sb = new StringBuilder().append(partSql);
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
