# Phase 2 Automation
Cucumber API smoke coverage for the Phase 2 endpoints plus Selenium can be added against the Vite UI.
Run after backend startup: `mvn -f automation/pom.xml test`. Do not claim all scenarios pass until the local environment is running and the report is reviewed.


## Phase 4 — Systech-inspired serialization automation

The automation layer now covers the simulated packaging line and serialization workflow:

- REST Assured: `/api/line`, `/api/serialization/verify`, print, commission and decommission APIs
- Selenium: React packaging-line HMI and serialization screens
- Cucumber: end-to-end business scenarios in `src/test/resources/features/serialization_line.feature`
- JDBC: validate `serialized_units`, `serialization_events`, and `vision_results`
- Negative tests: wrong DataMatrix, wrong lot/expiry, device fault, duplicate serial and aggregation mismatch

This is a portfolio/functional simulation inspired by public industry concepts; it is not a validated Systech product or a GxP production implementation.


## Test reports (Cucumber + Allure)

Start the backend first (`mvn spring-boot:run` from the project root), then from `automation/`:

```bash
mvn clean test -DbaseUrl=http://localhost:8080/api   # runs the suite, writes raw results
mvn allure:serve                                      # builds the Allure report and opens it in the browser
# or
mvn allure:report                                     # static report -> target/site/allure-maven-plugin/index.html
```

| Report | Location | What it shows |
|---|---|---|
| Cucumber HTML | `target/cucumber-reports/cucumber.html` | Single-page, feature -> scenario -> step, pass/fail per step |
| Cucumber JSON / JUnit XML | `target/cucumber-reports/cucumber.json`, `cucumber.xml` | Machine-readable, for CI (Jenkins, GitHub Actions) |
| Cucumber timeline | `target/cucumber-reports/timeline/index.html` | Execution timeline per thread |
| Allure | `target/site/allure-maven-plugin/index.html` | Dashboard, trends, severity, features/stories, every API request & response attached, environment, failure categories |

Notes
- The Allure report is generated even when tests fail: run `mvn allure:serve` after a red `mvn test`.
- Cucumber tags (`@gmp`, `@negative`, `@regression`, `@severity=critical`) appear as Allure tags/severity; filter a run with `-Dcucumber.filter.tags="@gmp and @negative"`.
- Allure failure categories (`src/test/resources/allure-categories.json`) separate product defects (5xx, broken GMP rules) from environment problems (backend down, 401).
- The plain JUnit smoke test (`SerializationApiSmokeTest`) appears in the Surefire report, not in Allure.


## Cross-platform UI tests — web, Windows, Android, iOS

The same Cucumber scenarios (`features/ui/*.feature`, tag `@ui`) run on every target. Pick one with `-Dplatform`.
A default `mvn test` runs **only the API suite**. UI runs use the `ui` profile (`-Pui`). `-Pall` runs both.

**Before a UI run:** start the backend (`mvn spring-boot:run`) and the UI (`cd frontend && npm run dev`).
Vite now listens on all interfaces (`http://<your-mac-ip>:5173`). The UI calls the API on the same host it was opened from,
so emulators, phones and other PCs work. Backend CORS accepts localhost, the 10.x, 172.x and 192.168.x ranges, and BrowserStack Local;
override with the `CORS_ORIGIN_PATTERNS` environment variable.

| Target | Command | One-time setup |
|---|---|---|
| Chrome (Mac/Win/Linux) | `mvn test -Pui -Dplatform=chrome` (add `-Dheadless=true` for CI) | none (Selenium Manager downloads the driver) |
| Firefox / Edge | `mvn test -Pui -Dplatform=firefox` / `-Dplatform=edge` | browser installed |
| Safari (Mac) | `mvn test -Pui -Dplatform=safari` | `safaridriver --enable`, Safari ▸ Develop ▸ Allow Remote Automation |
| **Windows** (on a Windows PC) | `mvn test -Pui -Dplatform=windows -DuiBaseUrl=http://<mac-ip>:5173 -DbaseUrl=http://<mac-ip>:8080/api` | Edge, JDK 21, Maven |
| **Windows** (from this Mac) | `mvn test -Pui -Dplatform=windows -Dexecution=browserstack` or `-Dexecution=grid -Dgrid.url=http://<grid>:4444` | BrowserStack or a Selenium Grid with a Windows node |
| **Android** emulator (Chrome) | `mvn test -Pui -Dplatform=android` | Android Studio + an emulator running; `npm i -g appium`; `appium driver install uiautomator2`; start `appium --allow-insecure chromedriver_autodownload` |
| **Android** real phone | `mvn test -Pui -Dplatform=android -Dudid=<adb devices id> -DuiBaseUrl=http://<mac-ip>:5173` | USB debugging on; phone and Mac on the same Wi-Fi |
| **iOS** simulator (Safari) | `mvn test -Pui -Dplatform=ios -Ddevice="iPhone 16" -Dos.version=18.0` | Xcode + simulator; `appium driver install xcuitest`; start `appium` |
| **iOS** real iPhone | `mvn test -Pui -Dplatform=ios -Dudid=<udid> -DuiBaseUrl=http://<mac-ip>:5173` | WebDriverAgent signing in Xcode (Apple developer team); Web Inspector on in Safari settings |
| Any device in the cloud | `mvn test -Pui -Dplatform=ios -Dexecution=browserstack` (also `android`, `windows`, `safari`, `chrome`) | `BROWSERSTACK_USERNAME` / `BROWSERSTACK_ACCESS_KEY` env vars + BrowserStack Local running (`BrowserStackLocal --key $BROWSERSTACK_ACCESS_KEY`) |

Other switches: `-Ddevice=...`, `-Dos.version=...`, `-Dudid=...`, `-Dappium.url=...`, `-Dui.timeout=30`.
Find the Mac's LAN IP with `ipconfig getifaddr en0`.

Screenshots of every UI scenario (marked "FAILED" on failure) are embedded in both the Cucumber HTML report and Allure.
The Allure Environment widget shows which platform the run used.

**Design:**

- `ui/DriverFactory` builds the driver from `platform` × `execution`.
- Page objects (`pages/*`) locate elements only by `data-testid`.
- Dropdowns are set through JS plus a `change` event, because native pickers on iOS and Android can't be driven with Selenium's `Select`.
- Clicks scroll the element into view first, which matters on small screens.
