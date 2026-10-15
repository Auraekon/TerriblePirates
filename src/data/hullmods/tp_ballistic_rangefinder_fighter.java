package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.combat.listeners.WeaponBaseRangeModifier;
import com.fs.starfarer.api.loading.WeaponSlotAPI;

public class tp_ballistic_rangefinder_fighter extends BaseHullMod {

	public static float BONUS_MAX_1 = 800;
	public static float BONUS_MAX_2 = 800;
	public static float BONUS_MAX_3 = 900;
	public static float BONUS_SMALL_1 = 100;
	public static float BONUS_SMALL_2 = 100;
	public static float BONUS_SMALL_3 = 200;
	public static float BONUS_MEDIUM_3 = 100;
	
	public static float HYBRID_MULT = 2f;
	public static float HYBRID_BONUS_MIN = 100f;
	

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
	}
}









