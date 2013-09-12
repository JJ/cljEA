(ns pea)

(defrecord TEvaluator [manager profiler])

(defrecord TReproducer [manager profiler])

(defrecord TIslandManager
  [pools ; set
   profiler manager
   endEvol numberOfEvals
   solutions cierre])

(defrecord TPoolManager
  [table active migrantsDestination
   profiler manager pmConf
   evals reps poolSize evaluations])

(defrecord TProfiler
  [conf manager initEvol nIslands iterations emigrations evolData])

(defrecord TManager
  [results profiler instances])



(defn checkListPairs [pairs]
  (if (not (every? #(= (count %) 2) pairs))
    (throw (RuntimeException.
             (clojure.string/join ["not all in list are pairs: " (clojure.string/join ":" pairs)])))
    )
  )
(defn checkListIntPairs [pairs]
  (if (not (every? #(and
                      (= (count %) 2)
                      ;                      (instance? java.lang.Number (% 0))
                      ;                      (instance? java.lang.Number (% 1))
                      ) pairs))
    (throw (RuntimeException.
             (clojure.string/join ["not all are int pairs: " (clojure.string/join ":" pairs)])))
    )
  )

(def error (atom true))

(def jaGlobal (atom #(println "testing")))

(defn print-error [the-agent exception]
  (when @error
    ;      (println (class @the-agent) "::" @(.hs @the-agent))
    (.println System/out (.getMessage exception))
    (swap! error #(identity %2) false)
    (@jaGlobal)
    (clojure.repl/pst exception)

    (with-open [w (writer (file "log.txt"))]
      ;        (.write w "EvolutionDelay,NumberOfEvals,Emigrations,EvaluatorsCount,ReproducersCount,IslandsCount\n")
      ;        (def allH @(.hs @the-agent))
      ;        (doseq [ hEntry (subvec allH (- (count allH) 1))]
      ;          (.write w (str hEntry))
      ;          )

      (.write w (str @(.hs @the-agent)))

      )

    )

  )

(defn islandManager-error [the-agent exception]
  (print-error the-agent exception)
  )

(defn poolManager-error [the-agent exception]
  (print-error the-agent exception)
  )

(defn evaluator-error [the-agent exception]
  (print-error the-agent exception)
  )

(defn reproducer-error [the-agent exception]
  (print-error the-agent exception)
  )

(defn manager-error [the-agent exception]
  (print-error the-agent exception)
  )

(defn profiler-error [the-agent exception]
  (print-error the-agent exception)
  )

(defn contador-error [the-agent exception]
  (print-error the-agent exception)
  )

(def contador (atom 0
                ;                :error-handler pea/contador-error
                )
  )

;(add-watch contador :log #(println "contador:" %4))

(defn merge-tables-function [v1 v2]
  (when (or
          (not= (count v1) 2)
          (not= (count v2) 2)
          (not (instance? java.lang.Number (v1 1)))
          (not (instance? java.lang.Number (v2 1)))
          )
    (throw (IllegalStateException. "Values not correct." (clojure.string/join "--" [v1 v2])))
    )
  (if (= (v1 1) 1) ; Si el que lo recibe está en estado 1: ok
    v2
    v1
    )
  )

(defn cmp2 [e1 e2]
  ;    (swap! pea/jaGlobal #(identity %2) #(println (clojure.string/join ":::" [e1 e2])))
  (> (e1 1) (e2 1))
  )

(defn cmp1 [e1 e2]
  ;  (swap! pea/jaGlobal #(identity %2) #(println (clojure.string/join ":::" [e1 e2])))
  (if (< (e1 1) (e2 1)) e2 e1)
  )