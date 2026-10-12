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

	public static WeaponSize getLargestBallisticSlot(ShipAPI ship) {
		if (ship == null) return null;
		WeaponSize largest = null;
		for (WeaponSlotAPI slot : ship.getHullSpec().getAllWeaponSlotsCopy()) {
			if (slot.isDecorative() ) continue;
			if (slot.getWeaponType() == WeaponType.BALLISTIC) {
				if (largest == null || largest.ordinal() < slot.getSlotSize().ordinal()) {
					largest = slot.getSlotSize();
				}
			}
		}
		return largest;
	}
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		WeaponSize largest = getLargestBallisticSlot(ship);
		if (largest == null) return;
		float small = 0f;
		float medium = 0f;
		float max = 0f;
		switch (largest) {
		case LARGE:
			small = BONUS_SMALL_3;
			medium = BONUS_MEDIUM_3;
			max = BONUS_MAX_3;
			break;
		case MEDIUM:
			small = BONUS_SMALL_2;
			max = BONUS_MAX_2;
			break;
		case SMALL:
			small = BONUS_SMALL_1;
			max = BONUS_MAX_1;
			break;
		}
		
		ship.addListener(new RangefinderRangeModifier(small, medium, max));
	}
	
	public static class RangefinderRangeModifier implements WeaponBaseRangeModifier {
		public float small, medium, max;
		public RangefinderRangeModifier(float small, float medium, float max) {
			this.small = small;
			this.medium = medium;
			this.max = max;
		}
		
		public float getWeaponBaseRangePercentMod(ShipAPI ship, WeaponAPI weapon) {
			return 0;
		}
		public float getWeaponBaseRangeMultMod(ShipAPI ship, WeaponAPI weapon) {
			return 1f;
		}
		public float getWeaponBaseRangeFlatMod(ShipAPI ship, WeaponAPI weapon) {
			if (weapon.getSpec() == null) {
				return 0f;
			}
			if (weapon.getSpec().getMountType() != WeaponType.BALLISTIC && 
					weapon.getSpec().getMountType() != WeaponType.HYBRID) {
				return 0f;
			}
			if (weapon.hasAIHint(AIHints.PD)) {
				return 0f;
			}
			
			float bonus = 0;
			if (weapon.getSize() == WeaponSize.SMALL) {
				bonus = small;
			} else if (weapon.getSize() == WeaponSize.MEDIUM) {
				bonus = medium;
			}
			if (weapon.getSpec().getMountType() == WeaponType.HYBRID) {
				bonus *= HYBRID_MULT;
				if (bonus < HYBRID_BONUS_MIN) {
					bonus = HYBRID_BONUS_MIN;
				}
			}
			if (bonus == 0f) return 0f;
			
			float base = weapon.getSpec().getMaxRange();
			if (base + bonus > max) {
				bonus = max - base;
			}
			if (bonus < 0) bonus = 0;
			return bonus;
		}
	}
}









