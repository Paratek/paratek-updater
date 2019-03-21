import io.paratek.rs.analysis.hook.Game;
import io.paratek.rs.deob.TransformationMediator;
import io.paratek.rs.deob.impl.RenameUnique;
import io.paratek.rs.loader.Frame;
import io.paratek.rs.util.JarHandler;

import java.io.File;

public class DeobfuscationTest {

    public static void main(String[] args) {
        final JarHandler handler = new JarHandler(Game.OSRS, new File("/home/sysassist/IdeaProjects/deob/src/main/resources/gamepack_178.jar"));
        final TransformationMediator mediator = new TransformationMediator(handler);
        mediator.submit(new RenameUnique());
        mediator.run();
//
        handler.dumpTo("/home/sysassist/IdeaProjects/deob/src/main/resources/gamepack_178-deob.jar");
//
//        try {
//            Frame.start(handler);
//        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
//            e.printStackTrace();
//        }
    }


}
