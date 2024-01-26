/*
 * Author: Gia Bao Hoang
 * Date: 02/08/2023
 *
 * This file, CalculatorTesting.java, serves as a testing utility for validating the output of
 * the CalculatorClient class. It ensures that the content of two files provided as command-line arguments
 * are identical.
 *
 * This class can be utilized to automatically verify that the client's output matches the expected
 * results, thus assisting in the validation of the remote calculator service's functionality.
 */

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.List;

public class CalculatorTesting {
    /**
     * The entry point for the testing application. Compares two specified files and prints the result.
     *
     * Inputs: File paths of the two files to be compared, provided as command-line arguments.
     * Outputs: Prints a message indicating whether the files are identical or different.
     * Special Cases: If either file is not found or is unreadable, an IOException may be thrown.
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Usage: java CalculatorTesting <file1_path> <file2_path>");
            return;
        }

        if (areFilesIdentical(args[0], args[1])) {
            System.out.println("The files are identical.");
        } else {
            System.out.println("The files are different.");
        }
    }

    /**
     * Checks if the content of two files is identical.
     * Input: file1 representing the path to the first file, file2 representing the path to the second file.
     * Output: Returns true if the files are identical, false otherwise.
     * Special Cases: If either file is not found or is unreadable, an IOException may be thrown.
     */
    public static boolean areFilesIdentical(String file1, String file2) throws IOException {
        List<String> content1 = Files.readAllLines(Paths.get(file1));
        List<String> content2 = Files.readAllLines(Paths.get(file2));

        return content1.equals(content2);
    }
}
