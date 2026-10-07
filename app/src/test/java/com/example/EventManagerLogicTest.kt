package com.example

import com.example.data.model.Client
import com.example.data.model.Event
import com.example.data.model.Material
import com.example.data.model.Payment
import com.example.data.model.Reservation
import com.example.data.model.ReservationMaterial
import com.example.data.sql.MySqlExporter
import org.junit.Assert.assertTrue
import org.junit.Test

class EventManagerLogicTest {

    @Test
    fun testMySqlScriptGeneration() {
        val client = Client(
            id = 1,
            name = "Jean Dupont",
            phone = "+33612345678",
            email = "jean@example.com",
            company = "Events Corp"
        )
        val event = Event(
            id = 1,
            title = "Mariage de Jean",
            type = "Mariage",
            date = "2026-10-30",
            time = "14:00",
            location = "Château",
            guestCount = 100,
            status = "Planifié",
            estimatedBudget = 5000.0
        )
        val material = Material(
            id = 1,
            name = "Chaises Napoléon",
            category = "Chaises",
            totalStock = 200,
            availableStock = 100,
            unitRentalPrice = 4.0
        )
        val reservation = Reservation(
            id = 1,
            clientId = 1,
            eventId = 1,
            reservationDate = "2026-10-01",
            eventDate = "2026-10-30",
            location = "Château",
            status = "Confirmée",
            totalAmount = 3000.0
        )
        val payment = Payment(
            id = 1,
            reservationId = 1,
            amount = 1500.0,
            paymentDate = "2026-10-02",
            paymentMethod = "Virement"
        )

        val sqlScript = MySqlExporter.generateMySqlScript(
            clients = listOf(client),
            events = listOf(event),
            materials = listOf(material),
            reservations = listOf(reservation),
            reservationMaterials = listOf(ReservationMaterial(1, 1, 1, 50, 4.0)),
            payments = listOf(payment)
        )

        assertTrue(sqlScript.contains("CREATE DATABASE IF NOT EXISTS `eventmanager_db`"))
        assertTrue(sqlScript.contains("CREATE TABLE IF NOT EXISTS `clients`"))
        assertTrue(sqlScript.contains("CREATE TABLE IF NOT EXISTS `events`"))
        assertTrue(sqlScript.contains("CREATE TABLE IF NOT EXISTS `materials`"))
        assertTrue(sqlScript.contains("CREATE TABLE IF NOT EXISTS `reservations`"))
        assertTrue(sqlScript.contains("CREATE TABLE IF NOT EXISTS `payments`"))
        assertTrue(sqlScript.contains("Jean Dupont"))
        assertTrue(sqlScript.contains("Mariage de Jean"))
        assertTrue(sqlScript.contains("1500.0"))
    }
}
