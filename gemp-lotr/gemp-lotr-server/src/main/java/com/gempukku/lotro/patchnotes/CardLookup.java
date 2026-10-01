package com.gempukku.lotro.patchnotes;

/**
 * Finds the card a patch note's {@code [[...]]} card link names (see {@link PatchNoteRenderer}): a blueprint id
 * ({@code 1_5}), a collector's info ({@code 1C5}) or a card name ({@code Cleaving Blow},
 * {@code Aragorn, Ranger of the North}).
 */
public interface CardLookup {
    /**
     * @param reference what the note wrote between the brackets (before any {@code |})
     * @return the card, or why there is none (unknown, or several cards match)
     */
    Result resolve(String reference);

    /**
     * @param blueprintId the card to show, null when the reference could not be resolved
     * @param name        the card's full name ("Aragorn, Ranger of the North"), null when unresolved
     * @param error       why the reference could not be resolved, null when it was
     */
    record Result(String blueprintId, String name, String error) {
        public static Result found(String blueprintId, String name) {
            return new Result(blueprintId, name, null);
        }

        public static Result failed(String error) {
            return new Result(null, null, error);
        }

        public boolean isFound() {
            return blueprintId != null;
        }
    }
}
