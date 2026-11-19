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

public class Card_12_086_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hammer", "12_86");
					put("grunt", "13_117");     // Ordnance Grunt: [orc] Troll, not unique; a discard-pile ability only
					put("deep", "19_23");       // Troll of the Deep, Cave Troll: unique [orc] Troll; a skirmish ability only
					put("vile", "12_106");      // Vile Goblin: [orc] Orc, no lurker; fierce only when assigned to a Dwarf
					put("gorgor", "10_101");    // Troll of Cirith Gorgor: [sauron] Troll
					put("halftroll", "14_12");  // Half-troll of Far Harad: [orc] Half-troll; a start-of-assignment trigger
					put("skulker", "12_95");    // Orc Skulker: [orc] Orc, lurker; skirmish events cost 1 less
					put("hillman", "12_69");    // Harrying Hillman: [men] Man, lurker; adds (2) when a companion loses to a [men] minion
					put("ax", "18_84");         // Orkish Ax: [orc] hand weapon, cost 1, bearer must be an [orc] Orc

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] companion, vitality 3
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] companion, vitality 3
					put("barliman", "1_70");    // Barliman Butterbur: [gandalf] ally, vitality 2; a fellowship ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CaveTrollsHammerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Cave Troll's Hammer, Unwieldy Cudgel
		 * Unique: True
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 3
		 * Game Text: Bearer must be an [orc] Troll.<br>When you play this possession, the Free Peoples player must exert a companion for each lurker you spot.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("hammer");

		assertEquals("Cave Troll's Hammer", card.getBlueprint().getTitle());
		assertEquals("Unwieldy Cudgel", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnAnOrcTrollOnlyAndWithNoLurkerExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var hammer = scn.GetShadowCard("hammer");
		var grunt = scn.GetShadowCard("grunt");
		var deep = scn.GetShadowCard("deep");
		var vile = scn.GetShadowCard("vile");
		var gorgor = scn.GetShadowCard("gorgor");
		var halftroll = scn.GetShadowCard("halftroll");

		scn.StartGame();
		scn.SetShadowHand(hammer);
		scn.MoveMinionsToTable(grunt, deep, vile, gorgor, halftroll);
		assertInHand(hammer);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(scn.GetRingBearer()));

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPlayCard(hammer);
		// Two [orc] Trolls; an [orc] Orc, a [sauron] Troll and an [orc] Half-troll are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(grunt, deep));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(vile, gorgor, halftroll));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(deep);
		assertAttachedTo(hammer, deep);

		// No lurker to spot: nothing is asked, and nobody exerts.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(scn.GetRingBearer()));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenPlayedTheFreePeoplesPlayerExertsACompanionForEachLurkerSpottedTheSameOneTwiceIfHeLikes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(aragorn, gimli, boromir);
		scn.MoveAlliesToTable(barliman);
		// Boromir exhausted: 2 wounds on vitality 3.
		scn.AddWoundsToChar(boromir, 2);

		var hammer = scn.GetShadowCard("hammer");
		var grunt = scn.GetShadowCard("grunt");
		var vile = scn.GetShadowCard("vile");
		var skulker = scn.GetShadowCard("skulker");
		var hillman = scn.GetShadowCard("hillman");

		scn.StartGame();
		scn.SetShadowHand(hammer);
		scn.MoveMinionsToTable(grunt, vile, skulker, hillman);
		assertInHand(hammer);
		assertEquals(2, scn.GetWoundsOn(boromir));

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		// The Grunt is the one [orc] Troll: the Hammer goes on him unasked.
		scn.ShadowPlayCard(hammer);
		assertAttachedTo(hammer, grunt);

		// Two lurkers, of two cultures; the Grunt and the Vile Goblin are none.  The Shadow player names how many he spots.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertEquals(Integer.valueOf(2), scn.ShadowGetChoiceDefault());
		scn.ShadowChoose("2");

		// The Free Peoples player chooses each companion, the exhausted one, the ally and the minions not offered.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir, barliman, grunt, vile));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		// "For each" may come to the same companion again.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli));
		scn.FreepsChooseCard(aragorn);

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SpottingOneOfTwoLurkersExertsOnceAndAnotherPossessionPlayedAfterExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var hammer = scn.GetShadowCard("hammer");
		var ax = scn.GetShadowCard("ax");
		var grunt = scn.GetShadowCard("grunt");
		var vile = scn.GetShadowCard("vile");
		var skulker = scn.GetShadowCard("skulker");
		var hillman = scn.GetShadowCard("hillman");

		scn.StartGame();
		scn.SetShadowHand(hammer, ax);
		scn.MoveMinionsToTable(grunt, vile, skulker, hillman);
		assertInHand(hammer, ax);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPlayCard(hammer);
		assertAttachedTo(hammer, grunt);
		scn.ShadowChoose("1");
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gimli));
		scn.FreepsChooseCard(gimli);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another [orc] possession played after the Hammer is in play asks nothing.
		scn.ShadowPlayCard(ax);
		assertTrue(scn.ShadowHasCardChoiceAvailable(vile, skulker));
		scn.ShadowChooseCard(vile);
		assertAttachedTo(ax, vile);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
