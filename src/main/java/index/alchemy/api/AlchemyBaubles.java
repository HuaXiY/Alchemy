package index.alchemy.api;

import java.util.List;

import com.google.common.collect.ImmutableList;

import baubles.api.BaubleType;

public class AlchemyBaubles {
	
	private static final List<BaubleType> baubles = ImmutableList.of(
			BaubleType.AMULET,
			BaubleType.RING,
			BaubleType.RING,
			BaubleType.BELT,
			BaubleType.HEAD,
			BaubleType.BODY,
			BaubleType.CHARM
	);
	
	/** 
	 * See {@link index.alchemy.core.asm.transformer.TransformerNetHandlerPlayServer
	 * 		#transform(String name, String transformedName, byte[] basicClass)} 
	 **/
	public static int asm_offset = getAllBaubles().size();
	
	public static List<BaubleType> getAllBaubles() {
		return baubles;
	}
	
	public static int getBaublesSize() {
		return baubles.size();
	}
	
}
