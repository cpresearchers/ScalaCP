package cpscala.XModel;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
//import java.lang.Runtime;

//import java.io.FileWriter;

public class Model {
    public static long filterSum = 0, updateSum = 0;
    public static int node = 0;
    public static long timeUpBound = 600000;
    public static String satisfaction = "--";

    public Relation rs[] = null;
    int rnum = 0;

    Domain ds[] = null;
    int dnum = 0;

    Variable vs[] = null;
    int vnum = 0;

    Constraint cs[] = null;
    int cnum = 0;

    int maxd = 0;

    int maxr = 0;

    boolean short_state = false;

    Relation rcur[] = null;
    int rnumcur = 0;
    //Nodebox inrcur=new Nodebox();

    int RN = 0;

    public Model(String name) {
        try {
            File f = new File(name);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(f);


            NodeList cl = doc.getElementsByTagName("constraint");
            cnum = cl.getLength();
            cs = new Constraint[cnum];

            ////////////////////////////////////////////////////////////
            NodeList dl = doc.getElementsByTagName("domain");
            dnum = dl.getLength();
            ds = new Domain[dnum];


            for (int i = 0; i < dnum; i++) {
                String d = dl.item(i).getTextContent();
                int num = Integer.parseInt(dl.item(i).getAttributes().item(1).getNodeValue());

                String ss = dl.item(i).getAttributes().item(0).getNodeValue();

                if (num > maxd) {
                    maxd = num;
                }

                ds[i] = new Domain(num, d, i, ss);
                // System.out.println(num);
            }

            ///////////////////////////////////////////////////////////

            NodeList vl = doc.getElementsByTagName("variable");
            vnum = vl.getLength();
            vs = new Variable[vnum];

            for (int i = 0; i < vnum; i++) {
                String ss = vl.item(i).getAttributes().item(0).getNodeValue();
                String name1 = vl.item(i).getAttributes().item(1).getNodeValue();
                int d = 0;

                for (; d < dnum; d++) {
                    if (ds[d].name.equals(ss)) {
                        break;
                    }
                }

                vs[i] = new Variable(ds[d], i, cnum, name1);
            }


            //////////////////////////////////////////////////

            NodeList rl = doc.getElementsByTagName("relation");
            rnum = rl.getLength();
            rs = new Relation[rnum];

            for (int i = 0; i < rnum; i++) {
                String text = rl.item(i).getTextContent();
                String intype = rl.item(i).getAttributes().item(3).getNodeValue();
                int inrnum = Integer.parseInt(rl.item(i).getAttributes().item(2).getNodeValue());
                int invnum = Integer.parseInt(rl.item(i).getAttributes().item(0).getNodeValue());

                String name1 = rl.item(i).getAttributes().item(1).getNodeValue();

                rs[i] = new Relation(inrnum, invnum, text, intype, i);
               // System.out.println(inrnum + "  " + invnum + "    " + name1 + "    " + i);
                rs[i].name = name1;
                //System.out.println(text);
            }

            //System.out.println(text);


            ////////////////////////////////////////////////////////////
            rcur = new Relation[cnum];

            for (int i = 0; i < cnum; i++) {
                int invnum = Integer.parseInt(cl.item(i).getAttributes().getNamedItem("arity").getNodeValue());
                String cname = cl.item(i).getAttributes().getNamedItem("name").getNodeValue();

                String rname = cl.item(i).getAttributes().getNamedItem("reference").getNodeValue();
                int inr = 0;

                for (; inr < rnum; inr++) {
                    if (rs[inr].name.equals(rname)) {
                        break;
                    }
                }


                String s[] = cl.item(i).getAttributes().getNamedItem("scope").getNodeValue().split(" ");
                Variable invs[] = new Variable[invnum];

                for (int j = 0; j < invnum; j++) {
                    int v = 0;

                    for (; v < vnum; v++) {
                        if (s[j].equals(vs[v].name)) {
                            break;
                        }
                    }

                    invs[j] = vs[v];
                }


                cs[i] = new Constraint(invnum, rs[inr], invs, i,cname, this);

                if (cs[i].rnum > maxr) {
                    maxr = cs[i].rnum;
                }

                //System.out.println(invs[2]);
            }

            /////////////////////////////////////////////////////////////
           /* rs = new Relation[rnumcur];
            for (int i = 0; i < rnumcur; i++) {
                rs[i] = rcur[i];
            }
            rnum = rnumcur;*/

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("aaa" + e.getMessage());
        }
    }

//    public static void main(String[] args) {
////        String name = "E:/Projects/benchmarks/queen/queens-12_ext.xml";
////        String name = "/home/leezear/benchmarks/zzdubois/dubois-20_ext.xml";
////        String name = "E:/benckmarks/optiSTRpackage/rand-8-20-5_total20/rand-8-20-5-18-800-0_ext.xml";
////        String name = "E:/benckmarks/optiSTRpackage/rand-10-60-20_600s32/rand-10-60-20-30-p51200-0_ext_glb.xml";
//////        String name = "E:/benckmarks/optiSTRpackage/rand-3-20-20_total50/rand-3-20-20-60-632-4_ext.xml";
//////        String name = "/home/leezear/optiSTRpackage/cril_600s2/cril-5_ext.xml";
////        Model mm = new Model(name);
////        DATArv data = new DATArv(mm);
////        X2ToX3 xx = new X2ToX3(data);
//
////        ProblemAPI api = Compiler.buildDocument(args);
//        String[] as = {"X2ToX3", "-data=0"};
//        Compiler.main(as);
//
////        node = 0;
//////        STR2 str = new STR2(data);
//////        CompactTable3 str = new CompactTable3(data);
////        CompactTable2 str = new CompactTable2(data);
////        long time1 = System.currentTimeMillis();
////        str.MAC();
////        long time2 = System.currentTimeMillis();
////        long result = time2 - time1;
////        str = null;
////        data = null;
////        mm = null;
////        System.out.println("node: " + node);
////        System.out.println("runTime: " + result);
//
////        Model mm = new Model(name);
////        DATA data = new DATA(mm);
////        node = 0;
////        STR2_star str = new STR2_star(data, mm);
////        long time1 = System.currentTimeMillis();
////        str.MAC();
////        long time2 = System.currentTimeMillis();
////        long result = time2 - time1;
////        str = null;
////        data = null;
////        mm = null;
////        System.out.println("node: " + node);
////        System.out.println("runTime: " + result);
//    }


//    public static void StrStarFolderTest
//
//    {
//        try {
//            File file = new File("f:/optiSTRpackage");
//            File[] ff = file.listFiles();
//            for (int ii = 0; ii < ff.length; ii++) {
//                if (ff[ii].isDirectory()) {
//                    String path = ff[ii].getPath();
//                    //System.out.println(path);
//                    File ffile = new File(path);
//                    String ssname[] = ffile.list();
//                    WritableWorkbook book = Workbook.createWorkbook(new File("e:/aaaaaaaaaaaaaa/" + ff[ii].getName() + ".xls"));
//                    WritableSheet sheet = book.createSheet(ff[ii].getName(), ii);
//
//                    for (int i = 0; i < ssname.length; i++) {
//                        System.out.println(ssname[i]);
//                        String name = path + "\\" + ssname[i];
//                        Label label = new Label(0, i, ssname[i]);
//                        sheet.addCell(label);
//
//                        {
//                            Model mm = new Model(name);
//                            DATArv data = new DATArv(mm);
//                            node = 0;
//                            STR2 str = new STR2(data);
//                            long time1 = System.currentTimeMillis();
//                            str.MAC();
//                            long time2 = System.currentTimeMillis();
//                            long result = time2 - time1;
//                            str = null;
//                            data = null;
//                            mm = null;
//                            System.out.println("node: " + node);
//                            System.out.println("runTime: " + result);
//                            jxl.write.Number jNode = new jxl.write.Number(3, i, node);
//                            sheet.addCell(jNode);
//                            jxl.write.Number jResult = new jxl.write.Number(6, i, result);
//                            sheet.addCell(jResult);
//                        }
//
//                        {
//                            Model mm = new Model(name);
//                            DATA data = new DATA(mm);
//                            node = 0;
//                            STR2_star str = new STR2_star(data, mm);
//                            long time1 = System.currentTimeMillis();
//                            str.MAC();
//                            long time2 = System.currentTimeMillis();
//                            long result = time2 - time1;
//                            str = null;
//                            data = null;
//                            mm = null;
//                            System.out.println("node: " + node);
//                            System.out.println("runTime: " + result);
//                            jxl.write.Number jNode = new jxl.write.Number(3, i, node);
//                            sheet.addCell(jNode);
//                            jxl.write.Number jResult = new jxl.write.Number(6, i, result);
//                            sheet.addCell(jResult);
//                        }
//
//                        {
//                            Model mm = new Model(name);
//                            DATA data = new DATA(mm);
//                            node = 0;
//                            hybridSTR str = new hybridSTR(data);
//                            long time1 = System.currentTimeMillis();
//                            str.MAC();
//                            long time2 = System.currentTimeMillis();
//                            long result = time2 - time1;
//                            str = null;
//                            data = null;
//                            mm = null;
//                            System.out.println("node: " + node);
//                            System.out.println("runTime: " + result);
//                            jxl.write.Number jNode = new jxl.write.Number(3, i, node);
//                            sheet.addCell(jNode);
//                            jxl.write.Number jResult = new jxl.write.Number(6, i, result);
//                            sheet.addCell(jResult);
//                        }
//
//                    }
//                    book.write();
//                    book.close();
//                }
//            }
//        } catch (Exception e) {
//            System.out.println(e);
//        }
//    }


    Model() {

    }
}
