MPArc {
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
			res.add(MPArc(s_, nextCycle));
			s_ = nextCycle;
		};
		if (s_ < e_ && s_.floor == e_.floor) { res.add(MPArc(s_, e_)) };
		^res;
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

	@ { |rpat|
		rpat = rpat.mp;
		^MPPattern { |start, end|
			var events = List[];
			this.(start, end).do { |ev|
				var values = List[];
				var revents = rpat.(ev.activeArc.start, ev.activeArc.end);

				revents = revents.select { |ev| ev.activeArc.contains(ev.positionArc.start) };
				values.addAll(ev.value);
				revents.do { |rev| values.addAll(rev.value) };
				events.add(MPEvent(ev.positionArc, ev.activeArc, values));
			};
			events.asArray;
		}
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

	withEventValue { |fn|
		^MPPattern { |start, end|
			this.(start, end).collect { |ev|
				MPEvent(ev.positionArc, ev.activeArc, fn.(ev.value))
			}
		};
	}

	splitQueries {
		^MPPattern { |start, end|
			var cycles = MPArc(start, end).cycles;
			var res = List[];
			cycles.do { |cycle|
				res.addAll(this.(cycle.start, cycle.end));
			};
			res.asArray;
		};
	}

	splitAtSam {
		^MPPattern { |start, end|
			this.(start, end).collect { |ev|
				var sam = start.floor;
				var newArc = MPArc(ev.activeArc.start.max(sam), ev.activeArc.end.min(sam + 1));
				MPEvent(ev.positionArc, newArc, ev.value);
			};
		}.splitQueries;
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

	density { |value|
		^this.withQueryTime { |t| t * value }.withResultTime { |t| t / value };
	}
	fast { ^this.density }

	sparsity { |value|
		^this.density(Rational(1, value));
	}
	slow { ^this.sparsity }

	rotLeft { |value|
		^this.withResultTime { |t| t - value }.withQueryTime { |t| t + value };
	}

	rotRight { |value|
		^this.rotLeft(0 - value);
	}

	when { |testFn, fn|
		^MPPattern { |start, end|
			testFn.(start.floor).if {
				fn.(this).(start, end)
			} {
				this.(start, end)
			}
		};
	}

	every { |num, fn|
		^this.when({ |t| (t % num == 0) }, fn);
	}

	// discretize TODO

	*cat { |array|
		^MPPattern { |start, end|
			var l = array.size;
			var r = start.floor;
			var n = r % l;
			var p = array[n].mp;
			var offset = r - ((r - n).div(l));
			p.withResultTime { |t| t + offset }.(start - offset, end - offset);
		}.splitQueries;
	}

	*fastcat { |array|
		^array.cat.fast(array.size);
	}

	*signal { |fn|
		^MPPattern { |start, end|
			if (start > end) { [] } { fn.(start).mp.(start, end) }
		}
	}

	*sin {
		^this.signal { |t| (sin(2*pi*t.asFloat) + 1) / 2 }
	}
}

MP : MPPattern {}