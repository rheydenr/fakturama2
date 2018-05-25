package org.odftoolkit.simple.common.navigation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.odftoolkit.odfdom.dom.OdfDocumentNamespace;
import org.odftoolkit.odfdom.dom.element.office.OfficeAnnotationElement;
import org.odftoolkit.odfdom.dom.element.office.OfficeMasterStylesElement;
import org.odftoolkit.odfdom.dom.element.table.TableTableElement;
import org.odftoolkit.odfdom.dom.element.table.TableTableRowElement;
import org.odftoolkit.odfdom.dom.element.text.TextPlaceholderElement;
import org.odftoolkit.odfdom.pkg.OdfElement;
import org.odftoolkit.simple.Document;
import org.odftoolkit.simple.common.TextExtractor;
import org.odftoolkit.simple.common.navigation.PlaceholderNode.PlaceholderNodeType;
import org.odftoolkit.simple.common.navigation.PlaceholderNode.PlaceholderTableType;
import org.odftoolkit.simple.table.Row;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The {@link PlaceholderNavigation} class is for navigating in ODF documents,
 * primarily in Writer documents (ODT, OTT).
 *
 */
public class PlaceholderNavigation extends Navigation {

	/**
	 * This is the opening tag for a placeholder.
	 */
	public static final String PLACEHOLDER_PREFIX = "<";

	/**
	 * This is the closing tag for a placeholder.
	 */
	public static final String PLACEHOLDER_SUFFIX = ">";

	private Document mDocument;
	private PlaceholderNode mNextSelectedItem;
	private PlaceholderNode mTempSelectedItem;
	private int mNextIndex;
	private String mPattern;
	private int maxIndex = 0;
	private List<PlaceholderNode> placeHolders = new ArrayList<>();
	private boolean useDelimiters;
    private PlaceholderTableType[] tableIdentifiers = new PlaceholderTableType[]{};

    /**
     * Strings for the table identifiers, perhaps with additional delimiters
     */
    private String[] tableIdentifierStrings;

	private static final Logger logger = Logger.getLogger(PlaceholderNavigation.class.getName());

	/**
	 * Constructor. Collects all placeholders in a {@link Document}.
	 *
	 * @param doc
	 */
	public PlaceholderNavigation(Document doc) {
		this(null, doc, true);
	}

	/**
	 * Constructor. Creates a new {@link PlaceholderNavigation} with a
	 * placeholder key.
	 *
	 * @param pattern
	 *            the placeholder pattern to search for
	 * @param doc
	 *            the document
	 * @param useDelimiters
	 *            if <code>true</code>, the placeholder pattern is surrounded
	 *            with default delimiters ("&lt;" and "&gt;")
	 */
	public PlaceholderNavigation(String pattern, Document doc, boolean useDelimiters) {
		this.mPattern = pattern;
		this.useDelimiters = useDelimiters;
		if (pattern != null && useDelimiters) {
			this.mPattern = String.format("%s%s%s", (useDelimiters ? PLACEHOLDER_PREFIX : ""), pattern,
			        (useDelimiters ? PLACEHOLDER_SUFFIX : ""));
		}
		mDocument = doc;
		mNextSelectedItem = null;
		mTempSelectedItem = null;
		mNextIndex = 0;

		// now initialize the placeholder list
		collectPlaceHolders(this.mPattern);
		placeHolders.sort(PlaceholderNode::compareByText);
		maxIndex = placeHolders.size();
	}
	
	/**
	 * Default constructor.
	 */
	public PlaceholderNavigation() {}
	
	public PlaceholderNavigation of(Document doc) {
		mDocument = doc;
		return this;
	}
    
    public PlaceholderNavigation withTableIdentifiers(PlaceholderTableType... tableIdentifiers ) {
        this.tableIdentifiers = tableIdentifiers;
        return this;
    }
	
	public PlaceholderNavigation withPattern(String pattern) {
        this.mPattern = pattern;
	    return this;
	}
    
    public PlaceholderNavigation withDelimiters(boolean useDelimiters) {
        this.useDelimiters = useDelimiters;
        return this;
    }
    
    public PlaceholderNavigation build() {
        if (useDelimiters) {
            if (this.mPattern != null) {
                this.mPattern = StringUtils.appendIfMissing(StringUtils.prependIfMissing(this.mPattern, PLACEHOLDER_PREFIX), PLACEHOLDER_SUFFIX);
            }

            if (tableIdentifiers != null) {
                this.tableIdentifierStrings = Arrays.stream(tableIdentifiers).map(ti -> StringUtils.prependIfMissing(ti.getKey(), PLACEHOLDER_PREFIX))
                        .collect(Collectors.toList()).toArray(new String[] {});
            }
        }        
        
        mNextSelectedItem = null;
        mTempSelectedItem = null;
        mNextIndex = 0;

        // now initialize the placeholder list
        collectPlaceHolders(this.mPattern);
        placeHolders.sort(PlaceholderNode::compareByText);
        maxIndex = placeHolders.size();
        return this;
    }

	/**
	 * Checks if the given Node (which has to be a placeholder) has the name
	 * which was initially set by constructor.
	 *
	 * @param element
	 *            node to test
	 * @return <code>true</code> if node has the correct placeholder name
	 */
	@Override
	public boolean match(Node element) {
		if (element instanceof TextPlaceholderElement) {
			String content = TextExtractor.getText((OdfElement) element);
			if (content.contentEquals(mPattern)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if has next <code>PlaceholderNode</code> with satisfied content
	 * pattern.
	 */
	public boolean hasNext() {
		mTempSelectedItem = findNext(mNextSelectedItem);
		return (mTempSelectedItem != null);
	}

	/**
	 * Get next <code>PlaceholderNode</code>.
	 *
	 */
	public PlaceholderNode nextSelection() {
		if (mTempSelectedItem != null) {
			mNextSelectedItem = mTempSelectedItem;
			mTempSelectedItem = null;
		} else {
			mNextSelectedItem = findNext(mNextSelectedItem);
		}
		if (mNextSelectedItem == null) {
			return null;
		} else {
			return mNextSelectedItem;
		}
	}

	/**
	 * Find the next <code>Selection</code> start from the <code>selected</code>
	 * .
	 */
	private PlaceholderNode findNext(PlaceholderNode selected) {
		PlaceholderNode element = null;
		if (selected == null && !placeHolders.isEmpty()) {
			element = placeHolders.get(0);
			if (element != null) {
				mNextIndex++;
				return element;
			}
		} else {
			if (mNextIndex < maxIndex) {
				element = placeHolders.get(mNextIndex);
				mNextIndex++;
			}
		}
		return element;
	}

	/**
	 * Get placeholders with a certain pattern.
	 *
	 * @param pattern
	 *            only placeholders which match this pattern are collected. If
	 *            <code>null</code>, all placeholders in the document are
	 *            collected.
	 * @return list of placeholders (for convenience, could be also get via getter)
	 */
    private List<PlaceholderNode> collectPlaceHolders(String pattern) {
		// at first we look into header and footer
		collectPlaceholdersInHeaderFooter(pattern);

		// now collect all the others
		Element rootElement = null;
		try {
			rootElement = mDocument.getContentRoot();
		} catch (Exception ex) {
			logger.log(Level.SEVERE, ex.getMessage(), ex);
		}

		NodeList localPlaceHolders = rootElement.getElementsByTagName(TextPlaceholderElement.ELEMENT_NAME.getQName());
		for (int i = 0; i < localPlaceHolders.getLength(); i++) {
			Node item = localPlaceHolders.item(i);
			PlaceholderNode placeholderNode;
			// Search for entries within VATLIST or ITEM table
			
			Optional<PlaceholderTableType> tableIdentifier = Arrays.stream(tableIdentifiers)
			                .filter(id -> item.getTextContent().startsWith(useDelimiters ? StringUtils.prependIfMissing(id.getKey(), PLACEHOLDER_PREFIX) : id.getKey()))
			                .findFirst();
			
            Node tableForNode = getTableForNode(item);
			if (tableForNode != null && tableIdentifier.isPresent()) {
				// set the table name as identifier for this node since we can have more than one tables of this type
				String tableNameAttribute = StringUtils.defaultString(((TableTableElement)tableForNode).getTableNameAttribute(), "Unnamed_Table");				
				item.setUserData("TABLE_ID", tableNameAttribute, null);
				placeholderNode = new PlaceholderNode(item, PlaceholderNodeType.TABLE_NODE, tableIdentifier.orElse(PlaceholderTableType.NO_TABLE), false);
			} else {
				placeholderNode = new PlaceholderNode(item);
			}

			// if a pattern is set we only collect matching patterns
			if (pattern != null) {
				if (item.getTextContent().equalsIgnoreCase(pattern)) {
					placeHolders.add(placeholderNode);
				}
			} else {
				// else we add any placeholder
				placeHolders.add(placeholderNode);
			}
		}
		return placeHolders;
	}

	/**
	 * Checks if the given node is inside a table.
	 *
	 * @param item
	 * @return
	 */
	private Node getTableForNode(Node item) {
		return getContainerNode(item, OdfDocumentNamespace.TABLE.getUri(), TableTableElement.class);
	}

	/**
     * @return the tableIdentifierStrings
     */
    public final String[] getTableIdentifierStrings() {
        return tableIdentifierStrings;
    }

    private Node getContainerNode(Node item, String urn, Class<?> clazz) {
		if (item == null || item.getParentNode() == null) {
			return null;
		} else {
			//if (StringUtils.equals(item.getParentNode().getNamespaceURI(), urn)) {
			if (clazz.isInstance(item)) {
				return item;
			} else {
				return getContainerNode(item.getParentNode(), urn, clazz);
			}
		}
	}

    private List<PlaceholderNode> collectPlaceholdersInHeaderFooter(String pattern) {
        org.w3c.dom.Document styledom = null;
        Element masterpage = null;

		try {
			styledom = mDocument.getStylesDom();
			NodeList list = styledom.getElementsByTagName(OfficeMasterStylesElement.ELEMENT_NAME.getQName());
			if (list.getLength() > 0) {
				masterpage = (Element) list.item(0);
			} else {
				return placeHolders;
			}
			NodeList placeholders = masterpage.getElementsByTagName(TextPlaceholderElement.ELEMENT_NAME.getQName());
			for (int i = 0; i < placeholders.getLength(); i++) {
				Node item = placeholders.item(i);
				PlaceholderNode placeholderNode = new PlaceholderNode(item, true);

				// if a pattern is set we only collect matching patterns
				if (pattern != null) {
					if (item.getTextContent().equalsIgnoreCase(pattern)) {
						placeHolders.add(placeholderNode);
					}
				} else {
					// else we add any placeholder
					placeHolders.add(placeholderNode);
				}
			}
		} catch (Exception ex) {
			logger.log(Level.SEVERE, ex.getMessage(), ex);
		}
		return placeHolders;
	}

	protected Node getNextPlaceholderInTree(Node startpoint, Node root) {
		Node matchedNode = null;
		matchedNode = traverseTree(startpoint);
		Node currentpoint = startpoint;
		while ((matchedNode == null) && (currentpoint != root)) {
			Node sibling = currentpoint.getNextSibling();
			if ((sibling != null)
			        && (sibling.getNodeType() == Node.ELEMENT_NODE && sibling.getLocalName().equals(
			                TextPlaceholderElement.ELEMENT_NAME.getLocalName()))) {
				matchedNode = sibling;
				break;
			}
			while ((sibling != null) && (matchedNode == null)) {
				if ((sibling.getNodeType() == Node.ELEMENT_NODE && sibling.getLocalName().equals(
				        TextPlaceholderElement.ELEMENT_NAME.getLocalName()))) {
					matchedNode = traverseTree(sibling);
				}
				if (matchedNode == null) {
					sibling = sibling.getNextSibling();
					if (sibling != null) {
						matchedNode = sibling;
						break;
					}
				}
			}
			currentpoint = currentpoint.getParentNode();
		}
		return matchedNode;
	}

	private Node traverseTree(Node root) {
		Node matchedNode = null;
		if (root == null) {
			return null;
		}
		Node node = root.getFirstChild();
		while (node != null) {
			if ((node.getNodeType() == Node.ELEMENT_NODE && node.getLocalName().equals(
			        TextPlaceholderElement.ELEMENT_NAME.getLocalName()))
			        && (!(node instanceof OfficeAnnotationElement))) {
				if (match(node) == true) {
					matchedNode = node;
					break;
				} else {
					matchedNode = traverseTree(node);
					if (matchedNode != null) {
						break;
					}
				}
			}
			node = node.getNextSibling();
		}
		return matchedNode;
	}

	public Row getTableRow(PlaceholderNode placeholderNode) {
		Row odfRow = null;
		// makes no sense to search for a table if the placeholder isn't inside
		// a table
		if (placeholderNode.getNodeType() == PlaceholderNodeType.TABLE_NODE) {
			Node tRow = findContainerNodeWithName(placeholderNode.getNode(),
			        TableTableRowElement.ELEMENT_NAME.getQName());
			// PlaceholderTableRow row = new
			// PlaceholderTableRow((TableTableRowElement) tRow);
			odfRow = Row.getInstance((TableTableRowElement) tRow);
		}
		return odfRow;
	}

	private Node findContainerNodeWithName(Node item, String nodeName) {
		if (item == null || item.getParentNode() == null) {
			return null;
		} else {
			if (StringUtils.equals(item.getParentNode().getNodeName(), nodeName)) {
				return item.getParentNode();
			} else {
				return findContainerNodeWithName(item.getParentNode(), nodeName);
			}
		}
	}

	/**
	 * @return the placeHolders
	 */
	public List<PlaceholderNode> getPlaceHolders() {
		return placeHolders;
	}

	/**
	 * Replaces each placeholder key with the content from the properties hash.
	 *
	 * @param property
	 * @return
	 */
	public void replaceEachWithValue(Properties properties) {
		while (hasNext()) {
			PlaceholderNode item = nextSelection();
			item.replaceWith(properties.getProperty(item.getNodeText()));
		}
	}
}
