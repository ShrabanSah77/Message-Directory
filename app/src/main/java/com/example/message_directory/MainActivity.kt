package com.example.message_directory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.message_directory.ui.theme.MessageDirectoryTheme
import java.util.UUID

// --- Data Models (Expanded for "Major Project" status) ---
data class Message(
    val id: String = UUID.randomUUID().toString(),
    val sender: String,
    val content: String,
    val time: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: MessageType = MessageType.TEXT
)

enum class MessageType { TEXT, IMAGE, VOICE, SYSTEM }

data class Directory(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val colors: List<Color>,
    val messages: MutableList<Message>,
    var isPinned: Boolean = false,
    var category: String = "General"
)

enum class Screen {
    Home, Dashboard, Profile, Messages, Settings, Notifications, EditProfile, Security, AnalyticsDetails
}

// --- Initial Data ---
val INITIAL_DIRECTORIES = listOf(
    Directory("1", "Personal", Icons.Default.Person, listOf(Color(0xFFFF5722), Color(0xFFFF9800)), mutableListOf(
        Message(sender = "System", content = "Secure vault active.", time = "10:00 AM", isRead = true, type = MessageType.SYSTEM)
    ), isPinned = true, category = "Private"),
    Directory("2", "Home", Icons.Default.Home, listOf(Color(0xFF81D4FA), Color(0xFF03A9F4)), mutableListOf(
        Message(sender = "Mom", content = "Pick up groceries later.", time = "6:30 PM")
    ), category = "Family"),
    Directory("3", "Work", Icons.Default.Work, listOf(Color(0xFF80DEEA), Color(0xFF00BCD4)), mutableListOf(
        Message(sender = "Project Lead", content = "The report is due tonight.", time = "9:15 AM")
    ), isPinned = true, category = "Professional"),
    Directory("4", "Family", Icons.Default.FamilyRestroom, listOf(Color(0xFFB39DDB), Color(0xFF673AB7)), mutableListOf(
        Message(sender = "Sister", content = "Check out this photo!", time = "Yesterday", type = MessageType.IMAGE)
    ), category = "Family"),
    Directory("5", "Friends", Icons.Default.Groups, listOf(Color(0xFFA5D6A7), Color(0xFF4CAF50)), mutableListOf(
        Message(sender = "Group Chat", content = "Road trip next month?", time = "3 days ago")
    ), category = "Social"),
    Directory("6", "Alerts", Icons.Default.Warning, listOf(Color(0xFFEF9A9A), Color(0xFFF44336)), mutableListOf(
        Message(sender = "Bank", content = "Suspicious login attempt detected.", time = "1 week ago", type = MessageType.SYSTEM)
    ), category = "Security")
)

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
    
    // Global User State
    var userName by remember { mutableStateOf("Commander Alex") }
    var userEmail by remember { mutableStateOf("alex.pro@nexus.com") }
    var securityLevel by remember { mutableStateOf("Advanced") }
    
    // Search & Data State
    var searchQuery by remember { mutableStateOf("") }
    val directories = remember { mutableStateListOf(*INITIAL_DIRECTORIES.toTypedArray()) }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            NexusTopBar(
                currentScreen = currentScreen,
                selectedDirectory = selectedDirectory,
                onBack = { 
                    currentScreen = when (currentScreen) {
                        Screen.Messages -> Screen.Home
                        Screen.Settings, Screen.Notifications, Screen.Security -> Screen.Profile
                        Screen.EditProfile -> Screen.Profile
                        else -> Screen.Home
                    }
                }
            )
        },
        bottomBar = {
            if (currentScreen in listOf(Screen.Home, Screen.Dashboard, Screen.Profile)) {
                NexusBottomBar(currentScreen) { currentScreen = it }
            }
        },
        floatingActionButton = {
            NexusFAB(currentScreen) { 
                if (currentScreen == Screen.Home) showAddDialog = true 
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                when (screen) {
                    Screen.Home -> HomeScreen(
                        directories = directories,
                        searchQuery = searchQuery,
                        onSearch = { searchQuery = it },
                        onDirClick = { dir ->
                            selectedDirectory = dir
                            currentScreen = Screen.Messages
                        },
                        onTogglePin = { id ->
                            val index = directories.indexOfFirst { it.id == id }
                            if (index != -1) {
                                directories[index] = directories[index].copy(isPinned = !directories[index].isPinned)
                            }
                        }
                    )
                    Screen.Dashboard -> ProDashboard(directories)
                    Screen.Messages -> MessageDetailsScreen(selectedDirectory)
                    Screen.Profile -> ProProfile(
                        name = userName,
                        email = userEmail,
                        security = securityLevel,
                        onNavigate = { currentScreen = it }
                    )
                    Screen.EditProfile -> EditProfileScreen(
                        currentName = userName,
                        currentEmail = userEmail,
                        onSave = { n, e -> userName = n; userEmail = e; currentScreen = Screen.Profile },
                        onCancel = { currentScreen = Screen.Profile }
                    )
                    Screen.Settings -> SettingsScreen(darkMode, onDarkModeToggle)
                    Screen.Notifications -> NotificationsScreen()
                    Screen.Security -> SecurityScreen(securityLevel) { securityLevel = it }
                    else -> Box(Modifier.fillMaxSize())
                }
            }
        }
    }

    if (showAddDialog) {
        AddDirectoryDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, cat ->
                directories.add(Directory(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    icon = Icons.Default.Folder,
                    colors = listOf(Color.Gray, Color.DarkGray),
                    messages = mutableListOf(),
                    category = cat
                ))
                showAddDialog = false
            }
        )
    }
}

// --- Specialized UI Components (Nexus Design System) ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NexusTopBar(currentScreen: Screen, selectedDirectory: Directory?, onBack: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = when (currentScreen) {
                        Screen.Messages -> selectedDirectory?.name ?: "Protocol"
                        else -> currentScreen.name.uppercase()
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                if (currentScreen == Screen.Home) {
                    Text("SECURE DIRECTORY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        },
        navigationIcon = {
            if (currentScreen !in listOf(Screen.Home, Screen.Dashboard, Screen.Profile)) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
            }
        },
        actions = {
            IconButton(onClick = { }) {
                Icon(Icons.Default.CloudSync, "Sync")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        )
    )
}

@Composable
fun NexusBottomBar(currentScreen: Screen, onNavigate: (Screen) -> Unit) {
    NavigationBar(
        tonalElevation = 0.dp,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        val items = listOf(
            Triple(Screen.Home, Icons.Default.Explore, "Core"),
            Triple(Screen.Dashboard, Icons.Default.Insights, "Intel"),
            Triple(Screen.Profile, Icons.Default.AccountCircle, "Profile")
        )
        items.forEach { (screen, icon, label) ->
            NavigationBarItem(
                selected = currentScreen == screen,
                onClick = { onNavigate(screen) },
                icon = { Icon(icon, label) },
                label = { Text(label, fontWeight = FontWeight.Bold) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

@Composable
fun NexusFAB(currentScreen: Screen, onClick: () -> Unit) {
    if (currentScreen == Screen.Home) {
        ExtendedFloatingActionButton(
            onClick = onClick,
            icon = { Icon(Icons.Default.AddModerator, null) },
            text = { Text("INITIALIZE") },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    }
}

// --- Home Screen (Major Version) ---

@Composable
fun HomeScreen(
    directories: List<Directory>,
    searchQuery: String,
    onSearch: (String) -> Unit,
    onDirClick: (Directory) -> Unit,
    onTogglePin: (String) -> Unit
) {
    val pinned = directories.filter { it.isPinned }
    val others = directories.filter { !it.isPinned }
    val filteredOthers = others.filter { it.name.contains(searchQuery, true) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            SearchBarPro(searchQuery, onSearch)
        }

        if (pinned.isNotEmpty() && searchQuery.isEmpty()) {
            item { SectionHeader("PINNED PROTOCOLS") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    items(pinned) { dir ->
                        PinnedDirectoryCard(dir, onDirClick, onTogglePin)
                    }
                }
            }
        }

        item { SectionHeader("ACTIVE DIRECTORIES") }
        
        if (filteredOthers.isEmpty()) {
            item { EmptyState() }
        } else {
            items(filteredOthers.chunked(2)) { pair ->
                Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    pair.forEach { dir ->
                        DirectoryTile(dir, Modifier.weight(1f), onDirClick, onTogglePin)
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
fun SearchBarPro(query: String, onValueChange: (String) -> Unit) {
    Surface(
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        TextField(
            value = query,
            onValueChange = onValueChange,
            placeholder = { Text("Search encrypted logs...") },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp),
        letterSpacing = 1.sp
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PinnedDirectoryCard(dir: Directory, onClick: (Directory) -> Unit, onPin: (String) -> Unit) {
    Card(
        modifier = Modifier
            .size(width = 160.dp, height = 180.dp)
            .combinedClickable(
                onClick = { onClick(dir) },
                onLongClick = { onPin(dir.id) }
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = dir.colors.first().copy(alpha = 0.1f))
    ) {
        Column(Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Box(
                Modifier.size(48.dp).background(Brush.linearGradient(dir.colors), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(dir.icon, null, tint = Color.White)
            }
            Column {
                Text(dir.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(dir.category, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PushPin, null, Modifier.size(12.dp), tint = dir.colors.first())
                Spacer(Modifier.width(4.dp))
                Text("${dir.messages.size} Logs", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun DirectoryTile(dir: Directory, modifier: Modifier, onClick: (Directory) -> Unit, onPin: (String) -> Unit) {
    Surface(
        onClick = { onClick(dir) },
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 2.dp
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).background(dir.colors.first(), CircleShape), contentAlignment = Alignment.Center) {
                Icon(dir.icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(dir.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${dir.messages.size} items", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            IconButton(onClick = { onPin(dir.id) }, Modifier.size(24.dp)) {
                Icon(Icons.Default.PushPin, null, tint = if(dir.isPinned) MaterialTheme.colorScheme.primary else Color.LightGray)
            }
        }
    }
}

// --- Dashboard Screen (Pro Version) ---

@Composable
fun ProDashboard(directories: List<Directory>) {
    var isLoading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(800)
        isLoading = false
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(strokeWidth = 2.dp)
        }
        return
    }

    val totalMsgs = directories.sumOf { it.messages.size }
    val unread = 12 // Mocked
    
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text("SYSTEM INTELLIGENCE", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            Text("Real-time data synchronization active", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(24.dp))
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                IntelCard("Total Logs", totalMsgs.toString(), Icons.Default.Dataset, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                IntelCard("Unread", unread.toString(), Icons.Default.MarkChatUnread, Color(0xFFFF5252), Modifier.weight(1f))
            }
        }

        item { Spacer(Modifier.height(16.dp)) }

        item {
            ProVisualizer(directories)
        }

        item {
            SectionHeader("RECENT GLOBAL ACTIVITY")
        }

        items(directories.flatMap { it.messages }.sortedByDescending { it.timestamp }.take(4)) { msg ->
            ActivityLogItem(msg)
        }

        item {
            SecurityStatusCard()
        }
    }
}

@Composable
fun IntelCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(12.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

@Composable
fun ProVisualizer(directories: List<Directory>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("DATA DISTRIBUTION", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(16.dp))
            directories.take(4).forEach { dir ->
                val progress = if(directories.sumOf { it.messages.size } == 0) 0f else dir.messages.size.toFloat() / directories.sumOf { it.messages.size }
                Column(Modifier.padding(vertical = 6.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(dir.name, style = MaterialTheme.typography.labelSmall)
                        Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                        color = dir.colors.first(),
                        trackColor = Color.LightGray.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}

@Composable
fun SecurityStatusCard() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.VerifiedUser, null, tint = Color(0xFF2E7D32))
            Spacer(Modifier.width(16.dp))
            Column {
                Text("Security Protocol: ACTIVE", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                Text("256-bit encryption verified.", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32).copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun ActivityLogItem(msg: Message) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("${msg.sender}: ${msg.content}", style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(msg.time, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
        Icon(Icons.Default.ChevronRight, null, Modifier.size(16.dp), tint = Color.LightGray)
    }
}

// --- Profile & Specialized Screens ---

@Composable
fun ProProfile(name: String, email: String, security: String, onNavigate: (Screen) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(Modifier.fillMaxWidth().height(200.dp)) {
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.surface))))
            Column(Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(Modifier.size(100.dp), shape = CircleShape, color = Color.White, shadowElevation = 8.dp) {
                    Icon(Icons.Default.AccountCircle, null, Modifier.size(100.dp), tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(8.dp))
                Text(name, fontWeight = FontWeight.Black, style = MaterialTheme.typography.headlineSmall)
                Text(email, color = MaterialTheme.colorScheme.primary)
            }
        }

        Column(Modifier.padding(24.dp)) {
            ProfileMenuSection("ACCOUNT COMMANDS") {
                ProfileMenuItem(Icons.Default.EditNote, "Edit Nexus Profile") { onNavigate(Screen.EditProfile) }
                ProfileMenuItem(Icons.Default.Security, "Security Protocol ($security)") { onNavigate(Screen.Security) }
                ProfileMenuItem(Icons.Default.NotificationsActive, "Notification Nodes") { onNavigate(Screen.Notifications) }
            }

            ProfileMenuSection("SYSTEM PREFERENCES") {
                ProfileMenuItem(Icons.Default.Tune, "Interface Settings") { onNavigate(Screen.Settings) }
                ProfileMenuItem(Icons.Default.Storage, "Storage Management") { }
                ProfileMenuItem(Icons.AutoMirrored.Filled.HelpCenter, "Knowledge Base") { }
            }
            
            Spacer(Modifier.height(32.dp))
            OutlinedButton(
                onClick = { },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
            ) {
                Text("TERMINATE SESSION", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProfileMenuSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.padding(vertical = 12.dp)) {
        Text(title, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(Modifier.height(8.dp))
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
    }
}

// --- Message Screen (Advanced) ---

@Composable
fun MessageDetailsScreen(directory: Directory?) {
    if (directory == null) return
    
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxWidth().background(Brush.linearGradient(directory.colors)).padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(directory.icon, null, tint = Color.White, modifier = Modifier.size(40.dp))
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(directory.name, color = Color.White, fontWeight = FontWeight.Black, style = MaterialTheme.typography.headlineSmall)
                    Text("${directory.messages.size} encrypted entries found", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { Spacer(Modifier.height(16.dp)) }
            items(directory.messages) { msg ->
                NexusMessageItem(msg)
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun NexusMessageItem(msg: Message) {
    val bgColor = if(msg.type == MessageType.SYSTEM) Color.Black.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 0.dp, topEnd = 24.dp, bottomEnd = 24.dp, bottomStart = 24.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(Modifier.size(24.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(msg.sender.take(1), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text(msg.sender, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.weight(1f))
                Text(msg.time, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Spacer(Modifier.height(8.dp))
            when(msg.type) {
                MessageType.IMAGE -> {
                    Box(Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(12.dp)).background(Color.LightGray), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Image, "Encrypted Image", Modifier.size(48.dp), tint = Color.Gray)
                    }
                }
                else -> {
                    Text(msg.content, style = MaterialTheme.typography.bodyMedium)
                }
            }
            if(msg.type == MessageType.SYSTEM) {
                Text("ENCRYPTED NODE", Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- Support Screens ---

@Composable
fun SecurityScreen(currentLevel: String, onLevelChange: (String) -> Unit) {
    val levels = listOf("Standard", "Advanced", "Military-Grade")
    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("SECURITY ARCHITECTURE", fontWeight = FontWeight.Black)
        levels.forEach { level ->
            Surface(
                onClick = { onLevelChange(level) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = if(currentLevel == level) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, if(currentLevel == level) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = currentLevel == level, onClick = null)
                    Spacer(Modifier.width(16.dp))
                    Text(level, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Card(modifier = Modifier.fillMaxWidth().padding(top = 24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.padding(16.dp)) {
                Text("Nexus Shield is protecting your local database with hardware-level biometric integration.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun SettingsScreen(darkMode: Boolean, onToggle: (Boolean) -> Unit) {
    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Stealth Mode (Dark Theme)", fontWeight = FontWeight.Bold)
                Text("Optimize for low light environments", style = MaterialTheme.typography.labelSmall)
            }
            Switch(checked = darkMode, onCheckedChange = onToggle)
        }
        
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Auto-Sync", fontWeight = FontWeight.Bold)
                Text("Sync nodes when on WiFi", style = MaterialTheme.typography.labelSmall)
            }
            Switch(checked = true, onCheckedChange = {})
        }
    }
}

@Composable
fun NotificationsScreen() {
    Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.NotificationsNone, null, Modifier.size(100.dp), tint = Color.LightGray)
        Text("No alerts in the last 24 hours", color = Color.Gray)
    }
}

@Composable
fun EditProfileScreen(currentName: String, currentEmail: String, onSave: (String, String) -> Unit, onCancel: () -> Unit) {
    var n by remember { mutableStateOf(currentName) }
    var e by remember { mutableStateOf(currentEmail) }
    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(value = n, onValueChange = { n = it }, label = { Text("Operational Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = e, onValueChange = { e = it }, label = { Text("Nexus ID (Email)") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { onSave(n, e) }, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("UPDATE CORE DATA") }
        TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("ABORT") }
    }
}

@Composable
fun AddDirectoryDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Social") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("INITIALIZE NEW NODE") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Node Name") })
                Text("Category:", style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Social", "Family", "Private").forEach { cat ->
                        FilterChip(selected = category == cat, onClick = { category = cat }, label = { Text(cat) })
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { if(name.isNotBlank()) onConfirm(name, category) }) { Text("INITIALIZE") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCEL") } }
    )
}

@Composable
fun EmptyState() {
    Column(Modifier.fillMaxWidth().padding(48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Inbox, null, Modifier.size(64.dp), tint = Color.LightGray)
        Text("No active nodes found in this sector.", color = Color.Gray, textAlign = TextAlign.Center)
    }
}
