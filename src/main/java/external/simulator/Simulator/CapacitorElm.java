package external.simulator.Simulator;

import me.querol.andrew.ic.Gui.CircuitGUI;
import org.lwjgl.util.Point;

import java.awt.*;
import java.util.StringTokenizer;

public class CapacitorElm extends CircuitElm {
	private static final int FLAG_BACK_EULER = 2;
	double capacitance;
	private double compResistance;
	private double voltdiff;
	private Point[] plate1;
	private Point[] plate2;
	private double curSourceValue;

	public CapacitorElm(int xx, int yy) {
		super(xx, yy);
		capacitance = 1e-5;
	}

	public CapacitorElm(int xa, int ya, int xb, int yb, int f,
	                    StringTokenizer st) {
		super(xa, ya, xb, yb, f);
		capacitance = new Double(st.nextToken());
		voltdiff = new Double(st.nextToken());
	}

	boolean isTrapezoidal() {
		return (flags & FLAG_BACK_EULER) == 0;
	}

	void setNodeVoltage(int n, double c) {
		super.setNodeVoltage(n, c);
		voltdiff = volts[0] - volts[1];
	}

	void reset() {
		current = curcount = 0;
		// put small charge on caps when reset to start oscillators
		voltdiff = 1e-3;
	}

	int getDumpType() {
		return 'c';
	}

	String dump() {
		return super.dump() + " " + capacitance + " " + voltdiff;
	}

	void setPoints() {
		super.setPoints();
		double f = (dn / 2 - 4) / dn;
		// calc leads
		lead1 = interpPoint(point1, point2, f);
		lead2 = interpPoint(point1, point2, 1 - f);
		// calc plates
		plate1 = newPointArray(2);
		plate2 = newPointArray(2);
		interpPoint2(point1, point2, plate1[0], plate1[1], f, 12);
		interpPoint2(point1, point2, plate2[0], plate2[1], 1 - f, 12);
	}

	public void draw(CircuitGUI.ClientCircuitGui g, int mouseX, int mouseY, float partialTicks) {
		int hs = 12;
		setBbox(point1, point2, hs);

		// draw first lead and plate
		drawThickLine(g, point1, lead1, getVoltageColor(volts[0]));
		drawThickLine(g, plate1[0], plate1[1], Color.YELLOW);

		// draw second lead and plate
		drawThickLine(g, point2, lead2, getVoltageColor(volts[1]));
		drawThickLine(g, plate2[0], plate2[1], Color.YELLOW);

		updateDotCount();
		if (sim.dragElm != this) {
			drawDots(g, point1, lead1, curcount);
			drawDots(g, point2, lead2, -curcount);
		}
		drawPosts(g, Color.lightGray);
/*        if (sim.showValuesCheckItem.getState()) {
            String s = getShortUnitText(capacitance, "F");
            drawValues(g, s, hs, (Color) CircuitElm.whiteColor);
        }*/
	}

	void stamp() {
		// capacitor companion model using trapezoidal approximation
		// (Norton equivalent) consists of a current source in
		// parallel with a resistor.  Trapezoidal is more accurate
		// than backward euler but can cause oscillatory behavior
		// if RC is small relative to the timestep.
		if (isTrapezoidal())
			compResistance = sim.timeStep / (2 * capacitance);
		else
			compResistance = sim.timeStep / capacitance;
		sim.stampResistor(nodes[0], nodes[1], compResistance);
		sim.stampRightSide(nodes[0]);
		sim.stampRightSide(nodes[1]);
	}

	void startIteration() {
		if (isTrapezoidal())
			curSourceValue = -voltdiff / compResistance - current;
		else
			curSourceValue = -voltdiff / compResistance;
		//System.out.println("cap " + compResistance + " " + curSourceValue + " " + current + " " + voltdiff);
	}

	void calculateCurrent() {
		double voltdiff = volts[0] - volts[1];
		// we check compResistance because this might get called
		// before stamp(), which sets compResistance, causing
		// infinite current
		if (compResistance > 0)
			current = voltdiff / compResistance + curSourceValue;
	}

	void doStep() {
		sim.stampCurrentSource(nodes[0], nodes[1], curSourceValue);
	}

	public void getInfo(String arr[]) {
		arr[0] = "capacitor";
		getBasicInfo(arr);
		arr[3] = "C = " + getUnitText(capacitance, "F");
		arr[4] = "P = " + getUnitText(getPower(), "W");
		//double v = getVoltageDiff();
		//arr[4] = "U = " + getUnitText(.5*capacitance*v*v, "J");
	}

	public EditInfo getEditInfo(int n) {
		if (n == 0)
			return new EditInfo("Capacitance (F)", capacitance, 0, 0);
		if (n == 1) {
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.checkbox = new Toggleable("Trapezoidal Approximation", isTrapezoidal());
			return ei;
		}
		return null;
	}

	public void setEditValue(int n, EditInfo ei) {
		if (n == 0 && ei.value > 0)
			capacitance = ei.value;
		if (n == 1) {
			if (ei.checkbox.getState())
				flags &= ~FLAG_BACK_EULER;
			else
				flags |= FLAG_BACK_EULER;
		}
	}

	@Override
	boolean isWire() {
		return false;
	}

	int getShortcut() {
		return 'c';
	}
}
