+ Object {
	pure { ^MareaPattern.pure(this) }
	mp { ^this.pure }

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
	cat { ^MareaPattern.cat(this) }
	fastcat { ^MareaPattern.fastcat(this) }
	mp { ^this.fastcat }
}

+ Array {
	cat { ^MareaPattern.cat(this) }
	fastcat { ^MareaPattern.fastcat(this) }
	mp { ^this.cat }
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
}