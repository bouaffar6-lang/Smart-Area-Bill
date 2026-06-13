package com.aistudio.smartareabill

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.aistudio.smartareabill.data.database.AppDatabase
import com.aistudio.smartareabill.data.repository.RoomRepository
import com.aistudio.smartareabill.ui.screens.MainScreen
import com.aistudio.smartareabill.ui.screens.RoomDetailScreen
import com.aistudio.smartareabill.ui.screens.SummaryScreen
import com.aistudio.smartareabill.ui.screens.SettingsScreen
import com.aistudio.smartareabill.ui.theme.MyApplicationTheme
import com.aistudio.smartareabill.viewmodel.RoomViewModel
import com.aistudio.smartareabill.viewmodel.RoomViewModelFactory

class MainActivity : ComponentActivity() {

    private val database by lazy { AppDatabase.getDatabase(applicationContext) }
    private val repository by lazy { RoomRepository(database.roomDao()) }

    private val viewModel by lazy {
        ViewModelProvider(
            this,
            RoomViewModelFactory(repository, applicationContext)
        )[RoomViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val currentScreen by viewModel.currentScreen.collectAsState()

                // Handle system OS back button press correctly inside state navigation
                BackHandler(enabled = currentScreen != RoomViewModel.Screen.LIST) {
                    if (currentScreen == RoomViewModel.Screen.SETTINGS) {
                        viewModel.navigateBack()
                    } else {
                        viewModel.navigateTo(RoomViewModel.Screen.LIST)
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Crossfade(
                        targetState = currentScreen,
                        label = "screen_transitions",
                        modifier = Modifier.padding(innerPadding)
                    ) { screen ->
                        when (screen) {
                            RoomViewModel.Screen.LIST -> {
                                MainScreen(
                                    viewModel = viewModel,
                                    onAddRoomClick = {
                                        viewModel.clearForm()
                                        viewModel.navigateTo(RoomViewModel.Screen.DETAIL)
                                    }
                                )
                            }
                            RoomViewModel.Screen.DETAIL -> {
                                RoomDetailScreen(
                                    viewModel = viewModel
                                )
                            }
                            RoomViewModel.Screen.SUMMARY -> {
                                SummaryScreen(
                                    viewModel = viewModel
                                )
                            }
                            RoomViewModel.Screen.SETTINGS -> {
                                SettingsScreen(
                                    viewModel = viewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
