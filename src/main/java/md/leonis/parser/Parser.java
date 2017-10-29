package md.leonis.parser;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import md.leonis.parser.domain.Confidence;
import md.leonis.parser.domain.PageEncoding;
import md.leonis.parser.domain.token.Token;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
@Setter
public class Parser {

    private String encoding;
    private PageEncoding pageEncoding;
    private byte[] html;
    private String htmlString;

    private List<Token> tokens;

    Parser(byte[] html, String encoding) {
        this.html = html;
        this.encoding = encoding;
        tokens = new ArrayList<>();
    }

    public String parse() {
        //TODO check size
        // Determining the character encoding
        if (encoding == null) {
            pageEncoding = EncodingIdentifier.detectCharset(html);
        } else {
            pageEncoding = new PageEncoding(encoding, Confidence.CERTAIN);
        }
        // Decode input stream
        ByteStreamDecoder decoder = new ByteStreamDecoder(html, pageEncoding);
        htmlString = decoder.decode();
        // Preprocessing the input stream
        InputStreamPreprocessor preprocessor = new InputStreamPreprocessor(htmlString);
        htmlString = preprocessor.process();

        // Tokenization
        Tokenizer tokenizer = new Tokenizer(htmlString);
        tokens = tokenizer.tokenize();
        tokens.forEach(System.out::println);

        //TODO Tree construction
        TreeConstructor treeConstructor = new TreeConstructor(tokens);
        Document document = treeConstructor.constructDomTree();
        System.out.println(document);
        return "";
    }

}
