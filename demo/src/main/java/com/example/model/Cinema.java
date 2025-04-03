package com.example.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Cinema {
    private String name;
    private String address;
    private int hallCount;
    private List<Session> sessions;
    
    @JsonCreator
    public Cinema(
            @JsonProperty("name") String name,
            @JsonProperty("address") String address,
            @JsonProperty("hallCount") int hallCount) {
        this.name = name;
        this.address = address;
        this.hallCount = hallCount;
        this.sessions = new ArrayList<>();
    }
    
    public Cinema() {
        this.sessions = new ArrayList<>();
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public int getHallCount() {
        return hallCount;
    }
    
    public void setHallCount(int hallCount) {
        this.hallCount = hallCount;
    }
    
    public List<Session> getSessions() {
        return new ArrayList<>(sessions);
    }
    
    public void setSessions(List<Session> sessions) {
        this.sessions = new ArrayList<>(sessions);
    }
    
    public boolean addSession(Session session) {
        if (session == null) {
            return false;
        }
        return sessions.add(session);
    }
    
    public boolean removeSession(Session session) {
        return sessions.remove(session);
    }
    
    public double calculateTotalRevenue() {
        double totalRevenue = 0;
        for (Session session : sessions) {
            int soldTickets = session.getTotalSeats() - session.getAvailableSeats();
            totalRevenue += soldTickets * session.getTicketPrice();
        }
        return totalRevenue;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cinema cinema = (Cinema) o;
        return hallCount == cinema.hallCount &&
               Objects.equals(name, cinema.name) &&
               Objects.equals(address, cinema.address);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, address, hallCount);
    }
    
    @Override
    public String toString() {
        return "Cinema{" +
               "name='" + name + '\'' +
               ", address='" + address + '\'' +
               ", hallCount=" + hallCount +
               ", sessions=" + sessions +
               '}';
    }
}
