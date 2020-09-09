/*
 *
 *  * The MIT License
 *  *
 *  * Copyright 2006 - 2020 kpfalzer.
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in
 *  * all copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  * THE SOFTWARE.
 *
 *
 */

package gblibx;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class PeriodicWorkTest {

    static class Work extends PeriodicWork<Integer> {

        public Work() {
            super(5, new Consumer<List<Integer>>() {
                @Override
                public void accept(List<Integer> integers) {
                    for (Integer i : integers) {
                        System.out.println(i);
                    }
                }
            });
        }
    }

    @Test
    void add() throws InterruptedException {
        Work work = new Work();
        int n = work.add(5,6,7);
        assertEquals(3, n);
        Thread.sleep(10*1000);
        assertEquals(0, work.howMuchWork());
        assertFalse(work.hasWork());
        assertFalse(work.timerRunning());
        work.add(789);
        assertEquals(1, work.howMuchWork());
        assertTrue(work.hasWork());
        assertTrue(work.timerRunning());
        Thread.sleep(8*1000);
        assertFalse(work.hasWork());
        assertFalse(work.timerRunning());
    }
}