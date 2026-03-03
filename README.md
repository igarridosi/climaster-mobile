# CliMaster 🌦️ | AI-Powered Weather Ecosystem

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.20-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=android)](https://developer.android.com/jetpack/compose)
[![Clean Architecture](https://img.shields.io/badge/Architecture-Clean%20%2B%20MVVM-success)](#)
[![Hilt](https://img.shields.io/badge/DI-Dagger%20Hilt-orange)](#)

**CliMaster** ez da eguraldi aplikazio arrunt bat. Datu meteorologikoak, Adimen Artifizial sortzailea (Generative AI) eta plataforma anitzeko sinkronizazioa (Desktop-to-Mobile) uztartzen dituen **ekosistema adimendun eta proaktibo bat da**. 

Aplikazio honek ez dizu soilik tenperatura esaten; zure profil termikoa ikasten du, datuak testuinguruan kokatzen ditu eta Euskarazko gomendio pertsonalizatuak ematen dizkizu **Groq API**-aren tximista-abiadurari esker.

---

## 🏗️ Arkitektura Teknikoa

Proiektua **Clean Architecture** printzipio zorrotzetan oinarrituta dago, modulu anitzeko (Multi-module) egitura erabiliz eskalagarritasuna eta dependentzien isolamendua bermatzeko.

*   📦 `:app` (Presentation & DI): Jetpack Compose bidezko UI deklaratiboa, MVVM patroia `StateFlow` erabiliz, eta Dagger Hilt injekzio-zuhaitza.
*   📦 `:domain` (Business Logic): Android framework-etik erabat isolatuta. Hemen daude Negozio-Ereduak (`Weather`, `AiInsight`), Repositorio-Interfazeak eta Erabilera-Kasuak (`GenerateInsightUseCase`, `AskAgentUseCase`).
*   📦 `:data` (Infrastructure): Datu-iturrien inplementazioa. Retrofit (Sarea), Room (Local Cache SQLite) eta Jetpack DataStore (Preferences). Ereduen mapeoa (`DTO -> Domain`) hemen kudeatzen da.
*   📦 `:core` (Utilities): Aplikazio osorako utilitateak, hala nola sareko erantzunak biltzen dituen `Resource<T>` Wrapper-a.

---

## ✨ Funtzionalitate Berritzaileak

### 🧠 1. AI Motorra (Hyper-Personalization)
**Groq (llama-3.3-70b-versatile)** modeloa integratu da. Ereduak "Prompt Engineering" aurreratua erabiltzen du: Ingeleseko arrazoibide logikoa (zehaztasun handiagoa lortzeko) eta JSON formatuko irteera estriktoa Euskaraz.
*   **Briefing Dinamikoa:** Orduaren, eguraldiaren eta erabiltzailearen historia termikoaren araberako testuinguruko aholkuak.
*   **Hizkuntza Naturaleko Txata:** Botoi flotatzaile (FAB) bidezko txat-interfazea, agenteari etorkizuneko iragarpenen inguruko galderak egiteko.

### 🎨 2. UI/UX Murgiltzailea eta Dinamikoa
UI-a "Pixel-Perfect" diseinuan pentsatuta dago.
*   **Dynamic Backgrounds:** Atzeko planoko gradienteak eta koloreak dinamikoki aldatzen dira bertako orduaren (Timezone erreala) eta eguraldi-baldintzen arabera.
*   **Canvas Animazioak:** Elurra, euria, tximistak eta behe-lainoa Jetpack Compose Canvas bidez renderizatzen dira 60fps-ra, GPU azelerazioarekin.
*   **Glassmorphism:** Txartel erdi-gardenak, atzealdeko efektu lausotuarekin (`Modifier.blur`), iOS/Windows 11 estiloko estetika lortuz.

### 🌉 3. Mahaigaineko Sinkronizazioa (The Bridge)
Mahaigaineko C# WPF aplikazio osagarriarekin batera lan egiteko diseinatua.
*   Erabiltzaileak PCan diseinatzen du bere Widget pertsonalizatua (Drag & Drop).
*   PC-ak JSON formatuko egitura bat kapsulatzen du **QR Kode** batean.
*   Android aplikazioak **Google Play Services Barcode Scanner** erabiltzen du QR hori irakurri, JSON-a deskodetu eta **Jetpack DataStore**-n gordetzeko.
*   **Jetpack Glance** erabiliz, Android-eko Hasierako Pantailako (Home Screen) Widget-ak JSON hori irakurtzen du eta UI-a dinamikoki eraikitzen du.

### 📍 4. Kokapen Adimenduna
*   **FusedLocationProviderClient:** Bateria kontsumo optimizatua duen GPS jarraipena.
*   **Reverse Geocoding:** Koordenatuak modu adimendunean itzultzen dira Euskarazko hiri-izenetara.

---

## 🛠️ Teknologia Stack-a

| Kategoria | Teknologia / Liburutegia |
| :--- | :--- |
| **Hizkuntza** | Kotlin 2.0.20 |
| **UI Framework** | Jetpack Compose (Material Design 3) |
| **Arkitektura** | MVVM, MVI (StateFlow), Clean Architecture |
| **Injekzioa (DI)** | Dagger Hilt & KSP |
| **Sarea (Network)** | Retrofit2, OkHttp3, Gson |
| **Datu-basea (Local)** | Room Database (Offline-first approach) |
| **Datu-Gordailua** | Jetpack DataStore Preferences |
| **Kamera / QR** | Google Play Services Code Scanner |
| **Widget-ak** | Jetpack Glance |
| **APIak** | Pirate Weather (Eguraldia), Groq (LLM AI) |

---

## 🚀 Instalazioa eta Konfigurazioa

Proiektu hau zure makinan konpilatzeko, API gako propioak beharko dituzu.

1.  Klonatu biltegia:
    ```bash
    git clone https://github.com/ZureErabiltzailea/CliMaster.git
    ```
2.  Ireki proiektua **Android Studio**-n (Ladybug edo berriagoa gomendatua).
3.  Konfiguratu API Gakoak proiektuaren erroan `local.properties` fitxategia sortuz (edo existitzen denean editatuz):
    ```properties
    # local.properties
    GROQ_API_KEY=zure_groq_api_gakoa_hemen
    PIRATE_WEATHER_API_KEY=zure_pirate_weather_api_gakoa_hemen
    ```
    *(Oharra: Gako hauek `BuildConfig` bidez edo `AppModule.kt`-n injektatzen dira zuzenean).*
4.  Egin proiektuaren sinkronizazioa (Gradle Sync) eta sakatu **Run**.

---

## 📜 Lizentzia
Proiektu hau [MIT Lizentzia](LICENSE)-ren pean dago. Egin fork, moldatu eta hobetu askatasunez!

---
*Garatzailea: Ibai Garrido & Ivan Hernandez - Software Arkitektura eta UX/UI Diseinua ardatz hartuta.*