MareaPattern {
	var func;

	const <maxEventsInStream = 128;

	*new { |fn|
		^super.newCopyArgs(fn)
	}

	value { |start, end|
		^func.(start, end)
	}

	mp { ^this }

	play { |clock, quant|
		^this.asPatternPlayer(clock).play(clock, quant)
	}

	asPatternPlayer { |clock, protoEvent|
		^MareaPatternPlayer.new(this, clock, protoEvent)
	}

	withQueryTime { |fn|
		^MareaPattern { |start, end| this.(fn.(start), fn.(end)) }
	}

	withResultTime { |fn|
		^MareaPattern { |start, end|
			this.(start, end).collect { |ev|
				var posArc = ev.positionArc, activeArc = ev.activeArc;
				posArc = MareaArc(fn.(posArc.start), fn.(posArc.end));
				activeArc = MareaArc(fn.(activeArc.start), fn.(activeArc.end));
				MareaEvent(posArc, activeArc, ev.value)
			}
		}
	}

	withEventValue { |fn|
		^MareaPattern { |start, end|
			this.(start, end).collect { |ev|
				MareaEvent(ev.positionArc, ev.activeArc, fn.(ev.value))
			}
		}
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
			res.asArray
		}
	}

	splitAtSam {
		^MareaPattern { |start, end|
			this.(start, end).collect { |ev|
				var sam = start.floor;
				var newArc = MareaArc(ev.activeArc.start.max(sam), ev.activeArc.end.min(sam + 1));
				MareaEvent(ev.positionArc, newArc, ev.value)
			}
		}.splitQueries
	}

	seqToRelOnsetDeltas { |s, e|
		^this.(s, e).select { |event|
			(event.onsetIn(s, e)) && (event.onset >= event.start)
		}.collect { |event|
			var s_ = event.positionArc.start;
			var e_ = event.positionArc.end;
			[(s_ - s) / (e - s), (e_ - s) / (e - s), event.value]
		}
	}

	asSCEvents { |from=0, to=1|
		var patEvents = this.seqToRelOnsetDeltas(from, to);
		var dur = (to - from).asFloat;
		var events = List[];

		patEvents.do { |ev|
			var onset = ev[0] * dur, offset = ev[1] * dur, value = ev[2];

			if (value.isKindOf(Event).not) {
				"MareaEvent values are not Events: %".format(ev).error
			} {
				value.putPairs([\dur, (offset - onset).asFloat, \timingOffset, onset.asFloat]);
				events.add(value)
			}
		};

		^events.asArray
	}

	merge { |rpat, mergeFn|
		^this.prMerge(rpat, mergeFn)
	}

	mergeRight { |rpat, mergeFn|
		^this.prMerge(rpat, mergeFn, true)
	}

	prMerge { |rpat, mergeFn, invert=false|
		var srcPat, dstPat;

		rpat = rpat.mp;
		mergeFn = mergeFn ? { |a, b| b };

		srcPat = rpat; dstPat = this;
		if (invert) { srcPat = this; dstPat = rpat };

		^MareaPattern { |start, end|
			var events = List[];
			dstPat.(start, end).do { |ev|
				var value;
				var srcEvents = srcPat.(ev.activeArc.start, ev.activeArc.end);
				srcEvents = srcEvents.select { |ev| ev.activeArc.contains(ev.positionArc.start) };
				value = ev.value;
				srcEvents.do { |srcEv|
					value = if (value.isKindOf(Event) && srcEv.value.isKindOf(Event)) {
						value.merge(srcEv.value, mergeFn)
					} {
						mergeFn.(value, srcEv.value)
					}
				};
				events.add(MareaEvent(ev.positionArc, ev.activeArc, value))
			};
			events.asArray
		}
	}

	<< { |rpat| ^this.merge(rpat, { |a, b| b }) }
	>> { |rpat| ^this.mergeRight(rpat, { |a, b| a }) }
	<<* { |rpat| ^this.merge(rpat, { |a, b| a * b }) }
	* { |rpat| ^(this <<* rpat) }
	*>> { |rpat| ^this.mergeRight(rpat, { |a, b| a * b }) }
	+ { |rpat| ^(this <<+ rpat) }
	<<+ { |rpat| ^this.merge(rpat, { |a, b| a + b }) }
	+>> { |rpat| ^this.mergeRight(rpat, { |a, b| a + b }) }
	/ { |rpat| ^(this <</ rpat) }
	<</ { |rpat| ^this.merge(rpat, { |a, b| a / b }) }
	/>> { |rpat| ^this.mergeRight(rpat, { |a, b| a / b }) }
	- { |rpat| ^(this <<- rpat); }
	<<- { |rpat| ^this.merge(rpat, { |a, b| a - b }) }
	->> { |rpat| ^this.mergeRight(rpat, { |a, b| a - b }) }
	% { |rpat| ^(this <<% rpat); }
	<<% { |rpat| ^this.merge(rpat, { |a, b| a % b }) }
	%>> { |rpat| ^this.mergeRight(rpat, { |a, b| a % b }) }
	** { |rpat| ^(this <<** rpat); }
	<<** { |rpat| ^this.merge(rpat, { |a, b| a ** b }) }
	**>> { |rpat| ^this.mergeRight(rpat, { |a, b| a ** b }) }
}

MP : MareaPattern {
	printOn { |stream| stream << "a MareaPattern" }
}