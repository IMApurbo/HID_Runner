package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testHidKeycodesAndModifiers() {
    val reportA = charToHidReport('a')!!
    assertEquals(0.toByte(), reportA.modifier)
    assertEquals(0x04.toByte(), reportA.keycode)

    val reportUpperA = charToHidReport('A')!!
    assertEquals(0x02.toByte(), reportUpperA.modifier)
    assertEquals(0x04.toByte(), reportUpperA.keycode)

    val reportExclamation = charToHidReport('!')!!
    assertEquals(0x02.toByte(), reportExclamation.modifier)
    assertEquals(0x1E.toByte(), reportExclamation.keycode)

    val reportZero = charToHidReport('0')!!
    assertEquals(0.toByte(), reportZero.modifier)
    assertEquals(0x27.toByte(), reportZero.keycode)

    assertEquals(0x28.toByte(), resolveKeycode("ENTER"))
    assertEquals(0x29.toByte(), resolveKeycode("ESC"))
    assertEquals(0x2A.toByte(), resolveKeycode("BACKSPACE"))
    assertEquals(0x2B.toByte(), resolveKeycode("TAB"))
    assertEquals(0x2C.toByte(), resolveKeycode("SPACE"))
    assertEquals(0x4C.toByte(), resolveKeycode("DELETE"))
    assertEquals(0x4C.toByte(), resolveKeycode("DEL"))
    assertEquals(0x4F.toByte(), resolveKeycode("RIGHT"))
    assertEquals(0x50.toByte(), resolveKeycode("LEFT"))
    assertEquals(0x51.toByte(), resolveKeycode("DOWN"))
    assertEquals(0x52.toByte(), resolveKeycode("UP"))
    assertEquals(0x39.toByte(), resolveKeycode("CAPSLOCK"))
    assertEquals(0x3A.toByte(), resolveKeycode("F1"))
    assertEquals(0x45.toByte(), resolveKeycode("F12"))
    assertEquals(0x46.toByte(), resolveKeycode("PRINTSCREEN"))
    assertEquals(0x49.toByte(), resolveKeycode("INSERT"))
    assertEquals(0x4A.toByte(), resolveKeycode("HOME"))
    assertEquals(0x4D.toByte(), resolveKeycode("END"))
    assertEquals(0x4B.toByte(), resolveKeycode("PAGEUP"))
    assertEquals(0x4E.toByte(), resolveKeycode("PAGEDOWN"))
  }

  @Test
  fun testAllAsciiCharactersSupported() {
    val asciiChars = " !\"#\$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~"
    for (char in asciiChars) {
      val report = charToHidReport(char)
      assertNotNull("Character '$char' (code ${char.code}) should have a valid HID report", report)
    }
  }

  @Test
  fun testParseScriptStandardCommands() {
    val script = """
      REM This is a comment
      STRING abc
      DELAY 250
      ENTER
      GUI r
      CTRL c
    """.trimIndent()

    val actions = parseScript(script)
    assertEquals(7, actions.size)

    assertTrue(actions[0] is ScriptAction.KeyPress)
    assertEquals(0x04.toByte(), (actions[0] as ScriptAction.KeyPress).report.keycode)

    assertTrue(actions[3] is ScriptAction.Delay)
    assertEquals(250L, (actions[3] as ScriptAction.Delay).ms)

    assertTrue(actions[4] is ScriptAction.KeyPress)
    assertEquals(0x28.toByte(), (actions[4] as ScriptAction.KeyPress).report.keycode)

    assertTrue(actions[5] is ScriptAction.KeyPress)
    val guiR = actions[5] as ScriptAction.KeyPress
    assertEquals(0x08.toByte(), guiR.report.modifier)
    assertEquals(resolveKeycode("r"), guiR.report.keycode)

    assertTrue(actions[6] is ScriptAction.KeyPress)
    val ctrlC = actions[6] as ScriptAction.KeyPress
    assertEquals(0x01.toByte(), ctrlC.report.modifier)
    assertEquals(resolveKeycode("c"), ctrlC.report.keycode)
  }

  @Test
  fun testStringLnAppendsEnter() {
    val script = "STRINGLN test"
    val actions = parseScript(script)
    // 4 characters (t, e, s, t) + ENTER
    assertEquals(5, actions.size)
    val lastAction = actions.last() as ScriptAction.KeyPress
    assertEquals(0x28.toByte(), lastAction.report.keycode) // ENTER
  }

  @Test
  fun testRepeatCommand() {
    val script = """
      SPACE
      REPEAT 3
    """.trimIndent()
    val actions = parseScript(script)
    // 1 original SPACE + 3 repeated = 4 total
    assertEquals(4, actions.size)
    for (action in actions) {
      assertTrue(action is ScriptAction.KeyPress)
      assertEquals(0x2C.toByte(), (action as ScriptAction.KeyPress).report.keycode)
    }
  }

  @Test
  fun testHyphenatedModifiers() {
    val script = """
      CTRL-ALT DELETE
      CTRL-SHIFT ENTER
      ALT-F4
      GUI-R
      GUI
    """.trimIndent()
    val actions = parseScript(script)
    assertEquals(5, actions.size)

    // CTRL-ALT DELETE
    val ctrlAltDel = actions[0] as ScriptAction.KeyPress
    assertEquals(0x05.toByte(), ctrlAltDel.report.modifier) // 0x01 (Ctrl) | 0x04 (Alt)
    assertEquals(0x4C.toByte(), ctrlAltDel.report.keycode) // DELETE

    // CTRL-SHIFT ENTER
    val ctrlShiftEnter = actions[1] as ScriptAction.KeyPress
    assertEquals(0x03.toByte(), ctrlShiftEnter.report.modifier) // 0x01 | 0x02
    assertEquals(0x28.toByte(), ctrlShiftEnter.report.keycode) // ENTER

    // ALT-F4
    val altF4 = actions[2] as ScriptAction.KeyPress
    assertEquals(0x04.toByte(), altF4.report.modifier)
    assertEquals(0x3D.toByte(), altF4.report.keycode) // F4

    // GUI-R
    val guiR = actions[3] as ScriptAction.KeyPress
    assertEquals(0x08.toByte(), guiR.report.modifier)
    assertEquals(resolveKeycode("r"), guiR.report.keycode)

    // GUI standalone (opens start menu)
    val gui = actions[4] as ScriptAction.KeyPress
    assertEquals(0x08.toByte(), gui.report.modifier)
    assertEquals(0x00.toByte(), gui.report.keycode)
  }

  @Test
  fun testAllDemoScriptsParseSuccessfully() {
    val scripts = DemoScriptsRepository.scripts
    assertEquals("Should have exactly 30 demo scripts", 30, scripts.size)
    for (demo in scripts) {
      val actions = parseScript(demo.script)
      assertTrue("Script '${demo.title}' should parse into actions", actions.isNotEmpty())
    }
  }

  @Test
  fun testParseLsOutput() {
    val lsOutput = """
      -rw-rw-rw- 1 root root        9 2026-09-15 15:52 /dev/hidg0
      crw------- 1 root root 464,   1 2026-09-15 16:22 /dev/hidg1
    """.trimIndent()

    val nodes = parseLsOutput(lsOutput)
    assertEquals(2, nodes.size)

    assertEquals("hidg0", nodes[0].name)
    assertEquals(false, nodes[0].isCharDevice)

    assertEquals("hidg1", nodes[1].name)
    assertEquals(true, nodes[1].isCharDevice)
  }

  @Test
  fun testKeyboardLayoutMappingJisFixesColonAndEquals() {
    // US layout sends keycode 0x33 with Shift for ':', and keycode 0x2E unshifted for '='
    val usColon = charToHidReport(':', KeyboardLayout.US)!!
    assertEquals(0x02.toByte(), usColon.modifier)
    assertEquals(0x33.toByte(), usColon.keycode)

    val usEquals = charToHidReport('=', KeyboardLayout.US)!!
    assertEquals(0x00.toByte(), usEquals.modifier)
    assertEquals(0x2E.toByte(), usEquals.keycode)

    // Under JIS layout, key 0x34 is ':' (unshifted) and Shift+0x2D is '='!
    // This fixes the issue where ':' typed '+' and '=' typed '^' on Japanese systems.
    val jisColon = charToHidReport(':', KeyboardLayout.JIS)!!
    assertEquals(0x00.toByte(), jisColon.modifier) // Unshifted key 0x34
    assertEquals(0x34.toByte(), jisColon.keycode)

    val jisEquals = charToHidReport('=', KeyboardLayout.JIS)!!
    assertEquals(0x02.toByte(), jisEquals.modifier) // Shift + Key 0x2D
    assertEquals(0x2D.toByte(), jisEquals.keycode)

    val jisPlus = charToHidReport('+', KeyboardLayout.JIS)!!
    assertEquals(0x02.toByte(), jisPlus.modifier) // Shift + Key 0x33
    assertEquals(0x33.toByte(), jisPlus.keycode)

    val jisCaret = charToHidReport('^', KeyboardLayout.JIS)!!
    assertEquals(0x00.toByte(), jisCaret.modifier) // Unshifted Key 0x2E
    assertEquals(0x2E.toByte(), jisCaret.keycode)
  }

  @Test
  fun testAllLayoutsSupportCommonAsciiCharacters() {
    val commonAscii = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789://?&=.-_"
    for (layout in KeyboardLayout.values()) {
      for (char in commonAscii) {
        val report = charToHidReport(char, layout)
        assertNotNull("Character '$char' should have valid report for layout ${layout.displayName}", report)
      }
    }
  }

  @Test
  fun testParseScriptWithJisLayout() {
    val script = "STRING https://www.youtube.com/watch?v=dQw4w9WgXcQ"
    val actions = parseScript(script, KeyboardLayout.JIS)
    assertEquals(script.removePrefix("STRING ").length, actions.size)

    // The ':' character is at index 5 in the string
    val colonAction = actions[5] as ScriptAction.KeyPress
    assertEquals("JIS colon should be 0x34 unshifted", 0x34.toByte(), colonAction.report.keycode)
    assertEquals("JIS colon should have no Shift modifier", 0x00.toByte(), colonAction.report.modifier)
  }

  @Test
  fun testFormatInterfaceDisplayName() {
    assertEquals("Interface 0", formatInterfaceDisplayName("hidg0"))
    assertEquals("Interface 1", formatInterfaceDisplayName("hidg1"))
    assertEquals("Interface 2", formatInterfaceDisplayName("hidg2"))
    assertEquals("custom_node", formatInterfaceDisplayName("custom_node"))
  }

  @Test
  fun testAutoExecuteScriptParsing() {
    val sampleScript = "STRING AutoExecTest\nENTER"
    val actions = parseScript(sampleScript, KeyboardLayout.US)
    assertTrue("Should contain parsed keypresses", actions.isNotEmpty())
  }

  @Test
  fun testStringParsingWithoutDefaultDelayProducesOnlyKeyPresses() {
    val sampleScript = "STRING Instant"
    val actions = parseScript(sampleScript, KeyboardLayout.US)
    // 7 characters in "Instant" -> exactly 7 KeyPress actions, 0 Delay actions
    assertEquals(7, actions.size)
    assertTrue(actions.all { it is ScriptAction.KeyPress })
  }
}
