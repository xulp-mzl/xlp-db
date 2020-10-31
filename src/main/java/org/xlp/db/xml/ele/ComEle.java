package org.xlp.db.xml.ele;

/**
 * sqls中com对象
 * 
 * @author 徐龙平
 * 
 * @version 1.0	
 */
public class ComEle {
	//com id
	private String id;
	//com 的content
	private String content;
	
	public ComEle(){
		
	}

	public ComEle(String id, String content) {
		this.id = id;
		this.content = content;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return "Comm [content=" + content + ", id=" + id + "]";
	}
}
