MareaPatternPlayer : Routine {
	classvar tickDuration = 0.125;

	*new { |pattern, clock, protoEvent|
		protoEvent = protoEvent ?? { Event.default };

		^super.new {
			loop {
				var clk = clock ? TempoClock.default;
				var beats = clk.beats;
				var from = beats.asRational;
				var to = (from + tickDuration).asRational;
				// "beats: %; from: %; to: %".format(beats, from.asFloat, to.asFloat).postln;
				pattern.asSCEvents(from, to).do { |event|
					event.next(protoEvent).play;
				};
				tickDuration.yield;
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