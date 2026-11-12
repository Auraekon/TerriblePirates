package data.scripts.weapons;

import java.awt.Color;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;

/**
 * Visual-only tiled/scrolled beam with brightness-linked muzzle flash.
 * The native BeamAPI remains responsible for ALL collisions and damage.
 */
public class DrakeAnimationScript implements EveryFrameWeaponEffectPlugin {

    private boolean rendererAdded = false;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (rendererAdded || engine == null || weapon == null || weapon.getShip() == null) {
            return;
        }
        engine.addLayeredRenderingPlugin(new ZappyRenderer(weapon));
        rendererAdded = true;
    }


    private static final class ZappyRenderer extends BaseCombatLayeredRenderingPlugin {
        // Supplied zappy_beam.png is VERTICAL, 64 x 256 pixels.
        // At these dimensions the artwork is rendered at 1 px per world unit.
        private static final float VISUAL_WIDTH = 14.5f;
        private static final float TILE_LENGTH = 256f;

        // World units per second, travelling from muzzle towards impact.
        // Set to 0 for stationary tiling; set negative to reverse the scroll.
        private static final float SCROLL_SPEED = 1500f;

        // Distance over which the lightning fades to zero opacity before
        // BeamAPI.getTo(). For short beams, fade at most the final half.
        // Set to 0f to disable the endpoint fade.
        private static final float END_FADE_LENGTH = 128f;

        // drake_muzzle_flash.png is VERTICAL, 32 x 52 pixels.
        // Its bright rounded bulb is centered about 39 px below the top.
        // Position that bulb at BeamAPI.getFrom(), with the tip facing out.
        private static final float FLASH_WIDTH = 32f;
        private static final float FLASH_LENGTH = 52f;
        private static final float FLASH_BULB_Y_FROM_TOP = 39f;

        // 1 = exactly the native beam brightness * viewport alpha.
        // Can be lowered if the additive flash looks too intense.
        private static final float FLASH_ALPHA_MULT = 1f;

        private final WeaponAPI weapon;
        private final SpriteAPI sprite;
        private final SpriteAPI muzzleFlash;
        private float scrollPhase = 0f;

        private ZappyRenderer(WeaponAPI weapon) {
            super(CombatEngineLayers.ABOVE_SHIPS_LAYER);
            this.weapon = weapon;
            this.sprite = Global.getSettings().getSprite("fx", "tp_drake_beam");
            this.muzzleFlash = Global.getSettings().getSprite("fx", "tp_drake_muzzle_flash");
            this.muzzleFlash.setColor(Color.WHITE);
            this.muzzleFlash.setAdditiveBlend();
        }

        @Override
        public void advance(float amount) {
            CombatEngineAPI engine = Global.getCombatEngine();
            if (engine != null && !engine.isPaused() && amount > 0f) {
                scrollPhase = positiveMod(scrollPhase + SCROLL_SPEED * amount,
                        TILE_LENGTH);
            }
            int frame = (int) ((weapon.getAnimation().getNumFrames() - 1) * (1f - (weapon.getCooldownRemaining() / weapon.getCooldown())));
            weapon.getAnimation().setFrame(frame);

            // The rendered effect occupies the weapon's vicinity plus its range.
            if (entity != null && weapon.getShip() != null) {
                entity.getLocation().set(weapon.getLocation());
            }
        }

        @Override
        public float getRenderRadius() {
            return weapon.getRange() + VISUAL_WIDTH + 200f;
        }

        @Override
        public boolean isExpired() {
            CombatEngineAPI engine = Global.getCombatEngine();
            return engine == null || weapon.getShip() == null
                    || !weapon.getShip().isAlive()
                    || !engine.isEntityInPlay(weapon.getShip());
        }

        @Override
        public void render(CombatEngineLayers layer, ViewportAPI viewport) {
            if (isExpired() || layer != CombatEngineLayers.ABOVE_SHIPS_LAYER) return;

            // Use the sprite's UV bounds (rather than assuming [0..1]), so this
            // also works if the sprite is packed into a texture atlas.
            final float u0 = sprite.getTexX();
            final float u1 = u0 + sprite.getTexWidth();
            final float vBase = sprite.getTexY();
            final float vRange = sprite.getTexHeight();

            // Save OpenGL state; layered rendering shares its context with
            // Starsector's own combat rendering.
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT
                    | GL11.GL_TEXTURE_BIT | GL11.GL_CURRENT_BIT
                    | GL11.GL_LIGHTING_BIT);
            try {
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
                GL11.glShadeModel(GL11.GL_SMOOTH); // interpolate vertex alpha
                for (BeamAPI beam : weapon.getBeams()) {
                    if (beam == null) continue;
                    float alpha = clamp01(beam.getBrightness()
                            * viewport.getAlphaMult());
                    if (alpha <= 0f) continue;

                    Vector2f from = beam.getFrom();
                    Vector2f to = beam.getTo();
                    if (from == null || to == null) continue;

                    final float dx = to.x - from.x;
                    final float dy = to.y - from.y;
                    final float length = (float) Math.sqrt(dx * dx + dy * dy);
                    if (length < 1f) continue;

                    // Unit beam direction and perpendicular vector.
                    final float dirX = dx / length;
                    final float dirY = dy / length;
                    final float halfX = -dirY * VISUAL_WIDTH * 0.5f;
                    final float halfY =  dirX * VISUAL_WIDTH * 0.5f;

                    // The last part of the damaging beam receives an alpha
                    // gradient. Keep the muzzle and short beams fully readable.
                    final float fadeLength = Math.min(END_FADE_LENGTH,
                            length * 0.5f);
                    final float fadeStart = length - fadeLength;

                    // Rebind the beam texture for each beam because rendering
                    // the muzzle flash binds a second sprite texture.
                    sprite.bindTexture();
                    GL11.glBegin(GL11.GL_QUADS);
                    try {
                        float drawn = 0f;
                        while (drawn < length) {
                            // Where the current segment starts inside a tile.
                            float inTile = positiveMod(drawn + scrollPhase,
                                    TILE_LENGTH);
                            float segmentLength = Math.min(TILE_LENGTH - inTile,
                                    length - drawn);

                            // Split a tile exactly where fading begins so a
                            // long tile never smears the fade across a section
                            // that should remain fully opaque.
                            if (fadeLength > 0f && drawn < fadeStart
                                    && drawn + segmentLength > fadeStart) {
                                segmentLength = fadeStart - drawn;
                            }
                            if (segmentLength <= 0.0001f) break;

                            // Only read the needed portion of the PNG; this
                            // *crops* short end tiles instead of squashing them.
                            float v0 = vBase + (inTile / TILE_LENGTH) * vRange;
                            float v1 = vBase + ((inTile + segmentLength)
                                    / TILE_LENGTH) * vRange;

                            float startX = from.x + dirX * drawn;
                            float startY = from.y + dirY * drawn;
                            float endX = startX + dirX * segmentLength;
                            float endY = startY + dirY * segmentLength;

                            // Interpolated alpha fades smoothly to exactly
                            // zero at the collision endpoint. Preserve the
                            // existing brightness/viewport alpha multiplier.
                            float startAlpha = alpha * fadeFactor(
                                    drawn, length, fadeLength);
                            float endAlpha = alpha * fadeFactor(
                                    drawn + segmentLength, length, fadeLength);

                            // Image's vertical axis maps onto the beam axis.
                            GL11.glColor4f(1f, 0.44f, 0.37f, alpha);
                            GL11.glTexCoord2f(u0, v0);
                            GL11.glVertex2f(startX - halfX, startY - halfY);
                            GL11.glTexCoord2f(u1, v0);
                            GL11.glVertex2f(startX + halfX, startY + halfY);
                            GL11.glColor4f(1f, 0.44f, 0.37f, endAlpha);
                            GL11.glTexCoord2f(u1, v1);
                            GL11.glVertex2f(endX + halfX, endY + halfY);
                            GL11.glTexCoord2f(u0, v1);
                            GL11.glVertex2f(endX - halfX, endY - halfY);

                            drawn += segmentLength;
                        }
                    } finally {
                        GL11.glEnd();
                    }

                    // Draw on top of the beam's first tile at its real origin.
                    // No damage or collision logic is changed.
                    renderMuzzleFlash(from, dirX, dirY, alpha);
                }
            } finally {
                GL11.glPopAttrib();
            }
        }

        private void renderMuzzleFlash(Vector2f origin,
                                       float dirX, float dirY, float beamAlpha) {
            float alpha = clamp01(beamAlpha * FLASH_ALPHA_MULT);
            if (alpha <= 0f) return;

            // A 0-degree sprite points up in Starsector. Map the vertical
            // lightning/flame artwork onto the weapon's outgoing beam angle.
            float angle = (float) Math.toDegrees(Math.atan2(dirY, dirX));

            // Image-space bulb is at y=39 (top=0); sprite center is y=26.
            // Offset the sprite forward by 13 units to pin that bulb to
            // beam.getFrom(), rather than pinning the empty bottom edge.
            float forwardOffset = FLASH_BULB_Y_FROM_TOP - FLASH_LENGTH * 0.5f;
            float centerX = origin.x + dirX * forwardOffset;
            float centerY = origin.y + dirY * forwardOffset;

            muzzleFlash.setSize(FLASH_WIDTH, FLASH_LENGTH);
            muzzleFlash.setAngle(angle - 90f);
            muzzleFlash.setAlphaMult(alpha);
            muzzleFlash.renderAtCenter(centerX, centerY);
        }

        private static float fadeFactor(float distance, float length,
                                        float fadeLength) {
            if (fadeLength <= 0f) return 1f;
            return clamp01((length - distance) / fadeLength);
        }

        private static float positiveMod(float x, float period) {
            float value = x % period;
            return value < 0f ? value + period : value;
        }

        private static float clamp01(float x) {
            return Math.max(0f, Math.min(1f, x));
        }
    }
}
