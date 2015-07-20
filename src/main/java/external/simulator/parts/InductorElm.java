package external.simulator.parts;

import external.simulator.CircuitElm;
import external.simulator.EditInfo;
import external.simulator.Toggleable;
import me.querol.andrew.ic.Gui.CircuitGUI;

import java.awt.*;
import java.util.StringTokenizer;

public class InductorElm extends CircuitElm {
	private final Inductor ind;
	private double inductance;

	public InductorElm(int xx, int yy) {
		super(xx, yy);
		ind = new Inductor(sim);
		inductance = 1;
		ind.setup(inductance, current, flags);
	}

	public InductorElm(int xa, int ya, int xb, int yb, int f,
	                   StringTokenizer st) {
		super(xa, ya, xb, yb, f);
		ind = new Inductor(sim);
		inductance = new Double(st.nextToken());
		current = new Double(st.nextToken());
		ind.setup(inductance, current, flags);
	}

	public int getDumpType() {
		return 'l';
	}

	protected String dump() {
		return super.dump() + " " + inductance + " " + current;
	}

	protected void setPoints() {
		super.setPoints();
		calcLeads(32);
	}

	public double getInductance() {
		return inductance;
	}

	public void draw(CircuitGUI.ClientCircuitGui g, int mouseX, int mouseY, float partialTicks) {
		double v1 = volts[0];
		double v2 = volts[1];
		int i;
		int hs = 8;
		setBbox(point1, point2, hs);
		draw2Leads(g, Color.lightGray);
		drawCoil(g, 8, lead1, lead2, v1, v2, Color.lightGray);
/*        if (sim.showValuesCheckItem.getState()) {
            String s = getShortUnitText(inductance, "H");
            drawValues(g, s, hs);
        }*/
		doDots(g);
		drawPosts(g, Color.lightGray);
	}

	public void reset() {
		current = volts[0] = volts[1] = curcount = 0;
		ind.reset();
	}

	public void stamp() {
		ind.stamp(nodes[0], nodes[1]);
	}

	protected void startIteration() {
		ind.startIteration(volts[0] - volts[1]);
	}

	protected boolean nonLinear() {
		return ind.nonLinear();
	}

	protected void calculateCurrent() {
		double voltdiff = volts[0] - volts[1];
		current = ind.calculateCurrent(voltdiff);
	}

	public void doStep() {
		double voltdiff = volts[0] - volts[1];
		ind.doStep(voltdiff);
	}

	public void getInfo(String arr[]) {
		arr[0] = "inductor";
		getBasicInfo(arr);
		arr[3] = "L = " + getUnitText(inductance, "H");
		arr[4] = "P = " + getUnitText(getPower(), "W");
	}

	public EditInfo getEditInfo(int n) {
		if (n == 0)
			return new EditInfo("Inductance (H)", inductance, 0, 0);
		if (n == 1) {
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.setCheckbox(new Toggleable("Trapezoidal Approximation",
				ind.isTrapezoidal()));
			return ei;
		}
		return null;
	}

	public void setEditValue(int n, EditInfo ei) {
		if (n == 0)
			inductance = ei.getValue();
		if (n == 1) {
			if (ei.getCheckbox().getState())
				flags &= ~Inductor.FLAG_BACK_EULER;
			else
				flags |= Inductor.FLAG_BACK_EULER;
		}
		ind.setup(inductance, current, flags);
	}

	@Override
	protected boolean isWire() {
		return false;
	}
}
