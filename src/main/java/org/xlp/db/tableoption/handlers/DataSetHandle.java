package org.xlp.db.tableoption.handlers;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.xlp.db.tableoption.handlers.result.DataSet;

public class DataSetHandle implements ResultSetHandle<DataSet>{

	@Override
	public DataSet handle(ResultSet rs) throws SQLException {
		DataSet dataSet = new DataSet();
		ResultSetMetaData rsmd = rs.getMetaData();
		//列数
		int columnCount = rsmd.getColumnCount();
		
		String[] titles = new String[columnCount];
		for (int i = 0; i < columnCount; i++) {
			titles[i] = rsmd.getColumnLabel(i + 1); 
		}
		dataSet.setTitles(titles);
		
		init(rs, dataSet, columnCount);
		
		return dataSet;
	}

	/**
	 * 初始化dataset的其它数据
	 * 
	 * @param rs
	 * @param dataSet
	 * @param columnCount
	 * @throws SQLException 
	 */
	private void init(ResultSet rs, DataSet dataSet, int columnCount) throws SQLException {
		Class<?>[] columnType = new Class<?>[columnCount];
		List<Object[]> datas = new ArrayList<Object[]>();
		
		Object[] values = null;
		long count = 0;
		if(rs.next()){
			values = new Object[columnCount];
			for (int i = 0; i < columnCount; i++) {
				values[i] = rs.getObject(i + 1);
				if(values[i] != null)
					columnType[i] = values[i].getClass();
				else
					columnType[i] = Object.class;
			}
			datas.add(values);
			count ++;
		}else {
			dataSet.setColumnType(columnType);
			dataSet.setDatas(datas);
			return;
		}
		
		while (rs.next()) {
			values = new Object[columnCount];
			for (int i = 0; i < columnCount; i++) 
				values[i] = rs.getObject(i + 1);
			datas.add(values);
			count ++;
		}
		
		dataSet.setColumnType(columnType);
		dataSet.setDatas(datas);
		dataSet.setCount(count);
	}

}
