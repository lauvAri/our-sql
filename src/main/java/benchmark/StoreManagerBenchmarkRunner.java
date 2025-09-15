package benchmark;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class StoreManagerBenchmarkRunner {
    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(StoreManagerBenchmark.class.getSimpleName())
                .forks(1)           // 只 fork 1 个进程
                .warmupIterations(3) // 预热 3 轮
                .measurementIterations(5) // 测量 5 轮
                .build();

        new Runner(options).run();
    }
}
