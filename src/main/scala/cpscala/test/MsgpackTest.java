//package cpscala.Experiment;
//
//import org.msgpack.core.ExtensionTypeHeader;
//import org.msgpack.core.MessagePack;
//import org.msgpack.core.MessagePacker;
//import org.msgpack.core.MessageUnpacker;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//
//public class MsgpackTest {
//    public static void main(String[] args) {
//        String filepath = "src/Experiment.txt";
//        try {
//            File tempFile = new File(filepath);
//            boolean a = tempFile.createNewFile();
////            tempFile.deleteOnExit();
//// Write packed data to a file. No need exists to wrap the file stream with BufferedOutputStream, since MessagePacker has its own buffer
//            MessagePacker packer = MessagePack.newDefaultPacker(new FileOutputStream(tempFile));
//            /* 以下是对自定义数据类型的打包*/
//            byte[] extData = "custom data type".getBytes(MessagePack.UTF8);
//            packer.packExtensionTypeHeader((byte) 1, extData.length);  // type number [0, 127], data byte length
//            packer.writePayload(extData);
//            packer.close();
//
//        FileInputStream fileInputStream = new FileInputStream(new File(filepath));
//        MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(fileInputStream);
////先将自定义数据的消息头读出
////        ExtensionTypeHeader et = unpacker.unpackExtensionTypeHeader();
//////判断消息类型
////        if (et.getType() == (ExtType.TYPE_TAB)) {
////            int lenth = et.getLength();
////            //按长度读取二进制数据
////            byte[] bytes = new byte[lenth];
////            unpacker.readPayload(bytes);
////            //构造tabsjson对象?
////            TabsJson tab = new TabsJson();
////            //构造unpacker将二进制数据解包到java对象中
////            MessageUnpacker unpacker1 = MessagePack.newDefaultUnpacker(bytes);
////            tab.type = unpacker1.unpackInt();
////            tab.f = unpacker1.unpackString();
////            unpacker1.close();
////        }
////        unpacker.close();
////
////        } catch (Exception ex) {
////            System.out.println(ex);
////        }
//ex
//    }
//}
