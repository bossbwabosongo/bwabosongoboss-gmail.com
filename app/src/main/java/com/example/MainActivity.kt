package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Event
import com.example.data.model.Reservation
import com.example.ui.EventManagerViewModel
import com.example.ui.InvoiceData
import com.example.ui.screens.ClientsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EventFormDialog
import com.example.ui.screens.EventsScreen
import com.example.ui.screens.MaterialScreen
import com.example.ui.screens.MySqlExportDialog
import com.example.ui.screens.PaymentFormDialog
import com.example.ui.screens.PaymentsInvoicesScreen
import com.example.ui.screens.ReservationFormDialog
import com.example.ui.screens.ReservationsScreen
import com.example.ui.theme.EventManagerTheme
import com.example.ui.theme.IndigoPrimary

enum class AppDestination(val route: String, val title: String, val icon: ImageVector) {
    DASHBOARD("dashboard", "Accueil", Icons.Default.Dashboard),
    EVENTS("events", "Événements", Icons.Default.Celebration),
    RESERVATIONS("reservations", "Réservations", Icons.Default.CalendarMonth),
    CLIENTS("clients", "Clients", Icons.Default.People),
    MATERIAL("material", "Matériel", Icons.Default.Chair),
    FINANCE("finance", "Factures", Icons.Default.Receipt)
}

class MainActivity : ComponentActivity() {

    private val viewModel: EventManagerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EventManagerTheme {
                EventManagerApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventManagerApp(viewModel: EventManagerViewModel) {
    var currentDestination by remember { mutableStateOf(AppDestination.DASHBOARD) }
    var showMySqlDialog by remember { mutableStateOf(false) }
    var selectedInvoice by remember { mutableStateOf<InvoiceData?>(null) }

    // Quick Add dialog states
    var quickAddEventDialog by remember { mutableStateOf(false) }
    var quickAddReservationDialog by remember { mutableStateOf(false) }
    var quickAddPaymentDialog by remember { mutableStateOf(false) }
    var preselectedEventForReservation by remember { mutableStateOf<Event?>(null) }

    val clients by viewModel.clients.collectAsStateWithLifecycle()
    val events by viewModel.events.collectAsStateWithLifecycle()
    val upcomingEvents by viewModel.upcomingEvents.collectAsStateWithLifecycle()
    val materials by viewModel.materials.collectAsStateWithLifecycle()
    val reservations by viewModel.reservations.collectAsStateWithLifecycle()
    val payments by viewModel.payments.collectAsStateWithLifecycle()
    val kpis by viewModel.financialKpi.collectAsStateWithLifecycle()
    val mySqlDump by viewModel.mySqlDump.collectAsStateWithLifecycle()
    val isGeneratingSql by viewModel.isGeneratingSql.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // User feedback notification
    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearUserMessage()
        }
    }

    // Android back navigation handler
    if (currentDestination != AppDestination.DASHBOARD) {
        BackHandler {
            currentDestination = AppDestination.DASHBOARD
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentDestination) {
                            AppDestination.DASHBOARD -> "EventManager"
                            AppDestination.EVENTS -> "Gestion des Événements"
                            AppDestination.RESERVATIONS -> "Réservations & Dates"
                            AppDestination.CLIENTS -> "Répertoire Clients"
                            AppDestination.MATERIAL -> "Inventaire du Matériel"
                            AppDestination.FINANCE -> "Paiements & Facturation"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.generateMySqlDump()
                            showMySqlDialog = true
                        },
                        modifier = Modifier.testTag("action_mysql_backup")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = "Sauvegarde MySQL",
                            tint = IndigoPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("main_bottom_nav")
            ) {
                AppDestination.values().forEach { destination ->
                    NavigationBarItem(
                        selected = currentDestination == destination,
                        onClick = { currentDestination = destination },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.title,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = destination.title,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        },
                        modifier = Modifier.testTag("nav_item_${destination.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentDestination) {
                AppDestination.DASHBOARD -> {
                    DashboardScreen(
                        kpis = kpis,
                        upcomingEvents = upcomingEvents,
                        recentReservations = reservations,
                        clients = clients,
                        onNavigateToEvents = { currentDestination = AppDestination.EVENTS },
                        onNavigateToReservations = { currentDestination = AppDestination.RESERVATIONS },
                        onNavigateToClients = { currentDestination = AppDestination.CLIENTS },
                        onNavigateToPayments = { currentDestination = AppDestination.FINANCE },
                        onOpenMySqlExport = {
                            viewModel.generateMySqlDump()
                            showMySqlDialog = true
                        },
                        onQuickAddEvent = { quickAddEventDialog = true },
                        onQuickAddClient = { currentDestination = AppDestination.CLIENTS },
                        onQuickAddReservation = { quickAddReservationDialog = true },
                        onQuickAddPayment = { quickAddPaymentDialog = true }
                    )
                }

                AppDestination.EVENTS -> {
                    EventsScreen(
                        events = events,
                        onSaveEvent = { viewModel.saveEvent(it) },
                        onDeleteEvent = { viewModel.deleteEvent(it) },
                        onCreateReservationForEvent = { event ->
                            preselectedEventForReservation = event
                            quickAddReservationDialog = true
                        }
                    )
                }

                AppDestination.RESERVATIONS -> {
                    ReservationsScreen(
                        reservations = reservations,
                        clients = clients,
                        events = events,
                        payments = payments,
                        onSaveReservation = { viewModel.saveReservation(it) },
                        onDeleteReservation = { viewModel.deleteReservation(it) },
                        onGenerateInvoice = { res ->
                            viewModel.getInvoiceForReservation(res) { inv ->
                                selectedInvoice = inv
                            }
                        },
                        onAddPaymentForReservation = {
                            quickAddPaymentDialog = true
                        }
                    )
                }

                AppDestination.CLIENTS -> {
                    ClientsScreen(
                        clients = clients,
                        onSaveClient = { viewModel.saveClient(it) },
                        onDeleteClient = { viewModel.deleteClient(it) }
                    )
                }

                AppDestination.MATERIAL -> {
                    MaterialScreen(
                        materials = materials,
                        onSaveMaterial = { viewModel.saveMaterial(it) },
                        onDeleteMaterial = { viewModel.deleteMaterial(it) }
                    )
                }

                AppDestination.FINANCE -> {
                    PaymentsInvoicesScreen(
                        kpis = kpis,
                        payments = payments,
                        reservations = reservations,
                        clients = clients,
                        events = events,
                        onAddPayment = { viewModel.addPayment(it) },
                        onDeletePayment = { viewModel.deletePayment(it) },
                        onGenerateInvoice = { res ->
                            viewModel.getInvoiceForReservation(res) { inv ->
                                selectedInvoice = inv
                            }
                        },
                        selectedInvoice = selectedInvoice,
                        onCloseInvoice = { selectedInvoice = null }
                    )
                }
            }
        }
    }

    // MySQL Backup Dialog
    if (showMySqlDialog) {
        MySqlExportDialog(
            sqlDump = mySqlDump,
            isGenerating = isGeneratingSql,
            onGenerate = { viewModel.generateMySqlDump() },
            onClose = { showMySqlDialog = false },
            clientCount = clients.size,
            eventCount = events.size,
            materialCount = materials.size,
            reservationCount = reservations.size,
            paymentCount = payments.size
        )
    }

    // Quick Add Event Dialog
    if (quickAddEventDialog) {
        EventFormDialog(
            initialEvent = null,
            onDismiss = { quickAddEventDialog = false },
            onConfirm = { event ->
                viewModel.saveEvent(event)
                quickAddEventDialog = false
            }
        )
    }

    // Quick Add Reservation Dialog
    if (quickAddReservationDialog) {
        val initialRes = preselectedEventForReservation?.let { evt ->
            Reservation(
                clientId = clients.firstOrNull()?.id ?: 0L,
                eventId = evt.id,
                reservationDate = "2026-10-06",
                eventDate = evt.date,
                location = evt.location,
                status = "Confirmée",
                totalAmount = evt.estimatedBudget
            )
        }
        ReservationFormDialog(
            initialReservation = initialRes,
            clients = clients,
            events = events,
            onDismiss = {
                quickAddReservationDialog = false
                preselectedEventForReservation = null
            },
            onConfirm = { res ->
                viewModel.saveReservation(res)
                quickAddReservationDialog = false
                preselectedEventForReservation = null
            }
        )
    }

    // Quick Add Payment Dialog
    if (quickAddPaymentDialog) {
        PaymentFormDialog(
            reservations = reservations,
            clients = clients,
            onDismiss = { quickAddPaymentDialog = false },
            onConfirm = { pay ->
                viewModel.addPayment(pay)
                quickAddPaymentDialog = false
            }
        )
    }
}
