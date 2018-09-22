/*
TestPatternGenerators.run
*/

TestPattern : UnitTest {
	e { |posArc, activeArc, value|
		^MareaEvent(posArc, activeArc, value)
	}

	a { |start, end|
		^MareaArc(start, end)
	}
}

TestPatternGenerators : TestPattern {
	test_pure {
		var subject = MP.pure(42);
		this.assert(subject.isKindOf(MareaPattern));
		this.assertEquals(subject.(0, 1), [MareaEvent(MareaArc(0, 1), MareaArc(0, 1), 42)]);
		this.assertEquals(subject.(0, 2), [
			MareaEvent(MareaArc(0, 1), MareaArc(0, 1), 42),
			MareaEvent(MareaArc(1, 2), MareaArc(1, 2), 42)
		])
	}

	test_silence {
		var subject = MP.silence;
		this.assert(subject.isKindOf(MareaPattern));
		this.assertEquals(subject.(0, 1), [])
	}
}