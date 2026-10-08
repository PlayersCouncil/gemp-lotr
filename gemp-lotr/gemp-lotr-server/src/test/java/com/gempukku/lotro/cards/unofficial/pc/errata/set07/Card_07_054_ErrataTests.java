package com.gempukku.lotro.cards.unofficial.pc.errata.set07;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_054_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hobbits", "57_54");
					put("smeagol", "6_45");     // Smeagol, Poor Creature: strength 3; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8

					// The Free Peoples player's conditions.
					put("daysdark", "7_55");    // Days Growing Dark: [gollum] condition in the support area, a fellowship ability
					put("deadcity", "7_56");    // The Dead City: [gollum] condition borne by Smeagol
					put("echoes", "7_34");      // Echoes of Valinor: [gandalf] condition; nothing without Gandalf

					// The Shadow player's conditions.
					put("itsmine", "7_62");     // It's Mine: [gollum] condition, a Shadow ability needing Gollum
					put("heavy", "7_60");       // Heavy Burden: [gollum] condition, a Regroup ability needing Gollum
					put("bowmen", "1_264");     // Orc Bowmen: [sauron] condition; nothing without a [sauron] Orc

					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3, damage +1 only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CleverHobbitsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Clever Hobbits
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Spot any number of [gollum] conditions. Make Smeagol strength +3 and damage +1 for each
		 * condition spotted. Discard each of your spotted conditions and hinder each of your opponent's spotted
		 * conditions.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hobbits");

		assertEquals("Clever Hobbits", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingTwoGollumConditionsMakesSmeagolStrengthPlus6AndDamagePlus2AndDiscardsOrHindersThem() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hobbits = scn.GetFreepsCard("hobbits");
		var smeagol = scn.GetFreepsCard("smeagol");
		var daysdark = scn.GetFreepsCard("daysdark");
		var deadcity = scn.GetFreepsCard("deadcity");
		var echoes = scn.GetFreepsCard("echoes");
		var itsmine = scn.GetShadowCard("itsmine");
		var heavy = scn.GetShadowCard("heavy");
		var bowmen = scn.GetShadowCard("bowmen");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(hobbits);
		scn.MoveCompanionsToTable(smeagol);
		scn.AttachCardsTo(smeagol, deadcity);
		scn.MoveCardsToSupportArea(daysdark, echoes, itsmine, heavy, bowmen);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(smeagol, savage);

		assertTrue(scn.FreepsPlayAvailable(hobbits));
		assertEquals(3, scn.GetStrength(smeagol));
		assertEquals(0, scn.GetKeywordCount(smeagol, Keyword.DAMAGE));

		scn.FreepsPlayCard(hobbits);

		// Your [gollum] conditions, in the support area and borne, and the opponent's.
		assertTrue(scn.FreepsHasCardChoiceAvailable(daysdark, deadcity, itsmine, heavy));
		// Conditions of other cultures, and a [gollum] card that is no condition.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(echoes, bowmen, smeagol));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(4, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(daysdark, itsmine);

		assertInDiscard(hobbits);
		// 3 + 3 for each of the two spotted, read after both have left the board.
		assertEquals(9, scn.GetStrength(smeagol));
		assertEquals(2, scn.GetKeywordCount(smeagol, Keyword.DAMAGE));
		assertEquals(5, scn.GetStrength(savage));

		assertInDiscard(daysdark);
		assertInPlay(itsmine);
		assertTrue(scn.IsHindered(itsmine));
		// The unspotted ones are untouched.
		assertEquals(smeagol, deadcity.getAttachedTo());
		assertFalse(scn.IsHindered(heavy));
		assertInPlay(echoes, bowmen);
		assertFalse(scn.IsHindered(bowmen));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void OneSpottedGivesStrengthPlus3AndDamagePlus1UntilTheSkirmishPhaseEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hobbits = scn.GetFreepsCard("hobbits");
		var smeagol = scn.GetFreepsCard("smeagol");
		var deadcity = scn.GetFreepsCard("deadcity");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(hobbits);
		scn.MoveCompanionsToTable(smeagol);
		scn.AttachCardsTo(smeagol, deadcity);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(smeagol, savage);

		scn.FreepsPlayCard(hobbits);
		scn.FreepsChooseCards(deadcity);

		// Your condition is discarded from Smeagol.
		assertInDiscard(deadcity);
		assertEquals(6, scn.GetStrength(smeagol));
		assertEquals(1, scn.GetKeywordCount(smeagol, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(savage));

		scn.PassSkirmishActions();

		// Strength 6 beats 5: one wound for the win and one for damage +1.
		assertEquals(2, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(3, scn.GetStrength(smeagol));
		assertEquals(0, scn.GetKeywordCount(smeagol, Keyword.DAMAGE));
	}

	@Test
	public void SmeagolIsMadeStrongerThoughHeIsNotSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hobbits = scn.GetFreepsCard("hobbits");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var heavy = scn.GetShadowCard("heavy");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(hobbits);
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.MoveCardsToSupportArea(heavy);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, savage);

		assertFalse(scn.IsCharAssigned(smeagol));
		assertEquals(3, scn.GetStrength(smeagol));
		assertEquals(8, scn.GetStrength(aragorn));

		scn.FreepsPlayCard(hobbits);
		scn.FreepsChooseCards(heavy);

		assertEquals(6, scn.GetStrength(smeagol));
		assertEquals(1, scn.GetKeywordCount(smeagol, Keyword.DAMAGE));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.IsHindered(heavy));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotPlayableInTheAssignmentPhaseAndPlayableSpottingNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hobbits = scn.GetFreepsCard("hobbits");
		var smeagol = scn.GetFreepsCard("smeagol");
		var daysdark = scn.GetFreepsCard("daysdark");
		var itsmine = scn.GetShadowCard("itsmine");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(hobbits);
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToSupportArea(daysdark, itsmine);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(hobbits));

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignAndResolve(smeagol, savage);

		assertTrue(scn.FreepsPlayAvailable(hobbits));
		scn.FreepsPlayCard(hobbits);
		scn.FreepsChooseCards();

		assertInDiscard(hobbits);
		assertEquals(3, scn.GetStrength(smeagol));
		assertEquals(0, scn.GetKeywordCount(smeagol, Keyword.DAMAGE));
		assertInPlay(daysdark, itsmine);
		assertFalse(scn.IsHindered(itsmine));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
