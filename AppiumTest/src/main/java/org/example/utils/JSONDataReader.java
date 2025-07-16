package org.example.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class JSONDataReader {
    
    public static Object[][] readLoginData(String fileName) {
        List<Object[]> data = new ArrayList<>();
        
        try (InputStream inputStream = JSONDataReader.class.getClassLoader().getResourceAsStream(fileName)) {
            
            if (inputStream == null) {
                throw new RuntimeException("File not found in classpath: " + fileName + 
                    ". Make sure the file is in src/main/resources/");
            }
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                StringBuilder jsonContent = new StringBuilder();
                String line;
                
                // Read entire JSON file
                while ((line = reader.readLine()) != null) {
                    jsonContent.append(line.trim());
                }
                
                // Parse JSON manually (simple approach without external libraries)
                String json = jsonContent.toString();
                System.out.println("Read JSON content: " + json.substring(0, Math.min(100, json.length())) + "...");
                
                // Extract the loginTestData array
                int startIndex = json.indexOf("\"loginTestData\":");
                if (startIndex == -1) {
                    throw new RuntimeException("loginTestData not found in JSON");
                }
                
                int arrayStart = json.indexOf("[", startIndex);
                int arrayEnd = findMatchingBracket(json, arrayStart);
                String arrayContent = json.substring(arrayStart + 1, arrayEnd);
                
                // Parse objects more carefully
                List<String> objects = parseObjects(arrayContent);
                
                for (String obj : objects) {
                    // Parse individual fields
                    String email = extractJsonValue(obj, "email");
                    String password = extractJsonValue(obj, "password");
                    String expectedResult = extractJsonValue(obj, "expectedResult");
                    String description = extractJsonValue(obj, "description");
                    
                    data.add(new Object[]{email, password, expectedResult, description});
                }
                
            } // Close the inner try block for BufferedReader
            
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to read JSON file: " + fileName, e);
        }
        
        return data.toArray(new Object[0][]);
    }
    
    private static int findMatchingBracket(String json, int openIndex) {
        int count = 0;
        for (int i = openIndex; i < json.length(); i++) {
            if (json.charAt(i) == '[') count++;
            else if (json.charAt(i) == ']') {
                count--;
                if (count == 0) return i;
            }
        }
        return json.length() - 1;
    }
    
    private static List<String> parseObjects(String arrayContent) {
        List<String> objects = new ArrayList<>();
        int braceCount = 0;
        int start = 0;
        
        for (int i = 0; i < arrayContent.length(); i++) {
            char c = arrayContent.charAt(i);
            if (c == '{') {
                if (braceCount == 0) start = i;
                braceCount++;
            } else if (c == '}') {
                braceCount--;
                if (braceCount == 0) {
                    objects.add(arrayContent.substring(start + 1, i));
                }
            }
        }
        
        return objects;
    }
    
    private static String extractJsonValue(String jsonObject, String key) {
        String searchKey = "\"" + key + "\"";
        int keyIndex = jsonObject.indexOf(searchKey);
        if (keyIndex == -1) {
            return "";
        }
        
        int colonIndex = jsonObject.indexOf(":", keyIndex);
        if (colonIndex == -1) return "";
        
        // Skip whitespace after colon
        int valueStart = colonIndex + 1;
        while (valueStart < jsonObject.length() && Character.isWhitespace(jsonObject.charAt(valueStart))) {
            valueStart++;
        }
        
        if (valueStart >= jsonObject.length() || jsonObject.charAt(valueStart) != '"') {
            return "";
        }
        
        valueStart++; // Skip opening quote
        int valueEnd = jsonObject.indexOf("\"", valueStart);
        
        if (valueEnd == -1) {
            return "";
        }
        
        return jsonObject.substring(valueStart, valueEnd);
    }
}
