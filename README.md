# GCPD Management System
**Gotham City Municipal Police Department Management System**

Submitted by: Shaheer Sohail (24I-3060) & Abubakr Ahmed (24I-3016)
Course: SE2004 – Software Design and Architecture
Instructor: Ms. Laiba Imran | Spring 2026

---

## Project Overview

The GC-MPDMS is a Java-based centralized management system for the Gotham City Police Department. It replaces fragmented manual reporting with a structured, role-based digital system that manages cases, incidents, suspects, evidence, and forensic operations across 6 departmental roles.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| UI | JavaFX 21 |
| Business Logic | Java 21 |
| Database | Microsoft SQL Server (via SSMS) |
| Build Tool | Maven |
| JDBC Driver | mssql-jdbc 12.6.1 |

---

## Prerequisites

Before running the project make sure you have:

- Java JDK 21 installed
- IntelliJ IDEA (recommended)
- Microsoft SQL Server installed and running
- SQL Server Management Studio (SSMS)
- Maven (bundled with IntelliJ)

---

## Setup Instructions

### Step 1 — Set up the Database

1. Open **SQL Server Management Studio (SSMS)**
2. Connect to your SQL Server instance
3. Open the file `gcpd_schema.sql` from the project root
4. Execute the entire script (press **F5** or click **Execute**)
5. This will create the `GCPD_DB` database with all tables and seed data

### Step 2 — Configure the DB Connection

Open the file:
```
src/main/java/com/gcpd/db/DatabaseConnection.java
```

Update these fields to match your SQL Server setup:

```java
private static final String SERVER   = "localhost";      // your SQL Server host
private static final String DATABASE = "GCPD_DB";        // leave as is
private static final String USERNAME = "your_username";  // your SQL Server login
private static final String PASSWORD = "your_password";  // your SQL Server password
```

If you use **Windows Authentication** instead of a username/password, change the URL to:
```java
private static final String URL =
    "jdbc:sqlserver://" + SERVER + ":1433;"
    + "databaseName=" + DATABASE + ";"
    + "integratedSecurity=true;"
    + "encrypt=false;"
    + "trustServerCertificate=true;";
```
And change `getConnection()` to use:
```java
connection = DriverManager.getConnection(URL);
```

### Step 3 — Open in IntelliJ

1. Open IntelliJ IDEA
2. Click **File → Open** and select the project folder (the one containing `pom.xml`)
3. IntelliJ will detect it as a Maven project — click **Load Maven Project** if prompted
4. Wait for dependencies to download (JavaFX + JDBC driver)

### Step 4 — Run the Application

1. Navigate to `src/main/java/com/gcpd/ui/MainApp.java`
2. Right-click → **Run 'MainApp'**
3. The login screen will appear

---

## Demo Login Credentials

| User ID | Password | Role |
|---------|----------|------|
| USR001 | admin123 | System Admin |
| USR002 | gordon123 | Commissioner |
| USR003 | bullock123 | Sergeant |
| USR004 | montoya123 | Detective |
| USR005 | lucius123 | Forensic Analyst |
| USR006 | alfred123 | Evidence Custodian |

---

## Project Structure

```
GCPD-MS/
│   pom.xml
│   gcpd_schema.sql
│   README.md
│
└───src/main/java/com/gcpd/
    ├───ui/
    │   ├───MainApp.java
    │   └───screens/
    │       ├───LoginScreen.java
    │       ├───BaseScreen.java
    │       ├───DetectiveDashboard.java
    │       ├───SergeantDashboard.java
    │       ├───CommissionerDashboard.java
    │       ├───ForensicAnalystDashboard.java
    │       ├───EvidenceCustodianDashboard.java
    │       └───AdminDashboard.java
    │
    ├───bl/
    │   ├───model/
    │   │   ├───User.java (abstract)
    │   │   ├───Detective.java
    │   │   ├───Sergeant.java
    │   │   ├───Commissioner.java
    │   │   ├───ForensicAnalyst.java
    │   │   ├───EvidenceCustodian.java
    │   │   ├───SystemAdmin.java
    │   │   ├───Case.java
    │   │   ├───Incident.java
    │   │   ├───Evidence.java
    │   │   ├───Suspect.java
    │   │   ├───ForensicReport.java
    │   │   ├───Operation.java
    │   │   ├───ChainOfCustody.java
    │   │   ├───CrimeAnalytics.java
    │   │   ├───SessionManager.java
    │   │   └───CaseObserver.java
    │   ├───factory/
    │   │   └───UserFactory.java
    │   └───strategy/
    │       ├───NotificationStrategy.java
    │       └───IntranetNotification.java
    │
    └───db/
        ├───DatabaseConnection.java
        ├───UserDAO.java
        ├───IncidentDAO.java
        ├───CaseDAO.java
        ├───EvidenceDAO.java
        ├───SuspectDAO.java
        ├───OperationDAO.java
        └───ForensicReportDAO.java
```

---

## Implemented Use Cases

| UC | Name | Role | Layer |
|----|------|------|-------|
| UC-01 | Report New Incident | Detective | Detective Dashboard |
| UC-02 | Assign Detective to Case | Sergeant | Sergeant Dashboard |
| UC-03 | Log Forensic Evidence | Forensic Analyst | Forensic Dashboard |
| UC-04 | Approve High-Risk Operation | Sergeant + Commissioner | Both Dashboards |
| UC-05 | Identify Repeat Offender | Detective | Detective Dashboard |
| UC-06 | Manage Evidence Lifecycle | Evidence Custodian | Custodian Dashboard |
| UC-07 | Register System User | System Admin | Admin Dashboard |
| UC-08 | Authenticate User Login | All Users | Login Screen |
| UC-09 | Escalate Incident to Case | Detective | Detective Dashboard |
| UC-10 | Update Case Status | Detective | Detective Dashboard |
| UC-11 | Authorize Evidence Transfer | Evidence Custodian | Custodian Dashboard |
| UC-12 | View Crime Analytics Dashboard | Commissioner | Commissioner Dashboard |

---

## Architectural Style

This system follows a **3-Layer (N-Tier) Layered Architecture**:

- `com.gcpd.ui` — Presentation Layer (JavaFX)
- `com.gcpd.bl` — Business Logic Layer (OOP + Design Patterns)
- `com.gcpd.db` — Data Access Layer (DAOs + SQL Server)

Each layer only communicates downward. The UI calls the BL, the BL calls the DB.

---

## Design Patterns Applied

**GRASP Patterns:**
- Information Expert, Creator, Controller, Low Coupling, High Cohesion, Pure Fabrication, Indirection, Protected Variation

**GoF Patterns:**
- Singleton — `SessionManager`
- Factory Method — `UserFactory`
- Observer — `CaseObserver` interface
- Strategy — `NotificationStrategy` interface
