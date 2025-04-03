package com.example.service;

import com.example.model.Cinema;
import com.example.model.Session;
import com.example.model.Ticket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class CinemaServiceTest {
    private CinemaService cinemaService;
    private Cinema cinema;
    private Session session;
    
    @BeforeEach
    void setUp() {
        cinema = new Cinema("Test Cinema", "Test Address", 3);
        session = new Session("Test Movie", LocalDateTime.now().plusDays(1), 100, 120.0);
        
        cinema.addSession(session);
        
        cinemaService = new CinemaService(cinema);
    }
    
    @Test
    void testGetAllSessions() {
        List<Session> sessions = cinemaService.getAllSessions();
        
        assertEquals(1, sessions.size());
        assertTrue(sessions.contains(session));
    }
    
    @Test
    void testAddSession() {
        Session newSession = new Session("New Movie", LocalDateTime.now().plusDays(2), 150, 150.0);
        assertTrue(cinemaService.addSession(newSession));
        
        List<Session> sessions = cinemaService.getAllSessions();
        assertEquals(2, sessions.size());
        assertTrue(sessions.contains(newSession));
    }
    
    @Test
    void testRemoveSession() {
        assertTrue(cinemaService.removeSession(session));
        
        List<Session> sessions = cinemaService.getAllSessions();
        assertEquals(0, sessions.size());
    }
    
    @Test
    void testBuyTickets() {
        List<Ticket> tickets = cinemaService.buyTickets(session, 5);
        
        assertEquals(5, tickets.size());
        assertEquals(95, session.getAvailableSeats());
    }
    
    @Test
    void testBuyTicketsInvalidSession() {
        Session unknownSession = new Session("Unknown Movie", LocalDateTime.now(), 100, 100.0);
        
        assertThrows(IllegalArgumentException.class, () -> cinemaService.buyTickets(unknownSession, 5));
    }
    
    @Test
    void testCalculateTotalRevenue() {
        cinemaService.buyTickets(session, 10);
        
        assertEquals(1200.0, cinemaService.calculateTotalRevenue(), 0.01);
    }
    
    @Test
    void testUninitializedCinema() {
        CinemaService emptyCinemaService = new CinemaService();
        
        assertThrows(IllegalStateException.class, emptyCinemaService::getAllSessions);
        assertThrows(IllegalStateException.class, () -> emptyCinemaService.addSession(session));
        assertThrows(IllegalStateException.class, () -> emptyCinemaService.removeSession(session));
        assertThrows(IllegalStateException.class, () -> emptyCinemaService.buyTickets(session, 1));
        assertThrows(IllegalStateException.class, emptyCinemaService::calculateTotalRevenue);
    }
    
    @Test
    void testGetValidSessions() {
        cinema = new Cinema("Test Cinema", "Test Address", 3);
        cinemaService = new CinemaService(cinema);
        
        Session futureSession = new Session("Future Movie", LocalDateTime.now().plusDays(1), 100, 120.0);
        cinema.addSession(futureSession);
        
        Session pastSession = new Session("Past Movie", LocalDateTime.now().minusDays(1), 100, 120.0);
        cinema.addSession(pastSession);
        
        List<Session> validSessions = cinemaService.getValidSessions();
        assertEquals(1, validSessions.size());
        assertTrue(validSessions.contains(futureSession));
        assertFalse(validSessions.contains(pastSession));
    }
    
    @Test
    void testBuyTicketsForExpiredSession() {
        Session expiredSession = new Session("Past Movie", LocalDateTime.now().minusDays(1), 100, 120.0);
        cinema.addSession(expiredSession);
        
        assertThrows(IllegalStateException.class, () -> cinemaService.buyTickets(expiredSession, 5));
    }
    
    @Test
    void testGetValidSessionsWithEmptyCinema() {
        cinema = new Cinema("Empty Cinema", "Empty Address", 1);
        cinemaService.setCinema(cinema);
        
        List<Session> validSessions = cinemaService.getValidSessions();
        assertTrue(validSessions.isEmpty());
    }
    
    @Test
    void testGetValidSessionsWithOnlyExpiredSessions() {
        cinema = new Cinema("Test Cinema", "Test Address", 3);
        cinemaService.setCinema(cinema);
        
        Session pastSession1 = new Session("Past Movie 1", LocalDateTime.now().minusDays(1), 100, 120.0);
        Session pastSession2 = new Session("Past Movie 2", LocalDateTime.now().minusDays(2), 80, 100.0);
        
        cinema.addSession(pastSession1);
        cinema.addSession(pastSession2);
        
        List<Session> validSessions = cinemaService.getValidSessions();
        assertTrue(validSessions.isEmpty());
    }
    
    @Test
    void testBuyTicketsWithMaximumAvailable() {
        int totalSeats = 100;
        
        List<Ticket> tickets = cinemaService.buyTickets(session, totalSeats);
        
        assertEquals(totalSeats, tickets.size());
        assertEquals(0, session.getAvailableSeats());
    }
    
    @Test
    void testBuyTicketsWithNullSession() {
        assertThrows(IllegalArgumentException.class, () -> cinemaService.buyTickets(null, 5));
    }
    
    @Test
    void testAddNullSession() {
        assertFalse(cinemaService.addSession(null));
    }
    
    @Test
    void testRemoveNullSession() {
        assertFalse(cinemaService.removeSession(null));
    }

    @Test
    void testUpdateSessionSuccess() {
        Session sessionToUpdate = new Session("Original Movie", LocalDateTime.now().plusDays(1), 100, 120.0);
        String sessionId = sessionToUpdate.getId();
        cinema.addSession(sessionToUpdate);
        
        String newTitle = "Updated Movie";
        LocalDateTime newDateTime = LocalDateTime.now().plusDays(2);
        int newTotalSeats = 150;
        double newPrice = 150.0;
        
        boolean result = cinemaService.updateSession(sessionId, newTitle, newDateTime, newTotalSeats, newPrice);
        
        assertTrue(result);
        Optional<Session> updatedSession = cinemaService.findSessionById(sessionId);
        assertTrue(updatedSession.isPresent());
        assertEquals(newTitle, updatedSession.get().getMovieTitle());
        assertEquals(newDateTime, updatedSession.get().getDateTime());
        assertEquals(newTotalSeats, updatedSession.get().getTotalSeats());
        assertEquals(newPrice, updatedSession.get().getTicketPrice(), 0.01);
    }

    @Test
    void testUpdateSessionWithEarlierDateFails() {
        LocalDateTime originalDate = LocalDateTime.now().plusDays(5);
        Session sessionToUpdate = new Session("Original Movie", originalDate, 100, 120.0);
        String sessionId = sessionToUpdate.getId();
        cinema.addSession(sessionToUpdate);
        
        LocalDateTime earlierDate = LocalDateTime.now().plusDays(3); 
        
        Exception exception = assertThrows(IllegalArgumentException.class, 
                   () -> cinemaService.updateSession(sessionId, "New Title", earlierDate, 150, 150.0));
                   
        assertTrue(exception.getMessage().contains("earlier than the current date"),
                  "Exception should mention the date is too early");
        
        Optional<Session> session = cinemaService.findSessionById(sessionId);
        assertTrue(session.isPresent());
        assertEquals("Original Movie", session.get().getMovieTitle());
        assertEquals(originalDate, session.get().getDateTime());
    }

    @Test
    void testUpdateSessionWithNonexistentIdReturnsFalse() {
        boolean result = cinemaService.updateSession("nonexistent-id", "New Title", 
                                                   LocalDateTime.now().plusDays(1), 100, 100.0);
        
        assertFalse(result);
    }

    @Test
    void testDeleteTicketSuccess() {
        Session session = cinemaService.getAllSessions().get(0);
        int initialAvailableSeats = session.getAvailableSeats();
        
        Ticket ticket = session.buyTicket();
        String ticketId = ticket.getId();
        
        assertEquals(initialAvailableSeats - 1, session.getAvailableSeats());
        
        boolean result = cinemaService.deleteTicket(ticketId);
        
        assertTrue(result);
        assertEquals(initialAvailableSeats, session.getAvailableSeats());
    }

    @Test
    void testDeleteNonexistentTicketReturnsFalse() {
        boolean result = cinemaService.deleteTicket("nonexistent-ticket-id");
        
        assertFalse(result);
    }

    @Test
    void testUpdateTicketSessionSuccess() {
        Session newSession = new Session("Test Movie 2", LocalDateTime.now().plusDays(2), 100, 120.0);
        cinema.addSession(newSession);
        
        Session originalSession = cinemaService.getAllSessions().get(0);
        Session targetSession = cinemaService.getAllSessions().get(1);
        
        if (originalSession.isExpired() || targetSession.isExpired()) {
            return;
        }
        
        int originalSessionInitialSeats = originalSession.getAvailableSeats();
        int newSessionInitialSeats = targetSession.getAvailableSeats();
        
        Ticket ticket = originalSession.buyTicket();
        String ticketId = ticket.getId();
        
        assertEquals(originalSessionInitialSeats - 1, originalSession.getAvailableSeats());
        
        boolean result = cinemaService.updateTicketSession(ticketId, targetSession.getId());
        
        assertTrue(result);
        
        assertEquals(originalSessionInitialSeats, originalSession.getAvailableSeats());
        
        assertEquals(newSessionInitialSeats - 1, targetSession.getAvailableSeats());
    }

    @Test
    void testUpdateTicketToExpiredSessionFails() {
        Session validSession = cinemaService.getValidSessions().get(0);
        
        final Session expiredSession = new Session("Expired Movie", LocalDateTime.now().minusDays(1), 100, 100.0);
        cinema.addSession(expiredSession);
        
        Ticket ticket = validSession.buyTicket();
        String ticketId = ticket.getId();
        
        assertThrows(IllegalStateException.class, 
                    () -> cinemaService.updateTicketSession(ticketId, expiredSession.getId()));
    }

    @Test
    void testUpdateTicketToFullSessionFails() {
        Session sourceSession = new Session("Source Movie", LocalDateTime.now().plusDays(1), 10, 100.0);
        Session fullSession = new Session("Full Movie", LocalDateTime.now().plusDays(1), 1, 100.0);
        
        cinema.addSession(sourceSession);
        cinema.addSession(fullSession);
        
        fullSession.buyTicket();
        
        Ticket ticket = sourceSession.buyTicket();
        String ticketId = ticket.getId();
        
        assertThrows(IllegalStateException.class, 
                    () -> cinemaService.updateTicketSession(ticketId, fullSession.getId()));
    }

    @Test
    void testUpdateTicketWithNonexistentIdsReturnsFalse() {
        assertFalse(cinemaService.updateTicketSession("nonexistent-ticket-id", session.getId()));
        
        Ticket ticket = session.buyTicket();
        assertFalse(cinemaService.updateTicketSession(ticket.getId(), "nonexistent-session-id"));
    }

    @Test
    void testUpdateSessionWithFewerSeatsThanSoldTicketsFails() {
        Session sessionToUpdate = new Session("Original Movie", LocalDateTime.now().plusDays(1), 100, 120.0);
        String sessionId = sessionToUpdate.getId();
        cinema.addSession(sessionToUpdate);
        
        int ticketsToBuy = 20;
        for (int i = 0; i < ticketsToBuy; i++) {
            sessionToUpdate.buyTicket();
        }
        
        assertEquals(100 - ticketsToBuy, sessionToUpdate.getAvailableSeats());
        
        int invalidNewSeats = ticketsToBuy - 5; 
        
        Exception exception = assertThrows(IllegalArgumentException.class, 
                    () -> cinemaService.updateSession(sessionId, "New Title", null, invalidNewSeats, 0));
                    
        assertTrue(exception.getMessage().contains("tickets already sold"));
        
        assertEquals(100, sessionToUpdate.getTotalSeats());
        assertEquals(80, sessionToUpdate.getAvailableSeats());
    }

    @Test
    void testUpdateSessionWithExactSameNumberOfSeatsAsTicketsSold() {
        Session sessionToUpdate = new Session("Original Movie", LocalDateTime.now().plusDays(1), 100, 120.0);
        String sessionId = sessionToUpdate.getId();
        cinema.addSession(sessionToUpdate);
        
        int ticketsToBuy = 20;
        for (int i = 0; i < ticketsToBuy; i++) {
            sessionToUpdate.buyTicket();
        }
        
        int exactlyTicketsSold = ticketsToBuy; 
        
        boolean result = cinemaService.updateSession(sessionId, "New Title", null, exactlyTicketsSold, 0);
        
        assertTrue(result);
        assertEquals(exactlyTicketsSold, sessionToUpdate.getTotalSeats());
        assertEquals(0, sessionToUpdate.getAvailableSeats());
    }

    @Test
    void testUpdateSessionWithMoreSeats() {
        Session sessionToUpdate = new Session("Original Movie", LocalDateTime.now().plusDays(1), 50, 120.0);
        String sessionId = sessionToUpdate.getId();
        cinema.addSession(sessionToUpdate);
        
        int ticketsToBuy = 10;
        for (int i = 0; i < ticketsToBuy; i++) {
            sessionToUpdate.buyTicket();
        }
        
        assertEquals(40, sessionToUpdate.getAvailableSeats());
        
        int newTotalSeats = 100;
        boolean result = cinemaService.updateSession(sessionId, null, null, newTotalSeats, 0);
        
        assertTrue(result);
        assertEquals(100, sessionToUpdate.getTotalSeats());
        
        assertEquals(90, sessionToUpdate.getAvailableSeats()); 
    }
}
