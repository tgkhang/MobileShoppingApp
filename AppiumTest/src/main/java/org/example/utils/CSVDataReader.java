package org.example.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CSVDataReader {
    
    public static Object[][] readLoginData(String fileName) {
        List<Object[]> data = new ArrayList<>();
        
        try (InputStream inputStream = CSVDataReader.class.getClassLoader().getResourceAsStream(fileName)) {
            
            if (inputStream == null) {
                throw new RuntimeException("File not found in classpath: " + fileName + 
                    ". Make sure the file is in src/main/resources/");
            }
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                // Skip header line
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                
                String[] values = line.split(",");
                if (values.length >= 4) {
                    String email = values[0].trim();
                    String password = values[1].trim();
                    String expectedResult = values[2].trim();
                    String description = values[3].trim();
                    
                    data.add(new Object[]{email, password, expectedResult, description});
                }
            }
            
            } // Close the inner try block for BufferedReader
            
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to read CSV file: " + fileName, e);
        }
        
        return data.toArray(new Object[0][]);
    }
}
