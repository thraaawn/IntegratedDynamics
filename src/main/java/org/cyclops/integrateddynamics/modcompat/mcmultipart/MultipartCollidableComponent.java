package org.cyclops.integrateddynamics.modcompat.mcmultipart;

import mcmultipart.block.TileMultipart;
import mcmultipart.raytrace.RayTraceUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.core.block.CollidableComponent;
import org.cyclops.integrateddynamics.core.block.ICollidableParent;

import java.util.List;

/**
 * A collidable component dependent on multiparts.
 * @author rubensworks
 */
public class MultipartCollidableComponent<P, B extends Block & ICollidableParent> extends CollidableComponent<P, B> {

    public MultipartCollidableComponent(B block, List<IComponent<P, B>> iComponents) {
        super(block, iComponents);
    }

    @Override
    public MovingObjectPosition collisionRayTrace(World world, BlockPos pos, Vec3 origin, Vec3 direction) {
        MovingObjectPosition mop = super.collisionRayTrace(world, pos, origin, direction);
        if(mop != null) {
            return mop;
        }
        if (world == null || pos == null || origin == null || direction == null) return null;
        TileMultipart tile = getMultipartTile(world, pos);
        if (tile == null) return null;
        RayTraceUtils.RayTraceResultPart result = tile.getPartContainer().collisionRayTrace(origin, direction);
        if (result == null) return null;
        result.setBounds(world, pos);
        return result.hit;
    }

    @Override
    public void addCollisionBoxesToList(World world, BlockPos pos, IBlockState state, AxisAlignedBB axisalignedbb, List list, Entity collidingEntity) {
        super.addCollisionBoxesToList(world, pos, state, axisalignedbb, list, collidingEntity);
        TileMultipart tile = getMultipartTile(world, pos);
        if (tile != null) {
            tile.getPartContainer().addCollisionBoxes(axisalignedbb, list, collidingEntity);
        }
    }

    private TileMultipart getMultipartTile(World world, BlockPos pos) {
        return TileHelpers.getSafeTile(world, pos, TileMultipart.class);
    }

}
