# **AGENTS.md \- System Specification: PDS Hierarchical Polling App**

## **1\. Project Overview & Architectural Intent**

You are operating as an autonomous Senior Principal Engineering agent developing an enterprise-grade, secure, hierarchical public polling mobile application for the Indian Public Distribution System (PDS). The objective is to eliminate Stage-II supply chain corruption.

**Crucial Constraint:** This is strictly a **Mobile Application**. There is no web portal. Both Administrative users (DMs, SDOs) and Citizens (NFSA beneficiaries) will use the same Android application, with interfaces dynamically rendered based on Role-Based Access Control (RBAC).

## **2\. Technology Stack & Firm Constraints**

Agents must strictly adhere to the following stack. Deviations are strictly prohibited.

* **Frontend (Mobile App)**: Kotlin (Native Android).  
  * *UI Framework*: Jetpack Compose.  
  * *Networking*: Retrofit2 \+ OkHttp.  
  * *Asynchrony*: Kotlin Coroutines & Flow.  
  * *Location Services*: Google Play Services FusedLocationProviderClient.  
  * *Push Notifications*: Firebase Cloud Messaging (FCM).  
* **Backend Core**: Go (Golang 1.21+).  
  * *Framework*: Gin Web Framework (for routing and middleware).  
  * *Database Driver*: pgx (for direct Postgres connections and PostGIS support).  
* **Database & Auth Layer**: Supabase.  
  * *Database*: PostgreSQL 15+ hosted on Supabase.  
  * *Geospatial*: PostGIS extension MUST be enabled via Supabase migrations.  
  * *Authentication*: Supabase Auth (JWTs). The Go backend will act as a resource server validating Supabase JWTs.

## **3\. Database Schema Definitions (Supabase PostgreSQL)**

Agents must generate Supabase migration files (.sql) for the following schema. All spatial calculations must utilize PostGIS.

### **Table: lgd\_hierarchy (Denormalized for Mobile Read-Efficiency)**

* id (Primary Key, UUID)  
* state\_code, state\_name (VARCHAR)  
* district\_code, district\_name (VARCHAR)  
* block\_code, block\_name (VARCHAR)  
* panchayat\_code, panchayat\_name (VARCHAR)

### **Table: fair\_price\_shops**

* fps\_id (Primary Key, VARCHAR(20))  
* panchayat\_code (Foreign Key)  
* fps\_name, dealer\_name (VARCHAR)  
* location (GEOMETRY(Point, 4326)) \- PostGIS spatial column (Latitude/Longitude).

### **Table: users (Extends Supabase auth.users)**

* id (Primary Key, UUID, Foreign Key \-\> auth.users.id)  
* role (ENUM: 'ADMIN\_STATE', 'ADMIN\_DISTRICT', 'ADMIN\_BLOCK', 'CITIZEN')  
* ration\_card\_no (VARCHAR(12), Nullable for Admins)  
* fps\_id (Foreign Key \-\> fair\_price\_shops.fps\_id, Nullable for Admins)  
* device\_fcm\_token (VARCHAR) \- For push notifications.  
* hardware\_uuid (VARCHAR) \- For device binding.

### **Table: active\_polls**

* poll\_id (Primary Key, UUID)  
* created\_by (Foreign Key \-\> users.id)  
* target\_level (ENUM: 'STATE', 'DISTRICT', 'BLOCK', 'PANCHAYAT', 'FPS')  
* target\_code (VARCHAR) \- E.g., a specific block code.  
* commodity (VARCHAR) \- E.g., 'WHEAT', 'RICE'.  
* is\_active (BOOLEAN, Default TRUE)

### **Table: poll\_responses**

* response\_id (Primary Key, UUID)  
* poll\_id (Foreign Key \-\> active\_polls.poll\_id)  
* user\_id (Foreign Key \-\> users.id)  
* received\_ration (BOOLEAN)  
* distance\_from\_shop\_meters (FLOAT)  
* submitted\_at (TIMESTAMPTZ, Default NOW())

## **4\. Agent Orchestration: Core Workflows**

### **Agent Task 1: Go Backend \- Poll Generation & FCM Push**

* **Endpoint**: POST /api/v1/polls (Authenticated via Supabase JWT middleware).  
* **Logic**: When an Admin creates a poll targeting a specific LGD code (e.g., target\_level \= 'BLOCK', target\_code \= 'B123'), the Go backend must:  
  1. Insert the poll into active\_polls.  
  2. Query the users table, joining with fair\_price\_shops and lgd\_hierarchy, to extract device\_fcm\_token for all 'CITIZEN' users residing under that block.  
  3. Trigger an asynchronous Go routine (go sendPushNotifications(...)) to send push alerts via FCM: "Stock dispatched to your FPS. Open app to verify."

### **Agent Task 2: Kotlin Android \- Anti-Fraud & Geofencing Client**

* **UI**: Build a Jetpack Compose screen showing active polls for the citizen.  
* **Hardware Checks**: Before allowing a vote, Kotlin must verify the device UUID matches the registered UUID.  
* **Location Spoofing Check**: Use location.isFromMockProvider (Android 12+) or location.isMock (older Androids). If true, abort immediately and show a strict error dialog.  
* **Payload**: App sends { "poll\_id": "uuid", "lat": 28.123, "lng": 77.123, "received": true } to the Go backend.

### **Agent Task 3: Go Backend \- PostGIS Validation**

* **Endpoint**: POST /api/v1/polls/submit  
* **Spatial Query**: The Go backend MUST execute a PostGIS query using ST\_DistanceSphere comparing the incoming payload coordinates against the fair\_price\_shops.location associated with the user.  
* **Constraint Rule**:  
  SELECT ST\_DistanceSphere(ST\_MakePoint($1, $2), location) as distance  
  FROM fair\_price\_shops WHERE fps\_id \= $3

* If distance \> 100, return HTTP 403 Forbidden with the payload: {"error": "GEO\_FENCE\_VIOLATION", "message": "You must be physically present at the Fair Price Shop."}.

## **5\. Security & Supabase Policies**

* **Go Middleware**: Implement a custom Gin middleware that verifies the Supabase JWT signature using the Supabase project JWT secret. Extract the user\_id and role from the token claims and inject them into the Gin Context.  
* **Rate Limiting**: Use Go's golang.org/x/time/rate package to limit poll submissions to 1 request per user per minute to prevent brute-force attacks.

## **6\. Development & Artifact Instructions**

1. **Phase 1 Artifact**: Generate the supabase/migrations/0001\_initial\_schema.sql containing all table definitions and PostGIS indexes.  
2. **Phase 2 Artifact**: Generate the Go backend boilerplate (main.go, routes.go, handlers/poll.go) utilizing Gin and pgx.  
3. **Phase 3 Artifact**: Generate the Kotlin Jetpack Compose UI architecture (MainActivity.kt, PollViewModel.kt, LocationHelper.kt).  
4. **Testing**: Generate \_test.go files for the backend ensuring the 100-meter PostGIS logic is rigorously tested with mock coordinates.