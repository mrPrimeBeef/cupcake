package app.entities;

public class Orderline {
    private int ordernumber;
    private Bottom bottom;
    private Topping topping;
    private int quantity;
    private double orderlinePrice;
    private int orderlineId;

    public Orderline(int orderlineId, int ordernumber, Bottom bottom, Topping topping, int quantity, double orderlinePrice) {
        this.orderlineId=orderlineId;
        this.ordernumber = ordernumber;
        this.bottom = bottom;
        this.topping = topping;
        this.quantity = quantity;
        this.orderlinePrice = orderlinePrice;
    }

    public Orderline(int ordernumber, Bottom bottom, Topping topping, int quantity, double orderlinePrice) {
        this.ordernumber = ordernumber;
        this.bottom = bottom;
        this.topping = topping;
        this.quantity = quantity;
        this.orderlinePrice = orderlinePrice;
    }

    // Getters and Setters
    public int getOrderNumber() { return ordernumber; }
    public Bottom getBottom() { return bottom; }
    public Topping getTopping() { return topping; }
    public int getQuantity() { return quantity; }
    public double getOrderlinePrice() { return orderlinePrice; }

    public int getOrderlineId() {return orderlineId;}
}