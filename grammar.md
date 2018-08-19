# Marea grammar

This is the grammar that Marea currently uses, written in Backus-Naur form and
extended with regular expressions.  Although it is heavly inspired in
TidalCycles language, it is not a 100% replica.

```
<expr>    ::= <term> (<s> <term>)*
<term>    ::= <event> | '[' <expr> ']'
<event>   ::= <atom> (':' <number>+)? (<op> <number>)?
<atom>    ::= <number> | <string> | <rest>
<number>  ::= [0-9]+
<string>  ::= [A-Za-z]+
<rest>    ::= '~'
<op>      ::= '*' | '/'
<s>       ::= [\s]+
```
