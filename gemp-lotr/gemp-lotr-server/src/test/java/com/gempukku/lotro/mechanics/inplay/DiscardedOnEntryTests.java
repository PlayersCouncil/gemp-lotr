package com.gempukku.lotro.mechanics.inplay;

import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.IsAdditionalRaceModifier;
import com.gempukku.lotro.logic.modifiers.VitalityModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * A card that leaves play as it is played.
 *
 * Comprehensive Rules 4.2, "playing a card", sets out the steps.  The card is placed and "is now in play"; then
 * "Respond to the playing of The Card (and to losing initiative if necessary)", which is where a card's own "When
 * you play..." and other cards' answers to the play happen; then its effects; and last "The card is played. ...
 * other cards are in play."  And "discard": "If a card is discarded when it comes into play, ignore any effects
 * triggered when it comes into play.  This includes effects from a minion's own game text (such as "When you
 * play this minion...") and effects from other cards in play (such as "Each time you play...")", with Watcher in
 * the Water and Goblin Runner as its example.
 *
 * Ketura, 2026-10-07 (F-0139), on a game that added Northern Ithilien's burdens for a Gollum the Watcher had
 * already discarded: "Step 5 is when Watcher would respond and discard the card, while it is not until Step 7
 * that 'The card is played', at which point we can interpret it to have fulfilled the cost in the case of
 * Northern Ithilien."  Ketura's steps: 4, the card is placed and is in play; 5, respond to the playing of the card and
 * to losing initiative; 6, perform effects; 7, the card is played.  And: "If anything in step 5 (a
 * Shadowplay/Unheeded combo is the classic example) causes it to exit play before it has made it to step 7, I
 * would count that as not fulfilling the cost."  A card's own "when you play this ... or discard it" is step 5
 * too: "I would also expect it to cause step 7 to not manifest, and therefore not count as executed."
 *
 * So a play that something else depends on, as a cost or as an "if you do", counts only if the card is still in
 * play when its play is complete.  What was paid to play the card stays paid, and the card is wherever it went.
 * The use is still offered where it can be seen to be pointless: "I think it should still be offered?"
 *
 * The game plays a card in the same steps.  The card is placed; the texts that watch the table all the time (the
 * Watcher's, a tentacle's "discarded if not at a marsh", Shadowfax's) act on it; and only then is its play
 * announced for anything to answer.  A card that is no longer in play by then, discarded or killed, is not
 * announced at all: neither its own "when you play" nor another card's "each time you play" nor a response
 * answers it.  What answers its discard still does.  The loss of initiative that the card's leaving the hand
 * causes is answered with the play: "Loss of initiative is step 5."
 *
 * And the cards "played this phase" are counted the same way.  Ketura on a card discarded as it enters: "now
 * that one's thornier.  I would think it would not"; and on one that a response removes: "Yes, the 'played this
 * phase' should also hold its counting until step 7, for consistency's sake."  While a play is being answered it
 * is counted, so that "the second time you play a Nazgul" can answer the second; once the play is over it is
 * counted only if the card stayed.
 *
 * On the Watcher played away from a marsh beside another minion: "it would have to evaluate the effects on
 * Watcher in the order in which they are printed, which would first discard the others and then discard itself".
 *
 * A card's own "When you play this ..., <do or pay something> or discard it" (Dunlending Rampager, Dunlending
 * Renegade, Wild Men of the Hills, Stampeding Chief) belongs to the card's entering play as well.  Ketura,
 * 2026-10-07, on a Rampager his own text had discarded and Ulaire Attea's "each time you play a minion": "a
 * self-discarding card should not trigger Attea or other 'each time you play' or 'when you play', that's pretty
 * straightforwardly the same situation as Runner, the only difference being where the reaction text is."  So the
 * choice in such a text is made before the play is announced, after the texts that watch the table have had
 * their turn.  If the card is discarded there, it was not played: nothing answers its play, a cost of playing it
 * is unpaid, and it is not a card played this phase.  If it stays, its play is announced and answered as usual.
 */
public class DiscardedOnEntryTests
{
	// The King-block path with Northern Ithilien as site 8.
	protected VirtualTableScenario GetNorthernIthilienScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gollum", "9_28");      // Gollum, Dark as Darkness: cost 2, site 3; abilities only
					// Watcher in the Water: While you can spot Watcher in the Water, discard all other minions
					// (except tentacles). ... is discarded if not at a marsh
					put("watcher", "2_73");
					// Evil-smelling Fens: Each site on the adventure path is a marsh. Here it keeps the Watcher
					// in play and does nothing else
					put("fens", "5_22");
					put("aragorn", "1_89");     // three companions beside Frodo: room for four threats
					put("gimli", "1_13");
					put("sam", "1_311");
				}},
				new HashMap<>()
				{{
					put("site1", "7_330");
					put("site2", "7_335");
					put("site3", "8_117");
					put("site4", "7_342");
					put("site5", "7_345");
					put("site6", "7_350");
					put("site7", "8_120");
					// Northern Ithilien. Shadow: Remove 2 threats and play Gollum from your discard pile to add
					// 2 burdens
					put("site8", "7_359");
					put("site9", "7_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetWeaponsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Weapons of Isengard. Shadow: Play an [isengard] archer to place an [isengard] token here
					put("weapons", "4_211");
					// Uruk Crossbowman: [isengard] archer, cost 3, vitality 2, site 5; an archery ability only
					put("crossbowman", "4_183");
					put("watcher", "2_73");
					put("fens", "5_22");

					// Shadowplay: Each time a minion is played, you may exert a Hobbit companion to exert that
					// minion (limit once per turn)
					put("shadowplay", "10_114");
					// Unheeded: Response: If a minion exerts, exert an unbound Hobbit to wound that minion
					put("unheeded", "8_115");
					put("merry", "1_302");      // Merry, Friend to Sam: a skirmish ability only
					put("pippin", "1_306");     // Pippin, Friend to Frodo: protects [shire] tales

					// Mordor Guard: cost 3, site 6. When this minion is discarded, remove 3 threats (required)
					put("mordorguard", "7_287");
					put("runner", "1_178");     // Goblin Runner: When you play this minion, you may add (2)
					// Foul Tentacle: cost 2. When you play this minion, you may play a tentacle from your draw
					// deck. ... is discarded if not at a marsh
					put("foul", "2_58");
					// Reaching Tentacle: cost 2, no text on being played; discarded if not at a marsh
					put("reaching", "19_19");
					// Great Day, Great Hour: Response: If an opponent plays a minion, exert a Dwarf who is
					// damage +X to exert that minion X times
					put("greatday", "10_1");
					put("gimli", "1_13");       // Gimli, Son of Gloin: damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers-block path: Ring of Isengard is its site 8.
	protected VirtualTableScenario GetRingOfIsengardScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Both: When you play this minion, the Free Peoples player may discard 2 cards from hand to
					// discard him. [dunland] Men, site 3, costs 1 and 2
					put("rampager", "5_1");
					put("renegade", "5_2");
					put("troop1", "1_177");     // Goblin Patrol Troop: no text; cards to draw
					put("troop2", "1_177");
					put("guard1", "1_7");       // Dwarf Guard: cards for the Free Peoples player to discard
					put("guard2", "1_7");
				}},
				VirtualTableScenario.TowersSites,   // site 8, Ring of Isengard. Shadow: Play a [dunland] minion to draw a card
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers-block path with Hornburg Armory as site 6.
	protected VirtualTableScenario GetHornburgArmoryScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk
					// Shadowfax, Greatest of the Mearas: Bearer must be a [gandalf] Wizard. Discard all other
					// possessions on bearer
					put("shadowfax", "17_24");
					put("pipe", "1_74");        // Gandalf's Pipe: possession, cost 0, bearer must be Gandalf
					put("gimli", "1_13");
					put("armor", "1_8");        // Dwarven Armor: possession, cost 0, bearer must be a Dwarf
					put("aragorn", "1_89");     // cards to draw
					put("boromir", "1_97");
				}},
				new HashMap<>()
				{{
					put("site1", "4_325");
					put("site2", "4_331");
					put("site3", "4_337");
					put("site4", "4_346");
					put("site5", "4_348");
					// Hornburg Armory: Sanctuary. Fellowship: Play a possession to draw a card
					put("site6", "4_354");
					put("site7", "102_71");
					put("site8", "4_358");
					put("site9", "4_363");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship-block path with Trollshaw Forest as site 2.
	protected VirtualTableScenario GetTrollshawForestScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");
					put("armor", "1_8");        // Dwarven Armor: possession, cost 0, bearer must be a Dwarf
					// Glimpse of Fate: Each time you lose initiative, make a minion strength -4 until the regroup
					// phase (required)
					put("glimpse", "10_12");
					put("guard1", "1_7");       // Dwarf Guard: cards to hold
					put("guard2", "1_7");
					put("guard3", "1_7");
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text

					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk
					put("shadowfax", "17_24");  // Shadowfax: Discard all other possessions on bearer
					put("pipe", "1_74");        // Gandalf's Pipe: possession, cost 0, bearer must be Gandalf
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					// Trollshaw Forest: Each time you play a possession or artifact on your companion, draw a
					// card (required)
					put("site2", "1_334");
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship-block path with Midgewater Moors as site 2.
	protected VirtualTableScenario GetMidgewaterMoorsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("watcher", "2_73");
					put("fens", "5_22");
					put("runner", "1_178");     // Goblin Runner: When you play this minion, you may add (2)
					put("guard1", "1_7");       // Dwarf Guard: cards for the Free Peoples player to hold
					put("guard2", "1_7");
					put("guard3", "1_7");
					// Dunlending Rampager: cost 1, site 3. When you play this minion, the Free Peoples player
					// may discard 2 cards from hand to discard him
					put("rampager", "5_1");
					// Stampeding Chief: unique, cost 8, site 4. If you play Stampeding Chief from your hand,
					// remove (3) and spot another [men] minion or discard Stampeding Chief
					put("chief1", "17_49");
					put("chief2", "17_49");
					// Swarthy Hillman: [men] minion, site 4; an archer only while you control a site
					put("hillman", "15_93");
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					// Midgewater Moors: Plains. Each time a minion is played, the Free Peoples player discards
					// a card from hand (required)
					put("site2", "1_333");
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetAtteaScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Ulaire Attea, Bestowed with Dominion: Each time you play a minion from hand, you may exert
					// Ulaire Attea to play another minion with the same twilight cost from your discard pile
					put("attea", "103_72");
					put("runner1", "1_178");    // Goblin Runner: cost 1
					put("runner2", "1_178");
					put("reaching", "19_19");   // Reaching Tentacle: cost 2; discarded if not at a marsh
					put("soldier", "1_271");    // Orc Soldier: cost 2
					// Dunlending Rampager: cost 1, site 3. When you play this minion, the Free Peoples player
					// may discard 2 cards from hand to discard him
					put("rampager1", "5_1");
					put("rampager2", "5_1");
					put("guard1", "1_7");       // Dwarf Guard: cards for the Free Peoples player to discard
					put("guard2", "1_7");
					put("guard3", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship-block path with Great Chasm as site 4.
	protected VirtualTableScenario GetGreatChasmScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("marksman1", "1_176");  // Goblin Marksman: [moria] archer, cost 3, site 4
					put("marksman2", "1_176");
					put("watcher", "2_73");
					put("fens", "5_22");
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "1_331");
					put("site3", "1_341");
					// Great Chasm: The twilight cost of the first [moria] archer played each Shadow phase is -2
					put("site4", "2_118");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers-block path with Deep of Helm as site 5.
	protected VirtualTableScenario GetDeepOfHelmScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("crossbowman1", "4_183");   // Uruk Crossbowman: Uruk-hai, cost 3, vitality 2, site 5
					put("crossbowman2", "4_183");
					put("crossbowman3", "4_183");
					put("shadowplay", "10_114");
					put("unheeded", "8_115");
					put("merry", "1_302");
					put("pippin", "1_306");
					// Dunlending Renegade: cost 2, site 3. When you play this minion, the Free Peoples player
					// may discard 2 cards from hand to discard him
					put("renegade1", "5_2");
					put("renegade2", "5_2");
					put("renegade3", "5_2");
					put("guard1", "1_7");       // Dwarf Guard: cards for the Free Peoples player to discard
					put("guard2", "1_7");
					put("guard3", "1_7");
					put("guard4", "1_7");
				}},
				new HashMap<>()
				{{
					put("site1", "4_325");
					put("site2", "4_331");
					put("site3", "4_337");
					put("site4", "4_346");
					// Deep of Helm: The twilight cost of the first Uruk-hai played at Deep of Helm each turn
					// is -3
					put("site5", "4_347");
					put("site6", "4_352");
					put("site7", "102_71");
					put("site8", "4_358");
					put("site9", "4_363");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PlayFromDiscardAsACostAddsNothingForACardDiscardedAsItEntersHoweverOftenItIsUsed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetNorthernIthilienScenario();

		var gollum = scn.GetShadowCard("gollum");
		var watcher = scn.GetShadowCard("watcher");
		var fens = scn.GetShadowCard("fens");
		scn.MoveCompanionsToTable("aragorn", "gimli", "sam");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCardsToSupportArea(fens);
		scn.MoveMinionsToTable(watcher);
		scn.MoveCardsToDiscard(gollum);
		scn.AddThreats(4);
		scn.SetTwilight(0);
		scn.FreepsPass();

		var site = scn.GetCurrentSite();
		assertEquals("Northern Ithilien", site.getBlueprint().getTitle());
		assertInZone(Zone.SHADOW_CHARACTERS, watcher);
		// 4 companions + 8 for Northern Ithilien
		assertEquals(12, scn.GetTwilight());
		assertEquals(4, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());
		// The use is offered though the Watcher will make it pointless.
		assertTrue(scn.ShadowActionAvailable(site));

		scn.ShadowUseCardAction(site);

		// Both costs were attempted and what they took stays taken: 2 threats, and (2) for Gollum.
		assertEquals(2, scn.GetThreats());
		assertEquals(10, scn.GetTwilight());
		// The Watcher discarded Gollum as he came into play, so he was not played and the cost is not fulfilled.
		assertInZone(Zone.DISCARD, gollum);
		assertInZone(Zone.SHADOW_CHARACTERS, watcher);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Gollum is in the discard pile again, so it can all be done again, to the same end.
		assertTrue(scn.ShadowActionAvailable(site));
		scn.ShadowUseCardAction(site);

		assertEquals(0, scn.GetThreats());
		assertEquals(8, scn.GetTwilight());
		assertInZone(Zone.DISCARD, gollum);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayFromHandAsACostPlacesNoTokenForACardDiscardedAsItEnters() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetWeaponsScenario();

		var weapons = scn.GetShadowCard("weapons");
		var crossbowman = scn.GetShadowCard("crossbowman");
		var watcher = scn.GetShadowCard("watcher");
		var fens = scn.GetShadowCard("fens");
		scn.MoveCardsToHand(crossbowman);

		scn.StartGame();
		scn.MoveCardsToSupportArea(weapons, fens);
		scn.MoveMinionsToTable(watcher);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 2 for Ettenmoors
		assertEquals(13, scn.GetTwilight());
		assertEquals(0, scn.GetCultureTokensOn(weapons));
		assertTrue(scn.ShadowActionAvailable(weapons));

		scn.ShadowUseCardAction(weapons);

		// 3 to play + 2 roaming, paid and not given back.
		assertEquals(8, scn.GetTwilight());
		assertInZone(Zone.DISCARD, crossbowman);
		assertEquals(0, scn.GetCultureTokensOn(weapons));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayAsACostIsPaidByACardThatIsStillInPlayWhenEveryAnswerToItsPlayIsFinished() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetWeaponsScenario();

		var weapons = scn.GetShadowCard("weapons");
		var crossbowman = scn.GetShadowCard("crossbowman");
		var shadowplay = scn.GetFreepsCard("shadowplay");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToSupportArea(shadowplay);
		scn.MoveCardsToHand(crossbowman);

		scn.StartGame();
		scn.MoveCardsToSupportArea(weapons);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(0, scn.GetCultureTokensOn(weapons));

		scn.ShadowUseCardAction(weapons);

		// The play is answered while the ability waits on it: Shadowplay exerts the Crossbowman, who lives.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(shadowplay));
		assertEquals(0, scn.GetCultureTokensOn(weapons));
		scn.FreepsChooseOptionalTrigger(shadowplay);
		scn.FreepsChooseCard(merry);

		assertEquals(1, scn.GetWoundsOn(crossbowman));
		assertInZone(Zone.SHADOW_CHARACTERS, crossbowman);
		assertEquals(1, scn.GetCultureTokensOn(weapons));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayAsACostIsNotPaidByACardThatAnAnswerToItsPlayRemoves() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetWeaponsScenario();

		var weapons = scn.GetShadowCard("weapons");
		var crossbowman = scn.GetShadowCard("crossbowman");
		var shadowplay = scn.GetFreepsCard("shadowplay");
		var unheeded = scn.GetFreepsCard("unheeded");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(merry, pippin);
		scn.MoveCardsToSupportArea(shadowplay);
		scn.MoveCardsToHand(unheeded);
		scn.MoveCardsToHand(crossbowman);

		scn.StartGame();
		scn.MoveCardsToSupportArea(weapons);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 3 companions + 2 for Ettenmoors
		assertEquals(15, scn.GetTwilight());
		assertEquals(0, scn.GetCultureTokensOn(weapons));

		scn.ShadowUseCardAction(weapons);

		// The Crossbowman is in play and his play is announced: Shadowplay answers it and exerts him,
		assertInZone(Zone.SHADOW_CHARACTERS, crossbowman);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(shadowplay));
		scn.FreepsChooseOptionalTrigger(shadowplay);
		scn.FreepsChooseCard(merry);
		assertEquals(1, scn.GetWoundsOn(crossbowman));

		// and Unheeded answers the exertion with the wound that kills him, all before his play is complete.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(unheeded));
		scn.FreepsChooseOptionalTrigger(unheeded);
		scn.FreepsChooseCard(pippin);

		assertInZone(Zone.DISCARD, crossbowman);
		// 3 to play + 2 roaming, paid and not given back.
		assertEquals(10, scn.GetTwilight());
		// He was not in play when his play was complete, so he was not played: no token.
		assertEquals(0, scn.GetCultureTokensOn(weapons));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayAsACostIsNotPaidByACardItsOwnWhenPlayedTextDiscards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingOfIsengardScenario();

		var rampager = scn.GetShadowCard("rampager");
		var renegade = scn.GetShadowCard("renegade");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCardsToHand(rampager, renegade);
		scn.MoveCardsToTopOfShadowDeck("troop1", "troop2");
		scn.MoveCardsToHand(guard1, guard2);
		scn.SetTwilight(10);
		scn.FreepsPass();

		var site = scn.GetCurrentSite();
		assertEquals("Ring of Isengard", site.getBlueprint().getTitle());
		int twilight = scn.GetTwilight();
		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();
		int freepsHand = scn.GetFreepsHandCount();

		// The Free Peoples player lets the Rampager be: he is played, and the site draws its card.
		scn.ShadowUseCardAction(site);
		scn.ShadowChooseCard(rampager);
		assertTrue(scn.FreepsDecisionAvailable("discard 2 cards from hand"));
		scn.FreepsChooseNo();

		assertInZone(Zone.SHADOW_CHARACTERS, rampager);
		assertEquals(twilight - 1, scn.GetTwilight());
		// one card played, one drawn
		assertEquals(hand, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The Free Peoples player discards 2 cards to discard the Renegade: his own text took him out of play
		// as he came into it, so he was not played and no card is drawn.
		scn.ShadowUseCardAction(site);
		assertTrue(scn.FreepsDecisionAvailable("discard 2 cards from hand"));
		scn.FreepsChooseYes();
		scn.FreepsChooseCards(guard1, guard2);

		assertInZone(Zone.DISCARD, renegade);
		assertEquals(freepsHand - 2, scn.GetFreepsHandCount());
		// 1 for the Rampager + 2 for the Renegade
		assertEquals(twilight - 3, scn.GetTwilight());
		assertEquals(hand - 1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayOnABearerAsACostDrawsNothingForACardDiscardedAsItIsAttached() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHornburgArmoryScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var shadowfax = scn.GetFreepsCard("shadowfax");
		var pipe = scn.GetFreepsCard("pipe");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(gandalf, gimli);
		scn.AttachCardsTo(gandalf, shadowfax);

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCardsToHand(pipe, armor);
		scn.MoveCardsToTopOfFreepsDeck("aragorn", "boromir");
		scn.FreepsRefreshDecision();

		var site = scn.GetCurrentSite();
		assertEquals("Hornburg Armory", site.getBlueprint().getTitle());
		int hand = scn.GetFreepsHandCount();
		int deck = scn.GetFreepsDeckCount();

		// The Armor stays on Gimli: a possession was played, and a card is drawn for it.
		scn.FreepsUseCardAction(site);
		scn.FreepsChooseCard(armor);

		assertAttachedTo(armor, gimli);
		// one card played, one drawn
		assertEquals(hand, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Shadowfax discards the Pipe as it is put on Gandalf: no possession was played, and nothing is drawn.
		scn.FreepsUseCardAction(site);

		assertInZone(Zone.DISCARD, pipe);
		assertAttachedTo(shadowfax, gandalf);
		assertEquals(hand - 1, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void StandingDiscardsSetOffByACardThatStaysAreDoneBeforeItsPlayIsAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMidgewaterMoorsScenario();

		var watcher = scn.GetShadowCard("watcher");
		var fens = scn.GetShadowCard("fens");
		var runner = scn.GetShadowCard("runner");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		scn.MoveCardsToHand(watcher);

		scn.StartGame();
		scn.MoveCardsToHand(guard1, guard2);
		scn.MoveCardsToSupportArea(fens);
		scn.MoveMinionsToTable(runner);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals("Midgewater Moors", scn.GetCurrentSite().getBlueprint().getTitle());
		// 10 set + 1 companion + 1 for Midgewater Moors
		assertEquals(12, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertEquals(2, scn.GetFreepsHandCount());

		scn.ShadowPlayCard(watcher);

		// The site answers the Watcher's being played, and by then the Watcher's own text has already cleared
		// the table: the two are not put to the Free Peoples player to order.
		assertFalse(scn.FreepsAwaitingRequiredResponseOrder());
		assertTrue(scn.FreepsDecisionAvailable("discard"));
		assertInZone(Zone.DISCARD, runner);
		assertInZone(Zone.SHADOW_CHARACTERS, watcher);
		// 4 to play + 2 roaming
		assertEquals(6, scn.GetTwilight());

		scn.FreepsChooseCard(guard1);

		assertInZone(Zone.DISCARD, guard1);
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void LossOfInitiativeIsAnsweredTogetherWithThePlayThatCausedIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTrollshawForestScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var glimpse = scn.GetFreepsCard("glimpse");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(glimpse);

		scn.StartGame();
		scn.SkipToSite(2);
		// Exactly four cards in hand, and one in the draw deck for the site to draw.
		scn.MoveCardsToHand(armor, scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"), scn.GetFreepsCard("guard3"));
		scn.MoveCardsToDiscard(scn.GetFreepsCard("gandalf"), scn.GetFreepsCard("shadowfax"), scn.GetFreepsCard("pipe"));
		scn.MoveCardsToTopOfDeck(scn.GetFreepsCard("troop"));
		scn.MoveMinionsToTable(troop);
		scn.FreepsRefreshDecision();

		assertEquals("Trollshaw Forest", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDeckCount());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(13, scn.GetStrength(troop));

		scn.FreepsPlayCard(armor);

		// The Armor has left the hand for good and three cards are left: initiative is lost.  That loss and the
		// play are answered at the same moment, so the site's trigger for the play and the condition's for the
		// loss of initiative are both waiting, and the Free Peoples player says which goes first.
		assertAttachedTo(armor, gimli);
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(1, scn.GetFreepsDeckCount());

		scn.FreepsResolveActionOrder("Trollshaw Forest");

		// The site drew its card, and the condition, the one trigger left, followed without being asked for.
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(9, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACardDiscardedAsItEntersSetsOffNeitherItsOwnWhenPlayedNorAnotherCardsEachTimeAMinionIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMidgewaterMoorsScenario();

		var watcher = scn.GetShadowCard("watcher");
		var fens = scn.GetShadowCard("fens");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(runner);

		scn.StartGame();
		scn.MoveCardsToHand(scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"));
		scn.MoveCardsToSupportArea(fens);
		scn.MoveMinionsToTable(watcher);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals("Midgewater Moors", scn.GetCurrentSite().getBlueprint().getTitle());
		// 10 set + 1 companion + 1 for Midgewater Moors
		assertEquals(12, scn.GetTwilight());
		assertEquals(2, scn.GetFreepsHandCount());

		scn.ShadowPlayCard(runner);

		// The Watcher discarded the Runner as it came into play.  What it cost stays paid, 1 to play + 2
		// roaming, and that is all: its own (2) is not added,
		assertInZone(Zone.DISCARD, runner);
		assertEquals(9, scn.GetTwilight());
		// the site does not answer a minion being played, and nobody is asked anything.
		assertEquals(2, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ACardDiscardedAsItEntersIsOfferedToNoResponseToItsBeingPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetWeaponsScenario();

		var watcher = scn.GetShadowCard("watcher");
		var fens = scn.GetShadowCard("fens");
		var reaching = scn.GetShadowCard("reaching");
		var runner = scn.GetShadowCard("runner");
		var greatday = scn.GetFreepsCard("greatday");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(greatday);
		scn.MoveCardsToHand(reaching, runner);

		scn.StartGame();
		scn.MoveCardsToSupportArea(fens);
		scn.MoveMinionsToTable(watcher);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// A tentacle is spared by the Watcher and every site is a marsh: it stays, and the response to its being
		// played is offered.
		scn.ShadowPlayCard(reaching);
		assertInZone(Zone.SHADOW_CHARACTERS, reaching);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(greatday));
		scn.FreepsDeclineOptionalTrigger();
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The Runner is discarded as it comes into play: there is no play to respond to.
		scn.ShadowPlayCard(runner);
		assertInZone(Zone.DISCARD, runner);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(greatday));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertInZone(Zone.HAND, greatday);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ACardThatDiscardsItselfAsItEntersIsOfferedNeitherItsOwnWhenPlayedNorAnotherPlayersEachTimeAMinionIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetWeaponsScenario();

		var foul = scn.GetShadowCard("foul");
		var reaching = scn.GetShadowCard("reaching");
		var runner = scn.GetShadowCard("runner");
		var shadowplay = scn.GetFreepsCard("shadowplay");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToSupportArea(shadowplay);
		scn.MoveCardsToHand(runner, foul);

		scn.StartGame();
		// a tentacle in the draw deck for the Foul Tentacle's own text to fetch
		scn.MoveCardsToTopOfDeck(reaching);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// A minion that stays: Shadowplay is offered for it.
		scn.ShadowPlayCard(runner);
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(shadowplay));
		scn.FreepsDeclineOptionalTrigger();
		assertTrue(scn.AwaitingShadowPhaseActions());
		int twilight = scn.GetTwilight();

		// Ettenmoors is no marsh: the Foul Tentacle is discarded as it comes into play.  Its own "when you play
		// this minion, you may" is not offered, and neither is the Free Peoples player's Shadowplay.
		scn.ShadowPlayCard(foul);
		assertInZone(Zone.DISCARD, foul);
		// 2 to play + 2 roaming
		assertEquals(twilight - 4, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertInZone(Zone.DECK, reaching);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ACardThatDiscardsItselfAsItEntersIsNotOfferedToItsOwnersEachTimeYouPlayAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAtteaScenario();

		var attea = scn.GetShadowCard("attea");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var reaching = scn.GetShadowCard("reaching");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(runner1, reaching);
		scn.MoveCardsToDiscard(runner2, soldier);

		scn.StartGame();
		scn.MoveMinionsToTable(attea);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// A minion that stays: Attea is offered a minion of its cost from the discard pile.
		scn.ShadowPlayCard(runner1);
		assertInZone(Zone.SHADOW_CHARACTERS, runner1);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(attea));
		scn.ShadowDeclineOptionalTrigger();
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Ettenmoors is no marsh: the tentacle is discarded as it comes into play, and Attea is not offered,
		// though an Orc of the tentacle's cost is in the discard pile.
		scn.ShadowPlayCard(reaching);
		assertInZone(Zone.DISCARD, reaching, soldier);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(attea));
		assertEquals(0, scn.GetWoundsOn(attea));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ACardDiscardedAsItIsAttachedSetsOffNoEachTimeYouPlayButItsLeavingTheHandStillLosesInitiative() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTrollshawForestScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var shadowfax = scn.GetFreepsCard("shadowfax");
		var pipe = scn.GetFreepsCard("pipe");
		var glimpse = scn.GetFreepsCard("glimpse");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, shadowfax);
		scn.MoveCardsToSupportArea(glimpse);

		scn.StartGame();
		scn.SkipToSite(2);
		// Exactly four cards in hand, and one in the draw deck that the site is not to draw.
		scn.MoveCardsToHand(pipe, scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"), scn.GetFreepsCard("guard3"));
		scn.MoveCardsToDiscard(scn.GetFreepsCard("gimli"), scn.GetFreepsCard("armor"));
		scn.MoveCardsToTopOfDeck(scn.GetFreepsCard("troop"));
		scn.MoveMinionsToTable(troop);
		scn.FreepsRefreshDecision();

		assertEquals("Trollshaw Forest", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDeckCount());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(13, scn.GetStrength(troop));

		scn.FreepsPlayCard(pipe);

		// Shadowfax discarded the Pipe as it was put on Gandalf: no possession was played on a companion, and
		// the site draws nothing.
		assertInZone(Zone.DISCARD, pipe);
		assertAttachedTo(shadowfax, gandalf);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDeckCount());
		// The Pipe left the hand all the same, and the loss of initiative is answered by itself.
		assertTrue(scn.ShadowHasInitiative());
		assertEquals(9, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACardDiscardedAsItEntersStillSetsOffWhatAnswersItsDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetWeaponsScenario();

		var watcher = scn.GetShadowCard("watcher");
		var fens = scn.GetShadowCard("fens");
		var mordorguard = scn.GetShadowCard("mordorguard");
		scn.MoveCompanionsToTable("merry", "pippin");
		scn.MoveCardsToHand(mordorguard);

		scn.StartGame();
		scn.MoveCardsToSupportArea(fens);
		scn.MoveMinionsToTable(watcher);
		scn.AddThreats(3);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 3 companions + 2 for Ettenmoors
		assertEquals(15, scn.GetTwilight());
		assertEquals(3, scn.GetThreats());

		scn.ShadowPlayCard(mordorguard);

		// 3 to play + 2 roaming
		assertEquals(10, scn.GetTwilight());
		assertInZone(Zone.DISCARD, mordorguard);
		// "When this minion is discarded, remove 3 threats."
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ACharacterKilledAsItEntersIsNotAnnouncedAsPlayedEither() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMidgewaterMoorsScenario();

		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(runner);

		scn.StartGame();
		scn.MoveCardsToHand(scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"));
		// Every minion has one vitality less: the Runner, vitality 1, arrives with none.
		scn.ApplyAdHocModifier(new VitalityModifier(null, CardType.MINION, null, -1));
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 1 for Midgewater Moors
		assertEquals(12, scn.GetTwilight());

		scn.ShadowPlayCard(runner);

		// Killed and not discarded, but gone before its play could be announced all the same: 1 to play + 2
		// roaming is paid, its (2) is not added, and the site does not make the Free Peoples player discard.
		assertInZone(Zone.DISCARD, runner);
		assertEquals(9, scn.GetTwilight());
		assertEquals(2, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WatcherPlayedAwayFromAMarshDiscardsTheOtherMinionsAndThenItselfAndItsPlayIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMidgewaterMoorsScenario();

		var watcher = scn.GetShadowCard("watcher");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(watcher);

		scn.StartGame();
		scn.MoveCardsToHand(scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"));
		scn.MoveMinionsToTable(runner);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Plains, and no Evil-smelling Fens to make it a marsh.
		assertEquals("Midgewater Moors", scn.GetCurrentSite().getBlueprint().getTitle());
		// 10 set + 1 companion + 1 for Midgewater Moors
		assertEquals(12, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, runner);

		scn.ShadowPlayCard(watcher);

		// Its texts act in the order they are printed: the other minions are discarded, then the Watcher
		// itself.  Nobody is asked to order them.
		assertInZone(Zone.DISCARD, runner, watcher);
		// 4 to play + 2 roaming
		assertEquals(6, scn.GetTwilight());
		// The Watcher was gone before its play could be announced: the site does not answer it.
		assertEquals(2, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ACardDiscardedAsItEntersIsNotACardPlayedThisPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGreatChasmScenario();

		var marksman1 = scn.GetShadowCard("marksman1");
		var marksman2 = scn.GetShadowCard("marksman2");
		var watcher = scn.GetShadowCard("watcher");
		var fens = scn.GetShadowCard("fens");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToHand(marksman1, marksman2);
		scn.MoveCardsToSupportArea(fens);
		scn.MoveMinionsToTable(watcher);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals("Great Chasm", scn.GetCurrentSite().getBlueprint().getTitle());
		// 10 set + 1 companion + 4 for Great Chasm
		assertEquals(15, scn.GetTwilight());
		// 3 - 2 for the first [moria] archer played this Shadow phase
		assertEquals(1, scn.GetTwilightCost(marksman1));
		assertEquals(1, scn.GetTwilightCost(marksman2));

		scn.ShadowPlayCard(marksman1);

		assertInZone(Zone.DISCARD, marksman1);
		assertEquals(14, scn.GetTwilight());
		// The Watcher discarded that Marksman as it came into play, so no [moria] archer has been played this
		// phase yet: the next one is still the first.
		assertEquals(1, scn.GetTwilightCost(marksman2));

		scn.ShadowPlayCard(marksman2);

		assertInZone(Zone.DISCARD, marksman2);
		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ACardThatAnAnswerToItsPlayRemovesIsNotACardPlayedThisPhaseAndOneThatStaysIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDeepOfHelmScenario();

		var crossbowman1 = scn.GetShadowCard("crossbowman1");
		var crossbowman2 = scn.GetShadowCard("crossbowman2");
		var crossbowman3 = scn.GetShadowCard("crossbowman3");
		var shadowplay = scn.GetFreepsCard("shadowplay");
		var unheeded = scn.GetFreepsCard("unheeded");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(merry, pippin);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToSupportArea(shadowplay);
		scn.MoveCardsToHand(unheeded);
		scn.MoveCardsToHand(crossbowman1, crossbowman2, crossbowman3);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals("Deep of Helm", scn.GetCurrentSite().getBlueprint().getTitle());
		int twilight = scn.GetTwilight();
		// 3 - 3 for the first Uruk-hai played here
		assertEquals(0, scn.GetTwilightCost(crossbowman1));
		assertEquals(0, scn.GetTwilightCost(crossbowman2));

		// The first Crossbowman's play is announced, and Shadowplay and Unheeded kill him before it is complete.
		scn.ShadowPlayCard(crossbowman1);
		assertInZone(Zone.SHADOW_CHARACTERS, crossbowman1);
		scn.FreepsChooseOptionalTrigger(shadowplay);
		scn.FreepsChooseCard(merry);
		scn.FreepsChooseOptionalTrigger(unheeded);
		scn.FreepsChooseCard(pippin);

		assertInZone(Zone.DISCARD, crossbowman1);
		assertTrue(scn.AwaitingShadowPhaseActions());
		// He was not in play when his play was complete, so no Uruk-hai has been played yet: the next is still
		// the first.
		assertEquals(0, scn.GetTwilightCost(crossbowman2));

		// Shadowplay has done its part; with it gone nothing answers the next play.
		scn.MoveCardsToDiscard(shadowplay);
		scn.ShadowPlayCard(crossbowman2);

		assertInZone(Zone.SHADOW_CHARACTERS, crossbowman2);
		assertEquals(twilight, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// That one stayed, and is the first Uruk-hai played: the third costs what is printed on him.
		assertEquals(3, scn.GetTwilightCost(crossbowman3));
	}

	@Test
	public void ACardItsOwnWhenPlayedTextDiscardsIsNotOfferedToItsOwnersEachTimeYouPlayAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAtteaScenario();

		var attea = scn.GetShadowCard("attea");
		var rampager1 = scn.GetShadowCard("rampager1");
		var rampager2 = scn.GetShadowCard("rampager2");
		var runner = scn.GetShadowCard("runner1");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var guard3 = scn.GetFreepsCard("guard3");
		scn.MoveCardsToHand(rampager1, rampager2);
		// a minion of the Rampager's cost for Attea to play
		scn.MoveCardsToDiscard(runner);

		scn.StartGame();
		scn.MoveCardsToHand(guard1, guard2, guard3);
		scn.MoveMinionsToTable(attea);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// The Free Peoples player is asked before anything answers the play, and lets the first Rampager be: he
		// was played, and only now is Attea offered.
		scn.ShadowPlayCard(rampager1);
		assertTrue(scn.FreepsDecisionAvailable("discard 2 cards from hand"));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(attea));
		scn.FreepsChooseNo();

		assertInZone(Zone.SHADOW_CHARACTERS, rampager1);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(attea));
		scn.ShadowDeclineOptionalTrigger();
		assertTrue(scn.AwaitingShadowPhaseActions());
		int twilight = scn.GetTwilight();

		// The Free Peoples player discards 2 cards to discard the second: his own text discarded him as he came
		// into play, so he was not played, and Attea is not offered though the Runner is still there to play.
		scn.ShadowPlayCard(rampager2);
		assertTrue(scn.FreepsDecisionAvailable("discard 2 cards from hand"));
		scn.FreepsChooseYes();
		scn.FreepsChooseCards(guard1, guard2);

		assertInZone(Zone.DISCARD, rampager2, runner, guard1, guard2);
		assertInZone(Zone.HAND, guard3);
		// what he cost stays paid: 1 to play + 2 roaming
		assertEquals(twilight - 3, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(attea));
		assertEquals(0, scn.GetWoundsOn(attea));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ACardItsOwnWhenPlayedTextDiscardsSetsOffNoRequiredEachTimeAMinionIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMidgewaterMoorsScenario();

		var chief1 = scn.GetShadowCard("chief1");
		var chief2 = scn.GetShadowCard("chief2");
		var hillman = scn.GetShadowCard("hillman");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var guard3 = scn.GetFreepsCard("guard3");
		scn.MoveCardsToHand(chief1, chief2);

		scn.StartGame();
		scn.MoveCardsToHand(guard1, guard2, guard3);
		scn.MoveMinionsToTable(hillman);
		scn.SetTwilight(30);
		scn.FreepsPass();

		assertEquals("Midgewater Moors", scn.GetCurrentSite().getBlueprint().getTitle());
		// 30 set + 1 companion + 1 for Midgewater Moors
		assertEquals(32, scn.GetTwilight());
		assertEquals(3, scn.GetFreepsHandCount());

		// The Shadow player has the Chief's choice before anything answers the play, and discards him: he was
		// not played, and the site does not make the Free Peoples player discard a card.
		scn.ShadowPlayCard(chief1);
		assertTrue(scn.ShadowChoiceAvailable("Remove (3)"));
		assertTrue(scn.ShadowChoiceAvailable("Discard Stampeding Chief"));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		scn.ShadowChoose("Discard Stampeding Chief");

		assertInZone(Zone.DISCARD, chief1);
		// 8 to play + 2 roaming, and no (3)
		assertEquals(22, scn.GetTwilight());
		assertEquals(3, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The second Chief's price is paid and he stays: his play is announced, and the site answers it.
		scn.ShadowPlayCard(chief2);
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		scn.ShadowChoose("Remove (3)");

		assertInZone(Zone.SHADOW_CHARACTERS, chief2);
		// 8 to play + 2 roaming + (3)
		assertEquals(9, scn.GetTwilight());
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard1));
		scn.FreepsChooseCard(guard1);
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ACardDiscardedAsItEntersIsNotAskedTheChoiceInItsOwnWhenPlayedText() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMidgewaterMoorsScenario();

		var watcher = scn.GetShadowCard("watcher");
		var fens = scn.GetShadowCard("fens");
		var rampager = scn.GetShadowCard("rampager");
		scn.MoveCardsToHand(rampager);

		scn.StartGame();
		// two cards the Free Peoples player could discard to discard the Rampager, were he asked
		scn.MoveCardsToHand(scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"));
		scn.MoveCardsToSupportArea(fens);
		scn.MoveMinionsToTable(watcher);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 1 for Midgewater Moors
		assertEquals(12, scn.GetTwilight());

		scn.ShadowPlayCard(rampager);

		// The Watcher discards the Rampager first, as it does any minion; his own text has nothing left to ask
		// about, and the site has no play to answer.
		assertInZone(Zone.DISCARD, rampager);
		// 1 to play + 2 roaming
		assertEquals(9, scn.GetTwilight());
		assertEquals(2, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ACardItsOwnWhenPlayedTextDiscardsIsNotACardPlayedThisPhase() throws DecisionResultInvalidException, CardNotFoundException, InvalidCardDefinitionException {
		var scn = GetDeepOfHelmScenario();

		var renegade1 = scn.GetShadowCard("renegade1");
		var renegade2 = scn.GetShadowCard("renegade2");
		var renegade3 = scn.GetShadowCard("renegade3");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToHand(renegade1, renegade2, renegade3);
		scn.MoveCardsToHand(guard1, guard2, scn.GetFreepsCard("guard3"), scn.GetFreepsCard("guard4"));
		// No card that discards itself on being played is an Uruk-hai, and Deep of Helm counts Uruk-hai.
		scn.ApplyAdHocModifier(new IsAdditionalRaceModifier(null, scn.GenerateShadowFilter("name(Dunlending Renegade)"), null, Race.URUK_HAI));
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals("Deep of Helm", scn.GetCurrentSite().getBlueprint().getTitle());
		int twilight = scn.GetTwilight();
		// 2 - 3 for the first Uruk-hai played here
		assertEquals(0, scn.GetTwilightCost(renegade1));
		assertEquals(0, scn.GetTwilightCost(renegade2));

		// The Free Peoples player discards 2 cards to discard the first Renegade.
		scn.ShadowPlayCard(renegade1);
		scn.FreepsChooseYes();
		scn.FreepsChooseCards(guard1, guard2);

		assertInZone(Zone.DISCARD, renegade1);
		assertTrue(scn.AwaitingShadowPhaseActions());
		// His own text discarded him as he came into play, so no Uruk-hai has been played yet: the next is
		// still the first.
		assertEquals(0, scn.GetTwilightCost(renegade2));

		// The second is let be.
		scn.ShadowPlayCard(renegade2);
		scn.FreepsChooseNo();

		assertInZone(Zone.SHADOW_CHARACTERS, renegade2);
		assertEquals(twilight, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// That one stayed, and is the first Uruk-hai played: the third costs what is printed on him.
		assertEquals(2, scn.GetTwilightCost(renegade3));
	}
}
