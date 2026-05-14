# Smart Household Scheduler

### Java Final Project

**Eleanor Robertson**
CS6103 Section I

---

# Project Overview

Smart Household Scheduler is a Java desktop application designed to help families organize household tasks, appointments, and responsibilities in a shared scheduling system. Users can add tasks for different family members, view schedules, automatically assign tasks into available time slots, and prevent scheduling conflicts between overlapping tasks.

The project moves beyond a basic CRUD application by incorporating scheduling logic, conflict detection, multithreading, persistent database storage, and event-driven GUI programming. The application was built using Java Swing for the interface and SQLite for persistent storage.

---

# Advanced Topics Used

## GUI / Event-Driven Programming

The application uses Java Swing to create an interactive graphical user interface. Users interact with forms, buttons, combo boxes, and tables to manage household schedules. Event listeners are used to handle user actions such as adding tasks, deleting tasks, and auto-scheduling tasks.

---

## Database

The project uses SQLite to permanently store task data. Tasks are saved into a database and automatically reloaded when the application starts again. This allows schedules to persist across sessions instead of existing only temporarily in memory.

---

## Multithreading

The application uses `SwingWorker` background threads to perform database operations without freezing the GUI. Loading, saving, and deleting tasks are handled asynchronously so the interface remains responsive during longer operations.

---

## Scheduling Logic and Conflict Detection

The system includes scheduling logic that automatically places tasks into available time slots. It also detects scheduling conflicts by preventing overlapping tasks for the same family member on the same date and time.

---

# Features

* Add household tasks and appointments
* Assign tasks to family members
* Store schedules in a SQLite database
* Automatically detect scheduling conflicts
* Automatically schedule tasks into available time slots
* Delete existing tasks
* Persistent schedule storage across sessions
* Calendar View
* Drag-and-Drop from Calendar View
* Responsive GUI using multi-threading

---

# Technologies Used

* Java
* Java Swing
* SQLite
* JDBC (SQLite JDBC Driver)
* SwingWorker Multi-threading

---

## Video Demo

Watch the project demo here:

[Smart Household Scheduler Demo](https://drive.google.com/drive/folders/17vnL2uxifxltUNZ8K3Igee60o4QM_5v6?usp=drive_link)
(Will delete after)


# Project Structure

```text
SmartHouseholdScheduler/
├── src/
│  	├── Main.java
│  	├── Task.java
│  	├── DatabaseManager.java
│  	├── ConflictManager.java
│  	├── SchedulerUI.java
│  	├── CalendarPanel.java
│  	└── SmartHouseholdGUI.java
├── ReferencedLibraries/
│   └── sqlite-jdbc-xxxxx.jar
└── README.md
```

---

# How to Run the Project

## 1. Download SQLite JDBC Driver

Download the SQLite JDBC jar file and place it inside the `ReferencedLibraries` folder:

```text
sqlite-jdbc-xxxxx.jar
```

---

## 2. Compile the Project

### Windows

```bash
javac -cp "ReferencedLibraries/sqlite-jdbc-xxxxx.jar" -d out src/smarthouseholdscheduler/*.java
```

### Mac/Linux

```bash
javac -cp "ReferencedLibraries/sqlite-jdbc-xxxxx.jar" -d out src/smarthouseholdscheduler/*.java
```

---

## 3. Run the Project

### Windows

```bash
java -cp "out;ReferencedLibraries/sqlite-jdbc-xxxxx.jar" smarthouseholdscheduler.Main
```

### Mac/Linux

```bash
java -cp "out:ReferencedLibraries/sqlite-jdbc-xxxxx.jar" smarthouseholdscheduler.Main
```

---

# Demo Instructions

1. Launch the application
2. Enter a task name, family member, date, start time, duration, and priority
3. Click **Add Task** to save the task
4. Try adding another overlapping task for the same family member to see conflict detection
5. Use **Auto Schedule** to automatically place a task into an available time slot
6. Switch to **Calendar View** to see tasks
7. **Drag-and-Drop** events into a different day
8. Close and reopen the application to verify database persistence

---

# Notes

* The application automatically creates the SQLite database file when it runs
* No external API keys or datasets are required
* The project was designed as a simplified but functional household scheduling system demonstrating advanced Java concepts from the course
