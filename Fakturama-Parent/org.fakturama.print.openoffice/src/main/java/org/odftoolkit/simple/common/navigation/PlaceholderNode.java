/**
 *
 */
package org.odftoolkit.simple.common.navigation;

import java.net.URI;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.commons.lang3.StringUtils;
import org.odftoolkit.odfdom.dom.element.text.TextLineBreakElement;
import org.odftoolkit.odfdom.dom.element.text.TextPElement;
import org.odftoolkit.odfdom.dom.element.text.TextParagraphElementBase;
import org.odftoolkit.odfdom.dom.element.text.TextPlaceholderElement;
import org.odftoolkit.odfdom.dom.element.text.TextSpanElement;
import org.odftoolkit.odfdom.pkg.OdfElement;
import org.odftoolkit.odfdom.pkg.OdfFileDom;
import org.odftoolkit.odfdom.type.Length;
import org.odftoolkit.odfdom.type.Length.Unit;
import org.odftoolkit.simple.common.TextExtractor;
import org.odftoolkit.simple.draw.Image;
import org.odftoolkit.simple.text.Span;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Container for a placeholder. With some additional information.
 *
 *
 */
public class PlaceholderNode extends Selection {

	public enum PlaceholderNodeType {
		TABLE_NODE, NORMAL_NODE, IMAGE_NODE
	}

	public enum PlaceholderTableType {
		ITEMS_TABLE("ITEM."), 
		VATLIST_TABLE("VATLIST."), 
		SALESEQUALIZATIONTAX_TABLE("SALESEQUALIZATIONTAX."), 
		DISCOUNT_TABLE("ITEMS.DISCOUNT."), 
		DEPOSIT_TABLE("DOCUMENT.DEPOSIT."), 
		NO_TABLE("");
		
		private String key;
		private PlaceholderTableType(String key) {
		    this.key = key;
		}
		
        /**
         * @return the key
         */
        public final String getKey() {
            return key;
        }
		
	}

	private Node node;

	/**
	 * flag which indicates that this node is inside a table. default is
	 * NORMAL_NODE.
	 */
	private PlaceholderNodeType nodeType = PlaceholderNodeType.NORMAL_NODE;

	/**
	 * If this placeholder is inside a table this field contains the info about
	 * which table type it is.
	 */
	private PlaceholderTableType tableType = PlaceholderTableType.NO_TABLE;

	/**
	 * flag which indicates that this node is inside the styles section
	 */
	private boolean styleNode;

	/**
	 * Constructor for a new PlaceholderNode.
	 *
	 * @param node
	 *            the node to start
	 */
	public PlaceholderNode(Node node) {
		this(node, PlaceholderNodeType.NORMAL_NODE, null, false);
	}

	/**
	 * Constructor for a new PlaceholderNode.
	 *
	 * @param node
	 * @param styleNode
	 *            if this node is a node inside style section or in header/footer section
	 */
	public PlaceholderNode(Node node, boolean styleNode) {
		this(node, PlaceholderNodeType.NORMAL_NODE, null, styleNode);
	}

	/**
	 * Create a {@link PlaceholderNode} with the given type.
	 *
	 * @param node
	 * @param nodeType
	 */
	public PlaceholderNode(Node node, PlaceholderNodeType nodeType) {
		this(node, nodeType, null, false);
	}

	/**
	 * @param node
	 * @param nodeType
	 * @param tableType
	 * @param styleNode
	 */
	public PlaceholderNode(Node node, PlaceholderNodeType nodeType, PlaceholderTableType tableType, boolean styleNode) {
		this.node = node;
		this.nodeType = nodeType;
		// determine table type, if any
		if (tableType == null && nodeType == PlaceholderNodeType.TABLE_NODE && node != null
		        && node.getNodeType() == Node.ELEMENT_NODE) {
			String content = node.getTextContent();
			if (StringUtils.defaultString(content).startsWith("<ITEM.")) {
				this.tableType = PlaceholderTableType.ITEMS_TABLE;
			}
		} else if(tableType != null){
			this.tableType = tableType;
		}
		this.styleNode = styleNode;
	}

	/**
	 * Replace the text content of this placeholder with a new string. If the string is a multiline string (with 
	 * one or more line breaks), the string is split into multiple text nodes. This is, because a simple line-break element
	 * doesn't create a hard line break (e.g., if you format the paragraph with "justify").  
	 *
	 * @param newText
	 *            the replace text String
	 * @return the first replaced Node
	 */
    public Node replaceWith(String newText) {    	
        Node parentNode = getNode().getParentNode();
        
        // the simple case: if a placeholder sits in a node with only one child
        // AND the new text has no line breaks then we can simply replace this 
        // placeholder node with some text.
        if(newText != null && !newText.contains("\r")) {
        	Span s = Span.getInstanceof(new TextSpanElement((OdfFileDom) parentNode.getOwnerDocument()));
        	s.setTextContent(newText);
        	parentNode.replaceChild(s.getOdfElement(), getNode());
        	return s.getOdfElement();
        }
        
        // else, we have to tokenize the new text,
        // clone the original node, put all the children _after_ the placeholder
        // again in the clone and append a new text node for each new line on the cloned node.
        // But wait - all reminding nodes _before_ the origin placeholder have to be retained, too.
        // We do this in reversed order for simplicity.
        // Sounds complicated, eh? But - yes, it is ;-).
        
        // a list with all created nodes
        // since we can't _append_ one node at another node we have to collect all the 
        // nodes and insert it after all nodes were created.  
        LinkedList<Node> substitutes = new LinkedList<>();
        
        // create a copy of all children, if any
        NodeList childNodes = parentNode.getChildNodes();
        Node childNodesAfterPlaceholder = parentNode.cloneNode(false);
        Node previousNode = null;
        if(childNodes.getLength() > 1) {
        	int currentPosition = childNodes.getLength()-1;
        	
        	// collect all nodes _after_ placeholder node, start at the end of tree
    		// put all nodes into a clone of the origin parent node
        	// giving them a new parent (this removes the nodes from origin parent)
        	for (int i = currentPosition; i >= 0; i--) {
        		if(childNodes.item(i).isSameNode(getNode())) {
        			break;
        		}
        		if(previousNode == null) {
        			previousNode = childNodesAfterPlaceholder.appendChild(childNodes.item(i)); 
        		} else {
        			previousNode = childNodesAfterPlaceholder.insertBefore(childNodes.item(i), previousNode);
        		}
			}
        }
        

        String[] st = StringUtils.splitByWholeSeparatorPreserveAllTokens(StringUtils.defaultString(newText), "\r");
        
        // if the first line is empty, we have a flag for this case
        boolean firstSkip = false;
        for (String s : st) {
        	if(s.isEmpty() && !firstSkip) {
        		continue;
        	}
        	firstSkip = true;
            // create a "template node" by cloning the original node
            Node templateNode = parentNode.cloneNode(false);
            templateNode.setTextContent(s);
            substitutes.add(templateNode);
        }
        
        // now lets assemble the nodes altogether.
        /*
         * BEFORE:
         * 
         *     O     parentNode
         *    /|\ 
         *  (O O O)
         *       +-- childNodesAfterPlaceholder
         *     +---- placeholder node
         *  +------- (unnamed children from parentNode)
         *  
         * AFTER:
         * 
         *     O     parentNode (cloned)
         *    /|\ 
         * (O  | O)
         *     | +-- childNodesAfterPlaceholder
         *     O
         *    /|\
         *   O O O 
         *     +---- substituted placeholder node
         *  +------- (unnamed children from parentNode)
         */
        
        Node insertedNode = null;
        
        // at first insert the (old) childNodes which were _after_ the placeholder node (if not empty)
        if(childNodesAfterPlaceholder.hasChildNodes()) {
       		insertedNode = parentNode.getParentNode().insertBefore(childNodesAfterPlaceholder, parentNode.getNextSibling());
        }
        // ...put the collected nodes into parent container (before the origin parent node) ==> reversed order!
        Iterator<Node> it = substitutes.descendingIterator();
        while(it.hasNext()) {
        	Node node = (Node)it.next();
        	Node lineBreak;
	        if(insertedNode == null) {
				lineBreak = parentNode.getParentNode().insertBefore(new TextLineBreakElement((OdfFileDom) parentNode.getOwnerDocument()), parentNode.getNextSibling());
	        } else {
	        	lineBreak = parentNode.getParentNode().insertBefore(new TextLineBreakElement((OdfFileDom) parentNode.getOwnerDocument()), insertedNode);
	        }
	        insertedNode = parentNode.getParentNode().insertBefore(node, lineBreak);
        }
        // ...then remove the origin "placeholder" node...
        parentNode.removeChild(getNode());
        if(!parentNode.hasChildNodes()) {
        	// delete (old) parent node if it's now empty
        	parentNode.getParentNode().removeChild(parentNode);
        }
        return substitutes.isEmpty() ? null : substitutes.getFirst();
    }
    
	public Node replaceWith(URI uri, Integer width, Integer height) {
	    // find paragraph
		TextParagraphElementBase paragraphElement = (TextParagraphElementBase) findParentNode(
		        TextPElement.ELEMENT_NAME.getQName(), getNode());
		// get selection
		ImageSelection sel = new ImageSelection(getTextSelection(paragraphElement));
		// replace image from URI
        Image img = sel.replaceWithImage(uri);
        
        // if scaling is needed
        if(width != null) {
        	img.getFrame().getDrawFrameElement().setSvgWidthAttribute(Length.mapToUnit(String.valueOf(width) + "px", Unit.CENTIMETER));
        }
        
        if(height != null) {
        	img.getFrame().getDrawFrameElement().setSvgHeightAttribute(Length.mapToUnit(String.valueOf(height) + "px", Unit.CENTIMETER));
        }
        
        /* cleanup:
         * The image was inserted inside the Placeholder tags. Therefore it wouldn't be visible if you
         * open the document. Thus, we change the parent of the Frame element so that it hangs right
         * before the placeholder tags. After this we have to delete the (now empty) placeholder tag
         * because else some brackets would be left.
        */
        Node parentNode = getNode().getParentNode();
        parentNode.insertBefore(img.getFrame().getDrawFrameElement(), getNode());
        // if the placeholder has siblings only delete the placeholder
        if (getNode().getPreviousSibling() != null || getNode().getNextSibling() != null) {
            parentNode.removeChild(getNode());
        } else {
            // remove the placeholder node and all empty parent nodes up to the
            // parent paragraph
            Node currentNode = getNode();
            while (!currentNode.getNodeName().contentEquals(TextPElement.ELEMENT_NAME.getQName())) {
                if (currentNode.getNodeName().contentEquals(TextPlaceholderElement.ELEMENT_NAME.getQName()) || currentNode.getPreviousSibling() == null
                        || currentNode.getNextSibling() == null) {
                    parentNode = currentNode.getParentNode();
                    parentNode.removeChild(currentNode);
                }
                currentNode = parentNode;
            }
            parentNode = currentNode.getParentNode();
        }
		return parentNode;
	}

	/**
	 * Replaces the placeholder with an image.
	 *
	 * @param uri
	 *            URI for the image
	 * @return the replaced Node
	 */
	public Node replaceWith(URI uri) {
		return replaceWith(uri, null, null);
	}

	/**
	 * Look for a parent node with the given class, beginning from startNode.
	 *
	 * @param qName
	 * @param startNode
	 * @return
	 */
	public OdfElement findParentNode(String qName, Node startNode) {
		// OdfElement retval = null;
		if (startNode != null && startNode.getParentNode() != null) {
			if (startNode.getParentNode().getNodeName().contentEquals(qName)) {
				return (OdfElement) startNode.getParentNode();
			} else {
				return findParentNode(qName, startNode.getParentNode());
			}
		}
		return null;
	}

	/**
	 * Converts this placeholder to a {@link TextSelection} object.
	 *
	 * @param elementBase
	 *
	 * @return
	 */
	private TextSelection getTextSelection(OdfElement elementBase) {
		TextNavigation search = new TextNavigation(getNode().getTextContent(), elementBase);
		TextSelection ts = TextSelection.newTextSelection(search, getNode().getTextContent(), elementBase, 0);
		return ts;
	}

	/**
	 * @return the node
	 */
	public Node getNode() {
		return node;
	}

	/**
	 * @param node
	 *            the node to set
	 */
	public void setNode(Node node) {
		this.node = node;
	}

	/**
	 * @return the styleNode
	 */
	public boolean isStyleNode() {
		return styleNode;
	}

	/**
	 * @param styleNode
	 *            the styleNode to set
	 */
	public void setStyleNode(boolean styleNode) {
		this.styleNode = styleNode;
	}

	/**
	 * @return the nodeType
	 */
	public PlaceholderNodeType getNodeType() {
		return nodeType;
	}

	/**
	 * @param nodeType
	 *            the nodeType to set
	 */
	public void setNodeType(PlaceholderNodeType nodeType) {
		this.nodeType = nodeType;
	}

	/**
	 * @return the tableType
	 */
	public PlaceholderTableType getTableType() {
		return tableType;
	}

	/**
	 * @param tableType
	 *            the tableType to set
	 */
	public void setTableType(PlaceholderTableType tableType) {
		this.tableType = tableType;
	}

    /**
	 * Compares the (text) content of this node to another
	 * {@link PlaceholderNode}.
	 *
	 * @param other
	 * @return
	 */
	public int compareTo(PlaceholderNode other) {
		return getNodeText().compareTo(other.getNodeText());
	}

	/**
	 * Compares the (text) content of two nodes to another
	 * {@link PlaceholderNode}. This is a convenience method for comparing with
	 * a functional interface.
	 *
	 * @param node1
	 *            the first node to compare
	 * @param node2
	 *            the second node to compare
	 * @return the comparision result (0 if both node texts are equal)
	 */
	public static int compareByText(PlaceholderNode node1, PlaceholderNode node2) {
		return node1.compareTo(node2);
	}

	/**
	 * The text content of a node. If the node is a text node (of type
	 * {@link Node#TEXT_NODE}) then the node value is returned. Otherwise the
	 * content of the appropriate {@link OdfElement} is returned.
	 *
	 * @return a string containing the text content of this node.
	 */
	public String getNodeText() {
		if (node.getNodeType() == Node.TEXT_NODE)
			return node.getNodeValue();
		if (node instanceof OdfElement)
			return TextExtractor.getText((OdfElement) node);
		return "";
	}

	@Override
	public String toString() {
		StringBuffer retval = new StringBuffer("PlaceholderNode for '");
		if (getNode() != null) {
			retval.append(getNode().getTextContent()).append("'");
		}
		return retval.toString();
	}

	@Override
	public void cut() throws InvalidNavigationException {
		// TODO Auto-generated method stub

	}

	@Override
	public void pasteAtFrontOf(Selection positionItem) throws InvalidNavigationException {
		// TODO Auto-generated method stub

	}

	@Override
	public void pasteAtEndOf(Selection positionItem) throws InvalidNavigationException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void refreshAfterFrontalDelete(Selection deletedItem) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void refreshAfterFrontalInsert(Selection insertedItem) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void refresh(int offset) {
		// TODO Auto-generated method stub

	}

}
