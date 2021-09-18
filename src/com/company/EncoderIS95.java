package com.company;

import java.awt.image.BufferedImage;

import static com.company.ViterbiBMP.BITS_PER_PIXEL;

/**
 * Class implementing the convolutional encoder in IS-95 standard
 */
public class EncoderIS95 implements IEncoder
{
    final int       IS95_OUTPUT_LENGTH;  // 32 bit maximum
    final int       IS95_DELAY          = 8;
    final boolean   WITH_OUTPUT_BITS    = true;

//    private  BufferedImage inImg;

    public EncoderIS95()
    {
        if (WITH_OUTPUT_BITS)
            IS95_OUTPUT_LENGTH = 4;
        else
            IS95_OUTPUT_LENGTH = 3;

    }

    @Override
    public int delay() { return IS95_DELAY; }

    @Override
    public String encode(BufferedImage inImg) throws NullPointerException
    {
        if (inImg == null) throw new NullPointerException("Load BMP image from disk first!");

        StringBuilder sb = new StringBuilder();
        int rgb;
        byte lsr = 0;

        for (int h = 0; h < inImg.getHeight(); ++h) {
            for (int w = 0; w < inImg.getWidth(); ++w) {
                rgb = inImg.getRGB(w, h);
                rgb <<= 32 - BITS_PER_PIXEL;
                sb.append(encode24bits(rgb | (int)lsr & 0xff));
                lsr = (byte) (rgb >>> BITS_PER_PIXEL);
            }
        }

        return sb.toString();
    }

    @Override
    public int g0(int inputBitsLSR)  // octal 557, decimal 367, binary 101101111
    {
        return Integer.bitCount(inputBitsLSR & 0b1_0110_1111) % 2;
/*        return (inputBitsLSR >> 8 ^
                inputBitsLSR >> 6 ^
                inputBitsLSR >> 5 ^
                inputBitsLSR >> 3 ^
                inputBitsLSR >> 2 ^
                inputBitsLSR >> 1 ^
                inputBitsLSR) & 0x01; */
    }

    @Override
    public int g1(int inputBitsLSR)  // octal 663
    {
        return Integer.bitCount(inputBitsLSR & 0b1_1011_0011) % 2;
/*        return (inputBitsLSR >> 8 ^
                inputBitsLSR >> 7 ^
                inputBitsLSR >> 5 ^
                inputBitsLSR >> 4 ^
                inputBitsLSR >> 1 ^
                inputBitsLSR) & 0x01; */
    }

    @Override
    public int g2(int inputBitsLSR)  // octal 711
    {
        return Integer.bitCount(inputBitsLSR & 0b1_1100_1001) % 2;
/*        return (inputBitsLSR >> 8 ^
                inputBitsLSR >> 7 ^
                inputBitsLSR >> 6 ^
                inputBitsLSR >> 3 ^
                inputBitsLSR) & 0x01; */
    }
    @Override
    public boolean withOutputBits() { return WITH_OUTPUT_BITS; }

    private String encode24bits(int rgblsr)
    {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < BITS_PER_PIXEL; ++i) {
            sb.append(g0(rgblsr));  // c0
            if (WITH_OUTPUT_BITS) sb.append(rgblsr & 0x01);  // output bits
            sb.append(g1(rgblsr));  // c1
            sb.append(g2(rgblsr));  // c2
            rgblsr >>>= 1;
        }

        return sb.toString();
    }

}
