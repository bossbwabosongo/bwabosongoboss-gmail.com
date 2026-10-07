package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.Client
import com.example.data.model.Event
import com.example.data.model.Material
import com.example.data.model.Payment
import com.example.data.model.Reservation
import com.example.data.model.ReservationMaterial
import com.example.data.repository.EventManagerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FinancialKpi(
    val totalRevenue: Double = 0.0,
    val totalCollected: Double = 0.0,
    val totalRemaining: Double = 0.0,
    val totalEventsCount: Int = 0,
    val totalReservationsCount: Int = 0,
    val totalClientsCount: Int = 0
)

data class InvoiceData(
    val invoiceNumber: String,
    val issueDate: String,
    val client: Client?,
    val event: Event?,
    val reservation: Reservation,
    val items: List<Pair<Material, Int>>, // Material and quantity
    val subtotal: Double,
    val totalPaid: Double,
    val remainingDue: Double,
    val payments: List<Payment>
)

class EventManagerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EventManagerRepository

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = EventManagerRepository(database)
    }

    // Reactive State
    val clients: StateFlow<List<Client>> = repository.allClients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val events: StateFlow<List<Event>> = repository.allEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val upcomingEvents: StateFlow<List<Event>> = repository.upcomingEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val materials: StateFlow<List<Material>> = repository.allMaterials
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reservations: StateFlow<List<Reservation>> = repository.allReservations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payments: StateFlow<List<Payment>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Financial KPIs combined
    val financialKpi: StateFlow<FinancialKpi> = combine(
        reservations,
        payments,
        events,
        clients
    ) { resList, payList, evtList, cltList ->
        val totalRev = resList.sumOf { it.totalAmount }
        val totalPaid = payList.sumOf { it.amount }
        val remaining = (totalRev - totalPaid).coerceAtLeast(0.0)

        FinancialKpi(
            totalRevenue = totalRev,
            totalCollected = totalPaid,
            totalRemaining = remaining,
            totalEventsCount = evtList.size,
            totalReservationsCount = resList.size,
            totalClientsCount = cltList.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialKpi())

    // MySQL dump state
    private val _mySqlDump = MutableStateFlow<String?>(null)
    val mySqlDump: StateFlow<String?> = _mySqlDump.asStateFlow()

    private val _isGeneratingSql = MutableStateFlow(false)
    val isGeneratingSql: StateFlow<Boolean> = _isGeneratingSql.asStateFlow()

    // Notification / Toast Message
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun clearUserMessage() {
        _userMessage.value = null
    }

    // --- Clients CRUD ---
    fun saveClient(client: Client) {
        viewModelScope.launch {
            if (client.id == 0L) {
                repository.insertClient(client)
                _userMessage.value = "Client '${client.name}' ajouté avec succès"
            } else {
                repository.updateClient(client)
                _userMessage.value = "Client '${client.name}' mis à jour"
            }
        }
    }

    fun deleteClient(client: Client) {
        viewModelScope.launch {
            repository.deleteClient(client)
            _userMessage.value = "Client '${client.name}' supprimé"
        }
    }

    // --- Events CRUD ---
    fun saveEvent(event: Event) {
        viewModelScope.launch {
            if (event.id == 0L) {
                repository.insertEvent(event)
                _userMessage.value = "Événement '${event.title}' planifié"
            } else {
                repository.updateEvent(event)
                _userMessage.value = "Événement '${event.title}' mis à jour"
            }
        }
    }

    fun deleteEvent(event: Event) {
        viewModelScope.launch {
            repository.deleteEvent(event)
            _userMessage.value = "Événement supprimé"
        }
    }

    // --- Materials CRUD ---
    fun saveMaterial(material: Material) {
        viewModelScope.launch {
            if (material.id == 0L) {
                repository.insertMaterial(material)
                _userMessage.value = "Matériel '${material.name}' ajouté au catalogue"
            } else {
                repository.updateMaterial(material)
                _userMessage.value = "Matériel '${material.name}' mis à jour"
            }
        }
    }

    fun deleteMaterial(material: Material) {
        viewModelScope.launch {
            repository.deleteMaterial(material)
            _userMessage.value = "Matériel supprimé"
        }
    }

    // --- Reservations CRUD ---
    fun saveReservation(
        reservation: Reservation,
        selectedMaterials: List<Pair<Long, Int>> = emptyList() // materialId to quantity
    ) {
        viewModelScope.launch {
            val resId = if (reservation.id == 0L) {
                repository.insertReservation(reservation)
            } else {
                repository.updateReservation(reservation)
                reservation.id
            }

            if (selectedMaterials.isNotEmpty()) {
                val currentMaterials = materials.value.associateBy { it.id }
                if (reservation.id != 0L) {
                    repository.clearReservationMaterials(resId)
                }
                for ((matId, qty) in selectedMaterials) {
                    val price = currentMaterials[matId]?.unitRentalPrice ?: 0.0
                    repository.addMaterialToReservation(resId, matId, qty, price)
                }
            }
            _userMessage.value = "Réservation enregistrée avec succès"
        }
    }

    fun deleteReservation(reservation: Reservation) {
        viewModelScope.launch {
            repository.deleteReservation(reservation)
            _userMessage.value = "Réservation supprimée"
        }
    }

    // --- Payments CRUD ---
    fun addPayment(payment: Payment) {
        viewModelScope.launch {
            repository.insertPayment(payment)
            // Update reservation status if fully paid
            val res = repository.getReservationById(payment.reservationId)
            if (res != null) {
                val allResPayments = payments.value.filter { it.reservationId == res.id }
                val newPaidTotal = allResPayments.sumOf { it.amount } + payment.amount
                val newStatus = when {
                    newPaidTotal >= res.totalAmount -> "Confirmée"
                    newPaidTotal > 0 -> "Acompte versé"
                    else -> res.status
                }
                if (newStatus != res.status) {
                    repository.updateReservation(res.copy(status = newStatus))
                }
            }
            _userMessage.value = "Paiement de ${formatCurrency(payment.amount)} enregistré"
        }
    }

    fun deletePayment(payment: Payment) {
        viewModelScope.launch {
            repository.deletePayment(payment)
            _userMessage.value = "Paiement supprimé"
        }
    }

    // --- Generate Invoice Data ---
    fun getInvoiceForReservation(
        reservation: Reservation,
        onReady: (InvoiceData) -> Unit
    ) {
        viewModelScope.launch {
            val client = clients.value.find { it.id == reservation.clientId }
            val event = events.value.find { it.id == reservation.eventId }
            val resPayments = payments.value.filter { it.reservationId == reservation.id }
            val totalPaid = resPayments.sumOf { it.amount }
            val remaining = (reservation.totalAmount - totalPaid).coerceAtLeast(0.0)

            val invoiceNumber = "FACT-${SimpleDateFormat("yyyyMM", Locale.getDefault()).format(Date())}-${String.format(Locale.ROOT, "%04d", reservation.id)}"
            val issueDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

            val invoice = InvoiceData(
                invoiceNumber = invoiceNumber,
                issueDate = issueDate,
                client = client,
                event = event,
                reservation = reservation,
                items = emptyList(), // Can be enriched with materials
                subtotal = reservation.totalAmount,
                totalPaid = totalPaid,
                remainingDue = remaining,
                payments = resPayments
            )
            onReady(invoice)
        }
    }

    // --- MySQL Export ---
    fun generateMySqlDump() {
        viewModelScope.launch {
            _isGeneratingSql.value = true
            try {
                val sql = repository.generateMySqlDump()
                _mySqlDump.value = sql
                _userMessage.value = "Script MySQL généré avec succès (${sql.lines().size} lignes)"
            } catch (e: Exception) {
                _userMessage.value = "Erreur lors de la génération MySQL : ${e.message}"
            } finally {
                _isGeneratingSql.value = false
            }
        }
    }

    companion object {
        fun formatCurrency(amount: Double): String {
            val format = NumberFormat.getCurrencyInstance(Locale.FRANCE)
            return format.format(amount)
        }
    }
}
