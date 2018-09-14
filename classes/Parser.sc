MareaASTNode {
	var <type, <token, <value;

	*new { |type, token, value|
		^super.new.newCopyArgs(type, token, value);
	}
}

MareaParser {
	var tokens, curTokenPos, curToken, curList, finished;

	const <tokenRegex = "[\\[\\]\\(\\){},%/\\*!\\?]|[A-Za-z0-9\._\\-]+";
	const <regexes = #[
		\float, "^-?[0-9]+\\.[0-9]*$",
		\integer, "^-?[0-9]+$",
		\string, "^[A-Za-z][A-Za-z0-9\\-_]*$"
	];

	parse { |string|
		var result;

		finished = false;

		tokens = this.tokenize(string);
		curTokenPos = 0;
		curToken = tokens[curTokenPos];

		this.parseExpr;
		this.match(\end);

		^result;
	}

	tokenize { |string|
		var tokenStrings = string.findRegexp(tokenRegex);
		var tokens = List.newFrom(tokenStrings.collect { |elem|
			var pos = elem[0];
			var string = elem[1];
			var re = regexes.asAssociations.detect { |r| r.matchRegexp(string) };
			var type = if (re.isNil) { string.asSymbol } { re.key };
			(pos: pos, type: type, string: string)
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