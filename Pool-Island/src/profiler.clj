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

(extend-type TProfiler
  profiler/Profiler

  (init [self rprt]
    (swap! (.report self) #(identity %2) rprt)
    self
    )

  (configuration [self nConf nNIslands]
    (swap! (.conf self) #(identity %2) nConf)
    (swap! (.nIslands self) #(identity %2) nNIslands)
    (swap! (.iterations self) #(identity %2) [])
    (swap! (.emigrations self) #(identity %2) [])
    self
    )

  (migration [self [_ _] t]
    (swap! (.emigrations self) #(conj %1 %2) t)
    self
    )

  (initEvol [self t]
    (swap! (.initEvol self) #(identity %2) t)
    self
    )

  (iteration [self population]
    (let [
           popEval (map #(evaluator/maxOnes %) population)
           ]

      (swap! (.iterations self)
        #(conj %1 %2)
        [(reduce #(if (> %1 %2) %2 %1) popEval)
         (reduce #(if (< %1 %2) %2 %1) popEval)
         (/ (reduce + popEval) (* 1.0 (count population))) ; promedio
         ]
        )
      )

    self
    )

  (endEvol [self t numberOfEvals]
    (let [
           evolutionDelay (/ (- t @(.initEvol self)) 1000.0)
           ]

      (send @(.report self) report/experimentEnd
        evolutionDelay (count @(.emigrations self))
        @(.conf self) @(.nIslands self) numberOfEvals)
      )

    self
    )

  finalize/Finalize
  (finalize [self]
    self
    )
  )