package external.simulator.Simulator;


import org.lwjgl.util.Point;

import java.util.StringTokenizer;

class TunnelDiodeElm extends CircuitElm {
    private static final double pvp = .1;
    private static final double pip = 4.7e-3;
    private static final double pvv = .37;
    private static final double pvt = .026;
    private static final double pvpp = .525;
    private static final double piv = 370e-6;
    private final int hs = 8;
    //Polygon poly;
    private Point[] cathode;
    private double lastvoltdiff;

    public TunnelDiodeElm(int xx, int yy) {
        super(xx, yy);
        setup();
    }

    public TunnelDiodeElm(int xa, int ya, int xb, int yb, int f,
                          StringTokenizer st) {
        super(xa, ya, xb, yb, f);
        setup();
    }

    boolean nonLinear() {
        return true;
    }

    void setup() {
    }

    int getDumpType() {
        return 175;
    }

    void setPoints() {
        super.setPoints();
        calcLeads(16);
        cathode = newPointArray(4);
        Point pa[] = newPointArray(2);
        interpPoint2(lead1, lead2, pa[0], pa[1], 0, hs);
        interpPoint2(lead1, lead2, cathode[0], cathode[1], 1, hs);
        interpPoint2(lead1, lead2, cathode[2], cathode[3], .8, hs);
//        poly = createPolygon(pa[0], pa[1], lead2);
    }

/*    void draw(CircuitGUI g) {
        setBbox(point1, point2, hs);

        double v1 = volts[0];
        double v2 = volts[1];

        draw2Leads(g);

        // draw arrow thingy
        getPowerColor(g, true);
        getVoltageColor(g, v1);
        g.fillPolygon(poly);

        // draw thing arrow is pointing to
        getVoltageColor(g, v2);
        drawThickLine(g, cathode[0], cathode[1]);
        drawThickLine(g, cathode[2], cathode[0]);
        drawThickLine(g, cathode[3], cathode[1]);

        doDots(g);
        drawPosts(g);
    }*/

    void reset() {
        lastvoltdiff = volts[0] = volts[1] = curcount = 0;
    }

    double limitStep(double vnew, double vold) {
        // Prevent voltage changes of more than 1V when iterating.  Wow, I thought it would be
        // much harder than this to prevent convergence problems.
        if (vnew > vold + 1)
            return vold + 1;
        if (vnew < vold - 1)
            return vold - 1;
        return vnew;
    }

    void stamp() {
        sim.stampNonLinear(nodes[0]);
        sim.stampNonLinear(nodes[1]);
    }

    void doStep() {
        double voltdiff = volts[0] - volts[1];
        if (Math.abs(voltdiff - lastvoltdiff) > .01)
            sim.converged = false;
        //System.out.println(voltdiff + " " + lastvoltdiff + " " + Math.abs(voltdiff-lastvoltdiff));
        voltdiff = limitStep(voltdiff, lastvoltdiff);
        lastvoltdiff = voltdiff;

        double i = pip * Math.exp(-pvpp / pvt) * (Math.exp(voltdiff / pvt) - 1) +
                pip * (voltdiff / pvp) * Math.exp(1 - voltdiff / pvp) +
                piv * Math.exp(voltdiff - pvv);

        double geq = pip * Math.exp(-pvpp / pvt) * Math.exp(voltdiff / pvt) / pvt +
                pip * Math.exp(1 - voltdiff / pvp) / pvp
                - Math.exp(1 - voltdiff / pvp) * pip * voltdiff / (pvp * pvp) +
                Math.exp(voltdiff - pvv) * piv;
        double nc = i - geq * voltdiff;
        sim.stampConductance(nodes[0], nodes[1], geq);
        sim.stampCurrentSource(nodes[0], nodes[1], nc);
    }

    void calculateCurrent() {
        double voltdiff = volts[0] - volts[1];
        current = pip * Math.exp(-pvpp / pvt) * (Math.exp(voltdiff / pvt) - 1) +
                pip * (voltdiff / pvp) * Math.exp(1 - voltdiff / pvp) +
                piv * Math.exp(voltdiff - pvv);
    }

    public void getInfo(String arr[]) {
        arr[0] = "tunnel diode";
        arr[1] = "I = " + getCurrentText(getCurrent());
        arr[2] = "Vd = " + getVoltageText(getVoltageDiff());
        arr[3] = "P = " + getUnitText(getPower(), "W");
    }

    @Override
    boolean isWire() {
        return false;
    }
}
