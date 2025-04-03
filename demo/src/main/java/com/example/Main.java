package com.example;

import com.example.model.Cinema;
import com.example.model.Session;
import com.example.model.Ticket;
import com.example.service.CinemaService;
import com.example.service.ExportImportService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final CinemaService cinemaService = new CinemaService();
    private static final ExportImportService exportImportService = new ExportImportService();
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public static void main(String[] args) {
        populateInitialData();
        
        boolean exit = false;
        while (!exit) {
            printMenu();
            int choice = getIntInput("Enter your choice: ");
            
            switch (choice) {
                case 1:
                    displayAllSessions();
                    break;
                case 2:
                    buyTicket();
                    break;
                case 3:
                    addNewSession();
                    break;
                case 4:
                    removeSession();
                    break;
                case 5:
                    updateSession();
                    break;
                case 6:
                    displayTicketStatistics();
                    break;
                case 7:
                    exportData();
                    break;
                case 8:
                    importData();
                    break;
                case 9:
                    manageTickets();
                    break;
                case 0:
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
        
        System.out.println("Thank you for using Cinema Management System!");
    }
    
    private static void printMenu() {
        System.out.println("\n===== CINEMA MANAGEMENT SYSTEM =====");
        System.out.println("1. Display All Sessions");
        System.out.println("2. Buy Ticket");
        System.out.println("3. Add New Session");
        System.out.println("4. Remove Session");
        System.out.println("5. Update Session");
        System.out.println("6. Display Ticket Statistics");
        System.out.println("7. Export Data");
        System.out.println("8. Import Data");
        System.out.println("9. Manage Tickets");
        System.out.println("0. Exit");
        System.out.println("===================================");
    }
    
    private static void populateInitialData() {
        Cinema cinema = new Cinema("Grand Cinema", "123 Main St", 5);
        cinemaService.setCinema(cinema);
        
        Session session1 = new Session("Inception", LocalDateTime.now().plusDays(1), 100, 150.0);
        Session session2 = new Session("The Matrix", LocalDateTime.now().plusDays(2), 80, 120.0);
        
        cinemaService.addSession(session1);
        cinemaService.addSession(session2);
    }
    
    private static void displayAllSessions() {
        System.out.println("\n===== Available Sessions =====");
        List<Session> sessions = cinemaService.getAllSessions();
        
        if (sessions.isEmpty()) {
            System.out.println("No sessions available.");
            return;
        }
        
        for (int i = 0; i < sessions.size(); i++) {
            Session session = sessions.get(i);
            String status = session.isExpired() ? " [EXPIRED]" : "";
            System.out.printf("%d. %s - %s%s, Available seats: %d, Price: %.2f UAH\n", 
                    i + 1, session.getMovieTitle(), session.getDateTime().format(dateFormatter), 
                    status, session.getAvailableSeats(), session.getTicketPrice());
        }
    }
    
    private static void buyTicket() {
        System.out.println("\n===== Buy Tickets =====");
        List<Session> validSessions = cinemaService.getValidSessions();
        
        if (validSessions.isEmpty()) {
            System.out.println("No valid sessions available for booking.");
            return;
        }
        
        for (int i = 0; i < validSessions.size(); i++) {
            Session session = validSessions.get(i);
            System.out.printf("%d. %s - %s, Available seats: %d, Price: %.2f UAH\n", 
                    i + 1, session.getMovieTitle(), session.getDateTime().format(dateFormatter), 
                    session.getAvailableSeats(), session.getTicketPrice());
        }
        
        int sessionIndex = getIntInput("Select session (enter number): ") - 1;
        if (sessionIndex < 0 || sessionIndex >= validSessions.size()) {
            System.out.println("Invalid session selection.");
            return;
        }
        
        Session selectedSession = validSessions.get(sessionIndex);
        int numberOfTickets = getIntInput("Enter number of tickets to buy: ");
        
        try {
            List<Ticket> tickets = cinemaService.buyTickets(selectedSession, numberOfTickets);
            double totalAmount = tickets.stream().mapToDouble(Ticket::getPrice).sum();
            
            System.out.printf("\nPurchased %d tickets for %s\n", tickets.size(), selectedSession.getMovieTitle());
            System.out.printf("Total amount: %.2f UAH\n", totalAmount);
            System.out.printf("Remaining seats: %d\n", selectedSession.getAvailableSeats());
        } catch (IllegalStateException | IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private static void addNewSession() {
        System.out.println("\n===== Add New Session =====");
        
        String movieTitle = getStringInput("Enter movie title: ");
        
        LocalDateTime dateTime = null;
        while (dateTime == null) {
            try {
                String input = getStringInput("Enter date and time (dd.MM.yyyy HH:mm): ");
                dateTime = LocalDateTime.parse(input, dateFormatter);
                
                if (dateTime.isBefore(LocalDateTime.now())) {
                    System.out.println("Cannot create session in the past. Please enter a future date.");
                    dateTime = null;
                }
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use format dd.MM.yyyy HH:mm");
            }
        }
        
        int totalSeats = 0;
        while (totalSeats <= 0) {
            totalSeats = getIntInput("Enter total number of seats (must be positive): ");
            if (totalSeats <= 0) {
                System.out.println("Total seats must be greater than zero.");
            }
        }
        
        double ticketPrice = 0;
        while (ticketPrice <= 0) {
            ticketPrice = getDoubleInput("Enter ticket price (must be positive): ");
            if (ticketPrice <= 0) {
                System.out.println("Ticket price must be greater than zero.");
            }
        }
        
        try {
            Session newSession = new Session(movieTitle, dateTime, totalSeats, ticketPrice);
            cinemaService.addSession(newSession);
            System.out.println("Session added successfully!");
        } catch (IllegalArgumentException e) {
            System.out.println("Error creating session: " + e.getMessage());
        }
    }
    
    private static void removeSession() {
        displayAllSessions();
        List<Session> sessions = cinemaService.getAllSessions();
        
        if (sessions.isEmpty()) return;
        
        int sessionIndex = getIntInput("Select session to remove (enter number): ") - 1;
        if (sessionIndex < 0 || sessionIndex >= sessions.size()) {
            System.out.println("Invalid session selection.");
            return;
        }
        
        Session sessionToRemove = sessions.get(sessionIndex);
        cinemaService.removeSession(sessionToRemove);
        
        System.out.println("Session removed successfully!");
    }
    
    private static void updateSession() {
        System.out.println("\n===== Update Session =====");
        List<Session> sessions = cinemaService.getAllSessions();
        
        if (sessions.isEmpty()) {
            System.out.println("No sessions available to update.");
            return;
        }
        
        for (int i = 0; i < sessions.size(); i++) {
            Session session = sessions.get(i);
            String status = session.isExpired() ? " [EXPIRED]" : "";
            System.out.printf("%d. %s - %s%s, Available seats: %d, Price: %.2f UAH\n", 
                    i + 1, session.getMovieTitle(), session.getDateTime().format(dateFormatter), 
                    status, session.getAvailableSeats(), session.getTicketPrice());
        }
        
        int sessionIndex = getIntInput("Select session to update (enter number): ") - 1;
        if (sessionIndex < 0 || sessionIndex >= sessions.size()) {
            System.out.println("Invalid session selection.");
            return;
        }
        
        Session sessionToUpdate = sessions.get(sessionIndex);
        System.out.println("\nUpdating session: " + sessionToUpdate.getMovieTitle());
        System.out.println("Leave fields empty to keep current values.");
        
        String newTitle = getStringInputAllowEmpty("Enter new movie title: ");
        
        LocalDateTime newDateTime = null;
        String dateTimeInput = getStringInputAllowEmpty("Enter new date and time (dd.MM.yyyy HH:mm): ");
        if (!dateTimeInput.isEmpty()) {
            try {
                newDateTime = LocalDateTime.parse(dateTimeInput, dateFormatter);
            } catch (Exception e) {
                System.out.println("Invalid date format. Using current date.");
            }
        }
        
        int newTotalSeats = 0;
        String seatsInput = getStringInputAllowEmpty("Enter new total seats: ");
        if (!seatsInput.isEmpty()) {
            try {
                newTotalSeats = Integer.parseInt(seatsInput);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Using current value.");
            }
        }
        
        double newTicketPrice = 0;
        String priceInput = getStringInputAllowEmpty("Enter new ticket price: ");
        if (!priceInput.isEmpty()) {
            try {
                newTicketPrice = Double.parseDouble(priceInput);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Using current value.");
            }
        }
        
        try {
            boolean updated = cinemaService.updateSession(
                sessionToUpdate.getId(), 
                newTitle.isEmpty() ? null : newTitle, 
                newDateTime, 
                newTotalSeats, 
                newTicketPrice
            );
            
            if (updated) {
                System.out.println("Session updated successfully!");
            } else {
                System.out.println("Failed to update session.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private static void displayTicketStatistics() {
        System.out.println("\n===== Ticket Statistics =====");
        List<Session> sessions = cinemaService.getAllSessions();
        
        if (sessions.isEmpty()) {
            System.out.println("No sessions available.");
            return;
        }
        
        for (Session session : sessions) {
            int soldTickets = session.getTotalSeats() - session.getAvailableSeats();
            double totalRevenue = soldTickets * session.getTicketPrice();
            
            System.out.printf("Movie: %s\n", session.getMovieTitle());
            System.out.printf("Date/Time: %s\n", session.getDateTime().format(dateFormatter));
            System.out.printf("Sold tickets: %d/%d\n", soldTickets, session.getTotalSeats());
            System.out.printf("Revenue: %.2f UAH\n", totalRevenue);
            System.out.println("-----------------------------");
        }
        
        double totalRevenue = cinemaService.calculateTotalRevenue();
        System.out.printf("TOTAL REVENUE: %.2f UAH\n", totalRevenue);
    }
    
    private static void exportData() {
        System.out.println("\n===== Export Data =====");
        System.out.println("1. Export in default order");
        System.out.println("2. Sort by movie title");
        System.out.println("3. Sort by date/time");
        System.out.println("4. Sort by available seats");
        int sortOption = getIntInput("Choose sorting option: ");
        
        String filename = getStringInput("Enter filename to export to (will add .json extension): ");
        if (!filename.endsWith(".json")) {
            filename += ".json";
        }
        
        try {
            exportImportService.exportData(cinemaService.getCinema(), filename, sortOption);
            System.out.println("Data exported successfully to " + filename);
        } catch (Exception e) {
            System.out.println("Error exporting data: " + e.getMessage());
        }
    }
    
    private static void importData() {
        System.out.println("\n===== Import Data =====");
        String filename = getStringInput("Enter filename to import from (should have .json extension): ");
        if (!filename.endsWith(".json")) {
            filename += ".json";
        }
        
        try {
            Cinema importedCinema = exportImportService.importData(filename);
            cinemaService.setCinema(importedCinema);
            System.out.println("Data imported successfully from " + filename);
        } catch (Exception e) {
            System.out.println("Error importing data: " + e.getMessage());
        }
    }
    
    private static void manageTickets() {
        System.out.println("\n===== Manage Tickets =====");
        System.out.println("1. View All Tickets");
        System.out.println("2. Delete Ticket");
        System.out.println("3. Change Ticket Session");
        System.out.println("0. Back to Main Menu");
        
        int choice = getIntInput("Enter your choice: ");
        
        switch (choice) {
            case 1:
                displayAllTickets();
                break;
            case 2:
                deleteTicket();
                break;
            case 3:
                changeTicketSession();
                break;
            case 0:
                return;
            default:
                System.out.println("Invalid choice.");
        }
    }
    
    private static void displayAllTickets() {
        System.out.println("\n===== All Tickets =====");
        List<Session> sessions = cinemaService.getAllSessions();
        boolean hasTickets = false;
        
        for (Session session : sessions) {
            List<Ticket> tickets = session.getTickets();
            if (!tickets.isEmpty()) {
                hasTickets = true;
                System.out.printf("Session: %s - %s\n", 
                        session.getMovieTitle(), 
                        session.getDateTime().format(dateFormatter));
                
                for (int i = 0; i < tickets.size(); i++) {
                    Ticket ticket = tickets.get(i);
                    System.out.printf("  %d. Ticket ID: %s, Price: %.2f UAH, Purchased: %s\n", 
                            i + 1, 
                            ticket.getId(), 
                            ticket.getPrice(),
                            ticket.getPurchaseTime().format(dateFormatter));
                }
                System.out.println();
            }
        }
        
        if (!hasTickets) {
            System.out.println("No tickets have been purchased yet.");
        }
    }
    
    private static void deleteTicket() {
        System.out.println("\n===== Delete Ticket =====");
        List<Session> sessions = cinemaService.getAllSessions();
        boolean hasTickets = false;
        
        List<Ticket> allTickets = new ArrayList<>();
        
        for (Session session : sessions) {
            List<Ticket> tickets = session.getTickets();
            if (!tickets.isEmpty()) {
                hasTickets = true;
                allTickets.addAll(tickets);
                
                System.out.printf("Session: %s - %s\n", 
                        session.getMovieTitle(), 
                        session.getDateTime().format(dateFormatter));
                
                for (int i = 0; i < tickets.size(); i++) {
                    Ticket ticket = tickets.get(i);
                    System.out.printf("  %d. Ticket ID: %s, Price: %.2f UAH, Purchased: %s\n", 
                            allTickets.size() - tickets.size() + i + 1, 
                            ticket.getId(), 
                            ticket.getPrice(),
                            ticket.getPurchaseTime().format(dateFormatter));
                }
            }
        }
        
        if (!hasTickets) {
            System.out.println("No tickets available to delete.");
            return;
        }
        
        int ticketIndex = getIntInput("Select ticket to delete (enter number): ") - 1;
        if (ticketIndex < 0 || ticketIndex >= allTickets.size()) {
            System.out.println("Invalid ticket selection.");
            return;
        }
        
        Ticket selectedTicket = allTickets.get(ticketIndex);
        boolean deleted = cinemaService.deleteTicket(selectedTicket.getId());
        
        if (deleted) {
            System.out.println("Ticket deleted successfully!");
        } else {
            System.out.println("Failed to delete ticket.");
        }
    }
    
    private static void changeTicketSession() {
        System.out.println("\n===== Change Ticket Session =====");
        
        List<Session> sessions = cinemaService.getAllSessions();
        boolean hasTickets = false;
        
        List<Ticket> allTickets = new ArrayList<>();
        
        for (Session session : sessions) {
            List<Ticket> tickets = session.getTickets();
            if (!tickets.isEmpty()) {
                hasTickets = true;
                allTickets.addAll(tickets);
                
                System.out.printf("Session: %s - %s\n", 
                        session.getMovieTitle(), 
                        session.getDateTime().format(dateFormatter));
                
                for (int i = 0; i < tickets.size(); i++) {
                    Ticket ticket = tickets.get(i);
                    System.out.printf("  %d. Ticket ID: %s, Price: %.2f UAH, Purchased: %s\n", 
                            allTickets.size() - tickets.size() + i + 1, 
                            ticket.getId(), 
                            ticket.getPrice(),
                            ticket.getPurchaseTime().format(dateFormatter));
                }
            }
        }
        
        if (!hasTickets) {
            System.out.println("No tickets available to change.");
            return;
        }
        
        int ticketIndex = getIntInput("Select ticket to change (enter number): ") - 1;
        if (ticketIndex < 0 || ticketIndex >= allTickets.size()) {
            System.out.println("Invalid ticket selection.");
            return;
        }
        
        Ticket selectedTicket = allTickets.get(ticketIndex);
        
        List<Session> validSessions = cinemaService.getValidSessions();
        System.out.println("\nSelect new session for the ticket:");
        
        for (int i = 0; i < validSessions.size(); i++) {
            Session session = validSessions.get(i);
            System.out.printf("%d. %s - %s, Available seats: %d, Price: %.2f UAH\n", 
                    i + 1, session.getMovieTitle(), session.getDateTime().format(dateFormatter), 
                    session.getAvailableSeats(), session.getTicketPrice());
        }
        
        int sessionIndex = getIntInput("Select new session (enter number): ") - 1;
        if (sessionIndex < 0 || sessionIndex >= validSessions.size()) {
            System.out.println("Invalid session selection.");
            return;
        }
        
        Session newSession = validSessions.get(sessionIndex);
        
        try {
            boolean updated = cinemaService.updateTicketSession(selectedTicket.getId(), newSession.getId());
            
            if (updated) {
                System.out.println("Ticket moved to new session successfully!");
            } else {
                System.out.println("Failed to change ticket session.");
            }
        } catch (IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private static int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
    
    private static double getDoubleInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
    
    private static String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }
    
    private static LocalDateTime getDateTimeInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine();
                return LocalDateTime.parse(input, dateFormatter);
            } catch (DateTimeParseException e) {
                System.out.println("Please enter date in format dd.MM.yyyy HH:mm");
            }
        }
    }
    
    private static String getStringInputAllowEmpty(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }
}