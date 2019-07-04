/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lagswitch;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicInteger;
/**
 *
 * @author Miguel
 */
public class Flood extends Thread {
    
    private volatile boolean running = false;
    private static final AtomicInteger n_datagrams = new AtomicInteger(0);
    
    SecureRandom random;
    long delay;
    
    Flood(long d) {
        random = new SecureRandom();
        delay = d + randInt(0, 100);
    }
    
    private int randInt(int min, int max) {
        return random.nextInt(max - min) + min;
    }
    
    private InetAddress randomHost() {
        int n = 1, m = 130;
        String ip = 
                randInt(n, m) + "." + 
                randInt(n, m) + "." + 
                randInt(n, m) + "." + 
                randInt(n, m);
        try {
            return InetAddress.getByName(ip);
        } catch (UnknownHostException ex) {
            return null;
        }
    }
    
    public void flood_process() {
        try {
            DatagramSocket socketUDP = new DatagramSocket();
            socketUDP.setBroadcast(true);
            byte[] msg = new byte[randInt(35000, 65000)];
            random.nextBytes(msg);
            while(running) {
                try {
                    int port = randInt(50000, 65000);
                    InetAddress host = randomHost();
                    DatagramPacket packet = new DatagramPacket(msg, msg.length, host, port);
                    socketUDP.send(packet);
                    n_datagrams.getAndIncrement();
                    Thread.sleep(delay);
                } catch (Exception ex) {}
            }
            socketUDP.close();
        } catch (SocketException ex) {}
    }
    
    public void terminate() {
        if(!running) return;
        running = false;
    }

    public static int get_n_datagrams() {
        return n_datagrams.get();
    }
    
    @Override
    public void run() {
        running = true;
        flood_process();
    }
}
