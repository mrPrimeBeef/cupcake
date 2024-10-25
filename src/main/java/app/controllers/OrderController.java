package app.controllers;

import app.dto.OrderMemberDto;
import app.entities.*;
import app.exceptions.DatabaseException;
import app.persistence.*;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.sql.Date;
import java.util.ArrayList;

public class OrderController {

    public static void addRoutes(Javalin app, ConnectionPool connectionPool) {
        app.get("kunde", ctx -> showAddToCart(ctx, connectionPool));
        app.post("kunde", ctx -> addToOrder(ctx, connectionPool));
        app.post("tak", ctx -> thanks(ctx, connectionPool));
        app.get("kurv", ctx -> watchCart(ctx, connectionPool));
        app.get("/delete/{id}", ctx -> {int orderlineId = Integer.parseInt(ctx.pathParam("id"));cancelOrderline(ctx, connectionPool, orderlineId);});
        app.get("adminalleordrer", ctx -> adminShowAllOrders(ctx, connectionPool));
        app.get("adminordre", ctx -> adminShowOrder(ctx, connectionPool));
    }

    private static void adminShowOrder(Context ctx, ConnectionPool connectionPool) {
        Member currentMember = ctx.sessionAttribute("currentMember");
        if (currentMember == null || !currentMember.getRole().equals("admin")) {
            ctx.attribute("errorMessage", "Kun for admin.");
            ctx.render("error.html");
            return;
        }

        // TODO: Håndter når query parameteren is null
        int orderNumber = Integer.parseInt(ctx.queryParam("ordrenr"));

        try {
            OrderMemberDto orderMemberDto = OrderMapper.getOrderMemberDtoByOrderNumber(orderNumber, connectionPool);
            ctx.attribute("orderMemberDto", orderMemberDto);

            ArrayList<Orderline> orderlines = OrderlineMapper.getOrderlinesByOrderNumber(orderNumber, connectionPool);
            ctx.attribute("orderlines", orderlines);

            ctx.render("adminordre.html");
        } catch (DatabaseException e) {
            ctx.attribute("errorMessage", "Der er sket en fejl: " + e.getMessage());
            ctx.render("error.html");
        }


    }

    private static void adminShowAllOrders(Context ctx, ConnectionPool connectionPool) {
        Member currentMember = ctx.sessionAttribute("currentMember");
        if (currentMember == null || !currentMember.getRole().equals("admin")) {
            ctx.attribute("errorMessage", "Kun for admin.");
            ctx.render("error.html");
            return;
        }

        try {
            ArrayList<OrderMemberDto> allOrderMemberDtos = OrderMapper.getAllOrderMemberDtos(connectionPool);
            ctx.attribute("allOrderMemberDtos", allOrderMemberDtos);
            ctx.render("adminalleordrer.html");
        } catch (DatabaseException e) {
            ctx.attribute("errorMessage", "Der er sket en fejl i at hente data");
            ctx.render("error.html");
        }
    }

    private static boolean watchCart(Context ctx, ConnectionPool connectionPool) {
        Member currentMember = ctx.sessionAttribute("currentMember");
        if (currentMember == null) {
            ctx.attribute("errorMessage", "Log ind for at bestille.");
            ctx.render("error.html");
            return false;
        }

        boolean activeOrder = false;
        Order currentOrder = ctx.sessionAttribute("currentOrder");

        if (currentOrder == null) {
            ctx.attribute("tomKurv","Kurven er tom.");
            ctx.render("kurv.html");
            return activeOrder;
        }

        try {
            activeOrder = true;

            ArrayList<Orderline> orderlines = OrderlineMapper.getOrderlinesByOrderNumber(currentOrder.getOrderNumber(), connectionPool);

            double totalPrice = 0;
            for (Orderline orderline : orderlines) {
                totalPrice += orderline.getOrderlinePrice();
            }
            ctx.attribute("orderlines", orderlines);
            ctx.attribute("totalPrice", totalPrice);

        } catch (DatabaseException e) {
            ctx.attribute("errorMessage", "Der opstod et problem ved hentningen af dataen, prøv igen.");
            ctx.redirect("errorAlreadyLogin");
            throw new RuntimeException(e);
        }
        ctx.render("kurv.html");
        return activeOrder;
    }

    private static boolean validateBalance(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        Member currentMember = ctx.sessionAttribute("currentMember");
        if (currentMember == null) {
            ctx.attribute("errorMessage", "Log ind for at bestille.");
            ctx.render("error.html");
            return false;
        }

        try {
            double memberBalance = MemberMapper.getBalance(currentMember.getMemberId(), connectionPool);
            double totalOrderPrice = OrderMapper.getActiveOrder(ctx,connectionPool).getPrice();

            if (totalOrderPrice > memberBalance) {
                return false;
            }
            double newBalance = currentMember.getBalance() - totalOrderPrice;
            MemberMapper.updateMemberBalance(currentMember.getMemberId(), newBalance, connectionPool);
        } catch (DatabaseException e) {
            ctx.attribute("errorMessage", "Der opstod en fejl under hentning af orderlines.");
            ctx.render("errorAlreadyLogin.html");
            throw new RuntimeException(e);
        }
        return true;
    }

    private static void thanks(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        Order currentOrder = ctx.sessionAttribute("currentOrder");
        Member currentMember = ctx.sessionAttribute("currentMember");
        ctx.sessionAttribute("currentOrder", currentOrder);

        if (currentMember == null) {
            ctx.attribute("errorMessage", "Log ind for at bestille.");
            ctx.render("error.html");
            return;
        }

        if (currentOrder == null) {
            // ctx.attribute("errorMessage", "læg noget i kurven for at købe");
            ctx.sessionAttribute("errorMessage", "læg noget i kurven for at købe");
            ctx.redirect("kunde");
            return;
        }

        ArrayList<Orderline> anyOrderlines = OrderlineMapper.getOrderlinesByOrderNumber(currentOrder.getOrderNumber(), connectionPool);
        if (anyOrderlines.isEmpty()) {
            //ctx.attribute("errorMessage","læg noget i kurven for at købe" );
            ctx.sessionAttribute("errorMessage", "læg noget i kurven for at købe");
            ctx.redirect("kunde");
            return;
        }

        try {
           if(validateBalance(ctx, connectionPool)){
               checkoutOrder(ctx, connectionPool);
               ctx.render("tak.html");
               ctx.sessionAttribute("currentOrder", null);
           } else{
               ctx.attribute("errorMessage", "Ikke nok penge på kontoen til at gennemføre ordren.");
               ctx.render("errorAlreadyLogin.html");
           }

        } catch (DatabaseException e) {
            ctx.attribute("errorMessage", "Der opstod en fejl under behandlingen af din ordre.");
            ctx.render("errorAlreadyLogin.html");
        }
    }

    private static void showAddToCart(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        Member currentMember = ctx.sessionAttribute("currentMember");
        if (currentMember == null) {
            ctx.attribute("errorMessage", "Log ind for at bestille.");
            ctx.render("error.html");
            return;
        }
        try {
            ArrayList<Bottom> bottoms = BottomMapper.getAllBottoms(connectionPool);
            ArrayList<Topping> toppings = ToppingMapper.getAllBToppings(connectionPool);

            ctx.attribute("bottoms", bottoms);
            ctx.attribute("toppings", toppings);

        } catch (DatabaseException e) {
            ctx.attribute("errorMessage", "Der var et problem ved at hente siden pga. fejl ved at hente data");
            ctx.render("errorAlreadyLogin.html");
           throw new RuntimeException(e);
        }
        ctx.render("kunde.html");
    }

    private static void addToOrder(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        Member currentMember = ctx.sessionAttribute("currentMember");
        if (currentMember == null) {
            ctx.attribute("errorMessage", "Log ind for at bestille.");
            ctx.render("error.html");
            return;
        }
            Order currentOrder = ctx.sessionAttribute("currentOrder");

        if (currentOrder == null) {
            Date date = new Date(System.currentTimeMillis());
            int orderNumber = OrderMapper.createOrder(currentMember.getMemberId(), date, "In progress", 0, connectionPool);
            currentOrder = new Order(orderNumber, currentMember.getMemberId(), date, "In progress", 0.0);
            ctx.sessionAttribute("currentOrder", currentOrder);
        }

        int selectedBottom = Integer.parseInt(ctx.formParam("bund"));
        Bottom bottom = BottomMapper.getBottomNameById(selectedBottom, connectionPool);

        int selectedTopping = Integer.parseInt(ctx.formParam("topping"));
        Topping topping = ToppingMapper.getToppingNameById(selectedTopping, connectionPool);

        double toppingPrice = topping.getToppingPrice();
        double bottomPrice = bottom.getBottomPrice();

        int quantity= Integer.parseInt(ctx.formParam("antal"));
        double orderlinePrice = (toppingPrice + bottomPrice) * quantity;

        Orderline orderline = new Orderline(currentOrder.getOrderNumber(), bottom, topping, quantity, orderlinePrice);
        OrderlineMapper.createOrderline(orderline, connectionPool);

        updateOrderPrice(currentOrder.getOrderNumber(), connectionPool);
        showAddToCart(ctx, connectionPool);
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

    private static void cancelOrderline(Context ctx, ConnectionPool connectionPool, int orderlineId) throws DatabaseException {

        OrderMapper.deleteOrderline(orderlineId, connectionPool);
        ctx.redirect("/kurv");
    }



    private static void checkoutOrder(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        Order currentOrder = ctx.sessionAttribute("currentOrder");

        if (currentOrder == null) {
            ctx.attribute("errorMessage", "Du har ikke nogen igangværende ordre.");
        }
        updateOrderPrice(currentOrder.getOrderNumber(), connectionPool);
        OrderMapper.updateOrderStatus(currentOrder.getOrderNumber(), "Completed", connectionPool);
    }
}