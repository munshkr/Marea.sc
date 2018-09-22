# Marea grammar

This is the grammar that Marea currently uses, written in Extended Backus-Naur
form (EBNF), and with regular expressions.  Although it is heavily inspired in
the TidalCycles language, it is not a 100% replica.

```
root = expr ;

expr = group, {modifier} ;

group = polyGroup ;
polyGroup = "{", groupBody, "}", ["%", number] | "<", groupBody, ">" | seqGroup ;
seqGroup = "[", groupBody, "]" ;
groupBody = term, {term}, [",", groupBody] ;
term = (value, {modifier}) | expr ;

modifier = bjorklundMod ;
bjorklundMod = ("(", number, ",", number, [",", number], ")") | densityMod ;
densityMod = ("*", number) | sparsityMod ;
sparsityMod = ("/", number) | replicateMod ;
replicateMod = ("!", [replicateMod]) | degradeMod ;
degradeMod = "?" ;

value = number | string | rest ;
number = float | integer ;
rest = "~" ;

float = ["-"], /[0-9]/, {/[0-9]/}, ".", {/[0-9]/} ;
integer = ["-"], /[0-9]/, {/[0-9]/} ;
string = /[A-Za-z_]/, {/[A-Za-z0-9_\-:]/} ;

```


## Notes

* Rests with `~`

* Groups with `[` `]` (this is the "parenthesis" of expresions)

    "[bd bd] [sd sd sd] [bd sd]"

or by "marking out feet" with `.`

    "bd bd . sd sd sd . bd sd"

* Polyrhythms with `,`

    "[bd bd bd, sd cp sd cp]"

* Density `*` and sparsity `/` on events or groups:

    "[bd sn]*2 cp"

* Polymeters:

    "{bd hh sn cp, arpy bass2 drum notes can}"
    "{arpy bass2 drum notes can}%4"

* Increment density by one with `!` (applies to event or group)

    "bd!!"

* Degrade event by half with `?` (applies to event or group)

    "bd?"
    "bd?!"
    "bd!??"

* `< ... >` is the same as `{ ... }%1`

* Bjorkund with `v(x,y)` (v, x and y are values or groups)

    "bd(3,8) sn(5,8)"
    "bd([5 3]/2,8)"


    []
    {0}
    {1 2}%2
    bd sn
    [bd sn]
    {[bd sn]}
    [{bd sn}]
    [{sd} bd]
