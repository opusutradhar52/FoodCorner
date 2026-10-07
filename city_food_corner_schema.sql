CREATE DATABASE IF NOT EXISTS city_food_corner
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE city_food_corner;

-- Drop in child-before-parent order (sale_items references sales) so a
-- second run of this script never fails with "table already exists".
DROP TABLE IF EXISTS sale_items;
DROP TABLE IF EXISTS sales;
DROP TABLE IF EXISTS shift_records;
DROP TABLE IF EXISTS cash_expenses;
DROP TABLE IF EXISTS cash_reserve;
DROP TABLE IF EXISTS salary_config;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS employees;

-- ---------------------------------------------------------------------
-- 1. employees
--    One row per person who has ever signed up (Salesman / Inventory
--    Manager). employee_code (e.g. CFCE-001) is only filled in, and then
--    locked forever, the first time the admin confirms the account.
-- ---------------------------------------------------------------------
CREATE TABLE employees (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    employee_code   VARCHAR(20)  UNIQUE NULL,
    name            VARCHAR(100) NOT NULL,
    email           VARCHAR(100) NOT NULL UNIQUE,
    mobile          VARCHAR(15)  NOT NULL,
    password        VARCHAR(255) NOT NULL,
    role            ENUM('Salesman', 'Inventory Manager') NOT NULL,
    status          ENUM('Pending', 'Approved', 'Blocked') NOT NULL DEFAULT 'Pending',
    joined_on       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_locked       BOOLEAN NOT NULL DEFAULT FALSE
);

-- ---------------------------------------------------------------------
-- 2. products
--    Shared inventory - written by the Inventory Manager (InventoryManage
--    screen), mirrored read-only for the Admin (AdminInventory screen)
--    and shown as food cards to the Salesman (SaleItems screen).
-- ---------------------------------------------------------------------
CREATE TABLE products (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    product_id      VARCHAR(20)  NOT NULL UNIQUE,   -- e.g. CFC-001
    product_name    VARCHAR(100) NOT NULL,
    type            VARCHAR(50)  NOT NULL,
    stock           INT NOT NULL DEFAULT 0,
    price           DECIMAL(10,2) NOT NULL DEFAULT 0,
    status          VARCHAR(30)  NOT NULL,
    image_path      VARCHAR(255) NULL,
    created_date    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------
-- 3. sales
--    One row per completed sale/payment on the SaleItems (POS) screen.
-- ---------------------------------------------------------------------
CREATE TABLE sales (
    sale_id         INT AUTO_INCREMENT PRIMARY KEY,
    customer_name   VARCHAR(100) NOT NULL,
    customer_mobile VARCHAR(20)  NOT NULL,
    total_amount    DECIMAL(10,2) NOT NULL,
    amount_paid     DECIMAL(10,2) NOT NULL,
    change_amount   DECIMAL(10,2) NOT NULL,
    seller_id       VARCHAR(20)  NOT NULL,   -- employees.employee_code of the salesman
    seller_name     VARCHAR(100) NOT NULL,
    sale_date       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------
-- 4. sale_items
--    Line items belonging to a sale (what was actually bought).
-- ---------------------------------------------------------------------
CREATE TABLE sale_items (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    sale_id         INT NOT NULL,
    product_id      VARCHAR(20)  NOT NULL,
    product_name    VARCHAR(100) NOT NULL,
    quantity        INT NOT NULL,
    unit_price      DECIMAL(10,2) NOT NULL,
    line_total      DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_sale_items_sale FOREIGN KEY (sale_id) REFERENCES sales(sale_id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- 5. shift_records
--    Check-in / check-out log, one row per employee sign-in (see
--    ShiftRecord.fxml). AppConfig.ALLOW_MULTIPLE_LOGIN_PER_DAY in the
--    Java code controls whether more than one row per employee per day
--    is allowed.
-- ---------------------------------------------------------------------
CREATE TABLE shift_records (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    employee_code   VARCHAR(20)  NOT NULL,
    employee_name   VARCHAR(100) NOT NULL,
    role            VARCHAR(30)  NOT NULL,
    check_in        DATETIME NOT NULL,
    check_out       DATETIME NULL,
    work_date       DATE NOT NULL
);

-- ---------------------------------------------------------------------
-- 6. cash_expenses
--    Every employee salary payment AND every Personal / Inventory Update
--    withdrawal from the CashFlow screen lands here as one row.
-- ---------------------------------------------------------------------
CREATE TABLE cash_expenses (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    category        VARCHAR(30)  NOT NULL,   -- 'Employee Salary' / 'Personal' / 'For Inventory Update'
    description     VARCHAR(255) NULL,
    amount          DECIMAL(10,2) NOT NULL,
    expense_date    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------
-- 7. cash_reserve
--    Single-row table holding the running "Total Reserved" balance that
--    carries over month to month (see CashFlowDAO.getTotalReserved()).
-- ---------------------------------------------------------------------
CREATE TABLE cash_reserve (
    id                  INT PRIMARY KEY,
    total_reserved      DECIMAL(12,2) NOT NULL DEFAULT 0,
    last_updated_month  VARCHAR(7) NOT NULL   -- format: YYYY-MM
);

-- ---------------------------------------------------------------------
-- 8. salary_config
--    Single-row table remembering the last salary figures typed into the
--    CashFlow screen, so they reload automatically next time it opens.
-- ---------------------------------------------------------------------
CREATE TABLE salary_config (
    id                          INT PRIMARY KEY,
    base_salesman_salary        DECIMAL(10,2) NOT NULL DEFAULT 0,
    increment_percentage        DECIMAL(5,2)  NOT NULL DEFAULT 0,
    inventory_manager_salary    DECIMAL(10,2) NOT NULL DEFAULT 0
);


