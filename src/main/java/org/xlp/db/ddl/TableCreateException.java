package org.xlp.db.ddl;
/**
 * <p>创建时间：2021年3月19日 下午10:57:18</p>
 * @author xlp
 * @version 1.0 
 * @Description 创建数据表过程出错时，所需的异常类
*/
public class TableCreateException extends RuntimeException{
	private static final long serialVersionUID = 1123L;

	public TableCreateException(String message, Throwable cause) {
		super(message, cause);
	}

	public TableCreateException(String message) {
		super(message);
	}

	public TableCreateException(Throwable cause) {
		super(cause);
	}
}
