package HILBERT.davidmoten.hilbert;

import java.util.Arrays;

public class Test {
    public static void main(String[] args) {
        SmallHilbertCurve c = HilbertCurve.small().bits(5).dimensions(2);
        long index = c.index(3, 4);
        long[] point = c.point(31);
        System.out.println(index);
        System.out.println(Arrays.toString(point));
    }
}
