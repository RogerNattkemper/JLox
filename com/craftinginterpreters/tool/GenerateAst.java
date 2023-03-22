package com.craftinginterpreters.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;


    /* GenerateAST
     * Takes in a String, and seems to ....
     */

public class GenerateAst {
    public static void main(String[] args) throws IOException {
        // if (args.length != 1) {
        //     System.err.println("Usage: generate_ast <output directory>");
        //     System.exit(64);
        // }

        // The next line prints out where Java is executing from,
        // i.e. its "current working directory".
        System.out.println("Working Directory = " + System.getProperty("user.dir"));

        // Given the above information, this is the output direcotry
        // we need to be using. I'm hardcoding it for simplicity.
        String outputDir = "com/craftinginterpreters/lox"; //args[0]; 
        defineAst(outputDir, "Expr", Arrays.asList(
            "Binary     : Expr left, Token operator, Expr right",
            "Grouping   : Expr expression", 
            "Literal    : Object value",
            "Unary      : Token operator, Expr right"
        ));
    }

    /* DefineAST
     * 
     */
    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");

        writer.println("package com.craftinginterpreters.lox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName + " {");

        defineVisitor(writer, baseName, types);

        // The AST Classes
        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }

        // The Base Accept() method
        writer.println();
        writer.println("  abstract <R> R accept(Visitor<R> visitor);");

        writer.println("}");
        writer.close();
    }

    // The Separate Visitor Interface Function
    private static void defineVisitor(
        PrintWriter writer,
        String baseName,
        List<String> types) {
            for (String type : types) {
                String typeName = type.split(":")[0].trim();
                writer.println("    R visit" + typeName + baseName + "(" + typeName + " " + baseName.toLowerCase() + ")");
            }

            writer.println("  }");
        }

    /* DefineType
     * This seems to write a class definition given the name inputs
     * I am not sure how it defines the type of the parameters, maybe that's coming up
     */
    private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {
        writer.println("  static class " + className + " extends " + baseName + " {");

        // Constructor writer
        writer.println("     " + className + "(" + fieldList + ") {");

        // Store parameters in fields
        /// Split the string from the first letter to when it finds a ", "
        String[] fields = fieldList.split(", ");

        // I am interpretting this as the same as a foreach
        // So for every field in the Fields
        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println("      this." + name + " = " + name + ";");
        }

        writer.println("    }");

        // Visitor Pattern
        writer.println();
        writer.println("    @Override");
        writer.println("    <R> R accept(Visitor<R> visitor) {");
        writer.println("      return visitor.visit" + className + baseName + "(this);");
        writer.println("    }");

        //Fields
        writer.println();
        for(String field : fields) {
            writer.println("    final " + field + ";");
        }

        writer.println("  }");

    }   

}