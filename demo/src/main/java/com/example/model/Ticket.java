package com.example.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Ticket {
    private String id;
    
    @JsonIgnore
    private Session session;
    
    @JsonProperty("sessionId")
    private String sessionId;
    
    private LocalDateTime purchaseTime;
    private double price;
    
    public Ticket(Session session, double price) {
        this.id = UUID.randomUUID().toString();
        this.session = session;
        this.sessionId = session != null ? session.getId() : null;
        this.price = price;
        this.purchaseTime = LocalDateTime.now();
    }
    
    public Ticket() {
        this.id = UUID.randomUUID().toString();
        this.purchaseTime = LocalDateTime.now();
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    @JsonIgnore
    public Session getSession() {
        return session;
    }
    
    @JsonIgnore
    public void setSession(Session session) {
        this.session = session;
        this.sessionId = session != null ? session.getId() : null;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public LocalDateTime getPurchaseTime() {
        return purchaseTime;
    }
    
    public void setPurchaseTime(LocalDateTime purchaseTime) {
        this.purchaseTime = purchaseTime;
    }
    
    public double getPrice() {
        return price;
    }
    
    public void setPrice(double price) {
        this.price = price;
    }
    
    @JsonIgnore
    public boolean isValid() {
        return session != null && LocalDateTime.now().isBefore(session.getDateTime());
    }
    
    @JsonIgnore
    public boolean isExpired() {
        return !isValid();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ticket ticket = (Ticket) o;
        return Objects.equals(id, ticket.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Ticket{" +
               "id='" + id + '\'' +
               ", sessionId='" + sessionId + '\'' +
               ", purchaseTime=" + purchaseTime +
               ", price=" + price +
               '}';
    }
}
