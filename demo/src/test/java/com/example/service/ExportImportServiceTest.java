package com.example.service;

import com.example.model.Cinema;
import com.example.model.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ExportImportServiceTest {
    private ExportImportService exportImportService;
    private Cinema mockCinema;
    private Session mockSession1;
    private Session mockSession2;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        exportImportService = new ExportImportService();
        
        mockCinema = mock(Cinema.class);
        mockSession1 = mock(Session.class);
        mockSession2 = mock(Session.class);
        
        when(mockCinema.getName()).thenReturn("Mock Cinema");
        when(mockCinema.getAddress()).thenReturn("123 Mock St");
        when(mockCinema.getHallCount()).thenReturn(5);
        
        List<Session> sessions = new ArrayList<>();
        sessions.add(mockSession1);
        sessions.add(mockSession2);
        when(mockCinema.getSessions()).thenReturn(sessions);
        
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        when(mockSession1.getMovieTitle()).thenReturn("Mock Movie 1");
        when(mockSession1.getDateTime()).thenReturn(tomorrow);
        when(mockSession1.getTotalSeats()).thenReturn(100);
        when(mockSession1.getAvailableSeats()).thenReturn(90);
        when(mockSession1.getTicketPrice()).thenReturn(120.0);
        when(mockSession1.getTickets()).thenReturn(new ArrayList<>());
        
        LocalDateTime dayAfterTomorrow = LocalDateTime.now().plusDays(2);
        when(mockSession2.getMovieTitle()).thenReturn("Mock Movie 2");
        when(mockSession2.getDateTime()).thenReturn(dayAfterTomorrow);
        when(mockSession2.getTotalSeats()).thenReturn(150);
        when(mockSession2.getAvailableSeats()).thenReturn(150);
        when(mockSession2.getTicketPrice()).thenReturn(150.0);
        when(mockSession2.getTickets()).thenReturn(new ArrayList<>());
    }
    
    @Test
    void testExportNullCinema() {
        Path filePath = tempDir.resolve("null-cinema.json");
        
        assertThrows(IllegalArgumentException.class, 
                     () -> exportImportService.exportData(null, filePath.toString(), ExportImportService.NO_SORT));
    }
    
    @Test
    void testImportData() throws IOException {
        Cinema realCinema = new Cinema("Real Cinema", "123 Real St", 3);
        
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        Session realSession = new Session("Test Movie", tomorrow, 100, 120.0);
        realCinema.addSession(realSession);
        
        Path filePath = tempDir.resolve("real-export.json");
        exportImportService.exportData(realCinema, filePath.toString(), ExportImportService.NO_SORT);
        
        Cinema importedCinema = exportImportService.importData(filePath.toString());
        
        assertNotNull(importedCinema);
        assertEquals("Real Cinema", importedCinema.getName());
        assertEquals("123 Real St", importedCinema.getAddress());
        assertEquals(3, importedCinema.getHallCount());
        assertEquals(1, importedCinema.getSessions().size());
        
        Session importedSession = importedCinema.getSessions().get(0);
        assertEquals("Test Movie", importedSession.getMovieTitle());
        assertEquals(100, importedSession.getTotalSeats());
        assertEquals(100, importedSession.getAvailableSeats());
        assertEquals(120.0, importedSession.getTicketPrice(), 0.001);
    }
    
    @Test
    void testImportNonExistentFile() {
        Path nonExistentFile = tempDir.resolve("non-existent.json");
        
        assertThrows(IOException.class, 
                     () -> exportImportService.importData(nonExistentFile.toString()));
    }
    
    @Test
    void testExportWithDifferentSorting() throws IOException {
        Cinema realCinema = new Cinema("Test Cinema", "123 Test St", 2);
        
        Session session1 = new Session("B Movie", LocalDateTime.now().plusDays(2), 100, 120.0);
        Session session2 = new Session("A Movie", LocalDateTime.now().plusDays(1), 50, 150.0);
        Session session3 = new Session("C Movie", LocalDateTime.now().plusDays(3), 150, 100.0);
        
        realCinema.addSession(session1);
        realCinema.addSession(session2);
        realCinema.addSession(session3);
        
        Path titleSortPath = tempDir.resolve("title-sort.json");
        exportImportService.exportData(realCinema, titleSortPath.toString(), ExportImportService.SORT_BY_TITLE);
        
        Path dateSortPath = tempDir.resolve("date-sort.json");
        exportImportService.exportData(realCinema, dateSortPath.toString(), ExportImportService.SORT_BY_DATE);
        
        Path seatsSortPath = tempDir.resolve("seats-sort.json");
        exportImportService.exportData(realCinema, seatsSortPath.toString(), ExportImportService.SORT_BY_SEATS);
        
        assertTrue(titleSortPath.toFile().exists());
        assertTrue(dateSortPath.toFile().exists());
        assertTrue(seatsSortPath.toFile().exists());
    }
    
    @Test
    void testExportEmptyCinema() throws IOException {
        Cinema emptyCinema = new Cinema("Empty Cinema", "123 Empty St", 1);
        
        Path filePath = tempDir.resolve("empty-cinema.json");
        exportImportService.exportData(emptyCinema, filePath.toString(), ExportImportService.NO_SORT);
        
        assertTrue(filePath.toFile().exists());
        
        Cinema importedCinema = exportImportService.importData(filePath.toString());
        assertEquals("Empty Cinema", importedCinema.getName());
        assertTrue(importedCinema.getSessions().isEmpty());
    }
    
    @Test
    void testExportSortingWithIdenticalValues() throws IOException {
        Cinema cinema = new Cinema("Test Cinema", "123 Test St", 2);
        
        Session session1 = new Session("Same Title", LocalDateTime.now().plusDays(1), 100, 120.0);
        Session session2 = new Session("Same Title", LocalDateTime.now().plusDays(2), 100, 120.0);
        
        cinema.addSession(session1);
        cinema.addSession(session2);
        
        Path titleSortPath = tempDir.resolve("same-title-sort.json");
        exportImportService.exportData(cinema, titleSortPath.toString(), ExportImportService.SORT_BY_TITLE);
        
        cinema = new Cinema("Test Cinema", "123 Test St", 2);
        LocalDateTime sameDate = LocalDateTime.now().plusDays(1);
        
        session1 = new Session("A Movie", sameDate, 100, 120.0);
        session2 = new Session("B Movie", sameDate, 100, 120.0);
        
        cinema.addSession(session1);
        cinema.addSession(session2);
        
        Path dateSortPath = tempDir.resolve("same-date-sort.json");
        exportImportService.exportData(cinema, dateSortPath.toString(), ExportImportService.SORT_BY_DATE);
        
        assertTrue(titleSortPath.toFile().exists());
        assertTrue(dateSortPath.toFile().exists());
    }
    
    @Test
    void testImportCorruptedJson() throws IOException {
        Path corruptedPath = tempDir.resolve("corrupted.json");
        try (java.io.PrintWriter writer = new java.io.PrintWriter(corruptedPath.toFile())) {
            writer.println("{\"name\": \"Corrupted Cinema\", \"address\": \"Invalid JSON");
        }
        
        assertThrows(IOException.class, () -> exportImportService.importData(corruptedPath.toString()));
    }
    
    @Test
    void testExportWithBorderlineSessions() throws IOException {
        Cinema cinema = new Cinema("Edge Case Cinema", "123 Edge St", 3);
        
        Session pastSession = new Session("Past Movie", LocalDateTime.now().minusYears(10), 100, 120.0);
        Session currentSession = new Session("Current Movie", LocalDateTime.now(), 100, 120.0);
        Session futureSession = new Session("Future Movie", LocalDateTime.now().plusYears(10), 100, 120.0);
        
        cinema.addSession(pastSession);
        cinema.addSession(currentSession);
        cinema.addSession(futureSession);
        
        Path filePath = tempDir.resolve("extreme-dates.json");
        exportImportService.exportData(cinema, filePath.toString(), ExportImportService.NO_SORT);
        
        Cinema importedCinema = exportImportService.importData(filePath.toString());
        
        assertEquals(3, importedCinema.getSessions().size());
        
        boolean foundPast = false;
        boolean foundFuture = false;
        
        for (Session session : importedCinema.getSessions()) {
            if (session.getMovieTitle().equals("Past Movie") && 
                session.getDateTime().isBefore(LocalDateTime.now().minusYears(9))) {
                foundPast = true;
            }
            if (session.getMovieTitle().equals("Future Movie") && 
                session.getDateTime().isAfter(LocalDateTime.now().plusYears(9))) {
                foundFuture = true;
            }
        }
        
        assertTrue(foundPast, "Past session date was not preserved");
        assertTrue(foundFuture, "Future session date was not preserved");
    }
    
    @Test
    void testExportWithSpecialCharacters() throws IOException {
        Cinema cinema = new Cinema("Special \"Cinema\"", "123 Special St 'with quotes'", 1);
        
        Session session = new Session("Movie with \"quotes\" and symbols !@#$%^&*()", 
                                     LocalDateTime.now().plusDays(1), 100, 120.0);
        cinema.addSession(session);
        
        Path filePath = tempDir.resolve("special-chars.json");
        exportImportService.exportData(cinema, filePath.toString(), ExportImportService.NO_SORT);
        
        Cinema importedCinema = exportImportService.importData(filePath.toString());
        
        assertEquals("Special \"Cinema\"", importedCinema.getName());
        assertEquals(1, importedCinema.getSessions().size());
        assertEquals("Movie with \"quotes\" and symbols !@#$%^&*()", 
                    importedCinema.getSessions().get(0).getMovieTitle());
    }
}
