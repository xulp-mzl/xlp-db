package org.xlp.db.sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xlp.db.exception.EntityException;
import org.xlp.db.sql.limit.Limit;
import org.xlp.db.sql.table.Table;
import org.xlp.db.tableoption.key.CompoundPrimaryKey;
import org.xlp.db.tableoption.xlpenum.DBType;
import org.xlp.db.utils.BeanUtil;
import org.xlp.utils.XLPStringUtil;
import org.xlp.utils.collection.XLPCollectionUtil;


/**
 * 含条件多表SQL信息类
 * 
 * @author 徐龙平
 *         <p>
 *         2017-6-9
 *         </p>
 * @version 1.0
 * 
 * <p>主要功能是用对象来形成多表多条件查询SQL语句，类中的的方法调用顺序要按按照正常SQL语句顺序调用，否则可能不能达到预期效果
 * <p>此类不能处理复杂的SQL语句，所以对于复杂的查询，请直接用原始SQL
 * <p>对于多表中存在重复表时，如要条件中要区分是哪个表的，条件字段格式为 （类名(首字母大小写都可)+ "." +字段名 +"." +类名对应的别名(alias的值)）
 * <p>其他情况下，条件字段格式为 （类名(首字母大小写都可)+ "." +字段名 ）就可以了。
 */
public class ManyTablesQuerySQL implements SQL{
	//from 后的表集合
	private Map<Class<?>, Table<?>> tableMap = new HashMap<Class<?>, Table<?>>();
	//join 后的表集合
	private Map<Class<?>, Table<?>> joinTableMap = new HashMap<Class<?>, Table<?>>();
	//另一些条件值集合
	private List<Object> otherValueList = new ArrayList<Object>();
	//部分SQL
	private StringBuilder otherPartSql = new StringBuilder();
	//日志记录
	protected final static Logger LOGGER = LoggerFactory.getLogger(ManyTablesQuerySQL.class);
	//bean类型
	protected Class<?> beanClass;
	//标记是否去除重复的数据
	private boolean distinct;
	//limit对象
	private Limit limit;
	//主键信息对象
	private CompoundPrimaryKey primaryKey;
	
	private final static String _0 = "_0";
	
	public ManyTablesQuerySQL(){
		
	}
	
	/**
	 * 以基础实体类型创建该对象
	 * 
	 * @param beanClass
	 * @throws EntityException
	 */
	public ManyTablesQuerySQL(Class<?> beanClass) throws EntityException {
		init(beanClass);
	}

	/**
	 * 以基础实体对象创建该对象
	 * 
	 * @param beanClass
	 * @throws EntityException
	 */
	public <T> ManyTablesQuerySQL(T bean) throws EntityException {
		this(bean.getClass());
		this.primaryKey = new CompoundPrimaryKey(bean, false);
	}
	
	/**
	 * 初始化
	 * 
	 * @throws EntityException
	 */
	@SuppressWarnings("all")
	protected void init(Class<?> beanClass) throws EntityException {
		tableMap.put(beanClass, new Table(beanClass));
		this.beanClass = beanClass;
	}

	/**
	 * 添加另一些表
	 * 
	 * @param other 多表中另一个表对应的实体类型
	 * @param alias 其对应的别名（可为空）
	 * @return
	 * @throws EntityException 
	 */
	@SuppressWarnings("all")
	public ManyTablesQuerySQL from(Class<?> other, String alias) 
			throws EntityException{
		Table<?> table = new Table(other);
		table.setAlias(alias);
		tableMap.put(other, table);
		return this;
	}
	
	/**
	 * 给实体设置别名 
	 * 
	 * @param alias
	 */
	public ManyTablesQuerySQL setAlias(String alias) {
		Table<?> table = tableMap.get(beanClass);
		if (table != null) {
			table.setAlias(alias);
		}
		return this;
	}
	
	/**
	 * 得到带预处理参数的SQL语句
	 * 
	 * @return 假如不能得到理论上正确的SQL语句，则返回null，否则返回SQL语句
	 */
	@Override
	public String getParamSql() {
		String pre = tableFields();
		String sql = pre == null ? null : pre + whereSql(false);
		LOGGER.debug("形成的多表多條件查询SQL语句是：" + sql);
		return sql;
	}

	/**
	 * 得到不带预处理参数的SQL语句 
	 * 
	 * @return 假如不能得到理论上正确的SQL语句，则返回null，否则返回SQL语句
	 */
	@Override
	public String getSql() {
		String pre = tableFields();
		String sql = pre == null ? null : pre + whereSql(true);
		LOGGER.debug("形成的多表多條件查询SQL语句是：" + sql);
		return sql;
	}

	/**
	 * 查询字段组成的字符串
	 * 
	 * @return
	 */
	private String tableFields(){
		StringBuilder sb = new StringBuilder("select ");//存储查询字段
		StringBuilder allTableNames = new StringBuilder();//存储查询表名
		int size = tableMap.size();
		if(size == 0)
			return null;
		if(distinct)
			sb.append("distinct ");
		
		Table<?> table = null;
		String[] allCol = null;
		String alias = null;
		
		//标记是否是开头
		boolean flag = true;
		for (Entry<Class<?>, Table<?>> entry : tableMap.entrySet()) {
			table = entry.getValue();
			allCol = table.getAllColumnNames();
			int count = allCol.length;
			if(count == 0){
				LOGGER.warn(entry.getKey() + ": 该实体类的字段没有与" + table.getTableName() + 
						"该表中的自对段对应");
				continue;
			}
			
			if(!flag)
				allTableNames.append(COMMA);
			alias = table.getAlias();
			alias = alias == null ? table.getTableName() + _0 : alias;
			
			for (int i = 0; i < count; i++) {
				if(!flag)
					sb.append(COMMA);
				
				sb.append(alias).append(".").append(allCol[i]);
				flag = false;;
			}
			
			allTableNames.append(table.getTableName())
					.append(" ").append(alias);
		}
		
		if(allTableNames.length() == 0)
			return null;
		
		//处理join后的表
		for (Entry<Class<?>, Table<?>> entry : joinTableMap.entrySet()) {
			table = entry.getValue();
			allCol = table.getAllColumnNames();
			int count = allCol.length;
			if(count == 0){
				LOGGER.warn(entry.getKey() + ": 该实体类的字段没有与" + table.getTableName() + 
						"该表中的自对段对应");
				continue;
			}
			
			alias = table.getAlias();
			alias = alias == null ? table.getTableName() + _0 : alias;
			
			for (int i = 0; i < count; i++) {
				if(!flag)
					sb.append(COMMA);
				
				sb.append(alias).append(".").append(allCol[i]);
				//flag = false;;
			}
		}
		
		sb.append(" from ").append(allTableNames);
		return sb.toString();
	}
	
	/**
	 * 形成条件后的SQL
	 * 
	 * @return
	 */
	private String whereSql(boolean fromGetSql){
		String sql = otherPartSql.toString().toLowerCase();
		if(!XLPStringUtil.containSubString(sql, "[\\s]+join[\\s]+") 
				&& !XLPStringUtil.containSubString(sql, "[\\s]+where[\\s]+")) 
			sql = " where 1=1" + sql;
		if (!XLPStringUtil.containSubString(sql, "[\\s]+where[\\s]+")) 
			sql += " where 1=1";
		
		StringBuilder temp = new StringBuilder(sql);
		
		Table<?> table = tableMap.get(beanClass);
		//p拼接主键条件
		if (primaryKey != null && table != null) {
			String[] colNames = primaryKey.getNames();
			String alias = table.getAlias();
			alias = alias == null ? table.getTableName() + _0 : alias;
			
			for (int i = 0; i < colNames.length; i++) {
				if (i != 0) 
					temp.append(COMMA);
				temp.append(" and ").append(alias).append(".")
					.append(colNames[i]).append("=?");
			}
		}
		
		//拼接limit部分
		if(limit != null && limit.getDbType() == DBType.MYSQL_DB)
			temp.append(" limit ?,?");
		
		sql = temp.toString();
		if (!fromGetSql) {
			return sql;
		}else {
			Object[] values = getParams();
			return SQLUtil.fillWithParams(temp, values);
		}
	}
	
	@Override
	public Object[] getParams() {
		if (primaryKey != null) {
			XLPCollectionUtil.fillByArray(otherValueList, primaryKey.getValues());
		}
		
		if (limit != null && limit.getDbType() == DBType.MYSQL_DB) {
			otherValueList.add(limit.getStartPos());
			otherValueList.add(limit.getResultCount());
		}
		return otherValueList.toArray();
	}
	
	/**
	 * 添加左括号
	 * 
	 * @return
	 */
	public ManyTablesQuerySQL leftBracket(){
		otherPartSql.append(LEFT_BRACKET);
		return this;
	}
	
	/**
	 * 添加右括号
	 * 
	 * @return
	 */
	public ManyTablesQuerySQL rightBracket(){
		otherPartSql.append(RIGHT_BRACKET);
		return this;
	}
	
	/**
	 * 添加where条件
	 * 
	 * @return
	 */
	public ManyTablesQuerySQL where(){
		otherPartSql.append(" where 1=1");
		return this;
	}
	
	/**
	 * 预处理
	 * 
	 * @param string
	 * @return
	 */
	private String preDeal(String string){
		if (string == null)
			return null;
		String[] clNa_fdNa_alias = string.split("\\.");
		int len = clNa_fdNa_alias.length;
		if (len != 2 && len != 3) 
			return null;
		//from table
		String formCompletedColN = formCompletedColN(clNa_fdNa_alias, len, tableMap);
		return formCompletedColN == null ? formCompletedColN(clNa_fdNa_alias, len, joinTableMap)
				: formCompletedColN;
	}

	/**
	 * 得到完整的表列名，格式为(表别名+"."+列名)
	 * 
	 * @param clNa_fdNa_alias
	 * @param len
	 * @param map
	 * @return 假如没有形成，返回null
	 */
	private String formCompletedColN(String[] clNa_fdNa_alias, int len, Map<Class<?>, Table<?>> map) {
		for (Entry<Class<?>, Table<?>> entry: map.entrySet()) {
			if(entry.getKey().getSimpleName().equalsIgnoreCase(clNa_fdNa_alias[0])){
				//得到表对应的字段名
				String alias = BeanUtil.getFieldAlias(entry.getKey(), clNa_fdNa_alias[1]);
				if(alias == null)
					return null;
				String tableAlias = entry.getValue().getAlias();
				if (len == 3 ) {
					if(clNa_fdNa_alias[2].equals(tableAlias))
						return clNa_fdNa_alias[2] + "." + alias;
				}else {
					tableAlias = tableAlias == null ? entry.getValue().getTableName() + _0 
							: tableAlias;
					return tableAlias + "." + alias;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * 条件拼装
	 * 
	 * @param fieldName bean字段名，一定是bean字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @param otherFieldName 另一个标的字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @param condition 条件（and | or）
	 * @param op 操作符（=，>, < ...）
	 * @return
	 */
	private ManyTablesQuerySQL M(String fieldName, String otherFieldName
			, String condition, String op){
		String alias1 = preDeal(fieldName);
		String alias2 = preDeal(otherFieldName);
		if (alias1 == null || alias2 == null) 
			return this;
		
		otherPartSql.append(" ").append(condition).append(" ")
				.append(alias1).append(op).append(alias2);
		return this;
	}
	
	/**
	 * 条件拼装
	 * 
	 * @param fieldName bean字段名，一定是bean字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @param value 其对应值
	 * @param condition 条件（and | or）
	 * @param op 操作符（=，>, < ...）
	 * @return
	 */
	private ManyTablesQuerySQL MV(String fieldName, Object value
			, String condition, String op){
		String alias1 = preDeal(fieldName);
		if (alias1 == null) 
			return this;
		
		otherPartSql.append(" ").append(condition).append(" ")
				.append(alias1).append(op).append(INTERROGATION);
		otherValueList.add(value);
		return this;
	}
	
	/**
	 * null
	 * 
	 * @param fieldName bean字段名，一定是bean字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @param condition 条件（and | or）
	 * @param op 操作符（is null，is not null）
	 * @return
	 */
	private ManyTablesQuerySQL NULL(String fieldName
			, String condition, String op){
		String alias1 = preDeal(fieldName);
		if (alias1 == null) 
			return this;
		
		otherPartSql.append(" ").append(condition).append(" ")
				.append(alias1).append(" ").append(op);
		return this;
	}
	
	/**
	 * 条件or=
	 * 
	 * @param fieldName bean字段名，一定是bean字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @param otherFieldName 另一个标的字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @return SQL对象
	 */
	public ManyTablesQuerySQL orEqM(String fieldName, String otherFieldName){
		return M(fieldName, otherFieldName, OR, EQ);
	}
	
	/**
	 * 条件or=
	 * 
	 * @param fieldName bean字段名，一定是bean字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @param value 其对应值
	 * @return SQL对象
	 */
	public ManyTablesQuerySQL orEq(String fieldName, Object value){
		if (value == null) 
			return orIsNullM(fieldName);
		
		return MV(fieldName, value, OR, EQ);
	}
	
	/**
	 * 条件or is null
	 * 
	 * @param fieldName bean字段名，一定是bean字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @return SQL对象
	 */
	public ManyTablesQuerySQL orIsNullM(String fieldName){
		return NULL(fieldName, OR, IS_NULL);
	}
	
	/**
	 * 条件and=
	 * 
	 * @param fieldName bean字段名，一定是bean字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @param otherFieldName 另一个标的字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @return SQL对象
	 */
	public ManyTablesQuerySQL andEqM(String fieldName, String otherFieldName){
		return M(fieldName, otherFieldName, AND, EQ);
	}
	
	/**
	 * 条件and=
	 * 
	 * @param fieldName bean字段名，一定是bean字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @param value 其对应值
	 * @return SQL对象
	 */
	public ManyTablesQuerySQL andEq(String fieldName, Object value){
		if (value == null) 
			return andIsNullM(fieldName);
		return MV(fieldName, value, AND, EQ);
	}
	
	/**
	 * 条件and is null
	 * 
	 * @param fieldName bean字段名，一定是bean字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @return SQL对象
	 */
	public ManyTablesQuerySQL andIsNullM(String fieldName){
		return NULL(fieldName, AND, IS_NULL);
	}

	/**
	 * 条件or !=
	 * 
	 * @param fieldName bean字段名，一定是bean字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @param otherFieldName 另一个标的字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @return SQL对象
	 */
	public ManyTablesQuerySQL orNotEqM(String fieldName, String otherFieldName){
		return M(fieldName, otherFieldName, OR, NOT_EQ);
	}
	
	/**
	 * 条件or !=
	 * 
	 * @param fieldName bean字段名，一定是bean字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public ManyTablesQuerySQL orNotEq(String fieldName, Object value){
		if(value == null)
			return orNotNullM(fieldName);
		
		return MV(fieldName, value, OR, NOT_EQ);
	}
	
	/**
	 * 条件or is not null
	 * 
	 * @param fieldName bean字段名，一定是bean字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @return SQL对象
	 */
	public ManyTablesQuerySQL orNotNullM(String fieldName){
		return NULL(fieldName, OR, IS_NOT_NULL);
	}
	
	/**
	 * 条件and !=
	 * 
	 * @param fieldName bean字段名，一定是bean字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @param otherFieldName 另一个标的字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @return SQL对象
	 */
	public ManyTablesQuerySQL andNotEqM(String fieldName, String otherFieldName){
		return M(fieldName, otherFieldName, AND, NOT_EQ);
	}
	
	/**
	 * 条件and !=
	 * 
	 * @param fieldName bean字段名，一定是bean字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public ManyTablesQuerySQL andNotEq(String fieldName, Object value){
		if(value == null)
			return andNotNullM(fieldName);
		
		return MV(fieldName, value, AND, NOT_EQ);
	}
	
	/**
	 * 条件and is not null
	 * 
	 * @param fieldName bean字段名, 一定是bean字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @return SQL对象
	 */
	public ManyTablesQuerySQL andNotNullM(String fieldName){
		return NULL(fieldName, AND, IS_NOT_NULL);
	}
	
	/**
	 * 条件and >
	 * 
	 * @param fieldName bean字段名，一定是bean字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @param otherFieldName 另一个标的字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @return SQL对象
	 */
	public ManyTablesQuerySQL andGtM(String fieldName, String otherFieldName){
		return M(fieldName, otherFieldName, AND, GT);
	}
	
	/**
	 * 条件and >
	 * 
	 * @param fieldName bean字段名，一定是bean字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public ManyTablesQuerySQL andGt(String fieldName, Object value){
		return MV(fieldName, value, AND, GT);
	}
	
	/**
	 * 条件or >
	 * 
	 * @param fieldName bean字段名，一定是bean字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @param otherFieldName 另一个标的字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @return SQL对象
	 */
	public ManyTablesQuerySQL orGtM(String fieldName, String otherFieldName){
		return M(fieldName, otherFieldName, OR, GT);
	}
	
	/**
	 * 条件or >
	 * 
	 * @param fieldName bean字段名，一定是bean字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public ManyTablesQuerySQL orGt(String fieldName, Object value){
		return MV(fieldName, value, OR, GT);
	}
	
	/**
	 * 条件and <
	 * 
	 * @param fieldName bean字段名，一定是bean字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @param otherFieldName 另一个标的字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @return SQL对象
	 */
	public ManyTablesQuerySQL andLtM(String fieldName, String otherFieldName){
		return M(fieldName, otherFieldName, AND, LT);
	}
	
	/**
	 * 条件and <
	 * 
	 * @param fieldName bean字段名，一定是bean字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public ManyTablesQuerySQL andLt(String fieldName, Object value){
		return MV(fieldName, value, AND, LT);
	}
	
	/**
	 * 条件or <
	 * 
	 * @param fieldName bean字段名，一定是bean字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @param otherFieldName 另一个标的字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @return SQL对象
	 */
	public ManyTablesQuerySQL orLtM(String fieldName, String otherFieldName){
		return M(fieldName, otherFieldName, OR, LT);
	}
	
	/**
	 * 条件or <
	 * 
	 * @param fieldName bean字段名，一定是bean字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public ManyTablesQuerySQL orLt(String fieldName, Object value){
		return MV(fieldName, value, OR, LT);
	}
	
	/**
	 * 条件and <=
	 * 
	 * @param fieldName bean字段名，一定是bean字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @param otherFieldName 另一个标的字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @return SQL对象
	 */
	public ManyTablesQuerySQL andLeM(String fieldName, String otherFieldName){
		return M(fieldName, otherFieldName, AND, LE);
	}
	
	/**
	 * 条件and <=
	 * 
	 * @param fieldName bean字段名，一定是bean字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public ManyTablesQuerySQL andLe(String fieldName, Object value){
		return MV(fieldName, value, AND, LE);
	}
	
	/**
	 * 条件or <=
	 * 
	 * @param fieldName bean字段名，一定是bean字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @param otherFieldName 另一个标的字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @return SQL对象
	 */
	public ManyTablesQuerySQL orLeM(String fieldName, String otherFieldName){
		return M(fieldName, otherFieldName, OR, LE);
	}
	
	/**
	 * 条件or <=
	 * 
	 * @param fieldName bean字段名，一定是bean字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public ManyTablesQuerySQL orLe(String fieldName, Object value){
		return MV(fieldName, value, OR, LE);
	}
	
	/**
	 * 条件and >=
	 * 
	 * @param fieldName bean字段名，一定是bean字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @param otherFieldName 另一个标的字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @return SQL对象
	 */
	public ManyTablesQuerySQL andGeM(String fieldName, String otherFieldName){
		return M(fieldName, otherFieldName, AND, GE);
	}
	
	/**
	 * 条件and >=
	 * 
	 * @param fieldName bean字段名，一定是bean字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public ManyTablesQuerySQL andGe(String fieldName, Object value){
		return MV(fieldName, value, AND, GE);
	}
	
	/**
	 * 条件or >=
	 * 
	 * @param fieldName bean字段名，一定是bean字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @param otherFieldName 另一个标的字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @return SQL对象
	 */
	public ManyTablesQuerySQL orGeM(String fieldName, String otherFieldName){
		return M(fieldName, otherFieldName, OR, GE);
	}
	
	/**
	 * 条件or >=
	 * 
	 * @param fieldName bean字段名，一定是bean字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public ManyTablesQuerySQL orGe(String fieldName, Object value){
		return MV(fieldName, value, OR, GE);
	}
	
	/**
	 * 条件or like
	 * 
	 * @param fieldName bean字段名，一定是bean字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public ManyTablesQuerySQL orLike(String fieldName, Object value){
		String alias1 = preDeal(fieldName);
		if (alias1 == null) 
			return this;
		
		value = (value == null ? "" : value.toString()
				.replace("%", "\\%").replace("_", "\\_"));
		otherPartSql.append(" or ").append(alias1).append(" like ? ");
		otherValueList.add("%" + value + "%");
		return this;
	}
	
	/**
	 * 条件and like
	 * 
	 * @param fieldName bean字段名，一定是bean字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public ManyTablesQuerySQL andLike(String fieldName, Object value){
		String alias1 = preDeal(fieldName);
		if (alias1 == null) 
			return this;
		
		value = (value == null ? "" : value.toString()
				.replace("%", "\\%").replace("_", "\\_"));
		otherPartSql.append(" and ").append(alias1).append(" like ? ");
		otherValueList.add("%" + value + "%");
		return this;
	}
	
	/**
	 * in
	 * 
	 * @param fieldName bean字段名，一定是bean字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @param values 对应的值
	 * @param op 操作符（in | not in）
	 * @param condition (or | and)
	 * @return
	 */
	private ManyTablesQuerySQL _in(String fieldName, String op
			, String condition,  Object... values){
		if(fieldName == null || values == null || values.length == 0)
			return this;
		String alias1 = preDeal(fieldName);
		if (alias1 == null) 
			return this;
		
		otherPartSql.append(" ").append(condition).append(" ")
			.append(alias1).append(" ").append(op);
		in(values); 
		return this;
	}
	
	/**
	 * 条件and in
	 * 
	 * @param fieldName bean字段名，一定是bean字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public ManyTablesQuerySQL andIn(String fieldName, Object... values){
		return _in(fieldName, AND, IN, values);
	}

	/**
	 * 条件or in
	 * 
	 * @param fieldName bean字段名，一定是bean字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @param value 对应的值
	 * @return SQL对象
	 */
	public ManyTablesQuerySQL orIn(String fieldName, Object... values){
		return _in(fieldName, OR, IN, values);
	}
	
	/**
	 * 条件or not in
	 * 
	 * @param fieldName bean字段名，一定是bean字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @param values 对应的值
	 * @return SQL对象
	 */
	public ManyTablesQuerySQL orNotIn(String fieldName, Object... values){
		return _in(fieldName, OR, NOT_IN, values);
	}
	
	/**
	 * 条件and not in
	 * 
	 * @param fieldName bean字段名，一定是bean字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @param values 对应的值
	 * @return SQL对象
	 */
	public ManyTablesQuerySQL andNotIn(String fieldName, Object... values){
		return _in(fieldName, AND, NOT_IN, values);
	}
	
	/**
	 * 形成in部分SQL语句
	 * 
	 * @param values
	 */
	private void in(Object... values) {
		int len = values.length;
		otherPartSql.append(LEFT_BRACKET);
		for (int i = 0; i < len; i++) {
			if(i != 0)
				otherPartSql.append(COMMA);
			otherPartSql.append(INTERROGATION);
			otherValueList.add(values[i]);
		}
		otherPartSql.append(RIGHT_BRACKET);
	}
	
	/**
	 * 条件or between
	 * 
	 * @param fieldName bean字段名，一定是bean字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @param value1 对应的值1
	 * @param value2 对应的值2
	 * @return SQL对象
	 */
	public ManyTablesQuerySQL orBetween(String fieldName, Object value1, Object value2){
		return between(fieldName, OR, value1, value2);
	}
	
	/**
	 * 条件and between
	 * 
	 * @param fieldName bean字段名，一定是bean字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @param value1 对应的值1
	 * @param value2 对应的值2
	 * @return SQL对象
	 */
	public ManyTablesQuerySQL andBetween(String fieldName, Object value1, Object value2){
		return between(fieldName, AND, value1, value2);
	}

	/**
	 * between
	 * 
	 * @param fieldName 一定是bean字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @param condition
	 * @param value1
	 * @param value2
	 * @return
	 */
	private ManyTablesQuerySQL between(String fieldName,String condition,
			Object value1, Object value2) {
		String alias1 = preDeal(fieldName);
		if (alias1 == null) 
			return this;
		
		otherPartSql.append(" ").append(condition).append(" ")
			.append(alias1).append(" between ? and ?");
		otherValueList.add(value1);
		otherValueList.add(value2);
		return this;
	}

	@Override
	public Class<?> getEntityClass() {
		return beanClass;
	}
	
	/**
	 * 去除重复的数据
	 * 
	 * @return
	 */
	public ManyTablesQuerySQL distinct(){
		this.distinct = true;
		return this;
	}
	
	/**
	 * group by
	 * 
	 * @param fieldName bean字段名，一定是bean字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @param newCreate 此值设为true时，在原有的SQL语句后拼接上group by + fieldName，否则只拼接上fieldName。
	 * @return SQL对象
	 */
	public ManyTablesQuerySQL groupBy(String fieldName, boolean newCreate){
		String alias1 = preDeal(fieldName);
		if (alias1 == null) 
			return this;
		
		if(newCreate)
			otherPartSql.append(" group by ");
	    else
			otherPartSql.append(COMMA);
		otherPartSql.append(alias1);
		return this;
	}
	
	/**
	 * order by
	 * 
	 * @param fieldName bean字段名，一定是bean字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @param orderType 排序方式（asc | desc）
	 * @param newCreate 此值设为true时，在原有的SQL语句后拼接上order by + fieldName，否则只拼接上fieldName。
	 * @return
	 */
	private ManyTablesQuerySQL orderBy(String fieldName, String orderType
			, boolean newCreate){
		String alias1 = preDeal(fieldName);
		if (alias1 == null) 
			return this;
		
		if(newCreate)
			otherPartSql.append(" order by ");
		else
			otherPartSql.append(COMMA);
		otherPartSql.append(alias1).append(" ").append(orderType).append(" ");
		return this;
	}
	
	/**
	 * 排序升序
	 * 
	 * @param fieldName bean字段名，一定是bean字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @param newCreate 此值设为true时，在原有的SQL语句后拼接上order by + fieldName，否则只拼接上fieldName。
	 * @return SQL对象
	 */
	public ManyTablesQuerySQL asc(String fieldName, boolean newCreate){
		return orderBy(fieldName, ASC, newCreate);
	}
	
	/**
	 * 排序降序
	 * 
	 * @param fieldName bean字段名，一定是bean字段名（写法 ：  类名(首字母大小写都可)+ "." +字段名）
	 * @param newCreate 此值设为true时，在原有的SQL语句后拼接上order by + fieldName，否则只拼接上fieldName。
	 * @return SQL对象
	 */
	public ManyTablesQuerySQL desc(String fieldName, boolean newCreate){
		return orderBy(fieldName, DESC, newCreate);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("from 后的表集合:  tableMap=").append(tableMap).append("\r\n");
		sb.append("join 后的表集合:  joinTableMap=").append(joinTableMap).append("\r\n");
		sb.append("条件值集合:  otherValueList=").append(otherValueList).append("\r\n");
		sb.append("部分SQL:  otherPartSql=").append(otherPartSql).append("\r\n");
		return sb.toString();
	}
	
	/**
	 * join
	 * 
	 * @param otherEntityClass 接实体类型
	 * @param alias 其对应的别名（可为空）
	 * @param joinType 取值（left join | right join | inner join）
	 * @return
	 * @throws EntityException 
	 */
	@SuppressWarnings("all")
	private ManyTablesQuerySQL join(Class<?> otherEntityClass, String alias
			, String joinType) throws EntityException{
		
		Table<?> table = new Table(otherEntityClass);
		table.setAlias(alias);
		joinTableMap.put(otherEntityClass, table);
		
		alias = alias == null ? table.getTableName() + _0 : alias;
		otherPartSql.append(" ").append(joinType).append(" ").append(table.getTableName())
			.append(" ").append(alias).append(" on 1=1");
		
		return this;
	}
	
	/**
	 * left join
	 * 
	 * @param otherEntityClass 左连接实体类型
	 * @param alias 其对应的别名（可为空）
	 * @return SQL对象
	 * @throws EntityException 
	 */
	public ManyTablesQuerySQL leftJoin(Class<?> otherEntityClass, String alias) 
		throws EntityException{
		return join(otherEntityClass, alias, LEFT_JOIN);
	}
	
	/**
	 * right join
	 * 
	 * @param otherEntityClass 左连接实体类型
	 * @param alias 其对应的别名（可为空）
	 * @return SQL对象
	 * @throws EntityException 
	 */
	public ManyTablesQuerySQL rightJoin(Class<?> otherEntityClass, String alias) 
		throws EntityException{
		return join(otherEntityClass, alias, RIGHT_JOIN);
	}
	
	/**
	 * inner join
	 * 
	 * @param otherEntityClass 左连接实体类型
	 * @param alias 其对应的别名（可为空）
	 * @return SQL对象
	 * @throws EntityException 
	 */
	public ManyTablesQuerySQL innerJoin(Class<?> otherEntityClass, String alias) 
		throws EntityException{
		return join(otherEntityClass, alias, INNER_JOIN);
	}
	
	/**
	 * 分页查询信息，暂时只支持mysql数据库
	 * 
	 * @param limit
	 * @return
	 * @throws EntityException 
	 */
	public ManyTablesQuerySQL limit(Limit limit) throws EntityException{
		if(limit != null && limit.getDbType() != DBType.MYSQL_DB)
			throw new EntityException("该操作暂时自支持mysql数据库");
		this.limit = limit;
		return this;
	}
}
