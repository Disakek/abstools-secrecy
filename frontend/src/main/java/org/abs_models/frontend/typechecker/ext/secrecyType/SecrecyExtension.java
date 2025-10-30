package org.abs_models.frontend.typechecker.ext;

import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

/**
 * Class that is used to handover the user input for the --secrecy option to the SecrecyAnnotationChecker
 */
public class SecrecyExtension {
    private final Set<String> secrecyLevels;
    private final HashMap<String, Set<String>> latticeOrder;
    
    public SecrecyExtension(Set<String> levels, HashMap<String, Set<String>> order) {
        this.secrecyLevels = new HashSet<>(levels);
        this.latticeOrder = new HashMap<>(order);
    }
    
    public Set<String> getSecrecyLevels() {
        return new HashSet<>(secrecyLevels);
    }
    
    public HashMap<String, Set<String>> getLatticeOrder() {
        return new HashMap<>(latticeOrder);
    }
}