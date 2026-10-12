package data.campaign.terriblepiratesrulecmd;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.lwjgl.input.Keyboard;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.econ.RecentUnrest;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * Starsector 0.98a. Adds a credit-paying alternative to a standard valuables raid.
 * The original MarketCMD handles selection, casualties, consequences, XP and listeners.
 */
public class PirateRaidCMD extends MarketCMD {

    private static final String PIRATE_FACTION_ID = "pirates";
    private static final String MODE_KEY = "$pirate_raid_mod_active";
    private static final String OPTION_ID = "pirateRaidInitiate";
    private static final float MODE_EXPIRATION_DAYS = 1f;
    private static final long MAX_PAYOUT_CREDITS = 2000000000L;

    // This value is captured BEFORE the raid itself changes faction relationships.
    private float reputationAtLaunch = Float.NaN;

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog,
                           List<Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null || params == null || params.isEmpty()) return false;
        String command = params.get(0).getString(memoryMap);

        if ("pirateValuable".equals(command)) {
            MarketAPI target = dialog.getInteractionTarget().getMarket();
            if (target == null) return false;
            if (pirateRep() <= 0f) {
                dialog.getTextPanel().addPara(
                        "A positive relationship with the Pirates is required to initiate a pirate raid.");
                return true;
            }
            // The base command stores its temporary raid data on the market. Store the
            // pirate/non-pirate mode alongside it so subsequent rules can read it.
            target.getMemoryWithoutUpdate().set(MODE_KEY, true, MODE_EXPIRATION_DAYS);
            return super.execute(ruleId, dialog, Misc.tokenize("raidValuable"), memoryMap);
        }

        // Switching to either vanilla objective mode cancels pirate conversion.
        if ("normalValuable".equals(command) || "normalDisrupt".equals(command)) {
            MarketAPI target = dialog.getInteractionTarget().getMarket();
            if (target != null) target.getMemoryWithoutUpdate().unset(MODE_KEY);
            String vanilla = "normalValuable".equals(command) ? "raidValuable" : "raidDisrupt";
            return super.execute(ruleId, dialog, Misc.tokenize(vanilla), memoryMap);
        }

        return super.execute(ruleId, dialog, params, memoryMap);
    }

    private static float pirateRep() {
        if (Global.getSector() == null) return 0f;
        return Global.getSector().getPlayerFaction().getRelationship(PIRATE_FACTION_ID);
    }

    private boolean inPirateMode() {
        return market != null && market.getMemoryWithoutUpdate().getBoolean(MODE_KEY);
    }

    private void clearPirateMode() {
        if (market != null) market.getMemoryWithoutUpdate().unset(MODE_KEY);
    }

    @Override
    protected void raidMenu() {
        clearPirateMode();
        super.raidMenu(); // Vanilla strength calculation, projected losses, choices, etc.

        if (market == null || temp.nonMarket) return;

        // Put our new option before "Go back", not after it.
        options.removeOption(RAID_GO_BACK);
        options.addOption("Initiate a pirate raid", OPTION_ID);
        options.setTooltip(OPTION_ID,
                "Choose objectives same as with a normal valuables raid, but receive " +
                "credits instead of loot. Any valuables recovered will be fenced at " + "twice the calculated market value of the items obtained." + "Your cut is based on your reputation with the pirates.");

        if (pirateRep() <= 0f) {
            options.setEnabled(OPTION_ID, false);
            options.setTooltip(OPTION_ID,
                    "Unavailable: your relationship with the Pirates must be positive.");
        } else if (!temp.canRaid || getRaidCooldown() > 0f) {
            options.setEnabled(OPTION_ID, false);
            options.setTooltip(OPTION_ID, "You cannot launch a raid at this time.");
        } else if (temp.raidMult < VALUABLES_THRESHOLD) {
            options.setEnabled(OPTION_ID, false);
            options.setTooltip(OPTION_ID,
                    "Your forces are not sufficient to conduct an effective valuables raid.");
        }

        options.addOption("Go back", RAID_GO_BACK);
        options.setShortcut(RAID_GO_BACK, Keyboard.KEY_ESCAPE,
                false, false, false, true);
    }

    @Override
    protected void addConfirmOptions() {
        super.addConfirmOptions(); // Preserve vanilla confirmation and story-point secrecy.
        if (!inPirateMode()) return;

        options.setOptionText("Launch pirate raid", RAID_CONFIRM);
        text.addPara("Any valuables recovered will be fenced at " +
                "twice the calculated market value of the items obtained." +
                "Your cut is based on your reputation with the pirates.");
        if (pirateRep() <= 0f) {
            options.setEnabled(RAID_CONFIRM, false);
            if (options.hasOption(RAID_CONFIRM_STORY)) {
                options.setEnabled(RAID_CONFIRM_STORY, false);
            }
        }
    }

    @Override
    protected void raidConfirm(boolean secret) {
        if (inPirateMode()) {
            reputationAtLaunch = pirateRep();
            if (reputationAtLaunch <= 0f) {
                text.addPara("The Pirates no longer have a positive opinion of you. " +
                        "The pirate raid has been cancelled.");
                addConfirmOptions();
                return;
            }
        }
        try {
            // Vanilla implements the raid, applies unrest before calling performRaid,
            // updates defense preparedness/cooldowns, and fires post-raid listeners.
            super.raidConfirm(secret);
        } finally {
            reputationAtLaunch = Float.NaN;
        }
    }

    @Override
    protected CargoAPI performRaid(Random random, float raidEffectiveness) {
        // Run every vanilla objective plugin, including any custom mod objectives.
        CargoAPI vanillaLoot = super.performRaid(random, raidEffectiveness);
        if (!inPirateMode()) return vanillaLoot;

        double lootValue = vanillaLoot.getCredits().get();
        for (CargoStackAPI stack : vanillaLoot.getStacksCopy()) {
            if (!stack.isNull() && stack.getSize() > 0f) {
                lootValue += (double) stack.getBaseValuePerUnit() * stack.getSize();
            }
        }
        int divisor = Math.max(1, RecentUnrest.getPenalty(market));
        float rep = Float.isNaN(reputationAtLaunch) ? pirateRep() : reputationAtLaunch;
        long payout = calculatePayout(rep, market.getSize(), lootValue, divisor);

        // This replaces the entire item cargo. MarketCMD.raidConfirmContinue()
        // deposits its credits directly into the player's account in vanilla.
        CargoAPI creditOnly = Global.getFactory().createCargo(true);
        if (payout > 0L) creditOnly.getCredits().add(payout);
        double fencedValue = lootValue * 2;
        text.addPara("Recovered valuables fenced for " + Misc.getDGSCredits((float) fencedValue) +
                "; your cut: " + Misc.getDGSCredits(payout));

        return creditOnly;
    }

    /** Raw faction relationship [0,1]; zero unrest means a divisor of one. */
    public static long calculatePayout(float reputation, int size,
                                       double lootValue, int divisor) {
        if (reputation <= 0f || size <= 0 || lootValue <= 0d ||
                Double.isNaN(lootValue)) return 0L;
        // double result = (reputation * (double) size * lootValue) / divisor;
        double result = (reputation * lootValue) * 2;
        if (Double.isNaN(result) || result <= 0d) return 0L;
        if (Double.isInfinite(result) || result >= MAX_PAYOUT_CREDITS) {
            return MAX_PAYOUT_CREDITS;
        }
        return (long) Math.floor(result);
    }

    @Override
    public void raidConfirmContinue() {
        boolean pirate = inPirateMode();
        super.raidConfirmContinue(); // Pays the credits; reports objectives/listeners.
        if (pirate) {
            // Eliminate a second loot-collection step. The player has already been
            // paid by vanilla raidConfirmContinue(), and there are no items to claim.
            if (temp.raidLoot != null) {
                temp.raidLoot.clear();
                temp.raidLoot.getCredits().set(0f);
            }
            temp.contText = null;
            if (options.hasOption(RAID_RESULT)) {
                options.setOptionText("Continue", RAID_RESULT);
            }
        }
    }

    @Override
    protected void raidNeverMind() {
        clearPirateMode();
        super.raidNeverMind();
    }

    @Override
    protected void raidResult() {
        // Also covers a continuation that was intercepted by an external mod.
        if (inPirateMode() && temp.raidLoot != null) {
            temp.raidLoot.clear();
            temp.raidLoot.getCredits().set(0f);
        }
        clearPirateMode();
        super.raidResult();
    }
}
