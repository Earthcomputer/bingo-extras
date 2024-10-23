package net.earthcomputer.bingoextras;

import com.chocohead.mm.api.ClassTinkerers;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.util.asm.ASM;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class BingoExtrasEarlyRiser implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(BingoExtrasEarlyRiser.class);

    private static final String LEVEL_CLASS_NAME;
    private static final String SERVER_LEVEL_CLASS_NAME;
    private static final String RESOURCE_KEY_CLASS_NAME;
    private static final String DIMENSION_METHOD_NAME;
    private static final String OVERWORLD_FIELD_NAME;
    private static final String NETHER_FIELD_NAME;
    private static final String END_FIELD_NAME;
    static {
        MappingResolver mappings = FabricLoader.getInstance().getMappingResolver();
        LEVEL_CLASS_NAME = mappings.mapClassName("intermediary", "net.minecraft.class_1937").replace('.', '/');
        SERVER_LEVEL_CLASS_NAME = mappings.mapClassName("intermediary", "net.minecraft.class_3218").replace('.', '/');
        RESOURCE_KEY_CLASS_NAME = mappings.mapClassName("intermediary", "net.minecraft.class_5321").replace('.', '/');
        DIMENSION_METHOD_NAME = mappings.mapMethodName("intermediary", "net.minecraft.class_1937", "method_27983", "()Lnet/minecraft/class_5321;");
        OVERWORLD_FIELD_NAME = mappings.mapFieldName("intermediary", "net.minecraft.class_1937", "field_25179", "Lnet/minecraft/class_5321;");
        NETHER_FIELD_NAME = mappings.mapFieldName("intermediary", "net.minecraft.class_1937", "field_25180", "Lnet/minecraft/class_5321;");
        END_FIELD_NAME = mappings.mapFieldName("intermediary", "net.minecraft.class_1937", "field_25181", "Lnet/minecraft/class_5321;");
    }

    @Override
    public void run() {
        if (FabricLoader.getInstance().isModLoaded("fantasy")) {
            registerDimensionComparisonChanges();
        }
    }

    private static void registerDimensionComparisonChanges() {
        for (String targetClass : findDimensionComparisonClasses()) {
            ClassTinkerers.addTransformation(targetClass, clazz -> {
                if (clazz.methods != null) {
                    for (MethodNode method : clazz.methods) {
                        replaceDimensionComparisons(method);
                    }
                }
            });
        }
    }

    private static void replaceDimensionComparisons(MethodNode method) {
        if (method.instructions == null) {
            return;
        }

        MethodInsnNode target = null;
        int foundCount = 0;
        for (AbstractInsnNode insn : method.instructions.toArray()) {
            if (insn.getOpcode() < 0) {
                continue;
            }

            switch (insn) {
                case MethodInsnNode methodInsn -> {
                    if ((methodInsn.owner.equals(LEVEL_CLASS_NAME) || methodInsn.owner.equals(SERVER_LEVEL_CLASS_NAME))
                        && methodInsn.name.equals(DIMENSION_METHOD_NAME)
                        && methodInsn.desc.equals("()L" + RESOURCE_KEY_CLASS_NAME + ";")
                    ) {
                        foundCount = 1;
                        target = methodInsn;
                    } else {
                        foundCount = 0;
                    }
                }
                case FieldInsnNode fieldInsn -> {
                    if (foundCount == 1
                        && fieldInsn.owner.equals(LEVEL_CLASS_NAME)
                        && (fieldInsn.name.equals(OVERWORLD_FIELD_NAME) || fieldInsn.name.equals(NETHER_FIELD_NAME) || fieldInsn.name.equals(END_FIELD_NAME))
                        && fieldInsn.desc.equals("L" + RESOURCE_KEY_CLASS_NAME + ";")
                    ) {
                        foundCount = 2;
                    } else {
                        foundCount = 0;
                    }
                }
                case JumpInsnNode ignored -> {
                    if (foundCount == 2 && (insn.getOpcode() == Opcodes.IF_ACMPEQ || insn.getOpcode() == Opcodes.IF_ACMPNE)) {
                        method.instructions.insertBefore(target, new MethodInsnNode(Opcodes.INVOKESTATIC, "net/earthcomputer/bingoextras/FantasyUtil", "originalLevelOrSelf", "(L" + LEVEL_CLASS_NAME + ";)L" + LEVEL_CLASS_NAME + ";"));
                        target.owner = LEVEL_CLASS_NAME;
                    }
                    foundCount = 0;
                }
                default -> foundCount = 0;
            }
        }
    }

    private static List<String> findDimensionComparisonClasses() {
        LOGGER.info("Scanning Minecraft for dimension checks...");
        long startTime = System.nanoTime();

        List<String> result = new ArrayList<>();

        for (Path jarFile : FabricLoader.getInstance().getModContainer("minecraft").orElseThrow().getRootPaths()) {
            try (Stream<Path> files = Files.walk(jarFile)) {
                for (Path file : (Iterable<Path>) files::iterator) {
                    if (file.toString().endsWith(".class")) {
                        try (InputStream in = Files.newInputStream(file)) {
                            ClassReader reader = new ClassReader(in);
                            HasDimensionComparisonVisitor visitor = new HasDimensionComparisonVisitor();
                            reader.accept(visitor, ClassReader.SKIP_FRAMES);
                            if (visitor.result) {
                                result.add(reader.getClassName());
                            }
                        }
                    }
                }
            } catch (IOException e) {
                LOGGER.error("Failed to read minecraft jar", e);
            }
        }

        long elapsed = System.nanoTime() - startTime;
        LOGGER.info("Scanned Minecraft for dimension checks in {}ms, found {} classes to transform", elapsed / 1000000, result.size());

        return result;
    }

    private static class HasDimensionComparisonVisitor extends ClassVisitor {
        boolean result = false;

        HasDimensionComparisonVisitor() {
            super(ASM.API_VERSION);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            return new MethodVisitor(ASM.API_VERSION) {
                int foundCount = 0;
                @Override
                public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                    if ((owner.equals(LEVEL_CLASS_NAME) || owner.equals(SERVER_LEVEL_CLASS_NAME))
                        && name.equals(DIMENSION_METHOD_NAME)
                        && descriptor.equals("()L" + RESOURCE_KEY_CLASS_NAME + ";")
                    ) {
                        foundCount = 1;
                    } else {
                        foundCount = 0;
                    }
                }

                @Override
                public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                    if (foundCount == 1
                        && owner.equals(LEVEL_CLASS_NAME)
                        && (name.equals(OVERWORLD_FIELD_NAME) || name.equals(NETHER_FIELD_NAME) || name.equals(END_FIELD_NAME))
                        && descriptor.equals("L" + RESOURCE_KEY_CLASS_NAME + ";")
                    ) {
                        foundCount = 2;
                    } else {
                        foundCount = 0;
                    }
                }

                @Override
                public void visitJumpInsn(int opcode, Label label) {
                    if (foundCount == 2 && (opcode == Opcodes.IF_ACMPEQ || opcode == Opcodes.IF_ACMPNE)) {
                        result = true;
                        foundCount = 0;
                    }
                }

                @Override
                public void visitInsn(int opcode) {
                    foundCount = 0;
                }
            };
        }
    }
}
