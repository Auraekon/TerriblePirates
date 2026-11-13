package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import org.lwjgl.util.vector.Vector2f;


public class tp_light_missiles extends BaseHullMod {

    public static float MISSILE_DAMAGE_MULT = 0.8f;
    public static float MISSILE_HEALTH_MULT = 0.8f;
    public static float MISSILE_SPEED_MULT = 1.33f;
    private static final float LAUNCH_MULT = 1.33f;

    private static final String PROCESSED_KEY =
            "tp_light_missiles_processed";


// 20% reduced missile damage and health, 33% increased missile speed and maneuverability

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getMissileWeaponDamageMult().modifyMult(id, MISSILE_DAMAGE_MULT);
        stats.getMissileHealthBonus().modifyMult(id, MISSILE_HEALTH_MULT);
        stats.getMissileMaxSpeedBonus().modifyMult(id,  MISSILE_SPEED_MULT);
        stats.getMissileTurnAccelerationBonus().modifyMult(id,  MISSILE_SPEED_MULT);
        stats.getMissileMaxTurnRateBonus().modifyMult(id,  MISSILE_SPEED_MULT);
        stats.getMissileAccelerationBonus().modifyMult(id,  MISSILE_SPEED_MULT);

//        if (isSMod(stats)) {
//            stats.getMissileHealthBonus().unmodifyMult(id);
//        }
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return processTooltipMultiModifier(MISSILE_DAMAGE_MULT);
        if (index == 1) return processTooltipMultiModifier(MISSILE_SPEED_MULT);
        return null;
    }

    private String processTooltipMultiModifier(float tooltipNum) {
        return Math.round((tooltipNum * 100f - 100f)) + "%";
    }

    // increase the launch speed of missile projectiles
    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {

        CombatEngineAPI engine = Global.getCombatEngine();

        if (engine == null || engine.isPaused()) return;
        if (ship.isHulk()) return;

        for (MissileAPI missile : engine.getMissiles()) {

            // Only affect missiles originating from this ship.
            if (missile.getSource() != ship) continue;

            // Only affect first order (launched by the ship, not mirv nor script spawned sub-munition) missiles originating from this ship.
            //if (missile.getSource() != ship || missile.getWeapon() != null && missile.getWeapon().getShip() != ship) continue;

            // Each missile must only be modified once.
            if (missile.getCustomData()
                    .containsKey(PROCESSED_KEY)) {
                continue;
            }

            missile.setCustomData(PROCESSED_KEY, true);

            Vector2f velocity = missile.getVelocity();
            Vector2f shipVelocity = ship.getVelocity();

            // Reduce launch velocity relative to the ship,
            // while preserving the ship's own movement.
            velocity.x = shipVelocity.x +
                    (velocity.x - shipVelocity.x) * LAUNCH_MULT;

            velocity.y = shipVelocity.y +
                    (velocity.y - shipVelocity.y) * LAUNCH_MULT;
        }
    }
}
