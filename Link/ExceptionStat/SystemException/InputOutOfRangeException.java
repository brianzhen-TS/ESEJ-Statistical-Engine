package Link.ExceptionStat.SystemException;

public class InputOutOfRangeException extends IllegalArgumentException {
    /**
     * Throws {@code InputOutOfRangeException} when the detected input is not on the expected bounds.
     * @param message specified detailed message*/
    public InputOutOfRangeException(String message) {
        super(message);
    }
}
