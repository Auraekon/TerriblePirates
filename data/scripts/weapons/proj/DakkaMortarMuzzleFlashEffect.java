package data.scripts.weapons.proj;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.EnumSet;

public class DakkaMortarMuzzleFlashEffect implements OnFireEffectPlugin {
    final static private Color smokeColor = new Color(0.8f, 0.71f, 0.77f, 0.75f);
    final static private Color muzzleFlashBrightColor = new Color(1f, 0.81f, 0.81f, 0.95f);
    final static private Color muzzleFlashLessBrightColor = new Color(1f, 0.61f, 0.61f, 0.95f);
    final static private Color muzzleFlashDimColor = new Color(1f, 0.11f, 0.11f, 0.95f);
    final static private Color muzzleFlashDimColorFaint = new Color(1f, 0.11f, 0.11f, 0.25f);
    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        if(weapon != null) {
            final ShipAPI ship = weapon.getShip();
            if(ship != null) {
                final Vector2f shipVelocity = ship.getVelocity();
                final Vector2f projectileVelocity = projectile.getVelocity();
                final Vector2f location = projectile.getLocation();
                //engine.addLayeredRenderingPlugin(new LockedMuzzleFlashRenderer(weapon));
                if(shipVelocity != null && projectileVelocity != null && weapon.getSpec() != null) {
                    final Color flashGlowColor = weapon.getSpec().getGlowColor();
                    if(flashGlowColor != null) {
                        //void addNebulaSmokeParticle(Vector2f loc, Vector2f vel, float size,
                        //float endSizeMult, float rampUpFraction,
                        //float fullBrightnessFraction, float totalDuration, Color color);
                        engine.addNebulaSmokeParticle(location, shipVelocity, 25.0f, 0.1f, 1.0f, 1.0f, 0.2f, flashGlowColor);
                        engine.addHitParticle(projectile.getSpawnLocation(), shipVelocity, 8f, 255f, 0.2f, muzzleFlashBrightColor);
                        engine.addHitParticle(projectile.getSpawnLocation(), shipVelocity, 12f, 255f, 0.3f, muzzleFlashLessBrightColor);
                        engine.addHitParticle(projectile.getSpawnLocation(), shipVelocity, 24f, 255f, 0.5f, muzzleFlashDimColor);
                        engine.addHitParticle(projectile.getSpawnLocation(), shipVelocity, 48f, 255f, 0.5f, muzzleFlashDimColorFaint);
                        for(int i = 0; i < 4; i++) {
                            final float scale = 0.1f*Misc.random.nextFloat();
                            Vector2f velocity = new Vector2f(shipVelocity.x + projectileVelocity.x * scale, shipVelocity.y + projectileVelocity.y * scale);
                            engine.addNebulaSmokeParticle(location, velocity, 5.0f, 1.5f + 8f * Misc.random.nextFloat(), 0.66f, 0.1f, 0.8f-(scale*2.0f), smokeColor);
                        }
                    }
                }
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

