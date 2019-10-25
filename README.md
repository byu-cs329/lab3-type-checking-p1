# Objective

The objective of this lab is to implement [static type checking](https://en.wikipedia.org/wiki/Type_system#Static_type_checking) for a subset of Java. The type checker generates a set of dynamic tests that represent a type proof for the input program. If all the tests pass, then that is a proof certificate that the input program is static type safe.

As before the implementation will use the [org.eclipse.jdt.core.dom](https://help.eclipse.org/neon/index.jsp?topic=%2Forg.eclipse.jdt.doc.isv%2Freference%2Fapi%2Forg%2Feclipse%2Fjdt%2Fcore%2Fdom%2Fpackage-summary.html) to represent and manipulate Java.  The generated tests for the static type proof are generated with a specialized [ASTVisitor](https://help.eclipse.org/neon/index.jsp?topic=%2Forg.eclipse.jdt.doc.isv%2Freference%2Fapi%2Forg%2Feclipse%2Fjdt%2Fcore%2Fdom%2Fpackage-summary.html). 

The program will take no arguments as input and can only be invoked through the tests. The program should only apply to a limited subset of Java defined below. If an input file is outside the subset, then it should throw an appropriate run-time exception.

# Reading

Review carefully the [type checking](https://bitbucket.org/byucs329/byu-cs-329-lecture-notes/src/master/type-checking.md) lecture with its companion slides [09-type-checking.ppt](https://bitbucket.org/byucs329/byu-cs-329-lecture-notes/src/master/compilers/09-type-checking.ppt). **The notes have a lot of implementation details that are worth carefully reading.** It may even be a good idea to use those notes as the starting point for the implementation. It lays out a progression and makes some suggestions on the architecture that are worth considering; regardless, do not start coding until the notes are fully understood.

# Java Subset

The input programs to type check are restricted a subset of Java defined as follows:

  * `short`, `int`, and `boolean` are the only primitive values
  * No generics, lambda-expressions, or anonymous classes
  * No interfaces or inheritance (you do not need to track a type hierarchy in this part of the lab)
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

  * No variable shadowing
  
See [https://github.com/byu-cs329/lab0-constant-folding](https://github.com/byu-cs329/lab0-constant-folding) for an example of a interesting program that falls  comfortably inside this subset.

# Symbol Table Interface

All of the environment is in the symbol table for the type proof. The table itself is mostly static, meaning the information does not change after the table is constructed, except for local variables that are added and removed as the type proof is constructed. Only use the `ISymbolTable` interface in the visitor to construct the type proof. The implementation of the `ISymbolTable` that is a part of this lab may use methods beyond what is in the `ISymbolTable` interface. So building the symbol table, create and use any methods needed. Using the symbol table to construct the type proof, only use the `ISymbolTable` interface and store the symbol table as an instance of the `ISymbolTable` interface.

The symbol table relies on the class name to find fields, methods, and parameters. The type checker, and symbol table, do not support imports, so there will never be more than one input file. The implication is two fold: fully qualified names are not needed (ignore the package declaration) and there will be no more than one top level class in the input file (Java convention). That said, the class may have any number of nested classes. 

Java names nested classes with a path of class names, outermost class to target class, and that path is separated by the `$` sign. Consider the following:

```java
class F {
  class G {
    int y;
    class H {
    }
  }
}
```

The name of for the inner most class by convention is `F$G$H`: ```F$G$H x = new F$G$H()```. The symbol table will associate fields, methods, etc. with that `$`-separated name. As the symbol table is constructed, the class name needs to be tracked, with a stack, and updated as appropriate. Creating the class name from a stack is direct.

```java
className = this.className.stream().collect(Collectors.joining("$"));
```

Something that makes lookup hard is that inner classes may reference fields in outer classes as in an instance of `F$G$H` using `F$G.y` in a method as `y = 10`. When looking up the type for `y`, the lookup needs to first check the local variables for `y`, then the fields of the current class `F$G$H` in this example, and then move up through the nested classes checking for the variable in those fields. As such, when it checks for `F$G.f` it will find the name and return the `int` type. 

The lookup can use the class name stack as in the above and pop names off the stack in doing the look up though the classes (though it will need to first copy the stack to preserve it for later lookups). If the code actually uses the qualified name as in `F$G.y = 10`, then there will be a qualified name in the `ASTNode` along with the simple name `y` (stored in two separate areas). Use the simple name and created the full name from the stack to avoid having to special case; although, it may be that the fully qualified name is always populated as well and can be used. Do whatever is easiest and most clear for the code.

The symbol table, as the environment for the proof, also needs to return types for literals: `String getNumberLiteralType(String token);`. For numbers, always return the narrowest type (least number of bits needed). The Java API throws an exception when a provide string cannot be converted (e.g., `Short.parseShort(token)` will throw an exception if the string token is not a short because it is too big, too small, or uses invalid characters). 

# Type proof

The [lecture notes](https://bitbucket.org/byucs329/byu-cs-329-lecture-notes/src/master/type-checking.md) are somewhat extensive on constructing the type proof and detailing a possible implementation. Take time to really understand the lecture notes (maybe even start coding from the lecture notes) before getting too far into this lab.

The recursion can be confusing, and keeping track of state between different visit methods is confusing as well. Build as simple a mental model as possible. The [lecture notes](https://bitbucket.org/byucs329/byu-cs-329-lecture-notes/src/master/type-checking.md) suggest a stack model where each visit pushes a list on a stack to collect obligations and then pops that list at the end of the visit, builds a container, and adds that container to the obligations on the top of the stack. This model implies that the stack is initialized with a list to hold the final container for the class. The model is simple and effectively maps to a visitor pattern: `visit` pushes while `endVisit` pops and adds to the list on the top of the stack.

# Lab Requirements

It is strongly encouraged to adopt a test driven approach to the lab. Define a test, implement code to pass the test, and then repeat. Take some time at the front-end to plan out the test progression in a sensible way. A test driven approach will make the lab feel more manageable (gives an obvious place to start), and it will help provide an incremental approach to implementing features.

## Symbol Table

Implement the symbol table and a test framework for the symbol table with an appropriate set of tests. Justify the framework and the tests. Be sure the tests include handling local variables. 

## Type Proofs

Implement the dynamic test generation for the static type proof for the following language features using a mock for the symbol table. 

  * `CompilationUnit` (top level file)
  * `TypeDeclaration` (classes)
  * `Method`
  * `Block` (scopes)
  * `EmptyStatement`
  * `VariableDeclarationFragment` with initializers (fields and local variables)
  *  `NumberLiterals`, `StringLiterals`, and `BooleanLiterals`

## Test Code

There should be at least four sets of tests for this lab:

  1. Tests for the symbol table
  2. Tests that test the dynamically generated type proof tests to see if the generation code is correct using---those tests should use mocks or spys with the Mockito Verify interface to check interactions with the symbol table
  3. Tests for the actual type proof (run the dynamically generated tests that prove if a program is statically type safe) 
  4. Integration tests that use both the symbol table implementation (rather than a mock---a spy is fine too you want to verify interactions) and the type proof visitor

The tests should check files that type check and files that do not type check.

Tests of type (1), (3), and (4) are easy. Just run your tests on different Java input files.  

For the dynamically generated tests The JUnit report from Surefire is not super ideal (and efforts to find a better solution not yielded anything better). The Eclipse IDE shows every test and container in a tree structure; its report is super easy to read because it visualizes the tree structure of the type proof. The surefire report is much much less obvious only reporting the name of the test factory method with a path to the failing test. For example, if a test fails in the type proof, the output is something like

```
ERROR ... test Should_proveTypeSafe_When_oneFieldWithInitializer[3][1][1][2] ... failed ...
```

Here `Should_proveTypeSafe_When_oneFieldWithInitializer` is the name of the test factory. The sequence of numbers in the bracket is the path in the tree to the actual failed test. The output is *not* helpful in debugging. Using an IDE for the report is strongly encouraged. Anyways, the array idices in the sequence are one-based (and not zero-based). In this example it refers to the 3rd child, 1st child, 1st child, and 2nd child relative to the base container for the compilation unit. The child at that leaf is the test that failed. In such a path, each index is an index into the list of `DynamicNodes` at the current node in the tree. The node at that index should be a `DynamicContainer` unless it is the last entry in the path in which case it should be a `DynamicTest`.

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

The [lecture notes](https://bitbucket.org/byucs329/byu-cs-329-lecture-notes/src/master/type-checking.md) have a detailed discussion to what these tests might look like for the type checker.

# What to turn in?

Create a pull request when the lab is done. Submit to Canvas the URL of the repository.

# Rubric

| Item | Point Value |
| ------- | ----------- |
| Test framework for symbol table implementation | 30 |
| Symbol table implementation | 30 |
| Tests to tests proof structure | 30 |
| Tests to show outcome of proofs | 30 |
| Integration tests (different from unit tests) | 20 |
| Type proof visitor implementation | 30 | 
| Style, documentation, naming conventions, test organization, readability, etc. | 30 | 
