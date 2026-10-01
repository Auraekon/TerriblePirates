package data.hullmods;
import java.awt.*;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.Misc;

public class tp_auxiliary_engine_core extends BaseHullMod {

    private static final float ZERO_FLUX_MULT = 50f;
    private static final float ENGINE_HEALTH_BONUS = 100f;
    public static float VENT_RATE_BONUS = 75f;
    public static float OVERLOAD_BONUS = 33f;

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getZeroFluxMinimumFluxLevel().modifyFlat(id, ZERO_FLUX_MULT * 0.02f); // should set it usable at 5% or lower
        stats.getEngineHealthBonus().modifyPercent(id, ENGINE_HEALTH_BONUS);
        stats.getVentRateMult().modifyPercent(id, VENT_RATE_BONUS);
        stats.getOverloadTimeMod().modifyMult(id, 1f - OVERLOAD_BONUS * 0.01f);
	}

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 3f;
        float oPad = 10f;
        Color b = Misc.getHighlightColor();
        Color good = Misc.getPositiveHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        tooltip.setBulletedListMode("");
        tooltip.addPara("Zero-flux bonus triggers if current flux is below %s max.", pad, good, Math.round(ZERO_FLUX_MULT) + "%");
        tooltip.addPara("Increases the durability of the ship's engines by %s.", pad, good, Math.round(ENGINE_HEALTH_BONUS) + "%");
        tooltip.addPara("Increases active flux vent rate by %s.", pad, good, Math.round(VENT_RATE_BONUS) + "%");
        tooltip.addPara("Reduces overload duration by %s.", pad, good, Math.round(OVERLOAD_BONUS) + "%");
        tooltip.setBulletedListMode(null);
    }
}
