(ns experiment)

(import 'sheduling.ShedulingUtility)

(load-file "./src/loaderFile.clj")

(defn init []
  (ShedulingUtility/start)

  (let [
         eProfiler (agent (profiler/create) ; :error-mode :continue
                     :error-handler pea/profiler-error)

         eManager (agent (manager/create eProfiler) ; :error-mode :continue
                    :error-handler pea/manager-error)
         ]

    (send eProfiler profiler/init eManager)

    (send eManager manager/session
      (vec (for [_ (range 20)] [#(r2 eProfiler eManager) "r2"]))
      )
    )

  :ok )

(init)