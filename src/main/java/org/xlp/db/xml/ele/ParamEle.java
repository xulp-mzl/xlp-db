package org.xlp.db.xml.ele;

import org.xlp.db.xml.ele.ParamConstants.Connector;
import org.xlp.db.xml.ele.ParamConstants.Flag;
import org.xlp.db.xml.ele.ParamConstants.Op;
import org.xlp.db.xml.ele.ParamConstants.Type;

/**
 * sql中param对象
 * 
 * @author 徐龙平
 * 
 * @version 1.0	
 */
public class ParamEle {
	//标记
	private Flag flag;
	//是否拼接
	private boolean joint = false;
	//列名（[别名+"."] + 列名）
	private String col;
	//值类型
	private Type type = Type.string;
	//操作符
	private Op op;
	//字段名
	private String fn;
	//拼接符
	private Connector connector = Connector.blank;
	//默认值
	private String defaultV;
	//标记字段值为null或""时，是否连接到SQL语句后
	private boolean trim = false;
	
	public ParamEle(){
		
	}

	public ParamEle(Flag flag, Type type, String fn) {
		this.flag = flag;
		this.type = type;
		this.fn = fn;
	}

	public Flag getFlag() {
		return flag;
	}

	public void setFlag(Flag flag) {
		this.flag = flag;
	}

	public boolean isJoint() {
		return joint;
	}

	public void setJoint(boolean joint) {
		this.joint = joint;
	}

	public String getCol() {
		return col;
	}

	public void setCol(String col) {
		this.col = col;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Op getOp() {
		return op;
	}

	public void setOp(Op op) {
		this.op = op;
	}

	public String getFn() {
		return fn;
	}

	public void setFn(String fn) {
		this.fn = fn;
	}

	public Connector getConnector() {
		return connector;
	}

	public void setConnector(Connector connector) {
		this.connector = connector;
	}

	public String getDefaultV() {
		return defaultV;
	}

	public void setDefaultV(String defaultV) {
		this.defaultV = defaultV;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ParamEle [col=");
		builder.append(col);
		builder.append(", connector=");
		builder.append(connector);
		builder.append(", defaultV=");
		builder.append(defaultV);
		builder.append(", flag=");
		builder.append(flag);
		builder.append(", fn=");
		builder.append(fn);
		builder.append(", joint=");
		builder.append(joint);
		builder.append(", op=");
		builder.append(op);
		builder.append(", type=");
		builder.append(type);
		builder.append(", trim=");
		builder.append(trim);
		builder.append("]");
		return builder.toString();
	}

	public void setTrim(boolean trim) {
		this.trim = trim;
	}

	public boolean isTrim() {
		return trim;
	}
}
