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
(require '[clojure.set])

(defn extractSubpopulation
  "returns: (seq [ind fitness])"
  [sels n]
  (let [
         res (sort pea/cmp2 sels)
         ]
    (take n res)
    )
  ;  (send (.hs self) #(conj %1 %2) {:table table :extractSubpopulationSels extractSubpopulationSels})
  ;  (send (.hs self)  #(identity %2) {:table table :extractSubpopulationSels extractSubpopulationSels})


  ;  (def ^:private sels (pea/selectPairs table 2))
  ;  (pea/checkListIntPairs extractSubpopulationSels)
  ;  (swap! pea/jaGlobal #(identity %2) #(println (clojure.string/join ":" extractSubpopulationSels)))
  )
(defn bestParent [pop2r]
  ;  (def ^:private population (sort #(> (%1 1) (%2 1)) pop2r)) ; (OJO: Refactorizar)
  ;  (first population)
  (reduce #(if (> (%1 1) (%2 1)) %1 %2) pop2r)
  )

(defn updatePoolFunc [table subpop noParents
                      nInds bestParents]

  (let [
         p (concat noParents bestParents)
         l1 (map #(conj % 2) p)
         l2 (map #(identity [% -1 1]) nInds)
         l3 (concat l1 l2)
         res (zipmap (for [[I _ _] l3] I) (for [[_ F S] l3] [F S]))
         table1 (apply dissoc table (for [[I _] subpop] I)) ; Remove the subpopulation already selected
         ;         result (into table1 res)
         result (merge-with pea/merge-tables-function table1 res)
         ]

    result
    )

  )

(defn selectPop2Reproduce [population n]
  ;  (println "selectPop2Reproduce" population ":::" n)
  (let [
         tuple-3 (map #(nth population %)
                   (for [ind (range 3)] (rand-int (count population)))
                   )
         select1from3 (fn []
                        (reduce #(if (< (%1 1) (%2 1)) %2 %1) tuple-3) ; (OJO: Refactorizar)
                        )
         ]

    (for [_ (range (* 2 n))] (select1from3))
    )

  )

(defn parentsSelector
  "
  input:
   population: (seq a) 
   n: Int
  returns: array of (a, a)
  "
  [population n]

  (let [
         vp (vec population)
         positions (for [_ (range n)] [(rand-int (count vp)) (rand-int (count vp))])
         ]
    (for [[i j] positions] [(vp i) (vp j)])
    )

  )


(defn crossover
  "
  input: ((String, Int), (String, Int))
  returns: (String, String)
  "
  [[[ind1 _] [ind2 _]]]

  (let [
         changeB (fn [b]
                   (if (= b \1) \0 \1)
                   )
         ind-length (count ind1)
         cross-point (rand-int ind-length)
         cross1 (split-at cross-point ind1)
         cross2 (split-at cross-point ind2)
         child1 (concat (nth cross1 0) (nth cross2 1))
         muttation-point (rand-int ind-length)
         m-child (split-at (dec muttation-point) child1)
         m1 (nth m-child 0)
         m2 (nth m-child 1)
         m3 (rest m2)
         bit1 (changeB (first m2))
         result1 (concat m1 (conj m3 bit1))
         result2 (concat (nth cross2 0) (nth cross1 1))
         ]
    [(clojure.string/join result1) (clojure.string/join result2)]
    )
  ;  (println "saliendo de crossover" ind1)
  )

(defn flatt [parents2flatt]
  (concat (for [[a _] parents2flatt] a) (for [[_ b] parents2flatt] b))
  )

(defn evolve [& {:keys [subpop parentsCount doWhenLittle] :or {doWhenLittle #()}}]

  (if (< (count subpop) 3)
    (do
      ;        (println "*********************************************************")
      (doWhenLittle)
      [nil nil]
      )
    (do
      ;          (println "Reproducing" (count subpop))
      (let [

             pop2r (selectPop2Reproduce subpop parentsCount)
             parents2use (parentsSelector pop2r parentsCount)
             nIndsByPair (map crossover parents2use)
             nInds (concat (for [[I _] nIndsByPair] I) (for [[_ I] nIndsByPair] I))
             noParents (clojure.set/difference (set subpop) (set (flatt parents2use)))
             bestParents [(bestParent pop2r)]
             ]
        [:ok [
                noParents
                nInds
                bestParents
                ]
         ]
        )
      )
    )
  )

(extend-type TReproducer
  reproducer/Reproducer

  (evolve [self n]
    ;            (println "evolve")
    (let [
           subpop (extractSubpopulation
                    (for [[ind [fitness state]] @(.table @(.manager self))
                          :when (= state 2)]
                      [ind fitness]
                      )
                    n
                    )
           [res [noParents nInds bestParents]]
           (evolve
             :subpop subpop
             :parentsCount (quot n 2)
             :doWhenLittle (fn []
                             (send (.manager self) poolManager/repEmpthyPool *agent*)
                             )
             )
           ]

      (when res
        (send (.manager self)
          poolManager/updatePool
          (updatePoolFunc
            @(.table @(.manager self))
            subpop noParents
            nInds bestParents
            )
          )
        (send (.manager self) poolManager/evolveDone *agent*)
        (send (.profiler self) profiler/iteration nInds)
        )

      self
      )
    )

  (emigrateBest [self destination]

    ;    (def ^:private Sels (pea/selectPairs @(.table @(.manager self)) 2))

    (let [
           sels (for [[i [f s]] @(.table @(.manager self)) :when (= s 2)] [i f])
           ]

      ;    (send (.hs self) #(conj %1 %2) {:table @(.table @(.manager self)) :sels sels})
      ;    (send (.hs self) #(identity %2) {:table @(.table @(.manager self)) :sels sels})

      (when (> (count sels) 0)
        (let [
               p (reduce pea/cmp1 sels)
               ]

          (send (.profiler self) profiler/migration p (.getTime (Date.)))
          (send destination poolManager/migration p)

          )
        ;        (swap! pea/jaGlobal #(identity %2) #(println (clojure.string/join ":" emigrateBestSels)))
        )
      )

    self
    )

  (finalize [self]
    self
    )
  )
