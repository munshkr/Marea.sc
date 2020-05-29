+ MareaPattern {
	*pure { |atom|
		^MP { |start, end|
			var startPos, endPos;
			start.isNumber.not.if { Error("start must be a number").throw };
			end.isNumber.not.if { Error("end must be a number").throw };

			startPos = start.asFloat.floor.asInteger;
			endPos = end.asFloat.ceil.asInteger;

			startPos.to(endPos - 1).collect { |t|
				var arc = MareaArc(t, t+1);
				MareaEvent(arc, arc, atom);
			}
		}
	}

	*silence {
		^MP { [] }
	}

	*cat { |array|
		^MP { |start, end|
			var l = array.size;
			var r = start.floor;
			var n = r % l;
			var p = array[n].mp;
			var offset = r - ((r - n).div(l));
			p.withResultTime { |t| t + offset }.(start - offset, end - offset);
		}.splitQueries;
	}

	*fastcat { |array|
		^MP.cat(array).fast(array.size);
	}

	*stack { |patterns|
		^patterns.inject(MP.silence, { |a, b| a.overlay(b) });
	}

	*signal { |fn|
		^MP { |start, end|
			if (start > end) { [] } { fn.(start).mp.(start, end) }
		}
	}

	*rand {
		^MP { |start, end|
			var arc = MareaArc(start, end);
			var startPos = start.asFloat.floor.asInteger;
			var endPos = end.asFloat.ceil.asInteger;

			thisThread.randSeed = (arc.midPoint * 1000000).asInteger;

			startPos.to(endPos - 1).collect { |t|
				var arc = MareaArc(t, t + 1);
				MareaEvent(arc, arc, 1.0.rand);
			}
		}
	}

	*irand { |to|
		^MP.rand.withEventValue { |v| (v * to).asInteger }
	}

	*sine {
		^MP.signal { |t| (sin(2*pi*t.asFloat) + 1) / 2 }
	}

	*cosine {
		^this.sine.rotRight(0.25)
	}

	*saw {
		^MP.signal { |t| t.asFloat % 1 }
	}
}
