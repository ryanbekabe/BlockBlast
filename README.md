# Block Blast

A classic block puzzle game built with Modern Android development practices. This project is uniquely designed to run on both **Android Mobile** and **Android TV** using a single codebase with Jetpack Compose.

## 🎮 Features

- **Adaptive Layouts**: Automatically switches between Portrait (Mobile) and Landscape (TV) modes.
- **Android TV Support**: Optimized for D-Pad navigation using `androidx.tv.material3`.
- **Game Mechanics**:
  - 8x8 Grid system.
  - 3-block pool generation.
  - Ghost preview for precise block placement.
  - Row and Column clearing logic with combo scoring.
- **Modern UI**: Built entirely with Jetpack Compose and Material 3.

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **TV Components**: Compose for TV (Material 3)
- **Architecture**: MVVM (ViewModel, State-driven UI)
- **Build System**: Gradle Kotlin DSL

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug or newer.
- Android SDK 34+.

### How to Build
1. Clone the repository:
   ```bash
   git clone https://github.com/ryanbekabe/BlockBlast.git
   ```
2. Open the project in Android Studio.
3. Sync Gradle and run the `:app` module.

## 🕹️ Controls

| Input | Action (TV/Keyboard) | Action (Mobile) |
|-------|----------------------|-----------------|
| **D-Pad / Arrows** | Move selection / Navigate grid | - |
| **Enter / Center** | Select block / Place block | Tap to select/place |
| **Back / Esc** | Cancel placement | Tap "Cancel" button |

---
Developed by [Ryan](https://github.com/ryanbekabe)
