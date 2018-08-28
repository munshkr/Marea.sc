MPArc {
	var <start, <end;

	*new { |start, end|
		^super.newCopyArgs(start.asRational, end.asRational);
	}

	printOn { | stream |
		stream << "(" << start << ", " << end << ")";
	}

	contains { |t|
		^(t >= start) && (t < end)
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

	onset {
		^positionArc.start;
	}

	offset {
		^positionArc.end;
	}

	start {
		^activeArc.start;
	}

	hasOnset {
		^(positionArc.start == activeArc.start);
	}

	hasOffset {
		^(positionArc.end == activeArc.end);
	}

	onsetIn { |start, end|
		^MPArc(start, end).contains(this.onset)
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

	mp { ^this }

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

	seqToRelOnsetDeltas { |s, e|
		^this.(s, e).select { |event|
			(event.onsetIn(s, e)) && (event.onset >= event.start)
		}.collect { |event|
			var s_ = event.positionArc.start;
			var e_ = event.positionArc.end;
			[(s_ - s) / (e - s), (e_ - s) / (e - s), event.value]
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