import org.example.searcher.DocSearcher;
import org.example.searcher.Weight;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestMergeWeightLists {
    public static void main(String[] args) {
        List<List<Weight>> lists = new ArrayList<>();
        lists.add(Arrays.asList(weight(1, 90), weight(2, 40), weight(3, 10)));
        lists.add(Arrays.asList(weight(4, 80), weight(5, 60), weight(6, 20)));
        lists.add(Arrays.asList(weight(7, 100), weight(8, 70), weight(9, 30)));

        List<Weight> result = DocSearcher.mergeWeightLists(lists);

        int lastWeight = Integer.MAX_VALUE;
        for (Weight weight : result) {
            if (weight.getWeight() > lastWeight) {
                throw new RuntimeException("多路归并结果不是降序");
            }
            lastWeight = weight.getWeight();
            System.out.println("docId=" + weight.getDocId() + ", weight=" + weight.getWeight());
        }
        System.out.println("多路归并验证通过");
    }

    private static Weight weight(int docId, int value) {
        Weight weight = new Weight();
        weight.setDocId(docId);
        weight.setWeight(value);
        return weight;
    }
}
