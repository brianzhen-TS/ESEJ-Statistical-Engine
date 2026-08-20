package Link.ExceptionStat.SystemException;

@SuppressWarnings("ALL")
public class NoSuchDistributionException extends NoSuchMethodException {

    /**
     * Throws {@code NoSuchDistributionException} if the system didn't find the desired distribution name.
     * @Note Null inputs. meaning it won't provide any message of the exception
     * */
    public NoSuchDistributionException() {
        super();
    }

    /**
     * Throws {@code NoSuchDistributionException} if the system didn't find the desired distribution name.
     * @param message specified detailed message.*/
    public NoSuchDistributionException(String message) {
        super(message);
    }
}
