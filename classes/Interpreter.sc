MareaInterpreter {
	var parser;

	*new {
		^super.new.init
	}

	init {
		parser = MareaParser.new
	}

	eval { |string|
		var node;
		node = parser.parse(string);
		^node.evalWith(this)
	}

	eval_expr { |node|
		var group, modifiers;
		group = node.value[\group].evalWith(this);
		modifiers = node.value[\modifiers].collect { |m| m.evalWith(this) };
		group = modifiers.inject(group) { |g, m| g.perform(m[0], *m[1]) };
		^group
	}

	eval_group { |node|
		^node.value.evalWith(this)
	}

	/*
	eval_polyGroup { |polyGroup|
		// FIXME
		^polyGroup.value.evalWith(this)
	}
	*/

	eval_seqGroup { |node|
		var termGroups;
		termGroups = node.value.evalWith(this);
		^MareaPattern.stack(termGroups.collect { |g| MareaPattern.fastcat(g) })
	}

	eval_groupBody { |node|
		var termGroups, terms, sibling;
		terms = node.value[\terms].collect(_.evalWith(this));
		sibling = node.value[\sibling];
		termGroups = List[terms];
		if (sibling.isNil.not) {
			termGroups.add(sibling.evalWith(this))
		};
		^termGroups.asArray
	}

	eval_term { |node|
		var value, modifiers;
		value = node.value[\value].evalWith(this);
		modifiers = node.value[\modifiers].collect { |m| m.evalWith(this) };
		value = value.mp;
		value = modifiers.inject(value) { |v, m| v.perform(m[0], *m[1]) };
		^value
	}

	eval_densityMod { |node|
		var value = node.value.evalWith(this);
		^[\density, [value]]
	}

	eval_sparsityMod { |node|
		var value = node.value.evalWith(this);
		^[\sparsity, [value]]
	}

	eval_replicateMod { |node|
		^[\density, [node.value + 1]]
	}

	eval_degradeMod {
		^[\degrade, []]
	}

	eval_sample { |node|
		var name, index;
		name = node.value.name.evalWith(this);
		index = node.value.index;
		index = if (index.isNil) { 0 } { index.evalWith(this) };
		^(name: name, index: index).mp
	}

	eval_rest { |rest| ^MareaPattern.silence }

	eval_string { |node| ^node.value }
	eval_integer { |node| ^node.value }
	eval_float { |node| ^node.value }
}