(ns
  ea.problem
  (:gen-class))


(defprotocol Problem

  (fitnessFunction [self])
  (qualityFitnessFunction [self])

  (genIndividual [self])
  (getPop [self])

  (runSeqCEvals [self])

  (runParCEvals [self r-obtained-notification])


  (runSeqFitnessQuality [self])

  (runParFitnessQuality [self r-obtained-notification])

  )

(require '(ea [evaluator :as evaluator]
              [reproducer :as reproducer]
              [seq-ea :as seq-ea]
              )
         )

(require '(pea [par-ea :as par-ea]
               )
         )

(def any-problem
  { :genIndividual (fn [self]
                     (for [_ (range (:ChromosomeSize (.config self)))] (rand-int 2))
                     )

    :getPop (fn [self]
              (for [_ (range (:PopSize (.config self)))] (genIndividual self))
              )

    })


(defrecord MaxOne[config Evaluations])

(defn create-maxOneProblem [conf]
  (MaxOne. conf (atom 0))
  )

(extend MaxOne
  Problem  (merge any-problem seq-ea/seq-problem par-ea/par-problem
                  {
                   :fitnessFunction (fn[self]
                                      #(count (for [a % :when (= a 1)] a))
                                      )

                   :qualityFitnessFunction (fn[self]
                                             #(> % (- (:ChromosomeSize (.config self)) 2))
                                             )
                   }

                  )
  )

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
                                     [0 (dec (Math/abs %))]
                                     [1 (dec %)]
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
;(def chromosomeSize (.varsCount instance))

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


(defrecord MaxSAT [config Evaluations])

(defn create-maxSATProblem [conf]
  (MaxSAT. (assoc conf :ChromosomeSize (.varsCount instance)) (atom 0))
  )

(extend MaxSAT
  Problem  (merge any-problem seq-ea/seq-problem par-ea/par-problem
                  {
                   :fitnessFunction (fn[self]
                                      #(MaxSAT-evaluate instance %)
                                      )

                   :qualityFitnessFunction (fn[self]
                                             #(> % 420)
                                             )
                   }

                  )
  )
