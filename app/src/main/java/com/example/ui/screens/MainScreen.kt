package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.R
import com.example.data.models.RoomEntity
import com.example.viewmodel.RoomViewModel
import java.util.Locale

data class AggregatedWorkItem(
    val itemName: String,
    val qty: Double,
    val unit: String,
    val cost: Double
)

@Composable
fun MainScreen(
    viewModel: RoomViewModel,
    modifier: Modifier = Modifier,
    onAddRoomClick: () -> Unit
) {
    val rooms by viewModel.rooms.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    // Filter rooms by search query
    val filteredRooms = remember(rooms, searchQuery) {
        if (searchQuery.isBlank()) {
            rooms
        } else {
            rooms.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    // Engineering sums
    val totalRooms = filteredRooms.size
    val totalFloorArea = com.example.domain.engine.AggregationEngine.calculateTotalFloorArea(filteredRooms)
    val totalCost = com.example.domain.engine.AggregationEngine.calculateGrandTotal(filteredRooms)

    // Aggregated work items for global BOQ table (جدول الكميات والتكلفة الإجمالي)
    val aggregatedWorkItems = remember(filteredRooms) {
        val map = mutableMapOf<String, Triple<Double, String, Double>>()
        filteredRooms.forEach { room ->
            room.workItems.forEach { work ->
                val qty = work.getQuantity(room)
                val cost = work.getCost(room)
                val current = map[work.name]
                if (current != null) {
                    map[work.name] = Triple(current.first + qty, work.unitType, current.third + cost)
                } else {
                    map[work.name] = Triple(qty, work.unitType, cost)
                }
            }
        }
        map.toList().map { (name, triple) ->
            AggregatedWorkItem(
                itemName = name,
                qty = triple.first,
                unit = triple.second,
                cost = triple.third
            )
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddRoomClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .testTag("add_room_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.room_detail_title_add),
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            
            // 1. Sleek Modern Facebook-style Top Title
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp, horizontal = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(MaterialTheme.colorScheme.secondary, CircleShape)
                                        .clickable { viewModel.navigateTo(RoomViewModel.Screen.SETTINGS) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "⚙️", fontSize = 16.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(MaterialTheme.colorScheme.secondary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "📏", fontSize = 16.sp)
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = stringResource(R.string.app_title),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.primary // Facebook Blue for the title!
                                )
                                Text(
                                    text = stringResource(R.string.app_subtitle),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    // Thin subtle divider beneath the navbar just like Facebook Web/Mobile
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        thickness = 1.dp
                    )
                }
            }

            // 2. Overall Totals Dashboard Box
            item {
                val advancePayment by viewModel.advancePayment.collectAsState()
                var advancePaymentInput by remember(advancePayment) {
                    mutableStateOf(if (advancePayment > 0.0) String.format(Locale.ENGLISH, "%.0f", advancePayment) else "")
                }
                val netRemainingTotal = (totalCost - advancePayment).coerceAtLeast(0.0)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .testTag("dashboard_summary_card"),
                    shape = RoundedCornerShape(8.dp), // Facebook crisp rounded corners
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), // Elegant white Facebook card background
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = stringResource(R.string.total_cost_label),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Grand Price Display centered perfectly and responsive for all screen sizes
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${String.format(Locale.ENGLISH, "%,.0f", totalCost)} $currencySymbol",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary, // Styled nicely in brand blue
                                textAlign = TextAlign.Center
                            )
                        }

                        // Display Net remaining if there's any advance payment
                        if (advancePayment > 0.0) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${String.format(Locale.ENGLISH, "%,.0f", netRemainingTotal)} $currencySymbol",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32), // Clear green
                                    textAlign = TextAlign.Left
                                )
                                Text(
                                    text = "صافي المبلغ المتبقي نقداً:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32),
                                    textAlign = TextAlign.Right
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Advance payment Input Box with a clean money emoji/label
                        OutlinedTextField(
                            value = advancePaymentInput,
                            onValueChange = { newValue ->
                                val normalized = newValue.replace("٫", ".").trim()
                                advancePaymentInput = normalized
                                val parsed = normalized.toDoubleOrNull() ?: 0.0
                                viewModel.saveAdvancePayment(parsed)
                            },
                            label = { Text("الدفعة المقدمة المدفوعة سلفاً (سُلفة بالعملة) 💵", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            placeholder = { Text("أدخل مبلغ السلفة لخصمه من المجموع", fontSize = 11.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color(0xFFF2F3F5) // Soft Facebook-grey field bg when inactive
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 3. Lower part: CTA Button to show detailed BOQ table taking full width
                        Button(
                            onClick = { viewModel.navigateTo(RoomViewModel.Screen.SUMMARY) },
                            enabled = rooms.isNotEmpty(),
                            shape = RoundedCornerShape(8.dp), // Crisp corner radius
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Text(
                                text = "📜   " + stringResource(R.string.detailed_bill_btn).replace(" 📜", ""),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }


            // 4. Rooms List Area
            if (filteredRooms.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "📐",
                                fontSize = 48.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            Text(
                                text = if (searchQuery.isNotEmpty()) stringResource(R.string.no_results_search) else stringResource(R.string.no_rooms_yet),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (searchQuery.isNotEmpty()) stringResource(R.string.search_retry_hint) else stringResource(R.string.add_first_room_hint),
                                fontSize = 11.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                }
            } else {
                items(filteredRooms) { room ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        RoomCard(
                            room = room,
                            currencySymbol = currencySymbol,
                            onClick = { viewModel.selectRoom(room) },
                            onDelete = { viewModel.deleteRoom(room.id) }
                        )
                    }
                }

                // Bottom centered aggregated quantities & cost table (وجدول الكميات والتكلفة الاجمالي اجعله في الاسفل في الوسط)
                if (aggregatedWorkItems.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 12.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "📊  جدول الكميات والتكلفة الإجمالي للمشروع  📊",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                                )

                                // Table Header Row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                        .padding(vertical = 8.dp, horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "التكلفة الإجمالية",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.weight(1.3f),
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "الأمتار",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "الوحدة",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.weight(0.8f),
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "البند الحسابي",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.weight(1.9f),
                                        textAlign = TextAlign.Right
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Table Data Rows
                                aggregatedWorkItems.forEachIndexed { index, agg ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                if (index % 2 == 0) Color(0xFFF9FAFB) else Color.Transparent,
                                                RoundedCornerShape(6.dp)
                                            )
                                            .padding(vertical = 8.dp, horizontal = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${String.format(Locale.ENGLISH, "%,.0f", agg.cost)} $currencySymbol",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color.Black,
                                            modifier = Modifier.weight(1.3f),
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = String.format(Locale.ENGLISH, "%.1f", agg.qty),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color.DarkGray,
                                            modifier = Modifier.weight(1f),
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = agg.unit,
                                            fontSize = 11.sp,
                                            color = Color.Gray,
                                            modifier = Modifier.weight(0.8f),
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = agg.itemName,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.Black,
                                            modifier = Modifier.weight(1.9f),
                                            textAlign = TextAlign.Right
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE5E7EB)))
                                Spacer(modifier = Modifier.height(10.dp))

                                // Total Cost centered at the bottom of the table
                                Text(
                                    text = "المجموع الإجمالي للمشروع:  ${String.format(Locale.ENGLISH, "%,.0f", totalCost)} $currencySymbol",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RoomCard(
    room: RoomEntity,
    currencySymbol: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val accentColor = remember(room.name) {
        val n = room.name
        when {
            n.contains("نوم") || n.contains("Bed") -> Color(0xFF9C27B0) // Purple
            n.contains("صالون") || n.contains("جلوس") || n.contains("Living") || n.contains("استقبال") -> Color(0xFF009688) // Teal
            n.contains("مطبخ") || n.contains("Kitchen") -> Color(0xFFFF9800) // Amber
            n.contains("حمام") || n.contains("Bath") || n.contains("دورة") -> Color(0xFF2196F3) // Blue
            n.contains("ممر") || n.contains("مدخل") || n.contains("Hall") -> Color(0xFF607D8B) // Slate Gray
            else -> Color(0xFF71639E) // Odoo Classic Purple-Indigo
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .testTag("room_card_${room.id}"),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(64.dp)
                    .align(Alignment.CenterEnd)
                    .background(accentColor, RoundedCornerShape(topStart = 2.dp, bottomStart = 2.dp, topEnd = 8.dp, bottomEnd = 8.dp))
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp, horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFFFFEBEE), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Room",
                            tint = Color(0xFFC62828),
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "${String.format(Locale.ENGLISH, "%,.0f", room.totalCost)} $currencySymbol",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(accentColor, CircleShape)
                            )
                            Text(
                                text = "${room.workItems.size} ${stringResource(R.string.work_items_title)}",
                                fontSize = 10.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = room.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Right
                            )
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(accentColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = room.icon, fontSize = 16.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 44.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFF3F4F6), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "📏 ${String.format(Locale.ENGLISH, "%.1f", room.floorArea)} م²",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF374151)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(Color(0xFFE8F5E9), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "📐 ${String.format(Locale.ENGLISH, "%.1f", room.height)} م ارتفاع",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }

                    if (room.openings.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFFF3E0), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "🚪 ${room.openings.size} فتحات",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100)
                            )
                        }
                    }
                }

                // Line-by-line detailed calculations display (سطراً تحت سطر بكلمة قصيرة وتنضيم جيد)
                if (room.workItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 44.dp)
                            .height(0.5.dp)
                            .background(Color.LightGray.copy(alpha = 0.5f))
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 44.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        room.workItems.forEach { work ->
                            val qty = work.getQuantity(room)
                            val cost = work.getCost(room)
                            val categoryIcon = when {
                                work.name.contains("دهان") || work.name.contains("طلاء") || work.name.contains("صبغ") -> "🎨"
                                work.name.contains("بلاط") || work.name.contains("سيراميك") || work.name.contains("أرض") -> "🧱"
                                else -> "⚙️"
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${String.format(Locale.ENGLISH, "%,.0f", cost)} دج",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "(${String.format(Locale.ENGLISH, "%.1f", qty)} ${work.unitType})",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "• ${work.name} $categoryIcon",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.DarkGray,
                                        textAlign = TextAlign.Right
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
