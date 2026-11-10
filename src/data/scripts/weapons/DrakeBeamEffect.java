package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class DrakeBeamEffect implements BeamEffectPlugin
{
    private boolean hasFired=false;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {

        // Don't bother with any checks if the game is paused
        if (engine.isPaused()) {
            return;
        }

        if(beam.getBrightness()==1 & !hasFired) {
            hasFired=true;
            Vector2f start = beam.getFrom();
            Vector2f end = beam.getTo();

            Color beamFringeColor = beam.getFringeColor();
            Color beamCoreColor = beam.getCoreColor();
            //beam.getWeapon().getSpec().

                //play sound (to avoid limitations with the way weapon sounds are handled)
                Global.getSoundPlayer().playSound("tp_drake_fire", 1f, 1f, start, beam.getSource().getVelocity());

                //weapon glow
                engine.addHitParticle(
                        start,
                        new Vector2f(),
                        45,
                        1f,
                        0.2f,
                        beamFringeColor
                        // new Color(50,100,255,255)
                );
                engine.addHitParticle(
                        start,
                        new Vector2f(),
                        25,
                        1f,
                        0.1f,
                        beamCoreColor
                );

            if (beam.didDamageThisFrame()){
                //visual effect
                engine.spawnExplosion(
                    //where
                    end,
                    //speed
                    (Vector2f) new Vector2f(0,0),
                    //color
                    beamFringeColor,
                    //size
                    MathUtils.getRandomNumberInRange(30f,50f),
                    //duration
                    0.2f
                );
            }
        }

        if(beam.getWeapon().getChargeLevel() == 0 & hasFired)
        {
            hasFired=false;
        }
    }
}