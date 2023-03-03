package org.xlp.db.tableoption.xlpenum;

import org.xlp.utils.XLPStringUtil;

/**
 * 主键数据类型
 * 
 * @author 徐龙平
 * 
 * @version 1.0
 */
public enum PrimaryKeyDataType {
	/**
	 * long
	 */
	BIGINT("bigint"),
	/**
	 * 字符串
	 */
	CHAR("char"),
	/**
	 * int
	 */
	INT("int"),
	/**
	 * 字符串
	 */
	VARCHAR("varchar"),
	
	NONE(XLPStringUtil.EMPTY);
	
	private String dataTypeName;
	
	private PrimaryKeyDataType(String dataTypeName){
		this.dataTypeName = dataTypeName;
	}

	/**
	 * @return 数据库表字段的类型名称
	 */
	public String getDataTypeName() {
		return dataTypeName;
	}
}
