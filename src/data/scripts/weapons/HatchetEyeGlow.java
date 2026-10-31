package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import java.awt.Color;
import java.util.Map;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.plugins.MagicTrailPlugin;

public class HatchetEyeGlow implements EveryFrameWeaponEffectPlugin {
    float trailID = MagicTrailPlugin.getUniqueID();
    SpriteAPI sprite = Global.getSettings().getSprite("fx", "base_trail_smooth");
    final static private Color brightRed = new Color(1f, 0.235f, 0.0f, 0.95f);
    final static private Color darkRed = new Color(1f, 0.11f, 0.11f, 0.95f);

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (!Global.getCombatEngine().isPaused()) {
            ShipAPI ship = weapon.getShip();
            if (ship.isAlive()) {
                MagicTrailPlugin.addTrailMemberAdvanced(ship, this.trailID, this.sprite, weapon.getLocation(), 0.0F, 0.0F, VectorUtils.getFacing(ship.getVelocity()), ship.getAngularVelocity() / 4.0F, ship.getAngularVelocity() / 8.0F, 12.0F, 8.0F, brightRed, darkRed, 0.75F, 0.05F, 0.17F, 0.17F, 770, 1, 50.0F, 300.0F, 30.0F, new Vector2f(), (Map)null, CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER, 1.0F);
            }

        }
    }
}