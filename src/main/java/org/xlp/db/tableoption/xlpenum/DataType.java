package org.xlp.db.tableoption.xlpenum;
import org.xlp.utils.XLPStringUtil;

/**
 * <p>创建时间：2021年3月14日 下午10:48:06</p>
 * @author xlp
 * @version 1.0 
 * @Description 数据库表字段的类型
*/
public enum DataType {
	BIGINT("bigint"),
	BLOB("blob"),
	CHAR("char"),
	INT("int"),
	DATETIME("datetime"),
	DECIMAL("decimal"),
	DOUBLE("double"),
	ENUM("enum"),
	FLOAT("float"),
	LONGBLOB("longblob"),
	LONGTEXT("longtext"),
	MEDIUMBLOB("mediumblob"),
	MEDIUMTEXT("mediumtext"),
	SET("set"),
	SMALLINT("smallint"),
	TEXT("text"),
	TIME("time"),
	TIMESTAMP("timestamp"),
	BOOLEAN("boolean"),
	VARCHAR("varchar"),
	TINYINT("tinyint"),
	NONE(XLPStringUtil.EMPTY)
	;
	private String dataTypeName;
	
	private DataType(String dataTypeName){
		this.dataTypeName = dataTypeName;
	}

	/**
	 * @return 数据库表字段的类型名称
	 */
	public String getDataTypeName() {
		return dataTypeName;
	}
}
