package com.company;

/**
 * Class to represent state machine transitions with it's output dependent on the input.
 */
public class StateTransitionLUT
{
    private final int       OUTPUT_IDX     = 0;
    private final int       NEXT_STATE_IDX = 1;
    private final IEncoder  encoder;
    private final int[][][] lut;  // [current state] [input bit] [output, next state]
    private final int       numStates;

    public StateTransitionLUT(IEncoder enc)
    {
        encoder = enc;
        numStates = (int) Math.pow(2, encoder.delay());
        lut = new int[numStates][2][2];
        fillLUT();
    }

    public int output(int previousState, int input)
    {
        return lut[previousState][input][OUTPUT_IDX];
    }

    public int nextState(int previousState, int input)
    {
        return lut[previousState][input][NEXT_STATE_IDX];
    }

    private void fillLUT()
    {
        int rgblsr;
        int output;
        for (int currentState = 0; currentState < numStates; ++currentState) {
            for (int input = 0; input < 2; ++input) {
                rgblsr = currentState | input << encoder.delay();  // input bit number 8 is 0 or 1
                lut[currentState][input][NEXT_STATE_IDX] = rgblsr >>> 1;
                output = encoder.g0(rgblsr);
                output <<= 1;
                if (encoder.withOutputBits()) {
                    output |= rgblsr & 0x01;
                    output <<= 1;
                }
                output |= encoder.g1(rgblsr);
                output <<= 1;
                output |= encoder.g2(rgblsr);
                lut[currentState][input][OUTPUT_IDX] = output;
            }
        }
    }
}
