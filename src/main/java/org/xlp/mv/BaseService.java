package org.xlp.mv;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xlp.db.exception.EntityException;
import org.xlp.db.page.Page;
import org.xlp.db.sql.SQL;
import org.xlp.db.sql.limit.Limit;
import org.xlp.db.tableoption.handlers.result.DataSet;
import org.xlp.utils.XLPArrayUtil;

/**
 * 
 * @author 徐龙平
 * 
 */
public class BaseService {
	protected final static BaseDao BASEDAO = new BaseDao();

	/**
	 * 向数据库中插入一条数据
	 * <p>
	 * 假如给定要存储的对象是实体，并有相关的主键，并主键值未指定，那么此函数会自动生成一个可用的主键值
	 * 
	 * @param bean
	 * @return 主键值，假如返回null时，可能数据保存失败，也可能指定的bean对象没有主键属性。 假如复合主键时，返回他们组成的数组
	 * @throws EntityException
	 *             假如该对象不是实体或数据库访问出错时，抛出该异常
	 */
	public <T> Object save(T bean) throws EntityException {
		return BASEDAO.save(bean);
	}

	/**
	 * 向数据库中插入多条数据
	 * <p>
	 * 假如给定要存储的对象是实体，并有相关的主键，并主键值未指定，那么此函数会自动生成一个可用的主键值
	 * 
	 * @param beanList
	 *            数据集合
	 * @return 假如参数为null或保存失败，返回false，否则返回true
	 * @throws EntityException
	 *             假如该对象不是实体或数据库访问出错时，抛出该异常
	 */
	public <T> boolean save(List<T> beanList) throws EntityException {
		return BASEDAO.save(beanList);
	}

	/**
	 * 向数据库中插入多条数据
	 * <p>
	 * 假如给定要存储的对象是实体，并有相关的主键，并主键值未指定，那么此函数会自动生成一个可用的主键值
	 * 
	 * @param beanArray
	 *            数据数组
	 * @return 假如参数为null或保存失败，返回false，否则返回true
	 * @throws EntityException
	 *             假如该对象不是实体或数据库访问出错时，抛出该异常
	 */
	public <T> boolean save(T[] beanArray) throws EntityException {
		if (XLPArrayUtil.isEmpty(beanArray))
			return false;
		return BASEDAO.save(Arrays.asList(beanArray));
	}

	/**
	 * 通过SQL对象向数据库中插入一条数据
	 * 
	 * @param sqlObj
	 *            SQL对象
	 * @return 受影响数据的条数
	 * @throws EntityException
	 *             假如数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	public int save(SQL sqlObj) throws EntityException {
		return BASEDAO.save(sqlObj);
	}

	/**
	 * 得到单表数据的条数
	 * 
	 * @param beanClass
	 * @return
	 * @throws EntityException
	 *             假如该对象不是实体或数据库访问出错时，抛出该异常
	 */
	public <T> long count(Class<T> beanClass) throws EntityException {
		return BASEDAO.count(beanClass);
	}

	/**
	 * 得数据的条数
	 * 
	 * @param sqlObj
	 * @return
	 * @throws EntityException
	 *             假如数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	public long count(SQL sqlObj) throws EntityException {
		return BASEDAO.count(sqlObj);
	}

	/**
	 * 通过SQL对象得到javabean对象
	 * 
	 * @param sqlObj
	 * @return
	 * @throws EntityException
	 *             假如数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	public <T> T find(SQL sqlObj) throws EntityException {
		return BASEDAO.find(sqlObj);
	}

	/**
	 * 通过 实体对象得到javabean对象
	 * 
	 * @param bean
	 *            实体对象,此对象一定要有相关的主属性，否则抛出SQL异常
	 * @return 假如参数为null，返回null，没有符合要求的数据也返回null
	 * @throws EntityException
	 *             假如该对象没要有相关的主属性或不是实体或数据库访问出错时，抛出该异常
	 */
	public <T> T find(T bean) throws EntityException {
		return BASEDAO.find(bean);
	}

	/**
	 * 通过主键查找数据
	 * 
	 * @param beanClass
	 *            对象类型
	 * @param keyValues
	 *            主键属性值(单主键)
	 * @return 假如参数为null，返回null，没有符合要求的数据也返回null
	 * @throws EntityException
	 *             假如该对象类型对象不是实体或数据库访问出错时，抛出该异常
	 */
	public <T> T find(Class<T> beanClass, Object keyValue) throws EntityException{
		return BASEDAO.find(beanClass, keyValue);
	}

	/**
	 * 通过主键查找数据
	 * 
	 * @param beanClass
	 *            对象类型
	 * @param keyValues
	 *            主键属性值数组(复合主键)
	 * @return 假如参数为null，返回null，没有符合要求的数据也返回null
	 * @throws EntityException
	 *             假如该对象没要有相关的主属性或不是实体或数据库访问出错时，抛出该异常
	 */
	public <T> T find(Class<T> beanClass, Object... keyValues) throws EntityException {
		return BASEDAO.find(beanClass, keyValues);
	}

	/**
	 * 通过单表得到javabean list集合
	 * 
	 * @param beanClass
	 *            bean类型
	 * @return
	 * @throws EntityException
	 *             假如该对象不是实体或数据库访问出错时，抛出该异常
	 */
	public <T> List<T> list(Class<T> beanClass) throws EntityException {
		return BASEDAO.list(beanClass);
	}

	/**
	 * 通过SQL对象得到javabean list集合
	 * 
	 * @param sqlObj
	 * @return
	 * @throws EntityException
	 *             假如数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	public <T> List<T> list(SQL sqlObj) throws EntityException {
		return BASEDAO.list(sqlObj);
	}

	/**
	 * 通过单表得到javabean set集合
	 * 
	 * @param beanClass
	 * @return
	 * @throws EntityException
	 *             假如该对象不是实体或数据库访问出错时，抛出该异常
	 */
	public <T> Set<T> set(Class<T> beanClass) throws EntityException {
		return BASEDAO.set(beanClass);
	}

	/**
	 * 通过SQL对象得到javabean set集合
	 * 
	 * @param sqlObj
	 * @return
	 * @throws EntityException
	 *             假如数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	public <T> Set<T> set(SQL sqlObj) throws EntityException{
		return BASEDAO.set(sqlObj);
	}

	/**
	 * 删除指定bean类型对应的表中的所有数据
	 * 
	 * @param beanClass
	 * @return 假如返回true，删除数据成功，返回false，删除数据失败
	 * @throws EntityException
	 *             假如该对象不是实体或数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	public <T> boolean delete(Class<T> beanClass) throws EntityException {
		return BASEDAO.delete(beanClass);
	}

	/**
	 * 删除指定bean对应的表中的数据
	 * 
	 * @param bean
	 * @return 假如返回true，删除数据成功，返回false，删除数据失败
	 * @throws EntityException
	 *             假如该对象没要有相关的主属性或不是实体或数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	public <T> boolean delete(T bean) throws EntityException {
		return BASEDAO.delete(bean);
	}

	/**
	 * 根据SQL对象删除数
	 * 
	 * @param sql
	 * @return 假如返回true，删除数据成功，返回false，删除数据失败
	 * @throws EntityException
	 *             假如数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	public boolean update(SQL sql) throws EntityException{
		return BASEDAO.update(sql);
	}

	/**
	 * 通过主键删除数据
	 * 
	 * @param beanClass
	 *            对象类型
	 * @param keyValues
	 *            主键属性值(单主键)
	 * @return 假如返回true，删除数据成功，返回false，删除数据失败
	 * @throws EntityException
	 *             假如该对象没要有相关的主属性或不是实体或数据库访问出错时，抛出该异常
	 */
	public <T> boolean delete(Class<T> beanClass, Object keyValue) throws EntityException{
		return BASEDAO.delete(beanClass, keyValue);
	}

	/**
	 * 通过主键删除数据(复合主键)
	 * 
	 * @param beanClass
	 *            对象类型
	 * @param keyValues
	 *            主键属性值数组(复合主键)
	 * @return 假如返回true，删除数据成功，返回false，删除数据失败
	 * @throws EntityException
	 *             假如该对象没要有相关的主属性或不是实体或数据库访问出错时，抛出该异常
	 */
	public <T> boolean delete(Class<T> beanClass, Object... keyValues) throws EntityException{
		return BASEDAO.delete(beanClass, keyValues);
	}

	/**
	 * 更新，指定的bean一定要主键属性
	 * 
	 * @param bean
	 * @return 假如返回true，更新数据成功，返回false，更新数据失败
	 * @throws EntityException
	 *             假如该对象没要有相关的主属性或不是实体或数据库访问出错时，抛出该异常
	 */
	public <T> boolean update(T bean) throws EntityException {
		return BASEDAO.update(bean);
	}

	/**
	 * 通过bean对象获取新的bean对象
	 * <p>
	 * (E:Enhance 增强)
	 * 
	 * @param bean
	 * @return 假如bean对象属性中有别的bean对象或别的bean对象集合。
	 *         <p>
	 *         调用这个函数都会封装，如果没有则与find(T bean)的效果相同。
	 * @throws EntityException
	 *             假如该对象没要有相关的主属性或不是实体或数据库访问出错时，抛出该异常
	 */
	public <T> T findE(T bean) throws EntityException {
		return BASEDAO.findE(bean);
	}

	/**
	 * 通过SQL对象获取dataset对象
	 * 
	 * @param sql
	 * @return 从不返回null
	 * @throws EntityException
	 *             假如数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	public DataSet findDataSet(SQL sql) throws EntityException {
		return BASEDAO.findDataSet(sql);
	}

	/**
	 * 把数据处理成数组
	 * 
	 * @param sql
	 * @return 从不返回null, 如无数据返回大小为0的数组
	 * @throws EntityException
	 *             假如数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	public Object[] array(SQL sql) throws EntityException {
		return BASEDAO.array(sql);
	}

	/**
	 * 把数据处理成List<Object[]>集合
	 * 
	 * @param sql
	 * @return 从不返回null, 如无数据返回大小为0的集合
	 * @throws EntityException
	 *             假如数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	public List<Object[]> listArray(SQL sql) throws EntityException{
		return BASEDAO.listArray(sql);
	}

	/**
	 * 把数据处理成Map<String, Object>集合
	 * 
	 * @param sql
	 * @return 从不返回null, 如无数据返回大小为0的集合
	 * @throws EntityException
	 *             假如数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	public Map<String, Object> map(SQL sql) throws EntityException{
		return BASEDAO.map(sql);
	}

	/**
	 * 把数据处理成List<Map<String, Object>>集合
	 * 
	 * @param sql
	 * @return 从不返回null, 如无数据返回大小为0的集合
	 * @throws EntityException
	 *             假如数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	public List<Map<String, Object>> listMap(SQL sql) throws EntityException{
		return BASEDAO.listMap(sql);
	}

	/**
	 * 分页查询，暂时只对mysql有效
	 * 
	 * @param sql
	 *            SQL对象， 该对象中应不包含limit条件，否则可能抛出异常
	 * @param page
	 *            分页对象
	 * @return 返回完整分页对象
	 * @throws EntityException
	 *             假如数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	public <T> Page<T> searchPage(SQL sql, Page<T> page) throws EntityException{
		return BASEDAO.searchPage(sql, page);
	}

	/**
	 * 分页查询，暂时只对mysql有效
	 * 
	 * @param sql
	 *            SQL对象， 该对象中应不包含limit条件，否则可能抛出异常
	 * @return 返回默认分页的首页对象
	 * @throws EntityException
	 *             假如数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	public <T> Page<T> searchPage(SQL sql) throws EntityException {
		return this.searchPage(sql, new Page<T>());
	}

	/**
	 * 分页查询，暂时只对mysql有效
	 * 
	 * @param sql
	 *            SQL对象， 该对象中应不包含limit条件，否则可能抛出异常
	 * @param limit
	 *            <code>Limit</code>对象
	 * @return 返回默认分页对象
	 * @throws EntityException 
	 * @throws <code>EntityException</code> 假如数据库访问出错时，抛出该异常
	 * @throws <code>NullPointerException</code> 假如参数为null，抛出该异常
	 */
	public <T> Page<T> searchPage(SQL sql, Limit limit) throws EntityException  {
		return this.searchPage(sql, new Page<T>((int) limit.getStartPos() + 1,
				(int) limit.getResultCount()));
	}

	/**
	 * 分页查询，暂时只对mysql有效
	 * 
	 * @param sql
	 *            SQL对象， 该对象中应不包含limit条件，否则可能抛出异常
	 * @param start
	 *            分页开始号码，从1开始
	 * @param size
	 *            分页大小
	 * @return 返回默认分页对象
	 * @throws EntityException
	 * @throws <code>EntityException</code> 假如数据库访问出错时，抛出该异常
	 * @throws <code>NullPointerException</code> 假如参数为null，抛出该异常
	 */
	public <T> Page<T> searchPage(SQL sql, int start, int size) throws EntityException{
		return this.searchPage(sql, new Page<T>(start, size));
	}
	
	/**
	 * 得到单表数据的指定字段的最大值
	 * 
	 * @param beanClass
	 * @param maxFieldName
	 *            要求最大值字段名称对应的bean的属性名称
	 * @return 从不返回null，假如无数据返回0
	 * @throws EntityException 
	 */
	public <T> Double max(Class<T> beanClass, String maxFieldName) throws EntityException {
		return BASEDAO.max(beanClass, maxFieldName);
	}

	/**
	 * 得到单表数据的最大值
	 * 
	 * @param sqlObj
	 * @return 从不返回null，假如无数据返回0
	 * @throws EntityException 
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	public Double max(SQL sqlObj) throws EntityException {
		return BASEDAO.max(sqlObj);
	}

	/**
	 * 得到单表数据的指定字段的最小值
	 * 
	 * @param beanClass
	 * @param minFieldName
	 *            要求最小值字段名称对应的bean的属性名称
	 * @return 从不返回null，假如无数据返回0
	 * @throws EntityException 
	 */
	public <T> Double min(Class<T> beanClass, String minFieldName) throws EntityException {
		return BASEDAO.min(beanClass, minFieldName);
	}

	/**
	 * 得到单表数据的最小值
	 * 
	 * @param sqlObj
	 * @return 从不返回null，假如无数据返回0
	 * @throws EntityException 
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	public Double min(SQL sqlObj) throws EntityException {
		return BASEDAO.min(sqlObj);
	}

	/**
	 * 得到单表数据的指定字段的平均值
	 * 
	 * @param beanClass
	 * @param avgFieldName
	 *            要求平均值字段名称对应的bean的属性名称
	 * @return 从不返回null，假如无数据返回0
	 * @throws EntityException 
	 */
	public <T> Double avg(Class<T> beanClass, String avgFieldName) throws EntityException {
		return BASEDAO.avg(beanClass, avgFieldName);
	}

	/**
	 * 得到单表数据的平均值
	 * 
	 * @param sqlObj
	 * @return 从不返回null，假如无数据返回0
	 * @throws EntityException
	 *             假如数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	public Double avg(SQL sqlObj) throws EntityException {
		return BASEDAO.avg(sqlObj);
	}

	/**
	 * 得到单表数据的指定字段的和
	 * 
	 * @param beanClass
	 * @param sumFieldName
	 *            要求和段名称对应的bean的属性名称
	 * @return 从不返回null，假如无数据返回0
	 * @throws EntityException 
	 */
	public <T> Double sum(Class<T> beanClass, String sumFieldName) throws EntityException {
		return BASEDAO.sum(beanClass, sumFieldName);
	}

	/**
	 * 得到单表数据的和
	 * 
	 * @param sqlObj
	 * @return 从不返回null，假如无数据返回0
	 * @throws EntityException 
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	public Double sum(SQL sqlObj) throws EntityException {
		return BASEDAO.sum(sqlObj);
	}
}
