package com.example.message_directory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.message_directory.ui.theme.MessageDirectoryTheme
import java.util.UUID

// Data Models
data class Message(val id: String, val sender: String, val content: String, val time: String, val timestamp: Long = System.currentTimeMillis())
data class Directory(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val colors: List<Color>,
    val messages: MutableList<Message>
)

enum class Screen {
    Home, Dashboard, Profile, Messages, Settings, Notifications, EditProfile
}

// Global state for simplicity in this demo
val INITIAL_DIRECTORIES = listOf(
    Directory("1", "You", Icons.Default.Person, listOf(Color(0xFFFF5722), Color(0xFFFF9800)), mutableListOf(
        Message("m1", "System", "Welcome to your personal directory!", "10:00 AM")
    )),
    Directory("2", "Home", Icons.Default.Home, listOf(Color(0xFF81D4FA), Color(0xFF03A9F4)), mutableListOf(
        Message("m2", "Mom", "The dinner is ready.", "6:30 PM")
    )),
    Directory("3", "Love", Icons.Default.Favorite, listOf(Color(0xFFF48FB1), Color(0xFFE91E63)), mutableListOf(
        Message("m3", "Partner", "Love you!", "Yesterday")
    )),
    Directory("4", "Family", Icons.Default.FamilyRestroom, listOf(Color(0xFFB39DDB), Color(0xFF673AB7)), mutableListOf(
        Message("m4", "Dad", "Call me when you're free.", "2 days ago")
    )),
    Directory("5", "Friends", Icons.Default.Groups, listOf(Color(0xFFA5D6A7), Color(0xFF4CAF50)), mutableListOf(
        Message("m5", "John", "Are we still on for Friday?", "3 days ago")
    )),
    Directory("6", "School", Icons.Default.School, listOf(Color(0xFF80DEEA), Color(0xFF00BCD4)), mutableListOf(
        Message("m6", "Professor", "The assignment deadline is extended.", "Last week")
    ))
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
    
    // User Profile State
    var userName by remember { mutableStateOf("Alex Johnson") }
    var userEmail by remember { mutableStateOf("alex.johnson@example.com") }
    var notificationsEnabled by remember { mutableStateOf(true) }
    
    // Home State
    var searchQuery by remember { mutableStateOf("") }
    var sortAscending by remember { mutableStateOf(true) }
    var showAddDirectoryDialog by remember { mutableStateOf(false) }
    
    // Data State
    val directories = remember { mutableStateListOf(*INITIAL_DIRECTORIES.toTypedArray()) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = when (currentScreen) {
                            Screen.Messages -> selectedDirectory?.name ?: "Messages"
                            Screen.Settings -> "Settings"
                            Screen.Notifications -> "Notifications"
                            Screen.EditProfile -> "Edit Profile"
                            else -> currentScreen.name
                        },
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    ) 
                },
                navigationIcon = {
                    if (currentScreen != Screen.Home && currentScreen != Screen.Dashboard && currentScreen != Screen.Profile) {
                        IconButton(onClick = { 
                            currentScreen = if (currentScreen == Screen.Messages) Screen.Home else Screen.Profile 
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (currentScreen == Screen.Home) {
                        IconButton(onClick = { sortAscending = !sortAscending }) {
                            Icon(
                                if (sortAscending) Icons.Default.SortByAlpha else Icons.AutoMirrored.Filled.Sort,
                                contentDescription = "Sort"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            if (currentScreen in listOf(Screen.Home, Screen.Dashboard, Screen.Profile)) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = (currentScreen == Screen.Home),
                        onClick = { currentScreen = Screen.Home }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        label = { Text("Dashboard") },
                        selected = (currentScreen == Screen.Dashboard),
                        onClick = { currentScreen = Screen.Dashboard }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile") },
                        selected = (currentScreen == Screen.Profile),
                        onClick = { currentScreen = Screen.Profile }
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentScreen == Screen.Home) {
                FloatingActionButton(
                    onClick = { showAddDirectoryDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.CreateNewFolder, contentDescription = "Add Directory")
                }
            } else if (currentScreen == Screen.Messages) {
                FloatingActionButton(
                    onClick = { /* Open Compose Dialog */ },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Compose")
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                when (screen) {
                    Screen.Home -> DirectoryList(
                        directories = directories,
                        searchQuery = searchQuery,
                        sortAscending = sortAscending,
                        onSearchChange = { searchQuery = it },
                        onDirectoryClick = { 
                            selectedDirectory = it
                            currentScreen = Screen.Messages
                        },
                        onDeleteDirectory = { dirId ->
                            directories.removeIf { it.id == dirId }
                        }
                    )
                    Screen.Messages -> MessageList(
                        directory = selectedDirectory,
                        onDeleteMessage = { msgId ->
                            selectedDirectory?.messages?.removeIf { it.id == msgId }
                        }
                    )
                    Screen.Dashboard -> DashboardScreen(directories)
                    Screen.Profile -> ProfileScreen(
                        name = userName,
                        email = userEmail,
                        onEditProfile = { currentScreen = Screen.EditProfile },
                        onNavigateToSettings = { currentScreen = Screen.Settings },
                        onNavigateToNotifications = { currentScreen = Screen.Notifications }
                    )
                    Screen.EditProfile -> EditProfileScreen(
                        currentName = userName,
                        currentEmail = userEmail,
                        onSave = { name, email ->
                            userName = name
                            userEmail = email
                            currentScreen = Screen.Profile
                        },
                        onCancel = { currentScreen = Screen.Profile }
                    )
                    Screen.Settings -> SettingsScreen(
                        darkMode = darkMode,
                        onDarkModeChange = onDarkModeToggle
                    )
                    Screen.Notifications -> NotificationsScreen(
                        enabled = notificationsEnabled,
                        onToggle = { notificationsEnabled = it }
                    )
                }
            }
        }
    }

    if (showAddDirectoryDialog) {
        AddDirectoryDialog(
            onDismiss = { showAddDirectoryDialog = false },
            onConfirm = { name ->
                directories.add(
                    Directory(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        icon = Icons.Default.Folder,
                        colors = listOf(Color.Gray, Color.DarkGray),
                        messages = mutableListOf()
                    )
                )
                showAddDirectoryDialog = false
            }
        )
    }
}

@Composable
fun AddDirectoryDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Directory") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Directory Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onConfirm(name) }) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DirectoryList(
    directories: List<Directory>,
    searchQuery: String,
    sortAscending: Boolean,
    onSearchChange: (String) -> Unit,
    onDirectoryClick: (Directory) -> Unit,
    onDeleteDirectory: (String) -> Unit
) {
    val filteredDirectories = remember(searchQuery, directories, sortAscending) {
        val filtered = if (searchQuery.isEmpty()) directories
        else directories.filter { it.name.contains(searchQuery, ignoreCase = true) }
        
        if (sortAscending) filtered.sortedBy { it.name }
        else filtered.sortedByDescending { it.name }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Search directories...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            shape = RoundedCornerShape(24.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true
        )

        if (filteredDirectories.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No directories found", color = Color.Gray)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredDirectories, key = { it.id }) { directory ->
                    DirectoryItem(directory, onDirectoryClick, onDeleteDirectory)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DirectoryItem(directory: Directory, onClick: (Directory) -> Unit, onDelete: (String) -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    var showDeleteMenu by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isPressed) 0.92f else 1f, label = "Scale")

    Box {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scale)
                .combinedClickable(
                    onClick = { onClick(directory) },
                    onLongClick = { showDeleteMenu = true }
                )
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(110.dp)
                    .shadow(elevation = 12.dp, shape = CircleShape)
                    .background(
                        brush = Brush.verticalGradient(directory.colors),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = directory.icon,
                    contentDescription = directory.name,
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = directory.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = directory.colors.last(),
                letterSpacing = 0.5.sp
            )
        }

        DropdownMenu(expanded = showDeleteMenu, onDismissRequest = { showDeleteMenu = false }) {
            DropdownMenuItem(
                text = { Text("Delete") },
                onClick = {
                    onDelete(directory.id)
                    showDeleteMenu = false
                },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
            )
        }
    }
}

@Composable
fun MessageList(directory: Directory?, onDeleteMessage: (String) -> Unit) {
    val messages = directory?.messages ?: emptyList()
    
    if (messages.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No messages in this directory.", color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                MessageItem(message, onDeleteMessage)
            }
        }
    }
}

@Composable
fun MessageItem(message: Message, onDeleteMessage: (String) -> Unit) {
    var showDelete by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDelete = !showDelete }
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = message.sender.take(1),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = message.sender,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Text(
                    text = message.time,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (showDelete) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(
                        onClick = { onDeleteMessage(message.id) },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete")
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(directories: List<Directory>) {
    val totalMessages = directories.sumOf { it.messages.size }
    val recentMessages = remember(directories) {
        directories.flatMap { it.messages }.sortedByDescending { it.timestamp }.take(5)
    }
    
    var dashboardSearchQuery by remember { mutableStateOf("") }
    val globalSearchResults = remember(dashboardSearchQuery, directories) {
        if (dashboardSearchQuery.isBlank()) emptyList()
        else directories.flatMap { dir -> 
            dir.messages.filter { it.content.contains(dashboardSearchQuery, ignoreCase = true) || it.sender.contains(dashboardSearchQuery, ignoreCase = true) }
                .map { dir.name to it }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Analytics",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black
            )
        }

        // --- NEW: Quick Folders (Horizontal Scroll) ---
        item {
            Text("Quick Access", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(directories) { dir ->
                    Surface(
                        modifier = Modifier
                            .width(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { /* Could navigate to Messages directly if we had a navigation callback */ },
                        color = dir.colors.first().copy(alpha = 0.1f)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Icon(dir.icon, contentDescription = null, tint = dir.colors.first(), modifier = Modifier.size(24.dp))
                            Text(dir.name, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }
                    }
                }
            }
        }

        // --- Global Search ---
        item {
            OutlinedTextField(
                value = dashboardSearchQuery,
                onValueChange = { dashboardSearchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search all messages...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (dashboardSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { dashboardSearchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )
        }

        if (dashboardSearchQuery.isNotEmpty()) {
            item {
                Text("Search Results (${globalSearchResults.size})", fontWeight = FontWeight.Bold)
            }
            if (globalSearchResults.isEmpty()) {
                item { Text("No messages match your search.", color = Color.Gray) }
            } else {
                items(globalSearchResults) { (dirName, msg) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "In $dirName", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Text(text = "${msg.sender}: ${msg.content}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }
        }
        
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    label = "Total Folders",
                    value = directories.size.toString(),
                    icon = Icons.Default.Folder,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Messages",
                    value = totalMessages.toString(),
                    icon = Icons.AutoMirrored.Filled.Message,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // --- NEW: Weekly Activity Chart (Dummy) ---
        item {
            Text("Weekly Activity", fontWeight = FontWeight.Bold)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(100.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val days = listOf("M", "T", "W", "T", "F", "S", "S")
                    val values = listOf(0.4f, 0.7f, 0.5f, 0.9f, 0.6f, 0.3f, 0.4f)
                    days.forEachIndexed { index, day ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .width(12.dp)
                                    .fillMaxHeight(values[index])
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(day, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        item {
            Text("Recent Messages", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
        }

        if (recentMessages.isEmpty()) {
            item {
                Text("No recent messages.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            items(recentMessages) { msg ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("${msg.sender}: ${msg.content.take(30)}...", style = MaterialTheme.typography.bodySmall)
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cloud Storage", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { 0.45f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("4.5 GB of 10 GB used (45%)", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        
        item {
            Text("Folder Distribution", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
        }
        
        items(directories) { dir ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(12.dp).background(dir.colors.first(), CircleShape))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(dir.name, modifier = Modifier.weight(1f))
                    Text("${dir.messages.size} msgs", fontWeight = FontWeight.Medium)
                }
                val ratio = if (totalMessages == 0) 0f else dir.messages.size.toFloat() / totalMessages
                LinearProgressIndicator(
                    progress = { ratio },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                    color = dir.colors.first(),
                    trackColor = dir.colors.first().copy(alpha = 0.1f)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = value, fontSize = 28.sp, fontWeight = FontWeight.Black, color = color)
            Text(text = label, fontSize = 12.sp, color = color.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun ProfileScreen(
    name: String,
    email: String,
    onEditProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
        Text(email, color = Color.Gray)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onEditProfile,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Edit My Profile")
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                ProfileOption(Icons.Default.Settings, "Settings", onNavigateToSettings)
                ProfileOption(Icons.Default.Notifications, "Notifications", onNavigateToNotifications)
                ProfileOption(Icons.Default.Security, "Privacy & Security") {}
                ProfileOption(Icons.AutoMirrored.Filled.Help, "Help & Support") {}
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        TextButton(
            onClick = { /* Logout */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Sign Out", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun EditProfileScreen(
    currentName: String,
    currentEmail: String,
    onSave: (String, String) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var email by remember { mutableStateOf(currentEmail) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) }
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onSave(name, email) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            Text("Save Changes")
        }
        TextButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel")
        }
    }
}

@Composable
fun SettingsScreen(darkMode: Boolean, onDarkModeChange: (Boolean) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SettingsToggle(
            title = "Dark Appearance",
            subtitle = "Enable night theme across the app",
            checked = darkMode,
            onCheckedChange = onDarkModeChange,
            icon = Icons.Default.DarkMode
        )
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        
        Text("Account Settings", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        ProfileOption(Icons.Default.Language, "Language") {}
        ProfileOption(Icons.Default.CloudUpload, "Cloud Backup") {}
        
        Spacer(modifier = Modifier.weight(1f))
        Text("Version 1.0.8 (Stable)", modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun SettingsToggle(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, fontSize = 12.sp, color = Color.Gray)
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun NotificationsScreen(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SettingsToggle(
            title = "Push Notifications",
            subtitle = "Get alerted when new messages arrive",
            checked = enabled,
            onCheckedChange = onToggle,
            icon = Icons.Default.NotificationsActive
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
        ) {
            Text(
                "Note: System-level notifications must also be enabled in your device settings.",
                modifier = Modifier.padding(16.dp),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun ProfileOption(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
    }
}
