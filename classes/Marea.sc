Marea {
	*boot {
		1.to(9).do { |i|
			var name = "s%".format(i).asSymbol;
			currentEnvironment[name] = MareaStream.new
		}
	}

	*quit {
		var env = currentEnvironment;
		1.to(9).do { |i|
			var name = "s%".format(i).asSymbol;
			if (env[name].respondsTo(\stop)) {
				env[name].stop;
			};
			env[name] = nil;
		}
	}
}