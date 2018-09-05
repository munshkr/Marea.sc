+ Object {
	pure {
		^MareaPattern { |start, end|
			var startPos, endPos;
			start.isNumber.not.if { Error("start must be a number").throw };
			end.isNumber.not.if { Error("end must be a number").throw };

			startPos = start.asFloat.floor.asInt;
			endPos = end.asFloat.ceil.asInt;

			startPos.to(endPos - 1).collect { |t|
				var arc = MareaArc(t, t+1);
				MareaEvent(arc, arc, this);
			}
		};
	}

	mp {
		^this.pure;
	}
}

+ Interval {
	cat { ^MareaPattern.cat(this) }
	fastcat { ^MareaPattern.fastcat(this) }
	mp { ^this.fastcat; }
}

+ Array {
	cat { ^MareaPattern.cat(this) }
	fastcat { ^MareaPattern.fastcat(this) }
	mp { ^this.fastcat; }
}

+ Nil {
	mp { ^MareaPattern { [] } }
}

+ Rest {
	mp { ^MareaPattern { [] } }
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
		^MareaParser.new.parse(this);
	}
}