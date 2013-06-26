package com.salesforce.deploy;
import java.io.IOException;public class MainGenerator { 
    public void run()  throws IOException{ 
        FileWriteable[] instances = new FileWriteable[4];
        instances[0] = new CodeCoverage();
        instances[1] = new Resume();
        instances[2] = new UnitTest();
        instances[3] = new Tabs();
        for(int i = 0; i < instances.length; i++ ) {
            instances[i].run();
        }
    }    
}