package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.*;

public class tp_drive_capacitators extends BaseHullMod {

    private static final float ZERO_FLUX_MULT = 10f;
    private static final float FLUX_CAP_MULT = 10f;

    // Adds 10% to flux cap and gives zero flux bonus under 10% flux max.
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getZeroFluxMinimumFluxLevel().modifyFlat(id, ZERO_FLUX_MULT * 0.01f); // should set it usable at 5% or lower
        stats.getFluxCapacity().modifyMult(id, 1 + FLUX_CAP_MULT * 0.01f);
	}

//    @Override
//    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
//        float pad = 3f;
//        Color good = Misc.getPositiveHighlightColor();
//        tooltip.setBulletedListMode("");
//        tooltip.addPara("Zero-flux bonus triggers if current flux is below %s max.", pad, good, ZERO_FLUX_MULT + "%");
//        tooltip.addPara("Increases flux capacity by %s.", pad, good, FLUX_CAP_MULT + "%");
//        tooltip.setBulletedListMode(null);
//    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + FLUX_CAP_MULT + "%";
        if (index == 1) return "" + ZERO_FLUX_MULT + "%";
        return null;
    }
}
