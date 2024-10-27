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
        app.get("bestil", ctx -> showOrderingPage(ctx, connectionPool));
        app.post("bestil", ctx -> addToOrder(ctx, connectionPool));
        app.get("kurv", ctx -> showCart(ctx, connectionPool));
        app.get("/delete/{id}", ctx -> deleteOrderline(ctx, connectionPool));
        app.post("koeb", ctx -> attemptCheckout(ctx, connectionPool));
        app.get("ordredetaljer", ctx -> showOrder(ctx, connectionPool));
        app.get("mineordrer", ctx -> showAllOrders(ctx, connectionPool));
        app.get("adminordre", ctx -> adminShowOrder(ctx, connectionPool));
        app.get("adminalleordrer", ctx -> adminShowAllOrders(ctx, connectionPool));
    }


    private static void showOrderingPage(Context ctx, ConnectionPool connectionPool) {
        Member currentMember = ctx.sessionAttribute("currentMember");
        if (currentMember == null) {
            ctx.attribute("errorMessage", "Log ind for at bestille.");
            ctx.render("error.html");
            return;
        }
        try {
            ArrayList<Bottom> bottoms = BottomMapper.getAllBottoms(connectionPool);
            ArrayList<Topping> toppings = ToppingMapper.getAllToppings(connectionPool);

            ctx.attribute("bottoms", bottoms);
            ctx.attribute("toppings", toppings);
            ctx.render("bestil.html");

        } catch (DatabaseException e) {
            ctx.attribute("errorMessage", "Der var et problem ved at hente siden pga. fejl ved at hente data.");
            ctx.render("errorAlreadyLogin.html");
        }

    }


    private static void addToOrder(Context ctx, ConnectionPool connectionPool) {
        Member currentMember = ctx.sessionAttribute("currentMember");
        if (currentMember == null) {
            ctx.attribute("errorMessage", "Log ind for at bestille.");
            ctx.render("error.html");
            return;
        }

        int bottomId = Integer.parseInt(ctx.formParam("bund"));
        int toppingId = Integer.parseInt(ctx.formParam("topping"));
        int quantity = Integer.parseInt(ctx.formParam("antal"));

        try {

            double bottomPrice = BottomMapper.getBottomById(bottomId, connectionPool).getBottomPrice();
            double toppingPrice = ToppingMapper.getToppingById(toppingId, connectionPool).getToppingPrice();
            double orderlinePrice = (toppingPrice + bottomPrice) * quantity;

            int activeOrderNumber = OrderMapper.PBgetActiveOrderNumber(currentMember.getMemberId(), connectionPool);
            if (activeOrderNumber == -1) {
                activeOrderNumber = OrderMapper.PBcreateActiveOrder(currentMember.getMemberId(), connectionPool);
            }

            OrderlineMapper.createOrderline(activeOrderNumber, bottomId, toppingId, quantity, orderlinePrice, connectionPool);
            updateOrderPrice(activeOrderNumber, connectionPool);
            showOrderingPage(ctx, connectionPool);

        } catch (DatabaseException e) {
            ctx.attribute("errorMessage", "Der var et problem med at lægge i kurven.");
            ctx.render("errorAlreadyLogin.html");
        }

    }

    private static void showCart(Context ctx, ConnectionPool connectionPool) {
        Member currentMember = ctx.sessionAttribute("currentMember");
        if (currentMember == null) {
            ctx.attribute("errorMessage", "Log ind for at bestille.");
            ctx.render("error.html");
            return;
        }

        try {

            int activeOrderNumber = OrderMapper.PBgetActiveOrderNumber(currentMember.getMemberId(), connectionPool);
            if (activeOrderNumber == -1) {
                ctx.attribute("tomKurv", "Kurven er tom.");
                ctx.render("kurv.html");
                return;
            }

            ArrayList<Orderline> orderlines = OrderlineMapper.getOrderlinesByOrderNumber(activeOrderNumber, connectionPool);
            if (orderlines.isEmpty()) {
                ctx.attribute("tomKurv", "Kurven er tom.");
                ctx.render("kurv.html");
                return;
            }

            double totalPrice = OrderMapper.PBgetOrderPrice(activeOrderNumber, connectionPool);

            ctx.attribute("orderlines", orderlines);
            ctx.attribute("totalPrice", totalPrice);
            ctx.render("kurv.html");

        } catch (DatabaseException e) {
            ctx.attribute("errorMessage", "Der opstod et problem ved hentningen af dataen, prøv igen.");
            ctx.redirect("errorAlreadyLogin");
        }

    }


    private static void deleteOrderline(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        // TODO: I rapporten kan vi skrive om hvad det farlige er ved delete ud fra GET request
        int orderlineId = Integer.parseInt(ctx.pathParam("id"));
        OrderMapper.deleteOrderline(orderlineId, connectionPool);
        ctx.redirect("/kurv");
    }

    private static void updateOrderPrice(int orderNumber, ConnectionPool connectionPool) throws DatabaseException {
        ArrayList<Orderline> orderlines = OrderlineMapper.getOrderlinesByOrderNumber(orderNumber, connectionPool);

        double totalPrice = 0;
        for (Orderline orderline : orderlines) {
            totalPrice += orderline.getOrderlinePrice();
        }
        OrderMapper.updateOrderPrice(orderNumber, totalPrice, connectionPool);
    }


    private static void attemptCheckout(Context ctx, ConnectionPool connectionPool) {

        Member currentMember = ctx.sessionAttribute("currentMember");
        if (currentMember == null) {
            ctx.attribute("errorMessage", "Log ind for at bestille.");
            ctx.render("error.html");
            return;
        }

        try {

            int activeOrderNumber = OrderMapper.PBgetActiveOrderNumber(currentMember.getMemberId(), connectionPool);
            if (activeOrderNumber == -1) {
                ctx.attribute("errorMessage", "læg noget i kurven for at købe");
                showOrderingPage(ctx, connectionPool);
                return;
            }
            ArrayList<Orderline> orderlines = OrderlineMapper.getOrderlinesByOrderNumber(activeOrderNumber, connectionPool);
            if (orderlines.isEmpty()) {
                ctx.attribute("errorMessage", "læg noget i kurven for at købe");
                showOrderingPage(ctx, connectionPool);
                return;
            }

            // Now lets try to validate the balance
            double memberBalance = MemberMapper.getBalance(currentMember.getMemberId(), connectionPool);
            double totalOrderPrice = OrderMapper.PBgetOrderPrice(activeOrderNumber, connectionPool);
            if (totalOrderPrice > memberBalance) {
                ctx.attribute("errorMessage", "Ikke nok penge på kontoen til at gennemføre ordren.");
                ctx.render("errorAlreadyLogin.html");
                return;
            }

            double newBalance = memberBalance - totalOrderPrice;
            MemberMapper.updateMemberBalance(currentMember.getMemberId(), newBalance, connectionPool);
            OrderMapper.updateOrderStatus(activeOrderNumber, "Completed", connectionPool);
            ctx.attribute("activeOrderNumber", activeOrderNumber);
            ctx.render("tak.html");

        } catch (DatabaseException e) {
            ctx.attribute("errorMessage", "Der opstod en fejl under behandlingen af din ordre.");
            ctx.render("errorAlreadyLogin.html");
        }
    }

    private static void showOrder(Context ctx, ConnectionPool connectionPool) {
        Member currentMember = ctx.sessionAttribute("currentMember");
        // TODO: Her skal være et check så currentMember kun har lov til at se sine egne ordre. Ikke andres.
        if (currentMember == null) {
            ctx.attribute("errorMessage", "Log ind for at se ordredetaljer.");
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

            ctx.render("ordredetaljer.html");
        } catch (DatabaseException e) {
            ctx.attribute("errorMessage", "Der er sket en fejl i at hente data");
            ctx.render("error.html");
        }
    }


    private static void showAllOrders(Context ctx, ConnectionPool connectionPool) {
        Member currentMember = ctx.sessionAttribute("currentMember");
        if (currentMember == null) {
            ctx.attribute("errorMessage", "Log ind for at se dine ordrer.");
            ctx.render("error.html");
            return;
        }

        try {
            ArrayList<Order> orders = OrderMapper.getOrdersByMemberId(currentMember.getMemberId(), false, connectionPool);
            ctx.attribute("orders", orders);
            ctx.render("mineordrer.html");
        } catch (DatabaseException e) {
            ctx.attribute("errorMessage", "Der er sket en fejl i at hente data");
            ctx.render("error.html");
        }
    }

    private static void adminShowOrder(Context ctx, ConnectionPool connectionPool) {
        Member currentMember = ctx.sessionAttribute("currentMember");
        if (currentMember == null || !currentMember.getRole().equals("admin")) {
            ctx.attribute("errorMessage", "Kun adgang for admin.");
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
            ctx.attribute("errorMessage", "Kun adgang for admin.");
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
}