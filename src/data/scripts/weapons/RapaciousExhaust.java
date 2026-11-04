package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.plugins.MagicTrailPlugin;

import java.awt.*;
import java.util.Map;

public class RapaciousExhaust implements EveryFrameWeaponEffectPlugin {
    float trailID = MagicTrailPlugin.getUniqueID();
    SpriteAPI sprite = Global.getSettings().getSprite("fx", "base_trail_smooth");
    final static private Color brightRed = new Color(1f, 0.235f, 0.0f, 0.95f);
    final static private Color darkRed = new Color(1f, 0.11f, 0.11f, 0.95f);
    final static private Color muzzleFlashBrightColor = new Color(1f, 0.81f, 0.81f, 0.95f);
    final static private Color muzzleFlashLessBrightColor = new Color(1f, 0.61f, 0.61f, 0.95f);
    final static private Color muzzleFlashDimColor = new Color(1f, 0.11f, 0.11f, 0.95f);
    final static private Color muzzleFlashDimColorFaint = new Color(1f, 0.11f, 0.11f, 0.25f);
    final static private Color smokeColor = new Color(0.22f, 0.18f, 0.12f, 0.45f);


    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (!Global.getCombatEngine().isPaused()) {
            ShipAPI ship = weapon.getShip();
            var shipVelocity = ship.getVelocity();
            var firePoint = weapon.getFirePoint(0);
            var aimDirection = Misc.getUnitVectorAtDegreeAngle(weapon.getCurrAngle());
            var exhaustVector = Vector2f.add(shipVelocity, (Vector2f) aimDirection.scale(105f), aimDirection);

            if (ship.isAlive()) {
                final float scale = 0.1f*Misc.random.nextFloat();
                engine.addNebulaSmokeParticle(firePoint, exhaustVector, 5.0f, 1.5f + 8f * Misc.random.nextFloat(), 0.66f, 0.1f, 0.8f-(scale*2.0f), smokeColor);
               // engine.addHitParticle(firePoint, shipVelocity, 8f, 255f, 0.2f, muzzleFlashBrightColor);
               // engine.addHitParticle(firePoint, shipVelocity, 12f, 255f, 0.3f, muzzleFlashLessBrightColor);
               // engine.addHitParticle(firePoint, shipVelocity, 24f, 255f, 0.5f, muzzleFlashDimColor);
               // engine.addHitParticle(firePoint, shipVelocity, 48f, 255f, 0.5f, muzzleFlashDimColorFaint);
               // MagicTrailPlugin.addTrailMemberAdvanced(ship, this.trailID, this.sprite, weapon.getLocation(), 0.0F, 0.0F, VectorUtils.getFacing(ship.getVelocity()), ship.getAngularVelocity() / 4.0F, ship.getAngularVelocity() / 8.0F, 12.0F, 8.0F, brightRed, darkRed, 0.75F, 0.05F, 0.17F, 0.17F, 770, 1, 50.0F, 300.0F, 30.0F, new Vector2f(), (Map)null, CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER, 1.0F);

            }

        }
    }
}