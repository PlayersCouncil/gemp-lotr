package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_147_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("farmer", "15_147");

					// Hobbits beside the Ring-bearer, Frodo, Son of Drogo (strength 3, and The Ruling Ring's +1).
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion, strength 3 (cheated in); a skirmish ability used only on purpose
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit ally, twilight 1; a skirmish ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: strength 3; a fellowship ability used only on purpose
					// Not Hobbits.
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit (cheated in); an assignment ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man; a maneuver ability used only on purpose
					// Daddy Twofoot: a [shire] follower, which has no race; its texts answer its own Aid
					put("twofoot", "13_144");

					// White Hand Destroyer: a fierce [uruk-hai] Uruk-hai; hunter only for sites controlled (none are)
					put("destroyer", "17_124");
					// Black Rider: a fierce [wraith] Nazgul, twilight 5, site 3; strength +1 for each companion
					put("rider", "12_161");
					put("savage", "1_151");     // Uruk Savage: a minion who is not fierce; damage +1 and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HobbitonFarmerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Hobbiton Farmer, Lover of Pipeweed
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Follower
		 * Subtype:
		 * Strength: +1
		 * Resistance: +1
		 * Aid Cost: Add a burden
		 * Game Text: <b>Aid</b> – Add a burden. <helper>(At the start of the maneuver phase, you may add a burden to transfer this to a companion.)</helper><br>To play, spot 2 Hobbits.<br>Bearer is strength +1 for each fierce minion you can spot.
		*/

		var scn = GetScenario();

		var farmer = scn.GetFreepsCard("farmer");

		assertEquals("Hobbiton Farmer", farmer.getBlueprint().getTitle());
		assertEquals("Lover of Pipeweed", farmer.getBlueprint().getSubtitle());
		assertTrue(farmer.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, farmer.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, farmer.getBlueprint().getCulture());
		assertEquals(CardType.FOLLOWER, farmer.getBlueprint().getCardType());
		assertEquals(2, farmer.getBlueprint().getTwilightCost());
		assertEquals(1, farmer.getBlueprint().getStrength());
		assertEquals(1, farmer.getBlueprint().getResistance());
	}

	@Test
	public void CannotBePlayedUntilASecondHobbitIsSpottedAndAHobbitAllyIsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var farmer = scn.GetFreepsCard("farmer");
		var bounder = scn.GetFreepsCard("bounder");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var twofoot = scn.GetFreepsCard("twofoot");
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.MoveCardsToSupportArea(twofoot);
		scn.MoveCardsToHand(farmer, bounder);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, twofoot);
		assertInHand(farmer, bounder);

		// The Ring-bearer is the one Hobbit: a [shire] companion who is no Hobbit, a Man and a [shire] follower
		// (no race) do not make a second.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(farmer));

		scn.FreepsPlayCard(bounder);
		assertInZone(Zone.SUPPORT, bounder);
		assertTrue(scn.FreepsPlayAvailable(farmer));

		scn.FreepsPlayCard(farmer);
		assertInZone(Zone.SUPPORT, farmer);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayedBesideAGondorHobbitItsAidAddsABurdenAndBearerIsStrengthPlus1ForTheFierceMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var farmer = scn.GetFreepsCard("farmer");
		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var destroyer = scn.GetShadowCard("destroyer");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToHand(farmer);

		scn.StartGame();
		assertInHand(farmer);
		// Two Hobbits: the Ring-bearer and a [gondor] Hobbit.
		assertTrue(scn.FreepsPlayAvailable(farmer));
		scn.FreepsPlayCard(farmer);
		assertInZone(Zone.SUPPORT, farmer);

		scn.MoveMinionsToTable(destroyer, savage);
		assertInZone(Zone.SHADOW_CHARACTERS, destroyer, savage);
		// In the support area it is borne by nobody: a fierce minion in play changes no one.
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(4, scn.GetStrength(frodo));

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(farmer));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, pippin));
		scn.FreepsChooseCard(pippin);
		assertAttachedTo(farmer, pippin);

		// 3 + 1 printed + 1 for the Destroyer; the Uruk Savage is not fierce.  Frodo bears nothing.
		assertEquals(5, scn.GetStrength(pippin));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void BearerIsStrengthPlus1ForEachFierceMinionOfAnyCultureAndNotForOneWhoIsNotFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var farmer = scn.GetFreepsCard("farmer");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var destroyer = scn.GetShadowCard("destroyer");
		var savage = scn.GetShadowCard("savage");
		var rider = scn.GetShadowCard("rider");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(rider);

		scn.StartGame();
		assertInHand(rider);
		scn.AttachCardsTo(sam, farmer);
		assertAttachedTo(farmer, sam);
		// No minion: 3 + 1 printed.
		assertEquals(4, scn.GetStrength(sam));

		scn.MoveMinionsToTable(destroyer, savage);
		assertInZone(Zone.SHADOW_CHARACTERS, destroyer, savage);
		// One fierce minion; the Uruk Savage is not fierce.
		assertEquals(5, scn.GetStrength(sam));
		assertEquals(4, scn.GetStrength(frodo));

		// 5 + 2 for the move to Ettenmoors and 2 for the companions: Black Rider's 5 and 2 roaming.
		scn.SetTwilight(5);
		scn.FreepsPass();
		assertEquals(9, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(rider));
		scn.ShadowPlayCard(rider);
		assertInZone(Zone.SHADOW_CHARACTERS, rider);

		// A second fierce minion, of another culture and race, played: every one is counted, nobody asked.
		assertEquals(6, scn.GetStrength(sam));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
