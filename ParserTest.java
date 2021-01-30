import java.io.*;
import minipython.lexer.Lexer;
import minipython.parser.Parser;
import minipython.node.*;
import java.util.*;

public class ParserTest {
  public static void main(String[] args) {
    try {
      Parser parser =
        new Parser(
        new Lexer(
        new PushbackReader(
        new FileReader(args[0].toString()), 1024)));

      Hashtable symtable =  new Hashtable();
      Start ast = parser.parse();
      Visitor v1 = new Visitor(symtable);
      System.out.println("Starting scan #1. . .");
      ast.apply(v1);
      System.out.println("Scan #1 Completed. Total errors found = "+v1.errors);
      if(v1.errors == 0) {
        System.out.println("Starting scan #2. . .");
        Visitor2 v2 = new Visitor2(symtable);
        ast.apply(v2);
        System.out.println("Scan #2 Completed. Total errors found = "+v2.errors);
      }
    }catch (Exception e) {
        System.err.println(e);
    }
  }
}