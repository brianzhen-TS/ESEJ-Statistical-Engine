package Link.ExceptionStat.SystemException;

@SuppressWarnings("ALL")
public class InvalidInputException extends IllegalArgumentException {
    /**
     * Throws {@code InvalidInputException} when the detected input is invalid.
     * @param message specified detail message.*/
    public InvalidInputException(String message) {
        super(message);
    }

    /**
     * Throws {@code InvalidInputException} when the detected input is invalid.
     * @param message specified detail message.
     * @param cause throwable cause(s).*/
    public InvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }
}
