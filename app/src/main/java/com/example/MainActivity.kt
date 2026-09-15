package com.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGreenBg
import com.example.ui.theme.StatusGrey
import com.example.ui.theme.StatusGreyBg
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusRedBg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        HidRunnerApp()
      }
    }
  }
}

// Root permission state representation
sealed class RootStatus {
  object Checking : RootStatus()
  object Granted : RootStatus()
  object Denied : RootStatus()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HidRunnerApp() {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }

  // App settings & disclaimer
  val prefs = remember { context.getSharedPreferences("hid_runner_prefs", Context.MODE_PRIVATE) }
  var showFirstRunDialog by remember {
    mutableStateOf(!prefs.getBoolean("disclaimer_accepted", false))
  }

  // Active Tab: 0 = Runner/Editor, 1 = Demo & Prank Scripts
  var selectedTabIndex by remember { mutableIntStateOf(0) }

  // Root state
  var rootStatus by remember { mutableStateOf<RootStatus>(RootStatus.Checking) }

  // HID Node selection with status
  var hidNodesWithStatus by remember { mutableStateOf<List<HidNodeStatus>>(emptyList()) }
  var selectedNode by remember { mutableStateOf("hidg1") }
  var isCharDevice by remember { mutableStateOf(false) }

  // Target Keyboard layout selection
  var selectedKeyboardLayout by remember {
    mutableStateOf(KeyboardLayout.fromCode(prefs.getString("keyboard_layout", "US")))
  }

  // Script text and execution state
  val sampleScript = remember {
    "REM -- HID Interface Runner Script --\n" +
      "REM Developed by IMApurbo\n" +
      "DELAY 500\n" +
      "STRING Hello from HID Interface Runner!\n" +
      "ENTER\n"
  }
  var scriptText by remember { mutableStateOf(sampleScript) }
  var isRunning by remember { mutableStateOf(false) }
  var isCancelled by remember { mutableStateOf(false) }
  var runningJob by remember { mutableStateOf<Job?>(null) }

  // Auto Execute (Wait for USB) State
  var isWaitingForUsb by remember { mutableStateOf(false) }
  var autoExecuteJob by remember { mutableStateOf<Job?>(null) }

  // Dialog states for Save / Load
  var showSaveDialog by remember { mutableStateOf(false) }
  var showLoadDialog by remember { mutableStateOf(false) }

  // Typing Delay in ms - default 0L (instant hardware typing speed for HID)
  var typingDelayMs by remember {
    mutableStateOf(prefs.getLong("typing_delay_ms", 0L))
  }

  // Refresh HID nodes using root ls
  fun refreshHidNodes() {
    coroutineScope.launch(Dispatchers.IO) {
      val nodes = scanHidNodesWithStatus()
      withContext(Dispatchers.Main) {
        hidNodesWithStatus = nodes
        if (nodes.isNotEmpty()) {
          val savedPref = prefs.getString("user_selected_interface", null)
          val savedMatch = nodes.find { it.name == savedPref }
          if (savedMatch != null) {
            // User selected earlier, remember it
            selectedNode = savedMatch.name
            isCharDevice = savedMatch.isCharDevice
          } else {
            // Auto-select real character device (isCharDevice == true, e.g. hidg1)
            val realNode = nodes.find { it.isCharDevice } ?: nodes.first()
            selectedNode = realNode.name
            isCharDevice = realNode.isCharDevice
          }
        } else {
          isCharDevice = checkIsCharacterDevice(selectedNode)
        }
      }
    }
  }

  // Check root permission automatically
  fun checkRootPermission() {
    rootStatus = RootStatus.Checking
    coroutineScope.launch(Dispatchers.IO) {
      val isGranted = try {
        val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
        val output = process.inputStream.bufferedReader().use { it.readText() }
        process.waitFor()
        output.contains("uid=0")
      } catch (e: Exception) {
        false
      }
      withContext(Dispatchers.Main) {
        if (isGranted) {
          rootStatus = RootStatus.Granted
          refreshHidNodes()
        } else {
          rootStatus = RootStatus.Denied
          refreshHidNodes()
          Toast.makeText(context, "Root permission denied", Toast.LENGTH_SHORT).show()
        }
      }
    }
  }

  // Launch root check automatically when the app opens
  LaunchedEffect(Unit) {
    checkRootPermission()
  }

  // Helper to execute any script string
  fun runScriptContent(contentToRun: String, scriptName: String = "Script") {
    if (isRunning) {
      isCancelled = true
      isRunning = false
      runningJob?.cancel()
      Toast.makeText(context, "Execution stopped", Toast.LENGTH_SHORT).show()
      return
    }

    if (rootStatus != RootStatus.Granted) {
      Toast.makeText(context, "Root permission denied", Toast.LENGTH_SHORT).show()
      checkRootPermission()
      return
    }
    if (!isCharDevice) {
      val msg = "Selected interface is not a real HID device. Please select a valid character device."
      Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
      coroutineScope.launch {
        snackbarHostState.showSnackbar(msg)
      }
      return
    }

    isRunning = true
    isCancelled = false
    Toast.makeText(context, "Running $scriptName…", Toast.LENGTH_SHORT).show()

    runningJob = coroutineScope.launch {
      val snackJob = launch {
        snackbarHostState.showSnackbar("Running $scriptName…")
      }
      val nodePath = "/dev/$selectedNode"
      val actions = try {
        parseScript(contentToRun, selectedKeyboardLayout)
      } catch (e: Exception) {
        isRunning = false
        snackJob.cancel()
        val errorMsg = "Parse error: ${e.message}"
        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
        snackbarHostState.showSnackbar(errorMsg)
        return@launch
      }

      val result = executeScript(
        nodePath = nodePath,
        actions = actions,
        keyDelayMs = typingDelayMs,
        isActive = { !isCancelled }
      )
      isRunning = false
      snackJob.cancel()
      if (result.isSuccess) {
        Toast.makeText(context, "$scriptName finished successfully", Toast.LENGTH_SHORT).show()
        snackbarHostState.showSnackbar("$scriptName done")
      } else {
        val msg = result.exceptionOrNull()?.message ?: "Execution failed"
        Toast.makeText(context, "Error: $msg", Toast.LENGTH_LONG).show()
        snackbarHostState.showSnackbar("Error: $msg")
      }
    }
  }

  // Toggle Auto Execute: Wait for USB connection before executing instantly
  fun toggleAutoExecute() {
    if (isWaitingForUsb) {
      isWaitingForUsb = false
      autoExecuteJob?.cancel()
      autoExecuteJob = null
      Toast.makeText(context, "Auto-execute cancelled", Toast.LENGTH_SHORT).show()
      coroutineScope.launch {
        snackbarHostState.showSnackbar("Auto-execute cancelled")
      }
      return
    }

    if (isRunning) {
      Toast.makeText(context, "A script is already running", Toast.LENGTH_SHORT).show()
      return
    }

    if (rootStatus != RootStatus.Granted) {
      Toast.makeText(context, "Root permission required for Auto Execute", Toast.LENGTH_SHORT).show()
      checkRootPermission()
      return
    }

    if (!isCharDevice) {
      val msg = "Selected interface is not a valid HID character device."
      Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
      coroutineScope.launch {
        snackbarHostState.showSnackbar(msg)
      }
      return
    }

    isWaitingForUsb = true
    Toast.makeText(context, "Waiting for USB connection…", Toast.LENGTH_SHORT).show()

    autoExecuteJob = coroutineScope.launch {
      val snackJob = launch {
        snackbarHostState.showSnackbar("Armed: Waiting for USB connection to host PC…")
      }

      var usbConnected = false

      val receiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) {
          val action = intent?.action ?: return
          if (action == "android.hardware.usb.action.USB_STATE") {
            val connected = intent.getBooleanExtra("connected", false)
            val configured = intent.getBooleanExtra("configured", false)
            if (connected || configured) {
              usbConnected = true
            }
          } else if (action == Intent.ACTION_POWER_CONNECTED) {
            usbConnected = true
          }
        }
      }

      val filter = IntentFilter().apply {
        addAction("android.hardware.usb.action.USB_STATE")
        addAction(Intent.ACTION_POWER_CONNECTED)
      }
      context.registerReceiver(receiver, filter)

      try {
        val initiallyConnected = UsbConnectionMonitor.isUsbConnected(context)
        var hasSeenDisconnect = !initiallyConnected

        while (isActive && !usbConnected) {
          val connectedNow = UsbConnectionMonitor.isUsbConnected(context)
          if (!connectedNow) {
            hasSeenDisconnect = true
          } else if (hasSeenDisconnect) {
            usbConnected = true
            break
          }
          delay(300L)
        }

        if (usbConnected && isActive) {
          isWaitingForUsb = false
          snackJob.cancel()
          Toast.makeText(context, "USB Connected! Executing instantly…", Toast.LENGTH_SHORT).show()
          delay(400L)
          runScriptContent(scriptText, "Auto Execute")
        }
      } finally {
        try {
          context.unregisterReceiver(receiver)
        } catch (e: Exception) {
          // Ignored
        }
        isWaitingForUsb = false
      }
    }
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "HID Interface Runner",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "by IMApurbo",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary
            )
          }
        },
        actions = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(end = 12.dp)
          ) {
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = when (rootStatus) {
                RootStatus.Granted -> StatusGreenBg
                RootStatus.Denied -> StatusRedBg
                RootStatus.Checking -> StatusGreyBg
              },
              modifier = Modifier
                .clickable { checkRootPermission() }
                .testTag("root_status_chip")
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(8.dp)
                    .background(
                      color = when (rootStatus) {
                        RootStatus.Granted -> StatusGreen
                        RootStatus.Denied -> StatusRed
                        RootStatus.Checking -> StatusGrey
                      },
                      shape = CircleShape
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = when (rootStatus) {
                    RootStatus.Granted -> "Root: Granted"
                    RootStatus.Denied -> "Root: Denied"
                    RootStatus.Checking -> "Root: Checking…"
                  },
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                  color = when (rootStatus) {
                    RootStatus.Granted -> StatusGreen
                    RootStatus.Denied -> StatusRed
                    RootStatus.Checking -> StatusGrey
                  }
                )
              }
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
      )
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      // Navigation Tabs
      TabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary
      ) {
        Tab(
          selected = selectedTabIndex == 0,
          onClick = { selectedTabIndex = 0 },
          icon = { Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(20.dp)) },
          text = { Text("Runner & Editor", style = MaterialTheme.typography.labelMedium) }
        )
        Tab(
          selected = selectedTabIndex == 1,
          onClick = { selectedTabIndex = 1 },
          icon = { Icon(Icons.Default.Celebration, contentDescription = null, modifier = Modifier.size(20.dp)) },
          text = { Text("Demo & Pranks (30)", style = MaterialTheme.typography.labelMedium) }
        )
      }

      // Tab Content
      if (selectedTabIndex == 0) {
        // Tab 0: Runner & Script Editor
        RunnerEditorTab(
          selectedNode = selectedNode,
          isCharDevice = isCharDevice,
          hidNodesWithStatus = hidNodesWithStatus,
          selectedKeyboardLayout = selectedKeyboardLayout,
          scriptText = scriptText,
          isRunning = isRunning,
          isWaitingForUsb = isWaitingForUsb,
          typingDelayMs = typingDelayMs,
          onRefreshNodes = { refreshHidNodes() },
          onSelectNode = { node ->
            selectedNode = node.name
            isCharDevice = node.isCharDevice
            prefs.edit().putString("user_selected_interface", node.name).apply()
          },
          onSelectKeyboardLayout = { layout ->
            selectedKeyboardLayout = layout
            prefs.edit().putString("keyboard_layout", layout.code).apply()
          },
          onTypingDelayChange = { newDelay ->
            typingDelayMs = newDelay
            prefs.edit().putLong("typing_delay_ms", newDelay).apply()
          },
          onScriptTextChange = { scriptText = it },
          onPlayStop = { runScriptContent(scriptText, "Script") },
          onToggleAutoExecute = { toggleAutoExecute() },
          onSaveClick = { showSaveDialog = true },
          onLoadClick = { showLoadDialog = true }
        )
      } else {
        // Tab 1: Demo & Prank Scripts (30 scripts)
        DemoScriptsTab(
          isRunning = isRunning,
          onLoadToEditor = { demo ->
            scriptText = demo.script
            selectedTabIndex = 0
            coroutineScope.launch {
              snackbarHostState.showSnackbar("Loaded '${demo.title}' into Editor")
            }
          },
          onQuickRun = { demo ->
            runScriptContent(demo.script, demo.title)
          }
        )
      }
    }
  }

  // One-time first-run dialog
  if (showFirstRunDialog) {
    AlertDialog(
      onDismissRequest = { /* Require explicit confirmation */ },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 8.dp)
          )
          Text(
            text = "Important Notice",
            style = MaterialTheme.typography.titleLarge
          )
        }
      },
      text = {
        Column(
          modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(vertical = 4.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text(
            text = "HID Interface Runner (Developed by IMApurbo) is a legitimate developer and accessibility utility that lets you send keyboard input from your rooted Android phone to a connected host device over the USB HID gadget interface.",
            style = MaterialTheme.typography.bodyMedium
          )
          Text(
            text = "Typical legitimate uses include:\n" +
              "• Accessibility: using a phone as an assistive input device.\n" +
              "• Custom input peripherals: macro keypad, presentation clicker, media remote.\n" +
              "• Device testing and QA: automating input on target devices without attached keyboards.\n" +
              "• DIY hardware projects: driving host devices in maker builds.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "The app is NOT intended for any unauthorized use. It only writes to the local /dev/hidg* node that you have created on your own rooted device with the USB Gadget Tool. It does not exploit anything, does not bypass any security, and does not touch any remote system without your own physical USB connection.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            prefs.edit().putBoolean("disclaimer_accepted", true).apply()
            showFirstRunDialog = false
          },
          modifier = Modifier.testTag("disclaimer_confirm_button")
        ) {
          Text("I understand")
        }
      }
    )
  }

  // Save Dialog
  if (showSaveDialog) {
    var saveFileName by remember {
      val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
      mutableStateOf("payload_$timeStamp.txt")
    }

    AlertDialog(
      onDismissRequest = { showSaveDialog = false },
      title = { Text("Save Script") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "Script will be saved to app storage in /ducky_scripts/",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          OutlinedTextField(
            value = saveFileName,
            onValueChange = { saveFileName = it },
            label = { Text("File name") },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("save_filename_input")
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val result = saveScriptToFile(context, saveFileName, scriptText)
            if (result.isSuccess) {
              Toast.makeText(context, "Saved ${result.getOrNull()}", Toast.LENGTH_SHORT).show()
              showSaveDialog = false
            } else {
              val err = result.exceptionOrNull()?.message ?: "Save failed"
              Toast.makeText(context, "Error saving: $err", Toast.LENGTH_LONG).show()
            }
          },
          modifier = Modifier.testTag("save_confirm_button")
        ) {
          Text("Save")
        }
      },
      dismissButton = {
        TextButton(
          onClick = { showSaveDialog = false },
          modifier = Modifier.testTag("save_cancel_button")
        ) {
          Text("Cancel")
        }
      }
    )
  }

  // Load Dialog
  if (showLoadDialog) {
    val savedFiles = remember { listSavedScripts(context) }

    AlertDialog(
      onDismissRequest = { showLoadDialog = false },
      title = { Text("Load Saved Script") },
      text = {
        if (savedFiles.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "No saved scripts found",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        } else {
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(max = 320.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            items(savedFiles) { file ->
              val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(file.lastModified()))
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    try {
                      val content = file.readText()
                      scriptText = content
                      Toast.makeText(context, "Loaded ${file.name}", Toast.LENGTH_SHORT).show()
                      showLoadDialog = false
                    } catch (e: Exception) {
                      Toast.makeText(context, "Failed to load: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                  }
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = file.name,
                      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                      text = "$dateStr • ${file.length()} bytes",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                  Icon(
                    imageVector = Icons.Default.FileOpen,
                    contentDescription = "Load script file",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                  )
                }
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(
          onClick = { showLoadDialog = false },
          modifier = Modifier.testTag("load_close_button")
        ) {
          Text("Close")
        }
      }
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunnerEditorTab(
  selectedNode: String,
  isCharDevice: Boolean,
  hidNodesWithStatus: List<HidNodeStatus>,
  selectedKeyboardLayout: KeyboardLayout,
  scriptText: String,
  isRunning: Boolean,
  isWaitingForUsb: Boolean,
  typingDelayMs: Long,
  onRefreshNodes: () -> Unit,
  onSelectNode: (HidNodeStatus) -> Unit,
  onSelectKeyboardLayout: (KeyboardLayout) -> Unit,
  onTypingDelayChange: (Long) -> Unit,
  onScriptTextChange: (String) -> Unit,
  onPlayStop: () -> Unit,
  onToggleAutoExecute: () -> Unit,
  onSaveClick: () -> Unit,
  onLoadClick: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    // Row containing HID Interface dropdown and Keyboard Layout dropdown
    var hidDropdownExpanded by remember { mutableStateOf(false) }
    var layoutDropdownExpanded by remember { mutableStateOf(false) }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // HID Interface Dropdown
      ExposedDropdownMenuBox(
        expanded = hidDropdownExpanded,
        onExpandedChange = {
          hidDropdownExpanded = !hidDropdownExpanded
          if (hidDropdownExpanded) onRefreshNodes()
        },
        modifier = Modifier
          .weight(1f)
          .testTag("hid_interface_dropdown")
      ) {
        OutlinedTextField(
          value = formatInterfaceDisplayName(selectedNode),
          onValueChange = {},
          readOnly = true,
          label = { Text("HID Interface", maxLines = 1) },
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = hidDropdownExpanded) },
          modifier = Modifier
            .menuAnchor()
            .fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
          )
        )

        ExposedDropdownMenu(
          expanded = hidDropdownExpanded,
          onDismissRequest = { hidDropdownExpanded = false }
        ) {
          if (hidNodesWithStatus.isEmpty()) {
            DropdownMenuItem(
              text = { Text("No HID interfaces found in /dev") },
              onClick = { hidDropdownExpanded = false }
            )
          } else {
            hidNodesWithStatus.forEach { node ->
              DropdownMenuItem(
                text = {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = formatInterfaceDisplayName(node.name),
                      fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = if (node.isCharDevice) "ready" else "unsupported",
                      style = MaterialTheme.typography.bodySmall,
                      color = if (node.isCharDevice) StatusGreen else StatusRed
                    )
                  }
                },
                onClick = {
                  onSelectNode(node)
                  hidDropdownExpanded = false
                }
              )
            }
          }
        }
      }

      // Keyboard Layout Dropdown
      ExposedDropdownMenuBox(
        expanded = layoutDropdownExpanded,
        onExpandedChange = { layoutDropdownExpanded = !layoutDropdownExpanded },
        modifier = Modifier
          .weight(1f)
          .testTag("keyboard_layout_dropdown")
      ) {
        OutlinedTextField(
          value = selectedKeyboardLayout.displayName,
          onValueChange = {},
          readOnly = true,
          label = { Text("Target Layout", maxLines = 1) },
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = layoutDropdownExpanded) },
          modifier = Modifier
            .menuAnchor()
            .fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
          )
        )

        ExposedDropdownMenu(
          expanded = layoutDropdownExpanded,
          onDismissRequest = { layoutDropdownExpanded = false }
        ) {
          KeyboardLayout.values().forEach { layout ->
            DropdownMenuItem(
              text = {
                Column {
                  Text(
                    text = layout.displayName,
                    fontWeight = if (layout == selectedKeyboardLayout) FontWeight.Bold else FontWeight.Normal,
                    color = if (layout == selectedKeyboardLayout) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = layout.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              },
              onClick = {
                onSelectKeyboardLayout(layout)
                layoutDropdownExpanded = false
              }
            )
          }
        }
      }
    }

    // Status and Layout Indicator Row
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 4.dp)
    ) {
      Text(
        text = if (isCharDevice) "● Interface ready (character device)" else "● Not a real HID node",
        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
        color = if (isCharDevice) StatusGreen else StatusRed
      )
      Text(
        text = "Target: ${selectedKeyboardLayout.code}",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary
      )
    }

    // Typing Delay Option (Instant 0ms default)
    Surface(
      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
      shape = RoundedCornerShape(10.dp),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("typing_delay_selector")
    ) {
      Column(
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Bolt,
              contentDescription = "Typing Speed",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = "Typing Delay",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
              color = MaterialTheme.colorScheme.onSurface
            )
          }
          Text(
            text = if (typingDelayMs == 0L) "⚡ Instant (0ms)" else "${typingDelayMs}ms delay",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = if (typingDelayMs == 0L) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
          )
        }

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          listOf(
            0L to "0ms (Instant)",
            5L to "5ms (Fast)",
            12L to "12ms (Normal)",
            25L to "25ms (Safe)"
          ).forEach { (delayVal, label) ->
            FilterChip(
              selected = typingDelayMs == delayVal,
              onClick = { onTypingDelayChange(delayVal) },
              label = {
                Text(
                  text = label,
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (typingDelayMs == delayVal) FontWeight.Bold else FontWeight.Normal
                  )
                )
              },
              modifier = Modifier.testTag("typing_delay_${delayVal}ms"),
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
              )
            )
          }
        }
      }
    }

    // Layout helper alert if user might be suffering from JIS mismatch
    if (selectedKeyboardLayout == KeyboardLayout.JIS) {
      Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Language,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "JIS Mode: Translates keys so ':' and '=' type correctly on Japanese hosts",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
        }
      }
    } else if (scriptText.contains(":") || scriptText.contains("=")) {
      Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "PC typing '+' for ':' or '^' for '='?",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          TextButton(
            onClick = { onSelectKeyboardLayout(KeyboardLayout.JIS) },
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
          ) {
            Text("Switch to JIS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
          }
        }
      }
    }

    // Armed Auto-Execute Indicator Banner
    if (isWaitingForUsb) {
      Card(
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("auto_execute_banner")
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            CircularProgressIndicator(
              modifier = Modifier.size(18.dp),
              strokeWidth = 2.dp,
              color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Column {
              Text(
                text = "Armed: Waiting for USB…",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onTertiaryContainer
              )
              Text(
                text = "Plug phone into host PC to execute script instantly",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
              )
            }
          }
          TextButton(
            onClick = onToggleAutoExecute,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
          ) {
            Text(
              "Cancel",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.error
            )
          }
        }
      }
    }

    // Quick toolbar above Editor
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      val lines = if (scriptText.isEmpty()) 0 else scriptText.lines().size
      val chars = scriptText.length
      Text(
        text = "$lines lines • $chars chars",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        TextButton(
          onClick = { onScriptTextChange("") },
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
        ) {
          Text("Clear", style = MaterialTheme.typography.labelSmall)
        }
        TextButton(
          onClick = {
            onScriptTextChange("REM Quick Test\nDELAY 500\nSTRING Testing HID...\nENTER\n")
          },
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
        ) {
          Text("Sample", style = MaterialTheme.typography.labelSmall)
        }
      }
    }

    // Script editor
    OutlinedTextField(
      value = scriptText,
      onValueChange = onScriptTextChange,
      placeholder = { Text("Input Ducky script…") },
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .testTag("script_input"),
      textStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 13.sp,
        lineHeight = 18.sp
      ),
      colors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
      )
    )

    // Bottom Action Controls (Two clean, spacious rows)
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 4.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      // Row 1: Primary Execution (Play/Stop & Auto Execute/Cancel)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Play / Stop
        Button(
          onClick = onPlayStop,
          enabled = !isWaitingForUsb,
          contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp),
          modifier = Modifier
            .weight(1f)
            .testTag("play_button"),
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            contentColor = if (isRunning) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary
          )
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
              contentDescription = if (isRunning) "Stop script" else "Play script",
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (isRunning) "Stop" else "Play",
              maxLines = 1,
              softWrap = false,
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
            )
          }
        }

        // Auto Execute / Cancel Mode
        Button(
          onClick = onToggleAutoExecute,
          enabled = !isRunning,
          contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp),
          modifier = Modifier
            .weight(1f)
            .testTag("auto_execute_button"),
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isWaitingForUsb) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary,
            contentColor = if (isWaitingForUsb) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onTertiary
          )
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = if (isWaitingForUsb) Icons.Default.Close else Icons.Default.Usb,
              contentDescription = if (isWaitingForUsb) "Cancel auto execute" else "Auto execute on USB connect",
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (isWaitingForUsb) "Cancel Auto" else "Auto Execute",
              maxLines = 1,
              softWrap = false,
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
            )
          }
        }
      }

      // Row 2: Script Management (Save & Load)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Save
        Button(
          onClick = onSaveClick,
          enabled = !isRunning && !isWaitingForUsb,
          contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp),
          modifier = Modifier
            .weight(1f)
            .testTag("save_button"),
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
          )
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = Icons.Default.Save,
              contentDescription = "Save script",
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Save",
              maxLines = 1,
              softWrap = false,
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
            )
          }
        }

        // Load
        Button(
          onClick = onLoadClick,
          enabled = !isRunning && !isWaitingForUsb,
          contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp),
          modifier = Modifier
            .weight(1f)
            .testTag("load_button"),
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface
          )
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = Icons.Default.FolderOpen,
              contentDescription = "Load script",
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Load",
              maxLines = 1,
              softWrap = false,
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
            )
          }
        }
      }
    }
  }
}

@Composable
fun DemoScriptsTab(
  isRunning: Boolean,
  onLoadToEditor: (DemoScript) -> Unit,
  onQuickRun: (DemoScript) -> Unit
) {
  val clipboardManager = LocalClipboardManager.current
  val context = LocalContext.current
  var searchQuery by remember { mutableStateOf("") }
  var selectedOsFilter by remember { mutableStateOf("All") }
  val osFilters = listOf("All", "Windows", "macOS", "Linux", "Universal")

  val allScripts = remember { DemoScriptsRepository.scripts }

  val filteredScripts = remember(searchQuery, selectedOsFilter) {
    allScripts.filter { script ->
      val matchesOs = if (selectedOsFilter == "All") true else script.targetOs.equals(selectedOsFilter, ignoreCase = true)
      val matchesQuery = if (searchQuery.isBlank()) {
        true
      } else {
        script.title.contains(searchQuery, ignoreCase = true) ||
          script.description.contains(searchQuery, ignoreCase = true) ||
          script.category.contains(searchQuery, ignoreCase = true) ||
          script.targetOs.contains(searchQuery, ignoreCase = true)
      }
      matchesOs && matchesQuery
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    // Search input
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      label = { Text("Search demo & prank scripts…") },
      leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
      trailingIcon = {
        if (searchQuery.isNotEmpty()) {
          IconButton(onClick = { searchQuery = "" }) {
            Icon(Icons.Default.Clear, contentDescription = "Clear search", modifier = Modifier.size(18.dp))
          }
        }
      },
      singleLine = true,
      modifier = Modifier.fillMaxWidth(),
      colors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
      )
    )

    // OS Filter Chips
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      osFilters.forEach { os ->
        val selected = selectedOsFilter == os
        FilterChip(
          selected = selected,
          onClick = { selectedOsFilter = os },
          label = { Text(if (os == "All") "All (${allScripts.size})" else os) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
          )
        )
      }
    }

    Text(
      text = "Showing ${filteredScripts.size} curated scripts",
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    // List of scripts
    LazyColumn(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      items(filteredScripts, key = { it.id }) { demo ->
        DemoScriptCard(
          demo = demo,
          isRunning = isRunning,
          onLoadToEditor = { onLoadToEditor(demo) },
          onQuickRun = { onQuickRun(demo) },
          onCopyScript = {
            clipboardManager.setText(AnnotatedString(demo.script))
            Toast.makeText(context, "Script copied to clipboard", Toast.LENGTH_SHORT).show()
          }
        )
      }
    }
  }
}

@Composable
fun DemoScriptCard(
  demo: DemoScript,
  isRunning: Boolean,
  onLoadToEditor: () -> Unit,
  onQuickRun: () -> Unit,
  onCopyScript: () -> Unit
) {
  var isExpanded by remember { mutableStateOf(false) }

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Header: Title and Badges
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = demo.title,
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
          modifier = Modifier.weight(1f)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          // OS Badge
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
          ) {
            Text(
              text = demo.targetOs,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }

          // Category Badge
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
          ) {
            Text(
              text = demo.category,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.secondary,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }
      }

      // Description
      Text(
        text = demo.description,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      // Collapsible Script Code Preview
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
          .padding(8.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Code,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (isExpanded) "Hide Ducky Script" else "View Ducky Script",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
              color = MaterialTheme.colorScheme.primary
            )
          }
          Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
          )
        }

        if (isExpanded) {
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = demo.script,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            lineHeight = 15.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth()
          )
        }
      }

      // Actions
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        OutlinedButton(
          onClick = onLoadToEditor,
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
          modifier = Modifier.weight(1f)
        ) {
          Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("To Editor", style = MaterialTheme.typography.labelSmall, maxLines = 1, softWrap = false)
        }

        Button(
          onClick = onQuickRun,
          enabled = !isRunning,
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
          modifier = Modifier.weight(1f)
        ) {
          Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Quick Run", style = MaterialTheme.typography.labelSmall, maxLines = 1, softWrap = false)
        }

        IconButton(
          onClick = onCopyScript,
          modifier = Modifier.size(36.dp)
        ) {
          Icon(
            Icons.Default.ContentCopy,
            contentDescription = "Copy script",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun HidRunnerPreview() {
  MyApplicationTheme {
    HidRunnerApp()
  }
}
