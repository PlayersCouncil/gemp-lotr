package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_234_Tests
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
					put("comp7", "1_53");
					put("celeborn", "1_34");   // Celeborn, Lord of Lorien: an ally, not a companion

					put("nertea", "1_234");
					put("attea", "1_229");     // Ulaire Attea, Keeper of Dol Guldur: [wraith] Nazgul, cost 6, site 3
					put("savage", "1_151");    // Uruk Savage: [isengard] Uruk-hai, cost 2, site 5
					put("twk", "2_85");        // The Witch-king, Lord of the Nazgul: [wraith] Nazgul, cost 8, site 3
					put("otsea", "1_235");     // Ulaire Otsea, Lieutenant of Morgul: a minion kept in hand
					put("enquea", "1_231");    // Ulaire Enquea, Lieutenant of Morgul: only the Free Peoples player's copy is used
					put("armory", "1_173");    // Goblin Armory: a Shadow condition, cost 0, played to the support area
					put("runner", "1_178");    // Goblin Runner: [moria] Orc, cost 1
					put("rit", "101_40");      // Ringwraith in Twilight: [wraith] Nazgul, cost 4
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
		 * Subtype: Nazgûl
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: When you play Úlairë Nertëa, for each companion over 4, you may play 1 minion from your discard pile.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("nertea");

		assertEquals("Úlairë Nertëa", card.getBlueprint().getTitle());
		assertEquals("Messenger of Dol Guldur", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());

	}

	@Test
	public void NerteaDoesNotTriggerWith4Companions() throws DecisionResultInvalidException, CardNotFoundException {
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

		// Frodo and three Elves are four companions; Celeborn is an ally.
		assertInZone(Zone.SHADOW_CHARACTERS, nertea);
		assertEquals(20, scn.GetTwilight()); // 4 + 2 roaming
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInDiscard(attea);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NerteaPlays1MinionIf5Companions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("comp2", "comp3", "comp4", "comp5");

		var nertea = scn.GetShadowCard("nertea");
		var attea = scn.GetShadowCard("attea");
		var savage = scn.GetShadowCard("savage");
		var twk = scn.GetShadowCard("twk");
		var otsea = scn.GetShadowCard("otsea");
		var armory = scn.GetShadowCard("armory");
		// A minion that could be played, in the Free Peoples player's discard pile: not "your" discard pile. (Its
		// title is none of the Shadow player's cards here, since a choice from a pile is read by title.)
		var freepsEnquea = scn.GetFreepsCard("enquea");
		scn.MoveCardsToHand(nertea, otsea);
		scn.MoveCardsToDiscard(attea, savage, twk, armory, freepsEnquea);

		scn.StartGame();
		scn.SetTwilight(19);
		scn.FreepsPass();

		assertEquals(26, scn.GetTwilight()); // 19 set + 5 companions + 2 for Bree Gate
		scn.ShadowPlayCard(nertea);
		assertEquals(20, scn.GetTwilight()); // 4 + 2 roaming

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(nertea));
		scn.ShadowAcceptOptionalTrigger();

		// The offer is the play: once it is taken, a minion is chosen.
		assertTrue(scn.ShadowHasCardChoiceAvailable(attea, savage, twk));
		// A Shadow card that is no minion; a minion in hand; a minion in the other player's discard pile.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(armory, otsea, freepsEnquea));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(savage);

		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertEquals(16, scn.GetTwilight()); // 2 + 2 roaming
		// Five companions are one over four: no second play, though the pool would pay for either Nazgul.
		assertInDiscard(attea, twk, armory, freepsEnquea);
		assertInHand(otsea);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NerteaPlays2MinionsIf6Companions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("comp2", "comp3", "comp4", "comp5", "comp6");

		var nertea = scn.GetShadowCard("nertea");
		var attea = scn.GetShadowCard("attea");
		var savage = scn.GetShadowCard("savage");
		var twk = scn.GetShadowCard("twk");
		var otsea = scn.GetShadowCard("otsea");
		var armory = scn.GetShadowCard("armory");
		var freepsEnquea = scn.GetFreepsCard("enquea");
		scn.MoveCardsToHand(nertea, otsea);
		scn.MoveCardsToDiscard(attea, savage, twk, armory, freepsEnquea);

		scn.StartGame();
		scn.SetTwilight(30);
		scn.FreepsPass();

		assertEquals(38, scn.GetTwilight()); // 30 set + 6 companions + 2 for Bree Gate
		scn.ShadowPlayCard(nertea);
		assertEquals(32, scn.GetTwilight()); // 4 + 2 roaming

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(nertea));
		scn.ShadowAcceptOptionalTrigger();

		// The Shadow player decides which is played first: the Uruk Savage, and then Ulaire Attea.
		assertTrue(scn.ShadowHasCardChoiceAvailable(attea, savage, twk));
		scn.ShadowChooseCardBPFromSelection(savage);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertInDiscard(attea, twk);
		assertEquals(28, scn.GetTwilight()); // 2 + 2 roaming

		// The second play is a "may" of its own: a choice of none or one.
		assertTrue(scn.ShadowHasCardChoiceAvailable(attea, twk));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(armory, otsea, freepsEnquea));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCardBPFromSelection(attea);
		assertInZone(Zone.SHADOW_CHARACTERS, attea);
		assertEquals(20, scn.GetTwilight()); // 6 + 2 roaming

		// Six companions are two over four: no third play, though the pool would pay for the Witch-king.
		assertInDiscard(twk, armory, freepsEnquea);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CancelingNerteaAfterFirstMinionDoesntAskAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("comp2", "comp3", "comp4", "comp5", "comp6");

		var nertea = scn.GetShadowCard("nertea");
		var attea = scn.GetShadowCard("attea");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(nertea);
		scn.MoveCardsToDiscard(attea, savage);

		scn.StartGame();
		scn.SetTwilight(30);
		scn.FreepsPass();

		scn.ShadowPlayCard(nertea);
		assertEquals(32, scn.GetTwilight()); // 30 set + 6 companions + 2 for Bree Gate - 6 for Nertea
		// Saying no to the first play ends it: nothing is played, and the second is not asked.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(nertea));
		scn.ShadowDeclineOptionalTrigger();

		assertInDiscard(attea, savage);
		assertEquals(32, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NerteaStopsPlayingMinionsIfUserDeclinesPartwayThrough() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("comp2", "comp3", "comp4", "comp5", "comp6");

		var nertea = scn.GetShadowCard("nertea");
		var attea = scn.GetShadowCard("attea");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(nertea);
		scn.MoveCardsToDiscard(attea, savage);

		scn.StartGame();
		scn.SetTwilight(30);
		scn.FreepsPass();

		scn.ShadowPlayCard(nertea);
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasCardChoiceAvailable(attea, savage));
		scn.ShadowChooseCardBPFromSelection(attea);
		assertInZone(Zone.SHADOW_CHARACTERS, attea);
		assertEquals(24, scn.GetTwilight()); // 32 after Nertea - 6 - 2 roaming

		// The Savage is the one candidate left, and the second play is still asked: a choice of none or one.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage));
		scn.ShadowDeclineChoosing();

		assertInDiscard(savage);
		assertInZone(Zone.SHADOW_CHARACTERS, attea);
		assertEquals(24, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningOnePlayPartwayThroughDoesNotEndTheLaterOnes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("comp2", "comp3", "comp4", "comp5", "comp6", "comp7");

		var nertea = scn.GetShadowCard("nertea");
		var attea = scn.GetShadowCard("attea");
		var savage = scn.GetShadowCard("savage");
		var twk = scn.GetShadowCard("twk");
		scn.MoveCardsToHand(nertea);
		scn.MoveCardsToDiscard(attea, savage, twk);

		scn.StartGame();
		scn.SetTwilight(60);
		scn.FreepsPass();

		assertEquals(69, scn.GetTwilight()); // 60 set + 7 companions + 2 for Bree Gate
		scn.ShadowPlayCard(nertea);
		assertEquals(63, scn.GetTwilight()); // 4 + 2 roaming

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(nertea));
		scn.ShadowAcceptOptionalTrigger();

		// Seven companions are three over four: three plays, and each is a "may" of its own.
		assertTrue(scn.ShadowHasCardChoiceAvailable(attea, savage, twk));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(savage);
		assertEquals(59, scn.GetTwilight()); // 2 + 2 roaming

		assertTrue(scn.ShadowHasCardChoiceAvailable(attea, twk));
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowDeclineChoosing();
		assertInDiscard(attea, twk);
		assertEquals(59, scn.GetTwilight());

		// The second was declined, and the third is still put.
		assertTrue(scn.ShadowHasCardChoiceAvailable(attea, twk));
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(attea);
		assertInZone(Zone.SHADOW_CHARACTERS, savage, attea);
		assertEquals(51, scn.GetTwilight()); // 6 + 2 roaming

		// Three plays and no fourth, though the pool would pay for the Witch-king.
		assertInDiscard(twk);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherMinionBeingPlayedWhileNerteaIsInPlayDoesNotTriggerIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("comp2", "comp3", "comp4", "comp5");

		var nertea = scn.GetShadowCard("nertea");
		var attea = scn.GetShadowCard("attea");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(savage);
		scn.MoveCardsToDiscard(attea);

		scn.StartGame();
		// The board that offers the trigger when Nertea herself is played (five companions, a minion in the
		// discard pile the pool can pay for), with her already in play.
		scn.MoveMinionsToTable(nertea);
		scn.SetTwilight(30);
		scn.FreepsPass();

		assertEquals(37, scn.GetTwilight()); // 30 set + 5 companions + 2 for Bree Gate
		assertInZone(Zone.SHADOW_CHARACTERS, nertea);
		scn.ShadowPlayCard(savage);

		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertEquals(33, scn.GetTwilight()); // 2 + 2 roaming
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInDiscard(attea);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWhenNoMinionInTheDiscardPileCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("comp2", "comp3", "comp4", "comp5", "comp6");

		var nertea = scn.GetShadowCard("nertea");
		var twk = scn.GetShadowCard("twk");
		var otsea = scn.GetShadowCard("otsea");
		var runner = scn.GetShadowCard("runner");
		var armory = scn.GetShadowCard("armory");
		var freepsEnquea = scn.GetFreepsCard("enquea");
		scn.MoveCardsToHand(nertea, otsea, runner);
		scn.MoveCardsToDiscard(twk, armory, freepsEnquea);

		scn.StartGame();
		scn.SetTwilight(7);
		scn.FreepsPass();

		assertEquals(15, scn.GetTwilight()); // 7 set + 6 companions + 2 for Bree Gate
		scn.ShadowPlayCard(nertea);
		assertEquals(9, scn.GetTwilight()); // 4 + 2 roaming

		// The Witch-king would cost 8 + 2 roaming, one more than the pool holds.  The minions in hand, the
		// condition, and the minion in the other player's discard pile could all be paid for.
		assertInZone(Zone.SHADOW_CHARACTERS, nertea);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInDiscard(twk, armory, freepsEnquea);
		assertInHand(otsea, runner);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	// Converted from legacy AT test
	@Test
	public void NerteaCannotPlayFromDiscardAtTheGreatRiver() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		scn.MoveCompanionsToTable("comp2", "comp3", "comp4", "comp5");

		var greatriver = scn.GetShadowSite(7);

		var twk = scn.GetShadowCard("twk");
		var attea = scn.GetShadowCard("attea");
		var nertea = scn.GetShadowCard("nertea");
		scn.MoveCardsToShadowDiscard("runner", "rit", "twk", "attea");
		scn.MoveCardsToDiscard(twk);
		scn.MoveCardsToHand(nertea);

		scn.StartGame();

		scn.SkipToSite(6);
		scn.SetTwilight(30);
		scn.FreepsPass();

		assertEquals(greatriver, scn.GetCurrentSite());

		scn.ShadowPlayCard(nertea);
		assertInZone(Zone.SHADOW_CHARACTERS, nertea);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInDiscard(twk, attea);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
