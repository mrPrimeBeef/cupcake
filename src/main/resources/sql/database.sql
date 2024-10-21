BEGIN;

-- Create bottom table
CREATE TABLE IF NOT EXISTS public.bottom
(
    bottom_id
    serial
    NOT
    NULL,
    bottom_name
    character
    varying
    COLLATE
    pg_catalog
    .
    "default"
    NOT
    NULL,
    bottom_price
    numeric
    NOT
    NULL,
    CONSTRAINT
    bottom_pkey
    PRIMARY
    KEY
(
    bottom_id
)
    );

-- Create member table
CREATE TABLE IF NOT EXISTS public.member
(
    member_id
    serial
    NOT
    NULL,
    name
    character
    varying
    COLLATE
    pg_catalog
    .
    "default"
    NOT
    NULL,
    email
    character
    varying
    COLLATE
    pg_catalog
    .
    "default"
    NOT
    NULL,
    mobile
    character
    varying
    NOT
    NULL,
    password
    character
    varying
    COLLATE
    pg_catalog
    .
    "default"
    NOT
    NULL,
    role
    character
    varying
    COLLATE
    pg_catalog
    .
    "default"
    NOT
    NULL
    DEFAULT
    'customer',
    balance
    integer
    NOT
    NULL,
    CONSTRAINT
    customer_pkey
    PRIMARY
    KEY
(
    member_id
)
    );

-- Create member_order table
CREATE TABLE IF NOT EXISTS public.member_order
(
    order_number
    serial
    NOT
    NULL,
    member_id
    integer
    NOT
    NULL,
    date
    date
    NOT
    NULL
    DEFAULT
    Current_date,
    status
    character
    varying
    COLLATE
    pg_catalog
    .
    "default"
    NOT
    NULL,
    order_price
    numeric
    NOT
    NULL,
    CONSTRAINT
    customer_order_pkey
    PRIMARY
    KEY
(
    order_number
)
    );

-- Create orderline table
CREATE TABLE IF NOT EXISTS public.orderline
(
    orderline_id
    integer
    NOT
    NULL,
    bottom_id
    integer
    NOT
    NULL,
    topping_id
    integer
    NOT
    NULL,
    quantity
    integer
    NOT
    NULL,
    orderline_price
    integer
    NOT
    NULL,
    CONSTRAINT
    orderline_pkey
    PRIMARY
    KEY
(
    orderline_id
)
    );

-- Create topping table
CREATE TABLE IF NOT EXISTS public.topping
(
    topping_id
    serial
    NOT
    NULL,
    topping_name
    character
    varying
    COLLATE
    pg_catalog
    .
    "default"
    NOT
    NULL,
    topping_price
    numeric
    NOT
    NULL,
    CONSTRAINT
    topping_pkey
    PRIMARY
    KEY
(
    topping_id
)
    );

-- Add foreign keys
ALTER TABLE IF EXISTS public.member_order
    ADD CONSTRAINT fk_customer FOREIGN KEY (member_id)
    REFERENCES public.member (member_id) MATCH SIMPLE
    ON
UPDATE NO ACTION
ON
DELETE
NO ACTION;

ALTER TABLE IF EXISTS public.orderline
    ADD CONSTRAINT fk_order FOREIGN KEY (orderline_id)
    REFERENCES public.member_order (order_number) MATCH SIMPLE
    ON
UPDATE NO ACTION
ON
DELETE
NO ACTION;

ALTER TABLE IF EXISTS public.orderline
    ADD CONSTRAINT bottom FOREIGN KEY (bottom_id)
    REFERENCES public.bottom (bottom_id) MATCH SIMPLE
    ON
UPDATE NO ACTION
ON
DELETE
NO ACTION
    NOT VALID;

ALTER TABLE IF EXISTS public.orderline
    ADD CONSTRAINT topping FOREIGN KEY (topping_id)
    REFERENCES public.topping (topping_id) MATCH SIMPLE
    ON
UPDATE NO ACTION
ON
DELETE
NO ACTION
    NOT VALID;

INSERT INTO public.bottom (bottom_name, bottom_price)
VALUES ('Chokolade', 5.00),
       ('Vanilje', 5.00),
       ('Muskatnød', 5.00),
       ('Pistacie', 6.00),
       ('Mandel', 7.00);

INSERT INTO public.topping (topping_name, topping_price)
VALUES ('Chokolade', 5.00),
       ('Blåbær', 5.00),
       ('Hindbær', 5.00),
       ('Sprød', 6.00),
       ('Jordbær', 6.00),
       ('Rom/Rosin', 7.00),
       ('Appelsin', 8.00),
       ('Citron', 8.00),
       ('Blåskimmelost', 9.00);

-- Add admin user
INSERT INTO public.member (name, email, mobile, password, role, balance)
VALUES ('admin', 'admin@example.com', '09090909', '1234', 'admin', 0);

END;