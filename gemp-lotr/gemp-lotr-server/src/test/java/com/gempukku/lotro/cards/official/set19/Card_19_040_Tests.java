package com.gempukku.lotro.cards.official.set19;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_19_040_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("witchking", "19_40");
					// Úlairë Nelya, Lieutenant of Morgul: a [wraith] Nazgûl; only her Shadow ability, never used
					put("nelya", "1_233");
					// Ulaire Attea, Khamul of the East: a [raider] Nazgûl, so a Nazgûl of another culture
					put("khamul", "103_61");
					// Morgul Creeper: a [wraith] Orc, so a [wraith] minion who is no Nazgûl
					put("creeper", "8_75");
					// Black Rider: a fierce [wraith] Nazgûl, not unique; strength 8, +1 for each companion. One in
					// play, the other in hand, of the same title.
					put("rider1", "12_161");
					put("rider2", "12_161");
					// The Witch-king, Lord of Angmar: another card of the title, only ever in hand
					put("angmar", "1_237");

					// Aragorn, Ranger of the North (strength 8, vitality 4) and Gimli, Son of Glóin (strength 6,
					// vitality 3): their abilities are never used
					put("aragorn", "1_89");
					put("gimli", "1_13");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheWitchkingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 19
		 * Name: The Witch-king, Dark Lord
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 8
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 14
		 * Vitality: 4
		 * Site Number: 3
		 * Game Text: <b>Fierce</b>.<br><b>Skirmish:</b> Discard a Nazgûl from hand to make The Witch-king strength +1 (or strength +2 if you can spot that discarded Nazgûl).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("witchking");

		assertEquals("The Witch-king", card.getBlueprint().getTitle());
		assertEquals("Dark Lord", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(8, card.getBlueprint().getTwilightCost());
		assertEquals(14, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishAbilityDiscardsANazgulFromHandToMakeHimStrengthPlusOneUntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var witchking = scn.GetShadowCard("witchking");
		var rider = scn.GetShadowCard("rider1");
		var nelya = scn.GetShadowCard("nelya");
		var khamul = scn.GetShadowCard("khamul");
		var creeper = scn.GetShadowCard("creeper");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(nelya, khamul, creeper);

		scn.StartGame();
		assertInHand(nelya, khamul, creeper);
		scn.MoveMinionsToTable(witchking, rider);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, witchking}, new PhysicalCardImpl[]{gimli, rider});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertInHand(nelya, khamul, creeper);
		assertEquals(14, scn.GetStrength(witchking));
		assertTrue(scn.ShadowActionAvailable(witchking));
		scn.ShadowUseCardAction(witchking);

		// Either Nazgûl pays, whatever its culture; the [wraith] Orc does not
		assertTrue(scn.ShadowHasCardChoiceAvailable(nelya, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(creeper));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(nelya);

		assertInDiscard(nelya);
		assertInHand(khamul, creeper);
		// +1: no Úlairë Nelya is in play, and the Black Rider in play is a Nazgûl of another title
		assertEquals(15, scn.GetStrength(witchking));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Aragorn's 8 loses to 15 and takes a wound; in the next skirmish the bonus is gone
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(14, scn.GetStrength(witchking));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithACardOfTheDiscardedNazgulsTitleInPlayTheBonusIsPlusTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var witchking = scn.GetShadowCard("witchking");
		var rider1 = scn.GetShadowCard("rider1");
		var rider2 = scn.GetShadowCard("rider2");
		var angmar = scn.GetShadowCard("angmar");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(rider2, angmar);

		scn.StartGame();
		assertInHand(rider2, angmar);
		scn.MoveMinionsToTable(witchking, rider1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, witchking}, new PhysicalCardImpl[]{gimli, rider1});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertInHand(rider2, angmar);
		assertEquals(14, scn.GetStrength(witchking));

		// A Black Rider discarded while the other Black Rider is in play
		scn.ShadowUseCardAction(witchking);
		assertTrue(scn.ShadowHasCardChoiceAvailable(rider2, angmar));
		scn.ShadowChooseCard(rider2);
		assertInDiscard(rider2);
		assertEquals(16, scn.GetStrength(witchking));

		// A Witch-king discarded: he spots The Witch-king, himself
		scn.FreepsPass();
		scn.ShadowUseCardAction(witchking);
		assertInDiscard(angmar);
		assertEquals(18, scn.GetStrength(witchking));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AbilityIsOfferedInAnotherSkirmishAndNotInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var witchking = scn.GetShadowCard("witchking");
		var rider = scn.GetShadowCard("rider1");
		var nelya = scn.GetShadowCard("nelya");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(nelya);

		scn.StartGame();
		assertInHand(nelya);
		scn.MoveMinionsToTable(witchking, rider);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertInHand(nelya);
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(witchking));
		scn.ShadowPass();

		// He is left out of the skirmishes, and is offered the ability in the Black Rider's
		scn.FreepsAssignToMinions(gimli, rider);
		scn.ShadowDeclineAssignments();
		assertFalse(scn.IsCharAssigned(witchking));
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(witchking));
		scn.ShadowUseCardAction(witchking);
		assertInDiscard(nelya);
		assertEquals(15, scn.GetStrength(witchking));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AbilityIsNotOfferedWithNoNazgulInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var witchking = scn.GetShadowCard("witchking");
		var creeper = scn.GetShadowCard("creeper");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		// A [wraith] minion in hand, but no Nazgûl
		scn.MoveCardsToHand(creeper);

		scn.StartGame();
		scn.MoveMinionsToTable(witchking);
		assertInHand(creeper);
		assertEquals(1, scn.GetShadowHandCount());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, witchking);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(witchking));
	}
}
