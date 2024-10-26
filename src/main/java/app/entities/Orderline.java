package app.entities;

public class Orderline {
    private int orderlineId;
    private int orderNumber;
    private Bottom bottom;
    private Topping topping;
    private int quantity;
    private double orderlinePrice;

    public Orderline(int orderlineId, int orderNumber, Bottom bottom, Topping topping, int quantity, double orderlinePrice) {
        this.orderlineId = orderlineId;
        this.orderNumber = orderNumber;
        this.bottom = bottom;
        this.topping = topping;
        this.quantity = quantity;
        this.orderlinePrice = orderlinePrice;
    }

    public Orderline(int orderNumber, Bottom bottom, Topping topping, int quantity, double orderlinePrice) {
        this.orderNumber = orderNumber;
        this.bottom = bottom;
        this.topping = topping;
        this.quantity = quantity;
        this.orderlinePrice = orderlinePrice;
    }

    public int getOrderlineId() {
        return orderlineId;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public Bottom getBottom() {
        return bottom;
    }

    public Topping getTopping() {
        return topping;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getOrderlinePrice() {
        return orderlinePrice;
    }

}