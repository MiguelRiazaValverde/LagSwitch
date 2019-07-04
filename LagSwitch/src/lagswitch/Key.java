package lagswitch;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

public class Key implements NativeKeyListener {
    
    public static String key;
    public static String increase;
    public static String decrease;
    private static boolean pressed = false;
    
    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (key.equals(NativeKeyEvent.getKeyText(e.getKeyCode())) && !pressed) {
            LagSwitch.click(false);
            pressed = true;
        }
        else if(increase.equals(NativeKeyEvent.getKeyText(e.getKeyCode())))
            LagSwitch.increase_threads();
        else if(decrease.equals(NativeKeyEvent.getKeyText(e.getKeyCode())))
            LagSwitch.decrease_threads();
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        if (key.equals(NativeKeyEvent.getKeyText(e.getKeyCode())) && pressed) {
            LagSwitch.click(true);
            pressed = false;
        }
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {}

    public static void close() {
        try {
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException e1) {}
    }
    
    public static void init() {
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.WARNING);
        logger.setUseParentHandlers(false);
        
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());
            System.exit(1);
        }
        GlobalScreen.addNativeKeyListener(new Key());
    }
}
