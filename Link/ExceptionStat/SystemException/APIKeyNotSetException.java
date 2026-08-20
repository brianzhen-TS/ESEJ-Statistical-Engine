package Link.ExceptionStat.SystemException;

public class APIKeyNotSetException extends RuntimeException {
    public APIKeyNotSetException(String message) {
        super(message);
    }
}
