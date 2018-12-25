package swftool;

import com.adobe.flash.abc.ABCParser;
import com.adobe.flash.swf.tags.DoABCTag;
import com.adobe.flash.abc.ABCEmitter;

public class Test {
    public Test(DoABCTag abcTag) {
        System.out.println("adobe"+abcTag.getName());
        ABCParser abcParser=new ABCParser(abcTag.getABCData());
        com.adobe.flash.abc.ABCEmitter abc=new com.adobe.flash.abc.ABCEmitter();
        abc.setAllowBadJumps(true);
        abcParser.parseABC(abc);

        System.out.println("adobe------------"+abcTag.getName());
    }
}
