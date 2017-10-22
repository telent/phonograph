with import <nixpkgs> {};

let sourceFilesOnly = path: type:
    !((baseNameOf path == "var") ||
      (baseNameOf path == "target"));
in stdenv.mkDerivation rec {
    name = "phonograph";
    srcs = builtins.filterSource sourceFilesOnly ./.;
    buildInputs = [ openjdk makeWrapper ];
}
