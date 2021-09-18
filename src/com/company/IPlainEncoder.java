package com.company;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Interface for straight-forward plaintext encoder
 */
public interface IPlainEncoder
{
    String encode(BufferedImage inImg) throws NullPointerException;
    BufferedImage readBMP(Path path) throws IOException;
}
