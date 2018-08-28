MareaParser {
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
		var tokens = List.newFrom(tokenStrings.collect { |elem|
			var pos = elem[0];
			var string = elem[1];

			if ("[0-9]+".matchRegexp(string)) {
				(pos: pos, type: \number, string: string)
			} {
				(pos: pos, type: string.asSymbol, string: string)
			};
		});
		tokens.add((pos: string.size, type: \end));
		^tokens;
	}

	parseExpr {
		while { [\number, '['].includes(curToken[\type]) } {
			this.parseTerm;
		};
	}

	parseTerm {
		if (curToken[\type] == \number) {
			var t = this.match(\number);
			curList.add(t[\string]);
		} {
			if (curToken[\type] == '[') {
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
		if (curToken.isNil.not and: { curToken[\type] != expectedTokenType }) {
            Error("Expected '%'".format(expectedTokenType)).throw;
		};
        token = curToken;
		if (curTokenPos < tokens.size) {
            curTokenPos = curTokenPos + 1;
			curToken = tokens[curTokenPos];
		};
		^token
	}
}
