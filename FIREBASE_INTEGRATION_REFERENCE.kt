// Enhanced CaptureLineButton with Firebase integration
// This shows the key changes needed

@Composable
fun CaptureLineButton(restaurant: Restaurant) {
    val context = LocalContext.current
    val waitTimeRepository = remember { WaitTimeRepository() }
    val firebaseRepository = remember { FirebaseRepository() }
    val coroutineScope = rememberCoroutineScope()
    
    // State management
    var isUploading by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    var estimatedWaitTime by remember { mutableStateOf<Int?>(null) }
    var aiAnalysis by remember { mutableStateOf<LineAnalysis?>(null) }
    var venueMetrics by remember { mutableStateOf<VenueMetrics?>(null) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    
    // Load venue metrics on first composition
    LaunchedEffect(restaurant.name) {
        venueMetrics = firebaseRepository.getVenueMetrics(restaurant.name)
    }
    
    // Process image function (shared by camera and gallery)
    fun processImage() {
        isUploading = true
        resultMessage = null
        
        coroutineScope.launch {
            val apiKey = BuildConfig.GEMINI_API_KEY
            val result = waitTimeRepository.uploadImage(imageFile, apiKey)
            
            isUploading = false
            
            if (result.isSuccess && result.analysis != null) {
                aiAnalysis = result.analysis
                val calculator = WaitTimeCalculator()
                val waitTime = calculator.calculateWaitTime(result.analysis, venueMetrics)
                estimatedWaitTime = waitTime
                resultMessage = calculator.formatWaitTime(
                    waitTime,
                    result.confidence!!,
                    venueMetrics
                )
            } else {
                resultMessage = result.errorMessage ?: "Unknown error"
            }
        }
    }
    
    // Camera and gallery launchers (same as before)
    // ...
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Camera button
        Button(/* ... */) {
            Text(if (isUploading) "UPLOADING..." else "CAPTURE LINE")
        }
        
        // Gallery button
        TextButton(/* ... */) {
            Text("Or choose from gallery")
        }
        
        // Display result
        resultMessage?.let { message ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(message, /* ... */)
            
            // Feedback UI - only show if we have a result
            if (estimatedWaitTime != null && aiAnalysis != null) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    "Was this accurate?",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Thumbs up
                    IconButton(onClick = {
                        coroutineScope.launch {
                            firebaseRepository.saveFeedback(
                                venueId = restaurant.name,
                                venueName = restaurant.name,
                                estimatedMinutes = estimatedWaitTime!!,
                                actualMinutes = estimatedWaitTime!!, // Same as estimated
                                analysis = aiAnalysis!!
                            )
                            Toast.makeText(context, "Thanks for feedback!", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text("👍", fontSize = 24.sp)
                    }
                    
                    // Thumbs down - show dialog
                    IconButton(onClick = {
                        showFeedbackDialog = true
                    }) {
                        Text("👎", fontSize = 24.sp)
                    }
                }
            }
        }
    }
    
    // Feedback dialog for actual wait time
    if (showFeedbackDialog && estimatedWaitTime != null && aiAnalysis != null) {
        AlertDialog(
            onDismissRequest = { showFeedbackDialog = false },
            title = { Text("What was the actual wait time?") },
            text = {
                Column {
                    listOf(5, 10, 15, 20, 30, 45, 60).forEach { minutes ->
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    firebaseRepository.saveFeedback(
                                        venueId = restaurant.name,
                                        venueName = restaurant.name,
                                        estimatedMinutes = estimatedWaitTime!!,
                                        actualMinutes = minutes,
                                        analysis = aiAnalysis!!
                                    )
                                    showFeedbackDialog = false
                                    Toast.makeText(context, "Thanks for feedback!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("$minutes minutes")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showFeedbackDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
