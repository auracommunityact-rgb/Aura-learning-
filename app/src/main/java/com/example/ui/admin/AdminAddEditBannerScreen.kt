package com.example.ui.admin

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.data.models.Banner
import com.example.data.repository.AuraRepository
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddEditBannerScreen(navController: NavController, bannerId: String? = null) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { AuraRepository() }
    val isEditing = bannerId != null

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var ctaText by remember { mutableStateOf("Learn More") }
    var ctaLink by remember { mutableStateOf("") }
    var order by remember { mutableStateOf("1") }
    var isEnabled by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(bannerId) {
        if (isEditing) {
            isLoading = true
            try {
                val banners = repository.getBanners() // Fetching all is easiest for now
                val banner = banners.find { it.id == bannerId }
                if (banner != null) {
                    title = banner.title
                    description = banner.description
                    imageUrl = banner.imageUrl
                    ctaText = banner.ctaText
                    ctaLink = banner.link
                    order = banner.order.toString()
                    isEnabled = banner.isEnabled
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error loading banner: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Banner" else "Add Banner", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Banner Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Short Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
                
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Image URL") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = ctaText,
                        onValueChange = { ctaText = it },
                        label = { Text("CTA Button Text") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = order,
                        onValueChange = { if (it.all { char -> char.isDigit() }) order = it },
                        label = { Text("Order") },
                        modifier = Modifier.weight(0.5f)
                    )
                }
                
                OutlinedTextField(
                    value = ctaLink,
                    onValueChange = { ctaLink = it },
                    label = { Text("CTA Button Link (Screen or URL)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(checked = isEnabled, onCheckedChange = { isEnabled = it })
                    Text("Enable this banner")
                }
                
                Button(
                    onClick = {
                        if (title.isEmpty() || imageUrl.isEmpty()) {
                            Toast.makeText(context, "Please fill title and image URL", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        coroutineScope.launch {
                            try {
                                val banner = Banner(
                                    id = bannerId ?: UUID.randomUUID().toString(),
                                    title = title,
                                    description = description,
                                    imageUrl = imageUrl,
                                    ctaText = ctaText,
                                    link = ctaLink,
                                    order = order.toIntOrNull() ?: 1,
                                    isEnabled = isEnabled,
                                    backgroundColor = "#6366f1" // Default
                                )
                                
                                if (isEditing) {
                                    repository.updateBanner(banner)
                                    Toast.makeText(context, "Banner updated", Toast.LENGTH_SHORT).show()
                                } else {
                                    repository.addBanner(banner)
                                    Toast.makeText(context, "Banner added", Toast.LENGTH_SHORT).show()
                                }
                                navController.popBackStack()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isEditing) "Save Changes" else "Add Banner")
                }
            }
        }
    }
}
