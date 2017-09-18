package md.leonis.watcher.domain;

import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.text.Text;
import lombok.Getter;
import lombok.Setter;
import org.jsoup.nodes.Element;

@Getter
@Setter
public class TagTreeItem<T> extends TreeItem<String> {

    public Element getElement() {
        return element;
    }

    private Element element;

    public TagTreeItem() {
        super();
    }

    public TagTreeItem(final Element element) {
        this(element, new Text("<" + element.tagName() + ">"));
    }

    public TagTreeItem(final Element element, final Node node) {
        super(element.ownText(), node);
        this.element = element;
    }

    public void addChildren() {
        element.children().forEach(e -> {
            TagTreeItem<Element> item = new TagTreeItem<>(e);
            item.setExpanded(true);
            this.getChildren().add(item);
            item.addChildren();
        });
    }

    public String getParents() {
        StringBuilder result = new StringBuilder();
        element.parents().forEach(parent -> result.insert(0, parent.nodeName() + " / "));
        result.append(element.nodeName()).append(" <");
        result.append(element.attributes()).append(">");
        return result.toString();
    }

}
