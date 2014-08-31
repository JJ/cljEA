(ns
  ea.evaluator
  (:gen-class))

(defn evaluate [& {:keys [config p2Eval]}]
  (if (> (count p2Eval) 0)
    (loop [aEvaluar p2Eval evaluados '()]
      (let [
            next2Eval (first aEvaluar)
            fitnessValue ((:ff config) next2Eval)
            qfval ((:qf config) fitnessValue)
            ]
        (when qfval
          ((:df config) [next2Eval fitnessValue])
          )
        (if (or (empty? (rest aEvaluar)) qfval)
          (conj evaluados [next2Eval fitnessValue])
          (recur (rest aEvaluar) (conj evaluados [next2Eval fitnessValue]))
          )
        )
      )
    '()
    )


  )
