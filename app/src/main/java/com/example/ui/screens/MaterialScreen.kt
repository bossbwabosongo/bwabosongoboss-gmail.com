package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NightShelter
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Material
import com.example.ui.EventManagerViewModel
import com.example.ui.components.EmptyPlaceholder
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseError

@Composable
fun MaterialScreen(
    materials: List<Material>,
    onSaveMaterial: (Material) -> Unit,
    onDeleteMaterial: (Material) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("Tous") }
    var materialToEdit by remember { mutableStateOf<Material?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var materialToDelete by remember { mutableStateOf<Material?>(null) }

    val categories = listOf("Tous", "Chaises", "Tables", "Tentes", "Podiums", "Sonorisation", "Éclairage", "Autre")

    val filteredMaterials = if (selectedCategory == "Tous") {
        materials
    } else {
        materials.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    materialToEdit = null
                    showDialog = true
                },
                containerColor = AmberSecondary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_material")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter du matériel")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("material_screen")
        ) {
            // Category Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 12.sp) }
                    )
                }
            }

            if (filteredMaterials.isEmpty()) {
                EmptyPlaceholder(
                    icon = Icons.Default.Inventory,
                    title = "Aucun matériel",
                    description = if (selectedCategory == "Tous")
                        "Ajoutez vos équipements (chaises, tables, tentes, podiums...) pour gérer les stocks."
                    else "Aucun matériel dans la catégorie '$selectedCategory'."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredMaterials, key = { it.id }) { item ->
                        MaterialCard(
                            material = item,
                            onEdit = {
                                materialToEdit = item
                                showDialog = true
                            },
                            onDelete = { materialToDelete = item }
                        )
                    }
                }
            }
        }
    }

    // Material Form Dialog
    if (showDialog) {
        MaterialFormDialog(
            initialMaterial = materialToEdit,
            onDismiss = { showDialog = false },
            onConfirm = { saved ->
                onSaveMaterial(saved)
                showDialog = false
            }
        )
    }

    // Delete Confirmation
    if (materialToDelete != null) {
        AlertDialog(
            onDismissRequest = { materialToDelete = null },
            title = { Text("Supprimer ce matériel ?") },
            text = { Text("Voulez-vous supprimer '${materialToDelete?.name}' de l'inventaire ?") },
            confirmButton = {
                Button(
                    onClick = {
                        materialToDelete?.let { onDeleteMaterial(it) }
                        materialToDelete = null
                    }
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { materialToDelete = null }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
fun MaterialCard(
    material: Material,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryIcon = getCategoryIcon(material.category)
    val ratio = if (material.totalStock > 0) {
        (material.availableStock.toFloat() / material.totalStock.toFloat()).coerceIn(0f, 1f)
    } else 0f
    val stockColor = when {
        ratio > 0.5f -> EmeraldSuccess
        ratio > 0.2f -> AmberSecondary
        else -> RoseError
    }

    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("material_card_${material.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = material.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${material.category} • ${EventManagerViewModel.formatCurrency(material.unitRentalPrice)} / unité",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Modifier", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stock progress indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Disponibilité : ${material.availableStock} / ${material.totalStock}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = stockColor
                )
                Text(
                    text = "${(ratio * 100).toInt()}% en stock",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { ratio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = stockColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            if (material.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = material.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

fun getCategoryIcon(category: String): ImageVector {
    return when (category.lowercase()) {
        "chaises" -> Icons.Default.Chair
        "tables" -> Icons.Default.TableRestaurant
        "tentes" -> Icons.Default.NightShelter
        "podiums" -> Icons.Default.ViewCarousel
        "sonorisation" -> Icons.Default.Mic
        "éclairage" -> Icons.Default.Lightbulb
        else -> Icons.Default.Inventory
    }
}

@Composable
fun MaterialFormDialog(
    initialMaterial: Material?,
    onDismiss: () -> Unit,
    onConfirm: (Material) -> Unit
) {
    var name by remember { mutableStateOf(initialMaterial?.name ?: "") }
    var category by remember { mutableStateOf(initialMaterial?.category ?: "Chaises") }
    var totalStockStr by remember { mutableStateOf((initialMaterial?.totalStock ?: 100).toString()) }
    var availableStockStr by remember { mutableStateOf((initialMaterial?.availableStock ?: 100).toString()) }
    var priceStr by remember { mutableStateOf((initialMaterial?.unitRentalPrice ?: 5.0).toString()) }
    var description by remember { mutableStateOf(initialMaterial?.description ?: "") }

    val categories = listOf("Chaises", "Tables", "Tentes", "Podiums", "Sonorisation", "Éclairage", "Décoration", "Autre")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialMaterial == null) "Ajouter du matériel" else "Modifier le matériel") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nom de l'équipement *") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_material_name")
                    )
                }

                item {
                    Text("Catégorie *", style = MaterialTheme.typography.bodySmall)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = totalStockStr,
                            onValueChange = {
                                totalStockStr = it
                                if (initialMaterial == null) availableStockStr = it
                            },
                            label = { Text("Stock total") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = availableStockStr,
                            onValueChange = { availableStockStr = it },
                            label = { Text("Stock dispo") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("Tarif de location unitaire (€)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description / Spécifications") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val total = totalStockStr.toIntOrNull() ?: 10
                        val avail = availableStockStr.toIntOrNull() ?: total
                        val price = priceStr.toDoubleOrNull() ?: 5.0
                        val item = Material(
                            id = initialMaterial?.id ?: 0L,
                            name = name.trim(),
                            category = category,
                            totalStock = total,
                            availableStock = avail.coerceAtMost(total),
                            unitRentalPrice = price,
                            description = description.trim()
                        )
                        onConfirm(item)
                    }
                },
                modifier = Modifier.testTag("btn_save_material")
            ) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}
