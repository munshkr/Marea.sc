# Marea.sc

A [TidalCycles](https://tidalcycles.org/) port for SuperCollider.


## How it looks like *right now*?

```supercollider
(
p = ProxySpace.push(s);
p.makeTempoClock;
)

(
~x.play;
~x = (degree: "0(3,8), 1 3, 6 7, 8 ~ 9, 2(7,16)/2".t).mp
    .every(2, {|p| p.within(0.75, 1, _.fast(2))})
    .every(3, _.brak)
    .every(2, _.rotRight(0.125))
  << (scale: Scale.dorian)
  << (carPartial: "3".t).mp
  << (modPartial: 2)
  << (octave: 3).mp.every(3, {|p| p << (octave: "4 5".t) <<+ (degree: "3 4 5".t)})
)
```


## To do

- [x] Basic functionality of Pattern and some functions
- [x] Event playing
- [x] ProxySpace compatibility
- [x] `+`, `-`, `*`, `/` operators for patterns
- [x] Refactor methods in classes that extend Pattern
- [x] Tidal Parser
- [ ] Define palindrome, rev, cut, jux, juxBy
- [ ] Define more signal patterns: saw, tri, pulse, envs (see Tidal refactor branch)
- [ ] Support for patterns on slow/fast/etc. using unwrap
- [ ] Define a way to add dynamically define instance/class Pattern methods
- [ ] Start writing unit tests
- [ ] Write documentation


## Some explanations

In Tidal, a Pattern is a function from an Arc (an interval of rationals that
represent a time position measured in cycles) to a list of Events.  An Event is
a tuple of 3 things: a "position" arc, which indicates the start and end of the
event, an "active" arc, which indicates the *active* part of the event when the
pattern is *queried*, and the value of the event itself.  Because a Pattern is
a function, it can be evaluated or queried for a specific arc, and it will
return the events that occur in that interval of time.

In Marea, a Pattern is a class (`MareaPattern`, or `MP`) that responds to
`value` (same as a function), and does the same thing as Tidal, it returns an
array of `MareaEvent` given `start` and `end` parameters.

Every pattern starts with either a *pure* or *silence* pattern. Any object can
be converted to a pure pattern by calling the `#pure` method:

```supercollider
// Any object can be converted to a "pure" pattern
1.pure  //-> a MPPattern

// Query the pattern for the first cycle: arc (0, 1)
1.pure.value(0, 1)  //-> [ E(0/1 1/1, 0/1 1/1, 1) ]
// In SCLang you can also omit `value`:
1.pure.(0, 1)  //-> [ E(0/1 1/1, 0/1 1/1, 1) ]
```

A common alias is `#mp`, which is polymorphic method of all objects.  In
general, calling `mp` on any object will convert it into a *pure* pattern,
except for the following classes:

* `Nil`, `Rest`: Creates a *silence* pattern
* `Array`: Calls `#mp` on all of its elements and then `#fastcat` on the new
  array.
* `Event`: Calls `#mp` on its values and creates a new pattern whose
  event values are Events with their keys and values merged.

```supercollider
// silence/rest
nil.mp.(0, 1)  //-> [ ]

(k: 1, a: 2).mp.(0, 1)  //-> [ E(0/1 1/1, 0/1 1/1, ( 'a': 1, 'k': foo )) ]
```

### Tidal language parser

In Tidal you can write Patterns in a more concise way as a string in a special
language.  You can read more about it at the [Tidal
docs](https://tidalcycles.org/patterns.html). There is a grammar written in
EBNF notation for the precise implementation of the language in Marea.  You can
read more in the [grammar.md](grammar.md) file.

In Marea you just write your pattern as a String and then call the `t` method:

```supercollider
"1 2 3".t  //-> a MP
"1 2 3".t.(0, 1)   //-> [ E(0/1 1/3, 0/1 1/3, 1), E(1/3 2/3, 1/3 2/3, 2), E(2/3 1/1, 2/3 1/1, 3) ]
```

So you would use it like this:

```supercollider
~x = (s: "[bd sn, hh hh ~ hh]".t).mp
```

## Contributing

Bug reports and pull requests are welcome on GitHub at
https://github.com/munshkr/Marea.sc. This project is intended to be a safe,
welcoming space for collaboration, and contributors are expected to adhere to
the [Contributor Covenant](http://contributor-covenant.org) code of conduct.

## LICENSE

See [LICENSE](LICENSE)
