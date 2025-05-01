package proyect.proyectefinal.exception;

import lombok.Getter;

@Getter
public class FiltroException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final String errorCode;
    private final String message;
    private String detailedMessage;

    public FiltroException(String errorCode, String message, String detailedMessage) {
        super(message);
        this.errorCode = errorCode;
        this.message = message;
        this.detailedMessage = detailedMessage;
    }
}
