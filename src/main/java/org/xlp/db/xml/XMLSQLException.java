package org.xlp.db.xml;

import java.sql.SQLException;

public class XMLSQLException extends SQLException{

	private static final long serialVersionUID = -88714860320365046L;

	public XMLSQLException() {
		super();
	}

	public XMLSQLException(String reason, Throwable cause) {
		super(reason, cause);
	}

	public XMLSQLException(String reason) {
		super(reason);
	}
}
