package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertAttachedTo;
import static com.gempukku.lotro.framework.Assertions.assertInHand;
import static org.junit.Assert.*;

public class Card_05_012_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sword", "5_12");
					// Legolas, Companion of the Ring: Elf companion, vitality 3, no archer (an archer stops the
					// skippers); +2 strength and resistance only at a battleground or forest
					put("legolas", "11_21");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an Elf companion who is not Legolas; Ranger, +3 only against a Nazgul
					put("gimli", "1_13");       // Gimli, Son of Gloin: a companion who is no Elf; a skirmish ability only

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("ted", "18_75");        // Ted Sandyman: [men] Hobbit minion, vitality 4; maneuver and regroup abilities used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetThreeMoveScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sword", "5_12");
					put("legolas", "11_21");    // Legolas, Companion of the Ring: no archer; +2 strength and resistance at a forest
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: the ranger the Waterfall spots; a maneuver ability only

					put("ted", "18_75");        // Ted Sandyman: [men] Hobbit minion, vitality 4; maneuver and regroup abilities used only on purpose
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					// Rivendell Waterfall: site 3, "While you can spot a ranger at Rivendell Waterfall, the move limit
					// is +1 for this turn"
					put("site3", "1_342");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LegolasSwordStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Legolas' Sword
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +1
		 * Game Text: Bearer must be Legolas.
		 * Each time the fellowship moves, you may spot a wounded minion to heal Legolas.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sword");

		assertEquals("Legolas' Sword", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getPossessionClasses().size());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void CanBePlayedOnlyOnceLegolasIsInPlayAndGoesOnHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(sword, legolas);
		scn.MoveCompanionsToTable(arwen, gimli);

		scn.StartGame();

		// An Elf companion who is not Legolas, a Dwarf and Frodo, all of whom could bear a hand weapon: no bearer.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(sword));

		scn.FreepsPlayCard(legolas);
		assertTrue(scn.FreepsPlayAvailable(sword));

		// Legolas is the one bearer it may have: it goes on him with nothing asked.
		scn.FreepsPlayCard(sword);
		assertAttachedTo(sword, legolas);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheRegroupMoveWithAWoundedMinionHealsLegolasAndNoOneElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(legolas, arwen);
		scn.AttachCardsTo(legolas, sword);
		scn.AddWoundsToChar(legolas, 1);
		scn.AddWoundsToChar(arwen, 1);

		var troop = scn.GetShadowCard("troop");
		var ted = scn.GetShadowCard("ted");

		scn.StartGame();
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(arwen));

		// The move that ends the fellowship phase: Legolas is wounded, and no minion is in play.
		scn.FreepsPass();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(sword));
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// One wounded [moria] Orc and an unwounded minion beside it.
		scn.MoveMinionsToTable(troop, ted);
		scn.AddWoundsToChar(troop, 1);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(ted));
		scn.SkipToMovementDecision();
		assertEquals(1, scn.GetWoundsOn(legolas));

		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sword));
		scn.FreepsAcceptOptionalTrigger();

		// Legolas is healed; the other wounded companion and the minions keep their wounds.
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheHealMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);
		scn.AttachCardsTo(legolas, sword);
		scn.AddWoundsToChar(legolas, 1);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertEquals(1, scn.GetWoundsOn(legolas));
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop);
		scn.AddWoundsToChar(troop, 1);
		assertEquals(1, scn.GetWoundsOn(troop));
		scn.SkipToMovementDecision();

		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sword));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoWoundedMinionToSpotTheMoveOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(legolas, arwen);
		scn.AttachCardsTo(legolas, sword);
		scn.AddWoundsToChar(legolas, 1);
		// A wounded character who is no minion.
		scn.AddWoundsToChar(arwen, 1);

		var troop = scn.GetShadowCard("troop");
		var ted = scn.GetShadowCard("ted");

		scn.StartGame();
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(arwen));
		scn.FreepsPass();
		// Two minions, neither wounded.
		scn.MoveMinionsToTable(troop, ted);
		scn.SkipToMovementDecision();
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(ted));

		scn.FreepsChooseToMove();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(sword));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithLegolasUnwoundedTheMoveOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(legolas, arwen);
		scn.AttachCardsTo(legolas, sword);
		// Another companion is wounded, but only Legolas is healed, so the heal would do nothing.
		scn.AddWoundsToChar(arwen, 1);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertEquals(1, scn.GetWoundsOn(arwen));
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop);
		scn.AddWoundsToChar(troop, 1);
		assertEquals(1, scn.GetWoundsOn(troop));
		scn.SkipToMovementDecision();
		assertEquals(0, scn.GetWoundsOn(legolas));

		scn.FreepsChooseToMove();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(sword));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void StayingAtTheRegroupDecisionOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);
		scn.AttachCardsTo(legolas, sword);
		scn.AddWoundsToChar(legolas, 1);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertEquals(1, scn.GetWoundsOn(legolas));
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop);
		scn.AddWoundsToChar(troop, 1);
		assertEquals(1, scn.GetWoundsOn(troop));
		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(sword));

		// The fellowship stays where it is: the turn ends with nothing offered, and the other player's begins.
		scn.FreepsChooseToStay();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(sword));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void EachMoveOfATurnWithAWoundedMinionHealsLegolasAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetThreeMoveScenario();

		var sword = scn.GetFreepsCard("sword");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(legolas, aragorn);
		scn.AttachCardsTo(legolas, sword);
		scn.AddWoundsToChar(legolas, 2);

		var ted = scn.GetShadowCard("ted");

		scn.StartGame();
		assertEquals(2, scn.GetWoundsOn(legolas));
		scn.FreepsPass();
		// A wounded [men] Hobbit minion this time.
		scn.MoveMinionsToTable(ted);
		scn.AddWoundsToChar(ted, 1);
		assertEquals(1, scn.GetWoundsOn(ted));
		scn.SkipToMovementDecision();

		// The move from site 2 to Rivendell Waterfall.
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sword));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// With a ranger at the Waterfall the fellowship may move a third time, and the wounded minion is there.
		assertEquals(3, scn.GetMoveLimit());
		scn.SkipToMovementDecision();
		assertEquals(1, scn.GetWoundsOn(ted));
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sword));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(legolas));

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
