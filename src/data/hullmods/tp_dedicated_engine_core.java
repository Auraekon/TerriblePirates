package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;

public class tp_dedicated_engine_core extends BaseHullMod {

    private static final float ZERO_FLUX_MULT = 50f;
    private static final float ENGINE_HEALTH_BONUS = 100f;
    public static float VENT_RATE_BONUS = 25f;
    public static float OVERLOAD_BONUS = 33f;
    public static float TURRET_SPEED_BONUS = 50f;

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getZeroFluxMinimumFluxLevel().modifyFlat(id, ZERO_FLUX_MULT * 0.01f); // should set it usable at 5% or lower
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
        tooltip.addPara("Reduces overload duration by %s.", pad, good, Math.round(OVERLOAD_BONUS) + "%");
        tooltip.setBulletedListMode(null);
    }
}
