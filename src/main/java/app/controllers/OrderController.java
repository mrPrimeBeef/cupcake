package app.controllers;

import app.entities.Bottom;
import app.entities.Topping;
import app.exceptions.DatabaseException;
import app.persistence.BottomMapper;
import app.persistence.ConnectionPool;
import app.persistence.ToppingMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.ArrayList;


public class OrderController {
    public static void addRoutes(Javalin app, ConnectionPool connectionPool) {
        app.get("kunde", ctx -> showPage(ctx, connectionPool));
       // app.post("kunde", ctx -> updateBasket(ctx, connectionPool));
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

            System.out.println(bottoms);
            System.out.println(toppings);

        } catch (DatabaseException e) {
            ctx.attribute("errorMessage", "There was a problem retrieving your data. Please try again later.");
            ctx.render("error.html");
            throw new RuntimeException(e);

        }
        ctx.render("kunde.html");
    }

    private static void updateBasket(Context ctx, ConnectionPool connectionPool) {

    }
}
