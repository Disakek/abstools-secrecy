package org.abs_models.frontend.typechecker.ext;

import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

/**
 * Class that is used to handover the user input for the --secrecy option to the SecrecyAnnotationChecker
 */
public class SecrecyLatticeStructure {

    private final Set<String> secrecyLevels;
    
    private final HashMap<String, Set<String>> latticeOrder;
    
    private String maxSecrecyLevel = null;

    private String minSecrecyLevel = null;
    
    public SecrecyLatticeStructure(Set<String> levels, HashMap<String, Set<String>> order) {
        this.secrecyLevels = new HashSet<>(levels);
        this.latticeOrder = new HashMap<>(order);

        calculateMaxAndMin();
        System.out.println("Lowest: " + minSecrecyLevel + "\nHighest: " + maxSecrecyLevel);
    }

    public void calculateMaxAndMin() {

        //Calculate the maxSecrecyLevel
        for (String level : latticeOrder.keySet()) {
            if (latticeOrder.get(level).isEmpty()) {
                maxSecrecyLevel = level;
                break; // found it, exit loop
            }
        }

        //Calculate the minSecrecyLevel
        for(String secLevel : secrecyLevels) {
            boolean containsAllOthers = true;
            for(String otherLevels : secrecyLevels){
                Set<String> secLevelContains = latticeOrder.get(secLevel);
                if(!secLevel.equals(otherLevels) && latticeOrder.getOrDefault(otherLevels, Set.of()).contains(secLevel)) {
                    containsAllOthers = false;
                    break;
                }
            }

            if(containsAllOthers) {
                minSecrecyLevel = secLevel;
                break;
            }
        }
    }
    
    public String getMaxSecrecyLevel() {
        return maxSecrecyLevel;
    }

    public String getMinSecrecyLevel() {
        return minSecrecyLevel;
    }

    public Set<String> getSecrecyLevels() {
        return new HashSet<>(secrecyLevels);
    }
    
    public HashMap<String, Set<String>> getLatticeOrder() {
        return new HashMap<>(latticeOrder);
    }

    public Set<String> getSetForSecrecyLevel(String input) {
        return latticeOrder.get(input);
    }

    public boolean isValidLabel(String input) {
        return secrecyLevels.contains(input);
    }

    //TODO: more testing
    //issue if one of those is null so never use it as default value (shouldnt do that anyways)
    public String join(String secrecyOne, String secrecyTwo) {
        
        if(!secrecyLevels.contains(secrecyOne) || !secrecyLevels.contains(secrecyTwo)){
            throw new IllegalArgumentException("Non existing secrecy label found");
        }

        Set<String> setOfsecrecyOne = latticeOrder.get(secrecyOne);
        Set<String> setOfsecrecyTwo = latticeOrder.get(secrecyTwo);

        if(setOfsecrecyOne.contains(secrecyTwo))return secrecyTwo;

        if(setOfsecrecyTwo.contains(secrecyOne))return secrecyOne;

        if(secrecyOne.equals(secrecyTwo))return secrecyOne;

        Set<String> candidateLUB = new HashSet<>();

        for(String candidate : secrecyLevels) {
            if (setOfsecrecyOne.contains(candidate) && setOfsecrecyTwo.contains(candidate)) {
                candidateLUB.add(candidate);
            }
        }

        for (String candidate : candidateLUB) {

            Set<?> greaterOrEqual = latticeOrder.getOrDefault(candidate, Set.of());
            boolean isLeastUpperBound = true;

            for (String other : candidateLUB) {
                // Skip comparing with itself
                if (!candidate.equals(other) && greaterOrEqual.contains(other)) {
                    // Found a smaller 'other' under this candidate → not least
                    isLeastUpperBound = false;
                    break;
                }
            }

            if (isLeastUpperBound) {
                return candidate;
            }
        }


        throw new IllegalStateException("No common upper bound found");
    }

    //TODO: missing implementations for meet mby optional
}