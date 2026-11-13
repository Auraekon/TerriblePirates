package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import java.awt.*;

public class tp_battlecruiser_retrofit extends BaseHullMod {

    private static final float FLUX_DISSIPATION_MULT = 15f;
    private static final float SPEED_ADD = 25f;
    private static final float ACCEL_MULT = 25f;


    private static final float RANGE_THRESHOLD = 700f;
    private static final float RANGE_MULT = 0.25f;
    private static final float ZERO_FLUX_MULT = 1f;



    private static final float FIGHTER_RANGE_CAP = 1800f;
    private static final float FIGHTER_REFIT_MULT = 25f;


    // Capital only SO analog that caps range at 700 and gives +25 speed. Zero flux bonus below 1% max flux. +15% flux dissipation. 25% fighter refit time, cap fighter engage range at 1800.
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().modifyFlat(id, SPEED_ADD);
        stats.getAcceleration().modifyFlat(id, 1f + ACCEL_MULT * 0.01f);
        stats.getDeceleration().modifyFlat(id, 1f + ACCEL_MULT * 0.01f);
        stats.getZeroFluxMinimumFluxLevel().modifyFlat(id, ZERO_FLUX_MULT * 0.01f);

        stats.getFluxDissipation().modifyMult(id, 1f + FLUX_DISSIPATION_MULT * 0.01f);

        stats.getWeaponRangeThreshold().modifyFlat(id, RANGE_THRESHOLD);
        stats.getWeaponRangeMultPastThreshold().modifyMult(id, RANGE_MULT);

        stats.getFighterWingRange().modifyFlat(id, FIGHTER_RANGE_CAP);
        stats.getFighterRefitTimeMult().modifyPercent(id, FIGHTER_REFIT_MULT);
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + ZERO_FLUX_MULT + "%";
        if (index == 1) return "" + FLUX_DISSIPATION_MULT + "%";
        if (index == 2) return "" + FIGHTER_REFIT_MULT + "%";
        return null;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return ship.getVariant().getHullSize() == HullSize.CAPITAL_SHIP;
    }

    public String getUnapplicableReason(ShipAPI ship) {
        if (ship.getVariant().getHullSize() != HullSize.CAPITAL_SHIP) {
            return "Can only be installed on capital ships.";
        }
        return null;
    }

    private Color color = new Color(255,100,255,255);
    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        ship.getEngineController().fadeToOtherColor(this, color, null, 1f, 0.133f);
        ship.getEngineController().extendFlame(this, 0.083f, 0.083f, 0.083f);
//		}
    }
}
