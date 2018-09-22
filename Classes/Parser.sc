MareaParser {
	var tokens, curTokenPos, curToken, curString, finished;

	const <tokenRegex = "[<>{}\\[\\]\\(\\),%/\\*!\\?~]|[A-Za-z0-9\._\\-:]+";
	const <regexes = #[
		\float, "^-?[0-9]+\\.[0-9]*$",
		\integer, "^-?[0-9]+$",
		\string, "^[A-Za-z][A-Za-z0-9\\-_:]*$"
	];

	parse { |string|
		var expr;

		finished = false;

		// Wrap everything inside a seq group,
		// to account for patterns without an explicit grouping:
		string = "[" ++ string ++ "]";
		curString = string;

		tokens = this.tokenize(string);
		curTokenPos = 0;
		curToken = tokens[curTokenPos];

		expr = this.parseExpr;
		this.match(\end);

		^expr;
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
		var modifiers = List[];
		var group = this.parseGroup;
		while { ['(', '*', '/', '!', '?'].includes(curToken[\type]) } {
			modifiers.add(this.parseModifier)
		}
		^MareaASTNode(\expr, (group: group, modifiers: modifiers))
	}

	parseGroup {
		^this.parsePolyGroup
	}

	parsePolyGroup {
		if (curToken[\type] == '{') {
			var body, mod;
			this.match('{');
			body = this.parseGroupBody;
			this.match('}');
			if (curToken[\type] == '%') {
				this.match('%');
				mod = this.parseNumber;
			}
			^MareaASTNode(\polyGroup, (body: body, mod: mod))
		} {
			if (curToken[\type] == '<') {
				var body;
				this.match('<');
				body = this.parseGroupBody;
				this.match('>')
				^MareaASTNode(\polyGroup, (body: body, mod: MareaASTNode(\integer, 1)))
			} {
				if (curToken[\type] == '[') {
					^this.parseSeqGroup
				} {
					this.error("'{', '<' or '['")
				}
			}
		}
	}

	parseSeqGroup {
		var body;
		this.match('[');
		body = this.parseGroupBody;
		this.match(']')
		^MareaASTNode(\seqGroup, body)
	}

	parseGroupBody {
		var terms = List[];
		var sibling;
		terms.add(this.parseTerm);
		while { [\integer, \float, \string, '~', '<', '{', '['].includes(curToken[\type]) } {
			terms.add(this.parseTerm)
		};
		if (curToken[\type] == ',') {
			this.match(',');
			sibling = this.parseGroupBody
		}
		^MareaASTNode(\groupBody, (terms: terms, sibling: sibling))
	}

	parseTerm {
		if ([\integer, \float, \string, '~'].includes(curToken[\type])) {
			var value, modifiers = List[];
			value = this.parseValue;
			while { ['(', '*', '/', '!', '?'].includes(curToken[\type]) } {
				modifiers.add(this.parseModifier)
			}
			^MareaASTNode(\term, (value: value, modifiers: modifiers))
		} {
			if (['<', '{', '['].includes(curToken[\type])) {
				^this.parseExpr
			} {
				this.error("value or expr")
			}
		}
	}

	parseModifier {
		^this.parseBjorklundMod;
	}

	parseBjorklundMod {
		if (curToken[\type] == '(') {
			var x, y, z;
			this.match('(');
			x = this.parseNumber;
			this.match(',');
			y = this.parseNumber;
			if (curToken[\type] == ',') {
				this.match(',');
				z = this.parseNumber
			};
			this.match(')');
			^MareaASTNode(\bjorklundMod, (x: x, y: y, z: z))
		} {
			if (['*', '/', '!', '?'].includes(curToken[\type])) {
				^this.parseDensityMod
			} {
				this.error("'(' or other modifiers")
			}
		}
	}

	parseDensityMod {
		if (curToken[\type] == '*') {
			var value;
			this.match('*');
			value = this.parseNumber;
			^MareaASTNode(\densityMod, value)
		} {
			if (['/', '!', '?'].includes(curToken[\type])) {
				^this.parseSparsityMod
			} {
				this.error("'*' or other modifiers")
			}
		}
	}

	parseSparsityMod {
		if (curToken[\type] == '/') {
			var value;
			this.match('/');
			value = this.parseNumber;
			^MareaASTNode(\sparsityMod, value)
		} {
			if (['!', '?'].includes(curToken[\type])) {
				^this.parseReplicateMod
			} {
				this.error("'/' or other modifiers")
			}
		}
	}

	parseReplicateMod {
		if (curToken[\type] == '!') {
			var repetitions = 1;
			this.match('!');
			while { curToken[\type] == '!' } {
				repetitions = repetitions + 1;
				this.match('!');
			}
			^MareaASTNode(\replicateMod, repetitions)
		} {
			if (['?'].includes(curToken[\type])) {
				^this.parseDegradeMod
			} {
				this.error("'!' or other modifier")
			}
		}
	}

	parseDegradeMod {
		this.match('?');
		^MareaASTNode(\degradeMod)
	}

	parseValue {
		if ([\integer, \float].includes(curToken[\type])) {
			^this.parseNumber
		} {
			if (curToken[\type] == \string) {
				^this.parseString
			} {
				if (curToken[\type] == '~') {
					^this.parseRest
				} {
					this.error("number, sample or rest")
				}
			}
		}
	}

	parseNumber {
		if (curToken[\type] == \integer) {
			^this.parseInteger
		} {
			if (curToken[\type] == \float) {
				^this.parseFloat
			} {
				this.error("integer or float")
			}
		}
	}

	parseString {
		var value;
		value = curToken[\string];
		this.match(\string);
		^MareaASTNode(\string, value)
	}

	parseInteger {
		var value;
		value = curToken[\string].asInt;
		this.match(\integer);
		^MareaASTNode(\integer, value)
	}

	parseFloat {
		var value;
		value = curToken[\string].asFloat;
		this.match(\float);
		^MareaASTNode(\integer, value)
	}

	parseRest {
		this.match('~')
		^MareaASTNode(\rest)
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
		var tokenString = tokens.collect(_[\string]).join(" ");
		var msg = "%\n".format(tokenString);
		msg = msg ++ " ".dup(curTokenPos * 2).join ++ "^\n";
		Error("Parser error at %: expected '%' but found '%'\n%".format(curTokenPos, expectedTokens, curToken[\type], msg)).throw
	}
}

MareaASTNode {
	var <type, <value;

	*new { |type, value|
		^super.newCopyArgs(type, value)
	}

	printOn { |stream|
		stream << "(" << type << " " << value << ")";
	}

	evalWith { |interpreter|
		var method = "eval_%".format(type).asSymbol;
		^interpreter.perform(method, this)
	}

	== { |that|
		^this.compareObject(that, #[\type, \value])
	}

	hash {
		^this.instVarHash(#[\type, \value])
	}
}