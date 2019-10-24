package edu.byu.cs329.typechecker;

import java.util.List;

public interface ISymbolTable {

  public static final String SHORT = "short";
  public static final String SHORT_OBJECT = "Short";
  public static final String INT = "int";
  public static final String INT_OBJECT = "Integer";
  public static final String BOOL = "boolean";
  public static final String BOOL_OBJECT = "Boolean";
  public static final String STRING_OBJECT = "String";
  public static final String OBJECT = "Object";
  public static final String NO_TYPE = "";

  public String getNumberLiteralType(String token);
  
  public String getFieldType(String className, String fieldName);

  public String getMethodReturnType(String className, String methodName);

  public String getParameterType(String className, String methodName,
      String paramName);

  public String getLocalType(String name); 

  public boolean classExists(String className);

  public boolean fieldExists(String className, String fieldName);

  public boolean methodExists(String className, String methodName);

  public boolean parameterExists(String className, String methodName,
      String paramName);
  
  public boolean localExists(String name);
  
  public boolean addLocal(String name, String type);
  
  public void removeLocals(List<String> locals);
}
