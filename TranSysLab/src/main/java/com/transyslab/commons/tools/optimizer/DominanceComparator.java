package com.transyslab.commons.tools.optimizer;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.comparator.ConstraintViolationComparator;
import org.uma.jmetal.util.comparator.impl.OverallConstraintViolationComparator;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by yali on 2017/11/28.
 * 无约束优化问题的支配解比较器
 * 允许约束违反的比较器为null
 */
public class DominanceComparator<S extends Solution<?>> implements Comparator<S>, Serializable {
	private ConstraintViolationComparator<S> constraintViolationComparator;
	private double boundedValue;
	private int slackObjIndex = -1;

	/** Constructor */
	public DominanceComparator() {
		this(new OverallConstraintViolationComparator<S>(), 0.0) ;
	}

	/** Constructor */
	public DominanceComparator(double epsilon) {
		this(new OverallConstraintViolationComparator<S>(), epsilon) ;
	}

	/** Constructor */
	public DominanceComparator(ConstraintViolationComparator<S> constraintComparator) {
		this(constraintComparator, 0.0) ;
	}

	/** Constructor */
	public DominanceComparator(ConstraintViolationComparator<S> constraintComparator, double epsilon) {
		constraintViolationComparator = constraintComparator ;
	}
	/**判断约束条件**/
	public DominanceComparator(int slackObjIndex,double boundedValue) {
		this(new OverallConstraintViolationComparator<S>(), 0.0) ;
		this.boundedValue = boundedValue;
		this.slackObjIndex = slackObjIndex;
	}
	/**
	 * Compares two solutions.
	 *
	 * @param solution1 Object representing the first <code>Solution</code>.
	 * @param solution2 Object representing the second <code>Solution</code>.
	 * @return -1, or 0, or 1 if solution1 dominates solution2, both are
	 * non-dominated, or solution1  is dominated by solution2, respectively.
	 */
	@Override
	public int compare(S solution1, S solution2) {
		if (solution1 == null) {
			throw new JMetalException("Solution1 is null") ;
		} else if (solution2 == null) {
			throw new JMetalException("Solution2 is null") ;
		} else if (solution1.getNumberOfObjectives() != solution2.getNumberOfObjectives()) {
			throw new JMetalException("Cannot compare because solution1 has " +
					solution1.getNumberOfObjectives()+ " objectives and solution2 has " +
					solution2.getNumberOfObjectives()) ;
		}
		int result ;
		if(constraintViolationComparator!=null)
			result = constraintViolationComparator.compare(solution1, solution2) ;
		else
			result = 0;
		if (result == 0) {
			result = dominanceTest(solution1, solution2) ;
		}

		return result ;
	}

	private int dominanceTest(S solution1, S solution2) {
		int bestIsOne = 0 ;
		int bestIsTwo = 0 ;
		int result ;
		for (int i = 0; i < solution1.getNumberOfObjectives(); i++) {
			// 目标函数值小于boundedValue，则不进行比较
			if(slackObjIndex == i && solution1.getObjective(i)<=boundedValue && solution2.getObjective(i)<=boundedValue){
				continue;
			}
			double value1 = solution1.getObjective(i);
			double value2 = solution2.getObjective(i);
			if (value1 != value2) {
				if (value1 < value2) {
					bestIsOne = 1;
				}
				if (value2 < value1) {
					bestIsTwo = 1;
				}
			}


		}
		if (bestIsOne > bestIsTwo) {
			result = -1;
		} else if (bestIsTwo > bestIsOne) {
			result = 1;
		} else {
			result = 0;
		}
		return result ;
	}
}
