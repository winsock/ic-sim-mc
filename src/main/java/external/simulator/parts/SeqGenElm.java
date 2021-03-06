package external.simulator.parts;

import external.simulator.ChipElm;
import external.simulator.EditInfo;
import external.simulator.Toggleable;

import java.util.StringTokenizer;

// contributed by Edward Calver

public class SeqGenElm extends ChipElm {
	private short data = 0;
	private byte position = 0;
	private boolean oneshot = false;
	private double lastchangetime = 0;
	private boolean clockstate = false;

	public SeqGenElm(int xx, int yy) {
		super(xx, yy);
	}

	public SeqGenElm(int xa, int ya, int xb, int yb, int f,
	                 StringTokenizer st) {
		super(xa, ya, xb, yb, f, st);
		data = (short) (Integer.parseInt(st.nextToken()));
		if (st.hasMoreTokens()) {
			oneshot = Boolean.valueOf(st.nextToken());
			position = 8;
		}
	}

	boolean hasReset() {
		return false;
	}

	public String getChipName() {
		return "Sequence generator";
	}

	protected void setupPins() {
		sizeX = 2;
		sizeY = 2;
		pins = new Pin[getPostCount()];

		pins[0] = new Pin(0, SIDE_W, "");
		pins[0].clock = true;
		pins[1] = new Pin(1, SIDE_E, "Q");
		pins[1].output = true;
	}

	protected int getPostCount() {
		return 2;
	}

	protected int getVoltageSourceCount() {
		return 1;
	}

	private void GetNextBit() {
		pins[1].value = ((data >>> position) & 1) != 0;
		position++;
	}

	protected void execute() {
		if (oneshot) {
			if (sim.getTime() - lastchangetime > 0.005) {
				if (position <= 8)
					GetNextBit();
				lastchangetime = sim.getTime();
			}
		}
		if (pins[0].value && !clockstate) {
			clockstate = true;
			if (oneshot) {
				position = 0;
			} else {
				GetNextBit();
				if (position >= 8)
					position = 0;
			}
		}
		if (!pins[0].value)
			clockstate = false;

	}

	public int getDumpType() {
		return 188;
	}

	protected String dump() {
		return super.dump() + " " + data + " " + oneshot;
	}

	public EditInfo getEditInfo(int n) {
		//My code
		if (n == 0) {
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.setCheckbox(new Toggleable("Bit 0 set", (data & 1) != 0));
			return ei;
		}

		if (n == 1) {
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.setCheckbox(new Toggleable("Bit 1 set", (data & 2) != 0));
			return ei;
		}
		if (n == 2) {
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.setCheckbox(new Toggleable("Bit 2 set", (data & 4) != 0));
			return ei;
		}
		if (n == 3) {
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.setCheckbox(new Toggleable("Bit 3 set", (data & 8) != 0));
			return ei;
		}

		if (n == 4) {
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.setCheckbox(new Toggleable("Bit 4 set", (data & 16) != 0));
			return ei;
		}
		if (n == 5) {
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.setCheckbox(new Toggleable("Bit 5 set", (data & 32) != 0));
			return ei;
		}

		if (n == 6) {
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.setCheckbox(new Toggleable("Bit 6 set", (data & 64) != 0));
			return ei;
		}

		if (n == 7) {
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.setCheckbox(new Toggleable("Bit 7 set", (data & 128) != 0));
			return ei;
		}
		if (n == 8) {
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.setCheckbox(new Toggleable("One shot", oneshot));
			return ei;
		}
		return null;
	}

	public void setEditValue(int n, EditInfo ei) {
		if (n == 0) {
			if (ei.getCheckbox().getState())
				data |= 1;
			else
				data &= ~1;
			setPoints();
		}
		if (n == 1) {
			if (ei.getCheckbox().getState())
				data |= 2;
			else
				data &= ~2;
			setPoints();
		}
		if (n == 2) {
			if (ei.getCheckbox().getState())
				data |= 4;
			else
				data &= ~4;
			setPoints();
		}
		if (n == 3) {
			if (ei.getCheckbox().getState())
				data |= 8;
			else
				data &= ~8;
			setPoints();
		}
		if (n == 4) {
			if (ei.getCheckbox().getState())
				data |= 16;
			else
				data &= ~16;
			setPoints();
		}
		if (n == 5) {
			if (ei.getCheckbox().getState())
				data |= 32;
			else
				data &= ~32;
			setPoints();
		}
		if (n == 6) {
			if (ei.getCheckbox().getState())
				data |= 64;
			else
				data &= ~64;
			setPoints();
		}
		if (n == 7) {
			if (ei.getCheckbox().getState())
				data |= 128;
			else
				data &= ~128;
			setPoints();
		}
		if (n == 8) {
			if (ei.getCheckbox().getState()) {
				oneshot = true;
				position = 8;
			} else {
				position = 0;
				oneshot = false;
			}
		}

	}

}
