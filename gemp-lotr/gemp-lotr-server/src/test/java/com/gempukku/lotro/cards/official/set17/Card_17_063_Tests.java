package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.VitalityModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_063_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wildman", "17_63");
					put("wildman2", "17_63");

					put("corps", "11_73");      // Corps of Harad: another [men] Man; fierce only at a battleground (none on this path)
					put("saruman", "17_48");    // Saruman, Coldly Still: a [men] minion who is no Man (a Wizard); his texts need a possession or the skirmish phase
					put("axeman", "4_224");     // Easterling Axeman: a [raider] Man; strength +3 and fierce only at 2 burdens
					put("stampede", "17_53");   // Stampeding Savage: Maneuver: discard him and X stacked minions to play a [men] minion from the discard pile

					put("aragorn", "1_89");     // the companions, each with no standing text in the way
					put("gimli", "1_13");
					put("legolas", "1_50");
					put("arwen", "1_30");
					put("boromir", "1_96");
					put("barliman", "1_70");    // Barliman Butterbur: an ally, a Free Peoples character who is no companion; a fellowship ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void VengefulWildManStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Vengeful Wild Man
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: At the start of the maneuver phase, spot another [men] Man and exert this minion to add a threat. Then add an additional threat for each companion you spot over 4.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wildman");

		assertEquals("Vengeful Wild Man", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AtTheStartOfTheManeuverPhaseHeExertsToAddOneThreatWithFourCompanionsAndAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wildman = scn.GetShadowCard("wildman");
		var corps = scn.GetShadowCard("corps");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var barliman = scn.GetFreepsCard("barliman");

		scn.MoveCompanionsToTable(aragorn, gimli, legolas);
		scn.MoveAlliesToTable(barliman);
		scn.StartGame();
		scn.MoveMinionsToTable(wildman, corps);

		assertEquals(0, scn.GetWoundsOn(wildman));
		assertEquals(0, scn.GetWoundsOn(corps));
		assertEquals(0, scn.GetThreats());
		scn.SkipToPhase(Phase.MANEUVER);

		// No "may": the trigger is required, and the exertion is paid unasked.  The count of companions to spot is
		// asked: four companions (the ally is no companion), filled in with all four.
		assertEquals(1, scn.GetWoundsOn(wildman));
		assertEquals(0, scn.GetWoundsOn(corps));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(4, scn.ShadowGetChoiceMax());
		assertEquals(Integer.valueOf(4), scn.ShadowGetChoiceDefault());
		scn.ShadowChoose("4");

		// One threat, and none more: four is not over 4.
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithSixCompanionsAllSpottedTwoMoreThreatsAreAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wildman = scn.GetShadowCard("wildman");
		var corps = scn.GetShadowCard("corps");

		scn.MoveCompanionsToTable("aragorn", "gimli", "legolas", "arwen", "boromir");
		scn.StartGame();
		scn.MoveMinionsToTable(wildman, corps);

		assertEquals(0, scn.GetWoundsOn(wildman));
		assertEquals(0, scn.GetThreats());
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(1, scn.GetWoundsOn(wildman));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(6, scn.ShadowGetChoiceMax());
		assertEquals(Integer.valueOf(6), scn.ShadowGetChoiceDefault());
		scn.ShadowChoose("6");

		// 1 + (6 - 4)
		assertEquals(3, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithFiveCompanionsSpottingFourAddsNoMoreThanTheOneThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wildman = scn.GetShadowCard("wildman");
		var corps = scn.GetShadowCard("corps");

		scn.MoveCompanionsToTable("aragorn", "gimli", "legolas", "arwen");
		scn.StartGame();
		scn.MoveMinionsToTable(wildman, corps);

		assertEquals(0, scn.GetWoundsOn(wildman));
		assertEquals(0, scn.GetThreats());
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(1, scn.GetWoundsOn(wildman));
		assertEquals(5, scn.ShadowGetChoiceMax());
		assertEquals(Integer.valueOf(5), scn.ShadowGetChoiceDefault());
		// The player's own count: he spots fewer than there are.
		scn.ShadowChoose("4");

		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TwoCopiesEachSpotTheOtherAndEachExertsForItsOwnThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wildman = scn.GetShadowCard("wildman");
		var wildman2 = scn.GetShadowCard("wildman2");

		scn.MoveCompanionsToTable("aragorn", "gimli", "legolas");
		scn.StartGame();
		scn.MoveMinionsToTable(wildman, wildman2);

		assertEquals(0, scn.GetWoundsOn(wildman));
		assertEquals(0, scn.GetWoundsOn(wildman2));
		assertEquals(0, scn.GetThreats());
		scn.SkipToPhase(Phase.MANEUVER);

		scn.ShadowChoose("4");
		scn.ShadowChoose("4");

		assertEquals(1, scn.GetWoundsOn(wildman));
		assertEquals(1, scn.GetWoundsOn(wildman2));
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoOtherMenManHeDoesNotExertAndNoThreatIsAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wildman = scn.GetShadowCard("wildman");
		var saruman = scn.GetShadowCard("saruman");
		var axeman = scn.GetShadowCard("axeman");

		scn.MoveCompanionsToTable("aragorn", "gimli", "legolas");
		scn.StartGame();
		// A [men] minion who is no Man, and a Man of another culture; the Wild Man is no "other" [men] Man to himself.
		scn.MoveMinionsToTable(wildman, saruman, axeman);

		assertEquals(0, scn.GetWoundsOn(wildman));
		assertEquals(0, scn.GetThreats());
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(wildman));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(axeman));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ExhaustedHeCannotExertAndNoThreatIsAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wildman = scn.GetShadowCard("wildman");
		var corps = scn.GetShadowCard("corps");

		scn.MoveCompanionsToTable("aragorn", "gimli", "legolas");
		scn.StartGame();
		scn.MoveMinionsToTable(wildman, corps);
		scn.AddWoundsToChar(wildman, 1);

		// Vitality 2 with 1 wound: exhausted.
		assertEquals(1, scn.GetWoundsOn(wildman));
		assertEquals(0, scn.GetThreats());
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(1, scn.GetWoundsOn(wildman));
		assertEquals(0, scn.GetWoundsOn(corps));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void PlayedDuringTheManeuverPhaseHeDoesNotExertAtTheStartOfTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wildman = scn.GetShadowCard("wildman");
		var corps = scn.GetShadowCard("corps");
		var stampede = scn.GetShadowCard("stampede");

		scn.MoveCompanionsToTable("aragorn", "gimli", "legolas");
		scn.MoveCardsToDiscard(wildman);
		scn.StartGame();
		scn.MoveMinionsToTable(corps, stampede);
		assertInZone(Zone.DISCARD, wildman);
		scn.SetTwilight(10);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		scn.ShadowUseCardAction(stampede);
		// No minion is stacked anywhere, so X is 0 and nothing is asked of it; the Wild Man is the only [men]
		// minion in the discard pile but the Savage, who is barred.
		assertInZone(Zone.SHADOW_CHARACTERS, wildman);
		assertEquals(0, scn.GetWoundsOn(wildman));
		assertEquals(0, scn.GetThreats());

		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(Phase.ARCHERY, scn.GetCurrentPhase());
		assertEquals(0, scn.GetWoundsOn(wildman));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void TheManeuverPhaseAfterASecondMoveSetsItOffAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wildman = scn.GetShadowCard("wildman");
		var corps = scn.GetShadowCard("corps");

		// No archer among them, so the archery phase asks nothing.
		scn.MoveCompanionsToTable("aragorn", "gimli", "boromir");
		scn.StartGame();
		scn.MoveMinionsToTable(wildman, corps);
		// Vitality 3, so that he can exert at both maneuver phases.
		scn.ApplyAdHocModifier(new VitalityModifier(null, wildman, null, 1));

		assertEquals(0, scn.GetWoundsOn(wildman));
		assertEquals(0, scn.GetThreats());
		scn.SkipToPhase(Phase.MANEUVER);
		scn.ShadowChoose("4");
		assertEquals(1, scn.GetWoundsOn(wildman));
		assertEquals(1, scn.GetThreats());

		// Nobody is assigned, so no skirmish is fought; the fellowship moves again.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertInZone(Zone.SHADOW_CHARACTERS, wildman, corps);
		scn.ShadowRefreshDecision();

		scn.SkipToPhase(Phase.MANEUVER);
		scn.ShadowChoose("4");
		assertEquals(2, scn.GetWoundsOn(wildman));
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
