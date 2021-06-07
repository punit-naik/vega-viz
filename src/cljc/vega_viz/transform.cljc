(ns vega-viz.transform
  (:require [vega-viz.encoding :refer [get-fld-info]]
            [vega-viz.mark :refer [add-mark]]
            [vega-viz.animations :refer [select-on-click]]))

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
   filter-map]
  (let [vl-filter-spec {:filter filter-map}]
    (if transform
      (update vl-spec :transform conj vl-filter-spec)
      (assoc vl-spec :transform [vl-filter-spec]))))

(defn stack
  "Stacks charts based on a particular field on the aggregated field of the chart
   NOTE: Only to be used when there is an aggregated field in the chart in `encoding` and not in `transform`"
  [{:keys [encoding] :as vl-spec}
   {:keys [stack-fld stack-fld-type stack-fld-opts]
    :or {stack-fld-opts {}}}]
  (let [color {:color (merge {:field stack-fld :type stack-fld-type} stack-fld-opts)}]
    (if encoding
      (update vl-spec :encoding conj color)
      (assoc vl-spec :encoding color))))

(defn get-stack-fld-info
  "Gets the info the the field using which the chart is being stacked (coloured)"
  [spec]
  (get-in spec [:encoding :color]))

(defn get-step-size
  "Gets the step size for grouped bar charts based on data"
  [spec & [smaller?]]
  (let [labels (->> spec :data :values
                    (map :label))
        total-groups (->> labels
                          (partition-by identity)
                          first count)
        width (if smaller? 600 660)
        step (double (/ (- width (* (dec total-groups) 2))
                        (count labels)))
        step (if (< step 1) 1 step)
        new-width {:step step
                   ;; Just to keep track of the width of the line chart
                   ;; Does not have to do anything with the actual spec
                   :original-value width}]
    new-width))

(defn- header-for-grouped-bar-chart
  [x-fld-info total-records bucket]
  (merge (:axis x-fld-info)
         {:labelAngle 90
          :labelPadding 0
          :labelAlign "left"}
         (when (> total-records 100)
           {:labelExpr (str "[(timeFormat(datum.value, "
                            (cond
                              (contains? #{"1d" "1w"}
                                         bucket) "'%d'"
                              (contains? #{"1M" "1q"}
                                         bucket) "'%m'"
                              (contains? #{"1Y"}
                                         bucket) "'%Y'")
                            ") % " (if (= bucket "1d")
                                     7 3) ") == 0 ? "
                            "timeFormat (datum.value, '%d') + "
                            "'-' +"
                            "timeFormat (datum.value, '%m') + "
                            "'-' +"
                            "timeFormat(datum.value, '%Y') "
                            ": '']")
            :labelFontSize 9})))

(defn line->grouped-bar
  "Converts a spec with a multi line series (stacked) chart to a grouped bar chart
   Calculates width of each group's bar based on the number of stacks (colours) and current width of the chart"
  [spec compare-fld bucket smaller?]
  (let [spacing 0
        total-records (->> spec :data :values count)
        x-fld-info (get-fld-info spec :x)
        stack-fld-info (get-stack-fld-info spec)
        new-width (get-step-size spec smaller?)
        new-x-fld-info {:x (-> stack-fld-info
                               (dissoc :scale)
                               (assoc :title "")
                               (assoc :axis (merge (:axis x-fld-info)
                                                   {:labels false :ticks false})))}
        column-info {:column (assoc x-fld-info
                                    :spacing spacing
                                    :header (header-for-grouped-bar-chart x-fld-info total-records bucket))}
        new-mark (-> (:mark spec)
                     (assoc :type "bar")
                     ;; No need to do the below thing as the spec won't break
                     ;; For kv pairs which aren't applicable in this case
                     #_(dissoc :interpolate :point))]
    (cond-> (-> spec
                (add-mark new-mark)
                (update-in [:encoding :y] dissoc :impute))
      (not= compare-fld "none") (#(-> (update % :encoding merge new-x-fld-info)
                                      (update :encoding merge column-info)
                                      (assoc :width new-width))))))

(defn grouped-bar->line
  "Converts a spec with a grouped bar chart to a multi line series (stacked) chart"
  [spec]
  (let [width {:width (get-in spec [:width :original-value])}
        new-x-fld-info {:x (-> spec
                               (get-in [:encoding :column])
                               (dissoc :spacing)
                               (update :axis dissoc :labelExpr)
                               (update :header dissoc :labelExpr))}
        new-mark (assoc (:mark spec)
                        :type "line"
                        :interpolate "monotone")]
    (-> spec
        (add-mark new-mark)
        (update :encoding dissoc :column)
        (update :encoding merge new-x-fld-info)
        (merge width))))

(defn add-rule-for-line
  "Adds a rule (vertical line) when hovered over any point, works even when not exactly hovered over a point
   Makes clicking on line charts easier"
  [spec chart-type stack-fld-name chart-interpolate]
  (assoc spec :layer [(-> {:mark {:type "rule"
                                  :strokeDash [4 2] :strokeOpacity 0.5
                                  :point {:filled true :size 60}}
                           :encoding {:color {:condition {:selection "hover"
                                                          :field stack-fld-name}
                                              :value "transparent"}}
                           ;; The prop below `:nearest true` is causing the error
                           ;; vega-util.js:132 ERROR TypeError: Cannot read property '0' of undefined
                           ;; In the console of the browser
                           ;; Removing this prop makes the error go away, but the use will have to then
                           ;; Hover exactly over a point to click it
                           ;; NOTE: Presence of this error does not break the chart functionalities at all
                           ;;       This error shows as a result of a bug in vega-lite as described here:
                           ;;       https://github.com/vega/vega-lite/issues/4298
                           :selection {:hover {:type "single" :on "mouseover" :empty "none"
                                               :nearest true :clear "mouseout"}}}
                          (select-on-click {:select-fld stack-fld-name :select-name :B}))
                      (-> {}
                          (add-mark (cond-> {:type chart-type
                                             :invalid "filter"}
                                      (= chart-type "moving-avg") (assoc :type "line")
                                      (or (= chart-type "line")
                                          (= chart-type "moving-avg")) (assoc :interpolate chart-interpolate)))
                          (select-on-click {:select-fld stack-fld-name :select-name :company})
                          (apply-filter {:field "x" :valid true})
                          (apply-filter {:field "y" :valid true}))]))

(defn change-axis-color [spec color]
  (update-in spec [:config :axis] assoc
             :gridColor color
             :labelColor color
             :tickColor color
             :titleColor color))

(defn change-legend-color [spec color]
  (update-in spec [:config :legend] assoc
             :labelColor color
             :titleColor color))

(defn generate-colour-range
  "Generates `vega-lite` spec for colour ranges for field values"
  [field-values domain-mapping-fn range-mapping-fn]
  {:domain (map domain-mapping-fn field-values)
   :range (map range-mapping-fn field-values)})
