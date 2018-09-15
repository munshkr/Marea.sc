MareaASTNode {
	var <type, <token, <value;

	*new { |type, token, value|
		^super.new.newCopyArgs(type, token, value);
	}
}

MareaParser {
	var tokens, curTokenPos, curToken, curList, finished;

	const <tokenRegex = "[\\[\\]\\(\\){},%/\\*!\\?~]|[A-Za-z0-9\._\\-]+";
	const <regexes = #[
		\float, "^-?[0-9]+\\.[0-9]*$",
		\integer, "^-?[0-9]+$",
		\string, "^[A-Za-z][A-Za-z0-9\\-_]*$"
	];

	parse { |string|
		var result;

		finished = false;

		// Wrap everything inside a seq group,
		// to account for patterns without an explicit grouping:
		string = "[" ++ string ++ "]";

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
			var re = regexes.asAssociations.detect { |r| r.value.matchRegexp(string) };
			var type = if (re.isNil) { string.asSymbol } { re.key };
			(pos: pos, type: type, string: string)
		});
		tokens.add((pos: string.size, type: \end));
		^tokens;
	}

	parseExpr {
		this.parsePolyGroup;
		while { ['(', '*', '/', '!', '?'].includes(curToken[\type]) } {
			this.parseModifier
		}
	}

	parsePolyGroup {
		if (curToken[\type] == '<') {
			this.match('<');
			this.parsePolyMGroup;
			this.match('>')
		} {
			if (['{', '['].includes(curToken[\type])) {
				this.parsePolyMGroup
			}
		} {
			this.error("'<' or polyMGroup")
		}
	}

	parsePolyMGroup {
		if (curToken[\type] == '{') {
			this.match('{');
			this.parseSeqGroup;
			this.match('}');
			if (curToken[\type] == '%') {
				this.match('%');
				this.parseNumber;
			}
		} {
			if (curToken[\type] == '[') {
				this.parseSeqGroup
			}
		} {
			this.error("'{' or seqGroup")
		}
	}

	parseSeqGroup {
		this.match('[');
		this.parseSeq;
		this.match(']')
	}

	parseSeq {
		this.parseTerm;
		while { [\integer, \float, \string, '~', '<', '{', '['].includes(curToken[\type]) } {
			this.parseTerm
		};
		while { curToken[\type] == ',' } {
			this.match(',');
			this.parseSeq
		}
	}

	parseTerm {
		if ([\integer, \float, \string, '~'].includes(curToken[\type])) {
			this.parseValue;
			while { ['(', '*', '/', '!', '?'].includes(curToken[\type]) } {
				this.parseModifier
			}
		} {
			if (['<', '{', '['].includes(curToken[\type])) {
				this.parseExpr
			} {
				this.error("value or expr")
			}
		}
	}

	parseModifier {
		this.parseBjorklundMod;
	}

	parseBjorklundMod {
		if (curToken[\type] == '(') {
			this.match('(');
			this.parseExpr;
			this.match(',');
			this.parseExpr;
			this.match(')')
		} {
			if (['*', '/', '!', '?'].includes(curToken[\type])) {
				this.parseDensityMod
			} {
				this.error("'(' or other modifiers")
			}
		}
	}

	parseDensityMod {
		if (curToken[\type] == '*') {
			this.match('*');
			this.parseNumber;
		} {
			if (['/', '!', '?'].includes(curToken[\type])) {
				this.parseSparsityMod
			} {
				this.error("'*' or other modifiers")
			}
		}
	}

	parseSparsityMod {
		if (curToken[\type] == '/') {
			this.match('/');
			this.parseNumber;
		} {
			if (['!', '?'].includes(curToken[\type])) {
				this.parseReplicateMod
			} {
				this.error("'/' or other modifiers")
			}
		}
	}

	parseReplicateMod {
		if (curToken[\type] == '!') {
			this.match('!');
		} {
			if (['?'].includes(curToken[\type])) {
				this.parseDegradeMod
			} {
				this.error("'!' or other modifier")
			}
		}
	}

	parseDegradeMod {
		this.match('?');
	}

	parseValue {
		if ([\integer, \float].includes(curToken[\type])) {
			this.parseNumber
		} {
			if (curToken[\type] == \string) {
				this.parseSample
			} {
				if (curToken[\type] == '~') {
					this.parseRest
				} {
					this.error("number, sample or rest")
				}
			}
		}
	}

	parseNumber {
		if (curToken[\type] == \integer) {
			this.parseInteger
		} {
			if (curToken[\type] == \float) {
				this.parseFloat
			} {
				this.error("integer or float")
			}
		}
	}

	parseSample {
		this.parseString;
		if (curToken[\type] == ':') {
			this.match(':');
			this.parseInteger
		}
	}

	parseString {
		"String: %".format(curToken[\string]).postln;
		this.match(\string)
	}

	parseInteger {
		"Integer: %".format(curToken[\string]).postln;
		this.match(\integer)
	}

	parseFloat {
		"Float: %".format(curToken[\string]).postln;
		this.match(\float)
	}

	parseRest {
		"Rest".postln;
		this.match('~')
	}

	match { |expectedTokenType|
		var token;
		if (curToken.isNil.not and: { curToken[\type] != expectedTokenType }) {
			this.error(expectedTokenType)
		};
		token = curToken;
		if (curTokenPos < tokens.size) {
			curTokenPos = curTokenPos + 1;
			curToken = tokens[curTokenPos];
		};
		^token
	}

	error { |expectedTokens|
		Error("At %: expected % but found %".format(curTokenPos, expectedTokens, curToken[\type])).throw
	}
}