package org.cyclops.integrateddynamics.modcompat.mcmultipart;

import mcmultipart.client.multipart.ISmartMultipartModel;
import mcmultipart.client.multipart.MultipartStateMapper;
import mcmultipart.multipart.PartState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.property.IExtendedBlockState;

import java.util.Collections;
import java.util.List;

/**
 * Component for the cable model static rendering.
 * @author rubensworks
 */
public class CableModelPartComponent {

    protected List<PartState> getPartStates(IExtendedBlockState state) {
        List<PartState> list = state.getValue(MultipartBlockComponent.PROPERTY);
        if(list == null) {
            list = Collections.emptyList();
        }
        return list;
    }

    public void addFaceQuads(IExtendedBlockState state, List<BakedQuad> quads, EnumFacing face) {
        for (PartState partState : getPartStates(state)) {
            if (!partState.renderLayers.contains(MinecraftForgeClient.getRenderLayer())) continue;

            ModelResourceLocation modelLocation = new ModelResourceLocation(partState.modelPath,
                    MultipartStateMapper.instance.getPropertyString(partState.state.getProperties()));
            IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager()
                    .getModel(modelLocation);
            if (model != null) {
                model = model instanceof ISmartMultipartModel ? ((ISmartMultipartModel) model).handlePartState(partState.state) : model;
                quads.addAll(model.getFaceQuads(face));
            }
        }
    }

    public void addGeneralQuads(IExtendedBlockState state, List<BakedQuad> quads) {
        for (PartState partState : getPartStates(state)) {
            if (!partState.renderLayers.contains(MinecraftForgeClient.getRenderLayer())) continue;

            ModelResourceLocation modelLocation = new ModelResourceLocation(partState.modelPath,
                    MultipartStateMapper.instance.getPropertyString(partState.state.getProperties()));
            IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager()
                    .getModel(modelLocation);
            if (model != null) {
                model = model instanceof ISmartMultipartModel ? ((ISmartMultipartModel) model).handlePartState(partState.state) : model;
                quads.addAll(model.getGeneralQuads());
            }
        }
    }

}
