(ns test.junit-server 
  (:use 
    junit-server 
    junit 
    clojure.test))

(def junit4-test (java.lang.Class/forName "jamesr.tests.JUnit4TypeTest"))
(def junit3-fail-test (java.lang.Class/forName "jamesr.tests.JUnit3FailingTest"))

(deftest should-create-new-test-report []
  (let [
        now (java.lang.System/currentTimeMillis) 
        build-num "123"
        test {:success :ok :duration 12 :error-message "all is ok" :method (java.lang.Class/forName "java.lang.System")}
        test-report (create-new-test-report build-num now test)]
    (is (= "java.lang.System" (:name test-report)))
    (is (= 1 (count (:test-history test-report))))))

(deftest should-create-new-test-history []
  (let [
        now (java.lang.System/currentTimeMillis) 
        build-num "123"
        test {:success :ok :duration 12 :error-message "all is ok" :method (java.lang.Class/forName "java.lang.System")}
        test-report (create-new-test-report build-num now test)
        test-history (first (:test-history test-report))]
    (is (= :ok (:success test-history)))
    (is (= 12 (:duration test-history)))
    (is (= "123" (:build test-history)))
    (is (= now (:date test-history)))
    (is (= "all is ok" (:error-message test-history)))))
    
(deftest should-update-test-report-with-no-old-history []
  (let [
        now (java.lang.System/currentTimeMillis) 
        build-num "123"
        test {:success :ok :duration 12 :error-message "all is ok" :method (java.lang.Class/forName "java.lang.System")}
        test-report (update-test-report nil build-num now test)]
    (is (= "java.lang.System" (:name test-report)))
    (is (= 1 (count (:test-history test-report))))))

(deftest should-update-test-report-with-history []
  (let [
        now1 (java.lang.System/currentTimeMillis) 
        now2 (java.lang.System/currentTimeMillis) 
        test1 {:success :ok :duration 12 :error-message "all is ok" :method (java.lang.Class/forName "java.lang.System")}
        test2 {:success :ok2 :duration 15 :error-message "all is still ok" :method (java.lang.Class/forName "java.lang.System")}
        old-test-report (create-new-test-report "111" now1 test1)
        test-report (update-test-report old-test-report "222" now2 test2)]
    (is (= "java.lang.System" (:name test-report)))
    (is (= 2 (count (:test-history test-report))))))

(deftest should-update-test-history []
  (let [
        now1 (java.lang.System/currentTimeMillis) 
        now2 (java.lang.System/currentTimeMillis) 
        test1 {:success :ok :duration 12 :error-message "all is ok" :method (java.lang.Class/forName "java.lang.System")}
        test2 {:success :ok2 :duration 15 :error-message "all is still ok" :method (java.lang.Class/forName "java.lang.System")}
        old-test-report (create-new-test-report "111" now1 test1)
        test-report (update-test-report old-test-report "222" now2 test2)
        test-history (first (:test-history test-report))]
    (is (= :ok2 (:success test-history)))
    (is (= 15 (:duration test-history)))
    (is (= "222" (:build test-history)))
    (is (= now2 (:date test-history)))
    (is (= "all is still ok" (:error-message test-history)))))

(deftest should-keep-test-history []
  (let [
        now1 (java.lang.System/currentTimeMillis) 
        now2 (java.lang.System/currentTimeMillis) 
        test1 {:success :ok :duration 12 :error-message "all is ok" :method (java.lang.Class/forName "java.lang.System")}
        test2 {:success :ok2 :duration 15 :error-message "all is still ok" :method (java.lang.Class/forName "java.lang.System")}
        old-test-report (create-new-test-report "111" now1 test1)
        test-report (update-test-report old-test-report "222" now2 test2)
        test-history (second (:test-history test-report))]
    (is (= :ok (:success test-history)))
    (is (= 12 (:duration test-history)))
    (is (= "111" (:build test-history)))
    (is (= now1 (:date test-history)))
    (is (= "all is ok" (:error-message test-history)))))

(deftest should-create-new-suite-report []
  (let [
        now (java.lang.System/currentTimeMillis) 
        suite (run-suite (create-suite junit4-test)) 
        suite-report (create-new-report "123" now suite)]
    (is (= "jamesr.tests.JUnit4TypeTest" (:name suite-report)))
    (is (= "123" (:last-build suite-report)))
    (is (= now (:last-run-date suite-report)))
    (is (= now (:first-run-date suite-report)))
    (is (= 2 (:last-pass-count suite-report)))
    (is (= 2 (:last-total-test-count suite-report)))
    (is (<= 0 (:last-total-duration suite-report)))
    (is (= 1 (count (:suite-history suite-report))))
    (is (= 2 (count (:test-reports suite-report))))))

(deftest should-report-test-failures-in-suite-report []
  (let [
        now (java.lang.System/currentTimeMillis) 
        suite (run-suite (create-suite junit3-fail-test)) 
        suite-report (create-new-report "123" now suite)]
    (is (= 0 (:last-pass-count suite-report)))
    (is (= 2 (:last-total-test-count suite-report)))))

(deftest should-create-new-suite-history []
  (let [
        now (java.lang.System/currentTimeMillis) 
        suite (run-suite (create-suite junit4-test)) 
        suite-report (create-new-report "123" now suite)
        suite-history (first (:suite-history suite-report))]
    (is (= "123" (:build suite-history)))
    (is (= now (:date suite-history)))
    (is (<= 0 (:total-duration suite-history)))
    (is (= 2 (:pass-count suite-history)))
    (is (= 2 (:total-test-count suite-history)))))

(deftest should-report-test-failures-in-suite-history []
  (let [
        now (java.lang.System/currentTimeMillis) 
        suite (run-suite (create-suite junit3-fail-test)) 
        suite-report (create-new-report "123" now suite)
        suite-history (first (:suite-history suite-report))]
    (is (= 0 (:pass-count suite-history)))
    (is (= 2 (:total-test-count suite-history)))))

(deftest should-create-new-test-reports-in-new-suite-report []
  (let [
        now (java.lang.System/currentTimeMillis) 
        suite (run-suite (create-suite junit4-test)) 
        suite-report (create-new-report "123" now suite) 
        test-report (first (:test-reports suite-report))]
    (is (= "shouldDoSomethingAgain" (:name test-report)))
    (is (= 1 (count (:test-history test-report))))))

(deftest should-create-new-test-history-in-new-suite-report []
   (let [
        now (java.lang.System/currentTimeMillis) 
        suite (run-suite (create-suite junit4-test)) 
        suite-report (create-new-report "123" now suite) 
        test-report (first (:test-reports suite-report))
        test-history (first (:test-history test-report))]
    (is (= :passed (:success test-history)))
    (is (<= 0 (:duration test-history)))
    (is (= "123" (:build test-history)))
    (is (= now (:date test-history)))
    (is (= nil (:error-message test-history)))))

(deftest should-update-suite-report []
  (let [
        test1-time 12345
        test2-time 55666
        test1-suite (run-suite (create-suite junit4-test))
        test2-suite (run-suite (create-suite junit4-test))
        old-suite-report (create-new-report "111" test1-time test1-suite)
        suite-report (update-report old-suite-report "222" test2-time test2-suite)]
    (is (= "jamesr.tests.JUnit4TypeTest" (:name suite-report)))
    (is (= "222" (:last-build suite-report)))
    (is (= test2-time (:last-run-date suite-report)))
    (is (= test2-time (:first-run-date suite-report)))
    (is (= 2 (:last-pass-count suite-report)))
    (is (= 2 (:last-total-test-count suite-report)))
    (is (<= 0 (:last-total-duration suite-report)))
    (is (= 2 (count (:suite-history suite-report))))
    (is (= 2 (count (:test-reports suite-report))))))

(deftest should-update-suite-report-history []
  (let [
        test1-time 12345
        test2-time 55666
        test1-suite (run-suite (create-suite junit4-test))
        test2-suite (run-suite (create-suite junit4-test))
        old-suite-report (create-new-report "111" test1-time test1-suite)
        suite-report (update-report old-suite-report "222" test2-time test2-suite)
        suite-history (first (:suite-history suite-report))]
    (is (= "222" (:build suite-history)))
    (is (= test2-time (:date suite-history)))
    (is (<= 0 (:total-duration suite-history)))
    (is (= 2 (:pass-count suite-history)))
    (is (= 2 (:total-test-count suite-history)))))

(deftest should-keep-suite-report-history []
  (let [
        test1-time 12345
        test2-time 55666
        test1-suite (run-suite (create-suite junit4-test))
        test2-suite (run-suite (create-suite junit4-test))
        old-suite-report (create-new-report "111" test1-time test1-suite)
        suite-report (update-report old-suite-report "222" test2-time test2-suite)
        suite-history (second (:suite-history suite-report))]
    (is (= "111" (:build suite-history)))
    (is (= test1-time (:date suite-history)))
    (is (<= 0 (:total-duration suite-history)))
    (is (= 2 (:pass-count suite-history)))
    (is (= 2 (:total-test-count suite-history)))))
    
(deftest should-create-new-test-reports-in-updated-suite-report []
  (let [
        now1 (java.lang.System/currentTimeMillis) 
        now2 (java.lang.System/currentTimeMillis) 
        suite1 (run-suite (create-suite junit4-test)) 
        suite2 (run-suite (create-suite junit4-test)) 
        old-suite-report (create-new-report "111" now1 suite1) 
        suite-report (update-report old-suite-report "222" now2 suite2)
        test-report (first (:test-reports suite-report))]
    (is (= "shouldDoSomethingAgain" (:name test-report)))
    (is (= 2 (count (:test-history test-report))))))

(deftest should-create-new-test-history-in-updated-suite-report []
  (let [
        now1 (java.lang.System/currentTimeMillis) 
        now2 (java.lang.System/currentTimeMillis) 
        suite1 (run-suite (create-suite junit4-test)) 
        suite2 (run-suite (create-suite junit4-test)) 
        old-suite-report (create-new-report "111" now1 suite1) 
        suite-report (update-report old-suite-report "222" now2 suite2)
        test-report (first (:test-reports suite-report))
        test-history (first (:test-history test-report))]
    (is (= :passed (:success test-history)))
    (is (<= 0 (:duration test-history)))
    (is (= "222" (:build test-history)))
    (is (= now2 (:date test-history)))
    (is (= nil (:error-message test-history)))))

(deftest should-keep-test-history-in-updated-suite-report []
  (let [
        now1 (java.lang.System/currentTimeMillis) 
        now2 (java.lang.System/currentTimeMillis) 
        suite1 (run-suite (create-suite junit4-test)) 
        suite2 (run-suite (create-suite junit4-test)) 
        old-suite-report (create-new-report "111" now1 suite1) 
        suite-report (update-report old-suite-report "222" now2 suite2)
        test-report (first (:test-reports suite-report))
        test-history (second (:test-history test-report))]
    (is (= :passed (:success test-history)))
    (is (<= 0 (:duration test-history)))
    (is (= "111" (:build test-history)))
    (is (= now1 (:date test-history)))
    (is (= nil (:error-message test-history)))))