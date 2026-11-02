package data.scripts.weapons.proj;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

public class HeavyEMPArcEffect implements OnHitEffectPlugin {
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
                      Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (!shieldHit && target instanceof ShipAPI) {

            float mainEmp = projectile.getEmpAmount();
            float mainDam = projectile.getDamageAmount();

            float emp = projectile.getEmpAmount()/10;
            float dam = projectile.getDamageAmount()/10;


            engine.spawnEmpArc(projectile.getSource(), point, target, target,
                    DamageType.ENERGY,
                    mainEmp,
                    mainDam, // emp
                    100000f, // max range
                    "tachyon_lance_emp_impact",
                    40f, // thickness
                    new Color(25,100,155,255),
                    new Color(255,255,255,255)
            );

            for (int i = 0; i < 7; i++) {
                engine.spawnEmpArc(projectile.getSource(), point, target, target,
                        DamageType.ENERGY,
                        dam,
                        emp, // emp
                        100000f, // max range
                        "tachyon_lance_emp_impact",
                        20f, // thickness
                        new Color(25,100,155,255),
                        new Color(255,255,255,255)
                );
            }
            //engine.spawnProjectile(null, null, "plasma", point, 0, new Vector2f(0, 0));
        }
    }
}
