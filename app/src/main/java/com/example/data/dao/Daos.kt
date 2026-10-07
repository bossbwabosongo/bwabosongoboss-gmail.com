package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Client
import com.example.data.model.Event
import com.example.data.model.Material
import com.example.data.model.Payment
import com.example.data.model.Reservation
import com.example.data.model.ReservationMaterial
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {
    @Query("SELECT * FROM clients ORDER BY name ASC")
    fun getAllClients(): Flow<List<Client>>

    @Query("SELECT * FROM clients WHERE id = :id LIMIT 1")
    suspend fun getClientById(id: Long): Client?

    @Query("SELECT COUNT(*) FROM clients")
    fun getClientCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: Client): Long

    @Update
    suspend fun updateClient(client: Client)

    @Delete
    suspend fun deleteClient(client: Client)

    @Query("DELETE FROM clients WHERE id = :id")
    suspend fun deleteClientById(id: Long)
}

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY date ASC, time ASC")
    fun getAllEvents(): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE id = :id LIMIT 1")
    suspend fun getEventById(id: Long): Event?

    @Query("SELECT COUNT(*) FROM events")
    fun getEventCount(): Flow<Int>

    @Query("SELECT * FROM events WHERE status != 'Terminé' AND status != 'Annulé' ORDER BY date ASC LIMIT 5")
    fun getUpcomingEvents(): Flow<List<Event>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event): Long

    @Update
    suspend fun updateEvent(event: Event)

    @Delete
    suspend fun deleteEvent(event: Event)

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteEventById(id: Long)
}

@Dao
interface MaterialDao {
    @Query("SELECT * FROM materials ORDER BY category ASC, name ASC")
    fun getAllMaterials(): Flow<List<Material>>

    @Query("SELECT * FROM materials WHERE id = :id LIMIT 1")
    suspend fun getMaterialById(id: Long): Material?

    @Query("SELECT * FROM materials WHERE category = :category ORDER BY name ASC")
    fun getMaterialsByCategory(category: String): Flow<List<Material>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(material: Material): Long

    @Update
    suspend fun updateMaterial(material: Material)

    @Delete
    suspend fun deleteMaterial(material: Material)

    @Query("DELETE FROM materials WHERE id = :id")
    suspend fun deleteMaterialById(id: Long)
}

@Dao
interface ReservationDao {
    @Query("SELECT * FROM reservations ORDER BY eventDate DESC")
    fun getAllReservations(): Flow<List<Reservation>>

    @Query("SELECT * FROM reservations WHERE id = :id LIMIT 1")
    suspend fun getReservationById(id: Long): Reservation?

    @Query("SELECT * FROM reservations WHERE clientId = :clientId ORDER BY eventDate DESC")
    fun getReservationsForClient(clientId: Long): Flow<List<Reservation>>

    @Query("SELECT COUNT(*) FROM reservations")
    fun getReservationCount(): Flow<Int>

    @Query("SELECT SUM(totalAmount) FROM reservations")
    fun getTotalReservationsAmount(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReservation(reservation: Reservation): Long

    @Update
    suspend fun updateReservation(reservation: Reservation)

    @Delete
    suspend fun deleteReservation(reservation: Reservation)

    @Query("DELETE FROM reservations WHERE id = :id")
    suspend fun deleteReservationById(id: Long)

    // Items / Materials for reservation
    @Query("SELECT * FROM reservation_materials WHERE reservationId = :reservationId")
    fun getMaterialsForReservation(reservationId: Long): Flow<List<ReservationMaterial>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReservationMaterial(item: ReservationMaterial): Long

    @Query("DELETE FROM reservation_materials WHERE reservationId = :reservationId")
    suspend fun clearReservationMaterials(reservationId: Long)

    @Query("DELETE FROM reservation_materials WHERE id = :id")
    suspend fun deleteReservationMaterial(id: Long)
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY paymentDate DESC, createdAt DESC")
    fun getAllPayments(): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE reservationId = :reservationId ORDER BY paymentDate DESC")
    fun getPaymentsForReservation(reservationId: Long): Flow<List<Payment>>

    @Query("SELECT SUM(amount) FROM payments WHERE reservationId = :reservationId")
    fun getTotalPaidForReservation(reservationId: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM payments")
    fun getTotalCollectedPayments(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment): Long

    @Update
    suspend fun updatePayment(payment: Payment)

    @Delete
    suspend fun deletePayment(payment: Payment)

    @Query("DELETE FROM payments WHERE id = :id")
    suspend fun deletePaymentById(id: Long)
}
