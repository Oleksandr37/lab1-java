package com.example.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Session {
    private String id;
    private String movieTitle;
    private LocalDateTime dateTime;
    private int totalSeats;
    private int availableSeats;
    private double ticketPrice;
    private List<Ticket> tickets;
    
    public Session(String movieTitle, LocalDateTime dateTime, int totalSeats, double ticketPrice) {
        if (totalSeats <= 0) {
            throw new IllegalArgumentException("Total seats must be greater than zero");
        }
        if (ticketPrice <= 0) {
            throw new IllegalArgumentException("Ticket price must be greater than zero");
        }
        if (dateTime == null) {
            throw new IllegalArgumentException("Session date and time cannot be null");
        }
        
        this.id = UUID.randomUUID().toString();
        this.movieTitle = movieTitle;
        this.dateTime = dateTime;
        this.totalSeats = totalSeats;
        this.availableSeats = totalSeats;
        this.ticketPrice = ticketPrice;
        this.tickets = new ArrayList<>();
    }
    
    public Session() {
        this.id = UUID.randomUUID().toString();
        this.tickets = new ArrayList<>();
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getMovieTitle() {
        return movieTitle;
    }
    
    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }
    
    public LocalDateTime getDateTime() {
        return dateTime;
    }
    
    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }
    
    public int getTotalSeats() {
        return totalSeats;
    }
    
    public void setTotalSeats(int totalSeats) {
        if (totalSeats < (this.totalSeats - this.availableSeats)) {
            throw new IllegalArgumentException("Cannot set total seats less than sold tickets");
        }
        this.availableSeats += (totalSeats - this.totalSeats);
        this.totalSeats = totalSeats;
    }
    
    public int getAvailableSeats() {
        return availableSeats;
    }
    
    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }
    
    public double getTicketPrice() {
        return ticketPrice;
    }
    
    public void setTicketPrice(double ticketPrice) {
        if (ticketPrice < 0) {
            throw new IllegalArgumentException("Ticket price cannot be negative");
        }
        this.ticketPrice = ticketPrice;
    }
    
    public List<Ticket> getTickets() {
        return new ArrayList<>(tickets);
    }
    
    public void setTickets(List<Ticket> tickets) {
        this.tickets = new ArrayList<>(tickets);
    }
    
    @JsonIgnore
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(dateTime);
    }
    
    @JsonIgnore
    public boolean isValid() {
        return !isExpired();
    }
    
    public Ticket buyTicket() throws IllegalStateException {
        if (isExpired()) {
            throw new IllegalStateException("Cannot buy ticket for expired session");
        }
        
        if (availableSeats <= 0) {
            throw new IllegalStateException("No available seats for this session");
        }
        
        Ticket ticket = new Ticket(this, ticketPrice);
        tickets.add(ticket);
        availableSeats--;
        
        return ticket;
    }
    
    public List<Ticket> buyTickets(int numberOfTickets) throws IllegalArgumentException {
        if (isExpired()) {
            throw new IllegalStateException("Cannot buy tickets for expired session");
        }
        
        if (numberOfTickets <= 0) {
            throw new IllegalArgumentException("Number of tickets must be positive");
        }
        
        if (numberOfTickets > availableSeats) {
            throw new IllegalArgumentException("Not enough available seats. Only " + availableSeats + " left.");
        }
        
        List<Ticket> purchasedTickets = new ArrayList<>();
        for (int i = 0; i < numberOfTickets; i++) {
            Ticket ticket = new Ticket(this, ticketPrice);
            tickets.add(ticket);
            availableSeats--;
            purchasedTickets.add(ticket);
        }
        
        return purchasedTickets;
    }
    
    public boolean removeTicket(Ticket ticket) {
        if (ticket == null) {
            return false;
        }
        
        return tickets.removeIf(t -> t.getId().equals(ticket.getId()));
    }
    
    public void increaseAvailableSeats(int amount) {
        if (amount <= 0) {
            return;
        }
        
        this.availableSeats = Math.min(this.availableSeats + amount, this.totalSeats);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Session session = (Session) o;
        return Objects.equals(id, session.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Session{" +
               "id='" + id + '\'' +
               ", movieTitle='" + movieTitle + '\'' +
               ", dateTime=" + dateTime +
               ", totalSeats=" + totalSeats +
               ", availableSeats=" + availableSeats +
               ", ticketPrice=" + ticketPrice +
               '}';
    }
}
