package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_234_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("comp2", "1_53");      // Lorien Elf: companions to count, with Frodo
					put("comp3", "1_53");
					put("comp4", "1_53");
					put("comp5", "1_53");
					put("comp6", "1_53");
					put("celeborn", "1_34");   // Celeborn, Lord of Lorien: an ally, not a companion

					put("nertea", "51_234");
					put("attea", "1_229");     // Ulaire Attea, Keeper of Dol Guldur: unique [wraith] Nazgul, cost 6
					put("gorbag", "7_180");    // Gorbag, Lieutenant of Cirith Ungol: unique [wraith] Orc, cost 5
					put("twk", "2_85");        // The Witch-king, Lord of the Nazgul: unique [wraith] Nazgul, cost 8
					put("otsea", "1_235");     // Ulaire Otsea, Lieutenant of Morgul: unique [wraith] Nazgul, kept in hand
					put("rit", "101_40");      // Ringwraith in Twilight: a [wraith] Nazgul that is not unique
					put("khamul", "103_61");   // Ulaire Attea, Khamul of the East: a unique Nazgul that is [raider]
					put("runner", "1_178");    // Goblin Runner: a [moria] Orc, neither unique nor [wraith]
					put("enquea", "1_231");    // Ulaire Enquea, Lieutenant of Morgul: a unique [wraith] Nazgul, cost 6; only the Free Peoples player's copy is used
					put("stronghold", "7_206"); // Stronghold of Minas Morgul: a unique [wraith] condition, cost 1, nothing to do until a skirmish is won
				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "1_327");     // Bree Gate: Shadow number 2; a ranger raises the move limit
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "3_118");     // The Great River: no card may be played from a discard pile there
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireNerteaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Úlairë Nertëa, Messenger of Dol Guldur
		 * Unique: True
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Nazgul
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: When you play Úlairë Nertëa, for each companion over 4, you may play a unique [Wraith] minion from your discard pile.
		 */

		var scn = GetScenario();

		var nertea = scn.GetFreepsCard("nertea");

		assertEquals("Úlairë Nertëa", nertea.getBlueprint().getTitle());
		assertEquals("Messenger of Dol Guldur", nertea.getBlueprint().getSubtitle());
		assertTrue(nertea.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, nertea.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, nertea.getBlueprint().getCulture());
		assertEquals(CardType.MINION, nertea.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, nertea.getBlueprint().getRace());
		assertEquals(4, nertea.getBlueprint().getTwilightCost());
		assertEquals(9, nertea.getBlueprint().getStrength());
		assertEquals(2, nertea.getBlueprint().getVitality());
		assertEquals(3, nertea.getBlueprint().getSiteNumber());
	}

	@Test
	public void FourCompanionsAndAnAllyDoNotTrigger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("comp2", "comp3", "comp4");
		scn.MoveAlliesToTable("celeborn");

		var nertea = scn.GetShadowCard("nertea");
		var attea = scn.GetShadowCard("attea");
		scn.MoveCardsToHand(nertea);
		scn.MoveCardsToDiscard(attea);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertEquals(26, scn.GetTwilight()); // 20 set + 4 companions + 2 for Bree Gate
		scn.ShadowPlayCard(nertea);

		assertInZone(Zone.SHADOW_CHARACTERS, nertea);
		assertEquals(20, scn.GetTwilight()); // 4 + 2 roaming
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInDiscard(attea);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void FiveCompanionsPlayOneUniqueWraithMinionFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("comp2", "comp3", "comp4", "comp5");

		var nertea = scn.GetShadowCard("nertea");
		var attea = scn.GetShadowCard("attea");
		var gorbag = scn.GetShadowCard("gorbag");
		var twk = scn.GetShadowCard("twk");
		var otsea = scn.GetShadowCard("otsea");
		var rit = scn.GetShadowCard("rit");
		var khamul = scn.GetShadowCard("khamul");
		var runner = scn.GetShadowCard("runner");
		var stronghold = scn.GetShadowCard("stronghold");
		// A card that could be played, in the Free Peoples player's discard pile: not "your" discard pile. (Its
		// title is none of the Shadow player's, because the choice names cards by title and could not tell two copies apart.)
		var freepsEnquea = scn.GetFreepsCard("enquea");
		scn.MoveCardsToHand(nertea, otsea);
		scn.MoveCardsToDiscard(attea, gorbag, twk, rit, khamul, runner, stronghold, freepsEnquea);

		scn.StartGame();
		scn.SetTwilight(8);
		scn.FreepsPass();

		assertEquals(15, scn.GetTwilight()); // 8 set + 5 companions + 2 for Bree Gate
		scn.ShadowPlayCard(nertea);
		assertEquals(9, scn.GetTwilight()); // 4 + 2 roaming

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(nertea));
		scn.ShadowAcceptOptionalTrigger();

		// The Witch-king would cost 8 + 2 roaming, one more than the pool holds.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(attea, gorbag));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(twk, otsea, rit, khamul, runner, stronghold, freepsEnquea));
		scn.ShadowChooseCardBPFromSelection(attea);

		assertInZone(Zone.SHADOW_CHARACTERS, attea);
		assertEquals(1, scn.GetTwilight()); // 6 + 2 roaming
		assertInDiscard(gorbag, twk, rit, khamul, runner, stronghold, freepsEnquea);
		assertInHand(otsea);
		// Five companions are one over four: one play. The pool is 1 here and Gorbag would cost 5 + 2 roaming, so
		// this board cannot tell one play from two: AcceptingTheTriggerGoesStraightToTheChoiceOfMinion, with (30)
		// in the pool and a second minion left in the discard pile, is the test that shows the cap.
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SixCompanionsPlayTwoInTheOrderTheShadowPlayerChooses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("comp2", "comp3", "comp4", "comp5", "comp6");

		var nertea = scn.GetShadowCard("nertea");
		var attea = scn.GetShadowCard("attea");
		var gorbag = scn.GetShadowCard("gorbag");
		var twk = scn.GetShadowCard("twk");
		scn.MoveCardsToHand(nertea);
		scn.MoveCardsToDiscard(attea, gorbag, twk);

		scn.StartGame();
		scn.SetTwilight(30);
		scn.FreepsPass();

		assertEquals(38, scn.GetTwilight()); // 30 set + 6 companions + 2 for Bree Gate
		scn.ShadowPlayCard(nertea);
		assertEquals(32, scn.GetTwilight()); // 4 + 2 roaming

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(nertea));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasCardChoiceAvailable(attea, gorbag, twk));
		scn.ShadowChooseCardBPFromSelection(gorbag);
		assertInZone(Zone.SHADOW_CHARACTERS, gorbag);
		assertInDiscard(attea, twk);
		assertEquals(25, scn.GetTwilight()); // 5 + 2 roaming

		// The second play is a "may" of its own: a choice of none or one.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(attea, twk));
		scn.ShadowChooseCardBPFromSelection(attea);
		assertInZone(Zone.SHADOW_CHARACTERS, attea);
		assertInDiscard(twk);
		assertEquals(17, scn.GetTwilight()); // 6 + 2 roaming

		// Six companions are two over four: no third play, though the Witch-king could still be played.
		assertInDiscard(twk);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NoSecondPlayIsAskedOnceNoUniqueWraithMinionIsLeftThatCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("comp2", "comp3", "comp4", "comp5", "comp6");

		var nertea = scn.GetShadowCard("nertea");
		var attea = scn.GetShadowCard("attea");
		var rit = scn.GetShadowCard("rit");
		var khamul = scn.GetShadowCard("khamul");
		var runner = scn.GetShadowCard("runner");
		var stronghold = scn.GetShadowCard("stronghold");
		scn.MoveCardsToHand(nertea);
		scn.MoveCardsToDiscard(attea, rit, khamul, runner, stronghold);

		scn.StartGame();
		scn.SetTwilight(30);
		scn.FreepsPass();

		scn.ShadowPlayCard(nertea);
		assertEquals(32, scn.GetTwilight()); // 30 set + 6 companions + 2 for Bree Gate - 4 for Nertea - 2 roaming
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(nertea));
		// Attea is the one candidate, so he is chosen without asking.
		scn.ShadowAcceptOptionalTrigger();
		assertInZone(Zone.SHADOW_CHARACTERS, attea);
		assertEquals(24, scn.GetTwilight()); // 6 + 2 roaming

		// Six companions allow a second play, but no unique [wraith] minion is left: the four cards that are not
		// unique, not [wraith] or not a minion could all be played, and are not offered.
		assertInDiscard(rit, khamul, runner, stronghold);
		assertEquals(24, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheFirstPlayPlaysNothingAndAsksNoMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("comp2", "comp3", "comp4", "comp5", "comp6");

		var nertea = scn.GetShadowCard("nertea");
		var attea = scn.GetShadowCard("attea");
		var gorbag = scn.GetShadowCard("gorbag");
		scn.MoveCardsToHand(nertea);
		scn.MoveCardsToDiscard(attea, gorbag);

		scn.StartGame();
		scn.SetTwilight(30);
		scn.FreepsPass();

		scn.ShadowPlayCard(nertea);
		assertEquals(32, scn.GetTwilight()); // 30 set + 6 companions + 2 for Bree Gate - 6 for Nertea
		// The first play is the trigger itself: declining it plays nothing and asks nothing more.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(nertea));
		scn.ShadowDeclineOptionalTrigger();

		assertInDiscard(attea, gorbag);
		assertEquals(32, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheSecondPlayKeepsTheFirst() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("comp2", "comp3", "comp4", "comp5", "comp6");

		var nertea = scn.GetShadowCard("nertea");
		var attea = scn.GetShadowCard("attea");
		var gorbag = scn.GetShadowCard("gorbag");
		scn.MoveCardsToHand(nertea);
		scn.MoveCardsToDiscard(attea, gorbag);

		scn.StartGame();
		scn.SetTwilight(30);
		scn.FreepsPass();

		scn.ShadowPlayCard(nertea);
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCardBPFromSelection(attea);
		assertInZone(Zone.SHADOW_CHARACTERS, attea);
		assertEquals(24, scn.GetTwilight()); // 32 after Nertea - 6 - 2 roaming

		// Gorbag is the one candidate left, and the second play is still asked: a choice of none or one.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(gorbag));
		scn.ShadowDeclineChoosing();

		assertInDiscard(gorbag);
		assertInZone(Zone.SHADOW_CHARACTERS, attea);
		assertEquals(24, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWhereNoCardMayBePlayedFromADiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("comp2", "comp3", "comp4", "comp5");

		var greatriver = scn.GetShadowSite(7);

		var nertea = scn.GetShadowCard("nertea");
		var attea = scn.GetShadowCard("attea");
		var gorbag = scn.GetShadowCard("gorbag");
		scn.MoveCardsToHand(nertea);
		scn.MoveCardsToDiscard(attea, gorbag);

		scn.StartGame();

		scn.SkipToSite(6);
		scn.SetTwilight(30);
		scn.FreepsPass();

		assertEquals(greatriver, scn.GetCurrentSite());

		scn.ShadowPlayCard(nertea);
		assertInZone(Zone.SHADOW_CHARACTERS, nertea);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInDiscard(attea, gorbag);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWhenNoUniqueWraithMinionInTheDiscardPileCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("comp2", "comp3", "comp4", "comp5", "comp6");

		var nertea = scn.GetShadowCard("nertea");
		var attea = scn.GetShadowCard("attea");
		var rit = scn.GetShadowCard("rit");
		var khamul = scn.GetShadowCard("khamul");
		var runner = scn.GetShadowCard("runner");
		var stronghold = scn.GetShadowCard("stronghold");
		scn.MoveCardsToHand(nertea, attea);
		scn.MoveCardsToDiscard(rit, khamul, runner, stronghold);

		scn.StartGame();
		scn.SetTwilight(30);
		scn.FreepsPass();

		// Playing from the discard pile is a cost: with nothing there the card can play, there is nothing to offer.
		scn.ShadowPlayCard(nertea);
		assertInZone(Zone.SHADOW_CHARACTERS, nertea);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInDiscard(rit, khamul, runner, stronghold);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AcceptingTheTriggerGoesStraightToTheChoiceOfMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("comp2", "comp3", "comp4", "comp5");

		var nertea = scn.GetShadowCard("nertea");
		var attea = scn.GetShadowCard("attea");
		var gorbag = scn.GetShadowCard("gorbag");
		scn.MoveCardsToHand(nertea);
		scn.MoveCardsToDiscard(attea, gorbag);

		scn.StartGame();
		scn.SetTwilight(30);
		scn.FreepsPass();

		scn.ShadowPlayCard(nertea);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(nertea));
		scn.ShadowAcceptOptionalTrigger();

		assertFalse(scn.ShadowDecisionAvailable("play a unique WRAITH minion"));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(attea, gorbag));
		scn.ShadowChooseCardBPFromSelection(gorbag);
		assertInZone(Zone.SHADOW_CHARACTERS, gorbag);
		assertInDiscard(attea);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
