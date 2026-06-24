<h1>
  EtCare - Care that fits your life!
</h1>

[![Android](https://img.shields.io/badge/Android-3DDC84?style=flat&logo=android&logoColor=white)](https://developer.android.com)
[![Java](https://img.shields.io/badge/Java-11+-ED8B00?style=flat&logo=openjdk&logoColor=white)](https://www.java.com)
[![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=flat&logo=firebase&logoColor=black)](https://firebase.google.com)
[![GitHub](https://img.shields.io/badge/GitHub-Repository-181717?style=flat&logo=github)](https://github.com/Fuad-Fu/Etcare)

## Overview

**Etcare**  is a feature‑rich Android telehealth application that connects patients with doctors for real‑time text consultations. The app demonstrates practical mobile development concepts like fragment‑based navigation, Firestore‑backed real‑time chat, SQLite for local habits, and multi‑language content delivery. It combines appointment scheduling with time‑based activation, a wellness habit tracker, and offline‑first patterns to deliver a complete, production‑style health platform.

This repository is a capstone‑level project suitable for portfolio and demo use. Code is organized into modular UI components, Firebase data flows, and typical Android patterns (RecyclerView, adapters, Parcelable models, and shared preferences).

---

## Features
<ul>
  <li>Doctor discovery & booking — browse doctors by specialty, language, and available time slots; book a text‑based consultation that is stored in Firestore.</li>
  <li>Real‑time consultation chat — Firestore‑powered messaging with instant delivery; simulated doctor replies and an option to end the consultation with a saved prescription note.</li>
  <li>Smart appointment management — three tabs (Upcoming / Active / Completed); appointments automatically move to Active when the scheduled time arrives.</li>
  <li>Wellness plan (habit tracker) — daily checklist with local SQLite storage, streak counting, and custom habit creation.</li>
  <li>Multilingual health articles — browse, search, and read articles in English, Amharic, and Afaan Oromo; content is served from language‑specific Firestore collections.</li>
  <li>Firebase authentication — email/password sign‑up, login, session persistence, and password reset.</li>
</ul>

---

## Tech Stack
<ul>
  <li>Android (Java) — UI, app logic, fragment‑based navigation with custom bottom navigation bar.</li>
  <li>Firebase (Firestore, Auth) — real‑time data, user management, and serverless backend.</li>
  <li>SQLite — local storage for the wellness habit tracker.</li>
  <li>Material Design — consistent theming and UI components.</li>
  <li>Source & hosting: GitHub.</li>
</ul>
