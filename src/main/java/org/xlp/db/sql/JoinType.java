package org.xlp.db.sql;
/**
 * <p>创建时间：2022年5月15日 下午11:05:44</p>
 * @author xlp
 * @version 1.0 
 * @Description 类描述
*/
public enum JoinType {
	LEFT("left join"),
	RIGHT("right join"),
	INNER("inner join");
	
	private String descript;
	
	private JoinType(String descript){
		this.setDescript(descript);
	}

	/**
	 * @return the descript
	 */
	public String getDescript() {
		return descript;
	}

	/**
	 * @param descript the descript to set
	 */
	private void setDescript(String descript) {
		this.descript = descript;
	}
	
	
}
