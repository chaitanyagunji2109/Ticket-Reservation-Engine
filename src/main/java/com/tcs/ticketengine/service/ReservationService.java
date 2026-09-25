package com.tcs.ticketengine.service;

import com.tcs.ticketengine.entity.Seat;
import com.tcs.ticketengine.entity.SeatStatus;
import com.tcs.ticketengine.repository.SeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReservationService {

    private final SeatRepository seatRepository;

    public ReservationService(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }

    @Transactional
    public Seat holdSeat(Long seatId, String userId) {
        Seat seat = findSeatForUpdate(seatId);
        if (seat.getStatus() != SeatStatus.AVAILABLE) {
            throw new SeatUnavailableException("Seat is not available");
        }
        seat.setStatus(SeatStatus.HELD);
        seat.setHeldBy(userId);
        return seat;
    }

    @Transactional
    public Seat bookSeat(Long seatId, String userId) {
        Seat seat = findSeatForUpdate(seatId);
        if (seat.getStatus() != SeatStatus.HELD || !userId.equals(seat.getHeldBy())) {
            throw new SeatUnavailableException("Seat is not held by this user");
        }
        seat.setStatus(SeatStatus.BOOKED);
        return seat;
    }

    @Transactional
    public Seat cancelBooking(Long seatId, String userId) {
        Seat seat = findSeatForUpdate(seatId);
        if (seat.getStatus() != SeatStatus.BOOKED || !userId.equals(seat.getHeldBy())) {
            throw new SeatUnavailableException("Only the user who booked this seat can cancel it");
        }
        seat.setStatus(SeatStatus.AVAILABLE);
        seat.setHeldBy(null);
        return seat;
    }

    @Transactional(readOnly = true)
    public List<Seat> listSeats() {
        return seatRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Seat getSeat(Long seatId) {
        return seatRepository.findById(seatId)
                .orElseThrow(() -> new SeatNotFoundException("Seat does not exist"));
    }

    @Transactional
    public Seat createSeat(String seatNumber) {
        String normalizedSeatNumber = normalizeSeatNumber(seatNumber);
        if (seatRepository.findAll().stream()
                .anyMatch(seat -> seat.getSeatNumber().equalsIgnoreCase(normalizedSeatNumber))) {
            throw new SeatAlreadyExistsException("Seat number already exists");
        }
        return seatRepository.save(new Seat(normalizedSeatNumber, SeatStatus.AVAILABLE));
    }

    @Transactional
    public Seat updateSeat(Long seatId, String seatNumber) {
        Seat seat = getSeat(seatId);
        String normalizedSeatNumber = normalizeSeatNumber(seatNumber);
        boolean alreadyUsed = seatRepository.findAll().stream()
                .anyMatch(existingSeat -> !existingSeat.getId().equals(seatId)
                        && existingSeat.getSeatNumber().equalsIgnoreCase(normalizedSeatNumber));
        if (alreadyUsed) {
            throw new SeatAlreadyExistsException("Seat number already exists");
        }
        seat.setSeatNumber(normalizedSeatNumber);
        return seat;
    }

    @Transactional
    public void deleteSeat(Long seatId) {
        seatRepository.delete(getSeat(seatId));
    }

    private Seat findSeatForUpdate(Long seatId) {
        return seatRepository.findByIdForUpdate(seatId)
                .orElseThrow(() -> new SeatUnavailableException("Seat does not exist"));
    }

    private String normalizeSeatNumber(String seatNumber) {
        if (seatNumber == null || seatNumber.isBlank()) {
            throw new IllegalArgumentException("Seat number is required");
        }
        return seatNumber.trim().toUpperCase();
    }

    public static class SeatUnavailableException extends RuntimeException {
        public SeatUnavailableException(String message) {
            super(message);
        }
    }

    public static class SeatNotFoundException extends RuntimeException {
        public SeatNotFoundException(String message) {
            super(message);
        }
    }

    public static class SeatAlreadyExistsException extends RuntimeException {
        public SeatAlreadyExistsException(String message) {
            super(message);
        }
    }
}
