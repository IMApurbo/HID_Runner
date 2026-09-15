package com.example

import android.content.Context
import android.os.Environment
import android.system.Os
import android.system.OsConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.Locale

// HID report data structure (standard 8-byte USB HID keyboard report)
data class HidReport(val modifier: Byte, val keycode: Byte) {
  fun toPressByteArray(): ByteArray = byteArrayOf(modifier, 0x00, keycode, 0, 0, 0, 0, 0)

  companion object {
    val RELEASE_REPORT = byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0)
  }
}

// Actions parsed from the script
sealed class ScriptAction(val sourceLineNumber: Int) {
  class Delay(val ms: Long, lineNumber: Int) : ScriptAction(lineNumber)
  class KeyPress(val report: HidReport, lineNumber: Int) : ScriptAction(lineNumber)
}

/**
 * Resolves keyboard keycodes according to standard USB HID specification.
 */
fun resolveKeycode(key: String): Byte? {
  val upper = key.trim().uppercase(Locale.US)
  if (upper.length == 1) {
    val c = upper[0]
    if (c in 'A'..'Z') {
      return (0x04 + (c - 'A')).toByte()
    }
    if (c in '1'..'9') {
      return (0x1E + (c - '1')).toByte()
    }
    if (c == '0') {
      return 0x27
    }
  }
  return when (upper) {
    "ENTER", "RETURN" -> 0x28
    "ESC", "ESCAPE" -> 0x29
    "BACKSPACE", "BKSP" -> 0x2A
    "TAB" -> 0x2B
    "SPACE" -> 0x2C
    "MINUS" -> 0x2D
    "EQUAL" -> 0x2E
    "CAPSLOCK", "CAPS" -> 0x39
    "F1" -> 0x3A
    "F2" -> 0x3B
    "F3" -> 0x3C
    "F4" -> 0x3D
    "F5" -> 0x3E
    "F6" -> 0x3F
    "F7" -> 0x40
    "F8" -> 0x41
    "F9" -> 0x42
    "F10" -> 0x43
    "F11" -> 0x44
    "F12" -> 0x45
    "PRINTSCREEN", "PRINTSCRN", "PRTSCR", "SNAPSHOT" -> 0x46
    "SCROLLLOCK", "SCROLL_LOCK" -> 0x47
    "PAUSE", "BREAK" -> 0x48
    "INSERT", "INS" -> 0x49
    "HOME" -> 0x4A
    "PAGEUP", "PGUP" -> 0x4B
    "DELETE", "DEL" -> 0x4C
    "END" -> 0x4D
    "PAGEDOWN", "PGDN" -> 0x4E
    "RIGHT", "RIGHTARROW" -> 0x4F
    "LEFT", "LEFTARROW" -> 0x50
    "DOWN", "DOWNARROW" -> 0x51
    "UP", "UPARROW" -> 0x52
    "NUMLOCK" -> 0x53
    "MENU", "APP" -> 0x65
    "F13" -> 0x68
    "F14" -> 0x69
    "F15" -> 0x6A
    "F16" -> 0x6B
    "F17" -> 0x6C
    "F18" -> 0x6D
    "F19" -> 0x6E
    "F20" -> 0x6F
    "F21" -> 0x70
    "F22" -> 0x71
    "F23" -> 0x72
    "F24" -> 0x73
    "NUMPAD0" -> 0x62
    "NUMPAD1" -> 0x59
    "NUMPAD2" -> 0x5A
    "NUMPAD3" -> 0x5B
    "NUMPAD4" -> 0x5C
    "NUMPAD5" -> 0x5D
    "NUMPAD6" -> 0x5E
    "NUMPAD7" -> 0x5F
    "NUMPAD8" -> 0x60
    "NUMPAD9" -> 0x61
    "NUMPAD_ENTER" -> 0x58
    else -> null
  }
}

enum class KeyboardLayout(
  val code: String,
  val displayName: String,
  val description: String
) {
  US("US", "US (QWERTY)", "Standard English"),
  JIS("JIS", "Japanese (JIS)", "Fixes : becoming + and = becoming ^"),
  UK("UK", "UK (QWERTY)", "Great Britain layout"),
  DE("DE", "German (QWERTZ)", "German QWERTZ layout"),
  FR("FR", "French (AZERTY)", "French AZERTY layout"),
  ES("ES", "Spanish (QWERTY)", "Spanish layout");

  companion object {
    fun fromCode(code: String?): KeyboardLayout {
      return values().find { it.code.equals(code, ignoreCase = true) } ?: US
    }
  }
}

/**
 * Maps single characters (ASCII) to their corresponding HID report (modifier + keycode)
 * based on the target system's active keyboard layout.
 */
fun charToHidReport(c: Char, layout: KeyboardLayout = KeyboardLayout.US): HidReport? {
  return when (layout) {
    KeyboardLayout.US -> charToHidReportUs(c)
    KeyboardLayout.JIS -> charToHidReportJis(c)
    KeyboardLayout.UK -> charToHidReportUk(c)
    KeyboardLayout.DE -> charToHidReportDe(c)
    KeyboardLayout.FR -> charToHidReportFr(c)
    KeyboardLayout.ES -> charToHidReportEs(c)
  }
}

private fun charToHidReportUs(c: Char): HidReport? {
  if (c in 'a'..'z') {
    return HidReport(0x00, (0x04 + (c - 'a')).toByte())
  }
  if (c in 'A'..'Z') {
    return HidReport(0x02, (0x04 + (c - 'A')).toByte())
  }
  if (c in '1'..'9') {
    return HidReport(0x00, (0x1E + (c - '1')).toByte())
  }
  if (c == '0') return HidReport(0x00, 0x27)
  return when (c) {
    '!' -> HidReport(0x02, 0x1E)
    '@' -> HidReport(0x02, 0x1F)
    '#' -> HidReport(0x02, 0x20)
    '$' -> HidReport(0x02, 0x21)
    '%' -> HidReport(0x02, 0x22)
    '^' -> HidReport(0x02, 0x23)
    '&' -> HidReport(0x02, 0x24)
    '*' -> HidReport(0x02, 0x25)
    '(' -> HidReport(0x02, 0x26)
    ')' -> HidReport(0x02, 0x27)
    '-' -> HidReport(0x00, 0x2D)
    '_' -> HidReport(0x02, 0x2D)
    '=' -> HidReport(0x00, 0x2E)
    '+' -> HidReport(0x02, 0x2E)
    '[' -> HidReport(0x00, 0x2F)
    '{' -> HidReport(0x02, 0x2F)
    ']' -> HidReport(0x00, 0x30)
    '}' -> HidReport(0x02, 0x30)
    '\\' -> HidReport(0x00, 0x31)
    '|' -> HidReport(0x02, 0x31)
    ';' -> HidReport(0x00, 0x33)
    ':' -> HidReport(0x02, 0x33)
    '\'' -> HidReport(0x00, 0x34)
    '"' -> HidReport(0x02, 0x34)
    '`' -> HidReport(0x00, 0x35)
    '~' -> HidReport(0x02, 0x35)
    ',' -> HidReport(0x00, 0x36)
    '<' -> HidReport(0x02, 0x36)
    '.' -> HidReport(0x00, 0x37)
    '>' -> HidReport(0x02, 0x37)
    '/' -> HidReport(0x00, 0x38)
    '?' -> HidReport(0x02, 0x38)
    ' ' -> HidReport(0x00, 0x2C)
    '\t' -> HidReport(0x00, 0x2B)
    '\n' -> HidReport(0x00, 0x28)
    else -> null
  }
}

/**
 * Japanese (JIS 106/109) layout mapping.
 * Crucial fix:
 * - In JIS, physical key 0x34 is ':' (unshifted) and '*' (with shift).
 * - In JIS, physical key 0x33 is ';' (unshifted) and '+' (with shift).
 * - In JIS, physical key 0x2E is '^' (unshifted) and '~' (with shift).
 * - In JIS, physical key 0x2D is '-' (unshifted) and '=' (with shift).
 * - In JIS, physical key 0x2F is '@' (unshifted) and '`' (with shift).
 */
private fun charToHidReportJis(c: Char): HidReport? {
  if (c in 'a'..'z') {
    return HidReport(0x00, (0x04 + (c - 'a')).toByte())
  }
  if (c in 'A'..'Z') {
    return HidReport(0x02, (0x04 + (c - 'A')).toByte())
  }
  if (c in '1'..'9') {
    return HidReport(0x00, (0x1E + (c - '1')).toByte())
  }
  if (c == '0') return HidReport(0x00, 0x27)
  return when (c) {
    '!' -> HidReport(0x02, 0x1E) // Shift + 1
    '"' -> HidReport(0x02, 0x1F) // Shift + 2
    '#' -> HidReport(0x02, 0x20) // Shift + 3
    '$' -> HidReport(0x02, 0x21) // Shift + 4
    '%' -> HidReport(0x02, 0x22) // Shift + 5
    '&' -> HidReport(0x02, 0x23) // Shift + 6
    '\'' -> HidReport(0x02, 0x24) // Shift + 7
    '(' -> HidReport(0x02, 0x25) // Shift + 8
    ')' -> HidReport(0x02, 0x26) // Shift + 9
    '-' -> HidReport(0x00, 0x2D) // Key 0x2D unshifted is '-'
    '=' -> HidReport(0x02, 0x2D) // Shift + Key 0x2D is '='
    '^' -> HidReport(0x00, 0x2E) // Key 0x2E unshifted is '^'
    '~' -> HidReport(0x02, 0x2E) // Shift + Key 0x2E is '~'
    '@' -> HidReport(0x00, 0x2F) // Key 0x2F unshifted is '@'
    '`' -> HidReport(0x02, 0x2F) // Shift + Key 0x2F is '`'
    '[' -> HidReport(0x00, 0x30) // Key 0x30 unshifted is '['
    '{' -> HidReport(0x02, 0x30) // Shift + Key 0x30 is '{'
    ']' -> HidReport(0x00, 0x31) // Key 0x31 unshifted is ']'
    '}' -> HidReport(0x02, 0x31) // Shift + Key 0x31 is '}'
    ';' -> HidReport(0x00, 0x33) // Key 0x33 unshifted is ';'
    '+' -> HidReport(0x02, 0x33) // Shift + Key 0x33 is '+'
    ':' -> HidReport(0x00, 0x34) // Key 0x34 unshifted is ':'
    '*' -> HidReport(0x02, 0x34) // Shift + Key 0x34 is '*'
    '\\' -> HidReport(0x00, 0x89.toByte()) // Yen/Backslash key (Intl 3) or fallback 0x31
    '|' -> HidReport(0x02, 0x89.toByte())
    '_' -> HidReport(0x02, 0x87.toByte()) // Shift + Ro key (Intl 1)
    ',' -> HidReport(0x00, 0x36)
    '<' -> HidReport(0x02, 0x36)
    '.' -> HidReport(0x00, 0x37)
    '>' -> HidReport(0x02, 0x37)
    '/' -> HidReport(0x00, 0x38)
    '?' -> HidReport(0x02, 0x38)
    ' ' -> HidReport(0x00, 0x2C)
    '\t' -> HidReport(0x00, 0x2B)
    '\n' -> HidReport(0x00, 0x28)
    else -> charToHidReportUs(c)
  }
}

/**
 * UK (Great Britain) layout mapping.
 */
private fun charToHidReportUk(c: Char): HidReport? {
  if (c in 'a'..'z') return HidReport(0x00, (0x04 + (c - 'a')).toByte())
  if (c in 'A'..'Z') return HidReport(0x02, (0x04 + (c - 'A')).toByte())
  if (c in '1'..'9') return HidReport(0x00, (0x1E + (c - '1')).toByte())
  if (c == '0') return HidReport(0x00, 0x27)
  return when (c) {
    '!' -> HidReport(0x02, 0x1E)
    '"' -> HidReport(0x02, 0x1F) // Shift + 2
    '£' -> HidReport(0x02, 0x20) // Shift + 3
    '$' -> HidReport(0x02, 0x21)
    '%' -> HidReport(0x02, 0x22)
    '^' -> HidReport(0x02, 0x23)
    '&' -> HidReport(0x02, 0x24)
    '*' -> HidReport(0x02, 0x25)
    '(' -> HidReport(0x02, 0x26)
    ')' -> HidReport(0x02, 0x27)
    '-' -> HidReport(0x00, 0x2D)
    '_' -> HidReport(0x02, 0x2D)
    '=' -> HidReport(0x00, 0x2E)
    '+' -> HidReport(0x02, 0x2E)
    '[' -> HidReport(0x00, 0x2F)
    '{' -> HidReport(0x02, 0x2F)
    ']' -> HidReport(0x00, 0x30)
    '}' -> HidReport(0x02, 0x30)
    '#' -> HidReport(0x00, 0x32)
    '~' -> HidReport(0x02, 0x32)
    ';' -> HidReport(0x00, 0x33)
    ':' -> HidReport(0x02, 0x33)
    '\'' -> HidReport(0x00, 0x34)
    '@' -> HidReport(0x02, 0x34) // Shift + '
    '`' -> HidReport(0x00, 0x35)
    '\\' -> HidReport(0x00, 0x64)
    '|' -> HidReport(0x02, 0x64)
    ',' -> HidReport(0x00, 0x36)
    '<' -> HidReport(0x02, 0x36)
    '.' -> HidReport(0x00, 0x37)
    '>' -> HidReport(0x02, 0x37)
    '/' -> HidReport(0x00, 0x38)
    '?' -> HidReport(0x02, 0x38)
    ' ' -> HidReport(0x00, 0x2C)
    '\t' -> HidReport(0x00, 0x2B)
    '\n' -> HidReport(0x00, 0x28)
    else -> charToHidReportUs(c)
  }
}

/**
 * German (QWERTZ) layout mapping.
 */
private fun charToHidReportDe(c: Char): HidReport? {
  if (c in 'a'..'z') {
    val code = when (c) {
      'y' -> 0x1D.toByte()
      'z' -> 0x15.toByte()
      else -> (0x04 + (c - 'a')).toByte()
    }
    return HidReport(0x00, code)
  }
  if (c in 'A'..'Z') {
    val code = when (c) {
      'Y' -> 0x1D.toByte()
      'Z' -> 0x15.toByte()
      else -> (0x04 + (c - 'A')).toByte()
    }
    return HidReport(0x02, code)
  }
  if (c in '1'..'9') return HidReport(0x00, (0x1E + (c - '1')).toByte())
  if (c == '0') return HidReport(0x00, 0x27)
  return when (c) {
    '!' -> HidReport(0x02, 0x1E)
    '"' -> HidReport(0x02, 0x1F)
    '§' -> HidReport(0x02, 0x20)
    '$' -> HidReport(0x02, 0x21)
    '%' -> HidReport(0x02, 0x22)
    '&' -> HidReport(0x02, 0x23)
    '/' -> HidReport(0x02, 0x24)
    '(' -> HidReport(0x02, 0x25)
    ')' -> HidReport(0x02, 0x26)
    '=' -> HidReport(0x02, 0x27)
    '?' -> HidReport(0x02, 0x2D)
    '+' -> HidReport(0x00, 0x30)
    '*' -> HidReport(0x02, 0x30)
    '#' -> HidReport(0x00, 0x31)
    '\'' -> HidReport(0x02, 0x31)
    ',' -> HidReport(0x00, 0x36)
    ';' -> HidReport(0x02, 0x36)
    '.' -> HidReport(0x00, 0x37)
    ':' -> HidReport(0x02, 0x37)
    '-' -> HidReport(0x00, 0x38)
    '_' -> HidReport(0x02, 0x38)
    ' ' -> HidReport(0x00, 0x2C)
    '\t' -> HidReport(0x00, 0x2B)
    '\n' -> HidReport(0x00, 0x28)
    else -> charToHidReportUs(c)
  }
}

/**
 * French (AZERTY) layout mapping.
 */
private fun charToHidReportFr(c: Char): HidReport? {
  return when (c) {
    'a' -> HidReport(0x00, 0x14)
    'A' -> HidReport(0x02, 0x14)
    'q' -> HidReport(0x00, 0x04)
    'Q' -> HidReport(0x02, 0x04)
    'z' -> HidReport(0x00, 0x1A)
    'Z' -> HidReport(0x02, 0x1A)
    'w' -> HidReport(0x00, 0x1D)
    'W' -> HidReport(0x02, 0x1D)
    'm' -> HidReport(0x00, 0x33)
    'M' -> HidReport(0x02, 0x33)
    in '1'..'9' -> HidReport(0x02, (0x1E + (c - '1')).toByte())
    '0' -> HidReport(0x02, 0x27)
    ':' -> HidReport(0x00, 0x37)
    ';' -> HidReport(0x00, 0x36)
    ',' -> HidReport(0x00, 0x10)
    '=' -> HidReport(0x00, 0x2E)
    '+' -> HidReport(0x02, 0x2E)
    '/' -> HidReport(0x02, 0x37)
    ' ' -> HidReport(0x00, 0x2C)
    '\t' -> HidReport(0x00, 0x2B)
    '\n' -> HidReport(0x00, 0x28)
    else -> {
      if (c in 'a'..'z') HidReport(0x00, (0x04 + (c - 'a')).toByte())
      else if (c in 'A'..'Z') HidReport(0x02, (0x04 + (c - 'A')).toByte())
      else charToHidReportUs(c)
    }
  }
}

/**
 * Spanish layout mapping.
 */
private fun charToHidReportEs(c: Char): HidReport? {
  if (c in 'a'..'z') return HidReport(0x00, (0x04 + (c - 'a')).toByte())
  if (c in 'A'..'Z') return HidReport(0x02, (0x04 + (c - 'A')).toByte())
  if (c in '1'..'9') return HidReport(0x00, (0x1E + (c - '1')).toByte())
  if (c == '0') return HidReport(0x00, 0x27)
  return when (c) {
    '/' -> HidReport(0x02, 0x24)
    '(' -> HidReport(0x02, 0x25)
    ')' -> HidReport(0x02, 0x26)
    '=' -> HidReport(0x02, 0x27)
    '?' -> HidReport(0x02, 0x2D)
    '-' -> HidReport(0x00, 0x38)
    '_' -> HidReport(0x02, 0x38)
    ':' -> HidReport(0x02, 0x37)
    ';' -> HidReport(0x02, 0x36)
    ',' -> HidReport(0x00, 0x36)
    '.' -> HidReport(0x00, 0x37)
    ' ' -> HidReport(0x00, 0x2C)
    '\t' -> HidReport(0x00, 0x2B)
    '\n' -> HidReport(0x00, 0x28)
    else -> charToHidReportUs(c)
  }
}

/**
 * Parses full Ducky script (Hak5 standard v1/v2 compatible) line by line into ScriptActions.
 * Supports:
 * - STRING <text>
 * - STRINGLN <text>
 * - DELAY <ms>
 * - DEFAULT_DELAY / DEFAULTDELAY <ms>
 * - REPEAT <count>
 * - REM / // / # comments
 * - Single keys (ENTER, ESC, TAB, SPACE, BACKSPACE, DELETE, CAPSLOCK, F1-F24, arrows, etc.)
 * - Modifiers (GUI, WINDOWS, WIN, COMMAND, CTRL, ALT, SHIFT) alone or combined with keys
 * - Multi-modifier combinations with hyphens or spaces (e.g. CTRL-ALT DELETE, CTRL-SHIFT ENTER, ALT-F4)
 */
fun parseScript(script: String, layout: KeyboardLayout = KeyboardLayout.US): List<ScriptAction> {
  val actions = mutableListOf<ScriptAction>()
  val lines = script.lines()
  var lastCommandActions = listOf<ScriptAction>()
  var defaultDelayMs = 0L

  for ((index, rawLine) in lines.withIndex()) {
    val lineNumber = index + 1
    val trimmed = rawLine.trim()
    if (trimmed.isEmpty()) continue
    if (trimmed.startsWith("REM", ignoreCase = true) ||
        trimmed.startsWith("//") ||
        trimmed.startsWith("#")
    ) continue

    val currentLineActions = mutableListOf<ScriptAction>()

    if (trimmed.startsWith("STRINGLN ", ignoreCase = true)) {
      val text = rawLine.substring(rawLine.indexOf(trimmed.substring(0, 8)) + 9)
      for (char in text) {
        val report = charToHidReport(char, layout) ?: continue
        currentLineActions.add(ScriptAction.KeyPress(report, lineNumber))
        if (defaultDelayMs > 0) {
          currentLineActions.add(ScriptAction.Delay(defaultDelayMs, lineNumber))
        }
      }
      currentLineActions.add(ScriptAction.KeyPress(HidReport(0x00, 0x28), lineNumber))
    } else if (trimmed.equals("STRINGLN", ignoreCase = true)) {
      currentLineActions.add(ScriptAction.KeyPress(HidReport(0x00, 0x28), lineNumber))
    } else if (trimmed.startsWith("STRING ", ignoreCase = true)) {
      val text = rawLine.substring(rawLine.indexOf(trimmed.substring(0, 6)) + 7)
      for (char in text) {
        val report = charToHidReport(char, layout) ?: continue
        currentLineActions.add(ScriptAction.KeyPress(report, lineNumber))
        if (defaultDelayMs > 0) {
          currentLineActions.add(ScriptAction.Delay(defaultDelayMs, lineNumber))
        }
      }
    } else if (trimmed.equals("STRING", ignoreCase = true)) {
      continue
    } else if (trimmed.startsWith("DELAY ", ignoreCase = true) || trimmed.equals("DELAY", ignoreCase = true)) {
      val parts = trimmed.split(Regex("\\s+"))
      val delayMs = parts.getOrNull(1)?.toLongOrNull()
        ?: throw IllegalArgumentException("Invalid delay value on line $lineNumber: '$trimmed'")
      currentLineActions.add(ScriptAction.Delay(delayMs, lineNumber))
    } else if (trimmed.startsWith("DEFAULT_DELAY ", ignoreCase = true) ||
               trimmed.startsWith("DEFAULTDELAY ", ignoreCase = true)
    ) {
      val parts = trimmed.split(Regex("\\s+"))
      defaultDelayMs = parts.getOrNull(1)?.toLongOrNull() ?: 0L
    } else if (trimmed.startsWith("REPEAT ", ignoreCase = true)) {
      val parts = trimmed.split(Regex("\\s+"))
      val count = parts.getOrNull(1)?.toIntOrNull() ?: 1
      if (lastCommandActions.isNotEmpty()) {
        for (i in 1..count) {
          actions.addAll(lastCommandActions)
        }
      }
      continue
    } else {
      // Normalize hyphens in modifier combinations, e.g. "CTRL-ALT DEL" -> "CTRL ALT DEL"
      val normalized = trimmed.replace(
        Regex("(?i)(?<=\\b(CTRL|CONTROL|ALT|SHIFT|GUI|WINDOWS|WIN|COMMAND|OPTION))-(?=[a-zA-Z0-9])"),
        " "
      )
      val tokens = normalized.split(Regex("\\s+"))

      val modifierKeywords = setOf("GUI", "WINDOWS", "WIN", "COMMAND", "CTRL", "CONTROL", "ALT", "OPTION", "SHIFT")
      val hasModifier = tokens.any { it.uppercase(Locale.US) in modifierKeywords }

      if (hasModifier) {
        var modifier: Byte = 0
        var targetKeyToken: String? = null
        for (token in tokens) {
          when (token.uppercase(Locale.US)) {
            "GUI", "WINDOWS", "WIN", "COMMAND" -> modifier = (modifier.toInt() or 0x08).toByte()
            "CTRL", "CONTROL" -> modifier = (modifier.toInt() or 0x01).toByte()
            "ALT", "OPTION" -> modifier = (modifier.toInt() or 0x04).toByte()
            "SHIFT" -> modifier = (modifier.toInt() or 0x02).toByte()
            else -> targetKeyToken = token
          }
        }
        val keycode = if (targetKeyToken != null) {
          resolveKeycode(targetKeyToken)
            ?: throw IllegalArgumentException("Unrecognized key '$targetKeyToken' on line $lineNumber")
        } else {
          0x00.toByte() // Modifier alone, e.g. "GUI" opens Windows Start menu
        }
        currentLineActions.add(ScriptAction.KeyPress(HidReport(modifier, keycode), lineNumber))
      } else {
        val singleKeycode = resolveKeycode(tokens[0])
        if (tokens.size == 1 && singleKeycode != null) {
          currentLineActions.add(ScriptAction.KeyPress(HidReport(0x00, singleKeycode), lineNumber))
        } else {
          // Fallback: Plain typing of individual characters
          for (char in trimmed) {
            val report = charToHidReport(char, layout)
            if (report != null) {
              currentLineActions.add(ScriptAction.KeyPress(report, lineNumber))
              if (defaultDelayMs > 0) {
                currentLineActions.add(ScriptAction.Delay(defaultDelayMs, lineNumber))
              }
            }
          }
        }
      }
    }

    if (currentLineActions.isNotEmpty()) {
      actions.addAll(currentLineActions)
      lastCommandActions = currentLineActions
    }
  }
  return actions
}

data class HidNodeStatus(
  val name: String,
  val isCharDevice: Boolean
)

/**
 * Parses ls -ld or ls -l output to extract hidg node names and whether they are character devices.
 */
fun parseLsOutput(output: String): List<HidNodeStatus> {
  val result = mutableListOf<HidNodeStatus>()
  for (rawLine in output.lines()) {
    val line = rawLine.trim()
    if (line.isEmpty()) continue
    if (line.contains("No such file", ignoreCase = true)) continue
    val hidIdx = line.lastIndexOf("/dev/hidg")
    if (hidIdx != -1) {
      val fullPath = line.substring(hidIdx).trim()
      val name = fullPath.substringAfterLast('/')
      val isChar = line.startsWith("c")
      if (name.isNotEmpty()) {
        result.add(HidNodeStatus(name = name, isCharDevice = isChar))
      }
    } else {
      val tokens = line.split(Regex("\\s+"))
      val candidate = tokens.lastOrNull()?.substringAfterLast('/') ?: ""
      if (candidate.startsWith("hidg")) {
        val isChar = line.startsWith("c")
        result.add(HidNodeStatus(name = candidate, isCharDevice = isChar))
      }
    }
  }
  return result
}

/**
 * Checks if a node in /dev is a character device using Os.stat and root shell fallback.
 */
fun checkIsCharacterDevice(nodeName: String): Boolean {
  if (nodeName.isBlank()) return false
  try {
    val stat = Os.stat("/dev/$nodeName")
    return OsConstants.S_ISCHR(stat.st_mode)
  } catch (_: Throwable) {
  }

  return try {
    val p = Runtime.getRuntime().exec(arrayOf("su", "-c", "[ -c /dev/$nodeName ] && echo 1 || echo 0"))
    val out = p.inputStream.bufferedReader().use { it.readText() }.trim()
    p.waitFor()
    out == "1"
  } catch (_: Exception) {
    false
  }
}

/**
 * Scans /dev directory for hidg* entries using su -c "ls -ld /dev/hidg*" and fallback.
 */
fun scanHidNodesWithStatus(): List<HidNodeStatus> {
  try {
    val p = Runtime.getRuntime().exec(arrayOf("su", "-c", "ls -ld /dev/hidg* 2>/dev/null"))
    val out = p.inputStream.bufferedReader().use { it.readText() }
    p.waitFor()
    val parsed = parseLsOutput(out)
    if (parsed.isNotEmpty()) {
      return parsed.sortedBy { it.name }
    }
  } catch (_: Exception) {}

  val result = mutableListOf<HidNodeStatus>()
  try {
    val devDir = File("/dev")
    val files = devDir.listFiles { _, name -> name.startsWith("hidg") }
    files?.forEach { file ->
      val isChar = try {
        val stat = Os.stat(file.absolutePath)
        OsConstants.S_ISCHR(stat.st_mode)
      } catch (_: Throwable) {
        false
      }
      result.add(HidNodeStatus(name = file.name, isCharDevice = isChar))
    }
  } catch (_: Exception) {}

  return result.sortedBy { it.name }
}

fun scanHidNodes(): List<String> {
  return scanHidNodesWithStatus().map { it.name }
}

/**
 * Formats a raw device node name like 'hidg0' or 'hidg1' into a clean user-facing name,
 * removing raw 'hidg' keywords.
 */
fun formatInterfaceDisplayName(nodeName: String): String {
  if (nodeName.isBlank()) return "Select Interface"
  return if (nodeName.startsWith("hidg", ignoreCase = true)) {
    val number = nodeName.substring(4)
    if (number.isNotEmpty()) "Interface $number" else "Interface"
  } else {
    nodeName
  }
}

/**
 * Executes parsed script actions using direct character device stream or root process to /dev/hidg*.
 * keyDelayMs = 0L by default for instant hardware-speed typing.
 */
suspend fun executeScript(
  nodePath: String,
  actions: List<ScriptAction>,
  keyDelayMs: Long = 0L,
  isActive: () -> Boolean = { true }
): Result<Unit> = withContext(Dispatchers.IO) {
  var process: Process? = null
  var directStream: OutputStream? = null
  var failingLine = 1
  try {
    // Attempt to grant node read/write permissions via root
    try {
      val chmodP = Runtime.getRuntime().exec(arrayOf("su", "-c", "chmod 666 $nodePath"))
      chmodP.waitFor()
    } catch (_: Exception) {}

    // Check if we can write directly to the character device node without shell pipe overhead
    val targetFile = File(nodePath)
    if (targetFile.exists() && targetFile.canWrite()) {
      try {
        directStream = FileOutputStream(targetFile)
      } catch (_: Exception) {
        directStream = null
      }
    }

    val out: OutputStream = if (directStream != null) {
      directStream
    } else {
      val p = Runtime.getRuntime().exec(arrayOf("su", "-c", "cat > $nodePath"))
      process = p
      p.outputStream
    }

    // When writing through fallback root shell pipe, a 1ms micro-interval avoids
    // toybox cat buffering/coalescing multiple 8-byte reports into a single write syscall (>8 bytes causes EINVAL in f_hidg)
    val effectivePressDelay = if (keyDelayMs > 0L) keyDelayMs else if (directStream != null) 0L else 1L
    val effectiveReleaseDelay = if (keyDelayMs > 0L) keyDelayMs else if (directStream != null) 0L else 1L

    for (action in actions) {
      if (!isActive()) {
        try { out.close() } catch (_: Exception) {}
        process?.destroy()
        return@withContext Result.failure(Exception("Stopped by user"))
      }
      failingLine = action.sourceLineNumber
      when (action) {
        is ScriptAction.Delay -> {
          delay(action.ms)
        }
        is ScriptAction.KeyPress -> {
          val press = action.report.toPressByteArray()
          out.write(press)
          out.flush()

          if (effectivePressDelay > 0L) {
            delay(effectivePressDelay)
          }

          out.write(HidReport.RELEASE_REPORT)
          out.flush()

          if (effectiveReleaseDelay > 0L) {
            delay(effectiveReleaseDelay)
          }
        }
      }
    }

    try {
      out.close()
    } catch (_: Exception) {}

    process?.waitFor()
    Result.success(Unit)
  } catch (e: Exception) {
    try {
      directStream?.close()
      process?.destroy()
    } catch (_: Exception) {}
    Result.failure(Exception("Error on line $failingLine: ${e.message}", e))
  }
}

fun getDuckyDir(context: Context): File {
  val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "ducky_scripts")
  if (!dir.exists()) {
    dir.mkdirs()
  }
  return dir
}

fun saveScriptToFile(context: Context, filename: String, content: String): Result<String> {
  return try {
    val dir = getDuckyDir(context)
    val cleanName = if (filename.isBlank()) {
      "script_${System.currentTimeMillis()}.txt"
    } else {
      var name = filename.trim()
      if (!name.endsWith(".txt", ignoreCase = true)) {
        name += ".txt"
      }
      name
    }
    val file = File(dir, cleanName)
    file.writeText(content)
    Result.success(cleanName)
  } catch (e: Exception) {
    Result.failure(e)
  }
}

fun listSavedScripts(context: Context): List<File> {
  return try {
    val dir = getDuckyDir(context)
    dir.listFiles { _, name -> name.endsWith(".txt", ignoreCase = true) }
      ?.sortedByDescending { it.lastModified() }
      ?: emptyList()
  } catch (e: Exception) {
    emptyList()
  }
}
