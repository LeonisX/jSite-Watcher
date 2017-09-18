package md.leonis.watcher;

import de.danielbechler.diff.ObjectDiffer;
import de.danielbechler.diff.ObjectDifferBuilder;
import de.danielbechler.diff.node.PrintingVisitor;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toMap;

public class MainDiffExperiments {

    private static final ClassLoader classLoader = MainDiffExperiments.class.getClassLoader();

    public static void main(String[] args) throws IOException {

        File originalFile = new File(classLoader.getResource("originalFile.txt").getFile());
        File revisedFile = new File(classLoader.getResource("revisedFile.txt").getFile());
        FileComparator comparator = new FileComparator(originalFile, revisedFile);

        System.out.println("ch: " + comparator.getChangesFromOriginal());
        System.out.println("de: " + comparator.getDeletesFromOriginal());
        System.out.println("in: " + comparator.getInsertsFromOriginal());

        ///////////////////////////////////

        List<String> left = Arrays.asList("aaa", "bbb", "ccc", "dfdfdf");
        List<String> right = Arrays.asList("111", "aaa", "ccc", "bbb");
        Map<Integer, String> leftMap = IntStream.range(0, left.size())
                .boxed()
                .collect(toMap(idx -> idx, left::get));
        Map<Integer, String> rightMap = IntStream.range(0, right.size())
                .boxed()
                .collect(toMap(idx -> idx, right::get));
        ObjectDiffer objectDiffer = ObjectDifferBuilder.buildDefault();
        final de.danielbechler.diff.node.DiffNode root = objectDiffer.compare(rightMap, leftMap);
        System.out.println(root);
        root.visit(new PrintingVisitor(rightMap, leftMap));

    }

}