/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pl.nowojski.simd.blog;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.CompilerControl;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.Random;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.openjdk.jmh.annotations.Mode.AverageTime;
import static org.openjdk.jmh.annotations.Scope.Thread;

@State(Thread)
@OutputTimeUnit(NANOSECONDS)
@BenchmarkMode(AverageTime)
@Fork(value = 1, jvmArgsAppend = {
        "-XX:+UseSuperWord",
        "-XX:+UnlockDiagnosticVMOptions",
        "-XX:CompileCommand=print,*BenchmarkSIMDBlog.hashLoop"})
@Warmup(iterations = 5)
@Measurement(iterations = 10)
public class BenchmarkSIMDBlog
{
    public static final int SIZE = 1024;

    @State(Thread)
    public static class Context
    {
        public final int[] values = new int[SIZE];
        public final int[] results = new int[SIZE];

        @Setup
        public void setup()
        {
            Random random = new Random();
            for (int i = 0; i < SIZE; i++) {
                values[i] = random.nextInt(Integer.MAX_VALUE / 32);
            }
        }
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE) //makes looking at assembly easier
    public int[] increment(Context context)
    {
        for (int i = 0; i < SIZE; i++) {
            context.results[i] = context.values[i] + 1;
        }
        return context.results;
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE) //makes looking at assembly easier
    public int[] hashLoop(Context context)
    {
        for (int i = 0; i < SIZE; i++) {
            context.results[i] = getHashPosition(context.values[i], 1048575);
        }
        return context.results;
    }

    private static int getHashPosition(int rawHash, int mask)
    {
        rawHash ^= rawHash >>> 15;
        rawHash *= 0xed558ccd;
        rawHash ^= rawHash >>> 15;
        rawHash *= 0x1a85ec53;
        rawHash ^= rawHash >>> 15;

        return rawHash & mask;
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE) //makes looking at assembly easier
    public void hashLoopPart(Context context)
    {
        for (int i = 0; i < SIZE; i++) {
            context.results[i] = getHashPosition1(context.values[i]);
        }
    }

    private static int getHashPosition1(int rawHash)
    {
        rawHash ^= rawHash >>> 15;
        rawHash *= 0xed558ccd;
        return rawHash;
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE) //makes looking at assembly easier
    public int[] hashLoopSplit(Context context)
    {
        for (int i = 0; i < SIZE; i++) {
            context.results[i] = getHashPosition1(context.values[i]);
        }

        for (int i = 0; i < SIZE; i++) {
            context.results[i] = getHashPosition2(context.results[i]);
        }

        for (int i = 0; i < SIZE; i++) {
            context.results[i] = getHashPosition3(context.results[i], 1048575);
        }

        return context.results;
    }

    private static int getHashPosition2(int rawHash)
    {
        rawHash ^= rawHash >>> 15;
        rawHash *= 0x1a85ec53;
        return rawHash;
    }

    private static int getHashPosition3(int rawHash, int mask)
    {
        rawHash ^= rawHash >>> 15;
        return rawHash & mask;
    }
}
