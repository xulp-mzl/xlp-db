package org.xlp.db.xml.ele;

import java.util.List;

/**
 * sqls中sql对象
 * 
 * @author 徐龙平
 * 
 * @version 1.0	
 */
public class SQLEle {
	//sql元素标记，在同一SQLs元素中的id不能相同
	private String id;
	//描述内容
	private String comment;
	//sql内容(可以包含order by和group by的内容)，当包含时，order by和group by内容可以不写
	private String sqlContent;
	//order by 内容
	private String orderBy;
	//group by 内容
	private String groupBy;
	//param对象
	private List<ParamEle> paramEles;
	
	public SQLEle(){
		
	}

	public SQLEle(String id, String comment, String sqlContent, String orderBy,
			String groupBy, List<ParamEle> paramEles) {
		this.id = id;
		this.comment = comment;
		this.sqlContent = sqlContent;
		this.orderBy = orderBy;
		this.groupBy = groupBy;
		this.paramEles = paramEles;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getSqlContent() {
		return sqlContent;
	}

	public void setSqlContent(String sqlContent) {
		this.sqlContent = sqlContent;
	}

	public String getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	public String getGroupBy() {
		return groupBy;
	}

	public void setGroupBy(String groupBy) {
		this.groupBy = groupBy;
	}

	public List<ParamEle> getParamEles() {
		return paramEles;
	}

	public void setParamEles(List<ParamEle> paramEles) {
		this.paramEles = paramEles;
	}

	@Override
	public String toString() {
		return "SQLEle [comment=" + comment + ", groupBy=" + groupBy + ", id="
				+ id + ", orderBy=" + orderBy + ", paramEles=" + paramEles
				+ ", sqlContent=" + sqlContent + "]";
	}
	
}
