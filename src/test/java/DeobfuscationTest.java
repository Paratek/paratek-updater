import com.google.common.flogger.FluentLogger;
import io.paratek.rs.analysis.hook.Game;
import io.paratek.rs.deob.TransformationMediator;
import io.paratek.rs.deob.impl.ControlFlow;
import io.paratek.rs.deob.impl.IllegalStateExceptions;
import io.paratek.rs.deob.impl.RuntimeExceptions;
import io.paratek.rs.util.JarHandler;

import java.io.File;

public class DeobfuscationTest {

    public static void main(String[] args) {
        final JarHandler handler = new JarHandler(Game.OSRS, new File("/home/sysassist/Desktop/181/gamepack_181.jar"));
        final TransformationMediator mediator = new TransformationMediator(handler);
//        mediator.submit(new RenameUnique());
        mediator.submit(new IllegalStateExceptions());
        mediator.submit(new RuntimeExceptions());
        mediator.submit(new ControlFlow());
        mediator.run();
        handler.dumpTo("/home/sysassist/Desktop/181/gamepack-deob.jar");
    }


}
