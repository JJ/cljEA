/**
 * Author José Albert Cruz Almaguer <jalbertcruz@gmail.com>
 * Copyright 2013 by José Albert Cruz Almaguer.
 *
 * This program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http:www.gnu.org/licenses/agpl-3.0.txt) for more details.
 */

package main;

import clojure.lang.RT;
import clojure.lang.Var;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Object result;
        if (args.length > 0 && args[0].toLowerCase().equals("seq")) {
            RT.var("clojure.core", "load-file").invoke("./scripts/sequential/experiment-run.clj");
            Var report = RT.var("sequential.experiment-run", "run");
            result = report.invoke();
        } else {
            RT.var("clojure.core", "load-file").invoke("./scripts/experiment-run.clj");
            Var report = RT.var("experiment", "run");
            result = report.invoke();
        }
        System.out.println(result);

    }

}


