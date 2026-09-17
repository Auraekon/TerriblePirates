package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;


public class tp_missile_stowage extends BaseHullMod {

    public static float AMMO_BONUS = 100f;
    public static float ROF_MULT = 0.8f;

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getMissileRoFMult().modifyMult(id, ROF_MULT);
        stats.getMissileAmmoBonus().modifyPercent(id, AMMO_BONUS);
	}

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 3f;
        float oPad = 10f;
        Color b = Misc.getHighlightColor();
        Color good = Misc.getPositiveHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        tooltip.setBulletedListMode("");
        tooltip.addPara("Sets missile fire rate to %s.", pad, good, Math.round(ROF_MULT * 100f)  + "%");
        tooltip.addPara("Increases max missile ammo by %s.", pad, good, Math.round(AMMO_BONUS) + "%");
        tooltip.setBulletedListMode(null);
    }
}
