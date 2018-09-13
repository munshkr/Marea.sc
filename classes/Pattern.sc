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

MareaEvent {
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
		^MareaArc(start, end).contains(this.onset)
	}
}

MareaPattern {
	var func;

	classvar tickDuration = 0.125;

	*new { |fn|
		^super.newCopyArgs(fn);
	}

	value { |start, end|
		^func.(start, end);
	}

	mp { ^this }

	proxyControlClass { ^MareaStreamControl }
	buildForProxy { |proxy, channelOffset|
		var protoEvent = Event.default.buildForProxy(proxy, channelOffset);
		^this.asPatternPlayer(proxy.clock, protoEvent)
	}

	play { |clock, quant|
		^this.asPatternPlayer(clock).play(clock, quant)
	}

	asPatternPlayer { |clock, protoEvent|
		^MareaPatternPlayer.new(this, clock, protoEvent)
	}

	withQueryTime { |fn|
		^MareaPattern { |start, end| this.(fn.(start), fn.(end)) };
	}

	withResultTime { |fn|
		^MareaPattern { |start, end|
			this.(start, end).collect { |ev|
				var posArc = ev.positionArc, activeArc = ev.activeArc;
				posArc = MareaArc(fn.(posArc.start), fn.(posArc.end));
				activeArc = MareaArc(fn.(activeArc.start), fn.(activeArc.end));
				MareaEvent(posArc, activeArc, ev.value)
			}
		};
	}

	withEventValue { |fn|
		^MareaPattern { |start, end|
			this.(start, end).collect { |ev|
				MareaEvent(ev.positionArc, ev.activeArc, fn.(ev.value))
			}
		};
	}

	filterEventValue { |fn|
		^MareaPattern { |start, end|
			this.(start, end).select { |ev| fn.(ev.value) }
		}
	}

	splitQueries {
		^MareaPattern { |start, end|
			var cycles = MareaArc(start, end).cycles;
			var res = List[];
			cycles.do { |cycle|
				res.addAll(this.(cycle.start, cycle.end));
			};
			res.asArray;
		};
	}

	splitAtSam {
		^MareaPattern { |start, end|
			this.(start, end).collect { |ev|
				var sam = start.floor;
				var newArc = MareaArc(ev.activeArc.start.max(sam), ev.activeArc.end.min(sam + 1));
				MareaEvent(ev.positionArc, newArc, ev.value);
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

	asSCEvents { |from=0, to=1|
		var patEvents = this.seqToRelOnsetDeltas(from, to);
		var dur = (to - from).asFloat;
		var events = List[];

		patEvents.do { |ev|
			var onset = ev[0] * dur, offset = ev[1] * dur, value = ev[2];

			if (value.isKindOf(Event).not) {
				"MareaEvent values are not Events: %".format(ev).error;
			} {
				value.putPairs([\dur, (offset - onset).asFloat, \timingOffset, onset.asFloat]);
				events.add(value);
			}
		};

		^events.asArray
	}

	merge { |rpat, mergeFn|
		rpat = rpat.mp;
		^MareaPattern { |start, end|
			var events = List[];
			this.(start, end).do { |ev|
				var value;
				var revents = rpat.(ev.activeArc.start, ev.activeArc.end);
				revents = revents.select { |ev| ev.activeArc.contains(ev.positionArc.start) };
				value = ev.value;
				revents.do { |rev|
					value = if (value.isKindOf(Event) && rev.value.isKindOf(Event)) {
						value.merge(rev.value, mergeFn)
					} {
						if (value.isKindOf(Event)) {
							value.collect { |v| mergeFn.(v, rev.value) }
						} {
							if (rev.value.isKindOf(Event)) {
								rev.value.collect { |v| mergeFn.(ev.value, v) }
							} {
								mergeFn.(value, rev.value)
							}
						}
					}
				};
				events.add(MareaEvent(ev.positionArc, ev.activeArc, value));
			};
			events.asArray;
		}
	}

	<< { |rpat| ^this.merge(rpat, { |a, b| b }) }
	// >> { |rpat| ^this.mergeRight(rpat, { |a, b| a }) }
	<<* { |rpat| ^this.merge(rpat, { |a, b| a * b }) }
	* { |rpat| ^(this <<* rpat) }
	// *>> { |rpat| ^this.mergeRight(rpat, { |a, b| a * b }) }
	+ { |rpat| ^(this <<+ rpat) }
	<<+ { |rpat| ^this.merge(rpat, { |a, b| a + b }) }
	// +>> { |rpat| ^this.mergeRight(rpat, { |a, b| a + b }) }
	/ { |rpat| ^(this <</ rpat) }
	<</ { |rpat| ^this.merge(rpat, { |a, b| a / b }) }
	// />> { |rpat| ^this.mergeRight(rpat, { |a, b| a / b }) }
	- { |rpat| ^(this <<- rpat); }
	<<- { |rpat| ^this.merge(rpat, { |a, b| a - b }) }
	// ->> { |rpat| ^this.mergeRight(rpat, { |a, b| a - b }) }
	% { |rpat| ^(this <<% rpat); }
	<<% { |rpat| ^this.merge(rpat, { |a, b| a % b }) }
	// %>> { |rpat| ^this.mergeRight(rpat, { |a, b| a % b }) }
	** { |rpat| ^(this <<** rpat); }
	<<** { |rpat| ^this.merge(rpat, { |a, b| a ** b }) }
	// **>> { |rpat| ^this.mergeRight(rpat, { |a, b| a ** b }) }

	density { |value|
		^this.withQueryTime { |t| t * value }.withResultTime { |t| t / value };
	}
	fast { |v| ^this.density(v) }

	sparsity { |value|
		^this.density(Rational(1, value));
	}
	slow { |v| ^this.sparsity(v) }

	overlay { |pattern|
		^MareaPattern { |start, end|
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
		^MareaPattern { |start, end|
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
		^(this <> MareaPattern.rand)
		.filterEventValue { |v| v[v.size - 1] > value }
		.withEventValue { |v|
			var res = v[0..v.size-2];
			if (res.size == 1) { res = res[0] };
			res
		}
	}

	brak {
		^this.when({ |s| s % 2 == 1 }, { |p| [p, MareaPattern.silence].fastcat.rotRight(1 %/ 4) })
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
		^MareaPattern { |start, end|
			this.(start, end).select { |event| fn.(event.onset) }
		}
	}

	discretize { |num|
		// The left pattern "0.mp" is used to build the "structure".
		// The 0 value is an arbitrary value, and will be overwritten by the merge operator <<.
		^0.mp.density(num) << this
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

MP : MareaPattern {}