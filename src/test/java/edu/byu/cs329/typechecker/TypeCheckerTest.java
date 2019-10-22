package edu.byu.cs329.typechecker;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

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
