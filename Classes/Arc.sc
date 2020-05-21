MareaArc {
	var <start, <end;

	*new { |start, end|
		start.isNumber.not.if { Error("start must be a number").throw };
		end.isNumber.not.if { Error("end must be a number").throw };

		^super.newCopyArgs(start.asRational, end.asRational);
	}

	printOn { | stream |
		var startStr, endStr;
		startStr = start.numerator.asString;
		if (start.denominator != 1) {
			startStr = startStr ++ "/" ++ start.denominator.asString;
		};
		endStr = end.numerator.asString;
		if (end.denominator != 1) {
			endStr = endStr ++ "/" ++ end.denominator.asString;
		};
		stream << "(" << startStr << " " << endStr << ")";
	}

	contains { |t|
		^(t >= start) && (t < end)
	}

	// Splits this arc into a list of arcs, at cycle boundaries
	cycles {
		// FIXME Refactor
		var s_ = start, e_ = end, res = List[];
		while { (s_ < e_) && (s_.floor != e_.floor) } {
			var nextCycle = s_.floor + 1;
			res.add(MareaArc(s_, nextCycle));
			s_ = nextCycle;
		};
		if (s_ < e_ && s_.floor == e_.floor) { res.add(MareaArc(s_, e_)) };
		^res;
	}

	// Similar to #cycles, but this returns a list of arcs of the whole cycles
	// which are included in this arc.
	wholeCycles {
		var s = start.asFloat.floor.asInt;
		var e = end.asFloat.ceil.asInt;
		^s.to(e - 1).collect { |t| MareaArc(t, t+1) }
	}

	midPoint {
		^(start + end) / 2
	}

	sect { |otherArc|
		^MareaArc(this.start.max(otherArc.start), this.end.min(otherArc.end))
	}

	== { |that|
		^this.compareObject(that, #[\start, \end])
	}

	hash {
		^this.instVarHash(#[\start, \end])
	}
}