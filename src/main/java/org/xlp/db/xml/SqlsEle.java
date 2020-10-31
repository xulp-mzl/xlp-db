package org.xlp.db.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;
import org.xlp.db.xml.ele.ComEle;
import org.xlp.db.xml.ele.ParamEle;
import org.xlp.db.xml.ele.SQLEle;
import org.xlp.db.xml.ele.ParamConstants.Connector;
import org.xlp.db.xml.ele.ParamConstants.Flag;
import org.xlp.db.xml.ele.ParamConstants.Op;
import org.xlp.db.xml.ele.ParamConstants.Type;

/**
 * xlp中sqls对象 
 * 
 * @author 徐龙平
 * 
 * @version 1.0	
 */
public class SqlsEle {
	//标记符：{#com id}
	//id
	private String id;
	//mark
	private String mark;
	//ComEle对象集合
	private List<ComEle> comEles;
	//SQLEle对象集合
	private List<SQLEle> sqlEles;
	
	/**
	 * @param element sqls元素
	 */
	SqlsEle(Element element){
		createData(element);
	}

	/**
	 * @param key 指定key值
	 * @param map
	 */
    SqlsEle(String key, Map<String, Element> map){
		this(map.get(key));
	}
	
	/**
	 * 数据创建
	 * 
	 * @param element
	 */
	private void createData(Element element) {
		this.id = element.attributeValue("id");
		this.mark = element.attributeValue("mark");
		
		comEles = new ArrayList<ComEle>();
		sqlEles = new ArrayList<SQLEle>();
		
		List<Element> elements = element.elements("com");
		for (Element ele : elements) {
			comEles.add(createComEle(ele));
		}
		
		elements = element.elements("sql");
		for (Element ele : elements) {
			sqlEles.add(createSqlEle(ele));
		}
	}

	/**
	 * 创建SQLEle对象
	 * 
	 * @param ele
	 * @return
	 */
	private SQLEle createSqlEle(Element ele) {
		SQLEle sqlEle = new SQLEle();
		sqlEle.setId(ele.attributeValue("id"));
		sqlEle.setComment(ele.attributeValue("comment"));
		//设置orderby 内容
		Element e1 = ele.element("order-by");
		sqlEle.setOrderBy(e1 == null ? "" : e1.getTextTrim());
		//设置group by 内容
		e1 = ele.element("group-by");
		sqlEle.setGroupBy(e1 == null ? "" : e1.getTextTrim());
		
		e1 = ele.element("sql-ele");
		String sqlContent = e1 == null ? "" : e1.getTextTrim();
		//内容替换
		sqlContent = replace(sqlContent);
		sqlEle.setSqlContent(sqlContent);
		
		//设置paramEles对象
		List<ParamEle> paramEles = createParamEles(ele);
		sqlEle.setParamEles(paramEles);
		return sqlEle;
	}

	/**
	 * @param ele
	 * @return
	 */
	private List<ParamEle> createParamEles(Element ele) {
		List<Element> paramList = ele.elements("param");
		List<ParamEle> paramEles = new ArrayList<ParamEle>(paramList.size());
		
		ParamEle paramEle = null;
		for (Element param : paramList) {
			paramEle = createParamEle(param);
			
			paramEles.add(paramEle);
		}
		
		return paramEles;
	}

	/**
	 * 创建paramEle对象
	 * 
	 * @param param
	 * @return
	 */
	private ParamEle createParamEle(Element param) {
		ParamEle paramEle = new ParamEle();
		String flag = param.attributeValue("flag");
		if ("obj".equals(flag))
			paramEle.setFlag(Flag.obj);
		else if ("map".equals(flag))
			paramEle.setFlag(Flag.map);
		
		String joint = param.attributeValue("joint");
		if("true".equals(joint))
			paramEle.setJoint(true);
		
		String trim = param.attributeValue("trim");
		if("true".equals(trim))
			paramEle.setTrim(true);
			
		paramEle.setCol(param.attributeValue("col"));
		
		String type = param.attributeValue("type");
		if("number".equals(type))
			paramEle.setType(Type.number);
		else if("string".equals(type))
			paramEle.setType(Type.string);
		else if("date".equals(type))
			paramEle.setType(Type.date);
		else if("stream".equals(type))
			paramEle.setType(Type.stream);
		else if("bool".equals(type))
			paramEle.setType(Type.bool);
		
		String op = param.attributeValue("op");
		if("in".equals(op))
			paramEle.setOp(Op.in);
		else if("not_in".equals(op))
			paramEle.setOp(Op.not_in);
		else if("like".equals(op))
			paramEle.setOp(Op.like);
//		else if("is_null".equals(op))
//			paramEle.setOp(Op.is_null);
//		else if("is_not_null".equals(op))
//			paramEle.setOp(Op.is_not_null);
		else if("eq".equals(op))
			paramEle.setOp(Op.eq);
		else if("not_eq".equals(op))
			paramEle.setOp(Op.not_eq);
		else if("lt".equals(op))
			paramEle.setOp(Op.lt);
		else if("gt".equals(op))
			paramEle.setOp(Op.gt);
		else if("le".equals(op))
			paramEle.setOp(Op.le);
		else if("ge".equals(op))
			paramEle.setOp(Op.ge);
		
		paramEle.setFn(param.attributeValue("fn"));
		paramEle.setDefaultV(param.attributeValue("f_v"));
		
		String connector = param.attributeValue("connector");
		if("and".equals(connector))
			paramEle.setConnector(Connector.and);
		else if("or".equals(connector))
			paramEle.setConnector(Connector.or);
		else if("blank".equals(connector))
			paramEle.setConnector(Connector.blank);
		return paramEle; 
	}

	/**
	 * 用com中的id的内容替换标记符（{#com元素id}）
	 * 
	 * @param sqlContent
	 * @return
	 */
	private String replace(String sqlContent) {
		if (!sqlContent.equals("")) {
			for (ComEle comEle : comEles) {
				sqlContent = sqlContent.replace("{#" + comEle.getId() + "}",
						comEle.getContent());
			}
		}
		return sqlContent;
	}

	/**
	 * 创建ComEle对象
	 * 
	 * @param ele
	 * @return
	 */
	private ComEle createComEle(Element ele) {
		ComEle comEle = null;
		String id = ele.attributeValue("id");
		String content = ele.getTextTrim();
		comEle = new ComEle(id, content);
		return comEle;
	}

	/**
	 * 获取ComEle对象集合
	 * 
	 * @return
	 */
	public List<ComEle> getComEles() {
		return comEles;
	}

	/**
	 * 获取SQLEle对象集合
	 * 
	 * @return
	 */
	public List<SQLEle> getSqlEles() {
		return sqlEles;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMark() {
		return mark;
	}

	public void setMark(String mark) {
		this.mark = mark;
	}

	@Override
	public String toString() {
		return "SqlsEle [comEles=" + comEles + ", id=" + id + ", mark=" + mark
				+ ", sqlEles=" + sqlEles + "]";
	}
}
