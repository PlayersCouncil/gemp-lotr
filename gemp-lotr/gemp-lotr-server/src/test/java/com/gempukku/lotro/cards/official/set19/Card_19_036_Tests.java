package com.gempukku.lotro.cards.official.set19;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_19_036_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("enquea", "19_36");
					// Úlairë Nelya, Lieutenant of Morgul: a [wraith] Nazgûl; only her Shadow ability, never used
					put("nelya", "1_233");
					// Ulaire Attea, Khamul of the East: a [raider] Nazgûl, so a Nazgûl of another culture
					put("khamul", "103_61");
					// Morgul Creeper: a [wraith] Orc, so a [wraith] minion who is no Nazgûl
					put("creeper", "8_75");

					// Six companions with the Ring-bearer. Sam, Son of Hamfast is Ring-bound; Aragorn, Ranger of
					// the North ([gondor] Man) and Gimli, Son of Glóin ([dwarven] Dwarf) are unbound companions
					// of two cultures and races. Legolas, Companion of the Ring and Boromir, Son of Denethor make
					// up the six. Their abilities are never used.
					put("sam", "1_311");
					put("aragorn", "1_89");
					put("gimli", "1_13");
					put("legolas", "11_21");
					put("boromir", "1_97");
					// Thrarin, Dwarven Smith: an ally, so a Free Peoples character who is no companion
					put("thrarin", "1_27");

					// Gondorian Ranger and Ranger of Ithilien: Ring-bound [gondor] companions, not unique. Their
					// texts are about skirmishes, roaming minions and the starting fellowship.
					put("gondorian1", "4_122");
					put("gondorian2", "4_122");
					put("ithilien1", "4_130");
					put("ithilien2", "4_130");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireEnqueaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 19
		 * Name: Úlairë Enquëa, Dark Threat
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 11
		 * Vitality: 4
		 * Site Number: 3
		 * Game Text: <b>Fierce</b>.<br><b>Maneuver:</b> Spot 6 companions and discard a Nazgûl from hand to wound an unbound companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("enquea");

		assertEquals("Úlairë Enquëa", card.getBlueprint().getTitle());
		assertEquals("Dark Threat", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ManeuverAbilityDiscardsANazgulFromHandToWoundAnUnboundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enquea = scn.GetShadowCard("enquea");
		var nelya = scn.GetShadowCard("nelya");
		var khamul = scn.GetShadowCard("khamul");
		var creeper = scn.GetShadowCard("creeper");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var boromir = scn.GetFreepsCard("boromir");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(sam, aragorn, gimli, legolas, boromir);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(nelya, khamul, creeper);

		scn.StartGame();
		scn.MoveMinionsToTable(enquea);
		// Aragorn is exhausted: a wound kills him
		scn.AddWoundsToChar(aragorn, 3);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertInHand(nelya, khamul, creeper);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(enquea));
		scn.ShadowUseCardAction(enquea);

		// Either Nazgûl pays, whatever its culture; the [wraith] Orc does not
		assertTrue(scn.ShadowHasCardChoiceAvailable(nelya, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(creeper));
		scn.ShadowChooseCard(khamul);
		assertInDiscard(khamul);
		assertInHand(nelya, creeper);

		// Only the unbound companions: not the Ring-bearer, not Ring-bound Sam, not the ally, not a minion
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli, legolas, boromir));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, sam, thrarin, enquea));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(gimli);

		// One wound, not two; nobody else is touched
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(thrarin));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// Six companions are still spotted and a Nazgul is still in hand: the ability is offered again, and a
		// wound kills the exhausted Aragorn (an exertion could not)
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(enquea));
		scn.ShadowUseCardAction(enquea);
		assertInDiscard(nelya);
		assertInHand(creeper);
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli, legolas, boromir));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, sam, thrarin, enquea));
		scn.ShadowChooseCard(aragorn);

		assertInZone(Zone.DEAD, aragorn);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AbilityIsNotOfferedWithFiveCompanionsAndAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enquea = scn.GetShadowCard("enquea");
		var nelya = scn.GetShadowCard("nelya");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var thrarin = scn.GetFreepsCard("thrarin");
		// Five companions with the Ring-bearer, and an ally who is a sixth Free Peoples character
		scn.MoveCompanionsToTable(sam, aragorn, gimli, legolas);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(nelya);

		scn.StartGame();
		scn.MoveMinionsToTable(enquea);
		assertInHand(nelya);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(enquea));
	}

	@Test
	public void AbilityIsNotOfferedWithNoNazgulInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enquea = scn.GetShadowCard("enquea");
		var creeper = scn.GetShadowCard("creeper");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("sam"), scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("gimli"),
				scn.GetFreepsCard("legolas"), scn.GetFreepsCard("boromir"));
		// A [wraith] minion in hand, but no Nazgûl
		scn.MoveCardsToHand(creeper);

		scn.StartGame();
		scn.MoveMinionsToTable(enquea);
		assertInHand(creeper);
		assertEquals(1, scn.GetShadowHandCount());
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(enquea));
	}

	@Test
	public void WithSixCompanionsAllRingBoundTheNazgulIsDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enquea = scn.GetShadowCard("enquea");
		var nelya = scn.GetShadowCard("nelya");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var gondorian1 = scn.GetFreepsCard("gondorian1");
		var gondorian2 = scn.GetFreepsCard("gondorian2");
		var ithilien1 = scn.GetFreepsCard("ithilien1");
		var ithilien2 = scn.GetFreepsCard("ithilien2");
		scn.MoveCompanionsToTable(sam, gondorian1, gondorian2, ithilien1, ithilien2);
		scn.MoveCardsToHand(nelya);

		scn.StartGame();
		scn.MoveMinionsToTable(enquea);
		assertInHand(nelya);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Six companions spotted, a Nazgûl to discard: the ability is offered, with no unbound companion to wound
		assertTrue(scn.ShadowActionAvailable(enquea));
		scn.ShadowUseCardAction(enquea);

		assertInDiscard(nelya);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(gondorian1));
		assertEquals(0, scn.GetWoundsOn(gondorian2));
		assertEquals(0, scn.GetWoundsOn(ithilien1));
		assertEquals(0, scn.GetWoundsOn(ithilien2));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
