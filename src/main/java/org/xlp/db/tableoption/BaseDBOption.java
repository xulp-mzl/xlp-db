package org.xlp.db.tableoption;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.xlp.db.tableoption.handlers.ResultSetHandle;
import org.xlp.db.utils.XLPDBUtil;

/**
 * 对数据库表的操作类
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
public final class BaseDBOption extends AbstractDBOption {
	private final static Logger LOGGER = Logger.getLogger(BaseDBOption.class);
	private BaseDBOption() {
		super();
	}

	/**
	 * 获得单例
	 * 
	 * @return
	 */
	public static BaseDBOption newInstance(){
		return BaseDBOptionHolder.INSTANCE;
	}
	
	/**
	 * 批处理更新数据库如update，delete，insert into等操作
	 * 
	 * @param conn 数据库连接
	 * @param closeConn 标记是否关闭连接
	 * @param sql 预处理的sql语句
	 * @param params 预处理参数，假如不需要此参数，请给出一个大小为0的数组，不要填null
	 * @return
	 * @throws SQLException 假如数据库连接出错或参数为空，则抛出此异常
	 */
	private int[] batch(Connection conn, boolean closeConn, String sql,
			Object[][] params) throws SQLException {
		paramsProcesser(conn, closeConn, sql, (Object[])params);
		LOGGER.info("正在批量执行的sql语句是：" + sql);
		LOGGER.info("对应的预处理参数是：" + Arrays.deepToString(params));
		PreparedStatement ps = null;
		int[] updateRows = null;
		try{
			ps = preparedStatement(conn, sql);
			for (int i = 0; i < params.length; i++) {
				this.fillPreparedStatement(ps, params[i]);
				ps.addBatch();
			}
			updateRows = ps.executeBatch();
		}catch (SQLException e) {
			LOGGER.error("在执行批量数据操作时产生异常，异常原因如下：\n",e);
			this.rethrow(e, sql, (Object[])params);
		}finally{
			if (closeConn) {
				XLPDBUtil.closeConnAndStatemet(conn, ps);
			}else {
				XLPDBUtil.closeStatement(ps);
			}
		}
		return updateRows;
	}
	
	/**
	 * 批处理更新数据库如update，delete，insert into等操作
	 * 
	 * @param conn 数据库连接
	 * @param sql 预处理的sql语句
	 * @param params 预处理参数，假如不需要此参数，请给出一个大小为0的数组，不要填null
	 * @return
	 * @throws SQLException 假如数据库连接出错或参数为空，则抛出此异常
	 */
	public int[] batch(Connection conn, String sql,Object[][] params) 
			throws SQLException {
		return this.batch(conn, false, sql, params);
	}
	
	/**
	 * 批处理更新数据库如update，delete，insert into等操作
	 * 
	 * @param sql 预处理的sql语句
	 * @param params 预处理参数，假如不需要此参数，请给出一个大小为0的数组，不要填null
	 * @return
	 * @throws SQLException 假如数据库连接出错或参数为空，则抛出此异常
	 */
	public int[] batch(String sql,Object[][] params)throws SQLException {
		Connection conn = XLPDBUtil.getConnection();
		return this.batch(conn, true, sql, params);
	}
	
	/**
     * 查询操作
     * 
     * @param conn 数据库连接
     * @param closeConn 标记是否关闭连接
	 * @param sql 预处理的sql语句
	 * @param rsh 查询结果处理器
     * @param params 预处理参数 
     * @return 查询结果
     * @throws SQLException 假如参数为null或数据库访问出错，抛出此异常
     */
    private <T> T query(Connection conn, boolean closeConn, String sql, ResultSetHandle<T> rsh, 
    		Object... params) throws SQLException {
    	nullProcesser(conn, closeConn, sql);
		
    	if (rsh == null) {
    		if (closeConn) {
				XLPDBUtil.close(conn);
			}
    		LOGGER.error("查询结果处理器为空！");
			throw new SQLException("查询结果处理器为空！");
		}
    	LOGGER.info("正在执行的查询sql语句是：" + sql);
    	params = (params == null ? new Object[]{} : params);
		LOGGER.info("对应的预处理参数是：" + Arrays.toString(params));
    	PreparedStatement ps = null;
    	ResultSet rs = null;
    	T result = null;
    	
    	try {
			ps = preparedStatement(conn, sql);
			this.fillPreparedStatement(ps, params);
			rs = ps.executeQuery();
			result = rsh.handle(rs);
		} catch (SQLException e) {
			LOGGER.error("在执行查询操作时产生异常，异常原因如下：\n",e);
			this.rethrow(e, sql, params);
		}finally{
			if (closeConn) {
				XLPDBUtil.closeAll(conn, ps, rs);
			}else {
				XLPDBUtil.closeStatemebtAndResultSet(ps, rs);
			}
		}
    	
    	return result;
    }

    /**
     * 查询操作
     * 
     * @param conn 数据库连接
	 * @param sql 预处理的sql语句
	 * @param rsh 查询结果处理器
     * @param param 预处理参数
     * @return 查询结果
     * @throws SQLException 假如参数为null或数据库访问出错，抛出此异常
     */
    public <T> T query(Connection conn, String sql, ResultSetHandle<T> rsh, 
    		Object param) throws SQLException {
    	Object[] params = null;
    	if (param != null) {
			params = new Object[]{param};
		}
    	return query(conn, false, sql, rsh, params);
    }
    
    /**
     * 查询操作
     * 
     * @param conn 数据库连接
	 * @param sql 预处理的sql语句
	 * @param rsh 查询结果处理器
     * @param params 预处理参数
     * @return 查询结果
     * @throws SQLException 假如参数为null或数据库访问出错，抛出此异常
     */
    public <T> T query(Connection conn, String sql, ResultSetHandle<T> rsh, 
    		Object... params) throws SQLException {
    	return query(conn, false, sql, rsh, params);
    }
    
    /**
     * 查询操作
     * 
     * @param conn 数据库连接
	 * @param sql 预处理的sql语句
	 * @param rsh 查询结果处理器
     * @return 查询结果
     * @throws SQLException 假如参数为null或数据库访问出错，抛出此异常
     */
    public <T> T query(Connection conn, String sql, ResultSetHandle<T> rsh) 
    		throws SQLException {
    	return query(conn, false, sql, rsh, (Object[])null);
    }
    
    /**
     * 查询操作
     * 
	 * @param sql 预处理的sql语句
	 * @param rsh 查询结果处理器
     * @return 查询结果
     * @throws SQLException 假如参数为null或数据库访问出错，抛出此异常
     */
    public <T> T query(String sql, ResultSetHandle<T> rsh)throws SQLException {
    	Connection conn = XLPDBUtil.getConnection();
    	return query(conn, true, sql, rsh, (Object[])null);
    }
    
    /**
     * 查询操作
     * 
	 * @param sql 预处理的sql语句
	 * @param rsh 查询结果处理器
     * @param params 预处理参数
     * @return 查询结果
     * @throws SQLException 假如参数为null或数据库访问出错，抛出此异常
     */
    public <T> T query(String sql, ResultSetHandle<T> rsh, Object... params) 
    		throws SQLException {
    	Connection conn = XLPDBUtil.getConnection();
    	return query(conn, true, sql, rsh, params);
    }
    
    /**
     * 查询操作
     * 
	 * @param sql 预处理的sql语句
	 * @param rsh 查询结果处理器
     * @param param 预处理参数
     * @return 查询结果
     * @throws SQLException 假如参数为null或数据库访问出错，抛出此异常
     */
    public <T> T query(String sql, ResultSetHandle<T> rsh, Object param) 
    		throws SQLException {
    	Connection conn = XLPDBUtil.getConnection();
    	Object[] params = null;
    	if (param != null) {
			params = new Object[]{param};
		}
    	return query(conn, true, sql, rsh, params);
    }
    
    /**
     * 更新数据库如（delete，update，insert into等操作）
     * 
     * @param conn 数据库连接
     * @param closeConn 标志是否关闭连接
     * @param sql 预处理的sql语句
     * @param params 预处理参数
     * @return 更新的条数
     * @throws SQLException 假如参数为null或数据库访问出错，抛出此异常
     */
    private int update(Connection conn, boolean closeConn, String sql,
    		Object... params) throws SQLException {
    	nullProcesser(conn, closeConn, sql);
    	LOGGER.info("正在执行的更新sql语句是：" + sql);
    	params = (params == null ? new Object[]{} : params);
		LOGGER.info("对应的预处理参数是：" + Arrays.toString(params));
    	int row = 0;
    	PreparedStatement ps = null;
    	try{
    		ps = preparedStatement(conn, sql);
    		this.fillPreparedStatement(ps, params);
    		row = ps.executeUpdate();
    	}catch (SQLException e) {
    		LOGGER.error("在执行查询操作时产生异常，异常原因如下：\n",e);
			this.rethrow(e, sql, params);
    		//throw e;
		}finally{
    		if (closeConn) {
				XLPDBUtil.closeConnAndStatemet(conn, ps);
			}else {
				XLPDBUtil.closeStatement(ps);
			}
    	}
    	
    	return row;
    }
    
    /**
     * 更新数据库如（delete，update，insert into等操作）
     * 
     * @param conn 数据库连接
     * @param sql 预处理的sql语句
     * @param params 预处理参数
     * @return 更新的条数
     * @throws SQLException 假如参数为null或数据库访问出错，抛出此异常
     */
    public int update(Connection conn, String sql,Object... params) 
    		throws SQLException {
    	return update(conn, false, sql, params);
    }
    
    /**
     * 更新数据库如（delete，update，insert into等操作）
     * 
     * @param conn 数据库连接
     * @param sql 预处理的sql语句
     * @return 更新的条数
     * @throws SQLException 假如参数为null或数据库访问出错，抛出此异常
     */
    public int update(Connection conn, String sql)throws SQLException {
    	return update(conn, false, sql, (Object[])null);
    }
    
    /**
     * 更新数据库如（delete，update，insert into等操作）
     * 
     * @param sql 预处理的sql语句
     * @param params 预处理参数
     * @return 更新的条数
     * @throws SQLException 假如参数为null或数据库访问出错，抛出此异常
     */
    public int update(String sql,Object... params)throws SQLException {
    	Connection conn = XLPDBUtil.getConnection();
    	return update(conn, true, sql, params);
    }
    
    /**
     * 更新数据库如（delete，update，insert into等操作）
     * 
     * @param sql 预处理的sql语句
     * @return 更新的条数
     * @throws SQLException 假如参数为null或数据库访问出错，抛出此异常
     */
    public int update(String sql)throws SQLException {
    	Connection conn = XLPDBUtil.getConnection();
    	return update(conn, true, sql, (Object[])null);
    }
    
    /**
     * 参数处理
     * 
     * @param conn
     * @param closeConn
     * @param sql
     * @param params
     * @throws SQLException
     */
	private void paramsProcesser(Connection conn, boolean closeConn,
			String sql, Object... params) throws SQLException {
		nullProcesser(conn, closeConn, sql);
    	
		if (params == null) {
			if (closeConn) {
				XLPDBUtil.close(conn);
			}
			LOGGER.error("预处理参数为空！");
			throw new SQLException("预处理参数为空！");
		}
	}

	/**
	 * 参数空处理
	 * 
	 * @param conn
	 * @param closeConn
	 * @param sql
	 * @throws SQLException
	 */
	private void nullProcesser(Connection conn, boolean closeConn, String sql)
			throws SQLException {
		if (conn == null) {
			LOGGER.error("给定的数据库连接为空！");
			throw new SQLException("给定的数据库连接为空！");
		}
		
		if (sql == null) {
			if (closeConn) {
				XLPDBUtil.close(conn);
			}
			LOGGER.error("sql语句为空！");
			throw new SQLException("sql语句为空！");
		}
	}
	
	/**
	 * 单例实现
	 */
	private static class BaseDBOptionHolder{
		public static final BaseDBOption INSTANCE = new BaseDBOption();
	}
}
