package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_123_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("prize", "18_123");
					put("uruk1", "15_171");     // Tracking Uruk: [uruk-hai] Uruk-hai, strength 14; Hunter 1 is its whole text
					put("uruk2", "15_171");
					put("butcher", "17_122");   // White Hand Butcher: [uruk-hai] Uruk-hai, strength 5; Hunter 6 is its whole text
					put("herdsman", "15_83");   // Hunting Herdsman: [men] Man, Hunter 1, strength 8; +3 for each follower, and none is laid
					put("tyrannical", "11_204"); // Tyrannical Uruk: [uruk-hai] Uruk-hai, no hunter; Damage +1 and toil only

					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion; skirmish ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a Maneuver ability used only on purpose
					put("faramir", "18_48");    // Faramir, Captain of Ithilien: [gondor] companion who is a hunter; a Maneuver heal used only on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally; his own ability is not used
					put("follow", "1_109");     // One Whom Men Would Follow: exert Aragorn to have an ally participate in skirmishes
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TrackingthePrizeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Tracking the Prize
		 * Unique: True
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: While you can spot 3 [uruk-hai] hunters, at the start of each skirmish involving an [uruk-hai] hunter, each companion skirmishing that minion must exert.
		*/

		var scn = GetScenario();

		var prize = scn.GetFreepsCard("prize");

		assertEquals("Tracking the Prize", prize.getBlueprint().getTitle());
		assertNull(prize.getBlueprint().getSubtitle());
		assertTrue(prize.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, prize.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, prize.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, prize.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(prize, Keyword.SUPPORT_AREA));
		assertEquals(2, prize.getBlueprint().getTwilightCost());
	}

	@Test
	public void AtTheStartOfEachSkirmishOfAnUrukHaiHunterEachCompanionSkirmishingItMustExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var faramir = scn.GetFreepsCard("faramir");
		var prize = scn.GetShadowCard("prize");
		var uruk1 = scn.GetShadowCard("uruk1");
		var uruk2 = scn.GetShadowCard("uruk2");
		var butcher = scn.GetShadowCard("butcher");
		scn.MoveCompanionsToTable(gimli, aragorn, faramir);
		scn.MoveCardsToSupportArea(prize);
		assertInZone(Zone.SUPPORT, prize);

		scn.StartGame();
		// Exactly three [uruk-hai] hunters, of two titles.
		scn.MoveMinionsToTable(uruk1, uruk2, butcher);
		// Faramir, vitality 3, is exhausted: he cannot exert, and a wound would kill him.
		scn.AddWoundsToChar(faramir, 2);
		assertEquals(2, scn.GetWoundsOn(faramir));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, uruk1}, new PhysicalCardImpl[]{aragorn, butcher},
				new PhysicalCardImpl[]{faramir, uruk2});

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsResolveSkirmish(gimli);
		// Gimli's skirmish starts: he exerts, and no one in a skirmish still to come does.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(faramir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Gimli (6) is overwhelmed by the Tracking Uruk (14 + hunter 1).
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(aragorn);
		// A second skirmish of an [uruk-hai] hunter, a companion of another culture and race: he exerts too.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(faramir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Aragorn (8) loses to the Butcher (5 + hunter 6) and takes a wound.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		scn.FreepsResolveSkirmish(faramir);
		// The exhausted Faramir cannot exert, and is not wounded in its place.
		assertEquals(2, scn.GetWoundsOn(faramir));
		assertInZone(Zone.FREE_CHARACTERS, faramir);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithTwoUrukHaiHuntersNoCompanionExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var faramir = scn.GetFreepsCard("faramir");
		var prize = scn.GetShadowCard("prize");
		var uruk1 = scn.GetShadowCard("uruk1");
		var butcher = scn.GetShadowCard("butcher");
		var herdsman = scn.GetShadowCard("herdsman");
		var tyrannical = scn.GetShadowCard("tyrannical");
		scn.MoveCompanionsToTable(gimli, faramir);
		scn.MoveCardsToSupportArea(prize);
		assertInZone(Zone.SUPPORT, prize);

		scn.StartGame();
		// Two [uruk-hai] hunters; beside them a [men] hunter, an [uruk-hai] minion who is no hunter and a [gondor]
		// hunter companion, each of which would be the third if a word of "[uruk-hai] hunters" were dropped.
		scn.MoveMinionsToTable(uruk1, butcher, herdsman, tyrannical);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, uruk1);
		scn.ShadowDeclineAssignments();

		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ASkirmishWithNoUrukHaiHunterOrNoCompanionInItExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var faramir = scn.GetFreepsCard("faramir");
		var thrarin = scn.GetFreepsCard("thrarin");
		var follow = scn.GetFreepsCard("follow");
		var prize = scn.GetShadowCard("prize");
		var uruk1 = scn.GetShadowCard("uruk1");
		var uruk2 = scn.GetShadowCard("uruk2");
		var butcher = scn.GetShadowCard("butcher");
		var herdsman = scn.GetShadowCard("herdsman");
		var tyrannical = scn.GetShadowCard("tyrannical");
		scn.MoveCompanionsToTable(gimli, aragorn, faramir);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(follow);
		scn.MoveCardsToSupportArea(prize);
		assertInZone(Zone.SUPPORT, prize);
		assertInHand(follow);

		scn.StartGame();
		// Three [uruk-hai] hunters in play throughout.
		scn.MoveMinionsToTable(uruk1, uruk2, butcher, herdsman, tyrannical);
		scn.SkipToPhase(Phase.MANEUVER);

		// Thrarin is made to take part in skirmishes, by legal play.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsPlayCard(follow);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, herdsman}, new PhysicalCardImpl[]{faramir, tyrannical},
				new PhysicalCardImpl[]{thrarin, uruk1}, new PhysicalCardImpl[]{aragorn, uruk2},
				new PhysicalCardImpl[]{frodo, butcher});

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(faramir));
		assertEquals(0, scn.GetWoundsOn(thrarin));

		// A hunter who is not [uruk-hai].
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// A hunter, and an [uruk-hai] minion who is no hunter.
		scn.FreepsResolveSkirmish(faramir);
		assertEquals(0, scn.GetWoundsOn(faramir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// An [uruk-hai] hunter skirmishing an ally, who is no companion.
		scn.FreepsResolveSkirmish(thrarin);
		assertEquals(0, scn.GetWoundsOn(thrarin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// The same hunter's twin against a companion: he exerts.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
