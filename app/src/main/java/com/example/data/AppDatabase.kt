package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.ClientDao
import com.example.data.dao.EventDao
import com.example.data.dao.MaterialDao
import com.example.data.dao.PaymentDao
import com.example.data.dao.ReservationDao
import com.example.data.model.Client
import com.example.data.model.Event
import com.example.data.model.Material
import com.example.data.model.Payment
import com.example.data.model.Reservation
import com.example.data.model.ReservationMaterial
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Client::class,
        Event::class,
        Material::class,
        Reservation::class,
        ReservationMaterial::class,
        Payment::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun clientDao(): ClientDao
    abstract fun eventDao(): EventDao
    abstract fun materialDao(): MaterialDao
    abstract fun reservationDao(): ReservationDao
    abstract fun paymentDao(): PaymentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "event_manager_database.db"
                )
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            val clientDao = database.clientDao()
            val eventDao = database.eventDao()
            val materialDao = database.materialDao()
            val reservationDao = database.reservationDao()
            val paymentDao = database.paymentDao()

            // 1. Clients
            val c1Id = clientDao.insertClient(
                Client(
                    name = "Sophie Martin",
                    phone = "+33 6 12 34 56 78",
                    email = "sophie.martin@email.fr",
                    company = "Particulier",
                    address = "14 Rue des Lilas, Paris",
                    notes = "Organisation Mariage prestigieux"
                )
            )
            val c2Id = clientDao.insertClient(
                Client(
                    name = "Marc Laurent",
                    phone = "+33 6 98 76 54 32",
                    email = "m.laurent@techgroup.com",
                    company = "TechGroup Solutions",
                    address = "45 Avenue de la République, Lyon",
                    notes = "Client corporate récurrent"
                )
            )
            val c3Id = clientDao.insertClient(
                Client(
                    name = "Claire & David Moreau",
                    phone = "+33 7 45 21 89 00",
                    email = "claire.moreau@gmail.com",
                    company = "Particulier",
                    address = "8 Boulevard Victor Hugo, Nantes",
                    notes = "Anniversaire surprise 40 ans"
                )
            )
            val c4Id = clientDao.insertClient(
                Client(
                    name = "Amadou Diallo",
                    phone = "+33 6 55 44 33 22",
                    email = "amadou.diallo@africadev.org",
                    company = "Africa Dev Forum",
                    address = "22 Rue de Provence, Marseille",
                    notes = "Gala caritatif annuel"
                )
            )

            // 2. Events
            val e1Id = eventDao.insertEvent(
                Event(
                    title = "Mariage de Sophie & Alexandre",
                    type = "Mariage",
                    date = "2026-10-24",
                    time = "15:00",
                    location = "Château de Montmirail, 77000",
                    guestCount = 150,
                    status = "Planifié",
                    estimatedBudget = 4500.0,
                    notes = "Ambiance bohème chic, besoin de tentes cristal et chaises Napoléon"
                )
            )
            val e2Id = eventDao.insertEvent(
                Event(
                    title = "Conférence Innovation & IA 2026",
                    type = "Conférence",
                    date = "2026-11-05",
                    time = "09:00",
                    location = "Palais des Congrès, Salle B, Lyon",
                    guestCount = 300,
                    status = "Planifié",
                    estimatedBudget = 6800.0,
                    notes = "Configuration podium, micros HF et vidéoprojecteurs haute luminosité"
                )
            )
            val e3Id = eventDao.insertEvent(
                Event(
                    title = "Anniversaire Surprise de David",
                    type = "Anniversaire",
                    date = "2026-10-18",
                    time = "19:30",
                    location = "Domaine du Bel Air, Nantes",
                    guestCount = 80,
                    status = "Planifié",
                    estimatedBudget = 2200.0,
                    notes = "Buffet dînatoire, sonorisation DJ et éclairage d'ambiance"
                )
            )
            val e4Id = eventDao.insertEvent(
                Event(
                    title = "Gala d'Excellence & Solidarité",
                    type = "Gala",
                    date = "2026-12-12",
                    time = "20:00",
                    location = "Hôtel Intercontinental, Marseille",
                    guestCount = 200,
                    status = "Planifié",
                    estimatedBudget = 8500.0,
                    notes = "Soirée prestige avec dîner de bienfaisance"
                )
            )

            // 3. Materials
            val m1 = materialDao.insertMaterial(
                Material(
                    name = "Chaises Napoléon Dorées",
                    category = "Chaises",
                    totalStock = 300,
                    availableStock = 150,
                    unitRentalPrice = 4.5,
                    description = "Chaises de cérémonie avec coussin velours blanc"
                )
            )
            val m2 = materialDao.insertMaterial(
                Material(
                    name = "Chaises Pliantes Confort",
                    category = "Chaises",
                    totalStock = 500,
                    availableStock = 380,
                    unitRentalPrice = 2.0,
                    description = "Chaises polyvalentes noires rembourrées"
                )
            )
            val m3 = materialDao.insertMaterial(
                Material(
                    name = "Tables Rondes 10 Personnes (Ø 180cm)",
                    category = "Tables",
                    totalStock = 40,
                    availableStock = 25,
                    unitRentalPrice = 18.0,
                    description = "Table banquet en bois avec pieds pliants"
                )
            )
            val m4 = materialDao.insertMaterial(
                Material(
                    name = "Tables Rectangulaires Traiteur (200x80cm)",
                    category = "Tables",
                    totalStock = 30,
                    availableStock = 18,
                    unitRentalPrice = 15.0,
                    description = "Idéale pour buffets et bars traiteur"
                )
            )
            val m5 = materialDao.insertMaterial(
                Material(
                    name = "Tente Chapiteau Cristal 50m²",
                    category = "Tentes",
                    totalStock = 6,
                    availableStock = 4,
                    unitRentalPrice = 450.0,
                    description = "Bâche transparente imperméable, structure aluminium renforcée"
                )
            )
            val m6 = materialDao.insertMaterial(
                Material(
                    name = "Podium Scène Modulable (6x4m)",
                    category = "Podiums",
                    totalStock = 4,
                    availableStock = 3,
                    unitRentalPrice = 380.0,
                    description = "Scène hauteur 60cm avec juponnage noir et escalier"
                )
            )
            val m7 = materialDao.insertMaterial(
                Material(
                    name = "Pack Sonorisation Pro 2000W + 2 Micros HF",
                    category = "Sonorisation",
                    totalStock = 5,
                    availableStock = 3,
                    unitRentalPrice = 250.0,
                    description = "Enceintes actives RCF, table de mixage et micros sans fil"
                )
            )
            val m8 = materialDao.insertMaterial(
                Material(
                    name = "Pack Éclairage LED Ambiance & Soirée",
                    category = "Éclairage",
                    totalStock = 10,
                    availableStock = 7,
                    unitRentalPrice = 120.0,
                    description = "8 projecteurs sur batterie avec télécommande DMX"
                )
            )

            // 4. Reservations
            val r1Id = reservationDao.insertReservation(
                Reservation(
                    clientId = c1Id,
                    eventId = e1Id,
                    reservationDate = "2026-10-01",
                    eventDate = "2026-10-24",
                    location = "Château de Montmirail",
                    status = "Acompte versé",
                    totalAmount = 3200.0,
                    notes = "Livraison le matin à 10h, reprise le lendemain 11h"
                )
            )
            val r2Id = reservationDao.insertReservation(
                Reservation(
                    clientId = c2Id,
                    eventId = e2Id,
                    reservationDate = "2026-09-25",
                    eventDate = "2026-11-05",
                    location = "Palais des Congrès, Lyon",
                    status = "Confirmée",
                    totalAmount = 4800.0,
                    notes = "Installation technique la veille requise"
                )
            )
            val r3Id = reservationDao.insertReservation(
                Reservation(
                    clientId = c3Id,
                    eventId = e3Id,
                    reservationDate = "2026-10-02",
                    eventDate = "2026-10-18",
                    location = "Domaine du Bel Air, Nantes",
                    status = "Acompte versé",
                    totalAmount = 1450.0,
                    notes = "Accès salle à partir de 15h"
                )
            )

            // Reservation Materials
            reservationDao.insertReservationMaterial(
                ReservationMaterial(reservationId = r1Id, materialId = m1, quantity = 150, unitPrice = 4.5)
            )
            reservationDao.insertReservationMaterial(
                ReservationMaterial(reservationId = r1Id, materialId = m3, quantity = 15, unitPrice = 18.0)
            )
            reservationDao.insertReservationMaterial(
                ReservationMaterial(reservationId = r1Id, materialId = m5, quantity = 1, unitPrice = 450.0)
            )
            reservationDao.insertReservationMaterial(
                ReservationMaterial(reservationId = r1Id, materialId = m8, quantity = 1, unitPrice = 120.0)
            )

            reservationDao.insertReservationMaterial(
                ReservationMaterial(reservationId = r2Id, materialId = m2, quantity = 300, unitPrice = 2.0)
            )
            reservationDao.insertReservationMaterial(
                ReservationMaterial(reservationId = r2Id, materialId = m6, quantity = 1, unitPrice = 380.0)
            )
            reservationDao.insertReservationMaterial(
                ReservationMaterial(reservationId = r2Id, materialId = m7, quantity = 1, unitPrice = 250.0)
            )

            // 5. Payments
            // Pour r1 (Total 3200€) : Acompte de 1500€ versé -> Reste 1700€
            paymentDao.insertPayment(
                Payment(
                    reservationId = r1Id,
                    amount = 1500.0,
                    paymentDate = "2026-10-02",
                    paymentMethod = "Virement",
                    reference = "VIR-MAR-2026-01",
                    notes = "Acompte de 45% à la commande"
                )
            )
            // Pour r2 (Total 4800€) : Acompte de 2000€ versé -> Reste 2800€
            paymentDao.insertPayment(
                Payment(
                    reservationId = r2Id,
                    amount = 2000.0,
                    paymentDate = "2026-09-28",
                    paymentMethod = "Virement",
                    reference = "VIR-TECH-9912",
                    notes = "Bon de commande corporate validé"
                )
            )
            // Pour r3 (Total 1450€) : Payé 1450€ intégralement !
            paymentDao.insertPayment(
                Payment(
                    reservationId = r3Id,
                    amount = 800.0,
                    paymentDate = "2026-10-02",
                    paymentMethod = "Carte bancaire",
                    reference = "CB-MOREAU-01",
                    notes = "Premier versement réservation"
                )
            )
            paymentDao.insertPayment(
                Payment(
                    reservationId = r3Id,
                    amount = 650.0,
                    paymentDate = "2026-10-04",
                    paymentMethod = "Espèces",
                    reference = "ESP-SOLDE-40A",
                    notes = "Solde réglé en espèces au bureau"
                )
            )
        }
    }
}
