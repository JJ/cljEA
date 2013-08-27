(ns experiment)

(import 'sheduling.ShedulingUtility)

;(import 'java.io.FileReader)
;(import 'java.io.IOException)
;(import 'java.io.PrintStream)
;(import 'java.io.PrintWriter)
;(import 'java.io.FileOutputStream)
;
;(def fileStream (PrintStream. (FileOutputStream. "log.txt")))
;
;(System/setOut fileStream)
;(System/setErr fileStream)

(load-file "./src/loaderFile.clj")

(defn init []
  (ShedulingUtility/start)

  (let [
         eProfiler (agent (profiler/create) ;                        :error-mode :continue
                     :error-handler pea/profiler-error)

         eManager (agent (manager/create eProfiler) ;                        :error-mode :continue
                    :error-handler pea/manager-error)
         ]

    (send eProfiler profiler/init eManager)

    (send eManager manager/session ; RUN!
      (vec (for [_ (range 20)] [#(r2 eProfiler eManager) "r2"]))

      ;      [
      ;        [#(r1 eProfiler eManager) "r1"]
      ;        ;        [#(r2 eProfiler eManager) "r2"]
      ;        ]

      )
    )

  :ok )

(init)