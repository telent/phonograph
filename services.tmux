new-window nix-shell --run "boot cider repl"
new-window nix-shell --run "sleep 5 && boot watch cljs target"
new-window nix-shell --run "sleep 15 && boot pig brepl"
new-window bash
