(ns
  ea.reproducer
  (:gen-class))

(defn enhance-parents [pop]
  (loop [p2d pop  result '()]
    (let [current (first p2d) remain (rest p2d)]
      (if (empty? remain)
        (conj result current)
        (recur remain (concat result (repeat (inc (count remain)) current)))
        )
      )
    )
  )

(defn parents-selector [pop n]
  (let [nPar (count pop)
        f (fn []
            (let [
                  e (nth pop (rand-int nPar))
                  o (some #(when-not (= e %) %) (repeatedly #(nth pop (rand-int nPar)))) ; The first individual diferent to e
                  ]
              [e o]
              )
            )
        ]
    (repeatedly n f)
    )
  )

(defn crossover [p]
  (let [
        [a b] p
        cPoint (inc (rand-int (dec (count a))))
        [a1 a2] (split-at cPoint a)
        [b1 b2] (split-at cPoint b)
        ]
    [(vec (concat a1 b2)) (vec (concat b1 a2))]
    )
  )

(defn change-gen [g] (if (= g 0) 1 0))

(defn mutate [ind]
  (let [
        pos (rand-int (count ind))
        [a b] (split-at pos ind)
        b1 (first b)
        b2 (rest b)
        ]
    (vec (concat a (conj b2 (change-gen b1))))
    )
  )

(defn reproduce [& {:keys [config iEvals]}]
  (if (> (count iEvals) 0)
    (let [
          lenSubPop (count iEvals)
          p2Rep (enhance-parents (for [[a b] iEvals] a))
          parents (parents-selector p2Rep (quot lenSubPop 2))
          npInds (map crossover parents)
          l1 (for [[a _] npInds] a)
          l2 (for [[_ a] npInds] a)
          l3 (if (= (mod lenSubPop 2) 1) (list ((nth iEvals 0) 0)) '())
          nInds (concat l1 l2 l3)
          ]

      (map #(if (< (rand) (:PMutation config)) (mutate %) %) nInds)
      )
    '()
    )
  )