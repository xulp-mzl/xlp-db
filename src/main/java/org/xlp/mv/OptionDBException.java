package org.xlp.mv;
/**
 * <p>创建时间：2022年5月13日 下午1:58:52</p>
 * @author xlp
 * @version 1.0 
 * @Description 统一操作数据库错误时抛出的异常类
*/
public class OptionDBException extends RuntimeException
{
	private static final long serialVersionUID = -6016619539735662909L;

	public OptionDBException() {
		super();
	}
	
	public OptionDBException(Throwable cause) {
		super(cause);
	}

	public OptionDBException(String message, Throwable cause) {
		super(message, cause);
	}

	public OptionDBException(String message) {
		super(message);
	}
}
