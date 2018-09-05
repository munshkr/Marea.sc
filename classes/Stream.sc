MareaStream {
	var <>clock;
	var <source;

	var <isPlaying;
	var <isThreadRunning;
	var <>isTracing;

	classvar tickDuration = 0.125;

	*new { |clock|
		^super.new.init(clock);
	}

	init { |clk|
		clock = clk ? TempoClock.default;
		source = nil.mp;
		isThreadRunning = false;
		isPlaying = false;
		isTracing = false;
		CmdPeriod.add(this);
	}

	source_ { |newPattern|
		source = newPattern.mp;
		this.play;
	}
	src_ { ^this.source_ }
	src { ^this.source }

	play {
		if (isThreadRunning) { ^this };
		isThreadRunning = true;
		isPlaying = true;
		clock.play(this, tickDuration);
	}

	next { |beats|
		var from = beats.asRational;
		var to = (from + tickDuration).asRational;
		// "beats: %; from: %; to: %".format(beats, from.asFloat, to.asFloat).postln;

		this.prPlayEvents(from, to);

		^if (isPlaying) {
			tickDuration;
		} {
			isThreadRunning = false;
			nil;
		};
	}

	stop {
		isPlaying = false;
	}

	cmdPeriod {
		isPlaying = false;
		isThreadRunning = false;
	}

	prPlayEvents { |from, to|
		var patEvents = this.source.seqToRelOnsetDeltas(from, to);
		var eventsPerOnset = Dictionary.new;

		patEvents.do { |ev|
			var start = ev[0] * tickDuration, end = ev[1] * tickDuration, values = ev[2];

			if (values.isKindOf(List).not.or { values.any { |v| v.isKindOf(Association).not } }) {
				"Events are not list of associations: %".format(ev).error;
			} {
				if (eventsPerOnset.at(start).isNil) {
					eventsPerOnset.put(start, (dur: (end - start).asFloat));
				};
				eventsPerOnset[start].addAll(values);
			}
		};

		if (eventsPerOnset.isEmpty.not) {
			eventsPerOnset.keysValuesDo { |start, event|
				clock.sched(start.asFloat, {
					if (isTracing) { event.postln };
					event.play;
				});
			};
		};
	}
}