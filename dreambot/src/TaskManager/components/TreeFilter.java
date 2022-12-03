package TaskManager.components;

import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

@SuppressWarnings("serial")
public class TreeFilter extends JTree {
	
	public TreeFilter(DefaultTreeModel model) {
		super(model);
	}
	
	public void expandAllNodes() {
	    for(int i = 0; i < getRowCount(); i++){
	        expandRow(i);
	    }
	}
	
	public void collapseAllNodes() {
	    for(int i = 1; i < getRowCount(); i++){
	    	collapseRow(i);
	    }
	}
	
	public void restoreExpandedState(Node base, List<TreePath> exps, JTree tree) {
		if (base == null) {
			throw new NullPointerException();
		}
		if (wasExpanded(base, exps)) {
			tree.expandPath(new TreePath(base.getPath()));
		}
		int c = base.getChildCount();
		for (int i = 0; i < c; ++i) {
			Node n = (Node)base.getChildAt(i);
			restoreExpandedState(n, exps, tree);
		}
	}
	
	public boolean wasExpanded(Node n, List<TreePath> en) {
		if (n == null) {
			throw new NullPointerException();
		}
		for (TreePath path : en) {
			for (Object o : path.getPath()) {
				if (((Node)o).getUserObject() == n.getUserObject()) {
					return true;
				}
			}
		}
		return false;
	}
	
	public Node node(String name, Object ... children) {
		Node n = new Node(name);
		for (Object o : children) {
			if (o instanceof Node) {
				n.add((Node)o);
			} else {
				n.add(new Node(o));
			}
		}
		return n;
	}
	
	public Node createFilteredTree(Node parent, String filter) {
		int c = parent.getChildCount();
		Node fparent = new Node(parent.getUserObject());
		boolean matches = (parent.getUserObject().toString().toLowerCase()).contains(filter.toLowerCase());
		for (int i = 0; i < c; ++i) {
			Node n = (Node)parent.getChildAt(i);
			Node f = createFilteredTree(n, filter);
			if (f != null) {
				fparent.add(f);
				matches = true;
			}
		}
		return matches ? fparent : null;
	}
}
