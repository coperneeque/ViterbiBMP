package com.company;

import java.security.InvalidParameterException;
import java.security.SecureRandom;
import java.util.Arrays;

public class NoiseGenerator implements INoiseGenerator
{
    private final int    BOUND = 10001;  // with +1 trick

    private int          errorCount;
    private int          errorRate;
    private SecureRandom rand  = new SecureRandom();

    @Override
    public void setErrorRate(int r) throws InvalidParameterException
    {
        if (r < 0 || r > BOUND-1) throw new InvalidParameterException("ErrorRate must be between 0 and 10000!");
//        assert equals(r >= 0 && r < BOUND);
        errorRate = r;
    }

    @Override
    public String noisify(String s)
    {
        if (errorRate == 0) {
            System.out.println("[ NoiseGenerator ] Requested error rate: 0. No noise added to signal");
            return s;
        }

        errorCount = 0;
        char[] chars = s.toCharArray();

        for (int i = 0; i < chars.length; ++i) {
            chars[i] = noisifyChar(chars[i]);
        }

        System.out.println("[ NoiseGenerator ] " + chars.length + " bits in channel. Requested error rate: " +
                           (float) errorRate / (BOUND-1) * 100 +" %\n" +
                           "[ NoiseGenerator ] Generated " + errorCount + " bit errors. " +
                           "Actual error rate: " + (float) errorCount / chars.length * 100 + "%");

        return new String(chars);
    }

    @Override
    public void histogram()
    {
        final int MULT = 1000;
        final int START = 0;

        // Make a bar for printing the histogram on screen
        char[] c = new char[MULT * BOUND];
        Arrays.fill(c,'█');
        String barString = new String(c);

        // Keep count of drawing of random numbers
        int[] hist = new int[BOUND];
        Arrays.fill(hist, 0);

        // Draw random ints from range and count number of occurrences
        rand.ints(MULT * BOUND, START, START + BOUND)
            .forEach(i -> ++hist[i-START]);

        // Print histogram.
        // Use appropriate length substring of the bar string to print.
        // Calculate average
        System.out.println("Drawing " + MULT * BOUND + " numbers from range " + START + "-" + (START + BOUND) + " (incl.-excl.)");
        System.out.println("Histogram:");
        long sum = 0;
        for (int i = 0; i < hist.length; ++i){
            System.out.println(String.format("%3d: %4d  ", i+START, hist[i]) + barString.substring(0, hist[i]/10));
            sum += (long) (i + START) * hist[i];
        }
        System.out.println("Average: " + (double) sum / (MULT * BOUND));
    }

    private char noisifyChar(char c) {
//        if (c != '0' && c != '1') throw new InvalidParameterException("noisifyChar(char) only works for '0' and '1'!");
        assert equals((c == '0' || c == '1') == true);

//        if (errorRate == 0) return c;

        int draw = rand.nextInt(BOUND);
        if (draw <= errorRate) {
            ++errorCount;
            if (c == '0') return '1';
            else return '0';
        } else
            return c;
    }
}
