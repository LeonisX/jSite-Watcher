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
        Map<String, Object> filteredValues = new LinkedHashMap<>();
        values.entrySet()
                .stream()
                .filter(a -> a.getValue() != null)
                .forEach(e -> filteredValues.put(e.getKey(), e.getValue()));
        return filteredValues;
    }

    public void print() {
        Map<String, Object> filteredValues = getFilteredValues();
        if (!filteredValues.isEmpty()) {
            System.out.println("  " + name);
            filteredValues.forEach((key, value) -> {
                System.out.print(String.format("    %-15s", key));
                System.out.println(value);
            });
        }
    }
}
