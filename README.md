# Modu - Clothing Store

**Modu** is a native Android application designed to offer a seamless, modern, and efficient clothing shopping experience. The project serves as a solid foundation for an e-commerce platform, implementing a dynamic catalog with advanced filters and a shopping cart management system.

## Core Features

* **Product Catalog:** Display of garments with high-resolution images and technical details.
* **Filter System:** Dynamic filtering by categories, sizes, colors, and price ranges.
* **Shopping Cart:** Local cart management (add, remove, and update quantities) with data persistence.
* **Intuitive Navigation:** Optimized flow from product discovery to cart checkout.

## Architecture and Tech Stack

The project follows **Clean Architecture** principles and the **MVVM** (Model-View-ViewModel) design pattern, ensuring scalable, testable, and maintainable code.

* **Language:** [Kotlin](https://kotlinlang.org/)
* **Dependency Injection:** [Hilt](https://developer.android.com/training/dependency-injection/hilt-android) for clean and decoupled dependency management.
* **Networking:** [Retrofit 2](https://square.github.io/retrofit/) for consuming the products API.
* **Local Database:** [Room](https://developer.android.com/training/data-storage/room) for cart persistence and product caching (Offline-first approach).
* **Navigation:** [Navigation Component](https://developer.android.com/guide/navigation) with safe argument passing (Safe Args).
* **Asynchrony:** Coroutines and Flows for reactive data stream management.
* **UI:** View Binding for safe interaction with XML views.

## Prerequisites and Environment

To compile the project without errors, make sure you meet these version requirements:

* **Android Studio:** Panda 4 | 2025.3.4 Canary 4 (or higher).
* **Java Development Kit (JDK):** Version 21.
* **Gradle:** Version 9.1.0.

## Initial Setup

1.  **Clone the repository:**
    ```bash
    git clone <your-bitbucket-repository-url>
    ```
2.  **Open in Android Studio:** Select the root folder of the project and wait for the Gradle synchronization to finish.
3.  **Configure Local SDK:**
    Ensure your `local.properties` file points correctly to your SDK. Example:
    ```properties
    sdk.dir=C\:\\Users\\jlopezf\\AppData\\Local\\Android\\Sdk
    ```
4.  **Run:** Press `Shift + F10` or the **Run** icon in Android Studio.

---