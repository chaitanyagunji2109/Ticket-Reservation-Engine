package com.tcs.ticketengine.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"event_name", "seat_number"}))
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seat_number", nullable = false)
    private String seatNumber;

    @Column(name = "event_name", nullable = false)
    private String eventName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status = SeatStatus.AVAILABLE;

    private String heldBy;

    @Version
    private Long version;

    protected Seat() {
    }

    public Seat(String seatNumber, SeatStatus status) {
        this("General Event", seatNumber, status);
    }

    public Seat(String eventName, String seatNumber, SeatStatus status) {
        this.eventName = eventName;
        this.seatNumber = seatNumber;
        this.status = status;
    }

    public Long getId() { return id; }
    public String getEventName() { return eventName; }
    public String getSeatNumber() { return seatNumber; }
    public SeatStatus getStatus() { return status; }
    public String getHeldBy() { return heldBy; }
    public Long getVersion() { return version; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
    public void setStatus(SeatStatus status) { this.status = status; }
    public void setHeldBy(String heldBy) { this.heldBy = heldBy; }
}
