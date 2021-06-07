(defproject vega-viz "1.0.0"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/clojurescript "1.10.866"]
                 ;; Vega
                 [cljsjs/vega-lite "4.17.0-0"]
                 [cljsjs/vega "5.17.0-0"]
                 [cljsjs/vega-tooltip "0.24.2-0"]
                 [cljsjs/vega-embed "6.14.2-0"]
                 [reagent "1.1.0"]]

  :source-paths ["src/cljc"]
  :test-paths ["test"]
  :aliases {"testing" ["do" ["test"] ["install"]]
            "build" ["install"]})
