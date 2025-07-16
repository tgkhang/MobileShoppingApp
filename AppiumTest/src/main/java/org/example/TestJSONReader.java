package org.example;

import org.example.utils.JSONDataReader;

public class TestJSONReader {
    public static void main(String[] args) {
        try {
            System.out.println("Testing JSON reader...");
            Object[][] data = JSONDataReader.readLoginData("login_data.json");
            System.out.println("Successfully read " + data.length + " rows");
            
            for (Object[] row : data) {
                System.out.println("Email: " + row[0] + ", Password: " + row[1] + 
                                 ", Expected: " + row[2] + ", Description: " + row[3]);
            }
        } catch (Exception e) {
            System.err.println("Error reading JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
