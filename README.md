# NutriPath — Smart Nutrition Awareness & Routine Planner

> **"The user plans once. NutriPath reminds, records, calculates, and explains."**

NutriPath is a modern, offline-first Android application designed to shift the burden of nutrition tracking from the user to the smartphone. Built with **Kotlin** and **Jetpack Compose**, NutriPath replaces tedious manual food logging, complex nutritional databases, and paywalled reminders with a visual **Personal Food Pool**, **Two-Stage proactive notification reminders**, **one-tap logging**, and **flexible meal substitutions**.

---

## 📖 The Origin Story & Problem Statement

NutriPath originated from a field survey of **75 real-world individuals** alongside hands-on observation of existing fitness and diet applications.

### Key Observations & Insights:
1. **The Nutrition Literacy Gap**: Many individuals do not eat poorly out of neglect; they simply have never been provided with accessible, practical information about what their bodies require. Daily meals frequently consist of simple, low-variety breakfasts, rice-and-curry staples, and fried foods with limited dietary diversity.
2. **Accessibility & Friction Barriers**: Most existing nutrition applications assume users:
   - Have high reading comfort and understand complex nutritional terminology (e.g., macronutrient percentages, micronutrient thresholds).
   - Are willing and able to search extensive 100,000-item databases every time they eat.
   - Have the time and tech familiarity to manually weigh and log every meal component.
   - Can afford premium subscription fees just to unlock basic reminder notifications.
3. **Daily-Wage & Busy Workers**: Working professionals, daily-wage laborers, and active workers need immediate, practical cues rather than homework. When an app demands 15 minutes of data entry a day, retention drops to zero within a week.

NutriPath addresses these core challenges by reducing daily screen time to seconds while maintaining scientific rigor in its nutritional foundation.

---

## ⚡ Existing Nutrition Apps vs. NutriPath

| Traditional Nutrition Apps | NutriPath's Approach |
| :--- | :--- |
| **Search Fatigue**: Hundreds of confusing database entries for a single food item. | **Personal Food Pool**: Visual selection from familiar, curated staples with portion sizes. |
| **Manual Logging Friction**: Multi-step search, gram measurement, and entry for each meal. | **One-Tap Action Buttons**: Direct notification actions (`"Ate This"`) or instant substitutions. |
| **Passive & Paywalled Reminders**: Generic reminders locked behind subscriptions. | **Two-Stage System Notifications**: Stage 1 pre-meal preparation heads-up + Stage 2 direct consumption confirmation. |
| **Rigid Meal Plans**: Breaks down whenever a planned food item is unavailable. | **Seamless Meal Substitution**: Swap any item with a matching food in 1 tap, with automatic macro recalculation. |
| **Complex Nutritional Jargon**: Overwhelming graphs, obscure vitamins, and confusing ratios. | **Visual Goal Trackers & Mifflin-St Jeor Engine**: Clear progress bars for Calories, Protein, Carbs, Fats, and Water. |
| **Cloud/Data Harvesting Required**: Mandatory account creation and internet dependency. | **100% Offline-First (Room DB)**: Zero subscriptions, no accounts required, instant privacy. |

---

## ✨ Core Features

### 1. Evidence-Based Biometrics & Caloric Engine
- **Mifflin-St Jeor BMR & TDEE Calculation**: Scientifically computes Basal Metabolic Rate and Total Daily Energy Expenditure factoring in biological sex, age, height, weight, activity multiplier, and user goal (fat loss, maintenance, lean bulk).
- **Automated Target Distribution**: Sets daily macro targets for Calories (kcal), Protein (g), Carbohydrates (g), Fats (g), Dietary Fiber (g), and Hydration (ml).
- **Body Classification**: Instant BMI calculation with healthy range indicators and customized recommendations.

### 2. Multi-Stage Notification & Reminder Engine
- **Stage 1 (Pre-Meal Heads-Up)**: Proactive reminder with configurable advance notice (15–30 min ahead) showing what food is planned so users can prepare.
- **Stage 2 (Meal Time & Consumption Confirmation)**: Direct alert with interactive action buttons:
  - **"Ate This"**: Immediately marks the meal as consumed and logs calories/macros without opening the app.
  - **"Substitute"**: Launches the substitution screen for quick swaps when plans change.
- **Hydration Reminders**: Periodic interval notifications with quick-add action buttons (`+250 ml`).
- **Notification Testing Simulator**: Built-in developer and user testing drawer allowing instant simulation of single or all-day notification triggers.

### 3. Visual Meal Routine & Personal Food Pool
- **4 Routine Slots**: Structured schedules for Breakfast, Lunch, Snacks, and Dinner.
- **Curated Food Pool**: Add, edit, and organize preferred food items with portion reference sizes.
- **Live Macro Balance Indicator**: Real-time visual feedback showing whether planned routine meals meet or exceed target calories and macronutrients.

### 4. Interactive Daily Log & Flexible Substitutions
- **Intuitive Day-by-Day Navigation**: Move forward or backward across dates to review past adherence or preview upcoming routines.
- **Intelligent Auto-Reset**: Automatically defaults back to the current day when switching from the Home dashboard to Daily Logs.
- **Meal Item Substitution**: Replace any scheduled food item on the fly with a single tap, or restore it back to the original routine.
- **Consumption Checkmarks**: Mark items as eaten individually with instant visual feedback and macro updates.

### 5. Hydration Tracking & Streak System
- **Visual Fluid Tracker**: Clean water level animation with quick-increment buttons (+200ml, +250ml, +500ml).
- **Habit Streak Engine**: Tracks consecutive logging days to build long-term nutritional discipline.
- **Daily Adherence Score**: Visual breakdown of nutrient target compliance.

### 6. Accessibility & Regional Preferences
- **Time Formatting**: Seamless toggle between 12-hour (AM/PM) and 24-hour clock formats across the entire application.
- **Unit System**: Switch between Metric (kg, cm, ml) and Imperial (lbs, ft/in, oz) systems.
- **Multilingual Support**: Built-in dynamic localization for English and Telugu (తెలుగు).
- **Accessible Design**: Material 3 standards, minimum 48dp touch targets, semantic `testTag` annotations, and tap-outside automatic keyboard dismissal.

---

## 🏗️ Architecture & Tech Stack

NutriPath is built strictly adhering to modern Android development standards:

- **Language**: Kotlin 2.0+
- **UI Toolkit**: Jetpack Compose with Material Design 3 (M3)
- **Architecture**: Model-View-ViewModel (MVVM) with Unidirectional Data Flow (UDF)
- **Local Persistence**: Android Room Database with SQLite, Kotlin Coroutines, and Flow
- **Background Scheduling**: Android `AlarmManager` for exact alarm dispatching (`SCHEDULE_EXACT_ALARM`)
- **System Integration**: `NotificationManager` with specialized channels, and `BroadcastReceiver` for boot rescheduling (`RECEIVE_BOOT_COMPLETED`)
- **Target SDK**: Android 14+ (SDK 36) | Min SDK: 24 (Android 7.0)

---

## 📁 Project Directory Structure

```
NutriPath/
├── app/
│   ├── build.gradle.kts                 # App dependencies & build setup
│   ├── proguard-rules.pro               # ProGuard rules
│   └── src/main/
│       ├── AndroidManifest.xml          # Permissions, receivers, application tag
│       ├── java/com/example/
│       │   ├── MainActivity.kt          # Single-activity container, edge-to-edge
│       │   ├── NutriPathApplication.kt   # Notification channels, Room database init
│       │   ├── backend/
│       │   │   ├── data/
│       │   │   │   ├── DatasetFoods.kt  # Pre-seeded nutritional database
│       │   │   │   ├── local/AppDatabase.kt
│       │   │   │   │   ├── dao/         # DAOs (Food, MealLog, Target, User, etc.)
│       │   │   │   │   └── entity/      # Room Entities
│       │   │   │   └── repository/NutriPathRepository.kt
│       │   │   ├── logic/calculator/   # Mifflin-St Jeor engine
│       │   │   ├── receiver/           # MealAlarmReceiver, MealActionReceiver, BootReceiver
│       │   │   └── service/            # NotificationHelper, ReminderScheduler
│       │   └── frontend/
│       │       ├── MainViewModel.kt    # Unified UI state & async business operations
│       │       ├── components/         # Reusable M3 UI components & cards
│       │       ├── navigation/NutriPathNav.kt # Bottom navigation & screen routing
│       │       ├── screens/            # Home, Routine, Log, Nutrition, Settings
│       │       ├── theme/              # Color schemes, typography, shapes
│       │       └── util/               # Language, time format, keyboard dismiss helpers
│       └── res/                        # Vector drawables, mipmaps, strings, colors
├── gradle/
│   └── libs.versions.toml               # Dependency version catalog
├── build.gradle.kts                     # Root project build script
├── settings.gradle.kts                  # Root project settings
├── metadata.json                        # Platform metadata
└── README.md                            # Project documentation
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17 or JDK 21
- Android SDK 36 (Android 14+)

### Build & Run
1. Clone the repository:
   ```bash
   git clone https://github.com/<your-username>/NutriPath.git
   cd NutriPath
   ```
2. Open the project in Android Studio.
3. Allow Gradle to sync dependencies.
4. Run on a physical Android device or emulator (API 26+):
   ```bash
   gradle assembleDebug
   ```
   The compiled APK will be located at `app/build/outputs/apk/debug/app-debug.apk`.

---

## 🔒 Permissions & Security

NutriPath operates strictly offline on the user's local device without sending personal health data to third-party tracking servers:
- `android.permission.POST_NOTIFICATIONS`: Delivers proactive meal prep and consumption reminders (Android 13+).
- `android.permission.SCHEDULE_EXACT_ALARM`: Guarantees precise meal alarm delivery.
- `android.permission.RECEIVE_BOOT_COMPLETED`: Reschedules scheduled reminders after device restart.
- `android.permission.VIBRATE`: Provides gentle haptic alerts for meal reminders.

---

## 📄 License
This project is open-source under the Apache License 2.0. Feel free to fork, customize, and contribute!
