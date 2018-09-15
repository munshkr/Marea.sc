+ MareaPattern {
	density { |value|
		^this.withQueryTime { |t| t * value }.withResultTime { |t| t / value };
	}
	fast { |v| ^this.density(v) }

	sparsity { |value|
		^this.density(Rational(1, value));
	}
	slow { |v| ^this.sparsity(v) }

	overlay { |pattern|
		^MP { |start, end|
			this.(start, end) ++ pattern.mp.(start, end)
		}
	}

	rotLeft { |value|
		^this.withResultTime { |t| t - value }.withQueryTime { |t| t + value };
	}
	rotL { |value| ^this.rotLeft(value) }

	rotRight { |value|
		^this.rotLeft(0 - value);
	}
	rotR { |v| ^this.rotR(v) }

	when { |testFn, fn|
		^MP { |start, end|
			testFn.(start.floor).if {
				fn.(this).(start, end)
			} {
				this.(start, end)
			}
		}.splitQueries;
	}

	every { |num, fn|
		^this.when({ |t| (t % num == 0) }, fn);
	}

	whenmod { |a, b, fn|
		^this.when({ |t| (t % a) >= b }, fn)
	}

	degradeBy { |value|
		^this.merge(MP.rand, { |a, b| [a, b] })
		.filterEventValue { |v|
			if (v.isKindOf(Event)) { v = v[v.keys.asArray[0]] };
			v[v.size - 1] > value
		}
		.withEventValue { |v|
			var res;
			if (v.isKindOf(Event)) { v = v[v.keys.asArray[0]] };
			res = v[0..v.size-2];
			if (res.size == 1) { res = res[0] };
			res
		}
	}

	brak {
		^this.when({ |s| s % 2 == 1 }, { |p| [p, MP.silence].fastcat.rotRight(1 %/ 4) })
	}

	degrade {
		^this.degradeBy(0.5)
	}

	within { |start, end, fn|
		^MP.stack([
			fn.(this).playWhen { |t| var cp = t - t.floor; (cp >= start) && (cp < end) },
			this.playWhen { |t| var cp = t - t.floor; ((cp >= start) && (cp < end)).not }
		])
	}

	playWhen { |fn|
		^MP { |start, end|
			this.(start, end).select { |event| fn.(event.onset) }
		}
	}

	discretize { |num|
		// The left pattern "0.mp" is used to build the "structure".
		// The 0 value is an arbitrary value, and will be overwritten by the merge operator <<.
		^0.mp.density(num) << this
	}
}