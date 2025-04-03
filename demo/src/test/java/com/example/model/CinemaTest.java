package com.example.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CinemaTest {
    private Cinema cinema;
    private Session session1;
    private Session session2;
    
    @BeforeEach
    void setUp() {
        cinema = new Cinema("Test Cinema", "Test Address", 3);
        session1 = new Session("Test Movie 1", LocalDateTime.now().plusDays(1), 100, 120.0);
        session2 = new Session("Test Movie 2", LocalDateTime.now().plusDays(2), 150, 150.0);
    }
    
    @Test
    void testAddSession() {
        assertTrue(cinema.addSession(session1));
        assertEquals(1, cinema.getSessions().size());
        assertTrue(cinema.getSessions().contains(session1));
    }
    
    @Test
    void testAddNullSession() {
        assertFalse(cinema.addSession(null));
        assertEquals(0, cinema.getSessions().size());
    }
    
    @Test
    void testRemoveSession() {
        cinema.addSession(session1);
        assertTrue(cinema.removeSession(session1));
        assertEquals(0, cinema.getSessions().size());
    }
    
    @Test
    void testCalculateTotalRevenue() {
        cinema.addSession(session1);
        cinema.addSession(session2);
        
        for (int i = 0; i < 5; i++) {
            session1.buyTicket();
        }
        
        for (int i = 0; i < 10; i++) {
            session2.buyTicket();
        }
        
        assertEquals(2100.0, cinema.calculateTotalRevenue(), 0.01);
    }
    
    @Test
    void testEqualsAndHashCode() {
        Cinema cinema1 = new Cinema("Test Cinema", "Test Address", 3);
        Cinema cinema2 = new Cinema("Test Cinema", "Test Address", 3);
        Cinema cinema3 = new Cinema("Different Cinema", "Test Address", 3);
        
        assertEquals(cinema1, cinema2);
        assertEquals(cinema1.hashCode(), cinema2.hashCode());
        assertNotEquals(cinema1, cinema3);
        assertNotEquals(cinema1.hashCode(), cinema3.hashCode());
    }
    
    @Test
    void testAddSessionTwice() {
        assertTrue(cinema.addSession(session1));
        assertTrue(cinema.addSession(session1));
        
        List<Session> sessions = cinema.getSessions();
        assertEquals(2, sessions.size());
    }
    
    @Test
    void testRemoveNonexistentSession() {
        assertFalse(cinema.removeSession(session1));
    }
    
    @Test
    void testCalculateTotalRevenueWithNoSessions() {
        assertEquals(0.0, cinema.calculateTotalRevenue(), 0.01);
    }
    
    @Test
    void testCalculateTotalRevenueWithNoTicketsSold() {
        cinema.addSession(session1);
        cinema.addSession(session2);
        
        assertEquals(0.0, cinema.calculateTotalRevenue(), 0.01);
    }
    
    @Test
    void testSetSessionsEmpty() {
        cinema.addSession(session1);
        cinema.addSession(session2);
        
        cinema.setSessions(new ArrayList<>());
        
        assertTrue(cinema.getSessions().isEmpty());
    }
    
    @Test
    void testGetSessionsModification() {
        cinema.addSession(session1);
        
        List<Session> sessions = cinema.getSessions();
        sessions.add(session2);
        
        assertEquals(1, cinema.getSessions().size());
    }
}
