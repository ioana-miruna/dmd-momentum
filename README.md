# **MOMENTUM** App

## Description
**Momentum** is a budget tracking Android application which has different features including adding expenses based on categories such as food or others, allowing the users to stock all their monthly expenses. The users are also able to see their expenses one by one, as well as the total expenses for each category and the total spent during the month. By putting thresholds, real-time elapsed and counting the time spent in the app, the users can have a better own financial control. 

The app also encourages the user to add their expenses daily by sending notifications to different hours during the day and has functionalities regarding the airplane mode is on and off. The user can restore the monthly budget every time it is needed, on salary day or by their own judgement.

Having control of our budget can improve our lifestyles, therefore using Momentum provides the perfect workplace for this.

## Features
1. **Expense Management:**
   - Add expenses with detailed information, including name, amount, and category (Food, Others).
   - Store expenses persistently using `SharedPreferences`.
   - View categorized and summarized expenses, including totals for each category and the remaining budget.
   - Share expense summaries via social apps and reset all expenses when required.

2. **App Usage Monitoring:**
   - Tracks and logs the time spent within the app in real-time.
   - Provides elapsed time data on the main screen, updated every second.
   - Stops tracking when the app is closed or removed from recent apps.

3. **Notifications and Reminders:**
   - Sends notifications to remind users to log their expenses three times a day (10:00 AM, 4:00 PM, 8:00 PM).
   - Real-time notifications for elapsed time while the app is running.

4. **System and Custom Broadcasts:**
   - Responds to airplane mode changes to adjust app functionality dynamically.
   - Allows custom broadcasts with app-specific actions.

---

## Components Used in the Project

### **Activities**
1. **`MainActivity`**
   - **Purpose:** Acts as the main entry point for the app.
   - **Key Features:**
     - Allows users to navigate to the Add Expense or View Expenses screens.
     - Sets daily alarms for notifications using `AlarmManager`.
     - Binds to the `TaskMonitoringService` to display elapsed time in real-time.
     - Handles custom broadcast sending and airplane mode status updates.

2. **`AddExpenseActivity`**
   - **Purpose:** Allows users to input and save their expenses.
   - **Key Features:**
     - Collects expense details such as name, amount, and category.
     - Validates inputs to ensure data accuracy.
     - Saves expense data in `SharedPreferences` as a JSON array.

3. **`ViewExpensesActivity`**
   - **Purpose:** Displays all recorded expenses and summary data.
   - **Key Features:**
     - Categorizes and sums expenses into totals for Food and Others.
     - Shows the remaining budget based on a preset monthly budget.
     - Allows sharing expense summaries via external apps.
     - Provides a back button to return to the main screen.

---

### **Services**
1. **`BackgroundCounterService`**
   - **Purpose:** Tracks the total time spent within the app.
   - **Key Features:**
     - Starts as a foreground service to ensure consistent monitoring.
     - Updates a persistent notification with the elapsed time.
     - Stops tracking and logs the total time when the app is closed or removed.

2. **`BudgetTrackerService`**
   - **Purpose:** Sends periodic reminders to log expenses.
   - **Key Features:**
     - Runs as a foreground service with high-priority notifications.
     - Reminds users at fixed times daily using the `AlarmManager`.

3. **`TaskMonitoringService`**
   - **Purpose:** Monitors specific task durations within the app.
   - **Key Features:**
     - Provides a `LocalBinder` for activities to interact with the service.
     - Tracks elapsed time for user-defined tasks and updates notifications in real-time.
     - Saves and restores task monitoring states using `SharedPreferences`.

---

### **Broadcast Receivers**
1. **`AlarmReceiver`**
   - **Purpose:** Handles alarm events to send expense reminders.
   - **Key Features:**
     - Receives alarms set by `AlarmManager` and triggers the `BudgetTrackerService` to send notifications.
     - Logs when alarms are received for debugging purposes.

2. **`MyBroadcastReceiver`**
   - **Purpose:** Handles custom broadcasts sent by the app.
   - **Key Features:**
     - Displays messages received via custom intents using toast notifications.

3. **Airplane Mode Receiver** (Implemented in `MainActivity`)
   - **Purpose:** Listens for airplane mode changes.
   - **Key Features:**
     - Dynamically updates app functionality (e.g., disabling broadcast sending).

---

### **Other Components**
1. **`SharedPreferences`**
   - Used extensively to store:
     - Expense data as a JSON array.
     - Task monitoring states, including monitoring status and start time.
   - Ensures data persistence even when the app is closed.

2. **Alarms**
   - Set using the `AlarmManager` to trigger notifications at predefined times daily.

3. **Notifications**
   - Implemented with `NotificationChannel` to ensure compatibility with Android O+ devices.
   - Used in `BackgroundCounterService`, `BudgetTrackerService`, and `TaskMonitoringService`.

4. **Threads and Handlers**
   - `Handler` is used in `TaskMonitoringService` to update notifications in real-time.
   - A separate thread is used in `BackgroundCounterService` to calculate elapsed time without blocking the main thread.

---

## Example Usage
1. Open the app to access the main menu.
2. Add expenses using the **Add Expense** button. Enter the expense name, amount, and category, then save it.
3. View detailed expense summaries by selecting the **View Expenses** option. Check categorized totals and share summaries if needed.
4. Monitor app usage time on the main screen, with real-time updates displayed at the top.
5. Reset all expenses monthly or as needed using the **Reset Monthly** button.
