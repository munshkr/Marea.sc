Marea {
  *new {
    |dict, gateKey|
	"gateKey: %".format(gateKey).postln;
  }
}

+ IdentityDictionary {
  tp {
	|gateKey|
    ^Marea.new(this, gateKey);
  }
}