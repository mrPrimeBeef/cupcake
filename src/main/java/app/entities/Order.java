package app.entities;

import java.sql.Date;

public class Order {

    private int orderNumber;
    private int memberId;
    private Date date;
    private String status;
    private double price;

    public Order(int orderNumber, int memberId, Date date, String status, double price) {
        this.orderNumber = orderNumber;
        this.memberId = memberId;
        this.date = date;
        this.status = status;
        this.price = price;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public int getMemberId() {
        return memberId;
    }

    public Date getDate() {
        return date;
    }

    public String getStatus() {
        return status;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderNumber=" + orderNumber +
                ", memberId=" + memberId +
                ", date=" + date +
                ", status='" + status + '\'' +
                ", price=" + price +
                '}';
    }
}
