package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_052_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("underfoot", "10_52");
					put("explorer", "4_250");   // Southron Explorer: [raider] Man, a Southron; strength 5, vitality 3; a regroup ability used only on purpose
					put("axeman", "4_224");     // Easterling Axeman: [raider] Man, an Easterling; cost 2, site 4, strength 6, vitality 2; stronger and fierce only at 2 burdens
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] minion who is a Nazgul; strength 12, fierce; his text needs 3 burdens
					put("brigand", "4_10");     // Dunlending Brigand: a Man minion who is [dunland]; strength 8, no text
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: the other player's Man; strength 8
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6; a skirmish ability used only on purpose

					// Cards for the hands and the draw decks: Dunlending Brigands again.
					put("chaff1", "4_10");
					put("chaff2", "4_10");
					put("chaff3", "4_10");
					put("chaff4", "4_10");
					put("chaff5", "4_10");
					put("chaff6", "4_10");
					put("chaff7", "4_10");
					put("chaff8", "4_10");
					put("chaff9", "4_10");
					put("explorer2", "4_250");  // a second Southron Explorer, a [raider] Man assigned to no skirmish; with the Brigands, filler for the hands and decks
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UnderFootStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Under Foot
		 * Unique: True
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Shadow:</b> If you have initiative, spot a [raider] Man and discard this condition to reconcile your hand.
		 * <b>Skirmish:</b> Discard this condition to make a [raider] Man strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("underfoot");

		assertEquals("Under Foot", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ShadowAbilityDiscardsItToReconcileTheShadowPlayersHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var foot = scn.GetShadowCard("underfoot");
		var explorer = scn.GetShadowCard("explorer");
		var chaff1 = scn.GetShadowCard("chaff1");
		var chaff2 = scn.GetShadowCard("chaff2");
		var chaff3 = scn.GetShadowCard("chaff3");
		// Out of the Shadow player's deck: all that is left there is seven Brigands.
		scn.MoveCardsToShadowDiscard("axeman", "khamul", "brigand", "aragorn", "gimli");

		scn.StartGame();
		scn.MoveCardsToSupportArea(foot);
		scn.MoveMinionsToTable(explorer);
		scn.MoveCardsToHand(chaff1, chaff2, chaff3);
		assertInZone(Zone.SUPPORT, foot);
		assertInZone(Zone.HAND, chaff1, chaff2, chaff3);
		scn.FreepsPass();

		assertTrue(scn.ShadowHasInitiative());
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(7, scn.GetShadowDeckCount());
		int freepsHand = scn.GetFreepsHandCount();
		int freepsDeck = scn.GetFreepsDeckCount();
		assertInZone(Zone.SUPPORT, foot);
		assertTrue(scn.ShadowActionAvailable(foot));

		// The condition is discarded, and then a reconcile: he may discard a card, and then draws up to eight.
		scn.ShadowUseCardAction(foot);
		assertInZone(Zone.DISCARD, foot);
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(chaff1);
		assertInZone(Zone.DISCARD, chaff1);
		assertInZone(Zone.HAND, chaff2, chaff3);
		// 2 kept + 6 drawn
		assertEquals(8, scn.GetShadowHandCount());
		assertEquals(1, scn.GetShadowDeckCount());
		// It is his hand that is reconciled, not the other player's.
		assertEquals(freepsHand, scn.GetFreepsHandCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWhileTheFreePeoplesPlayerHasFourCardsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var foot = scn.GetShadowCard("underfoot");
		var explorer = scn.GetShadowCard("explorer");
		var freepsCard = scn.GetFreepsCard("chaff1");

		scn.StartGame();
		scn.MoveCardsToSupportArea(foot);
		scn.MoveMinionsToTable(explorer);
		// The Shadow player's own hand is large: it is the Free Peoples player's that counts.
		scn.MoveCardsToHand(scn.GetShadowCard("chaff1"), scn.GetShadowCard("chaff2"), scn.GetShadowCard("chaff3"),
				scn.GetShadowCard("chaff4"), scn.GetShadowCard("chaff5"));
		scn.MoveCardsToHand(freepsCard, scn.GetFreepsCard("chaff2"), scn.GetFreepsCard("chaff3"), scn.GetFreepsCard("chaff4"));
		assertEquals(5, scn.GetShadowHandCount());
		scn.FreepsPass();

		assertEquals(4, scn.GetFreepsHandCount());
		assertFalse(scn.ShadowHasInitiative());
		assertFalse(scn.ShadowActionAvailable(foot));

		scn.MoveCardsToDiscard(freepsCard);
		scn.ShadowRefreshDecision();
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertTrue(scn.ShadowActionAvailable(foot));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedUntilARaiderWhoIsAManIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var foot = scn.GetShadowCard("underfoot");
		var axeman = scn.GetShadowCard("axeman");
		var khamul = scn.GetShadowCard("khamul");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveCardsToSupportArea(foot);
		scn.MoveMinionsToTable(khamul, brigand);
		scn.MoveCardsToHand(axeman, scn.GetShadowCard("chaff1"));
		scn.SetTwilight(6);
		assertInHand(axeman);
		scn.FreepsPass();

		// A [raider] Nazgul, a [dunland] Man, the other player's [gondor] Man, and Under Foot itself, a [raider] card.
		assertTrue(scn.ShadowHasInitiative());
		assertInZone(Zone.SHADOW_CHARACTERS, khamul, brigand);
		assertFalse(scn.ShadowActionAvailable(foot));

		// 6 set + 2 companions + 2 for the site
		assertEquals(10, scn.GetTwilight());
		scn.ShadowPlayCard(axeman);
		// 2 to play + 2 roaming (his site is 4)
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(foot));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var foot = scn.GetShadowCard("underfoot");
		var explorer = scn.GetShadowCard("explorer");

		scn.StartGame();
		scn.MoveCardsToSupportArea(foot);
		scn.MoveMinionsToTable(explorer);
		assertInZone(Zone.SUPPORT, foot);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Initiative held and the Southron in play, one phase later.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowHasInitiative());
		assertInZone(Zone.SHADOW_CHARACTERS, explorer);
		assertFalse(scn.ShadowActionAvailable(foot));
		assertInZone(Zone.SUPPORT, foot);
	}

	@Test
	public void SkirmishAbilityDiscardsItToMakeARaiderManStrengthPlus2ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var foot = scn.GetShadowCard("underfoot");
		var explorer = scn.GetShadowCard("explorer");
		var axeman = scn.GetShadowCard("axeman");
		var explorer2 = scn.GetShadowCard("explorer2");
		var khamul = scn.GetShadowCard("khamul");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveCardsToSupportArea(foot);
		scn.MoveMinionsToTable(explorer, axeman, khamul, brigand, explorer2);
		assertInZone(Zone.SUPPORT, foot);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// Not an assignment action.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(foot));
		scn.ShadowPass();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { aragorn, explorer }, new PhysicalCardImpl[] { gimli, axeman });
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(foot));
		assertEquals(5, scn.GetStrength(explorer));
		assertEquals(6, scn.GetStrength(axeman));

		assertInZone(Zone.SUPPORT, foot);
		scn.ShadowUseCardAction(foot);
		assertInZone(Zone.DISCARD, foot);
		// The Southron in this skirmish, the Easterling waiting for the next and a second Southron assigned to no
		// skirmish; not the [raider] Nazgul, the [dunland] Man or the other player's Man.
		assertTrue(scn.ShadowHasCardChoiceAvailable(explorer, axeman, explorer2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(khamul, brigand, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(axeman);
		assertEquals(8, scn.GetStrength(axeman));
		assertEquals(5, scn.GetStrength(explorer));
		assertEquals(5, scn.GetStrength(explorer2));
		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(8, scn.GetStrength(brigand));

		// Aragorn (8) beats the Southron (5). In the Easterling's own skirmish the bonus is gone.
		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(6, scn.GetStrength(axeman));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityWithNoRaiderManInPlayDiscardsItForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var foot = scn.GetShadowCard("underfoot");
		var khamul = scn.GetShadowCard("khamul");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveCardsToSupportArea(foot);
		scn.MoveMinionsToTable(khamul, brigand);
		assertInZone(Zone.SUPPORT, foot);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { aragorn, brigand }, new PhysicalCardImpl[] { gimli, khamul });
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Its cost is the condition itself, so it is offered with no [raider] Man to make stronger.
		assertInZone(Zone.SUPPORT, foot);
		assertTrue(scn.ShadowActionAvailable(foot));
		scn.ShadowUseCardAction(foot);

		assertInZone(Zone.DISCARD, foot);
		assertEquals(8, scn.GetStrength(brigand));
		assertEquals(12, scn.GetStrength(khamul));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
