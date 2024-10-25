package app.dto;

import java.sql.Date;

public class OrderMemberDto {

    private int orderNumber;
    private int memberId;
    private String memberName;
    private String memberEmail;
    private Date orderDate;
    private String orderStatus;
    private double orderPrice;

    public OrderMemberDto(int orderNumber, int memberId, String memberName, String memberEmail, Date orderDate, String orderStatus, double orderPrice) {
        this.orderNumber = orderNumber;
        this.memberId = memberId;
        this.memberName = memberName;
        this.memberEmail = memberEmail;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.orderPrice = orderPrice;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public int getMemberId() {return memberId;}

    public String getMemberName() {
        return memberName;
    }

    public String getMemberEmail() {
        return memberEmail;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public double getOrderPrice() {
        return orderPrice;
    }

    @Override
    public String toString() {
        return "OrderMemberDto{" +
                "orderNumber=" + orderNumber +
                ", memberId=" + memberId +
                ", memberName='" + memberName + '\'' +
                ", memberEmail='" + memberEmail + '\'' +
                ", orderDate=" + orderDate +
                ", orderStatus='" + orderStatus + '\'' +
                ", orderPrice=" + orderPrice +
                '}';
    }

}
