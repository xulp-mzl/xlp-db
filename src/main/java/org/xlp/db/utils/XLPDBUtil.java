package org.xlp.db.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

/**
 * jdbcUtil类
 * 
 * @author 徐龙平
 *         <p>
 *         2017-1-16
 *         </p>
 * @version 1.0
 * 
 */
public final class XLPDBUtil {
	//数据源
	private static DataSource dataSource = null;
	//事务专用连接
	private final static ThreadLocal<Connection> tl = new ThreadLocal<Connection>();
	/**
	 * 开启事务的个数
	 */
	private final static ThreadLocal<Integer> TRAN_COUNT = new ThreadLocal<Integer>();
	
	/**
	 * 初始化数据源
	 * 
	 * @param dataSource
	 */
	public static void initDataSource(DataSource dataSource){
		XLPDBUtil.dataSource = dataSource;
	}
	
	/**
	 * 得到数据源
	 * 
	 * @return DataSource
	 */
	public static DataSource getDataSource() {
		return dataSource;
	}
	
	/**
	 * 得到数据库连接
	 * 
	 * @return
	 * @throws SQLException 
	 */
	public static Connection getConnection() throws SQLException{
		//事务专用连接
		Connection conOfTransaction = tl.get();
		if (conOfTransaction != null) {
			//返回事务专用连接
			return conOfTransaction;
		}
		return dataSource.getConnection();
	}
	
	/**
	 * 开启事务
	 * @throws SQLException 
	 */
	public static void beginTransaction() throws SQLException{
		//事务专用连接
		Connection conOfTransaction = tl.get();
		Integer tranCount = TRAN_COUNT.get();
		if (conOfTransaction != null) {
			TRAN_COUNT.set(Integer.valueOf(tranCount.intValue() + 1));
		}else {
			conOfTransaction = getConnection();
			conOfTransaction.setAutoCommit(false);
			tl.set(conOfTransaction);
			TRAN_COUNT.set(Integer.valueOf(1));
		}
	}
	
	/**
	 * 事务提交
	 * @throws SQLException 
	 */
	public static void commitTransaction() throws SQLException{
		//事务专用连接
		Connection conOfTransaction = tl.get();
		Integer tranCount = TRAN_COUNT.get();
		if (conOfTransaction != null ) {
			if (tranCount == null || tranCount.intValue() == 1) {
				conOfTransaction.commit();
				tl.remove();//移除连接
				TRAN_COUNT.remove();
				conOfTransaction.close();
			}else {
				TRAN_COUNT.set(Integer.valueOf(tranCount.intValue() - 1));
			}
		} 
	}
	
	/**
	 * 事务回滚
	 * @throws SQLException 
	 */
	public static void rollbackTransaction() throws SQLException{
		//事务专用连接
		Connection conOfTransaction = tl.get();
		Integer tranCount = TRAN_COUNT.get();
		if (conOfTransaction != null) {
			if (tranCount == null || tranCount.intValue() == 1) {
				conOfTransaction.rollback();
				tl.remove();//移除连接
				TRAN_COUNT.remove();
				conOfTransaction.close();
			}else {
				TRAN_COUNT.set(Integer.valueOf(tranCount.intValue() - 1));
			}
		}
	}
	
	/**
	 * 关闭连接
	 * @throws SQLException 
	 */
	public static void close(Connection connection) throws SQLException{
		//事务专用连接
		Connection conOfTransaction = tl.get();
		if (connection != null && connection != conOfTransaction) {
			connection.close();
		}
	}
	
	/**
	 * 关闭statement
	 * 
	 * @param statement
	 * @throws SQLException 
	 */
	public static void closeStatement(Statement statement) throws SQLException{
		if (statement != null) {
			statement.close();
		}
	}
	
	/**
	 * 关闭statement
	 * 
	 * @param resultSet
	 * @throws SQLException 
	 */
	public static void closeResultSet(ResultSet resultSet) throws SQLException{
		if (resultSet != null) {
			resultSet.close();
		}
	}
	
	/**
	 * 关闭连接
	 * @throws SQLException 
	 */
	public static void closeWithNoException(Connection connection){
		try {
			close(connection);
		} catch (SQLException e) {
		}
	}
	
	/**
	 * 关闭statement
	 * 
	 * @param statement
	 * @throws SQLException 
	 */
	public static void closeStatementWithNoException(Statement statement){
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
			}
		}
	}
	
	/**
	 * 关闭statement
	 * 
	 * @param resultSet
	 * @throws SQLException 
	 */
	public static void closeResultSetWithNoException(ResultSet resultSet){
		if (resultSet != null) {
			try {
				resultSet.close();
			} catch (SQLException e) {
			}
		}
	}
	
	/**
	 * 关闭所有资源
	 * 
	 * @param con
	 * @param st
	 * @param rs
	 * @throws SQLException 
	 */
	public static void closeAll(Connection con, Statement st, ResultSet rs)
			throws SQLException{
		try{
			closeResultSet(rs);
		}finally{
			try{
				closeStatement(st);
			}finally{
				close(con);
			}
		}
	}
	
	/**
	 * 关闭所有资源
	 * 
	 * @param con
	 * @param st
	 * @param rs
	 * @throws SQLException 
	 */
	public static void closeAllWithNoException(Connection con, Statement st, 
			ResultSet rs){
		try {
			closeAll(con, st, rs);
		} catch (SQLException e) {
		}
	}
	
	/**
	 * 关闭资源
	 * 
	 * @param st
	 * @param rs
	 * @throws SQLException 
	 */
	public static void closeStatemebtAndResultSet(Statement st, ResultSet rs)
			throws SQLException{
		try{
			closeResultSet(rs);
		}finally{
			closeStatement(st);
		}
	}
	
	/**
	 * 关闭资源
	 * 
	 * @param st
	 * @param rs
	 * @throws SQLException 
	 */
	public static void closeStatemebtAndResultSetWithNoException(Statement st, 
			ResultSet rs){
		try {
			closeStatemebtAndResultSet(st, rs);
		} catch (SQLException e) {
		}
	}
	
	/**
	 * 关闭资源
	 * 
	 * @param conn
	 * @param st
	 * @throws SQLException 
	 */
	public static void closeConnAndStatemetWithNoException(Connection conn, Statement st){
		try {
			closeConnAndStatemet(conn, st);
		} catch (SQLException e) {
		}
	}
	
	/**
	 * 关闭资源
	 * 
	 * @param conn
	 * @param st
	 * @throws SQLException 
	 */
	public static void closeConnAndStatemet(Connection conn, Statement st) 
			throws SQLException{
		try {
			closeStatement(st);
		} finally {
			close(conn);
		}
	}
	
	/**
	 * 获取此数据库产品的名称。
	 * 
	 * @return
	 */
	public static String getDatabaseProductName(){
		Connection connection = null;
		try {
			connection = getConnection();
			String driverName = connection.getMetaData()
					.getDatabaseProductName();
			return driverName;
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			closeWithNoException(connection);
		}
		return null;
	}
}
