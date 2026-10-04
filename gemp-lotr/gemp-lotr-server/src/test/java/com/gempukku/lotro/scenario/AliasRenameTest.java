package com.gempukku.lotro.scenario;

import org.hjson.JsonObject;
import org.hjson.JsonValue;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * A card's name can be a word the file uses for something else (a part of the file, a zone, a term).  Renaming the
 * card changes the card's name and leaves the other uses of the word alone.
 */
public class AliasRenameTest {
    private static JsonObject scenario() {
        return JsonValue.readHjson("""
                {
                	cards: {
                		freeps: {
                			frodo: 10_121
                			ring: 1_2
                			hand: 1_116
                			pass: 1_83
                		}
                	}
                	ringBearer: {
                		freeps: frodo
                	}
                	ring: {
                		freeps: ring
                	}
                	setup: {
                		freeps: {
                			hand: "ring, hand, pass"
                		}
                		attach: {
                			hand: ring
                		}
                		tokens: {
                			hand: 2
                		}
                	}
                	rng: [
                		"shuffle(freeps.hand: ring, hand)"
                	]
                	steps: [
                		{
                			when: "freeps, action, phase(fellowship), offers(hand), text(hand)"
                			do: "move(hand, freeps.hand), draw(freeps, 2), order(freeps.hand, hand, ring), attach(hand, ring)"
                			messages: [
                				{
                					text: "Your hand holds [[hand]]: {cost:hand}."
                					anchor: "pile:self.hand, card:hand, ui:assignment:hand"
                					zoom: "card:hand"
                				}
                			]
                			allow: "play(hand), option(play the hand), choose(ring, hand), assign(frodo: hand + ring)?, pass"
                		}
                		{
                			until: "phase(regroup)"
                			anyOrder: [
                				{
                					when: "fighting(hand)"
                					answer: "use(hand, heal hand), pass"
                				}
                			]
                		}
                	]
                }
                """).asObject();
    }

    @Test
    public void aCardCalledAfterAZoneIsNotTheZone() {
        JsonObject root = scenario();
        AliasRename.rename(root, "hand", "glove");
        assertEquals("1_116", root.get("cards").asObject().get("freeps").asObject().getString("glove", null));
        JsonObject setup = root.get("setup").asObject();
        assertEquals("The zone keeps its name; the card in it has the new one", "ring, glove, pass", setup.get("freeps").asObject().getString("hand", null));
        assertEquals("ring", setup.get("attach").asObject().getString("glove", null));
        assertEquals(2, setup.get("tokens").asObject().getInt("glove", 0));
        assertEquals("shuffle(freeps.hand: ring, glove)", root.get("rng").asArray().get(0).asString());
        JsonObject step = root.get("steps").asArray().get(0).asObject();
        assertEquals("freeps, action, phase(fellowship), offers(glove), text(hand)", step.getString("when", null));
        assertEquals("move(glove, freeps.hand), draw(freeps, 2), order(freeps.hand, glove, ring), attach(glove, ring)", step.getString("do", null));
        JsonObject message = step.get("messages").asArray().get(0).asObject();
        assertEquals("Your hand holds [[glove|hand]]: {cost:glove}.", message.getString("text", null));
        assertEquals("pile:self.hand, card:glove, ui:assignment:glove", message.getString("anchor", null));
        assertEquals("card:glove", message.getString("zoom", null));
        assertEquals("play(glove), option(play the hand), choose(ring, glove), assign(frodo: glove + ring)?, pass", step.getString("allow", null));
        JsonObject inBlock = root.get("steps").asArray().get(1).asObject().get("anyOrder").asArray().get(0).asObject();
        assertEquals("fighting(glove)", inBlock.getString("when", null));
        assertEquals("use(glove, heal hand), pass", inBlock.getString("answer", null));
    }

    @Test
    public void aCardCalledAfterAPartOfTheFileOrATermIsNotThatEither() {
        JsonObject root = scenario();
        AliasRename.rename(root, "ring", "band");
        assertEquals("The part of the file is still called ring", "band", root.get("ring").asObject().getString("freeps", null));
        assertEquals("hand", root.get("setup").asObject().get("attach").asObject().names().getFirst());
        assertEquals("band", root.get("setup").asObject().get("attach").asObject().getString("hand", null));

        root = scenario();
        AliasRename.rename(root, "pass", "servant");
        JsonObject step = root.get("steps").asArray().get(0).asObject();
        assertEquals("Passing is not a card", "play(hand), option(play the hand), choose(ring, hand), assign(frodo: hand + ring)?, pass", step.getString("allow", null));
        assertEquals("ring, hand, servant", root.get("setup").asObject().get("freeps").asObject().getString("hand", null));
    }
}
