package Link.ExceptionStat.SystemException;

import java.io.FileNotFoundException;

/**
 * The {@code ImageNotFoundException} tells you that the system can't find the image file that you've typed. It extends
 * {@code FileNotFoundException} for better identification of the system's problems.
 */
public class ImageNotFoundException extends FileNotFoundException {
    public ImageNotFoundException(String message) {
        super(message);
    }
}
