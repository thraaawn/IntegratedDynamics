package org.cyclops.integrateddynamics.modcompat.mcmultipart;

import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.MultipartContainer;
import mcmultipart.util.IWorldLocation;
import net.minecraft.util.ITickable;
import org.cyclops.integrateddynamics.core.tileentity.TileMultipartTicking;

/**
 * An extended container for multiparts.
 * @author rubensworks
 */
public class ExtendedMultipartContainer extends MultipartContainer {

    private final TileMultipartTicking tile;

    public ExtendedMultipartContainer(IWorldLocation worldLocation, TileMultipartTicking tile, boolean canTurnIntoBlock) {
        super(worldLocation, canTurnIntoBlock);
        this.tile = tile;
    }

    @Override
    public void notifyPartChanged(IMultipart part) {
        super.notifyPartChanged(part);
        tile.sendUpdate();
    }

    @Override
    public boolean canAddPart(IMultipart part) {
        // TODO: check if not overlapping with existing parts and center.
        return super.canAddPart(part);
    }

    public void updateParts() {
        for (IMultipart part : getParts()) {
            if (part instanceof ITickable) {
                ((ITickable) part).update();
            }
        }
    }
}
