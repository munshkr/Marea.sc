+ Object {
	mp { ^MareaPattern.pure(this) }

	<<* { |rpat| ^(this.mp <<* rpat) }
	*>> { |rpat| ^(this.mp *>> rpat) }
	<<+ { |rpat| ^(this.mp <<+ rpat) }
	+>> { |rpat| ^(this.mp +>> rpat) }
	<</ { |rpat| ^(this.mp <</ rpat) }
	/>> { |rpat| ^(this.mp />> rpat) }
	<<- { |rpat| ^(this.mp <<- rpat) }
	->> { |rpat| ^(this.mp ->> rpat) }
	<<% { |rpat| ^(this.mp <<% rpat) }
	%>> { |rpat| ^(this.mp %>> rpat) }
	<<** { |rpat| ^(this.mp <<** rpat) }
	**>> { |rpat| ^(this.mp **>> rpat) }
}

+ Interval {
	mp { ^MP.cat(this) }
}

+ Array {
	mp { ^MP.cat(this) }
}

+ Nil {
	mp { ^MareaPattern.silence }
}

+ Rest {
	mp { ^MareaPattern.silence }
}

+ Event {
	mp {
		var pat = nil;
		this.keysValuesDo { |key, value|
			var newPat = value.mp.withEventValue { |v| ().put(key, v) };
			pat = if (pat.isNil) { newPat } { pat.merge(newPat) }
		};
		^pat.mp
	}

	<< { |rpat| ^(this.mp << rpat) }
	>> { |rpat| ^(this.mp >> rpat) }
}

+ Rational {
	floor {
		^this.asInteger
	}
}

+ String {
	t {
		^MareaInterpreter.new.eval(this)
	}

	parseMarea {
		^MareaParser.new.parse(this)
	}
}

+ Stream {
	mp {
		var res = List[];
		this.do { |v|
			res.add(v);
			if (res.size >= MP.maxEventsInStream) { ^res.asArray.mp }
		};
		^res.asArray.mp
	}
}

+ Pattern {
	mp {
		^this.asStream.mp
	}
}