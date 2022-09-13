package util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Methods for manipulating arrays.
 *
 * @lucene.internal
 */
public final class ArrayUtil {

    /** Maximum length for an array (Integer.MAX_VALUE - RamUsageEstimator.NUM_BYTES_ARRAY_HEADER). */

    private ArrayUtil() {} // no instance

  /*
    Begin Apache Harmony code
    Revision taken on Friday, June 12. https://svn.apache.org/repos/asf/harmony/enhanced/classlib/archive/java6/modules/luni/src/main/java/java/lang/Integer.java
  */

    /**
     * Parses a char array into an int.
     *
     * @param chars the character array
     * @param offset The offset into the array
     * @param len The length
     * @return the int
     * @throws NumberFormatException if it can't parse
     */
    public static int parseInt(char[] chars, int offset, int len) throws NumberFormatException {
        return parseInt(chars, offset, len, 10);
    }

    /**
     * Parses the string argument as if it was an int value and returns the result. Throws
     * NumberFormatException if the string does not represent an int quantity. The second argument
     * specifies the radix to use when parsing the value.
     *
     * @param chars a string representation of an int quantity.
     * @param radix the base to use for conversion.
     * @return int the value represented by the argument
     * @throws NumberFormatException if the argument could not be parsed as an int quantity.
     */
    public static int parseInt(char[] chars, int offset, int len, int radix)
            throws NumberFormatException {
        if (chars == null || radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) {
            throw new NumberFormatException();
        }
        int i = 0;
        if (len == 0) {
            throw new NumberFormatException("chars length is 0");
        }
        boolean negative = chars[offset + i] == '-';
        if (negative && ++i == len) {
            throw new NumberFormatException("can't convert to an int");
        }
        if (negative == true) {
            offset++;
            len--;
        }
        return parse(chars, offset, len, radix, negative);
    }

    private static int parse(char[] chars, int offset, int len, int radix, boolean negative)
            throws NumberFormatException {
        int max = Integer.MIN_VALUE / radix;
        int result = 0;
        for (int i = 0; i < len; i++) {
            int digit = Character.digit(chars[i + offset], radix);
            if (digit == -1) {
                throw new NumberFormatException("Unable to parse");
            }
            if (max > result) {
                throw new NumberFormatException("Unable to parse");
            }
            int next = result * radix - digit;
            if (next > result) {
                throw new NumberFormatException("Unable to parse");
            }
            result = next;
        }
    /*while (offset < len) {
    }*/
        if (!negative) {
            result = -result;
            if (result < 0) {
                throw new NumberFormatException("Unable to parse");
            }
        }
        return result;
    }








    /**
     * Copies the specified range of the given array into a new sub array.
     *
     * @param array the input array
     * @param from the initial index of range to be copied (inclusive)
     * @param to the final index of range to be copied (exclusive)
     */
    public static byte[] copyOfSubArray(byte[] array, int from, int to) {
        final byte[] copy = new byte[to - from];
        System.arraycopy(array, from, copy, 0, to - from);
        return copy;
    }

    /**
     * Copies the specified range of the given array into a new sub array.
     *
     * @param array the input array
     * @param from the initial index of range to be copied (inclusive)
     * @param to the final index of range to be copied (exclusive)
     */
    public static char[] copyOfSubArray(char[] array, int from, int to) {
        final char[] copy = new char[to - from];
        System.arraycopy(array, from, copy, 0, to - from);
        return copy;
    }

    /**
     * Copies the specified range of the given array into a new sub array.
     *
     * @param array the input array
     * @param from the initial index of range to be copied (inclusive)
     * @param to the final index of range to be copied (exclusive)
     */
    public static short[] copyOfSubArray(short[] array, int from, int to) {
        final short[] copy = new short[to - from];
        System.arraycopy(array, from, copy, 0, to - from);
        return copy;
    }

    /**
     * Copies the specified range of the given array into a new sub array.
     *
     * @param array the input array
     * @param from the initial index of range to be copied (inclusive)
     * @param to the final index of range to be copied (exclusive)
     */
    public static int[] copyOfSubArray(int[] array, int from, int to) {
        final int[] copy = new int[to - from];
        System.arraycopy(array, from, copy, 0, to - from);
        return copy;
    }

    /**
     * Copies the specified range of the given array into a new sub array.
     *
     * @param array the input array
     * @param from the initial index of range to be copied (inclusive)
     * @param to the final index of range to be copied (exclusive)
     */
    public static long[] copyOfSubArray(long[] array, int from, int to) {
        final long[] copy = new long[to - from];
        System.arraycopy(array, from, copy, 0, to - from);
        return copy;
    }

    /**
     * Copies the specified range of the given array into a new sub array.
     *
     * @param array the input array
     * @param from the initial index of range to be copied (inclusive)
     * @param to the final index of range to be copied (exclusive)
     */
    public static float[] copyOfSubArray(float[] array, int from, int to) {
        final float[] copy = new float[to - from];
        System.arraycopy(array, from, copy, 0, to - from);
        return copy;
    }

    /**
     * Copies the specified range of the given array into a new sub array.
     *
     * @param array the input array
     * @param from the initial index of range to be copied (inclusive)
     * @param to the final index of range to be copied (exclusive)
     */
    public static double[] copyOfSubArray(double[] array, int from, int to) {
        final double[] copy = new double[to - from];
        System.arraycopy(array, from, copy, 0, to - from);
        return copy;
    }

    /**
     * Copies the specified range of the given array into a new sub array.
     *
     * @param array the input array
     * @param from the initial index of range to be copied (inclusive)
     * @param to the final index of range to be copied (exclusive)
     */
    public static <T> T[] copyOfSubArray(T[] array, int from, int to) {
        final int subLength = to - from;
        final Class<? extends Object[]> type = array.getClass();
        @SuppressWarnings("unchecked")
        final T[] copy =
                (type == Object[].class)
                        ? (T[]) new Object[subLength]
                        : (T[]) Array.newInstance(type.getComponentType(), subLength);
        System.arraycopy(array, from, copy, 0, subLength);
        return copy;
    }

    /** Comparator for a fixed number of bytes. */
    @FunctionalInterface
    public static interface ByteArrayComparator {

        /**
         * Compare bytes starting from the given offsets. The return value has the same contract as
         * {@link Comparator#compare(Object, Object)}.
         */
        int compare(byte[] a, int aI, byte[] b, int bI);
    }

    /** Return a comparator for exactly the specified number of bytes. */
    public static ByteArrayComparator getUnsignedComparator(int numBytes) {
        if (numBytes == Long.BYTES) {
            // Used by LongPoint, DoublePoint
            return ArrayUtil::compareUnsigned8;
        } else if (numBytes == Integer.BYTES) {
            // Used by IntPoint, FloatPoint, LatLonPoint, LatLonShape
            return ArrayUtil::compareUnsigned4;
        } else {
            return (a, aOffset, b, bOffset) ->
                    Arrays.compareUnsigned(a, aOffset, aOffset + numBytes, b, bOffset, bOffset + numBytes);
        }
    }

    /** Compare exactly 8 unsigned bytes from the provided arrays. */
    public static int compareUnsigned8(byte[] a, int aOffset, byte[] b, int bOffset) {
        return Long.compareUnsigned(
                (long) BitUtils.VH_BE_LONG.get(a, aOffset), (long) BitUtils.VH_BE_LONG.get(b, bOffset));
    }

    /** Compare exactly 4 unsigned bytes from the provided arrays. */
    public static int compareUnsigned4(byte[] a, int aOffset, byte[] b, int bOffset) {
        return Integer.compareUnsigned(
                (int) BitUtils.VH_BE_INT.get(a, aOffset), (int) BitUtils.VH_BE_INT.get(b, bOffset));
    }
}