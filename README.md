Fleet

Creators Julo Bretaña and Julia Louise Contreras

Language Overview 
[Provide a brief description of your programming language - what it's designed for, its main characteristics] 
Fleet is a general-purpose language that aims to be readable and simple. Its syntax style makes for quicker coding and is beginner-friendly.

Keywords [List all reserved words that cannot be used as identifiers - include the keyword and a brief description of its purpose] 
variables and functions: "var" defines a variable, "def" defines a function, "return" returns a value

variable types: "int" for integers, 
                "flt" for floats, 
                "char" for single characters, 
                "String" for strings

control flow: "if" for conditional branching, 
              "else" for alternate branches, 
              "while" for looping while condition is true, 
              "for" for iterated loops

logical values: "TRUE" or "FALSE" - boolean values, "null" - null values

Operators 
[List all operators organized by category (arithmetic, comparison, logical, assignment, etc.)] 

assignment: "="

arithmetic: "+" for addition, "-" for subtraction, "*" for multiplication, "/" for division

comparison: "==" for equality, 
            "!=" for not equal, 
            "<" for less than, 
            "<=" for less than or equal to, 
            ">" for greater than, 
            ">=" for greater than or equal to

comparison is not allowed for different types (e.g. int cannot be compared to a char)

logical: "!" - logical NOT, "AND" - logical AND, "OR" - logical OR

grouping: () - function calls and read operator precedence, {} - function body, ","(comma), "."(dot), ";"(semicolon)

Literals 
[Describe the format and syntax for each type of literal value (e.g., numbers, strings, characters, etc.) your language supports] 
numerical and character literals will be defined by the "var" keyword, followed by their variable type, then variable name, then the assignment operator "=", 
ex. var int foo = 10, var float bar = 12.34, var char coo = "c"

String literals will be defined by the "var" keyword, followed by their variable type, then variable name, 
then the assignment operator "=", then the string enclosed in quotation marks, ex. String vim = "Abcde"

default values are given to numerical literals if no values are assigned, ex. var int foo, var float bar foo = 0, bar = 0.0

Identifiers 
[Define the rules for valid identifiers (variable names, function names, etc.) and whether they are case-sensitive] 
identifiers cannot begin with a number

identifiers are case-sensitive, ex. fooBar != foobar

identifiers only accept letters, digits (if they are not the first character), and underscore(_)

Comments
[Describe the syntax for comments and whether nested comments are supported] 
single-line comments will be signified by # at the start and will end once the line ends 
group comments will be signified by ## at the start and will end at ## 
nested comments will not be supported

Syntax Style 
[Describe whether whitespace is significant, how statements are terminated, and what delimiters are used for blocks and grouping]
The statements will terminate once there is a semicolon at the end of grouped code.
Whitespace is not significant except for newlines when you want to track line numbers.
The delimeters used for blocks are braces - {} while for grouping it will use parentheses - ()

Sample Code 
[Provide a few examples of valid code in your language to demonstrate the syntax and features]
    for (item in items) { 
        item++; 
        print(item); 
    }

    def newItem (item) { 
        print(item); 
    }

Design Rationale 
[Explain the reasoning behind your design choices] 
The design of Fleet follows a readable structure to make coding straightforward and maintainable.

1. Language goals that guided design 
   - High-level language like Python with added C-like surface where braces {} and semicolons ; to its syntax making the language familiar to many programmers and simplify block parsing. 
   - Small, explicit keyword sets making the set of reserved words keep the grammar easy to learn and implement 
   - Type clarity and safety by disallowing implicit, cross-type comparisons reduces surprising behavior and keeps the parser/type-checker simple
   
2. Keywords and reserved words
   - keywords were selected to mirror widely used languages (Python and C) minimizing the learning curve for new users.
   - separate type keywords (int, float, char, String) emphasize strong typing while keeping variable declarations explicit. 
   - Control flow constructs (is, else, while, for) are familiar and align with common imperative paradigms, making translation from other languages straightforward.

3. Operators 
   - arithmetic and comparison operators are kept consistent with mathematical conventions and other mainstream languages, reducing ambiguity. 
   - Logical operators (!, and, or) provide clear boolean logic while avoiding overloading symbols for readability. 
   - Assignment uses a single = to alignn with most imperative languages.

4. Literals 
   - Default initialization (e.g., var int foo = 0) prevents undefined behavior and runtime errors. 
   - String literals enclosed in quotes "..." ensure compatibility with common text processing needs while being easy to parse.

5. Identifiers 
   - Case sensitivity allows more expressive meaning (e.g., fooBar vs foobar) and aligns with widely used conventions. 
   - Restricting the first character from being a digit ensures valid parsing without ambiguity.

6. Comments
   - Single-line (#) and block (## ... ##) comment systems were chosen for simplicity and readability.
   - Explicitly disallowing nested comments avoids implementation complexity and ambiguity during scanning.
   
7. Syntax style

   - Whitespace is not significant except for line tracking, reducing programmer burden and keeping the scanner/tokenizer simple.
   - Semicolons explicitly mark the end of statements, simplifying parsing and reducing ambiguity.
   - Curly braces {} clearly define code blocks, making nested structures easy to read and parse.
   
8. Readability and accessibility
   - Fleet’s syntax is designed to look familiar to anyone with C, Java, or Python background, easing adoption.
   - The language avoids “clever” shortcuts, prioritizing clarity and explicitness.
