+ MareaPattern {
	*pure { |atom|
		^MareaPattern { |start, end|
			var startPos, endPos;
			start.isNumber.not.if { Error("start must be a number").throw };
			end.isNumber.not.if { Error("end must be a number").throw };

			startPos = start.asFloat.floor.asInt;
			endPos = end.asFloat.ceil.asInt;

			startPos.to(endPos - 1).collect { |t|
				var arc = MareaArc(t, t+1);
				MareaEvent(arc, arc, atom);
			}
		}
	}

	*silence {
		^MareaPattern { [] }
	}

	*cat { |array|
		^MareaPattern { |start, end|
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

	*stack { |patterns|
		^patterns.inject(MareaPattern.silence, { |a, b| a.overlay(b) });
	}

	*signal { |fn|
		^MareaPattern { |start, end|
			if (start > end) { [] } { fn.(start).mp.(start, end) }
		}
	}

	*rand {
		^MareaPattern { |start, end|
			var arc = MareaArc(start, end);
			var startPos = start.asFloat.floor.asInt;
			var endPos = end.asFloat.ceil.asInt;

			thisThread.randSeed = (arc.midPoint * 1000000).asInteger;

			startPos.to(endPos - 1).collect { |t|
				var arc = MareaArc(t, t + 1);
				MareaEvent(arc, arc, 1.0.rand);
			}
		}
	}

	*irand { |to|
		^this.rand.withEventValue { |v| (v * to).asInteger }
	}

	*sin {
		^this.signal { |t| (sin(2*pi*t.asFloat) + 1) / 2 }
	}
}