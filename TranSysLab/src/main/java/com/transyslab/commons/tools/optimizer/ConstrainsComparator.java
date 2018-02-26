package com.transyslab.commons.tools.optimizer;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.comparator.ConstraintViolationComparator;

public class ConstrainsComparator<S extends Solution<?>> implements ConstraintViolationComparator<S> {
    @Override
    public int compare(S s1, S s2) {

        double value1 = (double)s1.getAttribute("GEH");
        double value2 = (double)s2.getAttribute("GEH");
        if(value1 == value2){
            return 1;
        }
        if(value1<5){
            if(value2<value1){
                // v2<v1<5
                return 0;
            }
            else{
                if(value2>=5){
                    //v2>5,v1<5
                    return 1;
                }
                else{
                    //v1<v2<5
                    return 0;
                }

            }
        }
        else{
            if(value2<5){
                //v2<5,v1>=5
                return -1;
            }
            else{
                if(value1>value2){
                    // v1>v2>=5
                    return -1;
                }
                else{
                    //v2>v1>=5
                    return 1;
                }
            }
        }
    }
}
