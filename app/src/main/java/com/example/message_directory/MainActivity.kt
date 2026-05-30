package com.example.message_directory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.message_directory.ui.theme.MessageDirectoryTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

// --- Models ---
data class Message(
    val id: String = UUID.randomUUID().toString(),
    val sender: String,
    val content: String,
    val time: String = SimpleDateFormat("HH:mm a", Locale.getDefault()).format(Date()),
    val timestamp: Long = System.currentTimeMillis(),
    val type: MessageType = MessageType.TEXT
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
            createDirectory("1", "You", Icons.Default.Person, listOf(Color(0xFFFF5722), Color(0xFFFF9800)), "Private", true).apply {
                messages.add(Message(sender = "System", content = "Protocol Alpha initialized.", type = MessageType.SYSTEM))
            },
            createDirectory("2", "Home", Icons.Default.Home, listOf(Color(0xFF81D4FA), Color(0xFF03A9F4)), "Family"),
            createDirectory("3", "Love", Icons.Default.Favorite, listOf(Color(0xFFF48FB1), Color(0xFFE91E63)), "Social"),
            createDirectory("4", "Family", Icons.Default.FamilyRestroom, listOf(Color(0xFFB39DDB), Color(0xFF673AB7)), "Family", true),
            createDirectory("5", "Friends", Icons.Default.Groups, listOf(Color(0xFFA5D6A7), Color(0xFF4CAF50)), "Social"),
            createDirectory("6", "School", Icons.Default.School, listOf(Color(0xFF80DEEA), Color(0xFF00BCD4)), "Professional")
        )
    }
    
    var showAddDirDialog by remember { mutableStateOf(false) }
    var showAddMsgDialog by remember { mutableStateOf(false) }
    var homeSearchQuery by remember { mutableStateOf("") }
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
            Crossfade(targetState = currentScreen, label = "Screen") { screen ->
                when (screen) {
                    Screen.Home -> HomeScreenGrid(directories, homeSearchQuery, { homeSearchQuery = it }, { selectedDirectory = it; currentScreen = Screen.Messages }, { id ->
                        val index = directories.indexOfFirst { it.id == id }
                        if (index != -1) directories[index] = directories[index].copy(isPinned = !directories[index].isPinned)
                    }, { id -> directories.removeIf { it.id == id } })
                    Screen.Dashboard -> ProDashboard(directories) { currentScreen = Screen.AnalyticsDetails }
                    Screen.Messages -> MessageDetailsScreen(selectedDirectory, msgSearchQuery, { msgSearchQuery = it }, { msgId -> selectedDirectory?.messages?.removeIf { it.id == msgId } })
                    Screen.Profile -> ProProfile(userName, userEmail, securityLevel) { currentScreen = it }
                    Screen.EditProfile -> EditProfileScreen(userName, userEmail, { n, e -> userName = n; userEmail = e; currentScreen = Screen.Profile }, { currentScreen = Screen.Profile })
                    Screen.Settings -> SettingsScreen(darkMode, onDarkModeToggle)
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

// --- UI Components ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NexusTopBar(currentScreen: Screen, selectedDirectory: Directory?, onBack: () -> Unit) {
    TopAppBar(
        title = { Text(when (currentScreen) { Screen.Messages -> selectedDirectory?.name ?: "NODE"; Screen.AnalyticsDetails -> "DETAILED INTEL"; Screen.Security -> "SECURITY ARCH"; Screen.Settings -> "SYSTEM CONFIG"; else -> currentScreen.name.uppercase() }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, letterSpacing = 2.sp) },
        navigationIcon = { if (currentScreen !in listOf(Screen.Home, Screen.Dashboard, Screen.Profile)) IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
        actions = { IconButton(onClick = {}) { Icon(Icons.Default.CloudSync, "Sync") } }
    )
}

@Composable
fun NexusBottomBar(currentScreen: Screen, onNavigate: (Screen) -> Unit) {
    NavigationBar {
        listOf(Screen.Home to Icons.Default.Explore, Screen.Dashboard to Icons.Default.Insights, Screen.Profile to Icons.Default.AccountCircle).forEach { (screen, icon) ->
            NavigationBarItem(selected = currentScreen == screen, onClick = { onNavigate(screen) }, icon = { Icon(icon, null) }, label = { Text(screen.name.uppercase(), style = MaterialTheme.typography.labelSmall) })
        }
    }
}

@Composable
fun NexusFAB(currentScreen: Screen, onClick: () -> Unit) {
    if (currentScreen in listOf(Screen.Home, Screen.Messages)) {
        FloatingActionButton(onClick = onClick, containerColor = MaterialTheme.colorScheme.primary) {
            Icon(if (currentScreen == Screen.Home) Icons.Default.CreateNewFolder else Icons.Default.AddComment, null)
        }
    }
}

@Composable
fun HomeScreenGrid(dirs: List<Directory>, query: String, onSearch: (String) -> Unit, onClick: (Directory) -> Unit, onPin: (String) -> Unit, onDelete: (String) -> Unit) {
    val pinned = dirs.filter { it.isPinned }
    val filteredOthers = dirs.filter { !it.isPinned && it.name.contains(query, true) }
    LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 80.dp)) {
        item(span = { GridItemSpan(2) }) { OutlinedTextField(value = query, onValueChange = onSearch, modifier = Modifier.fillMaxWidth().padding(16.dp), placeholder = { Text("Search Nodes...") }, leadingIcon = { Icon(Icons.Default.Search, null) }, shape = RoundedCornerShape(12.dp), singleLine = true) }
        if (pinned.isNotEmpty() && query.isEmpty()) {
            item(span = { GridItemSpan(2) }) { SectionHeader("PINNED PROTOCOLS") }
            item(span = { GridItemSpan(2) }) { LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) { items(pinned) { dir -> DirectoryCircleItem(dir, onClick, onPin, onDelete) } } }
        }
        item(span = { GridItemSpan(2) }) { SectionHeader("ACTIVE NODES") }
        if (filteredOthers.isEmpty()) { item(span = { GridItemSpan(2) }) { EmptyState() } }
        else { items(filteredOthers) { dir -> Box(Modifier.padding(16.dp), contentAlignment = Alignment.Center) { DirectoryCircleItem(dir, onClick, onPin, onDelete) } } }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DirectoryCircleItem(dir: Directory, onClick: (Directory) -> Unit, onPin: (String) -> Unit, onDelete: (String) -> Unit) {
    var menu by remember { mutableStateOf(false) }
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.combinedClickable(onClick = { onClick(dir) }, onLongClick = { menu = true })) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp).shadow(8.dp, CircleShape).background(Brush.verticalGradient(dir.colors), CircleShape)) {
            Icon(dir.icon, null, tint = Color.White, modifier = Modifier.size(40.dp))
            if (dir.isPinned) Icon(Icons.Default.PushPin, null, tint = Color.White, modifier = Modifier.size(16.dp).align(Alignment.TopEnd).padding(8.dp))
        }
        Spacer(Modifier.height(12.dp)); Text(dir.name, fontWeight = FontWeight.Bold, color = dir.colors.last(), fontSize = 16.sp); Text("${dir.messages.size} Logs", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
            DropdownMenuItem(text = { Text(if(dir.isPinned) "Unpin Node" else "Pin Node") }, onClick = { onPin(dir.id); menu = false }, leadingIcon = { Icon(Icons.Default.PushPin, null) })
            DropdownMenuItem(text = { Text("Delete Node", color = Color.Red) }, onClick = { onDelete(dir.id); menu = false }, leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) })
        }
    }
}

@Composable
fun ProDashboard(dirs: List<Directory>, onDetailed: () -> Unit) {
    val total = dirs.sumOf { it.messages.size }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        item { Text("INTEL CENTER", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black); Spacer(Modifier.height(24.dp)) }
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) { IntelStatCard("NODES", dirs.size.toString(), Icons.Default.Hub, MaterialTheme.colorScheme.primary, Modifier.weight(1f)); IntelStatCard("LOGS", total.toString(), Icons.Default.Description, MaterialTheme.colorScheme.secondary, Modifier.weight(1f)) } }
        item { Spacer(Modifier.height(24.dp)); Button(onClick = onDetailed, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp)) { Icon(Icons.Default.QueryStats, null); Spacer(Modifier.width(12.dp)); Text("ANALYZE GLOBAL METRICS", fontWeight = FontWeight.Bold) } }
        item { SectionHeader("RESOURCE ALLOCATION") }
        items(dirs) { dir ->
            val p = if(total == 0) 0f else dir.messages.size.toFloat() / total
            Column(Modifier.padding(vertical = 8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(dir.name, fontWeight = FontWeight.Bold); Text("${(p * 100).toInt()}%") }
                LinearProgressIndicator(progress = { p.coerceIn(0.01f, 1f) }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape), color = dir.colors.first())
            }
        }
    }
}

@Composable
fun IntelStatCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = color.copy(0.05f)), border = BorderStroke(1.dp, color.copy(0.1f))) {
        Column(Modifier.padding(16.dp)) { Icon(icon, null, tint = color, modifier = Modifier.size(20.dp)); Spacer(Modifier.height(12.dp)); Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = color); Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray) }
    }
}

@Composable
fun AnalyticsDetailsScreen(dirs: List<Directory>) {
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("DETAILED NODE METRICS", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black); Spacer(Modifier.height(16.dp))
        dirs.forEach { dir ->
            Card(Modifier.fillMaxWidth().padding(vertical = 8.dp), shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Surface(modifier = Modifier.size(8.dp), shape = CircleShape, color = dir.colors[0]) {}; Spacer(Modifier.width(12.dp)); Text(dir.name, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium) }
                    Spacer(Modifier.height(8.dp)); Text("Category: ${dir.category}", style = MaterialTheme.typography.bodySmall, color = Color.Gray); Text("Last Updated: Just now", style = MaterialTheme.typography.labelSmall); HorizontalDivider(Modifier.padding(vertical = 12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("ENTRIES", style = MaterialTheme.typography.labelSmall); Text("${dir.messages.size}", fontWeight = FontWeight.Bold) }; Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("STATUS", style = MaterialTheme.typography.labelSmall); Text("SYNCED", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold) } }
                }
            }
        }
    }
}

@Composable
fun MessageDetailsScreen(dir: Directory?, query: String, onSearch: (String) -> Unit, onDelete: (String) -> Unit) {
    if (dir == null) return
    val filtered = dir.messages.filter { it.content.contains(query, true) }
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxWidth().background(Brush.linearGradient(dir.colors)).padding(24.dp)) { Column { Text(dir.name.uppercase(), color = Color.White, fontWeight = FontWeight.Black, style = MaterialTheme.typography.headlineMedium); Text("ENCRYPTED NODE • ${dir.messages.size} ENTRIES", color = Color.White.copy(0.7f), style = MaterialTheme.typography.labelSmall) } }
        TextField(value = query, onValueChange = onSearch, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Filter logs in ${dir.name}...") }, leadingIcon = { Icon(Icons.Default.FilterList, null) }, colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent))
        LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { items(filtered) { msg -> MessageItemPro(msg, onDelete) }; item { Spacer(Modifier.height(80.dp)) } }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageItemPro(msg: Message, onDelete: (String) -> Unit) {
    var menu by remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth().combinedClickable(onClick = {}, onLongClick = { menu = true }), shape = RoundedCornerShape(topStart = 0.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp), colors = CardDefaults.cardColors(containerColor = if(msg.type == MessageType.SYSTEM) Color.Black.copy(0.03f) else MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(0.5f))) {
        Column(Modifier.padding(16.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Text(msg.sender, fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary); Spacer(Modifier.weight(1f)); Text(msg.time, style = MaterialTheme.typography.labelSmall, color = Color.Gray) }; Spacer(Modifier.height(4.dp)); Text(msg.content, style = MaterialTheme.typography.bodyMedium) }
        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) { DropdownMenuItem(text = { Text("Purge Log", color = Color.Red) }, onClick = { onDelete(msg.id); menu = false }) }
    }
}

@Composable
fun ProProfile(name: String, email: String, security: String, onNav: (Screen) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(Modifier.fillMaxWidth().height(200.dp).background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)))) {
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) { Surface(Modifier.size(100.dp), shape = CircleShape, color = Color.White.copy(0.2f), border = BorderStroke(2.dp, Color.White)) { Icon(Icons.Default.Face, null, Modifier.size(60.dp).padding(16.dp), tint = Color.White) }; Spacer(Modifier.height(12.dp)); Text(name, color = Color.White, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge); Text(email, color = Color.White.copy(0.7f), style = MaterialTheme.typography.labelSmall) }
        }
        Column(Modifier.padding(16.dp)) { ProfileItem(Icons.Default.Badge, "Modify Credentials") { onNav(Screen.EditProfile) }; ProfileItem(Icons.Default.VerifiedUser, "Security Level: $security") { onNav(Screen.Security) }; ProfileItem(Icons.Default.SettingsInputComponent, "System Settings") { onNav(Screen.Settings) }; ProfileItem(Icons.Default.NotificationsActive, "Alert Protocol") { onNav(Screen.Notifications) }; Spacer(Modifier.height(32.dp)); OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)) { Text("TERMINATE SESSION", fontWeight = FontWeight.Bold) } }
    }
}

@Composable
fun ProfileItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    ListItem(modifier = Modifier.clickable(onClick = onClick), headlineContent = { Text(label) }, leadingContent = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) }, trailingContent = { Icon(Icons.Default.ChevronRight, null) })
}

@Composable fun SectionHeader(t: String) = Text(t, modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
@Composable fun AddMessageDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("INITIALIZE LOG") }, text = { OutlinedTextField(value = text, onValueChange = { text = it }, modifier = Modifier.fillMaxWidth()) }, confirmButton = { Button(onClick = { if(text.isNotBlank()) onConfirm(text) }) { Text("TRANSMIT") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("ABORT") } })
}
@Composable fun AddDirectoryDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var cat by remember { mutableStateOf("Work") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("PROVISION NODE") }, text = { Column { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Node Name") }); Row { listOf("Social", "Work").forEach { c -> FilterChip(selected = cat == c, onClick = { cat = c }, label = { Text(c) }) } } } }, confirmButton = { Button(onClick = { if(name.isNotBlank()) onConfirm(name, cat) }) { Text("INIT") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("CANCEL") } })
}
@Composable fun SettingsScreen(darkMode: Boolean, onToggle: (Boolean) -> Unit) = Column(Modifier.padding(24.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("DARK MODE PROTOCOL", fontWeight = FontWeight.Bold); Switch(darkMode, onToggle) }; HorizontalDivider() }
@Composable fun NotificationsScreen() = Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("NO ALERTS") }
@Composable fun SecurityScreen(curr: String, onLevel: (String) -> Unit) = Column(Modifier.padding(24.dp)) { Text("ENCRYPTION TIERS (Current: $curr)", fontWeight = FontWeight.Bold); listOf("Standard", "Advanced", "Elite").forEach { l -> Button(onClick = { onLevel(l) }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) { Text(l) } } }
@Composable fun EditProfileScreen(n: String, e: String, onS: (String, String) -> Unit, onC: () -> Unit) {
    var name by remember { mutableStateOf(n) }
    var email by remember { mutableStateOf(e) }
    Column(Modifier.padding(24.dp)) { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }); OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }); Button(onClick = { onS(name, email) }) { Text("Save") }; TextButton(onClick = onC) { Text("Cancel") } }
}
@Composable fun EmptyState() = Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("EMPTY SECTOR") }
