package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.IsAdditionalCultureModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_054_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hollowing", "53_54");
					put("raiding", "1_158");    // Uruk-hai Raiding Party: [isengard] Uruk-hai, Damage +1 and nothing else
					put("servant", "3_58");     // Isengard Servant: [isengard] Orc, a regroup ability used only on purpose
					put("servant2", "3_58");
					put("armory", "1_157");     // Uruk-hai Armory: [isengard] condition, support area
					put("runner", "1_178");     // Goblin Runner: [moria] Orc
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc minion, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HollowingofIsengardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Hollowing of Isengard
		 * Unique: false
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot an [isengard] minion.
		 * 	At the start of your Shadow phase, you may discard an [isengard] card from hand to add (3) (or (5) if
		 * 	that card was an Orc).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hollowing");

		assertEquals("Hollowing of Isengard", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAnIsengardMinionIsSpottedThenPlaysToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hollowing = scn.GetShadowCard("hollowing");
		var raiding = scn.GetShadowCard("raiding");
		var armory = scn.GetShadowCard("armory");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(hollowing, raiding);

		scn.StartGame();
		// A [moria] minion and an [isengard] card that is no minion: neither is an [isengard] minion.
		scn.MoveMinionsToTable(troop);
		scn.MoveCardsToSupportArea(armory);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 2 for Ettenmoors
		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(hollowing));

		scn.ShadowPlayCard(raiding);
		// 4 to play + 2 roaming
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(hollowing));

		scn.ShadowPlayCard(hollowing);
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, hollowing);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CanBePlayedSpottingAnIsengardOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hollowing = scn.GetShadowCard("hollowing");
		var servant = scn.GetShadowCard("servant");
		scn.MoveCardsToHand(hollowing);

		scn.StartGame();
		scn.MoveMinionsToTable(servant);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(hollowing));

		scn.ShadowPlayCard(hollowing);
		assertEquals(11, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, hollowing);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void StartOfShadowPhaseDiscardsAnIsengardCardFromHandToAdd3ForAnUrukHai() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hollowing = scn.GetShadowCard("hollowing");
		var raiding = scn.GetShadowCard("raiding");
		var servant = scn.GetShadowCard("servant");
		var servant2 = scn.GetShadowCard("servant2");
		var armory = scn.GetShadowCard("armory");
		var runner = scn.GetShadowCard("runner");
		var freepsRaiding = scn.GetFreepsCard("raiding");
		scn.MoveCardsToSupportArea(hollowing);
		scn.MoveCardsToHand(raiding, servant, armory, runner, freepsRaiding);

		scn.StartGame();
		// An [isengard] card in play, not in hand.
		scn.MoveMinionsToTable(servant2);
		scn.FreepsPass();

		// 1 companion + 2 for Ettenmoors
		assertEquals(3, scn.GetTwilight());
		assertEquals(4, scn.GetShadowHandCount());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hollowing));

		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasCardChoiceAvailable(raiding, servant, armory));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner, servant2, hollowing, freepsRaiding));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCard(raiding);
		assertInDiscard(raiding);
		assertInHand(servant, armory, runner);
		assertEquals(3, scn.GetShadowHandCount());
		// An Uruk-hai is no Orc: 3 + 3
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DiscardingAnIsengardOrcAdds5WithoutAsking() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hollowing = scn.GetShadowCard("hollowing");
		var raiding = scn.GetShadowCard("raiding");
		var servant = scn.GetShadowCard("servant");
		scn.MoveCardsToSupportArea(hollowing);
		scn.MoveCardsToHand(raiding, servant);

		scn.StartGame();
		scn.FreepsPass();

		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hollowing));

		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasCardChoiceAvailable(raiding, servant));
		scn.ShadowChooseCard(servant);
		assertInDiscard(servant);
		assertInHand(raiding);
		// 3 + 5
		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnOrcOfAnotherCultureMadeIsengardIsOfferedAndAdds5() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hollowing = scn.GetShadowCard("hollowing");
		var armory = scn.GetShadowCard("armory");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(hollowing);
		scn.MoveCardsToHand(armory, runner);
		// Goblin Runner is a [moria] Orc, here [isengard] as well: the 5 goes by race.
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, runner, null, Culture.ISENGARD));

		scn.StartGame();
		scn.FreepsPass();

		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hollowing));

		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasCardChoiceAvailable(armory, runner));
		scn.ShadowChooseCard(runner);
		assertInDiscard(runner);
		assertInHand(armory);
		// 3 + 5
		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MayDeclineAndIsNotOfferedAtTheStartOfTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hollowing = scn.GetShadowCard("hollowing");
		var raiding = scn.GetShadowCard("raiding");
		var servant = scn.GetShadowCard("servant");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(hollowing);
		scn.MoveCardsToHand(raiding, servant);

		scn.StartGame();
		// A minion, so that there is a maneuver phase.
		scn.MoveMinionsToTable(troop);
		scn.FreepsPass();

		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hollowing));

		scn.ShadowDeclineOptionalTrigger();
		assertEquals(3, scn.GetTwilight());
		assertInHand(raiding, servant);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPass();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(3, scn.GetTwilight());
		assertEquals(2, scn.GetShadowHandCount());
	}

	@Test
	public void NotOfferedWithNoIsengardCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hollowing = scn.GetShadowCard("hollowing");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(hollowing);
		scn.MoveCardsToHand(runner);

		scn.StartGame();
		scn.FreepsPass();

		assertEquals(3, scn.GetTwilight());
		assertEquals(1, scn.GetShadowHandCount());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
