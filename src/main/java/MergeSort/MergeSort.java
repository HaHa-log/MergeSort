package MergeSort;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

public class MergeSort {

    // ==================== MERGE SORT ====================

    public static void mergeSortWithCutoff(int[] arr, int left, int right, int cutoff) {
        if (arr == null || arr.length < 2 || left >= right) {
            return;
        }

        int[] aux = new int[arr.length];
        sort(arr, aux, left, right, cutoff);
    }

    private static void sort(int[] arr, int[] aux, int left, int right, int cutoff) {
        if (right <= left) {
            return;
        }

        if (cutoff > 0 && right - left + 1 <= cutoff) {
            insertionSort(arr, left, right);
            return;
        }

        int mid = left + (right - left) / 2;
        sort(arr, aux, left, mid, cutoff);
        sort(arr, aux, mid + 1, right, cutoff);
        merge(arr, aux, left, mid, right);
    }

    public static void insertionSort(int[] arr, int left, int right) {
        for (int i = left + 1; i <= right; i++) {
            int key = arr[i];
            int j = i - 1;

            while (j >= left && arr[j] > key) {
                arr[j + 1] = arr[j];
                j--;
            }

            arr[j + 1] = key;
        }
    }

    public static void merge(int[] arr, int[] aux, int left, int middle, int right) {
        for (int k = left; k <= right; k++) {
            aux[k] = arr[k];
        }

        int i = left;
        int j = middle + 1;

        for (int k = left; k <= right; k++) {
            if (i > middle) {
                arr[k] = aux[j++];
            } else if (j > right) {
                arr[k] = aux[i++];
            } else if (aux[j] < aux[i]) {
                arr[k] = aux[j++];
            } else {
                arr[k] = aux[i++];
            }
        }
    }

    // ==================== GENERATE DATASET ====================

    public static int[] generateRandomArray(int size, long seed) {
        Random rand = new Random(seed);
        int[] arr = new int[size];

        for (int i = 0; i < size; i++) {
            arr[i] = rand.nextInt(10_000_000);
        }

        return arr;
    }

    public static int[] generateDataset(int size, long seed, String dist) {
        if (size <= 0) {
            throw new IllegalArgumentException("Kich thuoc dataset phai lon hon 0.");
        }

        Random rand = new Random(seed);
        int[] arr = new int[size];

        switch (dist) {
            case "random":
                return generateRandomArray(size, seed);

            case "nearly_sorted":
                for (int i = 0; i < size; i++) {
                    arr[i] = i * 10;
                }

                int swaps = Math.max(1, (int) (size * 0.025));
                for (int i = 0; i < swaps; i++) {
                    int a = rand.nextInt(size);
                    int b = rand.nextInt(size);

                    int temp = arr[a];
                    arr[a] = arr[b];
                    arr[b] = temp;
                }
                return arr;

            case "reverse":
                for (int i = 0; i < size; i++) {
                    arr[i] = (size - i) * 10;
                }
                return arr;

            case "few_unique":
                for (int i = 0; i < size; i++) {
                    arr[i] = rand.nextInt(16) * 1000;
                }
                return arr;

            default:
                throw new IllegalArgumentException("Phan phoi khong hop le: " + dist);
        }
    }

    // ==================== SAVE / LOAD DATASET ====================

    // Luu chinh dataset se duoc su dung trong benchmark vao file.
    public static void saveDataset(int[] arr, String filePath) throws IOException {
        File file = new File(filePath);
        File parent = file.getParentFile();

        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Khong the tao thu muc: " + parent.getAbsolutePath());
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (int value : arr) {
                bw.write(Integer.toString(value));
                bw.newLine();
            }
        }
    }

    // Doc lai dataset tu file da luu.
    public static int[] loadDataset(String filePath, int expectedSize) throws IOException {
        int[] arr = new int[expectedSize];

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            for (int i = 0; i < expectedSize; i++) {
                String line = br.readLine();

                if (line == null) {
                    throw new IOException(
                            "Dataset khong du " + expectedSize + " phan tu: " + filePath
                    );
                }

                try {
                    arr[i] = Integer.parseInt(line.trim());
                } catch (NumberFormatException e) {
                    throw new IOException(
                            "Du lieu khong hop le tai dong " + (i + 1) + " trong " + filePath,
                            e
                    );
                }
            }

            if (br.readLine() != null) {
                throw new IOException("Dataset co nhieu hon " + expectedSize + " phan tu: " + filePath);
            }
        }

        return arr;
    }

    // Tao dataset, luu ra file, sau do doc lai chinh file do.
    // Mang tra ve la mang se duoc dung trong benchmark.
    public static int[] createSaveAndLoadDataset(
            int size, long seed, String dist, String filePath
    ) throws IOException {
        int[] generated = generateDataset(size, seed, dist);
        saveDataset(generated, filePath);
        return loadDataset(filePath, size);
    }

    // ==================== UTILITIES ====================

    public static boolean isSorted(int[] arr) {
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] < arr[i - 1]) {
                return false;
            }
        }
        return true;
    }

    private static double mean(double[] values) {
        double sum = 0.0;

        for (double value : values) {
            sum += value;
        }

        return sum / values.length;
    }

    private static double sampleStdDev(double[] values, double avg) {
        if (values.length < 2) {
            return 0.0;
        }

        double sumSq = 0.0;

        for (double value : values) {
            double difference = value - avg;
            sumSq += difference * difference;
        }

        return Math.sqrt(sumSq / (values.length - 1));
    }

    // ==================== MAIN ====================

    public static void main(String[] args) throws IOException {
        int[] cutoffs = {0, 4, 7, 10, 12, 16, 20, 24, 32, 48, 64, 96};
        int[] doublingSizes = {62_500, 125_000, 250_000, 500_000, 1_000_000};
        String[] distributions = {"random", "nearly_sorted", "reverse", "few_unique"};

        int primaryN = 500_000;
        int trials = 7;
        int doublingTrials = 5;

        String datasetFolder = "datasets";

        // 1. JVM WARM-UP
        System.out.println("=== 1. JVM WARM-UP ===");

        for (int w = 0; w < 12; w++) {
            int[] warm = generateDataset(50_000, 999L + w, "random");
            mergeSortWithCutoff(warm, 0, warm.length - 1, (w % 2 == 0) ? 0 : 10);
        }

        System.gc();

        // 2. BENCHMARK VA XUAT DATASET
        // Moi dataset duoc tao mot lan, luu file, doc lai,
        // sau do moi cutoff deu dung ban sao cua dataset do.
        System.out.println("=== 2. TAO DATASET CHO THI NGHIEM ===");

        try (PrintWriter csv = new PrintWriter(new FileWriter("results.csv"))) {
            csv.println("n,cutoff,distribution,architecture,trial,seed,duration_ms,verified_sorted");

            // A: QUET CUTOFF TREN 4 PHAN PHOI
            System.out.println(
                    "=== 3. QUET DAI CUTOFF TAI N = " + primaryN
                            + " (TRIALS = " + trials + ") ==="
            );

            for (String dist : distributions) {
                System.out.println("\n--- Phan phoi: " + dist + " ---");
                System.out.printf(
                        Locale.US, "%-12s | %-12s | %-12s | %-12s%n",
                        "CUTOFF", "Mean (ms)", "StdDev (ms)", "vs C=0 (%)"
                );
                System.out.println("----------------------------------------------------------");

                double[][] samples = new double[cutoffs.length][trials];

                for (int t = 0; t < trials; t++) {
                    long seed = 1000L + t;

                    String filePath = String.format(
                            Locale.US,
                            "%s/A_%s_%d_seed%d.txt",
                            datasetFolder, dist, primaryN, seed
                    );

                    // Dataset duoc tao, luu, doc lai truoc khi benchmark.
                    int[] base = createSaveAndLoadDataset(
                            primaryN, seed, dist, filePath
                    );

                    for (int ci = 0; ci < cutoffs.length; ci++) {
                        int cutoff = cutoffs[ci];

                        // Moi cutoff nhan mot ban sao sach cua cung dataset goc.
                        int[] data = Arrays.copyOf(base, base.length);

                        long start = System.nanoTime();
                        mergeSortWithCutoff(data, 0, data.length - 1, cutoff);
                        double ms = (System.nanoTime() - start) / 1_000_000.0;

                        boolean ok = isSorted(data);

                        if (!ok) {
                            throw new AssertionError(
                                    "Sap xep sai tai dist=" + dist + ", cutoff=" + cutoff
                            );
                        }

                        samples[ci][t] = ms;

                        csv.printf(
                                Locale.US,
                                "%d,%d,%s,%s,%d,%d,%.4f,%b%n",
                                primaryN, cutoff, dist, "single_aux",
                                t + 1, seed, ms, ok
                        );
                    }
                }

                double baselineMean = mean(samples[0]);
                int bestCutoff = cutoffs[0];
                double bestMean = baselineMean;

                for (int ci = 0; ci < cutoffs.length; ci++) {
                    double avg = mean(samples[ci]);
                    double sd = sampleStdDev(samples[ci], avg);
                    double speedup = ((baselineMean - avg) / baselineMean) * 100.0;

                    if (avg < bestMean) {
                        bestMean = avg;
                        bestCutoff = cutoffs[ci];
                    }

                    System.out.printf(
                            Locale.US,
                            "%-12s | %12.3f | %12.3f | %+11.2f%%%n",
                            cutoffs[ci] == 0 ? "0 (Goc)" : String.valueOf(cutoffs[ci]),
                            avg, sd, speedup
                    );
                }

                System.out.printf(
                        Locale.US,
                        "=> Toi uu cho [%s]: CUTOFF = %d (%.3f ms)%n",
                        dist, bestCutoff, bestMean
                );
            }

            // B: DOUBLING RATIO TEST
            System.out.println(
                    "\n=== 4. DOUBLING RATIO TEST (RANDOM, CUTOFF = 0, 7, 10, 24) ==="
            );

            int[] keyCutoffs = {0, 7, 10, 24};

            System.out.printf(
                    Locale.US,
                    "%-10s | %-11s | %-11s | %-11s | %-11s | %-11s%n",
                    "N", "C=0 (ms)", "C=7 (ms)", "C=10 (ms)", "C=24 (ms)",
                    "T(N)/T(N/2)"
            );
            System.out.println(
                    "-------------------------------------------------------------------------------"
            );

            double prevC10 = -1.0;

            for (int n : doublingSizes) {
                double[] avgByCutoff = new double[keyCutoffs.length];

                // Tao, luu va doc lai 5 dataset.
                // Tat ca cutoff se su dung cung 5 dataset nay.
                int[][] baseDatasets = new int[doublingTrials][];
                long[] seeds = new long[doublingTrials];

                for (int t = 0; t < doublingTrials; t++) {
                    long seed = 2000L + t;
                    seeds[t] = seed;

                    String filePath = String.format(
                            Locale.US,
                            "%s/B_random_%d_seed%d.txt",
                            datasetFolder, n, seed
                    );

                    baseDatasets[t] = createSaveAndLoadDataset(
                            n, seed, "random", filePath
                    );
                }

                // Benchmark tung cutoff tren cung bo dataset.
                for (int ci = 0; ci < keyCutoffs.length; ci++) {
                    int cutoff = keyCutoffs[ci];
                    double[] runs = new double[doublingTrials];

                    for (int t = 0; t < doublingTrials; t++) {
                        int[] data = Arrays.copyOf(baseDatasets[t], n);

                        long start = System.nanoTime();
                        mergeSortWithCutoff(data, 0, data.length - 1, cutoff);
                        double ms = (System.nanoTime() - start) / 1_000_000.0;

                        boolean ok = isSorted(data);

                        if (!ok) {
                            throw new AssertionError(
                                    "Sap xep sai tai Doubling Test, n=" + n
                                            + ", cutoff=" + cutoff
                            );
                        }

                        runs[t] = ms;

                        csv.printf(
                                Locale.US,
                                "%d,%d,%s,%s,%d,%d,%.4f,%b%n",
                                n, cutoff, "random", "single_aux",
                                t + 1, seeds[t], ms, ok
                        );
                    }

                    avgByCutoff[ci] = mean(runs);
                }

                String ratioStr = (prevC10 > 0)
                        ? String.format(Locale.US, "%.3f", avgByCutoff[2] / prevC10)
                        : "---";

                prevC10 = avgByCutoff[2];

                System.out.printf(
                        Locale.US,
                        "%-10d | %11.3f | %11.3f | %11.3f | %11.3f | %11s%n",
                        n,
                        avgByCutoff[0],
                        avgByCutoff[1],
                        avgByCutoff[2],
                        avgByCutoff[3],
                        ratioStr
                );
            }
        }

        System.out.println("\nDa xuat dataset va ket qua benchmark.");
        System.out.println("Dataset folder: " + new File(datasetFolder).getAbsolutePath());
        System.out.println("Results file: " + new File("results.csv").getAbsolutePath());
    }
}
