package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_218_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("surrounded", "11_218");
					// Ulaire Lemenya, Lieutenant of Morgul: a [wraith] Nazgul; her text reaches only a companion or ally
					// bearing a [wraith] condition.
					put("lemenya", "1_232");
					// Ulaire Attea, Khamul of the East: a [raider] Nazgul, fierce; his text needs 3 burdens.
					put("khamul", "103_61");
					// Morgul Hound: a [wraith] Orc and no Nazgul; strength +4 and fierce only at 2 wounds on the Ring-bearer.
					put("hound", "7_192");
					// Gimli, Son of Gloin: a [dwarven] Dwarf companion, twilight cost 2, vitality 3; a skirmish ability.
					put("gimli", "1_13");
					// Boromir, Son of Denethor: a [gondor] Man companion, twilight cost 3, vitality 3; a skirmish ability.
					put("boromir", "1_97");
					// Sam, Son of Hamfast: a [shire] Hobbit companion, twilight cost 2, vitality 4; a fellowship ability
					// used only on purpose, and a response to Frodo's death.
					put("sam", "1_311");
					// Aragorn, Ranger of the North: a companion already in play; a maneuver ability.
					put("aragorn", "1_89");
					// Bounder: a [shire] Hobbit ally, twilight cost 1, no requirement to play; a skirmish ability.
					put("bounder", "1_286");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SurroundedbyWraithsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Surrounded by Wraiths
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot a Nazgûl.
		 * Each time the Free Peoples player plays a companion, that companion is exhausted unless the Free Peoples player pays its twilight cost again.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("surrounded");

		assertEquals("Surrounded by Wraiths", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedSpottingOnlyAWraithOrcAndIsPlayedOnceAWraithNazgulIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var surrounded = scn.GetShadowCard("surrounded");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(surrounded);

		scn.StartGame();
		scn.MoveMinionsToTable(hound);
		scn.SetTwilight(5);
		assertInHand(surrounded);
		assertInZone(Zone.SHADOW_CHARACTERS, hound);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		// A [wraith] minion who is no Nazgul is no Nazgul to spot.
		assertFalse(scn.ShadowPlayAvailable(surrounded));

		scn.MoveMinionsToTable(lemenya);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(surrounded));

		scn.ShadowPlayCard(surrounded);
		assertInZone(Zone.SUPPORT, surrounded);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsPlayedSpottingARaiderNazgul() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var surrounded = scn.GetShadowCard("surrounded");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCardsToHand(surrounded);

		scn.StartGame();
		scn.MoveMinionsToTable(khamul);
		scn.SetTwilight(5);
		assertInHand(surrounded);
		assertInZone(Zone.SHADOW_CHARACTERS, khamul);

		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(surrounded));

		scn.ShadowPlayCard(surrounded);
		assertInZone(Zone.SUPPORT, surrounded);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnAllyPlayedIsNotAnsweredAndEachCompanionPlayedIsExhaustedUnlessItsOwnCostIsPaidAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var surrounded = scn.GetShadowCard("surrounded");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCardsToSupportArea(surrounded);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(gimli, boromir, sam, bounder);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, surrounded);
		assertInHand(gimli, boromir, sam, bounder);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetTwilight());

		// An ally is no companion: nothing is asked.
		scn.FreepsPlayCard(bounder);
		assertFalse(scn.FreepsDecisionAvailable("twilight cost again"));
		assertEquals(1, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Gimli costs 2; the Free Peoples player pays it again and Gimli is not exhausted.
		scn.FreepsPlayCard(gimli);
		assertTrue(scn.FreepsDecisionAvailable("twilight cost again"));
		scn.FreepsChooseYes();
		// 1 + 2 to play + 2 again
		assertEquals(5, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The second companion played is answered again.  Boromir costs 3, paid again.
		scn.FreepsPlayCard(boromir);
		assertTrue(scn.FreepsDecisionAvailable("twilight cost again"));
		scn.FreepsChooseYes();
		// 5 + 3 to play + 3 again
		assertEquals(11, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Sam costs 2; the Free Peoples player does not pay, and Sam (vitality 4) is exhausted: 3 wounds.
		scn.FreepsPlayCard(sam);
		assertTrue(scn.FreepsDecisionAvailable("twilight cost again"));
		scn.FreepsChooseNo();
		// 11 + 2 to play
		assertEquals(13, scn.GetTwilight());
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
