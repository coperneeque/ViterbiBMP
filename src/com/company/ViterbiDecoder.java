package com.company;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.NoSuchElementException;

import static com.company.ViterbiBMP.BITS_PER_PIXEL;

/**
 * Class implementing Viterbi Hard Decision decoder.
 */
public class ViterbiDecoder implements IDecoder
{
    private       int                decodingDepth;
    private final IEncoder           encoder;
    private       int                fullPathMetric;
    private       int                MAX_PATH_METRIC;
    private final int                MASK;
    private final int                NUM_STATES;
    private       int[][]            pathMetric;
    private       int[][]            pathPreviousNode;
    private final StateTransitionLUT transitions;
    private       int                terminalNode = 0;

    public ViterbiDecoder(IEncoder e)
    {
        encoder     = e;
        NUM_STATES  = (int) Math.pow(2, encoder.delay());
        MASK        = NUM_STATES - 1;
        transitions = new StateTransitionLUT(encoder);
    }

    @Override
    public byte[] decodeIS95(byte[] codeText, int numPixels)
    {
        int[] signalChunks = parseToIntArray(codeText);

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < signalChunks.length; i += decodingDepth) {
            /*
             * During final iteration the remaining available signal might be shorter than required for the expected decoding depth.
             * That is not a problem because the copyOfRange() method automatically pads with zeroes.
             * Later we will trim the excess.
             */
            int[] toDecode = Arrays.copyOfRange(signalChunks, i, i + decodingDepth);
            initViterbi(terminalNode, toDecode);
            viterbi(toDecode);
            sb.append(originalMessage());
        }

//        byte[] plainText = sb.reverse().toString().getBytes(StandardCharsets.UTF_8);
        byte[] plainText = sb.toString().getBytes(StandardCharsets.UTF_8);
        /*
         * We are expecting height × width × BITS_PER_PIXEL bytes of image data (for bee100.bmp it's 978000 bytes).
         * Trim the excess if padding occurred.
         */
        if (plainText.length > numPixels * BITS_PER_PIXEL)
            plainText = Arrays.copyOf(plainText, numPixels * BITS_PER_PIXEL);

        System.out.println("[ ViterbiDecoder ] Full path metric: " + fullPathMetric);
        return plainText;
    }

    @Override
    public void setDecodingDepth(int d)
    {
        if (d < encoder.delay()) {
            System.out.println("[ ViterbiDecoder ] You requested decodingDepth " + d + "." +
                               " We need at least " + encoder.delay() + " steps to reach all state machine states." +
                               " We now set the decodingDepth to " + encoder.delay() + "." +
                               " You should request a multiple of that");
            decodingDepth = encoder.delay();
        } else {
            decodingDepth = d;
        }
        MAX_PATH_METRIC  = encoder.outputLength() * decodingDepth;
        pathMetric       = new int[NUM_STATES][decodingDepth];
        pathPreviousNode = new int[NUM_STATES][decodingDepth];
    }

    private int hamming(int a, int b)
    {
        return Integer.bitCount(a ^ b);
    }

    private void initViterbi(int startingNode, int[] toDecode)
    {
        // init 2 first possible edges (depth 1):
        wipePathMetric();
        pathMetric[startingNode >>> 1][0] = hamming(toDecode[0], transitions.output(startingNode, 0));
        pathMetric[startingNode >>> 1 | 0x01 << encoder.delay() - 1][0] = hamming(toDecode[0], transitions.output(startingNode, 1));

        // init previous vertex for first 2 possible edges (depth 1):
        wipePathPreviousNode();
        pathPreviousNode[startingNode >>> 1][0] = startingNode;
        pathPreviousNode[startingNode >>> 1 | 0x01 << encoder.delay() - 1][0] = startingNode;
    }

    private String originalMessage()
    {
        // find terminal node with lowest path metric
        terminalNode = 0;
        for (int node = 0; node < NUM_STATES; ++node) {
            if (pathMetric[node][decodingDepth-1] < pathMetric[terminalNode][decodingDepth - 1]) {
                terminalNode = node;
            }
        }

        fullPathMetric += pathMetric[terminalNode][decodingDepth -1];

        // read the path from terminal node back to starting node:
        int[] origMessage = new int[decodingDepth];
        int currentNode = terminalNode;
        int prevNode;
        int input;
        for (int depth = decodingDepth - 1; depth >= 0; --depth) {
            prevNode = pathPreviousNode[currentNode][depth];
            if (transitions.nextState(prevNode, 0) == currentNode) {
                input = 0;
            } else if (transitions.nextState(prevNode, 1) == currentNode) {
                input = 1;
            } else throw new NoSuchElementException("Impossible IS-95 state machine transition from state " + prevNode + " to state " + currentNode + " in Viterbi path at depth " + depth);
            origMessage[depth] = input;
            currentNode = prevNode;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < decodingDepth; ++i) {
            sb.append(Integer.toBinaryString(origMessage[i]));
//            sb.append(Integer.toBinaryString(origMessage[decodingDepth -1 -i]));
        }

        return sb.toString();
    }

    /**
     *
     * Convert received signal in ascii form into int array.
     * Individual bits of each int represent consecutive '0's and '1's of the source.
     * Least significant bits of each int are equivalent to IS95_OUTPUT_LENGTH consecutive bits.
     * Symbols received earlier occupy more significant bits.
     * Remaining most significant bits are padded with 0.
     * Assume source byte array contains only '0's and '1's and is correct size.
     * Signed int are used because the bits are not affected.
     *
     * @param src Source byte array
     * @return Array of int
     */
    private int[] parseToIntArray(byte[] src)
    {
        int[] ret = new int[src.length / encoder.outputLength()];

        for (int i = 0; i < src.length; i += encoder.outputLength()) {
            ret[i / encoder.outputLength()] = new BigInteger(new String(src, i, encoder.outputLength()), 2).intValue();
        }

        return ret;
    }

    private void viterbi(int[] toDecode)
    {
        int prev0, prev1;  // two possible previous vertices
        int input;  // one possible input to get to current state
        int metric0, metric1;
        for (int depth = 2; depth <= decodingDepth; ++depth) {  // begin with depth 2
            for (int state = 0; state < NUM_STATES; ++state) {
                prev0 = state << 1 & MASK;
                prev1 = state << 1 & MASK | 0x01;
                if (pathPreviousNode[prev0][depth - 2] > -1 || pathPreviousNode[prev1][depth - 2] > -1) {  // path to one of previous vertices exists
                    input = state >>> encoder.delay() - 1;
                    metric0 = pathMetric[prev0][depth - 2] + hamming(toDecode[depth - 1], transitions.output(prev0, input));
                    metric1 = pathMetric[prev1][depth - 2] + hamming(toDecode[depth - 1], transitions.output(prev1, input));
                    if (metric0 < metric1) {
                        pathMetric[state][depth - 1]       = metric0;
                        pathPreviousNode[state][depth - 1] = prev0;
                    }
                    else {
                        pathMetric[state][depth - 1]       = metric1;
                        pathPreviousNode[state][depth - 1] = prev1;
                    }
                }
            }
        }
    }

    /*
     * No paths exist in the beginning. All metrics are set to maximum + 1.
     */
    private void wipePathMetric()
    {
        for (int i = 0; i < NUM_STATES; ++i) {
            Arrays.fill(pathMetric[i], MAX_PATH_METRIC + 1);
        }
    }

    /*
     * No paths exist in the beginning. All previous vertices are set to -1.
     */
    private void wipePathPreviousNode()
    {
        for (int i = 0; i < NUM_STATES; ++i) {
            Arrays.fill(pathPreviousNode[i], -1);
        }
    }
}
