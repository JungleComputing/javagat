/**********************************************************
 * 
 * @file: examples/Initialization.java
 * 
 * @description: 
 *   example number 1: How to initialize GAT.
 *   
 * Copyright (C) GridLab Project (http://www.gridlab.org/)
 * 
 * Contributed by Marco de Reus   <mjfdreus@cs.vu.nl>.
 * 
 **********************************************************/

/*** LICENSE ***/

/*******************************************************************************
 * This program is the most simple GAT program: it creates a GATContext object,
 * and destroys it.
 ******************************************************************************/

package examples;


public class Initialization {

    public static void main(String[] args) {

        try {
            System.out.println("I am the most simple GAT program!");
        } catch (Exception x) {
            System.err.println("error: " + x);
            x.printStackTrace();
        }
    }
}