package com.exam.exam

import com.exam.exam.data.api.HospitalApiService
import com.exam.exam.data.model.Hospital
import com.exam.exam.data.repository.HospitalRepository
import com.exam.exam.data.repository.Resource
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HospitalApiTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: HospitalApiService
    private lateinit var repository: HospitalRepository

    private val sampleHospitalJson = """
        [
            {
                "name": "RS UMUM DAERAH DR. ZAINOEL ABIDIN",
                "address": "JL. TGK DAUD BEUREUEH, NO. 108 B. ACEH",
                "region": "KOTA BANDA ACEH, ACEH",
                "phone": "(0651) 34565",
                "province": "Aceh"
            },
            {
                "name": "RSUP SANGLAH",
                "address": "JL. DIPONEGORO DENPASAR BALI",
                "region": "KOTA DENPASAR, BALI",
                "phone": "(0361) 227912",
                "province": "Bali"
            }
        ]
    """.trimIndent()

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HospitalApiService::class.java)

        repository = HospitalRepository(apiService)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `test successful API response parsing`() = runTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(sampleHospitalJson)
        )

        // When
        val response = apiService.getHospitals()

        // Then
        assertTrue(response.isSuccessful)
        val hospitals = response.body()
        assertNotNull(hospitals)
        assertEquals(2, hospitals?.size)

        val firstHospital = hospitals?.first()
        assertEquals("RS UMUM DAERAH DR. ZAINOEL ABIDIN", firstHospital?.name)
        assertEquals("Aceh", firstHospital?.province)
        assertEquals("(0651) 34565", firstHospital?.phone)
    }

    @Test
    fun `test hospital data model properties`() {
        // Given
        val hospitalJson = """
            {
                "name": "RSUP SANGLAH",
                "address": "JL. DIPONEGORO DENPASAR BALI",
                "region": "KOTA DENPASAR, BALI",
                "phone": "(0361) 227912",
                "province": "Bali"
            }
        """.trimIndent()

        // When
        val hospital = Gson().fromJson(hospitalJson, Hospital::class.java)

        // Then
        assertNotNull(hospital)
        assertEquals("RSUP SANGLAH", hospital.name)
        assertEquals("Bali", hospital.province)
        assertTrue(hospital.isNationalHospital) // RSUP indicates national hospital
        assertFalse(hospital.imageUrl.isEmpty())
        assertEquals("(0361) 227912", hospital.formattedPhone)
    }

    @Test
    fun `test hospital with null phone number`() {
        // Given
        val hospitalJson = """
            {
                "name": "Test Hospital",
                "address": "Test Address",
                "region": "Test Region",
                "phone": null,
                "province": "Test Province"
            }
        """.trimIndent()

        // When
        val hospital = Gson().fromJson(hospitalJson, Hospital::class.java)

        // Then
        assertEquals("No phone available", hospital.formattedPhone)
    }

    @Test
    fun `test repository success flow`() = runTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(sampleHospitalJson)
        )

        // When
        val result = repository.getHospitals().first()

        // Then
        assertTrue(result is Resource.Success)
        val hospitals = (result as Resource.Success).data
        assertEquals(2, hospitals.size)
    }

    @Test
    fun `test repository error handling`() = runTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error")
        )

        // When
        val result = repository.getHospitals().first()

        // Then
        assertTrue(result is Resource.Error)
        val errorMessage = (result as Resource.Error).message
        assertTrue(errorMessage.contains("Failed to fetch hospitals"))
    }

    @Test
    fun `test search functionality`() = runTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(sampleHospitalJson)
        )

        // Load data first
        repository.getHospitals().first()

        // When
        val searchResults = repository.searchHospitals("Bali")

        // Then
        assertEquals(1, searchResults.size)
        assertEquals("RSUP SANGLAH", searchResults.first().name)
    }

    @Test
    fun `test filter by province`() = runTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(sampleHospitalJson)
        )

        // Load data first
        repository.getHospitals().first()

        // When
        val acehHospitals = repository.getHospitalsByProvince("Aceh")

        // Then
        assertEquals(1, acehHospitals.size)
        assertEquals("Aceh", acehHospitals.first().province)
    }



    @Test
    fun `test hospital image URL generation`() {
        // Test different hospital types generate different image URLs
        val rsupHospital = Hospital("RSUP Test", "Address", "Region", "Phone", "Province")
        val umumHospital = Hospital("RS UMUM Test", "Address", "Region", "Phone", "Province")
        val paruHospital = Hospital("RS PARU Test", "Address", "Region", "Phone", "Province")

        assertNotEquals(rsupHospital.imageUrl, umumHospital.imageUrl)
        assertNotEquals(umumHospital.imageUrl, paruHospital.imageUrl)
        assertTrue(rsupHospital.imageUrl.contains("unsplash.com"))
    }

    @Test
    fun `test national hospital identification`() {
        val rsupHospital = Hospital("RSUP Dr. Test", "Address", "Region", "Phone", "Province")
        val regularHospital = Hospital("RS Umum Test", "Address", "Region", "Phone", "Province")

        assertTrue(rsupHospital.isNationalHospital)
        assertFalse(regularHospital.isNationalHospital)
    }
}