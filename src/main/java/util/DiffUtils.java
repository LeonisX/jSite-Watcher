package util;

import domain.DiffStatus;
import domain.TextDiff;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DiffUtils {

    private static final int DIVIDER = 2;

    //TODO refactor, tests
    // http://introcs.cs.princeton.edu/java/23recursion/Diff.java.html
    public static List<TextDiff> diff(Map<Integer, String> leftMap, Map<Integer, String> rightMap) {
        List<TextDiff> textDiffs = new ArrayList<>();

        // opt[i][j] = length of LCS (longest common subsequence)
        int[][] opt = new int[leftMap.size() + 1][rightMap.size() + 1];

        // compute length of LCS and all subproblems via dynamic programming
        for (int i = leftMap.size() - 1; i >= 0; i--) {
            for (int j = rightMap.size() - 1; j >= 0; j--) {
                if (leftMap.get(i).equals(rightMap.get(j)) || isTextChanged(leftMap.get(i), rightMap.get(j))) {
                    opt[i][j] = opt[i + 1][j + 1] + 1;
                } else {
                    opt[i][j] = Math.max(opt[i + 1][j], opt[i][j + 1]);
                }
            }
        }

        // recover LCS itself and print out non-matching lines to standard output
        int i = 0, j = 0;
        while (i < leftMap.size() && j < rightMap.size()) {
            Integer levenshteinDistance = levenshteinDistance(leftMap.get(i), rightMap.get(j));
            if (levenshteinDistance == 0) {
                System.out.println("* " + leftMap.get(i));
                textDiffs.add(new TextDiff(i, leftMap.get(i), j, rightMap.get(j), DiffStatus.SAME));
                i++;
                j++;
            } else if (levenshteinDistance > 0) {
                System.out.println(leftMap.get(i));
                textDiffs.add(new TextDiff(i, leftMap.get(i), j, rightMap.get(j), DiffStatus.CHANGED));
                i++;
                j++;
            } else if (opt[i + 1][j] >= opt[i][j + 1]) {
                textDiffs.add(new TextDiff(i, leftMap.get(i), null, "", DiffStatus.DELETED));
                System.out.println("< " + leftMap.get(i));
                i++;
            } else {
                textDiffs.add(new TextDiff(null, "", j, rightMap.get(j), DiffStatus.ADDED));
                System.out.println("> " + rightMap.get(j));
                j++;
            }
        }

        // dump out one remainder of one string if the other is exhausted
        while (i < leftMap.size() || j < rightMap.size()) {
            if (i == leftMap.size()) {
                textDiffs.add(new TextDiff(null, "", j, rightMap.get(j), DiffStatus.ADDED));
                System.out.println("> " + rightMap.get(j));
                j++;
            } else if (j == rightMap.size()) {
                textDiffs.add(new TextDiff(i, leftMap.get(i), null, "", DiffStatus.DELETED));
                System.out.println("< " + leftMap.get(i));
                i++;
            }
        }

        textDiffs.forEach(System.out::println);

        System.out.println("");

        return textDiffs;
    }

    private static boolean isTextChanged(String left, String right) {
        return (levenshteinDistance(left, right) != -1);
    }

    private static Integer levenshteinDistance(String left, String right) {
        int threshold = left.length() / DIVIDER;
        LevenshteinDistance levenshteinDistance = new LevenshteinDistance(threshold);
        return (levenshteinDistance.apply(left, right));
    }



/*    private static List<TextDiff> correctChangedText(List<TextDiff> textDiffs) {
        if (textDiffs.size() >= 2) {
            List<TextDiff> result = new ArrayList<>();
            int idx = 0;
            while (idx < textDiffs.size() - 1) {
                TextDiff textDiff1 = textDiffs.get(idx);
                TextDiff textDiff2 = textDiffs.get(idx + 1);
                if (isTextDiffChanged(textDiff1, textDiff2)) {
                    result.add(new TextDiff(textDiff1.getLeftIndex(), textDiff1.getLeftText(),
                            textDiff2.getRightIndex(), textDiff2.getRightText()));
                    idx += 2;
                } else {
                    result.add(textDiff1);
                    idx++;
                }
            }
            return result;
        } else {
            return textDiffs;
        }
    }*/

    /*private static boolean isTextDiffChanged(TextDiff textDiff1, TextDiff textDiff2) {
        return textDiff1.getLeftIndex() != null && textDiff1.getRightIndex() == null
                && textDiff2.getLeftIndex() == null && textDiff2.getRightIndex() != null
                && isTextChanged(textDiff1.getLeftText(), textDiff2.getRightText());
    }*/

}
