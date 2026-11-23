IF DB_ID(N'webst2') IS NULL
BEGIN
    CREATE DATABASE [webst2];
END;
GO

USE [webst2];
GO

IF OBJECT_ID(N'dbo.categories', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.categories (
        CategoryId INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_categories PRIMARY KEY,
        CategoryName NVARCHAR(255) NOT NULL,
        Images NVARCHAR(500) NULL,
        status INT NOT NULL CONSTRAINT DF_categories_status DEFAULT (1)
    );
END;
GO

IF OBJECT_ID(N'dbo.Users', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Users (
        user_id INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Users PRIMARY KEY,
        email NVARCHAR(255) NOT NULL,
        username NVARCHAR(64) NOT NULL,
        full_name NVARCHAR(120) NOT NULL,
        password_hash NVARCHAR(255) NOT NULL,
        phone NVARCHAR(32) NULL,
        profile_image_url NVARCHAR(1000) NULL,
        cloudinary_public_id NVARCHAR(255) NULL,
        pending_email NVARCHAR(255) NULL,
        role_id INT NOT NULL CONSTRAINT DF_Users_Role DEFAULT (5),
        IsActive BIT NOT NULL CONSTRAINT DF_Users_IsActive DEFAULT (1),
        created_at DATETIME2(0) NOT NULL,
        CONSTRAINT UQ_Users_Email UNIQUE (email),
        CONSTRAINT UQ_Users_Username UNIQUE (username)
    );
END
ELSE IF COL_LENGTH(N'dbo.Users', N'IsActive') IS NULL
BEGIN
    ALTER TABLE dbo.Users ADD IsActive BIT NOT NULL
        CONSTRAINT DF_Users_IsActive DEFAULT (1) WITH VALUES;
END
GO

IF COL_LENGTH(N'dbo.Users', N'profile_image_url') IS NULL
    ALTER TABLE dbo.Users ADD profile_image_url NVARCHAR(1000) NULL;
IF COL_LENGTH(N'dbo.Users', N'cloudinary_public_id') IS NULL
    ALTER TABLE dbo.Users ADD cloudinary_public_id NVARCHAR(255) NULL;
IF COL_LENGTH(N'dbo.Users', N'pending_email') IS NULL
    ALTER TABLE dbo.Users ADD pending_email NVARCHAR(255) NULL;
GO

-- Convert a pre-existing non-Unicode name column so Vietnamese names remain intact.
IF EXISTS (
    SELECT 1
    FROM sys.columns AS c
    INNER JOIN sys.types AS t ON c.user_type_id = t.user_type_id
    WHERE c.object_id = OBJECT_ID(N'dbo.Users')
      AND c.name = N'full_name'
      AND t.name NOT IN (N'nvarchar', N'nchar')
)
BEGIN
    ALTER TABLE dbo.Users ALTER COLUMN full_name NVARCHAR(120) NOT NULL;
END;
GO

IF OBJECT_ID(N'dbo.RememberTokens', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.RememberTokens (
        token_hash NVARCHAR(64) NOT NULL CONSTRAINT PK_RememberTokens PRIMARY KEY,
        user_id INT NOT NULL,
        expires_at DATETIME2(0) NOT NULL,
        created_at DATETIME2(0) NOT NULL CONSTRAINT DF_RememberTokens_Created DEFAULT (SYSUTCDATETIME()),
        CONSTRAINT FK_RememberTokens_Users FOREIGN KEY (user_id)
            REFERENCES dbo.Users(user_id) ON DELETE CASCADE
    );
    CREATE INDEX IX_RememberTokens_UserId ON dbo.RememberTokens(user_id);
END;
GO

IF OBJECT_ID(N'dbo.Videos', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Videos (
        VideoId NVARCHAR(64) NOT NULL CONSTRAINT PK_Videos PRIMARY KEY,
        Active INT NOT NULL CONSTRAINT DF_Videos_Active DEFAULT (1),
        Description NVARCHAR(MAX) NULL,
        Poster NVARCHAR(500) NULL,
        Title NVARCHAR(500) NOT NULL,
        Views INT NOT NULL CONSTRAINT DF_Videos_Views DEFAULT (0),
        CategoryId INT NULL,
        UserId INT NULL,
        VideoFile NVARCHAR(255) NULL,
        CreatedAt DATETIME2(0) NOT NULL CONSTRAINT DF_Videos_CreatedAt DEFAULT (SYSUTCDATETIME()),
        CONSTRAINT FK_Videos_categories FOREIGN KEY (CategoryId)
            REFERENCES dbo.categories(CategoryId),
        CONSTRAINT FK_Videos_Users FOREIGN KEY (UserId)
            REFERENCES dbo.Users(user_id)
    );
END;
ELSE
BEGIN
    IF COL_LENGTH(N'dbo.Videos', N'UserId') IS NULL
        ALTER TABLE dbo.Videos ADD UserId INT NULL;
    IF COL_LENGTH(N'dbo.Videos', N'VideoFile') IS NULL
        ALTER TABLE dbo.Videos ADD VideoFile NVARCHAR(255) NULL;
    IF COL_LENGTH(N'dbo.Videos', N'CreatedAt') IS NULL
        ALTER TABLE dbo.Videos ADD CreatedAt DATETIME2(0) NOT NULL
            CONSTRAINT DF_Videos_CreatedAt DEFAULT (SYSUTCDATETIME()) WITH VALUES;
    IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_Videos_Users')
        ALTER TABLE dbo.Videos ADD CONSTRAINT FK_Videos_Users FOREIGN KEY (UserId)
            REFERENCES dbo.Users(user_id);
END;
GO

IF OBJECT_ID(N'dbo.OneTimeCodes', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.OneTimeCodes (
        OtpId BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_OneTimeCodes PRIMARY KEY,
        UserId INT NOT NULL,
        Purpose NVARCHAR(24) NOT NULL,
        TargetEmail NVARCHAR(255) NULL,
        CodeHash NVARCHAR(255) NOT NULL,
        CreatedAt DATETIME2(0) NOT NULL,
        ExpiresAt DATETIME2(0) NOT NULL,
        Attempts INT NOT NULL CONSTRAINT DF_OneTimeCodes_Attempts DEFAULT (0),
        Consumed BIT NOT NULL CONSTRAINT DF_OneTimeCodes_Consumed DEFAULT (0),
        CONSTRAINT FK_OneTimeCodes_Users FOREIGN KEY (UserId)
            REFERENCES dbo.Users(user_id) ON DELETE CASCADE
    );
    CREATE INDEX IX_OneTimeCodes_UserPurposeCreated
        ON dbo.OneTimeCodes(UserId, Purpose, CreatedAt DESC);
END;
ELSE IF COL_LENGTH(N'dbo.OneTimeCodes', N'TargetEmail') IS NULL
BEGIN
    ALTER TABLE dbo.OneTimeCodes ADD TargetEmail NVARCHAR(255) NULL;
END;
GO
