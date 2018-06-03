package com.johnhite.recipe.cli;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;


import jersey.repackaged.com.google.common.collect.Lists;

public class RecipeSolver {

	
	/*private void ks2(Weights[][] weightTable, List<Weights> capacities, Map<Weights, Integer> capacityIndices, List<Integer> sortedRequestedRecipes, Map<Integer, Weights> recipeWeights, int weightLength) {
		Weights zero = new Weights(new int[weightLength]);
		for (int i = 0; i < capacities.size(); i++) {
			Weights c = capacities.get(i);
			for (int j = 0; j < sortedRequestedRecipes.size(); j++) {
				Weights r = recipeWeights.get(sortedRequestedRecipes.get(j));
				Weights diff = c.subtract(r);
				if (diff.hasNegativeComponent()) {
					//can't take Recipe
					weightTable[i][j] = (j < 1) ? zero : weightTable[i][j-1];
				} 
				else{
					Weights a = (j < 1) ? zero : weightTable[i][j-1];
					Weights b = (j < 1) ? r : r.add(weightTable[capacityIndices.get(diff)][j-1]);
					Weights max = Weights.max(a, b);
					weightTable[i][j] = max;
				}
			}
		}
	}
	
	private List<List<Integer>> ksTrace(Weights[][] weightTable, List<Weights> capacities, Map<Weights, Integer> capacityIndices, List<Integer> sortedRequestedRecipes, Map<Integer, Weights> recipeWeights, int weightLength) {
		Set<Integer> solutionJ = Sets.newHashSet();
		Weights target = capacities.get(capacities.size()-1);
		for (int j =0; j < weightTable[capacities.size()-1].length; j++) {
			if (target.subtract(weightTable[capacities.size()-1][j]).isZero()) {
				solutionJ.add(j);
			}
		}
		
		List<List<Integer>> solutions = Lists.newArrayList();
		
		for (Integer maxJ : solutionJ) {
			int currenti = capacities.size()-1;
			Weights currentTarget = target;
			List<Integer> solution = Lists.newArrayList();
			for (int j = maxJ; j >= 0; j--) {
				if ( j > 0 && weightTable[currenti][j].equals(weightTable[currenti][j-1])) {
					//if equal, current item did not contribute
				}
				else {
					//current item did contribute. add it to the solution
					solution.add(sortedRequestedRecipes.get(j));
					//then find remaining capacity and move i and j
					Weights r = recipeWeights.get(sortedRequestedRecipes.get(j));
					currentTarget = currentTarget.subtract(r);
					currenti = capacityIndices.get(currentTarget);
				}
			}
			solutions.add(solution);
		}
		
		return solutions;
	}*/
}
