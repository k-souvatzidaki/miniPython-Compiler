# miniPython Compiler
A simple compiler for a **miniPython** high-level programming language. This is a group project for the Compilers course in the Computer Science department of AUEB

## miniPython instructions
The instructions set of miniPython is a **subset of Python's instructions** . It supports the following:
- Assign an integer,float number or string in a variable
- Assign a list in a variable
- Functions declaration with simple parameters (with support for default values) and one statement.
- If, for and while blocks with one statement
- Various statements/operations (print, type(x),min/max etc)
- Arithmetic operators (+,- etc) and the -= and /= operators
- Logical operators/ Comparison statements (<,>,==,! etc)
- Function calling
- One-line comments

## Lexical and Syntax analysis
MiniPython's instruction set is described in the Backus–Naur form (or Backus normal form - **BNF**) in the file BNF.html. It's a subset of **Python**'s syntax. 
In the **minipython.grammar** file, **tokens** are described for the lexical analysis, and **productions** (rules) for the syntax analysis.

## Abstract Syntax Tree
In the **Abstract Systax Tree** section of the .grammar file, syntax rules where redefined and the non-essential tokens where removed. Only identifiers and numbers where preserved, as those are needed for the semantic analysis.

## Semantic analysis - Semantic Errors
Two **Visitors**, implemented in **Java** and based on the classes **Sablecc** creates. Both Visitors traverse the AST, in order to **spot semantic errors**, **create a symbol table**, and define **types** for variables and **return types** for functions. The compiler recognises the following semantic errors:
- Use of non defined variable.
- Use of non defined function.
- Function redefinition with the samenumber of arguments.
- Parameters with the same name defined in a function.
- Wrong number of parameters passed in a function call.
- Non-numbers used in arithmetic and logical operations, functions' return types included.
- Functions with no return statement used in operations or print statements.

## Rules - Future Work
Some special rules apply on this compiler:
- Operators apply **only on number values**.
- All default parameters must be declared last in a function
- Type checking in variables used in functions is performed **only when the function is called**.
- **Inside a function's statement (body)**, the paremeters can be used in any operation **but** not as a parameter for calling other functions (other functions can be called with literals or global variables as arguments,tho) 

## SableCC
We used the [SableCC](https://sablecc.org/) parser generator to create this compiler.

## How to run the compiler
...

## Group
[Themelina Kouzoumpasi](https://github.com/themelinaKz)
[Lydia Athanasiou](https://github.com/lydia-ath)  
[Konstantina Souvatzidaki](https://github.com/k-souvatzidaki)
