package index.alchemy.util;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.tree.AbstractInsnNode; 
import org.objectweb.asm.tree.InsnList; 
import org.objectweb.asm.tree.LabelNode;

public class NodeCopier { 
	
	protected final Map<LabelNode, LabelNode> labelMap = new HashMap<LabelNode, LabelNode>() {
		@Override
		public LabelNode get(Object key) {
			LabelNode result = super.get(key);
			if (result == null)
				put((LabelNode) key, result = new LabelNode());
			return result;
		}
	};
	
	public void copyTo(AbstractInsnNode node, InsnList destination) {
		if (node == null)
			return;
		
		if (destination == null)
			return;
		
		destination.add(node.clone(labelMap));
	}
	
}