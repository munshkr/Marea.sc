Marea {
	var string, key;

	*new { |string, key|
		^super.new.init(string, key);
	}

	init { |string_, key_|
		string = string_.asString;
		key = key_.asSymbol;
	}

	pairs {
		var parser = MareaParser.new;
		var values = parser.parse(string);
		var events = this.buildEvents(values);
		var durSeq = List[];
		var valuesSeq = List[];
		events.do { |event|
			valuesSeq.add(event[\value]);
			durSeq.add(event[\dur]);
		}
		^[key, Pseq(valuesSeq.asArray, inf), \dur, Pseq(durSeq.asArray, inf)]
	}

	buildEvents { |values dur=1|
		var events = List[];
		dur = dur / values.size;
		values.do { |value|
			if (value.isKindOf(List)) {
				events.addAll(this.buildEvents(value, dur));
			} {
				events.add((value: value.asInteger, dur: dur));
			}
		};
		^events;
	}
}

+ String {
	m { |key|
		^Marea.new(this, key).pairs;
	}
}