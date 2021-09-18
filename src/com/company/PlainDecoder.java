package com.company;

import java.awt.image.BufferedImage;
import java.util.LinkedList;

public class PlainDecoder implements IPlainDecoder
{
    @Override
    public BufferedImage decodeBMP(byte[] asciiBytes, int width, int height)
    {
        assert equals(asciiBytes != null);
        assert equals(asciiBytes.length == width * height * ViterbiBMP.BITS_PER_PIXEL);

        BufferedImage outImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Take 24 ones and zeroes and make one rgb pixel:
        LinkedList<Integer> rgbList = new LinkedList<>();
        for (int i = 0; i < asciiBytes.length; i += ViterbiBMP.BITS_PER_PIXEL) {
            int tempRGB = Integer.parseInt(new String(asciiBytes, i, ViterbiBMP.BITS_PER_PIXEL), 2);
            rgbList.add(tempRGB);
        }
        assert rgbList.size() == width * height;

        for (int h = 0; h < height; ++h){
            for (int w = 0; w < width; ++w){
                outImg.setRGB(w, h, rgbList.poll());
            }
        }

        return outImg;
    }
}
