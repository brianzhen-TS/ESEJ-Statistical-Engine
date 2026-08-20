package Link.ExceptionStat.SystemException;

public class NonZeroException extends ArithmeticException {
    /**
     * Throws {@code NonZeroException} when the detected value is below zero.*/
    public NonZeroException(String message) {
        super(message);
    }
}
