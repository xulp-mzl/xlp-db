package org.xlp.db.tableoption;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.xlp.db.utils.XLPDBUtil;

/**
 * 主要功能是把查询结果以原始的ResultSet对象返回
 * 
 * @author 徐龙平
 *         <p>
 *         2017-5-26
 *         </p>
 * @version 1.0
 * 
 */
public class OriginalResultSetOption extends AbstractDBOption{
	private final static Logger LOGGER = Logger.getLogger(BaseDBOption.class);
	//连接
	private Connection connection;
	//预处理
	private Statement statement;
	//结果集
	private ResultSet resultSet;
	
	public OriginalResultSetOption() throws SQLException{
		this(XLPDBUtil.getConnection());
	}

	public OriginalResultSetOption(Connection connection){
		this.connection = connection;
	}
	
	public Connection getConnection() {
		return connection;
	}

	public Statement getStatement() {
		return statement;
	}

	public ResultSet getResultSet() {
		return resultSet;
	}
	
	/**
	 * 关闭资源
	 * 
	 * @throws SQLException
	 */
	public synchronized void close() throws SQLException{
		XLPDBUtil.closeAll(connection, statement, resultSet);
	}
	
	/**
     * 查询操作
     * 
	 * @param sql 预处理的sql语句
     * @param params 预处理参数 
     * @return 查询结果
     * @throws SQLException 假如参数为null或数据库访问出错，抛出此异常
     */
    public synchronized ResultSet query(String sql, Object... params) throws SQLException {
    	nullProcesser(connection, sql);
		
    	LOGGER.info("正在执行的查询sql语句是：" + sql);
		LOGGER.info("对应的预处理参数是：" + Arrays.toString(params));
    	PreparedStatement ps = null;
    	
    	try {
			ps = preparedStatement(connection, sql);
			this.fillPreparedStatement(ps, params);
			this.statement = ps;
			this.resultSet = ps.executeQuery();
		} catch (SQLException e) {
			LOGGER.error("在执行查询操作时产生异常，异常原因如下：\n",e);
			this.rethrow(e, sql, params);
		}
    	
    	return this.resultSet;
    }

    /**
     * 查询操作
     * 
	 * @param sql 预处理的sql语句
     * @param param 预处理参数
     * @return 查询结果
     * @throws SQLException 假如参数为null或数据库访问出错，抛出此异常
     */
    public ResultSet query(String sql, Object param) throws SQLException {
    	Object[] params = null;
    	if (param != null) {
			params = new Object[]{param};
		}
    	return query(sql, params);
    }
    
    /**
     * 查询操作
     * 
	 * @param sql 预处理的sql语句
     * @return 查询结果
     * @throws SQLException 假如参数为null或数据库访问出错，抛出此异常
     */
    public ResultSet query(String sql) throws SQLException {
    	return query(sql, (Object[])null);
    }
    
    /**
	 * 参数空处理
	 * 
	 * @param conn
	 * @param closeConn
	 * @param sql
	 * @throws SQLException
	 */
	private void nullProcesser(Connection conn, String sql)
			throws SQLException {
		if (conn == null) {
			LOGGER.error("给定的数据库连接为空！");
			throw new SQLException("给定的数据库连接为空！");
		}
		
		if (sql == null) {
			LOGGER.error("sql语句为空！");
			throw new SQLException("sql语句为空！");
		}
	}
}
