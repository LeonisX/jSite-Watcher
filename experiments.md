###Зависимости:

- compile group: 'net.sourceforge.htmlcleaner', name: 'htmlcleaner', version: '2.21'
- compile group: 'nu.validator.htmlparser', name: 'htmlparser', version: '1.4'
- compile group: 'net.sf.jtidy', name: 'jtidy', version: 'r938'
- compile group: 'org.ccil.cowan.tagsoup', name: 'tagsoup', version: '1.2.1'
- compile group: 'net.sourceforge.nekohtml', name: 'nekohtml', version: '1.9.22'
- ****compile group: 'net.htmlparser.jericho', name: 'jericho-html', version: '3.4'

###Код (вперемешку):

```
try {
    // jericho not work at all
    Source source = new Source(content[0]);
    OutputDocument outputDocument = new OutputDocument(source);
    content[0] = outputDocument.getSourceText().toString();
    DOMParser parser = new DOMParser();
    
    // nekohtml
    InputSource inputSource = new InputSource(new StringReader(content[0]));
    parser.parse(inputSource);
    org.w3c.dom.Document document = parser.getDocument();
    
    // very bad results tagsoup
    org.w3c.dom.Document document = getDom(content[0]);
    
    // very bad results jtidy
    Tidy tidy = new Tidy();
    tidy.setQuiet(false);
    tidy.setShowWarnings(true);
    tidy.setShowErrors(0);
    tidy.setMakeClean(true);
    tidy.setForceOutput(true);
    org.w3c.dom.Document document = tidy.parseDOM(new StringReader(content[0]), null);
    
    // not work htmlparser
    HtmlDocumentBuilder parser = new HtmlDocumentBuilder();
    parser.setXmlPolicy(XmlViolationPolicy.ALTER_INFOSET);
    parser.setScriptingEnabled(true);
    parser.setHtml4ModeCompatibleWithXhtml1Schemata(true);
    org.w3c.dom.Document document = parser.parse(content[0]);
    
    // trim tv-games htmlcleaner
    HtmlCleaner cleaner = new HtmlCleaner();
    CleanerProperties props = cleaner.getProperties();
    //props.setXXX(...);
    TagNode node = cleaner.clean(content[0]);
    org.w3c.dom.Document document = new DomSerializer(props, true).createDOM(node);*//*
    WebEngine webEngine = webView.getEngine();
    webEngine.loadContent(content[0]);
    org.w3c.dom.Document document = webEngine.getDocument();
    correctLinks(document, "link", "href");
    correctLinks(document, "a", "href");
    correctLinks(document, "img", "src");
    correctLinks(document, "script", "src");
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer transformer = tf.newTransformer();
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    StringWriter writer = new StringWriter();
    transformer.transform(new DOMSource(document), new StreamResult(writer));
    content[0] = writer.getBuffer().toString()*//*.replaceAll("\n|\r", "")*//*;
    //System.out.println(content[0]);
} catch (Exception e) {
    e.printStackTrace();
}
```