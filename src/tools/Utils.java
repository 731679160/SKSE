package tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


public class Utils {
    public static List<String> getPrefix(long range, int bitSize) {
        StringBuilder s = new StringBuilder(Long.toBinaryString(range));
        while (s.length() != bitSize) {
            s.insert(0, 0);
        }
        List<String> prefixes = new ArrayList<>(bitSize);
        char[] temp = s.toString().toCharArray();
        prefixes.add(String.valueOf(temp));
        for (int i = temp.length - 1; i >= 0; --i) {
            temp[i] = '*';
            prefixes.add(String.valueOf(temp));
        }
        return prefixes;
    }

    public static List<String> getRangePrefix(long start, long end, int bitSize) {//包括low和end
        long temp = 1;
        char[] prefix = new char[bitSize];
        Arrays.fill(prefix, '0');
        int pos = bitSize - 1;
        prefix[pos] = '*';
        while (temp - 1 < end) {
            temp <<= 1;
            prefix[pos--] = '*';
        }
        return getRangePrefixMain(start, end, 0, temp - 1, prefix, pos + 1);
    }

    public static List<String> getRangePrefixMain(long s1, long e1, long s2, long e2, char[] prefix, int pos) {
        int n = prefix.length;
        prefix = Arrays.copyOf(prefix, n);
        List<String> prefixes = new LinkedList<>();
        if (s1 <= s2 && e1 >= e2) {
            prefixes.add(String.valueOf(prefix));
            return prefixes;
        } else if (s1 > e2 || e1 < s2) return prefixes;
        prefix[pos] = '0';
        List<String> low = getRangePrefixMain(s1, e1, s2, e2 - (1L << (n - pos - 1)), prefix, pos + 1);
        prefix[pos] = '1';
        List<String> high = getRangePrefixMain(s1, e1, s2 + (1L << (n - pos - 1)), e2, prefix, pos + 1);
        prefixes.addAll(low);
        prefixes.addAll(high);
        return prefixes;
    }

    public static void main(String[] args) {
//        List<String> prefix = getPrefix(10, 4);
//        System.out.println(prefix.toString());
        List<String> rangePrefix = getRangePrefix(2, 9, 4);
        System.out.println(rangePrefix);
    }
}
