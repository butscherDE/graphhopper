package com.graphhopper.storage.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class RepititionFinder {
    public static boolean isRepitition(final List<Integer> nodesOnCell, final int repititionThreshold) {
        final ArrayList<Integer> contraNodesOnCell = new ArrayList<>(nodesOnCell);
        Collections.sort(contraNodesOnCell);

        int currentElement = contraNodesOnCell.get(0);
        int c = 0;
        for (Integer integer : contraNodesOnCell) {
            if (integer == currentElement) {
                c++;
                if (c == repititionThreshold) {
                    return true;
                }
            } else {
                currentElement = integer;
                c = 1;
            }
        }

//        final LinkedList<Integer> contraNodesOnCell = new LinkedList<>(nodesOnCell);
//
//
//        int i = 0;
//        for (Integer element : nodesOnCell) {
//            int c = 0;
//            contraNodesOnCell.removeFirst();
//            for (Integer contraElement : nodesOnCell) {
//                if (element == contraElement) {
//                    c++;
//                    if (c == repititionThreshold) {
//                        return true;
//                    }
//                }
//            }
//            if (i % 10000 == 0) {
//                System.out.println(i);
//            }
//            i++;
//        }


        return false;
    }
}
