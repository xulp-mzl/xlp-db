package org.xlp.db.tableoption;

import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 对数据库表的操作抽象类
 * 
 * @author 徐龙平
 *         <p>
 *         2017-5-24
 *         </p>
 * @version 3.0
 * 
 */
public abstract class AbstractDBOption {
	//记录日志
	protected final static Logger LOGGER = LoggerFactory.getLogger(AbstractDBOption.class);
	//未发现
	public final static int NOT_FOUND = -1;

	public AbstractDBOption() {

	}

	/**
	 * 得到预处理
	 * 
	 * @param con
	 * @param sql
	 * @return
	 * @throws SQLException
	 */
	protected PreparedStatement preparedStatement(Connection con, String sql)
			throws SQLException {
		return con.prepareStatement(sql);
	}

	/**
	 * 用给定的参数填充statement
	 * 
	 * @param ps
	 * @param params
	 * @throws SQLException
	 */
	public void fillPreparedStatement(PreparedStatement ps, Object... params)
			throws SQLException {
		if (ps == null || params == null) {
			return;
		}

		ParameterMetaData pmd = null;
		int paramsCount = params.length;
	
		pmd = ps.getParameterMetaData();
		int pmdCount = pmd.getParameterCount();
		if (paramsCount != pmdCount) {
			LOGGER.error("fillPreparedStatement函数内部出错，错误原因是：参数个数期望的是[" + pmdCount + "]个,而给的是["
					+ paramsCount + "]个");
			throw new SQLException("错误的参数个数：期望的 " + pmdCount + ",而给的是"
					+ paramsCount);
		}
		
		for (int i = 0; i < paramsCount; i++) {
			if(params[i] != null && params[i].getClass().equals(Boolean.class)){
				if(((Boolean)params[i]).booleanValue())
					params[i] = 1;
				else 
					params[i] = 0;
			}
			ps.setObject(i + 1, params[i]);
		}
	}

	/**
     * 抛出一个包含更多错误信息的新的sql异常.
     *
     * @param cause 当重新抛出时，将在原始的异常后连接一个新的异常
     * @param sql 正在执行的sql语句.
     * @param params 预处理参数.
     * @throws SQLException 
     */
    protected void rethrow(SQLException cause, String sql, Object... params) 
    		throws SQLException{
        String causeMessage = cause.getMessage();
        if (causeMessage == null) {
            causeMessage = "";
        }
        StringBuffer msg = new StringBuffer(causeMessage+"\r\n");

        msg.append("--sql语句: ");
        msg.append(sql);
        msg.append(" --预处理参数(Parameters): ");

        if (params == null) {
            msg.append("[]");
        } else {
            msg.append(Arrays.deepToString(params));
        }

        SQLException e = new SQLException(msg.toString(), cause);
        e.setNextException(cause);
        throw e;
    }
}
