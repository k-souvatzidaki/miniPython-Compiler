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
...

## Semantic analysis - Semantic Errors
...

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
