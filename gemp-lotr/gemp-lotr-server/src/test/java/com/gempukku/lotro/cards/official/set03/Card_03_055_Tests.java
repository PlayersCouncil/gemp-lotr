package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_055_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("axe", "3_55");
					put("axe2", "3_55");
					// Banner of Isengard: [isengard] hand weapon, cost 0, bearer must be an [isengard] minion; its
					// trigger answers its bearer winning a skirmish
					put("banner", "6_59");

					put("worker", "3_62");      // Isengard Worker: [isengard] Orc, a Regroup ability used only on purpose
					put("servant", "3_58");     // Isengard Servant: [isengard] Orc of another title, a Regroup ability used only on purpose
					put("savage", "1_151");     // Uruk Savage: an [isengard] minion who is no Orc (Uruk-hai), damage +1 and nothing else
					put("troop", "1_177");      // Goblin Patrol Troop: an Orc who is not [isengard] ([moria]), no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IsengardAxeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Isengard Axe
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +2
		 * Game Text: Bearer must be an [isengard] Orc.<br>When you play this weapon, add (1).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("axe");

		assertEquals("Isengard Axe", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnAnIsengardOrcAndAddsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetShadowCard("axe");
		var worker = scn.GetShadowCard("worker");
		var servant = scn.GetShadowCard("servant");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(axe);

		scn.StartGame();
		scn.MoveMinionsToTable(worker, servant, savage, troop);
		scn.FreepsPass();

		// 2 for Ettenmoors + 1 for Frodo
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(axe));

		scn.ShadowPlayCard(axe);
		// Two [isengard] Orcs of two titles; not the [isengard] Uruk-hai, nor the [moria] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(worker, servant));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(worker);

		assertAttachedTo(axe, worker);
		// 3 + 0 to play + 1 added, unasked
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASecondAxeAddsOneForItselfAndAnotherWeaponPlayedAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetShadowCard("axe");
		var axe2 = scn.GetShadowCard("axe2");
		var banner = scn.GetShadowCard("banner");
		var worker = scn.GetShadowCard("worker");
		var servant = scn.GetShadowCard("servant");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(axe2, banner);

		scn.StartGame();
		scn.MoveMinionsToTable(worker, servant, savage);
		scn.AttachCardsTo(worker, axe);
		assertAttachedTo(axe, worker);
		scn.FreepsPass();

		// 2 for Ettenmoors + 1 for Frodo: the Axe cheated into play added nothing.
		assertEquals(3, scn.GetTwilight());

		// The first Axe answers only its own play: the second adds its own (1) and no more.
		// The Worker bears a hand weapon already, so the Servant is the one bearer left, taken unasked.
		scn.ShadowPlayCard(axe2);
		assertAttachedTo(axe2, servant);
		assertEquals(4, scn.GetTwilight());

		// Another [isengard] hand weapon, cost 0: neither Axe answers it.
		scn.ShadowPlayCard(banner);
		assertAttachedTo(banner, savage);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
