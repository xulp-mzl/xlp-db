package org.xlp.db.tableoption.simple;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.xlp.db.exception.EntityException;
import org.xlp.db.page.Page;
import org.xlp.db.sql.AvgSQL;
import org.xlp.db.sql.CountSQL;
import org.xlp.db.sql.DeleteSQL;
import org.xlp.db.sql.InsertSQL;
import org.xlp.db.sql.MaxSQL;
import org.xlp.db.sql.MinSQL;
import org.xlp.db.sql.QuerySQL;
import org.xlp.db.sql.SQL;
import org.xlp.db.sql.SumSQL;
import org.xlp.db.sql.UpdateSQL;
import org.xlp.db.tableoption.OriginalResultSetOption;
import org.xlp.db.tableoption.executor.BatchUpdateExecutor;
import org.xlp.db.tableoption.executor.QueryExecutor;
import org.xlp.db.tableoption.executor.UpdateExecutor;
import org.xlp.db.tableoption.handlers.ArrayHandle;
import org.xlp.db.tableoption.handlers.ArrayListHandle;
import org.xlp.db.tableoption.handlers.DataSetHandle;
import org.xlp.db.tableoption.handlers.DefaultBeanHandle;
import org.xlp.db.tableoption.handlers.DefaultBeanListHandle;
import org.xlp.db.tableoption.handlers.DefaultBeanSetHandle;
import org.xlp.db.tableoption.handlers.MapHandle;
import org.xlp.db.tableoption.handlers.MapListHandle;
import org.xlp.db.tableoption.handlers.ScalarHandle;
import org.xlp.db.tableoption.handlers.result.DataSet;
import org.xlp.db.tableoption.key.CompoundPrimaryKey;
import org.xlp.db.utils.BeanUtil;
import org.xlp.db.utils.Constants;
import org.xlp.utils.XLPStringUtil;
import org.xlp.utils.collection.XLPCollectionUtil;

/**
 * 简化对数据库表的操作类（一般与注解一起使用）
 * <p>注：在使用此类时，须先调用XLPDBUtil.initDataSource()方法，否则可能会产生错误
 * <p>另外如果你的所创建的应用中已隐藏的调用了XLPDBUtil.initDataSource()方法，那么可以直接使用此类
 * 
 * @author 徐龙平
 *         <p>
 *         2017-5-24
 *         </p>
 * @version 3.0
 * 
 */
public final class DBSession{
	//日志记录
	private final static Logger LOGGER = Logger.getLogger(DBSession.class);
	
	private DBSession() {
		super();
	}

	/**
	 * 单例实现
	 */
	private static class DBSessionHolder{
		public static final DBSession INSTANCE = new DBSession();
	}
	
	/**
	 * 获得单例
	 * 
	 * @return
	 */
	public static DBSession newInstance(){
		return DBSessionHolder.INSTANCE;
	}
	
	/**
	 * 得到更新执行器,用来执行更新SQL语句
	 * 
	 * @return
	 */
	public static UpdateExecutor getUpdateExecutor(){
		return new UpdateExecutor() {
			@Override
			public int execute(String sql, Object... params) throws SQLException {
				return Constants.BASE_DB_OPTION.update(sql, params);
			}
			
			@Override
			public int execute(String sql) throws SQLException {
				return Constants.BASE_DB_OPTION.update(sql);
			}
		};
	}
	
	/**
	 * 得到批量更新执行器,用来批量执行更新SQL语句
	 * 
	 * @return
	 */
	public static BatchUpdateExecutor getBatchUpdateExecutor(){
		return new BatchUpdateExecutor() {
			@Override
			public int[] execute(String sql, Object[][] params) throws SQLException {
				return Constants.BASE_DB_OPTION.batch(sql, params);
			}
		};
	}
	
	/**
	 * 得到结果查询执行器
	 * 
	 * @return
	 */
	public static QueryExecutor getQueryExecutor(){
		return new QueryExecutor() {
			@Override
			public OriginalResultSetOption execute(String sql, Object... params)
					throws SQLException {
				OriginalResultSetOption rso = new OriginalResultSetOption();
				rso.query(sql, params);
				return rso;
			}
			@Override
			public OriginalResultSetOption execute(String sql) throws SQLException {
				OriginalResultSetOption rso = new OriginalResultSetOption();
				rso.query(sql);
				return rso;
			}
		};
	}
	
	/**
	 * 向数据库中插入一条数据
	 * <p>假如给定要存储的对象是实体，并有相关的主键，并主键值未指定，那么此函数会自动生成一个可用的主键值
	 * 
	 * @param bean
	 * @return 主键值，假如返回null时，可能数据保存失败，也可能指定的bean对象没有主键属性。
	 * 		    假如复合主键时，返回他们组成的数组
	 * @throws SQLException 假如该对象不是实体或数据库访问出错时，抛出该异常
	 * @throws EntityException 
	 */
	public <T> Object save(T bean) throws SQLException, EntityException {
		InsertSQL insertSQL = new InsertSQL(bean);
		
		//得到插入语句
		String sql = insertSQL.getParamSql();
		if (sql == null){
			LOGGER.error("传入的bean属性没有与数据库表列名相关联");
			throw new SQLException("传入的bean属性没有与数据库表列名相关联");
		}
		int count = Constants.BASE_DB_OPTION.update(sql, insertSQL.getParams());
		Object id = null;
		if (count > 0) {
			Object[] ids = insertSQL.getPrimaryKey().getCurrentValues();
			id = ids.length == 0 ? null : (ids.length == 1 ? ids[0] : ids);
//			BeanUtil.fillBeanKeys(bean, ids);
//			LOGGER.debug("数据保存成功！写入的数据是：\n" + bean.toString());
		}
		return id;
	}
	
	/**
	 * 通过SQL对象向数据库中插入一条数据
	 * 
	 * @param sqlObj SQL对象
	 * @return 受影响数据的条数
	 * @throws SQLException 假如数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 * 			假如参数为null，抛出该异常
	 */
	public int save(SQL sqlObj) throws SQLException{
		//得到插入语句
		String sql = sqlObj.getParamSql();
		if (sql == null){
			throw new SQLException("插入数据出错");
		}
		int count = Constants.BASE_DB_OPTION.update(sql, sqlObj.getParams());
		return count;
	}

	/**
	 * 得到单表数据的条数
	 * 
	 * @param beanClass
	 * @return
	 * @throws SQLException 假如该对象不是实体或数据库访问出错时，抛出该异常
	 * @throws EntityException 
	 */
	public <T> long count(Class<T> beanClass) throws SQLException, EntityException {
		CountSQL<T> countSQL = null;
		countSQL = new CountSQL<T>(beanClass);
		return count(countSQL);
	}
	
	/**
	 * 得到单表数据的条数
	 * 
	 * @param sqlObj
	 * @return 
	 * @throws SQLException 假如数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 * 			假如参数为null，抛出该异常
	 */
	public long count(SQL sqlObj) throws SQLException{
		String sql = sqlObj.getParamSql();
		Number number = (Number) Constants.BASE_DB_OPTION.query(sql, new ScalarHandle(), 
				sqlObj.getParams());
		return number == null ? 0l : number.longValue();
	}
	
	/**
	 * 通过SQL对象得到javabean对象
	 *
	 * @param sqlObj
	 * @return
	 * @throws SQLException 假如数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 * 			假如参数为null，抛出该异常
	 */
	@SuppressWarnings("unchecked")
	public <T> T find(SQL sqlObj) throws SQLException{
		String sql = sqlObj.getParamSql();
		
		return Constants.BASE_DB_OPTION.query(sql, 
				new DefaultBeanHandle<T>((Class<T>) sqlObj.getEntityClass()),
				sqlObj.getParams());
	}
	
	/**
	 * 通过 实体对象得到javabean对象
	 * 
	 * @param bean
	 *            实体对象,此对象一定要有相关的主属性，否则抛出SQL异常
	 * @return 假如参数为null，返回null，没有符合要求的数据也返回null
	 * @throws SQLException 假如该对象没要有相关的主属性或不是实体或数据库访问出错时，抛出该异常
	 * @throws EntityException 
	 */
	public <T> T find(T bean)throws SQLException, EntityException {
		if(bean == null)
			return null;
		QuerySQL<T> querySQL = new QuerySQL<T>(bean);
		//获取主键
		CompoundPrimaryKey cpk = querySQL.getPrimaryKey();
		//获取主键个数
		int keyCount = cpk.getCount();
		
		if(keyCount == 0)
			throw new SQLException(bean.getClass().getName() + "：该对象没有相关主键属性");
		//判断主键属性是否为空
		Object[] values = cpk.getValues();
		for (int i = 0; i < keyCount; i++) {
			if(values[i] == null || (cpk.isPrimitives()[i]
			            && "0".equals(values[i].toString())))
				throw new SQLException("主键属性值不能为null");
		}
		
		return find(querySQL);
	}

	/**
	 * 通过主键查找数据
	 * 
	 * @param beanClass 对象类型
	 * @param keyValues 主键属性值(单主键)
	 * @return 假如参数为null，返回null，没有符合要求的数据也返回null
	 * @throws SQLException 假如该对象类型对象不是实体或数据库访问出错时，抛出该异常
	 * @throws EntityException 
	 */
	public <T> T find(Class<T> beanClass, Object keyValue) 
		throws SQLException, EntityException{
		if(keyValue == null)
			return null;
		return find(beanClass, new Object[]{keyValue});
	}
	
	/**
	 * 通过主键查找数据
	 * 
	 * @param beanClass 对象类型
	 * @param keyValues 主键属性值数组(复合主键)
	 * @return 假如参数为null，返回null，没有符合要求的数据也返回null
	 * @throws SQLException 假如该对象没要有相关的主属性或不是实体或数据库访问出错时，抛出该异常
	 * @throws EntityException 
	 */
	public <T> T find(Class<T> beanClass, Object... keyValues) 
		throws SQLException, EntityException{
		if(beanClass == null || keyValues == null)
			return null;
		QuerySQL<T> querySQL = new QuerySQL<T>(beanClass);
		CompoundPrimaryKey primaryKey = querySQL.getPrimaryKey();
		int keyCount = querySQL.getPrimaryKey().getCount();
		if(keyCount == 0){
			LOGGER.error("数据查找过程中出错");
			throw new SQLException(beanClass.getName() + "：该对象没有相关主键属性");
		}
		if (keyCount != keyValues.length) {
			LOGGER.error("数据查找过程中出错");
			throw new SQLException("给的相关主键属性值的个数与实际相关属性主键个数不同");
		}
		
		for (int i = 0; i < keyCount; i++) //以主键值为查询条件
			querySQL.andEq(primaryKey.getNames()[i], keyValues[i]);
		return find(querySQL);
	}
	
	/**
	 * 通过单表得到javabean list集合
	 * 
	 * @param beanClass bean类型
	 * @return
	 * @throws SQLException 假如该对象不是实体或数据库访问出错时，抛出该异常
	 * @throws EntityException 
	 */
	public <T> List<T> list(Class<T> beanClass) throws SQLException, EntityException{
		QuerySQL<T> querySQL = new QuerySQL<T>(beanClass);
		return list(querySQL);
	}
	
	/**
	 * 通过SQL对象得到javabean list集合
	 *
	 * @param sqlObj
	 * @return
	 * @throws SQLException 假如数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 * 		假如参数为null，抛出该异常
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> list(SQL sqlObj) throws SQLException{
		String sql = sqlObj.getParamSql();
		return Constants.BASE_DB_OPTION.query(sql, new DefaultBeanListHandle<T>
			((Class<T>) sqlObj.getEntityClass()), sqlObj.getParams());
	}
	
	/**
	 * 通过单表得到javabean set集合
	 * 
	 * @param beanClass
	 * @return
	 * @throws SQLException 假如该对象不是实体或数据库访问出错时，抛出该异常
	 * @throws EntityException 
	 */
	public <T> Set<T> set(Class<T> beanClass) throws SQLException, EntityException{
		QuerySQL<T> querySQL = new QuerySQL<T>(beanClass);
		return set(querySQL);
	}
	
	/**
	 * 通过SQL对象得到javabean set集合
	 * 
	 * @param sqlObj
	 * @return
	 * @throws SQLException 假如数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 * 		假如参数为null，抛出该异常
	 */
	@SuppressWarnings("unchecked")
	public <T> Set<T> set(SQL sqlObj) throws SQLException{
		String sql = sqlObj.getParamSql();
		return Constants.BASE_DB_OPTION.query(sql, new DefaultBeanSetHandle<T>
			((Class<T>) sqlObj.getEntityClass()), sqlObj.getParams());
	}
	
	/**
	 * 删除指定bean类型对应的表中的所有数据
	 * 
	 * @param beanClass
	 * @return 假如返回true，删除数据成功，返回false，删除数据失败
	 * @throws SQLException 假如该对象不是实体或数据库访问出错时，抛出该异常
	 * @throws EntityException 
	 * @throws NullPointerException
	 * 		假如参数为null，抛出该异常
	 */
	public <T> boolean delete(Class<T> beanClass) throws SQLException, EntityException{
		return delete(BeanUtil.newInstance(beanClass));
	}
	
	/**
	 * 删除指定bean对应的表中的数据
	 * 
	 * @param bean
	 * @return 假如返回true，删除数据成功，返回false，删除数据失败
	 * @throws SQLException 假如该对象没要有相关的主属性或不是实体或数据库访问出错时，抛出该异常
	 * @throws EntityException 
	 * @throws NullPointerException 
	 * 		假如参数为null，抛出该异常 
	 */
	public <T> boolean delete(T bean) throws SQLException, EntityException{
		DeleteSQL<T> deleteSQL = new DeleteSQL<T>(bean);
		if(deleteSQL.getPrimaryKey().getCount() == 0)
			throw new SQLException(bean.getClass().getName() + "：该对象没有相关主键属性");
		
		return update(deleteSQL);
	}
	
	/**
	 * 根据SQL对象删除数
	 * 
	 * @param sql
	 * @return 假如返回true，删除数据成功，返回false，删除数据失败
	 * @throws SQLException 假如数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 * 		假如参数为null，抛出该异常 
	 */
	public boolean update(SQL sql) throws SQLException{
		int count = Constants.BASE_DB_OPTION.update(sql.getParamSql(), 
				sql.getParams());
		return count > 0 ? true : false;
	}
	
	/**
	 * 通过主键删除数据
	 * 
	 * @param beanClass 对象类型
	 * @param keyValues 主键属性值(单主键)
	 * @return 假如返回true，删除数据成功，返回false，删除数据失败
	 * @throws SQLException 假如该对象没要有相关的主属性或不是实体或数据库访问出错时，抛出该异常
	 * @throws EntityException 
	 */
	public <T> boolean delete(Class<T> beanClass, Object keyValue) 
		throws SQLException, EntityException{
		if(keyValue == null || beanClass == null)
			return false;
		return delete(beanClass, new Object[]{keyValue});
	}
	
	/**
	 * 通过主键删除数据(复合主键)
	 * 
	 * @param beanClass 对象类型
	 * @param keyValues 主键属性值数组(复合主键)
	 * @return 假如返回true，删除数据成功，返回false，删除数据失败
	 * @throws SQLException 假如该对象没要有相关的主属性或不是实体或数据库访问出错时，抛出该异常
	 * @throws EntityException 
	 */
	public <T> boolean delete(Class<T> beanClass, Object... keyValues) 
		throws SQLException, EntityException{
		if(beanClass == null || keyValues == null)
			return false;
		
		DeleteSQL<T> deleteSql = new DeleteSQL<T>(beanClass);
		CompoundPrimaryKey primaryKey = deleteSql.getPrimaryKey();
		int keyCount = deleteSql.getPrimaryKey().getCount();
		if(keyCount == 0){
			LOGGER.error("数据删除过程中出错");
			throw new SQLException(beanClass.getName() + "：该对象没有相关主键属性");
		}
		if (keyCount != keyValues.length) {
			LOGGER.error("数据删除过程中出错");
			throw new SQLException("给的相关主键属性值的个数与实际相关属性主键个数不同");
		}
		
		for (int i = 0; i < keyCount; i++) //以主键值为查询条件
			deleteSql.andEq(primaryKey.getNames()[i], keyValues[i]);
		return update(deleteSql);
	}
	
	/**
	 * 更新，指定的bean一定要主键属性 
	 * 
	 * @param bean
	 * @return 假如返回true，更新数据成功，返回false，更新数据失败
	 * @throws SQLException 假如该对象没要有相关的主属性或不是实体或数据库访问出错时，抛出该异常
	 * @throws EntityException 
	 */
	public <T> boolean update(T bean) throws SQLException, EntityException{
		if (bean == null) 
			return false;
		UpdateSQL<T> updateSQL = new UpdateSQL<T>(bean);
		if(updateSQL.getPrimaryKey().getCount() == 0){
			LOGGER.error(bean.getClass().getName() + "：该对象没有相关主键属性");
			throw new SQLException(bean.getClass().getName() + "：该对象没有相关主键属性");
		}
		return update(updateSQL);
	}
	
	/**
	 * 通过bean对象获取新的bean对象
	 * <p>(E:Enhance 增强)
	 * @param bean 
	 * @return 假如bean对象属性中有别的bean对象或别的bean对象集合。
	 * 			<p>调用这个函数都会封装，如果没有则与find(T bean)的效果相同。
	 * @throws SQLException 假如该对象没要有相关的主属性或不是实体或数据库访问出错时，抛出该异常
	 * @throws EntityException 
	 */
	public <T> T findE(T bean) throws SQLException, EntityException{
		if(bean == null)
			return null;
		
		return new EntityAttributeWrapper<T>(bean).createNewEntity();
	}
	
	/**
	 * 通过SQL对象获取dataset对象
	 * 
	 * @param sql
	 * @return 从不返回null
	 * @throws SQLException 
	 * 			假如数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 * 		            假如参数为null，抛出该异常
	 */
	public DataSet findDataSet(SQL sql) throws SQLException{
		return Constants.BASE_DB_OPTION.query(sql.getParamSql(),
				new DataSetHandle(), sql.getParams());
	}
	
	/**
	 * 把数据处理成数组
	 * 
	 * @param sql
	 * @return 从不返回null, 如无数据返回大小为0的数组
	 * @throws SQLException 
	 * 			假如数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 * 		            假如参数为null，抛出该异常
	 */
	public Object[] array(SQL sql) throws SQLException{
		return Constants.BASE_DB_OPTION.query(sql.getParamSql(), 
				new ArrayHandle(), sql.getParams());
	}
	
	/**
	 * 把数据处理成List<Object[]>集合
	 * 
	 * @param sql
	 * @return 从不返回null, 如无数据返回大小为0的集合
	 * @throws SQLException 
	 * 			假如数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 * 		            假如参数为null，抛出该异常
	 */
	public List<Object[]> listArray(SQL sql) throws SQLException{
		return Constants.BASE_DB_OPTION.query(sql.getParamSql(),
				new ArrayListHandle(), sql.getParams());
	}
	
	/**
	 * 把数据处理成Map<String, Object>集合
	 * 
	 * @param sql
	 * @return 从不返回null, 如无数据返回大小为0的集合
	 * @throws SQLException 
	 * 			假如数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 * 		            假如参数为null，抛出该异常
	 */
	public Map<String, Object> map(SQL sql) throws SQLException{
		return Constants.BASE_DB_OPTION.query(sql.getParamSql(),
				new MapHandle(), sql.getParams());
	}
	
	/**
	 * 把数据处理成List<Map<String, Object>>集合
	 * 
	 * @param sql
	 * @return 从不返回null, 如无数据返回大小为0的集合
	 * @throws SQLException 
	 * 			假如数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 * 		            假如参数为null，抛出该异常
	 */
	public List<Map<String, Object>> listMap(SQL sql) throws SQLException{
		return Constants.BASE_DB_OPTION.query(sql.getParamSql(),
				new MapListHandle(), sql.getParams());
	}
	
	/**
	 * 向数据库中批量插入数据
	 * <p>
	 * 假如给定要存储的对象是实体，并有相关的主键，并主键值未指定，那么此函数会自动生成一个可用的主键值
	 * 
	 * @param beanList
	 * @return 数据改变条数, 假如参数为null或大小为0，则返回int[0]
	 * @throws SQLException
	 * @throws EntityException 
	 */
	public <T> int[] batchSave(List<T> beanList) throws SQLException, EntityException {
		if (XLPCollectionUtil.isEmpty(beanList))
			return new int[0];
		
		//存储beanList值不为null的下标
		List<Integer> indexList = new ArrayList<Integer>();
		int i = 0;
		for (T bean0 : beanList) {
			if (bean0 != null)
				indexList.add(i);
			i++;
		}
		
		int count = indexList.size();
		if (count == 0) //判断是否有要插入的数据
			return new int[0];
			
		Object[][] params = new Object[count][];
		InsertSQL insertSQL = new InsertSQL(beanList.get(indexList.get(0)));
		// 得到插入语句
		String sql = insertSQL.getParamSql();
		if (sql == null) {
			LOGGER.error("传入的bean属性没有与数据库表列名相关联");
			throw new SQLException("传入的bean属性没有与数据库表列名相关联");
		}
		
		params[0] = insertSQL.getParams();
		for (i = 1; i < count; i++) {
			insertSQL = new InsertSQL(beanList.get(indexList.get(i))); 
			params[i] = insertSQL.getParams();
		}

		int[] changed = DBSession.getBatchUpdateExecutor().execute(
				sql, params);
		return changed;
	}
	
	/**
	 * 分页查询，暂时只对mysql有效
	 * 
	 * @param sql
	 *            SQL对象， 该对象中应不包含limit条件，否则可能抛出异常
	 * @param page
	 *            分页对象
	 * @return 返回完整分页对象
	 * @throws SQLException
	 *             假如数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 *             假如参数为null，抛出该异常
	 */
	@SuppressWarnings("unchecked")
	public <T> Page<T> searchPage(SQL sql, Page<T> page) throws SQLException{
		String sql1 = sql.getParamSql().toLowerCase().trim();
		String countSql;
		if (XLPStringUtil.startsWith(sql1, "select[\\s]+distinct[\\s]+")
				|| XLPStringUtil.startsWith(sql1, "select[\\s]+distinct[*]")) {  
			countSql = "select count(*) from (" + sql1 + ") t"; 
		}else {
			int index = XLPStringUtil.indexOf(sql1, "([*]|([\\s]+))from(([\\s]+)|[(])");
			//		sql1.indexOf("from");
			int orderByIndex = XLPStringUtil.indexOf(sql1, "(([\\s]+)|[)])+order[\\s]+by[\\s]+");
			//sql1.indexOf("order[\\s]+by");
			countSql = "select count(*) ";
			if(orderByIndex != -1)
				countSql += sql1.substring(index + 1, orderByIndex + 1);
			else
				countSql += sql1.substring(index + 1);
		}
		
		Number number = (Number) Constants.BASE_DB_OPTION.query(countSql, new ScalarHandle(),
				sql.getParams());
		long count = number == null ? 0l : number.longValue();
		page.setTotalCount(count);//获取总数据条数
		
		if(count <= (page.getCurrentNo() - 1) * page.getPageSize())//判断总数是否大于当前页面之前的总数
			page.setCurrentNo(1); //假如小于，则重新设置当前页码值为1
		sql1 += " limit " + ((page.getCurrentNo() - 1) * page.getPageSize()) + "," + page.getPageSize();
		List<T> datas = Constants.BASE_DB_OPTION.query(sql1, 
				new DefaultBeanListHandle<T>((Class<T>) sql.getEntityClass()), sql.getParams());
		page.setDatas(datas);
		return page;
	}
	
	/**
	 * 得到单表数据的指定字段的最大值
	 * 
	 * @param beanClass
	 * @param maxFieldName
	 *            要求最大值字段名称对应的bean的属性名称
	 * @return 从不返回null，假如无数据返回0
	 * @throws SQLException 假如该对象不是实体或数据库访问出错时，抛出该异常
	 * @throws EntityException 
	 */
	public <T> Double max(Class<T> beanClass, String maxFieldName) throws SQLException, EntityException {
		MaxSQL<T> maxSQL = null;
		maxSQL = new MaxSQL<T>(beanClass, maxFieldName);
		return max(maxSQL);	
	}
	
	/**
	 * 得到单表数据的最大值
	 * 
	 * @param sqlObj
	 * @return 从不返回null，假如无数据返回0
	 * @throws SQLException 假如数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 * 			假如参数为null，抛出该异常
	 */
	public Double max(SQL sqlObj) throws SQLException{
		String sql = sqlObj.getParamSql();
		Number number = (Number) Constants.BASE_DB_OPTION.query(sql, new ScalarHandle(), 
				sqlObj.getParams());
		return number == null ? Double.valueOf(0.0) : Double.valueOf(number.doubleValue());
	}
	
	/**
	 * 得到单表数据的指定字段的最小值
	 * 
	 * @param beanClass
	 * @param minFieldName
	 *            要求最小值字段名称对应的bean的属性名称
	 * @return 从不返回null，假如无数据返回0
	 * @throws SQLException 假如该对象不是实体或数据库访问出错时，抛出该异常
	 * @throws EntityException 
	 */
	public <T> Double min(Class<T> beanClass, String minFieldName) throws SQLException, EntityException {
		MinSQL<T> minSQL = null;
		minSQL = new MinSQL<T>(beanClass, minFieldName);
		return min(minSQL);	
	}
	
	/**
	 * 得到单表数据的最小值
	 * 
	 * @param sqlObj
	 * @return 从不返回null，假如无数据返回0
	 * @throws SQLException 假如数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 * 			假如参数为null，抛出该异常
	 */
	public Double min(SQL sqlObj) throws SQLException{
		return max(sqlObj);
	}
	
	/**
	 * 得到单表数据的指定字段的平均值
	 * 
	 * @param beanClass
	 * @param avgFieldName
	 *            要求平均值字段名称对应的bean的属性名称
	 * @return 从不返回null，假如无数据返回0
	 * @throws SQLException 假如该对象不是实体或数据库访问出错时，抛出该异常
	 * @throws EntityException 
	 */
	public <T> Double avg(Class<T> beanClass, String avgFieldName) throws SQLException, EntityException {
		AvgSQL<T> avgSQL = null;
		avgSQL = new AvgSQL<T>(beanClass, avgFieldName);
		return avg(avgSQL);	
	}
	
	/**
	 * 得到单表数据的平均值
	 * 
	 * @param sqlObj
	 * @return 从不返回null，假如无数据返回0
	 * @throws SQLException 假如数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 * 			假如参数为null，抛出该异常
	 */
	public Double avg(SQL sqlObj) throws SQLException{
		return max(sqlObj);
	}
	
	/**
	 * 得到单表数据的指定字段的和
	 * 
	 * @param beanClass
	 * @param sumFieldName
	 *            要求和段名称对应的bean的属性名称
	 * @return 从不返回null，假如无数据返回0
	 * @throws SQLException 假如该对象不是实体或数据库访问出错时，抛出该异常
	 * @throws EntityException 
	 */
	public <T> Double sum(Class<T> beanClass, String sumFieldName) throws SQLException, EntityException {
		SumSQL<T> sumSQL = null;
		sumSQL = new SumSQL<T>(beanClass, sumFieldName);
		return sum(sumSQL);	
	}
	
	/**
	 * 得到单表数据的和
	 * 
	 * @param sqlObj
	 * @return 从不返回null，假如无数据返回0
	 * @throws SQLException 假如数据库访问出错时，抛出该异常
	 * @throws NullPointerException
	 * 			假如参数为null，抛出该异常
	 */
	public Double sum(SQL sqlObj) throws SQLException{
		return max(sqlObj);
	}
}
