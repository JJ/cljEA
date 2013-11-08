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

(import 'sheduling.ShedulingUtility)

(load-file "scripts/loaderFile.clj")

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
      (vec (for [_ (range problem/repetitions)] [#(r2 eProfiler eManager) "r2"]))
      )
    )

  :ok )

;(init)