package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.IsAdditionalRaceModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_021_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("banner", "103_21");
					put("king", "8_38");        // King of the Dead, Oathbreaker: a [gondor] Wraith, strength 7, vitality 4, enduring; his response is about Aragorn
					put("deadman", "10_27");    // Dead Man of Dunharrow: a [gondor] Wraith, strength 6, vitality 3, enduring; his text is about being played
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man, strength 7; a skirmish ability never used here
					put("gimli", "1_13");       // Gimli, Son of Glóin: a [dwarven] Dwarf, strength 6, made a Wraith where a test says so

					put("savage1", "1_151");    // Uruk Savage: an [isengard] minion, strength 5, vitality 3; damage +1 is its whole text
					put("savage2", "1_151");
					put("savage3", "1_151");
					put("runner", "1_178");     // Goblin Runner: a [moria] minion, strength 5, vitality 1
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] minion, strength 13, vitality 3, no text
					put("corpse", "6_99");      // Corpse Lights: a [sauron] minion who is a Wraith, vitality 2; a skirmish ability the Shadow player never uses here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EtherealBannerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Ethereal Banner
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Vitality: +1
		 * Game Text: Bearer must be a [gondor] Wraith.
		 * 	Each time bearer wins a skirmish, you may exert a Wraith and a minion it is assigned to skirmish.
		 * 	Response: If bearer overwhelms a minion, discard this possession and remove 2 threats to wound 2 minions.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("banner");

		assertEquals("Ethereal Banner", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void PlayedForOneOnOneOfTheGondorWraithsWhoGainsAVitality() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetFreepsCard("banner");
		var king = scn.GetFreepsCard("king");
		var deadman = scn.GetFreepsCard("deadman");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(king, deadman, boromir, gimli);
		// No Free Peoples Wraith is of another culture, so a [dwarven] companion is made one.
		scn.ApplyAdHocModifier(new IsAdditionalRaceModifier(null, gimli, null, Race.WRAITH));
		scn.MoveCardsToHand(banner);

		scn.StartGame();

		assertTrue(scn.IsRace(gimli, Race.WRAITH));
		assertEquals(0, scn.GetTwilight());
		assertEquals(3, scn.GetVitality(deadman));
		assertEquals(4, scn.GetVitality(king));
		assertTrue(scn.FreepsPlayAvailable(banner));
		scn.FreepsPlayCard(banner);

		assertTrue(scn.FreepsHasCardChoiceAvailable(king, deadman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir, gimli, frodo));
		scn.FreepsChooseCard(deadman);

		assertEquals(1, scn.GetTwilight());
		assertAttachedTo(banner, deadman);
		assertEquals(4, scn.GetVitality(deadman));
		assertEquals(4, scn.GetVitality(king));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhenBearerWinsAWraithAndOneOfTheMinionsAssignedToItMayBeExerted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetFreepsCard("banner");
		var king = scn.GetFreepsCard("king");
		var deadman = scn.GetFreepsCard("deadman");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var savage3 = scn.GetShadowCard("savage3");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(king, deadman, boromir, gimli);
		// A Wraith who is not [gondor].
		scn.ApplyAdHocModifier(new IsAdditionalRaceModifier(null, gimli, null, Race.WRAITH));
		scn.AttachCardsTo(king, banner);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2, savage3, troop, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { king, savage1 },
				new PhysicalCardImpl[] { deadman, savage2 },
				new PhysicalCardImpl[] { gimli, savage3 },
				new PhysicalCardImpl[] { boromir, runner }
		);
		// The Shadow player puts the minion left over on the Dead Man as well: he has an [isengard] minion and a [moria] one.
		scn.ShadowAssignToMinions(deadman, troop);
		scn.FreepsResolveSkirmish(king);

		// The King's 7 beats the Savage's 5 without overwhelming it.
		scn.PassCurrentPhaseActions();

		assertEquals(1, scn.GetWoundsOn(savage1));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(banner));
		assertEquals(0, scn.GetWoundsOn(deadman));
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.FreepsChooseOptionalTrigger(banner);

		// A Wraith: the bearer, the other [gondor] Wraith and the [dwarven] one; not the Man, not Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(king, deadman, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(deadman);

		// A minion the Dead Man is assigned to skirmish: either of his two, and no other Wraith's or companion's.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage2, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(savage1, savage3, runner));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		assertEquals(1, scn.GetWoundsOn(deadman));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage2));
		assertEquals(0, scn.GetWoundsOn(savage3));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(king));
		assertEquals(0, scn.GetWoundsOn(gimli));
		// Only the wound for losing the skirmish.
		assertEquals(1, scn.GetWoundsOn(savage1));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void DecliningExertsNoOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetFreepsCard("banner");
		var king = scn.GetFreepsCard("king");
		var deadman = scn.GetFreepsCard("deadman");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCompanionsToTable(king, deadman);
		scn.AttachCardsTo(king, banner);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { king, savage1 },
				new PhysicalCardImpl[] { deadman, savage2 }
		);
		scn.FreepsResolveSkirmish(king);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(banner));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(king));
		assertEquals(0, scn.GetWoundsOn(deadman));
		assertEquals(0, scn.GetWoundsOn(savage2));
		assertEquals(1, scn.GetWoundsOn(savage1));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void AnotherWraithWinningAndBearerLosingBringNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetFreepsCard("banner");
		var king = scn.GetFreepsCard("king");
		var deadman = scn.GetFreepsCard("deadman");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(king, deadman);
		scn.AttachCardsTo(king, banner);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { king, troop },
				new PhysicalCardImpl[] { deadman, runner }
		);

		// The Dead Man, who does not bear the Banner, beats the Runner 6 to 5, while the King is still assigned to the Troop.
		scn.FreepsResolveSkirmish(deadman);
		scn.PassCurrentPhaseActions();

		assertInZone(Zone.DISCARD, runner);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// The King's 7 loses to the Troop's 13.
		scn.FreepsResolveSkirmish(king);
		scn.PassCurrentPhaseActions();

		assertEquals(1, scn.GetWoundsOn(king));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(deadman));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachSkirmishBearerWinsOffersItAgainAndBearerWithTheMinionHeHasBeatenMayBeTheOnesExerted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetFreepsCard("banner");
		var king = scn.GetFreepsCard("king");
		var deadman = scn.GetFreepsCard("deadman");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCompanionsToTable(king, deadman);
		scn.AttachCardsTo(king, banner);
		// Every minion is made fierce, so that the King fights twice in one turn.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, CardType.MINION, null, Keyword.FIERCE));

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { king, savage1 },
				new PhysicalCardImpl[] { deadman, savage2 }
		);
		scn.FreepsResolveSkirmish(king);
		scn.PassCurrentPhaseActions();

		// The King has won, and is still assigned to the Savage he beat until this skirmish phase is over.
		assertEquals(1, scn.GetWoundsOn(savage1));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(banner));
		scn.FreepsChooseOptionalTrigger(banner);

		assertTrue(scn.FreepsHasCardChoiceAvailable(king, deadman));
		scn.FreepsChooseCard(king);

		// The Savage he beat is the only minion he is assigned to.
		assertEquals(1, scn.GetWoundsOn(king));
		assertEquals(2, scn.GetWoundsOn(savage1));
		assertEquals(0, scn.GetWoundsOn(deadman));
		assertEquals(0, scn.GetWoundsOn(savage2));

		// The Dead Man beats his Savage 6 to 5: he does not bear the Banner and nothing is offered.
		scn.FreepsResolveSkirmish(deadman);
		scn.PassCurrentPhaseActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(savage2));

		// The fierce skirmishes. The King, 7 + 2 for his wound, beats the first Savage again and kills it.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		scn.BothPass();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { king, savage1 },
				new PhysicalCardImpl[] { deadman, savage2 }
		);
		scn.FreepsResolveSkirmish(king);
		scn.PassCurrentPhaseActions();

		assertInZone(Zone.DISCARD, savage1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(banner));
		scn.FreepsChooseOptionalTrigger(banner);
		scn.FreepsChooseCard(deadman);

		assertEquals(1, scn.GetWoundsOn(deadman));
		assertEquals(2, scn.GetWoundsOn(savage2));
		assertEquals(1, scn.GetWoundsOn(king));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void AWraithAssignedToNoMinionMayBeExertedAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetFreepsCard("banner");
		var king = scn.GetFreepsCard("king");
		var deadman = scn.GetFreepsCard("deadman");
		var boromir = scn.GetFreepsCard("boromir");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCompanionsToTable(king, deadman, boromir);
		scn.AttachCardsTo(king, banner);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToAssignments();
		// The Dead Man is assigned to no one.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { king, savage1 },
				new PhysicalCardImpl[] { boromir, savage2 }
		);
		scn.FreepsResolveSkirmish(king);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(banner));
		scn.FreepsChooseOptionalTrigger(banner);

		assertTrue(scn.FreepsHasCardChoiceAvailable(king, deadman));
		scn.FreepsChooseCard(deadman);

		// He exerts, and there is no minion he is assigned to: the rest is ignored.
		assertEquals(1, scn.GetWoundsOn(deadman));
		assertEquals(0, scn.GetWoundsOn(savage2));
		assertEquals(1, scn.GetWoundsOn(savage1));
		assertEquals(0, scn.GetWoundsOn(king));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void AShadowMinionWhoIsAWraithMayBeTheWraithExerted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetFreepsCard("banner");
		var king = scn.GetFreepsCard("king");
		var boromir = scn.GetFreepsCard("boromir");
		var savage1 = scn.GetShadowCard("savage1");
		var corpse = scn.GetShadowCard("corpse");
		scn.MoveCompanionsToTable(king, boromir);
		scn.AttachCardsTo(king, banner);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, corpse);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { king, savage1 },
				new PhysicalCardImpl[] { boromir, corpse }
		);
		scn.FreepsResolveSkirmish(king);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(banner));
		scn.FreepsChooseOptionalTrigger(banner);

		assertTrue(scn.FreepsHasCardChoiceAvailable(king, corpse));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir, savage1));
		scn.FreepsChooseCard(corpse);

		// Corpse Lights are assigned to a companion and to no minion: they exert alone.
		assertEquals(1, scn.GetWoundsOn(corpse));
		assertEquals(0, scn.GetWoundsOn(king));
		assertEquals(1, scn.GetWoundsOn(savage1));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void WithNoWraithAbleToExertAndNoMinionLeftAssignedItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetFreepsCard("banner");
		var king = scn.GetFreepsCard("king");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(king);
		scn.AttachCardsTo(king, banner);

		scn.StartGame();
		// Vitality 4 + 1 for the Banner, with 4 wounds: exhausted, and at strength 7 + 8.
		scn.AddWoundsToChar(king, 4);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(king, runner);
		scn.FreepsResolveSkirmish(king);

		assertEquals(15, scn.GetStrength(king));
		scn.PassCurrentPhaseActions();

		// The Runner is dead, the only Wraith cannot exert, and there are no threats for the response.
		assertInZone(Zone.DISCARD, runner);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(4, scn.GetWoundsOn(king));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenBearerOverwhelmsAMinionTheBannerAndTwoThreatsMayBeSpentToWoundTwoMinions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetFreepsCard("banner");
		var king = scn.GetFreepsCard("king");
		var deadman = scn.GetFreepsCard("deadman");
		var boromir = scn.GetFreepsCard("boromir");
		var runner = scn.GetShadowCard("runner");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(king, deadman, boromir);
		scn.AttachCardsTo(king, banner);

		scn.StartGame();
		// Enduring: 7 + 4 for two wounds is 11, more than double the Runner's 5.
		scn.AddWoundsToChar(king, 2);
		scn.AddThreats(3);
		scn.MoveMinionsToTable(runner, savage1, savage2, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(king, runner);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(king);

		assertEquals(11, scn.GetStrength(king));
		// Vitality 4 + 1 for the Banner, less 2 wounds
		assertEquals(3, scn.GetVitality(king));
		scn.PassCurrentPhaseActions();

		assertInZone(Zone.DISCARD, runner);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable("wound 2 minions"));
		assertAttachedTo(banner, king);
		assertEquals(3, scn.GetThreats());
		scn.FreepsChooseAction("wound 2 minions");

		assertTrue(scn.FreepsHasCardChoiceAvailable(savage1, savage2, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(king, deadman, boromir));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(savage1, troop);

		assertInZone(Zone.DISCARD, banner);
		assertEquals(1, scn.GetThreats());
		assertEquals(1, scn.GetWoundsOn(savage1));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage2));
		// The Banner's vitality went with it.
		assertEquals(2, scn.GetVitality(king));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnOverwhelmByAnotherWraithAndAWinByBearerThatIsNoOverwhelmBringNoResponse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetFreepsCard("banner");
		var king = scn.GetFreepsCard("king");
		var deadman = scn.GetFreepsCard("deadman");
		var runner = scn.GetShadowCard("runner");
		var savage1 = scn.GetShadowCard("savage1");
		scn.MoveCompanionsToTable(king, deadman);
		scn.AttachCardsTo(king, banner);

		scn.StartGame();
		// Enduring: the Dead Man's 6 + 4 for two wounds is 10, double the Runner's 5.
		scn.AddWoundsToChar(deadman, 2);
		scn.AddThreats(2);
		scn.MoveMinionsToTable(runner, savage1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { king, savage1 },
				new PhysicalCardImpl[] { deadman, runner }
		);

		// The Dead Man, who does not bear the Banner, overwhelms the Runner.
		scn.FreepsResolveSkirmish(deadman);
		assertEquals(10, scn.GetStrength(deadman));
		scn.PassCurrentPhaseActions();

		assertInZone(Zone.DISCARD, runner);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		// The King's 7 beats the Savage's 5, which is no overwhelm: the first sentence is offered, the response is not.
		scn.FreepsResolveSkirmish(king);
		scn.PassCurrentPhaseActions();

		assertEquals(1, scn.GetWoundsOn(savage1));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable("Exert a Wraith"));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable("wound 2 minions"));
		scn.FreepsDeclineOptionalTrigger();

		assertAttachedTo(banner, king);
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithOnlyOneThreatToRemoveAnOverwhelmBringsNoResponse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetFreepsCard("banner");
		var king = scn.GetFreepsCard("king");
		var boromir = scn.GetFreepsCard("boromir");
		var runner = scn.GetShadowCard("runner");
		var savage1 = scn.GetShadowCard("savage1");
		scn.MoveCompanionsToTable(king, boromir);
		scn.AttachCardsTo(king, banner);

		scn.StartGame();
		scn.AddWoundsToChar(king, 2);
		scn.AddThreats(1);
		scn.MoveMinionsToTable(runner, savage1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(king, runner);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(king);
		scn.PassCurrentPhaseActions();

		assertInZone(Zone.DISCARD, runner);
		// The first sentence is offered for the win; the response is not.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable("Exert a Wraith"));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable("wound 2 minions"));
		scn.FreepsDeclineOptionalTrigger();

		assertAttachedTo(banner, king);
		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(savage1));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
