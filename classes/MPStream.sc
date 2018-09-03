MPStream {
	var <tempoclock;
	var <source;
	var <isPlaying;
	var isThreadRunning;

	classvar ticksPerCycle = 8;

	*new { |tempoclock=nil|
		if (tempoclock.isNil) { tempoclock = TempoClock.default };
		^super.new.init(tempoclock);
	}

	init { |tc|
		tempoclock = tc;
		source = nil.mp; // silence
		isThreadRunning = false;
		isPlaying = false;
		CmdPeriod.add(this);
	}

	value { |newPattern|
		this.source = newPattern;
	}

	source_ { |newPattern|
		source = newPattern.mp;
		this.play;
	}
	src_ { ^this.source_ }

	play {
		var tick = 1 % ticksPerCycle;

		if (isThreadRunning) { ^this };
		isThreadRunning = true;

		isPlaying = true;
		tempoclock.sched(tick, { |quant|
			var ticks = (quant / 8).floor;
			this.prPlayTick(ticks);

			if (isPlaying) {
				tick;
			} {
				isThreadRunning = false;
				nil;
			};
		});
	}

	stop {
		isPlaying = false;
	}

	cmdPeriod {
		isPlaying = false;
		isThreadRunning = false;
	}

	prPlayTick { |ticks|
		var patEvents, eventsPerOnset;
		var a = ticks % ticksPerCycle;
		var b = (ticks + 1) % ticksPerCycle;

		"ticks: %".format(ticks).postln;
		patEvents = this.source.seqToRelOnsetDeltas(a, b);

		eventsPerOnset = Dictionary.new;
		patEvents.do { |ev|
			var start = ev[0], end = ev[1], values = ev[2];
			if (eventsPerOnset.at(start).isNil) {
				eventsPerOnset.put(start, (dur: (end - start).asFloat));
			};
			eventsPerOnset[start].addAll(values);
		};

		if (eventsPerOnset.isEmpty.not) {
			// eventsPerOnset.postln;
			eventsPerOnset.keysValuesDo { |start, event|
				tempoclock.sched(start.asFloat, { event.postln; event.play });
			};
		};
	}
}