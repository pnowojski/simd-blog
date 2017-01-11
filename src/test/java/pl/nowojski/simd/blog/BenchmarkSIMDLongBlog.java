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
        "-XX:CompileCommand=print,*BenchmarkSIMDLongBlog.bitshift"})
@Warmup(iterations = 5)
@Measurement(iterations = 10)
public class BenchmarkSIMDLongBlog
{
    public static final int SIZE = 1024;

    @State(Thread)
    public static class Context
    {
        public final long[] values = new long[SIZE];
        public final long[] temporary = new long[SIZE];
        public final int[] results = new int[SIZE];

        @Setup
        public void setup()
        {
            Random random = new Random();
            for (int i = 0; i < SIZE; i++) {
                values[i] = random.nextLong() % (Long.MAX_VALUE / 32L);
            }
        }
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE) //makes looking at assembly easier
    public void increment(Context context)
    {
        for (int i = 0; i < SIZE; i++) {
            context.temporary[i] = context.values[i] + 1;
        }
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE) //makes looking at assembly easier
    public void bitshift(Context context)
    {
        for (int i = 0; i < SIZE; i++) {
            context.temporary[i] = context.values[i] / 2;
        }
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE) //makes looking at assembly easier
    public int[] hashLongLoop(Context context)
    {
        for (int i = 0; i < SIZE; i++) {
            context.results[i] = getHashPosition(context.values[i], 1048575);
        }

        return context.results;
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE) //makes looking at assembly easier
    public int[] hashLongLoopSplit(Context context)
    {
        for (int i = 0; i < SIZE; i++) {
            context.temporary[i] = getHashPositionMangle(context.values[i]);
        }
        for (int i = 0; i < SIZE; i++) {
            context.temporary[i] = getHashPositionMul1(context.values[i]);
        }
        for (int i = 0; i < SIZE; i++) {
            context.temporary[i] = getHashPositionMangle(context.values[i]);
        }
        for (int i = 0; i < SIZE; i++) {
            context.temporary[i] = getHashPositionMul2(context.values[i]);
        }
        for (int i = 0; i < SIZE; i++) {
            context.temporary[i] = getHashPositionMangle(context.values[i]);
        }
        for (int i = 0; i < SIZE; i++) {
            context.results[i] = getHashPositionCast(context.values[i]);
        }
        for (int i = 0; i < SIZE; i++) {
            context.results[i] = getHashPositionMask(context.results[i], 1048575);
        }

        return context.results;
    }

    private static int getHashPosition(long rawHash, int mask)
    {
        rawHash ^= rawHash >>> 33;
        rawHash *= 0xff51afd7ed558ccdL;
        rawHash ^= rawHash >>> 33;
        rawHash *= 0xc4ceb9fe1a85ec53L;
        rawHash ^= rawHash >>> 33;

        return (int) (rawHash & mask);
    }

    private static long getHashPositionMangle(long rawHash)
    {
        return rawHash ^ (rawHash >>> 33);
    }

    private static long getHashPositionMul1(long rawHash)
    {
        return rawHash * 0xff51afd7ed558ccdL;
    }

    private static long getHashPositionMul2(long rawHash)
    {
        return rawHash * 0xc4ceb9fe1a85ec53L;
    }

    private static int getHashPositionCast(long rawHash)
    {
        return (int) rawHash;
    }

    private static int getHashPositionMask(int rawHash, int mask)
    {
        return rawHash & mask;
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE) //makes looking at assembly easier
    public long[] hashLoopTwoInstructions(Context context)
    {
        for (int i = 0; i < SIZE; i++) {
            context.temporary[i] = getHashPositionTwoInstructions(context.values[i]);
        }
        return context.temporary;
    }

    private static long getHashPositionTwoInstructions(long rawHash)
    {
        rawHash ^= rawHash >>> 33;
        return rawHash * 0xff51afd7ed558ccdL;
    }
}
