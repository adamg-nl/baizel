package nl.adamg.baizel.core.model;

import nl.adamg.baizel.core.api.BaizelOptions;
import nl.adamg.baizel.core.api.Invocation;
import nl.adamg.baizel.internal.common.serialization.JsonUtil;
import nl.adamg.baizel.internal.common.util.EntityModel;
import nl.adamg.baizel.internal.common.util.Text;

import java.util.*;
import java.util.function.Function;

/// Format:
/// ```
/// baizel [<BAIZEL_OPTION>...] [<TASK>...] [<TASK_ARG>...] [-- <TARGET>...]
/// ```
///
/// - API:    [nl.adamg.baizel.core.api.BaizelArguments]
/// - Entity: [nl.adamg.baizel.core.entities.BaizelArguments]
/// - Model:  [nl.adamg.baizel.core.model.BaizelArguments]
public class BaizelArguments
        extends EntityModel<nl.adamg.baizel.core.api.BaizelArguments, nl.adamg.baizel.core.entities.BaizelArguments, BaizelArguments>
        implements nl.adamg.baizel.core.api.BaizelArguments {
    //region factory
    public static nl.adamg.baizel.core.api.BaizelArguments of(
            nl.adamg.baizel.core.entities.BaizelOptions options,
            nl.adamg.baizel.core.entities.Invocation invocation
    ) {
        return new BaizelArguments(
                new nl.adamg.baizel.core.entities.BaizelArguments(
                        options,
                        invocation
                )
        );
    }

    public static nl.adamg.baizel.core.api.BaizelArguments parse(String... args) {
        if (args.length == 1) {
            var argsString = args[0];
            var base64Decoded = Text.tryDecodeBase64(argsString);
            if (base64Decoded != null && base64Decoded.startsWith("{") && base64Decoded.endsWith("}")) {
                argsString = base64Decoded;
            }
            if (argsString.startsWith("{") && argsString.endsWith("}")) {
                return new BaizelArguments(JsonUtil.fromJson(argsString, nl.adamg.baizel.core.entities.BaizelArguments.class));
            }
        }
        var remainingArgs = (Queue<String>)new LinkedList<>(Arrays.asList(args));
        var options = nl.adamg.baizel.core.model.BaizelOptions.parse(remainingArgs);
        var invocation = nl.adamg.baizel.core.model.Invocation.parse(remainingArgs);
        return BaizelArguments.of(options, invocation);
    }

    //endregion

    //region getters
    @Override
    public BaizelOptions options() {
        return new nl.adamg.baizel.core.model.BaizelOptions(entity.options);
    }

    @Override
    public Invocation invocation() {
        return new nl.adamg.baizel.core.model.Invocation(entity.invocation);
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
    public BaizelArguments(nl.adamg.baizel.core.entities.BaizelArguments entity) {
        super(entity);
    }
    //endregion
}
