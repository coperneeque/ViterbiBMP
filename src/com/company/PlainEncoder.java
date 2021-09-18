package com.company;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class PlainEncoder implements IPlainEncoder
{
    @Override
    public String encode(BufferedImage inImg) throws NullPointerException
    {
        if (inImg == null) { throw new NullPointerException("Load BMP image from disk first!"); }

        StringBuilder sb = new StringBuilder();
        int rgb;
        String binString;
        for (int h = 0; h < inImg.getHeight(); ++h){
            for (int w = 0; w < inImg.getWidth(); ++w){
                rgb = inImg.getRGB(w, h);
                binString = Integer.toBinaryString(rgb);
                sb.append(binString.substring(8));
            }
        }

//        if (noiseGenerator != null) {
//            s = noiseGenerator.noisify(s);
//        }
        return sb.toString();
    }

    @Override
    public BufferedImage readBMP(Path path) throws IOException
    {
        return ImageIO.read(new File(String.valueOf(path)));
    }
}
