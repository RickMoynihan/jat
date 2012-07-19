(ns jat.db (:require [somnium.congomongo :as congo]))

(def connection (congo/make-connection "junit"))

(defn load-suite 
  "Loads the suite with the given name from mongo"
  [suite-name]
  (congo/with-mongo connection
    (congo/fetch-one :suites
               :where {:name suite-name})))

(defn suite-exists? 
  "Checks to see if the suite with the given name exists already"
  [suite-name]
  (congo/with-mongo connection
    (< 0 (congo/fetch-count :suites
                 :where {:name suite-name}))))

(defn save-suite
  "Save the given suite, assumes it is new"
  [suite]
  (congo/with-mongo connection
                    (congo/insert! :suites suite)))

(defn update-suite 
  "Updates an existing suite"
  [oldsuite newsuite]
  (congo/with-mongo connection
                    (congo/update! :suites oldsuite newsuite)))

(defn- get-only [keys]
  (congo/with-mongo connection
                    (congo/fetch :suites :only keys)))

(def last-durations #(get-only [:name :last-total-duration]))

(defn last-success-rate []
  (map 
    (fn [r] {:name (:name r) :last-success-rate (/ (:last-pass-count r) (:last-total-test-count r))}) 
    (get-only [:name :last-total-test-count :last-pass-count])))
  
(defn configure 
  "Configures indexes on the suite collection"
  []
  (congo/with-mongo connection
                    (congo/add-index! :suites {:name "name" :unique true})))