package com.example.data.sql

import com.example.data.model.Client
import com.example.data.model.Event
import com.example.data.model.Material
import com.example.data.model.Payment
import com.example.data.model.Reservation
import com.example.data.model.ReservationMaterial
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object MySqlExporter {

    fun generateMySqlScript(
        clients: List<Client>,
        events: List<Event>,
        materials: List<Material>,
        reservations: List<Reservation>,
        reservationMaterials: List<ReservationMaterial>,
        payments: List<Payment>
    ): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val generatedAt = dateFormat.format(Date())

        val sb = StringBuilder()
        sb.append("-- ==========================================================\n")
        sb.append("-- EventManager - Système de Gestion des Événements\n")
        sb.append("-- Script de sauvegarde / Export MySQL & MariaDB\n")
        sb.append("-- Généré le : $generatedAt\n")
        sb.append("-- Compatible : MySQL 5.7+ / MySQL 8.0+ / MariaDB 10.3+\n")
        sb.append("-- ==========================================================\n\n")

        sb.append("SET FOREIGN_KEY_CHECKS = 0;\n")
        sb.append("SET NAMES utf8mb4;\n\n")

        sb.append("-- Création de la base de données\n")
        sb.append("CREATE DATABASE IF NOT EXISTS `eventmanager_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;\n")
        sb.append("USE `eventmanager_db`;\n\n")

        // Table Clients
        sb.append("-- ----------------------------------------------------------\n")
        sb.append("-- Structure de la table `clients`\n")
        sb.append("-- ----------------------------------------------------------\n")
        sb.append("CREATE TABLE IF NOT EXISTS `clients` (\n")
        sb.append("  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,\n")
        sb.append("  `name` VARCHAR(191) NOT NULL,\n")
        sb.append("  `phone` VARCHAR(50) NOT NULL,\n")
        sb.append("  `email` VARCHAR(191) NOT NULL,\n")
        sb.append("  `company` VARCHAR(191) DEFAULT '',\n")
        sb.append("  `address` TEXT,\n")
        sb.append("  `notes` TEXT,\n")
        sb.append("  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP\n")
        sb.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;\n\n")

        if (clients.isNotEmpty()) {
            sb.append("INSERT INTO `clients` (`id`, `name`, `phone`, `email`, `company`, `address`, `notes`) VALUES\n")
            val rows = clients.joinToString(",\n") { c ->
                "  (${c.id}, '${escape(c.name)}', '${escape(c.phone)}', '${escape(c.email)}', '${escape(c.company)}', '${escape(c.address)}', '${escape(c.notes)}')"
            }
            sb.append(rows).append(";\n\n")
        }

        // Table Events
        sb.append("-- ----------------------------------------------------------\n")
        sb.append("-- Structure de la table `events`\n")
        sb.append("-- ----------------------------------------------------------\n")
        sb.append("CREATE TABLE IF NOT EXISTS `events` (\n")
        sb.append("  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,\n")
        sb.append("  `title` VARCHAR(191) NOT NULL,\n")
        sb.append("  `type` VARCHAR(100) NOT NULL,\n")
        sb.append("  `date` DATE NOT NULL,\n")
        sb.append("  `time` TIME DEFAULT '14:00:00',\n")
        sb.append("  `location` VARCHAR(191) NOT NULL,\n")
        sb.append("  `guest_count` INT DEFAULT 50,\n")
        sb.append("  `status` ENUM('Planifié', 'En cours', 'Terminé', 'Annulé') DEFAULT 'Planifié',\n")
        sb.append("  `estimated_budget` DECIMAL(12,2) DEFAULT 0.00,\n")
        sb.append("  `notes` TEXT,\n")
        sb.append("  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP\n")
        sb.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;\n\n")

        if (events.isNotEmpty()) {
            sb.append("INSERT INTO `events` (`id`, `title`, `type`, `date`, `time`, `location`, `guest_count`, `status`, `estimated_budget`, `notes`) VALUES\n")
            val rows = events.joinToString(",\n") { e ->
                val timeStr = if (e.time.count { it == ':' } == 1) "${e.time}:00" else e.time
                "  (${e.id}, '${escape(e.title)}', '${escape(e.type)}', '${escape(e.date)}', '${escape(timeStr)}', '${escape(e.location)}', ${e.guestCount}, '${escape(e.status)}', ${e.estimatedBudget}, '${escape(e.notes)}')"
            }
            sb.append(rows).append(";\n\n")
        }

        // Table Materials
        sb.append("-- ----------------------------------------------------------\n")
        sb.append("-- Structure de la table `materials` (Inventaire matériel)\n")
        sb.append("-- ----------------------------------------------------------\n")
        sb.append("CREATE TABLE IF NOT EXISTS `materials` (\n")
        sb.append("  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,\n")
        sb.append("  `name` VARCHAR(191) NOT NULL,\n")
        sb.append("  `category` VARCHAR(100) NOT NULL,\n")
        sb.append("  `total_stock` INT NOT NULL DEFAULT 0,\n")
        sb.append("  `available_stock` INT NOT NULL DEFAULT 0,\n")
        sb.append("  `unit_rental_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00,\n")
        sb.append("  `description` TEXT\n")
        sb.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;\n\n")

        if (materials.isNotEmpty()) {
            sb.append("INSERT INTO `materials` (`id`, `name`, `category`, `total_stock`, `available_stock`, `unit_rental_price`, `description`) VALUES\n")
            val rows = materials.joinToString(",\n") { m ->
                "  (${m.id}, '${escape(m.name)}', '${escape(m.category)}', ${m.totalStock}, ${m.availableStock}, ${m.unitRentalPrice}, '${escape(m.description)}')"
            }
            sb.append(rows).append(";\n\n")
        }

        // Table Reservations
        sb.append("-- ----------------------------------------------------------\n")
        sb.append("-- Structure de la table `reservations`\n")
        sb.append("-- ----------------------------------------------------------\n")
        sb.append("CREATE TABLE IF NOT EXISTS `reservations` (\n")
        sb.append("  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,\n")
        sb.append("  `client_id` BIGINT NOT NULL,\n")
        sb.append("  `event_id` BIGINT DEFAULT NULL,\n")
        sb.append("  `reservation_date` DATE NOT NULL,\n")
        sb.append("  `event_date` DATE NOT NULL,\n")
        sb.append("  `location` VARCHAR(191) NOT NULL,\n")
        sb.append("  `status` ENUM('Confirmée', 'En attente', 'Acompte versé', 'Terminée', 'Annulée') DEFAULT 'Confirmée',\n")
        sb.append("  `total_amount` DECIMAL(12,2) DEFAULT 0.00,\n")
        sb.append("  `notes` TEXT,\n")
        sb.append("  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n")
        sb.append("  CONSTRAINT `fk_res_client` FOREIGN KEY (`client_id`) REFERENCES `clients` (`id`) ON DELETE CASCADE,\n")
        sb.append("  CONSTRAINT `fk_res_event` FOREIGN KEY (`event_id`) REFERENCES `events` (`id`) ON DELETE SET NULL\n")
        sb.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;\n\n")

        if (reservations.isNotEmpty()) {
            sb.append("INSERT INTO `reservations` (`id`, `client_id`, `event_id`, `reservation_date`, `event_date`, `location`, `status`, `total_amount`, `notes`) VALUES\n")
            val rows = reservations.joinToString(",\n") { r ->
                val eventIdStr = r.eventId?.toString() ?: "NULL"
                "  (${r.id}, ${r.clientId}, $eventIdStr, '${escape(r.reservationDate)}', '${escape(r.eventDate)}', '${escape(r.location)}', '${escape(r.status)}', ${r.totalAmount}, '${escape(r.notes)}')"
            }
            sb.append(rows).append(";\n\n")
        }

        // Table Reservation Materials
        sb.append("-- ----------------------------------------------------------\n")
        sb.append("-- Structure de la table `reservation_materials`\n")
        sb.append("-- ----------------------------------------------------------\n")
        sb.append("CREATE TABLE IF NOT EXISTS `reservation_materials` (\n")
        sb.append("  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,\n")
        sb.append("  `reservation_id` BIGINT NOT NULL,\n")
        sb.append("  `material_id` BIGINT NOT NULL,\n")
        sb.append("  `quantity` INT NOT NULL,\n")
        sb.append("  `unit_price` DECIMAL(10,2) NOT NULL,\n")
        sb.append("  CONSTRAINT `fk_rm_reservation` FOREIGN KEY (`reservation_id`) REFERENCES `reservations` (`id`) ON DELETE CASCADE,\n")
        sb.append("  CONSTRAINT `fk_rm_material` FOREIGN KEY (`material_id`) REFERENCES `materials` (`id`) ON DELETE CASCADE\n")
        sb.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;\n\n")

        if (reservationMaterials.isNotEmpty()) {
            sb.append("INSERT INTO `reservation_materials` (`id`, `reservation_id`, `material_id`, `quantity`, `unit_price`) VALUES\n")
            val rows = reservationMaterials.joinToString(",\n") { rm ->
                "  (${rm.id}, ${rm.reservationId}, ${rm.materialId}, ${rm.quantity}, ${rm.unitPrice})"
            }
            sb.append(rows).append(";\n\n")
        }

        // Table Payments
        sb.append("-- ----------------------------------------------------------\n")
        sb.append("-- Structure de la table `payments` (Paiements & Acomptes)\n")
        sb.append("-- ----------------------------------------------------------\n")
        sb.append("CREATE TABLE IF NOT EXISTS `payments` (\n")
        sb.append("  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,\n")
        sb.append("  `reservation_id` BIGINT NOT NULL,\n")
        sb.append("  `amount` DECIMAL(12,2) NOT NULL,\n")
        sb.append("  `payment_date` DATE NOT NULL,\n")
        sb.append("  `payment_method` VARCHAR(50) DEFAULT 'Espèces',\n")
        sb.append("  `reference` VARCHAR(100) DEFAULT '',\n")
        sb.append("  `notes` TEXT,\n")
        sb.append("  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n")
        sb.append("  CONSTRAINT `fk_pay_reservation` FOREIGN KEY (`reservation_id`) REFERENCES `reservations` (`id`) ON DELETE CASCADE\n")
        sb.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;\n\n")

        if (payments.isNotEmpty()) {
            sb.append("INSERT INTO `payments` (`id`, `reservation_id`, `amount`, `payment_date`, `payment_method`, `reference`, `notes`) VALUES\n")
            val rows = payments.joinToString(",\n") { p ->
                "  (${p.id}, ${p.reservationId}, ${p.amount}, '${escape(p.paymentDate)}', '${escape(p.paymentMethod)}', '${escape(p.reference)}', '${escape(p.notes)}')"
            }
            sb.append(rows).append(";\n\n")
        }

        sb.append("SET FOREIGN_KEY_CHECKS = 1;\n")
        sb.append("-- Fin du script de sauvegarde\n")

        return sb.toString()
    }

    private fun escape(value: String): String {
        return value.replace("\\", "\\\\")
            .replace("'", "\\'")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
    }
}
