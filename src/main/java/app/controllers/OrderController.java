package app.controllers;

import java.util.ArrayList;
import io.javalin.Javalin;
import io.javalin.http.Context;
import app.dto.OrderMemberDto;
import app.entities.*;
import app.exceptions.DatabaseException;
import app.persistence.*;

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
            ctx.attribute("errorMessage", "Der var et problem ved at hente data.");
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

            int activeOrderNumber = OrderMapper.getActiveOrderNumber(currentMember.getMemberId(), connectionPool);
            if (activeOrderNumber == -1) {
                activeOrderNumber = OrderMapper.createActiveOrder(currentMember.getMemberId(), connectionPool);
            }

            OrderlineMapper.createOrderline(activeOrderNumber, bottomId, toppingId, quantity, orderlinePrice, connectionPool);

            showOrderingPage(ctx, connectionPool);

        } catch (DatabaseException e) {
            ctx.attribute("errorMessage", "Der var et problem med at tilføje til kurven.");
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
            int activeOrderNumber = OrderMapper.getActiveOrderNumber(currentMember.getMemberId(), connectionPool);
            ArrayList<Orderline> orderlines = OrderlineMapper.getOrderlinesByOrderNumber(activeOrderNumber, connectionPool);
            if (orderlines.isEmpty()) {
                ctx.attribute("tomKurv", "Kurven er tom.");
                ctx.render("kurv.html");
                return;
            }

            double totalPrice = OrderMapper.getOrderPrice(activeOrderNumber, connectionPool);

            ctx.attribute("orderlines", orderlines);
            ctx.attribute("totalPrice", totalPrice);
            ctx.render("kurv.html");

        } catch (DatabaseException e) {
            ctx.attribute("errorMessage", "Der opstod et problem ved hentningen af dataen, prøv igen.");
            ctx.render("errorAlreadyLogin.html");
        }
    }

    private static void deleteOrderline(Context ctx, ConnectionPool connectionPool) {
        int orderlineId = Integer.parseInt(ctx.pathParam("id"));
        try {
            OrderlineMapper.deleteOrderline(orderlineId, connectionPool);
            ctx.redirect("/kurv");
        } catch (DatabaseException e) {
            ctx.attribute("errorMessage", "Der opstod et problem med at slette ordrelinjen.");
            ctx.render("errorAlreadyLogin.html");
        }
    }

    private static void attemptCheckout(Context ctx, ConnectionPool connectionPool) {
        Member currentMember = ctx.sessionAttribute("currentMember");
        if (currentMember == null) {
            ctx.attribute("errorMessage", "Log ind for at købe.");
            ctx.render("error.html");
            return;
        }

        try {

            int activeOrderNumber = OrderMapper.getActiveOrderNumber(currentMember.getMemberId(), connectionPool);
            ArrayList<Orderline> orderlines = OrderlineMapper.getOrderlinesByOrderNumber(activeOrderNumber, connectionPool);
            if (orderlines.isEmpty()) {
                ctx.attribute("errorMessage", "Læg noget i kurven for at købe.");
                showOrderingPage(ctx, connectionPool);
                return;
            }

            double memberBalance = MemberMapper.getBalance(currentMember.getMemberId(), connectionPool);
            double totalOrderPrice = OrderMapper.getOrderPrice(activeOrderNumber, connectionPool);
            if (totalOrderPrice > memberBalance) {
                ctx.attribute("errorMessage", "Der er ikke nok penge på din konto til at gennemføre ordren.");
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
        if (currentMember == null) {
            ctx.attribute("errorMessage", "Log ind for at se ordredetaljer.");
            ctx.render("error.html");
            return;
        }

        int orderNumber = Integer.parseInt(ctx.queryParam("ordrenr"));

        try {
            OrderMemberDto orderMemberDto = OrderMapper.getOrderMemberDtoByOrderNumber(orderNumber, connectionPool);
            if (currentMember.getMemberId() != orderMemberDto.getMemberId()) {
                ctx.attribute("errorMessage", "Du har ikke adgang til at se denne ordrer.");
                ctx.render("errorAlreadyLogin.html");
                return;
            }

            ArrayList<Orderline> orderlines = OrderlineMapper.getOrderlinesByOrderNumber(orderNumber, connectionPool);

            ctx.attribute("orderMemberDto", orderMemberDto);
            ctx.attribute("orderlines", orderlines);
            ctx.render("ordredetaljer.html");

        } catch (DatabaseException e) {
            ctx.attribute("errorMessage", "Der er sket en fejl i at hente data.");
            ctx.render("errorAlreadyLogin.html");
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
            ctx.attribute("errorMessage", "Der er sket en fejl i at hente data.");
            ctx.render("errorAlreadyLogin.html");
        }
    }

    private static void adminShowOrder(Context ctx, ConnectionPool connectionPool) {
        Member currentMember = ctx.sessionAttribute("currentMember");
        if (currentMember == null || !currentMember.getRole().equals("admin")) {
            ctx.attribute("errorMessage", "Kun adgang for admin.");
            ctx.render("error.html");
            return;
        }

        int orderNumber = Integer.parseInt(ctx.queryParam("ordrenr"));

        try {
            OrderMemberDto orderMemberDto = OrderMapper.getOrderMemberDtoByOrderNumber(orderNumber, connectionPool);
            ArrayList<Orderline> orderlines = OrderlineMapper.getOrderlinesByOrderNumber(orderNumber, connectionPool);

            ctx.attribute("orderMemberDto", orderMemberDto);
            ctx.attribute("orderlines", orderlines);
            ctx.render("adminordre.html");

        } catch (DatabaseException e) {
            ctx.attribute("errorMessage", "Der er sket en fejl i at hente data.");
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
            ArrayList<OrderMemberDto> orderMemberDtos = OrderMapper.getAllOrderMemberDtos(connectionPool);
            ctx.attribute("orderMemberDtos", orderMemberDtos);
            ctx.render("adminalleordrer.html");
        } catch (DatabaseException e) {
            ctx.attribute("errorMessage", "Der er sket en fejl i at hente data.");
            ctx.render("error.html");
        }
    }
}