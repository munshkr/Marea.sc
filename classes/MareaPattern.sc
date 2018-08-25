MPArc {
	var <start, <end;

	*new { |start, end|
		^super.newCopyArgs(start, end);
	}

	printOn { | stream |
		stream << "(" << start << ", " << end << ")";
	}
}

MPEvent {
	var <positionArc, <activeArc, <value;

	*new { |positionArc, activeArc, value|
		^super.newCopyArgs(positionArc, activeArc, value);
	}

	printOn { | stream |
		stream << "E(" << positionArc << ", " << activeArc << ", " << value.asString << ")";
	}
}

MPPattern {
	var func;

	*new { |fn|
		^super.newCopyArgs(fn);
	}

	value { |start, end|
		^func.(start, end);
	}

	withQueryTime { |fn|
		^MPPattern({ |start, end|	this.(fn.(start), fn.(end)) });
	}

	withResultTime { |fn|
		^MPPattern({ |start, end|
			this.(start, end).collect { |ev|
				var posArc = ev.positionArc, activeArc = ev.activeArc;
				posArc = MPArc(fn.(posArc.start), fn.(posArc.end));
				activeArc = MPArc(fn.(activeArc.start), fn.(activeArc.end));
				MPEvent(posArc, activeArc, ev.value)
			}
		});
	}

	fast { |value|
		^this.withQueryTime { |t| t * value }.withResultTime { |t| t / value };
	}

	slow { |value|
		^this.fast(Rational(1, value));
	}
}

+ Object {
	pure {
		^MPPattern({ |start, end|
			start.isNumber.not.if { Error("start must be a number").throw };
			end.isNumber.not.if { Error("end must be a number").throw };

			start.floor.asInt.to(end.asInt.ceil).collect { |t|
				var arc = MPArc(t, t+1);
				MPEvent(arc, arc, this);
			}
		});
	}
}