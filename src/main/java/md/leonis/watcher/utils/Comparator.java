package md.leonis.watcher.utils;

import md.leonis.watcher.domain.DiffStatus;
import md.leonis.watcher.domain.TextDiff;

import java.util.List;

public class Comparator {

    public static boolean compare(List<TextDiff> textList) {
        long count = textList.stream().filter(t -> t.getStatus().equals(DiffStatus.SAME)).count();
        return (count == textList.size());
    }
}
