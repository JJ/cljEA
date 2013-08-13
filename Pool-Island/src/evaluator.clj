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

(defn evaluate [& {:keys [table n doIfFitnessTerminationCondition]}]
  (let [
         sels (take n
                (for [[ind [_ state]] table
                      :when (= state 1)]
                  ind
                  )
                )

         ]
    (if (empty? sels)
      (do
        [false nil]
        )
      (do
        (let [
               nSels
               (map
                 (fn [ind]
                   (let [
                          fit (problem/function ind)
                          ]

                     (when (= problem/terminationCondition :fitnessTerminationCondition )
                       (when (problem/fitnessTerminationCondition ind fit)
                         (doIfFitnessTerminationCondition ind fit)
                         )
                       )

                     [ind [fit 2]]
                     )
                   )
                 sels
                 )
               ]
          [true nSels]
          )
        )
      )
    )
  )

(extend-type TEvaluator
  evaluator/Evaluator

  (evaluate [self n]
    (let [
           [res nSels] (pea/evaluate
                         :table @(.table @(.manager self))
                         :n n
                         :doIfFitnessTerminationCondition #(send (.manager self) poolManager/solutionReachedbyEvaluator [%1 %2] *agent*)
                         )
           ]

      (if res
        (do
          (send (.manager self) poolManager/add2Pool-Ind-Fit-State nSels)
          (send (.manager self) poolManager/evalDone *agent* (count nSels))
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