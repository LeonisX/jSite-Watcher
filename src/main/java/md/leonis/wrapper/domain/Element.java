package md.leonis.wrapper.domain;

import lombok.Getter;
import lombok.Setter;
import org.w3c.dom.Node;

@Getter
@Setter
public class Element {

    private Node node;

    private Node parent;

    private ElementType type;

    public Element(Node node, Node parent, ElementType type) {
        this.node = node;
        this.parent = parent;
        this.type = type;
    }

    public String getXpath() {
        return getParent(node, "");
    }

    private String getParent(Node node, String path) {
        if (node.getParentNode() == null) {
            return path;
        } else {
            Node parentNode = node.getParentNode();
            int index = getNodeIndex(parentNode, node);
            return getParent(parentNode, "/" + node.getNodeName() + "[" + index + "]" + path);
        }
    }

    private int getNodeIndex(Node parentNode, Node childNode) {
        int index = 0;
        for (int i = 0; i < parentNode.getChildNodes().getLength(); i++) {
            Node child = parentNode.getChildNodes().item(i);
            if (child.isSameNode(childNode)) {
                return index;
            }
            if (child.getNodeName().equals(childNode.getNodeName())) {
                index++;
            }
        }
        throw new IndexNotFoundException();
    }
}
