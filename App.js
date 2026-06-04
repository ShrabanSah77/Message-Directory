import React, { useState } from 'react';
import {
  SafeAreaView,
  StyleSheet,
  Text,
  View,
  FlatList,
  TouchableOpacity,
  TextInput,
  StatusBar,
} from 'react-native';

// --- Mock Data ---
const INITIAL_DIRECTORIES = [
  {
    id: '1', name: 'Private', icon: '👤', color: '#FF5722', category: 'Security',
    messages: [
      { id: 'm1', sender: 'System', content: 'Encryption protocols fully active.', time: '10:00 AM' }
    ]
  },
  {
    id: '2', name: 'Home Base', icon: '🏠', color: '#4FC3F7', category: 'Living',
    messages: [
      { id: 'm2', sender: 'Mom', content: 'Dinner is ready at 7.', time: '6:30 PM' }
    ]
  },
  {
    id: '3', name: 'Work Core', icon: '💼', color: '#9575CD', category: 'Professional',
    messages: [
      { id: 'm3', sender: 'Lead', content: 'Submit the report by EOD.', time: '9:15 AM' }
    ]
  },
  {
    id: '4', name: 'Social', icon: '👥', color: '#F06292', category: 'Social',
    messages: []
  },
  {
    id: '5', name: 'Archive', icon: '📂', color: '#AED581', category: 'System',
    messages: [
      { id: 'm4', sender: 'Storage', content: 'Backup completed successfully.', time: 'Last Week' }
    ]
  },
  {
    id: '6', name: 'Alerts', icon: '🔔', color: '#FF8A65', category: 'Security',
    messages: [
      { id: 'm5', sender: 'Security', content: 'New login from unknown device.', time: 'Yesterday' }
    ]
  },
];

const App = () => {
  const [currentScreen, setCurrentScreen] = useState('Home'); // 'Home' or 'Messages'
  const [selectedDir, setSelectedDir] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');

  // --- Navigation Logic ---
  const handleDirClick = (dir) => {
    setSelectedDir(dir);
    setCurrentScreen('Messages');
  };

  const filteredDirs = INITIAL_DIRECTORIES.filter(dir =>
    dir.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
    dir.category.toLowerCase().includes(searchQuery.toLowerCase())
  );

  // --- Components ---
  const DirectoryCard = ({ item }) => (
    <TouchableOpacity
      style={[styles.card, { borderColor: item.color + '40' }]}
      onPress={() => handleDirClick(item)}
    >
      <View style={[styles.iconCircle, { backgroundColor: item.color }]}>
        <Text style={styles.iconText}>{item.icon}</Text>
      </View>
      <Text style={styles.dirName}>{item.name}</Text>
      <Text style={styles.dirCategory}>{item.category.toUpperCase()}</Text>
      <Text style={styles.dirCount}>{item.messages.length} LOGS</Text>
    </TouchableOpacity>
  );

  const MessageItem = ({ item }) => (
    <View style={styles.messageBubble}>
      <View style={styles.messageHeader}>
        <Text style={styles.messageSender}>{item.sender}</Text>
        <Text style={styles.messageTime}>{item.time}</Text>
      </View>
      <Text style={styles.messageContent}>{item.content}</Text>
    </View>
  );

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="dark-content" />

      {/* Top Bar */}
      <View style={styles.topBar}>
        {currentScreen === 'Messages' && (
          <TouchableOpacity onPress={() => setCurrentScreen('Home')}>
            <Text style={styles.backButton}>← BACK</Text>
          </TouchableOpacity>
        )}
        <Text style={styles.topBarTitle}>
          {currentScreen === 'Home' ? 'COMMAND CENTER' : selectedDir?.name.toUpperCase()}
        </Text>
        <View style={{ width: 40 }} />
      </View>

      {currentScreen === 'Home' ? (
        /* --- HOME SCREEN --- */
        <View style={styles.content}>
          <TextInput
            style={styles.searchBar}
            placeholder="Search Encrypted Nodes..."
            placeholderTextColor="#999"
            value={searchQuery}
            onChangeText={setSearchQuery}
          />

          <Text style={styles.sectionHeader}>ACTIVE NODES ({filteredDirs.length})</Text>
          <FlatList
            data={filteredDirs}
            renderItem={DirectoryCard}
            keyExtractor={item => item.id}
            numColumns={2}
            contentContainerStyle={styles.listPadding}
            ListEmptyComponent={
              <Text style={styles.emptyText}>No nodes found in this sector.</Text>
            }
          />
        </View>
      ) : (
        /* --- MESSAGES SCREEN --- */
        <View style={styles.content}>
          <View style={[styles.headerBanner, { backgroundColor: selectedDir?.color }]}>
            <Text style={styles.bannerTitle}>{selectedDir?.name}</Text>
            <Text style={styles.bannerSubtitle}>{selectedDir?.messages.length} SECURE ENTRIES FOUND</Text>
          </View>

          <FlatList
            data={selectedDir?.messages}
            renderItem={MessageItem}
            keyExtractor={item => item.id}
            ListEmptyComponent={
              <Text style={styles.emptyText}>No encrypted logs detected in this node.</Text>
            }
            contentContainerStyle={styles.listPadding}
          />
        </View>
      )}
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#F0F2F5' },
  topBar: {
    height: 60,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 16,
    backgroundColor: '#FFF',
    borderBottomWidth: 1,
    borderBottomColor: '#EEE',
    elevation: 2,
  },
  topBarTitle: { fontSize: 16, fontWeight: '900', letterSpacing: 1.5, color: '#1A1A1B' },
  backButton: { color: '#007AFF', fontWeight: '900', fontSize: 12 },
  content: { flex: 1, padding: 16 },
  searchBar: {
    backgroundColor: '#FFF',
    padding: 14,
    borderRadius: 16,
    borderWidth: 1,
    borderColor: '#DCDFE4',
    marginBottom: 24,
    color: '#333',
    fontWeight: '500',
  },
  sectionHeader: { fontWeight: '900', color: '#007AFF', marginBottom: 16, fontSize: 11, letterSpacing: 1 },
  card: {
    flex: 1,
    backgroundColor: '#FFF',
    margin: 8,
    padding: 20,
    borderRadius: 28,
    alignItems: 'center',
    borderWidth: 1,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.05,
    shadowRadius: 10,
    elevation: 3,
  },
  iconCircle: { width: 64, height: 64, borderRadius: 32, justifyContent: 'center', alignItems: 'center', marginBottom: 12 },
  iconText: { fontSize: 28 },
  dirName: { fontWeight: '800', fontSize: 16, color: '#1C1E21' },
  dirCategory: { fontSize: 10, color: '#007AFF', marginTop: 4, fontWeight: '700' },
  dirCount: { fontSize: 10, color: '#8D949E', marginTop: 4, fontWeight: '600' },
  headerBanner: { padding: 24, borderRadius: 24, marginBottom: 24 },
  bannerTitle: { color: '#FFF', fontSize: 26, fontWeight: '900' },
  bannerSubtitle: { color: '#FFF', opacity: 0.9, fontSize: 11, fontWeight: '700', marginTop: 4 },
  messageBubble: {
    backgroundColor: '#FFF',
    padding: 18,
    borderRadius: 20,
    borderTopLeftRadius: 0,
    marginBottom: 16,
    borderWidth: 1,
    borderColor: '#E9EBEE',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.02,
    shadowRadius: 5,
    elevation: 1,
  },
  messageHeader: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: 6 },
  messageSender: { fontWeight: '900', color: '#007AFF', fontSize: 12 },
  messageTime: { fontSize: 10, color: '#B0B3B8', fontWeight: '600' },
  messageContent: { fontSize: 15, color: '#1C1E21', lineHeight: 20 },
  emptyText: { textAlign: 'center', marginTop: 60, color: '#8D949E', fontWeight: '600' },
  listPadding: { paddingBottom: 30 }
});

export default App;
