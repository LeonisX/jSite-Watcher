package md.leonis.watcher.domain;

public class TextDiff {

    private Integer leftIndex;
    private String leftText;

    private Integer rightIndex;
    private String rightText;

    private DiffStatus status;

    public TextDiff() {
    }

    public TextDiff(Integer leftIndex, String leftText, Integer rightIndex, String rightText, DiffStatus status) {
        this.leftIndex = leftIndex;
        this.leftText = leftText;
        this.rightIndex = rightIndex;
        this.rightText = rightText;
        this.status = status;
    }

//    public TextDiff(Integer leftIndex, String leftText, Integer rightIndex, String rightText) {
//        this.leftIndex = leftIndex;
//        this.leftText = leftText;
//        this.rightIndex = rightIndex;
//        this.rightText = rightText;
//        if (leftIndex == null) {
//            status = DiffStatus.ADDED;
//        } else if (rightIndex == null) {
//            status = DiffStatus.DELETED;
//        } else if (leftText.equals(rightText)) {
//            status = DiffStatus.SAME;
//        } else {
//            status = DiffStatus.CHANGED;
//        }
//    }

    public Integer getLeftIndex() {
        return leftIndex;
    }

    public String getLeftText() {
        return leftText;
    }

    public Integer getRightIndex() {
        return rightIndex;
    }

    public String getRightText() {
        return rightText;
    }

    public DiffStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "TextDiff{" +
                "leftIndex=" + leftIndex +
                ", leftText='" + leftText + '\'' +
                ", rightIndex=" + rightIndex +
                ", rightText='" + rightText + '\'' +
                ", status=" + status +
                '}';
    }
}