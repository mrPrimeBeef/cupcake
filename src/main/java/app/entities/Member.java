package app.entities;

public class Member {
    private int memberId;
    private String name;
    private String email;
    private String mobile;
    private String password;
    private String role;
    private int balance;

    public Member(int memberId, String name, String email, String mobile, String password, String role, int balance) {
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.password = password;
        this.role = role;
        this.balance = balance;
    }

    public int getMemberId() {
        return memberId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getMobile() {
        return mobile;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public int getBalance() {
        return balance;
    }

    //    TODO: Gør denne metode færdig
    @Override
    public String toString() {
        return "Member{" +
                "memberId=" + memberId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", mobile='" + mobile + '\'' +
                ", password='" + password + '\'' +
                ", role='" + role + '\'' +
                ", balance=" + balance +
                '}';
    }
}
