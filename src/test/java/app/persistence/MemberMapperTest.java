package app.persistence;

import app.entities.Member;
import app.exceptions.DatabaseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

class MemberMapperTest {
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";
    private static final String URL = "jdbc:postgresql://localhost:5432/%s?currentSchema=public";
    private static final String DB = "cupcake";

    private static final ConnectionPool connectionPool = ConnectionPool.getInstance(USER, PASSWORD, URL, DB);

    @BeforeAll
    public static void setUpClass() {

        try (Connection connection = connectionPool.getConnection();
             Statement stmt = connection.createStatement()) {

            stmt.execute("DROP TABLE IF EXISTS test.orderline CASCADE;");
            stmt.execute("DROP TABLE IF EXISTS test.member_order CASCADE;");
            stmt.execute("DROP TABLE IF EXISTS test.topping CASCADE;");
            stmt.execute("DROP TABLE IF EXISTS test.bottom CASCADE;");
            stmt.execute("DROP TABLE IF EXISTS test.member CASCADE;");

            stmt.execute("DROP SEQUENCE IF EXISTS test.bottom_bottom_id_seq CASCADE;");
            stmt.execute("DROP SEQUENCE IF EXISTS test.member_member_id_seq CASCADE;");
            stmt.execute("DROP SEQUENCE IF EXISTS test.member_order_order_number_seq CASCADE;");
            stmt.execute("DROP SEQUENCE IF EXISTS test.orderline_orderline_id_seq CASCADE;");
            stmt.execute("DROP SEQUENCE IF EXISTS test.topping_topping_id_seq CASCADE;");


            stmt.execute("""
                        CREATE TABLE test.bottom (
                            bottom_id serial NOT NULL,
                            bottom_name character varying COLLATE pg_catalog."default" NOT NULL,
                            bottom_price numeric NOT NULL,
                            CONSTRAINT bottom_pkey PRIMARY KEY (bottom_id)
                        );
                    """);

            stmt.execute("""
                        CREATE TABLE test.member (
                            member_id serial NOT NULL,
                            name character varying COLLATE pg_catalog."default" NOT NULL,
                            email character varying COLLATE pg_catalog."default" NOT NULL,
                            mobile character varying COLLATE pg_catalog."default" NOT NULL,
                            password character varying COLLATE pg_catalog."default" NOT NULL,
                            role character varying COLLATE pg_catalog."default" NOT NULL DEFAULT 'customer'::character varying,
                            balance integer NOT NULL,
                            CONSTRAINT customer_pkey PRIMARY KEY (member_id),
                            CONSTRAINT email UNIQUE (email)
                        );
                    """);

            stmt.execute("""
                        CREATE TABLE test.member_order (
                            order_number serial NOT NULL,
                            member_id integer NOT NULL,
                            date date NOT NULL DEFAULT CURRENT_DATE,
                            status character varying COLLATE pg_catalog."default" NOT NULL,
                            order_price numeric NOT NULL,
                            CONSTRAINT customer_order_pkey PRIMARY KEY (order_number)
                        );
                    """);

            stmt.execute("""
                        CREATE TABLE test.orderline (
                            orderline_id serial NOT NULL,
                            order_number integer NOT NULL,
                            bottom_id integer NOT NULL,
                            topping_id integer NOT NULL,
                            quantity integer NOT NULL,
                            orderline_price numeric NOT NULL,
                            CONSTRAINT orderline_pkey PRIMARY KEY (orderline_id)
                        );
                    """);

            stmt.execute("""
                        CREATE TABLE test.topping (
                            topping_id serial NOT NULL,
                            topping_name character varying COLLATE pg_catalog."default" NOT NULL,
                            topping_price numeric NOT NULL,
                            CONSTRAINT topping_pkey PRIMARY KEY (topping_id)
                        );
                    """);

            // Opret udenlandske nøglebegrænsninger
            stmt.execute("""
                        ALTER TABLE test.member_order
                            ADD CONSTRAINT fk_customer FOREIGN KEY (member_id)
                            REFERENCES test.member (member_id) MATCH SIMPLE
                            ON UPDATE NO ACTION
                            ON DELETE NO ACTION;
                    """);

            stmt.execute("""
                        ALTER TABLE test.orderline
                            ADD CONSTRAINT fk_bottom FOREIGN KEY (bottom_id)
                            REFERENCES test.bottom (bottom_id) MATCH SIMPLE
                            ON UPDATE NO ACTION
                            ON DELETE NO ACTION;
                    """);

            stmt.execute("""
                        ALTER TABLE test.orderline
                            ADD CONSTRAINT fk_order FOREIGN KEY (order_number)
                            REFERENCES test.member_order (order_number) MATCH SIMPLE
                            ON UPDATE NO ACTION
                            ON DELETE NO ACTION;
                    """);

            stmt.execute("""
                        ALTER TABLE test.orderline
                            ADD CONSTRAINT fk_topping FOREIGN KEY (topping_id)
                            REFERENCES test.topping (topping_id) MATCH SIMPLE
                            ON UPDATE NO ACTION
                            ON DELETE NO ACTION;
                    """);

            // Indsæt testdata i testtabeller
            stmt.execute("""
                        INSERT INTO test.bottom (bottom_name, bottom_price)
                        VALUES 
                            ('Chokolade', 5.00),
                            ('Vanilje', 5.00),
                            ('Muskatnød', 5.00),
                            ('Pistacie', 6.00),
                            ('Mandel', 7.00);
                    """);

            stmt.execute("""
                        INSERT INTO test.topping (topping_name, topping_price)
                        VALUES 
                            ('Chokolade', 5.00),
                            ('Blåbær', 5.00),
                            ('Hindbær', 5.00),
                            ('Sprød', 6.00),
                            ('Jordbær', 6.00),
                            ('Rom/Rosin', 7.00),
                            ('Appelsin', 8.00),
                            ('Citron', 8.00),
                            ('Blåskimmelost', 9.00);
                    """);

            stmt.execute("""
                        INSERT INTO test.member (name, email, mobile, password, role, balance)
                        VALUES
                            ('admin', 'admin@example.com', '09090909', '1234', 'admin', 0),
                            ('test', 'test@example.com', '08080808', '1234', 'customer', 1000);
                    """);

        } catch (SQLException e) {
            e.printStackTrace();
            // Håndter eventuelle fejl her
        }
    }

    @Test
    void login() throws DatabaseException {
        Member member = null;
        String sql = "SELECT * FROM member WHERE email=? AND password=?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setString(1, "test@example.com");
            ps.setString(2, "1234");

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int memberId = rs.getInt("member_id");
                String name = rs.getString("name");
                String email = rs.getString("email");
                String password = rs.getString("password");
                String mobile = rs.getString("mobile");
                String role = rs.getString("role");
                int balance = rs.getInt("balance");
                member = new Member(memberId, name, email, mobile, password, role, balance);
            }
        } catch (SQLException e) {

        }

        assertEquals("test",member.getName());
        assertEquals("customer", member.getRole());
        assertEquals(1000, member.getBalance());
    }

    @Test
    void createMember() {
    }
}