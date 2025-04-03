package com.example;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

public class MainSessionCreationTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
        System.setIn(originalIn);
    }
    
    @Test
    void testSessionCreationWithInvalidInputs() {
        String simulatedInput = "3\n" +               
                                "Test Movie\n" +       
                                "31-12-2023 12:00\n" + 
                                "31.12.2030 12:00\n" + 
                                "0\n" +                
                                "100\n" +             
                                "0\n" +               
                                "120.5\n" +            
                                "0\n";                 
        
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
        
        try {
            Thread mainThread = new Thread(() -> {
                try {
                    Main.main(new String[]{});
                } catch (Exception e) {
                }
            });
            mainThread.start();
            mainThread.join(3000); 
            
            if (mainThread.isAlive()) {
                mainThread.interrupt();
            }
        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
        
        String output = outContent.toString();
        
        assertTrue(output.contains("Invalid date format") || output.contains("Please enter date in format"),
                "Should detect invalid date format");
        assertTrue(output.contains("Total seats must be greater than zero"), 
                "Should detect zero seats");
        assertTrue(output.contains("Ticket price must be greater than zero"), 
                "Should detect zero price");
        assertTrue(output.contains("Session added successfully"), 
                "Should eventually add session");
    }
}
