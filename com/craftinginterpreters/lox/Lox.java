package com.craftinginterpreters.lox;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


public class Lox {

  static boolean hadError = false; // Set by the Report function / unset in RunPrompt
 // static private String testFile = "LoxExample"; // Uncomment to run test files (ensure the file is in the JLox directory)

  public static void main(String[] args) throws IOException {
   
    if (args.length > 1) {
      System.out.println("Usage: jlox [script]");
      System.exit(64); 
    } else if (args.length == 1) {
      runFile(args[0]);
    } else {
      runPrompt();
    }
    
    
    // Uncomment to run test files (ensure the file is in the JLox directory)
    /*
    Path currentDirectoryPath = FileSystems.getDefault().getPath("");
    String exampleTestFilePath = currentDirectoryPath.toAbsolutePath().toString() + File.separatorChar + testFile;
    System.out.println(exampleTestFilePath); 
    runFile(exampleTestFilePath);
    */
  }

/* RUNFILE
 * Allows Lox programs to be executed by typing in a path on the command line. 
 */

  private static void runFile(String path) throws IOException {
    byte[] bytes = Files.readAllBytes(Paths.get(path));
    run(new String(bytes, Charset.defaultCharset()));

    // If an error is encountered during 
    if (hadError) System.exit(65);
  }

  /* RUNPROMPT
   * Not 100% certain what this does. Here's from the book: 
   * "Fire up jlox without any arguments, and it drops you into a prompt where you can enter and execute code one line at a time."
   */

  private static void runPrompt() throws IOException {
    InputStreamReader input = new InputStreamReader(System.in);
    BufferedReader reader = new BufferedReader(input);

    for (;;) {
        System.out.print("> ");
        String line = reader.readLine();
        if (line == null) break;
        run (line);
        hadError = false;
    }
  }

  
/* RUN
 * This is the core function that both runFile and runPrompt use. 
 *  ---- Useless at the moment, it just prints out the "tokens"
 */
  private static void run(String source){
    Scanner scanner = new Scanner(source);
    List<Token> tokens = scanner.scanTokens();

    for (Token token : tokens) {
        System.out.println(token);
    }
  }

  /* ERROR and REPORT
   * This is going to be the core of error handling
   * Somehow it will get the line# and some message and print them out
   */
  static void error(int line, String message) {
    report(line, "", message);
  }

  private static void report(int line, String where, String message){
    System.err.println("[line " + line + "] Error" + where + ": " + message);
    hadError = true;
  }

}