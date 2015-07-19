package external.simulator.Simulator.parts;

import external.simulator.Simulator.parts.OrGateElm;

import java.util.StringTokenizer;

public class XorGateElm extends OrGateElm {
    public XorGateElm(int xx, int yy) {
        super(xx, yy);
    }

    public XorGateElm(int xa, int ya, int xb, int yb, int f,
                      StringTokenizer st) {
        super(xa, ya, xb, yb, f, st);
    }

    public String getGateName() {
        return "XOR gate";
    }

    public boolean calcFunction() {
        int i;
        boolean f = false;
        for (i = 0; i != inputCount; i++)
            f ^= getInput(i);
        return f;
    }

    public int getDumpType() {
        return 154;
    }

    public int getShortcut() {
        return '4';
    }
}
