package app.entities;

public class Cupcake {
    private int bottomId;
    private int toppingId;
    private int userId;
    private String bottom;
    private String topping;
    private String cupcake;

    public Cupcake(int bottomId, int toppingId, int userId, String bottom, String topping){
        this.bottomId = bottomId;
        this.toppingId = toppingId;
        this.userId = userId;
        this.bottom = bottom;
        this.topping = topping;
    }
    public String cupcake(){return cupcake;}
}