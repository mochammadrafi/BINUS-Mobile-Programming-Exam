package com.exam.exam.data.repository

import com.exam.exam.data.api.HospitalApiService
import com.exam.exam.data.model.Hospital
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

sealed class Resource<T> {
    class Loading<T> : Resource<T>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error<T>(val message: String) : Resource<T>()
}

class HospitalRepository(private val apiService: HospitalApiService) {
    
    private var cachedHospitals: List<Hospital>? = null
    private var lastFetchTime: Long = 0
    private val cacheValidityDuration = 5 * 60 * 1000L // 5 minutes
    
    // Pagination properties
    companion object {
        const val PAGE_SIZE = 20
        const val INITIAL_PAGE_SIZE = 10
    }
    
    private var currentPage = 0
    private var isLoadingMore = false
    
    fun getHospitals(): Flow<Resource<List<Hospital>>> = flow {
        emit(Resource.Loading())
        
        try {
            // Check if cache is valid
            if (isCacheValid()) {
                cachedHospitals?.let { hospitals ->
                    currentPage = 0
                    val initialHospitals = hospitals.take(INITIAL_PAGE_SIZE)
                    emit(Resource.Success(initialHospitals))
                    return@flow
                }
            }
            
            // Fetch from API
            val response = apiService.getHospitals()
            
            if (response.isSuccessful) {
                val hospitals = response.body() ?: emptyList()
                cachedHospitals = hospitals
                lastFetchTime = System.currentTimeMillis()
                currentPage = 0
                val initialHospitals = hospitals.take(INITIAL_PAGE_SIZE)
                emit(Resource.Success(initialHospitals))
            } else {
                emit(Resource.Error("Failed to fetch hospitals: ${response.code()}"))
            }
        } catch (e: Exception) {
            // If network fails, try to return cached data
            cachedHospitals?.let { hospitals ->
                currentPage = 0
                val initialHospitals = hospitals.take(INITIAL_PAGE_SIZE)
                emit(Resource.Success(initialHospitals))
            } ?: emit(Resource.Error("Network error: ${e.localizedMessage}"))
        }
    }.flowOn(Dispatchers.IO)
    
    suspend fun loadMoreHospitals(): Flow<Resource<List<Hospital>>> = flow {
        if (isLoadingMore) return@flow
        
        try {
            isLoadingMore = true
            emit(Resource.Loading())
            
            cachedHospitals?.let { allHospitals ->
                currentPage++
                val startIndex = INITIAL_PAGE_SIZE + (currentPage - 1) * PAGE_SIZE
                val endIndex = minOf(startIndex + PAGE_SIZE, allHospitals.size)
                
                if (startIndex < allHospitals.size) {
                    val newHospitals = allHospitals.subList(startIndex, endIndex)
                    emit(Resource.Success(newHospitals))
                } else {
                    emit(Resource.Success(emptyList())) // No more data
                }
            } ?: emit(Resource.Error("No data available"))
        } catch (e: Exception) {
            currentPage = maxOf(0, currentPage - 1) // Rollback page increment
            emit(Resource.Error("Failed to load more: ${e.localizedMessage}"))
        } finally {
            isLoadingMore = false
        }
    }.flowOn(Dispatchers.IO)
    
    suspend fun getHospitalsByProvince(province: String): List<Hospital> = withContext(Dispatchers.IO) {
        cachedHospitals?.filter { 
            it.province.equals(province, ignoreCase = true) 
        } ?: emptyList()
    }
    
    suspend fun searchHospitals(query: String): List<Hospital> = withContext(Dispatchers.IO) {
        cachedHospitals?.filter { hospital ->
            hospital.name.contains(query, ignoreCase = true) ||
            hospital.address.contains(query, ignoreCase = true) ||
            hospital.province.contains(query, ignoreCase = true) ||
            hospital.region.contains(query, ignoreCase = true)
        } ?: emptyList()
    }
    
    fun hasMoreData(): Boolean {
        val allHospitals = cachedHospitals ?: return false
        val currentlyLoaded = INITIAL_PAGE_SIZE + currentPage * PAGE_SIZE
        return currentlyLoaded < allHospitals.size
    }
    
    fun getCurrentPage(): Int = currentPage
    
    fun getTotalHospitals(): Int = cachedHospitals?.size ?: 0
    
    fun getLoadedHospitals(): Int {
        return INITIAL_PAGE_SIZE + currentPage * PAGE_SIZE
    }
    
    private fun isCacheValid(): Boolean {
        return cachedHospitals != null && 
               (System.currentTimeMillis() - lastFetchTime) < cacheValidityDuration
    }
    
    fun clearCache() {
        cachedHospitals = null
        lastFetchTime = 0
        currentPage = 0
        isLoadingMore = false
    }
}