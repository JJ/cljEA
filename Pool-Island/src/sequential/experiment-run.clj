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

(load-file "./src/loaderFile.clj")

(ns sequential.experiment-run)

(use '[clojure.java.io :only (writer file)])
(import 'java.util.Date)

(def evaluations (atom 5000))
(def solutionFound (atom false))

(defn bestSolution [pool]
  (let [
         all (for [[ind [fitness state]] pool
                   :when (= state 2)]
               [ind fitness]
               )
         ]
    (nth (reduce #(if (< (%1 1) (%2 1)) %2 %1) all) 1)
    )
  )

(defn terminationCondition []
  (case problem/terminationCondition
    :fitnessTerminationCondition @solutionFound
    ; else
    (do
      (= @evaluations 0)
      )
    )
  )

(defn runSeqEA [& {:keys [
                           initPool
                           ]}
                ]

  (loop [
          pool (ref initPool)
          evalDone 0
          ]

    (let [
           evaluatorsCapacity (case problem/terminationCondition
                                :fitnessTerminationCondition problem/evaluatorsCapacity
                                ; else
                                (mod
                                  (swap! evaluations #(- %1 %2) evalDone)
                                  problem/evaluatorsCapacity)
                                )
           newEvalDone (atom 0)
           _ (when (> evaluatorsCapacity 0)
               (let [
                      [resEval nSels] (pea/evaluate
                                        :table @pool
                                        :n evaluatorsCapacity
                                        :doIfFitnessTerminationCondition (fn [ind fit]
                                                                           (swap! solutionFound #(identity %2) true)
                                                                           )
                                        )
                      ]
                 (when resEval
                   (dosync
                     (alter pool #(into %1 %2) nSels)
                     )
                   (swap! newEvalDone #(identity %2) (count nSels))


                   )

                 )
               )



           subpop (pea/extractSubpopulation
                    (for [[ind [fitness state]] @pool
                          :when (= state 2)]
                      [ind fitness]
                      )
                    problem/reproducersCapacity
                    )

           [res [noParents nInds bestParents]]
           (pea/evolve
             :subpop subpop
             :parentsCount (quot (count subpop) 2)
             )



           ]

      (when res
        (dosync
          (alter pool #(identity %2) (pea/mergeFunction
                                       @pool
                                       subpop noParents
                                       nInds bestParents (count @pool)
                                       ))
          )


        )

      (if (terminationCondition)
        (bestSolution @pool)
        (recur pool @newEvalDone)
        )

      )
    )
  )

;(try
;  (runSeqEA
;    :initPool (into {} (for [ind (problem/genInitPop problem/popSize problem/chromosomeSize)] [ind [-1 1]]))
;    )
;
;  (catch Exception a
;    (clojure.repl/pst a)
;    )
;  )

(defn testsRunSeqEA []
  (let[
        initEvol (.getTime (Date.))
        res (runSeqEA
          :initPool (into {} (for [ind (problem/genInitPop problem/popSize problem/chromosomeSize)] [ind [-1 1]]))
          )
        ]
    (- (.getTime (Date.)) initEvol)
    )
  )

(def nRes (for [_ (range 100)]
            (testsRunSeqEA)
            ))

(with-open [w (writer (file "../../results/book2013/cljEA/seqResults.csv"))]
  (.write w "EvolutionDelay\n")
  (doseq [evolutionDelay nRes]
    (.write w (format "%1d\n" evolutionDelay))
    )
  )