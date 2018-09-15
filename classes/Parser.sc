MareaASTNode {
	var <type, <value;

	*new { |type, value|
		^super.newCopyArgs(type, value)
	}

	printOn { | stream |
		stream << "(" << type << " " << value << ")";
	}

	== { |that|
		^this.compareObject(that, #[\type, \value])
	}

	hash {
		^this.instVarHash(#[\type, \value])
	}
}

MareaParser {
	var tokens, curTokenPos, curToken, curNode, finished;

	const <tokenRegex = "[\\[\\]\\(\\){},%/\\*!\\?~]|[A-Za-z0-9\._\\-]+";
	const <regexes = #[
		\float, "^-?[0-9]+\\.[0-9]*$",
		\integer, "^-?[0-9]+$",
		\string, "^[A-Za-z][A-Za-z0-9\\-_]*$"
	];

	parse { |string|
		var expr;

		finished = false;

		// Wrap everything inside a seq group,
		// to account for patterns without an explicit grouping:
		string = "[" ++ string ++ "]";

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
		if (curToken[\type] == '<') {
			var body;
			this.match('<');
			body = this.parsePolyMGroup;
			this.match('>')
			^MareaASTNode(\polyGroup, body)
		} {
			if (['{', '['].includes(curToken[\type])) {
				^this.parsePolyMGroup
			}
		} {
			this.error("'<' or polyMGroup")
		}
	}

	parsePolyMGroup {
		if (curToken[\type] == '{') {
			var body, mod;
			this.match('{');
			body = this.parseSeqGroup;
			this.match('}');
			if (curToken[\type] == '%') {
				this.match('%');
				mod = this.parseNumber;
			}
			^MareaASTNode(\polyMGroup, (body: body, mod: mod))
		} {
			if (curToken[\type] == '[') {
				^this.parseSeqGroup
			}
		} {
			this.error("'{' or seqGroup")
		}
	}

	parseSeqGroup {
		var body;
		this.match('[');
		body = this.parseSeq;
		this.match(']')
		^MareaASTNode(\seqGroup, body)
	}

	parseSeq {
		var children = List[];
		var terms = List[];
		terms.add(this.parseTerm);
		while { [\integer, \float, \string, '~', '<', '{', '['].includes(curToken[\type]) } {
			terms.add(this.parseTerm)
		};
		while { curToken[\type] == ',' } {
			this.match(',');
			children.add(this.parseSeq)
		}
		^MareaASTNode(\seq, (terms: terms, children: children))
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
			var x, y;
			this.match('(');
			x = this.parseExpr;
			this.match(',');
			y = this.parseExpr;
			this.match(')');
			^MareaASTNode(\bjorklundMod, (x: x, y: y))
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
			this.match('!');
			^MareaASTNode(\replicateMod)
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
				^this.parseSample
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

	parseSample {
		var name, index;
		name = this.parseString;
		if (curToken[\type] == ':') {
			this.match(':');
			index = this.parseInteger
		};
		^MareaASTNode(\sample, (name: name, index: index))
	}

	parseString {
		this.match(\string)
		^MareaASTNode(\string, curToken[\string])
	}

	parseInteger {
		this.match(\integer)
		^MareaASTNode(\integer, curToken[\string].asInt)
	}

	parseFloat {
		this.match(\float)
		^MareaASTNode(\integer, curToken[\string].asFloat)
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
		Error("At %: expected % but found %".format(curTokenPos, expectedTokens, curToken[\type])).throw
	}
}