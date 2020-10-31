package org.xlp.db.tableoption.handlers.result;

import org.xlp.db.tableoption.annotation.XLPColumn;
import org.xlp.db.tableoption.annotation.XLPId;
import org.xlp.db.tableoption.annotation.XLPIrrelevantColumn;
import org.xlp.javabean.PropertyDescriptor;
import org.xlp.javabean.convert.mapandbean.MapBeanAbstract;
import org.xlp.javabean.processer.ValueProcesser;
import org.xlp.utils.XLPStringUtil;

/**
 * 用相关注解进行map与bean相互转换
 * 
 * @author 徐龙平
 *         <p>
 *         2017-5-23
 *         </p>
 * @version 1.0
 * 
 */
public class DBMapBeanConverter<T> extends MapBeanAbstract<T> {
	public DBMapBeanConverter() {
		super();
	}

	/**
	 * 标志map中是否存储bean字段值为空的条目
	 * 
	 * @param isContainNull
	 */
	public DBMapBeanConverter(boolean isContainNull) {
		super(isContainNull);
	}

	/**
	 * @param format
	 *            字符串日期相互转换格式
	 * @param isContainNull
	 *            标志map中是否存储bean字段值为空的条目
	 */
	public DBMapBeanConverter(String format, boolean isContainNull) {
		super(format, isContainNull);
	}

	/**
	 * @param format
	 *            字符串日期相互转换格式
	 */
	public DBMapBeanConverter(String format) {
		super(format);
	}

	/**
	 * 
	 * @param processer
	 *            值处理器
	 */
	public DBMapBeanConverter(ValueProcesser processer) {
		super(processer);
	}

	/**
	 * 
	 * @param processer
	 *            值处理器
	 * @param isContainNull
	 *            标志map中是否存储bean字段值为空的条目
	 */
	public DBMapBeanConverter(ValueProcesser processer, boolean isContainNull) {
		super(processer, isContainNull);
	}

	@Override
	protected String virtualReadFieldName(PropertyDescriptor<T> pd) {
		String virtualFieldName = null;
		XLPColumn xlpColumn = pd.getFieldAnnotation(XLPColumn.class);
		XLPId xlpId;
		XLPIrrelevantColumn irrelevantColumn;
		if (xlpColumn != null) {
			virtualFieldName = xlpColumn.columnName().trim();
			virtualFieldName = XLPStringUtil.isEmpty(virtualFieldName) ? pd.getFieldName() : virtualFieldName;
		} else if ((xlpId = pd.getFieldAnnotation(XLPId.class)) != null) {
			virtualFieldName = xlpId.columnName().trim();
			virtualFieldName = XLPStringUtil.isEmpty(virtualFieldName) ? pd.getFieldName() : virtualFieldName;
		} else if ((irrelevantColumn = pd.getFieldAnnotation(XLPIrrelevantColumn.class)) != null) {
			virtualFieldName = irrelevantColumn.columnName().trim();
			virtualFieldName = XLPStringUtil.isEmpty(virtualFieldName) ? pd.getFieldName() : virtualFieldName;
		}
		return virtualFieldName;
	}

	@Override
	protected String virtualWriteFieldName(PropertyDescriptor<T> pd) {
		return virtualReadFieldName(pd);
	}
}
