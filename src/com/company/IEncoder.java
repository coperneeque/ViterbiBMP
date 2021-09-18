package com.company;

import java.awt.image.BufferedImage;

public interface IEncoder
{
    int delay();
    String encode(BufferedImage inImg) throws NullPointerException;
    int g0(int inputBitsLSR);
    int g1(int inputBitsLSR);
    int g2(int inputBitsLSR);
    boolean withOutputBits();

}
