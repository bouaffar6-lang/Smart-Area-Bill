package com.example.ui.screens

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.WorkItemTemplate
import com.example.viewmodel.RoomViewModel
import java.util.Locale

@Composable
fun rememberUriImage(uriStr: String): ImageBitmap? {
    val context = LocalContext.current
    return remember(uriStr) {
        if (uriStr.isBlank()) null else {
            try {
                val uri = Uri.parse(uriStr)
                // Request persistable permission if possible
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    // Ignore if permission already granted or not persistable
                }
                
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                bitmap?.asImageBitmap()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: RoomViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val templates by viewModel.templates.collectAsState()
    
    // Profiles states from VM
    val companyNameState by viewModel.companyName.collectAsState()
    val companyPhoneState by viewModel.companyPhone.collectAsState()
    val companyWebsiteState by viewModel.companyWebsite.collectAsState()
    val companyLogoUriState by viewModel.companyLogoUri.collectAsState()
    val showCompanyInInvoiceState by viewModel.showCompanyInInvoice.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Account/Profile, 1: Pricing Templates

    var searchQuery by remember { mutableStateOf("") }
    var categoryFilter by remember { mutableStateOf("الكل") }
    var sortOrder by remember { mutableStateOf("الافتراضي") }

    val filteredTemplates = remember(templates, categoryFilter, searchQuery, sortOrder) {
        var result = templates.filter {
            val matchesSearch = if (searchQuery.isBlank()) true else {
                it.name.contains(searchQuery, ignoreCase = true)
            }
            val matchesCategory = when (categoryFilter) {
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

        result = when (sortOrder) {
            "الاسم: أ-ي 🔠" -> result.sortedBy { it.name }
            "السعر: أعلى 📈" -> result.sortedByDescending { it.price }
            "السعر: أدنى 📉" -> result.sortedBy { it.price }
            else -> result
        }
        result
    }

    // Inputs for Profile Tab
    var companyNameInput by remember(companyNameState) { mutableStateOf(companyNameState) }
    var companyPhoneInput by remember(companyPhoneState) { mutableStateOf(companyPhoneState) }
    var companyWebsiteInput by remember(companyWebsiteState) { mutableStateOf(companyWebsiteState) }
    var logoUriInput by remember(companyLogoUriState) { mutableStateOf(companyLogoUriState) }
    var displayOnInvoiceInput by remember(showCompanyInInvoiceState) { mutableStateOf(showCompanyInInvoiceState) }

    // Inputs for Client & Project Info Tab
    val clientNameState by viewModel.clientName.collectAsState()
    val clientPhoneState by viewModel.clientPhone.collectAsState()
    val projectAddressState by viewModel.projectAddress.collectAsState()
    val boqReferenceState by viewModel.boqReference.collectAsState()
    val boqDateState by viewModel.boqDate.collectAsState()
    val currencySymbolState by viewModel.currencySymbol.collectAsState()

    var clientNameInput by remember(clientNameState) { mutableStateOf(clientNameState) }
    var clientPhoneInput by remember(clientPhoneState) { mutableStateOf(clientPhoneState) }
    var projectAddressInput by remember(projectAddressState) { mutableStateOf(projectAddressState) }
    var boqReferenceInput by remember(boqReferenceState) { mutableStateOf(boqReferenceState) }
    var boqDateInput by remember(boqDateState) { mutableStateOf(boqDateState) }
    var currencySymbolInput by remember(currencySymbolState) { mutableStateOf(currencySymbolState) }
    
    // Save success animation/state helper
    var showSavedNotification by remember { mutableStateOf(false) }

    // Item Templates dialog/form inputs
    var showEditDialog by remember { mutableStateOf<WorkItemTemplate?>(null) }
    var editNameInput by remember { mutableStateOf("") }
    var editPriceInput by remember { mutableStateOf("") }

    var newNameInput by remember { mutableStateOf("") }
    var newPriceInput by remember { mutableStateOf("") }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            logoUriInput = uri.toString()
        }
    }

    Scaffold(
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
                            onClick = { viewModel.navigateBack() },
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "رجوع",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Text(
                            text = when (activeTab) {
                                0 -> "هوية المقاول والمؤسسة 💼"
                                1 -> "بيانات العميل والعملة 📊"
                                else -> "قوالب الأسعار 📋"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.primary, // Facebook Blue
                            textAlign = TextAlign.Right
                        )

                        Spacer(modifier = Modifier.width(36.dp))
                    }
                }
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    thickness = 1.dp
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            
            // Tab Selector Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Tab 2: Pricing templates
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (activeTab == 2) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { activeTab = 2 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📋 قوالب الأسعار",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeTab == 2) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Tab 1: Client & Project Info
                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (activeTab == 1) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { activeTab = 1 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📊 العميل والمشروع",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeTab == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Tab 0: Account / Business Profile
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (activeTab == 0) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { activeTab = 0 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "💼 حساب المقاول",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeTab == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (activeTab == 0) {
                // ACCOUNT / BUSINESS PROFILE FORM
                LazyColumn(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item {
                        // Profile Banner Card
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "💼 الهوية التجارية للفواتير والتقارير",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Right
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "عند ملء هذا النموذج، سيتم تضمين شعار شركتك ومسماها وبيانات التواصل تلقائياً في تقرير الفواتير المطبوع وحسابات المقايسات لتظهر بشكل احترافي للعميل.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    item {
                        // LOGO PROFILE PICKER CARD
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier.fillMaxWidth().border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "شعار الشركة والصورة الشخصية",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                val logoBitmap = rememberUriImage(logoUriInput)

                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (logoBitmap != null) {
                                        Image(
                                            bitmap = logoBitmap,
                                            contentDescription = "Company Logo",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Text(
                                            text = "🏗️",
                                            fontSize = 42.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (logoUriInput.isNotBlank()) {
                                        TextButton(
                                            onClick = { logoUriInput = "" },
                                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFC62828))
                                        ) {
                                            Text("حذف الشعار", fontSize = 11.sp)
                                        }
                                    }

                                    Button(
                                        onClick = { imageLauncher.launch("image/*") },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Text("اختر صورة الشعار", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        // INFO FORM FIELD BOX
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = companyNameInput,
                                onValueChange = { companyNameInput = it },
                                label = { Text("اسم الشركة / المهندس المستقل", fontSize = 11.sp) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right)
                            )

                            OutlinedTextField(
                                value = companyPhoneInput,
                                onValueChange = { companyPhoneInput = it },
                                label = { Text("رقم هاتف التواصل", fontSize = 11.sp) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right)
                            )

                            OutlinedTextField(
                                value = companyWebsiteInput,
                                onValueChange = { companyWebsiteInput = it },
                                label = { Text("مواقع التواصل / موقع الويب / العنوان الفعلي", fontSize = 11.sp) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right)
                            )
                        }
                    }

                    // Toggle option
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier.fillMaxWidth().border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Switch(
                                    checked = displayOnInvoiceInput,
                                    onCheckedChange = { displayOnInvoiceInput = it }
                                )
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "إظهار الهوية التجارية في الفاتورة",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = "تضمين الشعار والبيانات الترويسية في الفاتورة والملخص مفعّلاً آلياً",
                                        fontSize = 10.sp,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Right
                                    )
                                }
                            }
                        }
                    }

                    // Save Button
                    item {
                        Button(
                            onClick = {
                                viewModel.saveCompanyProfile(
                                    name = companyNameInput,
                                    phone = companyPhoneInput,
                                    website = companyWebsiteInput,
                                    logoUri = logoUriInput,
                                    show = displayOnInvoiceInput
                                )
                                showSavedNotification = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (showSavedNotification) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = if (showSavedNotification) Icons.Default.Check else Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (showSavedNotification) "تم حفظ التغييرات بنجاح!" else "حفظ البيانات التجارية",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        
                        if (showSavedNotification) {
                            LaunchedEffect(key1 = showSavedNotification) {
                                kotlinx.coroutines.delay(2500)
                                showSavedNotification = false
                            }
                        }
                    }
                }
            } else if (activeTab == 1) {
                // CLIENT & PROJECT INFO & CURRENCY
                var projectSavedNotification by remember { mutableStateOf(false) }

                LazyColumn(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "📊 بيانات العميل، موقع المشروع والعملة",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    textAlign = TextAlign.Right
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "تتيح لك هذه الصفحة إدخال تفاصيل العميل والمشروع وتخصيص رمز العملة. سيتم إدراج هذه المعلومات آلياً وبصورة منسقة في ملخص المقايسة وملف الـ PDF لمصداقية واحترافية أعلى.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = clientNameInput,
                                onValueChange = { clientNameInput = it },
                                label = { Text("اسم العميل الكامل", fontSize = 11.sp) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right)
                            )

                            OutlinedTextField(
                                value = clientPhoneInput,
                                onValueChange = { clientPhoneInput = it },
                                label = { Text("رقم هاتف العميل", fontSize = 11.sp) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right)
                            )

                            OutlinedTextField(
                                value = projectAddressInput,
                                onValueChange = { projectAddressInput = it },
                                label = { Text("عنوان / موقع المشروع (مثال: الجزائر العاصمة، فيلا 12)", fontSize = 11.sp) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = boqDateInput,
                                    onValueChange = { boqDateInput = it },
                                    label = { Text("تاريخ المقايسة", fontSize = 11.sp) },
                                    placeholder = { Text("مثال: 2026/06/09", fontSize = 10.sp) },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right)
                                )

                                OutlinedTextField(
                                    value = boqReferenceInput,
                                    onValueChange = { boqReferenceInput = it },
                                    label = { Text("رقم المرجع / رمز العقد", fontSize = 11.sp) },
                                    placeholder = { Text("مثال: BOQ-2026-001", fontSize = 10.sp) },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right)
                                )
                            }
                        }
                    }

                    // Currency Selector Section
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "💱 رمز العملة المعتمد في الحسابات",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "اختر العملة المناسبة لبلدك، وسيتم تغييرها فوراً في كافة الحسابات والـ PDF.",
                                    fontSize = 10.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))

                                val currencies = listOf(
                                    "دج" to "الدينار الجزائري (DZD)",
                                    "ج.م" to "الجنيه المصري (EGP)",
                                    "ر.س" to "الريال السعودي (SAR)",
                                    "د.إ" to "الدرهم الإماراتي (AED)",
                                    "د.ك" to "الدينار الكويتي (KWD)",
                                    "$" to "الدولار الأمريكي (USD)",
                                    "€" to "اليورو (EUR)"
                                )

                                Column(
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    currencies.forEach { (sym, fullDesc) ->
                                        val isSelected = currencySymbolInput == sym
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color(0xFFF8F9FA))
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.4f),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .clickable { currencySymbolInput = sym }
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = sym,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 14.sp,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black
                                                )
                                            }
                                            Text(
                                                text = fullDesc,
                                                fontSize = 11.sp,
                                                color = if (isSelected) Color.Black else Color.Gray,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Save Project Info & Currency Button
                    item {
                        Button(
                            onClick = {
                                viewModel.saveProjectInfo(
                                    cName = clientNameInput,
                                    cPhone = clientPhoneInput,
                                    address = projectAddressInput,
                                    reference = boqReferenceInput,
                                    date = boqDateInput
                                )
                                viewModel.saveCurrencySymbol(currencySymbolInput)
                                projectSavedNotification = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (projectSavedNotification) Color(0xFF2E7D32) else MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(
                                imageVector = if (projectSavedNotification) Icons.Default.Check else Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (projectSavedNotification) "تم حفظ بيانات المشروع بنجاح!" else "حفظ ومزامنة المشروع آلياً",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        if (projectSavedNotification) {
                            LaunchedEffect(key1 = projectSavedNotification) {
                                kotlinx.coroutines.delay(2500)
                                projectSavedNotification = false
                            }
                        }
                    }
                }
            } else {
                // TEMPLATES SECTION
                LazyColumn(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 44.dp)
                ) {
                    item {
                        // Create New Template Form Box
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "➕ إضافة قالب تسعير جديد",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = newNameInput,
                                    onValueChange = { newNameInput = it },
                                    label = { Text("اسم البند / الخدمة (مثال: دهان مائي)", fontSize = 11.sp) },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = newPriceInput,
                                    onValueChange = { newPriceInput = it },
                                    label = { Text("السعر الافتراضي الفردي ($currencySymbolState)", fontSize = 11.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        val priceVal = newPriceInput.toDoubleOrNull() ?: 0.0
                                        if (newNameInput.isNotBlank()) {
                                            viewModel.addWorkItemTemplate(newNameInput, priceVal)
                                            newNameInput = ""
                                            newPriceInput = ""
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("حفظ القالب الجديد", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("البحث في القوالب بالاسم...", fontSize = 11.sp, textAlign = TextAlign.Right) },
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
                    }

                    item {
                        Spacer(modifier = Modifier.height(6.dp))

                        val categories = listOf("الكل", "دهانات 🎨", "بلاط 🧱", "أسقف ☁️", "فتحات 🚪")
                        androidx.compose.foundation.lazy.LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End),
                            contentPadding = PaddingValues(vertical = 2.dp)
                        ) {
                            items(categories.size) { catIdx ->
                                val cat = categories[catIdx]
                                val isSelected = categoryFilter == cat
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFF3F4F6))
                                        .clickable { categoryFilter = cat }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
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
                    }

                    item {
                        Spacer(modifier = Modifier.height(6.dp))

                        val sortOrders = listOf("الافتراضي", "السعر: أعلى 📈", "السعر: أدنى 📉", "الاسم: أ-ي 🔠")
                        androidx.compose.foundation.lazy.LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End),
                            contentPadding = PaddingValues(vertical = 2.dp)
                        ) {
                            items(sortOrders.size) { sIdx ->
                                val so = sortOrders[sIdx]
                                val isSelected = sortOrder == so
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f) else Color.Transparent)
                                        .border(0.5.dp, if (isSelected) MaterialTheme.colorScheme.secondary else Color.LightGray, RoundedCornerShape(6.dp))
                                        .clickable { sortOrder = so }
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
                    }

                    item {
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (searchQuery.isBlank() && categoryFilter == "الكل" && sortOrder == "الافتراضي") {
                                Text(
                                    text = "💡 اسحب/رتب باستخدام الأسهم ⇅",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Medium
                                )
                            } else {
                                Spacer(modifier = Modifier.width(1.dp))
                            }

                            Text(
                                text = "📋 القوالب المخزنة حالياً (${filteredTemplates.size}):",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Right
                            )
                        }
                    }

                    items(filteredTemplates.size) { index ->
                        val template = filteredTemplates[index]
                        
                        val tName = template.name
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

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color.LightGray.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { viewModel.deleteWorkItemTemplate(template.name) },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .background(Color(0xFFFFEBEE), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "حذف القالب",
                                            tint = Color(0xFFC62828),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            showEditDialog = template
                                            editNameInput = template.name
                                            editPriceInput = if (template.price % 1 == 0.0) template.price.toInt().toString() else template.price.toString()
                                        },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "تعديل القالب",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }

                                    if (searchQuery.isBlank() && categoryFilter == "الكل" && sortOrder == "الافتراضي") {
                                        IconButton(
                                            onClick = { viewModel.moveTemplateUp(index) },
                                            enabled = index > 0,
                                            modifier = Modifier
                                                .size(34.dp)
                                                .background(if (index > 0) Color(0xFFF3F4F6) else Color.Transparent, CircleShape)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowUp,
                                                contentDescription = "تحريك لأعلى",
                                                tint = if (index > 0) Color.DarkGray else Color.LightGray,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = { viewModel.moveTemplateDown(index) },
                                            enabled = index < filteredTemplates.size - 1,
                                            modifier = Modifier
                                                .size(34.dp)
                                                .background(if (index < filteredTemplates.size - 1) Color(0xFFF3F4F6) else Color.Transparent, CircleShape)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowDown,
                                                contentDescription = "تحريك لأسفل",
                                                tint = if (index < filteredTemplates.size - 1) Color.DarkGray else Color.LightGray,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        modifier = Modifier.padding(end = 8.dp)
                                    ) {
                                        Text(
                                            text = template.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            textAlign = TextAlign.Right
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${String.format(Locale.ENGLISH, "%,.0f", template.price)} $currencySymbolState",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            textAlign = TextAlign.Right
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .height(36.dp)
                                            .background(sideColor, RoundedCornerShape(2.dp))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit dialog modal
    if (showEditDialog != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = null },
            confirmButton = {
                Button(
                    onClick = {
                        val original = showEditDialog!!
                        val priceVal = editPriceInput.toDoubleOrNull() ?: 0.0
                        if (editNameInput.isNotBlank()) {
                            viewModel.updateWorkItemTemplate(original.name, editNameInput, priceVal)
                            showEditDialog = null
                        }
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("حفظ التغييرات", fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = null }) {
                    Text("إلغاء", fontSize = 12.sp, color = Color.Gray)
                }
            },
            title = {
                Text(
                    text = "⚙️ تعديل قالب التسعير",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = editNameInput,
                        onValueChange = { editNameInput = it },
                        label = { Text("اسم الخدمة أو البند", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right)
                    )

                    OutlinedTextField(
                        value = editPriceInput,
                        onValueChange = { editPriceInput = it },
                        label = { Text("سعر الوحدة الافتراضي ($currencySymbolState)", fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right)
                    )
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

// Simple color helper object for transparent colors
object Colors {
    val White = Color.White
}
