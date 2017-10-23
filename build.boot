(set-env!
;# :resource-paths #{"resources"}
 :source-paths #{"src" "dev"}
 :dependencies '[[aleph "0.4.3"]
                 [boot-deps "0.1.6"]
                 [cheshire "5.6.2"]
                 [com.cemerick/piggieback "0.2.1"]
                 [com.cemerick/pomegranate "0.3.1"] ; dev only
                 [green-tags "0.3.0-alpha"]
                 [hiccup "1.0.5"]
                 [juxt/dirwatch "0.2.3"]
                 [org.apache.jena/jena-arq "3.4.0" ]
                 [org.apache.jena/jena-rdfconnection "3.4.0" ]
                 [org.apache.jena/jena-tdb "3.4.0" ]
                 [org.clojure/clojure "1.9.0-beta2"]
                 [org.clojure/clojurescript "1.9.946"]
                 [org.clojure/core.async "0.2.395"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/test.check "0.9.0"]
                 [org.clojure/tools.nrepl "0.2.12"]
                 [org.omcljs/om "1.0.0-alpha47"]
                 [ring "1.6.0-beta6"  :exclusions [org.clojure/java.classpath]]
                 [sablono "0.7.6"]
                 [weasel "0.7.0" :exclusions [org.clojure/clojurescript]]
                 ])

(def package-version
  (let [v (clojure.string/trim-newline (slurp "VERSION"))
        p (System/getenv "PATCH_LEVEL")]
    (if p
      (str v "." p)
      (str v ".0-SNAPSHOT" p))))

(require '[phonograph.boot-build :refer :all])

(task-options!
 pom {:project 'phonograph
      :version package-version}
 jar {:main 'phonograph.core}
 repl {:init-ns 'phonograph.user
       :no-color true}
 cljs {:main 'phonograph.core
       :optimizations :whitespace
       :options {}
       :output-file "phonograph/resources/public/frontend.js"}
 target {:dir #{"target/"}})

(comment
(require '[phonograph.boot-build :refer :all])
(require '[weasel.repl.websocket])
(require '[cemerick.piggieback])



(task-options!
 pom {:project 'phonograph
      :version package-version}
 jar {:main 'phonograph.core}
 cljs {:main 'phonograph.core
       :optimizations :whitespace
       :options {}
       :output-file "assets/js/main.js"}
 target {:dir #{"target/"}})

(deftask pig
  "Piggieback nrepl middleware"
  []
  (swap! @(resolve 'boot.repl/*default-middleware*)
         concat '[cemerick.piggieback/wrap-cljs-repl])
  identity)

(deftask reload-browser []
  (with-pre-wrap [fileset]
    (boot.util/info "reloading browser ...")
    (clojure.java.shell/sh "xdotool"
                           "search" "--onlyvisible"  "Phonograph - Nightly"
                           "key" "F5")
    (boot.util/info "\n")
    fileset))

(defn wait-for-browser-repl []
  (cemerick.piggieback/cljs-repl
   (weasel.repl.websocket/repl-env :ip "0.0.0.0" :port 9001)))

(deftask build []
  (comp
   (aot :namespace #{'phonograph.core})
   (pom)
   (cljs :optimizations :advanced)
   (uber)
   (jar)
   (sift :include #{#"project.jar$"})
   (target)))
)
