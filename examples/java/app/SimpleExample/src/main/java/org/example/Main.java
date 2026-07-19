package org.example;

import org.w21parser.W21Exception;
import org.w21parser.W21ParserLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.w21parser.W21ParserLoader.W21OutputJsonType.BYTE_ARRAY;
import static org.w21parser.W21ParserLoader.W21OutputType.BSON_BYTE_ARRAY;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    public static void main(String[] args)  {
        System.out.println("Welcome to Vortex21 0.1.0 beta version tester");

        if (args.length > 1)
            System.out.println("Too many arguments\n");
        else if (args.length > 0) {

            String file = args[0];
            W21ParserLoader parser = null;

            try {
                parser = W21ParserLoader.begin()
                        .withInputWitsmlStrict()    // Entering restrict mode (Energistics standards)
                        .withInputRulesValidator()  // Enable input validator (Regex ...)
                        .withIgnoreInputWitsmlNS()  // Ignore Namespaces checks
                        .withResourceStats()        // Load resources statistics for Grafana, Prometheus ...
                        .build();

                System.out.println("Opening file " + file + " in AUTODETECT MODE ...\n");
                parser.readFromFile(file);

                System.out.println("Detected object: " + parser.getInputObjectName());

                byte[] bsonObject = (byte[]) parser.parse(BSON_BYTE_ARRAY); // BSON file for transport / dump in MongoDB
                byte[] jsonStringAsUtf8 = (byte[])parser.parseJson(BYTE_ARRAY); // Json string as byte array (encoded UTF-8)

                System.out.println(parser.loadStatistics());

                saveToFile(bsonObject, file + ".bson");
                saveToFile(jsonStringAsUtf8, file + ".json");

            } catch (W21Exception e) {
                System.out.printf("WITSML 2.1 rule error for file " + file + ". Ignoring parsing\n");
                printW21ExceptionStatus(e);
            } catch (IOException e) {
                System.out.printf("File " + file + " IO error: " + e.getMessage() + ". Ignoring ...\n");
            } catch (Exception e) {
                System.out.println("Unexpected error " + e.getMessage());
            } finally {
                if (parser != null) {
                    int err = parser.close();
                    System.out.println("Vortex21 instance closed successfully with status code " + err + "\n");
                }
            }
        } else
            System.out.println("Select a valid WITSML 2.1 XML document file\n");
    }

    private static void printW21ExceptionStatus(W21Exception e) {
        System.out.println("======================================== DOCUMENT ERROR ========================================\n");
        System.out.println("MESSAGE: " + e.getMessage() + "\n");
        System.out.println("FAULT STRING: " + e.getFaultstring() + "\n");
        System.out.println("XML FAULT STRING: " + e.getXMLfaultdetail() + "\n");
        System.out.println("================================================================================================\n");
    }

    private static void saveToFile(byte[] input, String filename) throws IOException {
        File outputFile = new File(filename);
        System.out.println("Saving file " + filename + " ...\n");
        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            outputStream.write(input);
        }
        System.out.println("Saved: " + filename);
    }
}