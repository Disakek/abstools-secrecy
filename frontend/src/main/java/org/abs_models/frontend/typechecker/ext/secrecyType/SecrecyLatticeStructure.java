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
    
    public SecrecyLatticeStructure(Set<String> levels, HashMap<String, Set<String>> order) {
        this.secrecyLevels = new HashSet<>(levels);
        this.latticeOrder = new HashMap<>(order);
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

    //TODO: implemented but not tested yet
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

        for(String candidate : candidateLUB) {
            if(candidateLUB.stream().noneMatch(other -> 
                !candidate.equals(other) && 
                latticeOrder.getOrDefault(candidate, Set.of()).contains(other))) {
                return candidate;
            }
        }

        throw new IllegalStateException("No common upper bound found");
    }

    //TODO: missing implementations for meet, lowest element and highest element (mby optional) 
}