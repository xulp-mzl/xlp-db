package org.xlp.db.tableoption.handlers.result;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ResultSet转换的数据集合类
 * 
 * @author 徐龙平
 *         <p>
 *         2017-6-19
 *         </p>
 * @version 1.0
 * 
 */
public class DataSet {
	// 查询结果列名数组
	private String[] titles;
	// 查询结果数据集合
	private List<Object[]> datas;
	// 查询结果数据条数
	private long count = 0;
	// 各列对应的数据类型
	private Class<?>[] columnType;
	// 当前游标位置
	private volatile int currentPos = -1;

	/**
	 * 查询结果列名数组
	 * 
	 * @return
	 */
	public String[] getTitles() {
		return titles;
	}

	/**
	 * 设置查询结果列名数组
	 * 
	 * @param titles
	 */
	public void setTitles(String[] titles) {
		this.titles = titles;
	}

	/**
	 * 获取查询结果数据
	 * 
	 * @return
	 */
	public List<Object[]> getDatas() {
		return datas;
	}

	/**
	 * 设置查询结果数据
	 * 
	 * @param datas
	 */
	public void setDatas(List<Object[]> datas) {
		this.datas = datas;
	}

	/**
	 * 获取查询结果数据条数
	 * 
	 * @return
	 */
	public long getCount() {
		return count;
	}

	/**
	 * 设置查询结果数据条数
	 * 
	 * @param count
	 */
	public void setCount(long count) {
		this.count = count;
	}

	/**
	 * 获取各列对应的数据类型
	 * 
	 * @param columnType
	 */
	public void setColumnType(Class<?>[] columnType) {
		this.columnType = columnType;
	}

	/**
	 * 设置各列对应的数据类型
	 * 
	 * @return
	 */
	public Class<?>[] getColumnType() {
		return columnType;
	}

	/**
	 * 移动到第一行（即首行的前面）
	 */
	public synchronized void toFirst() {
		currentPos = -1;
	}

	/**
	 * 返回是否有下一行数据
	 * 
	 * @return
	 */
	public synchronized boolean next() {
		return ++currentPos < count;
	}

	/**
	 * 根据指定列标获取当前行该列的数据
	 * 
	 * @param column
	 *            列标从1开始
	 * @return
	 */
	public Object getData(int column) {
		return datas.get(currentPos)[column - 1];
	}

	/**
	 * 根据指定列标获取当前行该列的数据,假如该列值为null，则返回defaultValue该参数值
	 * 
	 * @param column
	 *            列标从1开始
	 * @param defaultValue
	 *            默认填充值
	 * @return
	 */
	public Object getData(int column, Object defaultValue) {
		Object value = getData(column);
		return value == null ? defaultValue : value;
	}

	/**
	 * 根据指定的列名获取当前行该列的数据
	 * 
	 * @param columnName
	 * @return
	 */
	public Object getData(String columnName) {
		int index = -1;
		for (int i = 0, len = titles.length; i < len; i++) {
			if (titles[i].equalsIgnoreCase(columnName)) {
				index = i;
				break;
			}
		}
		if (index == -1)
			return null;

		return datas.get(currentPos)[index];
	}

	/**
	 * 根据指定的列名获取当前行该列的数据
	 * 
	 * @param columnName
	 * @param defaultValue
	 *            默认值
	 * @return 假如指定列名的该列值不存在时，返回指定的默认值
	 */
	public Object getData(String columnName, Object defaultValue) {
		int index = -1;
		for (int i = 0, len = titles.length; i < len; i++) {
			if (titles[i].equalsIgnoreCase(columnName)) {
				index = i;
				break;
			}
		}
		if (index == -1)
			return defaultValue;

		return datas.get(currentPos)[index];
	}

	/**
	 * 根据指定的列名获取当前行该列的数据
	 * 
	 * @param columnName
	 *            列名
	 * @param defaultValue
	 *            填充值
	 * @return 假如指定列名的该列值不存在或为null时，返回指定的默认填充值
	 */
	public Object getFullData(String columnName, Object defaultValue) {
		Object value = getData(columnName);
		return value == null ? defaultValue : value;
	}

	@Override
	public String toString() {
		return "DataSet [columnType=" + Arrays.toString(columnType)
				+ ", count=" + count + ", currentPos=" + currentPos
				+ ", datas=" + datas + ", titles=" + Arrays.toString(titles)
				+ "]";
	}

	/**
	 * 得到当前行所有数据
	 * 
	 * @return
	 */
	public Object[] getRowData() {
		return datas.get(currentPos);
	}

	/**
	 * 获取指定行的所有数据
	 * 
	 * @param row
	 *            行号，数值从1开始
	 * @return
	 */
	public Object[] getRowData(int row) {
		return datas.get(row - 1);
	}

	/**
	 * 获取指定行的所有数据(以map集合形式返回，键为列名称，value为其对应的值)
	 * 
	 * @param row
	 *            行号，数值从1开始
	 * @return
	 */
	public Map<String, Object> getRowDataAsMap(int row) {
		Object[] rowData = datas.get(row - 1);
		Map<String, Object> map = new HashMap<String, Object>();
		int len = rowData.length;
		for (int i = 0; i < len; i++)
			map.put(titles[i], rowData[i]);
		return map;
	}

	/**
	 * 获取当前行的所有数据(以map集合形式返回，键为列名称，value为其对应的值)
	 * 
	 * @return
	 */
	public Map<String, Object> getRowDataAsMap() {
		return getRowDataAsMap(currentPos + 1);
	}
}
