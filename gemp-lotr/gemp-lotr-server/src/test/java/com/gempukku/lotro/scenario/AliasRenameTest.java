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

    @Test
    public void whereTheStoryHasGotToNamesItsCardsAndSitesToo() {
        JsonObject root = JsonValue.readHjson("""
                {
                	sites: {
                		freeps: {
                			bagEnd: 1_323
                			shire: 1_331
                		}
                	}
                	setup: {
                		startAt: {
                			site: 2
                			phase: skirmish
                		}
                		skirmishes: {
                			frodo: "runner + shadow"
                		}
                		controlled: {
                			shadow: "bagEnd, 2"
                		}
                		path: "bagEnd, shire"
                	}
                	steps: [
                		{
                			do: "marker(shadow, 2), control(bagEnd, shadow), liberate(bagEnd)"
                		}
                	]
                }
                """).asObject();
        AliasRename.rename(root, "bagEnd", "hobbiton");
        AliasRename.rename(root, "frodo", "baggins");
        //(a card that happens to be called after a seat: the seats keep their names)
        AliasRename.rename(root, "shadow", "wraith");
        JsonObject setup = root.get("setup").asObject();
        assertEquals("runner + wraith", setup.get("skirmishes").asObject().getString("baggins", null));
        assertEquals("hobbiton, 2", setup.get("controlled").asObject().getString("shadow", null));
        assertEquals("hobbiton, shire", setup.getString("path", null));
        assertEquals("marker(shadow, 2), control(hobbiton, shadow), liberate(hobbiton)",
                root.get("steps").asArray().get(0).asObject().getString("do", null));
    }

    @Test
    public void aPuzzlesPartsNameTheCardAsTheRestDo() {
        JsonObject root = JsonValue.readHjson("""
                {
                	cards: {
                		freeps: {
                			smeagol: 5_29
                		}
                	}
                	slots: {
                		trick: hand
                		cloak: {
                			zone: attached
                			on: smeagol
                		}
                	}
                	outcome: {
                		judge: {
                			at: endOf(archery)
                			win: {
                				type: canSpot
                				filter: alias(smeagol)
                			}
                		}
                		score: {
                			value: strength:smeagol
                		}
                	}
                	solutions: [
                		{
                			name: key
                			picks: {
                				trick: 1_2
                				cloak: 1_3
                			}
                			answers: [
                				use(smeagol)
                				play(trick)
                			]
                		}
                	]
                }
                """).asObject();
        AliasRename.rename(root, "smeagol", "gollum");
        assertEquals("gollum", root.get("slots").asObject().get("cloak").asObject().getString("on", null));
        assertEquals("alias(gollum)", root.get("outcome").asObject().get("judge").asObject().get("win").asObject().getString("filter", null));
        assertEquals("strength:gollum", root.get("outcome").asObject().get("score").asObject().getString("value", null));
        assertEquals("use(gollum)", root.get("solutions").asArray().get(0).asObject().get("answers").asArray().get(0).asString());
        //A slot is renamed as its card is: the slot, the solution's pick for it, and what names it
        AliasRename.rename(root, "trick", "gambit");
        assertEquals("hand", root.get("slots").asObject().getString("gambit", null));
        assertEquals("1_2", root.get("solutions").asArray().get(0).asObject().get("picks").asObject().getString("gambit", null));
        assertEquals("play(gambit)", root.get("solutions").asArray().get(0).asObject().get("answers").asArray().get(1).asString());
    }
}
