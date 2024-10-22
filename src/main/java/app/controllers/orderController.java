package app.controllers;

import app.persistence.ConnectionPool;
import io.javalin.Javalin;
import io.javalin.http.Context;


public class orderController {
    public static void addRoutes(Javalin app, ConnectionPool connectionPool) {
        app.get("kunde", ctx -> showPage(ctx, connectionPool));
        app.post("kunde", ctx -> updateBasket(ctx, connectionPool));
    }

    private static void showPage(Context ctx, ConnectionPool connectionPool) {
//        User currentUser = ctx.sessionAttribute("currentUser");
//
//        if (currentUser == null) {
//            ctx.attribute("errorMessage", "Please log in, in order to use this app.");
//            ctx.render("error.html");
//            return;
//        }

//        int user_id = currentUser.getUserId();

        ctx.render("/kunde.html");

//        try {
//            ctx.render("/kunde.html");
//
//        } catch (DatabaseException e) {
//            ctx.attribute("errorMessage", "There was a problem retrieving your data. Please try again later.");
//            ctx.render("error.html");
//            throw new RuntimeException(e);
//        }
    }
    private static void updateBasket(Context ctx, ConnectionPool connectionPool) {
    }
}
