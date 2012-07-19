(ns jat.junit
  (:use jat.junit
        clojure.test
        clojure.stacktrace))

(def junit3-test (java.lang.Class/forName "jamesr.tests.JUnit3TypeTest"))
(def junit4-test (java.lang.Class/forName "jamesr.tests.JUnit4TypeTest"))
(def junit3-fail-test (java.lang.Class/forName "jamesr.tests.JUnit3FailingTest"))
(def junit4-fail-test (java.lang.Class/forName "jamesr.tests.JUnit4FailingTest"))

(defn view-error [test]
  (print-stack-trace (:error test)))

(deftest should-create-junit3-suite-with-correct-values []
  (let [suite (create-suite junit3-test)]
    (is (= :junit3 (:version suite)))
    (is (= 1 (count (:setup-methods suite))))
    (is (= 1 (count (:teardown-methods suite))))
    (is (= 2 (count (:tests suite))))))

(deftest should-create-junit4-suite-with-correct-values []
  (let [suite (create-suite junit4-test)]
    (is (= :junit4 (:version suite)))
    (is (= 1 (count (:setup-methods suite))))
    (is (= 1 (count (:teardown-methods suite))))
    (is (= 2 (count (:tests suite))))))

(deftest should-run-junit3-suite []
  (let [suite (run-suite (create-suite junit3-test))]
    (is (= 2 (count (filter #(= :passed (:success %)) (:tests suite)))))
    (is (= 2 (count (filter #(<= 0 (:duration %)) (:tests suite)))))
    (is (= 2 (count (filter #(= nil (:error %)) (:tests suite)))))))
    
(deftest should-run-junit4-suite []
  (let [suite (run-suite (create-suite junit4-test))]
    (is (= 2 (count (filter #(= :passed (:success %)) (:tests suite)))))
    (is (= 2 (count (filter #(<= 0 (:duration %)) (:tests suite)))))
    (is (= 2 (count (filter #(= nil (:error %)) (:tests suite)))))))   
           
(deftest should-handle-failed-for-junit3-suite []
  (let [
        suite (run-suite (create-suite junit3-fail-test)) 
        failures (filter #(= :failed (:success %)) (:tests suite))
        errors (filter #(= :error (:success %)) (:tests suite))]
    (is (= 1 (count failures)))
    (is (= 1 (count errors)))
    (is (= "testFailure" (. (:method (first errors)) getName)))
    (is (= "testAssertion" (. (:method (first failures)) getName)))
    (is (= "expected:<false> but was:<true>" (:error-message (first failures))))
    (is (= "A test error message" (:error-message (first errors))))))

(deftest should-handle-failed-for-junit4-suite []
  (let [
        suite (run-suite (create-suite junit4-fail-test)) 
        failures (filter #(= :failed (:success %)) (:tests suite))
        errors (filter #(= :error (:success %)) (:tests suite))]
    (is (= 1 (count failures)))
    (is (= 1 (count errors)))
    (is (= "shouldFailWithError" (. (:method (first errors)) getName)))
    (is (= "shouldFailWithAssertion" (. (:method (first failures)) getName)))
    (is (= "expected:<false> but was:<true>" (:error-message (first failures))))
    (is (= "A test error message" (:error-message (first errors))))))

