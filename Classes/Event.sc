MareaEvent {
	var <wholeArc, <partArc, <value;

	*new { |wholeArc, partArc, value|
		^super.newCopyArgs(wholeArc, partArc, value);
	}

	printOn { | stream |
		stream << "E(" << wholeArc << ", " << partArc << ", " << value.asString << ")";
	}

	onset {
		^wholeArc.start;
	}

	offset {
		^wholeArc.end;
	}

	start {
		^partArc.start;
	}

	hasOnset {
		^(wholeArc.start == partArc.start);
	}

	hasOffset {
		^(wholeArc.end == partArc.end);
	}

	onsetIn { |start, end|
		^MareaArc(start, end).contains(this.onset)
	}

	== { |that|
		^this.compareObject(that, #[\wholeArc, \partArc, \value])
	}

	hash {
		^this.instVarHash(#[\wholeArc, \partArc, \value])
	}
}