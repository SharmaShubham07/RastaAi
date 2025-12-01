# RastaAi – Course Management App

This project is a part of the developer assignment for **Aiunika**.
The goal was to build a small Android application where a user can manage courses, fetch categories from an API, and allow basic CRUD operations.
I have implemented the application using a clean and modular approach, keeping readability and maintainability in mind.

---

## 1. Project Overview

The app allows the user to:

* View a list of courses
* Add a new course
* Edit an existing course
* Delete a course
* Search courses by title/description
* Filter courses by category
* View course details

Categories are fetched from a MockAPI endpoint and stored locally for offline usage.
Courses are always stored locally using Room.

---

## 2. Features Implemented

### Course List

* Displays all saved courses
* Search bar with live filtering
* Category filter (dropdown attached to Add/Edit screen)
* Loading state and empty state

### Add Course

* Title
* Description
* Category (fetched from API)
* Number of lessons

Score is automatically generated using:

```
score = title.length × lessons
```

### Edit Course

* Loads existing values into the form
* Allows updating all fields

### Course Details

Shows all information including score and category name.

### Delete Course

* Uses a confirmation dialog

### Offline Behaviour

* Categories are saved locally after the first API call
* Courses always work completely offline

---

## 3. Architecture

The app uses a clean MVVM structure:

```
data/
   local/ (Room DB + DAO + Entities)
   remote/ (Retrofit API)
   repository/
ui/
   main/ (Fragments + ViewModels)
   adapter/
```

Tools used:

* **Hilt** for dependency injection
* **Room** for local storage
* **Retrofit + OkHttp** for API consumption
* **Kotlin Flow** for observable data
* **Jetpack Navigation** for screen transitions

I kept the structure simple but scalable, so new features can be added easily.

---

## 4. API Details

The categories are fetched from:

```
https://6929899e9d311cddf34a4eab.mockapi.io/categories
```

Example response:

```json
[
  { "id": "1", "name": "Programming" },
  { "id": "2", "name": "Business" }
]
```

The API returns the ID as a **string**, so it is safely converted into a numeric format before saving in Room.

---

## 5. Local Database Models

### CategoryEntity

```kotlin
id: Long
name: String
```

### CourseEntity

```kotlin
id: Long
title: String
description: String
categoryId: Long?
categoryName: String?
lessons: Int
score: Int
createdAt: Long
```

---

## 6. How to Run the Project

1. Clone or download the project
2. Open the project in **Android Studio Hedgehog or newer**
3. Let Gradle sync
4. Run the app on an emulator or device (Android 8+)

No additional configuration is required.

---

## 7. APK Build Instructions

Android Studio →
**Build > Build APK(s)**

APK will be available at:

```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 8. Notes from Development

* I kept the UI minimal and clean
* Categories load once and are reused offline
* The codebase is structured so future features (authentication, bookmarks, etc.) can be added easily
* All major crashes were resolved and the app runs smoothly now
* Navigation and Safe Args are implemented correctly


