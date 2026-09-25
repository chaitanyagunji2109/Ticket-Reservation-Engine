package com.tcs.ticketengine;

import com.tcs.ticketengine.entity.Seat;
import com.tcs.ticketengine.entity.SeatStatus;
import com.tcs.ticketengine.entity.UserAccount;
import com.tcs.ticketengine.repository.SeatRepository;
import com.tcs.ticketengine.repository.UserAccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.stream.IntStream;

@SpringBootApplication
public class TicketEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketEngineApplication.class, args);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CommandLineRunner seedSeats(SeatRepository seatRepository, UserAccountRepository userAccountRepository,
                                PasswordEncoder passwordEncoder) {
        return args -> {
            if (seatRepository.count() == 0) {
                seatRepository.saveAll(IntStream.rangeClosed(1, 20)
                        .mapToObj(number -> new Seat("A" + number, SeatStatus.AVAILABLE))
                        .toList());
            }
            if (userAccountRepository.count() == 0) {
                userAccountRepository.save(new UserAccount("alice", passwordEncoder.encode("password123")));
                userAccountRepository.save(new UserAccount("bob", passwordEncoder.encode("password123")));
            }
        };
    }
}
