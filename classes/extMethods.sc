+ Object {
	mp {
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
	cat {
		^MPPattern { |start, end|
			var l = this.size;
			var r = start.floor;
			var n = r % l;
			var p = this[n].mp;
			var offset = r - ((r - n).div(l));
			p.withResultTime { |t| t + offset }.(start - offset, end - offset);
		};
	}

	fastcat {
		^this.cat.fast(this.size);
	}
}

+ Rest {
	mp {
		^MPPattern { [] }
	}
}