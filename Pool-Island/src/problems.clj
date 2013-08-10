
(ns problem)

(declare terminationCondition fitnessTerminationCondition changeGen function genInd)

(defn genInitPop [PopSize ChromosomeSize]
  (for [_ (range PopSize)] (genInd ChromosomeSize))
  )

(ns maxOnes)

(defn genInd [n]
  (clojure.string/join "" (for [_ (range n)] (rand-int 2)))
  )

(defn function [L]
  (count (for [l L :when (= l \1)] l))
  )

(defn changeGen [b]
  (if (= b \1) \0 \1)
  )

(defn fitnessTerminationCondition [ind fit]
  (< (- (count ind) fit) 3)
  )
