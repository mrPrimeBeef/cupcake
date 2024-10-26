package app.entities;

public class Bottom {
    private int bottom_id;
    private String bottom_name;
    private double bottom_price;

    public Bottom(int bottom_id, String bottom_name, double bottom_price) {
        this.bottom_id = bottom_id;
        this.bottom_name = bottom_name;
        this.bottom_price = bottom_price;
    }

    public int getBottomId() {
        return bottom_id;
    }

    public String getBottomName() {
        return bottom_name;
    }

    public double getBottomPrice() {
        return bottom_price;
    }
}
