package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class tp_improvised_missile_storage extends BaseHullMod {

    public static float AMMO_BONUS = 100f;
    public static float SMOD_ROF_MULT = 0.8f;

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getMissileAmmoBonus().modifyPercent(id, AMMO_BONUS);
        stats.getMissileRoFMult().modifyMult(id, SMOD_ROF_MULT);
        //stats.getMissileWeaponDamageMult().modifyPercent(id, 1000f);
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + (int) AMMO_BONUS + "%";
        if (index == 1) return "" + (int) Math.round((1f - SMOD_ROF_MULT) * 100f) + "%";
        return null;
    }
}
