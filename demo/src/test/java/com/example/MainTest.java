package com.example;

import com.example.model.Cinema;
import com.example.model.Session;
import com.example.model.Ticket;
import com.example.service.CinemaService;
import com.example.service.ExportImportService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

public class MainTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;

    private CinemaService cinemaService;
    private Cinema cinema;
    
    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
        
        cinema = new Cinema("Test Cinema", "Test Address", 3);
        cinemaService = new CinemaService(cinema);
        
        Session futureSession1 = new Session("Test Movie 1", LocalDateTime.now().plusDays(1), 100, 120.0);
        Session futureSession2 = new Session("Test Movie 2", LocalDateTime.now().plusDays(2), 150, 150.0);
        Session expiredSession = new Session("Expired Movie", LocalDateTime.now().minusDays(1), 100, 100.0);
        
        cinema.addSession(futureSession1);
        cinema.addSession(futureSession2);
        cinema.addSession(expiredSession);
    }
    
    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
        System.setIn(originalIn);
    }
    
    @Test
    void testGetValidSessions() {
        List<Session> validSessions = cinemaService.getValidSessions();
        
        assertEquals(2, validSessions.size());
        
        for (Session session : validSessions) {
            assertTrue(session.isValid(), "Session should be valid: " + session.getMovieTitle());
        }
        
        boolean containsExpiredMovie = validSessions.stream()
                .anyMatch(session -> session.getMovieTitle().equals("Expired Movie"));
        
        assertFalse(containsExpiredMovie, "Valid sessions should not include expired ones");
    }
    
    @Test
    void testBuyTicketsForValidSession() {
        List<Session> validSessions = cinemaService.getValidSessions();
        Session validSession = validSessions.get(0);
        
        int initialAvailableSeats = validSession.getAvailableSeats();
        
        int ticketsCount = 3;
        List<Ticket> tickets = cinemaService.buyTickets(validSession, ticketsCount);
        
        assertEquals(ticketsCount, tickets.size());
        assertEquals(initialAvailableSeats - ticketsCount, validSession.getAvailableSeats());
    }
    
    @Test
    void testCantBuyTicketsForExpiredSession() {
        Session expiredSession = cinema.getSessions().stream()
                .filter(Session::isExpired)
                .findFirst()
                .orElseThrow(() -> new AssertionError("No expired session found"));
        
        assertThrows(IllegalStateException.class, () -> cinemaService.buyTickets(expiredSession, 1));
    }
}
