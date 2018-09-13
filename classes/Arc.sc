MareaArc {
	var <start, <end;

	*new { |start, end|
		^super.newCopyArgs(start.asRational, end.asRational);
	}

	printOn { | stream |
		stream << start.numerator << "/" << start.denominator << " " << end.numerator << "/" << end.denominator;
	}

	contains { |t|
		^(t >= start) && (t < end)
	}

	cycles {
		var s_ = start, e_ = end, res = List[];
		while { (s_ < e_) && (s_.floor != e_.floor) } {
			var nextCycle = s_.floor + 1;
			res.add(MareaArc(s_, nextCycle));
			s_ = nextCycle;
		};
		if (s_ < e_ && s_.floor == e_.floor) { res.add(MareaArc(s_, e_)) };
		^res;
	}

	midPoint {
		^(start + end) / 2
	}
}