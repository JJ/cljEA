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

(ns evaluator)

(defn maxOnes [L]
  (count (for [l L :when (= l \1)] l))
  )

(ns pea)

(extend-type TEvaluator
  evaluator/Evaluator

  (evaluate [self n]
    ;    (println "evaluate")
    (let [
           sels (take n
                  (for [[ind [_ state]] @(.table @(.manager self))
                        :when (= state 1)]
                    ind
                    )
                  )

           _ (if (empty? sels)
               (do
                 ;        (println "No hay casos en estado 1")
                 ;        (println "---------------------------------------------")
                 (send (.manager self) poolManager/evalEmpthyPool *agent*)
                 )

               (do
                 (let [
                        nSels (map (fn [ind]
                                     (let [
                                            fit (evaluator/maxOnes ind)
                                            l (count ind)
                                            ]

                                       (when (< (- l fit) 3)
                                         (send (.manager self) poolManager/solutionReachedbyEvaluator ind *agent*)
                                         )

                                       [ind [fit 2]]
                                       )

                                     ) sels)
                        ]

                   ;        (println NSels)
                   ;        (assoc self :table (into (:table self) NSels))
                   (send (.manager self) poolManager/add2Pool-Ind-Fit-State nSels)
                   (send (.manager self) poolManager/evalDone *agent*)
                   )
                 )
               )
           ]
      )

    ;    (println "Saliendo de eval!!!!")
    self
    )

  finalize/Finalize
  (finalize [self]
    (send (.manager self) poolManager/evaluatorFinalized *agent*)
    self
    )
  )