package nl.adamg.baizel.core.impl;

import nl.adamg.baizel.core.api.BaizelArguments;
import nl.adamg.baizel.core.api.BaizelOptions;
import nl.adamg.baizel.core.api.Invocation;
import nl.adamg.baizel.internal.common.serialization.JsonUtil;
import nl.adamg.baizel.internal.common.util.EntityModel;
import nl.adamg.baizel.internal.common.util.Text;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/// Format:
/// ```
/// baizel [<BAIZEL_OPTION>...] [<TASK>...] [<TASK_ARG>...] [-- <TARGET>...]
/// ```
///
/// - API:    [nl.adamg.baizel.core.api.BaizelArguments]
/// - Entity: [nl.adamg.baizel.core.entities.BaizelArguments]
/// - Model:  [nl.adamg.baizel.core.impl.BaizelArgumentsImpl]
public class BaizelArgumentsImpl
        extends EntityModel<BaizelArguments, nl.adamg.baizel.core.entities.BaizelArguments, BaizelArgumentsImpl>
        implements BaizelArguments {
    //region factory
    public static BaizelArguments of(
            BaizelOptions options,
            Invocation invocation
    ) {
        return new BaizelArgumentsImpl(
                new nl.adamg.baizel.core.entities.BaizelArguments(
                        ((BaizelOptionsImpl)options).entity(),
                        ((InvocationImpl)invocation).entity()
                )
        );
    }

    public static BaizelArguments parse(String... args) {
        if (args.length == 1) {
            var argsString = args[0];
            var base64Decoded = Text.tryDecodeBase64(argsString);
            if (base64Decoded != null && base64Decoded.startsWith("{") && base64Decoded.endsWith("}")) {
                argsString = base64Decoded;
            }
            if (argsString.startsWith("{") && argsString.endsWith("}")) {
                return new BaizelArgumentsImpl(JsonUtil.fromJson(argsString, nl.adamg.baizel.core.entities.BaizelArguments.class));
            }
        }
        var remainingArgs = new LinkedList<>(Arrays.asList(args));
        var options = BaizelOptionsImpl.parse(remainingArgs);
        var invocation = InvocationImpl.parse(remainingArgs);
        return BaizelArgumentsImpl.of(options, invocation);
    }

    //endregion

    //region getters
    @Override
    public BaizelOptions options() {
        return new BaizelOptionsImpl(entity.options);
    }

    @Override
    public Invocation invocation() {
        return new InvocationImpl(entity.invocation);
    }
    //endregion

    //region entity model
    @Override
    public String toString() {
        return options() + " " + invocation();
    }

    @Override
    protected List<Function<nl.adamg.baizel.core.entities.BaizelArguments, ?>> fields() {
        return List.of(
                a -> a.options,
                a -> a.invocation
        );
    }
    //endregion

    //region generated code
    public BaizelArgumentsImpl(nl.adamg.baizel.core.entities.BaizelArguments entity) {
        super(entity);
    }
    //endregion
}
