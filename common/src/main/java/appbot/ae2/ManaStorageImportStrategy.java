package appbot.ae2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import appbot.AppliedBotanics;
import appbot.Lookup;
import vazkii.botania.api.mana.ManaReceiver;

import appeng.api.behaviors.StackImportStrategy;
import appeng.api.behaviors.StackTransferContext;
import appeng.api.config.Actionable;

@SuppressWarnings("UnstableApiUsage")
public class ManaStorageImportStrategy implements StackImportStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManaStorageImportStrategy.class);
    private final Lookup<ManaReceiver, Direction> apiCache;
    private final Direction fromSide;

    public ManaStorageImportStrategy(ServerLevel level,
            BlockPos fromPos,
            Direction fromSide) {
        this.apiCache = AppliedBotanics.getInstance().manaReceiver(level, fromPos);
        this.fromSide = fromSide;
    }

    @Override
    public boolean transfer(StackTransferContext context) {
        if (!context.isKeyTypeEnabled(ManaKeyType.TYPE)) {
            return false;
        }

        var receiver = apiCache.find(fromSide);

        if (receiver == null) {
            return false;
        }

        var remainingTransferAmount = context.getOperationsRemaining()
                * (long) ManaKeyType.TYPE.getAmountPerOperation();

        var inv = context.getInternalStorage();

        var amount = (int) Math.min(remainingTransferAmount, receiver.getCurrentMana());

        if (amount <= 0) {
            return false;
        }

        var inserted = (int) inv.getInventory().insert(ManaKey.KEY, amount, Actionable.MODULATE,
                context.getActionSource());

        receiver.receiveMana(-inserted);

        var opsUsed = Math.max(1, inserted / ManaKeyType.TYPE.getAmountPerOperation());
        context.reduceOperationsRemaining(opsUsed);

        return true;
    }
}
