package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.loading.WeaponGroupSpec;
import com.fs.starfarer.api.loading.WeaponGroupType;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;

/**
 *
 */
public class FxDroneWeaponStackScript implements EveryFrameWeaponEffectPlugin {

    protected ShipAPI ship;
    protected WeaponAPI weapon;
    protected ShipAPI demDrone;
    protected Vector2f aimDirection = new Vector2f(0,0);

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused() || weapon == null) {
            return;
        }
        if (demDrone == null ) {
            this.weapon = weapon;
            this.ship = weapon.getShip();
            ShipHullSpecAPI spec = Global.getSettings().getHullSpec("dem_drone");
            
            ShipVariantAPI v = Global.getSettings().createEmptyVariant("dem_drone", spec);
            WeaponGroupSpec g = new WeaponGroupSpec(WeaponGroupType.LINKED);
            g.addSlot("WS 000");
            v.addWeaponGroup(g);
            v.addWeapon("WS 000", weapon.getSpec().getWeaponId() + "_fxdrone");
            demDrone = Global.getCombatEngine().createFXDrone(v);
            demDrone.setLayer(CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER);
            demDrone.setOwner(ship.getOriginalOwner());
            demDrone.getMutableStats().getHullDamageTakenMult().modifyMult("dem", 0f); // so it's non-targetable
            demDrone.setDrone(true);
            demDrone.getAIFlags().setFlag(AIFlags.DRONE_MOTHERSHIP, 100000f, ship);

            demDrone.setDoNotRenderSprite(true);
        // demDrone.setDoNotRenderWeapons(true);
            demDrone.setCollisionClass(CollisionClass.NONE);
            demDrone.giveCommand(ShipCommand.SELECT_GROUP, null, 0);
            Global.getCombatEngine().addEntity(demDrone);
        };

            demDrone.setOwner(ship.getOwner());
            demDrone.getLocation().set(weapon.getLocation());
            demDrone.setFacing(weapon.getCurrAngle());
            demDrone.getVelocity().set(ship.getVelocity());
            demDrone.setAngularVelocity(0);
            // demDrone.setAngularVelocity(ship.getAngularVelocity());  // solve this if 0 is crappy.

            aimDirection = Misc.getUnitVectorAtDegreeAngle(weapon.getCurrAngle());
            aimDirection.scale(1000f);
            Vector2f.add(aimDirection, weapon.getLocation(), aimDirection);
            demDrone.getMouseTarget().set(aimDirection);

            //demDrone.getMutableStats().getWeaponTurnRateBonus().modifyMult("dem", 0f);

            WeaponAPI payload = demDrone.getWeaponGroupsCopy().get(0).getWeaponsCopy().get(0);
            payload.setFacing(weapon.getCurrAngle());
            payload.setWeaponGlowHeightMult(1);
            payload.setWeaponGlowWidthMult(1);
            // payload.setRemainingCooldownTo(weapon.getCooldownRemaining());
            // payload.setRemainingCooldownTo(0.0001f);

            if (weapon.getChargeLevel() > 0f)  {
                demDrone.giveCommand(ShipCommand.FIRE, aimDirection, 0);
            }
    }
}
