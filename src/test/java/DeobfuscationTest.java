import io.paratek.rs.analysis.hook.Game;
import io.paratek.rs.deob.TransformationMediator;
import io.paratek.rs.deob.impl.RenameUnique;
import io.paratek.rs.util.JarHandler;

import java.io.File;

public class DeobfuscationTest {

    public static void main(String[] args) {
        final JarHandler handler = new JarHandler(Game.OSRS, new File("/spare/gamepack_178.jar"));
        final TransformationMediator mediator = new TransformationMediator(handler);
        mediator.submit(new RenameUnique());
        mediator.run();
    }


}
