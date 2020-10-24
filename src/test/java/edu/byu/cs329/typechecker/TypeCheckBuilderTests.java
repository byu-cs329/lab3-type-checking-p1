package edu.byu.cs329.typechecker;

import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.ASTNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DisplayName("Tests for TypeCheckerBuilder")
public class TypeCheckBuilderTests {
  static final Logger log = LoggerFactory.getLogger(TypeCheckBuilderTests.class);

  private List<DynamicNode> getTypeChecker(final String fileName) {
    ASTNode compilationUnit = Utils.getASTNodeFor(this, fileName);
    SymbolTableBuilder symbolTableBuilder = new SymbolTableBuilder();
    ISymbolTable symbolTable = symbolTableBuilder.getSymbolTable(compilationUnit);
    TypeCheckBuilder typeCheckerBuilder = new TypeCheckBuilder();
    return typeCheckerBuilder.getTypeChecker(symbolTable, compilationUnit);
  }
 
  @TestFactory
  @DisplayName("Should prove type safe when given empty class")
  Stream<DynamicNode> should_proveTypeSafe_when_givenEmptyClass() {
    String fileName = "typeChecker/should_proveTypeSafe_when_givenEmptyClass.java";
    return getTypeChecker(fileName).stream();
  }

  @TestFactory
  @DisplayName("Should prove type safe when given empty method")
  Stream<DynamicNode> should_proveTypeSafe_when_givenEmptyMethod() {
    String fileName = "typeChecker/should_proveTypeSafe_when_givenEmptyMethod.java";
    return getTypeChecker(fileName).stream();
  }

  @TestFactory
  @DisplayName("Should prove type safe when given empty block")
  Stream<DynamicNode> should_proveTypeSafe_when_givenEmptyBlock() {
    String fileName = "typeChecker/should_proveTypeSafe_when_givenEmptyBlock.java";
    return getTypeChecker(fileName).stream();
  }

  @TestFactory
  @DisplayName("Should prove type safe when given variable declarations no inits")
  Stream<DynamicNode> should_proveTypeSafe_when_givenVariableDeclrationsNoInits() {
    String fileName = "typeChecker/should_proveTypeSafe_when_givenVariableDeclrationsNoInits.java";
    return getTypeChecker(fileName).stream();
  }
}
