package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_210_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("whispers", "1_210");

					put("lemenya", "1_232");    // Úlairë Lemenya: [wraith] Nazgûl, strength 9, vitality 2, no damage bonus; her text weakens only bearers of a [wraith] condition
					put("khamul", "103_61");    // Úlairë Attëa, Khamûl of the East: a [raider] Nazgûl; +3 at 3 burdens and damage +1 at 5, on himself only
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc and no Nazgûl, strength 5; its bonus needs 2 wounds on the Ring-bearer

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8, vitality 4; a maneuver ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion, strength 7; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,     // resistance 10: six burdens do not corrupt him
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DarkWhispersStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Dark Whispers
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Spot 3 burdens to make a Nazgûl strength +1 and <b>damage +1</b>, or spot 6 burdens to make a Nazgûl strength +3 and <b>damage +2</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("whispers");

		assertEquals("Dark Whispers", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AtExactlyThreeBurdensMakesANazgulStrengthPlusOneAndDamagePlusOneUntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var whispers = scn.GetShadowCard("whispers");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToHand(whispers);

		scn.StartGame();
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(lemenya, khamul, hound);
		assertEquals(3, scn.GetBurdens());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lemenya}, new PhysicalCardImpl[]{boromir, hound});
		// Khamûl is left over, and the Shadow player leaves him unassigned.
		scn.ShadowDeclineAssignments();
		assertFalse(scn.IsCharAssigned(khamul));

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(9, scn.GetStrength(lemenya));
		assertEquals(0, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));
		assertTrue(scn.ShadowPlayAvailable(whispers));
		int khamulStrength = scn.GetStrength(khamul);
		int khamulDamage = scn.GetKeywordCount(khamul, Keyword.DAMAGE);
		scn.ShadowPlayCard(whispers);

		// Six burdens cannot be spotted, so the first half is carried out with no question. A Nazgûl of either
		// culture, in the skirmish or not; the [wraith] Orc in the other skirmish is no Nazgûl.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound, aragorn, boromir));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(lemenya);

		assertEquals(10, scn.GetStrength(lemenya));
		assertEquals(1, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));
		// The Nazgûl who was not chosen is untouched.
		assertEquals(khamulStrength, scn.GetStrength(khamul));
		assertEquals(khamulDamage, scn.GetKeywordCount(khamul, Keyword.DAMAGE));
		assertInDiscard(whispers);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The skirmish ends; in the next one Lemenya is herself again.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(9, scn.GetStrength(lemenya));
		assertEquals(0, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWithTwoBurdensAndAWoundOnTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var whispers = scn.GetShadowCard("whispers");
		var lemenya = scn.GetShadowCard("lemenya");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(whispers);

		scn.StartGame();
		// Two burdens, and a wound beside them on the Ring-bearer that is no burden.
		scn.AddBurdens(2);
		scn.AddWoundsToChar(frodo, 1);
		scn.MoveMinionsToTable(lemenya);
		assertEquals(2, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(frodo));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertFalse(scn.ShadowPlayAvailable(whispers));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AtExactlySixBurdensTheSecondChoiceMakesANazgulStrengthPlusThreeAndDamagePlusTwoUntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var whispers = scn.GetShadowCard("whispers");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToHand(whispers);

		scn.StartGame();
		scn.AddBurdens(6);
		scn.MoveMinionsToTable(lemenya, khamul, hound);
		assertEquals(6, scn.GetBurdens());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lemenya}, new PhysicalCardImpl[]{boromir, hound});
		scn.ShadowDeclineAssignments();
		assertFalse(scn.IsCharAssigned(khamul));

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(9, scn.GetStrength(lemenya));
		assertEquals(0, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));
		int khamulStrength = scn.GetStrength(khamul);
		int khamulDamage = scn.GetKeywordCount(khamul, Keyword.DAMAGE);
		scn.ShadowPlayCard(whispers);

		// Both halves can be done, and the Shadow player is asked which.
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		assertTrue(scn.ShadowChoiceAvailable("strength +1 and damage +1"));
		assertTrue(scn.ShadowChoiceAvailable("strength +3 and damage +2"));
		scn.ShadowChoose("Make a Nazgul strength +3 and damage +2");

		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound, aragorn, boromir));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(lemenya);

		assertEquals(12, scn.GetStrength(lemenya));
		assertEquals(2, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));
		// The Nazgûl who was not chosen is untouched.
		assertEquals(khamulStrength, scn.GetStrength(khamul));
		assertEquals(khamulDamage, scn.GetKeywordCount(khamul, Keyword.DAMAGE));
		assertInDiscard(whispers);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The skirmish ends; in the next one Lemenya is herself again.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(9, scn.GetStrength(lemenya));
		assertEquals(0, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AtSixBurdensTheFirstChoiceCanStillBeTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var whispers = scn.GetShadowCard("whispers");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(whispers);

		scn.StartGame();
		scn.AddBurdens(6);
		scn.MoveMinionsToTable(lemenya, khamul);
		assertEquals(6, scn.GetBurdens());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(9, scn.GetStrength(lemenya));
		assertEquals(0, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));
		scn.ShadowPlayCard(whispers);

		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		scn.ShadowChoose("Make a Nazgul strength +1 and damage +1");
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, khamul));
		scn.ShadowChooseCard(lemenya);

		assertEquals(10, scn.GetStrength(lemenya));
		assertEquals(1, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AtFiveBurdensAndAWoundOnTheRingBearerOnlyTheFirstHalfIsCarriedOutUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var whispers = scn.GetShadowCard("whispers");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(whispers);

		scn.StartGame();
		// Five burdens, and a wound beside them on the Ring-bearer that is no burden.
		scn.AddBurdens(5);
		scn.AddWoundsToChar(frodo, 1);
		scn.MoveMinionsToTable(lemenya, khamul);
		assertEquals(5, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(frodo));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(9, scn.GetStrength(lemenya));
		assertEquals(0, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));
		scn.ShadowPlayCard(whispers);

		// No question of which half: the Nazgûl is chosen at once, and gets the first half's +1 and damage +1.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, khamul));
		scn.ShadowChooseCard(lemenya);

		assertEquals(10, scn.GetStrength(lemenya));
		assertEquals(1, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoNazgulInPlayItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var whispers = scn.GetShadowCard("whispers");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(whispers);

		scn.StartGame();
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(hound);
		assertEquals(3, scn.GetBurdens());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, hound);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(5, scn.GetStrength(hound));
		assertEquals(0, scn.GetKeywordCount(hound, Keyword.DAMAGE));
		assertTrue(scn.ShadowPlayAvailable(whispers));
		scn.ShadowPlayCard(whispers);

		// The [wraith] Orc is no Nazgûl: nothing is chosen and nothing changes.
		assertInDiscard(whispers);
		assertEquals(5, scn.GetStrength(hound));
		assertEquals(0, scn.GetKeywordCount(hound, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
