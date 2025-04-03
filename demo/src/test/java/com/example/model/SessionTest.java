package com.example.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SessionTest {
    private Session session;
    
    @BeforeEach
    void setUp() {
        session = new Session("Test Movie", LocalDateTime.now().plusDays(1), 100, 120.0);
    }
    
    @Test
    void testBuyTicket() {
        Ticket ticket = session.buyTicket();
        
        assertNotNull(ticket);
        assertEquals(99, session.getAvailableSeats());
        assertEquals("Test Movie", ticket.getSession().getMovieTitle());
        assertEquals(120.0, ticket.getPrice(), 0.01);
    }
    
    @Test
    void testBuyTicketWhenNoSeatsAvailable() {
        Session smallSession = new Session("Small Session", LocalDateTime.now().plusDays(1), 1, 100.0);
        smallSession.buyTicket();
        
        assertThrows(IllegalStateException.class, smallSession::buyTicket);
    }
    
    @Test
    void testBuyMultipleTickets() {
        List<Ticket> tickets = session.buyTickets(5);
        
        assertEquals(5, tickets.size());
        assertEquals(95, session.getAvailableSeats());
        
        for (Ticket ticket : tickets) {
            assertEquals(120.0, ticket.getPrice(), 0.01);
        }
    }
    
    @Test
    void testBuyNegativeTickets() {
        assertThrows(IllegalArgumentException.class, () -> session.buyTickets(-1));
    }
    
    @Test
    void testBuyMoreTicketsThanAvailable() {
        assertThrows(IllegalArgumentException.class, () -> session.buyTickets(101));
    }
    
    @Test
    void testSetTotalSeatsValid() {
        session.setTotalSeats(150);
        assertEquals(150, session.getTotalSeats());
        assertEquals(150, session.getAvailableSeats());
    }
    
    @Test
    void testSetTotalSeatsAfterTicketsSold() {
        session.buyTickets(10); 
        
        session.setTotalSeats(150);
        assertEquals(150, session.getTotalSeats());
        assertEquals(140, session.getAvailableSeats());
        
        assertThrows(IllegalArgumentException.class, () -> session.setTotalSeats(5));
    }
    
    @Test
    void testIsExpired() {
        Session futureSession = new Session("Future Movie", LocalDateTime.now().plusDays(1), 100, 120.0);
        assertFalse(futureSession.isExpired());
        assertTrue(futureSession.isValid());
        
        Session pastSession = new Session("Past Movie", LocalDateTime.now().minusDays(1), 100, 120.0);
        assertTrue(pastSession.isExpired());
        assertFalse(pastSession.isValid());
    }
    
    @Test
    void testBuyTicketForExpiredSession() {
        Session expiredSession = new Session("Past Movie", LocalDateTime.now().minusDays(1), 100, 120.0);
        
        assertThrows(IllegalStateException.class, expiredSession::buyTicket);
    }
    
    @Test
    void testBuyMultipleTicketsForExpiredSession() {
        Session expiredSession = new Session("Past Movie", LocalDateTime.now().minusDays(1), 100, 120.0);
        
        assertThrows(IllegalStateException.class, () -> expiredSession.buyTickets(5));
    }
    
    @Test
    void testSessionWithZeroSeats() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Session("Zero Seats Movie", LocalDateTime.now().plusDays(1), 0, 100.0);
        });
    }
    
    @Test
    void testSessionWithNegativePrice() {
        Session session = new Session("Test Movie", LocalDateTime.now().plusDays(1), 100, 120.0);
        
        assertThrows(IllegalArgumentException.class, () -> session.setTicketPrice(-50.0));
    }
    
    @Test
    void testSessionExactlyAtCurrentTime() {
        LocalDateTime now = LocalDateTime.now();
        Session borderlineSession = new Session("Borderline Movie", now, 100, 120.0);
        
        boolean isValid = borderlineSession.isValid();
        boolean isExpired = borderlineSession.isExpired();
        
        assertTrue(isValid != isExpired, "Session should be either valid or expired, not both or neither");
    }
    
    @Test
    void testSetTotalSeatsToSameValue() {
        session.setTotalSeats(100);
        assertEquals(100, session.getTotalSeats());
        assertEquals(100, session.getAvailableSeats());
    }
    
    @Test
    void testBuyAllAvailableTickets() {
        List<Ticket> tickets = session.buyTickets(100);
        
        assertEquals(100, tickets.size());
        assertEquals(0, session.getAvailableSeats());
        
        assertThrows(IllegalArgumentException.class, () -> session.buyTickets(1));
        assertThrows(IllegalStateException.class, session::buyTicket);
    }

    @Test
    void testCreateSessionWithZeroSeats() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Session("Test Movie", LocalDateTime.now().plusDays(1), 0, 100.0);
        });
    }

    @Test
    void testCreateSessionWithNegativeSeats() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Session("Test Movie", LocalDateTime.now().plusDays(1), -10, 100.0);
        });
    }

    @Test
    void testCreateSessionWithZeroPrice() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Session("Test Movie", LocalDateTime.now().plusDays(1), 100, 0.0);
        });
    }

    @Test
    void testCreateSessionWithNegativePrice() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Session("Test Movie", LocalDateTime.now().plusDays(1), 100, -50.0);
        });
    }

    @Test
    void testCreateSessionWithNullDate() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Session("Test Movie", null, 100, 100.0);
        });
    }
}
