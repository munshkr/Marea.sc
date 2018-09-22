MareaPatternPlayer {
	const <tickDuration = 0.125;

	var >pattern, >clock, >protoEvent;
	var routine;
	var isPlaying = false;

	*new { |pattern, clock, protoEvent|
		^super.newCopyArgs(pattern, clock, protoEvent).init
	}

	init {
		routine = this.prMakeRoutine
	}

	play { |clock, quant|
		if (isPlaying.not) {
			isPlaying = true;
			routine.play(clock, quant)
		}
	}

	stop   { isPlaying = false }
	pause  { isPlaying = false }
	resume { isPlaying = true }

	pattern {
		^pattern ? MareaPattern.silence
	}

	clock {
		^clock ?? { TempoClock.default }
	}

	protoEvent {
		^protoEvent ?? { Event.default }
	}

	prMakeRoutine {
		^Routine {
			loop {
				var beats, from, to;

				if (isPlaying) {
					beats = this.clock.beats;
					from = beats.asRational;
					to = (from + tickDuration).asRational;
					this.pattern.asSCEvents(from, to).do { |event|
						event.next(this.protoEvent).play
					};
				};
				tickDuration.yield
			}
		}
	}
}

MareaStreamControl : StreamControl {
	play {
		if (stream.isPlaying.not) {
			stream.play(clock)
		}
	}
}