# Marea.sc

Some kind of TidalCycles implementation for SuperCollider.  Because why not?

Actually just trying to have a better understanding of TidalCycles and improve
my Sclang.

## To do

- [x] Basic functionality of Pattern and some functions
- [x] Event playing
- [x] ProxySpace compatibility
- [x] `+`, `-`, `*`, `/` operators for patterns
- [x] Refactor methods in classes that extend Pattern
- [ ] Tidal Parser
- [ ] Define palindrome, rev, cut, jux, juxBy
- [ ] Define more signal patterns: saw, tri, pulse, envs (see Tidal refactor branch)
- [ ] Support for patterns on slow/fast/etc. using unwrap
- [ ] Define a way to add dynamically define instance/class Pattern methods
- [ ] Start writing unit tests
- [ ] Write documentation

## How it ~~looks~~will look like?

This **does not work and is subject to change**, but it might look like this...

Using ProxySpace:

```supercollider
(
p = ProxySpace.push(s);
p.makeTempoClock;
)

(
~x.play;
~x = (n: "[0 2 [~ 3?] 5*2, [0(3,16)]/2, [7(5,16)]/2, [10(2,16,1)]/2]".t).mp
    .slow(2)
    .every(3, _.brak)
    .every(2, _.rotLeft(0.25))
    .every(2, { |p| p.within(0.5, 1, _.gap(2)) })
    .every(3, { |p| p.jux.rev.stut(3, 0.5, 0.5) })
    .every(6, _ + (octave: "5 6 7".t))
  << (s: "fm"),
  << (octave: 5)
  << (modP: "0 2 4 6".t)
  << (index: "0 20".t)
  << (lpf: MP.saw1.slow(2) * 3000 + 250)
  << (lpq: MP.sine.slow(2).scale(0.05, 0.2));
)
```

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
* `Dictionary`: Calles `#mp` on its values and then creates a new pattern whose
  event values are Associations.

```supercollider
// silence/rest
nil.mp.(0, 1)  //-> [ ]

(k: 1, a: 2).mp.(0, 1)  //-> [ E(0/1 1/1, 0/1 1/1, (k -> 1)), E(0/1 1/1, 0/1 1/1, (a -> 2)) ]
```

...

## Contributing

Bug reports and pull requests are welcome on GitHub at
https://github.com/munshkr/Marea.sc. This project is intended to be a safe,
welcoming space for collaboration, and contributors are expected to adhere to
the [Contributor Covenant](http://contributor-covenant.org) code of conduct.

## LICENSE

See [LICENSE](LICENSE)
