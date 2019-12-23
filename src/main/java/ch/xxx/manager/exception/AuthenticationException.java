package ch.xxx.manager.exception;

public class AuthenticationException extends RuntimeException {

	private static final long serialVersionUID = -4778173207515812187L;
	
	public AuthenticationException(String message) {
		super(message);
	}

}
