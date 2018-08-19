Marea {
  *new {
    |dict, gateKey|
    ^dict;
  }
}

+ IdentityDictionary {
  tp { |gateKey|
    ^Marea.new(this, gateKey);
  }
}
