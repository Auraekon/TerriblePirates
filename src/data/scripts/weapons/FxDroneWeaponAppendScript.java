package data.scripts.weapons;

import com.fs.starfarer.api.combat.*;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.loading.WeaponGroupSpec;
import com.fs.starfarer.api.loading.WeaponGroupType;
import com.fs.starfarer.api.util.Misc;

/**
 *
 */
public class FxDroneWeaponAppendScript implements EveryFrameWeaponEffectPlugin {

    protected ShipAPI ship;
    protected WeaponAPI weapon;
    protected ShipAPI[] demDrones;
    protected Vector2f aimDirection = new Vector2f(0,0);
    private int numberOfWeaponOffsets;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused() || weapon == null) {
            return;
        }
        if (demDrones == null ) {
            this.weapon = weapon;
            this.ship = weapon.getShip();
            numberOfWeaponOffsets = weapon.getSpec().getTurretFireOffsets().size();
            demDrones = new ShipAPI[numberOfWeaponOffsets];

            ShipHullSpecAPI spec = Global.getSettings().getHullSpec("dem_drone");

            for (int i = 0; numberOfWeaponOffsets -1 >= i; i++) {
                ShipVariantAPI v = Global.getSettings().createEmptyVariant("dem_drone" + i, spec);
                WeaponGroupSpec g = new WeaponGroupSpec(WeaponGroupType.LINKED);
                g.addSlot("WS 000");
                v.addWeaponGroup(g);
                v.addWeapon("WS 000", weapon.getSpec().getWeaponId() + "_fxdrone");
                demDrones[i] = Global.getCombatEngine().createFXDrone(v);
                demDrones[i].setLayer(CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER);
                demDrones[i].setOwner(ship.getOriginalOwner());
                demDrones[i].getMutableStats().getHullDamageTakenMult().modifyMult("dem", 0f); // so it's non-targetable
                demDrones[i].setDrone(true);
                demDrones[i].getAIFlags().setFlag(AIFlags.DRONE_MOTHERSHIP, 100000f, ship);
                demDrones[i].getMutableStats().getEnergyWeaponDamageMult().applyMods(ship.getMutableStats().getMissileWeaponDamageMult());
                demDrones[i].getMutableStats().getMissileWeaponDamageMult().applyMods(ship.getMutableStats().getMissileWeaponDamageMult());
                demDrones[i].getMutableStats().getBallisticWeaponDamageMult().applyMods(ship.getMutableStats().getMissileWeaponDamageMult());
                demDrones[i].getMutableStats().getDamageToCapital().applyMods(ship.getMutableStats().getDamageToCapital());
                demDrones[i].getMutableStats().getDamageToCruisers().applyMods(ship.getMutableStats().getDamageToCruisers());
                demDrones[i].getMutableStats().getDamageToDestroyers().applyMods(ship.getMutableStats().getDamageToDestroyers());
                demDrones[i].getMutableStats().getDamageToFrigates().applyMods(ship.getMutableStats().getDamageToFrigates());
                demDrones[i].getMutableStats().getDamageToFighters().applyMods(ship.getMutableStats().getDamageToFighters());
                demDrones[i].getMutableStats().getDamageToMissiles().applyMods(ship.getMutableStats().getDamageToMissiles());
                demDrones[i].getMutableStats().getDamageToTargetEnginesMult().applyMods(ship.getMutableStats().getDamageToTargetEnginesMult());
                demDrones[i].getMutableStats().getDamageToTargetHullMult().applyMods(ship.getMutableStats().getDamageToTargetHullMult());
                demDrones[i].getMutableStats().getDamageToTargetShieldsMult().applyMods(ship.getMutableStats().getDamageToTargetShieldsMult());
                demDrones[i].getMutableStats().getDamageToTargetWeaponsMult().applyMods(ship.getMutableStats().getDamageToTargetWeaponsMult());

                demDrones[i].setDoNotRenderSprite(true);
            // demDrone.setDoNotRenderWeapons(true);
                demDrones[i].setCollisionClass(CollisionClass.NONE);
                demDrones[i].giveCommand(ShipCommand.SELECT_GROUP, null, 0);
                Global.getCombatEngine().addEntity(demDrones[i]);
            }
        };
        for (int i = 0; numberOfWeaponOffsets -1 >= i; i++) {

            demDrones[i].setOwner(ship.getOwner());
            demDrones[i].getLocation().set(weapon.getFirePoint(i));
            demDrones[i].setFacing(weapon.getCurrAngle());
            demDrones[i].getVelocity().set(ship.getVelocity());
            demDrones[i].setAngularVelocity(0);
            // demDrone.setAngularVelocity(ship.getAngularVelocity());  // solve this if 0 is crappy.

            aimDirection = Misc.getUnitVectorAtDegreeAngle(weapon.getCurrAngle());
            aimDirection.scale(1000f);
            Vector2f.add(aimDirection, weapon.getLocation(), aimDirection);
            demDrones[i].getMouseTarget().set(aimDirection);

            //demDrone.getMutableStats().getWeaponTurnRateBonus().modifyMult("dem", 0f);

            WeaponAPI payload = demDrones[i].getWeaponGroupsCopy().get(0).getWeaponsCopy().get(0);
            payload.setFacing(weapon.getCurrAngle());
            payload.setKeepBeamTargetWhileChargingDown(true);
            payload.updateBeamFromPoints();
            payload.getSpec().setMaxRange(weapon.getRange());
            // payload.setRemainingCooldownTo(weapon.getCooldownRemaining());
            // payload.setRemainingCooldownTo(0.0001f);

            if (weapon.getChargeLevel() > 0f & payload.getChargeLevel() == 0f)  {
                demDrones[i].giveCommand(ShipCommand.FIRE, aimDirection, 0);
            }
        }
    }
}
