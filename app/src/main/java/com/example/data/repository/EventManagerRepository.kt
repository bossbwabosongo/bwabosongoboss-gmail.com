package com.example.data.repository

import com.example.data.AppDatabase
import com.example.data.model.Client
import com.example.data.model.Event
import com.example.data.model.Material
import com.example.data.model.Payment
import com.example.data.model.Reservation
import com.example.data.model.ReservationMaterial
import com.example.data.sql.MySqlExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class EventManagerRepository(private val database: AppDatabase) {

    private val clientDao = database.clientDao()
    private val eventDao = database.eventDao()
    private val materialDao = database.materialDao()
    private val reservationDao = database.reservationDao()
    private val paymentDao = database.paymentDao()

    // --- Clients ---
    val allClients: Flow<List<Client>> = clientDao.getAllClients()
    val clientCount: Flow<Int> = clientDao.getClientCount()

    suspend fun getClientById(id: Long): Client? = clientDao.getClientById(id)
    suspend fun insertClient(client: Client): Long = clientDao.insertClient(client)
    suspend fun updateClient(client: Client) = clientDao.updateClient(client)
    suspend fun deleteClient(client: Client) = clientDao.deleteClient(client)
    suspend fun deleteClientById(id: Long) = clientDao.deleteClientById(id)

    // --- Events ---
    val allEvents: Flow<List<Event>> = eventDao.getAllEvents()
    val upcomingEvents: Flow<List<Event>> = eventDao.getUpcomingEvents()
    val eventCount: Flow<Int> = eventDao.getEventCount()

    suspend fun getEventById(id: Long): Event? = eventDao.getEventById(id)
    suspend fun insertEvent(event: Event): Long = eventDao.insertEvent(event)
    suspend fun updateEvent(event: Event) = eventDao.updateEvent(event)
    suspend fun deleteEvent(event: Event) = eventDao.deleteEvent(event)
    suspend fun deleteEventById(id: Long) = eventDao.deleteEventById(id)

    // --- Materials ---
    val allMaterials: Flow<List<Material>> = materialDao.getAllMaterials()

    suspend fun getMaterialById(id: Long): Material? = materialDao.getMaterialById(id)
    suspend fun insertMaterial(material: Material): Long = materialDao.insertMaterial(material)
    suspend fun updateMaterial(material: Material) = materialDao.updateMaterial(material)
    suspend fun deleteMaterial(material: Material) = materialDao.deleteMaterial(material)
    suspend fun deleteMaterialById(id: Long) = materialDao.deleteMaterialById(id)

    // --- Reservations ---
    val allReservations: Flow<List<Reservation>> = reservationDao.getAllReservations()
    val reservationCount: Flow<Int> = reservationDao.getReservationCount()
    val totalReservationsAmount: Flow<Double?> = reservationDao.getTotalReservationsAmount()

    suspend fun getReservationById(id: Long): Reservation? = reservationDao.getReservationById(id)
    suspend fun insertReservation(reservation: Reservation): Long = reservationDao.insertReservation(reservation)
    suspend fun updateReservation(reservation: Reservation) = reservationDao.updateReservation(reservation)
    suspend fun deleteReservation(reservation: Reservation) = reservationDao.deleteReservation(reservation)
    suspend fun deleteReservationById(id: Long) = reservationDao.deleteReservationById(id)

    fun getMaterialsForReservation(reservationId: Long): Flow<List<ReservationMaterial>> =
        reservationDao.getMaterialsForReservation(reservationId)

    suspend fun addMaterialToReservation(reservationId: Long, materialId: Long, quantity: Int, unitPrice: Double) =
        reservationDao.insertReservationMaterial(
            ReservationMaterial(
                reservationId = reservationId,
                materialId = materialId,
                quantity = quantity,
                unitPrice = unitPrice
            )
        )

    suspend fun clearReservationMaterials(reservationId: Long) =
        reservationDao.clearReservationMaterials(reservationId)

    suspend fun deleteReservationMaterial(id: Long) =
        reservationDao.deleteReservationMaterial(id)

    // --- Payments ---
    val allPayments: Flow<List<Payment>> = paymentDao.getAllPayments()
    val totalCollectedPayments: Flow<Double?> = paymentDao.getTotalCollectedPayments()

    fun getPaymentsForReservation(reservationId: Long): Flow<List<Payment>> =
        paymentDao.getPaymentsForReservation(reservationId)

    fun getTotalPaidForReservation(reservationId: Long): Flow<Double?> =
        paymentDao.getTotalPaidForReservation(reservationId)

    suspend fun insertPayment(payment: Payment): Long = paymentDao.insertPayment(payment)
    suspend fun updatePayment(payment: Payment) = paymentDao.updatePayment(payment)
    suspend fun deletePayment(payment: Payment) = paymentDao.deletePayment(payment)
    suspend fun deletePaymentById(id: Long) = paymentDao.deletePaymentById(id)

    // --- MySQL Dump Generation ---
    suspend fun generateMySqlDump(): String = withContext(Dispatchers.IO) {
        val clients = allClients.first()
        val events = allEvents.first()
        val materials = allMaterials.first()
        val reservations = allReservations.first()
        val payments = allPayments.first()

        // Gather all reservation materials
        val allResMaterials = mutableListOf<ReservationMaterial>()
        for (res in reservations) {
            val list = reservationDao.getMaterialsForReservation(res.id).first()
            allResMaterials.addAll(list)
        }

        MySqlExporter.generateMySqlScript(
            clients = clients,
            events = events,
            materials = materials,
            reservations = reservations,
            reservationMaterials = allResMaterials,
            payments = payments
        )
    }
}
