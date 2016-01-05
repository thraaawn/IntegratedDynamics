package org.cyclops.integrateddynamics.modcompat.mcmultipart;

import mcmultipart.MCMultiPartMod;
import mcmultipart.client.multipart.ICustomHighlightPart;
import mcmultipart.client.multipart.IHitEffectsPart;
import mcmultipart.client.multipart.ISmartMultipartModel;
import mcmultipart.client.multipart.MultipartStateMapper;
import mcmultipart.multipart.MultipartRegistry;
import mcmultipart.multipart.PartState;
import mcmultipart.property.PropertyMultipartStates;
import mcmultipart.raytrace.PartMOP;
import mcmultipart.raytrace.RayTraceUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.core.tileentity.TileMultipartTicking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Component to use in blocks that are multiparts.
 * Copied from {@link mcmultipart.block.BlockMultipart} on 31/12/2015, make sure the implementation is consistent.
 * @author rubensworks
 */
public class MultipartBlockComponent {

    @SuppressWarnings("unchecked")
    public static final IUnlistedProperty<List<PartState>> PROPERTY;

    static {
        PROPERTY = new PropertyMultipartStates("multipart_container");
    }

    public MultipartBlockComponent() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    private TileMultipartTicking getMultipartTile(IBlockAccess world, BlockPos pos) {
        return TileHelpers.getSafeTile(world, pos, TileMultipartTicking.class);
    }

    public int getLightValue(IBlockAccess world, BlockPos pos) {
        TileMultipartTicking tile = getMultipartTile(world, pos);
        if (tile == null) return 0;
        return tile.getPartContainer().getLightValue();
    }

    public ItemStack getPickBlock(MovingObjectPosition target, World world, BlockPos pos, EntityPlayer player) {
        if (target instanceof PartMOP) {
            TileMultipartTicking tile = getMultipartTile(world, pos);
            if (tile == null) return null;
            return tile.getPartContainer().getPickBlock(player, (PartMOP) target);
        }
        return null;
    }

    TileMultipartTicking brokenTile = null;

    public void onPreBlockDestroyed(World worldIn, BlockPos pos) {
        this.brokenTile = getMultipartTile(worldIn, pos);
    }

    public List<ItemStack> getDrops(IBlockAccess worldIn, BlockPos pos) {
        TileMultipartTicking tile = brokenTile;
        if(tile != null) {
            List<ItemStack> drops = tile.getPartContainer().getDrops();
            this.brokenTile = null;
            return drops;
        }
        return Collections.emptyList();
    }

    public boolean removedByPlayer(World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        return getMultipartTile(world, pos).getPartContainer().harvest(player, reTrace(world, pos, player));
    }

    public float getPlayerRelativeBlockHardness(EntityPlayer player, World world, BlockPos pos) {
        TileMultipartTicking tile = getMultipartTile(world, pos);
        if (tile == null) return 0;
        PartMOP hit = reTrace(world, pos, player);
        if (hit == null) return 0;
        return tile.getPartContainer().getHardness(player, hit);
    }

    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX,
                                    float hitY, float hitZ) {
        TileMultipartTicking tile = getMultipartTile(world, pos);
        if (tile == null) return false;
        return tile.getPartContainer().onActivated(player, player.getCurrentEquippedItem(), reTrace(world, pos, player));
    }

    public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
        TileMultipartTicking tile = getMultipartTile(world, pos);
        if (tile == null) return;
        tile.getPartContainer().onClicked(player, player.getCurrentEquippedItem(), reTrace(world, pos, player));
    }

    public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock) {
        TileMultipartTicking tile = getMultipartTile(world, pos);
        if (tile == null) return;
        tile.getPartContainer().onNeighborBlockChange(neighborBlock);
    }

    public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
        TileMultipartTicking tile = getMultipartTile(world, pos);
        if (tile == null) return;
        tile.getPartContainer().onNeighborTileChange(
                EnumFacing.getFacingFromVector(neighbor.getX() - pos.getX(), neighbor.getY() - pos.getY(), neighbor.getZ() - pos.getZ()));
    }

    public boolean canConnectRedstone(IBlockAccess world, BlockPos pos, EnumFacing side) {
        if (side == null) return false;
        TileMultipartTicking tile = getMultipartTile(world, pos);
        if (tile == null) return false;
        return tile.getPartContainer().canConnectRedstone(side.getOpposite());
    }

    public int getWeakPower(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side) {
        if (side == null) return 0;
        TileMultipartTicking tile = getMultipartTile(world, pos);
        if (tile == null) return 0;
        return tile.getPartContainer().getWeakSignal(side.getOpposite());
    }

    public int getStrongPower(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side) {
        if (side == null) return 0;
        TileMultipartTicking tile = getMultipartTile(world, pos);
        if (tile == null) return 0;
        return tile.getPartContainer().getStrongSignal(side.getOpposite());
    }

    public boolean isSideSolid(IBlockAccess world, BlockPos pos, EnumFacing side) {
        TileMultipartTicking tile = getMultipartTile(world, pos);
        if (tile == null) return false;
        return tile.getPartContainer().isSideSolid(side);
    }

    public boolean canPlaceTorchOnTop(IBlockAccess world, BlockPos pos) {
        TileMultipartTicking tile = getMultipartTile(world, pos);
        if (tile == null) return false;
        return tile.getPartContainer().canPlaceTorchOnTop();
    }

    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World world, BlockPos pos, IBlockState state, Random rand) {
        TileMultipartTicking tile = getMultipartTile(world, pos);
        if (tile != null) tile.getPartContainer().randomDisplayTick(rand);
    }

    @SideOnly(Side.CLIENT)
    public boolean addDestroyEffects(World world, BlockPos pos, EffectRenderer effectRenderer) {
        PartMOP hit = reTrace(world, pos, MCMultiPartMod.proxy.getPlayer());
        if (hit != null) {
            if (hit.partHit instanceof IHitEffectsPart)
                if (((IHitEffectsPart) hit.partHit).addDestroyEffects(IHitEffectsPart.AdvancedEffectRenderer.getInstance(effectRenderer))) return true;

            String path = hit.partHit.getModelPath();
            IBlockState state = hit.partHit.getExtendedState(MultipartRegistry.getDefaultState(hit.partHit).getBaseState());
            IBakedModel model = path == null ? null : Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes()
                    .getModelManager()
                    .getModel(new ModelResourceLocation(path, MultipartStateMapper.instance.getPropertyString(state.getProperties())));
            if (model != null) {
                model = model instanceof ISmartMultipartModel ? ((ISmartMultipartModel) model).handlePartState(hit.partHit
                        .getExtendedState(MultipartRegistry.getDefaultState(hit.partHit).getBaseState())) : model;
                if (model != null) {
                    TextureAtlasSprite icon = model.getParticleTexture();
                    if (icon != null) IHitEffectsPart.AdvancedEffectRenderer.getInstance(effectRenderer).addBlockDestroyEffects(pos, icon);
                }
            }
        }
        return true;
    }

    @SideOnly(Side.CLIENT)
    public boolean addHitEffects(World world, MovingObjectPosition target, EffectRenderer effectRenderer) {

        PartMOP hit = target instanceof PartMOP ? (PartMOP) target : null;
        if (hit != null) {
            if (hit.partHit instanceof IHitEffectsPart)
                if (((IHitEffectsPart) hit.partHit).addHitEffects(hit, IHitEffectsPart.AdvancedEffectRenderer.getInstance(effectRenderer))) return true;

            String path = hit.partHit.getModelPath();
            IBlockState state = hit.partHit.getExtendedState(MultipartRegistry.getDefaultState(hit.partHit).getBaseState());
            IBakedModel model = path == null ? null : Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes()
                    .getModelManager()
                    .getModel(new ModelResourceLocation(path, MultipartStateMapper.instance.getPropertyString(state.getProperties())));
            if (model != null) {
                model = model instanceof ISmartMultipartModel ? ((ISmartMultipartModel) model).handlePartState(hit.partHit
                        .getExtendedState(MultipartRegistry.getDefaultState(hit.partHit).getBaseState())) : model;
                if (model != null) {
                    TextureAtlasSprite icon = model.getParticleTexture();
                    if (icon != null)
                        IHitEffectsPart.AdvancedEffectRenderer.getInstance(effectRenderer).addBlockHitEffects(
                                target.getBlockPos(),
                                hit,
                                world.getBlockState(target.getBlockPos()).getBlock().getSelectedBoundingBox(world, target.getBlockPos())
                                        .offset(-target.getBlockPos().getX(), -target.getBlockPos().getY(), -target.getBlockPos().getZ()),
                                icon);
                }
            }
        }
        return true;
    }

    @SideOnly(Side.CLIENT)
    public boolean addLandingEffects(WorldServer worldObj, BlockPos blockPosition, IBlockState iblockstate, EntityLivingBase entity,
                                     int numberOfParticles) {
        return true;
    }

    private PartMOP reTrace(World world, BlockPos pos, EntityPlayer player) {
        Vec3 start = RayTraceUtils.getStart(player);
        Vec3 end = RayTraceUtils.getEnd(player);
        RayTraceUtils.RayTraceResultPart result = getMultipartTile(world, pos).getPartContainer().collisionRayTrace(start, end);
        return result == null ? null : result.hit;
    }

    public IExtendedBlockState applyExtendedState(IExtendedBlockState state, IBlockAccess world, BlockPos pos) {
        TileMultipartTicking tile = getMultipartTile(world, pos);
        return state.withProperty(PROPERTY,
                tile != null ? tile.getPartContainer().getExtendedStates(world, pos) : new ArrayList<PartState>());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    @SideOnly(Side.CLIENT)
    public final void onDrawBlockHighlight(DrawBlockHighlightEvent event) {
        PartMOP hit = event.target instanceof PartMOP ? (PartMOP) event.target : null;
        if (hit != null && hit.partHit instanceof ICustomHighlightPart) {
            GlStateManager.pushMatrix();

            BlockPos pos = hit.getBlockPos();
            EntityPlayer player = event.player;
            float partialTicks = event.partialTicks;
            double x = pos.getX() - (player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks);
            double y = pos.getY() - (player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks);
            double z = pos.getZ() - (player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks);
            GlStateManager.translate(x, y, z);

            if (((ICustomHighlightPart) hit.partHit).drawHighlight(hit, event.player, event.currentItem, event.partialTicks))
                event.setCanceled(true);

            GlStateManager.popMatrix();
        }
    }

}
