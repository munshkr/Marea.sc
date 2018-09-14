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

		this.parseRoot;
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

	parseRoot {
		if (['<', '{', '['].includes(curToken[\type])) {
			this.parseExpr
		} {
			if ([\float, \integer, \string, '~'].includes(curToken[\type])) {
				this.parseSeq
			}
		} {
			"ERROR: Expected expr or seq".error
		};
	}

	parseExpr {
		if (curToken[\type] == '<') {
			this.match('<');
			this.parsePolym;
			this.match('>')
		} {
			if (['{', '['].includes(curToken[\type])) {
				this.parsePolym
			}
		} {
			"ERROR: Expected '<' or polym".error
		}
	}

	parsePolym {
		if (curToken[\type] == '{') {
			this.match('{');
			this.parseGroup;
			this.match('}');
			if (curToken[\type] == '%') {
				this.match('%');
				this.parseNumber;
			}
		} {
			if (curToken[\type] == '[') {
				this.parseGroup
			}
		} {
			"ERROR: Expected '{' or group".error
		}
	}

	parseGroup {
		this.match('[');
		this.parseSeq;
		this.match(']')
	}

	parseSeq {
		this.parseTermMod;
		while { [\integer, \float, \string, '~', '<', '{', '['].includes(curToken[\type]) } {
			this.parseTermMod
		}
	}

	parseTermMod {
		this.parseTerm;
		while { ['(', '*', '/', '!', '?'].includes(curToken[\type]) } {
			this.parseModifier
		}
	}

	parseTerm {
		if ([\integer, \float, \string, '~'].includes(curToken[\type])) {
			this.parseValue
		} {
			if (['<', '{', '['].includes(curToken[\type])) {
				this.parseExpr
			} {
				"ERROR: Expected value or expr".error
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
				"ERROR: Expected '(' or density modifier".error
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
				"ERROR: Expected '*' or sparsity modifier".error
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
				"ERROR: Expected '/' or replicate modifier".error
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
				"ERROR: Expected '!' or degrade modifier".error
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
					"ERROR: Expected number, sample or rest".error
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
				"ERROR: Expected integer or float".error
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
			Error("Expected '%', but found '%'".format(expectedTokenType, curToken[\type])).throw;
		};
        token = curToken;
		if (curTokenPos < tokens.size) {
            curTokenPos = curTokenPos + 1;
			curToken = tokens[curTokenPos];
		};
		^token
	}
}