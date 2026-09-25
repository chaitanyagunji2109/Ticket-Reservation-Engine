package com.tcs.ticketengine.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Entity
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status = SeatStatus.AVAILABLE;

    private String heldBy;

    @Version
    private Long version;

    protected Seat() {
    }

    public Seat(String seatNumber, SeatStatus status) {
        this.seatNumber = seatNumber;
        this.status = status;
    }

    public Long getId() { return id; }
    public String getSeatNumber() { return seatNumber; }
    public SeatStatus getStatus() { return status; }
    public String getHeldBy() { return heldBy; }
    public Long getVersion() { return version; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
    public void setStatus(SeatStatus status) { this.status = status; }
    public void setHeldBy(String heldBy) { this.heldBy = heldBy; }
}
