-- ============================================================
-- GCPD Management System - SQL Server Schema
-- ============================================================

CREATE DATABASE GCPD_DB;
GO

USE GCPD_DB;
GO

-- ============================================================
-- USERS TABLE (Abstract base: Detective, Sergeant, Commissioner, etc.)
-- ============================================================
CREATE TABLE Users (
    userID      VARCHAR(50) PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    role        VARCHAR(50)  NOT NULL,  -- 'Detective','Sergeant','Commissioner','ForensicAnalyst','EvidenceCustodian','SystemAdmin'
    credential  VARCHAR(255) NOT NULL,  -- hashed password
    badgeNumber VARCHAR(50)  NULL,      -- for Detective
    squadID     VARCHAR(50)  NULL,      -- for Sergeant
    departmentID VARCHAR(50) NULL,      -- for Commissioner
    vaultID     VARCHAR(50)  NULL,      -- for EvidenceCustodian
    labID       VARCHAR(50)  NULL,      -- for ForensicAnalyst
    workloadScore INT DEFAULT 0         -- for Detective
);
GO

-- ============================================================
-- INCIDENTS TABLE
-- ============================================================
CREATE TABLE Incidents (
    incidentID  VARCHAR(50) PRIMARY KEY,
    date        DATETIME    NOT NULL DEFAULT GETDATE(),
    location    VARCHAR(255) NOT NULL,
    crimeType   VARCHAR(100) NOT NULL,
    description TEXT         NULL,
    status      VARCHAR(50)  NOT NULL DEFAULT 'Reported',  -- Reported, Escalated
    reportedBy  VARCHAR(50)  NOT NULL,  -- FK -> Users.userID (Detective)
    FOREIGN KEY (reportedBy) REFERENCES Users(userID)
);
GO

-- ============================================================
-- CASES TABLE
-- ============================================================
CREATE TABLE Cases (
    caseID             VARCHAR(50) PRIMARY KEY,
    status             VARCHAR(50)  NOT NULL DEFAULT 'Reported',
        -- Reported, UnderInvestigation, Escalated, Closed, ColdCase
    priority           VARCHAR(50)  NOT NULL DEFAULT 'Medium',
    startDate          DATETIME     NOT NULL DEFAULT GETDATE(),
    endDate            DATETIME     NULL,
    assignedDetective  VARCHAR(50)  NULL,   -- FK -> Users.userID
    incidentID         VARCHAR(50)  NULL,   -- FK -> Incidents.incidentID
    FOREIGN KEY (assignedDetective) REFERENCES Users(userID),
    FOREIGN KEY (incidentID) REFERENCES Incidents(incidentID)
);
GO

-- ============================================================
-- SUSPECTS TABLE
-- ============================================================
CREATE TABLE Suspects (
    suspectID       VARCHAR(50) PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    riskLevel       INT          NOT NULL DEFAULT 1,  -- 1-5
    criminalHistory TEXT         NULL,
    isRepeatOffender BIT         NOT NULL DEFAULT 0
);
GO

-- ============================================================
-- CASE_SUSPECTS (Many-to-Many: Case links to Suspect)
-- ============================================================
CREATE TABLE Case_Suspects (
    caseID    VARCHAR(50) NOT NULL,
    suspectID VARCHAR(50) NOT NULL,
    PRIMARY KEY (caseID, suspectID),
    FOREIGN KEY (caseID)    REFERENCES Cases(caseID),
    FOREIGN KEY (suspectID) REFERENCES Suspects(suspectID)
);
GO

-- ============================================================
-- EVIDENCE TABLE
-- ============================================================
CREATE TABLE Evidence (
    evidenceID      VARCHAR(50) PRIMARY KEY,
    type            VARCHAR(100) NOT NULL,
    status          VARCHAR(50)  NOT NULL DEFAULT 'Collected',
        -- Collected, InAnalysis, Transferred, Disposed
    storageLocation VARCHAR(255) NULL,
    collectedDate   DATETIME     NOT NULL DEFAULT GETDATE(),
    linkedCaseID    VARCHAR(50)  NOT NULL,
    FOREIGN KEY (linkedCaseID) REFERENCES Cases(caseID)
);
GO

-- ============================================================
-- CHAIN OF CUSTODY (Pure Fabrication from design)
-- ============================================================
CREATE TABLE ChainOfCustody (
    logID       VARCHAR(50) PRIMARY KEY,
    evidenceID  VARCHAR(50) NOT NULL,
    oldStatus   VARCHAR(50) NULL,
    newStatus   VARCHAR(50) NOT NULL,
    changedBy   VARCHAR(50) NOT NULL,   -- FK -> Users.userID
    timestamp   DATETIME    NOT NULL DEFAULT GETDATE(),
    notes       TEXT        NULL,
    FOREIGN KEY (evidenceID) REFERENCES Evidence(evidenceID),
    FOREIGN KEY (changedBy)  REFERENCES Users(userID)
);
GO

-- ============================================================
-- FORENSIC REPORTS TABLE
-- ============================================================
CREATE TABLE ForensicReports (
    reportID        VARCHAR(50) PRIMARY KEY,
    findings        TEXT        NOT NULL,
    verdict         VARCHAR(255) NULL,
    date            DATETIME    NOT NULL DEFAULT GETDATE(),
    analystID       VARCHAR(50) NOT NULL,   -- FK -> Users.userID (ForensicAnalyst)
    linkedEvidenceID VARCHAR(50) NULL,      -- FK -> Evidence.evidenceID
    linkedCaseID    VARCHAR(50) NOT NULL,   -- FK -> Cases.caseID
    FOREIGN KEY (analystID)        REFERENCES Users(userID),
    FOREIGN KEY (linkedEvidenceID) REFERENCES Evidence(evidenceID),
    FOREIGN KEY (linkedCaseID)     REFERENCES Cases(caseID)
);
GO

-- ============================================================
-- OPERATIONS TABLE (High-Risk Operation approval workflow)
-- ============================================================
CREATE TABLE Operations (
    operationID   VARCHAR(50) PRIMARY KEY,
    requestedBy   VARCHAR(50) NOT NULL,   -- FK -> Users.userID (Sergeant)
    status        VARCHAR(50) NOT NULL DEFAULT 'Pending',  -- Pending, Approved, Rejected
    riskLevel     VARCHAR(50) NOT NULL DEFAULT 'High',
    linkedCaseID  VARCHAR(50) NULL,
    timestamp     DATETIME    NOT NULL DEFAULT GETDATE(),
    approvedBy    VARCHAR(50) NULL,       -- FK -> Users.userID (Commissioner)
    rejectReason  TEXT        NULL,
    description   TEXT        NULL,
    FOREIGN KEY (requestedBy) REFERENCES Users(userID),
    FOREIGN KEY (approvedBy)  REFERENCES Users(userID),
    FOREIGN KEY (linkedCaseID) REFERENCES Cases(caseID)
);
GO

-- ============================================================
-- CRIME ANALYTICS TABLE (Pure Fabrication)
-- ============================================================
CREATE TABLE CrimeAnalytics (
    reportID        VARCHAR(50) PRIMARY KEY,
    crimeType       VARCHAR(100) NOT NULL,
    count           INT          NOT NULL DEFAULT 0,
    generatedDate   DATETIME     NOT NULL DEFAULT GETDATE(),
    commissionerID  VARCHAR(50)  NULL,
    FOREIGN KEY (commissionerID) REFERENCES Users(userID)
);
GO

-- ============================================================
-- SEED DATA - Default System Admin + sample users
-- ============================================================
INSERT INTO Users (userID, name, role, credential, badgeNumber, squadID, departmentID, vaultID, labID, workloadScore)
VALUES
('USR001', 'Bruce Wayne',     'SystemAdmin',       'admin123',    NULL,   NULL,   NULL,   NULL,  NULL, 0),
('USR002', 'James Gordon',    'Commissioner',      'gordon123',   NULL,   NULL,   'DEPT1',NULL,  NULL, 0),
('USR003', 'Harvey Bullock',  'Sergeant',          'bullock123',  NULL,   'SQ1',  NULL,   NULL,  NULL, 0),
('USR004', 'Renee Montoya',   'Detective',         'montoya123',  'DET01',NULL,   NULL,   NULL,  NULL, 0),
('USR005', 'Lucius Fox',      'ForensicAnalyst',   'lucius123',   NULL,   NULL,   NULL,   NULL,  'LAB1', 0),
('USR006', 'Alfred Pennyworth','EvidenceCustodian', 'alfred123',  NULL,   NULL,   NULL,   'VLT1',NULL, 0);
GO

PRINT 'GCPD Database schema created successfully.';
GO

BEGIN TRANSACTION;
BEGIN TRY
    DELETE FROM ChainOfCustody;
    DELETE FROM ForensicReports;
    DELETE FROM Evidence;
    DELETE FROM Operations;
    DELETE FROM Cases;
    DELETE FROM Incidents;
    DELETE FROM Suspects;
    UPDATE Users SET workloadScore = 0;
    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    ROLLBACK TRANSACTION;
END CATCH;


SELECT * FROM Users;
SELECT * FROM Suspects;
SELECT * FROM Incidents;