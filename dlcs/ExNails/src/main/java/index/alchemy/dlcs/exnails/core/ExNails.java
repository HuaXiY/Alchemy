package index.alchemy.dlcs.exnails.core;

import index.alchemy.api.annotation.DLC;
import index.alchemy.api.annotation.Premise;
import index.project.version.annotation.Alpha;

import static index.alchemy.dlcs.exnails.core.ExNails.*;

@Alpha
@Premise("ToughAsNails")
@DLC(id = DLC_ID, name = DLC_NAME, version = DLC_VERSION, mcVersion = "[1.10.2]")
public class ExNails {
	
	public static final String
			DLC_ID = "exnails",
			DLC_NAME = "ExNails",
			DLC_VERSION = "0.0.1-dev";
	
}
