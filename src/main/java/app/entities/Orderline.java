// File: src/main/java/app/entities/Orderline.java
package app.entities;

public class Orderline {
    private int orderlineId;
    private Bottom bottom;
    private Topping topping;
    private int quantity;
    private double orderlinePrice;

    public Orderline(int orderlineId, Bottom bottom, Topping topping, int quantity, double orderlinePrice) {
        this.orderlineId = orderlineId;
        this.bottom = bottom;
        this.topping = topping;
        this.quantity = quantity;
        this.orderlinePrice = orderlinePrice;
    }

    // Getters and Setters
    public int getOrderlineId() { return orderlineId; }
    public Bottom getBottom() { return bottom; }
    public Topping getTopping() { return topping; }
    public int getQuantity() { return quantity; }
    public double getOrderlinePrice() { return orderlinePrice; }
}
