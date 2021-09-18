package com.company;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

public interface IPlainDecoder
{
    byte[] readAscii(Path path) throws IOException;
    BufferedImage outputBMP(byte[] asciiBytes, int width, int height);
}
