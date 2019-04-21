package cpscala.TSolver.CpUtil;

import java.lang.*;

public class SingleBitSet {
    long value = JConstants.ALLONELONG;
    int capacity;

    public SingleBitSet(int size) {
        capacity = size;
        value <<= (JConstants.BITSIZE - capacity);
    }

    public void clear() {
        value = 0L;
    }

    public boolean empty() {
        return value == 0L;
    }

    public void add(int a) {
        value |= JConstants.Mask1[a];
    }

    public boolean has(int a) {
        return (value & JConstants.Mask1[a]) != 0L;
    }

    public void remove(int a) {
        value &= JConstants.Mask0[a];
    }

    public int size() {
        return Long.bitCount(value);
    }

    public long mask() {
        return value;
    }
}
