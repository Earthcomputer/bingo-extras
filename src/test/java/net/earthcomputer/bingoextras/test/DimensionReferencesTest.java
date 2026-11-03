package net.earthcomputer.bingoextras.test;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.util.asm.ASM;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Keeps track of every place in Minecraft and Bingo that refers to a specific dimension, such as
 * {@code level.dimension() == Level.NETHER} or {@code server.overworld()}. Team specific Fantasy dimensions are copies
 * of the overworld, nether and end with different keys, so each of these references may need a Mixin to make it also
 * apply to the team specific copies.
 *
 * <p>The references found are written to {@code docs/references.csv}, one per line, with the class, method and line
 * number they were found at. If that file exists, this test fails if the references found differ from it.
 *
 * <p>When porting to a new Minecraft or Bingo version and this test fails:
 * <ol>
 *     <li>Delete {@code docs/references.csv}.</li>
 *     <li>Rerun this test, which regenerates the file and passes.</li>
 *     <li>Look at the diff of the file in version control. For each added or changed reference, check the code to see
 *     whether it needs a Mixin (or a change to an existing Mixin) so that it works in team specific dimensions. Lines
 *     that only moved because of unrelated changes nearby can usually be ignored, but check that the Mixins targeting
 *     them still apply.</li>
 *     <li>Commit the regenerated file along with any Mixin changes.</li>
 * </ol>
 */
public class DimensionReferencesTest {
    private static final Path REFERENCES_FILE = Path.of("docs/references.csv");
    private static final String HEADER = "class,method,line,reference";

    private static final List<String> SCANNED_MODS = List.of("minecraft", "bingo");

    private static final Set<String> DIMENSION_FIELD_OWNERS = Set.of(
        "net/minecraft/world/level/Level",
        "net/minecraft/world/level/dimension/LevelStem",
        "net/minecraft/world/level/dimension/BuiltinDimensionTypes"
    );
    private static final Set<String> DIMENSION_FIELD_NAMES = Set.of("OVERWORLD", "NETHER", "END");
    private static final String RESOURCE_KEY_DESC = "Lnet/minecraft/resources/ResourceKey;";

    private static final Set<String> MINECRAFT_SERVER_CLASS_NAMES = Set.of(
        "net/minecraft/server/MinecraftServer",
        "net/minecraft/server/dedicated/DedicatedServer",
        "net/minecraft/client/server/IntegratedServer"
    );
    private static final Set<String> DIMENSION_METHOD_NAMES = Set.of("overworld", "findRespawnDimension");

    @Test
    public void testDimensionReferences() throws IOException {
        List<Reference> references = new ArrayList<>();
        for (String modId : SCANNED_MODS) {
            ModContainer mod = FabricLoader.getInstance().getModContainer(modId).orElseThrow(() -> new AssertionError("Mod " + modId + " is not loaded"));
            for (Path rootPath : mod.getRootPaths()) {
                scanRootPath(rootPath, references);
            }
        }
        references.sort(Comparator.comparing(Reference::className).thenComparingInt(Reference::line).thenComparing(Reference::method).thenComparing(Reference::reference));

        String actual = Stream.concat(Stream.of(HEADER), references.stream().map(Reference::toCsv)).collect(Collectors.joining("\n", "", "\n"));

        if (Files.notExists(REFERENCES_FILE)) {
            Files.createDirectories(REFERENCES_FILE.getParent());
            Files.writeString(REFERENCES_FILE, actual);
            return;
        }

        String expected = Files.readString(REFERENCES_FILE).replace("\r\n", "\n");
        assertEquals(expected, actual, "Dimension references have changed. Delete " + REFERENCES_FILE + ", rerun this test, and check the diff (see the comment on " + DimensionReferencesTest.class.getSimpleName() + ")");
    }

    private static void scanRootPath(Path rootPath, List<Reference> references) throws IOException {
        try (Stream<Path> files = Files.walk(rootPath)) {
            for (Path file : (Iterable<Path>) files::iterator) {
                if (file.toString().endsWith(".class")) {
                    try (InputStream in = Files.newInputStream(file)) {
                        new ClassReader(in).accept(new ReferenceFinder(references), ClassReader.SKIP_FRAMES);
                    }
                }
            }
        }
    }

    private record Reference(String className, String method, int line, String reference) {
        String toCsv() {
            return className + "," + method + "," + line + "," + reference;
        }
    }

    private static final class ReferenceFinder extends ClassVisitor {
        private final List<Reference> references;
        private String className;

        ReferenceFinder(List<Reference> references) {
            super(ASM.API_VERSION);
            this.references = references;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            this.className = name.replace('/', '.');
        }

        @Override
        public MethodVisitor visitMethod(int access, String methodName, String methodDesc, String signature, String[] exceptions) {
            return new MethodVisitor(ASM.API_VERSION) {
                private int line = -1;

                @Override
                public void visitLineNumber(int line, Label start) {
                    this.line = line;
                }

                @Override
                public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                    if (opcode == Opcodes.GETSTATIC) {
                        checkField(owner, name, descriptor);
                    }
                }

                @Override
                public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                    checkMethod(owner, name);
                }

                @Override
                public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
                    checkConstant(bootstrapMethodHandle);
                    for (Object arg : bootstrapMethodArguments) {
                        checkConstant(arg);
                    }
                }

                @Override
                public void visitLdcInsn(Object value) {
                    checkConstant(value);
                }

                // method references such as server::overworld are method handles in invokedynamic arguments, and
                // constant dynamics can refer to fields and methods through their bootstrap method and arguments
                private void checkConstant(Object constant) {
                    switch (constant) {
                        case Handle handle -> {
                            if (handle.getTag() == Opcodes.H_GETSTATIC) {
                                checkField(handle.getOwner(), handle.getName(), handle.getDesc());
                            } else if (handle.getTag() >= Opcodes.H_INVOKEVIRTUAL) {
                                checkMethod(handle.getOwner(), handle.getName());
                            }
                        }
                        case ConstantDynamic condy -> {
                            checkConstant(condy.getBootstrapMethod());
                            for (int i = 0; i < condy.getBootstrapMethodArgumentCount(); i++) {
                                checkConstant(condy.getBootstrapMethodArgument(i));
                            }
                        }
                        default -> {
                        }
                    }
                }

                private void checkField(String owner, String name, String descriptor) {
                    if (DIMENSION_FIELD_OWNERS.contains(owner) && DIMENSION_FIELD_NAMES.contains(name) && descriptor.equals(RESOURCE_KEY_DESC)) {
                        addReference(simpleName(owner) + "." + name);
                    }
                }

                private void checkMethod(String owner, String name) {
                    if (MINECRAFT_SERVER_CLASS_NAMES.contains(owner) && DIMENSION_METHOD_NAMES.contains(name)) {
                        addReference(simpleName(owner) + "." + name + "()");
                    }
                }

                private void addReference(String reference) {
                    references.add(new Reference(className, methodName, line, reference));
                }
            };
        }

        private static String simpleName(String internalName) {
            return internalName.substring(internalName.lastIndexOf('/') + 1);
        }
    }
}
