package com.company;

import java.awt.image.BufferedImage;

public interface IPlainDecoder
{
    BufferedImage decodeBMP(byte[] asciiBytes, int width, int height);
}
