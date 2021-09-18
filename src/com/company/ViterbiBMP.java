package com.company;

import java.nio.file.Path;

public class ViterbiBMP
{
    static final int BITS_PER_PIXEL     = 24;

    private IPlainEncoder plainEncoder;
    private IPlainDecoder plainDecoder;
    private IEncoder is95Encoder;

    public ViterbiBMP(IPlainEncoder pe, IPlainDecoder pd, IEncoder e)
    {
        assert equals(pe != null && pd != null && e != null);
        plainEncoder = pe;
        plainDecoder = pd;
        is95Encoder = e;
    }

    /**
     * Execute plain-text direct encoding of the BMP image pixels to text file containing '0's and '1's.
     * Execute decoding of text file to new image file.
     *
     * @param inBMPPath Path to original BMP image
     * @param txtPath Path to output txt file
     * @param outBMPPath Path to decoded BMP image
     */
    public void runPlain(Path inBMPPath, Path txtPath, Path outBMPPath)
    {

    }
}
