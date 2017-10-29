package md.leonis.parser;

import lombok.AllArgsConstructor;
import md.leonis.parser.domain.PageEncoding;

import java.io.UnsupportedEncodingException;

@AllArgsConstructor
class ByteStreamDecoder {

    private byte[] html;
    private PageEncoding pageEncoding;

    String decode() {
        try {
            //TODO move hack to separate class
            if (pageEncoding.getEncoding().isEmpty()) {
                pageEncoding.setEncoding("utf8");
            }
            return new String(html, pageEncoding.getEncoding());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
