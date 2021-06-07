# Vega-Viz

Data visualisation library for ClojureScript using Vega-JS

## Usage

```
(ns user
  (:require [vega-viz.parser :refer [plot]]
            [vega-viz.core :refer [init!]]
            [vega-viz.data :refer [add-dataset]]
            [vega-viz.encoding :refer [add-axes]]
            [vega-viz.mark :refer [add-mark]]
            [vega-viz.transform :refer [stack]]))

(def spec
  (-> (init!)
      (add-dataset [{:name "punit" :money 2 :account "axis"}
                    {:name "punit" :money 1 :account "hdfc"}
                    {:name "naik" :money 3 :account "axis"} {:name "naik" :money 4 :account "hdfc"}])
      (add-axes {:x-fld-name "name" :x-fld-type "nominal"
                 :y-fld-name "money" :y-fld-type "quantitative" :y-agg-op "sum"})
      (add-mark "bar")
      (stack {:stack-fld "account" :stack-fld-type "nominal"})))

;;; Now you can render this spec anywhere in your hiccup style view

(plot spec)
```
