package index.alchemy.core.asm.transformer;

import com.google.common.collect.Sets;

import index.alchemy.api.IAlchemyClassTransformer;
import index.alchemy.core.debug.JFXDialog;
import net.minecraftforge.fml.common.MissingModsException;
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion;

public class TransformerBiomesOPlenty implements IAlchemyClassTransformer {

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (transformedName.startsWith("biomesoplenty.") && basicClass == null)
			onMiss();
		return basicClass;
	}
	
	private static void onMiss() {
		MissingModsException e = new MissingModsException(Sets.newHashSet(new DefaultArtifactVersion("5.0.0")),
				"BiomesOPlenty", "Biomes O' Plenty");
		RuntimeException ex = new RuntimeException("Could not find a premise mod: Biomes O' Plenty", e);
		JFXDialog.showThrowableAndWait(ex);
		throw ex;
	}

	@Override
	public String getTransformerClassName() { return null; }

}
