Marea {
  *new {
    |dict, gateKey|
	"gateKey: %".format(gateKey).postln;
    ^dict;
  }
}

+ IdentityDictionary {
  tp {
	|gateKey|
    ^Marea.new(this, gateKey);
  }
}