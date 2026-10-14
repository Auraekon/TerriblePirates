package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.Misc;

public class TPBoosterEnginesStats extends BaseShipSystemScript {
	public float activeTime = 0f;

	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		if (state == ShipSystemStatsScript.State.OUT) {
			stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
			activeTime = 3f;
		} else {
			stats.getMaxSpeed().modifyFlat(id, 150f * effectLevel);
			stats.getAcceleration().modifyFlat(id, 113f * effectLevel);
		}
		if (state == State.IDLE) {
			if (activeTime > 0f) {
				activeTime -= Global.getCombatEngine().getElapsedInLastFrame();
				stats.getTurnAcceleration().modifyFlat(id, 30f);
				stats.getTurnAcceleration().modifyPercent(id, 200f);
				stats.getMaxTurnRate().modifyFlat(id, 15f);
				stats.getMaxTurnRate().modifyPercent(id, 100f);
				if (((ShipAPI)stats.getEntity()).getFluxTracker().isOverloaded()) {activeTime = 0f;}
			} else {
				stats.getMaxTurnRate().unmodify(id);
				stats.getTurnAcceleration().unmodify(id);
			}
		}
	}
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getMaxSpeed().unmodify(id);
		stats.getAcceleration().unmodify(id);
		stats.getMaxTurnRate().unmodify(id);
		stats.getTurnAcceleration().unmodify(id);
	}

	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (index == 0) {
			return new StatusData("increased engine power", false);
		}
		if (index == 1 && activeTime > 0f && state == State.IDLE) {
			return new StatusData("increased engine maneuverability " + Misc.getRoundedValueMaxOneAfterDecimal(activeTime), false);
		}
		return null;
	}
}


