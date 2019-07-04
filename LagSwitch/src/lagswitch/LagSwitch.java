package lagswitch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class LagSwitch {
    
    private static volatile int pool = 35;
    public static long delay = 50;
    
    private static Flood[] floods;
    private static boolean running = false;
    private static Timer timer = null;
    
    private static int mode_activation = 1;
    private static int lag_type = 1;
    private static long ms = 2000;
    private static long interval = 3000;
    private static boolean management_threads = false;
    
    private static final Scanner in = new Scanner(System.in);
    
    public static void main(String[] args) {
        try {
            init(args);
        } catch (IOException ex) {
            System.out.println("Error in input data. Bye :(");
            System.exit(2);
        } catch (Exception ex) {
            System.out.println("An error has occurred. Bye :(");
            System.exit(3);
        }
    }
    
    public static void init(String[] args) throws IOException {
        if(args.length >= 1)
            pool = Integer.parseInt(args[0]);
        if(args.length >= 2)
            delay = Integer.parseInt(args[1]);
        
        load_parameters();
        Key.init();
        
        while(true) {
            clear_interval();
            desactivate();
            
            System.out.println("");
            System.out.println("1 - HotKey.");
            System.out.println("2 - Mode.");
            System.out.println("3 - Parameters.");
            System.out.println("4 - Management of threads.");
            System.out.println("5 - Save configuration.");
            System.out.println("6 - Info.");
            System.out.println("7 - Exit.");
            int option = read_int(7);
            
            switch(option) {
                case 1:
                    System.out.println("");
                    System.out.print("HotKey (one letter): ");
                    char hotkey = in.next().charAt(0);
                    Key.key = ("" + hotkey).toUpperCase();
                    break;
                case 2:
                    System.out.println("");
                    configuration_mode();
                    break;
                case 3:
                    System.out.println("");
                    System.out.print("Number of threads (~60 recommended): ");
                    pool = in.nextInt();
                    pool = pool > 0 ? pool : 35;
                    System.out.print("Milliseconds delay by thread (~25 recommended): ");
                    delay = in.nextInt();
                    delay = delay > 0 ? delay : 50;
                    break;
                case 4:
                    System.out.println("");
                    while(true) {
                        System.out.print("Activate thread management? [y/n]: ");
                        char c = in.next().charAt(0);
                        if(c == 'y' || c == 'n')  {
                            management_threads = c == 'y';
                            if(!management_threads) break;
                            char increase = ' ';
                            while(!Character.isLetter(increase) && !Character.isDigit(increase)) {
                                System.out.print("Key to increase the amount of threads: ");
                                increase = in.next().charAt(0);
                            }
                            char decrease = ' ';
                            while(!Character.isLetter(decrease) && !Character.isDigit(decrease)) {
                                System.out.print("Key to decrease the amount of threads: ");
                                decrease = in.next().charAt(0);
                            }
                            Key.increase = ("" + increase).toUpperCase();
                            Key.decrease = ("" + decrease).toUpperCase();
                            break;
                        }
                        System.out.println("Write 'y' or 'n'");
                    }
                    break;
                case 5:
                    System.out.println("");
                    save_parameters();
                    System.out.println("Saved.");
                    break;
                case 6:
                    System.out.println("");
                    System.out.println("Packets sended: " + Flood.get_n_datagrams());
                    System.out.println("Configuration:");
                    System.out.println("\tHotKey: " + Key.key);
                    System.out.println("\tManagement thread: " + management_threads);
                    if(management_threads) {
                        System.out.println("\t\tKey increase: " + Key.increase);
                        System.out.println("\t\tKey decrease: " + Key.decrease);
                    }
                    System.out.println("\tThreads: " + pool);
                    System.out.println("\tDelay between messages " + delay + " milliseconds");
                    System.out.println("\tMode activation: " + (mode_activation == 1 ? "Pressed" : "Press activate/Press desactivate"));
                    System.out.print("\tType lag: ");
                    switch(lag_type) {
                        case 1:
                            System.out.println("Continuous");
                            break;
                        case 2:
                            System.out.println("Interval");
                            System.out.println("\t\t"  + ms + " milliseconds each " + interval + " milliseconds");
                            break;
                    }
                    break;
                case 7:
                    Key.close();
                    System.out.println("Bye :)");
                    System.exit(0);
            }
            
            
        }
    }
    
    private static void save_parameters() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter("parameters.conf"));
        String p = 
                Key.key + " " + 
                Key.increase + " " +
                Key.decrease + " " +
                management_threads + " " +
                pool + " " + 
                delay + " " + 
                ms + " " + 
                interval + " " + 
                mode_activation + " " + 
                lag_type;
        writer.write(p);
        writer.close();
    }
    
    private static void load_parameters() throws FileNotFoundException {
        File file = new File("./parameters.conf"); 
        Scanner sc = new Scanner(file);
        
        Key.key = "" + sc.next().charAt(0);
        Key.increase = "" + sc.next().charAt(0);
        Key.decrease = "" + sc.next().charAt(0);
        management_threads = sc.nextBoolean();
        pool = sc.nextInt();
        delay = sc.nextLong();
        ms = sc.nextLong();
        interval = sc.nextLong();
        mode_activation = sc.nextInt();
        lag_type = sc.nextInt();
        
        sc.close();
    }
    
    private static void configuration_mode() throws IOException {
        System.out.println("Mode activation:");
        System.out.println("\t1 - On key press active and on release desactive.");
        System.out.println("\t2 - On key press active and on next key press desactive.");
        mode_activation = read_int(2);

        System.out.println("Mode lag:");
        System.out.println("\t1 - Continuous. It emits a lag constant.");
        System.out.println("\t2 - Interval. It emits pulses of lag of X milliseconds every Y milliseconds.");
        lag_type = read_int(2);

        switch(lag_type) {
            case 1:
                break;
            case 2:
                System.out.print("Milliseconds of the pulse (~2000 milliseconds recommended, " + ms + " actual): ");
                ms = in.nextLong();
                while(true) {
                    System.out.print("Milliseconds of the interval (~3000 milliseconds recommended, " + interval + " actual): ");
                    interval = in.nextLong();
                    if(interval <= ms)
                        System.out.print("The interval must be less than the milliseconds.");
                    else
                        break;
                }
                break;
        }
    }
    
    public static synchronized void click(boolean release) {
        switch(lag_type) {
            case 1:
                if(mode_activation == 1)
                    if(release) desactivate();
                    else activate();
                else
                    if(release) break;
                    else if(running) desactivate();
                    else activate();
                break;
            case 2:
                if(mode_activation == 1)
                    if(!release) set_interval(ms, interval);
                    else clear_interval();
                else {
                    if(release) break;
                    else if(running) clear_interval();
                    else set_interval(ms, interval);
                }
                break;
        }
    }
    
    
    private static int read_int(int max) throws IOException {
        while(true) {
            try {
                System.out.print("Option (1-" + max + "): ");
                int n = in.nextInt();

                if(n > 0 && n <= max)
                    return n;

                System.out.println("Insert number between 1 and " + max + ".");
            } catch(Exception e) {
                in.next();
            }
        }
    }
    
    public static synchronized void increase_threads() {
        if(!management_threads) return;
        pool++;
    }
    
    public static synchronized void decrease_threads() {
        if(!management_threads) return;
        pool--;
    }
    
    private static synchronized void run_threads() {
        floods = new Flood[pool];
        for(int i = 0; i < floods.length; i++) {
            floods[i] = new Flood(delay);
            floods[i].start();
        }
    }
    
    private static synchronized void clear_threads() {
        for(int i = 0; i < floods.length; i++)
            if(floods[i] != null) {
                floods[i].terminate();
                floods[i] = null;
            }
    }
    
    public static synchronized void set_pool_size(int p) {
        clear_threads();
        pool = p;
    }
    
    public static synchronized void activate() {
        if(running) return;
        run_threads();
        running = true;
    }
    
    public static synchronized  void desactivate() {
        if(!running || timer != null) return;
        clear_threads();
        running = false;
    }
    
    public static synchronized void set_interval(long duration, long each_ms) {
        if(running) return;
        if(each_ms <= duration) 
            throw new RuntimeException("The duration must be less than the repetition period");
        running = true;
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                run_threads();
                try { Thread.sleep(duration); } catch (InterruptedException ex) {}
                clear_threads();
            }
        }, 1, each_ms);
    }
    
    public static synchronized void clear_interval() {
        if(timer != null) {
            timer.cancel();
            timer = null;
            running = false;
        }
    }
}
