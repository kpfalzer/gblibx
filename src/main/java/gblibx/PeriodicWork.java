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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import static gblibx.Util.isNonNull;
import static java.util.Objects.isNull;

/**
 * Accumulate work and dispatch periodically.
 */
public class PeriodicWork<T> {
    public PeriodicWork(int periodSec, Consumer<List<T>> consumer) {
        _periodMilliSec = 1000 * periodSec;
        _consumer = consumer;
    }

    public boolean timerRunning() {
        return isNonNull(_timer);
    }

    public boolean hasWork() {
        boolean empty = false;
        synchronized (_work) {
            empty = _work.isEmpty();
        }
        return !empty;
    }

    public int howMuchWork() {
        int n = 0;
        synchronized (_work) {
            n = _work.size();
        }
        return n;
    }

    public int add(T... work) {
        if (isNull(_timer)) {
            _timer = new Thread(new _Timer());
            _timer.start();
        }
        synchronized (_work) {
            _work.addAll(Arrays.asList(work));
        }
        return howMuchWork();
    }

    private class _Timer implements Runnable {

        @Override
        public void run() {
            try {
                Thread.sleep(_periodMilliSec);
            } catch (InterruptedException e) {
                ;//ignore: e.printStackTrace();
            }
            synchronized (_work) {
                if (!_work.isEmpty()) {
                    _consumer.accept(_work);
                    _work.clear();
                }
            }
            synchronized (_timer) {
                _timer = null;
            }
        }
    }

    private final LinkedList<T> _work = new LinkedList<>();
    private final Consumer<List<T>> _consumer;
    private final long _periodMilliSec;
    private Thread _timer = null;
}
