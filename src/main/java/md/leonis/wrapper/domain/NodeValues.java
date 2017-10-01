package md.leonis.wrapper.domain;

import lombok.Getter;
import lombok.Setter;
import org.w3c.dom.html.HTMLTitleElement;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
public class NodeValues {

    private String name;

    private Map<String, Object> values;

    public NodeValues(String name) {
        this.name = name;
        values = new LinkedHashMap<>();
    }

    public Map<String, Object> getFilteredValues() {
        return values.entrySet()
                .stream()
                .filter(a -> a.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public void print() {
        System.out.println(">>> " + name);
        values.forEach((key, value) -> {
            System.out.print(String.format("%-15s", key));
            System.out.println(value);
        });
        System.out.println("===============================");
        System.out.println();
    }
}
