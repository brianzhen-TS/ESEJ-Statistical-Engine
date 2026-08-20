package Link.ExceptionStat.SystemException;

public class PropertiesDoesNotExistException extends IllegalArgumentException {
    public PropertiesDoesNotExistException(String message) {
        super(message);
    }
}
