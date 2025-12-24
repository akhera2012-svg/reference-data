Reference Data (Maven Spring Boot)

This project imports a CSV of securities and persists records into an in-memory H2 database.

Quick run (Windows PowerShell):

1. Build:

```powershell
mvn clean package -DskipTests
```

2. Run (uses bundled `sample-securities.csv` by default):

```powershell
java -jar target\reference-data-0.0.1-SNAPSHOT.jar
```

3. To import a file from disk, pass its path as the first argument:

```powershell
java -jar target\reference-data-0.0.1-SNAPSHOT.jar C:\path\to\your\file.csv
```

H2 console: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:mrddb`)
