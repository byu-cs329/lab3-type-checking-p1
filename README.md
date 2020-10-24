# Objective

The objective of this lab is to implement [static type checking](https://en.wikipedia.org/wiki/Type_system#Static_type_checking) for a subset of Java. The type checker generates a set of dynamic tests that represent a type proof for the input program. If all the tests pass, then that is a proof certificate that the input program is static type safe.

As before, the implementation will use the [org.eclipse.jdt.core.dom](https://help.eclipse.org/neon/index.jsp?topic=%2Forg.eclipse.jdt.doc.isv%2Freference%2Fapi%2Forg%2Feclipse%2Fjdt%2Fcore%2Fdom%2Fpackage-summary.html) to represent and manipulate Java.  The generated tests for the static type proof are generated with a specialized [ASTVisitor](https://help.eclipse.org/neon/index.jsp?topic=%2Forg.eclipse.jdt.doc.isv%2Freference%2Fapi%2Forg%2Feclipse%2Fjdt%2Fcore%2Fdom%2Fpackage-summary.html). 

The program will take no arguments as input and can only be invoked through the tests. The program should only apply to a limited subset of Java defined below. If an input file is outside the subset, then all bets are off.

# Reading

Review carefully the [type checking](https://bitbucket.org/byucs329/byu-cs-329-lecture-notes/src/master/type-checking.md) lecture with its companion slides [09-type-checking.ppt](https://bitbucket.org/byucs329/byu-cs-329-lecture-notes/src/master/compilers/09-type-checking.ppt). **The notes have a lot of implementation details that are worth carefully reading.** It may even be a good idea to use those notes as the starting point for the implementation. It lays out a progression and makes some suggestions on the architecture that are worth considering; regardless, do not start coding until the notes are fully understood.

# Java Subset

A general overview of what is allowed in the subset:

  * No imports and nothing from `java.lang` such as `Integer` except for some limited tests (see existing tests)
  * A `CompilationUnit` has a single `TypeDeclaration` in it's declarations
  * A type-proof for a compilation unit is that all methods are type-correct in the class
  * All `FieldDeclaration` instances have no **initializer** 
  * Names for all entities are unique: no shadowing of any kind
  * Field references are type `FieldAccess` of the form `this.field` 
  * `int`, `boolean`, `NullType` (e.g., `NullLiteral`), and objects are the only types
  * `InfixExpression` instances for operators `+`, `-`, and `*` are always  `int,int:int` (e.g, expecting two `int` types and returning an `int` type)
  * `InfixExpression` instances for operators `&&` and `||` are always `boolean,boolean:boolean`
  * `InfixExpression` instances for operator `<` are always `int,int:boolean`
  * `PrefixExpression` instances for operator `!` are `boolean:boolean`
  * `InfixExpression` instances for operator `==` are always `Object,Object:boolean` where `Object` is an object type or `nullType`, `int,int:boolean`, or `boolean,boolean:boolean`
  * Assignment between objects is like the `==` requiring types to be the same with the added ability te assign objects to `null`.
  * No constructors

The type-checker must eventually prove the following:

  * `MethodDeclaration` (provided)
  * `CompilationUnit` (provided)
  * `Block` (provided)
  * `BooleanLiteral` with type `boolean` (provided)
  * `NumberLiteral` with type `int` (provided)
  * `NullLiteral` with type `nullType` (provided)
  * `VariableDeclarationStatement` (provided)
  * `ReturnStatement` (lab 3)
  * `ExpressionStatement` for `Assignment` (lab 3)
  * `PrefixExpression` for `!` (lab 3)
  * `InfixExpression` for `+`, `*`, and `-` with type according to above rules (lab 3)
  * `InfixExpression` for `&&`, `||`, `<`, and `==` with type according to above rules (lab 3)
  * `IfStatement` with type `void` (lab 3)
  * `WhileStatement` with type `void` (lab 3)  
  * `FieldAccess` with the type of the field as it `this.a` (lab 4)
  * `QualifiedName` for `n.a` where `n` is a variable (lab 4)
  * `ExpressionStatement` for `MethodInvocation` (lab 4)

If something seems unusually hard then be sure it is in the [language subset](https://bitbucket.org/byucs329/byu-cs-329-lecture-notes/src/master/java-subset/java-subset.md). 

# Symbol Table Interface

The lab code includes a symbol table. It also checks many of the above properties for the Java subset (see the code).
Only use the `ISymbolTable` interface to construct the type proof. The JavaDocs in `ISymbolTable` define the interface. When in doubt, look at `SymbolTableBuilder`. The symbol table is provided as is. Please report and fix defects.

# Type proof

The [lecture notes](https://bitbucket.org/byucs329/byu-cs-329-lecture-notes/src/master/type-checking.md) are somewhat extensive on constructing the type proof and detailing a possible implementation. Take time to really understand the lecture notes (maybe even start coding from the lecture notes) before getting too far into this lab.

The provided implementation uses two stacks to manage the type checking: `typeStack` and `typeCheckStack`. The first is the return type of the most recent node in the recursion tree for the type check and is the type returned on the edges. The second is the set of obligations that must hold at a node in the recursion true that determine the type to return on the edge. These checks are actual JUnit 5 tests.

The recursion can be confusing, and keeping track of state between different visit methods is confusing as well.  The provided code is one way to do it. In general, a `visit` method is a node in the recursion tree. So it pushes an empty set of checks on the `typeCheckStack` and adds obligations to that set as it progresses. At the end of the visit, it pushes the resulting type for the node in the `typeStack` as the return.

The `endVisit` method pops the set of checks from the `typeCheckStack`, wraps them in a container so the checks for the node are self-contained, and then adds that container to the set of obligations at the top of the stack. It is confusing. I agree. Read and study the provided implementations. There is a pattern for the `visit` and `endVisit` followed by every type. Follow that pattern!

## Utils

The `Utils` class provides a bevy of helper methods in the form of `getName` and `getType` to get the name and type from `ASTNodes`. Always check `Utils` before digging too deep into the `ASTNode` type to find something. More than likely what is needed is already there. There are many examples in both `SymbolTableBuilder` and `TypeCheckBuilder` using `Utils` and working with the different `ASTNode` types for this lab. Be sure to look in these files first.

# Lab Requirements

It is strongly encouraged to adopt a test driven approach to the lab. Define a test, implement code to pass the test, and then repeat. Take some time at the front-end to plan out the test progression in a sensible way. A test driven approach will make the lab feel more manageable (gives an obvious place to start), and it will help provide an incremental approach to implementing features.

Implement the dynamic test generation for the static type proof for the following language features:

  * `ReturnStatement` (lab 3)
  * `ExpressionStatement` for `Assignment` (lab 3)
  * `PrefixExpression` for `!` (lab 3)
  * `InfixExpression` for `+`, `*`, and `-` with type according to above rules (lab 3)
  * `InfixExpression` for `&&`, `||`, `<`, and `==` with type according to above rules (lab 3)
  * `IfStatement` with type `void` (lab 3)
  * `WhileStatement` with type `void` (lab 3)  

Implement a minimum number of tests for each of the above language features. The provided code includes an existing test framework to use. In general, the input file should either pass or fail the type-check tests. If it passes, running the type-check tests works. If it is a input intended to fail, then running the type-check tests is only desired for debugging purposes as the checks show the outcome of each obligation in the check so it is easy to spot what is not right. But for testing, it is bad because there are always failed tests.

To give the ability to check both input that should type-check and input that should not type-check, the test framework is able to only check the final type returned from the type-check. If the input should pass, then that type is `TypeCheckTypes.VOID`. If the input should fail, then that type is `TypeCheckTypes.ERROR`. The provided code shows hawe to decide which to use for testing and debugging.

Finally, not all IDEs display dynamic tests in a useful way. If the IDE you are using is one of those (e.g. Visual Studio Code), then the `pom.xml` configures `mvn compile test exec:java` to run the tests in the JUnit 5 standalone. The standalone output is super helpful for debugging as it organizes all the tests in a tree structure that lends itself to visual inspection.

For this lab, visual inspection coupled with making sure what should pass passes and what should fail fails is sufficient. The next lab will begin to explore how to write tests to test the tests!

# What to turn in?

Create a pull request when the lab is done. Submit to Canvas the URL of the repository.

# Rubric

| Item | Point Value |
| ------- | ----------- |
| Test framework for symbol table implementation | 20 |
| Symbol table implementation | 30 |
| Tests to tests proof structure | 20 |
| Tests to show outcome of proofs | 20 |
| Integration tests (different from unit tests) | 10 |
| `CompilationUnit` implementation | 5 |
| `TypeDeclaration` implementation | 10 |
| `MethodDeclaration` implementation | 5 |
| `Block` (scopes) implementation | 10 |
| `EmptyStatement` implementation | 5 |
| `FieldAccess` implementation (including `ThisExpression`) | 5 |
| `QualifiedName` implementation | 10 |
| `VariableDeclarationFragment` with initializers for fields and local variables implementation | 10 |
| `Assignment` implementation | 5 | 
| `NumberLiterals`, `StringLiterals`, and `BooleanLiterals` implementation | 5 | 
| Style, documentation, naming conventions, test organization, readability, etc. | 30 | 
