/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 * Written by @Maximilian_Paul for questions please refer to uukln@student.kit.edu
 */
package org.abs_models.frontend.typechecker.ext;

import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Class that is used to handover the user input for the --secrecy option to the SecrecyAnnotationChecker
 */
public class SecrecyLatticeStructure {

    /**
     * Set that contains all existing secrecylevels once. (default: Low, High)
     */
    private final Set<String> secrecyLevels;
    
    /**
     * Hashmap used to define the order between levels.
     * Each element of secrecyLevels has an entry.
     * A set of a lower element contains all higher elements but not itself (highest is an empty set).
     * (default: (Low, [High]), (High, []))
     */
    private final HashMap<String, Set<String>> latticeOrder;
    
    /**
     * Is the highest secrecyvalue for a lattice.
     */
    private String maxSecrecyLevel = null;

    /**
     * Is the lowest secrecyvalue for a lattice.
     */
    private String minSecrecyLevel = null;
    

    /**
     * Constructor for the SecrecyLatticeStructure.
     * @param levels - the set of all different existing levels in a lattice
     * @param order - the order defining structure
     */
    public SecrecyLatticeStructure(Set<String> levels, HashMap<String, Set<String>> order) {
        this.secrecyLevels = new HashSet<>(levels);
        this.latticeOrder = new HashMap<>(order);

        calculateMaxAndMin();
        //System.out.println("Lowest: " + minSecrecyLevel + "\nHighest: " + maxSecrecyLevel);
    }

    /**
     * This method takes the assigned secrecyLevels and the latticeOrder.
     * It calculates the values of the lattice for max and min and assigns them accordingly.
     * 
     * If there are multiple we take any of the highest/lowest. (There never should be multiple options!)
     * 
     */
    public void calculateMaxAndMin() {

        for (String level : latticeOrder.keySet()) {
            if (latticeOrder.get(level).isEmpty()) {
                maxSecrecyLevel = level;
                break;
            }
        }

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
    
    /**
     * Getter for the maximum secrecylevel of our lattice.
     * @return - the maximum secrecylevel.
     */
    public String getMaxSecrecyLevel() {
        return maxSecrecyLevel;
    }

    /**
     * Getter for the minimum secrecylevel of our lattice.
     * @return - the minimum secrecylevel.
     */
    public String getMinSecrecyLevel() {
        return minSecrecyLevel;
    }

    /**
     * Getter for all possible secrecylevel values.
     * @return - the secrecylevel field.
     */
    public Set<String> getSecrecyLevels() {
        return new HashSet<>(secrecyLevels);
    }
    
    /**
     * Getter for the latticeOrder.
     * @return - the latticeOrder field.
     */
    public HashMap<String, Set<String>> getLatticeOrder() {
        return new HashMap<>(latticeOrder);
    }

    /**
     * Getter for the set that contains levels above a certain secrecylevel.
     * @param input - the secrecylevel for which we want the set of higher levels.
     * @return - the set of higher levels.
     */
    public Set<String> getSetForSecrecyLevel(String input) {
        return latticeOrder.get(input);
    }

    /**
     * Checker for the existance of a lable.
     * @param input - the possibly existing secrecyvalue
     * @return - true if the input is an existing value (in secrecyLevels), false otherwise
     */
    public boolean isValidLabel(String input) {
        return secrecyLevels.contains(input);
    }

    //todo: requires more testing to ensure it works
    /**
     * This method is a join for two elements and returns the secrecyvalue which is equal or above both of them.
     * 
     * @param secrecyOne - the first element we want to join
     * @param secrecyTwo - the second element we want to join
     * 
     * @return - the join of the two elements so their least upper bound, "secrecyOne" if they are the same
     */
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
                if (!candidate.equals(other) && greaterOrEqual.contains(other)) {
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

    /**
     * Evaluates the secrecylevel given by a Linkedlist.
     * @param programConfidentiality - the linked list which gives the secrecylevel
     * @return - the join of all elements in the list so the least upper bound, error if it is empty or null
     */
    public String evaluateListLevel (LinkedList<ProgramCountNode> programConfidentiality) {

        if (programConfidentiality == null || programConfidentiality.isEmpty()) {
            throw new IllegalArgumentException("Cannot evaluate an empty confidentiality list.");
        }

        ProgramCountNode first = programConfidentiality.getFirst();
        String current = first.getSecrecyLevel();

        for (int i = 1; i < programConfidentiality.size(); i++) {
        
            ProgramCountNode nextNode = programConfidentiality.get(i);
            String nextLevel = nextNode.getSecrecyLevel();
            current = join(current, nextLevel);
        }

        return current;
    }
}
