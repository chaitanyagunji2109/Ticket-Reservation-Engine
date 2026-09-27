package com.tcs.ticketengine.controller;

import com.tcs.ticketengine.entity.Seat;
import com.tcs.ticketengine.service.ReservationService;
import com.tcs.ticketengine.service.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ReservationController {

    private final ReservationService reservationService;
    private final AuthenticationService authenticationService;

    public ReservationController(ReservationService reservationService, AuthenticationService authenticationService) {
        this.reservationService = reservationService;
        this.authenticationService = authenticationService;
    }

    @GetMapping("/seats")
    public List<Seat> listSeats() {
        return reservationService.listSeats();
    }

    @GetMapping("/seats/{seatId}")
    public Seat getSeat(@PathVariable Long seatId) {
        return reservationService.getSeat(seatId);
    }

    @PostMapping("/seats")
    public ResponseEntity<Seat> createSeat(@RequestBody SeatRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.createSeat(request.eventName(), request.seatNumber()));
    }

    @PutMapping("/seats/{seatId}")
    public Seat updateSeat(@PathVariable Long seatId, @RequestBody SeatRequest request) {
        return reservationService.updateSeat(seatId, request.seatNumber());
    }

    @DeleteMapping("/seats/{seatId}")
    public ResponseEntity<Void> deleteSeat(@PathVariable Long seatId) {
        reservationService.deleteSeat(seatId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/hold")
    public Seat holdSeat(@RequestBody ReservationRequest request, @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        return reservationService.holdSeat(request.seatId(), authenticationService.requireUsername(authorizationHeader));
    }

    @PostMapping("/book")
    public Seat bookSeat(@RequestBody ReservationRequest request, @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        return reservationService.bookSeat(request.seatId(), authenticationService.requireUsername(authorizationHeader));
    }

    @DeleteMapping("/book/{seatId}")
    public Seat cancelBooking(@PathVariable Long seatId, @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        return reservationService.cancelBooking(seatId, authenticationService.requireUsername(authorizationHeader));
    }

    @PostMapping("/login")
    public AuthenticationService.LoginResult login(@RequestBody LoginRequest request) {
        return authenticationService.login(request.username(), request.password());
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationService.LoginResult> register(@RequestBody LoginRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authenticationService.register(request.username(), request.password()));
    }

    public static record ReservationRequest(Long seatId) {
    }

    public static record SeatRequest(String eventName, String seatNumber) {
        public SeatRequest(String seatNumber) {
            this("General Event", seatNumber);
        }
    }

    public static record LoginRequest(String username, String password) {
    }

    @RestControllerAdvice
    public static class ReservationExceptionHandler {
        @ExceptionHandler(ReservationService.SeatUnavailableException.class)
        public ResponseEntity<ErrorResponse> handleSeatUnavailable(ReservationService.SeatUnavailableException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(exception.getMessage()));
        }

        @ExceptionHandler(ReservationService.SeatAlreadyExistsException.class)
        public ResponseEntity<ErrorResponse> handleDuplicateSeat(ReservationService.SeatAlreadyExistsException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(exception.getMessage()));
        }

        @ExceptionHandler(ReservationService.SeatNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleMissingSeat(ReservationService.SeatNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(exception.getMessage()));
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponse> handleInvalidRequest(IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(new ErrorResponse(exception.getMessage()));
        }

        @ExceptionHandler(AuthenticationService.InvalidCredentialsException.class)
        public ResponseEntity<ErrorResponse> handleInvalidCredentials(AuthenticationService.InvalidCredentialsException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(exception.getMessage()));
        }

        @ExceptionHandler(AuthenticationService.UnauthorizedException.class)
        public ResponseEntity<ErrorResponse> handleUnauthorized(AuthenticationService.UnauthorizedException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(exception.getMessage()));
        }

        @ExceptionHandler(AuthenticationService.UserAlreadyExistsException.class)
        public ResponseEntity<ErrorResponse> handleExistingUser(AuthenticationService.UserAlreadyExistsException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(exception.getMessage()));
        }
    }

    public static record ErrorResponse(String message) {
    }
}
