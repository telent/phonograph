with import <nixpkgs> {};
let thePackage = import ./default.nix ;
in lib.overrideDerivation thePackage (attr: {
    buildInputs = [ rlwrap # make "lein figwheel" repl usable
                    boot
                    bashInteractive
                    ] ++ attr.buildInputs;
    shellHook = ''
      source nix-shell-hook.sh
    '';
})

