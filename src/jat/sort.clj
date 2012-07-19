(ns junit-sort)

(defn create-map 
  "Creates a map of the sequence of suites based keyed by the suite name"
  [f s]
  (zipmap (map f s) s))

(defn merge-with-map-using
  "Given a map sortable-values whose keys are obtained from entries in seq-to-sort using the function f, merge the values in seq-to-merge with corresponding values in sortable-values."
  [f seq-to-merge sortable-values]
  (map #(merge (get sortable-values (f %)) %) seq-to-merge))
  
  