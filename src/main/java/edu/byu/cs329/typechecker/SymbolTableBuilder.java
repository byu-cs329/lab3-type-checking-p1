package edu.byu.cs329.typechecker;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SymbolTableBuilder {
  static final Logger log = LoggerFactory.getLogger(SymbolTableBuilder.class);

  class Visitor extends ASTVisitor {
    Map<String, String> typeMap = new HashMap<String, String>();
    Map<String, List<SimpleImmutableEntry<String, String>>> parameterTypeMap = 
        new HashMap<String, List<SimpleImmutableEntry<String, String>>>();
    String className = null;

    @Override
    public boolean visit(CompilationUnit node) {
      @SuppressWarnings("unchecked")
      List<ImportDeclaration> imports = (List<ImportDeclaration>) (node.imports());
      if (imports.size() > 0) {
        Utils.throwRuntimeException("no imports are allowed in the CompilationUnit");
      }

      @SuppressWarnings("unchecked")
      List<TypeDeclaration> types = (List<TypeDeclaration>) (node.types());
      if (types.size() > 1) {
        Utils.throwRuntimeException("only one type declaration allowed in the CompilationUnit");
      }
      return true;
    }

    @Override
    public boolean visit(TypeDeclaration node) {
      checkModifiers(node.getModifiers());
      className = Utils.getName(node);
      if (node.getTypes().length != 0) {
        Utils.throwRuntimeException("no type declarations allowed in " + className);
      }

      for (FieldDeclaration field : Arrays.asList(node.getFields())) {
        field.accept(Visitor.this);
      }

      for (MethodDeclaration method : Arrays.asList(node.getMethods())) {
        method.accept(Visitor.this);
      }
      return false;
    }

    @Override 
    public boolean visit(MethodDeclaration node) {
      checkModifiers(node.getModifiers());
      String type = Utils.getType(node);
      String methodName = Utils.getName(node);
      String name = Utils.buildName(className, methodName);

      // Not tested
      if (typeMap.containsKey(name)) {
        Utils.throwRuntimeException(name 
            + " already exists in symbol table with type " 
            + typeMap.get(name));
      }

      typeMap.put(name, type);
      List<SimpleImmutableEntry<String, String>> typeList = getParameterTypeList(node.parameters());
      parameterTypeMap.put(name, typeList);
      return false;
    }

    @Override
    public boolean visit(FieldDeclaration node) {
      checkModifiers(node.getModifiers());
      String type = Utils.getType(node);
      String fieldName = Utils.getName(node);
      String name = Utils.buildName(className, fieldName);

      // Not tested
      if (typeMap.containsKey(name)) {
        Utils.throwRuntimeException(name 
            + " already exists in symbol table with type " 
            + typeMap.get(name));
      }
      
      typeMap.put(name, type);
      return false;
    }

    private List<SimpleImmutableEntry<String, String>> getParameterTypeList(Object o) {
      @SuppressWarnings("unchecked")
      List<SingleVariableDeclaration> types = (List<SingleVariableDeclaration>)o;
      List<SimpleImmutableEntry<String, String>> typeList = 
          new ArrayList<SimpleImmutableEntry<String, String>>();
      for (SingleVariableDeclaration declaration : types) {
        String name = Utils.getName(declaration);
        String type = Utils.getType(declaration);
        typeList.add(new SimpleImmutableEntry<String, String>(name, type));
      }
      return typeList;
    }

    private void checkModifiers(int modifiers) {
      int mask = ~(Modifier.PRIVATE | Modifier.PUBLIC | Modifier.PROTECTED);
      if ((modifiers & mask) != 0) {
        Utils.throwRuntimeException(
            "only private, public, and protected are supported as modifiers");
      }
    }

  }

  public SymbolTableBuilder() {
  }

  /**
   * Creates a symbol table for the AST.
   * 
   * @requires node instanceof CompilationUnit
   * @requires node is the AST for a supported program
   * 
   * @param node is a CompilationUnit
   * @return the symbol table for the CompilationUnit
   */
  public ISymbolTable getSymbolTable(ASTNode node) {
    Visitor visitor = new Visitor();
    node.accept(visitor);
    Deque<Map<String, String>> typeMap = new ArrayDeque<Map<String, String>>();
    typeMap.push(visitor.typeMap);
    
    return new ISymbolTable() {    
      @Override
      public String getType(String name) {
        for (Map<String, String> map : typeMap) {
          if (map.containsKey(name)) {
            return map.get(name);
          }
        }
        return TypeCheckTypes.ERROR;
      }

      @Override
      public List<SimpleImmutableEntry<String, String>> getParameterTypeList(String name) {
        if (visitor.parameterTypeMap.containsKey(name)) {
          return visitor.parameterTypeMap.get(name);
        }
        return null;
      }

      @Override
      public void pushScope() {
        typeMap.push(new HashMap<String, String>());
      }

      @Override
      public void popScope() {
        typeMap.pop();
      }

      @Override
      public void addLocal(String name, String type) { 
        String existingType = getType(name);
        if (existingType.equals(TypeCheckTypes.ERROR)) {
          typeMap.peek().put(name, type);
        } else {
          Utils.throwRuntimeException(name 
              + " already exists in symbol table with type " 
              + existingType);
        } 
      } 
    };
  }
}
