package cat.institutmarianao.sailing.ws.exception;

public class ForbiddenException extends RuntimeException {
	public ForbiddenException(String message) {
		super(message);
	}

	private static final long serialVersionUID = 1L;
}