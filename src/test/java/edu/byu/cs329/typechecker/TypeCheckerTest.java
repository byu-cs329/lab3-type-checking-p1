package edu.byu.cs329.typechecker;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.opentest4j.AssertionFailedError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TypeCheckerTest {
  static final Logger log = LoggerFactory.getLogger(TypeCheckerTest.class);
  
  @TestFactory
  Collection<DynamicNode> dynamicTestsFromCollection() {
    return Arrays.asList(
        DynamicTest.dynamicTest("1st dynamic test", () -> Assertions.assertTrue(true)),
        DynamicTest.dynamicTest("2nd dynamic test", () -> Assertions.assertEquals(4, 2))
    );
  }
  
  @Test
  void myTest() {
    Collection<DynamicNode> tests = dynamicTestsFromCollection();
    Iterator<DynamicNode> iter = tests.iterator();
    DynamicNode n = iter.next();
    Assertions.assertTrue(n instanceof DynamicTest);
    DynamicTest t = (DynamicTest) n;
    Assertions.assertDoesNotThrow(t.getExecutable());
    n = iter.next();
    Assertions.assertTrue(n instanceof DynamicTest);
    t = (DynamicTest) n;
    Assertions.assertThrows(AssertionFailedError.class, t.getExecutable());
  }
}
