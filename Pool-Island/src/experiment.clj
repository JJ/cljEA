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


(ns experiment)

(defn genInd [n]
  (clojure.string/join "" (for [_ (range n)] (rand-int 2)))
  )

(defn genInitPop [PopSize ChromosomeSize]
  (for [_ (range PopSize)] (genInd ChromosomeSize))
  )

(defn r1 [pprofiler preport]
  (let [
         popSize 256
         chromosomeSize 128
         conf {
                :evaluatorsCount 4
                :evaluatorsCapacity 50 ; 20
                :reproducersCount 5 ;10
                :reproducersCapacity 50 ; 20
                }

         lmanager (agent (manager/create pprofiler preport) ;                              :error-mode :continue
                    :error-handler pea/manager-error)

         p1 (agent (poolManager/create pprofiler lmanager) ;                        :error-mode :continue
              :error-handler pea/poolManager-error)
         ]

    (send pprofiler profiler/configuration conf 1)

    (send p1 poolManager/init (assoc conf
                                :population (genInitPop popSize chromosomeSize)
                                )
      )

    (send p1 poolManager/migrantsDestination [p1])

    (send lmanager manager/init #{p1})

    )
  :ok )


(defn r2 [pprofiler preport]

  (let [
         popSize 256
         chromosomeSize 128

         conf {
                :evaluatorsCount 4
                :evaluatorsCapacity 50
                :reproducersCount 5
                :reproducersCapacity 50
                }

         lmanager (agent (manager/create pprofiler preport) ;                              :error-mode :continue
                    :error-handler pea/manager-error)

         p1 (agent (poolManager/create pprofiler lmanager) ;                        :error-mode :continue
              :error-handler pea/poolManager-error)

         p2 (agent (poolManager/create pprofiler lmanager) ;                        :error-mode :continue
              :error-handler pea/poolManager-error)

         ]

    (send pprofiler profiler/configuration conf 2)

    (send p1 poolManager/init (assoc conf
                                :population (genInitPop popSize chromosomeSize)))
    (send p1 poolManager/migrantsDestination [p2])

    (send p2 poolManager/init (assoc conf
                                :population (genInitPop popSize chromosomeSize)))
    (send p2 poolManager/migrantsDestination [p1])

    (send lmanager manager/init #{p1 p2})

    )

  :ok )