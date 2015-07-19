package external.simulator.Simulator.parts;

import external.simulator.Simulator.CircuitElm;

import java.util.StringTokenizer;

public class AntennaElm extends RailElm {
    private double fmphase;

    public AntennaElm(int xx, int yy) {
        super(xx, yy, VoltageElm.WF_DC);
    }

    public AntennaElm(int xa, int ya, int xb, int yb, int f,
                      StringTokenizer st) {
        super(xa, ya, xb, yb, f, st);
        waveform = VoltageElm.WF_DC;
    }

    public void stamp() {
        CircuitElm.sim.stampVoltageSource(0, nodes[0], voltSource);
    }

    public void doStep() {
        CircuitElm.sim.updateVoltageSource(0, nodes[0], voltSource, getVoltage());
    }

    protected double getVoltage() {
        fmphase += 2 * Math.PI * (2200 + Math.sin(2 * Math.PI * CircuitElm.sim.getTime() * 13) * 100) * CircuitElm.sim.getTimeStep();
        double fm = 3 * Math.sin(fmphase);
        return Math.sin(2 * Math.PI * CircuitElm.sim.getTime() * 3000) * (1.3 + Math.sin(2 * Math.PI * CircuitElm.sim.getTime() * 12)) * 3 +
                Math.sin(2 * Math.PI * CircuitElm.sim.getTime() * 2710) * (1.3 + Math.sin(2 * Math.PI * CircuitElm.sim.getTime() * 13)) * 3 +
                Math.sin(2 * Math.PI * CircuitElm.sim.getTime() * 2433) * (1.3 + Math.sin(2 * Math.PI * CircuitElm.sim.getTime() * 14)) * 3 + fm;
    }

    public int getDumpType() {
        return 'A';
    }

    public int getShortcut() {
        return 0;
    }
}
