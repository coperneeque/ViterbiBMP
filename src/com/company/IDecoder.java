package com.company;

public interface IDecoder
{
    byte[] decodeIS95(byte[] codeText, int width, int height);

    /**
     * Sets the decoding depth of the Viterbi algorithm.
     * Consequently, calculates MAX_PATH_METRIC and creates int[][] pathMetric
     * and int[][] pathPreviousVertex arrays, size of which depends on decoding depth.
     *
     * @param d Desired decoding depth
     */
    void setDecodingDepth(int d);
}
