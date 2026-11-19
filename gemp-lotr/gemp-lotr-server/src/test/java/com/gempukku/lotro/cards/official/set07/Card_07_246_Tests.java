package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_246_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rguard", "7_246");
					put("rguard2", "7_246");

					// Cards held in hand, of three types and two cultures: a [gondor] companion (Gondorian Knight), a
					// [shire] possession (Hobbit Sword) and a [shire] event (Sorry About Everything)
					put("h1", "5_35");
					put("h2", "1_299");
					put("h3", "1_312");

					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, strength 7, vitality 2; a skirmish ability used only on purpose
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 only
					put("slayer", "3_93");      // Morgul Slayer: [sauron] Orc, strength 7, vitality 2; a regroup ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RohirrimGuardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Rohirrim Guard
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Skirmish:</b> Discard a card from hand to exert a minion skirmishing this companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("rguard");

		assertEquals("Rohirrim Guard", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void DiscardsACardFromHandToExertAMinionSkirmishingHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rguard = scn.GetFreepsCard("rguard");
		var h1 = scn.GetFreepsCard("h1");
		var h2 = scn.GetFreepsCard("h2");
		var h3 = scn.GetFreepsCard("h3");
		scn.MoveCompanionsToTable(rguard);
		scn.MoveCardsToHand(h1, h2, h3);

		var soldier = scn.GetShadowCard("soldier");
		var savage = scn.GetShadowCard("savage");
		var slayer = scn.GetShadowCard("slayer");

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, savage, slayer);
		// Vitality 2 with 1 wound: exhausted, so it cannot be exerted.
		scn.AddWoundsToChar(slayer, 1);
		assertEquals(1, scn.GetWoundsOn(slayer));
		assertInHand(h1, h2, h3);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(rguard, soldier);
		// The Shadow player puts his other two minions on the Guard too.
		scn.ShadowAssignToMinions(rguard, savage, slayer);
		scn.FreepsResolveSkirmish(rguard);

		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsActionAvailable(rguard));
		scn.FreepsUseCardAction(rguard);

		// Any card: a companion, a possession and an event, of two cultures, are all offered; exactly one.
		assertTrue(scn.FreepsHasCardChoiceAvailable(h1, h2, h3));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(h2);
		assertInDiscard(h2);
		assertInHand(h1, h3);

		// An Orc and an Uruk-hai of two cultures skirmishing him; not the exhausted Slayer, who cannot exert.
		assertTrue(scn.FreepsHasCardChoiceAvailable(soldier, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(slayer));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);

		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(1, scn.GetWoundsOn(slayer));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void UsedInAnotherCopysSkirmishItExertsNoMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rguard = scn.GetFreepsCard("rguard");
		var rguard2 = scn.GetFreepsCard("rguard2");
		var h1 = scn.GetFreepsCard("h1");
		scn.MoveCompanionsToTable(rguard, rguard2);
		scn.MoveCardsToHand(h1);

		var soldier = scn.GetShadowCard("soldier");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, savage);
		assertInHand(h1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{rguard, savage}, new PhysicalCardImpl[]{rguard2, soldier});

		// The second copy's skirmish first; the Savage waits on this one, whose own skirmish has not begun.
		scn.FreepsResolveSkirmish(rguard2);
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(savage));
		// The cost can be paid, so the ability is offered there; the effect finds nobody skirmishing this Guard.
		assertTrue(scn.FreepsActionAvailable(rguard));
		scn.FreepsUseCardAction(rguard);

		assertInDiscard(h1);
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedWithAnEmptyHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rguard = scn.GetFreepsCard("rguard");
		scn.MoveCompanionsToTable(rguard);

		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(rguard, soldier);
		scn.FreepsResolveSkirmish(rguard);

		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(rguard));
		assertEquals(0, scn.GetWoundsOn(soldier));
	}
}
