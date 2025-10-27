# 🔠 Wordlist Generator (Android + CLI)

An **intelligent wordlist generator** for Android and desktop (Java CLI) that helps create custom password or keyword lists for testing, research, or ethical hacking.  
It supports advanced combination logic, year/symbol mixing, Leet transformations, and file export — all wrapped in a clean Android UI.

---

## 📱 Android App Overview

The Android app (`com.example.wordlistgen`) provides a simple UI to:
- Input custom **tokens**, **years**, and **symbols**
- Choose **max combinations** and **line limits**
- Enable **Leet (1337)** and **Reverse** options
- Generate a text file in `/Documents/WordlistGen/custom_wordlist.txt`

### 🧩 Features
- 🪄 Combine names, numbers, and symbols automatically  
- 🧠 Smart variants: Leet conversion (`a → 4`, `e → 3`, etc.)  
- 🔁 Reverse mode: adds reversed forms of tokens  
- 🗂️ Auto file export to user-accessible storage  
- 🧩 Optional suffix and separator handling  
- ⚙️ Limit generation size to prevent overflow  

---

## 💻 Project Structure

```
/Wordlist-Generator
├── src
│   └── main/java/com/vikkytech02/wordlist/
│       ├── App.java           # CLI entry point
│       ├── Generator.java     # Core wordlist generator
│       ├── MaskParser.java    # For pattern-based wordlists
│       └── MangleUtils.java   # Word mangling & transformations
│
├── src/test/java/com/vikkytech02/wordlist/
│   └── GeneratorTest.java     # Unit tests
│
├── examples/                  # Example configs & outputs
│   ├── example-config.json
│   └── small-output.txt
│
├── docs/
│   └── usage.md
│
├── scripts/
│   └── generate-large.sh      # Example shell script for batch generation
│
└── resources/dict/
    ├── common.txt
    └── names.txt
```

---

## 🧠 How It Works

| Component | Description |
|------------|--------------|
| **MainActivity.java** | Handles UI and background generation using a worker thread |
| **Generator.java** | Core logic — builds all token, year, and symbol combinations |
| **activity_main.xml** | UI layout with input fields, checkboxes, and progress display |
| **App.java (CLI)** | Command-line version for desktop use |
| **MaskParser.java / MangleUtils.java** | Extra modules for pattern-based or advanced generation |

---

## 🧩 Example Usage

### 🧱 Input
```
Tokens: Vikas, Yadav
Years: 2001, 2025
Symbols: @, _
Max Comb: 2
Max Lines: 50000
Options: ✅ Leet ✅ Reverse
```

### 📄 Output Preview
```
vikas
vikas2001
VIKAS_2025
yadav@2001
yadavvikas
yadav4
sakayv
vikas2025@
```

---

## ⚙️ Build & Run (Android)

1. Open the project in **Android Studio**
2. Connect your device or start an emulator
3. Run **MainActivity**
4. Generated wordlists are saved to:
   ```
   /Documents/WordlistGen/custom_wordlist.txt
   ```

---

## 🧰 Build & Run (CLI Version)

```bash
# Clone the repo
git clone https://github.com/vikkytech02/Wordlist-Generator.git
cd Wordlist-Generator

# If using Gradle
./gradlew build

# Or using Maven
mvn package

# Run
java -jar build/libs/wordlist-generator.jar --config examples/example-config.json
```

---

## 🧾 License
This project is licensed under the **MIT License** — feel free to use and modify it for personal or educational purposes.

---

## 👨‍💻 Author
**VikkyTech02**  
💼 Developer & Ethical Hacker Enthusiast  
📧 *Add your contact/email if you’d like*

---

> _“Strong wordlists build stronger security — generate smart, not random.”_
