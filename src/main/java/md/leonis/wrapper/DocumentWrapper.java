package md.leonis.wrapper;

import lombok.Getter;
import md.leonis.wrapper.domain.Element;
import md.leonis.wrapper.domain.ElementType;
import md.leonis.wrapper.domain.NodeValues;
import org.w3c.dom.*;
import org.w3c.dom.html.*;

import javax.lang.model.type.UnknownTypeException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class DocumentWrapper {

    private Document document;

    private List<Element> elements;

    private DocumentWrapper() {
        // empty
    }

    private DocumentWrapper(Document document) {
        this.document = document;
        elements = new ArrayList<>();
        walkDocument(new Element(document, null, ElementType.ROOT));
    }

    public static DocumentWrapper wrapDocument(Document document) {
        return new DocumentWrapper(document);
    }

    private void walkDocument(Element parentElement) {
        elements.add(parentElement);
        System.out.println(parentElement.getNode().getClass().getSimpleName());

        List<NodeValues> nodeValuesList = new ArrayList<>();

        if (parentElement.getNode() instanceof Node) {
            nodeValuesList.add(processNode(parentElement.getNode()));
        }

        if (parentElement.getNode() instanceof Document) {
            nodeValuesList.add(processDocument((Document) parentElement.getNode()));
        }

        if (parentElement.getNode() instanceof HTMLDocument) {
            nodeValuesList.add(processHTMLDocument((HTMLDocument) parentElement.getNode()));
        }

        if (parentElement.getNode() instanceof CharacterData) {
            nodeValuesList.add(processCharacterData((CharacterData) parentElement.getNode()));
        }

        /*if (parentElement.getNode() instanceof org.w3c.dom.Element) {
            nodeValuesList.add(processElement((org.w3c.dom.Element) parentElement.getNode()));
        }*/

        if (parentElement.getNode() instanceof Text) {
            nodeValuesList.add(processText((Text) parentElement.getNode()));
        }
        
        if (parentElement.getNode() instanceof Comment) {
            nodeValuesList.add(processComment((Comment) parentElement.getNode()));
        }



        if (parentElement.getNode() instanceof HTMLElement) {
            nodeValuesList.add(processHTMLElement((HTMLElement) parentElement.getNode()));
        }

        String className = parentElement.getNode().getClass().getSimpleName();

        switch (className) {
            case "HTMLDocumentImpl":
            case "DocumentTypeImpl":
            case "HTMLElementImpl":
            case "TextImpl":
            case "CommentImpl":
                break;
            case "HTMLHeadElementImpl":
                nodeValuesList.add(processHTMLHeadElement((HTMLHeadElement) parentElement.getNode()));
                break;
            case "HTMLBaseElementImpl":
                nodeValuesList.add(processHTMLBaseElement((HTMLBaseElement) parentElement.getNode()));
                break;
            case "HTMLMetaElementImpl":
                nodeValuesList.add(processHTMLMetaElement((HTMLMetaElement) parentElement.getNode()));
                break;
            case "HTMLTitleElementImpl":
                nodeValuesList.add(processHTMLTitleElement((HTMLTitleElement) parentElement.getNode()));
                break;
            case "HTMLLinkElementImpl":
                nodeValuesList.add(processHTMLLinkElement((HTMLLinkElement) parentElement.getNode()));
                break;
            case "HTMLScriptElementImpl":
                nodeValuesList.add(processHTMLScriptElement((HTMLScriptElement) parentElement.getNode()));
                break;
            case "HTMLStyleElementImpl":
                nodeValuesList.add(processHTMLStyleElement((HTMLStyleElement) parentElement.getNode()));
                break;
            case "HTMLBodyElementImpl":
                nodeValuesList.add(processHTMLBodyElement((HTMLBodyElement) parentElement.getNode()));
                break;
            case "HTMLDivElementImpl":
                nodeValuesList.add(processHTMLDivElement((HTMLDivElement) parentElement.getNode()));
                break;
            case "HTMLAnchorElementImpl":
                nodeValuesList.add(processHTMLAnchorElement((HTMLAnchorElement) parentElement.getNode()));
                break;
            case "HTMLHeadingElementImpl":
                nodeValuesList.add(processHTMLHeadingElement((HTMLHeadingElement) parentElement.getNode()));
                break;
            case "HTMLFormElementImpl":
                nodeValuesList.add(processHTMLFormElement((HTMLFormElement) parentElement.getNode()));
                break;
            case "HTMLInputElementImpl":
                nodeValuesList.add(processHTMLInputElement((HTMLInputElement) parentElement.getNode()));
                break;
            case "HTMLSelectElementImpl":
                nodeValuesList.add(processHTMLSelectElement((HTMLSelectElement) parentElement.getNode()));
                break;
            case "HTMLOptionElementImpl":
                nodeValuesList.add(processHTMLOptionElement((HTMLOptionElement) parentElement.getNode()));
                break;
            case "HTMLButtonElementImpl":
                nodeValuesList.add(processHTMLButtonElement((HTMLButtonElement) parentElement.getNode()));
                break;
            case "HTMLUListElementImpl":
                nodeValuesList.add(processHTMLUListElement((HTMLUListElement) parentElement.getNode()));
                break;
            case "HTMLLIElementImpl":
                nodeValuesList.add(processHTMLLIElement((HTMLLIElement) parentElement.getNode()));
                break;
            case "HTMLImageElementImpl":
                nodeValuesList.add(processHTMLImageElement((HTMLImageElement) parentElement.getNode()));
                break;
            case "HTMLParagraphElementImpl":
                nodeValuesList.add(processHTMLParagraphElement((HTMLParagraphElement) parentElement.getNode()));
                break;
            case "HTMLBRElementImpl":
                nodeValuesList.add(processHTMLBRElement((HTMLBRElement) parentElement.getNode()));
                break;
            case "HTMLIFrameElementImpl":
                nodeValuesList.add(processHTMLIFrameElement((HTMLIFrameElement) parentElement.getNode()));
                break;
            case "HTMLLabelElementImpl":
                nodeValuesList.add(processHTMLLabelElement((HTMLLabelElement) parentElement.getNode()));
                break;
            case "HTMLTableElementImpl":
                nodeValuesList.add(processHTMLTableElement((HTMLTableElement) parentElement.getNode()));
                break;
            case "HTMLTableSectionElementImpl":
                nodeValuesList.add(processHTMLTableSectionElement((HTMLTableSectionElement) parentElement.getNode()));
                break;
            case "HTMLTableRowElementImpl":
                nodeValuesList.add(processHTMLTableRowElement((HTMLTableRowElement) parentElement.getNode()));
                break;
            case "HTMLTableCellElementImpl":
                nodeValuesList.add(processHTMLTableCellElement((HTMLTableCellElement) parentElement.getNode()));
                break;
            case "HTMLOListElementImpl":
                nodeValuesList.add(processHTMLOListElement((HTMLOListElement) parentElement.getNode()));
                break;
            case "HTMLFontElementImpl":
                nodeValuesList.add(processHTMLFontElement((HTMLFontElement) parentElement.getNode()));
                break;
            case "HTMLDListElementImpl":
                nodeValuesList.add(processHTMLDListElement((HTMLDListElement) parentElement.getNode()));
                break;
                
            default:
                throw new RuntimeException(className);
        }

        Collections.reverse(nodeValuesList);

        System.out.println();
        System.out.println(parentElement.getXpath());
        nodeValuesList.forEach(NodeValues::print);

        for (int i = 0; i < parentElement.getNode().getChildNodes().getLength(); i++) {
            Node child = parentElement.getNode().getChildNodes().item(i);
            Element element = new Element(child, parentElement.getNode(), ElementType.NODE);
            walkDocument(element);
        }

    }

    private NodeValues processHTMLDListElement(HTMLDListElement node) {
        NodeValues result = new NodeValues("HTMLDListElement");
        result.getValues().put("Compact", node.getCompact());
        // setCompact
        // setStart
        // setType
        return result;
    }

    private NodeValues processHTMLFontElement(HTMLFontElement node) {
        NodeValues result = new NodeValues("HTMLFontElement");
        result.getValues().put("Color", node.getColor());
        result.getValues().put("Face", node.getFace());
        result.getValues().put("Size", node.getSize());
        // setColor
        // setFace
        // setSize
        return result;
    }

    private NodeValues processHTMLOListElement(HTMLOListElement node) {
        NodeValues result = new NodeValues("HTMLOListElement");
        result.getValues().put("Compact", node.getCompact());
        result.getValues().put("Start", node.getStart());
        result.getValues().put("Type", node.getType());
        // setCompact
        // setStart
        // setType
        return result;
    }

    private NodeValues processHTMLTableCellElement(HTMLTableCellElement node) {
        NodeValues result = new NodeValues("HTMLTableCellElement");
        result.getValues().put("CellIndex", node.getCellIndex());
        result.getValues().put("Abbr", node.getAbbr());
        result.getValues().put("Align", node.getAlign());
        result.getValues().put("Axis", node.getAxis());
        result.getValues().put("BgColor", node.getBgColor());
        result.getValues().put("Ch", node.getCh());
        result.getValues().put("ChOff", node.getChOff());
        result.getValues().put("ColSpan", node.getColSpan());
        result.getValues().put("Headers", node.getHeaders());
        result.getValues().put("Height", node.getHeight());
        result.getValues().put("NoWrap", node.getNoWrap());
        result.getValues().put("RowSpan", node.getRowSpan());
        result.getValues().put("Scope", node.getScope());
        result.getValues().put("VAlign", node.getVAlign());
        result.getValues().put("Width", node.getWidth());
        // setAbbr
        // setAxis
        // setBgColor
        // setCh
        // setChOff
        // setColSpan
        // setHeaders
        // setHeight
        // setNoWrap
        // setRowSpan
        // setScope
        // setVAlign
        // setWidth
        return result;
    }

    private NodeValues processHTMLTableRowElement(HTMLTableRowElement node) {
        NodeValues result = new NodeValues("HTMLTableRowElement");
        result.getValues().put("RowIndex", node.getRowIndex());
        result.getValues().put("SectionRowIndex", node.getSectionRowIndex());
        result.getValues().put("Cells", node.getCells());
        result.getValues().put("Align", node.getAlign());
        result.getValues().put("BgColor", node.getBgColor());
        result.getValues().put("Ch", node.getCh());
        result.getValues().put("ChOff", node.getChOff());
        result.getValues().put("VAlign", node.getVAlign());
        // setAlign
        // setBgColor
        // setCh​
        // setChOff
        // setVAlign
        // insertCell
        // deleteCell
        return result;
    }

    private NodeValues processHTMLTableSectionElement(HTMLTableSectionElement node) {
        NodeValues result = new NodeValues("HTMLTableSectionElement");
        result.getValues().put("Align", node.getAlign());
        result.getValues().put("Ch", node.getCh());
        result.getValues().put("ChOff", node.getChOff());
        result.getValues().put("VAlign", node.getVAlign());
        result.getValues().put("Rows", node.getRows());
        // setAlign
        // setCh
        // setChOff
        // setVAlign
        // insertRow
        // deleteRow
        return result;
    }

    private NodeValues processHTMLTableElement(HTMLTableElement node) {
        NodeValues result = new NodeValues("HTMLTableElement");
        result.getValues().put("Caption", node.getCaption());
        result.getValues().put("THead", node.getTHead());
        result.getValues().put("TFoot", node.getTFoot());
        result.getValues().put("Rows", node.getRows());
        result.getValues().put("TBodies", node.getTBodies());
        result.getValues().put("Align", node.getAlign());
        result.getValues().put("BgColor", node.getBgColor());
        result.getValues().put("Border", node.getBorder());
        result.getValues().put("CellPadding", node.getCellPadding());
        result.getValues().put("CellSpacing", node.getCellSpacing());
        result.getValues().put("Frame", node.getFrame());
        result.getValues().put("Rules", node.getRules());
        result.getValues().put("Summary", node.getSummary());
        result.getValues().put("Width", node.getWidth());
        // setCaption
        // setTHead
        // setTFoot
        // setAlign
        // setBgColor
        // setBorder
        // setCellPadding
        // setCellSpacing
        // setFrame
        // setRules
        // setSummary
        // setWidth
        // createTHead
        // deleteTHead
        // createTFoot
        // deleteTFoot
        // createCaption
        // deleteCaption​
        // insertRow​
        // deleteRow
        return result;
    }

    private NodeValues processComment(Comment node) {
        return new NodeValues("Comment");
    }

    private NodeValues processCharacterData(CharacterData node) {
        NodeValues result = new NodeValues("CharacterData");
        result.getValues().put("Data", node.getData());
        result.getValues().put("Length", node.getLength());
        // setData
        // substringData
        // appendData
        // insertData
        // deleteData
        // replaceData
        return result;
    }

    private NodeValues processHTMLLabelElement(HTMLLabelElement node) {
        NodeValues result = new NodeValues("HTMLLabelElement");
        result.getValues().put("Form", node.getForm());
        result.getValues().put("AccessKey", node.getAccessKey());
        result.getValues().put("HtmlFor", node.getHtmlFor());
        // setAccessKey
        // setHtmlFor
        return result;
    }

    private NodeValues processHTMLIFrameElement(HTMLIFrameElement node) {
        NodeValues result = new NodeValues("HTMLIFrameElement");
        result.getValues().put("Align", node.getAlign());
        result.getValues().put("FrameBorder", node.getFrameBorder());
        result.getValues().put("Height", node.getHeight());
        result.getValues().put("LongDesc", node.getLongDesc());
        result.getValues().put("MarginHeight", node.getMarginHeight());
        result.getValues().put("MarginWidth", node.getMarginWidth());
        result.getValues().put("Name", node.getName());
        result.getValues().put("Scrolling", node.getScrolling());
        result.getValues().put("Src", node.getSrc());
        result.getValues().put("Width", node.getWidth());
        result.getValues().put("ContentDocument", node.getContentDocument());
        // setAlign
        // setFrameBorder
        // setHeight
        // setLongDesc
        // setMarginHeight
        // setMarginWidth
        // setName
        // setScrolling
        // setSrc
        // setWidth
        return result;
    }

    private NodeValues processHTMLBRElement(HTMLBRElement node) {
        NodeValues result = new NodeValues("HTMLBRElement");
        result.getValues().put("Clear", node.getClear());
        // setClear
        return result;
    }

    private NodeValues processHTMLParagraphElement(HTMLParagraphElement node) {
        NodeValues result = new NodeValues("HTMLParagraphElement");
        result.getValues().put("Align", node.getAlign());
        // setAlign
        return result;
    }

    private NodeValues processHTMLImageElement(HTMLImageElement node) {
        NodeValues result = new NodeValues("HTMLImageElement");
        // result.getValues().put("LowSrc", node.getLowSrc()); UnsupportedOperationException
        result.getValues().put("Name", node.getName());
        result.getValues().put("Align", node.getAlign());
        result.getValues().put("Alt", node.getAlt());
        result.getValues().put("Border", node.getBorder());
        result.getValues().put("Height", node.getHeight());
        result.getValues().put("Hspace", node.getHspace());
        result.getValues().put("IsMap", node.getIsMap());
        result.getValues().put("LongDesc", node.getLongDesc());
        result.getValues().put("Src", node.getSrc());
        result.getValues().put("UseMap", node.getUseMap());
        result.getValues().put("Vspace", node.getVspace());
        result.getValues().put("Width", node.getWidth());
        // setLowSrc
        // setName
        // setAlign
        // setAlt
        // setBorder
        // setHeight
        // setHspace
        // setIsMap
        // setLongDesc
        // setSrc
        // setUseMap
        // setVspace
        // setWidth
        return result;
    }

    private NodeValues processHTMLLIElement(HTMLLIElement node) {
        NodeValues result = new NodeValues("HTMLLIElement");
        result.getValues().put("Type", node.getType());
        result.getValues().put("Value", node.getValue());
        // setType
        // setCompact
        return result;
    }

    private NodeValues processHTMLUListElement(HTMLUListElement node) {
        NodeValues result = new NodeValues("HTMLUListElement");
        result.getValues().put("Compact", node.getCompact());
        result.getValues().put("Type", node.getType());
        // setCompact
        // setValue
        return result;
    }

    private NodeValues processHTMLButtonElement(HTMLButtonElement node) {
        NodeValues result = new NodeValues("HTMLButtonElement");
        result.getValues().put("Form", node.getForm());
        result.getValues().put("AccessKey", node.getAccessKey());
        result.getValues().put("Disabled", node.getDisabled());
        result.getValues().put("Name", node.getName());
        result.getValues().put("TabIndex", node.getTabIndex());
        result.getValues().put("Type", node.getType());
        result.getValues().put("Value", node.getValue());
        // setAccessKey
        // setDisabled
        // setName
        // setTabIndex
        // setValue
        return result;
    }

    private NodeValues processHTMLOptionElement(HTMLOptionElement node) {
        NodeValues result = new NodeValues("HTMLOptionElement");
        result.getValues().put("Form", node.getForm());
        result.getValues().put("DefaultSelected", node.getDefaultSelected());
        result.getValues().put("Text", node.getText());
        result.getValues().put("Index", node.getIndex());
        result.getValues().put("Disabled", node.getDisabled());
        result.getValues().put("Label", node.getLabel());
        result.getValues().put("Selected", node.getSelected());
        result.getValues().put("Value", node.getValue());
        // setDefaultSelected
        // setDisabled
        // setDisabled
        // setSelected
        // setValue
        return result;
    }

    private NodeValues processHTMLSelectElement(HTMLSelectElement node) {
        NodeValues result = new NodeValues("HTMLSelectElement");
        result.getValues().put("Type", node.getType());
        result.getValues().put("SelectedIndex", node.getSelectedIndex());
        result.getValues().put("Value", node.getValue());
        result.getValues().put("Length", node.getLength());
        result.getValues().put("Form", node.getForm());
        result.getValues().put("Options", node.getOptions());
        result.getValues().put("Disabled", node.getDisabled());
        result.getValues().put("Multiple", node.getMultiple());
        result.getValues().put("Name", node.getName());
        result.getValues().put("Size", node.getSize());
        result.getValues().put("TabIndex", node.getTabIndex());
        // setSelectedIndex
        // setValue
        // setDisabled
        // setMultiple
        // setName
        // setSize
        // setTabIndex
        // add
        // remove
        // blur
        // focus
        return result;
    }

    private NodeValues processHTMLInputElement(HTMLInputElement node) {
        NodeValues result = new NodeValues("HTMLInputElement");
        result.getValues().put("DefaultValue", node.getDefaultValue());
        result.getValues().put("DefaultChecked", node.getDefaultChecked());
        result.getValues().put("Form", node.getForm());
        result.getValues().put("Accept", node.getAccept());
        result.getValues().put("AccessKey", node.getAccessKey());
        result.getValues().put("Align", node.getAlign());
        result.getValues().put("Alt", node.getAlt());
        result.getValues().put("Checked", node.getChecked());
        result.getValues().put("Disabled", node.getDisabled());
        result.getValues().put("MaxLength", node.getMaxLength());
        result.getValues().put("Name", node.getName());
        result.getValues().put("ReadOnly", node.getReadOnly());
        result.getValues().put("Size", node.getSize());
        result.getValues().put("Src", node.getSrc());
        result.getValues().put("TabIndex", node.getTabIndex());
        result.getValues().put("Type", node.getType());
        result.getValues().put("UseMap", node.getUseMap());
        result.getValues().put("Value", node.getValue());
        // setDefaultValue
        // setDefaultChecked
        // setAccept
        // setAccessKey
        // setAlign
        // setAlt
        // setChecked
        // setDisabled
        // setMaxLength
        // setName
        // setReadOnly
        // setSize
        // setSrc
        // setTabIndex
        // setUseMap
        // setValue
        // blur
        // focus
        // select
        // click
        return result;
    }

    private NodeValues processHTMLFormElement(HTMLFormElement node) {
        NodeValues result = new NodeValues("HTMLFormElement");
        result.getValues().put("Elements", node.getElements());
        result.getValues().put("Length", node.getLength());
        result.getValues().put("Name", node.getName());
        result.getValues().put("AcceptCharset", node.getAcceptCharset());
        result.getValues().put("Action", node.getAction());
        result.getValues().put("Enctype", node.getEnctype());
        result.getValues().put("Method", node.getMethod());
        result.getValues().put("Target", node.getTarget());
        // setName
        // setAcceptCharset
        // setAction
        // setEnctype
        // setMethod
        // setTarget
        // submit
        // reset
        return result;
    }

    private NodeValues processHTMLHeadingElement(HTMLHeadingElement node) {
        NodeValues result = new NodeValues("HTMLHeadingElement");
        result.getValues().put("Align", node.getAlign());
        // setAlign
        return result;
    }

    private NodeValues processHTMLAnchorElement(HTMLAnchorElement node) {
        NodeValues result = new NodeValues("HTMLAnchorElement");
        result.getValues().put("AccessKey", node.getAccessKey());
        result.getValues().put("Charset", node.getCharset());
        result.getValues().put("Coords", node.getCoords());
        result.getValues().put("Href", node.getHref());
        result.getValues().put("Hreflang", node.getHreflang());
        result.getValues().put("Name", node.getName());
        result.getValues().put("Rel", node.getRel());
        result.getValues().put("Rev", node.getRev());
        result.getValues().put("Shape", node.getShape());
        result.getValues().put("TabIndex", node.getTabIndex());
        result.getValues().put("Target", node.getTarget());
        result.getValues().put("Type", node.getType());
        // setAccessKey
        // setCharset
        // setCoords
        // setHref
        // setHreflang
        // setName
        // setRel
        // setRev
        // setShape
        // setTabIndex
        // setTarget
        // setType
        // blur
        // focus
        return result;
    }

    private NodeValues processHTMLDivElement(HTMLDivElement node) {
        NodeValues result = new NodeValues("HTMLDivElement");
        result.getValues().put("Align", node.getAlign());
        // setAlign
        return result;
    }

    private NodeValues processHTMLBodyElement(HTMLBodyElement node) {
        NodeValues result = new NodeValues("HTMLBodyElement");
        result.getValues().put("ALink", node.getALink());
        result.getValues().put("Background", node.getBackground());
        result.getValues().put("BgColor", node.getBgColor());
        result.getValues().put("Link", node.getLink());
        result.getValues().put("Text", node.getText());
        result.getValues().put("VLink", node.getVLink());
        // setALink
        // setBackground
        // setBgColor
        // setLink
        // setText
        // setVLink
        return result;
    }

    private NodeValues processHTMLStyleElement(HTMLStyleElement node) {
        NodeValues result = new NodeValues("HTMLStyleElement");
        result.getValues().put("Disabled", node.getDisabled());
        result.getValues().put("Media", node.getMedia());
        result.getValues().put("Type", node.getType());
        // setDisabled
        // setMedia
        // setType
        return result;
    }

    private NodeValues processHTMLScriptElement(HTMLScriptElement node) {
        NodeValues result = new NodeValues("HTMLScriptElement");
        result.getValues().put("Text", node.getText());
        result.getValues().put("HtmlFor", node.getHtmlFor());
        result.getValues().put("Event", node.getEvent());
        result.getValues().put("Charset", node.getCharset());
        result.getValues().put("Defer", node.getDefer());
        result.getValues().put("Src", node.getSrc());
        result.getValues().put("Type", node.getType());
        // setText
        // setHtmlFor
        // setEvent
        // setCharset
        // setDefer
        // setSrc
        // setType
        return result;
    }

    private NodeValues processHTMLLinkElement(HTMLLinkElement node) {
        NodeValues result = new NodeValues("HTMLLinkElement");
        result.getValues().put("Disabled", node.getDisabled());
        result.getValues().put("Charset", node.getCharset());
        result.getValues().put("Href", node.getHref());
        result.getValues().put("Hreflang", node.getHreflang());
        result.getValues().put("Media", node.getMedia());
        result.getValues().put("Rel", node.getRel());
        result.getValues().put("Rev", node.getRev());
        result.getValues().put("Target", node.getTarget());
        result.getValues().put("Type", node.getType());
        // setDisabled
        // setCharset
        // setHref
        // setHreflang
        // setMedia
        // setRel
        // setRev
        // setTarget
        // setType
        return result;
    }

    private NodeValues processHTMLTitleElement(HTMLTitleElement node) {
        NodeValues result = new NodeValues("HTMLTitleElement");
        result.getValues().put("Text", node.getText());
        // setText
        return result;
    }

    private NodeValues processHTMLMetaElement(HTMLMetaElement node) {
        NodeValues result = new NodeValues("HTMLMetaElement");
        result.getValues().put("Content", node.getContent());
        result.getValues().put("HttpEquiv", node.getHttpEquiv());
        result.getValues().put("Name", node.getName());
        result.getValues().put("Scheme", node.getScheme());
        // setContent
        // setHttpEquiv
        // setName
        // setScheme
        return result;
    }

    private NodeValues processHTMLBaseElement(HTMLBaseElement node) {
        NodeValues result = new NodeValues("HTMLBaseElement");
        result.getValues().put("Href", node.getHref());
        result.getValues().put("Target", node.getTarget());
        // setHref
        // setTarget
        return result;
    }

    private NodeValues processText(Text node) {
        NodeValues result = new NodeValues("HTMLHeadElement");
        //result.getValues().put("isElementContentWhitespace", node.isElementContentWhitespace()); //UnsupportedOperationException
        result.getValues().put("WholeText", node.getWholeText());
        // splitText
        // replaceWholeText
        return result;
    }

    private NodeValues processHTMLHeadElement(HTMLHeadElement node) {
        NodeValues result = new NodeValues("HTMLHeadElement");
        result.getValues().put("Profile", node.getProfile());
        // setProfile
        return result;
    }

    private NodeValues processHTMLElement(HTMLElement htmlElement) {
        NodeValues result = new NodeValues("HTMLElement");
        result.getValues().put("Id", htmlElement.getId());
        result.getValues().put("Title", htmlElement.getTitle());
        result.getValues().put("Lang", htmlElement.getLang());
        result.getValues().put("Dir", htmlElement.getDir());
        result.getValues().put("ClassName", htmlElement.getClassName());
        // setId
        // setTitle
        // setLang
        // setDir
        // setClassName
        return result;
    }

    private NodeValues processElement(org.w3c.dom.Element element) {
        NodeValues result = new NodeValues("Element");
        //result.getValues().put("getTagName", element.getTagName()); same as Node.getNodeName
        //result.getValues().put("getSchemaTypeInfo", element.getSchemaTypeInfo()); // UnsupportedOperationException
        // getAttribute
        // getAttributeNS
        // setAttribute
        // setAttributeNS
        // removeAttribute
        // removeAttributeNS
        // getAttributeNode
        // getAttributeNodeNS
        // setAttributeNode
        // setAttributeNodeNS
        // removeAttributeNode
        // hasAttribute
        // hasAttributeNS

        // setIdAttribute
        // setIdAttributeNS
        // setIdAttributeNode

        // getElementsByTagName
        // getElementsByTagNameNS
        return result;
    }


    private NodeValues processHTMLDocument(HTMLDocument node) {
        NodeValues result = new NodeValues("HTMLDocument");
        result.getValues().put("URL", node.getURL());
        result.getValues().put("Domain", node.getDomain());
        result.getValues().put("Referrer", node.getReferrer());
        result.getValues().put("Cookie", node.getCookie());
        //////////////////// result.getValues().put("getTitle", node.getTitle());
        //////////////////// result.getValues().put("getBody", node.getBody());

        //result.getValues().put("getImages", node.getImages());
        //result.getValues().put("getApplets", node.getApplets());
        //result.getValues().put("getLinks", node.getLinks());
        //result.getValues().put("getForms", node.getForms());
        //result.getValues().put("getAnchors", node.getAnchors());
        // setTitle
        // setBody
        // setCookie

        // open, close, write, writeln

        // getElementsByName
        return result;
    }

    private NodeValues processDocument(Document node) {
        NodeValues result = new NodeValues("Document");
        //////////////////// result.getValues().put("getDoctype", node.getDoctype());
        //////////////////// result.getValues().put("getDocumentElement", node.getDocumentElement());
        //////////////////// result.getValues().put("getImplementation", node.getImplementation());
        result.getValues().put("InputEncoding", node.getInputEncoding());
        result.getValues().put("XmlEncoding", node.getXmlEncoding());
        result.getValues().put("XmlStandalone", node.getXmlStandalone());
        result.getValues().put("XmlVersion", node.getXmlVersion());
        //result.getValues().put("getStrictErrorChecking", node.getStrictErrorChecking()); // UnsupportedOperationException
        //result.getValues().put("getDocumentURI", node.getDocumentURI()); // same as Node.getBaseURI
        //result.getValues().put("getDomConfig", node.getDomConfig()); // UnsupportedOperationException
        //result.getValues().put("getNodeName", node.getNodeName());

        // createAttribute
        // createAttributeNS
        // createCDATASection
        // createComment
        // createDocumentFragment
        // createElement
        // createElementNS
        // createEntityReference
        // createProcessingInstruction
        // createTextNode

        // importNode
        // adoptNode
        // renameNode

        // setXmlStandalone
        // setXmlVersion
        // setStrictErrorChecking
        // setDocumentURI

        // getElementsByTagName
        // getElementsByTagNameNS
        // getElementById

        // normalizeDocument
        return result;
    }

    private NodeValues processNode(Node node) {
        // Self operations: normalize, cloneNode, compareDocumentPosition, isSameNode, isEqualNode

        // Upper levels: getParentNode, getOwnerDocument
        // Sibling: getPreviousSibling, getNextSibling
        // Children: getChildNodes, getFirstChild, getLastChild, replaceChild, removeChild,
        // appendChild, hasChildNodes, insertBefore,

        // Attributes: getAttributes, hasAttributes
        // Features: getFeature, isSupported // https://www.w3.org/TR/2004/REC-DOM-Level-3-Core-20040407/core.html#DOMFeatures

        NodeValues result = new NodeValues("Node");
        result.getValues().put("BaseURI", node.getBaseURI());
        result.getValues().put("NodeName", node.getNodeName());
        result.getValues().put("LocalName", node.getLocalName()); // ELEMENT_NODE and ATTRIBUTE_NODE
        result.getValues().put("NodeType", node.getNodeType());
        result.getValues().put("NamespaceURI", node.getNamespaceURI());
        result.getValues().put("Prefix", node.getPrefix());
        result.getValues().put("NodeValue", node.getNodeValue()); // ELEMENT_NODE and ATTRIBUTE_NODE
        // ELEMENT_NODE, ATTRIBUTE_NODE, ENTITY_NODE, ENTITY_REFERENCE_NODE, DOCUMENT_FRAGMENT_NODE
        // TEXT_NODE, CDATA_SECTION_NODE, COMMENT_NODE, PROCESSING_INSTRUCTION_NODE 	nodeValue
        // DOCUMENT_NODE, DOCUMENT_TYPE_NODE, NOTATION_NODE 	null
        //////////////////////////result.getValues().put("getTextContent", node.getTextContent());
        // lookupPrefix(String namespaceURI)
        // lookupNamespaceURI(String prefix)
        // isDefaultNamespace()
        // setNodeValue
        // setPrefix
        // setTextContent
        // setUserData
        // getUserData
        return result;
    }




    private void walkTree(Node node, Element parent) {
        elements.add(parent);
    }
}
