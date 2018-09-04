+ Object {
	pure {
		^MPPattern { |start, end|
			var startPos, endPos;
			start.isNumber.not.if { Error("start must be a number").throw };
			end.isNumber.not.if { Error("end must be a number").throw };

			startPos = start.asFloat.floor.asInt;
			endPos = end.asFloat.ceil.asInt;

			startPos.to(endPos - 1).collect { |t|
				var arc = MPArc(t, t+1);
				MPEvent(arc, arc, this);
			}
		};
	}

	mp {
		^this.pure;
	}
}

+ Interval {
	cat { ^MPPattern.cat(this) }
	fastcat { ^MPPattern.fastcat(this) }
	mp { ^this.fastcat; }
}

+ Array {
	cat { ^MPPattern.cat(this) }
	fastcat { ^MPPattern.fastcat(this) }
	mp { ^this.fastcat; }
}

+ Nil {
	mp { ^MPPattern { [] } }
}

+ Rest {
	mp { ^MPPattern { [] } }
}

+ Dictionary {
	mp {
		var pat;
		this.keysValuesDo { |key, value|
			var newPat = value.mp.withEventValue { |v| List[(key -> v)] };
			if (pat.isNil) {
				pat = newPat;
			} {
				pat = pat @ newPat;
			}
		};
		^pat;
	}
}

+ Rational {
	floor {
		^this.asInteger;
	}
}

+ String {
	t {
		^MPParser.new.parse(this);
	}
}