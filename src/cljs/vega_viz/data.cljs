(ns vega-viz.data)


(defn add-dataset
  "Adds a dataset to the Vega-lite spec"
  [vl-spec data]
  (assoc-in vl-spec [:data :values] data))

(comment
  ;; These should have worked but not working right now

  (defn add-dataset
    "Adds a named dataset to the Vega-lite spec"
    [{:keys [datasets] :as vl-spec} dataset-name dataset-value]
    (if datasets
      (update vl-spec :datasets conj {(keyword dataset-name) dataset-value})
      (assoc vl-spec :datasets {(keyword dataset-name) dataset-value})))

  (defn use-data
    "Sets the dataset in the Vega-lite spec"
    [vl-spec dataset-name]
    (update vl-spec :data assoc :name (name dataset-name))))
