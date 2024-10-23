package app.entities;

public class Topping {
    int toppingId;
    String toppingName;
    double toppingPrice;

    public Topping(int toppingId, String toppingName, double toppingPrice) {
        this.toppingId = toppingId;
        this.toppingName = toppingName;
        this.toppingPrice = toppingPrice;
    }

    public int getToppingId() {
        return toppingId;
    }

    public String getToppingName() {
        return toppingName;
    }

    public double getToppingPrice() {
        return toppingPrice;
    }
}
