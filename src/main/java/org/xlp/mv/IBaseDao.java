package org.xlp.mv;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xlp.db.exception.EntityException;
import org.xlp.db.page.Page;
import org.xlp.db.sql.SQL;
import org.xlp.db.tableoption.handlers.result.DataSet;

/**
 * 
 * @author 徐龙平
 * 
 *         数据库操作基础类，该类可以完成大部分操作
 * 
 */
public interface IBaseDao {
	/**
	 * 向数据库中插入一条数据
	 * <p>
	 * 假如给定要存储的对象是实体，并有相关的主键，并主键值未指定，那么此函数会自动生成一个可用的主键值
	 * 
	 * @param bean
	 * @return 主键值，假如返回null时，可能数据保存失败，也可能指定的bean对象没有主键属性。 假如复合主键时，返回他们组成的数组
	 * @throws EntityException OptionDBException  
	 *             假如该对象不是实体或数据库访问出错时，抛出该异常
	 */
	public <T> Object save(T bean);

	/**
	 * 向数据库中插入多条数据
	 * <p>
	 * 假如给定要存储的对象是实体，并有相关的主键，并主键值未指定，那么此函数会自动生成一个可用的主键值
	 * 
	 * @param bean
	 *            数据集合
	 * @return 假如参数为null或保存失败，返回false，否则返回true
	 * @throws EntityException OptionDBException 
	 *             假如该对象不是实体或数据库访问出错时，抛出该异常
	 */
	public <T> boolean save(List<T> bean);

	/**
	 * 通过SQL对象向数据库中插入一条数据
	 * 
	 * @param sqlObj
	 *            SQL对象
	 * @return 受影响数据的条数
	 * @throws EntityException OptionDBException 
	 *             假如数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	public int save(SQL sqlObj);

	/**
	 * 得到单表数据的条数
	 * 
	 * @param beanClass
	 * @return
	 * @throws EntityException OptionDBException 
	 *             假如该对象不是实体或数据库访问出错时，抛出该异常
	 */
	public <T> long count(Class<T> beanClass);

	/**
	 * 得数据的条数
	 * 
	 * @param sqlObj
	 * @return
	 * @throws EntityException OptionDBException 
	 *             假如数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	public long count(SQL sqlObj);

	/**
	 * 通过SQL对象得到javabean对象
	 * 
	 * @param sqlObj
	 * @return
	 * @throws EntityException OptionDBException 
	 *             假如数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	public <T> T find(SQL sqlObj);

	/**
	 * 通过 实体对象得到javabean对象
	 * 
	 * @param bean
	 *            实体对象,此对象一定要有相关的主属性，否则抛出SQL异常
	 * @return 假如参数为null，返回null，没有符合要求的数据也返回null
	 * @throws EntityException OptionDBException 
	 *             假如该对象没要有相关的主属性或不是实体或数据库访问出错时，抛出该异常
	 */
	public <T> T find(T bean);

	/**
	 * 通过主键查找数据
	 * 
	 * @param beanClass
	 *            对象类型
	 * @param keyValues
	 *            主键属性值(单主键)
	 * @return 假如参数为null，返回null，没有符合要求的数据也返回null
	 * @throws EntityException OptionDBException 
	 *             假如该对象类型对象不是实体或数据库访问出错时，抛出该异常
	 */
	public <T> T find(Class<T> beanClass, Object keyValue);

	/**
	 * 通过主键查找数据
	 * 
	 * @param beanClass
	 *            对象类型
	 * @param keyValues
	 *            主键属性值数组(复合主键)
	 * @return 假如参数为null，返回null，没有符合要求的数据也返回null
	 * @throws EntityException OptionDBException 
	 *             假如该对象没要有相关的主属性或不是实体或数据库访问出错时，抛出该异常
	 */
	public <T> T find(Class<T> beanClass, Object... keyValues);

	/**
	 * 通过单表得到javabean list集合
	 * 
	 * @param beanClass
	 *            bean类型
	 * @return
	 * @throws EntityException OptionDBException 
	 *             假如该对象不是实体或数据库访问出错时，抛出该异常
	 */
	public <T> List<T> list(Class<T> beanClass);

	/**
	 * 通过SQL对象得到javabean list集合
	 * 
	 * @param sqlObj
	 * @return
	 * @throws EntityException OptionDBException 
	 *             假如数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	public <T> List<T> list(SQL sqlObj);
	/**
	 * 通过单表得到javabean set集合
	 * 
	 * @param beanClass
	 * @return
	 * @throws EntityException OptionDBException 
	 *             假如该对象不是实体或数据库访问出错时，抛出该异常
	 */
	public <T> Set<T> set(Class<T> beanClass);

	/**
	 * 通过SQL对象得到javabean set集合
	 * 
	 * @param sqlObj
	 * @return
	 * @throws EntityException OptionDBException 
	 *             假如数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	public <T> Set<T> set(SQL sqlObj);
	/**
	 * 删除指定bean类型对应的表中的所有数据
	 * 
	 * @param beanClass
	 * @return 假如返回true，删除数据成功，返回false，删除数据失败
	 * @throws EntityException OptionDBException 
	 *             假如该对象不是实体或数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	public <T> boolean delete(Class<T> beanClass);

	/**
	 * 删除指定bean对应的表中的数据
	 * 
	 * @param bean
	 * @return 假如返回true，删除数据成功，返回false，删除数据失败
	 * @throws EntityException OptionDBException 
	 *             假如该对象没要有相关的主属性或不是实体或数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	public <T> boolean delete(T bean);

	/**
	 * 根据SQL对象删除数
	 * 
	 * @param sql
	 * @return 假如返回true，删除数据成功，返回false，删除数据失败
	 * @throws EntityException OptionDBException 
	 *             假如数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	public boolean update(SQL sql);

	/**
	 * 通过主键删除数据
	 * 
	 * @param beanClass
	 *            对象类型
	 * @param keyValues
	 *            主键属性值(单主键)
	 * @return 假如返回true，删除数据成功，返回false，删除数据失败
	 * @throws EntityException OptionDBException 
	 *             假如该对象没要有相关的主属性或不是实体或数据库访问出错时，抛出该异常
	 */
	public <T> boolean delete(Class<T> beanClass, Object keyValue);
	/**
	 * 通过主键删除数据(复合主键)
	 * 
	 * @param beanClass
	 *            对象类型
	 * @param keyValues
	 *            主键属性值数组(复合主键)
	 * @return 假如返回true，删除数据成功，返回false，删除数据失败
	 * @throws EntityException OptionDBException 
	 *             假如该对象没要有相关的主属性或不是实体或数据库访问出错时，抛出该异常
	 */
	public <T> boolean delete(Class<T> beanClass, Object... keyValues);

	/**
	 * 更新，指定的bean一定要主键属性
	 * 
	 * @param bean
	 * @return 假如返回true，更新数据成功，返回false，更新数据失败
	 * @throws EntityException OptionDBException 
	 *             假如该对象没要有相关的主属性或不是实体或数据库访问出错时，抛出该异常
	 */
	public <T> boolean update(T bean);

	/**
	 * 通过bean对象获取新的bean对象
	 * <p>
	 * (E:Enhance 增强)
	 * 
	 * @param bean
	 * @return 假如bean对象属性中有别的bean对象或别的bean对象集合。
	 *         <p>
	 *         调用这个函数都会封装，如果没有则与find(T bean)的效果相同。
	 * @throws EntityException OptionDBException 
	 *             假如该对象没要有相关的主属性或不是实体或数据库访问出错时，抛出该异常
	 */
	public <T> T findE(T bean);

	/**
	 * 通过SQL对象获取dataset对象
	 * 
	 * @param sql
	 * @return 从不返回null
	 * @throws EntityException OptionDBException 
	 *             假如数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	public DataSet findDataSet(SQL sql);
	
	/**
	 * 把数据处理成数组
	 * 
	 * @param sql
	 * @return 从不返回null, 如无数据返回大小为0的数组
	 * @throws EntityException OptionDBException 
	 *             假如数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	public Object[] array(SQL sql);

	/**
	 * 把数据处理成List<Object[]>集合
	 * 
	 * @param sql
	 * @return 从不返回null, 如无数据返回大小为0的集合
	 * @throws EntityException OptionDBException 
	 *             假如数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	public List<Object[]> listArray(SQL sql);

	/**
	 * 把数据处理成Map<String, Object>集合
	 * 
	 * @param sql
	 * @return 从不返回null, 如无数据返回大小为0的集合
	 * @throws EntityException OptionDBException 
	 *             假如数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	public Map<String, Object> map(SQL sql);

	/**
	 * 把数据处理成List<Map<String, Object>>集合
	 * 
	 * @param sql
	 * @return 从不返回null, 如无数据返回大小为0的集合
	 * @throws EntityException OptionDBException 
	 *             假如数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	public List<Map<String, Object>> listMap(SQL sql);

	/**
	 * 分页查询，暂时只对mysql有效
	 * 
	 * @param sql
	 *            SQL对象， 该对象中应不包含limit条件，否则可能抛出异常
	 * @param page
	 *            分页对象
	 * @return 返回完整分页对象
	 * @throws EntityException OptionDBException 
	 *             假如数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	public <T> Page<T> searchPage(SQL sql, Page<T> page);
	/**
	 * 得到单表数据的指定字段的最大值
	 * 
	 * @param beanClass
	 * @param maxFieldName
	 *            要求最大值字段名称对应的bean的属性名称
	 * @return 从不返回null，假如无数据返回0
	 * @throws EntityException OptionDBException 
	 *             假如该对象不是实体或数据库访问出错时，抛出该异常
	 */
	public <T> Double max(Class<T> beanClass, String maxFieldName);

	/**
	 * 得到单表数据的最大值
	 * 
	 * @param sqlObj
	 * @return 从不返回null，假如无数据返回0
	 * @throws EntityException OptionDBException 
	 *             假如数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	public Double max(SQL sqlObj);

	/**
	 * 得到单表数据的指定字段的最小值
	 * 
	 * @param beanClass
	 * @param minFieldName
	 *            要求最小值字段名称对应的bean的属性名称
	 * @return 从不返回null，假如无数据返回0
	 * @throws EntityException OptionDBException 
	 *             假如该对象不是实体或数据库访问出错时，抛出该异常
	 */
	public <T> Double min(Class<T> beanClass, String minFieldName);

	/**
	 * 得到单表数据的最小值
	 * 
	 * @param sqlObj
	 * @return 从不返回null，假如无数据返回0
	 * @throws EntityException OptionDBException 
	 *             假如数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	public Double min(SQL sqlObj);

	/**
	 * 得到单表数据的指定字段的平均值
	 * 
	 * @param beanClass
	 * @param avgFieldName
	 *            要求平均值字段名称对应的bean的属性名称
	 * @return 从不返回null，假如无数据返回0
	 * @throws EntityException OptionDBException 
	 *             假如该对象不是实体或数据库访问出错时，抛出该异常
	 */
	public <T> Double avg(Class<T> beanClass, String avgFieldName);

	/**
	 * 得到单表数据的平均值
	 * 
	 * @param sqlObj
	 * @return 从不返回null，假如无数据返回0
	 * @throws EntityException OptionDBException 
	 *             假如数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	public Double avg(SQL sqlObj);

	/**
	 * 得到单表数据的指定字段的和
	 * 
	 * @param beanClass
	 * @param sumFieldName
	 *            要求和段名称对应的bean的属性名称
	 * @return 从不返回null，假如无数据返回0
	 * @throws EntityException OptionDBException 
	 *             假如该对象不是实体或数据库访问出错时，抛出该异常
	 */
	public <T> Double sum(Class<T> beanClass, String sumFieldName);

	/**
	 * 得到单表数据的和
	 * 
	 * @param sqlObj
	 * @return 从不返回null，假如无数据返回0
	 * @throws EntityException OptionDBException 
	 *             假如数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	public Double sum(SQL sqlObj);
}
