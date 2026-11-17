package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_163_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("entranced", "13_163");
					put("tyrannical", "11_204"); // Tyrannical Uruk: [uruk-hai] Uruk-hai, strength 14; damage +1 and toil only
					put("murderous", "11_195");  // Murderous Uruk: [uruk-hai] Uruk-hai; damage +1 and muster (regroup) only
					put("spear1", "17_117");     // Spear of the White Hand: [uruk-hai] hand weapon with no text
					put("spear2", "17_117");
					put("spear3", "17_117");
					put("siegebow", "11_191");   // Isengard Siege Bow: [uruk-hai] ranged weapon; archer only at a battleground, and none is laid
					put("siegebow2", "11_191");
					put("sword", "1_160");       // Uruk-hai Sword: [isengard] hand weapon; its text answers a win, and no skirmish is won
					put("fury", "18_116");       // Fury of the Evil Army: [uruk-hai] CONDITION borne by an [uruk-hai] minion; a trigger on a win
					put("enemy", "13_162");      // Enemy Without Number: an [uruk-hai] condition in the support area, never used
					put("arwen", "1_30");        // Arwen, Daughter of Elrond: [elven] Elf companion, only spotted, for Seclusion
					put("seclusion", "12_23");   // Seclusion: Maneuver: spot an Elf companion to discard a condition from play
					put("lemenya", "7_214");     // Úlairë Lemenya: Regroup: if you have initiative, discard her to discard a possession
					// Free Peoples cards that discard a possession: a character, an item and an event
					put("hama", "6_94");         // Háma, Doorward of Théoden: Skirmish: exert Háma to discard a possession borne by a minion he is skirmishing
					put("throne", "17_36");      // Throne of Minas Tirith: [gondor] artifact; Skirmish: remove 3 [gondor] tokens to discard a possession
					put("gandalf", "1_72");      // Gandalf, Friend of the Shirefolk: only spotted, for Roll of Thunder
					put("thunder", "4_99");      // Roll of Thunder: Fellowship or Maneuver: spot Gandalf to discard a Shadow possession or Shadow artifact
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EntrancedUrukStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Entranced Uruk
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>. <b>Lurker</b>. <helper>(Skirmishes involving lurker minions must be resolved after any others.)</helper><br>Possessions borne by this minion cannot be discarded by Free Peoples cards.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("entranced");

		assertEquals("Entranced Uruk", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(card, Keyword.LURKER));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ItsPossessionCannotBeDiscardedByAFreePeoplesItemOrCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hama = scn.GetFreepsCard("hama");
		var throne = scn.GetFreepsCard("throne");
		scn.MoveCompanionsToTable(hama);
		scn.MoveCardsToSupportArea(throne);

		var entranced = scn.GetShadowCard("entranced");
		var tyrannical = scn.GetShadowCard("tyrannical");
		var murderous = scn.GetShadowCard("murderous");
		var spear2 = scn.GetShadowCard("spear2");
		var spear3 = scn.GetShadowCard("spear3");
		var siegebow = scn.GetShadowCard("siegebow");
		var siegebow2 = scn.GetShadowCard("siegebow2");
		var sword = scn.GetShadowCard("sword");

		scn.StartGame();
		scn.AddTokensToCard(throne, 3);
		assertEquals(3, scn.GetCultureTokensOn(throne));
		scn.MoveMinionsToTable(entranced, tyrannical, murderous);
		// Two possessions of the Entranced Uruk that differ in item class and culture: neither may be offered.
		scn.AttachCardsTo(entranced, sword, siegebow);
		scn.AttachCardsTo(tyrannical, spear2, siegebow2);
		scn.AttachCardsTo(murderous, spear3);
		assertAttachedTo(sword, entranced);
		assertAttachedTo(siegebow, entranced);
		assertAttachedTo(spear2, tyrannical);
		assertAttachedTo(siegebow2, tyrannical);
		assertAttachedTo(spear3, murderous);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(hama, entranced);
		scn.ShadowAssignToMinions(hama, tyrannical, murderous);
		scn.FreepsResolveSkirmish(hama);

		// The item: the other minions' possessions are offered, the Entranced Uruk's is not.
		assertTrue(scn.FreepsActionAvailable(throne));
		scn.FreepsUseCardAction(throne);
		assertEquals(0, scn.GetCultureTokensOn(throne));
		assertTrue(scn.FreepsHasCardChoiceAvailable(spear2, siegebow2, spear3));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sword, siegebow));
		scn.FreepsChooseCard(siegebow2);
		assertInDiscard(siegebow2);
		assertAttachedTo(sword, entranced);
		assertAttachedTo(siegebow, entranced);

		scn.ShadowPass();
		// The character: the same.
		assertEquals(0, scn.GetWoundsOn(hama));
		scn.FreepsUseCardAction(hama);
		assertEquals(1, scn.GetWoundsOn(hama));
		assertTrue(scn.FreepsHasCardChoiceAvailable(spear2, spear3));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sword, siegebow));
		scn.FreepsChooseCard(spear2);
		assertInDiscard(spear2);
		assertAttachedTo(sword, entranced);
		assertAttachedTo(siegebow, entranced);
		assertAttachedTo(spear3, murderous);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ItsPossessionCannotBeDiscardedByAFreePeoplesEvent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var thunder = scn.GetFreepsCard("thunder");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(thunder);

		var entranced = scn.GetShadowCard("entranced");
		var tyrannical = scn.GetShadowCard("tyrannical");
		var spear1 = scn.GetShadowCard("spear1");
		var spear2 = scn.GetShadowCard("spear2");
		var siegebow = scn.GetShadowCard("siegebow");

		scn.StartGame();
		scn.MoveMinionsToTable(entranced, tyrannical);
		scn.AttachCardsTo(entranced, spear1);
		scn.AttachCardsTo(tyrannical, spear2, siegebow);
		assertAttachedTo(spear1, entranced);
		assertAttachedTo(spear2, tyrannical);
		assertAttachedTo(siegebow, tyrannical);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(thunder);
		assertTrue(scn.FreepsHasCardChoiceAvailable(spear2, siegebow));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(spear1));
		scn.FreepsChooseCard(spear2);
		assertInDiscard(spear2);
		assertAttachedTo(spear1, entranced);
		assertAttachedTo(siegebow, tyrannical);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AConditionBorneByItCanBeDiscardedByAFreePeoplesCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var seclusion = scn.GetFreepsCard("seclusion");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(seclusion);

		var entranced = scn.GetShadowCard("entranced");
		var spear1 = scn.GetShadowCard("spear1");
		var fury = scn.GetShadowCard("fury");
		var enemy = scn.GetShadowCard("enemy");
		scn.MoveCardsToSupportArea(enemy);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, enemy);
		scn.MoveMinionsToTable(entranced);
		scn.AttachCardsTo(entranced, spear1, fury);
		assertAttachedTo(spear1, entranced);
		assertAttachedTo(fury, entranced);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(seclusion);
		// Toil is asked even of one candidate: nobody is exerted, the card's cost is paid in the pool.
		scn.FreepsDeclineChoosing();
		// Only the possession is barred: a condition borne by the Uruk is offered beside one in the support area.
		assertTrue(scn.FreepsHasCardChoiceAvailable(fury, enemy));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(spear1));
		scn.FreepsChooseCard(fury);
		assertInDiscard(fury);
		assertAttachedTo(spear1, entranced);
		assertInZone(Zone.SUPPORT, enemy);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ItsPossessionCanBeDiscardedByAShadowCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var entranced = scn.GetShadowCard("entranced");
		var tyrannical = scn.GetShadowCard("tyrannical");
		var lemenya = scn.GetShadowCard("lemenya");
		var spear1 = scn.GetShadowCard("spear1");
		var spear2 = scn.GetShadowCard("spear2");

		scn.StartGame();
		scn.MoveMinionsToTable(entranced, tyrannical, lemenya);
		scn.AttachCardsTo(entranced, spear1);
		scn.AttachCardsTo(tyrannical, spear2);
		assertAttachedTo(spear1, entranced);
		assertAttachedTo(spear2, tyrannical);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		// The Free Peoples hand is empty: the Shadow player has initiative.
		assertTrue(scn.ShadowHasInitiative());
		assertTrue(scn.ShadowActionAvailable(lemenya));
		scn.ShadowUseCardAction(lemenya);
		assertInDiscard(lemenya);
		assertTrue(scn.ShadowHasCardChoiceAvailable(spear1, spear2));
		scn.ShadowChooseCard(spear1);
		assertInDiscard(spear1);
		assertAttachedTo(spear2, tyrannical);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
