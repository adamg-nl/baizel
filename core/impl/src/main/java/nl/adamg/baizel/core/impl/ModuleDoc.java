package nl.adamg.baizel.core.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import nl.adamg.baizel.internal.common.io.FileSystem;

/// First header is the title.
/// The first sentence ofthe first paragraph is the short description.
public class ModuleDoc {
    private final String title;
    private final String shortDescription;
    private final Path path;

    /// @return object representing `README.md` at the root of the module.
    public static ModuleDoc read(Path modulePath, FileSystem fileSystem) throws IOException {
        var readme = modulePath.resolve("README.md");
        if (!Files.exists(readme)) {
            return new ModuleDoc("", "", readme);
        }
        var lines = fileSystem.readAllLines(readme);
        var title = "";
        var shortDescription = "";
        for(var line : lines) {
            if (title.isEmpty() && line.startsWith("# ")) {
                title = line.substring(2);
            }
            if (shortDescription.isEmpty() && ! line.startsWith("#")) {
                shortDescription = line.replaceAll("\\..*", "");
            }
            if (! title.isEmpty() && ! shortDescription.isEmpty()) {
                break;
            }
        }
        return new ModuleDoc(title, shortDescription, readme);
    }

    //region getters
    public String title() {
        return title;
    }

    public String shortDescription() {
        return shortDescription;
    }
    //endregion

    //region generated code
    public ModuleDoc(String title, String shortDescription, Path path) {
        this.title = title;
        this.shortDescription = shortDescription;
        this.path = path;
    }
    //endregion
}
