package edu.byu.cs329.typechecker;

import java.util.Deque;

public interface ISymbolTable {

  public static final String SHORT = "short";
  public static final String SHORT_OBJECT = "Short";
  public static final String INT = "int";
  public static final String INT_OBJECT = "Integer";
  public static final String BOOL = "boolean";
  public static final String BOOL_OBJECT = "Boolean";
  public static final String STRING_OBJECT = "String";
  public static final String OBJECT = "Object";

  public String getFieldType(Deque<String> className, String fieldName);

  public String getMethodReturnType(Deque<String> className, String methodName);

  public String getParameterType(Deque<String> className, String methodName,
      String paramName);

  public String getLocalType(String name); 

  public boolean classExists(Deque<String> className);

  public boolean fieldExists(Deque<String> className, String fieldName);

  public boolean methodExists(Deque<String> className, String methodName);

  public boolean parameterExists(Deque<String> className, String methodName,
      String paramName);
  
  public boolean localExists(String name);
  
  public ISymbolTable addLocal(String name, String type);
}
