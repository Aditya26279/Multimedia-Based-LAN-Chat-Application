# Multimedia-Based LAN Chat Application

A fully-featured Java-based Local Area Network (LAN) Chat Application utilizing modern **Object-Oriented Programming (OOP)**, **Sockets**, and the **Java Sound API**.

## Features

- **Multi-Client Server Architecture**: Centralized server dashboard that can handle scaling multiple concurrent clients easily without thread-crossing or bottlenecks.
- **Text Chat**: Real-time localized messaging with rich-text UI elements.
- **Multimedia - Image Sharing**: Sends high-quality images directly into the chat stream inline. 
- **Multimedia - File Transfers**: Distribute any document extensions (`.pdf`, `.docx`, `.zip`) seamlessly across other clients. Safe downloading includes prompting users before saving blind incoming network files.
- **Multimedia - Voice Messages (Push-to-Talk)**: Integrated microphone hardware locking capable of rapidly recording `8000Hz` voice packets and automatically distributing them directly to the other clients' speakers in real time.

## Project Structure

- `src/com/chat/common/` - Core serialization payloads (`Message`, `MessageType`)
- `src/com/chat/server/` - Backend socket routing (`ChatServer`, `ClientHandler`, `ServerWindow`)
- `src/com/chat/client/` - Frontend multi-threaded listeners, GUI windows, and Audio Hardware drivers (`ChatClient`, `ClientWindow`, `AudioRecorder`, `AudioPlayer`)

---

## 🚀 How to Run

### Step 1: Build the Application
This application comes with an automated Windows compilation script. 
1. Open up your file explorer or terminal and run `build.bat`. 
2. This invokes the Java compiler to compile all packages inside the `src/` folder into an `out/` build directory, and then bundles them into two executables: `ChatServer.jar` and `ChatClient.jar`.

### Step 2: Start the Server
1. Double click `run_server.bat` (or execute `ChatServer.jar` directly). 
2. This opens the **Server Dashboard**.
3. Click "Start Server". It will host on port `8080` internally. Check the output logs within the Window to ensure successful port-binding.

### Step 3: Connect Clients
1. Run `run_client.bat` or open `ChatClient.jar`. (You can run multiple instances of this file to simulate different people).
2. Enter the **Server IP**. (If on the same machine, type `localhost` or `127.0.0.1`. If over an actual LAN, open `cmd`, type `ipconfig`, check your server's IPv4 Address and type that corresponding address).
3. Connect and begin chatting or sharing media!
