package com.solandra.hideout.utils;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public enum Schematic {
    HIDEOUT("hideout/hideout.schem"),
    MINE("hideout/mine.schem");

    private final String fileName;

    Schematic(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Gets the file name of the schematic.
     *
     * @return The schematic file name.
     */
    public String getPath() {
        return this.fileName;
    }

    public static File getHideout() {
        return FileUtil.getFile(HIDEOUT.getPath());
    }

    public static File getMine() {
        return FileUtil.getFile(MINE.getPath());
    }

    /**
     * Charge la schématique de l'hideout depuis le fichier spécifié.
     *
     * @return Le clipboard contenant la schématique chargée.
     */
    public static Clipboard loadSchematic(File schematicFile) {
        if (!schematicFile.exists()) {
            Common.throwError(new IllegalArgumentException("Le fichier de schématique n'existe pas : " + schematicFile.getPath()));
            return null;
        }

        Clipboard clipboard = null;
        try (FileInputStream fis = new FileInputStream(schematicFile)) {
            ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);

            if (format == null) {
                Common.throwError(new IllegalArgumentException("Le format de la schématique n'est pas reconnu : " + schematicFile.getPath()));
                return null;
            }

            try (ClipboardReader reader = format.getReader(fis)) {
                clipboard = reader.read();
            }
        } catch (IOException e) {
            Common.throwError(e, "Erreur lors du chargement de la schématique : " + schematicFile.getPath());
        }

        return clipboard;
    }
}