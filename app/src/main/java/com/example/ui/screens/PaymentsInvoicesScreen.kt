package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Client
import com.example.data.model.Event
import com.example.data.model.Payment
import com.example.data.model.Reservation
import com.example.ui.EventManagerViewModel
import com.example.ui.FinancialKpi
import com.example.ui.InvoiceData
import com.example.ui.components.EmptyPlaceholder
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RoseError

@Composable
fun PaymentsInvoicesScreen(
    kpis: FinancialKpi,
    payments: List<Payment>,
    reservations: List<Reservation>,
    clients: List<Client>,
    events: List<Event>,
    onAddPayment: (Payment) -> Unit,
    onDeletePayment: (Payment) -> Unit,
    onGenerateInvoice: (Reservation) -> Unit,
    selectedInvoice: InvoiceData?,
    onCloseInvoice: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddPaymentDialog by remember { mutableStateOf(false) }
    var paymentToDelete by remember { mutableStateOf<Payment?>(null) }

    val clientMap = clients.associateBy { it.id }
    val resMap = reservations.associateBy { it.id }

    Scaffold(
        floatingActionButton = {
            if (selectedInvoice == null) {
                FloatingActionButton(
                    onClick = { showAddPaymentDialog = true },
                    containerColor = EmeraldSuccess,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_add_payment")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Encaisser un paiement")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("payments_screen")
        ) {
            // Tabs: 0 -> Paiements & Versements, 1 -> Factures Simples
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Paiements & Versements", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.MonetizationOn, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Factures & Devis", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            if (selectedTab == 0) {
                // TAB 0: PAYMENTS
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Summary Card
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Bilan Financier Global",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Total Facturé", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = EventManagerViewModel.formatCurrency(kpis.totalRevenue),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Column {
                                        Text("Total Encaissé", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = EventManagerViewModel.formatCurrency(kpis.totalCollected),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldSuccess
                                        )
                                    }
                                    Column {
                                        Text("Reste à Percevoir", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = EventManagerViewModel.formatCurrency(kpis.totalRemaining),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (kpis.totalRemaining > 0) RoseError else EmeraldSuccess
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (payments.isEmpty()) {
                        item {
                            EmptyPlaceholder(
                                icon = Icons.Default.MonetizationOn,
                                title = "Aucun versement enregistré",
                                description = "Enregistrez les acomptes ou règlements complets de vos clients."
                            )
                        }
                    } else {
                        items(payments, key = { it.id }) { payment ->
                            val reservation = resMap[payment.reservationId]
                            val client = reservation?.let { clientMap[it.clientId] }

                            PaymentItemCard(
                                payment = payment,
                                client = client,
                                reservation = reservation,
                                onDelete = { paymentToDelete = payment }
                            )
                        }
                    }
                }
            } else {
                // TAB 1: INVOICES LIST (From reservations)
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (reservations.isEmpty()) {
                        item {
                            EmptyPlaceholder(
                                icon = Icons.Default.Receipt,
                                title = "Aucune réservation à facturer",
                                description = "Les factures sont générées automatiquement à partir des réservations d'événements."
                            )
                        }
                    } else {
                        items(reservations, key = { it.id }) { res ->
                            val client = clientMap[res.clientId]
                            val event = res.eventId?.let { events.find { e -> e.id == it } }
                            val resPayments = payments.filter { it.reservationId == res.id }
                            val paid = resPayments.sumOf { it.amount }
                            val remaining = (res.totalAmount - paid).coerceAtLeast(0.0)

                            ElevatedCard(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("invoice_card_${res.id}")
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Facture N° FACT-${String.format("%04d", res.id)}",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = client?.name ?: "Client #${res.clientId}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        Button(
                                            onClick = { onGenerateInvoice(res) },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.testTag("btn_view_invoice_${res.id}")
                                        ) {
                                            Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Voir facture", fontSize = 12.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider()
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Événement : ${event?.title ?: res.location}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Solde : ${EventManagerViewModel.formatCurrency(remaining)}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (remaining > 0) RoseError else EmeraldSuccess
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Full Invoice Viewer
    if (selectedInvoice != null) {
        InvoiceViewerDialog(
            invoice = selectedInvoice,
            onClose = onCloseInvoice
        )
    }

    // Add Payment Dialog
    if (showAddPaymentDialog) {
        PaymentFormDialog(
            reservations = reservations,
            clients = clients,
            onDismiss = { showAddPaymentDialog = false },
            onConfirm = { payment ->
                onAddPayment(payment)
                showAddPaymentDialog = false
            }
        )
    }

    // Delete Payment Confirmation
    if (paymentToDelete != null) {
        AlertDialog(
            onDismissRequest = { paymentToDelete = null },
            title = { Text("Supprimer ce paiement ?") },
            text = { Text("Voulez-vous supprimer ce versement de ${EventManagerViewModel.formatCurrency(paymentToDelete?.amount ?: 0.0)} ?") },
            confirmButton = {
                Button(
                    onClick = {
                        paymentToDelete?.let { onDeletePayment(it) }
                        paymentToDelete = null
                    }
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { paymentToDelete = null }) { Text("Annuler") }
            }
        )
    }
}

@Composable
fun PaymentItemCard(
    payment: Payment,
    client: Client?,
    reservation: Reservation?,
    onDelete: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("payment_item_${payment.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDCFCE7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (payment.paymentMethod) {
                        "Carte bancaire" -> Icons.Default.CreditCard
                        "Mobile Money" -> Icons.Default.PhoneAndroid
                        else -> Icons.Default.MonetizationOn
                    },
                    contentDescription = null,
                    tint = EmeraldSuccess,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = EventManagerViewModel.formatCurrency(payment.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldSuccess
                )
                Text(
                    text = "${payment.paymentMethod} • ${client?.name ?: "Client"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Date : ${payment.paymentDate} • Réf : ${payment.reference.ifEmpty { "N/A" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun PaymentFormDialog(
    reservations: List<Reservation>,
    clients: List<Client>,
    onDismiss: () -> Unit,
    onConfirm: (Payment) -> Unit
) {
    if (reservations.isEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Aucune réservation") },
            text = { Text("Vous devez d'abord créer une réservation pour pouvoir enregistrer un paiement.") },
            confirmButton = { Button(onClick = onDismiss) { Text("D'accord") } }
        )
        return
    }

    val clientMap = clients.associateBy { it.id }
    var selectedResId by remember { mutableStateOf(reservations.first().id) }
    var amountStr by remember { mutableStateOf("500.0") }
    var paymentDate by remember { mutableStateOf("2026-10-06") }
    var paymentMethod by remember { mutableStateOf("Espèces") }
    var reference by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val methods = listOf("Espèces", "Carte bancaire", "Virement", "Mobile Money", "Chèque")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Encaisser un paiement") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    Text("Sélectionner la réservation *", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(reservations) { res ->
                            val clientName = clientMap[res.clientId]?.name ?: "Client"
                            FilterChip(
                                selected = selectedResId == res.id,
                                onClick = { selectedResId = res.id },
                                label = { Text("#${res.id} - $clientName (${EventManagerViewModel.formatCurrency(res.totalAmount)})", fontSize = 11.sp) }
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("Montant versé (€) *") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_payment_amount")
                    )
                }

                item {
                    Text("Mode de règlement *", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(methods) { m ->
                            FilterChip(
                                selected = paymentMethod == m,
                                onClick = { paymentMethod = m },
                                label = { Text(m, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = paymentDate,
                        onValueChange = { paymentDate = it },
                        label = { Text("Date de paiement (AAAA-MM-JJ)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = reference,
                        onValueChange = { reference = it },
                        label = { Text("Référence (ex: VIR-2026-01, Reçu CB...)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Commentaires") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        val payment = Payment(
                            reservationId = selectedResId,
                            amount = amount,
                            paymentDate = paymentDate.trim(),
                            paymentMethod = paymentMethod,
                            reference = reference.trim(),
                            notes = notes.trim(),
                            createdAt = System.currentTimeMillis()
                        )
                        onConfirm(payment)
                    }
                },
                modifier = Modifier.testTag("btn_confirm_payment")
            ) {
                Text("Valider le paiement")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@Composable
fun InvoiceViewerDialog(
    invoice: InvoiceData,
    onClose: () -> Unit
) {
    val context = LocalContext.current

    val invoiceText = buildString {
        appendLine("==================================================")
        appendLine("         FACTURE ÉVÉNEMENTIELLE - EVENTMANAGER    ")
        appendLine("==================================================")
        appendLine("N° de Facture : ${invoice.invoiceNumber}")
        appendLine("Date d'émission : ${invoice.issueDate}")
        appendLine("--------------------------------------------------")
        appendLine("CLIENT :")
        appendLine("Nom : ${invoice.client?.name ?: "Client"}")
        appendLine("Téléphone : ${invoice.client?.phone ?: "Non renseigné"}")
        appendLine("Email : ${invoice.client?.email ?: "Non renseigné"}")
        if (!invoice.client?.company.isNullOrEmpty()) {
            appendLine("Entreprise : ${invoice.client?.company}")
        }
        appendLine("--------------------------------------------------")
        appendLine("DÉTAIL DE LA PRESTATION :")
        if (invoice.event != null) {
            appendLine("Événement : ${invoice.event.title} (${invoice.event.type})")
            appendLine("Date prévue : ${invoice.event.date} à ${invoice.event.time}")
            appendLine("Lieu : ${invoice.event.location}")
            appendLine("Nombre d'invités : ${invoice.event.guestCount}")
        } else {
            appendLine("Prestation : Réservation N°${invoice.reservation.id}")
            appendLine("Lieu : ${invoice.reservation.location}")
            appendLine("Date : ${invoice.reservation.eventDate}")
        }
        appendLine("--------------------------------------------------")
        appendLine("RÉCAPITULATIF FINANCIER :")
        appendLine("Total prestation TTC : ${EventManagerViewModel.formatCurrency(invoice.subtotal)}")
        appendLine("Total acomptes réglés : ${EventManagerViewModel.formatCurrency(invoice.totalPaid)}")
        appendLine("--------------------------------------------------")
        appendLine("NET À PAYER (SOLDE RESTANT) : ${EventManagerViewModel.formatCurrency(invoice.remainingDue)}")
        appendLine("Statut : ${if (invoice.remainingDue == 0.0) "RÉGLÉE INTÉGRALEMENT" else "ACOMPTE VERSÉ / EN ATTENTE DU SOLDE"}")
        appendLine("==================================================")
        appendLine("Merci de votre confiance ! - EventManager")
    }

    AlertDialog(
        onDismissRequest = onClose,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Facture ${invoice.invoiceNumber}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Fermer")
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("invoice_viewer_content"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("EventManager Agency", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = IndigoPrimary)
                                    Text("Organisation & Logistique", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(invoice.invoiceNumber, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Date: ${invoice.issueDate}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(10.dp))

                            Text("Facturé à :", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text(invoice.client?.name ?: "Client", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${invoice.client?.phone ?: ""} • ${invoice.client?.email ?: ""}", fontSize = 12.sp)

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(10.dp))

                            Text("Prestation :", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text(invoice.event?.title ?: "Réservation d'équipements", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Lieu : ${invoice.reservation.location}", fontSize = 12.sp)
                            Text("Date : ${invoice.reservation.eventDate}", fontSize = 12.sp)

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total TTC :", fontWeight = FontWeight.Medium)
                                Text(EventManagerViewModel.formatCurrency(invoice.subtotal), fontWeight = FontWeight.Bold)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Déjà payé :", color = EmeraldSuccess, fontWeight = FontWeight.Medium)
                                Text(EventManagerViewModel.formatCurrency(invoice.totalPaid), color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (invoice.remainingDue > 0) Color(0xFFFEF2F2) else Color(0xFFECFDF5),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Reste à payer :",
                                        fontWeight = FontWeight.Bold,
                                        color = if (invoice.remainingDue > 0) RoseError else EmeraldSuccess,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = EventManagerViewModel.formatCurrency(invoice.remainingDue),
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (invoice.remainingDue > 0) RoseError else EmeraldSuccess,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Facture EventManager", invoiceText))
                        Toast.makeText(context, "Facture copiée dans le presse-papiers !", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copier", fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Facture ${invoice.invoiceNumber} - EventManager")
                            putExtra(Intent.EXTRA_TEXT, invoiceText)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Partager la facture via"))
                    }
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Partager", fontSize = 12.sp)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onClose) { Text("Fermer") }
        }
    )
}
