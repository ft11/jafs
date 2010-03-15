package client.ui;

import client.Main;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.Vector;

/**
 *
 * @author Julian Sanio
 */
public class UiManager {

    public static final int decoHeight = 0;
    public static final int borderWidth = 1;

    private static Vector<UiWindow> content;

    public static void init() {
        content = new Vector<UiWindow>();
    }

    public static boolean addComponent(UiWindow c) {
        return content.add(c);
    }

    public static boolean removeComponent(UiWindow c) {
        return content.remove(c);
    }

    public static void renderAll(Graphics2D g) {
       for(int i=0; i<content.size(); i++) {
            renderComponent(content.get(i), g);
        }
    }

    private static void renderComponent(UiWindow cmp, Graphics2D g) {
        cmp.render(g);
    }

    public static void setForeground(UiWindow u) {
        content.remove(u);
        content.add(u);

        Main.getScreen().getContentPane().setComponentZOrder(u, 0);
    }
}
