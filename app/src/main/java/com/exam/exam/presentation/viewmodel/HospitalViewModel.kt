package com.exam.exam.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exam.exam.data.model.Hospital
import com.exam.exam.data.repository.HospitalRepository
import com.exam.exam.data.repository.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HospitalUiState(
    val hospitals: List<Hospital> = emptyList(),
    val filteredHospitals: List<Hospital> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val hasMoreData: Boolean = true,
    val currentPage: Int = 0,
    val totalHospitals: Int = 0
)

class HospitalViewModel(
    private val repository: HospitalRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HospitalUiState())
    val uiState: StateFlow<HospitalUiState> = _uiState.asStateFlow()
    
    private var allLoadedHospitals = mutableListOf<Hospital>()
    
    init {
        loadHospitals()
    }
    
    fun loadHospitals() {
        viewModelScope.launch {
            repository.getHospitals().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = true,
                            errorMessage = null
                        )
                    }
                    is Resource.Success -> {
                        val hospitals = resource.data
                        allLoadedHospitals.clear()
                        allLoadedHospitals.addAll(hospitals)
                        
                        _uiState.value = _uiState.value.copy(
                            hospitals = hospitals,
                            filteredHospitals = applyCurrentFilters(hospitals),
                            isLoading = false,
                            errorMessage = null,
                            hasMoreData = repository.hasMoreData(),
                            currentPage = repository.getCurrentPage(),
                            totalHospitals = repository.getTotalHospitals()
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = resource.message
                        )
                    }
                }
            }
        }
    }
    
    fun searchHospitals(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        val filteredList = applyCurrentFilters(allLoadedHospitals)
        _uiState.value = _uiState.value.copy(filteredHospitals = filteredList)
    }
    
    private fun applyCurrentFilters(hospitals: List<Hospital>): List<Hospital> {
        var filteredList = hospitals
        
        // Apply search filter
        val query = _uiState.value.searchQuery
        if (query.isNotBlank()) {
            filteredList = filteredList.filter { hospital ->
                hospital.name.contains(query, ignoreCase = true) ||
                hospital.address.contains(query, ignoreCase = true) ||
                hospital.province.contains(query, ignoreCase = true) ||
                hospital.region.contains(query, ignoreCase = true)
            }
        }
        
        return filteredList
    }
    
    fun loadMoreHospitals() {
        if (_uiState.value.isLoadingMore || !_uiState.value.hasMoreData) {
            return
        }
        
        viewModelScope.launch {
            repository.loadMoreHospitals().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoadingMore = true)
                    }
                    is Resource.Success -> {
                        val newHospitals = resource.data
                        if (newHospitals.isNotEmpty()) {
                            allLoadedHospitals.addAll(newHospitals)
                            
                            _uiState.value = _uiState.value.copy(
                                hospitals = allLoadedHospitals.toList(),
                                filteredHospitals = applyCurrentFilters(allLoadedHospitals),
                                isLoadingMore = false,
                                hasMoreData = repository.hasMoreData(),
                                currentPage = repository.getCurrentPage()
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoadingMore = false,
                                hasMoreData = false
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoadingMore = false,
                            errorMessage = resource.message
                        )
                    }
                }
            }
        }
    }
    
    fun refreshHospitals() {
        repository.clearCache()
        allLoadedHospitals.clear()
        _uiState.value = _uiState.value.copy(
            hasMoreData = true,
            currentPage = 0,
            isLoadingMore = false
        )
        loadHospitals()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun getHospitalsByProvince(province: String): List<Hospital> {
        return allLoadedHospitals.filter { 
            it.province.equals(province, ignoreCase = true) 
        }
    }
}