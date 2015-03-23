package external.simulator.Simulator;

import me.querol.andrew.ic.Gui.CircuitGUI;
import org.lwjgl.util.Color;
import org.lwjgl.util.Point;
import org.lwjgl.util.ReadableColor;

import java.util.StringTokenizer;

// contributed by Edward Calver

class AMElm extends CircuitElm {
    private static final int FLAG_COS = 2;
    private final int circleSize = 17;
    private double carrierfreq;
    private double signalfreq;
    private double maxVoltage;
    private double freqTimeZero;

    public AMElm(int xx, int yy) {
        super(xx, yy);
        maxVoltage = 5;
        carrierfreq = 1000;
        signalfreq = 40;
        reset();
    }

    public AMElm(int xa, int ya, int xb, int yb, int f,
                 StringTokenizer st) {
        super(xa, ya, xb, yb, f);
        carrierfreq = new Double(st.nextToken());
        signalfreq = new Double(st.nextToken());
        maxVoltage = new Double(st.nextToken());
        if ((flags & FLAG_COS) != 0) {
            flags &= ~FLAG_COS;
        }
        reset();
    }

    int getDumpType() {
        return 200;
    }
    /*void setCurrent(double c) {
      current = c;
      System.out.print("v current set to " + c + "\n");
      }*/

    String dump() {
        return super.dump() + " " + carrierfreq + " " + signalfreq + " " + maxVoltage;
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
        double w = 2 * pi * (sim.t - freqTimeZero);
        return ((Math.sin(w * signalfreq) + 1) / 2) * Math.sin(w * carrierfreq) * maxVoltage;
    }

    void draw(CircuitGUI screen, int mouseX, int mouseY, float partialTicks) {
        setBbox(point1, point2, circleSize);
        ReadableColor voltageColor = getVoltageColor(volts[0]);
        drawThickLine(screen, point1, lead1, (Color) voltageColor);

        Color color = (Color) (needsHighlight() ? selectColor : whiteColor);
        String s = "AM";
        drawCenteredText(screen, s, x2, y2, true, color);
        drawWaveform(screen, point2);
        drawPosts(screen, color);
        curcount = updateDotCount(-current, curcount);
        if (sim.dragElm != this)
            drawDots(screen, point1, lead1, curcount);
    }

    void drawWaveform(CircuitGUI g, Point center) {
        Color color = (Color) (needsHighlight() ? selectColor : Color.GREY);
        int xc = center.getX();
        int yc = center.getY();
        drawThickCircle(g, xc, yc, circleSize, color);
        adjustBbox(xc - circleSize, yc - circleSize, xc + circleSize, yc + circleSize);
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

        arr[0] = "AM Source";
        arr[1] = "I = " + getCurrentText(getCurrent());
        arr[2] = "V = " +
                getVoltageText(getVoltageDiff());
        arr[3] = "cf = " + getUnitText(carrierfreq, "Hz");
        arr[4] = "sf = " + getUnitText(signalfreq, "Hz");
        arr[5] = "VMax = " + getVoltageText(maxVoltage);
    }
}
