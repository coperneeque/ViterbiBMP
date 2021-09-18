package com.company;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

public class ViterbiBMP
{
    static final int BITS_PER_PIXEL = 24;

    private final IPlainEncoder   plainEncoder;
    private final IPlainDecoder   plainDecoder;
    private final INoiseGenerator noiseGenerator;
    private final IEncoder        is95Encoder;

    public ViterbiBMP(IPlainEncoder pe, IPlainDecoder pd, INoiseGenerator ng, IEncoder e)
    {
        assert equals(pe != null && pd != null && ng != null && e != null);
        plainEncoder = pe;
        plainDecoder = pd;
        noiseGenerator = ng;
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
        // read BMP image from disk:
        BufferedImage inImg = null;
        try {
            inImg = ImageIO.read(new File(String.valueOf(inBMPPath)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert equals(inImg != null);

        // encode image, add noise and write ascii to disk:
        String plainText = plainEncoder.encode(inImg);
        if (noiseGenerator != null) {
            plainText = noiseGenerator.noisify(plainText);
        }
        try {
            Files.write(txtPath, Collections.singleton(plainText));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // read ASCII from disk:
        byte[] asciiBytes = null;
        try {
            asciiBytes = plainDecoder.readAscii(txtPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert equals(asciiBytes != null);

        // decode image and write BMP to disk:
        BufferedImage outImg = plainDecoder.outputBMP(asciiBytes, inImg.getWidth(), inImg.getHeight());
        try {
            ImageIO.write(outImg, "bmp", new File(outBMPPath.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
