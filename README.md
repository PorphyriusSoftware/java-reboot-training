<p align="center">
  <img src="assets/banner-variant-s.svg" width="100%" alt="Java Reboot Training Banner"/>
</p>

<h1 align="center">Java Reboot Training</h1>

<p align="center">
  <strong>A clean, modern, senior‑grade Java training project by PorphyriusSoftware.</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-007396?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/Maven-3.9.0-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" />
  <img src="https://img.shields.io/badge/Linux-000000?style=for-the-badge&logo=linux&logoColor=white" />
  <img src="https://img.shields.io/badge/Build-Clean%20%26%20Strict-22c55e?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Status-Active-38bdf8?style=for-the-badge" />
</p>

---

## 📘 Overview

This repository is part of a structured, senior‑grade Java reboot program.  
It focuses on clean architecture, functional pipelines, test‑driven development, and professional project hygiene.

The goal is to build a modern Java skillset with:
- Clean, readable, maintainable code
- Strong testing discipline
- Functional programming patterns
- Modular design
- Real‑world project structure

---

## 🧰 Tech Stack

- **Java 17**
- **Maven 3.9+**
- **JUnit 5**
- **Mockito**
- **AssertJ**
- **GitHub Actions (CI)**
- **Linux‑friendly tooling**
- **JetBrains Mono / Fira Code** (recommended for ASCII banners)

---

## 📁 Project Structure

```
java-reboot-training/
├── assets/
│   └── banner-variant-s.svg
├── src/
│   ├── main/
│   │   └── java/
│   └── test/
│       └── java/
├── pom.xml
└── README.md
```

- `src/main/java` → application code
- `src/test/java` → unit tests
- `assets/` → banners, diagrams, visuals

---

## 🚀 Running the Project

### **Build**
```bash
mvn clean install
Run Tests
bash
mvn test
Run with Maven Exec Plugin (if configured)
bash
mvn exec:java

```

🧪 Testing Philosophy
This project follows a senior‑grade testing approach:

Small, isolated tests

No mocking unless necessary

Clear Arrange‑Act‑Assert structure

Readable test names

Edge‑case coverage

Zero flaky tests

Fast feedback loop

Tests are treated as first‑class citizens.

🛠️ Build & CI
A GitHub Actions workflow ensures:

Clean builds

Test execution

No regressions

No broken commits

CI is strict by design — the project must always be in a green state.



🤝 Contributing
Contributions are welcome — but must follow:

Clean code

Clear commit messages

Tests for every change

No unused dependencies

No commented‑out code

Open a PR and we’ll review it with senior‑level standards.

📄 License
This project is licensed under the MIT License.
You are free to use, modify, and distribute it.

<p align="center">
<sub>Maintained with discipline and style by <strong>PorphyriusSoftware</strong>.</sub>
</p>
