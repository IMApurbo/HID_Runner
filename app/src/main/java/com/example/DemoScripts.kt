package com.example

data class DemoScript(
  val id: String,
  val title: String,
  val targetOs: String,
  val category: String,
  val description: String,
  val script: String
)

object DemoScriptsRepository {
  val scripts: List<DemoScript> = listOf(
    DemoScript(
      id = "rickroll_win",
      title = "Classic Rickroll (Browser)",
      targetOs = "Windows",
      category = "Harmless Prank",
      description = "Opens the default browser and plays Rick Astley's 'Never Gonna Give You Up'.",
      script = """
        REM Classic Rickroll for Windows
        REM Author: IMApurbo
        DELAY 500
        GUI r
        DELAY 400
        STRING https://www.youtube.com/watch?v=dQw4w9WgXcQ
        ENTER
      """.trimIndent()
    ),
    DemoScript(
      id = "matrix_rain_win",
      title = "Matrix Terminal Rain",
      targetOs = "Windows",
      category = "Visual FX",
      description = "Opens command prompt in green-on-black and launches a fast scrolling file tree matrix effect.",
      script = """
        REM Green Matrix Rain effect
        DELAY 500
        GUI r
        DELAY 400
        STRING cmd /k color 0a && cls && tree C:\
        ENTER
      """.trimIndent()
    ),
    DemoScript(
      id = "talking_computer_win",
      title = "Talking Computer (Voice Synth)",
      targetOs = "Windows",
      category = "Speech & Audio",
      description = "Uses built-in Windows SpeechSynthesizer to speak aloud through the speakers.",
      script = """
        REM Harmless text-to-speech voice
        DELAY 500
        GUI r
        DELAY 400
        STRING powershell -c "Add-Type -AssemblyName System.Speech; (New-Object System.Speech.Synthesis.SpeechSynthesizer).Speak('Attention! Your keyboard has gained artificial consciousness.')"
        ENTER
      """.trimIndent()
    ),
    DemoScript(
      id = "fake_update_win",
      title = "Fake Windows 10/11 Update",
      targetOs = "Windows",
      category = "Harmless Prank",
      description = "Opens an endlessly spinning fake Windows Update screen in fullscreen browser.",
      script = """
        REM Fake Windows Update screen
        DELAY 500
        GUI r
        DELAY 400
        STRING https://fakeupdate.net/win10ue/
        ENTER
        DELAY 1500
        F11
      """.trimIndent()
    ),
    DemoScript(
      id = "notepad_ghost_win",
      title = "Notepad Ghost Writer",
      targetOs = "Windows",
      category = "Fun Demo",
      description = "Opens Notepad and types a friendly autonomous ghost-writer message.",
      script = """
        REM Ghost Writer typing in Notepad
        DELAY 500
        GUI r
        DELAY 400
        STRING notepad
        ENTER
        DELAY 700
        STRING Greetings from HID Interface Runner!
        ENTER
        STRING This text is typed automatically over USB HID hardware.
        ENTER
        STRING Designed and developed by IMApurbo.
        ENTER
      """.trimIndent()
    ),
    DemoScript(
      id = "calc_frenzy_win",
      title = "Calculator Pop-up Trio",
      targetOs = "Windows",
      category = "Harmless Prank",
      description = "Spawns 3 calculator windows in rapid succession.",
      script = """
        REM Harmless calculator spawner
        DELAY 500
        GUI r
        DELAY 300
        STRING calc
        ENTER
        DELAY 300
        GUI r
        DELAY 300
        STRING calc
        ENTER
        DELAY 300
        GUI r
        DELAY 300
        STRING calc
        ENTER
      """.trimIndent()
    ),
    DemoScript(
      id = "fake_bsod_win",
      title = "Fake Blue Screen of Death (BSOD)",
      targetOs = "Windows",
      category = "Harmless Prank",
      description = "Launches an authentic-looking fake web BSOD in fullscreen (Press F11 or Esc to exit).",
      script = """
        REM Harmless Fake BSOD screen
        DELAY 500
        GUI r
        DELAY 400
        STRING https://fakeupdate.net/bsod/
        ENTER
        DELAY 1500
        F11
      """.trimIndent()
    ),
    DemoScript(
      id = "speaker_beeps_win",
      title = "Motherboard Beep Chime",
      targetOs = "Windows",
      category = "Speech & Audio",
      description = "Plays a gentle 3-note audio chime using the console frequency beeper.",
      script = """
        REM Play console audio beeps
        DELAY 500
        GUI r
        DELAY 400
        STRING powershell -c "[console]::beep(440,250); [console]::beep(554,250); [console]::beep(659,400)"
        ENTER
      """.trimIndent()
    ),
    DemoScript(
      id = "system_info_win",
      title = "Quick System Spec Summary",
      targetOs = "Windows",
      category = "Dev Utility",
      description = "Opens CMD and executes systeminfo to display OS version, CPU, RAM, and network specs.",
      script = """
        REM Display host hardware & OS specs
        DELAY 500
        GUI r
        DELAY 400
        STRING cmd /k systeminfo
        ENTER
      """.trimIndent()
    ),
    DemoScript(
      id = "popup_message_win",
      title = "Custom Warning Dialog",
      targetOs = "Windows",
      category = "Harmless Prank",
      description = "Displays a native Windows warning popup with a humorous coffee alert.",
      script = """
        REM Display native MessageBox popup
        DELAY 500
        GUI r
        DELAY 400
        STRING powershell -c "[System.Windows.Forms.MessageBox]::Show('Notice: Host developer coffee levels critically low!', 'System Diagnostics', 0, 48)"
        ENTER
      """.trimIndent()
    ),
    DemoScript(
      id = "ascii_banner_win",
      title = "Retro ASCII Art Banner",
      targetOs = "Windows",
      category = "Fun Demo",
      description = "Launches Notepad and renders a retro ASCII art banner.",
      script = """
        REM Draw ASCII Banner in Notepad
        DELAY 500
        GUI r
        DELAY 400
        STRING notepad
        ENTER
        DELAY 700
        STRINGLN   ___ __  __    _                     _           
        STRINGLN  |_ _|  \/  |  / \   _ __  _   _ _ __| |__   ___  
        STRINGLN   | || |\/| | / _ \ | '_ \| | | | '__| '_ \ / _ \ 
        STRINGLN   | || |  | |/ ___ \| |_) | |_| | |  | |_) | (_) |
        STRINGLN  |___|_|  |_/_/   \_\ .__/ \__,_|_|  |_.__/ \___/ 
        STRINGLN                     |_|                           
        STRINGLN   >> Powered by HID Interface Runner <<
      """.trimIndent()
    ),
    DemoScript(
      id = "coin_flip_win",
      title = "CLI Coin Flip Decider",
      targetOs = "Windows",
      category = "Dev Utility",
      description = "Runs a random coin flipper in PowerShell with colorful Heads or Tails output.",
      script = """
        REM PowerShell Random Coin Flip
        DELAY 500
        GUI r
        DELAY 400
        STRING powershell -c "if ((Get-Random -Min 0 -Max 2) -eq 1) { Write-Host 'RESULT: HEADS' -F Green } else { Write-Host 'RESULT: TAILS' -F Yellow }; pause"
        ENTER
      """.trimIndent()
    ),
    DemoScript(
      id = "flip_screen_win",
      title = "Rotate Display 180° (Intel Hotkey)",
      targetOs = "Windows",
      category = "Harmless Prank",
      description = "Triggers the classic Intel graphics orientation hotkey (Ctrl+Alt+Down). Restore with Ctrl+Alt+Up.",
      script = """
        REM Screen flip hotkey (Intel HD/UHD graphics)
        DELAY 500
        CTRL-ALT DOWN
      """.trimIndent()
    ),
    DemoScript(
      id = "restore_screen_win",
      title = "Restore Display Orientation",
      targetOs = "Windows",
      category = "Dev Utility",
      description = "Restores normal landscape screen orientation via Ctrl+Alt+Up hotkey.",
      script = """
        REM Screen restore hotkey
        DELAY 500
        CTRL-ALT UP
      """.trimIndent()
    ),
    DemoScript(
      id = "google_gravity_cross",
      title = "Google Gravity Experiment",
      targetOs = "Universal",
      category = "Fun Demo",
      description = "Opens the classic browser physics simulation where Google elements crash to the floor.",
      script = """
        REM Open Google Gravity simulation
        DELAY 500
        GUI r
        DELAY 400
        STRING https://mrdoob.com/projects/chromeexperiments/google-gravity/
        ENTER
      """.trimIndent()
    ),
    DemoScript(
      id = "lofi_music_cross",
      title = "Ambient Lo-Fi Chill Beats",
      targetOs = "Universal",
      category = "Fun Demo",
      description = "Opens YouTube in default browser with relaxing lo-fi hip hop study/relax radio.",
      script = """
        REM Open Lofi Girl stream
        DELAY 500
        GUI r
        DELAY 400
        STRING https://www.youtube.com/watch?v=jfKfPfyJRdk
        ENTER
      """.trimIndent()
    ),
    DemoScript(
      id = "mac_rickroll",
      title = "macOS Rickroll (Spotlight)",
      targetOs = "macOS",
      category = "Harmless Prank",
      description = "Uses Spotlight search on macOS to launch Rick Astley video in Safari.",
      script = """
        REM macOS Rickroll via Spotlight
        DELAY 500
        GUI SPACE
        DELAY 400
        STRING https://www.youtube.com/watch?v=dQw4w9WgXcQ
        ENTER
      """.trimIndent()
    ),
    DemoScript(
      id = "mac_talking_say",
      title = "macOS Voice Synthesizer (`say`)",
      targetOs = "macOS",
      category = "Speech & Audio",
      description = "Launches Terminal on macOS and runs the native `say` speech command.",
      script = """
        REM macOS Terminal Speech
        DELAY 500
        GUI SPACE
        DELAY 400
        STRING terminal
        ENTER
        DELAY 700
        STRING say "Hello from your phone over USB HID gadget. Developed by IMApurbo."
        ENTER
      """.trimIndent()
    ),
    DemoScript(
      id = "mac_starwars_ascii",
      title = "macOS Star Wars ASCII Animation",
      targetOs = "macOS",
      category = "Visual FX",
      description = "Opens Terminal and streams the ASCII movie stream via curl.",
      script = """
        REM macOS ASCII animation stream
        DELAY 500
        GUI SPACE
        DELAY 400
        STRING terminal
        ENTER
        DELAY 700
        STRING curl ascii.live/can-you-hear-me
        ENTER
      """.trimIndent()
    ),
    DemoScript(
      id = "linux_cmatrix",
      title = "Linux Matrix Rain / Top Monitor",
      targetOs = "Linux",
      category = "Visual FX",
      description = "Spawns a Linux GNOME/KDE terminal and runs cmatrix or top monitor.",
      script = """
        REM Linux Terminal Matrix Rain
        DELAY 500
        CTRL-ALT t
        DELAY 700
        STRING cmatrix || top
        ENTER
      """.trimIndent()
    ),
    DemoScript(
      id = "linux_disk_health",
      title = "Linux Storage & RAM Health Check",
      targetOs = "Linux",
      category = "Dev Utility",
      description = "Spawns Linux terminal and checks free disk space and RAM usage in human-readable units.",
      script = """
        REM Linux Disk & RAM summary
        DELAY 500
        CTRL-ALT t
        DELAY 700
        STRING df -h && free -h
        ENTER
      """.trimIndent()
    ),
    DemoScript(
      id = "linux_neofetch",
      title = "Linux Fastfetch / System Logo",
      targetOs = "Linux",
      category = "Dev Utility",
      description = "Spawns terminal and runs fastfetch / neofetch / uname system hardware banner.",
      script = """
        REM Linux Fastfetch hardware banner
        DELAY 500
        CTRL-ALT t
        DELAY 700
        STRING fastfetch || neofetch || uname -a
        ENTER
      """.trimIndent()
    ),
    DemoScript(
      id = "caps_lock_disco",
      title = "Keyboard Caps Lock Disco Strobe",
      targetOs = "Universal",
      category = "Fun Demo",
      description = "Rapidly toggles the keyboard Caps Lock LED indicator on the host keyboard.",
      script = """
        REM Caps Lock LED strobe
        DELAY 500
        CAPSLOCK
        DELAY 250
        CAPSLOCK
        DELAY 250
        CAPSLOCK
        DELAY 250
        CAPSLOCK
        REPEAT 3
      """.trimIndent()
    ),
    DemoScript(
      id = "space_jiggler",
      title = "Keep-Alive Spacebar Jiggler",
      targetOs = "Universal",
      category = "Dev Utility",
      description = "Emulates user presence by tapping and backspacing space to prevent screensaver timeout.",
      script = """
        REM Harmless keep-alive jiggle
        DELAY 1000
        SPACE
        DELAY 500
        BACKSPACE
        REPEAT 4
      """.trimIndent()
    ),
    DemoScript(
      id = "dino_game_runner",
      title = "Launch Chrome Offline Dino Game",
      targetOs = "Universal",
      category = "Fun Demo",
      description = "Navigates directly to the Chrome offline dinosaur running game and jumps to start.",
      script = """
        REM Open Chrome Dino Game
        DELAY 500
        GUI r
        DELAY 400
        STRING chrome://dino
        ENTER
        DELAY 1200
        SPACE
      """.trimIndent()
    ),
    DemoScript(
      id = "speed_typing_test",
      title = "Monkeytype Typing Benchmark",
      targetOs = "Universal",
      category = "Dev Utility",
      description = "Opens Monkeytype speed test and automatically types sample text to benchmark HID throughput.",
      script = """
        REM Monkeytype throughput tester
        DELAY 500
        GUI r
        DELAY 400
        STRING https://monkeytype.com
        ENTER
        DELAY 2500
        STRING the quick brown fox jumps over the lazy dog
        ENTER
      """.trimIndent()
    ),
    DemoScript(
      id = "random_wiki_cross",
      title = "Wikipedia Random Article Roulette",
      targetOs = "Universal",
      category = "Fun Demo",
      description = "Opens a completely random intriguing article from English Wikipedia in browser.",
      script = """
        REM Wikipedia Random Article
        DELAY 500
        GUI r
        DELAY 400
        STRING https://en.wikipedia.org/wiki/Special:Random
        ENTER
      """.trimIndent()
    ),
    DemoScript(
      id = "powershell_matrix_stream",
      title = "PowerShell Cyber Stream Generator",
      targetOs = "Windows",
      category = "Visual FX",
      description = "Generates streaming high-speed green alphanumeric character matrix in PowerShell.",
      script = """
        REM Pure PowerShell green matrix stream
        DELAY 500
        GUI r
        DELAY 400
        STRING powershell -c "${'$'}host.ui.rawui.ForegroundColor='Green'; 1..150 | % { Write-Host (-join ((33..126) | Get-Random -Count 50 | % {[char]${'$'}_})) -NoNewline; Start-Sleep -Milliseconds 40 }"
        ENTER
      """.trimIndent()
    ),
    DemoScript(
      id = "godmode_win",
      title = "Windows GodMode Super Control Panel",
      targetOs = "Windows",
      category = "Dev Utility",
      description = "Creates the famous secret Windows GodMode shortcut on Desktop with 200+ advanced settings.",
      script = """
        REM Create GodMode shortcut on Desktop
        DELAY 500
        GUI r
        DELAY 400
        STRING cmd /c "mkdir \"%USERPROFILE%\Desktop\All Tasks GodMode.{ED7BA470-8E54-465E-825C-99712043E01C}\""
        ENTER
      """.trimIndent()
    ),
    DemoScript(
      id = "hid_symbol_audit",
      title = "Full Keyboard Symbol Verification Test",
      targetOs = "Universal",
      category = "Dev Utility",
      description = "Types every standard ASCII symbol, number, and case to verify 100% keycode mapping accuracy.",
      script = """
        REM Comprehensive HID Symbol Integrity Test
        DELAY 500
        STRING Standard ASCII Test:
        ENTER
        STRING ABCDEFGHIJKLMNOPQRSTUVWXYZ
        ENTER
        STRING abcdefghijklmnopqrstuvwxyz
        ENTER
        STRING 0123456789
        ENTER
        STRING Symbols: !@#${'$'}%^&*()_+-=[]{}\\|;:'\",.<>/?`~
        ENTER
        STRING Finished verification successfully!
        ENTER
      """.trimIndent()
    )
  )
}
