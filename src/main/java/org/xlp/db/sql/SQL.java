package org.xlp.db.sql;

/**
 * sql接口
 * 
 * @author 徐龙平
 *         <p>
 *         2017-5-26
 *         </p>
 * @version 1.0
 * 
 */
public interface SQL {
	//逗号
	public final static String COMMA = ",";
	//单引号
	public final static String SINGLE_QUOTE = "\'";
	//左括号
	public final static String LEFT_BRACKET = "(";
	//右括号+空格
	public final static String RIGHT_BRACKET = ") ";
	//问号
	public final static String INTERROGATION = "?";
	//or
	public final static String OR = "or";
	//and
	public final static String AND = "and";
	//=
	public final static String EQ = "=";
	//>
	public final static String GT = ">";
	//<
	public final static String LT = "<";
	//!=
	public final static String NOT_EQ = "!=";
	//>=
	public final static String GE = ">=";
	//<= 
	public final static String LE = "<=";
	//is null
	public final static String IS_NULL = "is null";
	//is not null
	public final static String IS_NOT_NULL = "is not null";
	//in
	public final static String IN = "in";
	//not in
	public final static String NOT_IN = "not in";
	//asc
	public final static String ASC = "asc";
	//desc
	public final static String DESC = "desc";
	// left join 
	public final static String LEFT_JOIN = "left join";
	//right join
	public final static String RIGHT_JOIN = "right join";
	//inner join
	public final static String INNER_JOIN = "inner join";
	//like
	public final static String LIKE = "like";
	
	/**
	 * 得到不带预处理参数的SQL语句
	 * 
	 * @return
	 */
	public String getSql();
	
	/**
	 * 得到带预处理参数的SQL语句
	 * 
	 * @return
	 */
	public String getParamSql();
	
	/**
	 * 返回对应的预处理参数数组
	 * 
	 * @return
	 */
	public Object[] getParams();
	
	/**
	 * 获取创建此SQL对象实体的类型
	 * 
	 * @return
	 */
	public Class<?> getEntityClass();
}
