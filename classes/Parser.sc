Token {
	var pos, name, value;

	*new { |pos, name=nil, string=nil|
		^super.new.init(pos, name, string);
	}

	init { |pos_, name_, string_|
		pos = pos_;
		name = name_ ?? { \end };
		name = name.asSymbol;
		value = string_ ?? { name_ };

		if (name == \number) {
			value = string_.interpret;
		}
	}

	pos { ^pos }
	name { ^name }
	value { ^value }
}

Parser {
	var tokens, curTokenPos, curToken, curList, finished;

	parse { |string|
		var result = List.new;
		finished = false;
		curList = result;
		tokens = this.tokenize(string);
		curTokenPos = 0;
		curToken = tokens[curTokenPos];
		this.parseExpr;
		this.match(\end);
		^result;
	}

	tokenize { |string|
		var tokenStrings = string.findRegexp("[0-9A-Za-z]+|[^ ]");
		tokenStrings = tokenStrings.collect { |elem|
			var pos = elem[0];
			var string = elem[1];

			if ("[0-9]+".matchRegexp(string)) {
				Token(pos, \number, string)
			} {
				Token(pos, string)
			};
		};
		tokenStrings.add(Token(string.size));
		^tokenStrings;
	}

	parseExpr {
		while { [\number, '['].includes(curToken.name) } {
			this.parseTerm;
		};
	}

	parseTerm {
		if (curToken.name == \number) {
			var t = this.match(\number);
			curList.add(t.value);
		} {
			if (curToken.name == '[') {
				var newList = List.new, oldList;
				this.match('[');
				curList.add(newList);
				oldList = curList;
				curList = newList;
				this.parseExpr;
				curList = oldList;
				this.match(']');
			}
		}
	}

	match { |expectedTokenName|
		var token;
		if (curToken.isNil.not and: { curToken.name != expectedTokenName }) {
            Error("Expected '%'".format(expectedTokenName)).throw;
		};
        token = curToken;
		if (curTokenPos < (tokens.size - 1)) {
            curTokenPos = curTokenPos + 1;
			curToken = tokens[curTokenPos];
			//"curTokenPos % curToken.value % tokens.size %".format(curTokenPos, curToken.value, tokens.size).postln;
		};
		^token
	}
}