package com.company;

/**
 * Class implementing Viterbi Hard Decision decoder.
 */
public class ViterbiDecoder implements IDecoder
{
    private IEncoder encoder;
    private int decodingDepth;
    private StateTransitionLUT transitions;

    public ViterbiDecoder(IEncoder e)
    {
        encoder = e;
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
    }
}
