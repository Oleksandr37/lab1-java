package com.example.service;

import com.example.model.Cinema;
import com.example.model.Session;
import com.example.model.Ticket;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CinemaService {
    private Cinema cinema;
    
    public CinemaService() {
    }
    
    public CinemaService(Cinema cinema) {
        this.cinema = cinema;
    }
    
    public Cinema getCinema() {
        return cinema;
    }
    
    public void setCinema(Cinema cinema) {
        this.cinema = cinema;
    }
    
    public List<Session> getAllSessions() {
        if (cinema == null) {
            throw new IllegalStateException("Cinema is not initialized");
        }
        return cinema.getSessions();
    }
    
    public List<Session> getValidSessions() {
        if (cinema == null) {
            throw new IllegalStateException("Cinema is not initialized");
        }
        return cinema.getSessions().stream()
                .filter(Session::isValid)
                .collect(Collectors.toList());
    }
    
    public boolean addSession(Session session) {
        if (cinema == null) {
            throw new IllegalStateException("Cinema is not initialized");
        }
        return cinema.addSession(session);
    }
    
    public boolean removeSession(Session session) {
        if (cinema == null) {
            throw new IllegalStateException("Cinema is not initialized");
        }
        return cinema.removeSession(session);
    }
    
    public List<Ticket> buyTickets(Session session, int numberOfTickets) {
        if (cinema == null) {
            throw new IllegalStateException("Cinema is not initialized");
        }
        
        if (session == null) {
            throw new IllegalArgumentException("Session cannot be null");
        }
        
        if (!cinema.getSessions().contains(session)) {
            throw new IllegalArgumentException("Session not found in this cinema");
        }
        
        if (session.isExpired()) {
            throw new IllegalStateException("Cannot buy tickets for expired session");
        }
        
        return session.buyTickets(numberOfTickets);
    }
    
    public double calculateTotalRevenue() {
        if (cinema == null) {
            throw new IllegalStateException("Cinema is not initialized");
        }
        return cinema.calculateTotalRevenue();
    }
    
    public Optional<Session> findSessionById(String sessionId) {
        if (cinema == null) {
            throw new IllegalStateException("Cinema is not initialized");
        }
        
        return cinema.getSessions().stream()
            .filter(session -> session.getId().equals(sessionId))
            .findFirst();
    }
    
    public boolean updateSession(String sessionId, String newTitle, LocalDateTime newDateTime,
                               int newTotalSeats, double newTicketPrice) {
        if (cinema == null) {
            throw new IllegalStateException("Cinema is not initialized");
        }
        
        Optional<Session> optionalSession = findSessionById(sessionId);
        if (optionalSession.isEmpty()) {
            return false;
        }
        
        Session session = optionalSession.get();
        
        if (newDateTime != null) {
            if (session.getDateTime().isAfter(newDateTime)) {
                throw new IllegalArgumentException("New session date cannot be earlier than the current date");
            }
        }
        
        if (newTitle != null && !newTitle.trim().isEmpty()) {
            session.setMovieTitle(newTitle);
        }
        
        if (newDateTime != null) {
            session.setDateTime(newDateTime);
        }
        
        if (newTotalSeats > 0) {
            int ticketsSold = session.getTotalSeats() - session.getAvailableSeats();
            if (newTotalSeats < ticketsSold) {
                throw new IllegalArgumentException("New total seats cannot be less than the number of tickets already sold (" + ticketsSold + ")");
            }
            session.setTotalSeats(newTotalSeats);
        }
        
        if (newTicketPrice > 0) {
            session.setTicketPrice(newTicketPrice);
        }
        
        return true;
    }
    
    public boolean deleteTicket(String ticketId) {
        if (cinema == null) {
            throw new IllegalStateException("Cinema is not initialized");
        }
        
        for (Session session : cinema.getSessions()) {
            List<Ticket> tickets = session.getTickets();
            Optional<Ticket> ticketToDelete = tickets.stream()
                .filter(ticket -> ticket.getId().equals(ticketId))
                .findFirst();
            
            if (ticketToDelete.isPresent()) {
                boolean removed = session.removeTicket(ticketToDelete.get());
                if (removed) {
                    session.increaseAvailableSeats(1);
                }
                return removed;
            }
        }
        
        return false;
    }
    
    public boolean updateTicketSession(String ticketId, String newSessionId) {
        if (cinema == null) {
            throw new IllegalStateException("Cinema is not initialized");
        }
        
        Optional<Session> newSessionOpt = findSessionById(newSessionId);
        if (newSessionOpt.isEmpty()) {
            return false;
        }
        
        Session newSession = newSessionOpt.get();
        
        if (newSession.isExpired()) {
            throw new IllegalStateException("Cannot move ticket to an expired session");
        }
        
        if (newSession.getAvailableSeats() <= 0) {
            throw new IllegalStateException("New session has no available seats");
        }
        
        for (Session currentSession : cinema.getSessions()) {
            List<Ticket> tickets = currentSession.getTickets();
            Optional<Ticket> ticketToUpdate = tickets.stream()
                .filter(ticket -> ticket.getId().equals(ticketId))
                .findFirst();
            
            if (ticketToUpdate.isPresent()) {
                Ticket ticket = ticketToUpdate.get();
                
                if (currentSession.removeTicket(ticket)) {
                    currentSession.increaseAvailableSeats(1);
                    
                    Ticket newTicket = newSession.buyTicket();
                    
                    newTicket.setId(ticketId);
                    
                    return true;
                }
            }
        }
        
        return false;
    }
}
