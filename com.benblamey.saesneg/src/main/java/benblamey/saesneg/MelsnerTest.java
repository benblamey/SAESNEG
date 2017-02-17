/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package benblamey.saesneg;

import com.benblamey.core.ProcessUtilities;

public class MelsnerTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String[] cmds = {
            //"C:\\cygwin64\\bin\\mintty.exe",
            //"-e",
            //"/cygdrive/c/work/data/runMelsner.sh"
            
            
            "C:/work/code/3rd_Ben/correlation-distr/bin64/chainedSolvers.exe", "log", "vote", "boem"
                , "C:/work/data/output/clustering.matrix"
                //+ " >\"\"/cygdrive/c/work/data/output/clustering.output\"\"\""
                //+ " 2>\"\"/cygdrive/c/work/data/output/clustering.err\"\"\""
        };
                 
        String output = ProcessUtilities.runAndReturnOutput(cmds);
        
        System.out.println(output);
        
                
                
                
                
    }

}
