import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;

public class MyCache {
    int cache_size, associativity, block_size, total_sets;
    List<List<Integer>> cache_data;
    
    public MyCache(int cache_size, int associativity, int block_size) {
        this.cache_size = cache_size;
        this.associativity = associativity;
        this.block_size = block_size;
        this.total_sets = (cache_size) / (associativity * block_size);
        this.cache_data = new ArrayList<>(total_sets);
        for (int i = 0; i < total_sets; i++) {
            List<Integer> set_append = new ArrayList<>(associativity);
            for (int j = 0; j < associativity; j++) {
                set_append.add(-1);
            }
            cache_data.add(set_append);
        }
    }
    public void runCache(List<String> file_input) {
        int misses = 0;
        int hits = 0;
        int[] set_wise_misses = new int[total_sets];
        int[] set_wise_hits = new int[total_sets];
        for (String memory_address : file_input) {
            long decimal_address = Long.parseLong(memory_address, 16);
            int set_index;
            if (total_sets == 1) {
                set_index = 0;
            } else {
                set_index = (int) ((decimal_address >> 6) & (total_sets - 1));
            }
            int tag = (int) (decimal_address >> 6) >> ((int) (Math.log(total_sets) / Math.log(2)));
            List<Integer> set = cache_data.get(set_index);
            boolean cache_hits = false;
            for (int i = 0; i < associativity; i++) {
                int block_tag = set.get(i);
                if (block_tag == tag) {
                    cache_hits = true;
                    hits++;
                    set_wise_hits[set_index]++;
                    if (set.get(associativity - 1) != -1) {
                        set.remove(i);
                        set.add(tag);
                    } else {
                        for (int j = i + 1; j < associativity; j++) {
                            if (set.get(j) == (-1)) {
                                set.set(j, tag);
                                set.remove(i);
                                set.add(-1);
                                break;
                            }
                        }
                    }
                    break;
                }
            }
            if (cache_hits==false) {
                misses++;
                set_wise_misses[set_index]++;
                int lru_index = 0;
                for (int k = 0; k < associativity; k++) {
                    if (set.get(k) == (-1))
                    {
                        lru_index = k;
                        break;
                    }
                }
                if (lru_index == 0 && set.get(0) != -1) {
                    set.remove(0);
                    set.add(tag);
                } else
                    set.set(lru_index, tag);
            }
        }
        System.out.println("Misses: " + misses);
        System.out.println("Hits: " + hits);
        System.out.println("Set-wise Misses for the given trace file: " + Arrays.toString(set_wise_misses));
        System.out.println("Set-wise Hits for the given trace file: " + Arrays.toString(set_wise_hits));
    }
    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println(
                    "Invalid input. \nUsage: java MyCache <cache_size in KB> <associativity> <block_size> <trace_file>");
            return;
        }
        int cache_size = Integer.parseInt(args[0]) * 1024;
        int associativity = Integer.parseInt(args[1]);
        int block_size = 64;
        String trace = args[3];
        System.out.println("The Cache size is assumed to be in KB");
        if (Integer.parseInt(args[2]) != block_size) {
            System.out.println("The block size is assumed to be 64 bytes instead of " + args[2]);
        }
        List<String> input = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File(trace))) {
            while (scanner.hasNextLine()) {
                String address = scanner.nextLine();
                input.add(address);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Trace file not found in the specified location");
            return;
        }
        MyCache mycache = new MyCache(cache_size, associativity, block_size);
        mycache.runCache(input);
    }
}