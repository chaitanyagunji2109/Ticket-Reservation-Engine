package com.tcs.ticketengine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.tcs.ticketengine.entity.Seat;
import com.tcs.ticketengine.entity.SeatStatus;
import com.tcs.ticketengine.repository.SeatRepository;
import com.tcs.ticketengine.service.ReservationService;
import com.tcs.ticketengine.service.ReservationService.SeatUnavailableException;

@SpringBootTest
class ConcurrencyTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private SeatRepository seatRepository;

    @Test
    void onlyOneUserCanHoldTheSameSeat() throws Exception {
        int users = 50;
        Long seatId = reservationService.listSeats().get(0).getId();
        ExecutorService executor = Executors.newFixedThreadPool(users);
        CountDownLatch startGate = new CountDownLatch(1);
        AtomicInteger successes = new AtomicInteger();
        AtomicInteger failures = new AtomicInteger();
        List<String> winners = new ArrayList<>();
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < users; i++) {
            int user = i;
            futures.add(executor.submit(() -> {
                startGate.await();
                try {
                    reservationService.holdSeat(seatId, "user-" + user);
                    successes.incrementAndGet();
                    synchronized (winners) { winners.add("user-" + user); }
                } catch (SeatUnavailableException exception) {
                    failures.incrementAndGet();
                }
                return null;
            }));
        }
        startGate.countDown();
        for (Future<?> future : futures) future.get();
        executor.shutdown();

        Seat finalSeat = reservationService.listSeats().stream()
                .filter(seat -> seat.getId().equals(seatId))
                .findFirst().orElseThrow();
        System.out.printf("winner=%s, successes=%d, failures=%d%n", winners.get(0), successes.get(), failures.get());
        assertEquals(1, successes.get());
        assertEquals(49, failures.get());
        assertEquals(SeatStatus.HELD, finalSeat.getStatus());
    }

    @Test
    void usersCanHoldDifferentSeatsConcurrently() throws Exception {
        int users = 50;
        List<Seat> testSeats = seatRepository.saveAll(
                java.util.stream.IntStream.rangeClosed(1, users)
                        .mapToObj(number -> new Seat("B" + number, SeatStatus.AVAILABLE))
                        .toList());
        List<Long> seatIds = testSeats.stream().map(Seat::getId).toList();
        ExecutorService executor = Executors.newFixedThreadPool(users);
        CountDownLatch startGate = new CountDownLatch(1);
        AtomicInteger successes = new AtomicInteger();
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < users; i++) {
            int user = i;
            futures.add(executor.submit(() -> {
                startGate.await();
                reservationService.holdSeat(seatIds.get(user), "different-user-" + user);
                successes.incrementAndGet();
                return null;
            }));
        }
        startGate.countDown();
        for (Future<?> future : futures) future.get();
        executor.shutdown();

        assertEquals(50, successes.get());
    }

    @Test
    void duplicateSeatNumbersAreRejectedAcrossConcurrentCreates() throws Exception {
        int users = 2;
        ExecutorService executor = Executors.newFixedThreadPool(users);
        CountDownLatch startGate = new CountDownLatch(1);
        AtomicInteger successes = new AtomicInteger();
        AtomicInteger failures = new AtomicInteger();
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < users; i++) {
            futures.add(executor.submit(() -> {
                startGate.await();
                try {
                    reservationService.createSeat("C-99");
                    successes.incrementAndGet();
                } catch (ReservationService.SeatAlreadyExistsException exception) {
                    failures.incrementAndGet();
                }
                return null;
            }));
        }

        startGate.countDown();
        for (Future<?> future : futures) future.get();
        executor.shutdown();

        assertEquals(1, successes.get());
        assertEquals(1, failures.get());
    }
}
