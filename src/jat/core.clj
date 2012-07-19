(ns jat.core (:require [jat.junit :as junit] [jat.db :as db] [jat.sort :as sort]))

(defn- new-test-report [build-num start-time test]
  "Create a new test report map"
  {:name (. (:method test) getName) :test-history ()})

(defn- append-test-history [test-report build-num start-time test]
  "Add test history to a test"
  (assoc test-report :test-history (conj (:test-history test-report)
                                         {:success (:success test)
                                          :duration (:duration test)
                                          :build build-num
                                          :date start-time
                                          :error-message (:error-message test)})))

(defn create-new-test-report [build-num start-time test]
  "Create a new test report with history"
  (let [test-report (new-test-report build-num start-time test)]
    (append-test-history test-report build-num start-time test)))

(defn- add-detail-to-suite-report [suite-report build-num start-time suite]
  "Add information about last test suite run"
  (assoc suite-report
   :last-build build-num
   :first-run-date start-time
   :last-run-date start-time
   :last-total-test-count (count (:tests suite))
   :last-pass-count (count (filter #(= :passed (:success %)) (:tests suite)))
   :last-total-duration (apply + (filter #(< 0 %) (map :duration (:tests suite))))))

(defn- new-suite-report [build-num start-time suite]
  "Create a new test suite report"
  (add-detail-to-suite-report {:name (. (:class suite) getName)
                               :suite-history ()
                               :test-reports (map (fn [test] (create-new-test-report build-num start-time test)) (:tests suite))}
                              build-num start-time suite))

(defn- append-suite-history [suite-report]
  "Add test suite history"
  (assoc suite-report :suite-history (conj (:suite-history suite-report) 
                              {:build (:last-build suite-report)
                               :date (:last-run-date suite-report)
                               :pass-count (:last-pass-count suite-report)
                               :total-test-count (:last-total-test-count suite-report)
                               :total-duration (:last-total-duration suite-report)
                               })))

(defn create-new-report [build-num start-time suite]
  "Create a new test suite and add history"
  (let [new-suite-report (new-suite-report build-num start-time suite)]
    (append-suite-history new-suite-report)))
  
(defn- get-test [suite-report test-name]
  "Find a test in a test suite"
  (let [test-seq (filter #(= test-name (:name %)) (:test-reports suite-report))]
    (if (empty? test-seq)
      nil
      (first test-seq))))

(defn update-test-report [old-test-report build-num start-time test]
  "Update a test based on the last run"
  (if (= nil old-test-report)
    (create-new-test-report build-num start-time test)
    (append-test-history old-test-report build-num start-time test)))

(defn- update-test-report-on-suite [suite-report build-num start-time test]
  "Update a suite based on the last run"
  (update-test-report (get-test suite-report (. (:method test) getName)) build-num start-time test))

(defn- update-test-reports-on-suite [suite-report build-num start-time suite]
  (assoc suite-report :test-reports (map (fn [test] (update-test-report-on-suite suite-report build-num start-time test)) (:tests suite))))
                                         
(defn update-report [suite-report build-num start-time suite]
  "Update an existing suite report"
  (-> suite-report
    (add-detail-to-suite-report build-num start-time suite)
    (append-suite-history)
    (update-test-reports-on-suite build-num start-time suite)))

(defn- update-existing-report-in-db [suite-name suite build-num start-time]
  (let [oldsuite (db/load-suite suite-name) id { :_id (:_id oldsuite) }]
    (db/update-suite id (update-report oldsuite build-num start-time suite))))

(defn store-results [suite build-num start-time]
  "Persist a suite, either as new or existing"
  (let [suite-name (. (:class suite) getName)]
    (if (db/suite-exists? suite-name)
      (update-existing-report-in-db suite-name suite build-num start-time)
      (db/save-suite (create-new-report build-num start-time suite)))))

(defn create-suites-from [classname-list sortable-values]
  "Create a test suite from the given class"
  (sort/merge-with-map-using #(. (:class %) getName) (map junit/create-suite classname-list) (sort/create-map :name sortable-values)))

(defmulti create-suites :sorting-alg)
(defmethod create-suites :fastest-first [configuration] (sort-by :last-duration (create-suites-from (:classnames configuration) (db/last-durations))))
(defmethod create-suites :most-errored-first [configuration] (sort-by :last-success-rate (create-suites-from (:classnames configuration) (db/last-success-rate))))

(defn run-tests-in [suite build-num time]
  "Run all tests in the given suite and persists the results"
  (-> suite
    (junit/run-suite)
    (store-results build-num time)))

(defn execute [configuration]
  "Execute the given configuration"
  (let [suites (create-suites configuration)]
    (pmap #(run-tests-in % (:build configuration) (:time configuration)) suites)))

(def results (execute {
          :sorting-alg :most-errored-first 
          :build "2"
          :time (java.lang.System/currentTimeMillis)
          :classnames (map #(java.lang.Class/forName %) (list 
                        "jamesr.tests.JUnit3TypeTest" 
                        "jamesr.tests.JUnit4TypeTest" 
                        "jamesr.tests.JUnit3FailingTest" 
                        "jamesr.tests.JUnit4FailingTest"))}))
