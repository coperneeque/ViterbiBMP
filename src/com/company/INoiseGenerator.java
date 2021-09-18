package com.company;

public interface INoiseGenerator
{
    /**
     * Set error rate
     *
     * @param r Error rate in parts per 10 000.
     */
    void setErrorRate(int r);

    /**
     * Simulate channel noise by flipping bits randomly from '0' to '1' and vice-versa.
     * Must be a binary string i.e. containing only '1's and '0's.
     *
     * @param s String object to pollute with noise.
     */
    String noisify(String s);

    /**
     * Displays histogram of uniform distribution RNG.
     * Allows user to visually ensure distribution is uniform.
     */
    void histogram();
}
