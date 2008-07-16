/*
 * Created on Aug 1, 2005 by rob
 */
package org.gridlab.gat.engine;

import java.util.List;
import java.util.Map;

/**
 * @author rob
 * 
 */
interface AdaptorOrderPolicy {
    void order(Map<String, List<Adaptor>> adaptors);
}
