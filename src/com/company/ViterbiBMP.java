package com.company;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

public class ViterbiBMP
{
    static final int BITS_PER_PIXEL = 24;

    private final IPlainEncoder   plainEncoder;
    private final IPlainDecoder   plainDecoder;
    private final INoiseGenerator noiseGenerator;
    private final IEncoder        is95Encoder;
    private final IDecoder        viterbiDecoder;

    public ViterbiBMP(IPlainEncoder pe, IPlainDecoder pd, INoiseGenerator ng, IEncoder e, IDecoder vd)
    {
        assert equals(pe != null && pd != null && ng != null && e != null && vd != null);
        plainEncoder = pe;
        plainDecoder = pd;
        noiseGenerator = ng;
        is95Encoder = e;
        viterbiDecoder = vd;
    }

    /**
     * Execute IS-95 standard encoding of the BMP image pixels to text file containing '0's and '1's.
     * Execute Viterbi decoding of text file to new image file.
     *
     * @param inBMPPath Path to original BMP image
     * @param txtPath Path to output txt file
     * @param outBMPPath Path to decoded BMP image
     */
    public void runIS95(Path inBMPPath, Path txtPath, Path outBMPPath)
    {
        // read BMP image from disk:
        BufferedImage inImg = null;
        try {
            inImg = ImageIO.read(new File(String.valueOf(inBMPPath)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert equals(inImg != null);

        // encode image, add noise and write ascii code-text to disk:
        String is95Text = is95Encoder.encode(inImg);
        if (noiseGenerator != null) {
            is95Text = noiseGenerator.noisify(is95Text);
        }
        try {
            Files.write(txtPath, Collections.singleton(is95Text));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // read ASCII code-text from disk:
        byte[] codeText = null;
        try {
            codeText = readCodeText(txtPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert equals(codeText != null);

        // decode image and write to disk:
        byte[] plainText = viterbiDecoder.decodeIS95(codeText, inImg.getWidth(), inImg.getHeight());
        // after decoding use existing functionality to write plain-text to BMP:
        BufferedImage outImg = plainDecoder.decodeBMP(plainText, inImg.getWidth(), inImg.getHeight());
        try {
            ImageIO.write(outImg, "bmp", new File(outBMPPath.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            asciiBytes = readCodeText(txtPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert equals(asciiBytes != null);

        // decode image and write BMP to disk:
        BufferedImage outImg = plainDecoder.decodeBMP(asciiBytes, inImg.getWidth(), inImg.getHeight());
        try {
            ImageIO.write(outImg, "bmp", new File(outBMPPath.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] readCodeText(Path path) throws IOException
    {
        byte[] bytes = Files.readAllBytes(path);  // assume there is correct number of bytes

        // trim EOL:
        if (bytes[bytes.length-1] == 0x0D)  // CR - macOS
            bytes = Arrays.copyOfRange(bytes, 0, bytes.length - 1);
        else if (bytes[bytes.length-1] == 0x0A)  // LF - Unix/Mac
            bytes = Arrays.copyOfRange(bytes, 0, bytes.length-1);
        else if (bytes[bytes.length-2] == 0x0D && bytes[bytes.length-1] == 0x0A)  // CR LF - Windows
            bytes = Arrays.copyOfRange(bytes, 0, bytes.length-2);

        return bytes;
    }
}
