package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "clients")
data class Client(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val email: String,
    val company: String = "",
    val address: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val type: String, // Mariage, Anniversaire, Conférence, Séminaire, Festival, Gala, Soirée d'entreprise, Autre
    val date: String, // YYYY-MM-DD
    val time: String = "14:00",
    val location: String,
    val guestCount: Int = 50,
    val status: String = "Planifié", // Planifié, En cours, Terminé, Annulé
    val estimatedBudget: Double = 0.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "materials")
data class Material(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String, // Chaises, Tables, Tentes, Podiums, Sonorisation, Éclairage, Décoration, Autre
    val totalStock: Int,
    val availableStock: Int,
    val unitRentalPrice: Double, // Prix par unité par jour
    val description: String = ""
)

@Entity(
    tableName = "reservations",
    foreignKeys = [
        ForeignKey(
            entity = Client::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Event::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("clientId"), Index("eventId")]
)
data class Reservation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clientId: Long,
    val eventId: Long? = null,
    val reservationDate: String, // Date de prise de réservation
    val eventDate: String, // Date prévue pour l'événement
    val location: String,
    val status: String = "Confirmée", // Confirmée, En attente, Acompte versé, Terminée, Annulée
    val totalAmount: Double = 0.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "reservation_materials",
    foreignKeys = [
        ForeignKey(
            entity = Reservation::class,
            parentColumns = ["id"],
            childColumns = ["reservationId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Material::class,
            parentColumns = ["id"],
            childColumns = ["materialId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("reservationId"), Index("materialId")]
)
data class ReservationMaterial(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val reservationId: Long,
    val materialId: Long,
    val quantity: Int,
    val unitPrice: Double
)

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = Reservation::class,
            parentColumns = ["id"],
            childColumns = ["reservationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("reservationId")]
)
data class Payment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val reservationId: Long,
    val amount: Double,
    val paymentDate: String,
    val paymentMethod: String = "Espèces", // Espèces, Carte bancaire, Virement, Mobile Money, Chèque
    val reference: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

// DTO for Reservation with complete details
data class ReservationDetail(
    val reservation: Reservation,
    val client: Client?,
    val event: Event?,
    val paidAmount: Double,
    val remainingAmount: Double,
    val itemsCount: Int
)
