package com.company;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main
{
    public static void main(String[] args)
    {
        Path imagePath = Paths.get("assets", "bee100.bmp");
        if (args.length > 1) imagePath = Paths.get(args[1]);

        Path textPath = Paths.get("assets", "bee100.txt");
        if (args.length > 2) textPath = Paths.get(args[2]);

        Path outImgPath = Paths.get("assets", "out100.bmp");
        if (args.length > 3) outImgPath = Paths.get(args[3]);

        int errorRate = 5;
        if (args.length > 4) errorRate = Integer.parseInt(args[4]);

        Path outIS95Path = Paths.get("assets", "outIS95.txt");
        if (args.length > 5) outImgPath = Paths.get(args[5]);

        int decodingDepth = 24;
        if (args.length > 6) decodingDepth = Integer.parseInt(args[6]);

        Path decodedImgPath = Paths.get("assets", "outIS95.bmp");
        if (args.length > 7) decodedImgPath = Paths.get(args[7]);

        IPlainEncoder plainEncoder = new PlainEncoder();
        IPlainDecoder plainDecoder = new PlainDecoder();
        INoiseGenerator noiseGenerator = new NoiseGenerator();
        noiseGenerator.setErrorRate(errorRate);
        IEncoder is95Encoder = new EncoderIS95();
        ViterbiBMP viterbi = new ViterbiBMP(plainEncoder, plainDecoder, noiseGenerator, is95Encoder);
        viterbi.runPlain(imagePath, textPath, outImgPath);
        viterbi.runIS95(imagePath, outIS95Path, decodedImgPath);
    }
}
