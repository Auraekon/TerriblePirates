package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class tp_medium_ballistics_integration extends BaseHullMod {

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {        
        stats.getDynamic().getMod(Stats.MEDIUM_BALLISTIC_MOD).modifyFlat(id, -10);
    }
    
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + 10 + "";
        return null;
    }
    
    @Override
    public boolean affectsOPCosts() {
        return true;
    }
}