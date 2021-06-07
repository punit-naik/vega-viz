(ns vega-viz.animations)

(defn select-on-click
  "Selects data only belonging to a particular label which was clicked
   and fades out others from the legend bar"
  [spec {:keys [select-fld select-type select-bind select-name]
         :or {select-fld "label"
              select-type "multi"
              select-bind "legend"
              select-name :A}}]
  (-> spec
        ;; Selection
      (update :selection assoc select-name {:type select-type :fields [select-fld] :bind select-bind})
        ;; Selection Action
      (update :encoding assoc :opacity {:condition {:selection (name select-name) :value 1} :value 0.05})))

(defn zoom-on-scroll
  "Zooms in/out the graph"
  [spec]
  (-> spec
      (update :selection assoc :grid {:type "interval" :bind "scales"})))