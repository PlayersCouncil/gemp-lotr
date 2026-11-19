package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_044_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("concealment", "12_44");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion, strength 8; his maneuver ability is not used
					put("saga", "1_114");       // The Saga of Elendil: on Aragorn, a Free Peoples skirmish special ability (discard it to make him strength +2)
					put("faramir", "4_117");    // Faramir, Son of Denethor: a [gondor] Man companion, strength 7; his bar holds only in skirmishes involving him
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] Man who is an ally, strength 6; his maneuver ability is not used
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] companion who is a Hobbit, strength 3; his skirmish ability is not used
					put("eowyn", "4_270");      // Éowyn, Lady of Rohan: a [rohan] Man companion, strength 6; a response to a wound, never set off here

					put("runner", "1_178");     // Goblin Runner: site 4, so roaming at site 2; strength 5, vitality 1; cheated in, so its text does nothing
					put("nelya", "2_84");       // Úlairë Nelya, Ringwraith in Twilight: site 2, so not roaming at site 2; strength 10; no burdens, so not fierce
					// Uruk Slayer: site 5, so roaming at site 2; "Skirmish: Remove (1) to make this minion strength +1", usable
					// in any skirmish: the Shadow player's special ability
					put("slayer", "1_153");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ConcealmentStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Concealment
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a [gondor] Man strength +1. If he or she is skirmishing a roaming minion, your opponents cannot use special abilities.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("concealment");

		assertEquals("Concealment", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesAGondorManStrengthPlus1AndSkirmishingARoamingMinionTheShadowPlayerCannotUseSpecialAbilities() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var concealment = scn.GetFreepsCard("concealment");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var saga = scn.GetFreepsCard("saga");
		var faramir = scn.GetFreepsCard("faramir");
		var merchant = scn.GetFreepsCard("merchant");
		var pippin = scn.GetFreepsCard("pippin");
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCompanionsToTable(aragorn, faramir, pippin, eowyn);
		scn.AttachCardsTo(aragorn, saga);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(concealment);

		var runner = scn.GetShadowCard("runner");
		var slayer = scn.GetShadowCard("slayer");

		scn.StartGame();
		assertAttachedTo(saga, aragorn);
		scn.MoveMinionsToTable(runner, slayer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, runner},
				new PhysicalCardImpl[]{pippin, slayer});

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.HasKeyword(runner, Keyword.ROAMING));
		assertTrue(scn.GetTwilight() >= 1);
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(faramir));
		assertEquals(6, scn.GetStrength(merchant));
		assertTrue(scn.FreepsPlayAvailable(concealment));

		scn.FreepsPlayCard(concealment);
		// [gondor] Men: a companion in this skirmish, one in none, and the ally; not the [gondor] Hobbit, the
		// [rohan] Man, Frodo or a minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, faramir, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, eowyn, frodo, runner, slayer));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		assertEquals(9, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(faramir));
		assertEquals(6, scn.GetStrength(merchant));
		assertInDiscard(concealment);

		// Aragorn is skirmishing a roaming minion: the Shadow player, who could pay (1), is not offered the Slayer.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(slayer));
		scn.ShadowPass();
		// The Free Peoples player's own special ability is not barred.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(saga));
		scn.FreepsPass();
		assertInDiscard(runner);

		// The next skirmish is a phase of its own: Aragorn's +1 and the bar are both gone.
		scn.FreepsResolveSkirmish(pippin);
		assertEquals(8, scn.GetStrength(aragorn));
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(slayer));
	}

	@Test
	public void SkirmishingAMinionThatIsNotRoamingTheShadowPlayerMayStillUseSpecialAbilities() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var concealment = scn.GetFreepsCard("concealment");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merchant = scn.GetFreepsCard("merchant");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(aragorn, pippin);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(concealment);

		var nelya = scn.GetShadowCard("nelya");
		var slayer = scn.GetShadowCard("slayer");

		scn.StartGame();
		assertInHand(concealment);
		scn.MoveMinionsToTable(nelya, slayer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, nelya},
				new PhysicalCardImpl[]{pippin, slayer});

		scn.FreepsResolveSkirmish(aragorn);
		assertFalse(scn.HasKeyword(nelya, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(slayer, Keyword.ROAMING));
		assertEquals(8, scn.GetStrength(aragorn));

		scn.FreepsPlayCard(concealment);
		scn.FreepsChooseCard(aragorn);
		assertEquals(9, scn.GetStrength(aragorn));
		assertInDiscard(concealment);

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(slayer));
	}

	@Test
	public void AGondorManChosenWhoIsNotSkirmishingARoamingMinionBarsNothingThoughAnotherIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var concealment = scn.GetFreepsCard("concealment");
		var aragorn = scn.GetFreepsCard("aragorn");
		var faramir = scn.GetFreepsCard("faramir");
		scn.MoveCompanionsToTable(aragorn, faramir);
		scn.MoveCardsToHand(concealment);

		var runner = scn.GetShadowCard("runner");
		var slayer = scn.GetShadowCard("slayer");

		scn.StartGame();
		assertInHand(concealment);
		scn.MoveMinionsToTable(runner, slayer);
		scn.SkipToAssignments();
		// Faramir is assigned to a roaming minion too, but his skirmish comes later.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, runner},
				new PhysicalCardImpl[]{faramir, slayer});

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.HasKeyword(runner, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(slayer, Keyword.ROAMING));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(faramir));

		scn.FreepsPlayCard(concealment);
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, faramir));
		scn.FreepsChooseCard(faramir);
		assertEquals(8, scn.GetStrength(faramir));
		assertEquals(8, scn.GetStrength(aragorn));
		assertInDiscard(concealment);

		// Aragorn is skirmishing a roaming minion, but the Man made stronger is Faramir, who is not skirmishing.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(slayer));
	}

	@Test
	public void WithNoGondorManItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var concealment = scn.GetFreepsCard("concealment");
		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCompanionsToTable(pippin, eowyn);
		scn.MoveCardsToHand(concealment);

		var runner = scn.GetShadowCard("runner");
		var slayer = scn.GetShadowCard("slayer");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, slayer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{eowyn, runner},
				new PhysicalCardImpl[]{pippin, slayer});

		scn.FreepsResolveSkirmish(eowyn);
		assertEquals(6, scn.GetStrength(eowyn));
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(4, scn.GetStrength(frodo));    // 3 printed + 1 for The Ruling Ring
		assertTrue(scn.FreepsPlayAvailable(concealment));

		scn.FreepsPlayCard(concealment);
		assertInDiscard(concealment);
		assertEquals(6, scn.GetStrength(eowyn));
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(slayer));
	}

	@Test
	public void CannotBePlayedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var concealment = scn.GetFreepsCard("concealment");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(concealment);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertInHand(concealment);
		assertFalse(scn.FreepsPlayAvailable(concealment));

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(concealment));
	}
}
