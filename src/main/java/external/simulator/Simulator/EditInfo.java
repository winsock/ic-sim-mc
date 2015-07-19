package external.simulator.Simulator;


public class EditInfo {
	private String text;
	private double value;
	private Toggleable checkbox;
    private boolean newDialog;
    private final String name;
    private double minval;
    private final double maxval;
    private final boolean forceLargeM;
    private boolean dimensionless;

    public EditInfo(String n, double val, double mn, double mx) {
        name = n;
        value = val;
        if (mn == 0 && mx == 0 && val > 0) {
            minval = 1e10;
            while (minval > val / 100)
                minval /= 10.;
            maxval = minval * 1000;
        } else {
            minval = mn;
            maxval = mx;
        }
        forceLargeM = name.indexOf("(ohms)") > 0 ||
                name.indexOf("(Hz)") > 0;
        dimensionless = false;
    }

    public EditInfo setDimensionless() {
        dimensionless = true;
        return this;
    }

	public Toggleable getCheckbox() {
		return checkbox;
	}

	public void setCheckbox(Toggleable checkbox) {
		this.checkbox = checkbox;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public boolean isNewDialog() {
		return newDialog;
	}

	public void setNewDialog(boolean newDialog) {
		this.newDialog = newDialog;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
    
