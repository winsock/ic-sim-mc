package external.simulator.parts;

import external.simulator.CircuitElm;
import me.querol.andrew.ic.Gui.CircuitGUI;
import org.lwjgl.util.Point;

import java.awt.*;
import java.util.StringTokenizer;

// contributed by Edward Calver

public class AMElm extends CircuitElm {
	private static final int FLAG_COS = 2;
	private final int circleSize = 17;
	private final double carrierfreq;
	private final double signalfreq;
	private final double maxVoltage;
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

	public int getDumpType() {
		return 200;
	}
	/*void setCurrent(double c) {
	  current = c;
      System.out.print("v current set to " + c + "\n");
      }*/

	protected String dump() {
		return super.dump() + " " + carrierfreq + " " + signalfreq + " " + maxVoltage;
	}

	public void reset() {
		freqTimeZero = 0;
		curcount = 0;
	}

	protected int getPostCount() {
		return 1;
	}

	public void stamp() {
		sim.stampVoltageSource(0, nodes[0], voltSource);
	}

	public void doStep() {
		sim.updateVoltageSource(0, nodes[0], voltSource, getVoltage());
	}

	private double getVoltage() {
		double w = 2 * Math.PI * (sim.getTime() - freqTimeZero);
		return ((Math.sin(w * signalfreq) + 1) / 2) * Math.sin(w * carrierfreq) * maxVoltage;
	}

	public void draw(CircuitGUI.ClientCircuitGui screen, int mouseX, int mouseY, float partialTicks) {
		setBbox(point1, point2, circleSize);
		Color voltageColor = getVoltageColor(volts[0]);
		drawThickLine(screen, point1, lead1, voltageColor);

		Color color = needsHighlight() ? selectColor : whiteColor;
		String s = "AM";
		drawCenteredText(screen, s, x2, y2, true, color);
		drawWaveform(screen, point2);
		drawPosts(screen, color);
		curcount = updateDotCount(-current, curcount);
		if (sim.getDragElm() != this)
			drawDots(screen, point1, lead1, curcount);
	}

	private void drawWaveform(CircuitGUI.ClientCircuitGui g, Point center) {
		Color color = needsHighlight() ? selectColor : Color.gray;
		int xc = center.getX();
		int yc = center.getY();
		drawThickCircle(g, xc, yc, circleSize, color);
		adjustBbox(xc - circleSize, yc - circleSize, xc + circleSize, yc + circleSize);
	}

	protected void setPoints() {
		super.setPoints();
		lead1 = interpPoint(point1, point2, 1 - circleSize / dn);
	}

	protected double getVoltageDiff() {
		return volts[0];
	}

	protected boolean hasGroundConnection(int n1) {
		return true;
	}

	@Override
	protected boolean isWire() {
		return false;
	}

	protected int getVoltageSourceCount() {
		return 1;
	}

	protected double getPower() {
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
