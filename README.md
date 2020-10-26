# Objective

The objective of this lab is to implement [static type checking](https://en.wikipedia.org/wiki/Type_system#Static_type_checking) for a subset of Java. The type checker generates a set of dynamic tests that represent a type proof for the input program. If all the tests pass, then that is a proof certificate that the input program is static type safe.

As before, the implementation will use the [org.eclipse.jdt.core.dom](https://help.eclipse.org/neon/index.jsp?topic=%2Forg.eclipse.jdt.doc.isv%2Freference%2Fapi%2Forg%2Feclipse%2Fjdt%2Fcore%2Fdom%2Fpackage-summary.html) to represent and manipulate Java.  The generated tests for the static type proof are generated with a specialized [ASTVisitor](https://help.eclipse.org/neon/index.jsp?topic=%2Forg.eclipse.jdt.doc.isv%2Freference%2Fapi%2Forg%2Feclipse%2Fjdt%2Fcore%2Fdom%2Fpackage-summary.html). 

The program will take no arguments as input and can only be invoked through the tests. The program should only apply to a limited subset of Java defined below. If an input file is outside the subset, then all bets are off.

This lab comes with code that implements a working environment (`ISymbolTable` and `SymbolTableBuilder`), tests for the that environment (`SymbolTableBuilderTests`), a partial implementation of the type checker (`TypeCheckBuilder`), and tests for that type checker (`TypeCheckBuilderTests`). The code is shipped *as is* with no guarantees. There are **no intentional defects** seeded in the code, but the code is **non-trivial** and not without complexity. 

# Reading

It is recommended that the `TypeCheckBuilder` code and `TypeCheckBulderTests` code be read carefully before starting the lab.

Review carefully the [type checking](https://bitbucket.org/byucs329/byu-cs-329-lecture-notes/src/master/type-checking.md) lecture with its companion slides [09-type-checking.ppt](https://bitbucket.org/byucs329/byu-cs-329-lecture-notes/src/master/compilers/09-type-checking.ppt). **The notes have a lot of implementation details that are worth carefully reading.** It may even be a good idea to use those notes as the starting point for the implementation. It lays out a progression and makes some suggestions on the architecture that are worth considering; regardless, do not start coding until the notes are fully understood.

# Java Subset

A general overview of what is and is not allowed in the Java subset for this lab:

  * There are no imports and nothing from `java.lang` such as `Integer`, `String`, etc. (although these are used in some limited tests)
  * There are no constructor definitions or invocations 
  * A `CompilationUnit` has a single `TypeDeclaration` in its declarations
  * A type-proof for a compilation unit is that all methods are type-correct in the class
  * All `FieldDeclaration` instances have no **initializer** 
  * Names for all entities are unique: no shadowing of any kind
  * All field references are type `FieldAccess` of the form `this.field` or `QualifiedName` of the form `n.m`
  * `int`, `boolean`, `NullType` (e.g., `NullLiteral`), and objects are the only types
  * `InfixExpression` instances for operators `+`, `-`, and `*` are always  `int,int:int` (e.g, expecting two `int` types and returning an `int` type)
  * `InfixExpression` instances for operators `&&` and `||` are always `boolean,boolean:boolean`
  * `InfixExpression` instances for operator `<` are always `int,int:boolean`
  * `PrefixExpression` instances for operator `!` are `boolean:boolean`
  * `InfixExpression` instances for operator `==` are always `Object,Object:boolean` where `Object` is an object type or `nullType`, `int,int:boolean`, or `boolean,boolean:boolean`
  * Assignment, `=`, between objects is like the `==` in that it requires types to be the same but with the added ability te assign objects to `null`, so `object,null:void` is type safe.
  
The type-checker must eventually prove the following languague features. The notation is the `ASTNode` type followed by the type it should have have to *type-safe*: `<ASTNode>:<type>` where `<environment>` means that it is the type defined by the environment. The types are listed in `TypeCheckTypes`.

  * `MethodDeclaration:void` (provided)
  * `CompilationUnit:void` (provided)
  * `Block:void` (provided)
  * `BooleanLiteral:boolean` (provided)
  * `NumberLiteral:int` (provided)
  * `NullLiteral:nullType` (provided)
  * `VariableDeclarationStatement:void` (provided)
  * `SimpleName:<environment>` (provided)
  * `ReturnStatement:void` (lab 3)
  * `ExpressionStatement:void` for `Assignment` (lab 3)
  * `PrefixExpression:boolean` for `!` (lab 3)
  * `InfixExpression:int` for `+`, `*`, and `-` (lab 3)
  * `InfixExpression:boolean` for `&&`, `||`, `<`, and `==` (lab 3)
  * `IfStatement:void` (lab 3)
  * `WhileStatement:void` (lab 3) 
  * `FieldAccess:<environment>` (e.g., `this.a`) (lab 4)
  * `QualifiedName:<environment>` (e.q., `n.a`) (lab 4)
  * `MethodInvocation:<environment>` (lab 4)

If something seems unusually hard then reach out to the instructor as it most likely out of scope or not intended. 

# Symbol Table Interface

The lab code includes a symbol table to implement the environment. It also checks many of the above properties for the Java subset and throws exceptions when it sees langauge features that are *out of scope* (see the code).
Only use the `ISymbolTable` interface to construct the type proof as is patterend in the `TypeCheckBuilder` code. There are JavaDoc comments in `ISymbolTable` to define the interface. When in doubt, look at `SymbolTableBuilder`. The symbol table is provided as is. There are no intentional defects, but the code is non-trivial, so please report and fix defects discovered.

# Type proof

The [lecture notes](https://bitbucket.org/byucs329/byu-cs-329-lecture-notes/src/master/type-checking.md) are somewhat extensive on constructing the type proof and detailing a possible implementation. Take time to really understand the lecture notes before getting too far into this lab.

The provided partial implementation in `TypeCheckBuilder` uses two stacks to manage the type proof: `typeStack` and `typeCheckStack`. The first, `typeStack` is the return type of the most recent node in the recursion tree for the type check and is the type returned on the edges of that tree in the class lectures. The second is the set of obligations that must hold at a node in the recursion tree from lecture that determine the type to return on the edge back up to the node above. These checks are actual JUnit 5 tests (e.g. `DynamicTest` and `DynamicContainer` instances). Both stacks are manipulated using helper methods: `pushType(String)` and `popType()` for the `typeStack` or `pushTypeCheck(List<DynamicNode> proof)` and `popTypeCheck()` for `typeCheckStack`.

The recursion can be confusing, and keeping track of the state between different visit methods is confusing as well.  The provided code is one way to do it. In that code, in general, a `visit` method is a node in the recursion tree from lecture. So it pushes an empty set of checks on the `typeCheckStack` and adds obligations to that set as it progresses. Those checks represent the obligations that must be met in order for the particular element being visited to be proved *type safe*. The checks are added by the visit method as it checks different elements of the particular node being visited by calling accept methods on field members of that node as appropriate. The last thing the `visit` method does before returning is push on the `typeStack` the resulting type of the node: `error` or otherwise.

An `endVisit` method, in this type check implementation,  pops the set of checks (obligations) from the `typeCheckStack` and wraps those in a container representing the type-rule for that node. In other words, when the code arrives at the `endVisit`, all the obligations for a given rule are in the container at the top of the `typeCheckStack`. So that top is poped, put in a dynamic container representing the rule for the particular `ASTNode`, and then pushed as an obligation in the new top of the `typeCheckStack`---it's all recursive! It is confusing. I agree. Read and study the provided implementations. There is a pattern for the `visit` and `endVisit` methods followed by every type. Follow that pattern!

## Utils

The `Utils` class provides a bevy of helper methods in the form of `getName` and `getType` to get the name and type from `ASTNode` instances. Always check `Utils` before digging too deep into the `ASTNode` type to find something. More than likely what is needed is already there. There are many examples in both `SymbolTableBuilder` and `TypeCheckBuilder` using `Utils` and working with the different `ASTNode` types for this lab. Be sure to look in these files first for examples and clarification to what matters (or does not matter) in any givin `ASTNode` type.

# Lab Requirements

It is strongly encouraged to adopt a test driven approach to the lab. Define a test, implement code to pass the test, and then repeat. Take some time at the front-end to plan out the test progression in a sensible way. A test driven approach will make the lab feel more manageable (gives an obvious place to start), and it will help provide an incremental approach to implementing features.

Implement the type rules as dynamic tests for the static type proof for the following language features:

  * `ReturnStatement:void` 
  * `ExpressionStatement:void` 
  * `PrefixExpression:boolean`
  * `InfixExpression:int` 
  * `InfixExpression:boolean` 
  * `IfStatement:void` 
  * `WhileStatement:void` 

Implement a minimum number of tests for each of the above language features. The provided code includes an existing test framework to use. In general, the input file should either pass or fail the type-check tests. If it passes, running the type-check tests works. If it is a input that is intended to fail, then running the type-check tests is only desired for debugging purposes as the checks show the outcome of each obligation in the check so it is easy to spot what is not right. But for testing, it is bad because there are always failed tests since the inputs are purposely not type-safe.

To give the ability to check both input that should type-check and input that should not type-check, the test framework is able to only check the final type returned from the type-check. If the input should pass, then that type is `TypeCheckTypes.VOID`. If the input should fail, then that type is `TypeCheckTypes.ERROR`. The provided code shows how to decide which to use for testing and debugging. The final type used in these types of tests comes from the `typeStack` and is the type returned on the edge in the recursive tree in class lecture.

Finally, not all IDEs display dynamic tests in a useful way. If the IDE you are using is one of those (e.g. Visual Studio Code), then the `pom.xml` configures `mvn compile test exec:java` to run the tests in the JUnit 5 standalone. The standalone output is super helpful for debugging as it organizes all the tests in a tree structure that lends itself to visual inspection.

For this lab, visual inspection coupled with making sure what should pass passes and what should fail fails is sufficient. The next lab will begin to explore how to write tests to test the tests!

# What to turn in?

Create a pull request when the lab is done. Submit to Canvas the URL of the repository.

# Rubric

| Item | Point Value |
| ------- | ----------- |
| `ReturnStatement:void` | 10 | 
| `ReturnStatement:void` tests| 15 |
| `ExpressionStatement:void` for `Assignment` | 10 | 
| `ExpressionStatement:void` for `Assignment` tests | 15 | 
| `PrefixExpression:boolean` | 10 |
| `PrefixExpression:boolean` tests | 15 |
| `InfixExpression:int` | 10 |
| `InfixExpression:int` tests | 15 |
| `InfixExpression:boolean` | 10 | 
| `InfixExpression:boolean` tests | 15 | 
| `IfStatement:void` | 10 |
| `IfStatement:void` tests | 15 |
| `WhileStatement:void` | 10 | 
| `WhileStatement:void` tests | 15 | 
| Style, documentation, naming conventions, test organization, readability, etc. | 25 | 
