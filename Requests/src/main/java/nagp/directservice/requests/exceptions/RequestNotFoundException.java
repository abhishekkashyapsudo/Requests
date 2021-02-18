package nagp.directservice.requests.exceptions;

public class RequestNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6170196487063827224L;
	private static final String ERROR_MESSAGE = "No Request exists with the passed request id";
	public RequestNotFoundException(String msg) {
		super(ERROR_MESSAGE +":" + msg);
	}
	
	public RequestNotFoundException() {
		super(ERROR_MESSAGE);
	}
}
