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

(load-file "scripts/loaderFile.clj")

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

  (swap! evaluations #(identity %2) problem/evaluations)
  (swap! solutionFound #(identity %2) false)

  (loop [
          pool (ref initPool)
          evalDone 0
          ]

    (let [
           evaluatorsCapacity (case problem/terminationCondition
                                :fitnessTerminationCondition problem/evaluatorsCapacity
                                ; else
                                (do
                                  (swap! evaluations #(- %1 %2) evalDone)
                                  (min @evaluations problem/evaluatorsCapacity)
                                  )
                                )
           newEvalDone (atom 0)
           _ (when (> evaluatorsCapacity 0)
               (let [
                      [resEval nSels] (pea/evaluate
                                        :sels (take evaluatorsCapacity
                                                (for [[ind [_ state]] @pool
                                                      :when (= state 1)]
                                                  ind
                                                  )
                                                )
                                        :doIfFitnessTerminationCondition (fn [ind fit]
                                                                           (swap! solutionFound #(identity %2) true)
                                                                           )
                                        )
                      ]
                 (when resEval
                   (let [
                          pnSels (for [[i f] nSels] [i [f 2]])
                          ]

                     (dosync
                       (alter pool #(into %1 %2) pnSels)
                       )
                     (swap! newEvalDone #(identity %2) (count pnSels))
                     )
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

           [res [noParents nInds bestParents]] (pea/evolve
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
  (println (format "Doing experiment (time -> %2d)" (.getTime (Date.))))
  (let [
         initEvol (.getTime (Date.))
         res (runSeqEA
               :initPool (into {} (for [ind (problem/genInitPop problem/popSize problem/chromosomeSize)] [ind [-1 1]]))
               )
         ]
    [(- (.getTime (Date.)) initEvol) res]
    )
  )

(def nRes (for [_ (range problem/repetitions)]
            (testsRunSeqEA)
            )
  )

;(doseq [n nRes]
;  (println n)
;  )

(with-open [w (writer (file problem/seqOutputFilename))]
  (.write w "EvolutionDelay,BestSol\n")
  (doseq [[evolutionDelay bestSol] nRes]
    (.write w (format "%1d,%1d\n" evolutionDelay bestSol))
    )
  )

(println "Ends!")