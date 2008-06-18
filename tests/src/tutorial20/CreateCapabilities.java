package tutorial20;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.gridlab.gat.AdaptorInfo;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATInvocationException;

public class CreateCapabilities {

    /**
     * @param args
     * @throws IOException
     * @throws GATInvocationException
     */
    public static void main(String[] args) throws IOException,
            GATInvocationException {
        for (String gatObjectType : GAT.getAdaptorTypes()) {
            File htmlFile = new File("doc" + File.separator + gatObjectType
                    + "-capabilities.html");
            if (!htmlFile.exists()) {
                htmlFile.createNewFile();
            }
            FileOutputStream out = new FileOutputStream(htmlFile);
            out.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\">\n"
                    .getBytes());
            out.write("<html>\n".getBytes());
            out.write("<head>\n".getBytes());
            out
                    .write(("<title>JavaGAT capabilities: " + gatObjectType + "</title>\n")
                            .getBytes());
            out.write(("<script>\n" + "function showhide(id){\n"
                    + "\tif (document.getElementById){\n"
                    + "\t\tobj = document.getElementById(id);\n"
                    + "\t\tif (obj.style.display == \"none\"){\n"
                    + "\t\t\tobj.style.display = \"\";\n" + "\t\t} else {\n"
                    + "\t\t\tobj.style.display = \"none\";\n" + "\t\t}\n"
                    + "\t}\n" + "}\n" + "</script>\n").getBytes());

            out.write("</head>\n".getBytes());
            out.write("<body>\n".getBytes());
            out.write("<table frame=box cellpadding=5 cellspacing=0>\n"
                    .getBytes());
            out.write("<tr>\n".getBytes());
            out.write("<td></td>\n".getBytes());
            Set<String> methods = new HashSet<String>();
            AdaptorInfo[] adaptorInfos = GAT.getAdaptors(gatObjectType);
            for (AdaptorInfo adaptorInfo : adaptorInfos) {
                out
                        .write(("<td>"
                                + adaptorInfo.getShortName().substring(0, 5) + "</td>\n")
                                .getBytes());
                if (adaptorInfo.getSupportedCapabilities() != null) {
                    methods.addAll(adaptorInfo.getSupportedCapabilities()
                            .keySet());
                }
            }
            out.write("</tr>\n".getBytes());
            for (String method : methods) {
                out.write("<tr>\n".getBytes());
                out.write(("<td>" + method + "</td>\n").getBytes());
                for (AdaptorInfo adaptorInfo : adaptorInfos) {
                    if (!adaptorInfo.getSupportedCapabilities().containsKey(
                            method)) {
                        out.write(("<td>?</td>\n").getBytes());
                    } else if (adaptorInfo.getSupportedCapabilities().get(
                            method)) {
                        out
                                .write(("<td><font color=#00FF00><b>V</b></font></td>\n")
                                        .getBytes());
                    } else {
                        out
                                .write(("<td><font color=#FF0000><b>X</b></font></td>\n")
                                        .getBytes());
                    }
                }
                out.write("</tr>\n".getBytes());
            }
            out.write("<tr>\n".getBytes());
            out.write("<td>total</td>\n".getBytes());
            for (AdaptorInfo adaptorInfo : adaptorInfos) {
                int i = 0;
                if (adaptorInfo.getSupportedCapabilities() != null) {
                    for (String key : adaptorInfo.getSupportedCapabilities()
                            .keySet()) {
                        if (adaptorInfo.getSupportedCapabilities().get(key)) {
                            i++;
                        }
                    }
                    out
                            .write(("<td>"
                                    + (i * 100)
                                    / adaptorInfo.getSupportedCapabilities()
                                            .size() + " %</td>\n").getBytes());
                } else {
                    out.write("<td>no info</td>\n".getBytes());
                }
            }
            out.write("</tr>\n".getBytes());

            out.write("</table>\n".getBytes());
            out.write("</body>\n".getBytes());
            out.write("</html>\n".getBytes());
        }
    }

}
