import java.io.*;
import java.util.*;

public class Experiment {
    static final int[] CUTOFFS = {1,2,3,4,5,7,10,12,15,20,25,30,40,50,75,100};
    static final int[] SIZES   = {10_000, 100_000, 1_000_000, 10_000_000};
    static final String[] TYPES = {"random", "sorted", "reversed", "nearly", "dups"};
    static final int REPS = 15, WARMUP = 5;

    static int[] generate(String type, int n, long seed) {
        Random r = new Random(seed);
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = r.nextInt();
        switch (type) {
            case "sorted" -> Arrays.sort(a);
            case "reversed" -> {
                Arrays.sort(a);
                for (int i = 0; i < n / 2; i++) { int t = a[i]; a[i] = a[n-1-i]; a[n-1-i] = t; }
            }
            case "nearly" -> {
                Arrays.sort(a);
                for (int k = 0; k < n / 100; k++) {          // đảo ~1% cặp phần tử
                    int i = r.nextInt(n), j = r.nextInt(n);
                    int t = a[i]; a[i] = a[j]; a[j] = t;
                }
            }
            case "dups" -> {
                for (int i = 0; i < n; i++) a[i] = r.nextInt(100);
            }
        }
        return a;
    }

    static boolean isSorted(int[] a) {
        for (int i = 1; i < a.length; i++) if (a[i-1] > a[i]) return false;
        return true;
    }

    public static void main(String[] args) throws IOException {
        try (PrintWriter out = new PrintWriter("results/raw.csv")) {
            out.println("type,n,cutoff,rep,nanos");
            for (String type : TYPES) {
                for (int n : SIZES) {
                    int[] data = generate(type, n, 12345L);
                    int[] work = new int[n], aux = new int[n];

                    // warm-up
                    for (int w = 0; w < WARMUP; w++) {
                        System.arraycopy(data, 0, work, 0, n);
                        MergeSortCutoff.sort(work, aux, 10);
                    }

                    List<Integer> order = new ArrayList<>();
                    for (int c : CUTOFFS) order.add(c);
                    Random shuffler = new Random(1);

                    for (int rep = 0; rep < REPS; rep++) {
                        Collections.shuffle(order, shuffler);   
                        for (int c : order) {
                            System.arraycopy(data, 0, work, 0, n);
                            long t0 = System.nanoTime();
                            MergeSortCutoff.sort(work, aux, c);
                            long t1 = System.nanoTime();
                            if (!isSorted(work)) throw new IllegalStateException("Sai kết quả!");
                            out.println(type + "," + n + "," + c + "," + rep + "," + (t1 - t0));
                        }
                    }
                    out.flush();
                    System.out.println("xong " + type + " n=" + n);
                }
            }
        }
    }
}   