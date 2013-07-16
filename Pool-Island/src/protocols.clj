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

(use '[clojure.java.io :only (writer file)])

(defprotocol HasPid
  (setPid [self pid])
  )

(ns evaluator)

(defn maxOnes [L]
  (count (for [l L :when (= l \1)] l))
  )

(defprotocol Evaluator
  (evaluate [self N])
  (finalize [self])
  )

(ns manager)

(defprotocol Manager
  (init [self ppools])
  (evalDone [self pid])
  (poolManagerEnd [self pid])
  (endEvol [self t])
  (solutionReachedByPoolManager [self pid])
  (finalize [self])
  )

(ns poolManager)

(defprotocol PoolManager
  (init [self conf])
  (updatePool [self newPool])
  (add2Pool-Ind-Fit-State [self individuos])
  (migrantsDestination [self Dests])
  (migration [self ParIndividuoFitness])
  ;  (setPoolsManager [self Manager])
  (evaluatorFinalized [self pid])
  (reproducerFinalized [self pid])
  (evolveDone [self pid])
  (evalDone [self pid])
  (sReps [self])
  (sEvals [self])
  (solutionReachedbyAny [self])
  (solutionReachedbyEvaluator [self pid])
  (evalEmpthyPool [self pid])
  (repEmpthyPool [self pid])
  (finalize [self])
  )

(ns report)

(defprotocol Report

  (experimentEnd [self EvolutionDelay NEmig Conf NIslands NumberOfEvals])
  (mkExperiment [self])
  (session [self Funs])
  )

(ns reproducer)

(defprotocol Reproducer
  (evolve [self n])
  (emigrateBest [self destination])
  (finalize [self])
  )

(ns profiler)

(defprotocol Profiler
  (init [self rprt])
  (configuration [self nConf nNIslands])
  (migration [self [_ _] t])
  (initEvol [self t])
  (iteration [self population])
  (endEvol [self t numberOfEvals])
  (finalize [self])
  )