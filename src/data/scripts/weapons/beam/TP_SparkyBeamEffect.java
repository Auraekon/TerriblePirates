package data.scripts.weapons.beam;

import org.lazywizard.lazylib.MathUtils;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lwjgl.util.vector.Vector2f;

public class TP_SparkyBeamEffect implements BeamEffectPlugin {

    private final IntervalUtil flashInterval = new IntervalUtil(0.16f,0.3f);

    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {

        flashInterval.advance(engine.getElapsedInLastFrame());
        if (flashInterval.intervalElapsed()) {
            var muzzleLoc = beam.getWeapon().getFirePoint(0);
            float size = beam.getWidth() * MathUtils.getRandomNumberInRange(1.9f, 2.2f);
            Vector2f randomArcLocation = new Vector2f(muzzleLoc.x - 15f + Math.round(Math.random() * 30f), muzzleLoc.y - 15f + Math.round(Math.random() * 30f));

            float dur = MathUtils.getRandomNumberInRange(0.15f,0.24f);

            engine.addHitParticle(beam.getFrom(), beam.getSource().getVelocity(), beam.getWidth(), 0.4f, dur, beam.getCoreColor());
            engine.addHitParticle(beam.getFrom(), beam.getSource().getVelocity(), size, 0.4f, dur, beam.getFringeColor().brighter());

            if (beam.didDamageThisFrame()) {
                engine.addHitParticle(beam.getTo(), beam.getSource().getVelocity(), size * 2.5f, 0.4f, dur, beam.getFringeColor());
            }
        }
    }
    }