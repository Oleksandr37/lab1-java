package com.example.service;

import com.example.model.Cinema;
import com.example.model.Session;
import com.example.model.Ticket;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ExportImportService {
    public static final int NO_SORT = 1;
    public static final int SORT_BY_TITLE = 2;
    public static final int SORT_BY_DATE = 3;
    public static final int SORT_BY_SEATS = 4;
    
    private final ObjectMapper objectMapper;
    
    public ExportImportService() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }
    
    public void exportData(Cinema cinema, String filename, int sortOption) throws IOException {
        if (cinema == null) {
            throw new IllegalArgumentException("Cinema cannot be null");
        }
        
        Map<String, Object> exportData = new HashMap<>();
        
        exportData.put("name", cinema.getName());
        exportData.put("address", cinema.getAddress());
        exportData.put("hallCount", cinema.getHallCount());
        
        List<Session> sessions = new ArrayList<>(cinema.getSessions());
        
        switch (sortOption) {
            case SORT_BY_TITLE:
                sessions.sort(Comparator.comparing(Session::getMovieTitle));
                break;
            case SORT_BY_DATE:
                sessions.sort(Comparator.comparing(Session::getDateTime));
                break;
            case SORT_BY_SEATS:
                sessions.sort(Comparator.comparing(Session::getAvailableSeats).reversed());
                break;
            case NO_SORT:
            default:
                break;
        }
        
        exportData.put("sessions", sessions);
        
        objectMapper.writeValue(new File(filename), exportData);
    }
    
    public Cinema importData(String filename) throws IOException {
        Map<String, Object> importedData = objectMapper.readValue(new File(filename), 
                                                               objectMapper.getTypeFactory().constructMapType(
                                                                   HashMap.class, String.class, Object.class));
        
        String name = (String) importedData.get("name");
        String address = (String) importedData.get("address");
        int hallCount = (Integer) importedData.get("hallCount");
        
        Cinema cinema = new Cinema(name, address, hallCount);
        
        List<LinkedHashMap<String, Object>> sessionMaps = (List<LinkedHashMap<String, Object>>) importedData.get("sessions");
        
        Map<String, Session> sessionsById = new HashMap<>();
        
        if (sessionMaps != null) {
            for (LinkedHashMap<String, Object> sessionMap : sessionMaps) {
                String id = (String) sessionMap.get("id");
                String movieTitle = (String) sessionMap.get("movieTitle");
                String dateTimeStr = (String) sessionMap.get("dateTime");
                int totalSeats = (Integer) sessionMap.get("totalSeats");
                int availableSeats = (Integer) sessionMap.get("availableSeats");
                double ticketPrice = ((Number) sessionMap.get("ticketPrice")).doubleValue();
                
                Session session = objectMapper.readValue(objectMapper.writeValueAsString(sessionMap), Session.class);
                
                cinema.addSession(session);
                sessionsById.put(id, session);
                
                List<LinkedHashMap<String, Object>> ticketMaps = 
                    (List<LinkedHashMap<String, Object>>) sessionMap.get("tickets");
                
                if (ticketMaps != null) {
                    for (LinkedHashMap<String, Object> ticketMap : ticketMaps) {
                        Ticket ticket = objectMapper.readValue(objectMapper.writeValueAsString(ticketMap), Ticket.class);
                        ticket.setSession(session);
                    }
                }
            }
        }
        
        return cinema;
    }
}
