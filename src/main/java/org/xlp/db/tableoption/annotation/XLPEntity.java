package org.xlp.db.tableoption.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.xlp.db.tableoption.xlpenum.DBEngine;
import org.xlp.db.tableoption.xlpenum.PrimaryKeyDataType;
import org.xlp.db.tableoption.xlpenum.PrimaryKeyType;
import org.xlp.db.tableoption.xlpenum.TableType;

/**
 * 用来判断是否是实体，以及对应的数据库中标的名字
 * 
 * @author 徐龙平
 *         <p>
 *         2017-5-23
 *         </p>
 * @version 2.0
 * 
 */
@Retention(RetentionPolicy.RUNTIME) // 注解会在class字节码文件中存在，在运行时可以通过反射获取到  
@Target({ElementType.TYPE})//定义注解的作用目标**作用范围字段 
@Documented//说明该注解将被包含在javadoc中 
public @interface XLPEntity {
	public String tableName();//数据库中标的名字
	public String descriptor() default "";//描述
	
	//以下字段用来根据实体类创建相应的表
	/**
	 * 字符编码
	 */
	public String chartsetName() default "utf8mb4";
	
	/**
	 * db引擎类型
	 */
	public DBEngine dbEngine() default DBEngine.INNODB;
	
	/**
	 * 视图类型，默认为数据表实体
	 */
	public TableType tableType() default TableType.TABLE;
	
	/**
	 * 主键类型
	 */
	public PrimaryKeyType primaryKeyType() default PrimaryKeyType.UUID;
	
	/**
	 * 主键描述
	 * @return
	 */
	public String primaryKeyDescriptor() default "";
	
	/**
	 * 主键长度, 默认值适用于字符串类
	 */
	public int primaryKeyLength() default -1;

	/**
	 * 主键数据类型
	 */
	public PrimaryKeyDataType primaryKeyDataType() default PrimaryKeyDataType.VARCHAR;
}
