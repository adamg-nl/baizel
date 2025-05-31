package nl.adamg.baizel.cli.internal;

import nl.adamg.baizel.cli.Baizel;

import java.util.ArrayList;

public class CliParser {
    /// Format:
    /// ```
    /// baizel [<BAIZEL_OPTION>...] <COMMAND> [<COMMAND_ARG>...] [-- <TARGET>...]
    /// ```
    public static Baizel.Args parse(String... args) {
        var options = new ArrayList<String>();
        var command = "";
        var commandArgs = new ArrayList<String>();
        var targets = new ArrayList<String>();

        if (args.length == 0) {
            return new Baizel.Args(options, command, commandArgs, targets);
        }
//        var next arg

        return new Baizel.Args(options, command, commandArgs, targets);
    }
}
