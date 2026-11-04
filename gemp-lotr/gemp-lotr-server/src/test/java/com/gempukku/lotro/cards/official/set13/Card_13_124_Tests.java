package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_124_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eowyn", "13_124");
					// Éomer, Sister-son of Théoden: [rohan] Man companion; a cost discount, and a start-of-turn heal of a
					// [rohan] ally that is withheld with no wounded ally in play
					put("eomer", "4_266");
					// Théoden, Son of Thengel: [rohan] Man companion; a regroup ability used only on purpose
					put("theoden", "4_292");
					// Sturdy Shield: [rohan] possession, cost 2, on a [rohan] Man; strength +1 while no opponent controls
					// a site, and a regroup ability used only on purpose. Not unique.
					put("shield", "15_141");
					put("shield2", "15_141");
					put("coat", "1_101");       // Coat of Mail: [gondor] possession, cost 1, on a Man; an overwhelm rule only
					// Brought Down from Inside: [rohan] condition, cost 2, on a [rohan] Man; a transfer at the start of each
					// skirmish and a return to hand at the end of the turn
					put("brought", "19_24");
					// Freely Across Our Land: [rohan] support-area condition; "Regroup: Discard this from play or remove a
					// token from here to play a [rohan] possession from your discard pile on your [rohan] companion."
					put("freely", "13_127");
					// Doom Drove Them: [rohan] support-area condition, not unique, that takes [rohan] tokens by its own
					// fellowship ability; its abilities are used only on purpose
					put("doom", "8_86");
					put("doom2", "8_86");
					// Heirs of Gondor: [gondor] support-area condition that takes a [gondor] token as it is played; a
					// skirmish ability used only on purpose
					put("heirs", "13_69");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Rivendell Waterfall as site 3, with Aragorn, a ranger: the move limit is 3 there, so a turn has two regroup moves.
	protected VirtualTableScenario GetWaterfallScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eowyn", "13_124");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a ranger; a maneuver ability used only on purpose
					put("doom", "8_86");        // Doom Drove Them: [rohan] support-area condition that takes [rohan] tokens
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "1_331");
					put("site3", "1_342");   // Rivendell Waterfall
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

	@Test
	public void EowynStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Éowyn, Restless Warrior
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: Each possession you play on Éowyn during the fellowship phase is twilight cost -1.<br>Each time the fellowship moves during the regroup phase, you may reinforce a [rohan] token (or reinforce 2 [rohan] tokens if you can spot Éomer or Théoden).
		*/

		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");

		assertEquals("Éowyn", eowyn.getBlueprint().getTitle());
		assertEquals("Restless Warrior", eowyn.getBlueprint().getSubtitle());
		assertTrue(eowyn.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, eowyn.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, eowyn.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, eowyn.getBlueprint().getCardType());
		assertEquals(Race.MAN, eowyn.getBlueprint().getRace());
		assertEquals(2, eowyn.getBlueprint().getTwilightCost());
		assertEquals(6, eowyn.getBlueprint().getStrength());
		assertEquals(3, eowyn.getBlueprint().getVitality());
		assertEquals(6, eowyn.getBlueprint().getResistance());
	}

	@Test
	public void EachPossessionPlayedOnHerInTheFellowshipPhaseIsTwilightCostMinusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var eomer = scn.GetFreepsCard("eomer");
		var shield = scn.GetFreepsCard("shield");
		var shield2 = scn.GetFreepsCard("shield2");
		var coat = scn.GetFreepsCard("coat");
		var brought = scn.GetFreepsCard("brought");
		scn.MoveCompanionsToTable(eowyn, eomer);
		scn.MoveCardsToHand(shield, shield2, coat, brought);

		// Fellowship sites: Edoras Hall, the default site 1, would cut each [rohan] possession by 1 as well.
		scn.StartGame();
		assertInHand(shield, shield2, coat, brought);
		assertEquals(0, scn.GetTwilight());

		// A [rohan] possession: 2 - 1.
		scn.FreepsPlayCard(shield);
		assertTrue(scn.FreepsHasCardChoiceAvailable(eowyn, eomer));
		scn.FreepsChooseCard(eowyn);
		assertAttachedTo(shield, eowyn);
		assertEquals(1, scn.GetTwilight());

		// A [gondor] possession: 1 - 1.
		scn.FreepsPlayCard(coat);
		scn.FreepsChooseCard(eowyn);
		assertAttachedTo(coat, eowyn);
		assertEquals(1, scn.GetTwilight());

		// A condition is no possession: its full 2.
		scn.FreepsPlayCard(brought);
		scn.FreepsChooseCard(eowyn);
		assertAttachedTo(brought, eowyn);
		assertEquals(3, scn.GetTwilight());

		// A possession played on Éomer: its full 2 (Éowyn bears a shield already, so he is its only bearer).
		scn.FreepsPlayCard(shield2);
		assertAttachedTo(shield2, eomer);
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void APossessionPlayedOnHerInTheRegroupPhaseCostsItsFullTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var eomer = scn.GetFreepsCard("eomer");
		var shield = scn.GetFreepsCard("shield");
		var freely = scn.GetFreepsCard("freely");
		scn.MoveCompanionsToTable(eowyn, eomer);
		// With no token left on it, Freely Across Our Land pays by discarding itself.
		scn.MoveCardsToSupportArea(freely);
		scn.MoveCardsToDiscard(shield);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, freely);
		assertEquals(0, scn.GetCultureTokensOn(freely));
		assertInDiscard(shield);

		// No minion: the Shadow phase is followed by the regroup phase.
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.FreepsActionAvailable(freely));
		int twilight = scn.GetTwilight();

		scn.FreepsUseCardAction(freely);
		assertInDiscard(freely);
		assertTrue(scn.FreepsHasCardChoiceAvailable(eowyn, eomer));
		scn.FreepsChooseCard(eowyn);
		assertAttachedTo(shield, eowyn);
		// Sturdy Shield's full 2: the discount is for the fellowship phase only.
		assertEquals(twilight + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ARegroupMoveOffersToReinforceARohanTokenAndTheFellowshipPhaseMoveDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var doom = scn.GetFreepsCard("doom");
		var doom2 = scn.GetFreepsCard("doom2");
		var heirs = scn.GetFreepsCard("heirs");
		scn.MoveCompanionsToTable(eowyn);
		scn.MoveCardsToSupportArea(doom, doom2, heirs);

		scn.StartGame();
		scn.AddTokensToCard(doom, 1);
		scn.AddTokensToCard(doom2, 1);
		scn.AddTokensToCard(heirs, 1);
		assertEquals(1, scn.GetCultureTokensOn(doom));
		assertEquals(1, scn.GetCultureTokensOn(doom2));
		assertEquals(1, scn.GetCultureTokensOn(heirs));

		// The move at the end of the fellowship phase is no regroup move.
		scn.FreepsPass();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(eowyn));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToPhase(Phase.REGROUP);
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(eowyn));
		scn.FreepsAcceptOptionalTrigger();

		// Either card with a [rohan] token; the [gondor] token is no [rohan] one.
		assertTrue(scn.FreepsHasCardChoiceAvailable(doom, doom2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(heirs));
		scn.FreepsChooseCard(doom2);

		// Neither Éomer nor Théoden to spot (Éowyn is neither): one token, nothing more asked.
		assertEquals(1, scn.GetCultureTokensOn(doom));
		assertEquals(2, scn.GetCultureTokensOn(doom2));
		assertEquals(1, scn.GetCultureTokensOn(heirs));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheReinforcementLeavesTheTokensAsTheyWere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var doom = scn.GetFreepsCard("doom");
		scn.MoveCompanionsToTable(eowyn);
		scn.MoveCardsToSupportArea(doom);

		scn.StartGame();
		scn.AddTokensToCard(doom, 1);
		assertEquals(1, scn.GetCultureTokensOn(doom));

		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(eowyn));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(1, scn.GetCultureTokensOn(doom));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoRohanTokenInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var doom = scn.GetFreepsCard("doom");
		var heirs = scn.GetFreepsCard("heirs");
		scn.MoveCompanionsToTable(eowyn);
		// A [rohan] card with no token, and a [gondor] token.
		scn.MoveCardsToSupportArea(doom, heirs);

		scn.StartGame();
		scn.AddTokensToCard(heirs, 1);
		assertEquals(0, scn.GetCultureTokensOn(doom));
		assertEquals(1, scn.GetCultureTokensOn(heirs));

		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(eowyn));
		assertEquals(0, scn.GetCultureTokensOn(doom));
		assertEquals(1, scn.GetCultureTokensOn(heirs));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithEomerToSpotItReinforcesTwoRohanTokens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var eomer = scn.GetFreepsCard("eomer");
		var doom = scn.GetFreepsCard("doom");
		var doom2 = scn.GetFreepsCard("doom2");
		scn.MoveCompanionsToTable(eowyn, eomer);
		scn.MoveCardsToSupportArea(doom, doom2);

		scn.StartGame();
		scn.AddTokensToCard(doom, 1);
		scn.AddTokensToCard(doom2, 1);
		assertEquals(1, scn.GetCultureTokensOn(doom));
		assertEquals(1, scn.GetCultureTokensOn(doom2));

		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(eowyn));
		scn.FreepsAcceptOptionalTrigger();

		// Two tokens, one at a time, each to a card of the player's choice; the second is a "you may" of its own
		// (Q-0339: 0 to 2).
		assertTrue(scn.FreepsHasCardChoiceAvailable(doom, doom2));
		scn.FreepsChooseCard(doom);
		assertEquals(2, scn.GetCultureTokensOn(doom));
		assertTrue(scn.FreepsDecisionAvailable("reinforce"));
		scn.FreepsChooseYes();
		assertTrue(scn.FreepsHasCardChoiceAvailable(doom, doom2));
		scn.FreepsChooseCard(doom2);

		assertEquals(2, scn.GetCultureTokensOn(doom));
		assertEquals(2, scn.GetCultureTokensOn(doom2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithEomerToSpotThePlayerMayStopAfterOneToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var eomer = scn.GetFreepsCard("eomer");
		var doom = scn.GetFreepsCard("doom");
		var doom2 = scn.GetFreepsCard("doom2");
		scn.MoveCompanionsToTable(eowyn, eomer);
		scn.MoveCardsToSupportArea(doom, doom2);

		scn.StartGame();
		scn.AddTokensToCard(doom, 1);
		scn.AddTokensToCard(doom2, 1);
		assertEquals(1, scn.GetCultureTokensOn(doom));
		assertEquals(1, scn.GetCultureTokensOn(doom2));

		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(eowyn));
		scn.FreepsAcceptOptionalTrigger();

		scn.FreepsChooseCard(doom);
		assertEquals(2, scn.GetCultureTokensOn(doom));
		// Q-0339 (Ketura): "0-2 ... if the requirement is met": the second token may be declined.
		assertTrue(scn.FreepsDecisionAvailable("reinforce"));
		scn.FreepsChooseNo();

		assertEquals(2, scn.GetCultureTokensOn(doom));
		assertEquals(1, scn.GetCultureTokensOn(doom2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTheodenToSpotItReinforcesTwoRohanTokens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var theoden = scn.GetFreepsCard("theoden");
		var doom = scn.GetFreepsCard("doom");
		var doom2 = scn.GetFreepsCard("doom2");
		scn.MoveCompanionsToTable(eowyn, theoden);
		scn.MoveCardsToSupportArea(doom, doom2);

		scn.StartGame();
		scn.AddTokensToCard(doom, 1);
		scn.AddTokensToCard(doom2, 1);
		assertEquals(1, scn.GetCultureTokensOn(doom));
		assertEquals(1, scn.GetCultureTokensOn(doom2));

		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(eowyn));
		scn.FreepsAcceptOptionalTrigger();

		// Both tokens on one card.
		scn.FreepsChooseCard(doom);
		assertEquals(2, scn.GetCultureTokensOn(doom));
		scn.FreepsChooseYes();
		scn.FreepsChooseCard(doom);

		assertEquals(3, scn.GetCultureTokensOn(doom));
		assertEquals(1, scn.GetCultureTokensOn(doom2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachOfTwoRegroupMovesInOneTurnOffersTheReinforcement() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetWaterfallScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var doom = scn.GetFreepsCard("doom");
		scn.MoveCompanionsToTable(eowyn, aragorn);
		scn.MoveCardsToSupportArea(doom);

		scn.StartGame();
		scn.AddTokensToCard(doom, 1);
		assertEquals(1, scn.GetCultureTokensOn(doom));

		scn.FreepsPass();
		scn.SkipToMovementDecision();

		// The first regroup move, to Rivendell Waterfall: the only [rohan] token is reinforced unasked.
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(eowyn));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetCultureTokensOn(doom));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetMoveLimit());

		scn.SkipToMovementDecision();

		// The second regroup move of the same turn.
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(eowyn));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(3, scn.GetCultureTokensOn(doom));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetMoveCount());
	}
}
