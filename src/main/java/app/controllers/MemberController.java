package app.controllers;

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
//        app.post("opretbruger", ctx -> createMember(ctx, connectionPool));

        app.get("logout", ctx -> logout(ctx));
    }

//    private static void createMember(Context ctx, ConnectionPool connectionPool)
//    {
//        // Hent form parametre
//        String name = ctx.formParam("name");
//        String email = ctx.formParam("email");
//        String mobile = ctx.formParam("mobile");
//        String password1 = ctx.formParam("password1");
//        String password2 = ctx.formParam("password2");
//
//        if (password1.equals(password2))
//        {
//            try
//            {
//                MemberMapper.createuser(username, password1, connectionPool);
//                ctx.attribute("message", "Du er hermed oprettet med brugernavn: " + username +
//                        ". Nu skal du logge på.");
//                ctx.render("index.html");
//            }
//
//            catch (DatabaseException e)
//            {
//                ctx.attribute("message", "Dit brugernavn findes allerede. Prøv igen, eller log ind");
//                ctx.render("opretbruger.html");
//            }
//        } else
//        {
//            ctx.attribute("message", "De to kodeord er ikke ens");
//            ctx.render("opretbruger.html");
//        }
//
//    }

    private static void logout(Context ctx) {
        ctx.req().getSession().invalidate();
        ctx.redirect("/");
    }


    public static void login(Context ctx, ConnectionPool connectionPool) {
        // Hent form parametre
        String email = ctx.formParam("email");
        String password = ctx.formParam("password");

        // Check om bruger findes i DB med de angivne email + password
        try {
            Member member = MemberMapper.login(email, password, connectionPool);
            System.out.println(member);
            ctx.sessionAttribute("currentMember", member);

            Order currentOrder = OrderMapper.getActiveOrder(ctx, connectionPool);
            ctx.sessionAttribute("currentOrder", currentOrder);

            // Hvis ja, send videre til forsiden
            ctx.render("index.html");
        } catch (DatabaseException e) {
            // Hvis nej, send tilbage til login side med fejl besked
            ctx.attribute("message", e.getMessage());
            ctx.render("login.html");
        }

    }
}
