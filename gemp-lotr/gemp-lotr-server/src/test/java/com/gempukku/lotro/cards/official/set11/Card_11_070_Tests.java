package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_070_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bloodthirsty", "11_70");
					// Corps of Harad: [men] Man minion; fierce only at a battleground, and this path has none
					put("corps", "11_73");
					// Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit; his abilities are used only on purpose
					put("ted", "18_75");
					put("soldier", "4_254");    // Southron Soldier: a [raider] Man minion; Southron and ambush (2) only
					// Goaded to War: a [men] condition in the support area; its trigger answers a move to a
					// battleground or plains, and no move is made after it is laid
					put("goaded", "12_67");
					put("gimli", "1_13");       // Gimli, Son of Gloin: an unbound [dwarven] Dwarf, vitality 3; a skirmish ability
					put("boromir", "1_97");     // Boromir, Son of Denethor: an unbound [gondor] Man, vitality 3; a skirmish ability
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, and not the Ring-bearer
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: a Dwarf ally (only companions are unbound)
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BloodthirstyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Bloodthirsty
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: Spot 2 [men] minions to wound an unbound companion.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("bloodthirsty");

		assertEquals("Bloodthirsty", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingAMenManAndAMenHobbitItWoundsAnUnboundCompanionAnExhaustedOneIncluded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bloodthirsty = scn.GetShadowCard("bloodthirsty");
		var corps = scn.GetShadowCard("corps");
		var ted = scn.GetShadowCard("ted");
		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var sam = scn.GetFreepsCard("sam");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(gimli, boromir, sam);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(bloodthirsty);
		// Boromir exhausted: a wound may still be placed on him, where an exertion could not.
		scn.AddWoundsToChar(boromir, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(corps, ted);
		assertEquals(2, scn.GetWoundsOn(boromir));
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(1);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowPlayAvailable(bloodthirsty));
		scn.ShadowPlayCard(bloodthirsty);

		// Unbound companions of two cultures and races, one exhausted; not the Ring-bearer, a Ring-bound companion,
		// an ally, or a minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli, boromir));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, sam, thrarin, corps, ted));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(boromir);

		// A wound, not an exertion: the exhausted Boromir takes it and is killed.
		assertInZone(Zone.DEAD, boromir);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(thrarin));
		assertEquals(0, scn.GetWoundsOn(corps));
		assertEquals(0, scn.GetWoundsOn(ted));
		assertInDiscard(bloodthirsty);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedWithOneMenMinionBesideAMenConditionAndAManMinionOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bloodthirsty = scn.GetShadowCard("bloodthirsty");
		var corps = scn.GetShadowCard("corps");
		var soldier = scn.GetShadowCard("soldier");
		var goaded = scn.GetShadowCard("goaded");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(bloodthirsty);

		scn.StartGame();
		scn.MoveMinionsToTable(corps, soldier);
		scn.MoveCardsToSupportArea(goaded);
		assertInHand(bloodthirsty);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(1);
		scn.FreepsPass();

		assertInHand(bloodthirsty);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(bloodthirsty));
		assertEquals(0, scn.GetWoundsOn(gimli));
	}

	@Test
	public void WithNoUnboundCompanionItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bloodthirsty = scn.GetShadowCard("bloodthirsty");
		var corps = scn.GetShadowCard("corps");
		var ted = scn.GetShadowCard("ted");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(sam);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(bloodthirsty);

		scn.StartGame();
		scn.MoveMinionsToTable(corps, ted);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(1);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(bloodthirsty));
		scn.ShadowPlayCard(bloodthirsty);

		assertInDiscard(bloodthirsty);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(thrarin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
