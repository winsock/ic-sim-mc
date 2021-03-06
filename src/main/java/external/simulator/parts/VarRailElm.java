package external.simulator.parts;

import external.simulator.EditInfo;

import java.util.StringTokenizer;

public class VarRailElm extends RailElm {
	//    Scrollbar slider;
	//    Label label;
	private String sliderText;
	private double railVoltage = 0d;

	public VarRailElm(int xx, int yy) {
		super(xx, yy, WF_VAR);
		sliderText = "Voltage";
		frequency = maxVoltage;
		//        createSlider();
	}

	public VarRailElm(int xa, int ya, int xb, int yb, int f,
	                  StringTokenizer st) {
		super(xa, ya, xb, yb, f, st);
		sliderText = st.nextToken();
		while (st.hasMoreTokens())
			sliderText += ' ' + st.nextToken();
		//        createSlider();
	}

	protected String dump() {
		return super.dump() + " " + sliderText;
	}

	public int getDumpType() {
		return 172;
	}

/*    void createSlider() {
        waveform = WF_VAR;
        sim.main.add(label = new Label(sliderText, Label.CENTER));
        int value = (int) ((frequency - bias) * 100 / (maxVoltage - bias));
        sim.main.add(slider = new Scrollbar(Scrollbar.HORIZONTAL, value, 1, 0, 101));
        sim.main.validate();
    }*/

	protected double getVoltage() {
		frequency = railVoltage * (maxVoltage - bias) / 100. + bias;
		return frequency;
	}

/*    void delete() {
        sim.main.remove(label);
        sim.main.remove(slider);
    }*/

	public EditInfo getEditInfo(int n) {
		if (n == 0)
			return new EditInfo("Min Voltage", bias, -20, 20);
		if (n == 1)
			return new EditInfo("Max Voltage", maxVoltage, -20, 20);
		if (n == 2) {
			return new EditInfo("Voltage Magnitude", railVoltage, -20, 20);
		}
		return null;
	}

	public void setEditValue(int n, EditInfo ei) {
		if (n == 0)
			bias = ei.getValue();
		if (n == 1)
			maxVoltage = ei.getValue();
		if (n == 2) {
			railVoltage = ei.getValue();
		}
	}

	public int getShortcut() {
		return 0;
	}
}
