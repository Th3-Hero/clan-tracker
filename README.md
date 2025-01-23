# Member Activity Tracker (Server)

ClanTracker is a full-stack application built to simplify the management of clan member activity in World of Tanks. The app was developed to replace a slow and unwieldy spreadsheet system that, despite some automation, struggled to handle the growing demands of tracking large amounts of data. 

ClanTracker provides a faster, more reliable solution by offering a centralized platform for monitoring participation and performance. Clan leaders can use the app to track critical metrics like battle counts, ranks, and clan activity, while members can view their own contributions and engagement.

The application allows users to filter data by date range, sort tables by various metrics, and identify trends using performance-based color coding for quick assessment. By addressing the limitations of the previous system, ClanTracker ensures data is organized, accessible, and efficient. This makes it easy to evaluate member participation in key activities, such as random battles, skirmishes, advances, and clan wars.

## Tech Stack
### Backend:
- Spring Framework:
  - Spring Boot for application development.
  - Spring MVC for REST API development.
  - Spring Security for authentication and data security.
  - Jakarta Validation for input validation.
- Hibernate:
  - Flyway for database migrations.
- Quartz for scheduled tasks.
- PostgreSQL for efficient data storage and retrieval.
- Lombok for reducing boilerplate code.

### [Frontend](https://github.com/Th3-Hero/clan-tracker-ui): 
Svelte with TypeScript for a lightweight, responsive UI. Repository for the frontend can be found [here](https://github.com/Th3-Hero/clan-tracker-ui)

### Deployment: 
- Hosted on a local server with secure access enabled via Cloudflare Tunnels.
- Deployment is semi-automated using custom scripts.

### Integration 
The backend integrates with World of Tanks REST API to fetch real-time game data. REST APIs are documented with Swagger for seamless integration and debugging.

## Pre-requisites:
### Backend
- Java Development Kit (JDK) 21 found [here](https://adoptium.net/temurin/releases/?os=windows)
- Maven (build tool) found [here](https://maven.apache.org/download.cgi)
- PostgresSQL (database) found [here](https://www.postgresql.org/download/)