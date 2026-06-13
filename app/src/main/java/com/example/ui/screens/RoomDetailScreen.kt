package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.core.spring
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.models.Opening
import com.example.data.models.RoomEntity
import com.example.data.models.WorkItem
import com.example.viewmodel.RoomViewModel
import java.util.Locale

@Composable
fun RoomDetailScreen(
    viewModel: RoomViewModel,
    modifier: Modifier = Modifier
) {
    val selectedRoomId by viewModel.selectedRoomId.collectAsState()
    
    val name by viewModel.name.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val length by viewModel.length.collectAsState()
    val width by viewModel.width.collectAsState()
    val height by viewModel.height.collectAsState()
    val iconEmoji by viewModel.icon.collectAsState()
    val openings by viewModel.openings.collectAsState()
    val workItems by viewModel.workItems.collectAsState()
    val remarks by viewModel.remarks.collectAsState()
    val templates by viewModel.templates.collectAsState()

    var templateCategoryFilter by remember { mutableStateOf("الكل") }
    var templateSearchQuery by remember { mutableStateOf("") }
    var templateSortOrder by remember { mutableStateOf("الافتراضي") }

    val filteredTemplates = remember(templates, templateCategoryFilter, templateSearchQuery, templateSortOrder) {
        var result = templates.filter {
            val matchesSearch = if (templateSearchQuery.isBlank()) true else {
                it.name.contains(templateSearchQuery, ignoreCase = true)
            }
            val matchesCategory = when (templateCategoryFilter) {
                "الكل" -> true
                "دهانات 🎨" -> {
                    val n = it.name
                    n.contains("دهان") || n.contains("طلاء") || n.contains("صبغ") || n.contains("لياسة") || n.contains("محارة") || n.contains("معجون")
                }
                "بلاط 🧱" -> {
                    val n = it.name
                    n.contains("بلاط") || n.contains("سيراميك") || n.contains("أرض") || n.contains("رخام") || n.contains("نعل") || n.contains("نعلات")
                }
                "أسقف ☁️" -> {
                    val n = it.name
                    n.contains("جبس") || n.contains("أسقف") || n.contains("سقف") || n.contains("بورد")
                }
                "فتحات 🚪" -> {
                    val n = it.name
                    n.contains("باب") || n.contains("نافذة") || n.contains("نافدة") || n.contains("شباك") || n.contains("أبواب") || n.contains("ألمنيوم")
                }
                else -> true
            }
            matchesSearch && matchesCategory
        }

        result = when (templateSortOrder) {
            "الاسم: أ-ي 🔠" -> result.sortedBy { it.name }
            "السعر: أعلى 📈" -> result.sortedByDescending { it.price }
            "السعر: أدنى 📉" -> result.sortedBy { it.price }
            else -> result
        }
        result
    }

    var showAddOpeningDialog by remember { mutableStateOf(false) }
    var openingTypeInput by remember { mutableStateOf("WINDOW") } // WINDOW / DOOR
    var openingWidthText by remember { mutableStateOf("") }
    var openingHeightText by remember { mutableStateOf("") }
    var openingCountText by remember { mutableStateOf("1") }

    var showAddWorkItemDialog by remember { mutableStateOf(false) }
    var workNameInput by remember { mutableStateOf("") }
    var workUnitInput by remember { mutableStateOf("m²") } // m², m, piece
    var workPriceInput by remember { mutableStateOf("") }
    var quantityTypeInput by remember { mutableStateOf("WALL_NET") } // WALL_NET, FLOOR, CEILING, PERIMETER, DOORS_COUNT, WINDOWS_COUNT, CUSTOM
    var customQuantityInput by remember { mutableStateOf("1.0") }

    // Smart Irregular Wall Calculator State
    var showIrregularWallCalculator by remember { mutableStateOf(false) }
    var irregularWallShape by remember { mutableStateOf("TRIANGLE_THREE_SIDES") } // TRIANGLE_ISOSCELES, TRIANGLE_THREE_SIDES, TRAPEZOID, TRIANGLE_BASE_HEIGHT, QUAD_FOUR_SIDES
    var sideAText by remember { mutableStateOf("") }
    var sideBText by remember { mutableStateOf("") }
    var sideCText by remember { mutableStateOf("") }
    var quadSideAText by remember { mutableStateOf("") }
    var quadSideBText by remember { mutableStateOf("") }
    var quadSideCText by remember { mutableStateOf("") }
    var quadSideDText by remember { mutableStateOf("") }
    var trapezoidBase1Text by remember { mutableStateOf("") }
    var trapezoidBase2Text by remember { mutableStateOf("") }
    var trapezoidHeightText by remember { mutableStateOf("") }
    var triangleBaseText by remember { mutableStateOf("") }
    var triangleHeightText by remember { mutableStateOf("") }
    var irregularCalcError by remember { mutableStateOf("") }

    var isDropdownExpanded by remember { mutableStateOf(false) }
    val presetOptions = listOf("غرفة", "حمام", "مطبخ", "صالة", "استراحة", "صالة ضيوف", "شرفة", "ممر", "مستودع")

    // Calculations for preview card
    val lenNum = length.toDoubleOrNull() ?: 0.0
    val widNum = width.toDoubleOrNull() ?: 0.0
    val heightNum = height.toDoubleOrNull() ?: 0.0

    val metrics = com.example.domain.engine.QuantityCalculationEngine.calculateMetrics(
        lenNum, widNum, heightNum, openings
    )

    val floorArea = metrics.floorArea
    val ceilingArea = metrics.ceilingArea
    val perimeter = metrics.perimeter
    val wallArea = metrics.wallArea
    val openingsArea = metrics.openingsArea
    val netWallArea = metrics.netWallArea

    // Temp room model to query prices based on mathematical functions
    val tempRoom = RoomEntity(
        id = selectedRoomId ?: 0,
        name = name.ifBlank { "غرفة" },
        length = lenNum,
        width = widNum,
        height = heightNum,
        floorArea = floorArea,
        wallArea = wallArea,
        ceilingArea = ceilingArea,
        perimeter = perimeter,
        openingsArea = openingsArea,
        netWallArea = netWallArea,
        totalCost = 0.0,
        icon = iconEmoji,
        openings = openings,
        workItems = workItems,
        remarks = remarks
    )

    val finalRoomCost = com.example.domain.engine.PricingEngine.calculateTotal(workItems.map { it.getCost(tempRoom) })

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = { viewModel.navigateTo(RoomViewModel.Screen.LIST) },
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Text(
                            text = if (selectedRoomId == null) stringResource(R.string.room_detail_title_add) else stringResource(R.string.room_detail_title_edit),
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.primary // Facebook Blue for brand consistency
                        )

                        Button(
                            onClick = { viewModel.saveRoom() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("save_room_btn")
                        ) {
                            Text(stringResource(R.string.save_btn), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    thickness = 1.dp
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            
            // 1. Preset Dropdown & Custom name triggers
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = stringResource(R.string.room_name_label),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, Color.LightGray, RoundedCornerShape(10.dp))
                            .background(Color(0xFFFBFBFB))
                            .clickable { isDropdownExpanded = !isDropdownExpanded }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "▼",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = name.ifBlank { "اضغط لاختيار نوع الغرفة..." },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (name.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .background(Color.White)
                    ) {
                        presetOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        text = option, 
                                        textAlign = TextAlign.Right, 
                                        modifier = Modifier.fillMaxWidth(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    ) 
                                },
                                onClick = {
                                    viewModel.onPresetSelected(option)
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { viewModel.name.value = it },
                        placeholder = { Text(stringResource(R.string.room_name_placeholder), fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("name_text_input"),
                        textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.LightGray
                        ),
                        singleLine = true
                    )
                }
            }

            // 2. Physical Dimensions Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = stringResource(R.string.dimensions_title),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = height,
                            onValueChange = { viewModel.height.value = it },
                            label = { Text(stringResource(R.string.height_label), fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("height_text_input"),
                            suffix = { Text("م", fontSize = 11.sp, color = Color.Gray) },
                            textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Left),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.LightGray
                            )
                        )

                        OutlinedTextField(
                            value = width,
                            onValueChange = { viewModel.width.value = it },
                            label = { Text(stringResource(R.string.width_label), fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("width_text_input"),
                            suffix = { Text("م", fontSize = 11.sp, color = Color.Gray) },
                            textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Left),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.LightGray
                            )
                        )

                        OutlinedTextField(
                            value = length,
                            onValueChange = { viewModel.length.value = it },
                            label = { Text(stringResource(R.string.length_label), fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("length_text_input"),
                            suffix = { Text("م", fontSize = 11.sp, color = Color.Gray) },
                            textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Left),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.LightGray
                            )
                        )
                    }
                }
            }

            // 3. Openings & Deductions (subtract from walls ONLY)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = stringResource(R.string.openings_title),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.net_wall_area_msg),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val isLinearMeterRoom = name.contains("طولي") || name.contains("متر طولي") || name.contains("perimeter") || name.contains("محيط") || name.contains("نعل") || name.contains("نعلات")
                            val hasPerimeterWorkItem = workItems.any { it.quantityType == "PERIMETER" }
                            if (isLinearMeterRoom || hasPerimeterWorkItem) {
                                openingTypeInput = "DOOR"
                            } else {
                                openingTypeInput = "WINDOW"
                            }
                            showAddOpeningDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.add_opening_btn), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (openings.isEmpty()) {
                        Text(
                            text = stringResource(R.string.empty_openings),
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            openings.forEach { opening ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp))
                                        .border(0.5.dp, Color(0xFFEEEEEE), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    IconButton(
                                        onClick = { viewModel.removeOpening(opening.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFC62828), modifier = Modifier.size(14.dp))
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${opening.count}x (${String.format(Locale.ENGLISH, "%.1f", opening.totalArea)}م²)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.DarkGray
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (opening.type == "WINDOW") "🚪 ${stringResource(R.string.window_label)} (${opening.width}x${opening.height}م)" else "🚪 ${stringResource(R.string.door_label)} (${opening.width}x${opening.height}م)",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Detailed Work Items System (multiple operations per room!)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { showAddWorkItemDialog = true },
                            modifier = Modifier
                                .size(32.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }

                        Text(
                            text = stringResource(R.string.work_items_title),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Right
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (workItems.isEmpty()) {
                        Text(
                            text = stringResource(R.string.empty_work_items),
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.animateContentSize(animationSpec = spring())
                        ) {
                            workItems.forEach { work ->
                                val qty = work.getQuantity(tempRoom)
                                val cost = work.getCost(tempRoom)
                                
                                val tName = work.name
                                val isPaint = tName.contains("دهان") || tName.contains("طلاء") || tName.contains("صبغ") || tName.contains("لياسة") || tName.contains("محارة") || tName.contains("معجون")
                                val isTile = tName.contains("بلاط") || tName.contains("سيراميك") || tName.contains("أرض") || tName.contains("رخام") || tName.contains("نعل") || tName.contains("نعلات")
                                val isCeiling = tName.contains("جبس") || tName.contains("أسقف") || tName.contains("سقف") || tName.contains("بورد")
                                val isOpening = tName.contains("باب") || tName.contains("نافذة") || tName.contains("نافدة") || tName.contains("شباك") || tName.contains("أبواب") || tName.contains("ألمنيوم")

                                val sideColor = when {
                                    isPaint -> Color(0xFF2196F3)
                                    isTile -> Color(0xFF009688)
                                    isCeiling -> Color(0xFF9C27B0)
                                    isOpening -> Color(0xFFFF9800)
                                    else -> Color(0xFF607D8B)
                                }
                                val categoryIcon = when {
                                    isPaint -> "🎨"
                                    isTile -> "🧱"
                                    isCeiling -> "☁️"
                                    isOpening -> "🚪"
                                    else -> "⚙️"
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFFAFAFA), RoundedCornerShape(12.dp))
                                        .border(0.5.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                                        .padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    IconButton(
                                        onClick = { viewModel.removeWorkItem(work.id) },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(Color(0xFFFFEBEE), CircleShape)
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFC62828), modifier = Modifier.size(13.dp))
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.End,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.End,
                                            modifier = Modifier.padding(end = 10.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = work.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.onBackground
                                                )
                                                Text(text = categoryIcon, fontSize = 11.sp)
                                            }
                                            Text(
                                                text = "${String.format(Locale.ENGLISH, "%.1f", qty)} ${work.unitType} × ${String.format(Locale.ENGLISH, "%.0f", work.unitPrice)} ${stringResource(R.string.currency_symbol)}",
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
                                            Text(
                                                text = "= ${String.format(Locale.ENGLISH, "%,.0f", cost)} ${stringResource(R.string.currency_symbol)}",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        // Left/Right accent side card Odoo style ribbon
                                        Box(
                                            modifier = Modifier
                                                .width(4.dp)
                                                .height(40.dp)
                                                .background(sideColor, RoundedCornerShape(2.dp))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. Instantly displaying exact calculated quantities for only the added/active items (دفتر حساب الكميات التفصيلي للبند)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("calculations_card"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "🧮 دفتر حساب الكميات الهندسية والتكلفة بالتفصيل:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                )

                if (workItems.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.08f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
                    ) {
                        Box(
                            modifier = Modifier.padding(14.dp).fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "💡 يرجى إضافة بند عمل (مثل دهان، بلاط، إلخ) أعلاه للبدء في حساب الكميات التفصيلية وتنزيل الفتحات تلقائياً.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.secondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    workItems.forEach { work ->
                        val qtyType = work.quantityType
                        val isWall = qtyType == "WALL_NET"
                        val isFloor = qtyType == "FLOOR"
                        val isCeiling = qtyType == "CEILING"
                        val isPeri = qtyType == "PERIMETER"

                        val hasDeductions = isWall || isFloor || isCeiling || isPeri

                        val grossQty = when (qtyType) {
                            "WALL_NET" -> wallArea
                            "FLOOR" -> floorArea
                            "CEILING" -> ceilingArea
                            "PERIMETER" -> 2 * (lenNum + widNum)
                            else -> work.getQuantity(tempRoom)
                        }

                        val grossFormula = when (qtyType) {
                            "WALL_NET" -> "${String.format(Locale.ENGLISH, "%.1f", 2 * (lenNum + widNum))}م (محيط) × ${String.format(Locale.ENGLISH, "%.1f", heightNum)}م (ارتفاع)"
                            "FLOOR" -> "${String.format(Locale.ENGLISH, "%.1f", lenNum)}م (طول) × ${String.format(Locale.ENGLISH, "%.1f", widNum)}م (عرض)"
                            "CEILING" -> "${String.format(Locale.ENGLISH, "%.1f", lenNum)}م (طول) × ${String.format(Locale.ENGLISH, "%.1f", widNum)}م (عرض)"
                            "PERIMETER" -> "( ${String.format(Locale.ENGLISH, "%.1f", lenNum)}م (طول) + ${String.format(Locale.ENGLISH, "%.1f", widNum)}م (عرض) ) × 2"
                            else -> "كمية ثابتة"
                        }

                        val categoryIcon = when {
                            work.name.contains("دهان") || work.name.contains("طلاء") || work.name.contains("صبغ") -> "🎨"
                            work.name.contains("بلاط") || work.name.contains("سيراميك") || work.name.contains("أرض") -> "🧱"
                            else -> "⚙️"
                        }

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${String.format(Locale.ENGLISH, "%,.0f", work.unitPrice)} دج/${work.unitType}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        text = "📐 حساب بند: ${work.name} $categoryIcon",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.Right
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF3F4F6)))
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${String.format(Locale.ENGLISH, "%.2f", grossQty)} ${work.unitType}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.DarkGray
                                    )
                                    Text(
                                        text = "المساحة/الأمتار الإجمالية ($grossFormula)",
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Right
                                    )
                                }

                                if (hasDeductions && openings.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "⬇️ الخصومات والتنزيلات بالتفصيل:",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFC62828),
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Right
                                    )

                                    openings.forEach { op ->
                                        val deductVal = when {
                                            isPeri && op.type == "DOOR" -> op.width * op.count
                                            isPeri && op.type == "WINDOW" -> 0.0
                                            isFloor && op.type == "DOOR" -> op.width * op.count
                                            isFloor && op.type == "WINDOW" -> 0.0
                                            isCeiling -> 0.0
                                            else -> op.totalArea
                                        }

                                        if (deductVal > 0.0) {
                                            val labelType = if (isFloor && op.type == "DOOR") "🚪 خصم عرض الباب" else if (op.type == "DOOR") "🚪 خصم الباب" else "🪟 خصم النافذة"
                                            val descText = if (isFloor && op.type == "DOOR") "$labelType (${op.width}م) عدد ${op.count}" else "$labelType (${op.width}م × ${op.height}م) عدد ${op.count}"
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(start = 12.dp, top = 2.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "- ${String.format(Locale.ENGLISH, "%.2f", deductVal)} ${work.unitType}",
                                                    fontSize = 11.sp,
                                                    color = Color(0xFFC62828),
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Text(
                                                    text = descText,
                                                    fontSize = 10.5.sp,
                                                    color = Color.Gray,
                                                    textAlign = TextAlign.Right
                                                )
                                            }
                                        }
                                    }

                                    val totalDeductForWork = when {
                                        isPeri -> openings.filter { it.type == "DOOR" }.sumOf { it.width * it.count }
                                        isFloor -> openings.filter { it.type == "DOOR" }.sumOf { it.width * it.count }
                                        isCeiling -> 0.0
                                        else -> openingsArea
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "- ${String.format(Locale.ENGLISH, "%.2f", totalDeductForWork)} ${work.unitType}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFC62828)
                                        )
                                        Text(
                                            text = "مجموع مساحة الخصم والفتحات",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.DarkGray,
                                            textAlign = TextAlign.Right
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE5E7EB)))
                                Spacer(modifier = Modifier.height(8.dp))

                                val netWorkQty = work.getQuantity(tempRoom)
                                val netWorkCost = work.getCost(tempRoom)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${String.format(Locale.ENGLISH, "%.2f", netWorkQty)} ${work.unitType}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32)
                                    )
                                    Text(
                                        text = "الأمتار الصافية المتبقية",
                                        fontSize = 11.sp,
                                        color = Color.DarkGray,
                                        textAlign = TextAlign.Right
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${String.format(Locale.ENGLISH, "%,.2f", netWorkQty)} ${work.unitType} × ${String.format(Locale.ENGLISH, "%,.0f", work.unitPrice)} $currencySymbol",
                                        fontSize = 10.5.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "صيغة حساب التكلفة الصافية",
                                        fontSize = 10.5.sp,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Right
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${String.format(Locale.ENGLISH, "%,.0f", netWorkCost)} $currencySymbol",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "التكلفة الإجمالية الصافية للبند",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        textAlign = TextAlign.Right
                                    )
                                }
                            }
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${String.format(Locale.ENGLISH, "%,.0f", finalRoomCost)} $currencySymbol",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFD32F2F)
                            )
                            Text(
                                text = "إجمالي تكلفة الغرفة بالكامل:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                textAlign = TextAlign.Right
                            )
                        }
                    }
                }
            }

            // 6. Special Remarks / Materials specifications
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = stringResource(R.string.remarks_label),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = remarks,
                        onValueChange = { viewModel.remarks.value = it },
                        placeholder = { Text(stringResource(R.string.remarks_placeholder), fontSize = 11.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp)
                            .testTag("remarks_input"),
                        textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                }
            }

            // 7. Delete Button (when editing)
            if (selectedRoomId != null) {
                Button(
                    onClick = { viewModel.deleteRoom(selectedRoomId!!) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .testTag("delete_room_btn")
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Room from bill", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.delete_room_btn), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // A. Dialogue adding manual opening
    if (showAddOpeningDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showAddOpeningDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "➕ ${stringResource(R.string.add_opening_btn)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    val isLinearMeterRoom = name.contains("طولي") || name.contains("متر طولي") || name.contains("perimeter") || name.contains("محيط") || name.contains("نعل") || name.contains("نعلات")
                    val hasPerimeterWorkItem = workItems.any { it.quantityType == "PERIMETER" }
                    val allowWindow = !(isLinearMeterRoom || hasPerimeterWorkItem)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable(enabled = allowWindow) { if (allowWindow) openingTypeInput = "WINDOW" }
                        ) {
                            Text(
                                stringResource(R.string.window_label),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (allowWindow) Color.Black else Color.Gray
                            )
                            RadioButton(
                                selected = openingTypeInput == "WINDOW",
                                onClick = { if (allowWindow) openingTypeInput = "WINDOW" },
                                enabled = allowWindow
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { openingTypeInput = "DOOR" }
                        ) {
                            Text(stringResource(R.string.door_label), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            RadioButton(selected = openingTypeInput == "DOOR", onClick = { openingTypeInput = "DOOR" })
                        }
                    }

                    if (!allowWindow) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "⚠️ إضافة النوافذ غير مسموحة في حسابات المتر الطولي (المحيط)",
                            color = Color(0xFFC62828),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = openingWidthText,
                        onValueChange = { openingWidthText = it },
                        label = { Text(stringResource(R.string.opening_width), fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = openingHeightText,
                        onValueChange = { openingHeightText = it },
                        label = { Text(stringResource(R.string.opening_height), fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = openingCountText,
                        onValueChange = { openingCountText = it },
                        label = { Text(stringResource(R.string.opening_count), fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showAddOpeningDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.cancel_btn), color = Color.Black, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                val w = openingWidthText.toDoubleOrNull() ?: 1.0
                                val h = openingHeightText.toDoubleOrNull() ?: 2.0
                                val c = openingCountText.toIntOrNull() ?: 1
                                viewModel.addManualOpening(openingTypeInput, w, h, c)
                                showAddOpeningDialog = false
                                openingWidthText = ""
                                openingHeightText = ""
                                openingCountText = "1"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text(stringResource(R.string.add_btn), color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // B. Dialogue adding Work Item with detailed custom options
    if (showAddWorkItemDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showAddWorkItemDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "➕ " + stringResource(R.string.add_work_item_btn),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚙️ ضبط القوالب",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier
                                .clickable { viewModel.navigateTo(RoomViewModel.Screen.SETTINGS) }
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )

                        Text(
                            text = "📋 البنود وقوانين الأسعار هندسياً:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Right
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = templateSearchQuery,
                        onValueChange = { templateSearchQuery = it },
                        placeholder = { Text("البحث في القوالب بالاسم...", fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.LightGray,
                            focusedContainerColor = Color(0xFFF9FBFD),
                            unfocusedContainerColor = Color(0xFFF9FBFD)
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val categories = listOf("الكل", "دهانات 🎨", "بلاط 🧱", "أسقف ☁️", "فتحات 🚪")
                    androidx.compose.foundation.lazy.LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        items(categories.size) { catIdx ->
                            val cat = categories[catIdx]
                            val isSelected = templateCategoryFilter == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFF3F4F6))
                                    .clickable { templateCategoryFilter = cat }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color.DarkGray
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    val sortOrders = listOf("الافتراضي", "السعر: أعلى 📈", "السعر: أدنى 📉", "الاسم: أ-ي 🔠")
                    androidx.compose.foundation.lazy.LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        items(sortOrders.size) { sIdx ->
                            val so = sortOrders[sIdx]
                            val isSelected = templateSortOrder == so
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f) else Color.Transparent)
                                    .border(0.5.dp, if (isSelected) MaterialTheme.colorScheme.secondary else Color.LightGray, RoundedCornerShape(6.dp))
                                    .clickable { templateSortOrder = so }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = so,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.secondary else Color.Gray
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    if (filteredTemplates.isEmpty()) {
                        Text(
                            text = "لا توجد قوالب تطابق خياراتك",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        androidx.compose.foundation.lazy.LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize(animationSpec = spring()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                        ) {
                            items(filteredTemplates.size) { index ->
                                val template = filteredTemplates[index]
                                val tName = template.name
                                val isPaint = tName.contains("دهان") || tName.contains("طلاء") || tName.contains("صبغ") || tName.contains("لياسة") || tName.contains("محارة") || tName.contains("معجون")
                                val isTile = tName.contains("بلاط") || tName.contains("سيراميك") || tName.contains("أرض") || tName.contains("رخام") || tName.contains("نعل") || tName.contains("نعلات")
                                val isCeiling = tName.contains("جبس") || tName.contains("أسقف") || tName.contains("سقف") || tName.contains("بورد")
                                val isOpening = tName.contains("باب") || tName.contains("نافذة") || tName.contains("نافدة") || tName.contains("شباك") || tName.contains("أبواب") || tName.contains("ألمنيوم")

                                val pillColor = when {
                                    isPaint -> Color(0xFFE3F2FD)
                                    isTile -> Color(0xFFE0F2F1)
                                    isCeiling -> Color(0xFFF3E5F5)
                                    isOpening -> Color(0xFFFFF3E0)
                                    else -> Color(0xFFF5F5F5)
                                }
                                val borderCol = when {
                                    isPaint -> Color(0xFF90CAF9)
                                    isTile -> Color(0xFF80CBC4)
                                    isCeiling -> Color(0xFFCE93D8)
                                    isOpening -> Color(0xFFFFCC80)
                                    else -> Color(0xFFE0E0E0)
                                }
                                val textCol = when {
                                    isPaint -> Color(0xFF1565C0)
                                    isTile -> Color(0xFF00695C)
                                    isCeiling -> Color(0xFF6A1B9A)
                                    isOpening -> Color(0xFFD84315)
                                    else -> Color(0xFF424242)
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(pillColor)
                                        .border(1.dp, borderCol, RoundedCornerShape(10.dp))
                                        .clickable {
                                            workNameInput = template.name
                                            workPriceInput = if (template.price % 1 == 0.0) {
                                                template.price.toInt().toString()
                                            } else {
                                                template.price.toString()
                                            }
                                            val inferred = com.example.domain.engine.TemplateEngine.inferQuantityTypeAndUnit(template.name)
                                            quantityTypeInput = inferred.first
                                            workUnitInput = inferred.second
                                        }
                                        .padding(horizontal = 12.dp, vertical = 9.dp)
                                ) {
                                    Text(
                                        text = "${template.name} (${if (template.price % 1 == 0.0) template.price.toInt() else template.price} دج)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textCol
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = workNameInput,
                        onValueChange = { workNameInput = it },
                        label = { Text(stringResource(R.string.work_name_label), fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = workPriceInput,
                        onValueChange = { workPriceInput = it },
                        label = { Text(stringResource(R.string.unit_price_label), fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        suffix = { Text(stringResource(R.string.currency_symbol), fontSize = 10.sp, color = Color.Gray) }
                    )

                    // Helper row to save custom preset template
                    if (workNameInput.isNotBlank() && workPriceInput.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "💾 حفظ هذا البند كبند مسعر مخزن دائم",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier
                                    .clickable {
                                        val priceVal = workPriceInput.toDoubleOrNull() ?: 0.0
                                        viewModel.addWorkItemTemplate(workNameInput, priceVal)
                                    }
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(stringResource(R.string.quantity_type_label), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(6.dp))

                    val quantityOptions = listOf(
                        "WALL_NET" to stringResource(R.string.qty_type_wall_net),
                        "FLOOR" to stringResource(R.string.qty_type_floor),
                        "CEILING" to stringResource(R.string.qty_type_ceiling),
                        "PERIMETER" to stringResource(R.string.qty_type_perimeter),
                        "DOORS_COUNT" to stringResource(R.string.qty_type_doors_count),
                        "WINDOWS_COUNT" to stringResource(R.string.qty_type_windows_count),
                        "CUSTOM" to stringResource(R.string.qty_type_custom)
                    )

                    Column(modifier = Modifier.fillMaxWidth()) {
                        quantityOptions.forEach { pair ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { 
                                        quantityTypeInput = pair.first 
                                        if (pair.first == "CUSTOM") {
                                            showIrregularWallCalculator = true
                                        }
                                        workUnitInput = when (pair.first) {
                                            "WALL_NET", "FLOOR", "CEILING" -> "m²"
                                            "PERIMETER" -> "m"
                                            else -> "piece"
                                        }
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (pair.first == "CUSTOM") {
                                        Box(
                                            modifier = Modifier
                                                .padding(end = 6.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "📐 حاسبة الحوائط الذكية",
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Text(pair.second, fontSize = 12.sp)
                                }
                                RadioButton(selected = quantityTypeInput == pair.first, onClick = { 
                                    quantityTypeInput = pair.first 
                                    if (pair.first == "CUSTOM") {
                                        showIrregularWallCalculator = true
                                    }
                                    workUnitInput = when (pair.first) {
                                        "WALL_NET", "FLOOR", "CEILING" -> "m²"
                                        "PERIMETER" -> "m"
                                        else -> "piece"
                                    }
                                })
                            }
                        }
                    }

                    AnimatedVisibility(visible = quantityTypeInput == "CUSTOM") {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = customQuantityInput,
                                onValueChange = { customQuantityInput = it },
                                label = { Text(stringResource(R.string.custom_quantity_label), fontSize = 11.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Smart Irregular Wall Calculator
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)), RoundedCornerShape(12.dp)),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (showIrregularWallCalculator) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clickable { showIrregularWallCalculator = !showIrregularWallCalculator }
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.clickable { showIrregularWallCalculator = !showIrregularWallCalculator }
                                        ) {
                                            Text(
                                                text = "📐 حاسبة مساحة جدار غير مستطيل",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    if (showIrregularWallCalculator) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "اختر شكل الجدار لحساب أبعاده ومساحته بدقة كجدار فردي:",
                                            fontSize = 11.sp,
                                            color = Color.DarkGray,
                                            textAlign = TextAlign.Right,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Shapes lists and selection
                                        val shapes = listOf(
                                            "TRIANGLE_ISOSCELES" to "ضلعين متساويين 📐",
                                            "TRIANGLE_THREE_SIDES" to "3 أضلاع مختلفة 📐",
                                            "TRAPEZOID" to "شبه منحرف ⏃",
                                            "TRIANGLE_BASE_HEIGHT" to "قاعدة وارتفاع 📐",
                                            "QUAD_FOUR_SIDES" to "رباعي 4 أضلاع ⬠"
                                        )

                                        androidx.compose.foundation.lazy.LazyRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End),
                                            contentPadding = PaddingValues(vertical = 2.dp)
                                        ) {
                                            items(shapes.size) { shapeIdx ->
                                                val shapePair = shapes[shapeIdx]
                                                val isSelected = irregularWallShape == shapePair.first
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFEEEEEE))
                                                        .clickable {
                                                            irregularWallShape = shapePair.first
                                                            irregularCalcError = ""
                                                        }
                                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                                ) {
                                                    Text(
                                                        text = shapePair.second,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isSelected) Color.White else Color.DarkGray
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        when (irregularWallShape) {
                                            "TRIANGLE_ISOSCELES" -> {
                                                OutlinedTextField(
                                                    value = sideAText,
                                                    onValueChange = { sideAText = it },
                                                    label = { Text("طول الضلعين المتساويين (م)", fontSize = 11.sp) },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(8.dp),
                                                    textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right)
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                OutlinedTextField(
                                                    value = sideBText,
                                                    onValueChange = { sideBText = it },
                                                    label = { Text("طول الضلع الثالث/القاعدة (م)", fontSize = 11.sp) },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(8.dp),
                                                    textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right)
                                                )
                                            }
                                            "TRIANGLE_THREE_SIDES" -> {
                                                OutlinedTextField(
                                                    value = sideAText,
                                                    onValueChange = { sideAText = it },
                                                    label = { Text("طول الضلع الأول (م)", fontSize = 11.sp) },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(8.dp),
                                                    textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right)
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                OutlinedTextField(
                                                    value = sideBText,
                                                    onValueChange = { sideBText = it },
                                                    label = { Text("طول الضلع الثاني (م)", fontSize = 11.sp) },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(8.dp),
                                                    textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right)
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                OutlinedTextField(
                                                    value = sideCText,
                                                    onValueChange = { sideCText = it },
                                                    label = { Text("طول الضلع الثالث (م)", fontSize = 11.sp) },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(8.dp),
                                                    textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right)
                                                )
                                            }
                                            "TRAPEZOID" -> {
                                                OutlinedTextField(
                                                    value = trapezoidBase1Text,
                                                    onValueChange = { trapezoidBase1Text = it },
                                                    label = { Text("طول القاعدة الموازية الأولى (م)", fontSize = 11.sp) },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(8.dp),
                                                    textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right)
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                OutlinedTextField(
                                                    value = trapezoidBase2Text,
                                                    onValueChange = { trapezoidBase2Text = it },
                                                    label = { Text("طول القاعدة الموازية الثانية (م)", fontSize = 11.sp) },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(8.dp),
                                                    textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right)
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                OutlinedTextField(
                                                    value = trapezoidHeightText,
                                                    onValueChange = { trapezoidHeightText = it },
                                                    label = { Text("الارتفاع العمودي للعامود (م)", fontSize = 11.sp) },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(8.dp),
                                                    textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right)
                                                )
                                            }
                                            "TRIANGLE_BASE_HEIGHT" -> {
                                                OutlinedTextField(
                                                    value = triangleBaseText,
                                                    onValueChange = { triangleBaseText = it },
                                                    label = { Text("طول القاعدة (م)", fontSize = 11.sp) },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(8.dp),
                                                    textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right)
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                OutlinedTextField(
                                                    value = triangleHeightText,
                                                    onValueChange = { triangleHeightText = it },
                                                    label = { Text("الارتفاع العمودي (م)", fontSize = 11.sp) },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(8.dp),
                                                    textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right)
                                                )
                                            }
                                            "QUAD_FOUR_SIDES" -> {
                                                OutlinedTextField(
                                                    value = quadSideAText,
                                                    onValueChange = { quadSideAText = it },
                                                    label = { Text("الضلع المقابل الأول (العلوي) (م)", fontSize = 11.sp) },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(8.dp),
                                                    textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right)
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                OutlinedTextField(
                                                    value = quadSideCText,
                                                    onValueChange = { quadSideCText = it },
                                                    label = { Text("الضلع المقابل الثاني (السفلي) (م)", fontSize = 11.sp) },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(8.dp),
                                                    textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right)
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                OutlinedTextField(
                                                    value = quadSideBText,
                                                    onValueChange = { quadSideBText = it },
                                                    label = { Text("الضلع الجانبي الأول (الأيمن) (م)", fontSize = 11.sp) },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(8.dp),
                                                    textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right)
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                OutlinedTextField(
                                                    value = quadSideDText,
                                                    onValueChange = { quadSideDText = it },
                                                    label = { Text("الضلع الجانبي الثاني (الأيسر) (م)", fontSize = 11.sp) },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(8.dp),
                                                    textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right)
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "ℹ️ تحسب المساحة بضرب متوسط الأضلاع المتقابلة (صيغة البناء التقليدية للحوائط شبه المستطيلة).",
                                                    fontSize = 9.sp,
                                                    color = Color.Gray,
                                                    textAlign = TextAlign.Right,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        val areaResult: Double? = try {
                                            when (irregularWallShape) {
                                                "TRIANGLE_ISOSCELES" -> {
                                                    val aValue = sideAText.toDoubleOrNull() ?: 0.0
                                                    val bValue = sideBText.toDoubleOrNull() ?: 0.0
                                                    if (aValue > 0.0 && bValue > 0.0) {
                                                        if (aValue <= bValue / 2.0) {
                                                            irregularCalcError = "الضلع المتساوي يجب أن يكون أكبر من نصف القاعدة (${bValue / 2.0} م)"
                                                            null
                                                        } else {
                                                            irregularCalcError = ""
                                                            val hValue = Math.sqrt(aValue * aValue - (bValue * bValue) / 4.0)
                                                            0.5 * bValue * hValue
                                                        }
                                                    } else null
                                                }
                                                "TRIANGLE_THREE_SIDES" -> {
                                                    val aValue = sideAText.toDoubleOrNull() ?: 0.0
                                                    val bValue = sideBText.toDoubleOrNull() ?: 0.0
                                                    val cValue = sideCText.toDoubleOrNull() ?: 0.0
                                                    if (aValue > 0.0 && bValue > 0.0 && cValue > 0.0) {
                                                        if (aValue + bValue <= cValue || aValue + cValue <= bValue || bValue + cValue <= aValue) {
                                                            irregularCalcError = "مجموع أي ضلعين يجب أن يكون أكبر من الضلع الثالث!"
                                                            null
                                                        } else {
                                                            irregularCalcError = ""
                                                            val sTemp = (aValue + bValue + cValue) / 2.0
                                                            Math.sqrt(sTemp * (sTemp - aValue) * (sTemp - bValue) * (sTemp - cValue))
                                                        }
                                                    } else null
                                                }
                                                "TRAPEZOID" -> {
                                                    val b1Val = trapezoidBase1Text.toDoubleOrNull() ?: 0.0
                                                    val b2Val = trapezoidBase2Text.toDoubleOrNull() ?: 0.0
                                                    val hVal = trapezoidHeightText.toDoubleOrNull() ?: 0.0
                                                    if (b1Val > 0.0 && b2Val > 0.0 && hVal > 0.0) {
                                                        irregularCalcError = ""
                                                        ((b1Val + b2Val) / 2.0) * hVal
                                                    } else null
                                                }
                                                "TRIANGLE_BASE_HEIGHT" -> {
                                                    val bVal = triangleBaseText.toDoubleOrNull() ?: 0.0
                                                    val hVal = triangleHeightText.toDoubleOrNull() ?: 0.0
                                                    if (bVal > 0.0 && hVal > 0.0) {
                                                        irregularCalcError = ""
                                                        0.5 * bVal * hVal
                                                    } else null
                                                }
                                                "QUAD_FOUR_SIDES" -> {
                                                    val aVal = quadSideAText.toDoubleOrNull() ?: 0.0
                                                    val bVal = quadSideBText.toDoubleOrNull() ?: 0.0
                                                    val cVal = quadSideCText.toDoubleOrNull() ?: 0.0
                                                    val dVal = quadSideDText.toDoubleOrNull() ?: 0.0
                                                    if (aVal > 0.0 && bVal > 0.0 && cVal > 0.0 && dVal > 0.0) {
                                                        irregularCalcError = ""
                                                        ((aVal + cVal) / 2.0) * ((bVal + dVal) / 2.0)
                                                    } else null
                                                }
                                                else -> null
                                            }
                                        } catch (e: Exception) {
                                            irregularCalcError = "خطأ في حساب المخرجات"
                                            null
                                        }

                                        if (irregularCalcError.isNotBlank()) {
                                            Text(
                                                text = "⚠️ $irregularCalcError",
                                                color = Color.Red,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(),
                                                textAlign = TextAlign.Right
                                            )
                                        }

                                        if (areaResult != null && areaResult > 0.0) {
                                            val formattedArea = String.format(Locale.US, "%.2f", areaResult)
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                                    .padding(10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Button(
                                                    onClick = {
                                                        customQuantityInput = formattedArea
                                                        showIrregularWallCalculator = false
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                                    shape = RoundedCornerShape(6.dp),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                    modifier = Modifier.height(32.dp)
                                                ) {
                                                    Text("✅ تطبيق الكمية", color = Color.White, fontSize = 11.sp)
                                                }

                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text("المساحة المحسوبة للجدار:", fontSize = 10.sp, color = Color.Gray)
                                                    Text("$formattedArea م²", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { 
                                showAddWorkItemDialog = false 
                                showIrregularWallCalculator = false
                                sideAText = ""
                                sideBText = ""
                                sideCText = ""
                                quadSideAText = ""
                                quadSideBText = ""
                                quadSideCText = ""
                                quadSideDText = ""
                                trapezoidBase1Text = ""
                                trapezoidBase2Text = ""
                                trapezoidHeightText = ""
                                triangleBaseText = ""
                                triangleHeightText = ""
                                irregularCalcError = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.cancel_btn), color = Color.Black, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                if (workNameInput.isNotBlank()) {
                                    val price = workPriceInput.toDoubleOrNull() ?: 0.0
                                    val customQty = customQuantityInput.toDoubleOrNull() ?: 1.0
                                    viewModel.addWorkItem(
                                        wName = workNameInput,
                                        unitType = workUnitInput,
                                        unitPrice = price,
                                        quantityType = quantityTypeInput,
                                        customQuantity = customQty
                                    )
                                    showAddWorkItemDialog = false
                                    workNameInput = ""
                                    workPriceInput = ""
                                    quantityTypeInput = "WALL_NET"
                                    customQuantityInput = "1.0"
                                    showIrregularWallCalculator = false
                                    sideAText = ""
                                    sideBText = ""
                                    sideCText = ""
                                    quadSideAText = ""
                                    quadSideBText = ""
                                    quadSideCText = ""
                                    quadSideDText = ""
                                    trapezoidBase1Text = ""
                                    trapezoidBase2Text = ""
                                    trapezoidHeightText = ""
                                    triangleBaseText = ""
                                    triangleHeightText = ""
                                    irregularCalcError = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text(stringResource(R.string.add_btn), color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
