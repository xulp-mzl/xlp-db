package org.xlp.db.exception;

public class EntityException extends Exception{
	private static final long serialVersionUID = 9199905669447415856L;

	public EntityException() {
		super();
	}
	
	public EntityException(Throwable cause) {
		super(cause);
	}

	public EntityException(String message, Throwable cause) {
		super(message, cause);
	}

	public EntityException(String message) {
		super(message);
	}

	
}
