package com.example.montesorrilearning.ui.teacher

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.montesorrilearning.domain.model.Child
import com.example.montesorrilearning.domain.model.MontessoriArea
import com.example.montesorrilearning.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureScreen(
    child: Child,
    capturedPhotos: List<Uri>,
    dailyCount: Int,
    dailyLimitReached: Boolean,
    isUploading: Boolean,
    onPhotoCaptured: (Uri) -> Unit,
    onPhotoRemoved: (Int) -> Unit,
    onTitleChange: (String) -> Unit,
    onAreaChange: (String) -> Unit,
    onCommentChange: (String) -> Unit,
    onSubmit: (Boolean) -> Unit,
    onBack: () -> Unit,
    onDismissLimitWarning: () -> Unit
) {
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(LocalContext.current, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    var showLimitDialog by remember { mutableStateOf(dailyLimitReached) }
    var title by remember { mutableStateOf("") }
    var selectedArea by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }

    LaunchedEffect(dailyLimitReached) {
        showLimitDialog = dailyLimitReached
    }

    if (showLimitDialog) {
        AlertDialog(
            onDismissRequest = {
                showLimitDialog = false
                onDismissLimitWarning()
            },
            title = { Text("Daily Limit Reached") },
            text = {
                Text("${child.name} has $dailyCount images today (max 50). Do you want to upload more?")
            },
            confirmButton = {
                TextButton(onClick = {
                    showLimitDialog = false
                    onDismissLimitWarning()
                }) { Text("Upload Anyway") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showLimitDialog = false
                    onDismissLimitWarning()
                }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(child.name, style = MaterialTheme.typography.titleMedium)
                            Text("$dailyCount/50 today", style = MaterialTheme.typography.labelSmall, color = if (dailyLimitReached) SoftRed else SoftGreen)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WarmCream)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = CardBackground
            ) {
                Button(
                    onClick = {
                        val override = dailyLimitReached
                        onSubmit(override)
                    },
                    enabled = !isUploading && capturedPhotos.isNotEmpty() && title.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(50.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Submit Entry")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .background(SurfaceLight)
        ) {
            if (!hasCameraPermission) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(WarmCream),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Camera permission required", color = WarmBrown)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                            Text("Grant Permission")
                        }
                    }
                }
            } else {
                CameraPreview(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    onPhotoTaken = { uri -> onPhotoCaptured(uri) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (capturedPhotos.isNotEmpty()) {
                Text(
                    "Photos (${capturedPhotos.size})",
                    style = MaterialTheme.typography.labelLarge,
                    color = WarmBrownDark,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(capturedPhotos) { index, uri ->
                        Box {
                            AsyncImage(
                                model = uri,
                                contentDescription = "Photo ${index + 1}",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { onPhotoRemoved(index) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(24.dp)
                                    .background(SoftRed.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp), tint = White)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it; onTitleChange(it) },
                label = { Text("Title") },
                placeholder = { Text("e.g. Pink Tower, Sandpaper Letters") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            MontessoriAreaDropdown(
                selectedArea = selectedArea,
                onAreaSelected = { area -> selectedArea = area; onAreaChange(area) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it; onCommentChange(it) },
                label = { Text("Teacher's Observation") },
                placeholder = { Text("Describe what the child did...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp)
                    .padding(horizontal = 16.dp),
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onPhotoTaken: (Uri) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    var previewView by remember { mutableStateOf<PreviewView?>(null) }

    DisposableEffect(lifecycleOwner) {
        val cameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView?.surfaceProvider)
        }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        onDispose {
            cameraProvider.unbindAll()
            cameraExecutor.shutdown()
        }
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).also { view ->
                    previewView = view
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        IconButton(
            onClick = {
                val photoFile = File.createTempFile(
                    SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date()),
                    ".jpg",
                    context.cacheDir
                )
                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                imageCapture.takePicture(
                    outputOptions,
                    cameraExecutor,
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            val uri = Uri.fromFile(photoFile)
                            onPhotoTaken(uri)
                        }

                        override fun onError(exception: ImageCaptureException) {
                            exception.printStackTrace()
                        }
                    }
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .size(64.dp)
                .background(White, RoundedCornerShape(32.dp))
        ) {
            Icon(
                Icons.Default.CameraAlt,
                contentDescription = "Take Photo",
                modifier = Modifier.size(32.dp),
                tint = WarmBrownDark
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MontessoriAreaDropdown(
    selectedArea: String,
    onAreaSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        OutlinedTextField(
            value = selectedArea.ifEmpty { "" },
            onValueChange = {},
            readOnly = true,
            label = { Text("Montessori Area") },
            placeholder = { Text("Select area") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            singleLine = true
        )

        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            MontessoriArea.entries.forEach { area ->
                DropdownMenuItem(
                    text = { Text(area.displayName) },
                    onClick = {
                        onAreaSelected(area.name)
                        expanded = false
                    }
                )
            }
        }
    }
}

private val White = androidx.compose.ui.graphics.Color.White
