package org.xlp.db.utils;

import org.xlp.db.tableoption.BaseDBOption;

/**
 * 常量提供工具类
 * 
 * @author 徐龙平
 *         <p>
 *         2017-5-24
 *         </p>
 * @version 1.0
 * 
 */
public final class Constants {
	//默认数据库表操作的对象常量
	public transient final static BaseDBOption BASE_DB_OPTION = BaseDBOption.newInstance();
	
	/**
	 * 默认页码显示数量
	 */
	public static final int DEFAULT_PAGE_CODE_NUM = 5;
	
	/**
	 * 默认页面大小
	 */
	public static final int DEFAULT_PAGE_SIZE = 8;
}
