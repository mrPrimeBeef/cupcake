package app.controllers;

import app.entities.Bottom;
import app.entities.Member;
import app.entities.Orderline;
import app.entities.Topping;
import app.exceptions.DatabaseException;
import app.persistence.*;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;


public class OrderController {
    public static void addRoutes(Javalin app, ConnectionPool connectionPool) {
        app.get("kunde", ctx -> showPage(ctx, connectionPool));
        app.post("kunde", ctx -> addToOrder(ctx, connectionPool));
    }

    private static void showPage(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
//        User currentUser = ctx.sessionAttribute("currentUser");
//
//        if (currentUser == null) {
//            ctx.attribute("errorMessage", "Please log in, in order to use this app.");
//            ctx.render("error.html");
//            return;
//        }

//        int user_id = currentUser.getUserId();
        try {
            ArrayList<Bottom> bottoms = BottomMapper.getAllBottoms(connectionPool);
            ArrayList<Topping> toppings = ToppingMapper.getAllBToppings(connectionPool);

            ctx.attribute("bottoms", bottoms);
            ctx.attribute("toppings", toppings);

        } catch (DatabaseException e) {
            ctx.attribute("errorMessage", "There was a problem retrieving your data. Please try again later.");
            ctx.render("error.html");
            throw new RuntimeException(e);

        }
        ctx.render("kunde.html");
    }

    private static void addToOrder(Context ctx, ConnectionPool connectionPool) throws DatabaseException {

        int selectedBottom = Integer.parseInt(ctx.formParam("bund"));
        Bottom bottom = BottomMapper.getBottomNameById(selectedBottom, connectionPool);

        int selectedTopping = Integer.parseInt(ctx.formParam("topping"));
        Topping topping = ToppingMapper.getToppingNameById(selectedTopping, connectionPool);

        double toppingPrice = topping.getToppingPrice();
        double bottomPrice = bottom.getBottomPrice();

        String number = ctx.formParam("antal");
        int quantity = Integer.parseInt(number);

        double orderlinePrice = (toppingPrice + bottomPrice) * quantity;

        Member member = ctx.sessionAttribute("currentMember");

        Date date = new Date(System.currentTimeMillis());

        int orderNumber = OrderMapper.createOrder(member.getMemberId(), date, "In progress", 0,connectionPool);

        Orderline orderline = new Orderline(orderNumber, bottom,topping, quantity, orderlinePrice);

        OrderlineMapper.createOrderline(orderline, connectionPool);

        updateOrderPrice(orderNumber, connectionPool);
    }
    private static void updateOrderPrice(int orderNumber, ConnectionPool connectionPool) throws DatabaseException {
        ArrayList<Orderline> orderlines = OrderlineMapper.getOrderlinesByOrderNumber(orderNumber, connectionPool);


        double totalPrice = 0;
        for (Orderline orderline : orderlines) {
            totalPrice += orderline.getOrderlinePrice() * orderline.getQuantity();
        }

        OrderMapper.updateOrderPrice(orderNumber, totalPrice, connectionPool);
    }

}