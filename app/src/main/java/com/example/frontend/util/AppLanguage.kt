package com.example.frontend.util

import androidx.compose.runtime.compositionLocalOf

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    TELUGU("te", "Telugu", "తెలుగు");

    companion object {
        fun fromCode(code: String): AppLanguage =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
    }
}

val LocalAppLanguage = compositionLocalOf { AppLanguage.ENGLISH }

object NutriStrings {

    fun s(lang: AppLanguage, en: String, te: String): String =
        if (lang == AppLanguage.TELUGU) te else en

    // Navigation Items
    fun navHome(lang: AppLanguage): String = s(lang, "Home", "హోమ్")
    fun navLog(lang: AppLanguage): String = s(lang, "Daily Log", "డైలీ లాగ్")
    fun navInsight(lang: AppLanguage): String = s(lang, "Insights", "విశ్లేషణ")
    fun navProfile(lang: AppLanguage): String = s(lang, "Profile", "ప్రొఫైల్")
    fun navSettings(lang: AppLanguage): String = s(lang, "Settings", "సెట్టింగులు")

    // General Actions & Controls
    fun save(lang: AppLanguage): String = s(lang, "Save", "సేవ్ చేయండి")
    fun cancel(lang: AppLanguage): String = s(lang, "Cancel", "రద్దు చేయండి")
    fun edit(lang: AppLanguage): String = s(lang, "Edit", "సవరించండి")
    fun delete(lang: AppLanguage): String = s(lang, "Delete", "తొలగించండి")
    fun confirm(lang: AppLanguage): String = s(lang, "Confirm", "నిర్ధారించండి")
    fun done(lang: AppLanguage): String = s(lang, "Done", "పూర్తయింది")
    fun close(lang: AppLanguage): String = s(lang, "Close", "మూసివేయండి")
    fun reset(lang: AppLanguage): String = s(lang, "Reset", "రీసెట్")
    fun search(lang: AppLanguage): String = s(lang, "Search", "వెతకండి")
    fun clear(lang: AppLanguage): String = s(lang, "Clear", "క్లియర్ చేయండి")
    fun back(lang: AppLanguage): String = s(lang, "Back", "వెనుకకు")
    fun continueBtn(lang: AppLanguage): String = s(lang, "Continue", "కొనసాగించు")
    fun startNutriPath(lang: AppLanguage): String = s(lang, "Start NutriPath", "నూట్రిపాత్ ప్రారంభించండి")
    fun exitWarning(lang: AppLanguage): String = s(lang, "Press back again to exit", "నిష్క్రమించడానికి మళ్ళీ బ్యాక్ నొక్కండి")
    fun viewDetails(lang: AppLanguage): String = s(lang, "View Details", "వివరాలు చూడండి")
    fun apply(lang: AppLanguage): String = s(lang, "Apply", "వర్తింపజేయండి")
    fun add(lang: AppLanguage): String = s(lang, "Add", "చేర్చండి")
    fun remove(lang: AppLanguage): String = s(lang, "Remove", "తొలగించండి")

    // Nutrients & Macros
    fun calories(lang: AppLanguage): String = s(lang, "Calories", "క్యాలరీలు")
    fun protein(lang: AppLanguage): String = s(lang, "Protein", "ప్రోటీన్")
    fun carbs(lang: AppLanguage): String = s(lang, "Carbs", "కార్బ్స్")
    fun fats(lang: AppLanguage): String = s(lang, "Fats", "కొవ్వులు")
    fun fiber(lang: AppLanguage): String = s(lang, "Fiber", "ఫైబర్")
    fun water(lang: AppLanguage): String = s(lang, "Water", "నీరు")

    // Home Screen
    fun calorieEnergy(lang: AppLanguage): String = s(lang, "CALORIE ENERGY", "క్యాలరీ శక్తి")
    fun remaining(lang: AppLanguage): String = s(lang, "remaining", "మిగిలినవి")
    fun targetAchieved(lang: AppLanguage): String = s(lang, "Calorie target achieved!", "క్యాలరీల లక్ష్యం పూర్తయింది!")
    fun todaysMeals(lang: AppLanguage): String = s(lang, "Today's Meals", "నేటి భోజనాలు")
    fun waterIntake(lang: AppLanguage): String = s(lang, "Water Intake", "నీటి వినియోగం")
    fun quickAddWater(lang: AppLanguage): String = s(lang, "+250 ml Water", "+250 ml నీరు")
    fun markConsumed(lang: AppLanguage): String = s(lang, "Mark Eaten", "తిన్నాను")
    fun consumedTag(lang: AppLanguage): String = s(lang, "Eaten", "తిన్నవి")
    fun plannedTag(lang: AppLanguage): String = s(lang, "Planned", "ప్రణాళిక")
    fun substitute(lang: AppLanguage): String = s(lang, "Substitute", "మార్చండి")
    fun streakDays(days: Int, lang: AppLanguage): String = s(lang, "$days Day Streak", "$days రోజుల స్ట్రీక్")
    fun dailyWaterGoal(lang: AppLanguage): String = s(lang, "Daily Goal", "రోజువారీ లక్ష్యం")
    fun ofTarget(curr: Int, total: Int, lang: AppLanguage): String = s(lang, "$curr / $total ml", "$curr / $total ml")
    fun quickLogTitle(lang: AppLanguage): String = s(lang, "Quick Actions", "త్వరిత ఎంపికలు")
    fun simulateMealReminder(lang: AppLanguage): String = s(lang, "Simulate Meal Alert", "భోజన అలర్ట్ పరీక్ష")
    fun simulateWaterReminder(lang: AppLanguage): String = s(lang, "Simulate Water Alert", "నీటి అలర్ట్ పరీక్ష")

    // Daily Log Screen
    fun dailyLogTitle(lang: AppLanguage): String = s(lang, "Daily Meal Log", "రోజువారీ భోజన లాగ్")
    fun dailyLogSubtitle(lang: AppLanguage): String = s(lang, "Track, substitute, and monitor your nutrition", "మీ ఆహారం, మార్పులు మరియు పోషకాలను గమనించండి")
    fun today(lang: AppLanguage): String = s(lang, "Today", "ఈరోజు")
    fun yesterday(lang: AppLanguage): String = s(lang, "Yesterday", "నిన్న")
    fun tomorrow(lang: AppLanguage): String = s(lang, "Tomorrow", "రేపు")
    fun markWillEat(lang: AppLanguage): String = s(lang, "Will Eat", "తింటాను")
    fun addExtraFood(lang: AppLanguage): String = s(lang, "+ Add Food", "+ ఆహారం చేర్చండి")
    fun noMealsPlanned(lang: AppLanguage): String = s(lang, "No foods logged for this meal yet", "ఈ పూటకు ఇంకా ఆహారాలు నమోదు చేయలేదు")
    fun totalDailyCalories(lang: AppLanguage): String = s(lang, "Total Daily Calories", "మొత్తం రోజువారీ క్యాలరీలు")
    fun dailyNutritionSummary(lang: AppLanguage): String = s(lang, "Daily Nutrition Summary", "రోజువారీ పోషకాల సారాంశం")
    fun deleteLogConfirm(lang: AppLanguage): String = s(lang, "Delete this food entry?", "ఈ ఆహార ఎంట్రీని తొలగించాలా?")

    // Insights Screen
    fun insightsTitle(lang: AppLanguage): String = s(lang, "Nutrition Insights", "పోషకాహార విశ్లేషణ")
    fun insightsSubtitle(lang: AppLanguage): String = s(lang, "Macro distribution and weekly progress", "పోషకాల సమతుల్యత మరియు పురోగతి")
    fun weeklyAdherence(lang: AppLanguage): String = s(lang, "Weekly Routine Adherence", "వారపు భోజన క్రమశిక్షణ")
    fun macroDistribution(lang: AppLanguage): String = s(lang, "Macronutrient Distribution", "పోషకాల విభజన (మ్యాక్రోస్)")
    fun hydrationStatus(lang: AppLanguage): String = s(lang, "Hydration Progress", "నీటి వినియోగం పురోగతి")
    fun healthyTipsTitle(lang: AppLanguage): String = s(lang, "Nutrition Tips & Guidance", "పోషకాహార చిట్కాలు & సలహాలు")
    fun calorieBurnSummary(lang: AppLanguage): String = s(lang, "Calorie & Energy Balance", "క్యాలరీలు & శక్తి సమతుల్యత")
    fun consistencyScore(lang: AppLanguage): String = s(lang, "Consistency Score", "క్రమశిక్షణ స్కోర్")

    // Profile Screen
    fun profileTitle(lang: AppLanguage): String = s(lang, "Profile & Targets", "ప్రొఫైల్ & లక్ష్యాలు")
    fun profileSubtitle(lang: AppLanguage): String = s(lang, "Manage nutrition goals, food pool, and routine", "పోషకాహార లక్ష్యాలు మరియు దినచర్యను నిర్వహించండి")
    fun personalStats(lang: AppLanguage): String = s(lang, "Personal Stats", "వ్యక్తిగత వివరాలు")
    fun editProfile(lang: AppLanguage): String = s(lang, "Edit Profile", "ప్రొఫైల్ సవరించండి")
    fun foodPoolManagement(lang: AppLanguage): String = s(lang, "Food Pool Management", "ఆహారాల జాబితా (ఫుడ్ పూల్)")
    fun foodPoolSubtitle(lang: AppLanguage): String = s(lang, "Customize foods you regularly consume", "మీరు క్రమం తప్పకుండా తినే ఆహారాలను ఎంచుకోండి")
    fun mealTimingsSchedule(lang: AppLanguage): String = s(lang, "Meal Timings & Schedule", "భోజన సమయాలు & షెడ్యూల్")
    fun mealTimingsSubtitle(lang: AppLanguage): String = s(lang, "Set meal times for weekdays & weekends", "వారపు రోజులు & వారాంతపు భోజన సమయాలు")
    fun mealReminders(lang: AppLanguage): String = s(lang, "Meal Reminders", "భోజన రిమైండర్లు")
    fun mealRemindersSub(lang: AppLanguage): String = s(lang, "Configure advance meal notifications", "భోజనానికి ముందు నోటిఫికేషన్లను అమర్చండి")
    fun waterSettings(lang: AppLanguage): String = s(lang, "Water Tracker Settings", "నీటి ట్రాకర్ సెట్టింగులు")
    fun waterSettingsSub(lang: AppLanguage): String = s(lang, "Target volume & hydration reminders", "రోజువారీ నీటి లక్ష్యం & రిమైండర్లు")
    fun exerciseMovement(lang: AppLanguage): String = s(lang, "Daily Movement & Exercise", "రోజువారీ నడక & వ్యాయామం")
    fun exerciseMovementSub(lang: AppLanguage): String = s(lang, "Track activities and calorie burn", "శారీరక శ్రమ మరియు క్యాలరీ ఖర్చును నమోదు చేయండి")
    fun appSettings(lang: AppLanguage): String = s(lang, "App Settings", "యాప్ సెట్టింగులు")
    fun appSettingsSub(lang: AppLanguage): String = s(lang, "Language, units, and notifications", "భాష, కొలత ప్రమాణాలు మరియు నోటిఫికేషన్లు")
    fun logout(lang: AppLanguage): String = s(lang, "Log Out", "లాగౌట్")

    // Settings Screen
    fun settingsTitle(lang: AppLanguage): String = s(lang, "Settings", "సెట్టింగులు")
    fun settingsSubtitle(lang: AppLanguage): String = s(lang, "Preferences & app settings", "ప్రాధాన్యతలు & యాప్ అమరికలు")
    fun languagePreference(lang: AppLanguage): String = s(lang, "Language Preference", "భాష ఎంపిక (Language)")
    fun languageDesc(lang: AppLanguage): String = s(lang, "Select your preferred language for meal plans, metrics, and navigation", "యాప్‌లోని శీర్షికలు, ప్లాన్‌లు మరియు కొలతలకు భాషను ఎంచుకోండి")
    fun timeFormatPreference(lang: AppLanguage): String = s(lang, "Time Format", "సమయ ఫార్మాట్")
    fun timeFormatDesc(lang: AppLanguage): String = s(lang, "Choose between 12-hour (AM/PM) and 24-hour clock display for meals & schedule", "భోజన సమయాలు మరియు షెడ్యూల్‌ల కోసం 12-గంటల (AM/PM) లేదా 24-గంటల గడియారాన్ని ఎంచుకోండి")
    fun timeFormat12Hour(lang: AppLanguage): String = s(lang, "12-Hour (AM/PM)", "12-గంటలు (AM/PM)")
    fun timeFormat24Hour(lang: AppLanguage): String = s(lang, "24-Hour (24h)", "24-గంటలు (24h)")
    fun timeFormatLivePreview(lang: AppLanguage): String = s(lang, "Schedule Preview", "షెడ్యూల్ ప్రివ్యూ")
    fun unitsPreference(lang: AppLanguage): String = s(lang, "Units & Measurements", "కొలత ప్రమాణాలు")
    fun unitsDesc(lang: AppLanguage): String = s(lang, "Metric system used for weight and height", "బరువు మరియు ఎత్తు కోసం మెట్రిక్ కొలతలు")
    fun weightUnitLabel(lang: AppLanguage): String = s(lang, "Weight Unit", "బరువు ప్రమాణం")
    fun heightUnitLabel(lang: AppLanguage): String = s(lang, "Height Unit", "ఎత్తు ప్రమాణం")
    fun notificationsReminders(lang: AppLanguage): String = s(lang, "Notifications & Reminders", "నోటిఫికేషన్లు & రిమైండర్లు")
    fun notificationsDesc(lang: AppLanguage): String = s(lang, "System alerts for meals and water", "భోజనం మరియు నీటి కోసం నోటిఫికేషన్ అలర్ట్‌లు")
    fun hapticFeedback(lang: AppLanguage): String = s(lang, "Haptic Feedback", "వైబ్రేషన్ (హ్యాప్టిక్స్)")
    fun hapticFeedbackDesc(lang: AppLanguage): String = s(lang, "Vibrate on button clicks and actions", "బటన్లు నొక్కినప్పుడు వైబ్రేషన్ స్పందన")
    fun autoSyncReminders(lang: AppLanguage): String = s(lang, "Auto Sync Reminders", "ఆటో సింక్ రిమైండర్లు")
    fun autoSyncDesc(lang: AppLanguage): String = s(lang, "Automatically schedule alarms when times change", "సమయాలు మారినప్పుడు అలారాలను స్వయంచాలకంగా అప్‌డేట్ చేయండి")
    fun testSimulator(lang: AppLanguage): String = s(lang, "Test Notification Simulator", "నోటిఫికేషన్ సిమ్యులేటర్ పరీక్షించండి")
    fun testSimulatorDesc(lang: AppLanguage): String = s(lang, "Simulate meal alert notifications with actions", "భోజన అలర్ట్ నోటిఫికేషన్‌ను ఇప్పుడే అనుకరించండి")
    fun openSimulatorBtn(lang: AppLanguage): String = s(lang, "Open Simulator", "సిమ్యులేటర్ తెరవండి")
    fun walkthroughTour(lang: AppLanguage): String = s(lang, "App Walkthrough & Tour", "యాప్ గైడ్ & ఫీచర్స్ టూర్")
    fun walkthroughTourDesc(lang: AppLanguage): String = s(lang, "Explore app features with an interactive walkthrough", "యాప్ ఫీచర్ల వివరాలను సులభంగా తెలుసుకోండి")
    fun startTourBtn(lang: AppLanguage): String = s(lang, "View Tour", "టూర్ చూడండి")
    fun dataManagement(lang: AppLanguage): String = s(lang, "Data Management", "డేటా నిర్వహణ")
    fun clearTodayInputs(lang: AppLanguage): String = s(lang, "Clear Today's Log", "నేటి లాగ్‌ను క్లియర్ చేయండి")
    fun clearTodayInputsDesc(lang: AppLanguage): String = s(lang, "Reset today's meals and water without losing routine", "భోజన ప్రణాళిక మారకుండా నేటి తిన్న రికార్డులను క్లియర్ చేయండి")
    fun resetApp(lang: AppLanguage): String = s(lang, "Reset App Data", "యాప్ డేటాను పూర్తిగా రీసెట్ చేయండి")
    fun resetAppDesc(lang: AppLanguage): String = s(lang, "Clear all saved routines and return to onboarding", "అన్ని సెట్టింగులను క్లియర్ చేసి మళ్ళీ మొదటి నుండి ప్రారంభించండి")
    fun clearInputsConfirmTitle(lang: AppLanguage): String = s(lang, "Clear Today's Entries?", "నేటి రికార్డులను క్లియర్ చేయాలా?")
    fun clearInputsConfirmDesc(lang: AppLanguage): String = s(lang, "This will reset all consumed meals and water logged for today.", "ఇది ఈరోజు నమోదు చేసిన భోజనం మరియు నీటి రికార్డులను తీసివేస్తుంది.")
    fun resetAppConfirmTitle(lang: AppLanguage): String = s(lang, "Reset Entire App?", "యాప్‌ను పూర్తిగా రీసెట్ చేయాలా?")
    fun resetAppConfirmDesc(lang: AppLanguage): String = s(lang, "All personal information, routines, and custom foods will be reset to default.", "మీ ప్రొఫైల్, ఆహార ప్రణాళిక మరియు వివరాలు అన్నీ తొలగించబడతాయి.")

    // Food Pool Screen
    fun foodPoolTitle(lang: AppLanguage): String = s(lang, "Food Pool", "ఆహారాల జాబితా (ఫుడ్ పూల్)")
    fun foodPoolDesc(lang: AppLanguage): String = s(lang, "Select the foods you regularly eat. These are used in your daily meal plans.", "మీరు రోజూ తినే ఆహారాలను ఎంచుకోండి. ఇవి మీ భోజన ప్రణాళికలో ఉపయోగపడతాయి.")
    fun clearAllPool(lang: AppLanguage): String = s(lang, "Clear All", "అన్నీ తొలగించండి")
    fun addCustomFood(lang: AppLanguage): String = s(lang, "Add Custom Food", "కొత్త ఆహారాన్ని చేర్చండి")
    fun searchFoodPlaceholder(lang: AppLanguage): String = s(lang, "Search foods...", "ఆహారాలను వెతకండి...")
    fun allCategory(lang: AppLanguage): String = s(lang, "All", "అన్నీ")
    fun inPoolTag(lang: AppLanguage): String = s(lang, "In Pool", "పూల్‌లో ఉంది")
    fun notInPoolTag(lang: AppLanguage): String = s(lang, "Not in Pool", "పూల్‌లో లేదు")
    fun addToPool(lang: AppLanguage): String = s(lang, "Add to Pool", "పూల్‌కి చేర్చండి")
    fun removeFromPool(lang: AppLanguage): String = s(lang, "Remove", "తొలగించండి")
    fun selectedFoodsCount(count: Int, lang: AppLanguage): String = s(lang, "$count foods selected", "$count ఆహారాలు ఎంపికయ్యాయి")

    // Substitution Screen
    fun substituteTitle(lang: AppLanguage): String = s(lang, "Substitute Food", "ఆహారాన్ని మార్చుకోండి")
    fun substituteSubtitle(lang: AppLanguage): String = s(lang, "Swap your current item with a nutritionally similar food", "ఈ ఆహారానికి బదులుగా సమాన పోషకాలున్న మరొక ఆహారాన్ని ఎంచుకోండి")
    fun originalMeal(lang: AppLanguage): String = s(lang, "Current Planned Food", "ప్రస్తుత ఆహారం")
    fun chooseReplacement(lang: AppLanguage): String = s(lang, "Choose Replacement Food", "ప్రత్యామ్నాయ ఆహారాన్ని ఎంచుకోండి")
    fun poolFoodsTab(lang: AppLanguage): String = s(lang, "From Food Pool", "ఫుడ్ పూల్ నుండి")
    fun allFoodsTab(lang: AppLanguage): String = s(lang, "All Foods Database", "అన్ని ఆహారాల నుండి")
    fun swapFoodBtn(lang: AppLanguage): String = s(lang, "Confirm Substitution", "మార్పును నిర్ధారించండి")
    fun nutritionDifference(lang: AppLanguage): String = s(lang, "Nutrition Difference", "పోషకాల తేడా")
    fun portionAdjustment(lang: AppLanguage): String = s(lang, "Portion Quantity", "ఆహార పరిమాణం")

    // Meal Routine Schedule Screen
    fun routineScheduleTitle(lang: AppLanguage): String = s(lang, "Meal Routine & Foods", "భోజన దినచర్య & ఆహారాలు")
    fun routineScheduleSubtitle(lang: AppLanguage): String = s(lang, "Customize what you eat for each meal", "ప్రతి పూటకు మీరు తినే ఆహారాలను సెట్ చేయండి")
    fun saveRoutineBtn(lang: AppLanguage): String = s(lang, "Save Routine", "దినచర్యను సేవ్ చేయండి")
    fun openSimulator(lang: AppLanguage): String = s(lang, "Test Alarms", "అలారాలు పరీక్షించండి")

    // Water Settings Screen
    fun waterTrackerTitle(lang: AppLanguage): String = s(lang, "Water Tracker Settings", "నీటి ట్రాకర్ సెట్టింగులు")
    fun waterTrackerSubtitle(lang: AppLanguage): String = s(lang, "Daily target, quick logging, and smart reminders", "రోజువారీ లక్ష్యం, త్వరిత నమోదు మరియు రిమైండర్లు")
    fun dailyWaterTarget(lang: AppLanguage): String = s(lang, "Daily Water Target", "రోజువారీ నీటి లక్ష్యం")
    fun waterReminderAlerts(lang: AppLanguage): String = s(lang, "Hydration Reminder Notifications", "నీటి రిమైండర్ నోటిఫికేషన్లు")
    fun reminderInterval(lang: AppLanguage): String = s(lang, "Reminder Interval", "రిమైండర్ వ్యవధి")
    fun reminderIntervalHours(hours: Int, lang: AppLanguage): String = s(lang, "Every $hours hours", "ప్రతి $hours గంటలకు")
    fun simulateWaterAlert(lang: AppLanguage): String = s(lang, "Test Water Notification", "నీటి నోటిఫికేషన్ పరీక్షించండి")
    fun quickLogWaterTitle(lang: AppLanguage): String = s(lang, "Quick Log Water", "త్వరిత నీటి నమోదు")

    // Exercise Screen
    fun exerciseTitle(lang: AppLanguage): String = s(lang, "Daily Movement & Exercise", "రోజువారీ నడక & వ్యాయామం")
    fun exerciseSubtitle(lang: AppLanguage): String = s(lang, "Log physical activities and monitor calories burned", "శారీరక శ్రమ మరియు క్యాలరీ ఖర్చును నమోదు చేయండి")
    fun logMovement(lang: AppLanguage): String = s(lang, "Log Movement", "నమోదు చేయండి")
    fun walking(lang: AppLanguage): String = s(lang, "Brisk Walking", "వేగవంతమైన నడక")
    fun jogging(lang: AppLanguage): String = s(lang, "Jogging / Running", "జాగింగ్ / పరుగు")
    fun cycling(lang: AppLanguage): String = s(lang, "Cycling", "సైక్లింగ్")
    fun yoga(lang: AppLanguage): String = s(lang, "Yoga & Stretching", "యోగా & స్ట్రెచింగ్")
    fun strengthTraining(lang: AppLanguage): String = s(lang, "Strength Training", "వ్యాయామం / జిమ్")
    fun durationMin(min: Int, lang: AppLanguage): String = s(lang, "$min mins", "$min నిమిషాలు")
    fun burnedKcal(kcal: Int, lang: AppLanguage): String = s(lang, "$kcal kcal burned", "$kcal kcal ఖర్చు")

    // Edit Profile Screen
    fun editProfileTitle(lang: AppLanguage): String = s(lang, "Edit Profile", "ప్రొఫైల్ సవరించండి")
    fun editProfileSubtitle(lang: AppLanguage): String = s(lang, "Update your personal measurements and activity level", "మీ కొలతలు మరియు శ్రమ స్థాయిని అప్‌డేట్ చేయండి")
    fun saveProfileBtn(lang: AppLanguage): String = s(lang, "Save Profile", "ప్రొఫైల్ సేవ్ చేయండి")

    // Auth Screen
    fun welcomeToNutriPath(lang: AppLanguage): String = s(lang, "Welcome to NutriPath", "నూట్రిపాత్‌కి స్వాగతం")
    fun authSubtitle(lang: AppLanguage): String = s(lang, "Your personalized Indian nutrition & meal planning companion", "మీ వ్యక్తిగత పోషకాహార మరియు భోజన ప్రణాళిక యాప్")
    fun loginTab(lang: AppLanguage): String = s(lang, "Login", "లాగిన్")
    fun registerTab(lang: AppLanguage): String = s(lang, "Sign Up", "రిజిస్టర్")
    fun emailField(lang: AppLanguage): String = s(lang, "Email Address", "ఈమెయిల్ చిరునామా")
    fun passwordField(lang: AppLanguage): String = s(lang, "Password", "పాస్‌వర్డ్")
    fun fullNameField(lang: AppLanguage): String = s(lang, "Full Name", "పూర్తి పేరు")
    fun tryDemoAccount(lang: AppLanguage): String = s(lang, "Try Instant Demo Account", "డెమో ఖాతాతో ప్రారంభించండి")

    // Onboarding Strings
    fun setupTitle(lang: AppLanguage): String = s(lang, "Profile & Nutrition Setup", "ప్రొఫైల్ & పోషకాహార సెటప్")
    fun stepOf(current: Int, total: Int, lang: AppLanguage): String = s(lang, "Step $current of $total", "దశ $current / $total")

    fun step1Title(lang: AppLanguage): String = s(lang, "Welcome to NutriPath", "నూట్రిపాత్‌కి స్వాగతం")
    fun step1Subtitle(lang: AppLanguage): String = s(lang, "Answer a few quick questions to set up your plan.", "మీ ఆహార ప్రణాళికను సిద్ధం చేయడానికి కొన్ని సాధారణ ప్రశ్నలకు సమాధానం ఇవ్వండి.")
    fun yourName(lang: AppLanguage): String = s(lang, "Your Name", "మీ పేరు")
    fun biologicalSex(lang: AppLanguage): String = s(lang, "Biological Sex", "లింగం")
    fun biologicalSexDesc(lang: AppLanguage): String = s(lang, "Used to calculate your daily energy burn.", "మీ రోజువారీ క్యాలరీ అవసరాన్ని లెక్కించడానికి ఉపయోగపడుతుంది.")
    fun male(lang: AppLanguage): String = s(lang, "Male", "పురుషుడు")
    fun female(lang: AppLanguage): String = s(lang, "Female", "స్త్రీ")
    fun ageLabel(lang: AppLanguage): String = s(lang, "Age (15+)", "వయస్సు (15+)")
    fun heightLabel(lang: AppLanguage): String = s(lang, "Height (cm)", "ఎత్తు (cm)")
    fun weightLabel(lang: AppLanguage): String = s(lang, "Weight (kg)", "బరువు (kg)")
    fun activityLevel(lang: AppLanguage): String = s(lang, "Daily Activity Level", "రోజువారీ శ్రమ స్థాయి")
    fun activityLevelDesc(lang: AppLanguage): String = s(lang, "How active you are on a normal day.", "సాధారణ రోజులో మీ శారీరక చురుకుదనం.")
    fun activityLow(lang: AppLanguage): String = s(lang, "Low (Sedentary / Desk)", "తక్కువ (ఆఫీస్ / కూర్చునే పని)")
    fun activityModerate(lang: AppLanguage): String = s(lang, "Moderate (Active daily)", "మధ్యస్థం (చురుకైన రోజువారీ పని)")
    fun activityHigh(lang: AppLanguage): String = s(lang, "High (Heavy workout / Physical job)", "ఎక్కువ (ఎక్కువ శ్రమ / వ్యాయామం)")

    fun step2Title(lang: AppLanguage): String = s(lang, "Choose your Body Build", "మీ శరీర రకాన్ని ఎంచుకోండి")
    fun step2Subtitle(lang: AppLanguage): String = s(lang, "Choose the body type that fits you best.", "మీ శరీర స్వరూపానికి సరిపోయే రకాన్ని ఎంచుకోండి.")
    fun buildLean(lang: AppLanguage): Pair<String, String> =
        Pair(s(lang, "Lean", "సన్నని శరీరం (Lean)"), s(lang, "Slim frame with lower muscle", "తక్కువ కొవ్వు మరియు కండరాలతో సన్నని నిర్మాణం"))
    fun buildAverage(lang: AppLanguage): Pair<String, String> =
        Pair(s(lang, "Average", "సాధారణ శరీరం (Average)"), s(lang, "Medium, balanced build", "సమతుల్య మధ్యస్థ శరీర నిర్మాణం"))
    fun buildAthletic(lang: AppLanguage): Pair<String, String> =
        Pair(s(lang, "Athletic", "దృఢమైన శరీరం (Athletic)"), s(lang, "Toned with higher muscle", "మంచి కండరాలతో దృఢమైన ఆరోగ్యకర నిర్మాణం"))
    fun buildStocky(lang: AppLanguage): Pair<String, String> =
        Pair(s(lang, "Stocky", "బలమైన శరీరం (Stocky)"), s(lang, "Broad, heavier build", "విశాలమైన మరియు బరువైన శరీర నిర్మాణం"))

    fun step3Title(lang: AppLanguage): String = s(lang, "Your Personalized Requirements", "మీ వ్యక్తిగత పోషకాహార అవసరాలు")
    fun step3Subtitle(lang: AppLanguage): String = s(lang, "Daily targets calculated from your age, height, weight, and activity.", "మీ వయస్సు, ఎత్తు, బరువు మరియు శ్రమ ఆధారంగా రోజువారీ లక్ష్యాలు.")
    fun bmiTitle(lang: AppLanguage): String = s(lang, "Body Mass Index (BMI)", "బాడీ మాస్ ఇండెక్స్ (BMI)")
    fun bmrShortLabel(lang: AppLanguage): String = s(lang, "BMR (Resting)", "BMR (విశ్రాంతి)")
    fun tdeeShortLabel(lang: AppLanguage): String = s(lang, "TDEE (Maint.)", "TDEE (శ్రమతో)")
    fun healthyWeightShortLabel(lang: AppLanguage): String = s(lang, "Healthy Wt", "సరైన బరువు")
    fun caloriesShortLabel(lang: AppLanguage): String = s(lang, "Calories", "క్యాలరీలు")
    fun proteinShortLabel(lang: AppLanguage): String = s(lang, "Protein", "ప్రోటీన్")
    fun carbsShortLabel(lang: AppLanguage): String = s(lang, "Carbs", "కార్బ్స్")
    fun fatsShortLabel(lang: AppLanguage): String = s(lang, "Fats", "కొవ్వులు")
    fun fiberShortLabel(lang: AppLanguage): String = s(lang, "Fiber", "ఫైబర్")
    fun dailyCalorieTarget(lang: AppLanguage): String = s(lang, "DAILY CALORIE TARGET", "రోజువారీ క్యాలరీ లక్ష్యం")
    fun dailyCalorieDesc(lang: AppLanguage): String = s(lang, "Your daily energy to stay healthy and active.", "రోజంతా చురుగ్గా ఉండటానికి అవసరమైన శక్తి.")

    fun proteinTitle(lang: AppLanguage): String = s(lang, "Protein", "ప్రోటీన్")
    fun proteinDesc(lang: AppLanguage): String = s(lang, "Builds muscle and keeps you strong", "కండరాల నిర్మాణం మరియు శరీర బలానికి")
    fun carbsTitle(lang: AppLanguage): String = s(lang, "Carbohydrates", "కార్బోహైడ్రేట్లు")
    fun carbsDesc(lang: AppLanguage): String = s(lang, "Gives you steady energy throughout the day", "రోజంతా స్థిరమైన శక్తిని అందిస్తుంది")
    fun fatsTitle(lang: AppLanguage): String = s(lang, "Healthy Fats", "ఆరోగ్యకర కొవ్వులు")
    fun fatsDesc(lang: AppLanguage): String = s(lang, "Helps absorb vitamins and protects your body", "విటమిన్ల శోషణకు మరియు రక్షణకు")
    fun fiberTitle(lang: AppLanguage): String = s(lang, "Dietary Fiber", "ఫైబర్ (పీచు)")
    fun fiberDesc(lang: AppLanguage): String = s(lang, "Keeps digestion smooth and helps you feel full", "జీర్ణక్రియ మరియు ఆకలి నియంత్రణకు")
    fun waterTitle(lang: AppLanguage): String = s(lang, "Daily Water", "రోజువారీ నీరు")
    fun waterDesc(lang: AppLanguage): String = s(lang, "Keeps you hydrated and fresh all day", "శరీరాన్ని తాజాగా మరియు హైడ్రేటెడ్‌గా ఉంచుతుంది")

    fun step4Title(lang: AppLanguage): String = s(lang, "Food Pool", "ఆహార జాబితా (Food Pool)")
    fun step4Subtitle(lang: AppLanguage): String = s(lang, "Select the foods you eat regularly. You can plan with them and swap them anytime.", "మీరు రోజూ తినే ఆహారాలను ఎంచుకోండి. వీటితో మీరు భోజన ప్రణాళిక సిద్ధం చేసుకోవచ్చు.")
    fun searchFoods(lang: AppLanguage): String = s(lang, "Search foods...", "ఆహారాలను వెతకండి...")
    fun selectedItemsCount(count: Int, lang: AppLanguage): String = s(lang, "$count items selected", "$count ఆహారాలు ఎంపికయ్యాయి")

    fun step5Title(lang: AppLanguage): String = s(lang, "How Daily Meals Work", "భోజన ప్రణాళిక విధానం")
    fun step5Subtitle(lang: AppLanguage): String = s(lang, "Here is how to divide your foods across your meals each day.", "రోజూ మీ ఆహారాన్ని వివిధ పూటల్లో ఎలా విభజించుకోవాలో చూడండి.")
    fun tipsTitle(lang: AppLanguage): String = s(lang, "Tips for Planning", "ప్రణాళిక చిట్కాలు")
    fun tipsDesc(cals: Int, lang: AppLanguage): String = s(lang, "Spread your $cals kcal target across Breakfast, Lunch, Snacks, and Dinner for balanced daily energy.", "మీ $cals kcal లక్ష్యాన్ని అల్పాహారం, మధ్యాహ్నం, స్నాక్స్ మరియు రాత్రి భోజనాల్లో సమతుల్యంగా విభజించండి.")
    fun nutritionBudget(lang: AppLanguage): String = s(lang, "Your Daily Nutrition Budget", "మీ రోజువారీ పోషకాహార బడ్జెట్")
    fun recommendedDivision(lang: AppLanguage): String = s(lang, "Recommended Meal Division", "సూచించిన భోజన విభజన")
    fun balancedMealsDesc(lang: AppLanguage): String = s(lang, "Balanced meals keep your energy steady all day.", "సమతుల్య భోజనం రోజంతా మీ శక్తిని స్థిరంగా ఉంచుతుంది.")

    fun breakfastTitle(lang: AppLanguage): String = s(lang, "Breakfast (~25%)", "అల్పాహారం (~25%)")
    fun breakfastAdvice(lang: AppLanguage): String = s(lang, "Start your day with protein and fiber.", "ప్రోటీన్ మరియు ఫైబర్‌తో మంచి ఆరంభం.")
    fun lunchTitle(lang: AppLanguage): String = s(lang, "Lunch (~35%)", "మధ్యాహ్న భోజనం (~35%)")
    fun lunchAdvice(lang: AppLanguage): String = s(lang, "Main meal with grains, veggies, and protein.", "ధాన్యాలు, కూరగాయలు మరియు ప్రోటీన్‌తో ప్రధాన భోజనం.")
    fun snacksTitle(lang: AppLanguage): String = s(lang, "Snacks (~15%)", "స్నాక్స్ (~15%)")
    fun snacksAdvice(lang: AppLanguage): String = s(lang, "Light bite with fruit, nuts, or sprouts.", "పండ్లు లేదా గింజలతో తేలికపాటి ఆహారం.")
    fun dinnerTitle(lang: AppLanguage): String = s(lang, "Dinner (~25%)", "రాత్రి భోజనం (~25%)")
    fun dinnerAdvice(lang: AppLanguage): String = s(lang, "Lighter protein and veggies for restful sleep.", "మంచి నిద్ర కోసం ప్రోటీన్ & కూరగాయలతో తేలికపాటి భోజనం.")

    fun step6Title(lang: AppLanguage): String = s(lang, "Meal Routine & Items", "భోజన దినచర్య & ఆహారాలు")
    fun step6Subtitle(lang: AppLanguage): String = s(lang, "Assign foods from your pool to each daily meal.", "మీ ఫుడ్ పూల్ నుండి ప్రతి పూటకు ఆహారాలను ఎంచుకోండి.")
    fun weekdayTab(lang: AppLanguage): String = s(lang, "Weekday (Mon-Fri)", "వారపు రోజులు (సోమ-శుక్ర)")
    fun weekendTab(lang: AppLanguage): String = s(lang, "Weekend (Sat-Sun)", "వారాంతం (శని-ఆది)")
    fun searchPoolFoods(lang: AppLanguage): String = s(lang, "Search pool foods...", "ఆహారాలను వెతకండి...")
    fun unassigned(lang: AppLanguage): String = s(lang, "None (Unassigned)", "కేటాయించలేదు")

    fun mealName(meal: String, lang: AppLanguage): String = when (meal.lowercase()) {
        "breakfast" -> s(lang, "Breakfast", "అల్పాహారం")
        "lunch" -> s(lang, "Lunch", "మధ్యాహ్న భోజనం")
        "snacks" -> s(lang, "Snacks", "స్నాక్స్")
        "dinner" -> s(lang, "Dinner", "రాత్రి భోజనం")
        else -> meal
    }

    fun reviewPlanTitle(lang: AppLanguage): String = s(lang, "Review Your Meal Plan", "మీ భోజన ప్రణాళిక సమీక్ష")
    fun reviewPlanSubtitle(lang: AppLanguage): String = s(lang, "Here is how your planned routine compares to your daily target.", "మీ రోజువారీ లక్ష్యంతో మీ భోజన ప్రణాళిక పోలిక ఇక్కడ చూడండి.")
    fun totalPlanned(lang: AppLanguage): String = s(lang, "Total Planned", "మొత్తం ప్రణాళిక")
    fun dailyTarget(lang: AppLanguage): String = s(lang, "Daily Target", "రోజువారీ లక్ష్యం")
    fun editRoutine(lang: AppLanguage): String = s(lang, "Edit Routine", "మార్పులు చేయండి")
    fun continueToSchedule(lang: AppLanguage): String = s(lang, "Continue to Schedule", "షెడ్యూల్‌కి కొనసాగించండి")

    fun step7Title(lang: AppLanguage): String = s(lang, "Meal Timings & Schedule", "భోజన సమయాలు & షెడ్యూల్")
    fun step7Subtitle(lang: AppLanguage): String = s(lang, "Set when you eat each meal and configure timely reminders.", "మీరు ఎప్పుడు భోజనం చేయాలో సమయాలను మరియు రిమైండర్లను సెట్ చేసుకోండి.")
    fun separateWeekendSchedule(lang: AppLanguage): String = s(lang, "Separate Weekend Schedule", "వారాంతానికి వేరే షెడ్యూల్")
    fun separateWeekendDesc(lang: AppLanguage): String = s(lang, "Configure different meal timings and foods for Saturday & Sunday.", "శనివారం మరియు ఆదివారం కోసం ప్రత్యేక సమయాలు మరియు ఆహారాలను సెట్ చేయండి.")
    fun reminderLeadTime(lang: AppLanguage): String = s(lang, "Reminder Lead Time", "ముందస్తు రిమైండర్ సమయం")
    fun minutesBeforeMeal(minutes: Int, lang: AppLanguage): String = s(lang, "$minutes minutes before mealtime", "భోజనానికి $minutes నిమిషాల ముందు")
}
