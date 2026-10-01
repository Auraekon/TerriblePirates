package data.scripts.weapons.proj;
import java.awt.Color;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.loading.WeaponGroupSpec;
import com.fs.starfarer.api.loading.WeaponGroupType;
import org.lwjgl.util.vector.Vector2f;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.graphics.SpriteAPI;
import org.lwjgl.opengl.GL11;
import java.util.EnumSet;

public class DrakeMuzzleFlashEffect implements OnFireEffectPlugin, OnHitEffectPlugin {
    final static private Color muzzleFlashBrightColor = new Color(1f, 0.81f, 0.81f, 0.95f);
    final static private Color muzzleFlashLessBrightColor = new Color(1f, 0.61f, 0.61f, 0.95f);
    final static private Color muzzleFlashDimColor = new Color(1f, 0.11f, 0.11f, 0.95f);
    final static private Color muzzleFlashDimColorFaint = new Color(1f, 0.11f, 0.11f, 0.25f);
    final static private Color empBright = new Color(1f, 0.21f, 0.21f, 1f);
    final static private Color empDim = new Color(1f, 0.11f, 0.11f, 1f);
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {

    }

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        if (weapon != null) {
            final ShipAPI ship = weapon.getShip();
            if (ship != null) {
                // engine.spawnMuzzleFlashOrSmoke(ship, weapon.getFirePoint(0), weapon.getSpec(), weapon.getCurrAngle());
                final Vector2f shipVelocity = ship.getVelocity();
                Vector2f muzzleLoc = weapon.getFirePoint(0);
                var numberOfWeaponOffsets = weapon.getSpec().getTurretFireOffsets().size();
                engine.addHitParticle(projectile.getSpawnLocation(), shipVelocity, 8f, 255f, 0.2f, muzzleFlashBrightColor);
                engine.addHitParticle(projectile.getSpawnLocation(), shipVelocity, 12f, 255f, 0.3f, muzzleFlashLessBrightColor);
                engine.addHitParticle(projectile.getSpawnLocation(), shipVelocity, 24f, 255f, 0.5f, muzzleFlashDimColor);
                engine.addHitParticle(projectile.getSpawnLocation(), shipVelocity, 48f, 255f, 0.5f, muzzleFlashDimColorFaint);
                for (int i = 0; i < 4; i++) {
                    Vector2f randomArcLocation = new Vector2f(muzzleLoc.x - 15f + Math.round(Math.random() * 30f), muzzleLoc.y - 15f + Math.round(Math.random() * 30f));
                    engine.spawnEmpArcVisual(muzzleLoc, ship, randomArcLocation, ship, 10f, empBright, empDim);
                }

                //engine.addLayeredRenderingPlugin(new LockedMuzzleFlashRenderer(weapon));
            }
        }
    }

    private static class LockedMuzzleFlashRenderer extends BaseCombatLayeredRenderingPlugin {
        private final WeaponAPI weapon;
        private final SpriteAPI sprite;

        // FIXED: Reduced timings to realistic combat speeds (Total 0.22 seconds)
        private final float durationFadeIn = 0.02f;
        private final float durationHold = 0.05f;
        private final float durationFadeOut = 0.15f;
        private final float totalDuration = durationFadeIn + durationHold + durationFadeOut;

        private float elapsed = 0f;
        private boolean isDone = false;

        public LockedMuzzleFlashRenderer(WeaponAPI weapon) {
            this.weapon = weapon;
            this.sprite = Global.getSettings().getSprite("fx", "laser_splash_flash");
        }

        @Override
        public void advance(float amount) {
            if (isDone) return;

            elapsed += amount;
            if (elapsed >= totalDuration) {
                isDone = true;
            }
        }

        public void render(CombatEngineLayers layer, Vector2f viewportCenter, float viewportWidth, float viewportHeight) {
            if (isDone || weapon == null) return;

            float alpha = 0f;
            if (elapsed < durationFadeIn) {
                alpha = elapsed / durationFadeIn;
            } else if (elapsed < durationFadeIn + durationHold) {
                alpha = 1f;
            } else {
                float timeInFadeOut = elapsed - (durationFadeIn + durationHold);
                alpha = 1f - (timeInFadeOut / durationFadeOut);
            }
            alpha = Math.max(0f, Math.min(1f, alpha));

            Vector2f currentFirePoint = weapon.getFirePoint(0);
            float currentAngle = weapon.getCurrAngle();

            sprite.setAlphaMult(alpha);

            // FIXED: Your image is horizontal (left to right).
            // Starsector's 0 degrees faces East (right). Use currentAngle directly.
            // If the flash points backwards, change this to (currentAngle + 180f)
            sprite.setAngle(currentAngle);

            float sizeMultiplier = 1f + (elapsed / totalDuration) * 0.2f;

            // FIXED: Your image is wider than it is tall.
            // Matching the dimensions to the original layout (Width = 46, Height = 30)
            sprite.setSize(46f * sizeMultiplier, 30f * sizeMultiplier);

            sprite.setBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            sprite.renderAtCenter(currentFirePoint.x, currentFirePoint.y);
        }

        @Override
        public boolean isExpired() {
            return isDone;
        }

        @Override
        public EnumSet<CombatEngineLayers> getActiveLayers() {
            return EnumSet.of(CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER);
        }
    }
}

