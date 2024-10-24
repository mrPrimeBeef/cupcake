package app.controllers;

import app.dto.OrderMemberDto;
import app.entities.Member;
import app.entities.Order;
import app.entities.Orderline;
import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;
import app.persistence.MemberMapper;
import app.persistence.OrderMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.ArrayList;

public class MemberController {

    public static void addRoutes(Javalin app, ConnectionPool connectionPool) {
        app.post("login", ctx -> login(ctx, connectionPool));

        app.get("opretbruger", ctx -> ctx.render("opretbruger.html"));
        app.post("opretbruger", ctx -> createMember(ctx, connectionPool));

        app.get("logout", ctx -> logout(ctx));

        app.get("adminallekunder", ctx -> adminShowAllCustomers(ctx, connectionPool));
//        app.get("adminkunde", ctx -> adminShowCustomer(ctx, connectionPool));
    }


    private static void adminShowAllCustomers(Context ctx, ConnectionPool connectionPool) {
        Member currentMember = ctx.sessionAttribute("currentMember");
        if (currentMember == null || !currentMember.getRole().equals("admin")) {
            ctx.attribute("errorMessage", "Kun for admin.");
            ctx.render("error.html");
            return;
        }

        try {
            ArrayList<Member> allCustomers = MemberMapper.getAllCostumers(connectionPool);
//            ArrayList<OrderMemberDto> allOrderMemberDtos = OrderMapper.getAllOrderMemberDtos(connectionPool);
            ctx.attribute("allCustomers", allCustomers);
            ctx.render("adminallekunder.html");
        } catch (DatabaseException e) {
            ctx.attribute("errorMessage", "Der er sket en fejl i at hente data");
            ctx.render("error.html");
        }
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
                ctx.attribute("message", "Du er hermed oprettet med brugernavn: " + email + ". Nu kan du logge ind.");
                ctx.render("login.html");
            } catch (DatabaseException e) {
                ctx.attribute("message", "Brugernavnet findes allerede");
                ctx.render("opretbruger.html");
            }
        } else {
            ctx.attribute("message", "Kodeord matcher ikke");
            ctx.render("opretbruger.html");
        }

    }

    private static void logout(Context ctx) {
        ctx.req().getSession().invalidate();
        ctx.redirect("/");
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

            Order CurrentOrderId = OrderMapper.getActiveOrder(ctx, connectionPool);
            ctx.sessionAttribute("CurrentOrderId", CurrentOrderId);
            ctx.redirect("kunde");
        } catch (DatabaseException e) {
            ctx.attribute("message", e.getMessage());
            ctx.render("login.html");
        }
    }
}
