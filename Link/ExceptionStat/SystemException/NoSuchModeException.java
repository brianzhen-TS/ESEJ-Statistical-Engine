package Link.ExceptionStat.SystemException;

public class NoSuchModeException extends IllegalArgumentException {

    /**
     * Throws {@code NoSuchModeException} when there's no such a method that matches with the detected method.
     * @param message specified detailed message.*/
    public NoSuchModeException(String message) {
        super(message);
    }

    /**
     * Throws {@code NoSuchModeException} when there's no such a method that matches with the detected method.
     * @param message specified detailed message.
     * @param cause throwable cause(s)*/
    public NoSuchModeException(String message, Throwable cause) {
        super(message, cause);
    }
}
