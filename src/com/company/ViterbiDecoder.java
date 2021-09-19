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
    private       int                MAX_PATH_METRIC;
    private       int                MASK;
    private       int                numStates;
    private       int[][]            pathMetric;
    private       int[][]            pathPreviousVertex;
    private final StateTransitionLUT transitions;
    private       int                terminalState = 0;

    public ViterbiDecoder(IEncoder e)
    {
        encoder = e;
        numStates = (int) Math.pow(2, encoder.delay());
        MASK = numStates - 1;
        transitions = new StateTransitionLUT(encoder);
    }

    /**
     *
     * Viterbi Hard Decision decoder.
     * Decodes the signal and stores in memory in byte array.
     *
     * @param codeText byte array containig code-text in '0' and '1' form
     * @param width Original image width
     * @param height Original image height
     */
    @Override
    public byte[] decodeIS95(byte[] codeText, int width, int height)
    {
        int[] signalChunks = parseToIntArray(codeText);

        StringBuilder sb = new StringBuilder();

        int prevState = terminalState;  // previous state machine state, one of 0 to 2^IS95_DELAY-1
        for (int i = 0; i < signalChunks.length; i += decodingDepth) {
            /*
             * During final iteration the remaining available signal might be shorter than required for the expected decoding depth.
             * That is not a problem because the copyOfRange() method automatically pads with zeroes.
             * Later we will trim the excess.
             */
            int[] toDecode = Arrays.copyOfRange(signalChunks, i, i + decodingDepth);
            initViterbi(prevState, toDecode);
            viterbi(toDecode);
            sb.append(originalMessage());
            prevState = terminalState;
        }

        byte[] plainText = sb.toString().getBytes(StandardCharsets.UTF_8);
        /*
         * We are expecting height × width × BITS_PER_PIXEL bytes of image data (for bee100.bmp it's 978000 bytes).
         * Trim the excess if padding occurred.
         */
        if (plainText.length > height * width * BITS_PER_PIXEL)
            plainText = Arrays.copyOf(plainText, height * width * BITS_PER_PIXEL);

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
        MAX_PATH_METRIC = encoder.outputLength() * decodingDepth;
        pathMetric = new int[numStates][decodingDepth];
        pathPreviousVertex = new int[numStates][decodingDepth];
    }

    private int hamming(int a, int b)
    {
        return Integer.bitCount(a ^ b);
    }

    private void initViterbi(int startingState, int[] signalChunks)
    {
        // init 2 first possible edges (depth 1):
        wipePathMetric();
        pathMetric[startingState >>> 1][0] = hamming(signalChunks[0], transitions.output(startingState, 0));
        pathMetric[startingState >>> 1 | 0x01 << encoder.delay() - 1][0] = hamming(signalChunks[0], transitions.output(startingState, 1));

        // init previous vertex for first 2 possible edges (depth 1):
        wipePathPreviousVertex();
        pathPreviousVertex[startingState >>> 1][0] = startingState;
        pathPreviousVertex[startingState >>> 1 | 0x01 << encoder.delay() - 1][0] = startingState;
    }

    private String originalMessage()
    {
        // find terminal state with lowest path metric
        terminalState = 0;
        for (int state = 0; state < numStates; ++state) {
            if (pathMetric[state][decodingDepth-1] < pathMetric[terminalState][decodingDepth-1]) {
                terminalState = state;
            }
        }

        // read the path from terminal state back to starting state:
        int[] viterbiPath = new int[decodingDepth];
        int[] encoderOutput = new int[decodingDepth];
        int[] origMessage = new int[decodingDepth];

        int currentVertex = terminalState;
        int prevVertex;
        int input;
        for (int depth = decodingDepth - 1; depth >= 0; --depth) {
            prevVertex = pathPreviousVertex[currentVertex][depth];
            if (transitions.nextState(prevVertex, 0) == currentVertex) {
                input = 0;
            } else if (transitions.nextState(prevVertex, 1) == currentVertex) {
                input = 1;
            } else throw new NoSuchElementException("Impossible IS-95 state machine transition from state " + prevVertex + " to state " + currentVertex + " in Viterbi path at depth " + depth);
            viterbiPath[depth] = currentVertex;
            encoderOutput[depth] = transitions.output(prevVertex, input);
            origMessage[depth] = input;
            currentVertex = prevVertex;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < decodingDepth; ++i) {
//            sb.append(Integer.toBinaryString(origMessage[i]));
            sb.append(Integer.toBinaryString(origMessage[decodingDepth -1 -i]));
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

    private void viterbi(int[] signalChunks)
    {
        int depthIndex;  // hopefully less errors
        int prev0, prev1;  // two possible previous vertices
        int input;  // one possible input to get to current state
        int metric0, metric1;
        for (int depth = 2; depth <= decodingDepth; ++depth) {  // begin with depth 2
            depthIndex = depth  - 1;
            for (int state = 0; state < numStates; ++state) {
                prev0 = state << 1 & MASK;  // fixme
                prev1 = state << 1 & MASK | 0x01;
                if (pathPreviousVertex[prev0][depthIndex - 1] > -1 || pathPreviousVertex[prev1][depthIndex - 1] > -1) {  // path to one of previous vertices exists
                    input = state >>> encoder.delay() - 1;
                    metric0 = pathMetric[prev0][depthIndex - 1] + hamming(signalChunks[depthIndex], transitions.output(prev0, input));
                    metric1 = pathMetric[prev1][depthIndex - 1] + hamming(signalChunks[depthIndex], transitions.output(prev1, input));
                    if (metric0 < metric1) {
                        pathMetric[state][depthIndex] = metric0;
                        pathPreviousVertex[state][depthIndex] = prev0;
                    }
                    else {
                        pathMetric[state][depthIndex] = metric1;
                        pathPreviousVertex[state][depthIndex] = prev1;
                    }
                }
            }
        }
    }

    /**
     * No paths exist in the beginning. All metrics are set to maximum + 1.
     */
    private void wipePathMetric()
    {
        for (int i = 0; i < numStates; ++i) {
            Arrays.fill(pathMetric[i], MAX_PATH_METRIC + 1);
        }
    }

    /**
     * No paths exist in the beginning. All previous vertices are set to -1.
     */
    private void wipePathPreviousVertex()
    {
        for (int i = 0; i < numStates; ++i) {
            Arrays.fill(pathPreviousVertex[i], -1);
        }
    }
}
