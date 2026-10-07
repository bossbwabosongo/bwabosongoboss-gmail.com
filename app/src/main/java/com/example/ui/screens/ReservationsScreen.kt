package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Client
import com.example.data.model.Event
import com.example.data.model.Payment
import com.example.data.model.Reservation
import com.example.ui.EventManagerViewModel
import com.example.ui.components.EmptyPlaceholder
import com.example.ui.components.StatusBadge
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseError
import com.example.ui.theme.TealTertiary

@Composable
fun ReservationsScreen(
    reservations: List<Reservation>,
    clients: List<Client>,
    events: List<Event>,
    payments: List<Payment>,
    onSaveReservation: (Reservation) -> Unit,
    onDeleteReservation: (Reservation) -> Unit,
    onGenerateInvoice: (Reservation) -> Unit,
    onAddPaymentForReservation: (Reservation) -> Unit
) {
    var filterStatus by remember { mutableStateOf("Tous") }
    var showDialog by remember { mutableStateOf(false) }
    var reservationToEdit by remember { mutableStateOf<Reservation?>(null) }
    var reservationToDelete by remember { mutableStateOf<Reservation?>(null) }

    val statusFilters = listOf("Tous", "Confirmée", "Acompte versé", "En attente", "Terminée", "Annulée")

    val clientMap = clients.associateBy { it.id }
    val eventMap = events.associateBy { it.id }

    val filteredReservations = if (filterStatus == "Tous") {
        reservations
    } else {
        reservations.filter { it.status.equals(filterStatus, ignoreCase = true) }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    reservationToEdit = null
                    showDialog = true
                },
                containerColor = TealTertiary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_reservation")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nouvelle réservation")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("reservations_screen")
        ) {
            // Status Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(statusFilters) { status ->
                    FilterChip(
                        selected = filterStatus == status,
                        onClick = { filterStatus = status },
                        label = { Text(status, fontSize = 12.sp) }
                    )
                }
            }

            if (filteredReservations.isEmpty()) {
                EmptyPlaceholder(
                    icon = Icons.Default.CalendarMonth,
                    title = "Aucune réservation trouvée",
                    description = if (filterStatus == "Tous")
                        "Créez une réservation pour associer un client, une date et un lieu."
                    else "Aucune réservation avec le statut '$filterStatus'."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredReservations, key = { it.id }) { res ->
                        val client = clientMap[res.clientId]
                        val event = res.eventId?.let { eventMap[it] }
                        val resPayments = payments.filter { it.reservationId == res.id }
                        val paid = resPayments.sumOf { it.amount }
                        val remaining = (res.totalAmount - paid).coerceAtLeast(0.0)

                        ReservationCard(
                            reservation = res,
                            client = client,
                            event = event,
                            paidAmount = paid,
                            remainingAmount = remaining,
                            onEdit = {
                                reservationToEdit = res
                                showDialog = true
                            },
                            onDelete = { reservationToDelete = res },
                            onInvoice = { onGenerateInvoice(res) },
                            onPayment = { onAddPaymentForReservation(res) }
                        )
                    }
                }
            }
        }
    }

    // Reservation Form Dialog
    if (showDialog) {
        ReservationFormDialog(
            initialReservation = reservationToEdit,
            clients = clients,
            events = events,
            onDismiss = { showDialog = false },
            onConfirm = { saved ->
                onSaveReservation(saved)
                showDialog = false
            }
        )
    }

    // Delete Confirmation
    if (reservationToDelete != null) {
        AlertDialog(
            onDismissRequest = { reservationToDelete = null },
            title = { Text("Supprimer cette réservation ?") },
            text = { Text("Confirmez-vous la suppression de la réservation N°${reservationToDelete?.id} ?") },
            confirmButton = {
                Button(
                    onClick = {
                        reservationToDelete?.let { onDeleteReservation(it) }
                        reservationToDelete = null
                    }
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { reservationToDelete = null }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
fun ReservationCard(
    reservation: Reservation,
    client: Client?,
    event: Event?,
    paidAmount: Double,
    remainingAmount: Double,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onInvoice: () -> Unit,
    onPayment: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("reservation_card_${reservation.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Réservation #${reservation.id}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = client?.name ?: "Client inconnu",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                StatusBadge(status = reservation.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (event != null) {
                Text(
                    text = "Événement : ${event.title} (${event.type})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Date : ${reservation.eventDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = reservation.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Financial Summary Block
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Total", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = EventManagerViewModel.formatCurrency(reservation.totalAmount),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Column {
                        Text("Payé", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = EventManagerViewModel.formatCurrency(paidAmount),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = EmeraldSuccess
                        )
                    }
                    Column {
                        Text("Reste dû", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = EventManagerViewModel.formatCurrency(remainingAmount),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (remainingAmount > 0) RoseError else EmeraldSuccess
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = onInvoice,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("btn_invoice_${reservation.id}")
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Facture", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = onPayment,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.MonetizationOn, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Payer", fontSize = 12.sp)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Modifier", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ReservationFormDialog(
    initialReservation: Reservation?,
    clients: List<Client>,
    events: List<Event>,
    onDismiss: () -> Unit,
    onConfirm: (Reservation) -> Unit
) {
    if (clients.isEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Aucun client existant") },
            text = { Text("Veuillez d'abord créer au moins un client avant d'ajouter une réservation.") },
            confirmButton = {
                Button(onClick = onDismiss) { Text("D'accord") }
            }
        )
        return
    }

    var selectedClientId by remember {
        mutableStateOf(initialReservation?.clientId ?: clients.first().id)
    }
    var selectedEventId by remember {
        mutableStateOf<Long?>(initialReservation?.eventId ?: events.firstOrNull()?.id)
    }
    var eventDate by remember {
        mutableStateOf(initialReservation?.eventDate ?: "2026-10-25")
    }
    var location by remember {
        mutableStateOf(initialReservation?.location ?: (events.firstOrNull()?.location ?: "Salle des Fêtes"))
    }
    var totalAmountStr by remember {
        mutableStateOf((initialReservation?.totalAmount ?: 2500.0).toString())
    }
    var status by remember {
        mutableStateOf(initialReservation?.status ?: "Confirmée")
    }
    var notes by remember {
        mutableStateOf(initialReservation?.notes ?: "")
    }

    val statusOptions = listOf("Confirmée", "Acompte versé", "En attente", "Terminée", "Annulée")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initialReservation == null) "Nouvelle réservation" else "Modifier réservation")
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Client Selector
                item {
                    Text("Sélectionner le client *", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(clients) { c ->
                            FilterChip(
                                selected = selectedClientId == c.id,
                                onClick = { selectedClientId = c.id },
                                label = { Text(c.name, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Event Selector
                if (events.isNotEmpty()) {
                    item {
                        Text("Associer à un événement (optionnel)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            item {
                                FilterChip(
                                    selected = selectedEventId == null,
                                    onClick = { selectedEventId = null },
                                    label = { Text("Aucun", fontSize = 11.sp) }
                                )
                            }
                            items(events) { e ->
                                FilterChip(
                                    selected = selectedEventId == e.id,
                                    onClick = {
                                        selectedEventId = e.id
                                        location = e.location
                                        eventDate = e.date
                                    },
                                    label = { Text(e.title, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = eventDate,
                        onValueChange = { eventDate = it },
                        label = { Text("Date de l'événement (AAAA-MM-JJ) *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Lieu de l'événement *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = totalAmountStr,
                        onValueChange = { totalAmountStr = it },
                        label = { Text("Montant total facturé (€) *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text("Statut de la réservation", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(statusOptions) { s ->
                            FilterChip(
                                selected = status == s,
                                onClick = { status = s },
                                label = { Text(s, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes / Détails de livraison") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (location.isNotBlank()) {
                        val newRes = Reservation(
                            id = initialReservation?.id ?: 0L,
                            clientId = selectedClientId,
                            eventId = selectedEventId,
                            reservationDate = initialReservation?.reservationDate ?: "2026-10-06",
                            eventDate = eventDate.trim(),
                            location = location.trim(),
                            status = status,
                            totalAmount = totalAmountStr.toDoubleOrNull() ?: 0.0,
                            notes = notes.trim(),
                            createdAt = initialReservation?.createdAt ?: System.currentTimeMillis()
                        )
                        onConfirm(newRes)
                    }
                },
                modifier = Modifier.testTag("btn_save_reservation")
            ) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
