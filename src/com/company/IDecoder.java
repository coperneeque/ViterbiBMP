package com.company;

public interface IDecoder
{
    /**
     * Viterbi Hard Decision decoder.
     * Decodes the signal and stores in memory in byte array.
     *
     * @param codeText byte array containing code-text in '0' and '1' form
     * @param numPixels Number of pixels in original image
     */
    byte[] decodeIS95(byte[] codeText, int numPixels);

    /**
     * Sets the decoding depth of the Viterbi algorithm.
     * Consequently, calculates MAX_PATH_METRIC and creates int[][] pathMetric
     * and int[][] pathPreviousVertex arrays, size of which depends on decoding depth.
     *
     * @param d Desired decoding depth
     */
    void setDecodingDepth(int d);
}
