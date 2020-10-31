package org.xlp.db.tableoption.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记是否开启事务
 * <p>此注解标记的方法里有异常，应该抛出，否则当事物回滚时，可能不能正确的回滚
 * <p>使用此注解的类，如果要实例化时，应该调用<code>XLPFactory</code>中的create()方法，否则此注解无效
 * 
 * @author 徐龙平
 *         <p>
 *         2017-6-19
 *         </p>
 * @version 1.0
 * 
 */
@Retention(RetentionPolicy.RUNTIME) // 注解会在class字节码文件中存在，在运行时可以通过反射获取到  
@Target({ElementType.METHOD})//定义注解的作用目标**作用范围字段 
@Documented//说明该注解将被包含在javadoc中
public @interface XLPTransaction {
	/**
	 *描述 
	 * 
	 * @return
	 */
	public String descriptor() default "";
}
