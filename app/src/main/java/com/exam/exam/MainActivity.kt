package com.exam.exam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.exam.exam.data.api.NetworkModule
import com.exam.exam.data.repository.HospitalRepository
import com.exam.exam.presentation.screen.HospitalListScreen
import com.exam.exam.presentation.screen.ProvinceMapScreen
import com.exam.exam.presentation.viewmodel.HospitalViewModel
import com.exam.exam.ui.theme.ExamTheme

class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            ExamTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HospitalApp()
                }
            }
        }
    }
}

@Composable
fun HospitalApp() {
    val navController = rememberNavController()
    
    // Dependency injection setup
    val apiService = remember { NetworkModule.provideHospitalApiService() }
    val repository = remember { HospitalRepository(apiService) }
    
    // Shared ViewModel across navigation
    val viewModel: HospitalViewModel = viewModel { 
        HospitalViewModel(repository) 
    }
    
    NavHost(
        navController = navController,
        startDestination = "hospital_list"
    ) {
        composable("hospital_list") { 
            HospitalListScreen(
                viewModel = viewModel,
                onProvinceClick = { province ->
                    navController.navigate("province_map/$province")
                }
            )
        }
        
        composable("province_map/{province}") { backStackEntry ->
            val province = backStackEntry.arguments?.getString("province") ?: ""
            val hospitals = viewModel.getHospitalsByProvince(province)
            
            ProvinceMapScreen(
                province = province,
                hospitals = hospitals,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}