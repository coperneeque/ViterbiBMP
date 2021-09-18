package com.company;

import java.awt.image.BufferedImage;

public class PlainEncoder implements IPlainEncoder
{
    @Override
    public String encode(BufferedImage inImg) throws NullPointerException
    {
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

        return sb.toString();
    }
}
