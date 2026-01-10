# Requiem Project 📱

**Real-time on-screen text translation for Android**

Requiem is an innovative Android application that translates any text visible on your device screen in real-time. By combining **OCR (Optical Character Recognition)** with modern **AI-driven translation**, Requiem allows users to understand content in any app without switching screens.

This project was developed as a technical research project to master Android architecture, background services, and AI API integration.

---

## 📸 Screenshots

<p align="center">
  <img src="images/screenshot_main.png" width="280" alt="Main Dashboard">
  <img src="images/screenshot_translation.png" width="280" alt="Translation in Action">
</p>

> *Note: The UI is currently in active development and may change.*
> 
> *Note: screenshots may not be displayed right now*

---

## ✨ Features

* **Real-Time OCR:** High-speed text recognition directly from the screen buffer.
* **Dual AI Engines:** Toggle between **Google Translate** and **Gemini AI** for high-context translations.
* **Overlay UI:** Non-intrusive translation results displayed over other applications.
* **Local History:** Save and manage previous translations using **Room Database**.
* **Clean Architecture:** Separated into Data, Domain, and UI layers for maximum maintainability.
* **Performance First:** Optimized with Kotlin Coroutines for smooth background processing.

---

## 🛠 Tech Stack

* **Language:** Kotlin & Java
* **UI:** XML (View System)
* **Database:** Room Persistence Library
* **Networking:** Retrofit 2 & OkHttp
* **OCR Engine:** Optical Character Recognition (Google ML Kit)
* **AI Integration:** * Google Gemini AI API
    * Google Translate API
* **Concurrency:** Kotlin Coroutines

---

## 🚀 Setup & Installation

1.  **Clone the repo:**
    ```bash
    git clone [https://github.com/ddeeaaddllyy/requiem-project.git](https://github.com/ddeeaaddllyy/requiem-project.git)
    ```
2.  **Open in Android Studio:** Open the project folder and wait for Gradle sync.
3.  **API Keys:**
    * Obtain your API keys from the [Google AI Studio](https://aistudio.google.com/) and [Google Cloud Console](https://console.cloud.google.com/).
    * Add your keys to your `local.properties` file:
        ```properties
        GEMINI_API_KEY=your_key_here
        TRANSLATE_API_KEY=your_key_here
        ```
4.  **Run:** Build the project and deploy it to a physical Android device.

> [!WARNING]  
> This app requires **Screen Recording** and **Overlay** permissions to function correctly.

---

## 🛠 Usage

1.  Launch **Requiem**.
2.  Grant the necessary permissions for screen capture and system overlay.
3.  Select your target language.
4.  Navigate to any app (Game, Browser, Social Media) and trigger the translation to see the result instantly.

---

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!
* Follow the **Clean Architecture** patterns already established.
* Ensure all code is documented.
* Open a Pull Request (PR) for any major changes.

---

## 📜 License

This project is licensed under the **Apache License 2.0**. See the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Author

**ddeeaaddllyy** *"It was then I started getting lost in books"* - **GitHub:** [@ddeeaaddllyy](https://github.com/ddeeaaddllyy)  
- **Project Goal:** Personal growth and exploring the intersection of Android and AI.
