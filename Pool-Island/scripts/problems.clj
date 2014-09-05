;;
;; Author José Albert Cruz Almaguer <jalbertcruz@gmail.com>
;; Copyright 2013 by José Albert Cruz Almaguer.
;;
;; This program is licensed to you under the terms of version 3 of the
;; GNU Affero General Public License. This program is distributed WITHOUT
;; ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
;; MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
;; AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
;;

(ns problem)

(declare terminationCondition fitnessTerminationCondition
  changeGen function genInd popSize chromosomeSize )

(def genMerger identity)

(defn genInitPop [PopSize ChromosomeSize]
  (for [_ (range PopSize)] (genInd ChromosomeSize))
  )

(ns maxOnes)

(require '[clojure.string :as str])

(defn genInd [n]
  (str/join "" (for [_ (range n)] (rand-int 2)))
  )

(defn function [ind]
  (count (for [l ind :when (= l \1)] l))
  )

(defn changeGen [g]
  (if (= g \1) \0 \1)
  )

(def genMerger str/join)

(defn fitnessTerminationCondition [ind fit]
  (< (- (count ind) fit) 25)
  )

(def popSize 256)

(def chromosomeSize 128)


(ns maxSAT)

(import 'config.GAConfig)

(def configData (GAConfig/loadFromJSON "maxSATConfig.json"))

(def evaluatorsCount (.getEvaluatorsCount configData))
(def reproducersCount (.getReproducersCount configData)) ;10

(def evaluatorsCapacity (.getEvaluatorsCapacity configData))
(def reproducersCapacity (.getReproducersCapacity configData))

(def evaluations (.getEvaluations configData))
(def terminationCondition (keyword (.getTerminationCondition configData)))

(def popSize (.getPopSize configData))

(def seqOutputFilename (.getSeqOutputFilename configData))
(def parallelOutputFilename (.getParallelOutputFilename configData))
(def repetitions (.getRepetitions configData))

(require '[clojure.string :as str])
(use '[clojure.java.io :only (reader file)])

(defrecord TMaxsatProblem [clauseLength varsCount clausesCount clauses])

(defn MaxSAT-ProblemLoader [instanceFileName]
  (with-open [r (reader (file instanceFileName))]
    (dotimes [_ 5]
      (.readLine r)
      )
    (let [
           spaceRE #"\s+0*"
           l1 (.readLine r)
           f1 (str/split l1 spaceRE)
           _ (.readLine r)
           l2 (.readLine r)
           f2 (str/split l2 spaceRE)
           clauseLength (Integer/parseInt (last f1))
           varsCount (Integer/parseInt (nth f2 2))
           clausesCount (Integer/parseInt (nth f2 3))
           clauses (atom [])
           ]

      (loop [v []]
        (let [l (.readLine r)]
          (if (and
                l
                (not (.contains l "%"))
                )
            (let [
                   values (str/split (str/trim l) spaceRE)
                   intValues (map #(Integer/parseInt %) values)
                   ]
              (recur (conj v (map #(if (< % 0)
                                     [false (dec (Math/abs %))]
                                     [true (dec %)]
                                     )
                               intValues)
                       )
                )
              )
            (swap! clauses #(identity %2) v)
            )
          )
        )
      (TMaxsatProblem. clauseLength varsCount clausesCount @clauses)
      )
    )
  )

(def instance (MaxSAT-ProblemLoader "problems/uf100-01.cnf"))
(def chromosomeSize (.varsCount instance))

(defn fitnessTerminationCondition [ind fit]
  (let [
         res (> fit 395)
         ]

    res
    )
  )

(defn MaxSAT-evaluate [solution ind]
  (count (filter
           (fn [clause]
             "Al menos un componente de la cláusula coincide con el valor del gen"
             (not-every? (fn [[sg val]]
                           (not= (nth ind val) sg)
                           )
               clause
               )
             )
           (.clauses solution)
           )
    )
  )

(defn function [ind]
  (MaxSAT-evaluate instance ind)
  )

(defn genInd [n]
  (for [_ (range n)] (rand-nth [true false]))
  )

(defn changeGen [b]
  (not b)
  )


(ns problem)

(def problemName :maxSAT)
;(def problemName :maxOne )

;(def terminationCondition :cantEvalsTerminationCondition )
;(def terminationCondition :fitnessTerminationCondition)

(case problemName

  :maxSAT (do
            (def fitnessTerminationCondition maxSAT/fitnessTerminationCondition)
            (def changeGen maxSAT/changeGen)
            (def function maxSAT/function)
            (def genInd maxSAT/genInd)

            (def popSize maxSAT/popSize)
            (def chromosomeSize maxSAT/chromosomeSize)

            (def evaluatorsCount maxSAT/evaluatorsCount)
            (def reproducersCount maxSAT/reproducersCount)

            (def evaluatorsCapacity maxSAT/evaluatorsCapacity)
            (def reproducersCapacity maxSAT/reproducersCapacity)

            (def evaluations maxSAT/evaluations)
            (def terminationCondition maxSAT/terminationCondition)


            (def seqOutputFilename maxSAT/seqOutputFilename)
            (def parallelOutputFilename maxSAT/parallelOutputFilename)
            (def repetitions maxSAT/repetitions)

            )
  ;sino
  (do
    (def fitnessTerminationCondition maxOnes/fitnessTerminationCondition)
    (def changeGen maxOnes/changeGen)
    (def function maxOnes/function)
    (def genInd maxOnes/genInd)
    (def genMerger maxOnes/genMerger)

      (def popSize maxOnes/popSize)
    (def chromosomeSize maxOnes/chromosomeSize)
    )
  )

