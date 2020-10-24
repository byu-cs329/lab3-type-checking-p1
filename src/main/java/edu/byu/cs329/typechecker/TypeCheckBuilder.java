package edu.byu.cs329.typechecker;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TypeCheckBuilder {
  static final Logger log = LoggerFactory.getLogger(TypeCheckBuilder.class);

  class Visitor extends ASTVisitor {
    ISymbolTable symbolTable = null;
    String className = null;
    Deque<List<DynamicNode>> typeCheckStack = null;
    Deque<String> typeStack = null;
    int blockCounter = 0;
    int statementCounter = 0;
      
    public Visitor(ISymbolTable symbolTable) {
      this.symbolTable = symbolTable;
      typeCheckStack = new ArrayDeque<>();
      pushTypeCheck(new ArrayList<>());
      typeStack = new ArrayDeque<>();
    }
    
    @Override 
    public boolean visit(CompilationUnit node) {
      String type = TypeCheckTypes.VOID;
      @SuppressWarnings("unchecked")
      List<TypeDeclaration> typeDeclarations = (List<TypeDeclaration>)(node.types());
      for (TypeDeclaration declaration : typeDeclarations) {
        declaration.accept(this);
        type = shouldBeVoid(type, popType());
      }
      
      pushType(type);
      return false;
    }

    @Override
    public boolean visit(TypeDeclaration node) {
      pushTypeCheck(new ArrayList<>());
      className = Utils.getName(node);
    
      String type = TypeCheckTypes.VOID;
      for (MethodDeclaration method : Arrays.asList(node.getMethods())) {
        method.accept(Visitor.this);
        type = shouldBeVoid(type, popType());
      }

      pushType(type);
      return false;
    }
 
    @Override
    public boolean visit(MethodDeclaration node) {
      pushTypeCheck(new ArrayList<>());
      
      String name = Utils.buildName(className, Utils.getName(node));
      
      Map<String, String> typeMap = symbolTable.getParameterTypeMap(name); 
      symbolTable.pushScope();
      for (Map.Entry<String, String> entry : typeMap.entrySet()) {
        symbolTable.addLocal(entry.getKey(), entry.getValue());
      }

      symbolTable.addLocal("this", className);
      String type = symbolTable.getType(name);
      symbolTable.addLocal("return", type);
      
      node.getBody().accept(this);
      type = TypeCheckTypes.VOID;
      type = shouldBeVoid(type, popType());

      pushType(type);
      return false;
    }

    @Override
    public boolean visit(Block node) {
      pushTypeCheck(new ArrayList<>());
      symbolTable.pushScope();
     
      String type = TypeCheckTypes.VOID;
      @SuppressWarnings("unchecked")
      List<Statement> statements = (List<Statement>)node.statements();
      for (Statement statement : statements) {
        statement.accept(this);
        type = shouldBeVoid(type, popType());
      }

      pushType(type);
      return false;
    }

    @Override
    public boolean visit(VariableDeclarationStatement node) {
      pushTypeCheck(new ArrayList<>());
      String name = Utils.getName(node);
      String type = Utils.getType(node);
      symbolTable.addLocal(name, type);
      Utils.getSimpleName(node).accept(this);
      
      type = (TypeCheckTypes.VOID);
      Expression initializer = Utils.getInitializer(node);
      if (initializer != null) {
        String leftType = popType();
        initializer.accept(this);
        String rightType = popType();
        DynamicTest test = generateAssignmentCompatibleTest(leftType, rightType);
        type = shouldBeVoid(type, popType());
        peekTypeCheck().add(test);
      } 
      
      pushType(type);
      return false;
    }

    @Override
    public boolean visit(SimpleName node) {
      pushTypeCheck(new ArrayList<>());
      String name = Utils.getName(node);
      String type = symbolTable.getType(name);
      generatLookupTestAndAddToObligations(name, type);
      pushType(type);
      return false;
    }

    @Override
    public boolean visit(BooleanLiteral node) {
      pushTypeCheck(new ArrayList<>());
      String name = node.toString();
      String type = TypeCheckTypes.BOOL;
      generatLookupTestAndAddToObligations(name, type);
      pushType(type);
      return false;
    }

    @Override
    public boolean visit(NumberLiteral node) {
      pushTypeCheck(new ArrayList<>());
      String name = node.getToken();
      String type = TypeCheckTypes.INT;
      generatLookupTestAndAddToObligations(name, type);
      pushType(type);
      return false;
    }

    @Override
    public boolean visit(NullLiteral node) {
      pushTypeCheck(new ArrayList<>());
      String name = node.toString();
      String type = TypeCheckTypes.NULL;
      generatLookupTestAndAddToObligations(name, type);
      pushType(type);
      return false;
    }

    @Override
    public void endVisit(TypeDeclaration node) {
      String name = "class " + className;
      generateProofAndAddToObligations(name);
    }   
    
    @Override
    public void endVisit(MethodDeclaration node) {
      symbolTable.popScope();
      String name = Utils.buildName("method " + className, Utils.getName(node));
      generateProofAndAddToObligations(name);
    }

    @Override
    public void endVisit(Block node) {
      symbolTable.popScope();
      String name = generateBlockName();
      generateProofAndAddToObligations(name);
    }

    @Override
    public void endVisit(VariableDeclarationStatement node) {
      String name = generateStatementName();
      generateProofAndAddToObligations(name);
    }

    @Override
    public void endVisit(SimpleName node) {
      String name = Utils.getName(node);
      generateProofAndAddToObligations(name);
    }

    @Override
    public void endVisit(BooleanLiteral node) {
      String name = node.toString();
      generateProofAndAddToObligations(name);
    }

    @Override
    public void endVisit(NumberLiteral node) {
      String name = node.getToken();
      generateProofAndAddToObligations(name);
    }

    @Override
    public void endVisit(NullLiteral node) {
      String name = node.toString();
      generateProofAndAddToObligations(name);
    }

    private String shouldBeVoid(String currentType, String newType) {
      if (currentType.equals(TypeCheckTypes.ERROR) || newType.equals(TypeCheckTypes.ERROR)) {
        return TypeCheckTypes.ERROR;
      }

      if (!newType.equals(TypeCheckTypes.VOID)) {
        return TypeCheckTypes.ERROR;
      }

      return newType;
    }

    private void generatLookupTestAndAddToObligations(String name, String type) {
      String displayName = generateLookupDisplayName(name, type);
      DynamicTest test = DynamicTest.dynamicTest(displayName, 
          () -> Assertions.assertNotEquals(TypeCheckTypes.ERROR, type)
      );
      peekTypeCheck().add(test);
    }

    private void generateProofAndAddToObligations(String name) {
      String type = peekType();
      String displayName = generateProvesDisplayName(name, type);
      List<DynamicNode> proofs = popTypeCheck();
      addNoObligationIfEmpty(proofs);
      DynamicContainer proof = DynamicContainer.dynamicContainer(displayName, proofs.stream());
      List<DynamicNode> obligations = peekTypeCheck();
      obligations.add(proof);
    }

    private DynamicTest generateAssignmentCompatibleTest(String leftType, String rightType) {
      String displayName = leftType + " := " + rightType;
      boolean isAssignmentCompatible = TypeCheckTypes.isAssgnmentCompatible(leftType, rightType);
      DynamicTest test = DynamicTest.dynamicTest(displayName, 
          () -> assertTrue(isAssignmentCompatible));
      String type = TypeCheckTypes.VOID;
      if (!isAssignmentCompatible) {
        type = TypeCheckTypes.ERROR;
      }
      pushType(type);
      return test;
    }

    private void addNoObligationIfEmpty(List<DynamicNode> proofs) {
      if (proofs.size() > 0) {
        return;
      }
      proofs.add(generateNoObligation());
    }
    
    private DynamicNode generateNoObligation() {
      return DynamicTest.dynamicTest("true", () -> assertTrue(true));
    }

    private String generateProvesDisplayName(String name, String type) {
      return name + ":" + type;
    }

    private String generateLookupDisplayName(String name, String type) {
      return "E(" + name + ") = " + type;
    }

    private List<DynamicNode> popTypeCheck() {
      return typeCheckStack.pop();
    }
  
    private void pushTypeCheck(List<DynamicNode> proof) {
      typeCheckStack.push(proof);
    }
  
    private List<DynamicNode> peekTypeCheck() {
      return typeCheckStack.peek();
    }
    
    private String popType() {
      return typeStack.pop();
    }
  
    private void pushType(String type) {
      typeStack.push(type);
    }

    private String peekType() {
      return typeStack.peek();
    }

    private String generateBlockName() {
      return "B" + blockCounter++;
    }

    private String generateStatementName() {
      return "S" + statementCounter++;
    }

  }
  
  
  public TypeCheckBuilder() {
  }
  
  /**
   * Returns true if static type safe with the checks.
   * 
   * @param symbolTable the environment for the type checks
   * @param node the ASTNode for the compilation unit
   * @param tests a container to hold the tests
   * @return true iff the compilation is static type safe
   */
  public boolean getTypeChecker(ISymbolTable symbolTable, ASTNode node, List<DynamicNode> tests) {
    Visitor visitor = new Visitor(symbolTable);
    node.accept(visitor);
    tests.addAll(visitor.popTypeCheck());
    return visitor.popType().equals(TypeCheckTypes.VOID);
  }
}