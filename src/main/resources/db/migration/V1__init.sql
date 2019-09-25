DROP TABLE IF EXISTS User_Details;

CREATE TABLE User_Details(
    id SERIAL PRIMARY KEY ,
    name VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(255),
    password VARCHAR(255)
);
