Storyboard

Creators Julo Bretaña and Julia Louise Contreras

Language Overview 

Storyboard is a functional programming language designed to promote clarity, immutability, and safe computation. It is inspired by both the readability of Python, the precision of functional languages, and the structure of film directing. Just as movies are organized into scenes, actors, and takes, Storyboard maps these cinematic concepts into programming constructs—making code feel like writing a script. This design helps beginners visualize program flow as a sequence of controlled, well-defined steps.

Storyboard disallows mutable shared state and prevents values from being accessed outside their defining scope, ensuring predictable, side-effect-free execution. It is particularly suited for learning foundational programming concepts, modeling computations, transforming data, and writing small, verifiable programs. By emphasizing pure functions, clear structure, and human-readable syntax, Storyboard offers a gentle, narrative-inspired entry point into functional programming.

Keywords 

Special keywords:	
	“storyboard” defines a storyboard block 
	“cut” defines the end of a storyboard block,
	"Actor" defines a variable,
	"Role" specifies the datatype of an Actor,
	“Assign” assigns a value to an Actor,
	“Action” defines an operation/expression/statement to perform,
	“Roll” calls another storyboard or subroutine.

Variables and Functions:
	"return" returns a value
	“Set” defines a standalone function or global routine
	“to” used in assignment expressions (e.g. Assign :: 5 to x)

Variable Types:
	"int" for integers, 
	"float" for floats, 
	"char" for single characters, 
	“bool” for boolean logic
	"String" for strings

Control Flow: 
	"if" for conditional branching, 
	"else" for alternate branches, 
	"Scene” introduces a loop with a specified number of “takes” (iterations),
	“takes” indicates how many iterations a Scene will run

Logical Values: 
	"TRUE" or "FALSE" boolean values, 
	"null" - null values

Arithmetic keywords:	
	“add” for addition,
	“sub” for subtraction,
	“mul” for multiplication,
	“div” for division	
			
Operators 

Assignment: "::"

Arithmetic: 
	"+" for addition, 
	"-" for subtraction, 
	"*" for multiplication,
	"/" for division

Comparison: 
	"==" for equality, 
	"!=" for not equal, 
	"<" for less than, 
	"<=" for less than or equal to, 
	">" for greater than, 
	">=" for greater than or equal to
	*comparison is not allowed for different types (e.g. int cannot be compared to a char)

Logical operators: 
	"!" - logical NOT, 
	"AND" - logical AND, 
	"OR" - logical OR

Grouping:
	() - grouping expressions and precedence control.
	{} - denotes function or storyboard bodies.
	"," - separator for parameters or lists, 
	"." - member access or chaining, 

Literals

Literal are assigned using the syntax:
	Actor :: <identifier> Role :: <datatype>
	Assign :: <literal> to <identifier>

Numerical Literals
Numeric values are written directly as digits and support integer and float values. If no value is assigned, numerical Actors receive default values of 0 and 0.0. Action statements can have (pure arithmetic operations)
Example:
	Actor :: x Role :: int		Actor :: speed  Role :: float		Action :: 2 add 2
	Assign :: 10 to x			Assign :: 2.75 to speed

String and Character literals
Character literals and string literals should be both enclosed in double quotes.
Example:
	Actor :: key Role :: char		Actor :: name Role :: String
	Assign :: “c” to key			Assign :: “Abcde” to name

Identifiers 
Identifiers represent Actors and follow the following rules:
	- Must begin with a letter (A-Z, a-z) or underscore ( _ ) but cannot start with a number or other special characters. 
	- Can include letters, numbers, and underscores
	- Are strictly case-sensitive
	- Cannot contain spaces or special characters
	- Cannot be a reserved keyword from the language

Identifiers for defining a storyboard must begin with an uppercase letter
Example: Test, CountDown, Get_num

Valid identifiers: FooBar, enemyHP, _xp, shield_value, boss4
Invalid identifiers: 1number, player-health, Scene, get num

Comments 
Single-line Comments
	- Single-line comments will begin with # and continue until the end of the line.
Example: 
	# This is a single-line comment 

Group (Block) Comments
	- Group comments begin with ## and end at the next occurrence of ##. They can span multiple lines but cannot be nested.
Example:
	## This is a group comment
     	This is a group comment ##

Syntax Style 

Storyboard uses a syntax style that behaves like a wrapper function while also resembling a lightweight, struct-like block. A storyboard encloses its own variables (Actors), actions, and logic, creating a self-contained unit of gameplay behavior.

Whitespaces
- Indentation is enforced. Nested or interior statements inside a storyboard must be intended consistently for the parser to accept the block structure
- Newlines matter only for readability and for line-number tracking, not for execution order.
- Extra spaces around operators and delimiters are allowed but cannot break tokens. 

Statement Termination
Fleet does not use semicolons. A statement ends when the line ends.

Block Delimiters
- Curly braces { } define the body of a storyboard
- Parentheses ( ) are used for grouping arguments to be passed on a storyboard, arithmetic grouping and expression precedence.
- Every storyboard must end with the closing } followed by the “cut” keyword.

Sample Code 
[Provide a few examples of valid code in your language to demonstrate the syntax and features] 

storyboard PrintNumber () {
	Actor :: x Role :: int
	Assign :: 10 to x
	Present :: x
} cut

storyboard AddTwoNumbers {
	Actor :: a Role :: int
    Assign :: 5 to a
    Actor :: b Role :: int
	Assign :: 7 to b
	Action :: a add b
	Present :: a
} cut

Looping: 
storyboard LoopCount {
	Actor :: i Role :: int
    Assign :: 3 to i
    Scene :: 3 takes {
        Present :: i
        Action :: i add -1
	}
} cut

If-else Statements:
storyboard CheckNumber {
	Actor :: x Role :: int
	Assign :: 8 to x
	Action :: {
	     if (x > 5) {
	          Present :: x
	     } else {
	          Present :: 0
	     }
	}
} cut

storyboard ReturnExample {
	Actor :: x Role :: int
    Assign :: 20 to x
    Action :: {
		if (x == 20) {
			return x
        }
	}
    Present :: x
} cut

	
Design Rationale 

Storyboard was designed with the goal of making general-purpose programming more readable, structured, and approachable, while maintaining a level of formality suitable for parsing, static analysis, and compilation. The language adopts a block-oriented syntax that provides programmers with an intuitive mental model: each block represents a self-contained unit of computation governed by clear inputs, transformations, and outputs. This encourages writing programs as small, predictable components rather than large, state-heavy procedures.

The decision to structure a block like a lightweight function with explicitly declared values, followed by transformations and a final expression supports Storyboard’s functional programming foundation. By keeping data immutable and enforcing lexical scope, Storyboard ensures that each block behaves as a pure computational step. This predictable flow reduces cognitive load, prevents side effects, and allows beginners to reason about programs in a linear, mathematically grounded way. The result is a language that is expressive enough for general computation yet constrained enough to encourage correctness, clarity, and safe programming habits.

Storyboard’s keywords were chosen for readability over conventional programming syntax. Instead of symbols or cryptic operators, semantic keywords like Actor, Assign, Action, and Present make the language approachable for new developers while still mapping cleanly to common programming concepts. Arithmetic operators are expressed through action verbs (e.g., add, sub, mul, div) to reinforce the idea that Actions explicitly do something to an Actor, mirroring game mechanics.

Identifiers follow strict but simple rules to encourage clean coding habits and avoid ambiguities during parsing. Storyboard identifiers begin with uppercase letters to visually distinguish phase definitions from game variables (Actors). This mirrors conventions found in many languages, improving readability and aiding code navigation.

Indentation is required to emphasize structure, reinforce clarity, and avoid visually ambiguous code. However, semicolons are omitted to reduce syntactic noise fitting the language’s goal of being approachable and human-readable.

Overall, each design choice in Storyboard aims to balance approachability, expressiveness, and structural clarity—making it easier for beginners to understand while providing enough rigor for predictable compilation and execution.
