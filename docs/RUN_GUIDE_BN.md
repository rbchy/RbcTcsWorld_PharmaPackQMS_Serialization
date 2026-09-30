# PharmaPack QMS — সব ডিভাইসে চালানোর Command গাইড (বাংলা)

> Mac, Windows, Linux/Unix, Android আর iOS — প্রতিটা ডিভাইসে project চালানো, test করা, report দেখা আর
> desktop app ব্যবহারের সব command, বাংলা ব্যাখ্যা সহ।

**নিয়ম:**

- প্রতিটা code block-এ **একটাই command** থাকবে। একটা একটা করে copy করে চালান।
- Mac-এর terminal (zsh)-এ command-এর শেষে `# comment` লিখবেন না, তাতে error আসে। তাই সব ব্যাখ্যা code block-এর বাইরে লেখা।
- `<...>` চিহ্নের জায়গায় নিজের তথ্য বসান। যেমন `<mac-ip>`-এর জায়গায় `192.168.1.20`।

---

## সূচিপত্র
1. [কোথায় কী চলে](#1-কোথায়-কী-চলে)
2. [macOS](#2-macos)
3. [Windows](#3-windows)
4. [Linux / Unix](#4-linux--unix)
5. [Android](#5-android)
6. [iOS (iPhone / iPad)](#6-ios-iphone--ipad)
7. [Git, GitHub আর CI](#7-git-github-আর-ci)
8. [সমস্যা হলে (Troubleshooting)](#8-সমস্যা-হলে-troubleshooting)

---

## 1. কোথায় কী চলে

| ডিভাইস | App চালানো (server) | App ব্যবহার (browser) | Test automation চালানো |
|---|---|---|---|
| **macOS** | ✅ source থেকে, অথবা Desktop app (.dmg / .zip) | ✅ | ✅ API, Chrome, Safari, আর iOS simulator |
| **Windows** | ✅ source থেকে, অথবা Desktop app (.exe / .msi) | ✅ | ✅ API, Chrome, Edge, Android emulator |
| **Linux / Unix** | ✅ source থেকে, অথবা Desktop app (.deb / .tar.gz) | ✅ | ✅ API, Chrome, Firefox, Android emulator |
| **Android** | ❌ server চলে না | ✅ Chrome দিয়ে PC-র ঠিকানা খুলে | PC থেকে Appium দিয়ে |
| **iOS** | ❌ server চলে না | ✅ Safari দিয়ে PC-র ঠিকানা খুলে | শুধু Mac থেকে Appium দিয়ে |

**দুইভাবে চালানো যায়:**

- **A. Desktop app:** কিছু install লাগে না। শুধু download করে double-click করলেই চলে। Demo, সাধারণ ব্যবহার আর অন্যকে দেখানোর জন্য এটা ভালো।
- **B. Source থেকে:** Java, Maven, Node আর MySQL লাগে। Code বদলানো, test লেখা আর চালানোর জন্য এটা ব্যবহার করুন।

**Demo login:** `admin` / `admin123`। অন্য user: `qa_user/qa123`, `supervisor/super123`, `operator/operator123`, `inspector/inspect123`।

---

## 2. macOS

### 2.1 Desktop app (সবচেয়ে সহজ)

1. GitHub → **Actions → Package desktop apps** থেকে `PharmaPackQMS-macOS` download করুন। অথবা **Releases** page থেকে নিন।
2. `PharmaPackQMS-1.0.0-macos-portable.zip`-এ double-click করুন, ভেতরে `PharmaPackQMS.app` পাবেন।
3. প্রথমবার **right-click → Open → Open** দিয়ে খুলুন। App-টা Apple-signed নয়, তাই এটা লাগে।
4. Browser নিজে থেকে `http://localhost:8080` খুলবে। বন্ধ করতে menu bar-এর **Q** icon থেকে **Quit** চাপুন।

"App is damaged" বার্তা এলে quarantine চিহ্ন সরিয়ে দিন:
```bash
xattr -cr ~/Downloads/PharmaPackQMS.app
```

Terminal থেকে খুলতে চাইলে (log দেখা যায়):
```bash
~/Downloads/PharmaPackQMS.app/Contents/MacOS/PharmaPackQMS
```

### 2.2 একবারের setup (source থেকে চালাতে)

Homebrew না থাকলে আগে সেটা install করুন (https://brew.sh)। তারপর একে একে এগুলো:
```bash
brew install openjdk@21
```
```bash
brew install maven
```
```bash
brew install node
```
```bash
brew install mysql
```
```bash
brew install git gh
```

MySQL চালু করুন। Mac চালু হলে এটা নিজে থেকেই চালু হবে:
```bash
brew services start mysql
```

সব ঠিকমতো install হয়েছে কিনা দেখুন। Java-য় 21, Node-এ 20 বা তার বেশি দেখানো উচিত:
```bash
java -version
```
```bash
mvn -v
```
```bash
node -v
```

Project আনুন (প্রথমবার):
```bash
git clone https://github.com/rbchy/RbcTcsWorld_PharmaPackQMS_Serialization.git
```
```bash
cd RbcTcsWorld_PharmaPackQMS_Serialization
```

### 2.3 Database তৈরি (একবারই)

Project folder থেকে একটা একটা করে চালান। প্রতিবার MySQL root password চাইবে:
```bash
mysql -u root -p -e "source database/schema.sql"
```
```bash
mysql -u root -p -e "source database/seed.sql"
```
```bash
mysql -u root -p -e "source database/create_app_user.sql"
```
```bash
mysql -u root -p pharmapack_qms -e "source database/phase2-migration.sql"
```
```bash
mysql -u root -p pharmapack_qms -e "source database/phase3-password-update.sql"
```
```bash
mysql -u root -p pharmapack_qms -e "source database/phase4-systech-serialization.sql"
```

`schema.sql` আবার চালালে সব data মুছে নতুন করে শুরু হয়।

### 2.4 App চালানো (৩টা Terminal tab)

**Tab 1 — Backend (API, port 8080):**
```bash
mvn spring-boot:run
```

**Tab 2 — Frontend (UI, port 5173).** প্রথমবার `npm install`, পরে শুধু `npm run dev`:
```bash
cd frontend
```
```bash
npm install
```
```bash
npm run dev
```

**Browser:** `http://localhost:5173` খুলুন।

Mac-এর IP দেখতে, যেটা phone বা অন্য PC থেকে খোলার জন্য লাগবে:
```bash
ipconfig getifaddr en0
```

### 2.5 Test চালানো (Tab 3)

```bash
cd automation
```

**শুধু API test** (backend চালু থাকতে হবে):
```bash
mvn clean test -DbaseUrl=http://localhost:8080/api
```

**UI test, Chrome** (backend আর frontend দুটোই চালু থাকতে হবে):
```bash
mvn clean test -Pui -Dplatform=chrome
```

**সব test (API + UI) একসাথে:**
```bash
mvn clean test -Pall -Dplatform=chrome
```

**Browser window না খুলে (headless):**
```bash
mvn clean test -Pall -Dplatform=chrome -Dheadless=true
```

**Safari-র জন্য একবারের setup:** নিচের command চালান। তারপর Safari → Settings → Advanced → "Show features for web developers" চালু করুন, আর Develop → **Allow Remote Automation** চাপুন।
```bash
sudo safaridriver --enable
```
**Safari-তে UI test:**
```bash
mvn clean test -Pui -Dplatform=safari
```

**Firefox বা Edge** (browser install থাকতে হবে):
```bash
mvn clean test -Pui -Dplatform=firefox
```

**শুধু GMP negative test (tag দিয়ে বেছে):**
```bash
mvn clean test "-Dcucumber.filter.tags=@gmp and @negative"
```

### 2.6 Report দেখা

**Allure report** browser-এ খুলবে। দেখা শেষ হলে `Ctrl+C` চাপুন:
```bash
mvn allure:serve
```

**Cucumber HTML report:**
```bash
open target/cucumber-reports/cucumber.html
```

### 2.7 Desktop edition নিজে build করা (MySQL ছাড়া)

Project root থেকে একে একে:
```bash
cd frontend
```
```bash
npx vite build
```
```bash
cd ..
```
```bash
mvn -DskipTests package
```
```bash
java -Dspring.profiles.active=desktop -jar target/pharmapack-qms-0.2.0-SNAPSHOT.jar
```
এতে embedded database-সহ পুরো app `http://localhost:8080`-এ চলবে।

---

## 3. Windows

সব command **PowerShell**-এ চালান। Start menu-তে "PowerShell" লিখে খুলুন।

**PowerShell-এর বিশেষ নিয়ম:**

- `-D...` দিয়ে শুরু হওয়া argument-এ `.` থাকলে সেটা **quote** করুন, যেমন `"-DbaseUrl=http://localhost:8080/api"`। না করলে PowerShell argument-টা ভেঙে ফেলে।
- Path-এ `\` ব্যবহার করুন।

### 3.1 Desktop app (সবচেয়ে সহজ)

1. `PharmaPackQMS-Windows` artifact বা Release থেকে download করুন।
2. দুইভাবে চালাতে পারেন:
   - **Portable:** zip-এ right-click → **Extract All**, তারপর `PharmaPackQMS\PharmaPackQMS.exe`-এ double-click।
   - **Installer:** `PharmaPackQMS-1.0.0.msi` চালান। Start menu আর Desktop-এ shortcut তৈরি হবে।
3. SmartScreen এলে **More info → Run anyway** চাপুন।
4. Firewall প্রশ্ন করলে **Allow** দিন। তাহলে phone থেকেও app ব্যবহার করা যাবে।
5. একটা কালো log window খুলবে। App চলার সময় এটা খোলা রাখুন, **বন্ধ করলে app-ও বন্ধ হবে**।

### 3.2 একবারের setup (source থেকে চালাতে)

```powershell
winget install EclipseAdoptium.Temurin.21.JDK
```
```powershell
winget install OpenJS.NodeJS.LTS
```
```powershell
winget install Oracle.MySQL
```
```powershell
winget install Git.Git
```
```powershell
winget install GitHub.cli
```

**Maven:** https://maven.apache.org থেকে zip download করে `C:\maven`-এ extract করুন। তারপর `C:\maven\bin` folder-টা PATH-এ যোগ করুন (Start → "Edit the system environment variables" → Environment Variables → Path)। Chocolatey থাকলে এক command-এই হয়:
```powershell
choco install maven
```

নতুন PowerShell খুলে যাচাই করুন:
```powershell
java -version
```
```powershell
mvn -v
```

Project আনুন:
```powershell
git clone https://github.com/rbchy/RbcTcsWorld_PharmaPackQMS_Serialization.git
```
```powershell
cd RbcTcsWorld_PharmaPackQMS_Serialization
```

### 3.3 Database তৈরি (একবারই)

PowerShell-এ `<` দিয়ে ফাইল পাঠানো যায় না, তাই `source` ব্যবহার করুন:
```powershell
mysql -u root -p -e "source database/schema.sql"
```
```powershell
mysql -u root -p -e "source database/seed.sql"
```
```powershell
mysql -u root -p -e "source database/create_app_user.sql"
```
```powershell
mysql -u root -p pharmapack_qms -e "source database/phase2-migration.sql"
```
```powershell
mysql -u root -p pharmapack_qms -e "source database/phase3-password-update.sql"
```
```powershell
mysql -u root -p pharmapack_qms -e "source database/phase4-systech-serialization.sql"
```

`mysql` command না পাওয়া গেলে MySQL-এর bin folder PATH-এ যোগ করুন, যেমন `C:\Program Files\MySQL\MySQL Server 8.4\bin`।

### 3.4 App চালানো (৩টা PowerShell window)

**Window 1 — Backend:**
```powershell
mvn spring-boot:run
```

**Window 2 — Frontend:**
```powershell
cd frontend
```
```powershell
npm install
```
```powershell
npm run dev
```

**Browser:** `http://localhost:5173`

PC-র IP দেখতে। "IPv4 Address" লাইনটা দেখুন:
```powershell
ipconfig
```

### 3.5 Test চালানো (Window 3)

```powershell
cd automation
```
**API test:**
```powershell
mvn clean test "-DbaseUrl=http://localhost:8080/api"
```
**UI test, Edge** (Windows-এ আগে থেকেই থাকে):
```powershell
mvn clean test -Pui "-Dplatform=edge"
```
**UI test, Chrome:**
```powershell
mvn clean test -Pui "-Dplatform=chrome"
```
**Windows platform:** Edge-এর সাথে Windows-নির্দিষ্ট setting দিয়ে চালায়:
```powershell
mvn clean test -Pui "-Dplatform=windows"
```
**সব test:**
```powershell
mvn clean test -Pall "-Dplatform=edge"
```

**অন্য computer-এ চলা app-কে test করতে** (যেমন Mac-এ app চলছে, আর Windows থেকে test হচ্ছে):
```powershell
mvn clean test -Pui "-Dplatform=windows" "-DuiBaseUrl=http://<mac-ip>:5173" "-DbaseUrl=http://<mac-ip>:8080/api"
```

### 3.6 Report দেখা

```powershell
mvn allure:serve
```
```powershell
start target\cucumber-reports\cucumber.html
```

### 3.7 Desktop edition নিজে build করা

```powershell
cd frontend
```
```powershell
npx vite build
```
```powershell
cd ..
```
```powershell
mvn -DskipTests package
```
```powershell
java "-Dspring.profiles.active=desktop" -jar target\pharmapack-qms-0.2.0-SNAPSHOT.jar
```

---

## 4. Linux / Unix

Ubuntu বা Debian-এর command দেওয়া আছে। Fedora-তে `apt` বদলে `dnf` ব্যবহার করুন।

### 4.1 Desktop app

**Portable (.tar.gz):**
```bash
tar xzf PharmaPackQMS-1.0.0-linux-portable.tar.gz
```
```bash
./PharmaPackQMS/bin/PharmaPackQMS
```

**Installer (.deb).** Install করার পর Applications menu-তে পাওয়া যাবে:
```bash
sudo apt install ./pharmapackqms_1.0.0_amd64.deb
```
```bash
/opt/pharmapackqms/bin/PharmaPackQMS
```

**Uninstall:**
```bash
sudo apt remove pharmapackqms
```

### 4.2 একবারের setup (source থেকে)

```bash
sudo apt update
```
```bash
sudo apt install -y openjdk-21-jdk maven git mysql-server curl
```

Node.js 20 install করুন। Ubuntu-র নিজের version অনেক পুরোনো, তাই NodeSource থেকে নিন:
```bash
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
```
```bash
sudo apt install -y nodejs
```

MySQL চালু করুন:
```bash
sudo systemctl enable --now mysql
```

Project আনুন:
```bash
git clone https://github.com/rbchy/RbcTcsWorld_PharmaPackQMS_Serialization.git
```
```bash
cd RbcTcsWorld_PharmaPackQMS_Serialization
```

### 4.3 Database

Ubuntu-তে MySQL root সাধারণত `sudo` দিয়ে চলে:
```bash
sudo mysql -e "source database/schema.sql"
```
```bash
sudo mysql -e "source database/seed.sql"
```
```bash
sudo mysql -e "source database/create_app_user.sql"
```
```bash
sudo mysql pharmapack_qms -e "source database/phase2-migration.sql"
```
```bash
sudo mysql pharmapack_qms -e "source database/phase3-password-update.sql"
```
```bash
sudo mysql pharmapack_qms -e "source database/phase4-systech-serialization.sql"
```

### 4.4 App, test আর report

Mac-এর মতোই।

**Backend:**
```bash
mvn spring-boot:run
```
**Frontend** (অন্য terminal-এ):
```bash
cd frontend && npm install && npm run dev
```
**IP দেখা:**
```bash
hostname -I
```
**Test:**
```bash
cd automation
```
```bash
mvn clean test -Pall -Dplatform=chrome
```
```bash
mvn clean test -Pui -Dplatform=firefox
```
Display ছাড়া server-এ চালাতে headless দিন:
```bash
mvn clean test -Pall -Dplatform=chrome -Dheadless=true
```
**Report:**
```bash
mvn allure:serve
```
```bash
xdg-open target/cucumber-reports/cucumber.html
```

---

## 5. Android

### 5.1 Phone থেকে app ব্যবহার (কোনো setup লাগে না)

1. PC বা Mac-এ app চালু করুন: Desktop app, অথবা source থেকে backend আর frontend।
2. Phone আর PC **একই Wi-Fi-তে** থাকতে হবে।
3. PC-র IP বের করুন: Mac-এ `ipconfig getifaddr en0`, Windows-এ `ipconfig`, Linux-এ `hostname -I`।
4. Phone-এর **Chrome**-এ খুলুন:
   - **Desktop app** চললে: `http://<pc-ip>:8080`
   - **Source থেকে** চললে: `http://<pc-ip>:5173`
5. `admin` / `admin123` দিয়ে login করুন।

না খুললে PC-র firewall-এ port 8080 বা 5173 **Allow** করুন।

### 5.2 Android-এ automation test (Appium)

এটা PC থেকে চালানো হয়। Mac, Windows বা Linux যেকোনোটা চলবে।

**একবারের setup:**
1. **Android Studio** install করুন। তারপর Device Manager থেকে একটা emulator তৈরি করে চালু করুন (যেমন Pixel)।
2. Appium install করুন:
```bash
npm install -g appium
```
```bash
appium driver install uiautomator2
```

**Emulator চলছে কিনা দেখুন** (তালিকায় `emulator-5554` জাতীয় কিছু থাকবে):
```bash
adb devices
```

**Appium server চালু করুন** (আলাদা terminal-এ, খোলা রাখুন):
```bash
appium --allow-insecure chromedriver_autodownload
```

**Test চালান** (`automation` folder থেকে)। Emulator নিজে থেকেই PC-কে `10.0.2.2` ঠিকানায় দেখে:
```bash
mvn clean test -Pui -Dplatform=android
```

**আসল Android phone-এ:**
1. Phone-এ **Settings → About phone → Build number**-এ ৭ বার tap করুন। এতে Developer options চালু হবে।
2. **Developer options → USB debugging** চালু করুন, তারপর USB দিয়ে PC-তে লাগান।
3. `adb devices` চালিয়ে phone-এর ID দেখুন।
4. Test চালান:
```bash
mvn clean test -Pui -Dplatform=android -Dudid=<adb-device-id> -DuiBaseUrl=http://<pc-ip>:5173
```

---

## 6. iOS (iPhone / iPad)

### 6.1 iPhone থেকে app ব্যবহার (কোনো setup লাগে না)

Android-এর মতোই। PC-তে app চালু রাখুন, একই Wi-Fi-তে থাকুন, তারপর **Safari**-তে খুলুন:
- Desktop app চললে: `http://<pc-ip>:8080`
- Source থেকে চললে: `http://<pc-ip>:5173`

Home screen-এ app-এর মতো রাখতে Safari-র **Share → Add to Home Screen** চাপুন।

### 6.2 iOS-এ automation test (শুধু Mac থেকে)

**একবারের setup:**
1. App Store থেকে **Xcode** install করে একবার খুলুন।
2. Command line tools install করুন:
```bash
xcode-select --install
```
3. Appium আর iOS driver install করুন:
```bash
npm install -g appium
```
```bash
appium driver install xcuitest
```
4. কোন কোন simulator আছে দেখুন:
```bash
xcrun simctl list devices available
```

**Appium চালু করুন** (আলাদা terminal-এ):
```bash
appium
```

**Simulator-এ test** (`automation` folder থেকে)। Device-এর নাম আর version আপনার simulator-এর তালিকা অনুযায়ী দিন:
```bash
mvn clean test -Pui -Dplatform=ios -Ddevice="iPhone 16" -Dos.version=18.0
```

**আসল iPhone-এ:**
1. Xcode-এ Apple ID দিয়ে signing setup করুন, যাতে WebDriverAgent iPhone-এ চলতে পারে।
2. iPhone-এ **Settings → Safari → Advanced → Web Inspector** চালু করুন।
3. iPhone-এর UDID দেখুন:
```bash
xcrun xctrace list devices
```
4. Test চালান:
```bash
mvn clean test -Pui -Dplatform=ios -Dudid=<iphone-udid> -DuiBaseUrl=http://<mac-ip>:5173
```

### 6.3 Cloud-এর আসল device (BrowserStack)

নিজের কোনো phone বা simulator লাগে না। BrowserStack account থাকলে এভাবে চালান।

Mac বা Linux-এ:
```bash
export BROWSERSTACK_USERNAME=<username>
```
```bash
export BROWSERSTACK_ACCESS_KEY=<access-key>
```
BrowserStack Local tunnel চালু রাখুন (আলাদা terminal-এ):
```bash
BrowserStackLocal --key $BROWSERSTACK_ACCESS_KEY
```
তারপর test চালান। `ios`-এর জায়গায় `android`, `windows`, `safari` বা `chrome` দেওয়া যায়:
```bash
mvn clean test -Pui -Dplatform=ios -Dexecution=browserstack
```

Windows PowerShell-এ variable এভাবে set করুন:
```powershell
$env:BROWSERSTACK_USERNAME="<username>"
```
```powershell
$env:BROWSERSTACK_ACCESS_KEY="<access-key>"
```

---

## 7. Git, GitHub আর CI

সব OS-এ একই command।

| কাজ | Command |
|---|---|
| কী বদলেছে দেখা | `git status` |
| কোন লাইন বদলেছে | `git diff` |
| পরিবর্তন stage করা | `git add -A` |
| Commit | `git commit -m "বার্তা"` |
| GitHub-এ পাঠানো | `git push` |
| GitHub থেকে আনা | `git pull` |
| ইতিহাস | `git log --oneline` |
| নতুন branch | `git checkout -b fix/<নাম>` |
| main-এ ফেরা | `git checkout main` |

**GitHub login** (একবার)। `workflow` permission সহ:
```bash
gh auth login
```
```bash
gh auth refresh -h github.com -s workflow
```

**CI হাতে চালানো:** GitHub → **Actions → CI → Run workflow**।

**Desktop app build:** GitHub → **Actions → Package desktop apps → Run workflow**।

**নতুন version release করা** (Windows, Mac আর Linux-এর ফাইল Releases-এ চলে যাবে):
```bash
git tag v1.0.1
```
```bash
git push origin v1.0.1
```

**Live Allure report:** https://rbchy.github.io/RbcTcsWorld_PharmaPackQMS_Serialization/

---

## 8. সমস্যা হলে (Troubleshooting)

| সমস্যা | কারণ | সমাধান |
|---|---|---|
| `Port 8080 already in use` | আগের app এখনো চলছে | নিচের port command দেখুন |
| `vite: command not found` | frontend package install হয়নি | `cd frontend`, তারপর `npm install` |
| UI test-এ `ERR_CONNECTION_REFUSED` | frontend চালু নেই | `npm run dev` চালু করুন |
| API test-এ `401` বা login fail | backend বন্ধ, বা database seed হয়নি | backend চালু করুন, database script আবার চালান |
| Phone থেকে খোলে না | ভিন্ন Wi-Fi, বা firewall বাধা দিচ্ছে | একই Wi-Fi ব্যবহার করুন, port 8080 বা 5173 Allow করুন |
| Mac: "App is damaged" | unsigned app | `xattr -cr PharmaPackQMS.app` |
| Windows: SmartScreen | unsigned app | More info → Run anyway |
| Desktop app-এর data reset করতে চান | — | app বন্ধ করে `~/.pharmapack-qms` folder মুছে দিন |

**Port 8080 কে ব্যবহার করছে, বের করে বন্ধ করা:**

Mac বা Linux:
```bash
lsof -i :8080
```
```bash
kill -9 <PID>
```

Windows:
```powershell
netstat -ano | findstr :8080
```
```powershell
taskkill /PID <PID> /F
```

**অন্য port-এ চালানো** (যেমন 9090)।

Mac বা Linux:
```bash
SERVER_PORT=9090 mvn spring-boot:run
```
Windows:
```powershell
$env:SERVER_PORT="9090"; mvn spring-boot:run
```

**Desktop database reset:**

Mac বা Linux:
```bash
rm -rf ~/.pharmapack-qms
```
Windows:
```powershell
Remove-Item -Recurse -Force "$env:USERPROFILE\.pharmapack-qms"
```

---

*লেখক: RB Chowdhury — QA Automation Engineer · Pharmaceutical Packaging (GMP)*
