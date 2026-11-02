import com.sun.jdi.Bootstrap;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Pushes recompiled classes into a running dev game started with -PhotReload (debug port 5005).
 * Usage (repo root, JDK 17): ./gradlew classes && java tools/HotSwap.java
 * Only classes changed since the last push are sent (stamp: build/hotswap.stamp).
 */
public class HotSwap {
    private static final Path CLASSES = Path.of("build/classes/java/main");
    private static final Path STAMP = Path.of("build/hotswap.stamp");

    public static void main(String[] args) throws Exception {
        FileTime since = Files.exists(STAMP) ? Files.getLastModifiedTime(STAMP) : FileTime.fromMillis(0);
        List<Path> changed;
        try (Stream<Path> files = Files.walk(CLASSES)) {
            changed = files.filter(p -> p.toString().endsWith(".class")).filter(p -> isNewer(p, since)).toList();
        }
        if (changed.isEmpty()) {
            System.out.println("No changed classes since the last push.");
            return;
        }

        AttachingConnector connector = Bootstrap.virtualMachineManager().attachingConnectors().stream()
                .filter(c -> c.transport().name().equals("dt_socket")).findFirst().orElseThrow();
        Map<String, Connector.Argument> arguments = connector.defaultArguments();
        arguments.get("hostname").setValue("127.0.0.1");
        arguments.get("port").setValue("5005");
        VirtualMachine vm = connector.attach(arguments);
        try {
            Map<ReferenceType, byte[]> redefinitions = new HashMap<>();
            for (Path file : changed) {
                String name = CLASSES.relativize(file).toString().replace('\\', '/').replace('/', '.').replaceAll("\\.class$", "");
                byte[] bytes = Files.readAllBytes(file);
                for (ReferenceType type : vm.classesByName(name)) {
                    redefinitions.put(type, bytes);
                }
            }
            if (!redefinitions.isEmpty()) {
                vm.redefineClasses(redefinitions);
            }
            System.out.println("Reloaded " + redefinitions.size() + " class(es); " + (changed.size() - redefinitions.size())
                    + " changed class(es) not loaded yet (they'll load fresh when first used).");
        } finally {
            vm.dispose();
        }
        Files.createDirectories(STAMP.getParent());
        if (!Files.exists(STAMP)) {
            Files.createFile(STAMP);
        }
        Files.setLastModifiedTime(STAMP, FileTime.fromMillis(System.currentTimeMillis()));
    }

    private static boolean isNewer(Path file, FileTime since) {
        try {
            return Files.getLastModifiedTime(file).compareTo(since) > 0;
        } catch (IOException e) {
            return false;
        }
    }
}
