package external.simulator.parts;// stub ThermistorElm based on SparkGapElm
// FIXME need to uncomment ThermistorElm line from CirSim.java
// FIXME need to add ThermistorElm.java to srclist

import external.simulator.CirSim;
import external.simulator.CircuitElm;
import external.simulator.EditInfo;
import me.querol.andrew.ic.Gui.CircuitGUI;
import org.lwjgl.util.Point;

import java.util.StringTokenizer;

public class ThermistorElm extends CircuitElm {
	private double minresistance;
	private double maxresistance;
	private double resistance;
	/*    Scrollbar slider;
		Label label;*/
	private Point ps3;
	private Point ps4;

	public ThermistorElm(int xx, int yy) {
		super(xx, yy);
		maxresistance = 1e9;
		minresistance = 1e3;
		//createSlider();
	}

	public ThermistorElm(int xa, int ya, int xb, int yb, int f,
	                     StringTokenizer st) {
		super(xa, ya, xb, yb, f);
		minresistance = new Double(st.nextToken());
		maxresistance = new Double(st.nextToken());
		//createSlider();
	}

	protected boolean nonLinear() {
		return true;
	}

	public int getDumpType() {
		return 192;
	}

	protected String dump() {
		return super.dump() + " " + minresistance + " " + maxresistance;
	}

/*    void createSlider() {
        sim.main.add(label = new Label("Temperature", Label.CENTER));
        int value = 50;
        sim.main.add(slider = new Scrollbar(Scrollbar.HORIZONTAL, value, 1, 0, 101));
        sim.main.validate();
    }*/

	protected void setPoints() {
		super.setPoints();
		calcLeads(32);
		ps3 = new Point();
		ps4 = new Point();
	}

/*    void delete() {
        sim.main.remove(label);
        sim.main.remove(slider);
    }*/

	public void draw(CircuitGUI.ClientCircuitGui g, int mouseX, int mouseY, float partialTicks) {
		int i;
		double v1 = volts[0];
		double v2 = volts[1];
		setBbox(point1, point2, 6);
		draw2Leads(g, lightGrayColor);
		// FIXME need to draw properly, see ResistorElm.java
		doDots(g);
		drawPosts(g, lightGrayColor);
	}

	protected void calculateCurrent() {
		double vd = volts[0] - volts[1];
		current = vd / resistance;
	}

	protected void startIteration() {
		double vd = volts[0] - volts[1];
		// FIXME set resistance as appropriate, using slider.getValue()
		resistance = minresistance;
		//System.out.print(this + " res current set to " + current + "\n");
	}

	public void doStep() {
		sim.stampResistor(nodes[0], nodes[1], resistance);
	}

	public void stamp() {
		sim.stampNonLinear(nodes[0]);
		sim.stampNonLinear(nodes[1]);
	}

	public void getInfo(String arr[]) {
		// FIXME
		arr[0] = "spark gap";
		getBasicInfo(arr);
		arr[3] = "R = " + getUnitText(resistance, CirSim.ohmString);
		arr[4] = "Ron = " + getUnitText(minresistance, CirSim.ohmString);
		arr[5] = "Roff = " + getUnitText(maxresistance, CirSim.ohmString);
	}

	public EditInfo getEditInfo(int n) {
		// ohmString doesn't work here on linux
		if (n == 0)
			return new EditInfo("Min resistance (ohms)", minresistance, 0, 0);
		if (n == 1)
			return new EditInfo("Max resistance (ohms)", maxresistance, 0, 0);
		return null;
	}

	public void setEditValue(int n, EditInfo ei) {
		if (ei.getValue() > 0 && n == 0)
			minresistance = ei.getValue();
		if (ei.getValue() > 0 && n == 1)
			maxresistance = ei.getValue();
	}

	@Override
	protected boolean isWire() {
		return false;
	}
}

