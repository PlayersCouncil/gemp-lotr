package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_036_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cast", "10_36");

					put("scout", "4_252");      // Southron Scout: a Southron Man, ambush only
					put("rearguard", "103_43"); // Bladetusk Rearguard: [raider] artifact that its maneuver ability makes a Southron minion who is no Man
					put("axeman", "4_224");     // Easterling Axeman: a [raider] Man who is no Southron (fierce only at 2 burdens)
					put("gunners", "8_55");     // Corsair Gunners: a [raider] Man who is no Southron; a skirmish ability only

					put("gimli", "1_13");       // Gimli, Son of Gloin: an unbound [dwarven] Dwarf; a skirmish ability only
					put("boromir", "1_97");     // Boromir, Son of Denethor: an unbound [gondor] Man; a skirmish ability only
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound companion who is not the Ring-bearer
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: a Dwarf ally, no companion and so not unbound
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CastUntotheWindsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Cast Unto the Winds
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Regroup:</b> Spot a Southron, remove a threat, and remove (3) to wound an unbound companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("cast");

		assertEquals("Cast Unto the Winds", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RegroupAbilityRemovesAThreatAndThreeTwilightToWoundAnUnboundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cast = scn.GetShadowCard("cast");
		var scout = scn.GetShadowCard("scout");
		scn.MoveCardsToSupportArea(cast);

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var sam = scn.GetFreepsCard("sam");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(gimli, boromir, sam);
		scn.MoveAlliesToTable(thrarin);
		// Boromir exhausted: a wound may still be placed on him, where an exertion could not.
		scn.AddWoundsToChar(boromir, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(scout);
		assertEquals(2, scn.GetWoundsOn(boromir));
		scn.SkipToPhase(Phase.REGROUP);

		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertEquals(2, scn.GetThreats());
		assertEquals(5, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowActionAvailable(cast));
		scn.ShadowUseCardAction(cast);

		assertEquals(1, scn.GetThreats());
		assertEquals(2, scn.GetTwilight());
		// Unbound companions of two cultures and races, one exhausted; not the Ring-bearer, a Ring-bound companion,
		// an ally, or a minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli, boromir));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, sam, thrarin, scout));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(gimli);

		// One wound.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(thrarin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ASouthronWhoIsNoManIsSpottedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cast = scn.GetShadowCard("cast");
		var rearguard = scn.GetShadowCard("rearguard");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToSupportArea(cast, rearguard);

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		// The Axeman, no Southron, is there so that the game has a maneuver phase.
		scn.MoveMinionsToTable(axeman);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		scn.FreepsPass();
		assertFalse(scn.HasKeyword(rearguard, Keyword.SOUTHRON));
		scn.ShadowUseCardAction(rearguard);
		// A Southron minion now, and still no Man.
		assertTrue(scn.HasKeyword(rearguard, Keyword.SOUTHRON));
		assertTrue(scn.IsType(rearguard, CardType.MINION));
		assertFalse(scn.IsRace(rearguard, Race.MAN));

		scn.SkipToPhase(Phase.REGROUP);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowActionAvailable(cast));
		scn.ShadowUseCardAction(cast);

		// Gimli is the one unbound companion: wounded unasked.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithoutASouthronTheAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cast = scn.GetShadowCard("cast");
		var axeman = scn.GetShadowCard("axeman");
		var gunners = scn.GetShadowCard("gunners");
		var scout = scn.GetShadowCard("scout");
		scn.MoveCardsToSupportArea(cast);
		// A Southron in hand is not spotted.
		scn.MoveCardsToHand(scout);

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		// [raider] Men, neither of them a Southron.
		scn.MoveMinionsToTable(axeman, gunners);
		scn.SkipToPhase(Phase.REGROUP);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(cast));
		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(gimli));
	}

	@Test
	public void WithNoThreatToRemoveTheAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cast = scn.GetShadowCard("cast");
		var scout = scn.GetShadowCard("scout");
		scn.MoveCardsToSupportArea(cast);

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(scout);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(cast));
	}

	@Test
	public void WithTwoTwilightTheAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cast = scn.GetShadowCard("cast");
		var scout = scn.GetShadowCard("scout");
		scn.MoveCardsToSupportArea(cast);

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(scout);
		scn.SkipToPhase(Phase.REGROUP);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertEquals(1, scn.GetThreats());
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(cast));
	}

	@Test
	public void WithNoUnboundCompanionTheCostsArePaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cast = scn.GetShadowCard("cast");
		var scout = scn.GetShadowCard("scout");
		scn.MoveCardsToSupportArea(cast);

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(sam);
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();
		scn.MoveMinionsToTable(scout);
		scn.SkipToPhase(Phase.REGROUP);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertEquals(1, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(cast));
		scn.ShadowUseCardAction(cast);

		// The costs are paid, and nobody is wounded.
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(thrarin));
		assertEquals(0, scn.GetWoundsOn(scout));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
