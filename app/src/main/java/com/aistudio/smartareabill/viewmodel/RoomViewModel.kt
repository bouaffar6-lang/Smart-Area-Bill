package com.aistudio.smartareabill.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aistudio.smartareabill.data.models.Opening
import com.aistudio.smartareabill.data.models.RoomEntity
import com.aistudio.smartareabill.data.models.WorkItem
import com.aistudio.smartareabill.data.models.WorkItemTemplate
import com.aistudio.smartareabill.data.repository.RoomRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class RoomViewModel(private val repository: RoomRepository, private val context: Context) : ViewModel() {

    enum class Screen { LIST, DETAIL, SUMMARY, SETTINGS }
    
    private val _currentScreen = MutableStateFlow(Screen.LIST)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _previousScreen = MutableStateFlow(Screen.LIST)
    val previousScreen: StateFlow<Screen> = _previousScreen.asStateFlow()

    val rooms: StateFlow<List<RoomEntity>> = repository.allRooms
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedRoomId = MutableStateFlow<Long?>(null)
    val selectedRoomId = _selectedRoomId.asStateFlow()

    // Room properties form state
    val name = MutableStateFlow("")
    val length = MutableStateFlow("")
    val width = MutableStateFlow("")
    val height = MutableStateFlow("")
    val icon = MutableStateFlow("🛏️")
    val openings = MutableStateFlow<List<Opening>>(emptyList())
    val workItems = MutableStateFlow<List<WorkItem>>(emptyList())
    val remarks = MutableStateFlow("")

    // Work item templates state & management
    private val prefs = context.getSharedPreferences("work_item_templates_prefs", Context.MODE_PRIVATE)
    
    // Company Profile state flows
    val companyName = MutableStateFlow("")
    val companyPhone = MutableStateFlow("")
    val companyWebsite = MutableStateFlow("")
    val companyLogoUri = MutableStateFlow("")
    val showCompanyInInvoice = MutableStateFlow(true)

    // Client and Project Info
    val clientName = MutableStateFlow("")
    val clientPhone = MutableStateFlow("")
    val projectAddress = MutableStateFlow("")
    val boqReference = MutableStateFlow("")
    val boqDate = MutableStateFlow("")
    val advancePayment = MutableStateFlow(0.0)
    
    // Currency Selection
    val currencySymbol = MutableStateFlow("دج")

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val templateListType = Types.newParameterizedType(List::class.java, WorkItemTemplate::class.java)
    private val templatesAdapter = moshi.adapter<List<WorkItemTemplate>>(templateListType)

    private val _templates = MutableStateFlow<List<WorkItemTemplate>>(emptyList())
    val templates = _templates.asStateFlow()

    init {
        loadTemplates()
        loadCompanyProfile()
    }

    private fun loadCompanyProfile() {
        companyName.value = prefs.getString("company_name", "") ?: ""
        companyPhone.value = prefs.getString("company_phone", "") ?: ""
        companyWebsite.value = prefs.getString("company_website", "") ?: ""
        companyLogoUri.value = prefs.getString("company_logo_uri", "") ?: ""
        showCompanyInInvoice.value = prefs.getBoolean("show_company_invoice", true)

        clientName.value = prefs.getString("client_name", "") ?: ""
        clientPhone.value = prefs.getString("client_phone", "") ?: ""
        projectAddress.value = prefs.getString("project_address", "") ?: ""
        boqReference.value = prefs.getString("boq_reference", "") ?: ""
        boqDate.value = prefs.getString("boq_date", "") ?: ""
        advancePayment.value = prefs.getFloat("advance_payment", 0.0f).toDouble()
        currencySymbol.value = prefs.getString("currency_symbol", "دج") ?: "دج"
    }

    fun saveAdvancePayment(amount: Double) {
        advancePayment.value = amount
        prefs.edit().putFloat("advance_payment", amount.toFloat()).apply()
    }

    fun saveProjectInfo(cName: String, cPhone: String, address: String, reference: String, date: String) {
        clientName.value = cName
        clientPhone.value = cPhone
        projectAddress.value = address
        boqReference.value = reference
        boqDate.value = date
        prefs.edit().apply {
            putString("client_name", cName)
            putString("client_phone", cPhone)
            putString("project_address", address)
            putString("boq_reference", reference)
            putString("boq_date", date)
            apply()
        }
    }

    fun saveCurrencySymbol(symbol: String) {
        currencySymbol.value = symbol
        prefs.edit().putString("currency_symbol", symbol).apply()
    }

    fun saveCompanyProfile(name: String, phone: String, website: String, logoUri: String, show: Boolean) {
        companyName.value = name
        companyPhone.value = phone
        companyWebsite.value = website
        companyLogoUri.value = logoUri
        showCompanyInInvoice.value = show

        prefs.edit().apply {
            putString("company_name", name)
            putString("company_phone", phone)
            putString("company_website", website)
            putString("company_logo_uri", logoUri)
            putBoolean("show_company_invoice", show)
            apply()
        }
    }

    private fun loadTemplates() {
        val json = prefs.getString("templates_list", null)
        if (json != null) {
            try {
                _templates.value = templatesAdapter.fromJson(json) ?: getPresetTemplates()
            } catch (e: Exception) {
                _templates.value = getPresetTemplates()
            }
        } else {
            _templates.value = getPresetTemplates()
            saveTemplatesToPrefs(_templates.value)
        }
    }

    private fun getPresetTemplates(): List<WorkItemTemplate> {
        return com.aistudio.smartareabill.domain.engine.TemplateEngine.getPresetTemplates()
    }

    private fun saveTemplatesToPrefs(list: List<WorkItemTemplate>) {
        try {
            val json = templatesAdapter.toJson(list)
            prefs.edit().putString("templates_list", json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addWorkItemTemplate(tName: String, price: Double) {
        if (tName.isBlank()) return
        val current = _templates.value.toMutableList()
        current.removeAll { it.name.trim().equals(tName.trim(), ignoreCase = true) }
        current.add(0, WorkItemTemplate(tName.trim(), price))
        _templates.value = current
        saveTemplatesToPrefs(current)
    }

    fun deleteWorkItemTemplate(tName: String) {
        val current = _templates.value.filter { !it.name.trim().equals(tName.trim(), ignoreCase = true) }
        _templates.value = current
        saveTemplatesToPrefs(current)
    }

    fun updateWorkItemTemplate(oldName: String, newName: String, price: Double) {
        if (newName.isBlank()) return
        val current = _templates.value.toMutableList()
        current.removeAll { it.name.trim().equals(oldName.trim(), ignoreCase = true) }
        current.removeAll { it.name.trim().equals(newName.trim(), ignoreCase = true) }
        current.add(0, WorkItemTemplate(newName.trim(), price))
        _templates.value = current
        saveTemplatesToPrefs(current)
    }

    fun moveTemplateUp(index: Int) {
        val list = _templates.value.toMutableList()
        if (index > 0 && index < list.size) {
            val element = list.removeAt(index)
            list.add(index - 1, element)
            _templates.value = list
            saveTemplatesToPrefs(list)
        }
    }

    fun moveTemplateDown(index: Int) {
        val list = _templates.value.toMutableList()
        if (index >= 0 && index < list.size - 1) {
            val element = list.removeAt(index)
            list.add(index + 1, element)
            _templates.value = list
            saveTemplatesToPrefs(list)
        }
    }

    fun navigateTo(screen: Screen) {
        _previousScreen.value = _currentScreen.value
        _currentScreen.value = screen
    }

    fun navigateBack() {
        _currentScreen.value = _previousScreen.value
    }

    fun clearForm() {
        _selectedRoomId.value = null
        name.value = ""
        length.value = ""
        width.value = ""
        height.value = ""
        icon.value = "🛏️"
        openings.value = emptyList()
        workItems.value = emptyList()
        remarks.value = ""
    }

    fun selectRoom(room: RoomEntity) {
        _selectedRoomId.value = room.id
        name.value = room.name
        length.value = if (room.length > 0) room.length.toString() else ""
        width.value = if (room.width > 0) room.width.toString() else ""
        height.value = if (room.height > 0) room.height.toString() else ""
        icon.value = room.icon
        openings.value = room.openings
        workItems.value = room.workItems
        remarks.value = room.remarks
        _currentScreen.value = Screen.DETAIL
    }

    fun onPresetSelected(preset: String) {
        val list = rooms.value
        val excludingCurrent = list.filter { it.id != _selectedRoomId.value }
        val matchedCount = excludingCurrent.count { 
            val trimmedName = it.name.trim()
            trimmedName == preset || trimmedName.startsWith("$preset ")
        }
        val newName = "$preset ${matchedCount + 1}"
        name.value = newName
        
        icon.value = when (preset) {
            "غرفة" -> "🛏️"
            "حمام" -> "🚿"
            "مطبخ" -> "🍳"
            "صالة" -> "🛋️"
            "استراحة" -> "🏡"
            "صالة ضيوف" -> "🏛️"
            "شرفة" -> "🪴"
            "ممر" -> "🚪"
            "مستودع" -> "📦"
            else -> "🏠"
        }
    }

    fun addManualOpening(type: String, w: Double, h: Double, count: Int = 1) {
        val newList = openings.value.toMutableList()
        newList.add(Opening(UUID.randomUUID().toString(), type, w, h, count))
        openings.value = newList
    }

    fun removeOpening(id: String) {
        val newList = openings.value.filter { it.id != id }
        openings.value = newList
    }

    fun addWorkItem(wName: String, unitType: String, unitPrice: Double, quantityType: String, customQuantity: Double) {
        val newList = workItems.value.toMutableList()
        newList.add(
            WorkItem(
                id = UUID.randomUUID().toString(),
                name = wName,
                unitType = unitType,
                unitPrice = unitPrice,
                quantityType = quantityType,
                customQuantity = customQuantity
            )
        )
        workItems.value = newList
    }

    fun removeWorkItem(id: String) {
        val newList = workItems.value.filter { it.id != id }
        workItems.value = newList
    }

    fun saveRoom() {
        viewModelScope.launch {
            val lenVal = length.value.toDoubleOrNull() ?: 0.0
            val widVal = width.value.toDoubleOrNull() ?: 0.0
            val heightVal = height.value.toDoubleOrNull() ?: 0.0

            val metrics = com.aistudio.smartareabill.domain.engine.QuantityCalculationEngine.calculateMetrics(
                lenVal, widVal, heightVal, openings.value
            )

            // Create temporary entity to compute totalCost
            val tempEntity = RoomEntity(
                id = _selectedRoomId.value ?: 0,
                name = name.value.ifBlank { "غرفة مساحية" },
                length = lenVal,
                width = widVal,
                height = heightVal,
                floorArea = metrics.floorArea,
                wallArea = metrics.wallArea,
                ceilingArea = metrics.ceilingArea,
                perimeter = metrics.perimeter,
                openingsArea = metrics.openingsArea,
                netWallArea = metrics.netWallArea,
                totalCost = 0.0,
                icon = icon.value,
                openings = openings.value,
                workItems = workItems.value,
                remarks = remarks.value
            )

            val costVal = workItems.value.sumOf { it.getCost(tempEntity) }

            val entity = tempEntity.copy(totalCost = costVal)

            repository.insertRoom(entity)
            clearForm()
            _currentScreen.value = Screen.LIST
        }
    }

    fun deleteRoom(id: Long) {
        viewModelScope.launch {
            repository.deleteRoom(id)
            clearForm()
            _currentScreen.value = Screen.LIST
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.clearAll()
            _currentScreen.value = Screen.LIST
        }
    }
}

class RoomViewModelFactory(
    private val repository: RoomRepository, 
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RoomViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RoomViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
