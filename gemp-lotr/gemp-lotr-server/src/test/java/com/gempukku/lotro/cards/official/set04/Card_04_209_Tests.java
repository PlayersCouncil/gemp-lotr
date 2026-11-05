package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_209_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("volley", "4_209");
					// Orthanc Assassin: an [isengard] Uruk-hai archer, damage +1 and nothing else.
					put("assassin", "1_131");
					// Uruk Crossbow Troop: an [isengard] Uruk-hai archer; an optional token on a machine when played.
					put("crossbow", "4_182");
					// Band of Uruk Bowmen: an [isengard] Uruk-hai archer, damage +1; minion archery total +1 only
					// while its player controls a site, and none is controlled here.
					put("bowmen", "4_138");
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai who is no archer; damage +1 only
					// Lurtz, Halfling Hunter: an [uruk-hai] Uruk-hai archer; a maneuver and an assignment ability,
					// neither used.
					put("lurtz", "18_118");
					put("marksman", "1_176");   // Goblin Marksman: a [moria] Orc archer and nothing else

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: unbound [gondor] Man, vitality 4; a maneuver ability never used
					put("gimli", "1_13");       // Gimli, Son of Glóin: unbound [dwarven] Dwarf, vitality 3; a skirmish ability never used
					put("boromir", "1_97");     // Boromir, Son of Denethor: unbound, vitality 3; a skirmish ability never used
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound and not the Ring-bearer
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit ally; a skirmish ability never used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void VolleyFireStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Volley Fire
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Archery
		 * Game Text: <b>Archery:</b> Discard 2 [isengard] archers to exhaust an unbound companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("volley");

		assertEquals("Volley Fire", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.ARCHERY));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardingTwoIsengardArchersExhaustsAnUnboundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var volley = scn.GetShadowCard("volley");
		var assassin = scn.GetShadowCard("assassin");
		var crossbow = scn.GetShadowCard("crossbow");
		var bowmen = scn.GetShadowCard("bowmen");
		var savage = scn.GetShadowCard("savage");
		var lurtz = scn.GetShadowCard("lurtz");
		var marksman = scn.GetShadowCard("marksman");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn, gimli, boromir, sam);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(volley);
		assertInZone(Zone.HAND, volley);

		scn.StartGame();
		// Vitality 3 with 2 wounds: Boromir is exhausted already, and so no candidate for exhausting (F-0005).
		scn.AddWoundsToChar(boromir, 2);
		assertEquals(2, scn.GetWoundsOn(boromir));
		scn.MoveMinionsToTable(assassin, crossbow, bowmen, savage, lurtz, marksman);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(volley));
		scn.ShadowPlayCard(volley);

		// The cost: [isengard] archers of three titles; not an [isengard] Uruk-hai who is no archer, an
		// [uruk-hai] Uruk-hai archer, nor a [moria] archer.  Exactly two.
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(assassin, crossbow, bowmen));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, lurtz, marksman));
		scn.ShadowChooseCards(assassin, crossbow);
		assertInZone(Zone.DISCARD, assassin, crossbow);
		assertInZone(Zone.SHADOW_CHARACTERS, bowmen, savage, lurtz, marksman);

		// The target: unbound companions of two cultures and races; not the exhausted one, not Sam (Ring-bound),
		// not Frodo, and not the ally.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(boromir, sam, frodo, bounder));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.ShadowChooseCard(aragorn);

		// Vitality 4: exhausted is 3 wounds, where exerting twice would be 2.
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertInZone(Zone.DISCARD, volley);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void CannotBePlayedWithOneIsengardArcherBesideArchersOfOtherCulturesAndAnIsengardMinionWhoIsNoArcher() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var volley = scn.GetShadowCard("volley");
		var assassin = scn.GetShadowCard("assassin");
		var savage = scn.GetShadowCard("savage");
		var lurtz = scn.GetShadowCard("lurtz");
		var marksman = scn.GetShadowCard("marksman");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(volley);
		assertInZone(Zone.HAND, volley);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin, savage, lurtz, marksman);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		// The twilight cost can be paid; the cost in the text cannot.
		assertTrue(scn.GetTwilight() >= 3);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertInZone(Zone.HAND, volley);
		assertFalse(scn.ShadowPlayAvailable(volley));
		assertInZone(Zone.SHADOW_CHARACTERS, assassin, savage, lurtz, marksman);
	}

	@Test
	public void WithNoUnboundCompanionItIsPlayedAndTheArchersAreDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var volley = scn.GetShadowCard("volley");
		var assassin = scn.GetShadowCard("assassin");
		var crossbow = scn.GetShadowCard("crossbow");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(sam);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(volley);
		assertInZone(Zone.HAND, volley);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin, crossbow);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		// Only Frodo and Sam, both Ring-bound, and an ally: the event's target is no requirement (F-0665).
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(volley));
		scn.ShadowPlayCard(volley);

		// The two archers are the only two there are, and are discarded unasked; nobody is exhausted.
		assertInZone(Zone.DISCARD, assassin, crossbow, volley);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}
}
