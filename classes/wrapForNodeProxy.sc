+ MareaPattern {
	proxyControlClass { ^MareaStreamControl }
	buildForProxy { |proxy, channelOffset|
		var protoEvent = Event.default.buildForProxy(proxy, channelOffset);
		^this.asPatternPlayer(proxy.clock, protoEvent)
	}
}