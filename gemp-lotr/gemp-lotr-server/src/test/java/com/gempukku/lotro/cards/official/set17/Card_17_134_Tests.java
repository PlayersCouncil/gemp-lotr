package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_17_134_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("vanquisher", "17_134");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// An [uruk-hai] hunter minion and a Free Peoples hunter companion, each to be assigned to a skirmish.
	protected VirtualTableScenario GetHuntersOfBothSidesScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("vanquisher", "17_134");
					put("tracker", "15_171");   // Tracking Uruk: an [uruk-hai] hunter; hunter 1 is its whole text
					put("gimli", "15_5");       // Gimli, Eager Hunter: a [dwarven] hunter companion
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: no hunter
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Two [uruk-hai] hunters to be assigned and a third left unassigned, and an [uruk-hai] minion who is no hunter.
	protected VirtualTableScenario GetUrukHaiHuntersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("vanquisher", "17_134");
					put("tracker", "15_171");   // Tracking Uruk: an [uruk-hai] hunter, strength 14; hunter 1 is its whole text
					put("butcher1", "17_122");  // White Hand Butcher: an [uruk-hai] hunter, strength 5; hunter 6 is its whole text
					put("butcher2", "17_122");
					put("covetous", "15_158");  // Covetous Uruk: an [uruk-hai] Uruk-hai who is no hunter; damage +1, +1 for each possession
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4, no hunter
					put("knight1", "5_35");     // Gondorian Knight: Knight is his whole text
					put("knight2", "5_35");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Saruman, Instigator of Insurrection makes each minion hunter 2 while 6 companions can be spotted: Cirith Ungol
	// Guard is then a hunter Uruk-hai that is no [uruk-hai] card.
	protected VirtualTableScenario GetSarumanScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("vanquisher", "17_134");
					put("tracker", "15_171");   // Tracking Uruk: an [uruk-hai] hunter; hunter 1 is its whole text
					put("cirith", "10_81");     // Cirith Ungol Guard: a [sauron] Uruk-hai; damage +1 is its whole text
					put("saruman", "17_37");    // Saruman, Instigator of Insurrection: cannot be assigned; each minion hunter 2 with 6 companions
					put("knight1", "5_35");     // Gondorian Knight: Knight is his whole text
					put("knight2", "5_35");
					put("knight3", "5_35");
					put("knight4", "5_35");
					put("knight5", "5_35");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhiteHandVanquisherStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: White Hand Vanquisher
		 * Unique: True
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 8
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Fierce</b>.<br>This minion is <b>damage +1</b> for each [uruk-hai] hunter assigned to a skirmish.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("vanquisher");

		assertEquals("White Hand Vanquisher", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	// "damage +1 for each [uruk-hai] hunter assigned to a skirmish": the definition counted every hunter, the Free
	// Peoples player's among them.
	@Test
	public void OnlyUrukHaiCultureHuntersAssignedToASkirmishAreCounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHuntersOfBothSidesScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);

		var vanquisher = scn.GetShadowCard("vanquisher");
		var tracker = scn.GetShadowCard("tracker");

		scn.StartGame();
		scn.MoveMinionsToTable(vanquisher, tracker);
		scn.SkipToAssignments();
		assertEquals(0, scn.GetKeywordCount(vanquisher, Keyword.DAMAGE));

		// Gimli, Eager Hunter is a hunter and is assigned, to the Vanquisher herself; Tracking Uruk is the one
		// [uruk-hai] hunter assigned.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, vanquisher}, new PhysicalCardImpl[]{aragorn, tracker});
		assertTrue(scn.HasKeyword(gimli, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(vanquisher, Keyword.DAMAGE));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void IsDamagePlus1ForEachUrukHaiHunterStillAssignedToASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetUrukHaiHuntersScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var knight1 = scn.GetFreepsCard("knight1");
		var knight2 = scn.GetFreepsCard("knight2");
		scn.MoveCompanionsToTable(aragorn, knight1, knight2);

		var vanquisher = scn.GetShadowCard("vanquisher");
		var tracker = scn.GetShadowCard("tracker");
		var butcher1 = scn.GetShadowCard("butcher1");
		var butcher2 = scn.GetShadowCard("butcher2");
		var covetous = scn.GetShadowCard("covetous");

		scn.StartGame();
		scn.MoveMinionsToTable(vanquisher, tracker, butcher1, butcher2, covetous);
		scn.SkipToAssignments();
		// Three [uruk-hai] hunters in play, none assigned yet.
		assertEquals(0, scn.GetKeywordCount(vanquisher, Keyword.DAMAGE));

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, tracker},
				new PhysicalCardImpl[]{knight1, butcher1},
				new PhysicalCardImpl[]{knight2, covetous});
		// The Vanquisher and the second Butcher are left unassigned.
		scn.ShadowDeclineAssignments();
		assertFalse(scn.IsCharAssigned(butcher2));
		assertFalse(scn.IsCharAssigned(vanquisher));
		assertTrue(scn.IsCharAssigned(covetous));
		// The Tracking Uruk and the first Butcher: 2.  Not the unassigned Butcher, nor the Covetous Uruk.
		assertEquals(2, scn.GetKeywordCount(vanquisher, Keyword.DAMAGE));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// The first Butcher's skirmish (hunter 6: 11 to the Knight's 6) is over, and he is assigned to nothing.
		scn.FreepsResolveSkirmish(knight1);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(knight1));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(1, scn.GetKeywordCount(vanquisher, Keyword.DAMAGE));

		// Then the Tracking Uruk's (15 to Aragorn's 8): no [uruk-hai] hunter is left assigned.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(0, scn.GetKeywordCount(vanquisher, Keyword.DAMAGE));
	}

	@Test
	public void AHunterUrukHaiOfAnotherCultureIsNotCounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSarumanScenario();

		var knight1 = scn.GetFreepsCard("knight1");
		var knight2 = scn.GetFreepsCard("knight2");
		// With the Ring-bearer, six companions.
		scn.MoveCompanionsToTable(knight1, knight2, scn.GetFreepsCard("knight3"),
				scn.GetFreepsCard("knight4"), scn.GetFreepsCard("knight5"));

		var vanquisher = scn.GetShadowCard("vanquisher");
		var tracker = scn.GetShadowCard("tracker");
		var cirith = scn.GetShadowCard("cirith");
		var saruman = scn.GetShadowCard("saruman");

		scn.StartGame();
		scn.MoveMinionsToTable(vanquisher, tracker, cirith, saruman);
		scn.SkipToAssignments();
		assertEquals(0, scn.GetKeywordCount(vanquisher, Keyword.DAMAGE));

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{knight1, tracker},
				new PhysicalCardImpl[]{knight2, cirith});
		scn.ShadowDeclineAssignments();
		assertFalse(scn.IsCharAssigned(vanquisher));
		// The Cirith Ungol Guard is an assigned hunter Uruk-hai, and no [uruk-hai] card: only the Tracking Uruk counts.
		assertEquals(2, scn.GetKeywordCount(cirith, Keyword.HUNTER));
		assertTrue(scn.IsCharAssignedAgainst(knight2, cirith));
		assertEquals(1, scn.GetKeywordCount(vanquisher, Keyword.DAMAGE));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}
}
