package org.xlp.db.tableoption.handlers.result;

import java.sql.SQLException;

public class XLPJavaBeanException extends SQLException{

	private static final long serialVersionUID = -8756782092579108063L;

	public XLPJavaBeanException() {
		super();
	}

	public XLPJavaBeanException(String reason) {
		super(reason);
	}

	public XLPJavaBeanException(Throwable cause) {
		super(cause);
	}

	
}
