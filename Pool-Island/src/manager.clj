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

(import 'java.util.Date)

(extend-type TManager
  manager/Manager

  (init [self ppools]
    (swap! (.pools self) #(identity %2) ppools)
    (send (.profiler self) profiler/initEvol (.getTime (Date.)))

    (swap! (.endEvol self) #(identity %2) false)
    (swap! (.numberOfEvals self) #(identity %2) 0)

    (doseq [p ppools]
      ;        (send p poolManager/setPoolsManager *agent*)
      (send p poolManager/sReps)
      (send p poolManager/sEvals)
      )

    self
    )

  (evalDone [self _]
    ;    (println "entrando a evolveDone " @(.numberOfEvals self))
    (swap! (.numberOfEvals self) inc)
    self
    )

  (poolManagerEnd [self pid]
    ;    (println "manager/poolManagerEnd" pid)
    (send pid finalize/finalize)
    ; Cuando llega el último reporte de finalizacion:
    (when (empty? (swap! (.pools self) #(disj % pid)))
      ;        (println "manager/finalize")
      (finalize/finalize self)
      )
    self
    )

  (solutionReached [self _]
    ;    (println "solutionReachedByPoolManager")
    (if-not @(.endEvol self)
      (do
        (send (.profiler self) profiler/endEvol (.getTime (Date.)) @(.numberOfEvals self))
        (swap! (.endEvol self) #(identity %2) true)
        )
      )
    (doseq [p @(.pools self)]
      (send p poolManager/solutionReachedbyPool)
      ;      (println "manager/finalize")
      (finalize/finalize self)
      )
    self
    )

  finalize/Finalize
  (finalize [self]
;    (println "mkExperiment in manager")
    (send (.report self) report/mkExperiment)
    self
    )

  )