package appbot.ae2;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import appbot.AppliedBotanics;
import appbot.Lookup;
import vazkii.botania.api.mana.ManaReceiver;

import appeng.api.behaviors.StackExportStrategy;
import appeng.api.behaviors.StackTransferContext;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.stacks.AEKey;

@SuppressWarnings("UnstableApiUsage")
public class ManaStorageExportStrategy implements StackExportStrategy {

    private final Lookup<ManaReceiver, Direction> apiCache;
    private final Direction fromSide;

    public ManaStorageExportStrategy(ServerLevel level,
            BlockPos fromPos,
            Direction fromSide) {
        this.apiCache = AppliedBotanics.getInstance().manaReceiver(level, fromPos);
        this.fromSide = fromSide;
    }

    @Override
    public long transfer(StackTransferContext context, AEKey what, long amount) {
        if (!(what instanceof ManaKey)) {
            return 0;
        }

        var receiver = apiCache.find(fromSide);

        if (receiver == null) {
            return 0;
        }

        if(receiver.isFull()) {
            return 0;
        }

        //var insertable = (int) amount;
        /*var testExtracted = (int) StorageHelper.poweredExtraction(context.getEnergySource(),
                context.getInternalStorage().getInventory(), ManaKey.KEY, insertable, context.getActionSource(),
                Actionable.SIMULATE);*/
                
        //Extracted the above function and modified it below

        var energy = context.getEnergySource();
        var inv = context.getInternalStorage().getInventory();
        var request = ManaKey.KEY;
        var src = context.getActionSource();
        
        // Get actually available system mana
        var retrieved = inv.extract(request, amount, Actionable.SIMULATE, src);

        // Save previous endpoint mana
        var prevMana = receiver.getCurrentMana();

        // Put available mana in endpoint
        receiver.receiveMana((int) retrieved);

        // Compare endpoint's old mana and new mana to get the desired system mana
        var desiredMana = Math.abs(receiver.getCurrentMana() - prevMana);

        // This is to prevent ManaReceivers that have a constant capacity from
        // either duping (mana splitter) or causing other unintended issues with mana
        if(desiredMana == 0) {
            desiredMana = (int) retrieved;
        }

        // Below is mostly copy pasted from the previously mentioned function up above.
        
        var energyFactor = Math.max(1.0, request.getAmountPerOperation());
        var availablePower = energy.extractAEPower(desiredMana / energyFactor, Actionable.SIMULATE,
                PowerMultiplier.CONFIG);
        var itemToExtract = Math.min((long) (availablePower * energyFactor + 0.9), desiredMana);

        if (itemToExtract <= 0) {
            return 0;
        }

        energy.extractAEPower(desiredMana / energyFactor, Actionable.MODULATE, PowerMultiplier.CONFIG);
        var ret = inv.extract(request, itemToExtract, Actionable.MODULATE, src);

        return ret;
    }

    @Override
    public long push(AEKey what, long amount, Actionable mode) {
        if (!(what instanceof ManaKey)) {
            return 0;
        }

        var receiver = apiCache.find(fromSide);

        if (receiver == null) {
            return 0;
        }

        if (receiver.isFull()) {
            return 0;
        }

        var prevMana = receiver.getCurrentMana();

        receiver.receiveMana((int) amount);

        /*var inserted = (int) Math.min(amount,
                ManaHelper.getCapacity(receiver) - receiver.getCurrentMana());*/

        var inserted = Math.abs(receiver.getCurrentMana() - prevMana);

        // This is to prevent ManaReceivers that have a constant capacity from
        // either duping (mana splitter) or causing other unintended issues with mana
        if(inserted == 0) {
            inserted = (int) inserted;
        }

        // This COULD be an issue if a ManaReceiver isn't able to reverse
        // the process of putting in mana. Perhaps a config to disable mana
        // input/output for specific blocks
        if (mode == Actionable.SIMULATE) {
            receiver.receiveMana(-inserted);
        }

        return inserted;
    }
}
