import io.paratek.rs.analysis.hook.Game;
import io.paratek.rs.deob.TransformationMediator;
import io.paratek.rs.deob.impl.IllegalStateExceptions;
import io.paratek.rs.deob.impl.RenameUnique;
import io.paratek.rs.deob.impl.RuntimeExceptions;
import io.paratek.rs.loader.Frame;
import io.paratek.rs.util.JarHandler;

import java.io.File;

public class DeobfuscationTest {

    public static void main(String[] args) {
        final JarHandler handler = new JarHandler(Game.OSRS, new File("/home/sysassist/IdeaProjects/deob/src/main/resources/gamepack_178-deob.jar"));
//        final TransformationMediator mediator = new TransformationMediator(handler);
//        mediator.submit(new RenameUnique());
//        mediator.submit(new IllegalStateExceptions());
//        mediator.submit(new RuntimeExceptions());
//        mediator.run();

//        handler.dumpTo("/home/sysassist/IdeaProjects/deob/src/main/resources/gamepack_178-deob.jar");

        Frame.start(handler);
    }


}
