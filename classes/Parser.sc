Token {
	var pos, type, value;

	*new { |pos, type=nil, string=nil|
		^super.new.init(pos, type, string);
	}

	init { |pos_, type_, string_|
		pos = pos_;
		type = type_ ?? { \end };
		type = type.asSymbol;
		value = string_ ?? { type_ };

		if (type == \number) {
			value = string_.interpret;
		}
	}

	pos { ^pos }
	type { ^type }
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
		while { [\number, '['].includes(curToken.type) } {
			this.parseTerm;
		};
	}

	parseTerm {
		if (curToken.type == \number) {
			var t = this.match(\number);
			curList.add(t.value);
		} {
			if (curToken.type == '[') {
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

	match { |expectedTokenType|
		var token;
		if (curToken.isNil.not and: { curToken.type != expectedTokenType }) {
            Error("Expected '%'".format(expectedTokenType)).throw;
		};
        token = curToken;
		if (curTokenPos < (tokens.size - 1)) {
            curTokenPos = curTokenPos + 1;
			curToken = tokens[curTokenPos];
		};
		^token
	}
}