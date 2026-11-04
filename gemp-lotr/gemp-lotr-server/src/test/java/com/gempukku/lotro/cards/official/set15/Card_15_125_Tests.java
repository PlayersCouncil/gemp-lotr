package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_15_125_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eowyn", "15_125");

					put("faramir", "18_48");    // Faramir, Captain of Ithilien: a [gondor] Man companion, hunter 2, not valiant; a maneuver heal used only on purpose
					put("guardian", "17_7");    // Elven Guardian: an [elven] Elf companion, hunter 1, not valiant; a maneuver heal used only on purpose
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, valiant and nothing else
					put("boromir", "1_97");     // Boromir, Son of Denethor: a Man companion, neither hunter nor valiant
					put("lorien", "54_76");     // Lórien Guardian: a valiant Elf companion; "Regroup: Exert this companion to make a Shadow player wound a minion"
					put("guma", "4_277");       // Guma, Plains Farmer: a [rohan] Man ally; a fellowship ability used only on purpose
					// Northern Signal-fire: "Maneuver: Hinder this beacon and exert an unbound Man. Until the regroup
					// phase, each of your other Men gain each of the unloaded keywords (except race) on that Man."
					put("fire", "103_86");

					put("slayer1", "3_93");     // Morgul Slayer: "Regroup: Exert this minion to wound a companion
					put("slayer2", "3_93");     //   (except the Ring-bearer)."
					put("assassin", "2_93");    // Tower Assassin: "Maneuver: Exert Tower Assassin to wound an ally."
					put("tracker", "15_171");   // Tracking Uruk: a hunter minion (hunter 1 is its whole text), vitality 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EowynStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Éowyn, Willing Fighter
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Valiant</b>.
		 * 	<b>Response:</b> If a hunter companion or valiant Man is about to take a wound, exert Éowyn to prevent that wound.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("eowyn");

		assertEquals("Éowyn", card.getBlueprint().getTitle());
		assertEquals("Willing Fighter", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.VALIANT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void ExertsToPreventAWoundToAHunterCompanionOfEachRaceAndCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var faramir = scn.GetFreepsCard("faramir");
		var guardian = scn.GetFreepsCard("guardian");
		scn.MoveCompanionsToTable(eowyn, faramir, guardian);

		var slayer1 = scn.GetShadowCard("slayer1");
		var slayer2 = scn.GetShadowCard("slayer2");

		scn.StartGame();
		scn.MoveMinionsToTable(slayer1, slayer2);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertEquals(0, scn.GetWoundsOn(faramir));
		assertEquals(0, scn.GetWoundsOn(guardian));
		scn.ShadowUseCardAction(slayer1);
		scn.ShadowChooseCard(faramir);

		// A [gondor] Man hunter.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(eowyn));
		scn.FreepsChooseOptionalTrigger(eowyn);
		assertEquals(0, scn.GetWoundsOn(faramir));
		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// An [elven] Elf hunter.
		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer2);
		scn.ShadowChooseCard(guardian);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(eowyn));
		scn.FreepsChooseOptionalTrigger(eowyn);
		assertEquals(0, scn.GetWoundsOn(guardian));
		assertEquals(0, scn.GetWoundsOn(faramir));
		assertEquals(2, scn.GetWoundsOn(eowyn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ExertsToPreventAWoundToAValiantManCompanionHerselfIncluded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(eowyn, guard);

		var slayer1 = scn.GetShadowCard("slayer1");
		var slayer2 = scn.GetShadowCard("slayer2");

		scn.StartGame();
		scn.MoveMinionsToTable(slayer1, slayer2);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertEquals(0, scn.GetWoundsOn(guard));
		scn.ShadowUseCardAction(slayer1);
		scn.ShadowChooseCard(guard);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(eowyn));
		scn.FreepsChooseOptionalTrigger(eowyn);
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// Éowyn herself: her exertion is no wound, and the wound is prevented (2 wounds, not 3).
		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer2);
		scn.ShadowChooseCard(eowyn);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(eowyn));
		scn.FreepsChooseOptionalTrigger(eowyn);
		assertEquals(2, scn.GetWoundsOn(eowyn));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ExertsToPreventAWoundToAManAllyMadeValiant() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		var fire = scn.GetFreepsCard("fire");
		scn.MoveCompanionsToTable(eowyn, guard);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToSupportArea(fire);

		var assassin = scn.GetShadowCard("assassin");

		scn.StartGame();
		scn.MoveMinionsToTable(assassin);
		scn.SkipToPhase(Phase.MANEUVER);

		// The Signal-fire exerts the Household Guard, and Guma gains his valiant until the regroup phase.
		assertFalse(scn.HasKeyword(guma, Keyword.VALIANT));
		assertEquals(0, scn.GetWoundsOn(guard));
		scn.FreepsUseCardAction(fire);
		scn.FreepsChooseCard(guard);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.HasKeyword(guma, Keyword.VALIANT));

		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertEquals(0, scn.GetWoundsOn(guma));
		scn.ShadowUseCardAction(assassin);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(eowyn));
		scn.FreepsChooseOptionalTrigger(eowyn);
		assertEquals(0, scn.GetWoundsOn(guma));
		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void IsNotOfferedForAWoundToAManWhoIsNeitherHunterNorValiantAValiantElfOrAHunterMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var boromir = scn.GetFreepsCard("boromir");
		var lorien = scn.GetFreepsCard("lorien");
		scn.MoveCompanionsToTable(eowyn, boromir, lorien);

		var slayer1 = scn.GetShadowCard("slayer1");
		var slayer2 = scn.GetShadowCard("slayer2");
		var tracker = scn.GetShadowCard("tracker");

		scn.StartGame();
		scn.MoveMinionsToTable(slayer1, slayer2, tracker);
		scn.SkipToPhase(Phase.REGROUP);

		// A hunter who is no companion: the Lórien Guardian has the Shadow player wound the Tracking Uruk.
		assertEquals(0, scn.GetWoundsOn(tracker));
		scn.FreepsUseCardAction(lorien);
		scn.ShadowChooseCard(tracker);
		assertEquals(1, scn.GetWoundsOn(tracker));
		assertEquals(1, scn.GetWoundsOn(lorien));
		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());

		// A Man companion who is neither a hunter nor valiant.
		assertEquals(0, scn.GetWoundsOn(boromir));
		scn.ShadowUseCardAction(slayer1);
		scn.ShadowChooseCard(boromir);
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// A valiant companion who is no Man.
		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer2);
		scn.ShadowChooseCard(lorien);
		assertEquals(2, scn.GetWoundsOn(lorien));
		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhileEowynIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var faramir = scn.GetFreepsCard("faramir");
		scn.MoveCompanionsToTable(eowyn, faramir);

		var slayer1 = scn.GetShadowCard("slayer1");

		scn.StartGame();
		scn.AddWoundsToChar(eowyn, 2);
		assertEquals(2, scn.GetWoundsOn(eowyn));
		scn.MoveMinionsToTable(slayer1);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		// Vitality 3 with 2 wounds: she cannot exert.
		assertEquals(2, scn.GetWoundsOn(eowyn));
		assertEquals(0, scn.GetWoundsOn(faramir));
		scn.ShadowUseCardAction(slayer1);
		scn.ShadowChooseCard(faramir);
		assertEquals(1, scn.GetWoundsOn(faramir));
		assertEquals(2, scn.GetWoundsOn(eowyn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
