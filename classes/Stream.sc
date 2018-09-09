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

		this.source.asSCEvents(from, to).do(_.play);

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
}