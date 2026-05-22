package com.example.message_directory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.message_directory.ui.theme.MessageDirectoryTheme

// Data Models
data class Message(val id: String, val sender: String, val content: String, val time: String)
data class Directory(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val messages: List<Message>
)

enum class Screen {
    Home, Dashboard, Profile, Messages, Settings, Notifications, EditProfile
}

// Mock Data
val DIRECTORIES = listOf(
    Directory("1", "You", Icons.Default.Person, Color(0xFFFF5722), listOf(
        Message("m1", "System", "Welcome to your personal directory!", "10:00 AM")
    )),
    Directory("2", "Home", Icons.Default.Home, Color(0xFF81D4FA), listOf(
        Message("m2", "Mom", "The dinner is ready.", "6:30 PM")
    )),
    Directory("3", "Love", Icons.Default.Favorite, Color(0xFF2196F3), listOf(
        Message("m3", "Partner", "Love you!", "Yesterday")
    )),
    Directory("4", "Family", Icons.Default.FamilyRestroom, Color(0xFF673AB7), listOf(
        Message("m4", "Dad", "Call me when you're free.", "2 days ago")
    )),
    Directory("5", "Friends", Icons.Default.Groups, Color(0xFFE91E63), listOf(
        Message("m5", "John", "Are we still on for Friday?", "3 days ago")
    )),
    Directory("6", "School", Icons.Default.School, Color(0xFF00BCD4), listOf(
        Message("m6", "Professor", "The assignment deadline is extended.", "Last week")
    ))
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MessageDirectoryTheme {
                MessageDirectoryApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageDirectoryApp() {
    var currentScreen by remember { mutableStateOf(Screen.Home) }
    var selectedDirectory by remember { mutableStateOf<Directory?>(null) }
    
    // User Profile State
    var userName by remember { mutableStateOf("Alex Johnson") }
    var userEmail by remember { mutableStateOf("alex.johnson@example.com") }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var darkModeEnabled by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = when (currentScreen) {
                            Screen.Messages -> selectedDirectory?.name ?: "Messages"
                            Screen.Settings -> "Settings"
                            Screen.Notifications -> "Notifications"
                            Screen.EditProfile -> "Edit Profile"
                            else -> currentScreen.name
                        },
                        fontWeight = FontWeight.Bold
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            if (currentScreen == Screen.Home || currentScreen == Screen.Dashboard || currentScreen == Screen.Profile) {
                NavigationBar {
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
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (currentScreen) {
                Screen.Home -> DirectoryGrid { 
                    selectedDirectory = it
                    currentScreen = Screen.Messages
                }
                Screen.Messages -> MessageList(messages = selectedDirectory?.messages ?: emptyList())
                Screen.Dashboard -> DashboardScreen()
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
                    darkMode = darkModeEnabled,
                    onDarkModeChange = { darkModeEnabled = it }
                )
                Screen.Notifications -> NotificationsScreen(
                    enabled = notificationsEnabled,
                    onToggle = { notificationsEnabled = it }
                )
            }
        }
    }
}

@Composable
fun DirectoryGrid(onDirectoryClick: (Directory) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(DIRECTORIES) { directory ->
            DirectoryItem(directory, onDirectoryClick)
        }
    }
}

@Composable
fun DirectoryItem(directory: Directory, onClick: (Directory) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick(directory) }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(100.dp)
                .shadow(elevation = 8.dp, shape = CircleShape)
                .background(directory.color, CircleShape)
        ) {
            Icon(
                imageVector = directory.icon,
                contentDescription = directory.name,
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = directory.name,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = directory.color
        )
    }
}

@Composable
fun MessageList(messages: List<Message>) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(messages) { message ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = message.sender,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = message.time,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = message.content, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun DashboardScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Activity Dashboard", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard("Total Messages", "24", Modifier.weight(1f))
            StatCard("New Today", "5", Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Storage Usage", fontWeight = FontWeight.Bold)
                LinearProgressIndicator(
                    progress = { 0.45f },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                )
                Text("4.5 GB of 10 GB used", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 12.sp, color = Color.Gray)
            Text(text = value, fontSize = 28.sp, fontWeight = FontWeight.Bold)
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
                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(64.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(email, color = Color.Gray)
        
        Button(
            onClick = onEditProfile,
            modifier = Modifier.padding(top = 8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Edit Profile")
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        ProfileOption(Icons.Default.Settings, "Settings", onNavigateToSettings)
        ProfileOption(Icons.Default.Notifications, "Notifications", onNavigateToNotifications)
        ProfileOption(Icons.Default.Security, "Privacy & Security") {}
        ProfileOption(Icons.AutoMirrored.Filled.Help, "Help & Support") {}
        
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = { /* Logout */ },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout")
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
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { onSave(name, email) },
            modifier = Modifier.fillMaxWidth()
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
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Dark Mode", fontSize = 18.sp)
            Switch(checked = darkMode, onCheckedChange = onDarkModeChange)
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        Text("Language", fontSize = 18.sp)
        Text("English (United States)", fontSize = 14.sp, color = Color.Gray)
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        Text("Version", fontSize = 18.sp)
        Text("1.0.4", fontSize = 14.sp, color = Color.Gray)
    }
}

@Composable
fun NotificationsScreen(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Push Notifications", fontSize = 18.sp)
            Switch(checked = enabled, onCheckedChange = onToggle)
        }
        Text("Receive updates about new messages and activity", fontSize = 14.sp, color = Color.Gray)
    }
}

@Composable
fun ProfileOption(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
    }
}
