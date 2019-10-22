package edu.byu.cs329.typechecker;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO.
 * 
 * @author Eric Mercer
 *
 */
public class Parser {

  static final Logger log = LoggerFactory.getLogger(Parser.class);

  /**
   * Read the file at path and return its contents as a String.
   * 
   * @param path The location of the file to be read.
   * @return The contents of the file as a String.
   */
  private static String readFile(final URI path) {
    try {
      return String.join("\n", Files.readAllLines(Paths.get(path)));
    } catch (IOException ioe) {
      log.error(ioe.getMessage());
    }
    return "";
  }

  /**
   * Parse the given source.
   * 
   * @param sourceString The contents of some set of Java files.
   * @return An ASTNode representing the entire program.
   */
  private static ASTNode parse(final String sourceString) {
    ASTParser parser = ASTParser.newParser(AST.JLS3);
    parser.setKind(ASTParser.K_COMPILATION_UNIT);
    parser.setSource(sourceString.toCharArray());
    Map<?, ?> options = JavaCore.getOptions();
    JavaCore.setComplianceOptions(JavaCore.VERSION_1_4, options);
    parser.setCompilerOptions(options);
    return parser.createAST(null);
  }

  /**
   * Performs constant folding.
   * 
   * @param file URI to the input Java file
   * @return the root ASTNode for the constant folded version of the input
   */
  public static ASTNode parse(URI file) {

    String inputFileAsString = readFile(file);
    ASTNode node = parse(inputFileAsString);


    return node;
  }

}
