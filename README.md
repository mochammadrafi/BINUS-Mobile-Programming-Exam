# COVID-19 Hospital Referral Application

A comprehensive Android application that displays a list of COVID-19 referral hospitals across Indonesia with interactive province mapping functionality.

> **📋 For comprehensive project documentation, technical specifications, and implementation details, see [Summary.md](Summary.md)**

## Overview

This application provides users with easy access to information about COVID-19 referral hospitals throughout Indonesia. Users can browse hospital details, search for specific facilities, and view interactive maps of provinces when clicking on hospital locations.

## Features

- **Hospital List Display**: Complete list of COVID-19 referral hospitals with detailed information
- **Infinite Scrolling**: Smart pagination with initial 10 items, loading 20 more on scroll
- **Search Functionality**: Real-time search across hospital names, addresses, provinces, and regions
- **Hospital Images**: High-quality thumbnail images for each hospital
- **Google Maps Integration**: Click on any province to view hospitals with direct Google Maps app launches for each hospital
- **Material Design 3**: Modern UI following Google's latest design guidelines
- **Offline Support**: Cached hospital data for offline browsing
- **Medical Theme**: Professional color scheme optimized for healthcare applications
- **Performance Optimized**: Efficient memory usage with lazy loading

## API Integration

### Data Source
- **API Endpoint**: `https://dekontaminasi.com/api/id/covid19/hospitals`
- **Data Format**: JSON array containing hospital objects
- **Update Frequency**: Real-time with 5-minute cache validity

### Hospital Data Structure
```json
{
  "name": "RS UMUM DAERAH DR. ZAINOEL ABIDIN",
  "address": "JL. TGK DAUD BEUREUEH, NO. 108 B. ACEH",
  "region": "KOTA BANDA ACEH, ACEH",
  "phone": "(0651) 34565",
  "province": "Aceh"
}
```

## GUI Components Analysis

### Main Components Implemented

#### 1. **LazyColumn (RecyclerView Alternative)**
- **Purpose**: Efficiently displays scrollable list of hospitals
- **Rationale**: Better performance than RecyclerView in Compose, handles large datasets efficiently
- **Implementation**: Lazy loading with proper state management

#### 2. **Card Components**
- **Purpose**: Hospital information display with Material Design elevation
- **Layout**: Row-based layout with image, details, and interactive elements
- **Features**: Proper spacing, shadows, and rounded corners

#### 3. **AsyncImage (Coil)**
- **Purpose**: Hospital thumbnail loading with caching
- **Features**: Progressive loading, error handling, and placeholder support
- **Sources**: Curated medical images from Unsplash with proper attribution

#### 4. **ClickableText for Provinces**
- **Purpose**: Interactive province navigation to maps
- **Styling**: Underlined blue text following Material Design link patterns
- **Functionality**: Triggers navigation to province map screen

#### 5. **TopAppBar with Actions**
- **Purpose**: Application branding and quick actions
- **Features**: Refresh button, consistent theming
- **Design**: Medical blue color scheme

#### 6. **Search Interface**
- **Purpose**: Real-time hospital filtering
- **Components**: OutlinedTextField with search and clear icons
- **Functionality**: Searches across all hospital fields

#### 7. **Loading and Error States**
- **Purpose**: User feedback during data operations
- **Components**: CircularProgressIndicator, error cards with retry options
- **Design**: Consistent with medical theme

#### 8. **WebView for Maps**
- **Purpose**: Province map display using OpenStreetMap
- **Features**: Zoom controls, loading states, error handling
- **Fallback**: Graceful degradation when maps fail to load

## Architecture & Design Decisions

### 1. **MVVM Architecture Pattern**
- **ViewModel**: `HospitalViewModel` manages UI state and business logic
- **Repository**: `HospitalRepository` handles data operations and caching
- **UI State**: Reactive state management with StateFlow and Compose

### 2. **Jetpack Compose UI Framework**
- **Rationale**: Modern declarative UI, better performance, easier maintenance
- **Benefits**: Type-safe navigation, reactive UI updates, compose-first libraries

### 3. **Material Design 3 Implementation**
- **Theme Colors**: Medical blue primary (#1976D2), emergency red secondary (#F44336)
- **Typography**: Clear, accessible fonts optimized for medical information
- **Spacing**: Consistent 8dp grid system for professional appearance

### 4. **Navigation Architecture**
- **NavHost**: Compose navigation with type-safe arguments
- **Routes**: Simple string-based routing for hospital list and province maps
- **State Management**: Shared ViewModel instances across navigation

### 5. **Image Strategy**
- **Loading**: Coil library for efficient image loading and caching
- **Sources**: Curated medical facility images from royalty-free sources
- **Fallback**: Default hospital placeholder for failed loads
- **Performance**: Automatic memory and disk caching

### 6. **Data Management**
- **Caching**: 5-minute cache validity for API responses
- **Offline Support**: Cached data available when network fails
- **Error Handling**: Comprehensive error states with retry mechanisms

### 7. **Google Maps App Integration Strategy**
- **External App Launch**: Direct integration with Google Maps app for hospital locations
- **Hospital-Specific Search**: Each hospital launches Google Maps with precise search queries
- **Zero Dependencies**: No WebView, API keys, or SDK requirements
- **Universal Compatibility**: Works on all devices with automatic web fallback

## Technical Implementation

### Networking
```kotlin
// Retrofit configuration with OkHttp
private fun provideOkHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
        .addInterceptor(provideLoggingInterceptor())
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
}
```

### State Management
```kotlin
data class HospitalUiState(
    val hospitals: List<Hospital> = emptyList(),
    val filteredHospitals: List<Hospital> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = ""
)
```

#### Repository Pattern with Pagination
```kotlin
fun getHospitals(): Flow<Resource<List<Hospital>>> = flow {
    emit(Resource.Loading())
    
    try {
        if (isCacheValid()) {
            cachedHospitals?.let { hospitals ->
                val initialHospitals = hospitals.take(INITIAL_PAGE_SIZE)
                emit(Resource.Success(initialHospitals))
                return@flow
            }
        }
        
        val response = apiService.getHospitals()
        // Handle response with pagination...
    } catch (e: Exception) {
        // Error handling with cache fallback
    }
}.flowOn(Dispatchers.IO)

suspend fun loadMoreHospitals(): Flow<Resource<List<Hospital>>> = flow {
    // Load next page of hospitals
    val startIndex = INITIAL_PAGE_SIZE + (currentPage - 1) * PAGE_SIZE
    val endIndex = minOf(startIndex + PAGE_SIZE, allHospitals.size)
    val newHospitals = allHospitals.subList(startIndex, endIndex)
    emit(Resource.Success(newHospitals))
}
```

## Project Structure

```
app/src/main/java/com/exam/exam/
├── data/
│   ├── api/
│   │   ├── HospitalApiService.kt
│   │   └── NetworkModule.kt
│   ├── model/
│   │   └── Hospital.kt
│   └── repository/
│       └── HospitalRepository.kt
├── presentation/
│   ├── screen/
│   │   ├── HospitalListScreen.kt
│   │   └── ProvinceMapScreen.kt
│   └── viewmodel/
│       └── HospitalViewModel.kt
├── ui/theme/
│   ├── Color.kt
│   ├── Theme.kt
│   └── Type.kt
└── MainActivity.kt
```

## Key Features Implementation

### 1. **Hospital List with Pagination**
- Smart pagination: Initial 10 hospitals, then 20 per page
- Infinite scrolling with automatic load more
- Real-time filtering across all hospital fields
- Efficient LazyColumn rendering for large datasets
- Professional card design with hospital images
- Pagination status indicator showing progress

### 2. **Google Maps Province Navigation**
- Interactive province links in hospital cards
- Province hospital listing with individual Google Maps launch buttons
- Direct hospital search queries: "Hospital Name, Province, Indonesia"
- External Google Maps app integration with web browser fallback

### 3. **Stability & Reliability**
- 5-minute API response caching
- Graceful degradation when network unavailable
- Local image caching for thumbnails
- Zero-crash Google Maps integration via external app launch
- Guaranteed hospital information accessibility regardless of mapping availability

### 4. **Error Handling**
- Comprehensive error states with user-friendly messages
- Retry mechanisms for failed operations
- Fallback content when maps unavailable

### 5. **Material Design Compliance**
- Medical-themed color palette
- Consistent spacing and typography
- Proper elevation and shadows
- Accessibility-friendly design

## Dependencies

### Core Libraries
- **Jetpack Compose**: Modern UI toolkit
- **Navigation Compose**: Type-safe navigation
- **ViewModel & Lifecycle**: MVVM architecture support

### Networking
- **Retrofit**: HTTP client for API communication
- **OkHttp**: HTTP logging and connection management
- **Gson**: JSON serialization/deserialization

### Image Loading
- **Coil Compose**: Efficient image loading and caching

### Maps & Integration
- **Google Maps App**: External app launch with hospital-specific search queries
- **Intent-Based Navigation**: Android Intent system for seamless app switching
- **Dual Fallback**: Google Maps app primary, web browser secondary
- **No Dependencies**: Zero SDK, API key, or WebView requirements

## Installation & Setup

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK API 26+ (Android 8.0)
- Internet connection for initial data loading

### Building the Project
```bash
git clone <repository-url>
cd exam
./gradlew assembleDebug
```

### Running on Device/Emulator
```bash
./gradlew installDebug
```

## Application Screenshots

### Main Hospital List Screen
![Hospital List](screenshots/hospital_list.png)
- Clean list layout with hospital cards
- Search functionality at the top
- Professional medical theme
- Hospital images and key information

### Hospital Card Detail
![Hospital Card](screenshots/hospital_card.png)
- Hospital thumbnail image
- Complete contact information
- Clickable province link
- National referral hospital badge

### Province Hospital Screen
![Province Hospitals](screenshots/province_hospitals.png)
- Province hospital listing with complete details
- Individual "Find on Google Maps" buttons for each hospital
- Hospital-specific search queries with province context
- Direct Google Maps app integration with web fallback
- Clean, card-based hospital information display

### Search Functionality
![Search](screenshots/search_screen.png)
- Real-time search results
- Search across all hospital fields
- Clear search option
- Responsive filtering

## Performance Optimizations

### 1. **Lazy Loading & Pagination**
- LazyColumn for efficient list rendering
- Smart pagination: 10 initial + 20 per page
- Infinite scrolling with scroll position detection
- On-demand image loading with Coil
- Proper state management to prevent recomposition
- Memory-efficient data loading strategy

### 2. **Caching Strategy**
- API response caching for offline support
- Image memory and disk caching
- Intelligent cache invalidation
- Pagination state preservation across app lifecycle
- Efficient memory usage with incremental loading

### 3. **Memory Management**
- Proper ViewModel lifecycle management
- Efficient image loading with size constraints
- Background thread operations for network calls

## Testing Strategy

### Unit Tests
- Repository layer testing with mock API responses
- ViewModel state management testing
- Data model validation

### Integration Tests
- API service integration testing
- Navigation flow testing
- Error handling validation

### UI Tests
- Compose UI testing with semantics
- User interaction flow testing
- Accessibility testing

## Security Considerations

### 1. **Network Security**
- HTTPS enforcement for all API calls
- Certificate pinning for production builds
- Proper error message handling (no sensitive data exposure)

### 2. **Data Privacy**
- No personal data collection
- Public hospital information only
- Transparent data usage

## Future Enhancements

### Planned Features
1. **Google Maps Integration**: Enhanced mapping with satellite view
2. **Hospital Details Screen**: Extended information display
3. **Favorite Hospitals**: User bookmark functionality
4. **Directions Integration**: Navigate to hospital locations
5. **Push Notifications**: Hospital availability updates
6. **Multi-language Support**: Indonesian and English localization

### Technical Improvements
1. **Database Integration**: Room database for better offline support
2. **Dependency Injection**: Hilt/Dagger for better testability
3. **Modular Architecture**: Feature-based modules
4. **Performance Monitoring**: Firebase Performance integration

## Contributing

### Code Style
- Follow Kotlin coding conventions
- Use Compose best practices
- Maintain consistent naming patterns
- Document complex business logic

### Pull Request Process
1. Fork the repository
2. Create feature branch
3. Write comprehensive tests
4. Update documentation
5. Submit pull request with detailed description

## License

This project is developed for educational purposes. Hospital data is sourced from public APIs and should be used responsibly.

## Contact & Support

For technical questions or support:
- Create GitHub issues for bugs and feature requests
- Follow Android development best practices
- Reference official Jetpack Compose documentation

## Quick Reference

For detailed information about:
- **Architecture & Implementation**: See [Summary.md](Summary.md#architecture--technical-implementation)
- **API Integration**: See [Summary.md](Summary.md#api-integration--data-management) 
- **Performance Features**: See [Summary.md](Summary.md#pagination-system-implementation)
- **Google Maps Integration**: See [Summary.md](Summary.md#google-maps-integration-strategy)
- **Testing & Deployment**: See [Summary.md](Summary.md#testing-strategy)

---

**Note**: This application provides hospital reference information for COVID-19 treatment. Always verify hospital availability and contact information before visiting. In medical emergencies, contact local emergency services immediately.