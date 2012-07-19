(ns jat.test.sort
  (:use 
    [jat.sort :as sort] clojure.test))

(deftest should-create-map-keyed-on-name []
  (let [suites (list {:name "jim"} {:name "al" :age 33}) suites-map (create-map :name suites)]
    (is (= "jim" (:name (suites-map "jim"))))
    (is (= 33 (:age (suites-map "al"))))))

(deftest should-merge-maps []
  (is (= (list {:x 1 :y 1}) (merge-with-map-using :x (list {:x 1}) {1 {:y 1}}))))

