package edu.jsu.mcis.cs310;

import com.github.cliftonlabs.json_simple.*;
import com.opencsv.*;

public class Converter {
    
    /*
        
        Consider the following CSV data, a portion of a database of episodes of
        the classic "Star Trek" television series:
        
        "ProdNum","Title","Season","Episode","Stardate","OriginalAirdate","RemasteredAirdate"
        "6149-02","Where No Man Has Gone Before","1","01","1312.4 - 1313.8","9/22/1966","1/20/2007"
        "6149-03","The Corbomite Maneuver","1","02","1512.2 - 1514.1","11/10/1966","12/9/2006"
        
        (For brevity, only the header row plus the first two episodes are shown
        in this sample.)
    
        The corresponding JSON data would be similar to the following; tabs and
        other whitespace have been added for clarity.  Note the curly braces,
        square brackets, and double-quotes!  These indicate which values should
        be encoded as strings and which values should be encoded as integers, as
        well as the overall structure of the data:
        
        {
            "ProdNums": [
                "6149-02",
                "6149-03"
            ],
            "ColHeadings": [
                "ProdNum",
                "Title",
                "Season",
                "Episode",
                "Stardate",
                "OriginalAirdate",
                "RemasteredAirdate"
            ],
            "Data": [
                [
                    "Where No Man Has Gone Before",
                    1,
                    1,
                    "1312.4 - 1313.8",
                    "9/22/1966",
                    "1/20/2007"
                ],
                [
                    "The Corbomite Maneuver",
                    1,
                    2,
                    "1512.2 - 1514.1",
                    "11/10/1966",
                    "12/9/2006"
                ]
            ]
        }
        
        Your task for this program is to complete the two conversion methods in
        this class, "csvToJson()" and "jsonToCsv()", so that the CSV data shown
        above can be converted to JSON format, and vice-versa.  Both methods
        should return the converted data as strings, but the strings do not need
        to include the newlines and whitespace shown in the examples; again,
        this whitespace has been added only for clarity.
        
        NOTE: YOU SHOULD NOT WRITE ANY CODE WHICH MANUALLY COMPOSES THE OUTPUT
        STRINGS!!!  Leave ALL string conversion to the two data conversion
        libraries we have discussed, OpenCSV and json-simple.  See the "Data
        Exchange" lecture notes for more details, including examples.
        
    */
    
    @SuppressWarnings("unchecked")
    public static String csvToJson(String csvString) {
        
        String result = "{}"; // default return value; replace later!
        
        try {
        
         CSVReader reader = new CSVReaderBuilder(
            new java.io.StringReader(csvString)
        ).build();

        java.util.List<String[]> rows = reader.readAll();

        String[] headers = rows.get(0);

        JsonArray colHeadings = new JsonArray();
        for (String h : headers) {
            colHeadings.add(h);
        }

        JsonArray prodNums = new JsonArray();
        JsonArray data = new JsonArray();

        for (int i = 1; i < rows.size(); i++) {
            String[] row = rows.get(i);

            prodNums.add(row[0]);

            JsonArray dataRow = new JsonArray();
            for (int j = 1; j < row.length; j++) {
                String value = row[j];
                if (value.matches("\\d+")) {
                    dataRow.add(Integer.parseInt(value));
                }
                else {
                    dataRow.add(value);
                }
            }
            data.add(dataRow);
        }

        JsonObject root = new JsonObject();
        root.put("ProdNums", prodNums);
        root.put("ColHeadings", colHeadings);
        root.put("Data", data);

        result = root.toJson();
   
            
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        return result.trim();
        
    }
    
    @SuppressWarnings("unchecked")
    public static String jsonToCsv(String jsonString) {

    String result = "";

    try {
        JsonObject root = (JsonObject) Jsoner.deserialize(jsonString);

        JsonArray colHeadings = (JsonArray) root.get("ColHeadings");
        JsonArray prodNums = (JsonArray) root.get("ProdNums");
        JsonArray data = (JsonArray) root.get("Data");

        java.io.StringWriter writer = new java.io.StringWriter();
        CSVWriter csvWriter = new CSVWriter(writer);

        // Header row
        String[] header = new String[colHeadings.size()];
        for (int i = 0; i < colHeadings.size(); i++) {
            header[i] = colHeadings.get(i).toString().replaceAll("^\"|\"$", "");
        }
        csvWriter.writeNext(header);

        // Find which index is "Episode"
        int episodeIndex = -1;
        for (int i = 0; i < colHeadings.size(); i++) {
            if (colHeadings.get(i).toString().replaceAll("^\"|\"$", "").equalsIgnoreCase("Episode")) {
                episodeIndex = i;
                break;
            }
        }

        // Data rows
        for (int i = 0; i < data.size(); i++) {
            JsonArray dataRow = (JsonArray) data.get(i);
            String[] row = new String[dataRow.size() + 1];

            // First column is ProdNum
            row[0] = prodNums.get(i).toString().replaceAll("^\"|\"$", "");

            for (int j = 0; j < dataRow.size(); j++) {
                String value = dataRow.get(j).toString().replaceAll("^\"|\"$", "");

                // Pad episode if this column is "Episode" and numeric
                if (j == episodeIndex - 1 && value.matches("\\d+")) { 
                    // -1 because prodNums occupies row[0]
                    value = String.format("%02d", Integer.parseInt(value));
                }

                row[j + 1] = value;
            }

            csvWriter.writeNext(row);
        }

        csvWriter.close();
        result = writer.toString();

    } catch (Exception e) {
        e.printStackTrace();
    }

    return result.trim();
}
    
}
