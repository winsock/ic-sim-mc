package external.simulator.parts;

import external.simulator.ChipElm;
import me.querol.andrew.ic.Gui.CircuitGUI;

import java.awt.*;
import java.util.StringTokenizer;

public class SevenSegElm extends ChipElm {
	private Color color;

	public SevenSegElm(int xx, int yy) {
		super(xx, yy);
	}

	public SevenSegElm(int xa, int ya, int xb, int yb, int f,
	                   StringTokenizer st) {
		super(xa, ya, xb, yb, f, st);
	}

	public String getChipName() {
		return "7-segment driver/display";
	}

	protected void setupPins() {
		color = Color.red.darker();
		sizeX = 4;
		sizeY = 4;
		pins = new Pin[7];
		pins[0] = new Pin(0, SIDE_W, "a");
		pins[1] = new Pin(1, SIDE_W, "b");
		pins[2] = new Pin(2, SIDE_W, "c");
		pins[3] = new Pin(3, SIDE_W, "d");
		pins[4] = new Pin(1, SIDE_S, "e");
		pins[5] = new Pin(2, SIDE_S, "f");
		pins[6] = new Pin(3, SIDE_S, "g");
	}

	public void draw(CircuitGUI.ClientCircuitGui g, int mouseX, int mouseY, float partialTicks) {
		drawChip(g);
		int xl = x + cspc * 5;
		int yl = y + cspc;
		drawThickLine(g, xl, yl, xl + cspc, yl, color);
		drawThickLine(g, xl + cspc, yl, xl + cspc, yl + cspc, color);
		drawThickLine(g, xl + cspc, yl + cspc, xl + cspc, yl + cspc2, color);
		drawThickLine(g, xl, yl + cspc2, xl + cspc, yl + cspc2, color);
		drawThickLine(g, xl, yl + cspc, xl, yl + cspc2, color);
		drawThickLine(g, xl, yl, xl, yl + cspc, color);
		drawThickLine(g, xl, yl + cspc, xl + cspc, yl + cspc, color);
	}

	void setColor(Color c) {
		this.color = c;
	}

	protected int getPostCount() {
		return 7;
	}

	protected int getVoltageSourceCount() {
		return 0;
	}

	public int getDumpType() {
		return 157;
	}
}
