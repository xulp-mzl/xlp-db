package org.xlp.db.sql.item;
/**
 * <p>创建时间：2022年5月13日 下午11:15:35</p>
 * @author xlp
 * @version 1.0 
 * @Description sql操作符枚举
*/
public enum OperatorEnum {
	EQ("="),
	NEQ("!="),
	LIKE("like"),
	NLIKE("not like"),
	GT(">"),
	GE(">="),
	LT("<"),
	LE("<="),
	IS_NULL("is null"),
	IS_NOT_NULL("is not null"),
	IN("in"),
	NIN("not in"),
	BETWEEN("between ? and ?"),
	NBETWEEN("not between ? and ?"),
	LEFT_BRACKET("("),
	RIGHT_BRACKET(")");
	
	/**
	 * 连接符
	 */
	private String operator;
	
	private OperatorEnum(String operator){
		this.operator = operator;
	}
	
	public String getOperator(){
		return operator;
	}
}
