package com.example.testgame.helper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateFormatterHelper {

    public String formatDate(){
        // Get the current date and time
        LocalDateTime now = LocalDateTime.now();

        // Extract the date part from LocalDateTime
        LocalDate date = now.toLocalDate();

        // Define a custom date format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Format the LocalDate object as a string
        return date.format(formatter);

    }
}
