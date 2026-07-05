-- =====================================================================
-- ERASM (Enterprise Resource Allocation and Skill Management System)
-- Database Creation Script
-- =====================================================================
-- NOTE: If you run the Spring Boot application with
--       spring.jpa.hibernate.ddl-auto=update, Hibernate will create/update
--       these tables automatically on startup and you do NOT need to run
--       this script manually. This script is provided as a deliverable and
--       for reference / manual setup, and matches the JPA entity mappings
--       exactly.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS erasm_db;
USE erasm_db;

-- ---------------------------------------------------------------------
-- 1. roles
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE
);

-- ---------------------------------------------------------------------
-- 2. users
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role_id BIGINT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_date VARCHAR(50),
    modified_date VARCHAR(50),
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles (id)
);

-- ---------------------------------------------------------------------
-- 3. employees
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    role_id BIGINT,
    department VARCHAR(100),
    designation VARCHAR(100),
    date_of_joining VARCHAR(50),
    created_by VARCHAR(150),
    created_date VARCHAR(50),
    modified_by VARCHAR(150),
    modified_date VARCHAR(50),
    CONSTRAINT fk_employees_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_employees_role FOREIGN KEY (role_id) REFERENCES roles (id)
);

-- ---------------------------------------------------------------------
-- 4. skills
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    skill_name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- ---------------------------------------------------------------------
-- 5. employee_skill_profile (implements Employee <-> Skill Many-To-Many,
--    enriched with proficiency level and years of experience)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS employee_skill_profile (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    proficiency_level VARCHAR(50) NOT NULL,
    years_of_experience DOUBLE,
    created_date VARCHAR(50),
    modified_date VARCHAR(50),
    CONSTRAINT fk_empskill_employee FOREIGN KEY (employee_id) REFERENCES employees (id),
    CONSTRAINT fk_empskill_skill FOREIGN KEY (skill_id) REFERENCES skills (id),
    CONSTRAINT uq_employee_skill UNIQUE (employee_id, skill_id)
);

-- ---------------------------------------------------------------------
-- 6. certifications
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS certifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    certification_name VARCHAR(150) NOT NULL,
    issued_by VARCHAR(150),
    issued_date VARCHAR(50),
    CONSTRAINT fk_certifications_employee FOREIGN KEY (employee_id) REFERENCES employees (id)
);

-- ---------------------------------------------------------------------
-- 7. projects
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_name VARCHAR(150) NOT NULL,
    client_name VARCHAR(150),
    start_date VARCHAR(50),
    end_date VARCHAR(50),
    technology_stack VARCHAR(255),
    budget DOUBLE,
    status VARCHAR(30) DEFAULT 'OPEN',
    created_by VARCHAR(150),
    created_date VARCHAR(50),
    modified_by VARCHAR(150),
    modified_date VARCHAR(50)
);

-- ---------------------------------------------------------------------
-- 8. resource_requests
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS resource_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    required_count INT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    remarks VARCHAR(500),
    created_by VARCHAR(150),
    created_date VARCHAR(50),
    modified_by VARCHAR(150),
    modified_date VARCHAR(50),
    CONSTRAINT fk_resourcereq_project FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT fk_resourcereq_skill FOREIGN KEY (skill_id) REFERENCES skills (id)
);

-- ---------------------------------------------------------------------
-- 9. allocations
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS allocations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    resource_request_id BIGINT,
    allocation_percentage DOUBLE NOT NULL,
    start_date VARCHAR(50),
    end_date VARCHAR(50),
    status VARCHAR(30) DEFAULT 'ACTIVE',
    created_by VARCHAR(150),
    created_date VARCHAR(50),
    modified_by VARCHAR(150),
    modified_date VARCHAR(50),
    CONSTRAINT fk_allocations_employee FOREIGN KEY (employee_id) REFERENCES employees (id),
    CONSTRAINT fk_allocations_project FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT fk_allocations_resourcereq FOREIGN KEY (resource_request_id) REFERENCES resource_requests (id)
);

-- ---------------------------------------------------------------------
-- 10. audit_logs
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity_name VARCHAR(100) NOT NULL,
    entity_id BIGINT,
    action VARCHAR(50) NOT NULL,
    details VARCHAR(1000),
    created_by VARCHAR(150),
    modified_by VARCHAR(150),
    created_date VARCHAR(50),
    modified_date VARCHAR(50)
);

-- =====================================================================
-- Seed data: mandatory roles (also auto-seeded by the application on
-- startup via DataInitializer, safe to run either way)
-- =====================================================================
INSERT IGNORE INTO roles (role_name) VALUES ('ADMIN');
INSERT IGNORE INTO roles (role_name) VALUES ('DELIVERY_MANAGER');
INSERT IGNORE INTO roles (role_name) VALUES ('RESOURCE_MANAGER');
INSERT IGNORE INTO roles (role_name) VALUES ('EMPLOYEE');
INSERT IGNORE INTO roles (role_name) VALUES ('AUDITOR');

-- Seed data: a few sample skills matching the requirements document
INSERT IGNORE INTO skills (skill_name, description) VALUES ('Java', 'Core Java and Java EE');
INSERT IGNORE INTO skills (skill_name, description) VALUES ('Spring Boot', 'Spring Boot framework');
INSERT IGNORE INTO skills (skill_name, description) VALUES ('React', 'React JS front-end library');
INSERT IGNORE INTO skills (skill_name, description) VALUES ('Angular', 'Angular front-end framework');
INSERT IGNORE INTO skills (skill_name, description) VALUES ('AWS', 'Amazon Web Services cloud platform');
INSERT IGNORE INTO skills (skill_name, description) VALUES ('Azure', 'Microsoft Azure cloud platform');
