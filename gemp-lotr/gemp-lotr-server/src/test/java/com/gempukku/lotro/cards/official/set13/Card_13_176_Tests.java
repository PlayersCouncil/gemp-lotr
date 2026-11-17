package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_176_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("machine", "13_176");
					// [uruk-hai] lurkers, and minions that are not both
					put("entranced", "13_163"); // Entranced Uruk: [uruk-hai] Uruk-hai lurker, vitality 2; a bar on discarding its possessions
					put("saruman", "12_144");   // Saruman, Agent of the Dark Lord: [uruk-hai] Wizard lurker, vitality 4; a skirmish ability used only on purpose, a heal on a kill
					put("commander", "13_158"); // Assault Commander: [uruk-hai] Uruk-hai, no lurker, vitality 3; a Shadow ability used only on purpose
					put("prowler", "11_136");   // Prowling Orc: [orc] Orc lurker, vitality 2; a skirmish ability used only on purpose
					// Weapons in the discard pile
					put("spear", "17_117");     // Spear of the White Hand: [uruk-hai] hand weapon, cost 0, bearer an [uruk-hai] Uruk-hai, no text
					put("sword", "11_192");     // Isengard Sword: [uruk-hai] hand weapon, cost 1, bearer an [uruk-hai] minion; characters skirmishing bearer -2
					put("bow", "11_191");       // Isengard Siege Bow: [uruk-hai] RANGED weapon, cost 1, bearer an [uruk-hai] minion; archer at a battleground
					put("uruksword", "1_160");  // Uruk-hai Sword: [isengard] hand weapon, cost 1, bearer an Uruk-hai; a trigger on bearer's win
					put("opportunity", "12_159"); // Weapon of Opportunity: [uruk-hai] hand weapon, laid only in the Free Peoples player's discard pile

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// An [uruk-hai] lurker and an [orc] lurker, with an [uruk-hai] hand weapon in the Shadow player's discard pile.
	protected VirtualTableScenario GetTwoLurkersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("machine", "13_176");
					put("crusher", "12_140");
					put("emboldened", "11_119");
					put("spear", "17_117");
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WarMachineStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: War Machine
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Exert your [uruk-hai] lurker minion to play an [uruk-hai] hand weapon from your discard pile on your [uruk-hai] minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("machine");

		assertEquals("War Machine", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	// "Exert your [uruk-hai] lurker minion": the definition took any lurker minion of the player's.
	@Test
	public void TheLurkerExertedIsAnUrukHaiCultureOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTwoLurkersScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var machine = scn.GetShadowCard("machine");
		var crusher = scn.GetShadowCard("crusher");
		var emboldened = scn.GetShadowCard("emboldened");
		var spear = scn.GetShadowCard("spear");
		scn.MoveCardsToHand(machine);
		scn.MoveCardsToDiscard(spear);

		scn.StartGame();
		assertInDiscard(spear);
		scn.MoveMinionsToTable(crusher, emboldened);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, crusher);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(machine));
		scn.ShadowPlayCard(machine);

		// Crushing Uruk is the only [uruk-hai] lurker: he exerts with nothing asked, and Emboldened Orc, an [orc]
		// lurker, is left alone.  One weapon in the discard pile and one [uruk-hai] minion to bear it.
		assertEquals(1, scn.GetWoundsOn(crusher));
		assertEquals(0, scn.GetWoundsOn(emboldened));
		assertAttachedTo(spear, crusher);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ExertsAnUrukHaiLurkerToPlayAnUrukHaiHandWeaponFromTheDiscardPileOnAnUrukHaiMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var opportunity = scn.GetFreepsCard("opportunity");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToDiscard(opportunity);

		var machine = scn.GetShadowCard("machine");
		var entranced = scn.GetShadowCard("entranced");
		var saruman = scn.GetShadowCard("saruman");
		var commander = scn.GetShadowCard("commander");
		var prowler = scn.GetShadowCard("prowler");
		var spear = scn.GetShadowCard("spear");
		var sword = scn.GetShadowCard("sword");
		var bow = scn.GetShadowCard("bow");
		var uruksword = scn.GetShadowCard("uruksword");
		scn.MoveCardsToHand(machine);
		scn.MoveCardsToDiscard(spear, sword, bow, uruksword);

		scn.StartGame();
		assertInDiscard(opportunity, spear, sword, bow, uruksword);
		scn.MoveMinionsToTable(entranced, saruman, commander, prowler);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, commander);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(machine));
		scn.ShadowPlayCard(machine);

		// The lurkers of the culture, an Uruk-hai and a Wizard; not the [uruk-hai] minion who is no lurker, nor the [orc] lurker.
		assertTrue(scn.ShadowHasCardChoiceAvailable(entranced, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(commander, prowler));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(0, scn.GetWoundsOn(saruman));
		scn.ShadowChooseCard(saruman);
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(entranced));

		// [uruk-hai] hand weapons from his own pile: not the ranged weapon, the [isengard] hand weapon, or the
		// Free Peoples player's Weapon of Opportunity, a title his own pile does not hold.
		assertTrue(scn.ShadowDecisionAvailable("Choose card from discard"));
		assertTrue(scn.ShadowHasCardChoiceAvailable(spear, sword));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bow, uruksword, opportunity));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(sword);

		// Any [uruk-hai] minion of his, the lurker just exerted and the Wizard included; not the [orc] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(entranced, saruman, commander));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(prowler));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(commander);

		assertAttachedTo(sword, commander);
		assertInDiscard(machine, spear, bow, uruksword);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWithItsOnlyUrukHaiLurkerExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var machine = scn.GetShadowCard("machine");
		var entranced = scn.GetShadowCard("entranced");
		var commander = scn.GetShadowCard("commander");
		var prowler = scn.GetShadowCard("prowler");
		var spear = scn.GetShadowCard("spear");
		scn.MoveCardsToHand(machine);
		scn.MoveCardsToDiscard(spear);

		scn.StartGame();
		assertInDiscard(spear);
		scn.MoveMinionsToTable(entranced, commander, prowler);
		// Vitality 2 with 1 wound: exhausted.  The Commander (no lurker) and the Prowling Orc ([orc]) could exert.
		scn.AddWoundsToChar(entranced, 1);
		assertEquals(1, scn.GetWoundsOn(entranced));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, commander);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(machine));
	}

	@Test
	public void CannotBePlayedWithNoUrukHaiHandWeaponInHisOwnDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var freepsSpear = scn.GetFreepsCard("spear");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToDiscard(freepsSpear);

		var machine = scn.GetShadowCard("machine");
		var entranced = scn.GetShadowCard("entranced");
		var commander = scn.GetShadowCard("commander");
		var spear = scn.GetShadowCard("spear");
		var bow = scn.GetShadowCard("bow");
		var uruksword = scn.GetShadowCard("uruksword");
		scn.MoveCardsToHand(machine);
		scn.MoveCardsToDiscard(bow, uruksword);

		scn.StartGame();
		assertInDiscard(freepsSpear, bow, uruksword);
		scn.MoveMinionsToTable(entranced, commander);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, commander);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// The play from the discard pile is a cost (Q-0062): nothing in his own pile can pay it.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(machine));

		// A Spear of his own in his pile, and it can be played.
		scn.MoveCardsToDiscard(spear);
		assertTrue(scn.ShadowPlayAvailable(machine));
	}

	@Test
	public void CannotBePlayedWithoutTheTwilightForTheWeaponAsWell() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var machine = scn.GetShadowCard("machine");
		var entranced = scn.GetShadowCard("entranced");
		var commander = scn.GetShadowCard("commander");
		var sword = scn.GetShadowCard("sword");
		scn.MoveCardsToHand(machine);
		scn.MoveCardsToDiscard(sword);

		scn.StartGame();
		assertInDiscard(sword);
		scn.MoveMinionsToTable(entranced, commander);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, commander);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// 1 for the event + 1 for the Isengard Sword, the only weapon there.
		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(machine));

		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(machine));
	}
}
