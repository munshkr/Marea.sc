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
		^MPPattern { |start, end| this.(fn.(start), fn.(end)) };
	}

	withResultTime { |fn|
		^MPPattern { |start, end|
			this.(start, end).collect { |ev|
				var posArc = ev.positionArc, activeArc = ev.activeArc;
				posArc = MPArc(fn.(posArc.start), fn.(posArc.end));
				activeArc = MPArc(fn.(activeArc.start), fn.(activeArc.end));
				MPEvent(posArc, activeArc, ev.value)
			}
		};
	}

	fast { |value|
		^this.withQueryTime { |t| t * value }.withResultTime { |t| t / value };
	}

	slow { |value|
		^this.fast(Rational(1, value));
	}

	rotLeft { |value|
		^this.withResultTime { |t| t - value }.withQueryTime { |t| t + value };
	}

	rotRight { |value|
		^this.rotLeft(0 - value);
	}
}

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
}

+ Array {
	/*
	cat {
		^MPPattern { |start, end|
			var events = List[];
			var len = this.size;
			this.do { |pat|
				var r, n, offset, start_, end_;
				pat.isKindOf(MPPattern).not.if {
					Error("array must contain only MPPattern objects").throw;
				};
				r = start.floor;
				n = r.mod(len);
				offset = r - ((r - n).div(len));
				start_ = start - offset;
				end_ = end - offset;
				events.addAll(pat.withResultTime({ |t| t + offset }).(start_, end_));
			};
			events.asArray;
		};
	}
	*/

	cat {
		^MPPattern { |start, end|
			var events = List[];
			this.do { |pat, i|
				events.addAll(pat.withResultTime({ |t| t + i }).(start, end));
			};
			// Keep events that start or end in the current arc
			events.select { |ev|
				(ev.positionArc.start >= start) || (ev.positionArc.end <= end);
			}.asArray;
		};
	}

	fastcat {
		^this.cat.fast(this.size);
	}
}