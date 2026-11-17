package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_054_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hobbits", "7_54");
					put("smeagol", "6_45");     // Sméagol, Poor Creature: strength 3; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8

					// The Free Peoples player's conditions.
					put("daysdark", "7_55");    // Days Growing Dark: [gollum] condition in the support area, a fellowship ability
					put("deadcity", "7_56");    // The Dead City: [gollum] condition borne by Sméagol
					put("echoes", "7_34");      // Echoes of Valinor: [gandalf] condition; nothing without Gandalf

					// The Shadow player's cards.
					put("itsmine", "7_62");     // It's Mine: [gollum] condition, a Shadow ability needing Gollum
					put("heavy", "7_60");       // Heavy Burden: [gollum] condition, a Regroup ability needing Gollum
					put("bowmen", "1_264");     // Orc Bowmen: [sauron] condition; nothing without a [sauron] Orc
					put("larder", "8_23");      // Larder: a [gollum] possession in the support area, no condition; its abilities are used only on purpose

					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3, damage +1 only
					// The Mouth of Sauron, Bearer of Dread News: lurker; "Response: If a Free Peoples event is played,
					// exert this minion to cancel that event."
					put("mouth", "103_70");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetShireSmeagolScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hobbits", "7_54");
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion, strength 3, vitality 4; an assignment ability used only on purpose
					put("daysdark", "7_55");    // Days Growing Dark: [gollum] condition in the support area, a fellowship ability
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
		 * Game Text: Discard any number of [gollum] conditions. For each condition discarded, make Sméagol strength +3
		 * and <b>damage +1</b>.
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
	public void DiscardingTwoGollumConditionsMakesSmeagolStrengthPlus6AndDamagePlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hobbits = scn.GetFreepsCard("hobbits");
		var smeagol = scn.GetFreepsCard("smeagol");
		var daysdark = scn.GetFreepsCard("daysdark");
		var deadcity = scn.GetFreepsCard("deadcity");
		var echoes = scn.GetFreepsCard("echoes");
		var itsmine = scn.GetShadowCard("itsmine");
		var heavy = scn.GetShadowCard("heavy");
		var bowmen = scn.GetShadowCard("bowmen");
		var larder = scn.GetShadowCard("larder");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(hobbits);
		scn.MoveCompanionsToTable(smeagol);
		scn.AttachCardsTo(smeagol, deadcity);
		scn.MoveCardsToSupportArea(daysdark, echoes, itsmine, heavy, bowmen, larder);

		assertAttachedTo(deadcity, smeagol);
		assertInPlay(daysdark, echoes, itsmine, heavy, bowmen, larder);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(smeagol, savage);

		assertTrue(scn.FreepsPlayAvailable(hobbits));
		assertEquals(3, scn.GetStrength(smeagol));
		assertEquals(0, scn.GetKeywordCount(smeagol, Keyword.DAMAGE));

		scn.FreepsPlayCard(hobbits);

		// [gollum] conditions of both players, in a support area and borne.
		assertTrue(scn.FreepsHasCardChoiceAvailable(daysdark, deadcity, itsmine, heavy));
		// Conditions of other cultures, a [gollum] possession, and a [gollum] card that is a character.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(echoes, bowmen, larder, smeagol));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(4, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(daysdark, itsmine);

		assertInDiscard(hobbits, daysdark, itsmine);
		// 3 + 3 for each of the two discarded.
		assertEquals(9, scn.GetStrength(smeagol));
		assertEquals(2, scn.GetKeywordCount(smeagol, Keyword.DAMAGE));
		assertEquals(5, scn.GetStrength(savage));

		// The ones not chosen are untouched.
		assertAttachedTo(deadcity, smeagol);
		assertInPlay(heavy, echoes, bowmen, larder);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void OneDiscardedGivesStrengthPlus3AndDamagePlus1UntilTheSkirmishPhaseEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hobbits = scn.GetFreepsCard("hobbits");
		var smeagol = scn.GetFreepsCard("smeagol");
		var deadcity = scn.GetFreepsCard("deadcity");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(hobbits);
		scn.MoveCompanionsToTable(smeagol);
		scn.AttachCardsTo(smeagol, deadcity);

		assertAttachedTo(deadcity, smeagol);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(smeagol, savage);

		scn.FreepsPlayCard(hobbits);
		scn.FreepsChooseCards(deadcity);

		// Your condition is discarded from Sméagol.
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

		assertInPlay(heavy);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, savage);

		assertFalse(scn.IsCharAssigned(smeagol));
		assertEquals(3, scn.GetStrength(smeagol));
		assertEquals(8, scn.GetStrength(aragorn));

		scn.FreepsPlayCard(hobbits);
		scn.FreepsChooseCards(heavy);

		assertInDiscard(heavy);
		assertEquals(6, scn.GetStrength(smeagol));
		assertEquals(1, scn.GetKeywordCount(smeagol, Keyword.DAMAGE));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoSmeagolInPlayTheConditionsAreDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hobbits = scn.GetFreepsCard("hobbits");
		var aragorn = scn.GetFreepsCard("aragorn");
		var daysdark = scn.GetFreepsCard("daysdark");
		var savage = scn.GetShadowCard("savage");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(hobbits);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(daysdark);

		assertInPlay(daysdark);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, savage);

		assertTrue(scn.FreepsPlayAvailable(hobbits));
		scn.FreepsPlayCard(hobbits);
		scn.FreepsChooseCards(daysdark);

		assertInDiscard(hobbits, daysdark);
		// Nobody is Sméagol: neither the companion skirmishing nor the Ring-bearer gains anything.
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(frodo, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotPlayableInTheAssignmentPhaseAndPlayableDiscardingNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hobbits = scn.GetFreepsCard("hobbits");
		var smeagol = scn.GetFreepsCard("smeagol");
		var daysdark = scn.GetFreepsCard("daysdark");
		var itsmine = scn.GetShadowCard("itsmine");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(hobbits);
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToSupportArea(daysdark, itsmine);

		assertInPlay(daysdark, itsmine);

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
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheShireSmeagolIsMadeStrongerAsTheGollumOneIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShireSmeagolScenario();

		var hobbits = scn.GetFreepsCard("hobbits");
		var smeagol = scn.GetFreepsCard("smeagol");
		var daysdark = scn.GetFreepsCard("daysdark");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(hobbits);
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToSupportArea(daysdark);

		assertInPlay(daysdark);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(smeagol, savage);

		// This Smeagol is [shire], not [gollum]; the text names only the title.
		assertEquals(Culture.SHIRE, smeagol.getBlueprint().getCulture());
		assertEquals(3, scn.GetStrength(smeagol));
		assertEquals(0, scn.GetKeywordCount(smeagol, Keyword.DAMAGE));

		scn.FreepsPlayCard(hobbits);
		scn.FreepsChooseCards(daysdark);

		assertInDiscard(hobbits, daysdark);
		// 3 + 3 for the one condition discarded.
		assertEquals(6, scn.GetStrength(smeagol));
		assertEquals(1, scn.GetKeywordCount(smeagol, Keyword.DAMAGE));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CancelledByTheMouthOfSauronTheConditionsDiscardedForItStayDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hobbits = scn.GetFreepsCard("hobbits");
		var smeagol = scn.GetFreepsCard("smeagol");
		var daysdark = scn.GetFreepsCard("daysdark");
		var savage = scn.GetShadowCard("savage");
		var mouth = scn.GetShadowCard("mouth");
		scn.MoveCardsToHand(hobbits);
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToSupportArea(daysdark);

		assertInPlay(daysdark);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, mouth);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(smeagol, savage);

		assertEquals(0, scn.GetWoundsOn(mouth));
		scn.FreepsPlayCard(hobbits);
		// The discard is the price, chosen and paid before anything can answer the play.
		assertTrue(scn.FreepsHasCardChoiceAvailable(daysdark));
		scn.FreepsChooseCards(daysdark);

		assertTrue(scn.ShadowActionAvailable(mouth));
		scn.ShadowUseCardAction(mouth);

		assertEquals(1, scn.GetWoundsOn(mouth));
		// Cancelled: Sméagol gains nothing, and the condition paid stays paid.
		assertInDiscard(hobbits, daysdark);
		assertEquals(3, scn.GetStrength(smeagol));
		assertEquals(0, scn.GetKeywordCount(smeagol, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
