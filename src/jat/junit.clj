(ns jat.junit (:use clojure.stacktrace))

(defn- junit-version [suite]
  "Detects whether the unit test is junit 3 or junit 4 and returns the resulting keyword."
  (if (. (Class/forName "junit.framework.TestCase") isAssignableFrom (:class suite))
    :junit3
    :junit4))

(defn- has-annotation [annotation method] 
  "Checks for an annotation on a java method."
  (. method getAnnotation annotation))

(def is-junit4-test (partial has-annotation org.junit.Test))

(def is-junit4-setup (partial has-annotation org.junit.Before))

(def is-junit4-teardown (partial has-annotation org.junit.After))

(defn- has-zero-arguments [method]
  "Checks that a method has no arguments."
  (= (java.lang.reflect.Array/getLength (. method getParameterTypes)) 0))

(defn- is-junit3-method [name-filter method]
  (and (has-zero-arguments method) (name-filter (. method getName))))

(def is-junit3-test (partial is-junit3-method (fn [name] (. name startsWith "test"))))

(def is-junit3-setup (partial is-junit3-method (fn [name] (. name equals "setUp"))))

(def is-junit3-teardown (partial is-junit3-method (fn [name] (. name equals "tearDown"))))

(defmulti create-instance :version)
(defmethod create-instance :junit3 [suite] (. (:class suite) newInstance))
(defmethod create-instance :junit4 [suite] (. (:class suite) newInstance))

(defn- methods 
  "Returns all methods on the test suite class based on a filter."
  ([suite]
  (methods identity suite))
  ([f suite]
  (filter f (. (:class suite) getMethods))))

(defmulti test-methods :version)
(defmethod test-methods :junit4 [suite] (methods is-junit4-test suite))
(defmethod test-methods :junit3 [suite] (methods is-junit3-test suite))

(defmulti setup-methods :version)
(defmethod setup-methods :junit4 [suite] (methods is-junit4-setup suite))
(defmethod setup-methods :junit3 [suite] (methods is-junit3-setup suite))

(defmulti teardown-methods :version)
(defmethod teardown-methods :junit4 [suite] (methods is-junit4-teardown suite))
(defmethod teardown-methods :junit3 [suite] (methods is-junit3-teardown suite))

(defn- create-tests [suite]
  "Turns all methods in to a test map"
  (map (fn [method] {:method method :success :not-run :error nil :duration -1}) (test-methods suite)))

(defn- add-property [suite property-key property-generator]
  "Adds a new property to a suite map"
  (assoc suite property-key (property-generator suite)))

(defn create-suite [class]
  "Given a class, create a test suite from it."
  (let [suite {:class class}]
    (-> suite
      (add-property :version junit-version)
      (add-property :instance create-instance)
      (add-property :tests create-tests)
      (add-property :setup-methods setup-methods)
      (add-property :teardown-methods teardown-methods))))

(defn- errored [test error]
  "Mark a test as errored"
  (assoc test :success :error :error error :error-message (. error getMessage)))

(defn- failed [test error]
  "Mark a test as failed"
  (assoc test :success :failed :error error :error-message (. error getMessage)))

(defn- pass [test]
  "Mark a test as passed"
  (assoc test :success :passed :error nil :error-message nil))

(defn- invoke-methods [key testsuite] 
  "Invoke the required methods on the test suite. e.g. key :setup-methods would invoke all set up methods."
  (map #(. % invoke class) (key testsuite)))
  
(def setup (partial invoke-methods :setup-methods))

(def teardown (partial invoke-methods :teardown-methods))

(defn- handle-error [test exception]
  "Handle a test error and decide whether the test failed or errored"
  (let [actualexception (root-cause exception)]
    (println "Got error in test " (:method test))
    (print-stack-trace actualexception)
    (cond
      (= (java.lang.Class/forName "junit.framework.AssertionFailedError") (. actualexception getClass)) (failed test actualexception)
      (= (java.lang.Class/forName "java.lang.AssertionError") (. actualexception getClass)) (failed test actualexception)
      :else (errored test actualexception))))

(defn run-test [suite test]
  "Runs the inidividual test in the given suite"
  (let [starttime (java.lang.System/currentTimeMillis)]
	  (try
	    (do
	      (setup suite)
	      (. (:method test) invoke (:instance suite) (make-array Object 0))
	      (teardown suite)
	      (assoc (pass test) :duration (- (java.lang.System/currentTimeMillis) starttime)))
	    (catch java.lang.Throwable error (handle-error test error)))))
    
(defn run-suite 
  "Execute all tests in the given test suite"
  [suite]
  (loop 
    [test (first (:tests suite)) moretests (rest (:tests suite)) results ()]
    (let [new-results (conj results (run-test suite test))]
      (if (empty? moretests)
        (assoc suite :tests new-results)
        (recur (first moretests) (rest moretests) new-results)))))
