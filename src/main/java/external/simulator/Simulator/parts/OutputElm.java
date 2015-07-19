package external.simulator.Simulator.parts;

import external.simulator.Simulator.CircuitElm;
import external.simulator.Simulator.EditInfo;
import external.simulator.Simulator.Toggleable;
import me.querol.andrew.ic.Gui.CircuitGUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import org.lwjgl.util.Point;

import java.awt.*;
import java.util.StringTokenizer;

public class OutputElm extends CircuitElm {
	private final int FLAG_VALUE = 1;

	public OutputElm(int xx, int yy) {
		super(xx, yy);
	}

	public OutputElm(int xa, int ya, int xb, int yb, int f,
	                 StringTokenizer st) {
		super(xa, ya, xb, yb, f);
	}

	public int getDumpType() {
		return 'O';
	}

	protected int getPostCount() {
		return 1;
	}

	protected void setPoints() {
		super.setPoints();
		lead1 = new Point();
	}

	public void draw(CircuitGUI.ClientCircuitGui g, int mouseX, int mouseY, float partialTicks) {
		boolean selected = (needsHighlight() || sim.getPlotYElm() == this);
		Color color = selected ? selectColor : whiteColor;
		String s = (flags & FLAG_VALUE) != 0 ? getVoltageText(volts[0]) : "out";
		FontRenderer renderer = Minecraft.getMinecraft().fontRendererObj;
		if (this == sim.getPlotXElm())
			s = "X";
		if (this == sim.getPlotYElm())
			s = "Y";
		interpPoint(point1, point2, lead1, 1 - (renderer.getStringWidth(s) / 2 + 8) / dn);
		setBbox(point1, lead1, 0);
		drawCenteredText(g, s, x2, y2, true, color);
/*        if (selected)
            g.setColor(selectColor);*/
		drawThickLine(g, point1, lead1, getVoltageColor(volts[0]));
		drawPosts(g, lightGrayColor);
	}

	protected double getVoltageDiff() {
		return volts[0];
	}

	public void getInfo(String arr[]) {
		arr[0] = "output";
		arr[1] = "V = " + getVoltageText(volts[0]);
	}

	public EditInfo getEditInfo(int n) {
		if (n == 0) {
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.setCheckbox(new Toggleable("Show Voltage",
				(flags & FLAG_VALUE) != 0));
			return ei;
		}
		return null;
	}

	public void setEditValue(int n, EditInfo ei) {
		if (n == 0)
			flags = (ei.getCheckbox().getState()) ?
			        (flags | FLAG_VALUE) :
			        (flags & ~FLAG_VALUE);
	}

	@Override
	protected boolean isWire() {
		return false;
	}
}
