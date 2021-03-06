package external.simulator.parts;

import external.simulator.CircuitElm;
import external.simulator.EditInfo;
import me.querol.andrew.ic.Gui.CircuitGUI;
import org.lwjgl.util.Point;

import java.awt.*;
import java.util.StringTokenizer;

public class DiodeElm extends CircuitElm {
	static final int FLAG_FWDROP = 1;
	final Diode diode;
	private final double defaultdrop = .805904783;
	private final int hs = 8;
	double fwdrop, zvoltage;
	private Polygon poly;
	private Point[] cathode;

	public DiodeElm(int xx, int yy) {
		super(xx, yy);
		diode = new Diode(sim);
		fwdrop = defaultdrop;
		zvoltage = 0;
		setup();
	}

	public DiodeElm(int xa, int ya, int xb, int yb, int f,
	                StringTokenizer st) {
		super(xa, ya, xb, yb, f);
		diode = new Diode(sim);
		fwdrop = defaultdrop;
		zvoltage = 0;
		if ((f & FLAG_FWDROP) > 0) {
			try {
				fwdrop = new Double(st.nextToken());
			} catch (Exception ignored) {
			}
		}
		setup();
	}

	protected boolean nonLinear() {
		return true;
	}

	protected void setup() {
		diode.setup(fwdrop, zvoltage);
	}

	public int getDumpType() {
		return 'd';
	}

	protected String dump() {
		flags |= FLAG_FWDROP;
		return super.dump() + " " + fwdrop;
	}

	protected void setPoints() {
		super.setPoints();
		calcLeads(16);
		cathode = newPointArray(2);
		Point pa[] = newPointArray(2);
		interpPoint2(lead1, lead2, pa[0], pa[1], 0, hs);
		interpPoint2(lead1, lead2, cathode[0], cathode[1], 1, hs);
		poly = createPolygon(pa[0], pa[1], lead2);
	}

	public void draw(CircuitGUI.ClientCircuitGui g, int mouseX, int mouseY, float partialTicks) {
		drawDiode(g);
		doDots(g);
		drawPosts(g, Color.lightGray);
	}

	public void reset() {
		diode.reset();
		volts[0] = volts[1] = curcount = 0;
	}

	private void drawDiode(CircuitGUI.ClientCircuitGui g) {
		setBbox(point1, point2, hs);

		double v1 = volts[0];
		double v2 = volts[1];

		draw2Leads(g, getVoltageColor(v1));
		// draw arrow thingy
		drawPolygon(g, poly, getVoltageColor(v1));
		drawThickLine(g, cathode[0], cathode[1], getVoltageColor(v2));
	}

	public void stamp() {
		diode.stamp(nodes[0], nodes[1]);
	}

	public void doStep() {
		diode.doStep(volts[0] - volts[1]);
	}

	protected void calculateCurrent() {
		current = diode.calculateCurrent(volts[0] - volts[1]);
	}

	public void getInfo(String arr[]) {
		arr[0] = "diode";
		arr[1] = "I = " + getCurrentText(getCurrent());
		arr[2] = "Vd = " + getVoltageText(getVoltageDiff());
		arr[3] = "P = " + getUnitText(getPower(), "W");
		arr[4] = "Vf = " + getVoltageText(fwdrop);
	}

	public EditInfo getEditInfo(int n) {
		if (n == 0)
			return new EditInfo("Fwd Voltage @ 1A", fwdrop, 10, 1000);
		return null;
	}

	public void setEditValue(int n, EditInfo ei) {
		fwdrop = ei.getValue();
		setup();
	}

	@Override
	protected boolean isWire() {
		return false;
	}

	public int getShortcut() {
		return 'd';
	}
}
