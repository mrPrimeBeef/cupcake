package app.controllers;

import java.util.ArrayList;
import io.javalin.Javalin;
import io.javalin.http.Context;
import app.entities.Member;
import app.entities.Order;
import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;
import app.persistence.MemberMapper;
import app.persistence.OrderMapper;

public class MemberController {

    public static void addRoutes(Javalin app, ConnectionPool connectionPool) {
        app.get("/", ctx -> ctx.render("login.html"));
        app.post("login", ctx -> login(ctx, connectionPool));
        app.get("logout", ctx -> logout(ctx));
        app.get("opretbruger", ctx -> ctx.render("opretbruger.html"));
        app.post("opretbruger", ctx -> createMember(ctx, connectionPool));
        app.get("adminkunde", ctx -> adminShowCustomer(ctx, connectionPool));
        app.get("adminallekunder", ctx -> adminShowAllCustomers(ctx, connectionPool));
    }

    public static void login(Context ctx, ConnectionPool connectionPool) {
        String email = ctx.formParam("email");
        String password = ctx.formParam("password");

        try {
            Member member = MemberMapper.login(email, password, connectionPool);
            ctx.sessionAttribute("currentMember", member);

            if (member.getRole().equals("admin")) {
                ctx.redirect("adminalleordrer");
                return;
            }
            ctx.redirect("bestil");

        } catch (DatabaseException e) {
            ctx.attribute("message", e.getMessage());
            ctx.render("login.html");
        }
    }

    private static void logout(Context ctx) {
        ctx.req().getSession().invalidate();
        ctx.redirect("/");
    }

    private static void createMember(Context ctx, ConnectionPool connectionPool) {
        String name = ctx.formParam("name");
        String email = ctx.formParam("email");
        String mobile = ctx.formParam("mobile");
        String password1 = ctx.formParam("password1");
        String password2 = ctx.formParam("password2");

        if (password1.equals(password2)) {
            try {
                MemberMapper.createMember(name, email, mobile, password1, connectionPool);
                ctx.attribute("message", "Du er hermed oprettet som bruger med emailen: " + email);
                ctx.render("login.html");
            } catch (DatabaseException e) {
                ctx.attribute("message", "Emailen er allerede i brug.");
                ctx.render("opretbruger.html");
            }
        } else {
            ctx.attribute("message", "Adgangskoderne matcher ikke.");
            ctx.render("opretbruger.html");
        }
    }

    private static void adminShowCustomer(Context ctx, ConnectionPool connectionPool) {
        Member currentMember = ctx.sessionAttribute("currentMember");
        if (currentMember == null || !currentMember.getRole().equals("admin")) {
            ctx.attribute("errorMessage", "Kun adgang for admin.");
            ctx.render("error.html");
            return;
        }

        int customerNumber = Integer.parseInt(ctx.queryParam("kundenr"));

        try {
            Member customer = MemberMapper.getMemberById(customerNumber, connectionPool);
            ArrayList<Order> orders = OrderMapper.getOrdersByMemberId(customerNumber, true, connectionPool);

            ctx.attribute("customer", customer);
            ctx.attribute("orders", orders);
            ctx.render("adminkunde.html");

        } catch (DatabaseException e) {
            ctx.attribute("errorMessage", "Der er sket en fejl i at hente data for kunde nummer: " + customerNumber);
            ctx.render("error.html");
        }
    }

    private static void adminShowAllCustomers(Context ctx, ConnectionPool connectionPool) {
        Member currentMember = ctx.sessionAttribute("currentMember");
        if (currentMember == null || !currentMember.getRole().equals("admin")) {
            ctx.attribute("errorMessage", "Kun adgang for admin.");
            ctx.render("error.html");
            return;
        }

        try {
            ArrayList<Member> customers = MemberMapper.getAllCostumers(connectionPool);
            ctx.attribute("customers", customers);
            ctx.render("adminallekunder.html");
        } catch (DatabaseException e) {
            ctx.attribute("errorMessage", "Der er sket en fejl i at hente data.");
            ctx.render("error.html");
        }
    }
}