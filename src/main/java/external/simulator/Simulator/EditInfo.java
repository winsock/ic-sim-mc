package external.simulator.Simulator;


class EditInfo {
    String text;
    double value;
    Toggleable checkbox;
    boolean newDialog;
    private String name;
    private double minval;
    private double maxval;
    private boolean forceLargeM;
    private boolean dimensionless;

    EditInfo(String n, double val, double mn, double mx) {
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

    EditInfo setDimensionless() {
        dimensionless = true;
        return this;
    }
}
    
