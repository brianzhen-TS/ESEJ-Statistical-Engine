package Link.ExceptionStat.SystemException;

@SuppressWarnings("ALL")
public class ArrayIsEmptyException extends RuntimeException {
    /**
     * Throws {@code ArrayIsEmptyException} if the array was empty.
     * @Note Null inputs. meaning it won't provide any message of the exception
     * */
    public ArrayIsEmptyException(){
        super();
    }

    /**
     * Throws {@code ArrayIsEmptyException} if the array was empty.
     * @param message specified detail message.
     */
    public ArrayIsEmptyException(String message)
    {
        super(message);
    }

    /**
     * Throws {@code ArrayIsEmptyException} if the array was empty.
     * @param message specified detail message.
     * @param cause throwable cause(s). */
    public ArrayIsEmptyException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Throws {@code ArrayIsEmptyException} if the array was empty.
     * @param cause throwable cause(s). */
    public ArrayIsEmptyException(Throwable cause) {
        super(cause);
    }
}
