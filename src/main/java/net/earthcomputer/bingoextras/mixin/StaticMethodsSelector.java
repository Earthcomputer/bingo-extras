package net.earthcomputer.bingoextras.mixin;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.injection.selectors.ElementNode;
import org.spongepowered.asm.mixin.injection.selectors.ISelectorContext;
import org.spongepowered.asm.mixin.injection.selectors.ITargetSelector;
import org.spongepowered.asm.mixin.injection.selectors.ITargetSelectorDynamic;
import org.spongepowered.asm.mixin.injection.selectors.InvalidSelectorException;
import org.spongepowered.asm.mixin.injection.selectors.MatchResult;

public abstract class StaticMethodsSelector implements ITargetSelectorDynamic {
    @Override
    public ITargetSelector next() {
        return null;
    }

    @Override
    public ITargetSelector configure(Configure request, String... args) {
        return this;
    }

    @Override
    public ITargetSelector validate() throws InvalidSelectorException {
        return null;
    }

    @Override
    public ITargetSelector attach(ISelectorContext context) throws InvalidSelectorException {
        return this;
    }

    @Override
    public int getMinMatchCount() {
        return 0;
    }

    @Override
    public int getMaxMatchCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public <TNode> MatchResult match(ElementNode<TNode> node) {
        MethodNode method = node.getMethod();
        if (method == null) {
            return MatchResult.NONE;
        }
        return accept(method.access) ? MatchResult.MATCH : MatchResult.NONE;
    }

    protected abstract boolean accept(int access);

    @SelectorId(namespace = "bingoextras", value = "static")
    public static final class Static extends StaticMethodsSelector {
        @Override
        protected boolean accept(int access) {
            return (access & Opcodes.ACC_STATIC) != 0;
        }
    }

    @SelectorId(namespace = "bingoextras", value = "nonstatic")
    public static final class NonStatic extends StaticMethodsSelector {
        @Override
        protected boolean accept(int access) {
            return (access & Opcodes.ACC_STATIC) == 0;
        }
    }
}
