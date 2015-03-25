package external.simulator.Simulator;


import me.querol.andrew.ic.Gui.CircuitGUI;
import org.lwjgl.util.Point;
import org.lwjgl.util.Color;

import java.util.StringTokenizer;

// contributed by Edward Calver

class FMElm extends CircuitElm {
    private static final int FLAG_COS = 2;
    private final int circleSize = 17;
    private double carrierfreq;
    private double signalfreq;
    private double maxVoltage;
    private double freqTimeZero;
    private double deviation;
    private double lasttime = 0;
    private double funcx = 0;

    public FMElm(int xx, int yy) {
        super(xx, yy);
        deviation = 200;
        maxVoltage = 5;
        carrierfreq = 800;
        signalfreq = 40;
        reset();
    }

    public FMElm(int xa, int ya, int xb, int yb, int f,
                 StringTokenizer st) {
        super(xa, ya, xb, yb, f);
        carrierfreq = new Double(st.nextToken());
        signalfreq = new Double(st.nextToken());
        maxVoltage = new Double(st.nextToken());
        deviation = new Double(st.nextToken());
        if ((flags & FLAG_COS) != 0) {
            flags &= ~FLAG_COS;
        }
        reset();
    }

    int getDumpType() {
        return 201;
    }

    void setCurrent(double c) {
      current = c;
      System.out.print("v current set to " + c + "\n");
    }

    String dump() {
        return super.dump() + " " + carrierfreq + " " + signalfreq + " " + maxVoltage + " " + deviation;
    }

    void reset() {
        freqTimeZero = 0;
        curcount = 0;
    }

    int getPostCount() {
        return 1;
    }

    void stamp() {
        sim.stampVoltageSource(0, nodes[0], voltSource);
    }

    void doStep() {
        sim.updateVoltageSource(0, nodes[0], voltSource, getVoltage());
    }

    double getVoltage() {
        double deltaT = sim.t - lasttime;
        lasttime = sim.t;
        double signalamplitude = Math.sin((2 * pi * (sim.t - freqTimeZero)) * signalfreq);
        funcx += deltaT * (carrierfreq + (signalamplitude * deviation));
        double w = 2 * pi * funcx;
        return Math.sin(w) * maxVoltage;
    }

    void draw(CircuitGUI g, int mouseX, int mouseY, float partialTicks) {
        setBbox(point1, point2, circleSize);

        drawThickLine(g, point1, lead1, (Color) getVoltageColor(volts[0]));

        Color color = (Color) (needsHighlight() ? selectColor : whiteColor);
        double v = getVoltage();
        String s = "FM";
        drawCenteredText(g, s, x2, y2, true, color);
        drawWaveform(g, point2);
        drawPosts(g, color);
        curcount = updateDotCount(-current, curcount);
        if (sim.dragElm != this)
            drawDots(g, point1, lead1, curcount);
    }

    void drawWaveform(CircuitGUI g, Point center) {
        Color color = (Color) (needsHighlight() ? selectColor : Color.GREY);
        int xc = center.getX();
        int yc = center.getY();
        drawThickCircle(g, xc, yc, circleSize, color);
        int wl = 8;
        adjustBbox(xc - circleSize, yc - circleSize,
                xc + circleSize, yc + circleSize);
    }


    void setPoints() {
        super.setPoints();
        lead1 = interpPoint(point1, point2, 1 - circleSize / dn);
    }

    double getVoltageDiff() {
        return volts[0];
    }

    boolean hasGroundConnection(int n1) {
        return true;
    }

    @Override
    boolean isWire() {
        return false;
    }

    int getVoltageSourceCount() {
        return 1;
    }

    double getPower() {
        return -getVoltageDiff() * current;
    }

    public void getInfo(String arr[]) {

        arr[0] = "FM Source";
        arr[1] = "I = " + getCurrentText(getCurrent());
        arr[2] = "V = " +
                getVoltageText(getVoltageDiff());
        arr[3] = "cf = " + getUnitText(carrierfreq, "Hz");
        arr[4] = "sf = " + getUnitText(signalfreq, "Hz");
        arr[5] = "dev =" + getUnitText(deviation, "Hz");
        arr[6] = "Vmax = " + getVoltageText(maxVoltage);
    }

    public EditInfo getEditInfo(int n) {
        if (n == 0)
            return new EditInfo("Max Voltage", maxVoltage, -20, 20);
        if (n == 1)
            return new EditInfo("Carrier Frequency (Hz)", carrierfreq, 4, 500);
        if (n == 2)
            return new EditInfo("Signal Frequency (Hz)", signalfreq, 4, 500);
        if (n == 3)
            return new EditInfo("Deviation (Hz)", deviation, 4, 500);

        return null;
    }

    public void setEditValue(int n, EditInfo ei) {
        if (n == 0)
            maxVoltage = ei.value;
        if (n == 1)
            carrierfreq = ei.value;
        if (n == 2)
            signalfreq = ei.value;
        if (n == 3)
            deviation = ei.value;
    }
}
