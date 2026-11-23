# Web Homework Day 4 - SiteMesh, Validation, and User Profiles

The project continues the Day 3 homework. It includes account activation by email OTP, hashed-password login, logout, forgot-password OTP, Video CRUD, multipart MP4 upload, public video playback, pagination, and the Day 4 profile features.

## Day 4 requirements

- SiteMesh 3.2.1 applies one shared Bootstrap 5.3.9 decorator to JSP pages; the shared template provides responsive navigation and a footer.
- Bootstrap client-side validation with inline feedback and server-side validation for login, registration, forgot password, Category insert/edit, and Video insert/edit.
- Signed-in users can update their name and phone, upload a profile image to Cloudinary, and change email only after confirming an OTP sent to the new address.

## Technology

- Java 17, Maven, Jakarta Servlet 6.0 / JSP 3.1, Tomcat 10.1.
- Hibernate ORM with JPA and Microsoft SQL Server.
- Jakarta Mail (Eclipse Angus) for SMTP OTP delivery.
- Cloudinary Java SDK for signed server-side profile image uploads.
- Servlet `@MultipartConfig` for MP4 upload, following the supplied Servlet multipart guide.

## SQL Server setup

1. Open `database/schema.sql` in SSMS and execute it against the SQL Server instance. It creates `webst2` when missing and adds the Day 3 columns and OTP table to the earlier schema without dropping existing records.
2. In VS Code, open a PowerShell terminal at the project root and set your local values. Use your Tomcat path and SQL Server `sa` password:

```powershell
$tomcatHome = 'D:\apache-tomcat-10.1.61\apache-tomcat-10.1.61'
$env:CATALINA_HOME = $tomcatHome
$env:DB_URL = 'jdbc:sqlserver://localhost:1433;databaseName=webst2;encrypt=true;trustServerCertificate=true'
$env:DB_USER = 'sa'
$secureDbPassword = Read-Host 'SQL Server password for sa' -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new('', $secureDbPassword).Password
$env:APP_UPLOAD_DIR = 'D:\WEB\Homework_day4\uploads'
```

## Configure email OTP

The application sends activation and password-reset codes through SMTP username/password authentication. For Gmail, an App Password requires 2-Step Verification and is not available on every account; if it is unavailable, use your school's or another SMTP provider. Do not put any real email password in source control. Google documents its current account requirements [here](https://support.google.com/mail/answer/185833?hl=en). Gmail's SMTP server supports STARTTLS on port 587 [according to Google](https://support.google.com/mail/answer/7104828?hl=en). Set these values in the same PowerShell terminal before starting Tomcat:

```powershell
$env:MAIL_HOST = 'smtp.gmail.com'
$env:MAIL_PORT = '587'
$env:MAIL_USERNAME = 'your-sending-address@gmail.com'
$secureMailPassword = Read-Host 'SMTP app password' -AsSecureString
$env:MAIL_PASSWORD = [System.Net.NetworkCredential]::new('', $secureMailPassword).Password
$env:MAIL_FROM = $env:MAIL_USERNAME
```

## Cloudinary profile images

Create a Cloudinary account and copy the cloud name, API key, and API secret from its Dashboard. Set them in the same PowerShell terminal used to start Tomcat; do not commit these values:

```powershell
$env:CLOUDINARY_CLOUD_NAME = 'your-cloud-name'
$env:CLOUDINARY_API_KEY = 'your-api-key'
$secureCloudinarySecret = Read-Host 'Cloudinary API secret' -AsSecureString
$env:CLOUDINARY_API_SECRET = [System.Net.NetworkCredential]::new('', $secureCloudinarySecret).Password
```

The profile page is at `/profile`. The uploaded image is stored in the Cloudinary folder `web-homework-day4/profile-images`; only its secure URL and public ID are saved in the user row. Replacing a profile image removes the previous Cloudinary asset after the database update succeeds.

OTP codes expire after 10 minutes, are stored only as password hashes, can be used once, and allow at most five attempts. The application allows one new code per minute and five requests per hour for each email and purpose. These environment values apply only to that terminal session; keep the secrets out of Git.

## Build and run on Tomcat

From the project root in that same terminal:

```powershell
# The C: drive on this machine is full; keep Maven's cache and temp files on D:.
$buildTemp = 'D:\Temp\Homework_day4-maven-tmp'
New-Item -ItemType Directory -Path $buildTemp -Force | Out-Null
$env:TEMP = $buildTemp
$env:TMP = $buildTemp
$env:MAVEN_OPTS = "-Djava.io.tmpdir=$buildTemp"
mvn "-Dmaven.repo.local=D:\Temp\Homework_day4-m2repo\repository" -U -DskipTests clean package
Copy-Item .\target\web-homework-day4.war (Join-Path $tomcatHome 'webapps\web-homework-day4.war') -Force
& (Join-Path $tomcatHome 'bin\catalina.bat') run
```

Open [http://localhost:8080/web-homework-day4/](http://localhost:8080/web-homework-day4/). Stop Tomcat with **Ctrl+C** in the terminal. If Tomcat is running as a Windows service, stop it before replacing the WAR.

## Features and routes

- `/register`, `/activation`: register an account and activate it using the email OTP.
- `/login`, `/logout`: password-hash verification, session login, optional remember-me cookie, and logout.
- `/profile`: update full name, phone, Cloudinary profile image, and email after OTP confirmation.
- `/forgot-password`, `/reset-password`: request an email code and set a new password.
- `/home`: newest 10 public videos.
- `/video`: public videos, six per page. Add `?userId=ID` to filter to one user's videos.
- `/video/detail?id=VIDEO_ID`: public detail and MP4 playback; viewers do not need an account.
- `/video/manage`, `/video/add`, `/video/edit?id=VIDEO_ID`, `/video/delete`: signed-in users manage only their own uploads.
- `/admin/category/list`: Category CRUD from Day 2.

Video files are stored under `APP_UPLOAD_DIR\videos`, not inside the WAR or Git repository. Upload accepts MP4 files up to 500 MB. The Day 3 database migration adds a nullable `UserId` foreign key to old videos so earlier rows remain intact; videos created through the Day 4 form are linked to the signed-in user.
