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

(ns pea)

(defn evaluate [& {:keys [sels]}]
  (if (empty? sels)
    [false false nil nil]
    (loop [nSels sels ; The collection
           accu [] ; The results
           b-solution [nil -1] ; The best individual so far
           ]
      (let [
             ind (first nSels)
             fit (problem/function ind)
             no-follow (problem/fitnessTerminationCondition ind fit)
             ]

        (if no-follow
          [true true (conj accu [ind fit]) [ind fit]]
          (if (empty? (rest nSels))
            [true false (conj accu [ind fit]) b-solution]
            (recur (rest nSels) (conj accu [ind fit]) (if (> fit (b-solution 1)) [ind fit] b-solution))
            )
          )
        )
      )
    ;      ;************************************************************
    ;      (let [
    ;             nSels
    ;             (map
    ;               (fn [ind]
    ;                 (let [
    ;                        fit (problem/function ind)
    ;                        ]
    ;
    ;                   (when (= problem/terminationCondition :fitnessTerminationCondition)
    ;                     (when (problem/fitnessTerminationCondition ind fit)
    ;                       (doIfFitnessTerminationCondition ind fit)
    ;                       )
    ;                     )
    ;
    ;                   [ind fit]
    ;                   )
    ;                 )
    ;               sels
    ;               )
    ;             ]
    ;
    ;        [true nSels]
    ;        )
    ;      ;************************************************************

    )
  )

(extend-type TEvaluator
  evaluator/Evaluator

  (evaluate [self n]
    (let [
           [resEval solFound nSels bs] (pea/evaluate
                         :sels (take n
                                 (for [[ind [_ state]] @(.table @(.manager self))
                                       :when (= state 1)]
                                   ind
                                   )
                                 )
                         )
           ]

      (if resEval
        (let [
               pnSels (for [[i f] nSels] [i [f 2]])
               ]
          (send (.manager self) poolManager/evalDone *agent* (count pnSels) bs)
          (if solFound
            (send (.manager self) poolManager/solutionReachedbyEvaluator bs *agent*)
            (send (.manager self) poolManager/add2Pool pnSels)
            )
          )
        (send (.manager self) poolManager/evalEmpthyPool *agent*)
        )
      )

    self
    )

  finalize/Finalize
  (finalize [self]
    (send (.manager self) poolManager/evaluatorFinalized *agent*)
    self
    )
  )