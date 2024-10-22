package app.controllers;

import app.entities.Bottom;
import app.entities.Orderline;
import app.entities.Topping;
import app.exceptions.DatabaseException;
import app.persistence.*;
import io.javalin.Javalin;
import io.javalin.http.Context;

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

        String selectedBottom = ctx.formParam("bund");
        Bottom bottom = BottomMapper.getBottomByName(selectedBottom, connectionPool);

        String selectedTopping = ctx.formParam("topping");
        Topping topping = ToppingMapper.getToppingByName(selectedTopping, connectionPool);

        String quantity = ctx.formParam("antal");
        double toppingPrice = topping.getToppingPrice();
        double bottomPrice = bottom.getBottomPrice();

        double orderlinePrice = toppingPrice + bottomPrice;



        Orderline orderline = new Orderline(ordernumber, bottom,topping, quantity, orderlinePrice);

        OrderlineMapper.createOrderline(orderline, connectionPool);
    }
}
