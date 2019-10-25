package edu.byu.cs329.typechecker;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.core.dom.ASTNode;
import org.junit.jupiter.api.DynamicNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TypeCheckerTest {
  static final Logger log = LoggerFactory.getLogger(TypeCheckerTest.class);

  private static URI getURI(final String fileName) {
    URL url = ClassLoader.getSystemResource(fileName);
    Objects.requireNonNull(url, "\'" + fileName + "\'" + " not found in classpath");
    URI uri = null;
    try {
      uri = url.toURI();
    } catch (URISyntaxException e) {
      log.error("Failed to get URI for" + fileName);
      e.printStackTrace();
    }
    return uri;
  }

  private List<DynamicNode> generateProof(final String fileName, ISymbolTable symbolTable) {
    URI fileURI = getURI(fileName);
    ASTNode cu = Parser.parse(fileURI);

    // TODO: instanciate visitor, call cu.accept(visitor), and return proof
    return null;
  }

}
