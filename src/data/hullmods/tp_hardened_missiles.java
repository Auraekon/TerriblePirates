package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;


public class tp_hardened_missiles extends BaseHullMod {

    public static float MISSILE_DAMAGE_MULT = 0.667f;
    public static float MISSILE_HEALTH_MULT = 1.33f;

// 33% reduced missile damage. 33% increased missile health.

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getMissileWeaponDamageMult().modifyMult(id, MISSILE_DAMAGE_MULT);
        stats.getMissileHealthBonus().modifyMult(id, MISSILE_HEALTH_MULT);

//        if (isSMod(stats)) {
//            stats.getMissileHealthBonus().unmodifyMult(id);
//        }
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return processTooltipMultiModifier(MISSILE_DAMAGE_MULT);
        if (index == 1) return processTooltipMultiModifier(MISSILE_HEALTH_MULT);
        return null;
    }

    private String processTooltipMultiModifier(float tooltipNum) {
        return Math.round((tooltipNum * 100f - 100f)) + "%";
    }
}
