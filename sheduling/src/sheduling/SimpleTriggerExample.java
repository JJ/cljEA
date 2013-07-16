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

package sheduling;

import java.util.concurrent.Callable;

public class SimpleTriggerExample {
    public static void main(String[] args) throws Exception {

        ShedulingUtility.start();

        ShedulingUtility.send_after(2000, new Callable() {
            @Override
            public Object call() throws Exception {
                System.out.println("yesss!");
                ShedulingUtility.shutdown();
                return null;
            }
        });

        ShedulingUtility.send_after(1000, new Callable() {
            @Override
            public Object call() throws Exception {
                System.out.println("you?");
                return null;
            }
        });

    }
}
