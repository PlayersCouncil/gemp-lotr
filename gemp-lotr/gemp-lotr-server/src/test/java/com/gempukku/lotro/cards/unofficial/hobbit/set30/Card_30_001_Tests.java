package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_001_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("knowledge", "30_1");

					put("nori", "30_15");       // Nori, Of the Blue Mountains: Dwarf companion, vitality 3; his one ability
					                            // needs a [dwarven] follower to discard
					put("dain", "32_2");        // Dain Ironfoot, Lord of the Iron Hills: Dwarf ally, vitality 3

					// Shadow conditions of four cultures, three in the support area and one borne.  Their abilities are
					// Shadow, Skirmish and Regroup ones, and Weary's trigger wants an [isengard] tracker.
					put("hatred", "30_34");     // Hatred Rekindled: [gundabad] condition, support area
					put("danger", "32_37");     // Danger Wrapped in Shadows: [sauron] condition, support area
					put("secretive", "33_33");  // Secretive People: [mirkwood] condition, support area
					put("weary", "4_212");      // Weary: [isengard] condition, played on a companion or ally
					put("lord", "1_21");        // Lord of Moria: a Free Peoples condition, support area; each Dwarf is damage +1
					// Possessions, one of each side: no conditions.
					put("axe", "31_1");         // Dwarven Axe: [dwarven] possession for a [dwarven] character
					put("scimitar", "31_31");   // Goblin Scimitar: [moria] possession for an Orc; its text is about being played

					put("veteran", "32_43");    // Orkish Veteran: [sauron] Orc; texts for site 9 and the regroup phase
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void AncestralKnowledgeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Ancestral Knowledge
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Exert a Dwarf character to discard a condition (or 2 conditions if you can spot 4 Shadow conditions).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("knowledge");

		assertEquals("Ancestral Knowledge", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithThreeShadowConditionsExertingADwarfCharacterDiscardsOneConditionOfEitherSide() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knowledge = scn.GetFreepsCard("knowledge");
		var nori = scn.GetFreepsCard("nori");
		var dain = scn.GetFreepsCard("dain");
		var bilbo = scn.GetRingBearer();
		var lord = scn.GetFreepsCard("lord");
		var axe = scn.GetFreepsCard("axe");
		var hatred = scn.GetShadowCard("hatred");
		var danger = scn.GetShadowCard("danger");
		var weary = scn.GetShadowCard("weary");
		var scimitar = scn.GetShadowCard("scimitar");
		var veteran = scn.GetShadowCard("veteran");
		scn.MoveCompanionsToTable(nori);
		scn.MoveAlliesToTable(dain);
		scn.AttachCardsTo(nori, axe);
		scn.MoveCardsToSupportArea(lord, hatred, danger);
		scn.AttachCardsTo(bilbo, weary);
		scn.MoveCardsToHand(knowledge);

		scn.StartGame();
		scn.MoveMinionsToTable(veteran);
		scn.AttachCardsTo(veteran, scimitar);
		assertInZone(Zone.SUPPORT, lord, hatred, danger);
		assertInZone(Zone.ATTACHED, axe, weary, scimitar);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsPlayAvailable(knowledge));
		assertEquals(0, scn.GetWoundsOn(nori));
		assertEquals(0, scn.GetWoundsOn(dain));

		scn.FreepsPlayCard(knowledge);
		// A Dwarf companion and a Dwarf ally may pay; Bilbo, who could exert, is no Dwarf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(nori, dain));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(nori);
		assertEquals(1, scn.GetWoundsOn(nori));
		assertEquals(0, scn.GetWoundsOn(dain));

		// Three Shadow conditions, two in the support area and one on Bilbo, and the player's own Lord of Moria:
		// "a condition" names no side.  Not the possessions of either side.  Three Shadow conditions, with a
		// Free Peoples condition and a Shadow possession beside them, are one short of the four: one condition.
		assertTrue(scn.FreepsHasCardChoiceAvailable(hatred, danger, weary, lord));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(axe, scimitar));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(weary);
		assertInZone(Zone.DISCARD, weary, knowledge);
		assertInZone(Zone.SUPPORT, lord, hatred, danger);
		assertInZone(Zone.ATTACHED, axe, scimitar);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithFourShadowConditionsToSpotTwoConditionsAreDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knowledge = scn.GetFreepsCard("knowledge");
		var nori = scn.GetFreepsCard("nori");
		var bilbo = scn.GetRingBearer();
		var lord = scn.GetFreepsCard("lord");
		var hatred = scn.GetShadowCard("hatred");
		var danger = scn.GetShadowCard("danger");
		var secretive = scn.GetShadowCard("secretive");
		var weary = scn.GetShadowCard("weary");
		var veteran = scn.GetShadowCard("veteran");
		scn.MoveCompanionsToTable(nori);
		scn.MoveCardsToSupportArea(lord, hatred, danger, secretive);
		scn.AttachCardsTo(bilbo, weary);
		scn.MoveCardsToHand(knowledge);

		scn.StartGame();
		scn.MoveMinionsToTable(veteran);
		assertInZone(Zone.SUPPORT, lord, hatred, danger, secretive);
		assertInZone(Zone.ATTACHED, weary);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(nori));
		scn.FreepsPlayCard(knowledge);
		// Nori is the only Dwarf, so he is the one to exert.
		assertEquals(1, scn.GetWoundsOn(nori));

		// Exactly four Shadow conditions, of four cultures, one of them borne: the parenthesis is no choice of
		// the player's (CR 4.2, a conditional effect in parentheses), and the two may be any conditions.
		assertTrue(scn.FreepsHasCardChoiceAvailable(hatred, danger, secretive, weary, lord));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCards(hatred, weary);
		assertInZone(Zone.DISCARD, hatred, weary, knowledge);
		assertInZone(Zone.SUPPORT, lord, danger, secretive);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoConditionInPlayTheDwarfExertsAndNothingIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knowledge = scn.GetFreepsCard("knowledge");
		var nori = scn.GetFreepsCard("nori");
		var axe = scn.GetFreepsCard("axe");
		var scimitar = scn.GetShadowCard("scimitar");
		var veteran = scn.GetShadowCard("veteran");
		scn.MoveCompanionsToTable(nori);
		scn.AttachCardsTo(nori, axe);
		scn.MoveCardsToHand(knowledge);

		scn.StartGame();
		scn.MoveMinionsToTable(veteran);
		scn.AttachCardsTo(veteran, scimitar);
		assertInZone(Zone.ATTACHED, axe, scimitar);
		scn.SkipToPhase(Phase.MANEUVER);

		// The condition is what the event acts on, not something it needs: it may be played with none.
		assertTrue(scn.FreepsPlayAvailable(knowledge));
		assertEquals(0, scn.GetWoundsOn(nori));

		scn.FreepsPlayCard(knowledge);
		assertEquals(1, scn.GetWoundsOn(nori));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertInZone(Zone.ATTACHED, axe, scimitar);
		assertInZone(Zone.DISCARD, knowledge);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheOnlyDwarfCharacterIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knowledge = scn.GetFreepsCard("knowledge");
		var nori = scn.GetFreepsCard("nori");
		var bilbo = scn.GetRingBearer();
		var hatred = scn.GetShadowCard("hatred");
		var veteran = scn.GetShadowCard("veteran");
		// Nori has 1 vitality left.
		scn.MoveCompanionsToTable(nori);
		scn.AddWoundsToChar(nori, 2);
		scn.MoveCardsToSupportArea(hatred);
		scn.MoveCardsToHand(knowledge);

		scn.StartGame();
		scn.MoveMinionsToTable(veteran);
		assertInZone(Zone.SUPPORT, hatred);
		scn.SkipToPhase(Phase.MANEUVER);

		// Bilbo, unwounded, could exert; he is no Dwarf.  A condition is there to discard.
		assertEquals(2, scn.GetWoundsOn(nori));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(knowledge));
		assertInZone(Zone.HAND, knowledge);
	}
}
