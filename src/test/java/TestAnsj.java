import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;

import java.util.List;

public class TestAnsj {
    public static void main(String[] args) {
        String s_test = "我是小明, 在育苗小学上学!";

        List<Term> terms = ToAnalysis.parse(s_test).getTerms();
        for(Term term : terms){
            System.out.println(term.getName());
        }
    }
}
