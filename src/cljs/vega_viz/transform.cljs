(ns vega-viz.transform)

(defn apply-aggregate
  "Adds a vega aggregate element in the Vega-lite spec"
  [{:keys [transform] :as vl-spec}
   {:keys [fld-name fld-alias grp-by-flds op] :or {fld-alias (str fld-name "_agg")}}]
  (let [vl-transform-spec {:aggregate
                            [{:op op :field fld-name :as fld-alias}]
                           :groupby (if (coll? grp-by-flds)
                                      grp-by-flds
                                      [grp-by-flds])}]
    (if transform
      (update vl-spec :transform conj vl-transform-spec)
      (assoc vl-spec :transform [vl-transform-spec]))))

(defn do-calculation
  "Does a calculation on some fields in the data in the Vega-lite spec"
  [{:keys [transform] :as vl-spec}
   {:keys [calc-str calc-fld-alias]}]
  (let [vl-calc-spec {:calculate calc-str :as calc-fld-alias}]
    (if transform
      (update vl-spec :transform conj vl-calc-spec)
      (assoc vl-spec :transform [vl-calc-spec]))))

(defn apply-filter
  "Applies filter on the Dataset in the Vega-lite spec"
  [{:keys [transform] :as vl-spec}
   {:keys [filter-str]}]
  (let [vl-filter-spec {:filter filter-str}]
    (if transform
      (update vl-spec :transform conj vl-filter-spec)
      (assoc vl-spec :transform [vl-filter-spec]))))

(defn stack
  "Stacks charts based on a particular field on the aggregated field of the chart
   NOTE: Only to be used when there is an aggregated field in the chart in `encoding` and not in `transform`"
  [{:keys [encoding] :as vl-spec}
   {:keys [stack-fld stack-fld-type]}]
  (let [color {:color {:field stack-fld :type stack-fld-type}}]
    (if encoding
      (update vl-spec :encoding conj color)
      (assoc vl-spec :encoding color))))
