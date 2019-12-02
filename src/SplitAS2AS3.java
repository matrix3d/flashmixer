import com.adobe.flash.swf.SWF;
import com.adobe.flash.swf.io.SWFReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 分离as2和as3文件
 * created by lizhi
 */
public class SplitAS2AS3 {
    public static void main(String[] args) {
        File dir=new File("F:\\proj\\iosgame\\pachong\\out");
        File diras2=new File(dir.getPath()+"\\as2");
        if(!diras2.exists()){
            diras2.mkdir();
        }
        for (File f :dir.listFiles()
             ) {
            if(!f.isDirectory()&&f.getName().indexOf(".swf")!=-1){
                SWFReader reader=new SWFReader();
                boolean isAS3=false;
                File jpg=null;
                try {
                    FileInputStream fis=new FileInputStream(f);
                   SWF swf = (SWF) reader.readFrom(fis, f.getPath());
                   reader.close();
                   fis.close();
                   if(swf.getFileAttributes().isAS3()){
                       isAS3=true;
                       System.out.println(f.getName()+".as3");
                   }else {
                       isAS3=false;
                       System.out.println(f.getName()+".as2");
                   }
                   jpg=new File(f.getParent()+File.separator+f.getName().replace(".swf",".jpg"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    if(!isAS3) {
                        Files.move(f.toPath(), Paths.get(diras2.getPath(), f.getName()));
                        if(jpg.exists()){
                            Files.move(jpg.toPath(), Paths.get(diras2.getPath(), jpg.getName()));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }
}
