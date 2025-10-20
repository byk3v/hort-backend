-- seed.sql  (MySQL/MariaDB)
-- Crea BD si no existe y usa un esquema limpio
CREATE DATABASE IF NOT EXISTS hortdb CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE hortdb;

-- Evita problemas de FKs al insertar en orden arbitrario
SET FOREIGN_KEY_CHECKS = 0;

-- ========================
-- 1) Tablas
-- ========================

CREATE TABLE IF NOT EXISTS `person` (
                                        `id` bigint NOT NULL AUTO_INCREMENT,
                                        `phone` varchar(40) DEFAULT NULL,
    `first_name` varchar(120) NOT NULL,
    `last_name` varchar(120) NOT NULL,
    `address` varchar(250) DEFAULT NULL,
    PRIMARY KEY (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `group` (
                                       `id` bigint NOT NULL AUTO_INCREMENT,
                                       `name` varchar(160) NOT NULL,
    PRIMARY KEY (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `student` (
                                         `allowed_time_to_leave` date DEFAULT NULL,
                                         `can_leave_alone` bit(1) NOT NULL,
    `group_id` bigint NOT NULL,
    `id` bigint NOT NULL AUTO_INCREMENT,
    `person_id` bigint NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_student_person` (`person_id`),
    KEY `fk_student_group` (`group_id`),
    CONSTRAINT `fk_student_group` FOREIGN KEY (`group_id`) REFERENCES `group` (`id`),
    CONSTRAINT `fk_student_person` FOREIGN KEY (`person_id`) REFERENCES `person` (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `collector` (
                                           `id` bigint NOT NULL AUTO_INCREMENT,
                                           `person_id` bigint NOT NULL,
                                           `collector_type` enum('ADULT','STUDENT') NOT NULL,
    PRIMARY KEY (`id`),
    KEY `fk_collector_person` (`person_id`),
    CONSTRAINT `fk_collector_person` FOREIGN KEY (`person_id`) REFERENCES `person` (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `pickup_right` (
                                              `collector_id` bigint NOT NULL,
                                              `id` bigint NOT NULL AUTO_INCREMENT,
                                              `student_id` bigint NOT NULL,
                                              `type` enum('DAILY','PERMANENT') NOT NULL,
    PRIMARY KEY (`id`),
    KEY `fk_permission_collector` (`collector_id`),
    KEY `fk_permission_student` (`student_id`),
    CONSTRAINT `fk_permission_collector` FOREIGN KEY (`collector_id`) REFERENCES `collector` (`id`),
    CONSTRAINT `fk_permission_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `check_out` (
                                           `date_time` date NOT NULL,
                                           `id` bigint NOT NULL AUTO_INCREMENT,
                                           `id_collector` bigint NOT NULL,
                                           `id_student` bigint NOT NULL,
                                           `comment` varchar(500) DEFAULT NULL,
    `user_id` varchar(255) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_checkout_student_date` (`id_student`,`date_time`),
    KEY `fk_checkout_collector` (`id_collector`),
    CONSTRAINT `fk_checkout_collector` FOREIGN KEY (`id_collector`) REFERENCES `collector` (`id`),
    CONSTRAINT `fk_checkout_student` FOREIGN KEY (`id_student`) REFERENCES `student` (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ========================
-- 2) Datos de prueba (idempotentes)
-- Usamos INSERT IGNORE con IDs explícitos para que puedas ejecutar múltiples veces
-- ========================

-- PERSON
INSERT IGNORE INTO `person` (`id`,`phone`,`first_name`,`last_name`,`address`) VALUES
(1,'017455555','Anna','Muller','Hauptstrasse 5, Leipzig'),
(2,'017455555','Matilda','Muller','Hauptstrasse 5, Leipzig'),
(3,'017455555','Gina','Muller','Hauptstrasse 5, Leipzig'),
(4,'017455555','Rosa','Muller','Hauptstrasse 5, Leipzig'),
(5,'017455555','Elisabeth','Muller','Hauptstrasse 5, Leipzig'),
(6,'017455555','Larifea','Muller','Hauptstrasse 5, Leipzig'),
(7,'017455555','Wilhelm','Muller','Hauptstrasse 5, Leipzig'),
(8,'017455555','Paul','Muller','Hauptstrasse 5, Leipzig'),
(9,'017455555','Paul','Heinz','Hauptstrasse 5, Leipzig'),
(10,'017455555','Paul','Heinzin','Hauptstrasse 5, Leipzig');

-- GROUP
INSERT IGNORE INTO `group` (`id`,`name`) VALUES
(1,'Gruppe 1A'),(2,'Gruppe 1B'),(3,'Gruppe 1C'),(4,'Gruppe 1D'),
(5,'Gruppe 2A'),(6,'Gruppe 2B'),(7,'Gruppe 2C'),(8,'Gruppe 2D'),
(9,'Gruppe 3A'),(10,'Gruppe 3B'),(11,'Gruppe 3TM1'),(12,'Gruppe 3TM2');

-- STUDENT
INSERT IGNORE INTO `student` (`id`,`group_id`,`person_id`) VALUES
(1,2,1),
(2,2,2),
(3,2,3),
(4,2,4),
(5,2,5),
(6,2,6),
(7,2,7),
(8,2,8),
(9,3,9),
(10,10,10);

-- (Opcional) COLLECTOR / PICKUP_RIGHT con datos de ejemplo
-- INSERT IGNORE INTO collector (id, person_id, collector_type) VALUES (1, 7, 'ADULT');
-- INSERT IGNORE INTO pickup_right (id, collector_id, student_id, type) VALUES (1, 1, 1, 'PERMANENT');

SET FOREIGN_KEY_CHECKS = 1;
