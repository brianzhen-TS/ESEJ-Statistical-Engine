package Link.ExceptionStat.SystemError;

/**
 * A {@link RuntimeAnomalyError} extends {@link java.lang.Error} to catch the anomalies(unexpected exception occurrences) during the ESEJ system
 * (especially for the GUIs and core) runtime.
 * <p>
 * In the original system exceptions control design, the error was thrown in the {@code try}-{@code catch} block and output both
 * the messages and the throwable cause.
 * </p>
 * <p>
 * The reason it has to be an error is that when the system has received an exception, or caught an error occurrence,
 * it will crash the system(see below). The error configurations here is to make sure that the system can handle the error during
 * runtime, and able to detect why the error occurs so that the users / developers
 * can understand where the error comes from and makes addressing them much easier. Though now the web backends (built
 * using Spring boot) rarely uses it, it was still the primary components of the FXSystem GUI (includes the web server powered ver.)
 * and EtaCore error handling.
 * </p>
 * <p>
 * This Error indicates that the system has received invalid inputs / arguments from the clients(like GUIs) or the servers,
 * or the system encountered a serious error (which usually has the type of {@link java.lang.Error}) thrown from the JVM or elsewhere in this environment, causing it to crash or
 * dysfunctionality occurrences.
 * </p>
 * @see Throwable
 * @see Error
 * @see Exception
 * @see RuntimeException
 * @since ESEJ 1.0
 */
public class RuntimeAnomalyError extends Error {
    /**
     * Throws {@link RuntimeAnomalyError} with the specified message.
     * @param message specified message(s)
     */
    public RuntimeAnomalyError(String message) {
        super(message);
    }

    /**
     * Throws {@link RuntimeAnomalyError} when the system catches an unexpected exception occurrences.
     * @param message specified detailed message about the error.
     * @param cause throwable cause(s).*/
    public RuntimeAnomalyError(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Throws {@link RuntimeAnomalyError} with the following message:
     * <pre>
     * {@code Anomaly occurred, please check:
     * // then the cause...}
     * </pre>
     * @param cause throwable cause(s).*/
    public RuntimeAnomalyError(Throwable cause) {
        super("Anomaly occurred, please check: ", cause);
    }

    /**
     * Throws {@link RuntimeAnomalyError} with the following message:
     * <pre>
     * {@code Anomaly occurred, please check:
     * // then the cause...}
     * </pre>
     * and suppression or writableStackTrace is enabled or not. (see {@link java.lang.Error} for more details of the
     * boolean inputs)
     */
    public RuntimeAnomalyError(Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super("Anomaly occurred, please check: ", cause, enableSuppression, writableStackTrace);
    }
}
