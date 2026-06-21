@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package wombat.joshattic.us.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import wombat.joshattic.us.data.model.User
import wombat.joshattic.us.ui.theme.getUserColorSchemeColors

@Composable
fun EditProfileContent(
    profile: User,
    bioDraft: String,
    loading: Boolean,
    error: String?,
    cacheBuster: Long,
    onBioChange: (String) -> Unit,
    onSaveBio: () -> Unit,
    onUploadPfp: (ByteArray) -> Unit,
    onDeletePfp: () -> Unit,
    onUploadBanner: (ByteArray) -> Unit,
    onDeleteBanner: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val contentResolver = remember(context) { context.contentResolver }
    val accent = remember(profile.color) { getUserColorSchemeColors(profile.color).first }

    val pfpLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes != null) {
                onUploadPfp(bytes)
            }
        }
    }

    val bannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes != null) {
                onUploadBanner(bytes)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Edit Profile",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close Edit Profile")
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Banner section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Profile Banner",
                        style = MaterialTheme.typography.titleMedium,
                        color = accent
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(accent.copy(alpha = 0.15f))
                    ) {
                        val bannerUrl = "https://api.wasteof.money/users/${profile.name}/banner" +
                                if (cacheBuster > 0) "?cb=$cacheBuster" else ""
                        
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(bannerUrl)
                                .crossfade(true)
                                .build(),
                            loading = {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                }
                            },
                            contentDescription = "Current Banner",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Banner actions overlay
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = { bannerLauncher.launch("image/*") },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Filled.PhotoCamera, contentDescription = "Change Banner", tint = Color.White)
                            }
                            IconButton(
                                onClick = onDeleteBanner,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete Banner", tint = Color.Red)
                            }
                        }
                    }
                }

                // Profile Picture section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Profile Picture",
                        style = MaterialTheme.typography.titleMedium,
                        color = accent
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ProfilePicture(
                            username = profile.name,
                            size = 80.dp,
                            borderColor = accent,
                            cacheBuster = if (cacheBuster > 0) cacheBuster.toString() else null
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { pfpLauncher.launch("image/*") },
                                colors = ButtonDefaults.buttonColors(containerColor = accent),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Change")
                            }
                            OutlinedButton(
                                onClick = onDeletePfp,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Remove")
                            }
                        }
                    }
                }

                // Bio editor section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Bio",
                        style = MaterialTheme.typography.titleMedium,
                        color = accent
                    )
                    
                    OutlinedTextField(
                        value = bioDraft,
                        onValueChange = onBioChange,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        minLines = 3,
                        maxLines = 8,
                        placeholder = { Text("Tell us about yourself...") }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    if (!error.isNullOrBlank()) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    Button(
                        onClick = onSaveBio,
                        colors = ButtonDefaults.buttonColors(containerColor = accent),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Bio")
                    }
                }
            }
        }

        // Loading overlay
        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(enabled = false) {}, // Scrim input blocker
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(color = accent)
                        Text(
                            text = "Uploading changes...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EditProfileSheet(
    profile: User,
    bioDraft: String,
    loading: Boolean,
    error: String?,
    cacheBuster: Long,
    onBioChange: (String) -> Unit,
    onSaveBio: () -> Unit,
    onUploadPfp: (ByteArray) -> Unit,
    onDeletePfp: () -> Unit,
    onUploadBanner: (ByteArray) -> Unit,
    onDeleteBanner: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        EditProfileContent(
            profile = profile,
            bioDraft = bioDraft,
            loading = loading,
            error = error,
            cacheBuster = cacheBuster,
            onBioChange = onBioChange,
            onSaveBio = onSaveBio,
            onUploadPfp = onUploadPfp,
            onDeletePfp = onDeletePfp,
            onUploadBanner = onUploadBanner,
            onDeleteBanner = onDeleteBanner,
            onDismiss = onDismiss
        )
    }
}
