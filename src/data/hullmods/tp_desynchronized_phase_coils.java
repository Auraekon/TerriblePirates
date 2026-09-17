package data.hullmods;

import java.awt.Color;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class tp_desynchronized_phase_coils extends BaseHullMod {
	private final Color color = new Color(16,9,79,167);
	private final Color redshiftAuraColor = new Color(1, 1, 1,35);
	private final Color phaseDisruptionAuraColorUnder = new Color(102, 39, 244,25);

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getTimeMult().modifyMult(id, 1.3f);
		stats.getVentRateMult().modifyMult(id, 0f);
	}

	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
			ship.getEngineController().fadeToOtherColor(this, color, null, 1f, 0.4f);
			ship.getEngineController().extendFlame(this, -0.25f, -0.25f, -0.25f);
			ship.setJitter(this, redshiftAuraColor, 1f, 3, 0, 12f);
			ship.setJitterUnder(this, phaseDisruptionAuraColorUnder, 1f, 20, 0f, 32f);
	}
}