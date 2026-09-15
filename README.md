<div align="center">
  <img src="app/src/main/res/drawable/hid_runner_icon_1789466876061.jpg" alt="HID Interface Runner" width="120" height="120" style="border-radius: 24px;" />
  <h1>HID Interface Runner</h1>
  <p><strong>Send keyboard input to any connected host over USB HID — from your Android device.</strong></p>

  ![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)
  ![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white)
  ![Root Required](https://img.shields.io/badge/Requires-Root-red)
  ![License](https://img.shields.io/badge/License-MIT-blue)
  ![Release](https://img.shields.io/github/v/release/IMApurbo/HID_Runner?include_prereleases)
</div>

---

## Overview

**HID Interface Runner** is an Android developer and accessibility utility that lets you write and execute DuckyScript-style scripts over a USB HID gadget interface (`/dev/hidg*`). Your Android device becomes a **USB keyboard** — able to type text, trigger hotkeys, and automate input on any connected host (Windows, macOS, or Linux) without installing anything on the target machine.

> ⚠️ **Root access is required.** HID gadget interfaces (`/dev/hidg*`) are only accessible with root permissions. This app checks and requests root at launch.

### Why not just use Rucky?

Tools like [Rucky](https://github.com/mayankmetha/Rucky) are a popular choice for USB HID scripting on Android — but they come with a hard dependency: a **custom kernel** that has the HID gadget module compiled in. On stock or semi-stock kernels — even on a fully rooted device — Rucky simply won't find a valid HID interface and will refuse to work at all. This rules out a large portion of rooted devices that don't have a custom kernel available.

**HID Interface Runner is designed to be universal.** Instead of relying on a pre-built kernel module, it works with any rooted device that can expose `/dev/hidg*` nodes through the standard Linux `configfs` USB gadget framework. Pair it with [android-usb-gadget](https://github.com/tejado/android-usb-gadget) to enable HID on kernels that support configfs gadgets — which covers a **much wider range of devices** than Rucky's approach — and you're up and running without compiling or flashing a custom kernel.

| | Rucky | HID Interface Runner |
|---|---|---|
| Requires custom kernel | ✅ Yes | ❌ No |
| Works on stock/semi-stock kernels | ❌ No | ✅ Yes |
| Needs root | ✅ Yes | ✅ Yes |
| Uses configfs (`android-usb-gadget`) | ❌ | ✅ |
| Device coverage | Narrow | Wide |

In short: if Rucky works for you, great. If it doesn't — because your device doesn't have a compatible custom kernel — HID Interface Runner is built exactly for that situation.

---

## Features

### 🖊️ Script Editor & Runner
- Built-in DuckyScript-compatible editor with a live **Run / Stop** toggle
- Script execution with real-time status feedback via snackbar notifications
- Load scripts from storage or save your own

### ⌨️ Multi-Layout Keyboard Support
Switch between keyboard layouts so keycodes are mapped correctly for the target host:

| Layout | Description |
|--------|-------------|
| `US`   | Standard QWERTY (default) |
| `UK`   | Great Britain QWERTY |
| `DE`   | German QWERTZ |
| `FR`   | French AZERTY |
| `ES`   | Spanish QWERTY |
| `JIS`  | Japanese JIS 106/109 |

### ⚡ Auto Execute on USB Connect
A trigger mode that **arms** the script and fires it automatically the moment your device is plugged into a host — no manual tap needed.

How it works:
1. Write your script in the editor
2. Tap **Auto Execute** — the app enters armed/waiting state (shown with a banner)
3. Plug your Android device into the target host via USB
4. The app detects the connection instantly and runs the script without any further input

Detection uses a three-layer approach for maximum reliability:
- Android's `USB_STATE` broadcast (`connected` / `configured` extras)
- `ACTION_POWER_CONNECTED` broadcast as a fallback
- Direct sysfs polling via root (`/sys/class/udc/*/state`, `/sys/class/android_usb/android0/state`) for devices where broadcasts are delayed

Tap **Cancel Auto** at any time to disarm. Auto Execute requires root and a valid HID character device to be selected.

### 📡 HID Interface Selection
- Auto-detects available HID nodes (`hidg0`, `hidg1`, etc.) on the device
- Validates that the selected node is a real character device before execution
- Persists your preferred interface across sessions

### 🎭 30+ Built-in Demo Scripts
Browse and run a curated library of scripts from the **Demo & Pranks** tab. Categories include:

- **Harmless Pranks** — Rickroll, fake Windows update, Notepad ghost writer, and more
- **Fun Demos** — Matrix terminal rain, talking computer (voice synth), and others
- **Dev Utilities** — Quick automation helpers for common developer tasks
- **Visual FX & Speech/Audio** — Eye-catching effects and text-to-speech demos
- **Cross-platform** — Scripts targeting Windows, macOS, Linux, and Universal targets

### 📝 DuckyScript Command Support

| Command | Description |
|---------|-------------|
| `STRING <text>` | Types the given text |
| `STRINGLN <text>` | Types text followed by Enter |
| `DELAY <ms>` | Pauses execution for N milliseconds |
| `DEFAULT_DELAY / DEFAULTDELAY <ms>` | Sets a global delay between each line |
| `REPEAT <count>` | Repeats the last action N times |
| `REM / # / //` | Comments (ignored at runtime) |
| `ENTER`, `ESC`, `TAB`, `SPACE`, `BACKSPACE`, `DELETE` | Common control keys |
| `F1`–`F24` | Function keys |
| `CTRL`, `ALT`, `SHIFT`, `GUI / WIN / COMMAND` | Modifier keys |
| `CTRL-ALT DELETE`, `GUI r`, `ALT-F4` | Modifier combinations (hyphen or space syntax) |
| Arrow keys, `HOME`, `END`, `PAGEUP`, `PAGEDOWN`, `INSERT` | Navigation keys |
| `NUMPAD0`–`NUMPAD9`, `CAPSLOCK`, `NUMLOCK`, `SCROLLLOCK` | Numpad & lock keys |
| `PRINTSCREEN`, `PAUSE`, `MENU` | Special keys |

---

## Requirements

- Android device with **root access** (Magisk or equivalent)
- USB OTG + HID gadget support (kernel must expose `/dev/hidg*` interfaces)
- Android Studio (to build from source)

---

## Enabling HID Gadget on Your Device

HID Interface Runner writes to `/dev/hidg*` nodes — these must exist on your device before the app can send any keystrokes. Most stock kernels do **not** expose these by default, even on rooted phones.

### Is HID already available?

Open a root shell and check:

```bash
ls /dev/hidg*
```

If you see `/dev/hidg0`, `/dev/hidg1`, etc., you're ready. If the command returns nothing, you need to enable the HID gadget first.

### Enabling HID with android-usb-gadget

The easiest way is the **android-usb-gadget** app by [@tejado](https://github.com/tejado):

| Source | Link |
|--------|------|
| GitHub (source + releases) | [github.com/tejado/android-usb-gadget](https://github.com/tejado/android-usb-gadget) |
| F-Droid (free, no account needed) | [f-droid.org/en/packages/net.tjado.usbgadget](https://f-droid.org/en/packages/net.tjado.usbgadget) |

Steps:
1. Install android-usb-gadget on your rooted phone
2. Open it and grant root access
3. Enable the **HID** function (keyboard)
4. Apply the configuration — this creates `/dev/hidg0` (and sometimes `hidg1`)
5. Open HID Interface Runner — it will auto-detect the new node

> **Note:** Some devices require a USB disconnect/reconnect or a reboot after toggling gadget functions for the nodes to appear correctly.

### Alternative: Manual via `configfs`

If you prefer the shell or your device doesn't support android-usb-gadget, you can configure the HID gadget manually via `configfs`. Refer to the [Linux kernel USB gadget documentation](https://www.kernel.org/doc/html/latest/usb/gadget_configfs.html) for details. This is device/kernel-specific and may require a custom kernel.

---

## Getting Started

### Download (Pre-built APK)

Grab the latest APK from the [**Releases**](https://github.com/IMApurbo/HID_Runner/releases) page and sideload it onto your rooted device.

### Build from Source

1. Clone the repository:
   ```bash
   git clone https://github.com/IMApurbo/HID_Runner.git
   cd HID_Runner
   ```

2. Open the project in **Android Studio** (File → Open).

3. Allow Android Studio to sync and resolve any Gradle incompatibilities.

4. *(Optional)* If you plan to use Gemini AI features, create a `.env` file in the project root:
   ```
   GEMINI_API_KEY=your_key_here
   ```
   See `.env.example` for reference. Skip this step if you are not using AI features.

5. If needed, remove the `signingConfig` line from `app/build.gradle.kts`:
   ```kotlin
   // Remove this line if it causes a build error:
   signingConfig = signingConfigs.getByName("debugConfig")
   ```

6. Build and run on an emulator or a physical rooted device.

---

## How It Works

1. Android's USB gadget framework exposes `/dev/hidg*` character devices that speak the USB HID protocol.
2. HID Interface Runner writes standard **8-byte HID keyboard reports** directly to the selected gadget node using root shell access.
3. Scripts are parsed line-by-line into a sequence of `KeyPress` and `Delay` actions, then replayed in order over the HID interface.
4. The connected host sees a standard USB keyboard — no drivers or software needed on the host side.

---

## Project Structure

```
HID_Runner/
├── app/
│   └── src/main/java/com/example/
│       ├── MainActivity.kt          # UI (Jetpack Compose), tabs, script runner
│       ├── DuckyProtocol.kt         # Script parser, HID report builder, layout maps
│       ├── DemoScripts.kt           # 30+ built-in demo & prank scripts
│       └── UsbConnectionMonitor.kt  # USB connection detection for Auto Execute
├── .env.example                     # API key template (Gemini)
└── README.md
```

---

## Screenshots

> <img width="200" height="500" alt="WhatsApp Image 2026-09-15 at 9 01 15 PM" src="https://github.com/user-attachments/assets/24f8bb05-e5da-4e3d-b23d-88f4729f3d6c" />


---

## Disclaimer

This tool is intended for **authorized testing, accessibility research, and developer automation** on devices you own or have explicit permission to control. Misuse of HID injection tools against unauthorized systems may violate computer fraud laws in your jurisdiction. The author assumes no responsibility for misuse.

---

## Author

**IMApurbo** — [GitHub](https://github.com/IMApurbo)

---

## License

This project is licensed under the [MIT License](LICENSE).
