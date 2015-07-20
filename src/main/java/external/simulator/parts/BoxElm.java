package external.simulator.parts;

import external.simulator.EditInfo;
import external.simulator.GraphicElm;
import me.querol.andrew.ic.Gui.CircuitGUI;
import net.minecraft.client.gui.Gui;

import java.awt.*;
import java.util.StringTokenizer;

public class BoxElm extends GraphicElm {

	public BoxElm(int xx, int yy) {
		super(xx, yy);
		x2 = xx + 16;
		y2 = yy + 16;
		setBbox(x, y, x2, y2);
	}

	public BoxElm(int xa, int ya, int xb, int yb, int f,
	              StringTokenizer st) {
		super(xa, ya, xb, yb, f);
		x2 = xb;
		y2 = yb;
		if (st.hasMoreTokens())
			x = Integer.parseInt(st.nextToken());
		if (st.hasMoreTokens())
			y = Integer.parseInt(st.nextToken());
		if (st.hasMoreTokens())
			x2 = Integer.parseInt(st.nextToken());
		if (st.hasMoreTokens())
			y2 = Integer.parseInt(st.nextToken());
		setBbox(x, y, x2, y2);
	}

	@Override
	protected boolean isWire() {
		return super.isWire();
	}

	public int getDumpType() {
		return 'b';
	}

	protected void drag(int xx, int yy) {
		x = xx;
		y = yy;
	}

	public void draw(CircuitGUI.ClientCircuitGui g, int mouseX, int mouseY, float partialTicks) {
		//g.setColor(needsHighlight() ? selectColor : lightGrayColor);
		Color color = (needsHighlight() ? selectColor : Color.gray);
		setBbox(x, y, x2, y2);
		if (x < x2 && y < y2)
			Gui.drawRect(x, y, x2 - x, y2 - y, color.getRGB());
		else if (x > x2 && y < y2)
			Gui.drawRect(x2, y, x - x2, y2 - y, color.getRGB());
		else if (x < x2 && y > y2)
			Gui.drawRect(x, y2, x2 - x, y - y2, color.getRGB());
		else
			Gui.drawRect(x2, y2, x - x2, y - y2, color.getRGB());
	}

	public EditInfo getEditInfo(int n) {
		return null;
	}

	public void setEditValue(int n, EditInfo ei) {
	}

	public void getInfo(String arr[]) {
	}

	@Override
	public int getShortcut() {
		return 0;
	}
}

