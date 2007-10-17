package file;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;

public class RemoteCopy {
    public static void main(String[] args) throws Exception {
    	GATContext context = new GATContext();

        context.addPreference("File.adaptor.name", "ssh");
        
        URI src = new URI(args[0]);
        URI dest = new URI(args[1]);
        File file = GAT.createFile(context, src);
        file.copy(dest);
        /*
        System.out.println("absolute path: " + file.getAbsolutePath());
        System.out.println("isdir:         " + file.isDirectory());
        System.out.println("isfile:        " + file.isFile());
        if (file.isDirectory()) {
        	System.out.println("exists:        " + file.exists());
        	if (file.exists()) {
        		System.out.print("ls:            ");
        		String[] list = file.list();
        		for (int i = 0; i < list.length; i++) {
        			System.out.print(list[i] + ", ");
        		}
        		System.out.println();
        		System.out.println("can read:      " + file.canRead());
                System.out.println("can write:     " + file.canWrite());
        	} else {
        		System.out.println("mkdir:         " + file.mkdir());
        		System.out.println("exists:        " + file.exists());
        		System.out.println("can read:      " + file.canRead());
                System.out.println("can write:     " + file.canWrite());
        		System.out.println("delete:        " + file.delete());
        		System.out.println("exists:        " + file.exists());
        	}
        } else {
        	System.out.println("exists:        " + file.exists());
        	if (file.exists()) {
        		System.out.println("can read:      " + file.canRead());
                System.out.println("can write:     " + file.canWrite());
        		System.out.println("copy:          " + dest.toString());
                file.copy(dest);
        	} else {        		
        		System.out.println("create file:   " + file.createNewFile());
        		System.out.println("exists:        " + file.exists());
        		System.out.println("can read:      " + file.canRead());
                System.out.println("can write:     " + file.canWrite());
        		System.out.println("delete:        " + file.delete());
        		System.out.println("exists:        " + file.exists());
        	}
        }*/
        GAT.end();
        

    }
}
