package com.example.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TicketTest {
    
    @Test
    void testTicketCreation() {
        Session session = new Session("Test Movie", LocalDateTime.now().plusDays(1), 100, 120.0);
        Ticket ticket = new Ticket(session, 120.0);
        
        assertNotNull(ticket.getId());
        assertEquals(session, ticket.getSession());
        assertEquals(120.0, ticket.getPrice(), 0.01);
        assertNotNull(ticket.getPurchaseTime());
    }
    
    @Test
    void testTicketValidity() {
        Session futureSession = new Session("Future Movie", LocalDateTime.now().plusDays(1), 100, 120.0);
        Ticket validTicket = new Ticket(futureSession, 120.0);
        
        assertTrue(validTicket.isValid());
        assertFalse(validTicket.isExpired());
        
        Session pastSession = new Session("Past Movie", LocalDateTime.now().minusDays(1), 100, 120.0);
        Ticket expiredTicket = new Ticket(pastSession, 120.0);
        
        assertFalse(expiredTicket.isValid());
        assertTrue(expiredTicket.isExpired());
    }
    
    @Test
    void testEqualsAndHashCode() {
        Session session = new Session("Test Movie", LocalDateTime.now().plusDays(1), 100, 120.0);
        Ticket ticket1 = new Ticket(session, 120.0);
        Ticket ticket2 = new Ticket(session, 120.0);
        
        assertNotEquals(ticket1, ticket2);
        assertNotEquals(ticket1.hashCode(), ticket2.hashCode());
    }
    
    @Test
    void testTicketWithNullSession() {
        Ticket ticket = new Ticket(null, 100.0);
        
        assertNotNull(ticket.getId());
        assertNull(ticket.getSession());
        assertNull(ticket.getSessionId());
        assertEquals(100.0, ticket.getPrice(), 0.01);
        
        assertFalse(ticket.isValid());
        assertTrue(ticket.isExpired());
    }
    
    @Test
    void testSetSessionAndSessionId() {
        Session session1 = new Session("Movie 1", LocalDateTime.now().plusDays(1), 100, 120.0);
        Session session2 = new Session("Movie 2", LocalDateTime.now().plusDays(2), 150, 150.0);
        
        Ticket ticket = new Ticket(session1, 120.0);
        String originalSessionId = ticket.getSessionId();
        
        ticket.setSession(session2);
        
        assertNotEquals(originalSessionId, ticket.getSessionId());
        assertEquals(session2.getId(), ticket.getSessionId());
        assertEquals(session2, ticket.getSession());
    }
    
    @Test
    void testSetSessionToNull() {
        Session session = new Session("Test Movie", LocalDateTime.now().plusDays(1), 100, 120.0);
        Ticket ticket = new Ticket(session, 120.0);
        
        ticket.setSession(null);
        
        assertNull(ticket.getSession());
        assertNull(ticket.getSessionId());
    }
    
    @Test
    void testModifyTicketProperties() {
        Session session = new Session("Test Movie", LocalDateTime.now().plusDays(1), 100, 120.0);
        Ticket ticket = new Ticket(session, 120.0);
        
        LocalDateTime newTime = LocalDateTime.now().minusHours(1);
        ticket.setPurchaseTime(newTime);
        ticket.setPrice(150.0);
        
        assertEquals(newTime, ticket.getPurchaseTime());
        assertEquals(150.0, ticket.getPrice(), 0.01);
    }
}
