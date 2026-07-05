# ERASM — Simple Steps Guide (Spring Tool Suite / Eclipse)

This guide assumes zero prior Eclipse/STS experience with this project. Follow
it top to bottom.

## Step 1: Extract the zip

Unzip `ERASM.zip` somewhere on your computer, e.g. `C:\Projects\ERASM` or
`~/Projects/ERASM`. Do **not** unzip it inside your STS workspace folder
directly — you'll import it in Step 2 instead.

## Step 2: Import into Spring Tool Suite

1. Open STS.
2. `File → Import...`
3. Choose `Maven → Existing Maven Projects` → `Next`.
4. Click `Browse...` and select the extracted `ERASM` folder (the one
   containing `pom.xml`).
5. STS will detect the `pom.xml` and tick it automatically. Click `Finish`.
6. Wait for the bottom-right progress bar to finish — STS is downloading all
   the Maven dependencies (Spring Boot, Spring Security, JWT, MySQL driver,
   etc.). This can take a few minutes on first import.
7. If you see red error markers immediately after import, right-click the
   project → `Maven → Update Project...` → tick `Force Update of Snapshots/Releases` → `OK`.

## Step 3: Set up MySQL

You have two options:

**Option A — Let the app create the database automatically (recommended)**
Nothing to do. `application.properties` already has
`createDatabaseIfNotExist=true`, so the first time you run the app it will
create the `erasm_db` schema for you (the tables are then created by
Hibernate automatically).

**Option B — Run the SQL script yourself**
1. Open MySQL Workbench (or any MySQL client).
2. Open `database/erasm_database_script.sql` from the extracted project.
3. Run the whole script. It creates the database, all 10 tables, and seeds
   the 5 roles + sample skills.

Either way, make sure your local MySQL server is running before you start the
Spring Boot app.

## Step 4: Point the app at your MySQL credentials

1. In the STS Project Explorer, open
   `src/main/resources/application.properties`.
2. Update these two lines if your MySQL username/password are different from
   the defaults:
   ```properties
   spring.datasource.username=root
   spring.datasource.password=root
   ```
3. Save the file (`Ctrl+S`).

## Step 5: Run the application

1. In Project Explorer, expand `src/main/java → com.erasm`.
2. Right-click `ErasmApplication.java` → `Run As → Java Application`.
3. Watch the Console view. You should see Spring Boot's banner, then Hibernate
   creating tables, then log lines like:
   ```
   Seeded role: ADMIN
   Seeded role: DELIVERY_MANAGER
   ...
   Seeded default admin user: admin@erasm.com / Admin@123
   Tomcat started on port 8080
   ```
4. Leave this running — this is your live API server.

## Step 6: Test the APIs in Postman

1. Open Postman.
2. `File → Import...` → select
   `postman/ERASM_Postman_Collection.json` from the extracted project.
3. In the collection's **01 - Authentication** folder, run
   **Login - Default Admin** first.
   - Body already contains `admin@erasm.com` / `Admin@123`.
   - After you click **Send**, the response contains a JWT `token` — a small
     script in this request automatically saves it into the collection
     variable `adminToken`, so every other request in the collection is
     already pre-authorized with `Authorization: Bearer {{adminToken}}`.
4. Now you can run any other folder in order: Skill Management → Employee &
   Skill Profile → Project Management → Resource Request & Approval Workflow
   → Resource Allocation → Utilization Dashboard → Audit Management → Reports.
5. A few requests (like **Register - Delivery Manager**) automatically save
   their own token into a separate collection variable
   (`deliveryManagerToken`, `resourceManagerToken`, etc.) so that later
   requests can use the right role for `@PreAuthorize` checks.
6. If you get a `403 Forbidden`, it usually means you're using the wrong
   role's token for that endpoint — check the comment/label on the request.

## Step 7: Explore with Swagger (optional)

With the app running, open a browser to:
```
http://localhost:8080/swagger-ui.html
```
Click **Authorize**, paste in a JWT token (just the token string, Postman's
"Login" response gives you this), and you can try any endpoint directly from
the browser too.

## Step 8: Run the JUnit tests

1. Right-click the `erasm` project → `Run As → Maven test`.
2. Or, from a terminal in the project folder: `mvn test`
3. This generates the JUnit XML test reports under `target/surefire-reports`
   — this is your "Test Report / JUnit Test Execution Report" deliverable.
   You can attach these files (or a screenshot of the console showing
   `BUILD SUCCESS` / tests passed) to your submission document.

## Step 9: Push to GitHub (for the deliverable)

If you haven't set up Git for this project yet:

```bash
cd path/to/ERASM
git init
git add .
git commit -m "feat: initial ERASM project setup"
git branch -M main
git remote add origin https://github.com/<your-username>/erasm.git
git push -u origin main
```

Recommended follow-up branches per the doc's Git requirements:
```bash
git checkout -b develop
git checkout -b feature/resource-allocation
```

## Troubleshooting

| Symptom | Likely cause | Fix |
|---|---|---|
| `Port 8080 was already in use` | Another app (maybe a previous run) is on 8080 | Stop the other process, or change `server.port` in `application.properties` |
| `Communications link failure` | MySQL isn't running, or wrong host/port | Start MySQL service; check `spring.datasource.url` |
| `Access denied for user 'root'@'localhost'` | Wrong password in `application.properties` | Update `spring.datasource.password` |
| `401 Unauthorized` in Postman | Missing/expired JWT | Re-run the Login request to get a fresh token |
| `403 Forbidden` in Postman | Right token, wrong role for that endpoint | Use the token variable matching the role required (see the request name/comment) |
| pom.xml shows a red X in Eclipse after import | Local Maven repo cache issue | Right-click project → Maven → Update Project → Force Update |
