# Ticket Reservation Engine

A Spring Boot ticket-booking demo. Users can choose tickets for four seeded events, hold an available ticket, confirm the booking, or cancel their own booking. Ticket updates use database locking to prevent two users from reserving the same ticket.

## Requirements

- Java 17 or later
- Maven

## Run

From the project directory:

```bash
mvn spring-boot:run
```

Open [http://localhost:8080](http://localhost:8080). To run the tests:

```bash
mvn test
```

## Demo Accounts

On first startup, the application creates these accounts:

| Username | Password |
| --- | --- |
| `alice` | `password123` |
| `bob` | `password123` |

New users can register from the web interface. The demo passwords are for local development only; change them before deploying the application.

## Booking Tickets

Choose an event to view its tickets. Log in or register, select an available ticket, hold it, then confirm the booking. A user can cancel their own confirmed booking.

The default inventory has 20 tickets for each of four events: Movie Night, Downtown Express, Summer Concert, and City Final.

## Data and API

The application uses a local H2 database at `./ticketdb`; user accounts and ticket statuses persist across restarts. The path is relative to the directory where the application is started.

The REST API is under `/api` for seats, login, registration, holds, bookings, and cancellations. OpenAPI documentation is available at `/swagger-ui/index.html` while the application is running.
