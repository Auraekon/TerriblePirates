package data.hullmods;
import java.awt.Color;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.util.Misc;

public class tp_safety_overrides_fighter extends BaseHullMod {
	private static final float RANGE_THRESHOLD = 550f;
	private static final float RANGE_MULT = 0.25f;

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getZeroFluxMinimumFluxLevel().modifyFlat(id, 2f); // set to two, meaning boost is always on
		stats.getVentRateMult().modifyMult(id, 0f);
		stats.getWeaponRangeThreshold().modifyFlat(id, RANGE_THRESHOLD);
		stats.getWeaponRangeMultPastThreshold().modifyMult(id, RANGE_MULT);
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return Misc.getRoundedValue(RANGE_THRESHOLD);
		return null;
	}

	private Color color = new Color(255,100,255,255);
	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
		ship.getEngineController().fadeToOtherColor(this, color, null, 1f, 0.4f);
		ship.getEngineController().extendFlame(this, 0.25f, 0.25f, 0.25f);
	}
}