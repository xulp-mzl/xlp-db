package org.xlp.db.tableoption.handlers.result;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xlp.javabean.JavaBeanPropertiesDescriptor;
import org.xlp.javabean.PropertyDescriptor;
import org.xlp.javabean.convert.mapandbean.MapValueProcesser;
import org.xlp.javabean.processer.ValueProcesser;
import org.xlp.utils.XLPDateUtil;


/**
 * 结果集处理成不同的结果对象默认类
 * 
 * @author 徐龙平
 *         <p>
 *         2017-5-23
 *         </p>
 * @version 2.0
 * 
 */
public class DefaultResult implements Result{
	//未匹配设置为-1
	public final static int PROPERTY_NOT_FOUND = -1;
	//值处理器
	private final ValueProcesser valueProcesser;
	
	public DefaultResult(){
		this(XLPDateUtil.DATE_FORMAT);
	}
	
	/**
	 * 字符串与时间的转换格式
	 * @param format
	 */
	public DefaultResult(String format){
		valueProcesser = new MapValueProcesser(format);
	}
	
	/**
	 * 值处理器
	 * @param processer
	 */
	public DefaultResult(ValueProcesser processer){
		valueProcesser = processer;
	}
	
	/**
	 * 返回实例对象
	 * 
	 * @param cs
	 * @return
	 * @throws XLPJavaBeanException
	 * 			此异常继承与SQLException
	 */
	private <T> T newInstance(Class<T> cs) throws XLPJavaBeanException{
		try {
			return cs.newInstance();
		} catch (InstantiationException e) {
			throw new XLPJavaBeanException(cs.getSimpleName() + "该类实例化对象失败");
		} catch (IllegalAccessException e) {
			throw new XLPJavaBeanException(cs.getSimpleName() + "该类实例化对象失败");
		}
	}
	
	/**
	 * 处理成javabean
	 * 
	 * @param rs 结果集
	 * @param type JavaBean类型
	 * @return JavaBean对象
	 * @throws SQLException 假如数据库访问出错，抛出该异常
	 */
	@Override
	public <T> T toJavaBean(ResultSet rs, Class<T> type) throws SQLException {
		if(!rs.next()) return null;
		
		PropertyDescriptor<T>[] pds = new JavaBeanPropertiesDescriptor<T>(type)
				.getPds();
		
		ResultSetMetaData rsmd = rs.getMetaData();
		
		int[] columnToProperty = this.columnsToProperties(rsmd, pds);
		
		return this.createBean(rs, pds, columnToProperty, type);
	}
	
	 /**
     * 返回数组存储的值是 . <code>PropertyDescriptor[]</code> 数组的下标即
     * 与数据库表字段匹配的bean属性描述
     * 未匹配的地方用<code>PROPERTY_NOT_FOUND</code>表示
     *
     * @param rsmd The <code>ResultSetMetaData</code> 包含的结果集信息的对象
     *
     * @param pds bean属性描述.
     *
     * @throws SQLException 假如数据库访问出错，抛出该异常
     *
     * @return 
     */
	protected <T> int[] columnsToProperties(ResultSetMetaData rsmd, 
			PropertyDescriptor<T>[] pds) throws SQLException{
		int cols = rsmd.getColumnCount();
		int[] columnToProperty = new int[cols + 1];
	    Arrays.fill(columnToProperty, PROPERTY_NOT_FOUND);
	    String columnName, columnLabel, fieldName;
	    
	    for (int col = 1; col <= cols; col++) {
			columnName = rsmd.getColumnName(col);
			columnLabel = rsmd.getColumnLabel(col);
			
			for (int i = 0, len = pds.length; i < len; i++) {
				//判断属性名称与表列名是否相同
				fieldName = pds[i].getFieldName();
				if (fieldName.equalsIgnoreCase(columnName)
						|| fieldName.equalsIgnoreCase(columnLabel)) {
					columnToProperty[col] = i;
					break;
				}
			}
		}
	    
	    return columnToProperty;
	}
	
	/**
	 * 用数据库中查出的结果集创建一个给定类型的新的对象
	 * 
	 * @param rs 结果集
	 * @param type 对象类型
	 * @param pds 属性描述数组
	 * @param columnToProperty 属性描述中属性名与表字段相同的属性描述数组的下标
	 * @return
	 * @throws SQLException 假如数据库访问出错，抛出该异常
	 */
	private <T> T createBean(ResultSet rs, PropertyDescriptor<T>[] pds, int[] columnToProperty,
			Class<T> type) throws SQLException {
		int len = columnToProperty.length;
		
		T bean = newInstance(type);
		
		for (int i = 1; i < len; i++) {
			if (columnToProperty[i] == PROPERTY_NOT_FOUND) {
				continue;
			}
			
			PropertyDescriptor<T> pd = pds[columnToProperty[i]];
			
			Class<?> propertyType = pd.getFiledClassType();
			
			Object value = processColumnValue(i, propertyType, rs);
			
			if (propertyType != null && propertyType.isPrimitive() &&
					value == null) {
				 
			   // 当数据库中查出的数据为SQL NULL时，给bean属性为基本类型设置以下默认值
				value = ValueProcesser.PRIMITIVE_DEFAULTS.get(propertyType);
			}
			
			this.callSetter(value, pd, bean);
		}
		return bean;
	}

	/**
	 * 对数据库中列值进行处理
	 * 
	 * @param col 当前要处理的<code>ResultSet<code>的列标
	 * @param propertyType 把当前列标值处理成此类型的新对象
	 * @param rs 当前要处理的<code>ResultSet<code>
	 * @return 处理结果
	 * @throws SQLException 
	 */
	private Object processColumnValue(int col, Class<?> propertyType,
			ResultSet rs) throws SQLException {
		
		Object value = rs.getObject(col);
		return valueProcesser.processValue(propertyType, value);
	}

	/**
	 * 处理bean字段的写方法
	 * 
	 * @param value
	 * @param pd
	 * @param bean
	 */
	private <T> void callSetter(Object value, PropertyDescriptor<T> pd, T bean) {
		try {
			pd.executeWriteMethod(bean, value);
		} catch (Exception e) {
		}
	}

	/**
	 * 把结果集处理成javabean List集合
	 * 
	 * @param rs
	 * @param type
	 * @throws SQLException 
	 */
	@Override
	public <T> List<T> toJavaBeanList(ResultSet rs, Class<T> type) throws SQLException {
		List<T> beanList = new ArrayList<T>();
		if (!rs.next()) {
			return beanList;
		}
		
		ResultSetMetaData rsmd = rs.getMetaData();
		
		PropertyDescriptor<T>[] pds = new JavaBeanPropertiesDescriptor<T>(type)
											.getPds();
		int[] columnToProperty = this.columnsToProperties(rsmd, pds);
		
		do {
			beanList.add(this.createBean(rs, pds, columnToProperty, type));
		} while (rs.next());
		
		return beanList;
	}

	/**
	  * 处理成Map集合
	  * 
	  * @param rs
	  * @return
	  * @throws SQLException
	*/
	@Override
	public Map<String, Object> toMap(ResultSet rs) throws SQLException {
		if (!rs.next()) {
			return new HashMap<String, Object>();
		}
		
		ResultSetMetaData rsmd = rs.getMetaData();
		int cols = rsmd.getColumnCount();
		
		Map<String, Object> map = createMap(rs, rsmd, cols);
		return map;
	}

	/**
	 * 创建一个新的Map集合
	 * 
	 * @param rs
	 * @param rsmd
	 * @param cols
	 * @return
	 * @throws SQLException
	 */
	private Map<String, Object> createMap(ResultSet rs,
			ResultSetMetaData rsmd, int cols) throws SQLException {
		Map<String, Object> map = new HashMap<String, Object>();
		for (int i = 1; i <= cols; i++) {
			map.put(rsmd.getColumnName(i), rs.getObject(i));
		}
		return map;
	}

	/**
	  * 处理成Map List集合
	  * 
	  * @param rs
	  * @return
	  * @throws SQLException
	*/
	@Override
	public List<Map<String, Object>> toMapList(ResultSet rs)
			throws SQLException {
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
		if (!rs.next()) {
			return mapList;
		}
		
		ResultSetMetaData rsmd = rs.getMetaData();
		int cols = rsmd.getColumnCount();
		
		do {
			Map<String, Object> map = createMap(rs, rsmd, cols);
			mapList.add(map);
		} while (rs.next());
		
		return mapList;
	}

	 /**
	  * 把每行记录处理成数组
	  * 
	  * @param rs
	  * @return
	  * @throws SQLException
	*/
	@Override
	public Object[] toArray(ResultSet rs) throws SQLException {
		if (!rs.next()) {
			return new Object[]{};
		}
		
		ResultSetMetaData rsmd = rs.getMetaData();
		int cols = rsmd.getColumnCount();
		
		Object[] result = createArray(rs, cols);
		
		return result;
	}

	/**
	 * 创建一个新的object[]
	 * 
	 * @param rs 结果集
	 * @param cols 结果集的列数
	 * @return
	 * @throws SQLException
	 */
	private Object[] createArray(ResultSet rs, int cols) throws SQLException {
		Object[] result = new Object[cols];
		for (int i = 0; i < cols; i++) {
			result[i] = rs.getObject(i + 1);
		}
		return result;
	}

	 /**
	  * 把每行记录处理成数组 List集合
	  * 
	  * @param rs
	  * @return
	  * @throws SQLException
	*/
	@Override
	public List<Object[]> toArrayList(ResultSet rs) throws SQLException {
		List<Object[]> result = new ArrayList<Object[]>();
		if (!rs.next()) {
			return result;
		}
		
		ResultSetMetaData rsmd = rs.getMetaData();
		int cols = rsmd.getColumnCount();
		
		do {
			result.add(createArray(rs, cols));
		} while (rs.next());
		
		return result;
	}

	/**
	 * 处理成javabean Set集合
	 * 
	 * @param rs
	 * @param type
	 * @return
	 * @throws SQLException
	 */
	@Override
	public <T> Set<T> toJavaBeanSet(ResultSet rs, Class<T> type)
			throws SQLException {
		return new HashSet<T>(this.toJavaBeanList(rs, type));
	}

}
