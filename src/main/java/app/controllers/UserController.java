package app.controllers;

import app.entities.Member;
import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;
import app.persistence.MemberMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class UserController
{
    public static void addRoutes(Javalin app, ConnectionPool connectionPool)
    {
        app.get("login", ctx -> ctx.render("login.html"));

        app.post("login", ctx -> login(ctx, connectionPool));
        app.get("logout", ctx -> logout(ctx));
        app.get("createuser", ctx -> ctx.render("createuser.html"));
//        app.post("createuser", ctx -> createUser(ctx, connectionPool));
    }

//    private static void createUser(Context ctx, ConnectionPool connectionPool)
//    {
//        // Hent form parametre
//        String username = ctx.formParam("username");
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
//                ctx.render("createuser.html");
//            }
//        } else
//        {
//            ctx.attribute("message", "Dine to passwords matcher ikke! Prøv igen");
//            ctx.render("createuser.html");
//        }
//
//    }

    private static void logout(Context ctx)
    {
        ctx.req().getSession().invalidate();
        ctx.redirect("/");
    }


    public static void login(Context ctx, ConnectionPool connectionPool)
    {
        // Hent form parametre
        String email = ctx.formParam("email");
        String password = ctx.formParam("password");

        // Check om bruger findes i DB med de angivne email + password
        try
        {
            Member user = MemberMapper.login(email, password, connectionPool);
            ctx.sessionAttribute("currentUser", user);
            // Hvis ja, send videre til forsiden med login besked
            ctx.attribute("message", "Du er nu logget ind");
            ctx.render("index.html");
        }
        catch (DatabaseException e)
        {
            // Hvis nej, send tilbage til login side med fejl besked
            ctx.attribute("message", e.getMessage() );
            ctx.render("login.html");
        }

    }
}
