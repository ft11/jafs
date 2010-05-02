/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package common.engine;

import common.CVector2;
import common.resource.CImage;
import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 *
 * @author miracle
 */
public class CWeapon {
    /**
     *
     */
    protected Color color;
    /**
     *
     */
    protected int damage;
    /**
     *
     */
    protected double critRate;
    /**
     *
     */
    protected long coolDown;
    /**
     *
     */
    protected long shotLast;
    /**
     *
     */
    protected double spread;
    /**
     *
     */
    protected int speed;
    /**
     *
     */
    protected int bullets;
    /**
     *
     */
    protected int maxBullets;
    /**
     *
     */
    protected int id;

    /**
     *
     * @param dmg
     */
    public void setDamage(int dmg) {
        this.damage = dmg;
    }

    /**
     *
     * @return
     */
    public int getDamage() {
        return damage;
    }

    /**
     *
     * @param rate
     */
    public void setCriticalRate(double rate) {
        critRate = rate;
    }

    /**
     *
     * @return
     */
    public double getCriticalRate() {
        return critRate;
    }

    /**
     *
     * @param cd
     */
    public void setCoolDown(long cd) {
        coolDown = cd;
    }

    /**
     *
     * @return
     */
    public long getCoolDown() {
        return coolDown;
    }

    /**
     *
     * @return
     */
    public long getShotLast() {
        return shotLast;
    }

    /**
     *
     * @param spread
     */
    public void setSpread(double spread) {
        this.spread = spread;
    }

    /**
     *
     * @return
     */
    public double getSpread() {
        return spread;
    }

    /**
     *
     * @param speed
     */
    public void setSpeed(int speed) {
        this.speed = speed;
    }

    /**
     *
     * @return
     */
    public int getSpeed() {
        return speed;
    }

    /**
     *
     * @param img
     */
    public void setColor(Color img) {
        color = img;
    }

    /**
     *
     * @return
     */
    public Color getColor() {
        return color;
    }

    /**
     *
     * @param b
     */
    public void setMaxBullets(int b) {
        maxBullets = b;
    }

    /**
     *
     * @return
     */
    public int getMaxBullets() {
        return maxBullets;
    }

    /**
     *
     * @param b
     */
    public void setBullets(int b) {
        bullets = b;
    }

    /**
     *
     * @return
     */
    public int getBullets() {
        return bullets;
    }

    /**
     *
     * @return
     */
    public int getId() {
        return id;
    }

    /**
     *
     * @param player
     * @param u
     * @return
     */
    public CProjectile shoot(CPlayer player, UpdateLoop u) {
        if(Math.abs(shotLast - u.getCurrentTime()) > coolDown && bullets > 0) {
            if(maxBullets != -1)
                bullets--;
            shotLast = u.getCurrentTime();
            CVector2 offset = player.getDirection().resize_cpy(25.0d);
            CVector2 pos = player.getPosition().add_cpy(offset);
            CVector2 dir = player.getDirection().cpy();
            return new CProjectile(player.getId(), speed, this, dir, pos);
        }
        return null;
    }
}
