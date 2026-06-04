package com.example.message_directory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.message_directory.ui.theme.MessageDirectoryTheme
import java.text.SimpleDateFormat
import java.util.*

// --- Models ---
data class Message(
    val id: String = UUID.randomUUID().toString(),
    val sender: String,
    val content: String,
    val time: String = SimpleDateFormat("HH:mm a", Locale.getDefault()).format(Date()),
    val timestamp: Long = System.currentTimeMillis(),
    val type: MessageType = MessageType.TEXT,
    var isImportant: Boolean = false
)

enum class MessageType { TEXT, SYSTEM }

data class Directory(
    val id: String,
    var name: String,
    var icon: ImageVector,
    val colors: List<Color>,
    val messages: SnapshotStateList<Message>,
    var isPinned: Boolean = false,
    var category: String = "General"
)

enum class Screen { Home, Dashboard, Profile, Messages, Settings, Notifications, EditProfile, Security, AnalyticsDetails }

fun createDirectory(id: String, name: String, icon: ImageVector, colors: List<Color>, category: String, isPinned: Boolean = false): Directory {
    return Directory(id, name, icon, colors, mutableStateListOf(), isPinned, category)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var darkModeEnabled by remember { mutableStateOf(false) }
            MessageDirectoryTheme(darkTheme = darkModeEnabled) {
                MessageDirectoryApp(darkModeEnabled) { darkModeEnabled = it }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageDirectoryApp(darkMode: Boolean, onDarkModeToggle: (Boolean) -> Unit) {
    var currentScreen by remember { mutableStateOf(Screen.Home) }
    var selectedDirectory by remember { mutableStateOf<Directory?>(null) }
    
    var userName by remember { mutableStateOf("Commander Alex") }
    var userEmail by remember { mutableStateOf("alex.pro@nexus.com") }
    var securityLevel by remember { mutableStateOf("Advanced") }
    
    val directories = remember { 
        mutableStateListOf(
            createDirectory("1", "Security", Icons.Default.VpnKey, listOf(Color(0xFFFF5722), Color(0xFFFF9800)), "System", true).apply {
                messages.add(Message(sender = "System", content = "Encryption layers established.", type = MessageType.SYSTEM, isImportant = true))
            },
            createDirectory("2", "Home Base", Icons.Default.Home, listOf(Color(0xFF4FC3F7), Color(0xFF0288D1)), "Living"),
            createDirectory("3", "Social Node", Icons.Default.Groups, listOf(Color(0xFFF06292), Color(0xFFC2185B)), "Social"),
            createDirectory("4", "Work Core", Icons.Default.Work, listOf(Color(0xFF9575CD), Color(0xFF512DA8)), "Professional", true),
            createDirectory("5", "Archives", Icons.Default.Inventory, listOf(Color(0xFFAED581), Color(0xFF388E3C)), "System"),
            createDirectory("6", "Alerts", Icons.Default.NotificationsActive, listOf(Color(0xFFFF8A65), Color(0xFFD84315)), "System")
        )
    }
    
    var showAddDirDialog by remember { mutableStateOf(false) }
    var showAddMsgDialog by remember { mutableStateOf(false) }
    var homeSearchQuery by remember { mutableStateOf("") }
    var homeCategoryFilter by remember { mutableStateOf("All") }
    var msgSearchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            NexusTopBar(currentScreen, selectedDirectory) { 
                currentScreen = when (currentScreen) {
                    Screen.Messages, Screen.AnalyticsDetails -> Screen.Home
                    Screen.Settings, Screen.Notifications, Screen.Security, Screen.EditProfile -> Screen.Profile
                    else -> Screen.Home
                }
            }
        },
        bottomBar = { if (currentScreen in listOf(Screen.Home, Screen.Dashboard, Screen.Profile)) NexusBottomBar(currentScreen) { currentScreen = it } },
        floatingActionButton = {
            NexusFAB(currentScreen) { 
                if (currentScreen == Screen.Home) showAddDirDialog = true
                if (currentScreen == Screen.Messages) showAddMsgDialog = true
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                when (screen) {
                    Screen.Home -> AdvancedHomeScreen(
                        directories, homeSearchQuery, homeCategoryFilter, 
                        { homeSearchQuery = it }, { homeCategoryFilter = it },
                        { selectedDirectory = it; currentScreen = Screen.Messages }, 
                        { id -> 
                            val idx = directories.indexOfFirst { it.id == id }
                            if (idx != -1) directories[idx] = directories[idx].copy(isPinned = !directories[idx].isPinned)
                        }, 
                        { id -> directories.removeIf { it.id == id } }
                    )
                    Screen.Dashboard -> AdvancedDashboard(directories) { currentScreen = Screen.AnalyticsDetails }
                    Screen.Messages -> MessageDetailsScreen(
                        selectedDirectory, msgSearchQuery, { msgSearchQuery = it }, 
                        { msgId -> 
                            val msg = selectedDirectory?.messages?.find { it.id == msgId }
                            msg?.let { it.isImportant = !it.isImportant }
                            // Force refresh for SnapshotStateList
                            val idx = selectedDirectory?.messages?.indexOfFirst { it.id == msgId } ?: -1
                            if (idx != -1) {
                                val m = selectedDirectory!!.messages[idx]
                                selectedDirectory!!.messages[idx] = m.copy(isImportant = !m.isImportant)
                            }
                        },
                        { msgId -> selectedDirectory?.messages?.removeIf { it.id == msgId } }
                    )
                    Screen.Profile -> ProProfile(userName, userEmail, securityLevel) { currentScreen = it }
                    Screen.EditProfile -> EditProfileScreen(userName, userEmail, { n, e -> userName = n; userEmail = e; currentScreen = Screen.Profile }, { currentScreen = Screen.Profile })
                    Screen.Settings -> SettingsScreen(darkMode, onDarkModeToggle) { directories.forEach { it.messages.clear() } }
                    Screen.Notifications -> NotificationsScreen()
                    Screen.Security -> SecurityScreen(securityLevel) { securityLevel = it }
                    Screen.AnalyticsDetails -> AnalyticsDetailsScreen(directories)
                }
            }
        }
    }

    if (showAddDirDialog) {
        AddDirectoryDialog(onDismiss = { showAddDirDialog = false }, onConfirm = { name, cat ->
            directories.add(createDirectory(UUID.randomUUID().toString(), name, Icons.Default.Folder, listOf(Color.Gray, Color.DarkGray), cat))
            showAddDirDialog = false
        })
    }

    if (showAddMsgDialog && selectedDirectory != null) {
        AddMessageDialog(onDismiss = { showAddMsgDialog = false }, onConfirm = { content ->
            selectedDirectory!!.messages.add(0, Message(sender = userName, content = content))
            showAddMsgDialog = false
        })
    }
}

// --- Specialized UI Components ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NexusTopBar(currentScreen: Screen, selectedDirectory: Directory?, onBack: () -> Unit) {
    Surface(shadowElevation = 8.dp) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = when (currentScreen) {
                            Screen.Messages -> selectedDirectory?.name ?: "PROTOCOL"
                            Screen.AnalyticsDetails -> "INTEL ANALYSIS"
                            Screen.Security -> "SECURITY ARCH"
                            Screen.Settings -> "SYSTEM CONFIG"
                            else -> currentScreen.name.uppercase()
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    if (currentScreen == Screen.Home) Text("SECURE NODE MANAGEMENT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            },
            navigationIcon = {
                if (currentScreen !in listOf(Screen.Home, Screen.Dashboard, Screen.Profile)) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                }
            },
            actions = {
                IconButton(onClick = {}) { Icon(Icons.Default.Shield, "Security") }
                IconButton(onClick = {}) { Icon(Icons.Default.CloudSync, "Sync") }
            }
        )
    }
}

@Composable
fun NexusBottomBar(currentScreen: Screen, onNavigate: (Screen) -> Unit) {
    NavigationBar(tonalElevation = 12.dp) {
        val items = listOf(
            Triple(Screen.Home, Icons.Default.Hub, "CORE"),
            Triple(Screen.Dashboard, Icons.Default.Analytics, "INTEL"),
            Triple(Screen.Profile, Icons.Default.AccountCircle, "COMMAND")
        )
        items.forEach { (screen, icon, label) ->
            NavigationBarItem(
                selected = currentScreen == screen,
                onClick = { onNavigate(screen) },
                icon = { Icon(icon, null, modifier = Modifier.size(26.dp)) },
                label = { Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) }
            )
        }
    }
}

@Composable
fun NexusFAB(currentScreen: Screen, onClick: () -> Unit) {
    if (currentScreen in listOf(Screen.Home, Screen.Messages)) {
        ExtendedFloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            icon = { Icon(if (currentScreen == Screen.Home) Icons.Default.AddModerator else Icons.Default.PostAdd, null) },
            text = { Text(if (currentScreen == Screen.Home) "INIT NODE" else "ADD LOG") }
        )
    }
}

// --- Home Screen ---

@Composable
fun AdvancedHomeScreen(
    dirs: List<Directory>,
    query: String,
    categoryFilter: String,
    onSearch: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onClick: (Directory) -> Unit,
    onPin: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    val pinned = dirs.filter { it.isPinned }
    val others = dirs.filter { !it.isPinned && it.name.contains(query, true) && (categoryFilter == "All" || it.category == categoryFilter) }
    val categories = listOf("All", "System", "Living", "Social", "Professional")

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(span = { GridItemSpan(2) }) {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = onSearch,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    placeholder = { Text("Search Encrypted Nodes...") },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary) },
                    trailingIcon = { Icon(Icons.AutoMirrored.Filled.Sort, null) },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
                LazyRow(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = categoryFilter == cat,
                            onClick = { onCategoryChange(cat) },
                            label = { Text(cat) },
                            shape = CircleShape
                        )
                    }
                }
            }
        }

        if (pinned.isNotEmpty() && query.isEmpty() && categoryFilter == "All") {
            item(span = { GridItemSpan(2) }) { SectionHeader("PRIORITY PROTOCOLS") }
            item(span = { GridItemSpan(2) }) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(pinned) { dir -> DirectoryCircleItem(dir, onClick, onPin, onDelete) }
                }
            }
        }

        item(span = { GridItemSpan(2) }) { SectionHeader("ACTIVE NODES (${others.size})") }
        
        if (others.isEmpty() && pinned.isEmpty()) {
            item(span = { GridItemSpan(2) }) { EmptyState() }
        } else {
            items(others) { dir ->
                DirectoryCircleItem(dir, onClick, onPin, onDelete)
            }
        }
        
        item(span = { GridItemSpan(2) }) { Spacer(Modifier.height(80.dp)) }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DirectoryCircleItem(dir: Directory, onClick: (Directory) -> Unit, onPin: (String) -> Unit, onDelete: (String) -> Unit) {
    var menu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = { onClick(dir) }, onLongClick = { menu = true }),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, dir.colors[0].copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .shadow(12.dp, CircleShape)
                    .background(Brush.verticalGradient(dir.colors), CircleShape)
            ) {
                Icon(dir.icon, null, tint = Color.White, modifier = Modifier.size(32.dp))
                if (dir.isPinned) Icon(Icons.Default.PushPin, null, tint = Color.White, modifier = Modifier.size(14.dp).align(Alignment.TopEnd).padding(6.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(dir.name, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(dir.category.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Text("${dir.messages.size} ENTRIES", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
        
        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
            DropdownMenuItem(text = { Text(if(dir.isPinned) "Unpin Node" else "Pin Node") }, onClick = { onPin(dir.id); menu = false }, leadingIcon = { Icon(Icons.Default.PushPin, null) })
            DropdownMenuItem(text = { Text("Delete Node", color = Color.Red) }, onClick = { onDelete(dir.id); menu = false }, leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) })
        }
    }
}

// --- Dashboard ---

@Composable
fun AdvancedDashboard(dirs: List<Directory>, onDetailed: () -> Unit) {
    val total = dirs.sumOf { it.messages.size }
    val important = dirs.flatMap { it.messages }.count { it.isImportant }
    val recent = dirs.flatMap { it.messages }.sortedByDescending { it.timestamp }.take(3)

    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("INTEL CENTER", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            Text("GLOBAL DATA MONITORING SYSTEM", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                IntelStatCard("NODES", dirs.size.toString(), Icons.Default.Hub, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                IntelStatCard("STARRED", important.toString(), Icons.Default.Star, Color(0xFFFFD600), Modifier.weight(1f))
            }
        }

        item { SectionHeader("RECENT SYSTEM ACTIVITY") }
        if (recent.isEmpty()) {
            item { Text("No recent logs detected.", color = Color.Gray, style = MaterialTheme.typography.bodySmall) }
        } else {
            items(recent) { msg ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(12.dp)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("${msg.sender}: ${msg.content.take(30)}...", style = MaterialTheme.typography.labelMedium)
                            Text(msg.time, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.QueryStats, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("SYSTEM EFFICIENCY", fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onDetailed, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Text("GENERATE DETAILED ANALYTICS", fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        item { SectionHeader("STORAGE DISTRIBUTION") }
        items(dirs) { dir ->
            val p = if(total == 0) 0f else dir.messages.size.toFloat() / total
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).background(dir.colors[0], CircleShape))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(dir.name, fontWeight = FontWeight.Bold)
                            Text("${(p * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
                        }
                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(progress = { p.coerceIn(0.01f, 1f) }, modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape), color = dir.colors[0])
                    }
                }
            }
        }
    }
}

@Composable
fun IntelStatCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = color.copy(0.05f)), border = BorderStroke(1.dp, color.copy(0.1f))) {
        Column(Modifier.padding(20.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(16.dp))
            Text(value, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, letterSpacing = 1.sp)
        }
    }
}

// --- Messages ---

@Composable
fun MessageDetailsScreen(dir: Directory?, query: String, onSearch: (String) -> Unit, onStar: (String) -> Unit, onDelete: (String) -> Unit) {
    if (dir == null) return
    val filtered = dir.messages.filter { it.content.contains(query, true) }
    
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxWidth().background(Brush.linearGradient(dir.colors)).padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(Modifier.size(60.dp), shape = CircleShape, color = Color.White.copy(alpha = 0.2f)) {
                    Box(contentAlignment = Alignment.Center) { Icon(dir.icon, null, tint = Color.White, modifier = Modifier.size(32.dp)) }
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(dir.name.uppercase(), color = Color.White, fontWeight = FontWeight.Black, style = MaterialTheme.typography.headlineSmall)
                    Text("${dir.messages.size} SECURE ENTRIES", color = Color.White.copy(0.7f), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        
        TextField(
            value = query, onValueChange = onSearch, modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Filter node logs...") },
            leadingIcon = { Icon(Icons.Default.FilterList, null) },
            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
        )
        
        LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(filtered) { msg -> MessageItemPro(msg, onStar, onDelete) }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageItemPro(msg: Message, onStar: (String) -> Unit, onDelete: (String) -> Unit) {
    var menu by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = {}, onLongClick = { menu = true }),
        shape = RoundedCornerShape(topStart = 0.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 20.dp),
        colors = CardDefaults.cardColors(containerColor = if(msg.type == MessageType.SYSTEM) Color.Black.copy(0.04f) else MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if(msg.isImportant) Color(0xFFFFD600) else MaterialTheme.colorScheme.outlineVariant.copy(0.5f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(msg.sender, fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.weight(1f))
                if (msg.isImportant) Icon(Icons.Default.Star, null, tint = Color(0xFFFFD600), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(msg.time, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Spacer(Modifier.height(8.dp))
            Text(msg.content, style = MaterialTheme.typography.bodyMedium)
        }
        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
            DropdownMenuItem(text = { Text(if(msg.isImportant) "Unstar" else "Star Log") }, onClick = { onStar(msg.id); menu = false }, leadingIcon = { Icon(Icons.Default.Star, null) })
            DropdownMenuItem(text = { Text("Purge Log", color = Color.Red) }, onClick = { onDelete(msg.id); menu = false }, leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) })
        }
    }
}

// --- Profile ---

@Composable
fun ProProfile(name: String, email: String, security: String, onNav: (Screen) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(Modifier.fillMaxWidth().height(240.dp).background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)))) {
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(Modifier.size(100.dp), shape = CircleShape, color = Color.White.copy(0.2f), border = BorderStroke(2.dp, Color.White)) {
                    Icon(Icons.Default.Face, null, Modifier.size(60.dp).padding(16.dp), tint = Color.White)
                }
                Spacer(Modifier.height(12.dp))
                Text(name, color = Color.White, fontWeight = FontWeight.Black, style = MaterialTheme.typography.headlineSmall)
                Text(email, color = Color.White.copy(0.7f), style = MaterialTheme.typography.labelSmall)
            }
        }
        
        Column(Modifier.padding(20.dp)) {
            SectionHeader("COMMAND CENTER")
            ProfileItem(Icons.Default.Badge, "Operational Credentials") { onNav(Screen.EditProfile) }
            ProfileItem(Icons.Default.VerifiedUser, "Security Clearance: $security") { onNav(Screen.Security) }
            ProfileItem(Icons.Default.Tune, "System Configuration") { onNav(Screen.Settings) }
            ProfileItem(Icons.Default.NotificationsActive, "Alert Protocol") { onNav(Screen.Notifications) }
            
            Spacer(Modifier.height(32.dp))
            OutlinedButton(
                onClick = {}, 
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
            ) {
                Icon(Icons.Default.PowerSettingsNew, null)
                Spacer(Modifier.width(8.dp))
                Text("TERMINATE SESSION", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProfileItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Text(label, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.Chat, null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable fun SectionHeader(t: String) = Text(t, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.sp)

@Composable fun AnalyticsDetailsScreen(dirs: List<Directory>) {
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("DETAILED NODE METRICS", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black); Spacer(Modifier.height(16.dp))
        dirs.forEach { dir ->
            Card(Modifier.fillMaxWidth().padding(vertical = 8.dp), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.size(10.dp), shape = CircleShape, color = dir.colors[0]) {}; Spacer(Modifier.width(12.dp))
                        Text(dir.name, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Classification: ${dir.category}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    HorizontalDivider(Modifier.padding(vertical = 12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("ENTRIES", style = MaterialTheme.typography.labelSmall); Text("${dir.messages.size}", fontWeight = FontWeight.Bold) }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("STATUS", style = MaterialTheme.typography.labelSmall); Text("ONLINE", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}

// --- Dialogs & Screens ---

@Composable fun AddMessageDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("INITIATE LOG TRANSMISSION") }, text = { OutlinedTextField(value = text, onValueChange = { text = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Enter encrypted data...") }) }, confirmButton = { Button(onClick = { if(text.isNotBlank()) onConfirm(text) }) { Text("TRANSMIT") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("ABORT") } })
}
@Composable fun AddDirectoryDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var cat by remember { mutableStateOf("System") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("PROVISION NEW NODE") }, text = { Column { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Node Designation") }, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(16.dp)); Text("CLASSIFICATION"); Row { listOf("System", "Professional", "Personal").forEach { c -> FilterChip(selected = cat == c, onClick = { cat = c }, label = { Text(c) }) } } } }, confirmButton = { Button(onClick = { if(name.isNotBlank()) onConfirm(name, cat) }) { Text("PROVISION") } })
}
@Composable fun SettingsScreen(darkMode: Boolean, onToggle: (Boolean) -> Unit, onClear: () -> Unit) = Column(Modifier.padding(24.dp)) { 
    Text("SYSTEM CONFIGURATION", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
    Spacer(Modifier.height(24.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("STEALTH MODE (DARK)", fontWeight = FontWeight.Bold); Switch(darkMode, onToggle) }
    Spacer(Modifier.height(16.dp))
    Button(onClick = onClear, colors = ButtonDefaults.buttonColors(containerColor = Color.Red), modifier = Modifier.fillMaxWidth()) {
        Text("PURGE ALL LOGS", fontWeight = FontWeight.Bold)
    }
}
@Composable fun NotificationsScreen() = Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("SYSTEM QUIET - NO ALERTS") }
@Composable fun SecurityScreen(curr: String, onLevel: (String) -> Unit) = Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { Text("SECURITY ARCHITECTURE", fontWeight = FontWeight.Black); Text("CURRENT CLEARANCE: $curr", color = MaterialTheme.colorScheme.primary); listOf("Standard", "Advanced", "Elite").forEach { l -> Button(onClick = { onLevel(l) }, modifier = Modifier.fillMaxWidth()) { Text(l) } } }
@Composable fun EditProfileScreen(n: String, e: String, onS: (String, String) -> Unit, onC: () -> Unit) {
    var name by remember { mutableStateOf(n) }
    var email by remember { mutableStateOf(e) }
    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("OPERATIONAL NAME") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("ID CODE (EMAIL)") }, modifier = Modifier.fillMaxWidth()); Row { Button(onClick = { onS(name, email) }, Modifier.weight(1f)) { Text("COMMIT") }; Spacer(Modifier.width(8.dp)); TextButton(onClick = onC) { Text("ABORT") } } }
}
@Composable fun EmptyState() = Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("EMPTY SECTOR", fontWeight = FontWeight.Black, color = Color.Gray) }
