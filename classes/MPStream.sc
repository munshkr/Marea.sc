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
		var tick = 1 / ticksPerCycle;

		if (isThreadRunning) { ^this };
		isThreadRunning = true;

		isPlaying = true;
		tempoclock.sched(tick, { |quant|
			var events = this.source.seqToRelOnsetDeltas(quant, quant + tick);
			events.postln;
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
}