# Objective

The objective of this lab is to implement [static type checking](https://en.wikipedia.org/wiki/Type_system#Static_type_checking) for a subset of Java. The type checker generates a set of dynamic tests that represent a type proof for the input program. If all the tests pass, then that is a proof certificate that the input program is static type safe.

As before the implementation will use the [org.eclipse.jdt.core.dom](https://help.eclipse.org/neon/index.jsp?topic=%2Forg.eclipse.jdt.doc.isv%2Freference%2Fapi%2Forg%2Feclipse%2Fjdt%2Fcore%2Fdom%2Fpackage-summary.html) to represent and manipulate Java.  The generated tests for the static type proof are generated with a specialized [ASTVisitor](https://help.eclipse.org/neon/index.jsp?topic=%2Forg.eclipse.jdt.doc.isv%2Freference%2Fapi%2Forg%2Feclipse%2Fjdt%2Fcore%2Fdom%2Fpackage-summary.html). 

The program will take two arguments as input the Java file to prove statically type safe. The program should only apply to a limited subset of Java defined below. If an input file is outside the subset, then it should throw an appropriate run-time exception.

# Reading

Review carefully the [type checking](https://bitbucket.org/byucs329/byu-cs-329-lecture-notes/src/master/type-checking.md) lecture with its companion slides [09-type-checking.ppt](https://bitbucket.org/byucs329/byu-cs-329-lecture-notes/src/master/compilers/09-type-checking.ppt).

# Java Subset

The program should take as input only a subset of Java defined as follows:

  * `short`, `int`, and `boolean` are the only primitive values
  * No generics, lambda-expressions, or anonymous classes
  * No interfaces (inheritance and polymorphism are allowed)
  * No reflection
  * No imports
  * No shift operators: `<<`, `>>`, and `>>>`
  * No binary operators: `^`, `&`, and `|`
  * No `switch`-statements
  * Only the following classes from `java.lang` are recognized

    * Boolean
    * Integer
    * Object
    * Short
    * String
  
See [https://github.com/byu-cs329/lab0-constant-folding](https://github.com/byu-cs329/lab0-constant-folding) for an example of a interesting program that falls  comfortably inside this subset.

# Symbol Table Interface


# Lab Requirements

## Symbol Table

Implement a test framework for the symbol table with an appropriate set of tests. Justify the framework and the tests. Be sure the tests include handling local variable

## Type Proofs

Implement the dynamic test generation for the static type proof for the following language features using a mock for the symbol table. 

  * CompilationUnit
  * Class
  * Method
  * Statement Sequences
  * Variable Declaration
  * Assignment to literals

## Testing your test code?

There should be two sets of tests for this lab:

  1. The dynamically generated tests that prove if a program is statically type safe; and
  2. Tests that test the dynamically generated tests to see if the generation code is correct.

The the type (1) tests are easy. Just run your test generation code on Java input files. Some of those should pass and some of those should fair. The JUnit report makes it clean what the generated tests show.

The type (2) tests are trickier. These tests would need to inspect the dynamic generated tests to see if they have the right structure and content. As such, the dynamic tests would not run. They would be generated and inspected programmatically with JUnit tests that would fail when a generated test was not what was expected.

The `DynamicNode` class provides a `DynamicNode.getDisplayName()` method. The `DynamicContainer` gives access to the stream of nodes (`DynamicContainer.getChilderen()`) and the `DynamicContainer` gives access to the embedded executable (`DynamicTest.getExecutable()`). `Executable.execute()` runs that test. If the test fails, it throws a `AssertionFailedError` or subclass of that.

```java
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
```

The above is more detailed than it should be for the type checker test code, but it shows some of what is possible. The `DynamicContainer` only give access to a stream, but from there it should be possible to roughly test for an expected structure it terms of containers, number of tests, or even number of tests in each container.

## Suggested order of attack:

# What to turn in?

Create a pull request when the lab is done. Submit to Canvas the URL of the repository.

# Rubric

| Item | Point Value | Your Score |
| ------- | ----------- | ---------- |
