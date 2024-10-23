package app.controllers;

import app.entities.*;
import app.exceptions.DatabaseException;
import app.persistence.*;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;


public class OrderController {
    public static void addRoutes(Javalin app, ConnectionPool connectionPool) {
        app.get("kunde", ctx -> addToCart(ctx, connectionPool));
        app.post("kunde", ctx -> addToOrder(ctx, connectionPool));
        app.get("tak", ctx -> thanks(ctx, connectionPool));
        app.post("kurv", ctx-> watchCart(ctx, connectionPool));
        app.get("kurv", ctx -> watchCart(ctx, connectionPool));
    }

    private static void watchCart(Context ctx, ConnectionPool connectionPool) {
        Member currentMember = ctx.sessionAttribute("currentMember");
        Order currentOrder = ctx.sessionAttribute("currentOrder");

        if (currentMember == null) {
            ctx.attribute("errorMessage", "log ind for at bestille.");
            ctx.render("error.html");
            return;
        }

        try {
            ArrayList<Orderline> orderlines = OrderlineMapper.getOrderlinesByOrderNumber(currentOrder.getOrderNumber(),connectionPool);

            double totalPrice = 0;
            for (Orderline orderline : orderlines) {
                totalPrice += orderline.getOrderlinePrice();  // Summér prisen for hver orderline
            }

            // Send orderlines og totalPrice til Thymeleaf
            ctx.attribute("orderlines", orderlines);
            ctx.attribute("totalPrice", totalPrice);


        }catch (DatabaseException e) {
                ctx.attribute("errorMessage", "There was a problem retrieving your data. Please try again later.");
                ctx.render("error.html");
                throw new RuntimeException(e);
        }
        ctx.render("kurv.html");
    }
private static void validateBalance(Context ctx, ConnectionPool connectionPool)throws DatabaseException{
    Member currentMember = ctx.sessionAttribute("currentMember");
    Order currentOrder = ctx.sessionAttribute("currentOrder");

    if (currentMember == null) {
        ctx.attribute("errorMessage", "log ind for at bestille.");
        ctx.render("error.html");
        return;
    }

    try {
        currentMember = MemberMapper.getBalance(currentMember.getMemberId(), currentMember.getBalance(), ctx, connectionPool);
    } catch (DatabaseException e) {
        ctx.attribute("errorMessage", "There was a problem retrieving your data. Please try again later.");
        ctx.render("error.html");
        throw new RuntimeException(e);
    }

    double totalOrderPrice = 0.0;


    try {
        ArrayList<Orderline> orderlines = OrderlineMapper.getOrderlinesByOrderNumber(currentOrder.getOrderNumber(), connectionPool);
        for (Orderline orderline : orderlines) {
            totalOrderPrice += orderline.getOrderlinePrice() * orderline.getQuantity();
        }
    } catch (DatabaseException e) {
        ctx.attribute("errorMessage", "Der opstod en fejl under hentning af orderlines.");
        ctx.render("error.html");
        throw new RuntimeException(e);
    }

    // Tjek om ordren overstiger balancen
    if (totalOrderPrice > currentMember.getBalance()) {
        ctx.attribute("errorMessage", "Ikke nok penge på kontoen til at gennemføre ordren.");
        ctx.render("error.html");
        return; // Stop videre behandling
    }

    ctx.render("kurv.html");

}

    private static void thanks(Context ctx, ConnectionPool connectionPool)throws DatabaseException {

        try {
            // Valider balancen. Hvis der er en fejl, vil validateBalance håndtere det.
            validateBalance(ctx, connectionPool); // Denne metode håndterer sin egen fejlvisning

            // Hvis validateBalance er korrekt, kaldes checkoutOrder
            checkoutOrder(ctx, connectionPool);

            // Eventuelt kan du vise en takkeside efter checkoutOrder er kaldt
            ctx.render("thank_you.html"); // Render en takkeside, hvis checkout er vellykket

        } catch (DatabaseException e) {
            // Håndter undtagelser, hvis noget går galt
            ctx.attribute("errorMessage", "Der opstod en fejl under behandlingen af din ordre.");
            ctx.render("error.html"); // Vis fejlmeddelelse
        }
    }

    private static void addToCart(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        Member currentMember = ctx.sessionAttribute("currentMember");

        if (currentMember == null) {
            ctx.attribute("errorMessage", "log ind for at bestille.");
            ctx.render("error.html");
            return;
        }

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
        Member member = ctx.sessionAttribute("currentMember");
        Order currentOrder = ctx.sessionAttribute("currentOrder");

        if (currentOrder == null) {
            Date date = new Date(System.currentTimeMillis());
            int orderNumber = OrderMapper.createOrder(member.getMemberId(), date, "In progress", 0, connectionPool);
            currentOrder = new Order(orderNumber, member.getMemberId(), date, "In progress", 0.0);
            ctx.sessionAttribute("currentOrder", currentOrder);
        }

        int selectedBottom = Integer.parseInt(ctx.formParam("bund"));
        Bottom bottom = BottomMapper.getBottomNameById(selectedBottom, connectionPool);

        int selectedTopping = Integer.parseInt(ctx.formParam("topping"));
        Topping topping = ToppingMapper.getToppingNameById(selectedTopping, connectionPool);

        double toppingPrice = topping.getToppingPrice();
        double bottomPrice = bottom.getBottomPrice();

        String number = ctx.formParam("antal");
        int quantity = Integer.parseInt(number);
        double orderlinePrice = (toppingPrice + bottomPrice) * quantity;

        Orderline orderline = new Orderline(currentOrder.getOrderNumber(), bottom, topping, quantity, orderlinePrice);
        OrderlineMapper.createOrderline(orderline, connectionPool);

        updateOrderPrice(currentOrder.getOrderNumber(), connectionPool);

        addToCart(ctx, connectionPool);
    }
    private static void updateOrderPrice(int orderNumber, ConnectionPool connectionPool) throws DatabaseException {
        ArrayList<Orderline> orderlines = OrderlineMapper.getOrderlinesByOrderNumber(orderNumber, connectionPool);


        double totalPrice = 0;
        for (Orderline orderline : orderlines) {
            totalPrice += orderline.getOrderlinePrice();
        }

        OrderMapper.updateOrderPrice(orderNumber, totalPrice, connectionPool);
    }

    private static void cancelOrder(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        Order currentOrder = ctx.sessionAttribute("currentOrder");

        if (currentOrder == null) {
            throw new DatabaseException("No active order to cancel.");
        }

        OrderMapper.updateOrderStatus(currentOrder.getOrderNumber(), "Canceled", connectionPool);

        ctx.sessionAttribute("currentOrder", null);
    }

    private static void checkoutOrder(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        Order currentOrder = ctx.sessionAttribute("currentOrder");

        if (currentOrder == null) {
            throw new DatabaseException("No active order to checkout.");
        }
        updateOrderPrice(currentOrder.getOrderNumber(), connectionPool);
        OrderMapper.updateOrderStatus(currentOrder.getOrderNumber(), "Completed", connectionPool);
        ctx.sessionAttribute("currentOrder", null);
    }
}