package Link;

import Link.ExceptionStat.SystemException.InvalidInputException;

final class ArrayUtils {
    private ArrayUtils() {}

    /**
     * Converts an Object (Double or double[]) to a double[].
     * @param xValue the input (single Double or double[])
     * @return a double array
     * @throws InvalidInputException if type is not supported
     */
    public static double[] toDoubleArray(Object xValue) {
        if (xValue instanceof Double) {
            return new double[]{(Double) xValue};
        } else if (xValue instanceof double[]) {
            return (double[]) xValue;
        } else {
            throw new InvalidInputException("xValue must be Double or double[]");
        }
    }
}
